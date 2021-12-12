package ludiiInterface.games.Hex;

import games.Hex.StateObserverHex;
import other.context.Context;
import other.move.Move;

import java.util.List;

public class StateObserverHexTranslationLayer extends StateObserverHex {

    private Context ludiiContext;
    private int playerID, size;
    public StateObserverHexTranslationLayer(Context context, int playerID, int size){
        super();
        ludiiContext = context;
        this.playerID = playerID;
        this.size = size;
        updateGameState();
    }

    private void updateGameState(){
        SystemConversionHex conversionHex = new SystemConversionHex(size);
        List<Move> ludiiContextMoves = ludiiContext.trial().generateCompleteMovesList();

        for(Move x : ludiiContextMoves){
            int actionInt = conversionHex.getGBGIndexFromLudii(x.actions().get(0).to());
            int j = actionInt% 6;
            int i = (actionInt-j)/6;
            board[i][j].setPlayer(conversionHex.getGBGPlayerFromLudii(x.mover()));
            currentPlayer = (conversionHex.getGBGPlayerFromLudii(x.mover()) == 0 ? 1 : 0);
        }
        setAvailableActions();
    }
}
