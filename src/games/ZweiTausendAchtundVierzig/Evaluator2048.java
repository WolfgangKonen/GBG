package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgent;
import controllers.MC.MCAgentN;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import games.Arena;
import games.PStats;
import games.ZweiTausendAchtundVierzig.Heuristic.Evaluator2048_EA;
import tools.Types;
import tools.Types.ACTIONS_VT;

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
 * {@link Arena2048#makeEvaluator(PlayAgent, GameBoard, int, int, int) Arena[Train]2048.makeEvaluator(...)}.
 * <p>
 * Created by Johannes Kutsch, TH Koeln, 2016-12.
 * 
 * @see Evaluator2048_BoardPositions
 * @see Evaluator2048_EA
 */
public class Evaluator2048 extends Evaluator {
    private ExecutorService executorService = Executors.newFixedThreadPool(6);

    private double medianScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = Integer.MIN_VALUE;
    private List<Integer> scores = new ArrayList<>();
    private double standarddeviation;
    private double averageRolloutDepth;
    private TreeMap<Integer, Integer> tiles = new TreeMap<Integer, Integer>();
    private int moves = 0;
//	private int m_mode;			// now in Evaluator
    private long startTime;
    private long stopTime;
    private int verbose;
    private Arena ar;		// needed in eval_agent, if PStats.printPlayStats(psList, m_PlayAgent,ar) is called


    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose, Arena ar) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
        this.verbose = verbose;
        this.ar = ar;
    }

    @Override
    protected boolean evalAgent(PlayAgent pa) {
    	m_PlayAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
            return true;
        }
		int cumEmpty=0;
		// --- this part now in makePStats ---
//		int nEmpty = 0;
//		int moveNum=0;
//		double gameScore=0.0;
//		PStats pstats;
		ArrayList<PStats> psList = new ArrayList<PStats>();
        startTime = System.currentTimeMillis();
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
                    playAgent = new MCTSExpectimaxAgt("MCTS Expectimax", mctsExpectimaxAgt.params);
 
                    while (!so.isGameOver()) {
                        Types.ACTIONS action = playAgent.getNextAction2(so, false, true);
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

        } else if (	m_PlayAgent.getName().equals("ExpectimaxWrapper") ) {
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
                        Types.ACTIONS action = playAgent.getNextAction2(so, false, true);
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
                    long gameMoveTime = System.currentTimeMillis();
                    actBest = m_PlayAgent.getNextAction2(so2048, false, true);
                    so2048.advance(actBest);
//                  System.out.print("Finished move " + (so.moves) + " with score " + so.score + " after " + (System.currentTimeMillis() - gameMoveTime) + "ms.\n");
                    
                    // gather information for later printout to agents/gameName/csv/playStats.csv:
                    if (ConfigEvaluator.PLAYSTATS_CSV) {
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
            if (ConfigEvaluator.PLAYSTATS_CSV) {
            	PlayAgent[] paVector = {m_PlayAgent};
        		PStats.printPlayStats(psList, null, paVector,this.ar);
        									//we do not hand over a startSO, since psList may have many runs, 
        									//each with a different startSO.
            }
            
        } // else

        //evaluate games
        //Min/Max/Average Score
        for(StateObserver2048 so : stateObservers) {
            Integer value = tiles.get(so.getHighestTileValue());
            if (value == null) {
                tiles.put(so.getHighestTileValue(), 1);
            } else {
                tiles.put(so.getHighestTileValue(), value + 1);
            }

            scores.add(so.score);

            lastResult += so.score;
            if (so.score < minScore) {
                minScore = so.score;
            }
            if (so.score > maxScore) {
                maxScore = so.score;
            }

            moves += so.getMoveCounter();
        }

        lastResult/= ConfigEvaluator.NUMBEREVALUATIONS;

        //Standard deviation
        for(int score : scores) {
            standarddeviation += (score-lastResult)*(score-lastResult);
        }
        standarddeviation/= ConfigEvaluator.NUMBEREVALUATIONS;
        standarddeviation = Math.sqrt(standarddeviation);

        //Median Score
        Collections.sort(scores);
        int mid = (int) (scores.size()/2);
        if (scores.size()%2==0) {
        	medianScore = (scores.get(mid-1)+scores.get(mid))/2;        	
        } else {
        	medianScore = scores.get(mid);
        }

        averageRolloutDepth/=moves;

        stopTime = System.currentTimeMillis();

        //System.out.print("\n");
        if (verbose==1) System.out.println("Finished evaluation of "+ConfigEvaluator.NUMBEREVALUATIONS+" games with average score "
        		+ Math.round(lastResult) + " +- " + Math.round(standarddeviation/Math.sqrt(ConfigEvaluator.NUMBEREVALUATIONS)));

        return lastResult > ConfigEvaluator.MINPOINTS;
    }

 	// --- implemented by Evaluator ---
//  @Override
//  public double getLastResult() {
//      return lastResult;
//  }
    
    private PStats makePStats2048(int i, StateObserver2048 so2048, ACTIONS_VT actBest, int cumEmpty) {
    	int moveNum = so2048.getMoveCounter();
    	int actNum = (actBest==null) ? (-1) : actBest.toInt();
     	int nEmpty = so2048.getNumEmptyTiles();  
    	double gameScore = so2048.getGameScore(so2048)*so2048.MAXSCORE;
    	cumEmpty += nEmpty;
    	int highestTile = so2048.getHighestTileValue();
		PStats pstats = new PStats(i, moveNum, so2048.getPlayer(), actNum, gameScore, (double) nEmpty, (double) cumEmpty, highestTile);
		return pstats;
    }

    @Override
    public String getMsg() {
        String tilesString = "";
		DecimalFormat frm = new DecimalFormat("00000");
		DecimalFormat frm1 = new DecimalFormat("000");
		DecimalFormat frm2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		frm2.applyPattern("#0.0%");  

        for (Map.Entry tile : tiles.entrySet()) {
            tilesString += "\n" + frm.format(((Integer)tile.getKey()).longValue()) 
            	+ ", " + frm1.format(((Integer)tile.getValue()).longValue())
            	+ "  ("+ frm2.format(((Integer)tile.getValue()).doubleValue()/ConfigEvaluator.NUMBEREVALUATIONS)+")";
        }

        long duration = (stopTime - startTime);
        if(duration == 0) {
            duration = 1;
        }

        String agentSettings = "";

        if(m_PlayAgent.getName() == "MC") {
            MCAgentN mcAgent = (MCAgentN)m_PlayAgent;
            agentSettings = "\nROLLOUTDEPTH: " + mcAgent.getParMC().getRolloutDepth() +
                    "\nITERATIONS: " + mcAgent.getParMC().getNumIter() +
                    "\nNUMBERAGENTS: " + mcAgent.getParMC().getNumAgents();
        } else if(m_PlayAgent.getName() == "MCTS Expectimax") {
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
                "\nAverage score is: " + Math.round(lastResult) + 
                " +- " + Math.round(standarddeviation/Math.sqrt(ConfigEvaluator.NUMBEREVALUATIONS)) +
                "\nMedian score is: " + Math.round(medianScore) +
                "\nHighest score is: " + maxScore +
                "\nAverage game duration: " +  Math.round((double)duration / (double)ConfigEvaluator.NUMBEREVALUATIONS) + "ms" +
//              "\nDuration of evaluation: " + Math.round((double)duration/(double)1000) + "s" +
                "\nMoves per second: " + Math.round(moves/((double)duration/(double)1000)) +
                "\n" +
                "\nHighest tiles: " +
                tilesString +
                "\n\n";
    }
    
    @Override
    public String getShortMsg() {
        return "Quick Evaluation of "+ m_PlayAgent.getName() +": Average score "+ Math.round(lastResult);    	
    }

 	// --- implemented by Evaluator ---
//    @Override
//    public boolean isAvailableMode(int mode) {
//        switch (mode) {
//        	case -1: 
//        	case  0:
//            case  1:
//            case  2:
//                return true;
//            default:
//                return false;
//        }
//    }

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

//    @Override
//    public int getMultiTrainEvalMode() {
//        return 0;
//    }

    @Override
    public String getPrintString() {
        return "average score";
    }

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: avg score from 50 games<br>"
				+ "1: Evaluator2048_BoardPositions<br>"
				+ "2: Evaluator2048_EA<br>"
				+ "</html>";
	}

    @Override
    public String getPlotTitle() {
        return "average score";
    }
}