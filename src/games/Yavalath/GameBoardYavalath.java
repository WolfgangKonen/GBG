package games.Yavalath;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import games.Yavalath.GUI.GameBoardGUIYavalath;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class GameBoardYavalath extends GameBoardBase implements GameBoard {

    public StateObserverYavalath m_so;
    private GameBoardGUIYavalath gb_gui;
    protected Random rand;

    public GameBoardYavalath(Arena arena) {
        super(arena);
        m_so = new StateObserverYavalath();
        if(getArena().hasGUI() && gb_gui==null) {
            gb_gui = new GameBoardGUIYavalath(this);
        }
        rand = new Random(System.currentTimeMillis());
    }

    @Override
    public void initialize() {


    }

    /**
     * update game-specific parameters from {@link Arena}'s param tabs
     */
    @Override
    public void updateParams() {}

    @Override
    public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
        if(boardClear){
            m_so = new StateObserverYavalath();
        }
        if(vClear){
            m_so.clearValues();
        }
        if(gb_gui != null && getArena().taskState != Arena.Task.TRAIN){
            gb_gui.clearBoard(boardClear,vClear);
        }
    }

    @Override
    public void destroy() {
            if(gb_gui != null) gb_gui.destroy();
    }

    @Override
    public void setStateObs(StateObservation so) {
        if(so!=null){
            assert (so instanceof StateObserverYavalath)
                    :"StateObservation 'so' is not an instance of StateObserverYavalath";
            StateObserverYavalath soYav = (StateObserverYavalath) so;
            this.m_so = soYav;
        }
    }

    @Override
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
        setStateObs(so);    	// asserts that so is StateObserverYavalath

        if(gb_gui!=null)
            gb_gui.updateBoard((StateObserverYavalath) so,withReset,showValueOnGameboard);
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
    public StateObservation getDefaultStartState(Random cmpRand) {
        clearBoard(true,true, null);
        return m_so;
    }

    @Override
    public StateObservation chooseStartState(PlayAgent pa) {
        return chooseStartState();
    }

    @Override
    public StateObservation chooseStartState() {

        getDefaultStartState(null);
        if(rand.nextDouble()>0.5){
            ArrayList<Types.ACTIONS> actions = m_so.getAvailableActions();
            int actInt = rand.nextInt(actions.size());
            m_so.advance(actions.get(actInt), null);
        }
        return m_so;
    }

    public void HGameMove(Types.ACTIONS action){
        if(m_so.isLegalAction(action)){
            m_so.advance(action, null);
            if(getArena().taskState == Arena.Task.PLAY){
                getArena().getLogManager().addLogEntry(action,m_so,getArena().getLogSessionID());
            }
            setActionReq(true);
        }

    }

    public void useSwapRule(){
        m_so.useSwapRule();
        updateBoard(m_so,false,false);
    }
}
