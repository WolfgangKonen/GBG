package games.Yavalath;

import controllers.PlayAgent;
import games.*;

public class ArenaYavalath extends Arena {

    public ArenaYavalath(String title, boolean withUI) {
        super(title, withUI);
    }

    public ArenaYavalath(String title, boolean withUI, boolean withTrainRights) {
        super(title,withUI,withTrainRights);
    }

    @Override
    public String getGameName() {
        return "Yavalath";
    }

    @Override
    public GameBoard makeGameBoard() {
        gb = new GameBoardYavalath(this);
        return gb;
    }

    @Override
    public Feature makeFeatureClass(int featmode){
        return new FeatureYavalath();
    }

    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
        return new EvaluatorYavalath(pa,gb, mode);
    }

//    @Override
//    public void performArenaDerivedTasks() {}

    public XNTupleFuncs makeXNTupleFuncs(){
        return new XNTupleFuncsYavalath();
    }

    public static void setPlayerNumber(int players){
        ConfigYavalath.setPlayers(players);
    }

    public static void setBoardSize(int size){
        ConfigYavalath.setBoardSize(size);
    }

    public static void main(String[] args){
        ArenaYavalath arena = new ArenaYavalath("General Board Game Playing", true);

        if(args.length==0){
            arena.init();
        } else{
            throw new RuntimeException("[ArenaTrainYavalath.main] args="+args+" not allowed. Use batch facility");
        }

    }

}
