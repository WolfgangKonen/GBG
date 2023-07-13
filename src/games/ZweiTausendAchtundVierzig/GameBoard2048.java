package games.ZweiTausendAchtundVierzig;

import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import tools.Types;

import controllers.PlayAgent;

import java.util.Random;

/**
 * This class implements the GameBoard interface for 2048.
 * Its member {@link GameBoard2048Gui} {@code m_gameGui} has the game board GUI. 
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and 
 * InspectMove (used to enter legal moves during game play or 'Inspect'), 
 * since these methods need access to local  members. They are called from {@link GameBoard2048Gui}'s
 * action handlers
 * 
 * @author Johannes Kutsch, Wolfgang Konen, TH Koeln, 2016-2020
 */
public class GameBoard2048 extends GameBoardBase implements GameBoard {
    protected StateObserver2048 m_so;
	private transient GameBoard2048Gui m_gameGui = null;

    public GameBoard2048(Arena ztavGame) {
        super(ztavGame);
        initGameBoard();
    }

    @Override
    public void initialize() {}

    private void initGameBoard() {
        m_so = new StateObserver2048();
        if (getArena().hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoard2048Gui(this);
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
            m_so = m_so.reset(cmpRand);
            //m_so = new StateObserver2048();
        }
        					// considerable speed-up during training (!)
        if (m_gameGui!=null && getArena().taskState!=Arena.Task.TRAIN)
			m_gameGui.clearBoard(boardClear, vClear);
    }

    @Override
    public void setStateObs(StateObservation so) {
        if (so != null) {
            assert (so instanceof StateObserver2048)
                    : "StateObservation 'so' is not an instance of StateObserver2048";
            StateObserver2048 soZTAV = (StateObserver2048) so;
            m_so = soZTAV.copy();
        }
    }

    @Override
    public void updateBoard(StateObservation so,  
    						boolean withReset, boolean showValueOnGameboard) {
        setStateObs(so);    	// asserts that so is StateObserver2048

		if (m_gameGui!=null)
			m_gameGui.updateBoard((StateObserver2048) so, withReset, showValueOnGameboard);
    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public StateObservation getDefaultStartState(Random cmpRand) {
        clearBoard(true, true, cmpRand);
//        if (TDNTuple2Agt.DBG2_FIXEDSEQUENCE) 
//        	return new StateObserver2048();
        return m_so;
    }

    @Override
    public StateObservation chooseStartState() {
        return getDefaultStartState(null);
    }

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
	
    protected void HGameMove(int move) {
        Types.ACTIONS act = Types.ACTIONS.fromInt(move);
        assert m_so.isLegalAction(act) : "Desired action is not legal";
        m_so.advance(act, null);
		(getArena().getLogManager()).addLogEntry(act, m_so, getArena().getLogSessionID());
        setActionReq(true);            // ask Arena for next action
    }

    protected void InspectMove(int move) {
        Types.ACTIONS act = Types.ACTIONS.fromInt(move);
        assert m_so.isLegalAction(act) : "Desired action is not legal";
        m_so.advance(act, null);
        setActionReq(true);            // ask Arena for next action
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
