package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgentN;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import games.Arena;
import tools.PStats;
import games.ZweiTausendAchtundVierzig.Heuristic.Evaluator2048_EA;
import tools.Types;
import tools.Types.ACTIONS_VT;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base evaluator for 2048: average score from playing 50 episodes.
 * <p>
 * Note that the mode-selection for 2048 evaluators is done in 
 * {@link Arena2048#makeEvaluator(PlayAgent, GameBoard, int, int, int) Arena[Train]2048.makeEvaluator(...)}. <br>
 * This class {@link Evaluator2048} is only called for {@code m_mode==-1} (just return true) and {@code m_mode==0}.
 * <p>
 * Created by Johannes Kutsch, TH Koeln, 2016. Adapted by Wolfgang Konen, 2018-2020.
 * 
 * @see Evaluator2048_BoardPositions
 * @see Evaluator2048_EA
 */
public class Evaluator2048 extends Evaluator {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private double medianScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = Integer.MIN_VALUE;
    private double standarddeviation;
    private long avgGameDur;
    private double duration;
    private double avgMovPerGame;
    private double movesPerSec;
    private int moves = 0;
    private final List<Integer> scores = new ArrayList<>();
    private final TreeMap<Integer, Integer> tiles = new TreeMap<>();
    private final int verbose;
    private final Arena ar;		// needed in eval_agent, if PStats.printPlayStats(psList, m_PlayAgent,ar) is called
    						// and needed in printEResultList
    
    public EResult eResult; // holds 2048 evaluation results in one object. EResult is a nested class of this.


    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose, Arena ar) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
        this.verbose = verbose;
        this.ar = ar;
    }

    @Override
    public boolean evalAgent(PlayAgent pa) {
    	m_PlayAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
            return true;
        }
		int cumEmpty;
		ArrayList<PStats> psList = new ArrayList<>();
        //	private int m_mode;			// now in Evaluator
        long startTime = System.currentTimeMillis();
        if(verbose == 0) {
            System.out.println("Starting evaluation of " + ConfigEvaluator.NUMBEREVALUATIONS 
            		+ " games, this may take a while...");
            System.out.println("   " + pa.stringDescr() + "\n   " + pa.stringDescr2());
        }
        List<StateObserver2048> stateObservers = new ArrayList<>();


        if(	m_PlayAgent.getName().equals("MCTS Expectimax") ) {
            //async for MCTS Expectimax agents 
            List<Callable<StateObserver2048>> callables = new ArrayList<>();
            MCTSExpectimaxAgt mctsExpectimaxAgt = (MCTSExpectimaxAgt) m_PlayAgent;

        	if (verbose == 0) 
        		System.out.println("Detected MCTS Expectimax Agent, iterations: " + mctsExpectimaxAgt.params.getNumIter() + ", rolloutdepth: " + mctsExpectimaxAgt.params.getRolloutDepth() + ", Treedepth: " + mctsExpectimaxAgt.params.getTreeDepth() + ", k: " + mctsExpectimaxAgt.params.getK_UCT() + ", maxnodes: " + mctsExpectimaxAgt.params.getMaxNodes() + ", alternative version: " + mctsExpectimaxAgt.params.getAlternateVersion());

            //play Games
            for(int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
                int gameNumber = i+1;
                callables.add(() -> {
                    StateObserver2048 so = new StateObserver2048();
                    PlayAgent playAgent;
                    long gameStartTime = System.currentTimeMillis();

                    // we need a new agent for every eval thread, since MCTSExpectimaxAgt is
                    // not thread-safe in its method getNextAction2:
                    playAgent = new MCTSExpectimaxAgt("MCTS Expectimax", mctsExpectimaxAgt.params, pa.getParOther());

                    while (!so.isGameOver()) {
                        Types.ACTIONS action = playAgent.getNextAction2(so.partialState(), false, true);
                        so.advance(action);
                        
                        // TODO: integrate a branch for ConfigEvaluator.PLAYSTATS_CSV, if needed
                    }

                    if(verbose == 0) {
                        System.out.print("Finished game " + gameNumber + " with scores " + so.score + 
                        		" after " + (System.currentTimeMillis() - gameStartTime) + "ms."+
                        		" Highest tile is " + so.getHighestTileValue() + ".\n");
                    }
                    return so;
                });
            }

            //save final gameState
            try {
                executorService.invokeAll(callables).stream().map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }).forEach(stateObservers::add);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (	m_PlayAgent.getName().equals("ExpectimaxWrapper") || m_PlayAgent.getName().equals("Expectimax2Wrapper") ) {
            //async for wrapper agents (ExpectiMaxN or MaxN tree nply)
            List<Callable<StateObserver2048>> callables = new ArrayList<>();

            //play Games
            for(int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
                int gameNumber = i+1;
                callables.add(() -> {
                    StateObserver2048 so = new StateObserver2048();
                    long gameStartTime = System.currentTimeMillis();

                    // we can use the same m_PlayAgent in every parallel eval thread, since 
                    // ExpectiMaxN's method getNextAction2 and the wrapped agent's method getScore* are 
                    // thread-safe:
                    PlayAgent playAgent = m_PlayAgent;

                    while (!so.isGameOver()) {
                        Types.ACTIONS action = playAgent.getNextAction2(so.partialState(), false, true);
                        so.advance(action);
                        
                        // TODO: integrate a branch for ConfigEvaluator.PLAYSTATS_CSV, if needed
                    }

                    if(verbose == 0) {
                        System.out.print("Finished game " + gameNumber + " with scores " + so.score 
                        		+ " after " + (System.currentTimeMillis() - gameStartTime) 
                        		+ "ms. Highest tile is " + so.getHighestTileValue() + ".\n");
                    }
                    return so;
                });
            }

            //save final gameState
            try {
                executorService.invokeAll(callables).stream().map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }).forEach(stateObservers::add);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            //sync for other Agents (MC Agent uses multiple cores naturally, TD agents are very fast)
            for (int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
                long gameStartTime = System.currentTimeMillis();
                cumEmpty=0;
                ACTIONS_VT actBest;
                StateObserver2048 so2048 = new StateObserver2048();
//              moveNum=0;
//        		pstats = new PStats(i, so2048.getMoveCounter(), so2048.getPlayer(), -1, gameScore, (double) nEmpty, (double) cumEmpty);
//        		psList.add(pstats);
        		psList.add(makePStats2048(i, so2048, null, cumEmpty));

                while (!so2048.isGameOver()) {
                    actBest = m_PlayAgent.getNextAction2(so2048.partialState(), false, true);
                    so2048.advance(actBest);
//                  System.out.print("Finished move " + (so.moves) + " with score " + so.score + " after " + (System.currentTimeMillis() - gameMoveTime) + "ms.\n");
                    
                    // gather information for later printout to agents/gameName/csv/playStats.csv:
                    if (Types.PLAYSTATS_WRITING) {   // OLD: ConfigEvaluator.PLAYSTATS_CSV) {
//                        moveNum++;
//                    	nEmpty = so2048.getNumEmptyTiles();  
//                    	gameScore = so2048.getGameScore(so2048)*so2048.MAXSCORE;
//                    	cumEmpty += nEmpty;
//                		pstats = new PStats(i, moveNum, so2048.getPlayer(), actBest.toInt(), gameScore, (double) nEmpty, (double) cumEmpty);
                		psList.add(makePStats2048(i, so2048, actBest, cumEmpty));
                    }
                }

                if(verbose == 0) {
                    System.out.print("Finished game " + (i + 1) + " with score " + so2048.score + 
                    		" after " + (System.currentTimeMillis() - gameStartTime) + "ms."+ 
                    		" Highest tile is " + so2048.getHighestTileValue() + ".\n");
                }

                stateObservers.add(so2048);
                
            }
            if (Types.PLAYSTATS_WRITING) {   // OLD: ConfigEvaluator.PLAYSTATS_CSV) {
            	PlayAgent[] paVector = {m_PlayAgent};
        		PStats.printPlayStats(psList, null, paVector,this.ar);
        									//we do not hand over a startSO, since psList may have many runs, 
        									//each with a different startSO.
            }
            
        } // else

        //evaluate games
        //Average Score
        for(StateObserver2048 so : stateObservers) {
            tiles.merge(so.getHighestTileValue(), 1, Integer::sum);

            scores.add(so.score);

            lastResult += so.score;

            moves += so.getMoveCounter();
        } // for (so)

        lastResult/= ConfigEvaluator.NUMBEREVALUATIONS;

        //Standard deviation of avgScore  
        //[Standard deviation of the scores is bigger by factor 1/sqrt(NUMBEREVALUATIONS).]
        for(int score : scores) {
            standarddeviation += (score-lastResult)*(score-lastResult);
        }
        standarddeviation/= (ConfigEvaluator.NUMBEREVALUATIONS-1);
        standarddeviation = Math.sqrt(standarddeviation);
        standarddeviation /= Math.sqrt(ConfigEvaluator.NUMBEREVALUATIONS);

        //Median, Min and Max Score
        Collections.sort(scores);
        int mid = scores.size()/2;
        if (scores.size()%2==0) {
        	medianScore = (double)(scores.get(mid-1)+scores.get(mid))/2;
        } else {
        	medianScore = scores.get(mid);
        }
        minScore = scores.get(0);
        maxScore = scores.get(scores.size()-1);

        long stopTime = System.currentTimeMillis();
        duration = (double)(stopTime - startTime)/1000.0;

        //System.out.print("\n");
        if (verbose==1) System.out.println("Finished evaluation of "+ConfigEvaluator.NUMBEREVALUATIONS+" games with average score "
        		+ Math.round(lastResult) + " +- " + Math.round(standarddeviation));
        
        int nPly = pa.getParOther().getWrapperNPly();
        long duration = (stopTime - startTime);
        avgGameDur = Math.round((double) duration / (double)ConfigEvaluator.NUMBEREVALUATIONS);
        avgMovPerGame =  Math.round((double)moves / (double)ConfigEvaluator.NUMBEREVALUATIONS);
        movesPerSec = Math.round(moves/((double) duration /(double)1000));
        
        this.eResult = new EResult(nPly, ConfigEvaluator.NUMBEREVALUATIONS, minScore, maxScore, lastResult, medianScore,
        		standarddeviation, avgGameDur, avgMovPerGame, movesPerSec, tiles);
        
        return lastResult > ConfigEvaluator.MINPOINTS;
    }

    private PStats makePStats2048(int i, StateObserver2048 so2048, ACTIONS_VT actBest, int cumEmpty) {
    	int moveNum = so2048.getMoveCounter();
    	int actNum = (actBest==null) ? (-1) : actBest.toInt();
     	int nEmpty = so2048.getNumEmptyTiles();  
    	double gameScore = so2048.getGameScore(so2048.getPlayer())*StateObserver2048.MAXSCORE;
    	cumEmpty += nEmpty;
    	int highestTile = so2048.getHighestTileValue();
        return  new PStats(i, moveNum, so2048.getPlayer(), actNum, gameScore, nEmpty, cumEmpty, highestTile);
    }

    @Override
    public String getMsg() {
        StringBuilder tilesString = new StringBuilder();
		DecimalFormat frm = new DecimalFormat("00000");
		DecimalFormat frm1 = new DecimalFormat("000");
		DecimalFormat frm2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		frm2.applyPattern("#0.0%");  

        for (Map.Entry<Integer, Integer> tile : tiles.entrySet()) {
            tilesString.append("\n").append(frm.format(tile.getKey().longValue()))
                    .append(", ").append(frm1.format(tile.getValue().longValue()))
                    .append("  (").append(frm2.format(tile.getValue().doubleValue() / ConfigEvaluator.NUMBEREVALUATIONS)).append(")");
        }

        String agentSettings = "";

        if(m_PlayAgent.getName().equals("MC-N")) {
            MCAgentN mcAgent = (MCAgentN)m_PlayAgent;
            agentSettings = "\nROLLOUTDEPTH: " + mcAgent.getParMC().getRolloutDepth() +
                    "\nITERATIONS: " + mcAgent.getParMC().getNumIter() +
                    "\nNUMBERAGENTS: " + mcAgent.getParMC().getNumAgents();
        } else if(m_PlayAgent.getName().equals("MCTS Expectimax")) {
            MCTSExpectimaxAgt mctsExpectimaxAgt = (MCTSExpectimaxAgt) m_PlayAgent;
            agentSettings = "\nROLLOUTDEPTH: " + mctsExpectimaxAgt.params.getRolloutDepth() +
                    "\nITERATIONS: " + mctsExpectimaxAgt.params.getNumIter() +
                    "\nMAXNODES:" + mctsExpectimaxAgt.params.getMaxNodes();
        }

        return "\n\nSettings:" +
                "\nAgent Name: " + m_PlayAgent.getName() +
                agentSettings +
                "\nNumber of games: " + ConfigEvaluator.NUMBEREVALUATIONS +
                "\n" +
                "\nResults:" +
                "\nLowest score is: " + minScore +
                "\nAverage score is: " + Math.round(lastResult) + " +- " + Math.round(standarddeviation) +
                "\nMedian score is: " + Math.round(medianScore) +
                "\nHighest score is: " + maxScore +
                "\nAverage game duration: " +  avgGameDur + "ms" +
                "\nDuration of evaluation: " + Math.round(duration) + "s" +
				"\nAverage moves per game: " +  avgMovPerGame + 
                "\nMoves per second: " + movesPerSec +
                "\n" +
                "\nHighest tiles: " +
                tilesString +
                "\n\n";
    }
    
    /**
     * This class collects 2048 evaluation results in one object. Such objects may be combined in
     * {@code ArrayList<EResult>} from several runs, for later inspection and printout
     *
     */
    public static class EResult {
    	public int nPly;			// nPly
    	public int numEval;			// number of evaluation games (episodes) for this nPly
    	public double lowScore;		// lowest score
    	public double higScore;		// highest score
    	public double avgScore;		// average score
    	public double medScore;		// median score
    	public double stdDevScore;	// standard deviation of the scores
    	public long avgGameDur;		// average game duration in ms
    	public double avgMovPerGame;// average number of moves per game
    	public double movesPerSec;	// average number of moves second 
    	public double t16384_Perc;  // percentage of evaluation runs where tile 16384 is highest tile
    	public TreeMap<Integer, Integer> tiles;
    	String sep = ", ";
    	
    	public EResult(int nPly, int numEval, double lowScore, double higScore, double avgScore, double medScore, 
    			double stdDevScore, long avgGameDur, double avgMovPerGame, double movesPerSec,  TreeMap<Integer, Integer> tiles) {
    		this.nPly=nPly;
    		this.numEval = numEval;
    		this.lowScore = lowScore;
    		this.higScore = higScore; 
    		this.avgScore = avgScore;
    		this.medScore = medScore; 
    		this.stdDevScore = stdDevScore;
    		this.avgGameDur = avgGameDur;
    		this.avgMovPerGame = avgMovPerGame;
    		this.movesPerSec = movesPerSec;
    		this.tiles = tiles;
    		this.t16384_Perc = 0.0;
    		
            for (Map.Entry<Integer,Integer> tile : tiles.entrySet()) {
            	if (tile.getKey()==16384) {
            		t16384_Perc = tile.getValue().doubleValue()/ConfigEvaluator.NUMBEREVALUATIONS;
            	}
            }


    	}
    	
    	public void print(PrintWriter mtWriter)  {
    		DecimalFormat frm0 = new DecimalFormat("#0000");
    		DecimalFormat df, df2;
    		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
    		df.applyPattern("#0000.0");  // now numbers formatted by df  appear with a decimal *point*
    		df2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
    		df2.applyPattern("#0.000");  // now numbers formatted by df2 appear with a decimal *point*

    		mtWriter.print(nPly + sep + numEval + sep);
    		mtWriter.println(frm0.format(lowScore) + sep + frm0.format(avgScore) + sep + frm0.format(stdDevScore)
    				+ sep + frm0.format(medScore) + sep + frm0.format(higScore) + sep + avgGameDur 
    				+ sep + df.format(avgMovPerGame) + sep + frm0.format(movesPerSec) + sep + df2.format(t16384_Perc));
    	}

    	public void printEResultList(String csvName, ArrayList<EResult> erList, PlayAgent pa, Arena ar,
    			String userTitle1, String userTitle2){
    		PrintWriter erWriter = null;
    		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+ar.getGameName();
    		String subDir = ar.getGameBoard().getSubDir();
    		if (subDir != null){
    			strDir += "/"+subDir;
    		}
    		strDir += "/csv";
    		tools.Utils.checkAndCreateFolder(strDir);

    		boolean retry=true;
    		BufferedReader bufIn=new BufferedReader(new InputStreamReader(System.in));
    		while (retry) {
    			try {
    				erWriter = new PrintWriter(new FileWriter(strDir+"/"+csvName,false));
    				retry = false;
    			} catch (IOException e) {
    				try {
    					// We may get here if file csvName is open in another application (e.g. Excel).
    					// Here we give the user the chance to close the file in the other application:
    				    System.out.print("*** Warning *** Could not open "+strDir+"/"+csvName+". Retry? (y/n): ");
    				    String s = bufIn.readLine();
    				    retry = (s.contains("y"));
    				} catch (IOException e2) {
    					e2.printStackTrace();					
    				}
    			}			
    		}
    		
    		if (erWriter!=null) {
    			erWriter.println(pa.stringDescr());		
    			erWriter.println(pa.stringDescr2());
    			
    			erWriter.println("nPly"+sep+"numEval"+sep+"lowScore"+sep+"avgScore"+sep+"stdDevScore"+sep
    					+"medScore"+sep+"higScore"+sep
    					+"avgGameDur"+sep+"avgMovPerGame"+sep+"movesPerSec"+sep+"tile16kPerc");
                for (EResult result : erList) {
                    result.print(erWriter);
                }

    		    erWriter.close();
    		} else {
    			System.out.print("*** Warning *** Could not write "+strDir+"/"+csvName+".");
    		}
    	}
    }
    
    @Override
    public String getShortMsg() {
        return "Quick Evaluation of "+ m_PlayAgent.getName() +": Average score "+ Math.round(lastResult);    	
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1,0, 1, 2};
    }

    @Override
    public int getQuickEvalMode() {
        return ConfigEvaluator.DEFAULTEVALUATOR;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        return "average score";
    }

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: avg score from "+ConfigEvaluator.NUMBEREVALUATIONS+" games<br>"
				+ "1: Evaluator2048_BoardPositions<br>"
				+ "2: Evaluator2048_EA<br>"
				+ "</html>";
	}

    @Override
    public String getPlotTitle() {
        return "average score";
    }
}