package games.Yavalath;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import games.Yavalath.GUI.GameBoardGUIYavalath;
import tools.Types;

import java.util.ArrayList;
import java.util.Random;

public class GameBoardYavalath implements GameBoard {

    private boolean actionReq = false;
    public StateObserverYavalath so;
    private Arena arena;
    private GameBoardGUIYavalath gb_gui;
    private Random rand;

    public GameBoardYavalath(Arena arena){
        this.arena = arena;
        so = new StateObserverYavalath();
        gb_gui = new GameBoardGUIYavalath(this);
        rand = new Random(System.currentTimeMillis());
    }

    @Override
    public void initialize() {


    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if(boardClear){
            so = new StateObserverYavalath();
        } else if(vClear){
            so.clearValues();
        }
        if(gb_gui != null && arena.taskState != Arena.Task.TRAIN){
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
            this.so = soYav;
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
        return so;
    }

    @Override
    public String getSubDir() {
        return null;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true,true);
        return so;
    }

    @Override
    public StateObservation chooseStartState(PlayAgent pa) {
        return chooseStartState();
    }

    @Override
    public StateObservation chooseStartState() {

        getDefaultStartState();
        if(rand.nextDouble()>0.5){
            ArrayList<Types.ACTIONS> actions = so.getAvailableActions();
            int actInt = rand.nextInt(actions.size());
            so.advance(actions.get(actInt));
        }
        return so;
    }
}
