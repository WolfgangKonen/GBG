package games.RubiksCube;

import games.BoardVector;

/**
 * factory class that allows to delegate the CubeState object construction to classes derived from CubeState:
 */
public class CubeStateFactory {

    public CubeState makeCubeState() {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubeState2x2(CubeState.Type.COLOR_P);
            case RUBIKS -> new CubeState3x3(CubeState.Type.COLOR_R);
        };
    }

    public CubeState makeCubeState(CubeState.Type type) {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubeState2x2(type);
            case RUBIKS -> new CubeState3x3(type);
        };
    }

    @Deprecated
    public CubeState makeCubeState(BoardVector boardVector) {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubeState2x2(boardVector);
            case RUBIKS -> new CubeState3x3(boardVector);
        };
    }

    public CubeState makeCubeState(CubeState other) {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubeState2x2(other);
            case RUBIKS -> new CubeState3x3(other);
        };
    }

    /**
     * generate the <b>inverse</b> transformations {@link CubeState#invF}, {@link CubeState#invL} and {@link CubeState#invU}.
     */
    public static void generateInverseTs() {
        switch (CubeConfig.cubeSize) {
            case POCKET -> CubeState2x2.generateInverseTs();
            case RUBIKS -> CubeState3x3.generateInverseTs();
        }
    }

}
