package games.Nim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import controllers.PlayAgent;
import games.GameBoard;
import games.StateObservation;
import games.Arena;
import tools.Types;

/**
 * This class implements the game board for the game Nim3P = Nim <b>for 3 players</b>:
 * <p>
 * There are {@link NimConfig#NUMBER_HEAPS} heaps, each having initially {@link NimConfig#HEAP_SIZE}
 * items. There are <b>three</b> players and each player removes between 1 and {@link NimConfig#HEAP_SIZE} 
 * items from one heap in each move. <br>
 * <b>Special 3-player rule</b> [Luckhardt86]: The player <b>who comes after</b> the player removing the last item 
 * is the player who wins. He/she gets reward 1, the other two get reward 0. <br>
 * Optional <b>extra rule</b>, if {@link NimConfig#EXTRA_RULE} is {@code true}: The player who cames after the winning
 * player gets an extra reward of 0.2. (This helps to make some indifferent states decidable.)
 * <p>
 * This class implements the GameBoard interface for Nim3P.
 * Its member {@link GameBoardNimGui} {@code m_gameGui} has the game board GUI. 
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoardNimGui}'s
 * action handlers
 * 
 * @author Wolfgang Konen, TH Koeln, 2020
 */
public class GameBoardNim3P extends GameBoardNimBase implements GameBoard {

	protected StateObserverNim3P m_so;
	protected Random rand;
	private transient GameBoardNimGui m_gameGui = null;

	public GameBoardNim3P(Arena nimGame) {
		super(nimGame);
		initGameBoard();
//		clearBoard(true,true);
	}
	
	public int[] getHeaps() { return m_so.getHeaps(); }
	
    @Override
    public void initialize() {}

    private void initGameBoard()
	{
		m_so		= new StateObserverNim3P();	// heaps according to NimConfig
        rand 		= new Random(System.currentTimeMillis());
        if (getArena().hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoardNimGui(this);
        }
	}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
		if (boardClear) {
			m_so = new StateObserverNim3P();			// heaps according to NimConfig
		}
							// considerable speed-up during training (!)
		if (m_gameGui!=null && getArena().taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
	}

	@Override
	public void setStateObs(StateObservation so) {
		if(so!=null){
			assert( so instanceof StateObserverNim3P):"StateObservation 'so' is not an instance of StateObserverNim3P";
			StateObserverNim3P soN = (StateObserverNim3P) so;
			m_so = soN;
		}
	}

	/**
	 * Update the play board and the associated values (labels) to the new state {@code so}.
	 * 
	 * @param so	the game state. If {@code null}, call only {@link GameBoardNimGui#guiUpdateBoard(boolean)}.
	 * @param withReset  if true, reset the board prior to updating it to state {@code so}
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean withReset, boolean showValueOnGameboard) {
		setStateObs(so);    // asserts that so is StateObserverNim3P

		if (m_gameGui!=null)
			m_gameGui.updateBoard((StateObserverNim3P) so, withReset, showValueOnGameboard);
	}

	protected void HGameMove(int x, int y)
	{
		// reverse: iAction = MAX_MINUS*heap + j
		int iAction = NimConfig.MAX_MINUS*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act, null);			// perform action (optionally add random elements from game
									// environment - not necessary in Nim)
		(getArena().getLogManager()).addLogEntry(act, m_so, getArena().getLogSessionID());
//		updateBoard(null,false,false);
		setActionReq(true);			// ask Arena for next action
	}
	
	protected void InspectMove(int x, int y)
	{
		// reverse: iAction = MAX_MINUS*heap + j
		int iAction = NimConfig.MAX_MINUS*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!m_so.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			getArena().setStatusMessage("Desired action is not legal");
			return;
		} else {
			getArena().setStatusMessage("Inspecting the value function ...");
		}
		m_so.advance(act, null);			// perform action (optionally add random elements from game
									// environment - not necessary in Nim)
//		updateBoard(null,false,false);
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
	 * @return a start state which is with probability 0.5 the empty board 
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
	@Override
	public StateObservation chooseStartState() {
		getDefaultStartState(null);			// m_so is in default start state
//		clearBoard(true, true);			// m_so is in default start state 
		if (rand.nextDouble()>0.5) {
			// choose randomly one of the possible actions in default 
			// start state and advance m_so by one ply
			ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
			int i = (int) (rand.nextInt(acts.size()));
			m_so.advance(acts.get(i), null);
		}
		return m_so;
	}

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
    

	@Override
	public String getSubDir() {
		DecimalFormat form = new DecimalFormat("00");
		return  "N"+form.format(NimConfig.NUMBER_HEAPS)+
				"-S"+form.format(NimConfig.HEAP_SIZE)+
				"-Extra"+(NimConfig.EXTRA_RULE ? "Yes" : "No");
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
