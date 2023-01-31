package ludiiInterface.games.CFour;

import games.CFour.StateObserverC4;
import other.context.Context;
import other.move.Move;
import tools.Types;

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

        //List of all moves made in Ludii's game context so far
        List<Move> ludiiContextMoves = ludiiContext.trial().generateCompleteMovesList();

        for(Move x : ludiiContextMoves){
            int actionInt = conversion.getGBGIndexFromLudii(x.to()); //x.to() returns position of the placed piece
            super.advanceBase(Types.ACTIONS.fromInt(actionInt));

            gameOver = isWin = m_C4.canWin(actionInt);
            m_C4.putPiece(actionInt);
            if(!gameOver) gameOver = m_C4.isDraw();

            super.incrementMoveCounter();

            prevCell = getLastCell();
            int lastPlayer = conversion.getGBGPlayerFromLudii(x.mover()); //x.mover() returns the player who made the move
            lastCell = new LastCell(actionInt, m_C4.getColHeight(actionInt)-1, lastPlayer);
            m_Player = (lastPlayer == 0 ? 1 : 0);
        }

        setAvailableActions();

    }
}
