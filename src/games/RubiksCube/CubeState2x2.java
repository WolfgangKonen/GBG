package games.RubiksCube;

import games.BoardVector;

import java.io.Serial;
import java.util.Map;

/**
 * See {@link CubeState} for class description
 */
public class CubeState2x2 extends CubeState {

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip will become unreadable, or you have
     * to provide a special version transformation)
     */
    @Serial
    private static final long  serialVersionUID = -4066880431436899009L;

    // --- never used ---
//    public CubeState2x2() {
//        super(Type.COLOR_P);
//    }

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
        assert (CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET);
        CubeStateFactory csFactory = new CubeStateFactory();
        int[] bvec = boardVector.bvec;
        switch (bvec.length) {
            case 24 -> {    // boardvecType == CUBESTATE, 2x2x2
                this.type = Type.COLOR_P;
                this.fcol = bvec.clone();
                this.sloc = boardVector.aux.clone();
            }
            case 26 -> {    // boardvecType == CUBEPLUSACTION, 2x2x2
                this.type = Type.COLOR_P;
                this.fcol = new int[24];
                System.arraycopy(bvec, 0, this.fcol, 0, 24);
                this.sloc = boardVector.aux.clone();
            }
            case 49 -> {    // boardvecType == STICKER, 2x2x2
                this.type = Type.COLOR_P;
                this.sloc = slocFromSTICKER(bvec);
                CubeState def = csFactory.makeCubeState(this.type);
                this.fcol = new int[def.fcol.length];
                for (int i = 0; i < fcol.length; i++) this.fcol[sloc[i]] = def.fcol[i];
            }
            case 14 ->    // boardvecType == STICKER2, 2x2x2
                    throw new RuntimeException("Case bvec.length = " + bvec.length + " (STICKER2) not yet implemented.");
            default -> throw new RuntimeException("Case bvec.length = " + bvec.length + " not yet implemented.");
        }
    }

//    protected CubeState apply_sloc_wcr(CubeState trafo, boolean doAssert) {
//        int[] tmp = sloc.clone();
//        for (int i=0; i<sloc.length; i++)  sloc[trafo.fcol[i]]=tmp[i];
//
//        if (doAssert) {
//            CubeState2x2 cs2 = new CubeState2x2(this);
//            cs2.apply_sloc_slow(trafo,doAssert );
//            assert (cs2.isEqual(this)) : "sloc_slow check: cs2 and this differ!";
//        }
//        return this;
//    }

    /**
     * Apply transformation {@code trafo} to {@link #sloc}. Needs {@link #fcol} to be transformed
     * already, but does not need {@code trafo} (it is only here to have the same signature as
     * {@link #apply_sloc(CubeState, boolean) apply_sloc}).
     * Do it the <b>slow</b> way (as an independent check): <br>
     * 1) {@link #locate(CubieTriple) locate} one sticker {@code s} for every cubie by finding the right color
     * configuration in {@code this.}{@link #fcol} <br>
     * 2) Set {@link #sloc}{@code [s]} to this location, set the clockwise-right sticker to the 2nd cubie location, and
     * set its clockwise-right neighbor to the 3rd location.
     *
     * @param trafo a {@link CubeState} object of type TRAFO_P or TRAFO_R
     * @param doAssert assert the correctness of {@link #sloc} transformation by checking the {@code fcol[sloc]}-relation.
     * @return {@code this} with its member {@link #sloc} transformed
     */
    protected CubeState apply_sloc_slow(CubeState trafo, boolean doAssert) {
        assert(type==Type.COLOR_P || type==Type.COLOR_R) : "Wrong type "+type+" in apply_sloc_slow() !";
        int[] stickers = {0,1,2,3,12,13,14,15};     // the necessary stickers (one for every cubie)
        int[] rig = CubieTriple.right;
        CubieTriple where;
        // we implement it the slow way by locating each of the eight cubies:
        for (int s : stickers) {
            where = locate(ctFactory.makeCubieTriple(s));
            sloc[s] = where.loc[0];
            sloc[rig[s]] = where.loc[1];
            sloc[rig[rig[s]]] = where.loc[2];
        }

        if (doAssert) {
            CubeState def = csFactory.makeCubeState();   // COLOR_P or COLOR_R
            for (int i=0; i<sloc.length; i++)
                assert (fcol[sloc[i]] == def.fcol[i]) : "fcol[sloc[i]]-relation violated for i="+i;
        }

        return this;
    }

    /**
     * Apply color transformation {@code cT} to {@code this}: Each face color {@code fcol[i]} gets the new color
     * {@code cT.ccol[fcol[i]]}. Each sticker location {@code sloc[i]} gets its new sticker after color transform.  <br>
     * {@code this} has to be of type COLOR_P or COLOR_R.
     * @param cT color transformation
     * @param doAssert assert the correctness of {@link #sloc} transformation by calling
     *          {@link CubeState#apply_sloc_slow(CubeState, boolean) apply_sloc_slow}
     * @return the transformed {@code this}
     */
    public CubeState applyCT(ColorTrafo cT, boolean doAssert) {
        CubieTriple ygrCubie = ctFactory.makeCubieTriple();
        assert(this.type==Type.COLOR_P || this.type==Type.COLOR_R) : "Wrong type "+this.type+" in apply(cT) !";
        // 1) apply the color trafo to fcol:
        int[] tmp = this.fcol.clone();
        for (int i=0; i<fcol.length; i++) this.fcol[i] = cT.getCCol(tmp[i]);

        // 2) apply the color trafo to sloc:
        applyCT_sloc(cT,doAssert);

        // 3) locate the ygr-cubie and its forward whole-cube rot:
        CubieTriple where = this.locate(ygrCubie);
        Integer iWholeCubeRot = CubeStateMap.map_ygr_wholeKey.get(where.loc[0]);
        // --- only debug-printout: ---
//            DecimalFormat form = new DecimalFormat("00");
//            System.out.println("key: "+form.format(entry.getKey())
//                    + ", y-sticker: "+form.format(where.loc[0])
//                    + ", iWholeCubeRot: "+form.format(iWholeCubeRot));

        // 4) apply the corresponding whole-cube rotation that brings the ygr-cubie 'home':
        CubeState trafo = CubeStateMap.allWholeCubeRots.get(iWholeCubeRot);
        //applyWholeCubeRot(iWholeCubeRot,tS);      // 3.a: WRONG! (and no longer needed)
        this.apply(trafo,doAssert);                 // 3.b: works now, with repaired apply_sloc in apply
//      tmp = this.fcol.clone();                    // 3.c: not needed: is the same as 3.b. with apply_sloc
//      for (int i=0; i<fcol.length; i++)           //      and is slow with apply_sloc_slow
//          this.fcol[i] = tmp[trafo.fcol[i]];      //
//      //apply_sloc_slow(trafo,doAssert);          //
//      apply_sloc(trafo,doAssert);                 //

        if (doAssert) {
            CubeState2x2 cs2 = new CubeState2x2(this);
            cs2.apply_sloc_slow(trafo, doAssert);
            assert (cs2.isEqual(this)) : "sloc_slow check: cs2 and this differ!";
        }

        return this;
    }

    // --- not needed ---
//  public void applyWholeCubeRot(int iWholeCubeRot, CubeState tS) {
//        // given iWholeCubeRot, build the inverse trafo
//        tS.uTr(iWholeCubeRot % 4);
//        int iRot = iWholeCubeRot/4;
//        switch(iRot) {
//            case 0:
//                break;
//            case 1:
//                tS.fTr(3);
//                break;
//            case 2:
//                tS.fTr(2);
//                break;
//            case 3:
//                tS.fTr(1);
//                break;
//            case 4:
//                tS.lTr(3);
//                break;
//            case 5:
//                tS.lTr(1);
//                break;
//        }
//
//    }

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
     * @return a board vector representing the 'board' state (= cube state)
     *
     */
    public BoardVector getBoardVector() {
        // needed for STICKER and STICKER2
        final int[] orig = {0,1,2,3,13,14,15}; 	// the original locations of the tracked stickers
        // cor[i]: For face location i (out  of 24): to which corner-cubie does it belong?
        //                 00                       04                       08
        final Cor[] cor = {Cor.a,Cor.b,Cor.c,Cor.d, Cor.a,Cor.d,Cor.h,Cor.g, Cor.a,Cor.g,Cor.f,Cor.b,
        //                 12                       16                       20
                           Cor.e,Cor.f,Cor.g,Cor.h, Cor.e,Cor.c,Cor.b,Cor.f, Cor.e,Cor.h,Cor.d,Cor.c};
        final int[] face = {1,1,1,1,2,3,2,3,3,2,3,2,1,1,1,1,2,2,3,2,3,3,2,3};
        int column;
        int[] bvec;

        switch (CubeConfig.boardVecType) {
            case CUBESTATE -> bvec = fcol.clone();
            case CUBEPLUSACTION -> {
                bvec = new int[fcol.length + 2];
                System.arraycopy(this.fcol, 0, bvec, 0, fcol.length);
                bvec[fcol.length] = this.lastTwist.ordinal();
                bvec[fcol.length + 1] = this.lastTimes;
            }
            case STICKER -> {
                int[][] board = new int[7][7];
                for (int i = 0; i < 7; i++) {        // set in every column i (sticker) the row cell specified by 'cor'
                    // to the appropriate face value:
                    column = cor[sloc[orig[i]]].ordinal();
                    //assert column!=4;	// should not be the ygr-cubie
                    if (column > 4) column = column - 1;
                    //assert column<7;
                    board[column][i] = face[sloc[orig[i]]];
                }

                // copy to linear bvec according to STICKER coding specified above
                bvec = new int[7 * 7];
                for (int j = 0, k = 0; j < 7; j++)
                    for (int i = 0; i < 7; i++, k++)
                        bvec[k] = board[j][i];
            }
            case STICKER2 -> {
                int[][] board2 = new int[2][7];
                for (int i = 0; i < 7; i++) {        // set in every column i (sticker) the location specified by {cor,face}
                    column = cor[sloc[orig[i]]].ordinal();
                    //assert column!=4;	// should not be the ygr-cubie
                    if (column > 4) column = column - 1;
                    board2[0][i] = column;
                    board2[1][i] = face[sloc[orig[i]]] - 1;       // map faces 1,2,3 to 0,1,2
                }

                // copy to linear bvec according to STICKER2 coding specified above
                bvec = new int[7 * 2];
                for (int j = 0, k = 0; j < 2; j++)
                    for (int i = 0; i < 7; i++, k++)
                        bvec[k] = board2[j][i];
            }
            default -> throw new RuntimeException("Illegal value in switch boardVecType");
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
        int column;

        // index arithmetic, part one
        if (z<4) column=z;
        else if (z==4) return corfac;	// the ygr-cubie case
        else column=z-1;				// cases z=5,6,7 address column 4,5,6 of the STICKER board

        // find (row number, value) of the only non-zero element in 'column':
        int nonzero = 0;
        int rv;
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

    // invF_2x2[i] is the sticker location which moves under an F-twist to location i. E.g. sticker 18 moves to location 0.
    //                                		0          4         8            12           16           20
    private static final int[] 	invF_2x2 = {18,19,2,3,  1, 5,6,0, 11, 8, 9,10, 12, 7, 4,15, 16,17,13,14, 20,21,22,23},
                                invL_2x2 = { 9, 1,2,8,  7, 4,5,6, 14,15,10,11, 12,13,21,22, 16,17,18,19, 20, 3, 0,23},
                                invU_2x2 = { 3, 0,1,2, 22,23,6,7,  5, 9,10, 4, 12,13,14,15, 16,11, 8,19, 20,21,17,18};
    //
    // use the following line once on a default TRAFO_P CubeState t_cs to generate int[] invL above:
    //			return t_cs.uTr().FTw().uTr().uTr().uTr();   	// L(x) = u^3(F(u(x)))
    // use the following line once on a default TRAFO_P CubeState t_cs to generate int[] invU above:
    //			return t_cs.lTr().lTr().lTr().FTw().lTr();   	// U(x) = l(F(l^3(x)))
    // (see CubeState2x2.show_invF_invL_invU(), which is called once by ArenaCube.makeGameBoard if SHOW_INV==true)

    // invD|R|B_2x2 are just dummies, we do not need these twists for 2x2x2, but we need the variables to be present to
    // get everything compiled
    private static final int[] 	invD_2x2 = {18,19,2,3,  1, 5,6,0, 11, 8, 9,10, 12, 7, 4,15, 16,17,13,14, 20,21,22,23},
            invR_2x2 = { 9, 1,2,8,  7, 4,5,6, 14,15,10,11, 12,13,21,22, 16,17,18,19, 20, 3, 0,23},
            invB_2x2 = { 3, 0,1,2, 22,23,6,7,  5, 9,10, 4, 12,13,14,15, 16,11, 8,19, 20,21,17,18};

    /**
     * generate the <b>inverse</b> transformations {@link #invF}, {@link #invL} and {@link #invU}.
     */
    public static void generateInverseTs() {
        assert (CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET);
        invU = invU_2x2;
        invL = invL_2x2;
        invF = invF_2x2;
        invD = invD_2x2;
        invR = invR_2x2;
        invB = invB_2x2;
    }

    protected void show_invF_invL_invU() {
        CubeState2x2 t_cs = new CubeState2x2(CubeState.Type.TRAFO_P);
        System.out.println("--- generated by show_invF_invL_invU ---");
        t_cs.FTw();											// invF
        System.out.println("invF: "+t_cs);
        t_cs = new CubeState2x2(CubeState.Type.TRAFO_P);
        t_cs.uTr().FTw().uTr().uTr().uTr();					// invL
        System.out.println("invL: "+t_cs);
        t_cs = new CubeState2x2(CubeState.Type.TRAFO_P);
        t_cs.lTr().lTr().lTr().FTw().lTr();					// invU
        System.out.println("invU: "+t_cs);
        System.out.println();
    }

    /**
     * Whole-cube rotation 90° counter-clockwise around the u-face
     */
    protected CubeState uTr() {
        int i;
        // fcol(invT[i]) is the color which cubie face i gets after transformation:
        int[] invT = {3,0,1,2,22,23,20,21,5,6,7,4,13,14,15,12,10,11,8,9,19,16,17,18};
        int[] tmp = this.fcol.clone();
        for (i=0; i<invT.length; i++) this.fcol[i] = tmp[invT[i]];
        tmp = this.sloc.clone();
        //for (i=0; i<sloc.length; i++) this.sloc[invT[i]] = tmp[i];    // WRONG!
        //for (i=0; i<sloc.length; i++) this.sloc[fcol[i]] = i;         // ONLY correct if this is of type TRAFO
        int[] T = new int[invT.length];                                 // CORRECT for both types, COLOR and TRAFO
        for (i=0; i<invT.length; i++) T[invT[i]] = i;
        for (i=0; i<sloc.length; i++) sloc[i] = T[tmp[i]];

        //this.assert_fcol_sloc(" from uTr()");
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
        for (i=0; i<invT.length; i++) this.fcol[i] = tmp[invT[i]];
        tmp = this.sloc.clone();
        //for (i=0; i<sloc.length; i++) this.sloc[invT[i]] = tmp[i];    // WRONG!
        //for (i=0; i<sloc.length; i++) this.sloc[fcol[i]] = i;         // ONLY correct if this is of type TRAFO
        int[] T = new int[invT.length];                                 // CORRECT for both types, COLOR and TRAFO
        for (i=0; i<invT.length; i++) T[invT[i]] = i;
        for (i=0; i<sloc.length; i++) sloc[i] = T[tmp[i]];

        //this.assert_fcol_sloc(" from fTr()");
        return this;
    }

}
