package games.RubiksCube;

import java.util.*;

public class OlderCubeTest extends PocketCubeTest {

    /**
     * (Older) CubeStateMap- and ColorTrafoMap-tests and color symmetry tests
     */
    private void colorMapTests() {

        // this is just to check the correctness of CubeStateMap::countDifferentStates()
//		CubeStateMap hmTest = new CubeStateMap();
//		rot.FTw();
//		CubeState def2 = CubeState.makeCubeState(def);
//		hmTest.put(1, def);
//		hmTest.put(2, def2);  	// Are two different CubeState objects with the same content correctly counted as one?
//		hmTest.put(3, rot);
//		System.out.println("num states = "+hmTest.countDifferentStates() + ", " + hmTest.size());  	// should be 2, 3
//		hmTest.put(4, CubeState.makeCubeState(rot));
//		System.out.println("num states = "+hmTest.countDifferentStates() + ", " + hmTest.size());	// should be 2, 4

        System.out.println("\nTesting CubeStateMap::allWholeCubeRotTrafos()");
        // Create the map with all whole-cube rotation transformations.
        // This should give 24 *distinct* transformations.
        CubeStateMap hmRots = new CubeStateMap(CubeStateMap.CsMapType.AllWholeCubeRotTrafos);
        //hmRots.print();
        assert (hmRots.countDifferentStates()==hmRots.size()) : "there are duplicate elements in hmRots!";
        System.out.println("num states = "+hmRots.countDifferentStates() + ", " + hmRots.size() + " --> OK");

        System.out.println("\nTesting ColorMap::allColorTrafos()");
        // Create the map with all color transformations.
        // This should give 24 *distinct* transformations.
        ColorTrafoMap hmCols = new ColorTrafoMap(ColorTrafoMap.ColMapType.AllColorTrafos);
        //hmCols.print();
        assert (hmCols.countDifferentStates()==hmCols.size()) : "there are duplicate elements in hmRots!";
        System.out.println("num states = "+hmCols.countDifferentStates() + ", " + hmCols.size() + " --> OK");

        System.out.println("\nTesting ColorMap::applyColSymm(cS,hmRots)");
        // Given the maps hmRots and hmCols created in the previous steps, take a solved cube and
        // do, if DISTANCE==1, 3^1 different one-step twists,
        // or, if DISTANCE==2, 3^2 different two-step twists,
        // or, if DISTANCE==3, 3^3 different three-step twists.
        // For each resulting cube state cS, do the following:
        // 1) Apply all color symmetries to cS. Check the resulting CubeStateMap: Have all
        //	  resulting symmetry-states their ygr-cubie in the 'home'-position?
        // 2) Add all symmetry-states to HashSet set. A HashSet will only add an element
        //    if it is different to all other elements in the set, so we get via set.size()
        //    in the end the covered states of distance 1, 2 or 3. Ideally, the covered states
        // 	  should be 9, 54 or 321 for distance 1, 2 or 3, resp. Whether this coverage is
        //    achieved depends on the diversity of the actions undertaken.
        for (int DISTANCE = 1; DISTANCE<3; DISTANCE++) {
            HashSet<CubeState> set = new HashSet<>();
            int[] totalCoverage = {1, 9, 54, 321}; // see https://en.wikipedia.org/wiki/Pocket_Cube
            for (int act1=0; act1<3; act1++) {
                CubeState cS1 = csFactory.makeCubeState();
                switch(act1) {
                    case 0: cS1.UTw(1); break;
                    case 1: cS1.LTw(2); break;
                    case 2: cS1.FTw(3); break;
                }
                if (DISTANCE==1) {
                    int act2=-1;
                    int act3=-1;
                    innerApplyColSymmTest(cS1, act1,act2,act3, hmCols, hmRots, set);
                } else if (DISTANCE>=2) {
                    for (int act2=0; act2<3; act2++) {
                        CubeState cS2 = csFactory.makeCubeState(cS1);
                        switch(act2) {
                            // the ternary operators ensure that the twists done in the second step
                            // (act2) are different twists than the act1 actions (otherwise both actions
                            // could be concatenated to one action and would lead to a distance-1 state)
                            case 0: cS2 = (cS2.lastTwist== CubeState.Twist.F) ? cS2.UTw(3) : cS2.FTw(3); break;
                            case 1: cS2 = (cS2.lastTwist== CubeState.Twist.L) ? cS2.FTw(2) : cS2.LTw(2); break;
                            case 2: cS2 = (cS2.lastTwist== CubeState.Twist.U) ? cS2.LTw(1) : cS2.UTw(1); break;
                        }
                        if (DISTANCE==2) {
                            int act3=-1;
                            innerApplyColSymmTest(cS2, act1,act2,act3, hmCols, hmRots, set);
                        } else if (DISTANCE==3) {
                            for (int act3=0; act3<3; act3++) {
                                CubeState cS3 = csFactory.makeCubeState(cS2);
                                switch(act3) {
                                    // the ternary operators ensure that the twists done in the second step
                                    // (act2) are different twists than the act1 actions (otherwise both actions
                                    // could be concatenated to one action and would lead to a distance-1 state)
                                    case 0: cS3 = (cS3.lastTwist== CubeState.Twist.F) ? cS3.UTw(1) : cS3.FTw(1); break;
                                    case 1: cS3 = (cS3.lastTwist== CubeState.Twist.L) ? cS3.FTw(2) : cS3.LTw(2); break;
                                    case 2: cS3 = (cS3.lastTwist== CubeState.Twist.U) ? cS3.LTw(3) : cS3.UTw(3); break;
                                }
                                innerApplyColSymmTest(cS3, act1,act2,act3, hmCols, hmRots, set);
                            }
                        }
                    } // for (act2)
                } // if (DISTANCE>=2)
            }
            System.out.println("Covered states of distance "+DISTANCE+":   "
                    +set.size()+" from "+ totalCoverage[DISTANCE]+ " in total");
        }
    }

    private void innerApplyColSymmTest(CubeState cS, int act1,int act2,int act3,
                                       ColorTrafoMap hmCols, CubeStateMap hmRots, HashSet<CubeState> set) {
        CubeStateMap mapColSymm = hmCols.applyColSymm(cS,hmRots);
        //mapColSymm.print();
        //System.out.println("num states = "+mapColSymm.countYgrHomeStates() + ", " + mapColSymm.size());
        System.out.print("(act1,act2,act3) = ("+act1+","+act2+","+act3+"):   ");
        System.out.println("diff states = "+mapColSymm.countDifferentStates() + ", " + mapColSymm.size());
        assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) : "not all color-symmetric states have ygr 'home'!";

        Iterator<Map.Entry<Integer,CubeState>> it1 = mapColSymm.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<Integer,CubeState> entry = it1.next();
            set.add(entry.getValue());
        }
    }

    /**
     * Generate the distance sets D[p] and calculate the number of prevs, currents and twins
     * while generating them. If all works out correctly, the relation
     * <pre>
     *       D[p+1].size() = 6*D[p].size() - N_p - N_c - N_t     </pre>
     * should hold.
     * <p>
     * <b>WARNING</b>: This tests take <b>VERY</b> long for pmax &ge; 6 !<br>
     * Since distance sets are now deprecated, this test method is deprecated as well.
     */
    @Deprecated
    private void generatorTests(int pmax) {

        System.out.println("\nTesting CSArrayList [GenerateNext]");
        //            0          4                   8
        int[] Narr = {0,0,9,54, 321,1847,9992,50136, 227536,870072,1887748,623800};	// for GenerateNext
//		int[] Narr = {0,0,9,50, 150,600,3000,15000,  50,50,50,50};  // for GenerateNextColSymm
        int[] theoCov = {1,9,54,321,  	// the known maximum sizes for D[0],D[1],D[2],D[3] ...
                1847,9992,50136,227536,	// ... and D[4],D[5],D[6],D[7],
                870072,1887748,623800,	// ... and D[8],D[9],D[10],D[7],
                2644					// ... and D[11]
        };
        boolean silent=false;
        boolean doAssert=true;
        long seed = 99;
        Random rand = new Random(seed);
        ArrayList<CSArrayList.TupleInt>[] tintList = new ArrayList[12];
        CSArrayList[] D = new CSArrayList[12];
        D[0] = new CSArrayList(CSArrayList.CSAListType.GenerateD0);
        D[1] = new CSArrayList(CSArrayList.CSAListType.GenerateD1);
        D[1].assertTwistSeqInArrayList();
        for (int p=2; p<=pmax; p++) {
            if (p>3) silent=true;
            if (p>5) doAssert=false;
            tintList[p] = new ArrayList<>();
            System.out.print("Generating distance set for p="+p+" ..");
            long startTime = System.currentTimeMillis();
//			D[p] = new CSArrayList(CSAListType.GenerateNextColSymm, D[p-1], D[p-2], Narr[p], tintList[p], silent);
            D[p] = new CSArrayList(CSArrayList.CSAListType.GenerateNext, D[p-1], D[p-2], Narr[p]
                    , tintList[p], silent, doAssert, rand);
            double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
            assert(CubeStateMap.countDifferentStates(D[p])==D[p].size()) : "D["+p+"]: size and # diff. states differ!";
            D[p].assertTwistSeqInArrayList();
            System.out.println("\nCoverage D["+p+"] = "+D[p].size()+" of "+ theoCov[p]
                    +"    Time="+elapsedTime+" sec");
            //CSArrayList.printTupleIntList(tintList[p]);
            CSArrayList.printLastTupleInt(tintList[p]);
        }

    }

}
