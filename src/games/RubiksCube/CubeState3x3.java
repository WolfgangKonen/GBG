package games.RubiksCube;

import games.BoardVector;

public class CubeState3x3 extends CubeState {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip will become unreadable or you have
     * to provide a special version transformation)
     */
    private static final long  serialVersionUID = 12L;

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
        super();    // would be called silently otherwise
        //CubeStateFactory csFactory = new CubeStateFactory();  // may be needed later
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
            case 147: // = 7*21, see notes-WK-RubiksCube.docx, boardvecType == STICKER
                // TODO: STICKER implementation for 3x3x3
                throw new RuntimeException("Case STICKER for 3x3x3 cube not yet implemented");
            default:
                throw new RuntimeException("Case bvec.length = "+bvec.length+" not yet implemented.");
        }
    }

    /**
     * Locate the cubie with the colors of {@link CubieTriple} {@code tri} in {@code this}.
     * {@code this} has to be of type COLOR_P or COLOR_R.<br>
     * [This method is only needed if we want to use color symmetries.]
     *
     * @param tri
     * @return a {@link CubieTriple} whose member {@code loc} carries the location of the cubie with
     * 		   the colors of {@code tri}.
     */
    public CubieTriple locate(CubieTriple tri) {
        throw new RuntimeException("[CubeState.locate] is not yet implemented for 3x3x3 RubiksCube!");
    }


    /**
     * There are four possible board vector types, depending on {@link CubeConfig#boardVecType}
     * <ul>
     * <li> <b>CUBESTATE</b>: the face color array of the cube, i.e. member {@link #fcol}
     * <li> <b>CUBEPLUSACTION</b>: like CUBESTATE, but with two more int values added: the ordinal of the last twist and
     * 		the number of quarter turns in this twist
     * <li> <b>STICKER</b>: similar to the coding suggested by [McAleer2018], we track the location of seven stickers
     * 		(one face of each cubie, except the ygr-cubie) when the cube is twisted away from its original position. We
     * 		represent it as a 7x7 field (one-hot encoding).
     * <li> <b>STICKER2</b>: similar to STICKER, we track the location of seven stickers (one face of each cubie,
     * 		except the ygr-cubie). We represent it as a 2x7 field (more compact).
     * </ul>
     * Detail STICKER: The coding (cell numbering) of the 7x7 stickers field:
     * <pre>
     *           0  1  2  3  4  5  6
     *       a  00 01 02 03 04 05 06
     *       b  07 08 09 10 11 12 13
     *       c  14 15 16 17 18 19 20
     *       d  21 22 23 24 25 26 27
     *       e  28 29 30 31 32 33 34
     *       f  35 36 37 38 39 40 41
     *       g  42 43 44 45 46 47 48
     * </pre>
     * <p>
     * Detail STICKER2: If we have a sticker vector [d2 b1 c1 h3   f1 a3 g2], we code it as
     * <pre>
     *           0  1  2  3  4  5  6
     *     cor   d  b  c  h  f  a  g
     *    face   2  1  1  3  1  3  2
     * </pre>
     * The cell numbering of the 2x7 stickers field:
     * <pre>
     *           0  1  2  3  4  5  6
     *     cor  00 01 02 03 04 05 06
     *    face  07 08 09 10 11 12 13
     * </pre>
     *
     * @return an int[] vector representing the 'board' state (= cube state)
     *
     * NOTE: Currently, the implementation is only valid for 2x2x2 cube
     */
    public BoardVector getBoardVector() {
        if (true)
            throw new RuntimeException("getBoardVector not yet adapted to 3x3x3!");

        // needed for STICKER and STICKER2
        final int[] orig = {0,1,2,3,13,14,15}; 	// the original locations of the tracked stickers
        final Cor cor[] = {Cor.a,Cor.b,Cor.c,Cor.d,Cor.a,Cor.d,Cor.h,Cor.g,Cor.a,Cor.g,Cor.f,Cor.b,Cor.e,Cor.f,Cor.g,Cor.h,Cor.e,Cor.c,Cor.b,Cor.f,Cor.e,Cor.h,Cor.d,Cor.c};
        final int[] face = {1,1,1,1,2,3,2,3,3,2,3,2,1,1,1,1,2,2,3,2,3,3,2,3};

        int[] bvec;
        switch (CubeConfig.boardVecType) {
            case CUBESTATE:
                bvec = fcol.clone();
                break;
            case CUBEPLUSACTION:
                bvec = new int[fcol.length+2];
                System.arraycopy(this.fcol, 0, bvec, 0, fcol.length);
                bvec[fcol.length] = this.lastTwist.ordinal();
                bvec[fcol.length+1] = this.lastTimes;
                break;
            case STICKER:
                // TODO: STICKER implementation for 3x3x3
                throw new RuntimeException("Case STICKER for 3x3x3 cube not yet implemented");
            case STICKER2:
                int[][] board2 = new int[2][7];
                for (int i=0; i<7; i++) {		// set in every column i (sticker) the row cell specified by 'cor'
                    board2[0][i] = cor[sloc[orig[i]]].ordinal();
                    board2[1][i] = face[sloc[orig[i]]];
                }

                // copy to linear bvec according to STICKER2 coding specified above
                bvec = new int[7*2];
                for (int j=0, k=0; j<2; j++)
                    for (int i=0; i<7; i++,k++)
                        bvec[k] = board2[j][i];
                break;
            default:
                throw new RuntimeException("Illegal value in switch boardVecType");
        }
        return new BoardVector(bvec,sloc);   // return a BoardVector with aux = sloc (needed to reconstruct CubeState from BoardVector)
    }

    //
    // --- TODO: this has to be adapted to 3x3x3 ! ---
    //                                		0          4         8            12           16           20
    private static final int[] 	invF_2x2 = {18,19,2,3,  1, 5,6,0, 11, 8, 9,10, 12, 7, 4,15, 16,17,13,14, 20,21,22,23},
            invL_2x2 = { 9, 1,2,8,  7, 4,5,6, 14,15,10,11, 12,13,21,22, 16,17,18,19, 20, 3, 0,23},
            invU_2x2 = { 3, 0,1,2, 22,23,6,7,  5, 9,10, 4, 12,13,14,15, 16,11, 8,19, 20,21,17,18};
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
        assert (CubeConfig.cubeType== CubeConfig.CubeType.RUBIKS);
        invF = invF_2x2;
        invL = invL_2x2;
        invU = invU_2x2;
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
