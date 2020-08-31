package games.RubiksCube;

import games.BoardVector;

public class CubeStateFactory {
    //
    // factory methods: they allow to delegate the CubeState object construction to classes derived from CubeState:
    //

    public CubeState makeCubeState() {
        switch (CubeConfig.cubeType) {
            case POCKET -> { return new CubeState2x2(CubeState.Type.COLOR_P); }
            case RUBIKS -> { return new CubeState3x3(CubeState.Type.COLOR_P); }
        }
        throw new RuntimeException("we should not arrive here");
    }

    public CubeState makeCubeState(CubeState.Type type) {
        switch (CubeConfig.cubeType) {
            case POCKET -> { return new CubeState2x2(type); }
            case RUBIKS -> { return new CubeState3x3(type); }
        }
        throw new RuntimeException("we should not arrive here");
    }

    @Deprecated
    public CubeState makeCubeState(BoardVector boardVector) {
        switch (CubeConfig.cubeType) {
            case POCKET -> { return new CubeState2x2(boardVector); }
            case RUBIKS -> { return new CubeState3x3(boardVector); }
        }
        throw new RuntimeException("we should not arrive here");
    }

    public CubeState makeCubeState(CubeState other) {
        switch (CubeConfig.cubeType) {
            case POCKET -> { return new CubeState2x2(other); }
            case RUBIKS -> { return new CubeState3x3(other); }
        }
        throw new RuntimeException("we should not arrive here");
    }

    /**
     * generate the <b>inverse</b> transformations {@link CubeState#invF}, {@link CubeState#invL} and {@link CubeState#invU}.
     */
    public static void generateInverseTs() {
        switch (CubeConfig.cubeType) {
            case POCKET -> { CubeState2x2.generateInverseTs(); }
            case RUBIKS -> { CubeState3x3.generateInverseTs(); }
        }
    }

}
