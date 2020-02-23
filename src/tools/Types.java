package tools;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import controllers.MaxNAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.TD.ntuple2.NTuple2ValueFunc;
import games.Arena;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import tools.ScoreTuple;

import java.awt.event.KeyEvent;
import java.io.Serializable;

/**
 * Class <b>Types</b> holds various constants and the  
 * class {@link ACTIONS} and its derived classes.
 *
 */
public class Types {

	/**
	 *  Class ACTIONS holds an action + information whether it was selected at random. 
	 *  
	 *  @see ACTIONS_VT
	 *  @see ACTIONS_ST
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
        
        public ACTIONS(ACTIONS oa) {
            this.key = oa.key;
            this.randomSelect = oa.randomSelect;        	
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
     *  ACTIONS_ST = ACTIONS + ScoreTuple (for best action)
     *
     *  @see ACTIONS
     *  @see ACTIONS_VT
     *  @see MaxNAgent
     */
    public static class ACTIONS_ST extends ACTIONS {
    	public ScoreTuple m_st;
    	public ACTIONS_ST(Types.ACTIONS oa, ScoreTuple st) {
    		super(oa);
    		m_st = new ScoreTuple(st); 
    	}
    }
    
	/**
	 *  Class ACTIONS_VT (= ACTIONS + VTable) is derived from ACTIONS. 
	 *  It has the additional members 
	 *  <ul>
	 *  <li> double[] vTable: the game values for all other available actions when this action
	 *  	 is created via PlayAgent.getNextAction2(so,...) 
	 *  <li> double   vBest: the game value for the best action returned from 
	 *  	 PlayAgent.getNextAction2(so,...)
	 *  <li> ScoreTuple scBest
	 *  </ul>
	 *  
	 *  @see ACTIONS
	 *  @see ACTIONS_ST
	 */
    public static class ACTIONS_VT extends ACTIONS implements Serializable, Comparable<ACTIONS> {
        private double[] vTable;
        private double   vBest;
        private ScoreTuple scBest;

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
         * @param numVal	action code
         * @param random	flag for random move	
         * @param vtable	game values for all K available actions 
         * @param vbest		game value for {@code this} action. 
         */
        public ACTIONS_VT(int numVal, boolean random, double [] vtable, double vbest) {			
            this(numVal,random,vtable,vbest,null);
        }

        /**
         * @param numVal	action code
         * @param random	flag for random move	
         * @param vtable	game values for all K available actions 
         * @param vbest		game value for {@code this} action. 
         * @param scBest	score tuple for {@code this} action. 
         */
        public ACTIONS_VT(int numVal, boolean random, double [] vtable, double vbest, ScoreTuple scBest) {			
            super(numVal,random);
            this.vTable = vtable.clone();
            this.vBest = vbest;
            this.scBest = scBest;
        }

        public double[] getVTable() {
        	return vTable;
        }
        
        public double getVBest() {
        	return vBest;
        }
        
        public ScoreTuple getScoreTuple() {
        	return scBest;
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
    	= {"Random", "Max-N", "Expectimax-N", /*"MC",*/ "MC-N", /*"MCTS0",*/ 
    	   "MCTS", "MCTS Expectimax", "Human", /*"TD-Ntuple",*/ "TD-Ntuple-2", "TD-Ntuple-3", 
    	   "Sarsa", /*"Sarsa-2",*/ "TDS"};
    /**
     * initial agent choice for P0, P1, ... (for up to 5 players) 
     */
    public static final String[] GUI_AGENT_INITIAL  
    	//= {"MCTS", "MC", "MCTS Expectimax", "Human", "Human", "Human"};
    	//= {"Human", "MCTS", "Human", "Human", "Human", "Human"};
    	//= {"TD-Ntuple-3", "MCTS", "Human", "Human", "Human", "Human"};
		= {"MCTS", "MC", "MCTS Expectimax", "Human", "Human", "Human"};
    public static final String[] GUI_PLAYER_NAME  	// player names for P0, P1, ... (for up to 5 players)
    	//= {"P0", "P1", "P2", "P3", "P4"};
    	= {"0", "1", "2", "3", "4"};
    public static final String[] GUI_2PLAYER_NAME  	// player names for 2-player game
		= {"X", "O"};
    public static final Color[] GUI_PLAYER_COLOR  	// player colors for P0, P1, ... (for up to 5 players)
		= {Color.BLACK, Color.WHITE, Color.BLUE, Color.RED, Color.ORANGE};

    // GUI_X_PLAYER and GUI_O_PLAYER is not necessary anymore:
//  public static final String GUI_X_PLAYER = "TDS";  	// "MCTS" "TDS" "CMA-ES"  
//  public static final String GUI_O_PLAYER = "MCTS";	// "Human";"MCTS";

    /**
     * A global factor to scale all GUI components when working on larger / smaller displays. <br>
     * The factor 1.0 is appropriate for display size (1600 x 900).
     */
    public static double GUI_SCALING_FACTOR = 1.0;
    public static double GUI_SCALING_FACTOR_X = GUI_SCALING_FACTOR;
    public static double GUI_SCALING_FACTOR_Y = GUI_SCALING_FACTOR;
    
    /**
     * width and height of Arena and ArenaTrain windows
     */
	public static int GUI_ARENATRAIN_WIDTH = (int)(465*GUI_SCALING_FACTOR_X);	 
	public static int GUI_ARENATRAIN_HEIGHT = (int)(380*GUI_SCALING_FACTOR_Y);
	public static int GUI_ARENA_WIDTH = (int)(465*GUI_SCALING_FACTOR_X);	 
	public static int GUI_ARENA_HEIGHT = (int)(315*GUI_SCALING_FACTOR_Y);
    /**
     * width and height of 'Competition Options'  window
     */
	public static int GUI_WINCOMP_WIDTH = (int)(200*GUI_SCALING_FACTOR_X);	
	public static int GUI_WINCOMP_HEIGHT = (int)(250*GUI_SCALING_FACTOR_Y);
    /**
     * width and height of Param Tabs window, wide enough to hold 6 tabs
     */
	public static int GUI_PARAMTABS_WIDTH = (int)(464*GUI_SCALING_FACTOR_X); 
	public static int GUI_PARAMTABS_HEIGHT = (int)(330*GUI_SCALING_FACTOR_Y);

	public static int GUI_HELPFONTSIZE = (int)(14*GUI_SCALING_FACTOR_X);
	public static int GUI_MENUFONTSIZE = (int)(12*GUI_SCALING_FACTOR_X);
	public static int GUI_DIALOGFONTSIZE = (int)(12*GUI_SCALING_FACTOR_X);
	public static int GUI_TIPFONTSIZE = (int)(12*GUI_SCALING_FACTOR_X);
	public static int GUI_TITLEFONTSIZE = (int)(20*GUI_SCALING_FACTOR_X);
	
	
	static final int gray = 215;
	/**
	 * The background color for various GUI elements
	 */
	public static final Color GUI_BGCOLOR = new Color(gray,gray,gray);

	/**
	 * Default directory for loading and saving PlayAgents
	 */
	public static final String GUI_DEFAULT_DIR_AGENT = "agents";
	
	// obsolete now, use ParTD.getHorizonCut()
	//public static double TD_HORIZONCUT = 0.01;		// 0.1 or 0.01 
	
	/**
	 * Set all global GUI elements to the right scale. <br>
	 * (Call this function before setting any GUI elements: see {@link Arena}, initGame())
	 * 
	 * @param auto  If {@code auto==false}, set the global scale according
	 * to the value in {@link #GUI_SCALING_FACTOR}. <br>
	 * If {@code auto==true}, start from {@link #GUI_SCALING_FACTOR}, but perform additionally
	 * automatic scaling, based on the screen size of the current device.
	 */
	public static void globalGUIScaling(boolean auto) {
		if (auto) {
			// calculate scaling factors according to screen size and then recalculate
			// all window sizes and font sizes:
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			double width = screenSize.getWidth();
			double height = screenSize.getHeight();
			GUI_SCALING_FACTOR_X = GUI_SCALING_FACTOR*(width/1600);
			GUI_SCALING_FACTOR_Y = GUI_SCALING_FACTOR*(height/900);
		}
		
		GUI_ARENATRAIN_WIDTH = (int)(465*GUI_SCALING_FACTOR_X);	 
		GUI_ARENATRAIN_HEIGHT = (int)(380*GUI_SCALING_FACTOR_Y);
		GUI_WINCOMP_WIDTH = (int)(200*GUI_SCALING_FACTOR_X);	
		GUI_WINCOMP_HEIGHT = (int)(210*GUI_SCALING_FACTOR_Y);
		GUI_PARAMTABS_WIDTH = (int)(464*GUI_SCALING_FACTOR_X); 
		GUI_PARAMTABS_HEIGHT = (int)(330*GUI_SCALING_FACTOR_Y);

		GUI_TITLEFONTSIZE = (int)(20*GUI_SCALING_FACTOR_X);
		GUI_HELPFONTSIZE = (int)(14*GUI_SCALING_FACTOR_X);
		GUI_DIALOGFONTSIZE = (int)(12*GUI_SCALING_FACTOR_X);
		GUI_MENUFONTSIZE = (int)(12*GUI_SCALING_FACTOR_X);
		GUI_TIPFONTSIZE = (int)(12*GUI_SCALING_FACTOR_X);
		
		// set globally the font for all dialog texts:
		Font lFont = new Font("Arial", Font.PLAIN, GUI_DIALOGFONTSIZE);
		UIManager.put("Label.font", lFont);
		UIManager.put("CheckBox.font", lFont);
		UIManager.put("CheckBoxMenuItem.font", lFont);
		UIManager.put("TextField.font", lFont);
		Font dFont = new Font("Arial", Font.BOLD, GUI_DIALOGFONTSIZE);
		UIManager.put("Button.font", dFont);
		UIManager.put("List.font", dFont);
		UIManager.put("RadioButton.font", dFont);
		UIManager.put("RadioButtonMenuItem.font", dFont);
		UIManager.put("TabbedPane.font", dFont);
		UIManager.put("ComboBox.font", dFont);
        // set globally the font for all menu texts:
		Font mFont=new Font("Arial",Font.BOLD,GUI_MENUFONTSIZE);
        UIManager.put("Menu.font", new FontUIResource(mFont));
        UIManager.put("MenuItem.font", new FontUIResource(mFont));
        // set globally the font for all tooltip texts:
        UIManager.put("ToolTip.font",
           new FontUIResource("SansSerif", Font.ITALIC, GUI_TIPFONTSIZE));
		
	}

 }

