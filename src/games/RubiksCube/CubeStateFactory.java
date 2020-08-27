package games.RubiksCube;

import games.BoardVector;

public class CubeStateFactory {
    //
    // factory methods: they allow to delegate the CubeState object construction to classes derived from CubeState:
    //

    public CubeState makeCubeState() {
        return new CubeState(CubeState.Type.COLOR_P);
    }

    public CubeState makeCubeState(CubeState.Type type) {
        return new CubeState(type);
    }

    @Deprecated
    public CubeState makeCubeState(BoardVector boardVector) {
        return new CubeState(boardVector);
    }

    public CubeState makeCubeState(CubeState other) {
        return new CubeState(other);
    }

}
