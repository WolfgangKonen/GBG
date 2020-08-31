package games.RubiksCube;

import games.BoardVector;

public class CubeState3x3 extends CubeState {

    public CubeState3x3() {
        super(Type.COLOR_P);
    }

    public CubeState3x3(CubeState cs) {
        super(cs);
    }

    /**
     * Construct a new cube of Type {@code type} in default (solved) state
     * @param type	the cube type
     */
    public CubeState3x3(Type type) {
        super(type);
    }

    /**
     * Construct a new cube of <b>color representation</b> type from a board vector {@code bvec}
     * @param boardVector	the board vector, see {@link #getBoardVector()} for the different types
     */
    public CubeState3x3(BoardVector boardVector) {
        super(boardVector);
        CubeStateFactory csFactory = new CubeStateFactory();
        int[] bvec = boardVector.bvec;
        switch(bvec.length) {
            case 48:	// boardvecType == CUBESTATE, 3x3x3
                this.type = Type.COLOR_R;
                this.fcol = bvec.clone();
                break;
            case 50: 	// boardvecType == CUBEPLUSACTION, 3x3x3
                this.type = Type.COLOR_R;
                this.fcol = new int[48];
                System.arraycopy(bvec, 0, this.fcol, 0, 48);
                break;
            case 147: // = 7*21, see notes-WK-RubiksCube.docx
                // TODO: STICKER implementation for 3x3x3
            default:
                throw new RuntimeException("Case bvec.length = "+bvec.length+" not yet implemented.");
        }
    }


    //
    // --- TODO: this has to be adapted to 3x3x3 ! ---
    //                                		0          4         8            12           16           20
//    private static final int[] 	invF_2x2 = {18,19,2,3,  1, 5,6,0, 11, 8, 9,10, 12, 7, 4,15, 16,17,13,14, 20,21,22,23},
//            invL_2x2 = { 9, 1,2,8,  7, 4,5,6, 14,15,10,11, 12,13,21,22, 16,17,18,19, 20, 3, 0,23},
//            invU_2x2 = { 3, 0,1,2, 22,23,6,7,  5, 9,10, 4, 12,13,14,15, 16,11, 8,19, 20,21,17,18};
//    //
//    // use the following line once on a default TRAFO_P CubeState t_cs to generate int[] invL above:
//    //			return t_cs.uTr().FTw().uTr().uTr().uTr();   	// L(x) = u^3(F(u(x)))
//    // use the following line once on a default TRAFO_P CubeState t_cs to generate int[] invU above:
//    //			return t_cs.lTr().lTr().lTr().FTw().lTr();   	// U(x) = l(F(l^3(x)))
//
//    /**
//     * generate the <b>inverse</b> transformations {@link #invF}, {@link #invL} and {@link #invU}.
//     */
    public static void generateInverseTs() {
//        assert (CubeConfig.cubeType== CubeConfig.CubeType.POCKET);
//        invF = invF_2x2;
//        invL = invL_2x2;
//        invU = invU_2x2;
    }

    // TODO: needs to be adapted to 3x3x3
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

    // TODO: needs to be adapted to 3x3x3
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
