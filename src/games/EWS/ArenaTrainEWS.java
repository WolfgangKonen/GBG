package games.EWS;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.EWS.constants.ConfigEWS;
import games.Evaluator;
import games.GameBoard;
import games.XNTupleFuncs;

public class ArenaTrainEWS extends ArenaTrain {
    public ArenaTrainEWS(String title, boolean withUI) {
        super(title, withUI);
    }

    @Override
    public String getGameName() {
        return "EWS";
    }

    @Override
    public GameBoard makeGameBoard() {
        gb = new GameBoardEWS(this, ConfigEWS.BOARD_SIZE,ConfigEWS.NUM_PLAYERS);
        return  gb;
    }


    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
       return new EvaluatorEWS(pa,gb,stopEval,mode,verbose);
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsEWS();
    } // Reference to arena needed



    public static void setConfig(String str){
        switch(str){
            case"3x3 2-Player":{
                ConfigEWS.BOARD_SIZE = 3;
                ConfigEWS.NUM_PLAYERS = 2;
                break;
            }
            case"5x5 2-Player": {
                ConfigEWS.BOARD_SIZE = 5;
                ConfigEWS.NUM_PLAYERS = 2;
                break;
            }
            case"6x6 3-Player":{
                ConfigEWS.BOARD_SIZE = 6;
                ConfigEWS.NUM_PLAYERS = 3;
                break;
            }
            case"4x4 4-Player": {
                ConfigEWS.BOARD_SIZE = 4;
                ConfigEWS.NUM_PLAYERS = 4;
                break;
            }
            case"6x6 4-Player": {
                ConfigEWS.BOARD_SIZE = 6;
                ConfigEWS.NUM_PLAYERS = 4;
                break;
            }
            default: break;
        }
    }







    public static void main(String[] args) {
        ArenaTrainEWS t_Frame = new ArenaTrainEWS("General Board Game Playing",true);
        if(args.length == 0) {
            t_Frame.init();
        }else throw new RuntimeException("[Arena.main] args="+args+ "not allowed");
    }
}
