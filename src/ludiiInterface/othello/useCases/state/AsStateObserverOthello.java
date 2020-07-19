package ludiiInterface.othello.useCases.state;

import games.Othello.BaseOthello;
import games.Othello.StateObserverOthello;
import tools.Types;

import java.util.ArrayList;

public final class AsStateObserverOthello extends StateObserverOthello {
    private final GbgState _gbgState;

    public AsStateObserverOthello(final GbgState gbgState) {
        _gbgState = gbgState;
        updateCurrentGameState();
    }

    @Override
    public ArrayList<Types.ACTIONS> getAllAvailableActions() {
        return _gbgState.allAvailableActions();
    }

    @Override
    public ArrayList<Types.ACTIONS> getAvailableActions() {
        return _gbgState.availableActions();
    }

    @Override
    public int getNumAvailableActions() {
        return getAvailableActions().size();
    }

    @Override
    public void setAvailableActions() { }

    @Override
    public Types.ACTIONS getAction(final int i) {
        return getAvailableActions().get(i);
    }

    @Override
    public void advance(final Types.ACTIONS action) {
        _gbgState.advance(action);
        updateCurrentGameState();
    }

    @Override
    public int getPlayer() {
        return _gbgState.player().toInt();
    }

    @Override
    public AsStateObserverOthello copy() {
        return new AsStateObserverOthello(_gbgState.copy());
    }

    @Override
    public boolean isGameOver() {
        if (getNumAvailableActions() == 0)
            return BaseOthello.possibleActions(
                currentGameState,
                getOpponent(getPlayer())
            ).size() == 0;
        return false;
    }

    @Override
    public String stringDescr() {
        return _gbgState.stringRepresentation();
    }

    private void updateCurrentGameState() {
        currentGameState = _gbgState.toArray2D();
    }
}
