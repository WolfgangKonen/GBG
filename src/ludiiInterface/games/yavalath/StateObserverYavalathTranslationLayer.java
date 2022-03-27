package ludiiInterface.games.yavalath;

import games.Yavalath.ConfigYavalath;
import games.Yavalath.StateObserverYavalath;
import other.context.Context;
import other.move.Move;
import tools.Types;

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
        //Get a list of all moves made in the context
        List<Move> ludiiContextMoves = ludiiContext.trial().generateCompleteMovesList();

        for(Move x : ludiiContextMoves){
            if(!conversion.isValidLudiiIndex(x.to())){
                System.out.println("ludii index invalid"); //seems to be an error happening here, might have to do with swap rule
            }
            // x.to() returns the position of the stone we place, translate that into a action value
            int actionInt = conversion.getGBGIndexFromLudii(x.to());
            int tileValue = ConfigYavalath.getTileValueFromAction(new Types.ACTIONS(actionInt));
            int j = tileValue% ConfigYavalath.getMaxRowLength();
            int i = (tileValue-j)/ConfigYavalath.getMaxRowLength();
            // x.mover() returns who made the move, use that to adjust the state observer board accordingly
            board[i][j].setPlayer(conversion.getGBGPlayerFromLudii(x.mover()));
        }
        currentPlayer = conversion.getGBGPlayerFromLudii(playerID);
        setAvailableActions();

    }
}
