package games.RubiksCube;

import games.BoardVector;
import games.RubiksCube.ColorTrafoMap.ColMapType;
import games.RubiksCube.CubeConfig.BoardVecType;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

/**
 * Several JUnit tests for 2x2x2 Pocket Cube
 *
 * @see RubiksCubeTest
 */
public class PocketCubeTest {

    ArenaCube ar;
    GameBoardCube gb;		// needed for chooseStartState()

    protected Random rand = new Random(System.currentTimeMillis());
    protected final CubeStateFactory csFactory = new CubeStateFactory();
    protected final CubieTripleFactory ctFactory = new CubieTripleFactory();

    protected void init() {
        CubeConfig.cubeSize = CubeConfig.CubeSize.POCKET;

        ar = new ArenaCube("",false,true);
        gb = new GameBoardCube(ar);

        CubeStateFactory.generateInverseTs();
        CubeState.generateForwardTs();
    }

    /**
     * Four-times whole-cube rotation- and 90°-twist-test:
     *
     * 1) Test that 4x 90° whole-cube rotations u,l,f  on default cube lead to default cube again. <br>
     * 2) Test that 4x 90° twists UTw,LTw,FTw  on default cube lead to default cube again. <br>
     */
    @Test
    public void testFourTimeRots() {

        init();

        // Tests 1) 4x whole-cube rotations:

        CubeState def = csFactory.makeCubeState();
        CubeState rot = csFactory.makeCubeState(def);
        for (int k = 1; k <= 4; k++) {
            //System.out.println(k+"x u-rotation");
            rot.uTr(1);  //.print();
        }
        assert (def.isEqual(rot)) : "def and rot differ after 4x u-rotation!";

        //System.out.println(1+"x l-rotation");
        for (int k = 1; k <= 4; k++) {
            rot.lTr(1); //.print();
        }
        assert (def.isEqual(rot)) : "def and rot differ after 4x l-rotation!";

        for (int k = 1; k <= 4; k++) {
            //System.out.println(k+"x f-rotation");
            rot.fTr(1);  //.print();
        }
        assert (def.isEqual(rot)) : "def and rot differ after 4x f-rotation!";

        // Tests 2) 4x U-, L- and F-twist:

        //System.out.println(1+"x U twist");
        for (int k = 1; k <= 4; k++) {
            rot.UTw(1); //.print();
        }
        assert (def.isEqual(rot)) : "def and rot differ after 4x UTwist!";

        //System.out.println(1+"x L twist");
        for (int k = 1; k <= 4; k++) {
            rot.LTw(1); //.print();
        }
        assert (def.isEqual(rot)) : "def and rot differ after 4x LTwist!";

        //System.out.println(1+"x F twist");
        for (int k = 1; k <= 4; k++) {
            rot.FTw(1); //.print();
        }
        assert (def.isEqual(rot)) : "def and rot differ after 4x FTwist!";
        System.out.println("All tests in testFourTimeRots passed.");
    }

    /**
     * Further twist-tests:
     *
     * 1) Test that rep x UTw.LTw.FTw followed by rep x the inverse leads to default cube again, where rep is a random int.<br>
     * 2) Test that executing FTw, UTw, LTw on a cube state cs is identical to constructing the twist trafo and applying it
     *    via apply() to cs
     */
    @Test
    public void test_UTw_LTw_FTw() {

        init();

        // Test 1)
        CubeState def = csFactory.makeCubeState();
        CubeState rot = csFactory.makeCubeState(def);
        int runs = 3;
        for (int r=0; r<runs; r++) {
            int rep = 1+rand.nextInt(5);
            for (int k=0; k<rep; k++) rot.UTw(1).LTw(1).FTw(1);
            for (int k=0; k<rep; k++) rot.FTw(3).LTw(3).UTw(3);
            assert (def.isEqual(rot)) : "def and rot differ after rep x FTw.UTw.LTw!";
        }
        System.out.println("All twists rep x FTw.UTw.LTw followed by rep x the inverse lead back to the default cube");

        // Test 2)
        CubeState.Type trafoType = (CubeConfig.cubeSize== CubeConfig.CubeSize.POCKET)
                                 ? CubeState.Type.TRAFO_P : CubeState.Type.TRAFO_R;
        for (int tw=0; tw<3; tw++) {
            CubeState cs1 = csFactory.makeCubeState(def);
            cs1.LTw().UTw(3).fTr(2).FTw();      // apply some arbitrary twists
            CubeState cs2 = csFactory.makeCubeState(cs1);
            CubeState trafo = csFactory.makeCubeState(trafoType);
            switch (tw) {
                case 0 -> {
                    cs1.UTw();
                    trafo.UTw();
                }
                case 1 -> {
                    cs1.FTw();
                    trafo.FTw();
                }
                case 2 -> {
                    cs1.LTw();
                    trafo.LTw();
                }
            }
            cs2.apply(trafo,true);
            assert (cs1.isEqual(cs2)) : "cs1 and cs2 differ for case tw="+tw;
        }
        System.out.println("All twists U,L,F identical via .*Tw() and via apply() ");

    }

    /**
     * Test for a certain twisted cube state {@code cube}:
     * <ol>
     * <li> Does .LTw(1).FTw(1) lead to the same state as {@code apply(trafo)}?
     * <li> (only POCKET) Do all (or a selected subset of all) calor trafos deliver a {@link CubeState} that has
     *      its ygr-cubie at 'home'?
     * <li> How many distinct states produces the set of color trafos?
     * <li> For each color-transformed state: Does the inverse color trafo lead back to the original state?
     * </ol>
     * This test checks method {@link CubeState#applyCT(ColorTrafoMap)} and indirectly also the static members
     * {@link CubeStateMap#allWholeCubeRots} and {@link CubeStateMap#map_inv_wholeCube}. If POCKET, it checks also
     * {@link CubeStateMap#map_ygr_wholeKey}.
     */
    @Test
    public void test_colorTrafo() {
        init();
        boolean isPocketCube = (CubeConfig.cubeSize== CubeConfig.CubeSize.POCKET);
        boolean SELECTED = false;
        CubeState.Type trafoType = (CubeConfig.cubeSize== CubeConfig.CubeSize.POCKET)
                ? CubeState.Type.TRAFO_P : CubeState.Type.TRAFO_R;

        CubeState cS = csFactory.makeCubeState();
        cS.LTw(1).FTw(1);
        //cS.fTr(3);
        System.out.println("Start state 1: " + cS);

        CubeState cS2 = csFactory.makeCubeState();
        CubeState trafo = csFactory.makeCubeState(trafoType);
        trafo.LTw(1).FTw(1);
        //trafo.fTr(3);
        cS2.apply(trafo,true);
        System.out.println("Start state 2: " + cS2);

        assert (cS.isEqual(cS2)) : "cS and cS2 differ!";

        ColorTrafoMap allCT = new ColorTrafoMap(ColMapType.AllColorTrafos);
        ColorTrafoMap myMap;
        if (SELECTED) {
            int[] iSel = new int[]{12, 0, 1, 2, 5, 6, 8, 9, 15, 17};  // any subset of {0,1,...,23} (we have 24 color trafos)
            // 12 is 'blue up'
            myMap = new ColorTrafoMap();
            for (int j : iSel) myMap.put(j, allCT.get(j));
        } else {
            myMap = allCT;
        }

        CubeStateMap csMap = cS.applyCT(myMap,true);
        // this is the method to test: apply all color trafos stored in myMap to state cS
        // and return results in csMap

        HashSet<CubeState> distinctCS = new HashSet<>();
        for (Map.Entry<Integer, CubeState> entry : csMap.entrySet()) {
            CubeState ecs = csFactory.makeCubeState( entry.getValue());
            distinctCS.add(ecs);
            CubieTriple where = ecs.locate(ctFactory.makeCubieTriple());
            if (isPocketCube) {
                assert (where.loc[0] == 12) :             // 12 is the 'home' location of ygr's y-sticker
                        "ygr-cubie not at 'home' for " + ecs + " (color trafo key = " + entry.getKey() + ")";
                //System.out.println(ecs + " for ct=" + entry.getKey()+", ygr-cubie at "+where.loc[0]);
            }
        }
        if (isPocketCube)
            System.out.println("All color-transformed cS states have their ygr-cubie at 'home'.");
        System.out.println(distinctCS.size() + " distinct states:");
        for (CubeState dcs : distinctCS) {
            System.out.println(dcs);
        }

        // Check that applying to each entry of csMap the inverse color trafo inv_ct yields the initial state cS
        // (before each color trafo) back.
        // (The equality check isEqual checks each element of fcol and sloc.)
        for (Map.Entry<Integer, ColorTrafo> entry : myMap.entrySet()) {
            Integer key = entry.getKey();
            CubeState ics = csFactory.makeCubeState(csMap.get(key));  // make a new copy, otherwise we would change csMap (!)
            ColorTrafo inv_ct = allCT.get(CubeStateMap.map_inv_wholeCube.get(key));
            ics.applyCT(inv_ct, true);
            assert (ics.isEqual(cS)) : "ics and cS differ for key:" + key;
        }
        System.out.println("All inverse color trafos lead back to initial state cS");

    }

    /**
     * Test for various {@link CubeState#apply(CubeState)}-transformations, which are applied in sequence to a default
     * cube, whether the {@link CubeState#sloc sloc} transformation works correctly. <p>
     * Since we call {@link CubeState#apply(CubeState) apply} with {@code doAssert=true}, the assertion
     * check with {@link CubeState#apply_sloc_slow(CubeState, boolean) apply_sloc_slow} is done in each case.
     */
    @Test
    public void test_sloc() {
        init();
        CubeState.Type trafoType = (CubeConfig.cubeSize== CubeConfig.CubeSize.POCKET)
                ? CubeState.Type.TRAFO_P : CubeState.Type.TRAFO_R;
        CubeState.Type colorType = (CubeConfig.cubeSize== CubeConfig.CubeSize.POCKET)
                ? CubeState.Type.COLOR_P : CubeState.Type.COLOR_R;
        init();
        CubeState cS = csFactory.makeCubeState();
        // 1) make whole-cube rotation 12 with a default cube state cS: The resulting sloc should be identical
        //    to 'inv_fcol of trafo'
        CubeState trafo = csFactory.makeCubeState(CubeStateMap.allWholeCubeRots.get(12));
        cS.apply(trafo,true);
        CubeState cS2 = csFactory.makeCubeState(cS);
        cS2.inv_fcol_trafo_to_sloc(trafo);
        if (!cS2.isEqual(cS)) {
            System.out.println("trafo: \n"+trafo);
            System.out.println("inv_fcol of trafo: \n"+cS2.print_sloc());
            System.out.println("sloc: \n"+cS.print_sloc());
        }
        assert (cS2.isEqual(cS)) : "trafo.f^(-1) and cS.sloc differ!";
        cS2 = new CubeState2x2(cS);
        // 2) make an LTw(1) twist on the current cube cS: Now sloc and 'inv_fcol of trafo' are different, because trafo
        //    does not operate on the default cube
        trafo = csFactory.makeCubeState(trafoType);
        trafo.LTw(1);
        CubeState cS3 = csFactory.makeCubeState(cS);
        cS3.inv_fcol_trafo_to_sloc(trafo);
        System.out.println("trafo LTw(1): \n"+trafo);
        System.out.println("inv_fcol of trafo LTw(1): \n"+cS3.print_sloc());
        cS2.apply(trafo,true);
        System.out.println("sloc after LTw(1) trafo: \n"+cS2.print_sloc());
        // 3) make an additional LTw(3) twist on the current cube cS: Now sloc should be identical to sloc after step 1)
        //    because .LTw(1).LTw(3) are the identity operation.
        trafo = csFactory.makeCubeState(trafoType);
        trafo.LTw(3);
        cS2.apply(trafo,true);
        System.out.println("sloc after LTw(3) trafo: \n"+cS2.print_sloc());
        assert(cS2.isEqual(cS)) : "cS2 (after .LTw(1).LTw(3) and cS differ";
        System.out.println("cs2 (after .LTw(1).LTw(3) and cS are the same");

        // 4) time measurement of apply_sloc and apply_sloc_slow
        int repeat=100000;
        CubeState cs1,cs2;
        long startTime = System.currentTimeMillis();
        DecimalFormat form = new DecimalFormat("0.00");
        cs1 = csFactory.makeCubeState(colorType);
        cs1.LTw(1).FTw(2).UTw(1).uTr();
        cs2 = csFactory.makeCubeState(cs1);
        for (int k=0; k<repeat; k++) {
            cs1.apply_sloc(trafo, false);
        }
        long elapsed1 = System.currentTimeMillis() - startTime;
        System.out.println("time apply_sloc: "+ elapsed1);
        startTime = System.currentTimeMillis();
        for (int k=0; k<repeat; k++) {
            cs2.apply_sloc_slow(trafo, false);
        }
        long elapsed2 = System.currentTimeMillis() - startTime;
        System.out.println("time apply_sloc_slow: "+ elapsed2
                +" (factor "+form.format(((double)elapsed2)/elapsed1)+" slower)");
    }

    /**
     * Test that some adjacency sets in representation CUBESTATE and STICKER are as expected.
     */
    @Test
    public void testAdjacencySets() {
        init();
        XNTupleFuncsCube xnf = new XNTupleFuncsCube();
        CubeConfig.boardVecType = BoardVecType.CUBESTATE;
        String h5 = xnf.adjacencySet(5).toString();
        assert (h5.equals("[3, 4, 6, 22]")) : "adjacency set 1 not as excpected!";
        System.out.println(h5 + " --> OK");        // should be [3, 4, 6, 22]
        CubeConfig.boardVecType = BoardVecType.STICKER;
        h5 = xnf.adjacencySet(5).toString();
        assert (h5.equals("[7, 8, 9, 10, 11, 13]")) : "adjacency set 2 not as excpected!";
        System.out.println(h5 + " --> OK");        // should be  (all from 7...13 except 5+7=12
    }

    /**
     *    Test for several randomly twisted CubeStates: Does a transformation from CubeState to STICKER board vector
     *    and back to CubeState result in the original CubeState again?
     */
    @Test
    public void testCS_STICKER_transform() {
        init();

        //         [This test is not really necessary anymore since with new class StateObsWithBoardVector
        //          we do not need any longer to re-construct a StateObserverCube from BoardVector.
        //	        But we keep it for the moment to have a check on the correctness of BoardVector.]
        CubeConfig.boardVecType = BoardVecType.STICKER;
        CubeConfig.pMax=15;
        int runs = 3;
        for (int r=0; r<runs; r++) {
            StateObserverCube so = (StateObserverCube) gb.chooseStartState();
            //StateObserverCube so = new StateObserverCube(); so.advance(new ACTIONS(3));  // a specific state (debug)
            int[] bvec = so.getCubeState().getBoardVector().bvec;
            int[][] board = new int[7][7];
            for (int rb=0,k=0; rb<7; rb++)
                for (int cb=0; cb<7; cb++,k++) {
                    board[rb][cb] = bvec[k];
                }
            StateObserverCube sb = new StateObserverCube(new BoardVector(bvec));
            assert(sb.getCubeState().equals(so.getCubeState())) : "so and sb differ after STICKER trafo!";
        }
        System.out.println("Transformation CubeState --> STICKER --> CubeState: OK");

    }

}
