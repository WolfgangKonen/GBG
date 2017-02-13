package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgent;
import controllers.MCTS.MCTSAgentT;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import params.MCTSParams;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 09.02.2016.
 */
public class Evaluator2048_BoardPositions extends Evaluator{
    private Random random = new Random();
    private ExecutorService executorService = Executors.newWorkStealingPool();



    public Evaluator2048_BoardPositions(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent() {
        //find new realisitic boardPositions
        //newBoardPositions();

        //load saved boardPositions
        List<StateObserver2048> boardPositions = loadBoardPositions();
        System.out.println("Found " + boardPositions.size() + " boardPositions\n");

        /*ToDo: remove/mark controversial boardPositions like
        0 | 0 | 0 | 0
        0 | 2 | 4 | 0
        0 | 2 | 0 | 0
        0 | 0 | 0 | 0
        because up or down are equally good*/

        /*ToDo: group by availableActions*/

        //sort boardPositions by numEmptyTiles
        TreeMap<Integer, List<StateObserver2048>> groupedBoardPositions = groupBoardPositionsByNumEmptyTiles(boardPositions);

        //remove gameBoards until numGameBoards are left for each group
        for(List<StateObserver2048> groupedBoardPosition : groupedBoardPositions.values()) {
            while (groupedBoardPosition.size() > 10) {
                groupedBoardPosition.remove(random.nextInt(groupedBoardPosition.size()-1));
            }
        }

        //Analyse the groupedBoardPositions
        System.out.println("Analysing gameBoards, this may take a while...");
        List<Callable<Integer>> callables = new ArrayList<>();
        for(List<StateObserver2048>groupedBoardPosition : groupedBoardPositions.values()) {
            callables.add(() -> {
                analyseBoardPositions(groupedBoardPosition);
                return null;
            });
        }

        try {
            executorService.invokeAll(callables).stream();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void analyseBoardPositions(List<StateObserver2048> boardPositions) {
        MCTSParams mctsParams = new MCTSParams();
        mctsParams.setNumIter(10000);
        mctsParams.setK_UCT(1);
        mctsParams.setTreeDepth(1);
        mctsParams.setRolloutDepth(201);
        PlayAgent mctsAgent = new MCTSAgentT("MCTS",null,mctsParams);
        PlayAgent mcAgent = new MCAgent();

        int maxCertainty = Config.NUMBEREVALUATIONS*boardPositions.size();
        double mcCertainty = 0;
        double mctsCertainty = 0;
        double sameActionCounter = 0;

        for(StateObserver2048 so : boardPositions) {
            int[] mcActions = {0,0,0,0};
            int[] mctsActions = {0,0,0,0};
            int highestMCValue = 0;
            int bestMCAction = 0;
            int highestMCTSValue = 0;
            int bestMCTSAction = 0;

            for(int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
                int MCAction = mcAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true).toInt();
                mcActions[MCAction] +=1;
            }

            for(int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
                int MCTSAction = mctsAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true).toInt();
                mctsActions[MCTSAction] +=1;
            }

            for(int i = 0; i < 4; i++) {
                if(highestMCValue < mcActions[i]) {
                    highestMCValue = mcActions[i];
                    bestMCAction = i;
                }
                if(highestMCTSValue < mctsActions[i]) {
                    highestMCTSValue = mctsActions[i];
                    bestMCTSAction = i;
                }
            }
            mcCertainty += highestMCValue;
            mctsCertainty += highestMCTSValue;
            if(bestMCAction == bestMCTSAction) {
                sameActionCounter += 1;
            }
        }

        mcCertainty=(mcCertainty/maxCertainty)*100;
        mctsCertainty=(mctsCertainty/maxCertainty)*100;
        sameActionCounter=(sameActionCounter/boardPositions.size())*100;

        System.out.println("Analysed " + boardPositions.size() + " gameBoards with " + boardPositions.get(0).getNumEmptyTiles() + " emptyTile(s)");
        System.out.println("mcCertainty = " + mcCertainty);
        System.out.println("mctsCertainty = " + mctsCertainty);
        System.out.println("sameActionCounter = " + sameActionCounter);
        System.out.println();
    }

    private TreeMap<Integer, List<StateObserver2048>> groupBoardPositionsByNumEmptyTiles(List<StateObserver2048> boardPositions) {
        TreeMap<Integer, List<StateObserver2048>> sortedBoardPositions = new TreeMap<>();

        for(StateObserver2048 so : boardPositions) {
            Integer numEmptyTiles = so.getNumEmptyTiles();
            List<StateObserver2048> matchingSO = sortedBoardPositions.get(numEmptyTiles);
            if(matchingSO == null) {
                matchingSO = new ArrayList<>();
            }
            matchingSO.add(so);
            sortedBoardPositions.put(numEmptyTiles, matchingSO);
        }

        return sortedBoardPositions;
    }

    private void newBoardPositions() {
        System.out.println("Looking for boardPositions, this may take a while...");

        List<BoardPositionContainer> boardPositionContainers = new ArrayList<>();

        //play i games
        List<Callable<List<BoardPositionContainer>>> callables = new ArrayList<>();
        for(int i = 8; i > 0; i--) {
            callables.add(() -> {
                StateObserver2048 so = new StateObserver2048();
                PlayAgent playAgent = new MCAgent();
                List<BoardPositionContainer> boardPositionContainer = new ArrayList<>();
                while (!so.isGameOver()) {
                    boardPositionContainer.add(new BoardPositionContainer(so));
                    so.advance(playAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));
                }
                return boardPositionContainer;
            });
        }

        //merge all boardPositions
        try {
            executorService.invokeAll(callables).stream().map(future -> {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).forEach(boardPositionContainers::addAll);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //save all boardPositions
        try {
            FileOutputStream fos = new FileOutputStream("src\\games\\ZweiTausendAchtundVierzig\\boardpositions.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(boardPositionContainers);
            fos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<StateObserver2048> loadBoardPositions() {
        System.out.println("Loading boardPositions");

        List<StateObserver2048> boardPositions = new ArrayList<>();

        //load boardPositions
        try {
            FileInputStream fis = new FileInputStream("src\\games\\ZweiTausendAchtundVierzig\\boardpositions.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<BoardPositionContainer> boardPositionContainers = (List<BoardPositionContainer>)ois.readObject();
            fis.close();
            ois.close();

            for(BoardPositionContainer boardpositionContainer : boardPositionContainers) {
                boardPositions.add(new StateObserver2048(boardpositionContainer.values, boardpositionContainer.score, boardpositionContainer.winState));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return boardPositions;
    }

    @Override
    public double getLastResult() {
        return 0;
    }

    @Override
    public String getMsg() {
        return "";
    }
}

class BoardPositionContainer implements Serializable {
    public int[][] values;
    public int score;
    public int winState;

    public BoardPositionContainer(StateObserver2048 so) {
        this.values = so.toArray();
        this.score = so.getScore();
        this.winState = so.getWinState();
    }
}
