package games.Yavalath;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.GameBoard;
import games.XNTupleFuncs;

public class ArenaTrainYavalath extends ArenaTrain {

    public ArenaTrainYavalath(String title, boolean withUI){
        super(title, withUI);
    }

    @Override
    public String getGameName() {
        return "Yavalath";
    }

    @Override
    public GameBoard makeGameBoard() {
        return new GameBoardYavalath(this);
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
        return null;
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs(){
        return new XNTupleFuncsYavalath();
    }

    public static void setPlayerNumber(int players){
        ConfigYavalath.PLAYERS = players;
    }

    public static void setBoardSize(int size){
        ConfigYavalath.setBoardSize(size);
    }

    public static void main(String[] args){
        ArenaTrainYavalath frame = new ArenaTrainYavalath("General Board Game Playing - Yavalath", true);

        if(args.length==0){
            frame.init();
        } else{
            throw new RuntimeException("[ArenaTrainYavalath.main] args="+args+" not allowed. Use batch facility");
        }
    }
}
