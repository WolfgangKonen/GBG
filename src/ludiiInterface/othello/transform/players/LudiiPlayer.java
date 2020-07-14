package ludiiInterface.othello.transform.players;

import util.Context;
import util.action.Action;

public final class LudiiPlayer {
    private final int _playerIndex;

    public LudiiPlayer(final Action ludiiAction) {
        _playerIndex = ludiiAction.state().get(0);
    }

    public LudiiPlayer(final Context ludiiContext) {
        _playerIndex = ludiiContext.state().mover();
    }

    public int toInt() {
        return _playerIndex;
    }

    public GbgPlayer toGbgPlayer() {
        return new GbgPlayer(this);
    }

    public boolean isValid() {
        return IsBlack() || IsWhite();
    }

    boolean IsBlack() {
        return _playerIndex == 1;
    }

    boolean IsWhite() {
        return _playerIndex == 2;
    }
}
