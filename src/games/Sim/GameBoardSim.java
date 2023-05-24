package games.Sim;

import java.util.ArrayList;
import java.util.Random;

import controllers.PlayAgent;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import games.Othello.Gui.GameBoardOthelloGui;
import games.Sim.Gui.GameBoardSimGui;
import tools.Types;
import tools.Types.ACTIONS;
import games.Arena;

/**
 * This class implements the GameBoard interface for Sim.
 * Its member {@link GameBoardSimGui} {@code m_gameGui} has the game board GUI. 
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoardOthelloGui}'s
 * action handlers
 * 
 * @author Percy Wuensch, Wolfgang Konen, TH Koeln, 2019-2020
 */
public class GameBoardSim extends GameBoardBase implements GameBoard {

	/**
	 * SerialNumber
	 */
	private static final long serialVersionUID = 12L;
	
	//Framework
	protected Arena m_Arena;
	public StateObserverSim m_so;		// is public so that the classes in games.Sim.Gui can access it
	private boolean arenaActReq = false;
	public boolean isEnabled = false;	// is public so that GameBoardSimGui.Mouse can access it
	
	protected Random rand;

	// debug: start with simpler start states in getDefaultStartState()
	private final boolean m_DEBG = false; // false; true;
	
	private transient GameBoardSimGui m_gameGui = null;
	
	public GameBoardSim(Arena simGame)
	{
		super(simGame);
		//Framework
		m_Arena = simGame;
		m_so = new StateObserverSim();
        rand 		= new Random(System.currentTimeMillis());	
		arenaActReq = false;
		
        if (m_Arena.hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoardSimGui(this);
        }
	}
	
	@Override
	public void initialize() {	}

	/**
	 * update game-specific parameters from {@link Arena}'s param tabs
	 */
	@Override
	public void updateParams() {}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
		if(boardClear) {
            m_so = new StateObserverSim();
		}
							// considerable speed-up during training (!)
        if (m_gameGui!=null && m_Arena.taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
	}

	@Override
	public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
		StateObserverSim soS=null;
		if (so!=null) {
	        assert (so instanceof StateObserverSim)
			: "StateObservation 'so' is not an instance of StateObserverSim";
	        soS = (StateObserverSim) so;
			m_so = soS; //.copy();
	
			// --- this is now all done in m_gameGui.updateBoard ---
//			if (so.isGameOver()) {				
//				ScoreTuple sc = soS.getGameScoreTuple();
//				int winner = sc.argmax();
//				if (sc.max()==0.0) winner = -2;	// tie indicator
////				int winner = som.getGameWinner3player();		// make getGameWinner3player obsolete
//				if(winner < 0)
//					System.out.println("Tie");
//				else
//					System.out.println(winner  + " has won");
//					
//			} else {
////				int player = soS.getPlayer();
////				switch(player) {
////					case(0): 	System.out.println("0 to move   "); break;
////					case(1):	System.out.println("1 to move   "); break;
////					case(2):	System.out.println("2 to move   "); break;
////				}
//			}
		} // if (so!=null)
		
		if (m_gameGui!=null)
			m_gameGui.updateBoard(soS, withReset, showValueOnGameboard);
	}

	@Override
	public boolean isActionReq() 
	{
		return arenaActReq;
	}

	@Override
	public void setActionReq(boolean actionReq) 
	{
		arenaActReq = actionReq;
	}

	@Override
	public StateObservation getStateObs() {
		return m_so;
	}

	
	@Override
	public String getSubDir() {
		return "K"+ConfigSim.NUM_NODES+"_Player"+ConfigSim.NUM_PLAYERS;
	}

	@Override
	public Arena getArena() 
	{
		return m_Arena;
	}

	/**
	 * @return the 'empty-board' start state. <br>
	 * If {@code m_DEBG==true}, another default start state is returned (see source code).
     * @param cmpRand
	 */
	@Override
	public StateObservation getDefaultStartState(Random cmpRand) {
		clearBoard(true, true, null);
		if (m_DEBG) {		// just simpler to learn and simpler to debug start states:
			// 1) If all ACTIONS below are active: a very simple position with only 3 moves left.
			//    X to move and ACTIONS(10) lets X win, the other 2 let O win.
			// 2) If ACTIONS(8) below is commented out: another simple position with 4 moves left.
			//    now O can win with ACTIONS(10), the other 3 let X win.
			// 3) If ACTIONS(1) and ACTIONS(6) below are commented out as well: a medium-simple 
			//    position with 6 moves left. O has 3 first moves to win.
			// 4) If ACTIONS(13) and ACTIONS(3) below are commented out as well: a medium-simple 
			//    position with 8 moves left. O has 5 first moves to win.
			// 5) If ACTIONS(11) and ACTIONS(12) below are commented out as well: a medium-simple 
			//    position with 10 moves left. O has 5 first moves to win.
			// 6) If ACTIONS(7) and ACTIONS(5) below are commented out as well: a medium-complex 
			//    position with 12 moves left. O has 10 first moves to win.
			// 7) If ACTIONS(2) and ACTIONS(4) below are commented out as well: identical to the 
			//    full K6-Sim, only the symmetry is broken by setting the first move to be link 0.
			// 	  (Makes the game tree smaller, but the game difficulty remains the same. The other 
			//    1st-ply moves lead to equivalent episodes). 14 moves to go. Surprisingly, 
			//    *every* 2nd-ply move of player O is a winning move (!)
			m_so.advance(new ACTIONS( 0), null);	// P0: node 1 to 2
//			m_so.advance(new ACTIONS( 2));	// P1: node 1 to 4
//			m_so.advance(new ACTIONS( 4));	// P0: node 1 to 6
//			m_so.advance(new ACTIONS( 7));	// P1: node 2 to 5
//			m_so.advance(new ACTIONS( 5));	// P0: node 2 to 3
//			m_so.advance(new ACTIONS(11));	// P1: node 3 to 6
//			m_so.advance(new ACTIONS(12));	// P0: node 4 to 5
//			m_so.advance(new ACTIONS(13));	// P1: node 4 to 6
//			m_so.advance(new ACTIONS( 3));	// P0: node 1 to 5
//			m_so.advance(new ACTIONS( 1));	// P1: node 1 to 3
//			m_so.advance(new ACTIONS( 6));	// P0: node 2 to 4
//			m_so.advance(new ACTIONS( 8));	// P1: node 2 to 6
		}
		return m_so;
	}

	@Override
	public StateObservation chooseStartState(PlayAgent pa) {
		return chooseStartState();
	}

	/**
	 * @return a start state which is with probability 0.5 the default start state 
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
	@Override
	public StateObservation chooseStartState() {
		getDefaultStartState(null);			// m_so is in default start state
		// /WK/ this part was missing before 2019-09-04:
		if (rand.nextDouble()>0.5) {
			// choose randomly one of the possible actions in default 
			// start state and advance m_so by one ply
			ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
			int i = rand.nextInt(acts.size());
			m_so.advance(acts.get(i), null);
		}
		return m_so;
	}

	@Override
	public void enableInteraction(boolean enable) {
		isEnabled =enable;
		if (m_gameGui!=null)
			m_gameGui.enableInteraction(enable);
	}

	@Override
	public void showGameBoard(Arena arena, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(arena, alignToMain);
	}

	@Override
	public void toFront() {
		if (m_gameGui!=null)
			m_gameGui.toFront();
	}

	@Override
	public void destroy() {
		if (m_gameGui!=null)
			m_gameGui.destroy();
	}

}
