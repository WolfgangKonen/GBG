package games.RubiksCube;


import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import games.RubiksCube.CubeState.Twist;
import games.RubiksCube.CubeState.Type;

/**
 * A map of {@link ColorTrafo} objects. The key is just an integer counter to 
 * distinguish every ColorTrafo when marching through them (e.g. in allColorTrafos()).
 * 
 * @see ColorTrafo
 */
public class ColorTrafoMap extends Hashtable<Integer,ColorTrafo> {
	public enum ColMapType {AllColorTrafos};
	public ColorTrafoMap() {
		super();
	}
	public ColorTrafoMap(ColMapType csType) {
		switch(csType) {
		case AllColorTrafos:
			allColorTrafos();
			break;
		}
	}
	
	private void allColorTrafos() {
		int count=0;
		int[] loc = new int[3];
		for (int i=0; i<6; i++) {
			ColorTrafo rot = new ColorTrafo();
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
				Integer iCnt = new Integer(count);
				ColorTrafo tS = new ColorTrafo(rot);	// IMPORTANT: We have to make a copy of rot, 
					// so that each this.put(iCnt,tS) below really stores a different value!! (Otherwise 
					// just a reference to the (mutable) object rot is stored in the Hashtable.)
				tS.setT();
				this.put(iCnt, tS);	// put (key,value) = (Integer, ColorTrafo) into HashMap
				rot.uTr(1);
				count++;
			}
		}
	}
	
	public void print() {
		DecimalFormat form = new DecimalFormat("00");
	    Iterator it = this.entrySet().iterator(); 
	    while (it.hasNext()) {
		    Map.Entry entry = (Map.Entry)it.next();
		    System.out.println(
		            form.format((Integer)entry.getKey()) + " --> " +
                    ((ColorTrafo)entry.getValue()).toString()
            );
         } 		    
	}
	
	public int countDifferentStates() {
	    Iterator it1 = this.entrySet().iterator(); 
	    HashSet set = new HashSet();
	    while (it1.hasNext()) {
		    Map.Entry entry = (Map.Entry)it1.next();
		    set.add((ColorTrafo)entry.getValue());	
		    		// if set.add(value) is called for a ColorTrafo value which is already
		    		// present in the set (i.e. value.equals(other) is true for at least  
		    		// one ColorTrafo other in set), then it will not be added to the set. Thus 
		    		// set.size() will return the truly different values in HashMap this.
        } 		    
		return set.size();
	}

	/**
	 * Given a cube state in cS, apply all color transformation of {@code this} to it 
	 * 
	 * @param cS		a cube state of type POCKET or RUBIKS	
	 * @param hmRots	helper, all whole-cube rotations, to bring ygr-cubie 'home'
	 * @return	a map with all states which are color-symmetric equivalent to cS
	 */
	public CubeStateMap applyColSymm(CubeState cS, CubeStateMap hmRots) {
		assert(cS.type==Type.POCKET || cS.type==Type.RUBIKS) : "Wrong cS.type in applyColSymm(cS) !";
		int[] loc = new int[3];
		CubieTriple cub = new CubieTriple();
		CubieTriple ygr = new CubieTriple();
		CubeStateMap newMap = new CubeStateMap();
		for (int i=0; i<3; i++) cub.loc[i]=0;
		
	    Iterator it = this.entrySet().iterator(); 
	    while (it.hasNext()) {
		    Map.Entry entry = (Map.Entry)it.next();	    	
	    	Integer key = (Integer)entry.getKey();
	    	ColorTrafo cT = (ColorTrafo)entry.getValue();
	    	CubeState dS = new CubeState(cS);
	    	dS.applyCT(cT);							// apply color trafo to dS
	    	//
	    	// it is (currently) difficult to give the right values for 
	    	// dS.lastTwist and dS.twistSequence, so we set them to empty values ('not known'):
	    	dS.lastTwist = Twist.ID;
	    	dS.twistSeq = "";
	    	CubieTriple where = dS.locate(ygr);		// find new location of ygr-cubie
	    	CubeState trafo = (CubeState)hmRots.get(where.loc[0]);
	    	if (trafo==null) throw new RuntimeException("Key 'where' not found!");
	    	dS.apply(trafo);		// apply the right whole-cube rotation to bring ygr-cubie 'home'
	    	newMap.put(key, dS);	// add dS to newMap	
	    	//dS.print();
	    }
		return newMap;
	}
	

} // class ColorTrafoMap