package ludiiInterface.games.yavalath;

import games.Yavalath.ConfigYavalath;
import games.Yavalath.StateObserverYavalath;
import other.context.Context;
import other.move.Move;

import java.util.List;

public class StateObserverYavalathTranslationLayer extends StateObserverYavalath {

    private Context ludiiContext;
    private int playerID;

    public StateObserverYavalathTranslationLayer(Context ludiiContext, int playerID){
        super();
        this.ludiiContext = ludiiContext;
        this.playerID = playerID;
        updateGameState();
    }

    private void updateGameState(){
        SystemConversionYavalath conversion = new SystemConversionYavalath();

        List<Move> ludiiContextMoves = ludiiContext.trial().generateCompleteMovesList();

        for(Move x : ludiiContextMoves){
            if(!conversion.isValidLudiiIndex(x.to())){
                System.out.println("ludii index invalid"); //seems to be an error happening here, might have to do with swap rule
            }
            int actionInt = conversion.getGBGIndexFromLudii(x.to());
            int j = actionInt% ConfigYavalath.getMaxRowLength();
            int i = (actionInt-j)/ConfigYavalath.getMaxRowLength();
            board[i][j].setPlayer(conversion.getGBGPlayerFromLudii(x.mover()));
        }
        currentPlayer = conversion.getGBGPlayerFromLudii(playerID);
        setAvailableActions();

    }
}
