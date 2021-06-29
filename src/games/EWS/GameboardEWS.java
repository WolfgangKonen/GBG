package games.EWS;

import controllers.PlayAgent;
import games.Arena;
import games.EWS.StateObserverHelper.Helper;
import games.EWS.gui.GameBoardGuiEWS;
import games.GameBoard;
import games.Othello.ConfigOthello;
import games.StateObservation;
import tools.Types;

import java.util.Random;

/**
 * The GameboardEWS contains multiple classes:
 *      GameboardGuiEWS acts as a wrapper for the board and settings
 *      LegendGui: Container which holds the cube value
 *      BoardGui: Holds the board  with its pieces
 *      TileGui:
 *      TokenGui:
 */
public class GameBoardEWS implements GameBoard {

    /**
     * Serialnumber
     */
    public static final long serialVersionUID = 12L;

    /**
     * Gameboard Attributes
     */
    public Arena m_Arena;
    private int size;
    private int playerNum;
    private StateObserverEWS m_so;
    protected Random rand;
    private boolean arenaActReq = false;
    private transient GameBoardGuiEWS m_gameGui;


    public GameBoardEWS(Arena arena, int size, int playerNum){
        super();
        m_Arena = arena;
        this.size = size;
        this.playerNum = playerNum;
        m_so = new StateObserverEWS(size,playerNum);
        rand = new Random();
        if(m_Arena.hasGUI()){
            m_gameGui = new GameBoardGuiEWS(this);
        }
    }


    @Override
    public void initialize() {}

    /**
     * Resets the game to starting state
     * @param boardClear	whether to clear the board
     * @param vClear		whether to clear the value table
     */
    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if(boardClear){
            m_so = m_so.reset();
        }
        if(m_gameGui!=null && m_Arena.taskState!=Arena.Task.TRAIN){
            m_gameGui.clearBoard(boardClear,vClear);
        }
    }

    @Override
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
        StateObserverEWS soN = null;
        if(so!=null){
            assert( so instanceof StateObserverEWS):"StateObservation 'so' is not an instance of StateObserverEWS";
            soN = (StateObserverEWS) so;
            m_so = soN;
        }
        if(m_gameGui != null){
            m_gameGui.updateBoard(soN, withReset,showValueOnGameboard);
        }
    }

    public void hGameMove(int x, int y) {
        Types.ACTIONS act = Helper.parseAction(x,y, m_so.getSize());
        if( m_so.isLegalAction(act)) {
            m_so.advance(act);
            (m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
            arenaActReq = true;
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
            m_Arena.setStatusMessage("Inspecting the value function ...");
            m_so.advance(act);
        }else {m_Arena.setStatusMessage("Desired Action is not legal");}
        arenaActReq = true;
    }

    @Override
    public boolean isActionReq() {
        return arenaActReq;
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
    public void setActionReq(boolean actionReq) {
        arenaActReq = actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {
        if(m_gameGui!=null)
            m_gameGui.enableInteraction(enable);

    }

    @Override
    public void destroy() {
        if(m_gameGui!=null)
            m_gameGui.destroy();

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
    public Arena getArena() {
        return m_Arena;
    }

    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true,true);
        return m_so;
    }

    @Override
    public StateObservation chooseStartState(PlayAgent pa) {
        return chooseStartState();
    }

    /**
     * Choose a different placing for the tokens.
     *
     */
    @Override
    public StateObservation chooseStartState() {
        getDefaultStartState();
        return m_so;
    }
}
