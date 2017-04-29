package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgent;
import controllers.MC.MCAgentConfig;
import controllers.MCTS.MCTSAgentT;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import params.MCParams;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 02.12.2016.
 */
public class Evaluator2048 extends Evaluator {
    private ExecutorService executorService = Executors.newWorkStealingPool();

    private double averageScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = Integer.MIN_VALUE;
    List<Integer> scores = new ArrayList<>();
    private int medianScore;
    private long standartdeviation;
    private double averageRolloutDepth;
    private TreeMap<Integer, Integer> tiles = new TreeMap<Integer, Integer>();
    private int moves = 0;
    private long startTime;
    private long stopTime;

    int depth = -1;
    int iterations = -1;
    int numberAgents = -1;


    public Evaluator2048(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        if (isAvailableMode(mode)==false) 
        	throw new RuntimeException("Evaluator2048: Value mode = "+mode+" for parameter mode not allowed." );
    }

    @Override
    protected boolean eval_Agent() {
        startTime = System.currentTimeMillis();
        System.out.print("Starting evaluation of " + Config.NUMBEREVALUATIONS + " games, this may take a while...\n");
        List<StateObserver2048> stateObservers = new ArrayList<>();


        if(m_PlayAgent.getName().equals("MCTS")) {
            //async for MCTS Agents
            List<Callable<StateObserver2048>> callables = new ArrayList<>();
            MCTSAgentT mctsAgentT = (MCTSAgentT)m_PlayAgent;

            System.out.println("Detected MCTS Agent, Iterations: " + mctsAgentT.params.getNumIter() + ", Rolloutdepth: " + mctsAgentT.params.getRolloutDepth());
            //play Games
            for(int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
                int gameNumber = i+1;
                callables.add(() -> {
                    int depth = 0;
                    double numberActions = 0;
                    int currentNumberActions = 0;
                    StateObserver2048 so = new StateObserver2048();
                    long gameSartTime = System.currentTimeMillis();

                    PlayAgent playAgent = new MCTSAgentT("MCTS",null,mctsAgentT.params);

                    while (!so.isGameOver()) {
                        currentNumberActions = so.getNumAvailableActions();
                        so.advance(playAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));
                        currentNumberActions = currentNumberActions * so.getNumEmptyTiles() * 2;
                        numberActions += currentNumberActions;
                        depth++;
                    }

                    numberActions/=depth;
                    System.out.print("Finished game " + gameNumber + " with scores " + so.score + " after " + (System.currentTimeMillis() - gameSartTime) + "ms. Highest tile is " + so.highestTileValue + ". Depth is: " + depth + " and numberActions is: " + numberActions + "\n");

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
            //sync for other Agents (MC Agent uses multiple Cores naturally)
            for (int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
                long gameSartTime = System.currentTimeMillis();
                StateObserver2048 so = new StateObserver2048();

                while (!so.isGameOver()) {
                    so.advance(m_PlayAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));
                    MCAgent agent = (MCAgent)m_PlayAgent;
                    averageRolloutDepth += agent.getAverageRolloutDepth();
                }
                System.out.print("Finished game " + (i + 1) + " with score " + so.score + " after " + (System.currentTimeMillis() - gameSartTime) + "ms. Highest tile is " + so.highestTileValue + ".\n");

                stateObservers.add(so);
            }
        }

        //evaluate games
        //Min/Max/Average Score
        for(StateObserver2048 so : stateObservers) {
            Integer value = tiles.get(so.highestTileValue);
            if (value == null) {
                tiles.put(so.highestTileValue, 1);
            } else {
                tiles.put(so.highestTileValue, value + 1);
            }

            scores.add(so.score);

            averageScore += so.score;
            if (so.score < minScore) {
                minScore = so.score;
            }
            if (so.score > maxScore) {
                maxScore = so.score;
            }

            moves += so.moves;
        }

        averageScore/=Config.NUMBEREVALUATIONS;


        //Median Score
        Collections.sort(scores);

        if(Config.NUMBEREVALUATIONS %2 == 0) {
            medianScore+= scores.get(Config.NUMBEREVALUATIONS/2);
            medianScore+= scores.get((Config.NUMBEREVALUATIONS/2)-1);
            medianScore/=2;
        } else {
            medianScore= scores.get((Config.NUMBEREVALUATIONS-1)/2);
        }

        //Standartdeviation
        for(int score : scores) {
            standartdeviation += (score-averageScore)*(score-averageScore);
        }
        standartdeviation/=Config.NUMBEREVALUATIONS;
        standartdeviation = Double.valueOf(Math.sqrt(standartdeviation)).longValue();

        averageRolloutDepth/=moves;

        stopTime = System.currentTimeMillis();

        System.out.print("\n");

        return true;
    }

    @Override
    public double getLastResult() {
        return averageScore;
    }

    @Override
    public String getMsg() {
        String tilesString = "";

        for (Map.Entry tile : tiles.entrySet()) {
            tilesString += "\n" + tile.getKey() + ", " + tile.getValue();
        }

        long duration = (stopTime - startTime)/1000;


        return "\n\nSettings:" +
                "\nMC-Agent DEPTH: " + depth +
                "\nMC-Agent ITERATIONS: " + iterations +
                "\nMC-Agent NUMBERAGENTS: " + numberAgents +
                "\n" +
                "\nPENALISATION: " + Config.PENALISATION +
                "\nADDSCORE: " + Config.ADDSCORE +
                "\nEmptitiles multiplier: " + Config.EMPTYTILEMULTIPLIER +
                "\nHighesttileincorner multiplier: " + Config.HIGHESTTILEINCORENERMULTIPLIER +
                "\nRow multiplier: " + Config.ROWMULTIPLIER +
                "\nNumber of games: " + Config.NUMBEREVALUATIONS +
                "\n" +
                "\nResults:" +
                "\nLowest scores is: " + minScore +
                "\nAverage scores is: " + Math.round(averageScore) +
                "\nHighest scores is: " + maxScore +
                "\nStandartdeviation is: " + standartdeviation +
                "\nAverage Rolloutdepth is: " + averageRolloutDepth +
                "\nAverage game duration: " +  Math.round((stopTime - startTime)/Config.NUMBEREVALUATIONS) + "ms" +
                "\nDuration of evaluation: " + duration + "s" +
                "\nMoves per second: " + Math.round(moves/duration) +
                "\n" +
                "\nHighest tiles: " +
                tilesString +
                "\n\n";
    }
 
 	/**
 	 * Since Evaluator2048 does not use mode, this function returns always true
 	 */
	@Override
 	public boolean isAvailableMode(int mode) {
		return true;
 	}

 	@Override
 	public int[] getAvailableModes() {
 		return null;
 	}
 	
}