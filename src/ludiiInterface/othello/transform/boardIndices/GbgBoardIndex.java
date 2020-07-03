package ludiiInterface.othello.transform.boardIndices;

import tools.Types;

public final class GbgBoardIndex extends BoardIndex {
    private final int _boardIndex;

    public GbgBoardIndex(final Types.ACTIONS gbgAction) {
        _boardIndex = gbgAction.toInt();
    }

    public GbgBoardIndex(final LudiiBoardIndex ludiiIndex) {
        _boardIndex = transformIndices[ludiiIndex.toInt()];
    }

    @Override
    public int toInt() {
        return _boardIndex;
    }

    public LudiiBoardIndex toLudiiIndex() {
        return new LudiiBoardIndex(this);
    }
}