package games.EWN;

import controllers.PlayAgent;
import games.Arena;
import games.EWN.StateObserverHelper.Helper;
import games.EWN.config.ConfigEWN;
import games.EWN.gui.GameBoardGuiEWN;
import games.GameBoard;
import games.Othello.ConfigOthello;
import games.StateObservation;
import tools.Types;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * The GameboardEWS contains multiple classes:
 *      GameboardGuiEWS acts as a wrapper for the board and settings
 *      LegendGui: Container which holds the cube value
 *      BoardGui: Holds the board  with its pieces
 *      TileGui:
 *      TokenGui:
 */
public class GameBoardEWN implements GameBoard {

    /**
     * Serialnumber
     */
    public static final long serialVersionUID = 12L;

    /**
     * Gameboard Attributes
     */
    public Arena m_Arena;
    private StateObserverEWN m_so;
    protected Random rand;
    private boolean arenaActReq = false;
    private transient GameBoardGuiEWN m_gameGui;
    double[][] vGameState;
    private int selectedTokenPosition;
    private boolean selecting;
    private int[] cubed;

    public GameBoardEWN(Arena arena){
        super();
        m_Arena = arena;
        m_so = new StateObserverEWN();
        rand = new Random();
        selecting = true;
        vGameState = new double[ConfigEWN.BOARD_SIZE*3][ConfigEWN.BOARD_SIZE*3];
        selectedTokenPosition = -1;
        if(m_Arena.hasGUI()){
            m_gameGui = new GameBoardGuiEWN(this);
        }
    }

    public GameBoardEWN(Arena arena, int size, int player){
        super();
        ConfigEWN.BOARD_SIZE = size;
        ConfigEWN.NUM_PLAYERS = player;
        m_Arena = arena;
        m_so = new StateObserverEWN();
        rand = new Random();
        selecting = true;
        selectedTokenPosition = -1;
        if(m_Arena.hasGUI()){
            m_gameGui = new GameBoardGuiEWN(this);
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
        StateObserverEWN soN = null;
        if(so!=null){
            assert( so instanceof StateObserverEWN):"StateObservation 'so' is not an instance of StateObserverEWS";
            soN = (StateObserverEWN) so;
            m_so = soN;
        }
        if(m_gameGui != null){
            m_gameGui.updateBoard(soN, withReset,showValueOnGameboard);
        }

        if(soN != null && showValueOnGameboard && soN.getStoredValues() != null) {
            for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
                for( int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
                    vGameState[i][j] = Double.NaN;

            for(int y = 0 ; y < soN.getStoredValues().length; y++)
            {
                System.out.println("index: " + y + " Values" + soN.getStoredValues()[y]);
                /**Types.ACTIONS action = sot.getStoredAction(y);
                int iAction = action.toInt();
                int jFirst= iAction%ConfigOthello.BOARD_SIZE;
                int iFirst= (iAction-jFirst)/ConfigOthello.BOARD_SIZE;
                vGameState[iFirst][jFirst] = sot.getStoredValues()[y];**/
            }
        }
    }

    private void move(int index){
        if(selecting){
            m_gameGui.setError(" ");

            if(selectedTokenPosition > -1) {
                m_gameGui.unSelect();
                selectedTokenPosition = -1;
            }
            selectedTokenPosition = m_gameGui.hGameSelecting(index);
            //Check if the selected index is able to make a move
            ArrayList<Types.ACTIONS> allActions = m_so.getAvailableActions();
            for(Types.ACTIONS a : allActions){
                int[] indices = Helper.getIntsFromAction(a);
                if(indices[0] == index){
                    selecting = false;
                    break;
                }
            }
            if(selecting){
                int diceRoll = m_so.getNextNondeterministicAction().toInt() + 1;
                m_gameGui.setError("The dice rolled: " + diceRoll + ". Invalid token selection.");
                m_gameGui.unSelect();
                selectedTokenPosition = -1;
            }
        }else {
            if(selectedTokenPosition == index){
                m_gameGui.unSelect();
                selecting = true;
            }else{
                Types.ACTIONS act = Helper.parseAction(selectedTokenPosition,index);
                if( m_so.isLegalAction(act)) {
                    m_so.advance(act);
                    (m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
                    m_gameGui.unSelect();
                    arenaActReq = true;
                    selecting = true;
                    selectedTokenPosition = -1;

                }
            }
        }
    }

    public void hGameMove(int index) {
        if(!(m_Arena.taskState == Arena.Task.PLAY)) return;
        move(index);
    }

    public void inspectMove(int index)
    {
        if(!(m_Arena.taskState == Arena.Task.INSPECTV)) return;
        move(index);
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
        int boardSize = ConfigEWN.BOARD_SIZE;
        int cellCoding = ConfigEWN.CEll_CODING;
        boolean random = ConfigEWN.RANDOM_POSITION;
        String board = boardSize+"x"+boardSize+" ";
        String players = ConfigEWN.NUM_PLAYERS + " Players";
        String isRandom = random ? " random " : "";
        return board+players+" "+ConfigEWN.CELL_CODE_DIR_NAMING[cellCoding]+isRandom;


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
