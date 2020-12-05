package controllers.MCTSWrapper;

import controllers.MCTSWrapper.passStates.GameStateIncludingPass;
import controllers.MCTSWrapper.stateApproximation.Approximator;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.RandomAgent;
import games.ArenaTrain;
import games.GBGBatch;
import games.GameBoard;
import games.Othello.GameBoardOthello;
import games.RubiksCube.GameBoardCube;
import games.StateObservation;
import org.junit.Test;

import static org.junit.Assert.*;

public class MCTSWrapperAgentTest {
    String selectedGame = "Othello";
    String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);

    /**
     * Measure whether getVTable2For is faster than getVTableFor.
     * Result: Is 5x faster, but has only negligible impact, because seldom called.
     */
    @Test
    public void getVTable_Test() {
        ArenaTrain t_Game = GBGBatch.setupSelectedGame(selectedGame, scaPar);   // t_Game is ArenaTrain object
        GameBoard gb = new GameBoardOthello(t_Game);        // needed for chooseStartState()
        StateObservation sob = gb.getDefaultStartState();
        MCTSNode mctsNode = new MCTSNode(new GameStateIncludingPass(sob));
        Approximator approximator = new PlayAgentApproximator(new RandomAgent("rand"));
        MCTS mcts = new MCTS(approximator, 1.0, 50);
        MCTSWrapperAgent mwa = new MCTSWrapperAgent(100,1.0,approximator,"",50);
        int ncalls = 100000;
        long startTime;
        double elapsedTime;

        for (int i = 0; i < 100; i++) {
            mcts.search(mctsNode,0);
        }

        startTime = System.currentTimeMillis();
        for (int i = 0; i < ncalls; i++) {
            mwa.getVTable2For(mctsNode);
        }
        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[getVTable2For] "+elapsedTime+" sec.");

        startTime = System.currentTimeMillis();
        for (int i = 0; i < ncalls; i++) {
            mwa.getVTableFor(mctsNode);
        }
        elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
        System.out.println("[getVTableFor] "+elapsedTime+" sec.");

    }

}