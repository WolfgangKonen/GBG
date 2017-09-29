package tools;


import java.awt.Color;
import java.util.ArrayList;

import controllers.PlayAgent;

import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 * Class <b>Types</b> holds various constants and the class {@link Types.ACTIONS}
 *
 */
public class Types {

	/**
	 *  Class ACTIONS holds an action + information whether it was selected at random. 
	 *  
	 *  @see Types.ACTIONS_VT
	 */
    public static class ACTIONS implements Serializable, Comparable<ACTIONS> {
        private int key;
        private boolean randomSelect = false; // true, if this action was selected at random

		/**
    	 * change the version ID for serialization only if a newer version is no longer 
    	 * compatible with an older one (older .agt.zip will become unreadable or you have
    	 * to provide a special version transformation)
    	 */
    	private static final long  serialVersionUID = 12L;

        public ACTIONS(int numVal) {			
            this.key = numVal;
        }

        public ACTIONS(int numVal, boolean random) {			
            this.key = numVal;
            this.randomSelect = random;
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

            return key == ((ACTIONS) object).key;
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
        
    	public boolean isRandomAction() {
			return randomSelect;
		}

		public void setRandomSelect(boolean randomSelect) {
			this.randomSelect = randomSelect;
		}
    } // class ACTIONS

	/**
	 *  Class ACTIONS_VT (action with VTable) is derived from ACTIONS. It has the additional members 
	 *  <ul>
	 *  <li> double[] vTable: the game values for all other available actions when this action
	 *  	 is created via PlayAgent.getNextAction2(so,...) 
	 *  <li> double   vBest: the game value for the best action returned from 
	 *  	 PlayAgent.getNextAction2(so,...)
	 *  </ul>
	 *  
	 *  @see Types.ACTIONS
	 */
    public static class ACTIONS_VT extends ACTIONS implements Serializable, Comparable<ACTIONS> {
        private double[] vTable;
        private double   vBest;

		/**
    	 * change the version ID for serialization only if a newer version is no longer 
    	 * compatible with an older one (older .agt.zip will become unreadable or you have
    	 * to provide a special version transformation)
    	 */
    	private static final long  serialVersionUID = 12L;

        public ACTIONS_VT(int numVal) {			
            super(numVal);
        }

        public ACTIONS_VT(int numVal, boolean random) {			
            super(numVal,random);
        }

        /**
         * 
         * @param numVal	action code
         * @param random	flag for random move	
         * @param vtable	game values for all K available actions (+1)
         * <p>
         * It is assumed that {@code vtable} has K+1 elements and vtable[K] is the game value
         * for {@code this} action. 
         */
        public ACTIONS_VT(int numVal, boolean random, double [] vtable) {			
            super(numVal,random);
            this.vTable = vtable.clone();
            this.vBest = vtable[vtable.length-1];
        }

        /**
         * 
         * @param numVal	action code
         * @param random	flag for random move	
         * @param vtable	game values for all K available actions 
         * @param vbest		game value for {@code this} action. 
         */
        public ACTIONS_VT(int numVal, boolean random, double [] vtable, double vbest) {			
            super(numVal,random);
            this.vTable = vtable.clone();
            this.vBest = vbest;
        }

        public double[] getVTable() {
        	return vTable;
        }
        
        public double getVBest() {
        	return vBest;
        }
    } // class ACTIONS_VT


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

    //
    // Default (startup) settings for Arena and ArenaTrain
    //
    /**
     * list of available agents = list of choices in Agent Selectors
     */
    public static final String[] GUI_AGENT_LIST 	 
    	= {"Random", "Minimax", "MC", "MCTS", "MCTS Expectimax", "Human", "TD-Ntuple", "TD-Ntuple-2", "TDS"};
    /**
     * initial agent choice for P0, P1, ... (for up to 5 players) 
     */
    public static final String[] GUI_AGENT_INITIAL  
    	//= {"TD-Ntuple", "MC", "Human", "Human", "Human"};
    	= {"MCTS", "MC", "MCTS Expectimax", "Human", "Human", "Human"};
		//= {"TD-Ntuple-2", "Human", "MCTS Expectimax", "Human", "Human", "Human"};
    public static final String[] GUI_PLAYER_NAME  	// player names for P0, P1, ... (for up to 5 players)
    	//= {"P0", "P1", "P2", "P3", "P4"};
    	= {"0", "1", "2", "3", "4"};
    public static final String[] GUI_2PLAYER_NAME  	// player names for 2-player game
		= {"X", "O"};

    // probably GUI_X_PLAYER and GUI_O_PLAYER is not necessary anymore:
    public static final String GUI_X_PLAYER = "TDS";  	// "MCTS" "TDS" "CMA-ES" "Minimax" 
    public static final String GUI_O_PLAYER = "MCTS";	// "Human";"MCTS";

	public static final int GUI_ARENATRAIN_WIDTH = 465;	// width ArenaTrain window
	public static final int GUI_ARENATRAIN_HEIGHT = 420;
	public static final int GUI_WINCOMP_WIDTH = 350;	// width 'Competition Options' window
	public static final int GUI_WINCOMP_HEIGHT = 300;
	public static final int GUI_PARAMTABS_WIDTH = 464;	// wide enough to hold 6 tabs
	public static final int GUI_PARAMTABS_HEIGHT = 300;

	public static final int GUI_HELPFONTSIZE = 14;

	/**
	 * Default directory for loading and saving PlayAgents
	 */
	public static final String GUI_DEFAULT_DIR_AGENT = "agents";
	
	public static final double TD_HORIZONCUT = 0.1;		// see NTuple2ValueFunc.setHorizon()
	
 }
