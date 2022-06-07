package games.RubiksCube;

import games.BoardVector;

import java.io.Serial;

/**
 * See {@link CubeState} for class description
 */
public class CubeState3x3 extends CubeState {

    /**
     * We number the edges in the top layer with {E0,E1,E2,E3} in the order of the U face locations {0,2,4,6}
     * and those in the middle layer with {E4,E5,E6,E7} in the order of the locations {17,21,43,47} on F and B cube face
     * and those in the bottom layer with {E8,E9,Ea,Eb} in the order of the D face locations {24,26,28,30}.
     * @see GameBoardCubeGui3x3
     */
    public enum Edg {E0,E1,E2,E3,E4,E5,E6,E7,E8,E9,Ea,Eb}

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip will become unreadable, or you have
     * to provide a special version transformation)
     */
    @Serial
    private static final long  serialVersionUID = 12L;

//    public CubeState3x3() {
//        super(Type.COLOR_P);
//    }

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
        CubeStateFactory csFactory = new CubeStateFactory();  // may be needed later
        int[] bvec = boardVector.bvec;
        switch (bvec.length) {
            case 48 -> {    // boardvecType == CUBESTATE, 3x3x3
                this.type = Type.COLOR_R;
                this.fcol = bvec.clone();
            }
            case 50 -> {    // boardvecType == CUBEPLUSACTION, 3x3x3
                this.type = Type.COLOR_R;
                this.fcol = new int[48];
                System.arraycopy(bvec, 0, this.fcol, 0, 48);
            }
            case 147 -> // = 20*24, see notes-WK-RubiksCube.docx, boardvecType == STICKER, 3x3x3
                    // TODO: STICKER implementation for 3x3x3
                    throw new RuntimeException("Case STICKER for 3x3x3 cube not yet implemented");
            case 40 -> {  // = 2*8+2*12, see notes-WK-RubiksCube.docx, boardvecType == STICKER2, 3x3x3
                this.type = Type.COLOR_P;
                this.sloc = slocFromSTICKER2(bvec);
                CubeState def = csFactory.makeCubeState(this.type);
                this.fcol = new int[def.fcol.length];
                for (int i = 0; i < fcol.length; i++) this.fcol[sloc[i]] = def.fcol[i];
            }
            default -> throw new RuntimeException("Case bvec.length = " + bvec.length + " not yet implemented.");
        }
    }

    /**
     * just for a one-time printout of invU and invL, which can be calculated if we have {@link #FTw()}, which is based on invF
     */
    protected void show_invF_invL_invU() {
        CubeState3x3 t_cs = new CubeState3x3(CubeState.Type.TRAFO_R);
        t_cs.lTr().lTr().lTr().FTw().lTr();					// invU
        System.out.println("invU: "+t_cs);
        t_cs = new CubeState3x3(CubeState.Type.TRAFO_R);
        t_cs.uTr().FTw().uTr().uTr().uTr();					// invL
        System.out.println("invL: "+t_cs);
        t_cs = new CubeState3x3(CubeState.Type.TRAFO_R);
        t_cs.FTw();											// invF
        System.out.println("invF: "+t_cs);
    }

    /**
     * Locate the edge cubie with the colors of {@link CubieDouble} {@code edg} in {@code this}.
     * {@code this} has to be of type COLOR_P or COLOR_R.
     * <p>
     *     Details:
     * <ul>
     * <li> This method is only needed if we want to use color symmetries.
     * <li> This method relies on member {@code floc} to be transformed, member {@code sloc} needs not to be transformed.
     * </ul>
     *
     * @param edg an edge cubie
     * @return a {@link CubieDouble} whose member {@code loc} carries the location of the cubie with
     * 		   the colors of {@code edg}.
     */
    public CubieDouble locate_edge(CubieDouble edg) {
        assert(type==Type.COLOR_P || type==Type.COLOR_R) : "Wrong type "+type+" in locate() !";
        int[] other = CubieDouble.other;
        int oth;
        for (int i=1; i<fcol.length; i=i+2)
            assert(other[other[i]]==i) : "other-assertion failed for i="+i;
        CubieDouble where = new CubieDouble(edg);
        for (int i=1; i<fcol.length; i=i+2) {
            if (fcol[i]==edg.col[0]) {
                where.loc[0]=i;
                oth = other[i];
                if (fcol[oth]==edg.col[1]) {
                    where.loc[1]=oth;
                    return where;
                }
            }
        }
        throw new RuntimeException("Invalid cube, we should not arrive here!");
    }

    protected CubeState apply_sloc_slow(CubeState trafo, boolean doAssert) {
        assert(type==Type.COLOR_P || type==Type.COLOR_R) : "Wrong type "+type+" in apply_sloc_slow() !";
        int[] stickers_c = {0,2,4,6,24,26,28,30};     // the necessary corner stickers (one for every corner cubie)
        int[] rig = CubieTriple.right;
        CubieTriple where;
        // we implement it the slow way by locating each of the eight corner cubies:

        for (int s : stickers_c) {
            where = locate(ctFactory.makeCubieTriple(s));
            sloc[s] = where.loc[0];
            sloc[rig[s]] = where.loc[1];
            sloc[rig[rig[s]]] = where.loc[2];
        }

        int[] stickers_e = {1,3,5,7, 25,27,29,31, 11,15,21,33};     // the necessary edge stickers (one for every edge cubie)
        int[] oth = CubieDouble.other;
        CubieDouble edge;
        for (int s : stickers_e) {
            edge = locate_edge(new CubieDouble(s));
            sloc[s] = edge.loc[0];
            sloc[oth[s]] = edge.loc[1];
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
        assert(this.type==Type.COLOR_P || this.type==Type.COLOR_R) : "Wrong type "+this.type+" in apply(cT) !";
        // 1) apply the color trafo to fcol:
        int[] tmp = this.fcol.clone();
        for (int i=0; i<fcol.length; i++) this.fcol[i] = cT.getCCol(tmp[i]);

        // 2) apply the color trafo to sloc:
        applyCT_sloc(cT,doAssert);

        // 3) apply the inverse whole-cube rotation that brings the center cubies back to their 'home' position
        //    (here it differs from CubeState2x2.applyCT)
        int inv_wcr = CubeStateMap.map_inv_wholeCube.get(cT.getKey());
        CubeState itrafo = CubeStateMap.allWholeCubeRots.get(inv_wcr);
        this.apply(itrafo,doAssert);

        if (doAssert) {
            CubeState cs2 = csFactory.makeCubeState(this);
            cs2.apply_sloc_slow(itrafo, doAssert);
            assert (cs2.isEqual(this)) : "sloc_slow check: cs2 and this differ!";
        }

        return this;
    }

    /**
     * Whole-cube rotation 90° counter-clockwise around the u-face
     */
    protected CubeState uTr() {
        int i;
        // fcol(invT[i]) is the color which cubie face i gets after transformation:
        int[] invT = { 6, 7, 0, 1, 2, 3, 4, 5, 44,45,46,47,40,41,42,43, 10,11,12,13,14,15, 8, 9,
                      26,27,28,29,30,31,24,25, 20,21,22,23,16,17,18,19, 38,39,32,33,34,35,36,37};
        int[] tmp = this.fcol.clone();
        for (i=0; i<invT.length; i++) this.fcol[i] = tmp[invT[i]];
        tmp = this.sloc.clone();
        //for (i=0; i<sloc.length; i++) this.sloc[invT[i]] = tmp[i];    // WRONG!
        //for (i=0; i<sloc.length; i++) this.sloc[fcol[i]] = i;         // ONLY correct if this is of type TRAFO
        int[] T = new int[invT.length];                                 // CORRECT for both types, COLOR and TRAFO
        for (i=0; i<invT.length; i++) T[invT[i]] = i;
        for (i=0; i<sloc.length; i++) sloc[i] = T[tmp[i]];

        return this;
    }

    /**
     * Whole-cube rotation 90° counter-clockwise around the f-face
     */
    protected CubeState fTr() {
        int i;
        // fcol(invT[i]) is the color which cubie face i gets after transformation:
        int[] invT = {36,37,38,39,32,33,34,35,  2, 3, 4, 5, 6, 7, 0, 1, 22,23,16,17,18,19,20,21,
                      12,13,14,15, 8, 9,10,11, 30,31,24,25,26,27,28,29, 42,43,44,45,46,47,40,41};
        int[] tmp = this.fcol.clone();
        for (i=0; i<invT.length; i++) this.fcol[i] = tmp[invT[i]];
        tmp = this.sloc.clone();
        //for (i=0; i<sloc.length; i++) this.sloc[invT[i]] = tmp[i];    // WRONG!
        //for (i=0; i<sloc.length; i++) this.sloc[fcol[i]] = i;         // ONLY correct if this is of type TRAFO
        int[] T = new int[invT.length];                                 // CORRECT for both types, COLOR and TRAFO
        for (i=0; i<invT.length; i++) T[invT[i]] = i;
        for (i=0; i<sloc.length; i++) sloc[i] = T[tmp[i]];

        return this;
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
     * <li> <b>STICKER2</b>: similar to STICKER, we track the location of eight corner stickers (one face of each cubie).
     * 		We represent it as a 2x8 + 2*12 = 40-cell field (more compact).
     * </ul>
     * Detail STICKER2: If we have a sticker vector [d1 b0 c0 h2  e0 f0 a2 g1   E50 E01 ... E41 Eb1], we code it as
     * <pre>
     *           0  1  2  3  4  5  6  7  8  9 10 11
     *     cor   d  b  c  h  e  f  a  g
     *    face   1  0  0  2  0  0  2  1
     *    edge  E5 E0 ...                 ... E4 Eb
     *    face   0  1 ...                 ...  1  1
     * </pre>
     * The cell numbering of the 2*(8+12) = 40 stickers field:
     * <pre>
     *           0  1  2  3  4  5  6  7  8  9 10 11
     *     cor  00 01 02 03 04 05 06 07
     *    face  08 09 10 11 12 13 14 15
     *    edge  16 17 18 19 20 21 22 23 24 25 26 27
     *    face  28 29 30 31 32 33 34 35 36 37 38 39
     * </pre>
     *
     * @return an int[] vector representing the 'board' state (= cube state)
     */
    public BoardVector getBoardVector() {
        // needed for STICKER and STICKER2
        final int[] orig = {0,2,4,8, 24,26,28,30}; 	// the original locations of the 8 tracked corner stickers
        final int oL = orig.length;
        final int[] origE = {1,3,5,7, 17,21,43,47, 25,27,29,31}; 	// the original locations of the 12 tracked edge stickers
        final int oE = origE.length;
        // cor[i]: For face location i (even i \in {0,...,46}): to which corner-cubie does it belong?
        //                 00                                           08
        final Cor[] cor = {Cor.a,null,Cor.b,null,Cor.c,null,Cor.d,null, Cor.a,null,Cor.d,null,Cor.h,null,Cor.g,null,
        //                 16                                           24
                           Cor.a,null,Cor.g,null,Cor.f,null,Cor.b,null, Cor.e,null,Cor.f,null,Cor.g,null,Cor.h,null,
        //                 32                                           40
                           Cor.e,null,Cor.c,null,Cor.b,null,Cor.f,null, Cor.e,null,Cor.h,null,Cor.d,null,Cor.c,null};
        // edg[i]: For face location i (odd i \in {1,...,47}: to which edge-cubie does it belong?
        //                 00                                               08
        final Edg[] edg = {null,Edg.E0,null,Edg.E1,null,Edg.E2,null,Edg.E3, null,Edg.E3,null,Edg.E6,null,Edg.Ea,null,Edg.E4,
        //                 16                                           24
                           null,Edg.E4,null,Edg.E9,null,Edg.E5,null,Edg.E0, null,Edg.E8,null,Edg.E9,null,Edg.Ea,null,Edg.Eb,
        //                 32                                           40
                           null,Edg.E7,null,Edg.E1,null,Edg.E5,null,Edg.E8, null,Edg.Eb,null,Edg.E6,null,Edg.E2,null,Edg.E7};
        // E0	E1	E2	E3	E3	E6	Ea	E4	E4	E9	E5	E0	E8	E9	Ea	Eb	E7	E1	E5	E8	Eb	E6	E2	E7
        // 1	1	1	1	2	2	2	2	1	1	1	1	1	1	1	1	2	2	2	2	1	1	1	1
        // face[i]: for even i: the corner faces \in {1,2,3}; for odd i: the edge faces \in {1,2}
        //                 00               08               16
        final int[] face = {1,1,1,1,1,1,1,1, 2,2,3,2,2,2,3,2, 3,1,2,1,3,1,2,1,
        //                 24               32               40
                            1,1,1,1,1,1,1,1, 2,2,2,2,3,2,2,2, 3,1,3,1,2,1,3,1};

        int[] bvec;
        switch (CubeConfig.boardVecType) {
            case CUBESTATE -> bvec = fcol.clone();
            case CUBEPLUSACTION -> {
                bvec = new int[fcol.length + 2];
                System.arraycopy(this.fcol, 0, bvec, 0, fcol.length);
                bvec[fcol.length] = this.lastTwist.ordinal();
                bvec[fcol.length + 1] = this.lastTimes;
            }
            case STICKER ->
                    // TODO: STICKER implementation for 3x3x3
                    throw new RuntimeException("[getBoardVector] Case STICKER for 3x3x3 cube not yet implemented");
            case STICKER2 -> {
                int[][] board2 = new int[2][oL];
                for (int i = 0; i < oL; i++) {        // set in every column i (sticker) the location specified by {cor,face}
                    board2[0][i] = cor[sloc[orig[i]]].ordinal();
                    board2[1][i] = face[sloc[orig[i]]] - 1;       // map faces 1,2,3 to 0,1,2
                }
                int[][] board3 = new int[2][oE];
                for (int i = 0; i < oE; i++) {        // set in every column i (sticker) the location specified by {edg,face}
                    board3[0][i] = edg[sloc[origE[i]]].ordinal();
                    board3[1][i] = face[sloc[origE[i]]] - 1;       // map faces 1,2 to 0,1
                }

                // copy to linear bvec according to STICKER2 coding specified above
                bvec = new int[2 * (oL + oE)];
                int k = 0;
                for (int j = 0; j < 2; j++)
                    for (int i = 0; i < oL; i++, k++)
                        bvec[k] = board2[j][i];
                for (int j = 0; j < 2; j++)
                    for (int i = 0; i < oE; i++, k++)
                        bvec[k] = board3[j][i];
            }
            default -> throw new RuntimeException("Illegal value in switch boardVecType");
        }
        return new BoardVector(bvec,sloc);   // return a BoardVector with aux = sloc (needed to reconstruct CubeState from BoardVector)
    }

    //
    // invL_3x3[i] is the sticker location which moves under an L-twist to location i. E.g. sticker 18 moves to location 0.
    private static final int[]
            invU_3x3 = { 6, 7, 0, 1, 2, 3, 4, 5, 44,45,46,11,12,13,14,15, 10,17,18,19,20,21, 8, 9,
                        24,25,26,27,28,29,30,31, 32,33,22,23,16,37,38,39, 40,41,42,43,34,35,36,47},
            invL_3x3 = {18, 1, 2, 3, 4, 5,16,17, 14,15, 8, 9,10,11,12,13, 28,29,30,19,20,21,22,23,
                        24,25,26,27,42,43,44,31, 32,33,34,35,36,37,38,39, 40,41, 6, 7, 0,45,46,47},
            invF_3x3 = {36,37,38, 3, 4, 5, 6, 7,  2, 9,10,11,12,13, 0, 1, 22,23,16,17,18,19,20,21,
                        24,25,14,15, 8,29,30,31, 32,33,34,35,26,27,28,39, 40,41,42,43,44,45,46,47},
            invD_3x3 = { 0, 1, 2, 3, 4, 5, 6, 7,  8, 9,10,11,18,19,20,15, 16,17,38,39,32,21,22,23,
                        30,31,24,25,26,27,28,29, 42,33,34,35,36,37,40,41, 12,13,14,43,44,45,46,47},
            invR_3x3 = { 0, 1,46,47,40, 5, 6, 7,  8, 9,10,11,12,13,14,15, 16,17,18,19, 2, 3, 4,23,
                        20,21,22,27,28,29,30,31, 38,39,32,33,34,35,36,37, 26,41,42,43,44,45,24,25},
            invB_3x3 = { 0, 1, 2, 3,10,11,12, 7,  8, 9,30,31,24,13,14,15, 16,17,18,19,20,21,22,23,
                        34,25,26,27,28,29,32,33,  4, 5, 6,35,36,37,38,39, 46,47,40,41,42,43,44,45};
    //
    // use the following line once on a default TRAFO_R CubeState t_cs to generate int[] invU above:
    //			return t_cs.lTr().lTr().lTr().FTw().lTr();   	// U(x) = l(F(l^3(x)))
    // use the following line once on a default TRAFO_R CubeState t_cs to generate int[] invL above:
    //			return t_cs.uTr().FTw().uTr().uTr().uTr();   	// L(x) = u^3(F(u(x)))
    // (see CubeState3x3.show_invF_invL_invU(), which is called once by ArenaCube.makeGameBoard if SHOW_INV==true)

    /**
     * generate the <b>inverse</b> transformations {@link #invF}, {@link #invL} and {@link #invU}.
     */
    public static void generateInverseTs() {
        assert (CubeConfig.cubeSize == CubeConfig.CubeSize.RUBIKS);
        invU = invU_3x3;
        invL = invL_3x3;
        invF = invF_3x3;
        invD = invD_3x3;
        invR = invR_3x3;
        invB = invB_3x3;
    }

    /**
     * Helper for CubeState3x3(BoardVector):
     * Given a board vector in STICKER2 representation, reconstruct member {@code sloc}.
     * @param bvec	the board vector
     * @return <b>int[] sloc</b>. sloc[i] holds the new location of sticker i which is at location i in the default cube.
     */
    private int[] slocFromSTICKER2(int[] bvec) {
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


}
