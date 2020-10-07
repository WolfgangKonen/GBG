package games.RubiksCube;

import games.BoardVector;

/**
 * See {@link CubeState} for class description
 */
public class CubeState2x2 extends CubeState {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip will become unreadable or you have
     * to provide a special version transformation)
     */
    private static final long  serialVersionUID = -4066880431436899009L;

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
        super();    // would be called silently otherwise
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
     * Locate the cubie with the colors of {@link CubieTriple} {@code tri} in {@code this}.
     * {@code this} has to be of type COLOR_P or COLOR_R.<br>
     * [This method is only needed if we want to use color symmetries.]
     *
     * @param tri
     * @return a {@link CubieTriple} whose member {@code loc} carries the location of the cubie with
     * 		   the colors of {@code tri}.
     */
    public CubieTriple locate(CubieTriple tri) {
        CubieTriple where = new CubieTriple(tri);
        assert(this.type==Type.COLOR_P || this.type==Type.COLOR_R) : "Wrong type in apply() !";
        //            0           4          8          12         16          20
        int[] left = {4,11,17,22, 8,3,21,14, 0,7,13,18, 20,19,9,6, 12,23,1,10, 16,15,5,2};
        int[] right= {8,18,23,5, 0,22,15,9, 4,14,19,1, 16,10,7,21, 20,2,11,13, 12,6,3,17};
        int rig;
        switch(tri.ori) {
            case CLOCK:
                for (int i=0; i<fcol.length; i++) {
                    if (fcol[i]==tri.col[0]) {
                        where.loc[0]=i;
                        rig = right[i];
                        if (fcol[rig]==tri.col[1]) {
                            where.loc[1]=rig;
                            rig = right[rig];
                            if (fcol[rig]==tri.col[2]) {
                                where.loc[2]=rig;
                                return where;
                            }
                        }
                    }
                }
                break;
            case COUNTER:
                throw new RuntimeException("Case COUNTER not yet implemented");
        }
        throw new RuntimeException("Invalid cube, we should not arrive here!");
    }

    /**
     * There are four possible board vector types, depending on {@link CubeConfig#boardVecType}
     * <ul>
     * <li> <b>CUBESTATE</b>: the face color array of the cube, i.e. member {@link #fcol}
     * <li> <b>CUBEPLUSACTION</b>: like CUBESTATE, but with two more int values added: the ordinal of the last twist and
     * 		the number of quarter turns in this twist
     * <li> <b>STICKER</b>: similar to the coding suggested by [McAleer2018], we track the location of S=7 stickers
     * 		(one face of each cubie, except the ygr-cubie) when the cube is twisted away from its original position. We
     * 		represent it as a 7x7 field (one-hot encoding).
     * <li> <b>STICKER2</b>: similar to STICKER, we track the location of S=7 stickers (one face of each cubie,
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
        // needed for STICKER and STICKER2
        final int[] orig = {0,1,2,3,13,14,15}; 	// the original locations of the tracked stickers
        final Cor cor[] = {Cor.a,Cor.b,Cor.c,Cor.d,Cor.a,Cor.d,Cor.h,Cor.g,Cor.a,Cor.g,Cor.f,Cor.b,Cor.e,Cor.f,Cor.g,Cor.h,Cor.e,Cor.c,Cor.b,Cor.f,Cor.e,Cor.h,Cor.d,Cor.c};
        final int[] face = {1,1,1,1,2,3,2,3,3,2,3,2,1,1,1,1,2,2,3,2,3,3,2,3};
        int column;
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
                int[][] board = new int[7][7];
                for (int i=0; i<7; i++) {		// set in every column i (sticker) the row cell specified by 'cor'
                    // to the appropriate face value:
                    column = cor[sloc[orig[i]]].ordinal();
                    //assert column!=4;	// should not be the ygr-cubie
                    if (column>4) column = column-1;
                    //assert column<7;
                    board[column][i] = face[sloc[orig[i]]];
                }

                // copy to linear bvec according to STICKER coding specified above
                bvec = new int[7*7];
                for (int j=0, k=0; j<7; j++)
                    for (int i=0; i<7; i++,k++)
                        bvec[k] = board[j][i];
                break;
            case STICKER2:
                int[][] board2 = new int[2][7];
                for (int i=0; i<7; i++) {		// set in every column i (sticker) the row cell specified by 'cor'
                    column = cor[sloc[orig[i]]].ordinal();
                    //assert column!=4;	// should not be the ygr-cubie
                    if (column>4) column = column-1;
                    board2[0][i] = column;
                    board2[1][i] = face[sloc[orig[i]]]-1;       // map faces 1,2,3 to 0,1,2
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
