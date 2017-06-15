package tools;


import java.awt.Color;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import java.io.Serializable;

public class Types {

    public static class ACTIONS implements Serializable, Comparable<ACTIONS> {
        private int key;
        
    	/**
    	 * change the version ID for serialization only if a newer version is no longer 
    	 * compatible with an older one (older .agt.zip will become unreadable or you have
    	 * to provide a special version transformation)
    	 */
    	private static final long  serialVersionUID = 12L;

        ACTIONS(int numVal) {			// constructor
            this.key = numVal;
        }

        public int toInt() {
        	return(key);
        }

        public static ACTIONS fromInt(int iAct) {
        	return new ACTIONS(iAct);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof ACTIONS)) {
                return false;
            }

            ACTIONS action = (ACTIONS) object;
            return key == action.key;
        }

        @Override
        public int hashCode() {
            return key;
        }

        @Override
        public int compareTo(ACTIONS action) {
            if(key < action.key) {
                return -1;
            }

            if(key > action.key) {
                return 1;
            }

            return 0;
        }
    } // class ACTIONS


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

    //public static final int SCORE_DISQ = -1000;

    // Default (startup) settings for Arena and ArenaTrain
    public static final String[] GUI_AGENT_LIST 	// list of available agents = list of choices in Agent Selectors
    	= {"TDS", "Minimax", "Random", "MCTS", "Human", "MC", "TD-Ntuple", "MCTS Expectimax"};
    public static final String[] GUI_AGENT_INITIAL  // initial agent choice for P0, P1, ... (for up to 5 players)
    	//= {"TD-Ntuple", "MC", "Human", "Human", "Human"};
    	= {"MCTS", "MC", "MCTS Expectimax", "Human", "Human", "Human"};
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
    public static final Vector2d NIL = new Vector2d(-1, -1);

    public static final Vector2d NONE = new Vector2d(0, 0);
    public static final Vector2d RIGHT = new Vector2d(1, 0);
    public static final Vector2d LEFT = new Vector2d(-1, 0);
    public static final Vector2d UP = new Vector2d(0, -1);
    public static final Vector2d DOWN = new Vector2d(0, 1);

    public static final Vector2d[] BASEDIRS = new Vector2d[]{UP, LEFT, DOWN, RIGHT};
    public static int[] ALL_ACTIONS = new int[]{KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN,
                                                KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE};
    */

 }
