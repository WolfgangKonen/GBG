package games.Yavalath;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import games.Yavalath.GUI.GameBoardGUIYavalath;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class GameBoardYavalath implements GameBoard {

    private boolean actionReq = false;
    public StateObserverYavalath m_so;
    protected Arena m_arena;
    private GameBoardGUIYavalath gb_gui;
    protected Random rand;

    public GameBoardYavalath(Arena arena){
        m_arena = arena;
        m_so = new StateObserverYavalath();
        if(m_arena.hasGUI() && gb_gui==null) {
            gb_gui = new GameBoardGUIYavalath(this);
        }
        rand = new Random(System.currentTimeMillis());
    }

    @Override
    public void initialize() {


    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if(boardClear){
            m_so = new StateObserverYavalath();
        }
        if(vClear){
            m_so.clearValues();
        }
        if(gb_gui != null && m_arena.taskState != Arena.Task.TRAIN){
            gb_gui.clearBoard(boardClear,vClear);
        }
    }

    @Override
    public void destroy() {
            if(gb_gui != null) gb_gui.destroy();
    }

    @Override
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
        StateObserverYavalath soYav = null;
        if(so!=null){
            assert (so instanceof StateObserverYavalath)
                    :"StateObservation 'so' is not an instance of StateObserverYavalath";
            soYav = (StateObserverYavalath) so;
            this.m_so = soYav;
        }
        if(gb_gui!=null)
            gb_gui.updateBoard(soYav,withReset,showValueOnGameboard);
    }

    @Override
    public void showGameBoard(Arena arena, boolean alignToMain) {
        if(gb_gui != null){
            gb_gui.showGameBoard(arena,alignToMain);
        }
    }

    @Override
    public void toFront() {
        if(gb_gui!=null) gb_gui.toFront();
    }

    @Override
    public boolean isActionReq() {
        return actionReq;
    }

    @Override
    public void setActionReq(boolean actionReq) {
        this.actionReq = actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {

    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public String getSubDir() {
        DecimalFormat form = new DecimalFormat("00");
        return form.format(ConfigYavalath.getPlayers()) + " Players Size " + form.format(ConfigYavalath.getBoardSize());
    }

    @Override
    public Arena getArena() {
        return m_arena;
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

    @Override
    public StateObservation chooseStartState() {

        getDefaultStartState();
        if(rand.nextDouble()>0.5){
            ArrayList<Types.ACTIONS> actions = m_so.getAvailableActions();
            int actInt = rand.nextInt(actions.size());
            m_so.advance(actions.get(actInt));
        }
        return m_so;
    }

    public void HGameMove(Types.ACTIONS action){
        if(m_so.isLegalAction(action)){
            m_so.advance(action);
            if(m_arena.taskState == Arena.Task.PLAY){
                m_arena.getLogManager().addLogEntry(action,m_so,m_arena.getLogSessionID());
            }
            setActionReq(true);
        }

    }

    public void useSwapRule(){
        m_so.useSwapRule();
        updateBoard(m_so,false,false);
    }
}
