package games.RubiksCube;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class CubeStateMap extends Hashtable<Integer,CubeState> {
	public enum CsMapType {AllWholeCubeRotTrafos}
	
	public CubeStateMap() {
		super();
	}
	
	public CubeStateMap(CsMapType csType) {
		switch(csType) {
		case AllWholeCubeRotTrafos:
			allWholeCubeRotTrafos();
			break;
		}
	}
	
	private void allWholeCubeRotTrafos() {
		CubeStateFactory csFactory = new CubeStateFactory();
		int[] loc = new int[3];
		CubieTriple ygrCubie = new CubieTriple();
		for (int i=0; i<6; i++) {
			CubeState rot = csFactory.makeCubeState(CubeState.Type.TRAFO_P);
			switch(i) {
			case 0: 
				break;
			case 1: 
				rot.lTr(1);
				break;
			case 2:
				rot.lTr(3);
				break;
			case 3: 
				rot.fTr(1);
				break;
			case 4: 
				rot.fTr(2);
				break;
			case 5: 
				rot.fTr(3);
				break;
			}
			for (int j=0; j<4; j++) {
				//rot.print();
				
				// each whole-cube rotation is uniquely defined by the location of the 
				// y-face of the ygr-cubie:
				Integer key = rot.fcol[ygrCubie.loc[0]];
				CubeState tS = csFactory.makeCubeState(rot);	// IMPORTANT: We have to make a copy of rot, so that
							// each this.put(key,tS) below really stores a different value!! (Otherwise 
							// just a reference to the (mutable) object rot is stored in the HashMap.)
				this.put(key, tS);	// put (key,value) = (Integer, CubeState) into HashMap
				rot.uTr(1); // prepare for next for-loop pass
			}
		}
	}
	
	public void print() {
		DecimalFormat form = new DecimalFormat("00");
	    Iterator it = this.entrySet().iterator(); 
	    while (it.hasNext()) {
		    Map.Entry entry = (Map.Entry)it.next();
		    // just debug code in order to see whether .get and .equals on key works:
//		    Integer ct = (Integer)entry.getKey();
//		    Integer ct2 = new Integer(ct);
//		    boolean check = ct.equals(ct2);
//		    CubeState cs = (CubeState)entry.getValue();
//		    CubeState cs1 = (CubeState)this.get(ct);
//		    CubeState cs2 = (CubeState)this.get(ct2);
//		    boolean check1 = this.containsKey(ct);
//		    boolean check2 = this.containsKey(ct2);
		    System.out.println(
		            form.format((Integer)entry.getKey()) + " --> " +
                    ((CubeState)entry.getValue()).toString()
            );
         } 		    
	}
	
	public int countDifferentStates() {
	    Iterator it1 = this.entrySet().iterator(); 
	    HashSet set = new HashSet();
	    while (it1.hasNext()) {
		    Map.Entry entry = (Map.Entry)it1.next();
		    set.add((CubeState)entry.getValue());	
		    		// if set.add(value) is called for a CubeState value which is already
		    		// present in the set (i.e. value.equals(other) is true for at least  
		    		// one CubeState other in set), then it will not be added to the set. Thus 
		    		// set.size() will return the # of truly different values in HashMap this.
        } 		    
		return set.size();
	}
	
	public static int countDifferentStates(ArrayList D) {
	    Iterator it1 = D.iterator(); 
	    HashSet set = new HashSet();
	    while (it1.hasNext()) {
		    set.add((CubeState)it1.next());	
		    		// if set.add(value) is called for a CubeState value which is already
		    		// present in the set (i.e. value.equals(other) is true for at least  
		    		// one CubeState other in set), then it will not be added to the set. Thus 
		    		// set.size() will return the # of truly different values in HashMap this.
        } 		    
		return set.size();
	}
	
	public int countYgrHomeStates() {
		int counter = 0;
	    Iterator it1 = this.entrySet().iterator(); 
	    while (it1.hasNext()) {
		    Map.Entry entry = (Map.Entry)it1.next();
		    CubeState cS = (CubeState)entry.getValue();
		    if (cS.fcol[12]==3 && cS.fcol[16]==4 && cS.fcol[20]==5) counter++;
        } 		    
		return counter;
		
	}
	
} // class CubeStateMap

