package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgent;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import tools.Types;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 02.12.2016.
 */
public class Evaluator2048 extends Evaluator {
    private ExecutorService executorService = Executors.newFixedThreadPool(6);

    private double averageScore;
    private double medianScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = Integer.MIN_VALUE;
    private List<Integer> scores = new ArrayList<>();
    private double standarddeviation;
    private double averageRolloutDepth;
    private TreeMap<Integer, Integer> tiles = new TreeMap<Integer, Integer>();
    private int moves = 0;
    private long startTime;
    private long stopTime;
    private int verbose;


    public Evaluator2048(PlayAgent e_PlayAgent, int stopEval, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        this.verbose = verbose;
    }

    @Override
    protected boolean eval_Agent() {
        startTime = System.currentTimeMillis();
        if(verbose == 0) {
            System.out.print("Starting evaluation of " + ConfigEvaluator.NUMBEREVALUATIONS + " games, this may take a while...\n");
        }
        List<StateObserver2048> stateObservers = new ArrayList<>();


        if(m_PlayAgent.getName().equals("MCTS Expectimax")) {
            //async for MCTS Agents
            List<Callable<StateObserver2048>> callables = new ArrayList<>();
            MCTSExpectimaxAgt mctsExpectimaxAgt = (MCTSExpectimaxAgt) m_PlayAgent;

            if(verbose == 0) {
                System.out.println("Detected MCTS Expectimax Agent, iterations: " + mctsExpectimaxAgt.params.getNumIter() + ", rolloutdepth: " + mctsExpectimaxAgt.params.getRolloutDepth() + ", Treedepth: " + mctsExpectimaxAgt.params.getTreeDepth() + ", k: " + mctsExpectimaxAgt.params.getK_UCT() + ", maxnodes: " + mctsExpectimaxAgt.params.getMaxNodes() + ", alternative version: " + mctsExpectimaxAgt.params.getAlternativeVersion());
            }

            //play Games
            for(int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
                int gameNumber = i+1;
                callables.add(() -> {
                    StateObserver2048 so = new StateObserver2048();
                    long gameSartTime = System.currentTimeMillis();

                    PlayAgent playAgent = new MCTSExpectimaxAgt("MCTS Expectimax", mctsExpectimaxAgt.params);

                    while (!so.isGameOver()) {
                        Types.ACTIONS action = playAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true);
                        so.advance(action);
                    }

                    if(verbose == 0) {
                        System.out.print("Finished game " + gameNumber + " with scores " + so.score + " after " + (System.currentTimeMillis() - gameSartTime) + "ms. Highest tile is " + so.highestTileValue + ".\n");
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
            //sync for other Agents (MC Agent uses multiple Cores naturally)
            for (int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
                long gameSartTime = System.currentTimeMillis();
                StateObserver2048 so = new StateObserver2048();

                while (!so.isGameOver()) {
                    so.advance(m_PlayAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));
                }

                if(verbose == 0) {
                    System.out.print("Finished game " + (i + 1) + " with score " + so.score + " after " + (System.currentTimeMillis() - gameSartTime) + "ms. Highest tile is " + so.highestTileValue + ".\n");
                }

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

        averageScore/= ConfigEvaluator.NUMBEREVALUATIONS;

        //Standard deviation
        for(int score : scores) {
            standarddeviation += (score-averageScore)*(score-averageScore);
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

        System.out.print("\n");

        return averageScore > ConfigEvaluator.MINPOINTS;
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

        long duration = (stopTime - startTime);
        if(duration == 0) {
            duration = 1;
        }

        String agentSettings = "";

        if(m_PlayAgent.getName() == "MC") {
            MCAgent mcAgent = (MCAgent)m_PlayAgent;
            agentSettings = "\nROLLOUTDEPTH: " + mcAgent.getMCPar().getRolloutDepth() +
                    "\nITERATIONS: " + mcAgent.getMCPar().getNumIter() +
                    "\nNUMBERAGENTS: " + mcAgent.getMCPar().getNumAgents();
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
                "\nAverage score is: " + Math.round(averageScore) + " +- " + Math.round(standarddeviation) +
                "\nMedian score is: " + Math.round(medianScore) +
                "\nHighest score is: " + maxScore +
//              "\nStandard deviation is: " + standarddeviation +
//              "\nAverage rollout depth is: " + averageRolloutDepth +
                "\nAverage game duration: " +  Math.round((double)duration / (double)ConfigEvaluator.NUMBEREVALUATIONS) + "ms" +
//              "\nDuration of evaluation: " + Math.round((double)duration/(double)1000) + "s" +
                "\nMoves per second: " + Math.round(moves/((double)duration/(double)1000)) +
                "\n" +
                "\nHighest tiles: " +
                tilesString +
                "\n\n";
    }

    @Override
    public boolean isAvailableMode(int mode) {
        switch (mode) {
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return true;
            default:
                return false;
        }
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{0, 1, 2};
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
    public int getMultiTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        return"success rate";
    }

    @Override
    public String getPlotTitle() {
        return "success";
    }
}