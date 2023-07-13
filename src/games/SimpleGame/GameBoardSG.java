package games.SimpleGame;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import tools.Types;

import java.util.Random;

/**
 * This class implements the GameBoard interface for SimpleGame.
// * Its member {@code GameBoardSGGui} {@code m_gameGui} has the game board GUI.
// * {@code m_gameGui} may be {@code null} in batch runs.
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
// * since these methods need access to local  members. They are called from {@code GameBoardSGGui}'s
// * action handlers
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 *
 */
public class GameBoardSG extends GameBoardBase implements GameBoard {

	protected Random rand;
	private transient GameBoardSGGui m_gameGui = null;

	protected StateObserverSG m_so;

	public GameBoardSG(Arena sgGame) {
		super(sgGame);
		initGameBoard();
	}
	
    @Override
    public void initialize() {}

    private void initGameBoard()
	{
		m_so		= new StateObserverSG();	// random start state
        rand 		= new Random(System.currentTimeMillis());	
        if (getArena().hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoardSGGui(this);
        }

	}

	/**
	 * update game-specific parameters from {@link Arena}'s param tabs
	 */
	@Override
	public void updateParams() {}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
		if (boardClear) {
			m_so = new StateObserverSG();		// random start state
		}
							// considerable speed-up during training (!)
        if (m_gameGui!=null && getArena().taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
	}

	@Override
	public void setStateObs(StateObservation so) {
		if (so!=null) {
			assert (so instanceof StateObserverSG)
					: "StateObservation 'so' is not an instance of StateObserverSG";
			m_so = (StateObserverSG) so;
		} // if(so!=null)
	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param so	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean withReset, boolean showValueOnGameboard) {
		setStateObs(so);    	// asserts that so is StateObserverSG
		
		if (m_gameGui!=null)
			m_gameGui.updateBoard((StateObserverSG) so, withReset, showValueOnGameboard);
	}

	protected void HGameMove(int x, int y)
	{
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act, null);			// perform action (optionally add random elements from game
													// environment - not necessary in TicTacToe)
		(getArena().getLogManager()).addLogEntry(act, m_so, getArena().getLogSessionID());
		setActionReq(true);			// ask Arena for next action
	}
	
	protected void InspectMove(int x, int y)
	{
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!m_so.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			getArena().setStatusMessage("Desired action is not legal");
			return;
		} else {
			getArena().setStatusMessage("Inspecting the value function ...");
		}
		m_so.advance(act, null);			// perform action (optionally add random elements from game
													// environment - not necessary in TicTacToe)
		setActionReq(true);
	}
	
	public StateObservation getStateObs() {
		return m_so;
	}

	/**
	 * @return the 'empty-board' start state
     * @param cmpRand
	 */
	@Override
	public StateObservation getDefaultStartState(Random cmpRand) {
		clearBoard(true, true, null);
		return m_so;
	}

	/**
	 * @return a start state which is with probability 0.5 the default start state 
	 * 		and with probability 0.5 one of the possible one-ply successors
	 */
	@Override
	public StateObservation chooseStartState() {
		return getDefaultStartState(null);			// m_so is in default start state
	}

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
    

	@Override
	public String getSubDir() {
		return null;
	}
	
	@Override
	public void enableInteraction(boolean enable) {
		if (m_gameGui!=null)
			m_gameGui.enableInteraction(enable);
	}

	@Override
	public void showGameBoard(Arena ticGame, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(ticGame, alignToMain);
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
