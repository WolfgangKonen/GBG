package ludiiInterface.games.CFour;

import games.CFour.StateObserverC4;
import other.context.Context;
import other.move.Move;

import java.util.List;

public class StateObserverC4TranslationLayer extends StateObserverC4 {

    private Context ludiiContext;
    private int playerID;

    public StateObserverC4TranslationLayer(Context ludiiContext, int playerID){
        super();
        this.ludiiContext = ludiiContext;
        this.playerID = playerID;
        updateGameState();
    }

    private void updateGameState(){
        SystemConversionC4 conversion = new SystemConversionC4();

        //List of all moves made in Ludii Context
        List<Move> ludiiContextMoves = ludiiContext.trial().generateCompleteMovesList();

        for(Move x : ludiiContextMoves){
            int actionInt = conversion.getGBGIndexFromLudii(x.to());

            gameOver = isWin = m_C4.canWin(actionInt);
            m_C4.putPiece(actionInt);

            prevCell = getLastCell();
            int lastPlayer = conversion.getGBGPlayerFromLudii(x.mover());
            lastCell = new StateObserverC4.LastCell(actionInt, m_C4.getColHeight(actionInt)-1, lastPlayer);

        }
        m_Player = conversion.getGBGPlayerFromLudii(playerID);
        setAvailableActions();

    }
}
