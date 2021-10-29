package games.EWN;

import controllers.PlayAgent;
import games.ArenaTrain;
import games.EWN.config.ConfigEWN;
import games.Evaluator;
import games.GameBoard;
import games.XNTupleFuncs;

public class ArenaTrainEWN extends ArenaTrain {
    public ArenaTrainEWN(String title, boolean withUI) {
        super(title, withUI);
    }

    @Override
    public String getGameName() {
        return "EWN";  //"Einstein Wuerfelt Nicht";
    }

    @Override
    public GameBoard makeGameBoard() {
        gb = new GameBoardEWN(this);
        return  gb;
    }


    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose) {
       return new EvaluatorEWN(pa,gb,stopEval,mode,verbose);
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsEWN();
    } // Reference to arena needed



    public static void setConfig(String str){
        switch(str){
            case"3x3 2-Player":{
                ConfigEWN.BOARD_SIZE = 3;
                ConfigEWN.NUM_PLAYERS = 2;
                break;
            }
            case"5x5 2-Player": {
                ConfigEWN.BOARD_SIZE = 5;
                ConfigEWN.NUM_PLAYERS = 2;
                break;
            }
            case"6x6 3-Player":{
                ConfigEWN.BOARD_SIZE = 6;
                ConfigEWN.NUM_PLAYERS = 3;
                break;
            }
            case"4x4 4-Player": {
                ConfigEWN.BOARD_SIZE = 4;
                ConfigEWN.NUM_PLAYERS = 4;
                break;
            }
            case"6x6 4-Player": {
                ConfigEWN.BOARD_SIZE = 6;
                ConfigEWN.NUM_PLAYERS = 4;
                break;
            }
            default: break;
        }
    }


    public static void setCellCoding(String str){
        switch(str){
            case "[0,..,n]": ConfigEWN.CEll_CODING = 0;
                break;
            case "[0,1],[2,3],[4,5]": ConfigEWN.CEll_CODING = 1;
                break;
        }
    }


    public static void setRandomStartingPosition(String str){
        System.out.println(str);
        switch (str){
            case "True":ConfigEWN.RANDOM_POSITION = true;
                break;
            case "False":ConfigEWN.RANDOM_POSITION = false;
                break;
        }
    }




    public static void main(String[] args) {
        ArenaTrainEWN t_Frame = new ArenaTrainEWN("General Board Game Playing",true);
        if(args.length == 0) {
            t_Frame.init();
        }else throw new RuntimeException("[Arena.main] args="+args+ "not allowed");
    }
}
