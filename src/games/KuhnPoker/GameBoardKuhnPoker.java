package games.KuhnPoker;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.Hex.StateObserverHex;
import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.Random;

public class GameBoardKuhnPoker extends GameBoardBase implements GameBoard {

	protected Random rand;
	//private transient GameBoardPokerGui m_gameGui = null;
	private transient GameBoardKuhnPokerGui m_gameGui = null;

	protected StateObserverKuhnPoker m_so;

	private boolean waitAtEndOfRound = true;

	public GameBoardKuhnPoker(Arena pokerGame) {
		super(pokerGame);
		initGameBoard(pokerGame);
	}

	public GameBoardKuhnPoker() {
		super(null);
		initGameBoard(null);
	}
	
    @Override
    public void initialize() {}

    private void initGameBoard(Arena arGame) 
	{
		m_so		= new StateObserverKuhnPoker();
        rand 		= new Random(System.currentTimeMillis());	
        if (getArena()!=null&& getArena().hasGUI() && m_gameGui==null) {
			m_gameGui = new GameBoardKuhnPokerGui(this);
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
			m_so = new StateObserverKuhnPoker();
			if (getArena()!=null&&getArena().hasGUI() && m_gameGui!=null) {
				m_gameGui.resetLog();
			}
		}
	}

	@Override
	public void setStateObs(StateObservation so) {
		StateObserverKuhnPoker soT;

		if (so != null) {
			assert (so instanceof StateObserverKuhnPoker)
					: "StateObservation 'so' is not an instance of StateObserverHex";
			soT = (StateObserverKuhnPoker) so;
			m_so = soT; //.copy();
		}
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
		if(so!=null) {
			setStateObs(so);		// asserts that so is StateObserverKuhnPoker
			StateObserverKuhnPoker soT = (StateObserverKuhnPoker) so;
			if (m_gameGui != null)
				m_gameGui.updateBoard(soT, withReset, showValueOnGameboard);

		}
	}


	protected void inspectMove(int x){
		Types.ACTIONS act = Types.ACTIONS.fromInt(x);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act, null);
		this.setActionReq(true);
	}

	// Human Game Move
	protected void HGameMove(int x)
	{
		Types.ACTIONS act = Types.ACTIONS.fromInt(x);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		//getArena().roundOverWait = true;
		m_so.advance(act, null);
		//getArena().roundOverWait = m_so.isRoundOver();

		if(getArena()!=null)
			(getArena().getLogManager()).addLogEntry(act, m_so, getArena().getLogSessionID());

		//	this.setActionReq(!m_so.isRoundOver());
		this.setActionReq(true);
		//if(m_so.isRoundOver()){
		//	updateBoard(m_so,false,false);
		//}
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
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
	@Override
	public StateObservation chooseStartState() {
		getDefaultStartState(null);			// m_so is in default start state
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
	public void showGameBoard(Arena pokerGame, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(pokerGame, alignToMain);
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

	/**
	 * Pass through the number of players in the game
	 * @return number of players
	 */
	public int getNumPlayers(){
		return m_so.getNumPlayers();
	}

	public boolean getWaitAtEndOfRound(){
		return waitAtEndOfRound;
	}
}
