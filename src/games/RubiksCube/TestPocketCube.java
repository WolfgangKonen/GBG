package games.RubiksCube;

import games.RubiksCube.CubeState.Type;
import games.RubiksCube.CubeState.Twist;
import games.RubiksCube.CubeStateMap.CsMapType;
import games.RubiksCube.CSArrayList.TupleInt;
import games.RubiksCube.CSArrayList.CSAListType;
import games.RubiksCube.ColorTrafoMap.ColMapType;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple main program to calculate the number of states in the game of NIM.
 *  
 * @author Wolfgang Konen, TH Köln , Jan'18
 */
public class TestPocketCube {

	public int nHeap = 10;
	public int nPiece = 5;
	public int gCount1 = 0, gCount2=0;
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		TestPocketCube tpock = new TestPocketCube();
		
		XNTupleFuncsCube xnf = new XNTupleFuncsCube();
		xnf.adjacencySet(5);

		tpock.simpleTests01();
		tpock.simpleTests02();
		tpock.generatorTests();

		if (args.length==0) {
			;
		} else {
			throw new RuntimeException("[TestPocketCube.main] args="+args+" not allowed.");
		}
	}
	
	private void simpleTests01() {
		
		CubeState def = new CubeState();
		CubeState rot = new CubeState(def);
		for (int k=1; k<=4; k++) {
			//System.out.println(k+"x u-rotation");
			rot.uTr(1);  //.print();
		}		
		assert (def.isEqual(rot)) : "def and rot differ after 4x u-rotation!";
		
		//System.out.println(1+"x l-rotation");
		for (int k=1; k<=4; k++) {
			rot.lTr(1); //.print();
		}
		assert (def.isEqual(rot)) : "def and rot differ after 4x l-rotation!";

		for (int k=1; k<=4; k++) {
			//System.out.println(k+"x f-rotation");
			rot.fTr(1);  //.print();
		}		
		assert (def.isEqual(rot)) : "def and rot differ after 4x f-rotation!";
		
		//System.out.println(1+"x U twist");
		for (int k=1; k<=4; k++) {
			rot.UTw(1); //.print();
		}
		assert (def.isEqual(rot)) : "def and rot differ after 4x UTwist!";

		//System.out.println(1+"x L twist");
		for (int k=1; k<=4; k++) {
			rot.LTw(1); //.print();
		}
		assert (def.isEqual(rot)) : "def and rot differ after 4x LTwist!";

		//System.out.println(1+"x F twist");
		for (int k=1; k<=4; k++) {
			rot.FTw(1); //.print();
		}
		assert (def.isEqual(rot)) : "def and rot differ after 4x FTwist!";

		// this is just to check the correctness of CubeStateMap::countDifferentStates()
//		CubeStateMap hmTest = new CubeStateMap();
//		rot.FTw();
//		CubeState def2 = new CubeState(def);
//		hmTest.put(1, def);
//		hmTest.put(2, def2);  	// Are two different CubeState objects with the same content correctly counted as one?
//		hmTest.put(3, rot);
//		System.out.println("num states = "+hmTest.countDifferentStates() + ", " + hmTest.size());  	// should be 2, 3
//		hmTest.put(4, new CubeState(rot));
//		System.out.println("num states = "+hmTest.countDifferentStates() + ", " + hmTest.size());	// should be 2, 4
	}
	
	private void simpleTests02() {
		
		System.out.println("\nTesting CubeStateMap::allWholeCubeRotTrafos()");
		// Create the map with all whole-cube rotation transformations. 
		// This should give 24 *distinct* transformations.
		CubeStateMap hmRots = new CubeStateMap(CsMapType.AllWholeCubeRotTrafos);
		//hmRots.print();
		System.out.println("num states = "+hmRots.countDifferentStates() + ", " + hmRots.size());
		assert (hmRots.countDifferentStates()==hmRots.size()) : "there are duplicate elements in hmRots!";
		
		System.out.println("\nTesting ColorMap::allColorTrafos()");
		// Create the map with all color transformations. 
		// This should give 24 *distinct* transformations.
		ColorTrafoMap hmCols = new ColorTrafoMap(ColMapType.AllColorTrafos);
		//hmCols.print();
		System.out.println("num states = "+hmCols.countDifferentStates() + ", " + hmCols.size());
		assert (hmCols.countDifferentStates()==hmCols.size()) : "there are duplicate elements in hmRots!";
		
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
		HashSet set = new HashSet();
		int[] totalCoverage = {1, 9, 54, 321}; // see https://en.wikipedia.org/wiki/Pocket_Cube 
		for (int act1=0; act1<3; act1++) {
			CubeState cS1 = new CubeState();
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
					CubeState cS2 = new CubeState(cS1);
					switch(act2) {
					// the ternary operators ensure that the twists done in the second step
					// (act2) are different twists than the act1 actions (otherwise both actions
					// could be concatenated to one action and would lead to a distance-1 state)
					case 0: cS2 = (cS2.lastTwist==Twist.F) ? cS2.UTw(3) : cS2.FTw(3); break;
					case 1: cS2 = (cS2.lastTwist==Twist.L) ? cS2.FTw(2) : cS2.LTw(2); break;
					case 2: cS2 = (cS2.lastTwist==Twist.U) ? cS2.LTw(1) : cS2.UTw(1); break;
					}
					if (DISTANCE==2) {
						int act3=-1;
						innerApplyColSymmTest(cS2, act1,act2,act3, hmCols, hmRots, set);						
					} else if (DISTANCE==3) {
						for (int act3=0; act3<3; act3++) {
							CubeState cS3 = new CubeState(cS2);
							switch(act3) {
							// the ternary operators ensure that the twists done in the second step
							// (act2) are different twists than the act1 actions (otherwise both actions
							// could be concatenated to one action and would lead to a distance-1 state)
							case 0: cS3 = (cS3.lastTwist==Twist.F) ? cS3.UTw(1) : cS3.FTw(1); break;
							case 1: cS3 = (cS3.lastTwist==Twist.L) ? cS3.FTw(2) : cS3.LTw(2); break;
							case 2: cS3 = (cS3.lastTwist==Twist.U) ? cS3.LTw(3) : cS3.UTw(3); break;
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
			ColorTrafoMap hmCols, CubeStateMap hmRots, HashSet set) {
		CubeStateMap mapColSymm = hmCols.applyColSymm(cS,hmRots);
		//mapColSymm.print();
		//System.out.println("num states = "+mapColSymm.countYgrHomeStates() + ", " + mapColSymm.size());
		System.out.print("(act1,act2,act3) = ("+act1+","+act2+","+act3+"):   ");
		System.out.println("diff states = "+mapColSymm.countDifferentStates() + ", " + mapColSymm.size());
		assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) : "not all color-symmetric states have ygr 'home'!";

		Iterator it1 = mapColSymm.entrySet().iterator(); 
	    while (it1.hasNext()) {
		    Map.Entry entry = (Map.Entry)it1.next();
		    set.add((CubeState)entry.getValue());	
        } 	
	}
	
	private void generatorTests() {
		
		System.out.println("\nTesting CSArrayList [GenerateNext]");
		//            0          4                   8
		int[] Narr = {0,0,9,54, 321,1847,9992,50136, 50,50,50,50};	// for GenerateNext
//		int[] Narr = {0,0,9,50, 150,600,3000,15000,  50,50,50,50};  // for GenerateNextColSymm
		int[] theoCov = {1,9,54,321,  	// the known maximum sizes for D[0],D[1],D[2],D[3] ...
				1847,9992,50136,227536	// ... and D[4],D[5],D[6],D[7],
		};
		boolean silent=false;
		boolean doAssert=true;
		ArrayList<TupleInt>[] tintList = new ArrayList[12];
		CSArrayList[] D = new CSArrayList[12];
		D[0] = new CSArrayList(CSAListType.GenerateD0);
		D[1] = new CSArrayList(CSAListType.GenerateD1);
		D[1].assertTwistSeqInArrayList();
		for (int p=2; p<6; p++) {
			if (p>3) silent=true;
			if (p>5) doAssert=false;
			tintList[p] = new ArrayList();
			System.out.print("Generating distance set for p="+p+" ..");
			long startTime = System.currentTimeMillis();
//			D[p] = new CSArrayList(CSAListType.GenerateNextColSymm, D[p-1], D[p-2], Narr[p], tintList[p], silent);
			D[p] = new CSArrayList(CSAListType.GenerateNext, D[p-1], D[p-2], Narr[p]
					, tintList[p], silent, doAssert);
			double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
			assert(CubeStateMap.countDifferentStates(D[p])==D[p].size()) : "D["+p+"]: size and # diff. states differ!";
			D[p].assertTwistSeqInArrayList();
			System.out.println("\nCoverage D["+p+"] = "+D[p].size()+" of "+ theoCov[p]
					+"    Time="+elapsedTime+" sec");
			//CSArrayList.printTupleIntList(tintList[p]);
			CSArrayList.printLastTupleInt(tintList[p]);
			int dummy=1;
		}

	}

}
