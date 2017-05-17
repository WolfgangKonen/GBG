package tools;


import java.awt.Color;

import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Types {
	/*
    public static final int PHYSICS_NONE = -1;
    public static final int PHYSICS_GRID = 0;
    public static final int PHYSICS_CONT = 1;
    public static final int PHYSICS_NON_FRICTION = 2;
    public static final int PHYSICS_GRAVITY = 3;

    public static final int VGDL_GAME_DEF = 0;
    public static final int VGDL_SPRITE_SET = 1;
    public static final int VGDL_INTERACTION_SET = 2;
    public static final int VGDL_LEVEL_MAPPING = 3;
    public static final int VGDL_TERMINATION_SET = 4;
    */
    public static final Vector2d NIL = new Vector2d(-1, -1);

    public static final Vector2d NONE = new Vector2d(0, 0);
    public static final Vector2d RIGHT = new Vector2d(1, 0);
    public static final Vector2d LEFT = new Vector2d(-1, 0);
    public static final Vector2d UP = new Vector2d(0, -1);
    public static final Vector2d DOWN = new Vector2d(0, 1);

    public static final Vector2d[] BASEDIRS = new Vector2d[]{UP, LEFT, DOWN, RIGHT};

//    public static int[] ALL_ACTIONS = new int[]{KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN,
//                                                KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE};
    public static enum ACTIONS {
         ACTION_NIL(-1)
        ,ACTION_00(10)
        ,ACTION_01(11)
        ,ACTION_02(12)
        ,ACTION_03(13)
        ,ACTION_04(14)
        ,ACTION_05(15)
        ,ACTION_06(16)
        ,ACTION_07(17)
        ,ACTION_08(18)
        ,ACTION_09(19)
        ,ACTION_10(20)
        ,ACTION_11(21)
        ,ACTION_12(22)
        ,ACTION_13(23)
        ,ACTION_14(24)
        ,ACTION_15(25)
        ,ACTION_16(26)
        ,ACTION_17(27)
        ,ACTION_18(28)
        ,ACTION_19(29)
        ,ACTION_20(30)
        ,ACTION_21(31)
        ,ACTION_22(32)
        ,ACTION_23(33)
        ,ACTION_24(34)
        ,ACTION_25(35)
        ,ACTION_26(36)
        ,ACTION_27(37)
        ,ACTION_28(38)
        ,ACTION_29(39)
        ,ACTION_30(40)
        ,ACTION_31(41)
        ,ACTION_32(42)
//        ,ACTION_UP(new int[]{KeyEvent.VK_UP})
//        ,ACTION_LEFT(new int[]{KeyEvent.VK_LEFT})
//        ,ACTION_DOWN(new int[]{KeyEvent.VK_DOWN})
//        ,ACTION_RIGHT(new int[]{KeyEvent.VK_RIGHT})
//        ,ACTION_USE(new int[]{KeyEvent.VK_SPACE})
//        ,ACTION_ESCAPE(new int[]{KeyEvent.VK_ESCAPE})
        ;

        private int key;
        ACTIONS(int numVal) {			// constructor
            this.key = numVal;
        }

        public int toInt() {
        	switch(this) {
                case ACTION_00:
                    return 0;
                case ACTION_01:
                    return 1;
                case ACTION_02:
                    return 2;
                case ACTION_03:
                    return 3;
                case ACTION_04:
                    return 4;
                case ACTION_05:
                    return 5;
                case ACTION_06:
                    return 6;
                case ACTION_07:
                    return 7;
                case ACTION_08:
                    return 8;
                case ACTION_09:
                    return 9;
                case ACTION_10:
                    return 10;
                case ACTION_11:
                    return 11;
                case ACTION_12:
                    return 12;
                case ACTION_13:
                    return 13;
                case ACTION_14:
                    return 14;
                case ACTION_15:
                    return 15;
                case ACTION_16:
                    return 16;
                case ACTION_17:
                    return 17;
                case ACTION_18:
                    return 18;
                case ACTION_19:
                    return 19;
                case ACTION_20:
                    return 20;
                case ACTION_21:
                    return 21;
                case ACTION_22:
                    return 22;
                case ACTION_23:
                    return 23;
                case ACTION_24:
                    return 24;
                case ACTION_25:
                    return 25;
                case ACTION_26:
                    return 26;
                case ACTION_27:
                    return 27;
                case ACTION_28:
                    return 28;
                case ACTION_29:
                    return 29;
                case ACTION_30:
                    return 30;
                case ACTION_31:
                    return 31;
                case ACTION_32:
                    return 32;
                default:
                    return -1;
            }
        }
        
        public static ACTIONS fromInt(int iAct) {
        	switch (iAct) {
                case 0:
                    return ACTION_00;
                case 1:
                    return ACTION_01;
                case 2:
                    return ACTION_02;
                case 3:
                    return ACTION_03;
                case 4:
                    return ACTION_04;
                case 5:
                    return ACTION_05;
                case 6:
                    return ACTION_06;
                case 7:
                    return ACTION_07;
                case 8:
                    return ACTION_08;
                case 9:
                    return ACTION_09;
                case 10:
                    return ACTION_10;
                case 11:
                    return ACTION_11;
                case 12:
                    return ACTION_12;
                case 13:
                    return ACTION_13;
                case 14:
                    return ACTION_14;
                case 15:
                    return ACTION_15;
                case 16:
                    return ACTION_16;
                case 17:
                    return ACTION_17;
                case 18:
                    return ACTION_18;
                case 19:
                    return ACTION_19;
                case 20:
                    return ACTION_20;
                case 21:
                    return ACTION_21;
                case 22:
                    return ACTION_22;
                case 23:
                    return ACTION_23;
                case 24:
                    return ACTION_24;
                case 25:
                    return ACTION_25;
                case 26:
                    return ACTION_26;
                case 27:
                    return ACTION_27;
                case 28:
                    return ACTION_28;
                case 29:
                    return ACTION_29;
                case 30:
                    return ACTION_30;
                case 31:
                    return ACTION_31;
                case 32:
                    return ACTION_32;
                default:
                    return ACTION_NIL;
            }
        }

        public static ACTIONS fromString(String strKey)
        {
            if(strKey.equalsIgnoreCase("ACTION_00")) return ACTION_00;
            else if(strKey.equalsIgnoreCase("ACTION_01")) return ACTION_01;
            else if(strKey.equalsIgnoreCase("ACTION_02")) return ACTION_02;
            else if(strKey.equalsIgnoreCase("ACTION_03")) return ACTION_03;
            else if(strKey.equalsIgnoreCase("ACTION_04")) return ACTION_04;
            else if(strKey.equalsIgnoreCase("ACTION_05")) return ACTION_05;
            else if(strKey.equalsIgnoreCase("ACTION_06")) return ACTION_06;
            else if(strKey.equalsIgnoreCase("ACTION_07")) return ACTION_07;
            else if(strKey.equalsIgnoreCase("ACTION_08")) return ACTION_08;
            //ToDo: add Actions 09-32
//            if(strKey.equalsIgnoreCase("ACTION_UP")) return ACTION_UP;
//            else if(strKey.equalsIgnoreCase("ACTION_LEFT")) return ACTION_LEFT;
//            else if(strKey.equalsIgnoreCase("ACTION_DOWN")) return ACTION_DOWN;
//            else if(strKey.equalsIgnoreCase("ACTION_RIGHT")) return ACTION_RIGHT;
//            else if(strKey.equalsIgnoreCase("ACTION_USE")) return ACTION_USE;
//            else if(strKey.equalsIgnoreCase("ACTION_ESCAPE")) return ACTION_ESCAPE;
            else return ACTION_NIL;
        }

        public static ACTIONS fromVector(Vector2d move)
        {
            return ACTION_NIL;
        }
        
    } // enum ACTIONS


    public static enum WINNER {
        PLAYER_DISQ(-100),
        TIE(0),
        PLAYER_LOSES(-1),
        PLAYER_WINS(1);

        private int key;
        WINNER(int val) {key=val;}
        public int key() {return key;}
        public int toInt() {
            return this.key;
        }
    }

    public static final int[] PLAYER_PM = {+1,-1};
    
    public static final int SCORE_DISQ = -1000;
    
    // Default (startup) settings for Arena and ArenaTrain
    public static final String[] GUI_AGENT_LIST 	// list of available agents = list of choices in Agent Selectors
    	= {"TDS", "Minimax", "Random", "MCTS", "Human", "MC", "TD-Ntuple"};
    public static final String[] GUI_AGENT_INITIAL  // initial agent choice for P0, P1, ... (for up to 5 players)
    	//= {"TD-Ntuple", "MC", "Human", "Human", "Human"};
    	= {"MCTS", "MC", "Human", "Human", "Human"};
    public static final String[] GUI_PLAYER_NAME  	// player names for P0, P1, ... (for up to 5 players)
    	//= {"P0", "P1", "P2", "P3", "P4"};
    	= {"0", "1", "2", "3", "4"};
    public static final String[] GUI_2PLAYER_NAME  	// player names for 2-player game
		= {"X", "O"};
    
    // probably GUI_X_PLAYER and GUI_O_PLAYER is not necessary anymore:
    public static final String GUI_X_PLAYER = "TDS";  	// "MCTS" "TDS" "CMA-ES" "Minimax" 
    public static final String GUI_O_PLAYER = "MCTS";	// "Human";"MCTS";

	public static final int GUI_ARENATRAIN_WIDTH = 465;
	public static final int GUI_ARENATRAIN_HEIGHT = 420;
	public static final int GUI_WINCOMP_WIDTH = 350;
	public static final int GUI_WINCOMP_HEIGHT = 300;
	public static final int GUI_PARAMTABS_WIDTH = 464;	// wide enough to hold 6 tabs
	public static final int GUI_PARAMTABS_HEIGHT = 300;
	
	public static final int GUI_HELPFONTSIZE = 14;

	// Default directory for loading and saving PlayAgents
	public static final String GUI_DEFAULT_DIR_AGENT = "agents";
 }
