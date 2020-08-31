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
        assert (CubeConfig.cubeType== CubeConfig.CubeType.POCKET);
        CubeStateFactory csFactory = new CubeStateFactory();
        int[] bvec = boardVector.bvec;
        switch(bvec.length) {
            case 24: 	// boardvecType == CUBESTATE, 2x2x2
                this.type = Type.COLOR_P;
                this.fcol = bvec.clone();
                this.sloc = boardVector.aux.clone();
                break;
            case 26: 	// boardvecType == CUBEPLUSACTION, 2x2x2
                this.type = Type.COLOR_P;
                this.fcol = new int[24];
                System.arraycopy(bvec, 0, this.fcol, 0, 24);
                this.sloc = boardVector.aux.clone();
                break;
            case 49: 	// boardvecType == STICKER, 2x2x2
                this.type = Type.COLOR_P;
                this.sloc = slocFromSTICKER(bvec);
                CubeState def = csFactory.makeCubeState(Type.COLOR_P);
                this.fcol = new int[def.fcol.length];
                for (int i=0; i<24; i++) this.fcol[sloc[i]] = def.fcol[i];
                break;
            default:
                throw new RuntimeException("Case bvec.length = "+bvec.length+" not yet implemented.");
        }
    }

    /**
     * Helper for CubeState2x2(BoardVector):
     * Given a board vector in STICKER representation, reconstruct member {@code sloc}.
     * @param bvec	the board vector
     * @return <b>int[] sloc</b>. sloc[i] holds the new location of sticker i which is at location i in the default cube.
     */
    private int[] slocFromSTICKER(int[] bvec) {
        int[] sloc = new int[24];
        final 							// this array is found with the help of Table 3 in notes-WK-RubiksCube.docx:
        int[][] C = {{ 0,  4,  8},		// 1st row: the locations for a1,a2,a3
                { 1, 11, 18},		// 2nd row: the locations for b1,b2,b3
                { 2, 17, 23},		// and so on ...
                { 3, 22,  5},
                {12, 16, 20},
                {13, 19, 10},
                {14,  9,  7},
                {15,  6, 21}};
        int cor,fac;
        for (int z=0; z<8; z++) {
            int[] corfac = getCornerAndFace(z,bvec);
            cor=corfac[0];
            fac=corfac[1];
            for (int i=0; i<3; i++) {
                sloc[ C[z][i] ] = C[cor][(fac+i)%3];
            }
        }
        return sloc;
    }

    /**
     * Helper for slocFromSTICKERS:
     * Given the index z of a tracker sticker, return from {@code bvec} (the board vector in the STICKER representation)
     * the corner and face where this sticker z is found.
     *
     * @param z index from {0,...,7} of a tracked sticker
     * @param bvec the board vector
     * @return <b>int[2]</b> with the first element being the corner index {0,...,7} of the corner a,...,h and  the
     * 		second element being the face index {0,1,2} for the face values {1,2,3} found in bvec.
     * <p>
     * Details: We need a little index arithmetic to account for the fact that {@code bvec} represents a 7x7 array, but
     * the index z is for all 8 stickers (including the ever-constant 4th sticker of the ygr-cubie (corner e)).
     */
    private int[] getCornerAndFace(int z, int[] bvec) {
        int[] corfac = {4,0};	// the values for sticker z=4 (ygr-cubie, which stays always in place)
        int column=0;

        // index arithmetic, part one
        if (z<4) column=z;
        else if (z==4) return corfac;	// the ygr-cubie case
        else column=z-1;				// cases z=5,6,7 address column 4,5,6 of the STICKER board

        // find (row number, value) of the only non-zero element in 'column':
        int nonzero = 0;
        int rv=0;
        for (int r=0; r<7; r++) {
            rv = bvec[r*7+column];
            if (rv!=0) {
                nonzero++;
                corfac[0] = (r<4) ? r : (r+1);		// index arithmetic, part two
                corfac[1] = rv - 1;
            }
        }
        assert (nonzero==1) : "Oops, there are "+nonzero+" elements non-zero in column "+z+", but there should be exactly 1!";

        return corfac;
    }


    //                                		0          4         8            12           16           20
    private static final int[] 	invF_2x2 = {18,19,2,3,  1, 5,6,0, 11, 8, 9,10, 12, 7, 4,15, 16,17,13,14, 20,21,22,23},
            invL_2x2 = { 9, 1,2,8,  7, 4,5,6, 14,15,10,11, 12,13,21,22, 16,17,18,19, 20, 3, 0,23},
            invU_2x2 = { 3, 0,1,2, 22,23,6,7,  5, 9,10, 4, 12,13,14,15, 16,11, 8,19, 20,21,17,18};
    //
    // use the following line once on a default TRAFO_P CubeState t_cs to generate int[] invL above:
    //			return t_cs.uTr().FTw().uTr().uTr().uTr();   	// L(x) = u^3(F(u(x)))
    // use the following line once on a default TRAFO_P CubeState t_cs to generate int[] invU above:
    //			return t_cs.lTr().lTr().lTr().FTw().lTr();   	// U(x) = l(F(l^3(x)))

    /**
     * generate the <b>inverse</b> transformations {@link #invF}, {@link #invL} and {@link #invU}.
     */
    public static void generateInverseTs() {
        assert (CubeConfig.cubeType== CubeConfig.CubeType.POCKET);
        invF = invF_2x2;
        invL = invL_2x2;
        invU = invU_2x2;
    }

    /**
     * Whole-cube rotation 90° counter-clockwise around the u-face
     */
    protected CubeState uTr() {
        int i;
        // fcol(invT[i]) is the color which cubie face i gets after transformation:
        int[] invT = {3,0,1,2,22,23,20,21,5,6,7,4,13,14,15,12,10,11,8,9,19,16,17,18};
        int[] tmp = this.fcol.clone();
        //for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
        for (i=0; i<invT.length; i++) this.fcol[i] = tmp[invT[i]];
        return this;
    }

    /**
     * Whole-cube rotation 90° counter-clockwise around the f-face
     */
    protected CubeState fTr() {
        int i;
        // fcol(invT[i]) is the color which cubie face i gets after transformation:
        int[] invT = {18,19,16,17,1,2,3,0,11,8,9,10,6,7,4,5,15,12,13,14,21,22,23,20};
        int[] tmp = this.fcol.clone();
        //for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
        for (i=0; i<invT.length; i++) this.fcol[i] = tmp[invT[i]];
        return this;
    }

}
