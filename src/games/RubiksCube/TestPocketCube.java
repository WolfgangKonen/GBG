package games.RubiksCube;

import games.RubiksCube.CubeState.Type;
import games.RubiksCube.CubeState.Twist;
import games.RubiksCube.CubeStateMap.CsMapType;
import games.RubiksCube.ColorTrafoMap.ColMapType;
import java.io.IOException;
import java.text.DecimalFormat;
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

		tpock.calculate();

		if (args.length==0) {
			;
		} else {
			throw new RuntimeException("[TestPocketCube.main] args="+args+" not allowed.");
		}
	}
	
	private void calculate() {
		
		CubeState def = new CubeState();
		CubeState rot = new CubeState(def);
		for (int k=1; k<=4; k++) {
			//System.out.println(k+"x u-rotation");
			rot.uTr();  //.print();
		}		
		assert (def.isEqual(rot)) : "def and rot differ after 4x u-rotation!";
		
		//System.out.println(1+"x l-rotation");
		for (int k=1; k<=4; k++) {
			rot.lTr(); //.print();
		}
		assert (def.isEqual(rot)) : "def and rot differ after 4x l-rotation!";

		//System.out.println(1+"x F twist");
		for (int k=1; k<=4; k++) {
			rot.FTw(); //.print();
		}
		assert (def.isEqual(rot)) : "def and rot differ after 4x FTwist!";

		// this is just to check the correctness of CubeStateMap::countDifferentStates()
//		CubeStateMap hmTest = new CubeStateMap();
//		rot.FTw();
//		CubeState def2 = new CubeState(def);
//		hmTest.put(1, def);
//		hmTest.put(2, def2);  	// are two different CubeState objects with the same content really counted as one?
//		hmTest.put(3, rot);
//		System.out.println("num states = "+hmTest.countDifferentStates() + ", " + hmTest.size());  	// should be 2, 3
//		hmTest.put(4, new CubeState(rot));
//		System.out.println("num states = "+hmTest.countDifferentStates() + ", " + hmTest.size());	// should be 2, 4
		
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
		HashSet set = new HashSet();
		for (int act1=0; act1<3; act1++) {
			CubeState cS1 = new CubeState();
			switch(act1) {
			case 0: cS1.UTw(1); break;
			case 1: cS1.LTw(2); break;
			case 2: cS1.FTw(3); break;
			}
			for (int act2=0; act2<3; act2++) {
				CubeState cS = new CubeState(cS1);
				switch(act2) {
				case 0: cS = (cS.lastTwist==Twist.F) ? cS.UTw(3) : cS.FTw(3); break;
				case 1: cS = (cS.lastTwist==Twist.L) ? cS.FTw(2) : cS.LTw(2); break;
				case 2: cS = (cS.lastTwist==Twist.U) ? cS.LTw(1) : cS.UTw(1); break;
				}
				CubeStateMap mapColSymm = hmCols.applyColSymm(cS,hmRots);
				//mapColSymm.print();
				//System.out.println("num states = "+mapColSymm.countYgrHomeStates() + ", " + mapColSymm.size());
				System.out.print("(act1,act2) = ("+act1+","+act2+"):   ");
				System.out.println("diff states = "+mapColSymm.countDifferentStates() + ", " + mapColSymm.size());
				assert(mapColSymm.countYgrHomeStates()==mapColSymm.size()) : "not all color-symmetric states have ygr 'home'!";

				Iterator it1 = mapColSymm.entrySet().iterator(); 
			    while (it1.hasNext()) {
				    Map.Entry entry = (Map.Entry)it1.next();
				    set.add((CubeState)entry.getValue());	
		        } 
			}
		}
		System.out.println("Covered states of distance 1: "+set.size());
		
//		CubeState uS = new CubeState(Type.TRAFO_P);
//		uS.LTw().print();
	}
	

}
