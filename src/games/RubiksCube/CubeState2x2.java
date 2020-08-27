package games.RubiksCube;

import games.BoardVector;

public class CubeState2x2 extends CubeState {
    public CubeState2x2() {
        super(Type.COLOR_P);
    }

    public CubeState2x2(CubeState cs) {
        super(cs);
    }

    /**
     * Construct a new cube of Type {@code type} in default (solved) state
     * @param type	the cube type
     */
    public CubeState2x2(Type type) {
        super(type);
    }

    /**
     * Construct a new cube of <b>color representation</b> type from a board vector {@code bvec}
     * @param boardVector	the board vector, see {@link #getBoardVector()} for the different types
     */
    public CubeState2x2(BoardVector boardVector) {
        super(boardVector);
    }

}
