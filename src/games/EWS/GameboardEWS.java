package games.EWS.gui;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;

public class GameboardEWS implements GameBoard {


    @Override
    public void initialize() {

    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {

    }

    @Override
    public void showGameBoard(Arena arena, boolean alignToMain) {

    }

    @Override
    public void toFront() {

    }

    @Override
    public boolean isActionReq() {
        return false;
    }

    @Override
    public void setActionReq(boolean actionReq) {

    }

    @Override
    public void enableInteraction(boolean enable) {

    }

    @Override
    public StateObservation getStateObs() {
        return null;
    }

    @Override
    public String getSubDir() {
        return null;
    }

    @Override
    public Arena getArena() {
        return null;
    }

    @Override
    public StateObservation getDefaultStartState() {
        return null;
    }

    @Override
    public StateObservation chooseStartState(PlayAgent pa) {
        return null;
    }

    @Override
    public StateObservation chooseStartState() {
        return null;
    }
}
