package games.RubiksCube;

/**
 * factory class that allows to delegate the CubieTriple object construction to classes derived from CubieTriple:
 */
public class CubieTripleFactory {
    public CubieTriple makeCubieTriple() {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubieTriple2x2();
            case RUBIKS -> new CubieTriple3x3();
        };
    }

    public CubieTriple makeCubieTriple(int s) {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubieTriple2x2(s);
            case RUBIKS -> new CubieTriple3x3(s);
        };
    }

    public CubieTriple makeCubieTriple(CubieTriple other) {
        return switch (CubeConfig.cubeSize) {
            case POCKET -> new CubieTriple2x2(other);
            case RUBIKS -> new CubieTriple3x3(other);
        };
    }

}
