package games.BlackJack;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import games.TicTacToe.GameBoardTTTGui;
import tools.Types;

/**
 * This class implements the GameBoard interface for BlackJack.
 * Its member {@link GameBoardBlackJackGui} {@code m_gameGui} has the game board GUI.
 * {@code m_gameGui} may be {@code null} in batch runs.
 * <p>
 * It implements the interface functions and has the user interaction methods HGameMove and
 * InspectMove (used to enter legal moves during game play or 'Inspect'),
 * since these methods need access to local  members. They are called from {@link GameBoardTTTGui}'s
 * action handlers
 *
 */
public class GameBoardBlackJack implements GameBoard {

    protected StateObserverBlackJack m_so;
    protected Arena m_Arena; // a reference to the Arena object, needed to
    // infer the current taskState
    private transient GameBoardBlackJackGui m_gameGui = null;


    private boolean arenaActReq = false;

    public GameBoardBlackJack(Arena bjArena) {
        initGameBoard(bjArena);
    }

    private void initGameBoard(Arena arGame) {
        m_Arena = arGame;
        if (m_Arena.hasGUI() && m_gameGui == null) {
            m_gameGui = new GameBoardBlackJackGui(this);
        }
        m_so = new StateObserverBlackJack();

    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if (boardClear) {
            m_so = new StateObserverBlackJack();
        }

        if (m_gameGui != null && m_Arena.taskState != Arena.Task.TRAIN)
            m_gameGui.clear();

    }

    @Override
    public void destroy() {
        if (m_gameGui != null)
            m_gameGui.destroy();

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
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
        StateObserverBlackJack soT = (StateObserverBlackJack) so;
        

        // /WK/ this was problematic, because Arena.PlayGame relies on working always with the same object
        //      so = gb.getStateObs()
        // because only in this way the information is passed around properly.
        // So DO NOT make a copy of m_so while being in-game!
//        if (so != null) {
//            assert (so instanceof StateObserverBlackJack)
//                    : "StateObservation 'so' is not an instance of StateObserverBlackJack";
//            m_so = (StateObserverBlackJack) soT.copy();
//        }

        if(so != null) {
            m_so = soT;
            if (m_gameGui != null)
                m_gameGui.update(
                        (StateObserverBlackJack) soT.partialState(),
                        withReset, showValueOnGameboard);
        }
    }

    @Override
    public void showGameBoard(Arena arena, boolean alignToMain) {
        if (m_gameGui != null)
            m_gameGui.showGameBoard(arena, alignToMain);

    }

    @Override
    public void toFront() {
        if (m_gameGui != null)
            m_gameGui.toFront();

    }

    /**
     * @return  true: if an action is requested from Arena or ArenaTrain
     * 			false: no action requested from Arena, next action has to come
     * 			from GameBoard (e.g. user input / human move)
     */
    @Override
    public boolean isActionReq() {
        return arenaActReq;
    }

    /**
     * @param	actionReq true : GameBoard requests an action from Arena
     * 			(see {@link #isActionReq()})
     */
    @Override
    public void setActionReq(boolean actionReq) {
        this.arenaActReq = actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {
        // TODO Auto-generated method stub

    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public String getSubDir() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Arena getArena() {
        return m_Arena;
    }

    /**
     * @return the 'empty-board' start state
     */
    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true, true);
        return m_so;
    }

    /**
     * not implemented correctly yet
     * @return the 'empty-board' start state
     */
    @Override
    public StateObservation chooseStartState(PlayAgent pa) {
        return getDefaultStartState();
    }

    /**
     * not implemented correctly yet
     * @return the 'empty-board' start state
     */
    @Override
    public StateObservation chooseStartState() {
        return getDefaultStartState();
    }


    public void humanMove(int a) {
        Types.ACTIONS act = Types.ACTIONS.fromInt(a);
        m_so.advance(act);
        arenaActReq = true;
    }

    public void inspectMove(int a) {
        Types.ACTIONS act = Types.ACTIONS.fromInt(a);
        m_Arena.setStatusMessage("Inspecting the value function ...");
        m_so.advance(act);
        arenaActReq = true;
    }
}
