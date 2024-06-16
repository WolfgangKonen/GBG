package games.RubiksCube;

import games.Arena;
import org.junit.Test;
import starters.SetupGBG;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

/**
 * Several test methods related to Rubik's Cube color transformations
 */
public class DistinctColorTrafos {
    int TWISTMAX = 20;
    int RUNS=500;
    ArenaCube ar;
    GameBoardCube gb;		// needed for chooseStartState()
    String selectedGame = "RubiksCube";

    protected Random rand = new Random(System.currentTimeMillis());
    protected final CubeStateFactory csFactory = new CubeStateFactory();

    public static Hashtable<Integer,Double> meanDistinct = new Hashtable<>();

    /**
     * Test how many distinct states appear on average, if we take a t-twisted cube and apply all 24 color trafos
     * to it. The minimum is 1, the maximum is 24.
     */
    @Test
    public void countDistinct_Pocket() {
        //scaPar = SetupGBG.setDefaultScaPars(selectedGame);    // "2x2x2", "STICKER2", "HTM"
        String[] scaPar = new String[] {"2x2x2", "STICKER2", "HTM"};
        initPocket(scaPar);
        System.out.println("\nNumber of distinct color-trafo states for t-twisted 2x2x2 cube states:");
        System.out.println("scaPar:" +scaPar[0] + " " + scaPar[1] + " " + scaPar[2] + ";  average over NRUNS=" + RUNS);
        countDistinct();
    }
    @Test
    public void countDistinct_Rubiks() {
        String[] scaPar = new String[] {"3x3x3", "STICKER2", "QTM"};
        initRubiks(scaPar);
        System.out.println("\nMean number of distinct color-trafo states for t-twisted 3x3x3 cube states:");
        System.out.println("scaPar:" +scaPar[0] + " " + scaPar[1] + " " + scaPar[2] + ";  average over NRUNS=" + RUNS);
        countDistinct();
    }
    public void countDistinct() {
        ColorTrafoMap allCT = new ColorTrafoMap(ColorTrafoMap.ColMapType.AllColorTrafos);

        meanDistinct.clear();
        meanDistinct.put(0,1.0);          // the default cube has only one distinct state
        for (int t=1; t<TWISTMAX; t++) {
            double meanVal = 0.0;
            for (int r=0; r<RUNS; r++) {
                CubeState cs = ((StateObserverCube) gb.chooseStartState(t)).getCubeState();
                CubeStateMap csMap = cs.applyCT(allCT,true);
                HashSet<CubeState> distinctCS = new HashSet<>();
                for (Map.Entry<Integer, CubeState> entry : csMap.entrySet()) {
                    CubeState ecs = csFactory.makeCubeState(entry.getValue());
                    distinctCS.add(ecs);
                }
                meanVal += distinctCS.size();
            }
            meanVal /= RUNS;
            meanDistinct.put(t,meanVal);
        }
        DecimalFormat form = new DecimalFormat("00.000");
        System.out.println("TWISTS; NSYM_TRUE_DIFF");
        for (Map.Entry<Integer, Double> entry : meanDistinct.entrySet()) {
            System.out.println(entry.getKey()+"; "+form.format(entry.getValue()));
        }

    }

    /**
     * Generate an example color transformation for printing the right
     */
    @Test
    public void examplePocket_CT() {
        String[] scaPar = new String[] {"2x2x2", "STICKER2", "HTM"};
        ColorTrafoMap allCT = new ColorTrafoMap(ColorTrafoMap.ColMapType.AllColorTrafos);
        initPocket(scaPar);
        System.out.println("\nExample 2x2x2 cube state after U^1:");
        CubeState cs = ((StateObserverCube) gb.getDefaultStartState(null)).getCubeState();
        cs.UTw();
        System.out.println(cs);
        System.out.println(cs.print_inv_sloc());
        CubeStateMap csMap = cs.applyCT(allCT,true);
        CubeState csct = csMap.get(4);
        System.out.println(csct);
        System.out.println(csct.print_inv_sloc());
    }

    protected void initPocket(String[] scaPar) {
        CubeConfig.cubeSize = CubeConfig.CubeSize.POCKET;

        Arena arena = SetupGBG.setupSelectedGame(selectedGame, scaPar,"",false,true);
        assert (arena instanceof ArenaCube);
        ar = (ArenaCube) arena;
        gb = new GameBoardCube(ar);

        // this is already part of ArenaCube-constructor:
//      CubeStateFactory.generateInverseTs();
//      CubeState.generateForwardTs();
    }

    protected void initRubiks(String[] scaPar) {
        CubeConfig.cubeSize = CubeConfig.CubeSize.RUBIKS;

        String selectedGame = "RubiksCube";
        Arena arena = SetupGBG.setupSelectedGame(selectedGame, scaPar,"",false,true);
        assert (arena instanceof ArenaCube);
        ar = (ArenaCube) arena;
        gb = new GameBoardCube(ar);

        // this is already part of ArenaCube-constructor:
//      CubeStateFactory.generateInverseTs();
//      CubeState.generateForwardTs();
    }
}
