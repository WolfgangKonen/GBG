package games.ZweiTausendAchtundVierzig;

import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import games.EvalResult;
import games.Evaluator;
import games.GameBoard;
import games.Arena;
import tools.PStats;
import games.ZweiTausendAchtundVierzig.Heuristic.Evaluator2048_EA;
import tools.Types;
import tools.Types.ACTIONS_VT;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base evaluator for 2048: average score from playing 50 episodes.
 * <p>
 * Note that the mode-selection for 2048 evaluators is done in 
 * {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int) Arena[Train]2048.makeEvaluator(...)}. <br>
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
    /**
     * {@code tiles} holds key-value pairs &lt K,V &gt . It counts in its value V for key K how many evaluation episodes
     * end with a state having tile K as highest value.
     */
    private final TreeMap<Integer, Integer> tiles = new TreeMap<>();
    private final int verbose;
    private final Arena ar;		// needed in eval_agent, if PStats.printPlayStats(psList, m_PlayAgent,ar) is called
    						// and needed in printEResultList
    
    public EResult2048 eResult; // holds 2048 evaluation results in one object. EResult2048 is derived from EvalResult


    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose, Arena ar) {
        super(e_PlayAgent, gb, mode, verbose);
        this.verbose = verbose;
        this.ar = ar;
    }

    @Override
    public EvalResult evalAgent(PlayAgent pa) {
    	m_PlayAgent = pa;
        //Disable evaluation by using mode -1
        if (m_mode == -1) {
            m_msg = "no evaluation done ";
            lastResult = Double.NaN;
            return new EvalResult(Double.NaN, true, "no evaluation done ", m_mode, Double.NaN);
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
            // build the highest-tile statistics in tiles:
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
        
        int nPly = pa.getParWrapper().getWrapperNPly();
        long duration = (stopTime - startTime);
        avgGameDur = Math.round((double) duration / (double)ConfigEvaluator.NUMBEREVALUATIONS);
        avgMovPerGame =  Math.round((double)moves / (double)ConfigEvaluator.NUMBEREVALUATIONS);
        movesPerSec = Math.round(moves/((double) duration /(double)1000));
        boolean success = lastResult > ConfigEvaluator.MINPOINTS;
        m_msg = "Quick Evaluation of "+ m_PlayAgent.getName() +": Average score "+ Math.round(lastResult);

        this.eResult = new EResult2048(m_PlayAgent, nPly, ConfigEvaluator.NUMBEREVALUATIONS, minScore, maxScore, lastResult, medianScore,
        		standarddeviation, avgGameDur, avgMovPerGame, movesPerSec, tiles,
                lastResult, success, m_msg, m_mode, ConfigEvaluator.MINPOINTS);

        return this.eResult;
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

    // --- this is now EResult2048.getReport() ---
//  public String getLongMsg() {
//        StringBuilder tilesString = new StringBuilder();
//		DecimalFormat frm = new DecimalFormat("00000");
//		DecimalFormat frm1 = new DecimalFormat("000");
//		DecimalFormat frm2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
//		frm2.applyPattern("#0.0%");
//
//        for (Map.Entry<Integer, Integer> tile : tiles.entrySet()) {
//            tilesString.append("\n").append(frm.format(tile.getKey().longValue()))
//                    .append(", ").append(frm1.format(tile.getValue().longValue()))
//                    .append("  (").append(frm2.format(tile.getValue().doubleValue() / ConfigEvaluator.NUMBEREVALUATIONS)).append(")");
//        }
//
//        String agentSettings = "";
//
//        if(m_PlayAgent.getName().equals("MC-N")) {
//            MCAgentN mcAgent = (MCAgentN)m_PlayAgent;
//            agentSettings = "\nROLLOUTDEPTH: " + mcAgent.getParMC().getRolloutDepth() +
//                    "\nITERATIONS: " + mcAgent.getParMC().getNumIter() +
//                    "\nNUMBERAGENTS: " + mcAgent.getParMC().getNumAgents();
//        } else if(m_PlayAgent.getName().equals("MCTS Expectimax")) {
//            MCTSExpectimaxAgt mctsExpectimaxAgt = (MCTSExpectimaxAgt) m_PlayAgent;
//            agentSettings = "\nROLLOUTDEPTH: " + mctsExpectimaxAgt.params.getRolloutDepth() +
//                    "\nITERATIONS: " + mctsExpectimaxAgt.params.getNumIter() +
//                    "\nMAXNODES:" + mctsExpectimaxAgt.params.getMaxNodes();
//        }
//
//        return "\n\nSettings:" +
//                "\nAgent Name: " + m_PlayAgent.getName() +
//                agentSettings +
//                "\nNumber of games: " + ConfigEvaluator.NUMBEREVALUATIONS +
//                "\n" +
//                "\nResults:" +
//                "\nLowest score is: " + minScore +
//                "\nAverage score is: " + Math.round(lastResult) + " +- " + Math.round(standarddeviation) +
//                "\nMedian score is: " + Math.round(medianScore) +
//                "\nHighest score is: " + maxScore +
//                "\nAverage game duration: " +  avgGameDur + "ms" +
//                "\nDuration of evaluation: " + Math.round(duration) + "s" +
//				"\nAverage moves per game: " +  avgMovPerGame +
//                "\nMoves per second: " + movesPerSec +
//                "\n" +
//                "\nHighest tiles: " +
//                tilesString +
//                "\n\n";
//  }

    // --- this is now EResult2048.getMsg() ---
//  public String getShortMsg() {
//        return "Quick Evaluation of "+ m_PlayAgent.getName() +": Average score "+ Math.round(lastResult);
//  }

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