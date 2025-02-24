package games.EWN;

import controllers.PlayAgent;
import games.Arena;
import games.EWN.StateObserverHelper.Helper;
import games.EWN.config.ConfigEWN;
import games.EWN.gui.GameBoardGuiEWN;
import games.GameBoard;
import games.GameBoardBase;
import games.Othello.ConfigOthello;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;
import tools.Types;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * GameBoardEWN contains the 'game-theoretic' part of the game board. See {@link GameBoardGuiEWN} for the GUI.
 */
public class GameBoardEWN extends GameBoardBase implements GameBoard {

    /**
     * Serialnumber
     */
    public static final long serialVersionUID = 12L;

    /**
     * Game board Attributes
     */
    private StateObserverEWN m_so;
    protected Random rand;
    // private boolean arenaActReq = false;
    private transient GameBoardGuiEWN m_gameGui;
    double[][] vGameState;
    private int selectedTokenPosition;
    private boolean selecting;
    //private int[] cubed;      // never used

    public GameBoardEWN(Arena arena){
        super(arena);
        m_so = new StateObserverEWN();
        rand = new Random();
        selecting = true;
        vGameState = new double[ConfigEWN.BOARD_SIZE*3][ConfigEWN.BOARD_SIZE*3];
        selectedTokenPosition = -1;
        if(m_Arena.hasGUI()){
            m_gameGui = new GameBoardGuiEWN(this);
        }
    }

    // --- never used ---
//    public GameBoardEWN(Arena arena, int size, int player){
//        super();
//        ConfigEWN.BOARD_SIZE = size;
//        ConfigEWN.NUM_PLAYERS = player;
//        m_Arena = arena;
//        m_so = new StateObserverEWN();
//        rand = new Random();
//        selecting = true;
//        selectedTokenPosition = -1;
//        if(m_Arena.hasGUI()){
//            m_gameGui = new GameBoardGuiEWN(this);
//        }
//    }

    @Override
    public void initialize() {}

    /**
     * update game-specific parameters from {@link Arena}'s param tabs
     */
    @Override
    public void updateParams() {}

    /**
     * Resets the game to starting state
     * @param boardClear    whether to clear the board
     * @param vClear        whether to clear the value table
     * @param cmpRand       if non-null, use this (reproducible) RNG instead of StateObservation's RNG
     */
    @Override
    public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
        if(boardClear){
            m_so = m_so.reset(cmpRand);
            this.getStateQueue().clear();
        }
        if(m_gameGui!=null && m_Arena.taskState!=Arena.Task.TRAIN){
            m_gameGui.clearBoard(boardClear,vClear);
        }
    }

    @Override
    public void setStateObs(StateObservation so) {
        StateObserverEWN soN = null;
        if(so!=null){
            assert( so instanceof StateObserverEWN):"StateObservation 'so' is not an instance of StateObserverEWN";
            soN = (StateObserverEWN) so;
            m_so = soN;
        }
    }

    @Override
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
        setStateObs(so);    // asserts that so is StateObserverEWN

        StateObserverEWN soN = (StateObserverEWN) so;
        if(m_gameGui != null){
            m_gameGui.updateBoard(soN, withReset,showValueOnGameboard);
        }

        // --- TODO: check if this is really needed ---
        if(soN != null && showValueOnGameboard && soN.getStoredValues() != null) {
            for(int i = 0; i < ConfigEWN.BOARD_SIZE; i++)
                for( int j = 0; j < ConfigEWN.BOARD_SIZE; j++)
                    vGameState[i][j] = Double.NaN;

            for(int y = 0 ; y < soN.getStoredValues().length; y++)
            {
                System.out.println("index: " + y + " Values " + soN.getStoredValues()[y]);
                /*Types.ACTIONS action = sot.getStoredAction(y);
                int iAction = action.toInt();
                int jFirst= iAction%ConfigOthello.BOARD_SIZE;
                int iFirst= (iAction-jFirst)/ConfigOthello.BOARD_SIZE;
                vGameState[iFirst][jFirst] = sot.getStoredValues()[y];*/
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
                    m_so.advance(act, null);
                    (m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
                    m_gameGui.unSelect();
                    this.setActionReq(true);
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



    // --- now in GameBoardBase
//    @Override
//    public boolean isActionReq() {
//        return arenaActReq;
//    }

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
        int cellCoding = ConfigEWN.CELL_CODING;
        boolean random = ConfigEWN.RANDOM_POSITION;
        String board = boardSize+"x"+boardSize+" ";
        String players = ConfigEWN.NUM_PLAYERS + " Players";
        String isRandom = random ? " random " : "";
        return board+players+" "+ConfigEWN.CELL_CODE_DIR_NAMING[cellCoding]+isRandom;


    }

    @Override
    public StateObservation getDefaultStartState(Random cmpRand) {
        clearBoard(true,true, cmpRand);        // resets m_so
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
        getDefaultStartState(null);
        return m_so;
    }
}
