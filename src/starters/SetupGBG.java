package starters;

import games.Arena;
import games.BlackJack.ArenaBlackJack;
import games.CFour.ArenaC4;
import games.EWN.ArenaEWN;
import games.Hex.ArenaHex;
import games.KuhnPoker.ArenaKuhnPoker;
import games.Nim.ArenaNim2P;
import games.Nim.ArenaNim3P;
import games.Othello.ArenaOthello;
import games.Poker.ArenaPoker;
import games.RubiksCube.ArenaCube;
import games.Sim.ArenaSim;
import games.TicTacToe.ArenaTTT;
import games.Yavalath.ArenaYavalath;
import games.ZweiTausendAchtundVierzig.Arena2048;

/**
 * Base class for both {@link GBGLaunch} and {@link GBGBatch}, bundles common functionality.
 */
public class SetupGBG {
    /**
     * Set default values for the scalable parameters.
     * <p>
     * This is for the case where {@link GBGLaunch} is started with {@code args[0]=0}, which means "Start the game directly,
     * w/o launcher UI" or for the case where {@link GBGBatch} is started without {@code args[6], args[7], args[8]}.
     */
    public static String[] setDefaultScaPars(String selectedGame) {
        String[] scaPar = new String[3];
        switch(selectedGame) {
            case "Hex":
                scaPar[0]="6";		// the initial (recommended) value
                break;
            case "Nim":
                scaPar[0]="3";		//
                scaPar[1]="5";		// the initial (recommended) values
                scaPar[2]="5";		//
                break;
            case "Nim3P":
                scaPar[0]="3";		//
                scaPar[1]="5";		// the initial (recommended) values
                scaPar[2]="true";	//
                break;
            case "Sim":
                scaPar[0]="2";
                scaPar[1]="6";
                scaPar[2]="None";
                break;
            case "RubiksCube":
                scaPar[0]="2x2x2";
                scaPar[1]="STICKER2";
                scaPar[2]="ALL";
                break;
            case "EWN":
                scaPar[0] = "3x3 2-Player";
                scaPar[1]="[0,..,n]";
                scaPar[2]="False";
                break;
            case "Yavalath":
                scaPar[0] = "2";
                scaPar[1] = "5";
                break;
            case "2048":
            case "Blackjack":
            case "ConnectFour":
            case "Othello":
            case "Poker":
            case "KuhnPoker":
            case "TicTacToe":
                //
                // games with no scalable parameters
                //
                scaPar[0]=scaPar[1]=scaPar[2]="";
                break;
            default:
                System.err.println("[SetupGBG.setDefaulScaPars] "+selectedGame+": This game is unknown.");
                System.exit(1);
        }
        return scaPar;
    }

    /**
     * @param selectedGame      game name
     * @param scaPar            scalable parameters
     * @param title             title string for GUI
     * @param withUI            whether we have a GUI or not
     * @param withTrainRights   whether {@link Arena} has train rights or not
     * @return the new {@link Arena} object for the specific game
     */
    public static Arena setupSelectedGame(String selectedGame, String[] scaPar, String title,
                                          boolean withUI, boolean withTrainRights)
    {
        switch (selectedGame) {
            // Set configurable parameters of the game, e.g. for games Sim, Hex, Nim
            //      ConfigSim.{NUM_PLAYERS,NUM_NODES}       or
            //      HexConfig.BOARD_SIZE        or
            //      NimConfig.{NUMBER_HEAPS,HEAP_SIZE,MAX_MINUS}
            // *prior* to calling constructor ArenaXYZ, which will directly call Arena's constructor where
            // the game board and the Arena buttons are constructed
            case "2048":
                return new Arena2048(title, withUI, withTrainRights);
            case "Blackjack":
                return new ArenaBlackJack(title,withUI);
            case "ConnectFour":
                return new ArenaC4(title, withUI,withTrainRights);
            case "Hex":
                ArenaHex.setBoardSize(Integer.parseInt(scaPar[0]));
                return new ArenaHex(title, withUI,withTrainRights);
            case "Nim":
                ArenaNim2P.setNumHeaps(Integer.parseInt(scaPar[0]));
                ArenaNim2P.setHeapSize(Integer.parseInt(scaPar[1]));
                ArenaNim2P.setMaxMinus(Integer.parseInt(scaPar[2]));
                return new ArenaNim2P(title, withUI,withTrainRights);
            case "Nim3P":
                ArenaNim3P.setNumHeaps(Integer.parseInt(scaPar[0]));
                ArenaNim3P.setHeapSize(Integer.parseInt(scaPar[1]));
                ArenaNim3P.setMaxMinus(Integer.parseInt(scaPar[1]));    // Nim3P: always MaxMinus == HeapSize (!)
                ArenaNim3P.setExtraRule(Boolean.parseBoolean(scaPar[2]));
                return new ArenaNim3P(title, withUI,withTrainRights);
            case "Othello":
                return new ArenaOthello(title, withUI,withTrainRights);
            case "Poker":
                return new ArenaPoker(title,withUI);
            case "KuhnPoker":
                return new ArenaKuhnPoker(title,withUI,withTrainRights);
            case "RubiksCube":
                ArenaCube.setCubeType(scaPar[0]);
                ArenaCube.setBoardVecType(scaPar[1]);
                ArenaCube.setTwistType(scaPar[2]);
                return new ArenaCube(title, withUI,withTrainRights);
            case "Sim":
                ArenaSim.setNumPlayers(Integer.parseInt(scaPar[0]));
                ArenaSim.setNumNodes(Integer.parseInt(scaPar[1]));
                ArenaSim.setCoalition(scaPar[2]);
                return new ArenaSim(title, withUI,withTrainRights);
            case "TicTacToe":
                return new ArenaTTT(title, withUI,withTrainRights);
            case "EWN":
                ArenaEWN.setConfig(scaPar[0]);
                ArenaEWN.setCellCoding(scaPar[1]);
                ArenaEWN.setRandomStartingPosition(scaPar[2]);
                return new ArenaEWN(title, withUI,withTrainRights);
            case "Yavalath":
                ArenaYavalath.setPlayerNumber(Integer.parseInt(scaPar[0]));
                ArenaYavalath.setBoardSize(Integer.parseInt(scaPar[1]));
                return new ArenaYavalath(title,withUI);
            default:
                System.err.println("[SetupGBG.setupSelectedGame] args[0]=" + selectedGame + ": This game is unknown.");
                System.exit(1);
                return null;
        }
    }

}
