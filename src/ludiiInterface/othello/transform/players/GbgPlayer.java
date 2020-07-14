package ludiiInterface.othello.transform.players;

import games.Othello.ConfigOthello;
import util.Context;
import util.action.Action;

public final class GbgPlayer {
    private final int _playerIndex;

    public GbgPlayer(final LudiiPlayer ludiiPlayer) {
        _playerIndex = transformPlayerIndexFrom(ludiiPlayer);
    }

    public GbgPlayer(final Action ludiiAction) {
        this(new LudiiPlayer(ludiiAction));
    }

    public GbgPlayer(final Context ludiiContext) {
        this(new LudiiPlayer(ludiiContext));
    }

    public int toInt() {
        return _playerIndex;
    }

    private int transformPlayerIndexFrom(final LudiiPlayer ludiiPlayer) {
        if (ludiiPlayer.IsBlack())
            return ConfigOthello.BLACK;
        if (ludiiPlayer.IsWhite())
            return ConfigOthello.WHITE;
        throw new IllegalStateException("Color of the Ludii-player must be black or white");
    }
}
