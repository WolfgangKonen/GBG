package games.RubiksCube;

/**
 * Corner cubie for 3x3x3 cube
 */
public class CubieTriple3x3 extends CubieTriple {
    /**
     * If {@code i} is the sticker location for the y-face of the ygr-cubie, then {@code right_R[i]} is the sticker location
     * for the g-face of the ygr-cubie (marching around the cubie in clockwise orientation).
     * And {@code right_R[right_R[i]]} is the sticker location of the r-face of the ygr-cubie.
     */
                                //   0                     8                    16
    protected static int[] right_R= {16,0,36,0,46,0,10,0,  0,0,44,0,30,0,18,0,   8,0,28,0,38,0,2,0,
                                //   24                    32                   40
                                     32,0,20,0,14,0,42,0,  40,0,4,0,22,0,26,0,  24,0,12,0,6,0,34,0};
    protected static int[] ccolo_R= {0,0,0,0,0,0,0,0,      1,1,1,1,1,1,1,1,      2,2,2,2,2,2,2,2,
                                     3,3,3,3,3,3,3,3,      4,4,4,4,4,4,4,4,      5,5,5,5,5,5,5,5  };
    /**
     * The ygr-cubie in its default location {24,32,40} with colors {3,4,5}={y,g,r}
     */
    public CubieTriple3x3() {
        initialize3x3(24);
    }

    /**
     * The cubie triple from the default cube with its 1st face at location {@code i}
     * @param i the 1st-face location
     */
    public CubieTriple3x3(int i) {
        initialize3x3(i);
    }

    public CubieTriple3x3(CubieTriple ct) {
        super(ct);
    }

    protected void initialize3x3(int i) {
        right = right_R;
        ccolo = ccolo_R;
        initialize(i);
    }

}
