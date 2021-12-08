package games.Yavalath;

import org.junit.Test;
import tools.Types;

import static games.Yavalath.ConfigYavalath.*;

public class StateObserverYavalathTest {


    StateObserverYavalath soYav2P = new StateObserverYavalath();
    StateObserverYavalath soYav3P = new StateObserverYavalath();

    private void initSO2P() {
        soYav2P = null;
        soYav2P = new StateObserverYavalath();
        soYav2P.setNumPlayers(2);
    }
    private void initSO3P(){
        soYav3P = null;
        soYav3P = new StateObserverYavalath();
        soYav3P.setNumPlayers(3);
    }


    @Test
    public void testWinConditions(){
        winCondition2PlayersPlayerOne();
        winCondition2PlayersPlayerTwo();
    }

    @Test
    public void testLossConditions(){
        lossCondition2PlayersPlayerOne();
        lossCondition2PlayersPlayerTwo();
    }

    @Test
    public void testDraw2Players(){
        initSO2P();
        int[] actions = {
                  0,1,2,3,4,9,10,11,12,13,14,18,19,20,21,22,23,24,
                27,28,29,30,31,32,33,34,36,37,38,39,40,41,42,43,44,
                53,52,51,50,49,48,47,46,56,57,58,59,60,61,62,71,70,
                69,68,67,66,76,77,78,79,80
        };
        for (int x:actions){
            soYav2P.advance(new Types.ACTIONS(x));
        }
        assert soYav2P.isTie();
    }

    public void winCondition2PlayersPlayerOne(){
        initSO2P();

        int[] actions = {
                0,9,1,11,3,13,2
        };
        for(int x:actions){
            soYav2P.advance(new Types.ACTIONS(x));
        }
        assert soYav2P.getWinner() == PLAYER_ZERO;
    }

    public void winCondition2PlayersPlayerTwo(){
        initSO2P();
        int[] actions = {
                0,9,1,10,4,12,3,11
        };
        for(int x:actions){
            soYav2P.advance(new Types.ACTIONS(x));
        }
        assert soYav2P.getWinner() == PLAYER_ONE;
    }

    @Test
    public void winCondition3Player(){
        initSO3P();
        int[] actions = {
            0,9,18,1,10,19,3,12,21,2
        };
        for (int x : actions){
            soYav3P.advance(new Types.ACTIONS(x));
        }
        assert soYav3P.isGameOver();
        assert soYav3P.getWinner() == PLAYER_ZERO;
    }

    private void lossCondition2PlayersPlayerOne(){
        initSO2P();
        int[] actions = {
                0,9,1,10,2
        };
        for (int x:actions){
            soYav2P.advance(new Types.ACTIONS(x));
        }
        assert soYav2P.getLoser() == PLAYER_ZERO;
    }

    private void lossCondition2PlayersPlayerTwo(){
        initSO2P();
        int[] actions = {
                0,9,1,10,3,11
        };
        for (int x:actions){
            soYav2P.advance(new Types.ACTIONS(x));
        }
        assert soYav2P.getLoser() == PLAYER_ONE;
    }

    @Test
    public void lossCondition3Player(){
        //Player 1 loses first
        initSO3P();
        int[] actions = {
                0,9,18,1,10,19,2
        };
        for (int x : actions){
            soYav3P.advance(new Types.ACTIONS(x));
        }
        assert soYav3P.getLoser() == PLAYER_ZERO;

        //Player 2 also loses, game should be over
        soYav3P.advance(new Types.ACTIONS(11));
        assert soYav3P.isGameOver();
        assert soYav3P.getWinner() == PLAYER_TWO;

    }

    /**
     * Check if swap rule works correctly.
     */
    @Test
    public void swapRule(){
        initSO2P();
        soYav2P.advance(new Types.ACTIONS(1));
        soYav2P.advance(new Types.ACTIONS(1));

        assert soYav2P.swapRuleUsed();
        assert soYav2P.getMoveCounter() == 2;
    }

    /**
     * Check if game terminates correctly if the player that places the last tile loses with that move.
     * Can only happen for player 1 because of the number of tiles.
     */
    @Test
    public void tieAfterOnePlayerLost(){
        initSO3P();
        //Filling the board from top left to bottom right except for skipping tile 47/[5,2] and placing that one at the end is a valid board for this
        int[] actions = {
                0,1,2,3,4,9,10,11,12,13,14,18,19,20,21,22,23,24,27,28,29,30,
                31,32,33,34,36,37,38,39,40,41,42,43,44,46,48,49,50,51,52,53,
                56,57,58,59,60,61,62,66,67,68,69,70,71,76,77,78,79,80,47
        };
        for (int x:actions) {
            soYav3P.advance(new Types.ACTIONS(x));
        }

        assert soYav3P.getLoser() == PLAYER_ZERO;
        assert soYav3P.isTie();
        assert soYav3P.isGameOver();

    }

    /**
     * Check if actions get correctly adjusted if the next player can win the game.
     */
    @Test
    public void check3PlayerForcedMove(){
        initSO3P();
        int[] actions = {
                0,9,18,1,10,19,3,12
        };
        for(int x : actions){
            soYav3P.advance(new Types.ACTIONS(x));
        }
        assert soYav3P.getNumAvailableActions() == 1;

        soYav3P.advance(new Types.ACTIONS(2));

        assert soYav3P.getNumAvailableActions() == 1;
    }
}
