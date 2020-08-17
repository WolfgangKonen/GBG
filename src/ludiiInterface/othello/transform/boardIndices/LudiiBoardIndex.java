package ludiiInterface.othello.transform.boardIndices;

import util.action.Action;

public final class LudiiBoardIndex extends BoardIndex {
    private final int _boardIndex;

    public LudiiBoardIndex(final Action ludiiAction) {
        _boardIndex = ludiiAction.to();
    }

    public LudiiBoardIndex(final GbgBoardIndex gbgIndex) {
        _boardIndex = transformIndices[gbgIndex.toInt()];
    }

    @Override
    public int toInt() {
        return _boardIndex;
    }

    public GbgBoardIndex toGbgIndex() {
        return new GbgBoardIndex(this);
    }
}