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
        	case ACTION_00: return 0;
        	case ACTION_01: return 1;
        	case ACTION_02: return 2;
        	case ACTION_03: return 3;
        	case ACTION_04: return 4;
        	case ACTION_05: return 5;
        	case ACTION_06: return 6;
        	case ACTION_07: return 7;
        	case ACTION_08: return 8;
        	default: return -1;
        	}
        }
        
        public static ACTIONS fromInt(int iAct) {
        	switch (iAct) {
        	case 0: return ACTION_00;
        	case 1: return ACTION_01;
        	case 2: return ACTION_02;
        	case 3: return ACTION_03;
        	case 4: return ACTION_04;
        	case 5: return ACTION_05;
        	case 6: return ACTION_06;
        	case 7: return ACTION_07;
        	case 8: return ACTION_08;
        	default: return ACTION_NIL;
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

    public static final int SCORE_DISQ = -1000;
    
    // Default (startup) settings for Arena and ArenaTrain
    public static final String[] GUI_AGENT_LIST 	// list of available agents = list of choices in Agent Selectors
    	= {"TDS", "Minimax", "Random", "MCTS", "Human", "MC", "TD-Ntuple"};
    public static final String[] GUI_AGENT_INITIAL  // initial agent choice for P0, P1, ... (for up to 5 players)
    	= {"TD-Ntuple", "MC", "Human", "Human", "Human"};
    	//= {"MCTS", "MC", "Human", "Human", "Human"};
    public static final String[] GUI_PLAYER_NAME  	// player names for P0, P1, ... (for up to 5 players)
    	//= {"P0", "P1", "P2", "P3", "P4"};
    	= {"0", "1", "2", "3", "4"};
    public static final String[] GUI_2PLAYER_NAME  	// player names for 2-player game
		= {"X", "O"};
    
    // probably GUI_X_PLAYER and GUI_O_PLAYER is not necessary anymore:
    public static final String GUI_X_PLAYER = "TDS";  	// "MCTS" "TDS" "CMA-ES" "Minimax" 
    public static final String GUI_O_PLAYER = "MCTS";	// "Human";"MCTS";

	public static final int GUI_ARENATRAIN_WIDTH = 465;
	public static final int GUI_ARENATRAIN_HEIGHT = 390;
	public static final int GUI_WINCOMP_WIDTH = 350;
	public static final int GUI_WINCOMP_HEIGHT = 300;
	public static final int GUI_PARAMTABS_WIDTH = 464;	// wide enough to hold 6 tabs
	public static final int GUI_PARAMTABS_HEIGHT = 300;
	
	public static final int GUI_HELPFONTSIZE = 14;

	// Default directory for loading and saving PlayAgents
	public static final String GUI_DEFAULT_DIR_AGENT = "agents";
 }
