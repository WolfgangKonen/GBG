package games.Othello;

import java.util.ArrayList;
import java.util.Random;

import controllers.PlayAgent;
import games.Arena;
import games.EWN.StateObserverEWN;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import games.Othello.Gui.GameBoardOthelloGui;
import games.RubiksCube.GameBoardCube;
import tools.Types;


/**
 * This class implements the GameBoard interface for Othello.
 * Its member {@link GameBoardOthelloGui} {@code m_gameGui} has the game board GUI. 
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoardOthelloGui}'s
 * action handlers
 * 
 * @author Julian Coeln, Yannick Dittmar, TH Koeln, 2019
 */
public class GameBoardOthello extends GameBoardBase implements GameBoard {

	/**
	 * SerialNumber
	 */
	private static final long serialVersionUID = 12L;
	
	/**
	 * Game Attributes
	 */
	private StateObserverOthello m_so;
	protected Random rand;

	private transient GameBoardOthelloGui m_gameGui = null;
	
	public GameBoardOthello(Arena arena) {
		super(arena);
		initGameBoard();
	}
	
	/**
	 * Initialising the game board using {@code initBoard()} and other game relevant information
	 * @param arena	the parent Arena object
	 */
	public void initGameBoard()
	{
		m_so = new StateObserverOthello();
		rand = new Random();
        if (getArena().hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoardOthelloGui(this);
        }
	}
	
	@Override
	public void initialize() {}

	/**
	 * update game-specific parameters from {@link Arena}'s param tabs
	 */
	@Override
	public void updateParams() {}

	/**
	 * Resets the game to it starting state {@link StateObserverOthello}
	 */
	@Override
	public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
		if(boardClear) {
			m_so = new StateObserverOthello();
		}
							// considerable speed-up during training (!)
        if (m_gameGui!=null && getArena().taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(vClear);
	}

	@Override
	public void setStateObs(StateObservation so) {
		StateObserverOthello soN=null;
		if (so!=null) {
			assert (so instanceof StateObserverOthello)
					: "StateObservation 'so' is not an instance of StateObserverOthello";
			soN = (StateObserverOthello) so;
			m_so = soN; //.copy();
		}
	}

	@Override
	public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
		setStateObs(so);    // asserts that so is StateObserverOthello

		if (m_gameGui!=null)
			m_gameGui.updateBoard(so, withReset, showValueOnGameboard);
	}
	
	
	/**
	 * Human places a disc on board[x][y]
	 * @param x	index for board[x][]
	 * @param y index for board[][y]
	 */
	public void hGameMove(int x, int y) {
		int iAction = ConfigOthello.BOARD_SIZE * x + y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if( m_so.isLegalAction(act)) {			
			m_so.advance(act, null);
			updateBoard(m_so,false,false);   // show the human move (while agent might think about next move)
			(getArena().getLogManager()).addLogEntry(act, m_so, getArena().getLogSessionID());
			setActionReq(true);
		}
		else {
			System.out.println("Not Allowed: illegal Action");
		}
	}
	
	public void inspectMove(int x, int y)
	{
		int iAction = ConfigOthello.BOARD_SIZE * x + y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if(m_so.isLegalAction(act)) {
			getArena().setStatusMessage("Inspecting the value function ...");
			m_so.advance(act, null);
		}else {getArena().setStatusMessage("Desired Action is not legal");}
		setActionReq(true);
	}
		
	@Override
	public StateObservation getStateObs() {
		return m_so;
	}

	@Override
	public String getSubDir() {
		return null;
	}

	@Override
	public StateObservation getDefaultStartState(Random cmpRand) {
		clearBoard(true,true, null);
		return m_so;
	}

	/**
	 * Same as {@link #chooseStartState()}. (Only for {@link GameBoardCube} this method is different.)
	 */
	@Override
	public StateObservation chooseStartState(PlayAgent pa) {
		return chooseStartState();
	}

	/**
	 * Choose with probability 0.3 the default start state and with probability 0.7
	 * one of the 244 Othello states which are 4 plies away from the default start state.
	 */
	@Override
	public StateObservation chooseStartState() {
		getDefaultStartState(null);				// m_so is in default start state
		if (rand.nextDouble()>0.3) {
			for (int k=0; k<4; k++) {
				// choose randomly one of the possible actions in default 
				// start state and advance m_so by one ply
				ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
				int i = rand.nextInt(acts.size());
				m_so.advance(acts.get(i), null);
			}
		}
		return m_so;
	}
	
	@Override
	public void enableInteraction(boolean enable) {
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