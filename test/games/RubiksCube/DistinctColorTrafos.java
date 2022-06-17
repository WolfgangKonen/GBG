package games.RubiksCube;

import org.junit.Test;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

/**
 * Test how many distinct states appear on average, if we take a t-twisted cube and apply all 24 color trafos
 * to it. The minimum is 1, the maximum is 24.
 */
public class DistinctColorTrafos {
    int TWISTMAX = 15;
    int RUNS=100;
    ArenaCube ar;
    GameBoardCube gb;		// needed for chooseStartState()

    protected Random rand = new Random(System.currentTimeMillis());
    protected final CubeStateFactory csFactory = new CubeStateFactory();

    public static Hashtable<Integer,Double> meanDistinct = new Hashtable<>();

    @Test
    public void countDistinct_Pocket() {
        initPocket();
        System.out.println("\nNumber of distinct color-trafo states for t-twisted 2x2x2 cube states:");
        countDistinct();
    }
    @Test
    public void countDistinct_Rubiks() {
        initRubiks();
        System.out.println("\nNumber of distinct color-trafo states for t-twisted 3x3x3 cube states:");
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
        for (Map.Entry<Integer, Double> entry : meanDistinct.entrySet()) {
            System.out.println("t="+entry.getKey()+": "+form.format(entry.getValue()));
        }

    }

    protected void initPocket() {
        CubeConfig.cubeSize = CubeConfig.CubeSize.POCKET;

        ar = new ArenaCube("",false,true);
        gb = new GameBoardCube(ar);

        // this is already part of ArenaCube-constructor:
//      CubeStateFactory.generateInverseTs();
//      CubeState.generateForwardTs();
    }

    protected void initRubiks() {
        CubeConfig.cubeSize = CubeConfig.CubeSize.RUBIKS;

        ar = new ArenaCube("",false,true);
        gb = new GameBoardCube(ar);

        // this is already part of ArenaCube-constructor:
//      CubeStateFactory.generateInverseTs();
//      CubeState.generateForwardTs();
    }
}
