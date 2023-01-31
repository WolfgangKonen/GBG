package games.RubiksCube;

/**
 * Corner cubie for 2x2x2 cube
 */
public class CubieTriple2x2 extends CubieTriple {
    /**
     * If {@code i} is the sticker location for the y-face of the ygr-cubie, then {@code right_P[i]} is the sticker location
     * for the g-face of the ygr-cubie (marching around the cubie in clockwise orientation).
     * And {@code right_P[right_P[i]]} is the sticker location of the r-face of the ygr-cubie.
     */
                       //            0           4          8          12           16           20
    protected static int[] right_P= {8,18,23,5,  0,22,15,9, 4,14,19,1, 16,10,7,21,  20,2,11,13,  12,6,3,17};
    protected static int[] ccolo_P= {0,0,0,0,    1,1,1,1,   2,2,2,2,   3,3,3,3,     4,4,4,4,     5,5,5,5  };

    /**
     * The ygr-cubie in its default location {12,16,20} with colors {3,4,5}={y,g,r}
     */
    public CubieTriple2x2() {
        initialize2x2(12);
    }

    /**
     * The cubie triple from the default cube with its 1st face at location {@code i}
     * @param i the 1st-face location
     */
    public CubieTriple2x2(int i) {
        initialize2x2(i);
    }

    public CubieTriple2x2(CubieTriple ct) {
        super(ct);
    }

    protected void initialize2x2(int i) {
        right = right_P;
        ccolo = ccolo_P;
        initialize(i);
    }

}
