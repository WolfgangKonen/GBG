package ludiiInterface.games.othello;

import games.Othello.BaseOthello;
import games.Othello.ConfigOthello;
import games.Othello.StateObserverOthello;
import other.context.Context;
import other.move.Move;

import java.util.List;

public class StateObserverOthelloTranslationLayer extends StateObserverOthello {

    private Context context;
    private int playerID;

    public StateObserverOthelloTranslationLayer(Context context, int playerID){
        super();
        this.context = context.deepCopy();
        this.playerID = playerID;
        updateGameState();
    }

    private void updateGameState() {

        //Get a list of all the moves made in the Ludii context
        List<Move> ludiiMoveList = context.trial().generateCompleteMovesList();

        //We can remove the first 4 starting stones because they are already placed in the super() call in the constructor
        ludiiMoveList.remove(0);
        ludiiMoveList.remove(0);
        ludiiMoveList.remove(0);
        ludiiMoveList.remove(0);

        for(Move x : ludiiMoveList){
            if(x.isPass()) continue;
            advanceGameState(x);
        }

        //Update player, available actions and counters
        //The current player should only be relevant for available actions when getNextAction2 is called
        // and that is only called when it is the agents turn, so we can set it as the next player
        setPlayer(new SystemConversionOthello().getGBGPlayerFromLudii(playerID));
        setAvailableActions();
        setPieceCounters();

    }

    /**
     * Translates a Ludii move into the GBG coordinates and then executes that action.
     * @param move A Ludii move
     */
    private void advanceGameState(Move move){
        SystemConversionOthello sysConversion = new SystemConversionOthello();
        int actionInt = sysConversion.getGBGIndexFromLudii(move.actions().get(0).to());
        int y = actionInt % ConfigOthello.BOARD_SIZE;
        int x =(actionInt-y) / ConfigOthello.BOARD_SIZE;
        int player = sysConversion.getGBGPlayerFromLudii(move.actions().get(0).state());
        BaseOthello.flip(currentGameState,x,y,player);
        currentGameState[x][y] = player;
    }

    /**
     * Alternative stringDescr for better clarity when viewing from debugger
     */
    @Override
    public String stringDescr(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ConfigOthello.BOARD_SIZE; i++) {

            for (int j = 0; j < ConfigOthello.BOARD_SIZE; j++) {
                sb.append(currentGameState[i][j] == BaseOthello.getOpponent(1) ? "O" :
                        currentGameState[i][j] == 1 ? "X" : "-");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
