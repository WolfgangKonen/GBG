package games.ZweiTausendAchtundVierzig;

import controllers.MC.MCAgent;
import controllers.MCTS.MCTSAgentT;
import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import params.MCTSParams;
import tools.Types;

import java.io.*;
import java.util.*;

/**
 * Created by Johannes on 09.02.2016.
 */
public class Evaluator2048_BoardPositions extends Evaluator{



    public Evaluator2048_BoardPositions(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent() {
        //find new realisitic boardPositions
        //newBoardpositions();

        //load saved boardPositions
        List<StateObserver2048> boardPositions = loadBoardpositions();
        System.out.println("Found " + boardPositions.size() + " boardPositions\n");

        /*ToDo: remove/mark controversial boardPositions like
        0 | 0 | 0 | 0
        0 | 2 | 4 | 0
        0 | 2 | 0 | 0
        0 | 0 | 0 | 0
        because up or down are equally good*/

        /*ToDo: group by availableActions*/

        //sort boardPositions by numEmptyTiles
        TreeMap<Integer, List<StateObserver2048>> sortedBoardPositions = sortBoardPositionsByNumEmptyTiles(boardPositions);

        //Analyse the sortedBoardPositions
        //ToDo: Ab hier Parallel?

        for(List<StateObserver2048> so : sortedBoardPositions.values()) {
            analyseBoardPositions(so);
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

        System.out.println("Analysing " + boardPositions.size() + " gameBoards with " + boardPositions.get(0).getNumEmptyTiles() + " emptyTile(s), this may take a while...");

        for(StateObserver2048 so : boardPositions) {
            int[] mcActions = {0,0,0,0};
            int[] mctsActions = {0,0,0,0};
            int highestMCValue = 0;
            int bestMCAction = 0;
            int highestMCTSValue = 0;
            int bestMCTSAction = 0;

            for(int j = 0; j < Config.NUMBEREVALUATIONS; j++) {
                int MCAction = mcAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true).toInt();
                mcActions[MCAction] +=1;
            }

            for(int j = 0; j < Config.NUMBEREVALUATIONS; j++) {
                int MCTSAction = mctsAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true).toInt();
                mctsActions[MCTSAction] +=1;
            }

            for(int j = 0; j < 4; j++) {
                if(highestMCValue < mcActions[j]) {
                    highestMCValue = mcActions[j];
                    bestMCAction = j;
                }
                if(highestMCTSValue < mctsActions[j]) {
                    highestMCTSValue = mctsActions[j];
                    bestMCTSAction = j;
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

        System.out.println("mcCertainty = " + mcCertainty);
        System.out.println("mctsCertainty = " + mctsCertainty);
        System.out.println("sameActionCounter = " + sameActionCounter);
        System.out.println();
    }

    private TreeMap<Integer, List<StateObserver2048>> sortBoardPositionsByNumEmptyTiles(List<StateObserver2048> boardPositions) {
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

    private void newBoardpositions() {
        System.out.println("Looking for boardpositions, this may take a while...");

        List<BoardPositionContainer> boardPositionContainer = new ArrayList<>();
        StateObserver2048 so = new StateObserver2048();
        while (!so.isGameOver()) {
            boardPositionContainer.add(new BoardPositionContainer(so));
            so.advance(m_PlayAgent.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true));

        }

        try {
            FileOutputStream fos = new FileOutputStream("src\\games\\ZweiTausendAchtundVierzig\\boardpositions.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(boardPositionContainer);
            fos.close();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<StateObserver2048> loadBoardpositions() {
        System.out.println("Loading boardpositions");

        List<StateObserver2048> boardPositions = new ArrayList<>();

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
    int[][] values;
    int score;
    int winState;

    public BoardPositionContainer(StateObserver2048 so) {
        this.values = so.toArray();
        this.score = so.getScore();
        this.winState = so.getWinState();
    }
}
