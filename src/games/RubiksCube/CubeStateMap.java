package games.RubiksCube;

import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * A map of {@link CubeState} objects, usually of type TRAFO_P and usually denoting whole-cube rotations.
 * See {@link #CubeStateMap(CsMapType)} for the numbering convention of the {@link Integer} key.
 * <p>
 * But a {@code CubeStateMap} can contain as well arbitrary {@link CubeState} objects with arbitrary keys. Use
 * {@link #CubeStateMap()} in that case.
 */
public class CubeStateMap extends Hashtable<Integer,CubeState> {
	/**
	 * (currently only {@code AllWholeCubeRotTrafos})
	 */
	public enum CsMapType {AllWholeCubeRotTrafos}

	/**
	 * holds the inverse keys of whole-cube rotations (see Table 5 in notes-WK-RubiksCube.docx).<br>
	 * Example: whole-cube rot with key 5 has the inverse whole-cube rot with key {@code invk[5]=19}.<br>
	 * See {@link #CubeStateMap(CsMapType)} for whole-cube-rot numbering.<p>
	 * The inverse keys are stored in {@link #map_inv_wholeCube} for public reference.
	 */
	protected static int[] invk = new int[] { 0, 3, 2, 1,  12,19, 6,21,    8, 9,10,11,    4,23,14,17,   20,15,18, 5,   16, 7,22,13};

	/**
	 * A map of two Integers {@code <ygr,key>}: <br>
	 * {@code key = map_ygr_wholeKey[ygr]} returns for {@code ygr} being the y-location of the ygr-cubie
	 * the {@code key} of that trafo in {@link #allWholeCubeRots} that brings the ygr-cubie 'home'.
	 */
	public static Hashtable<Integer,Integer> map_ygr_wholeKey = new Hashtable<>();
	// this field has to be defined *before* CubeStateMap allWholeCubeRots, otherwise NullPointerException (!)

	/**
	 * A map of two Integers {@code <key,invk>}: <br>
	 * {@code invk = map_inv_wholeCube.get(key)} returns for the whole-cube rotation with key {@code key}
	 * the key {@code invk} from {@link #allWholeCubeRots} of the inverse whole-cube rotation.
	 */
	public static Hashtable<Integer,Integer> map_inv_wholeCube = new Hashtable<>();

	/**
	 * A map of 24 {@link CubeState} objects of type TRAFO_P or TRAFO_R that contains all valid whole-cube rotations
	 * of the cube. <br>
	 * See {@link CubeStateMap#CubeStateMap(CsMapType)} for trafo numbering.
	 */
	public static CubeStateMap allWholeCubeRots = new CubeStateMap(CsMapType.AllWholeCubeRotTrafos);

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .agt.zip will become unreadable, or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long  serialVersionUID = 12L;

	public CubeStateMap() {
		super();
	}

	/**
	 * Generate the map {@code this} (derived from {@link Hashtable}) of all whole-cube rotations. These are 24
	 * transformations, and they are numbered in the following order:
	 * <ul>
	 *     <li> 00-03, "w up", generated via id*{u0,u1,u2,u3}
	 *     <li> 04-07, "g up", generated via f1*{u0,u1,u2,u3}
	 *     <li> 08-11, "y up", generated via f2*{u0,u1,u2,u3}
	 *     <li> 12-15, "b up", generated via f3*{u0,u1,u2,u3}
	 *     <li> 16-19, "o up", generated via l1*{u0,u1,u2,u3}
	 *     <li> 20-23, "r up", generated via l3*{u0,u1,u2,u3}
	 * </ul>
	 * @param csType (currently only {@link CsMapType#AllWholeCubeRotTrafos} is allowed)
	 */
	public CubeStateMap(CsMapType csType) {
		if (csType == CsMapType.AllWholeCubeRotTrafos) {
			allWholeCubeRotTrafos();	// side effect: fills also map_ygr_wholeKey
		} else {
			throw new RuntimeException("Case " + csType + " not allowed!");
		}
	}
	
	private void allWholeCubeRotTrafos() {

		CubeStateFactory csFactory = new CubeStateFactory();
		CubieTripleFactory ctFactory = new CubieTripleFactory();
		CubieTriple ygrCubie = ctFactory.makeCubieTriple();   	// default constructor is for ygr-cubie
		int key=0;
		CubeState.Type type = (CubeConfig.cubeSize == CubeConfig.CubeSize.POCKET)
							? CubeState.Type.TRAFO_P : CubeState.Type.TRAFO_R;
		map_ygr_wholeKey = new Hashtable<>();
		map_inv_wholeCube = new Hashtable<>();
		for (int i=0; i<6; i++) {
			CubeState rot = csFactory.makeCubeState(type);
			switch(i) {
			case 0: 
				break;
			case 1:
				rot.fTr(1);
				break;
			case 2:
				rot.fTr(2);
				break;
			case 3:
				rot.fTr(3);
				break;
			case 4:
				rot.lTr(1);
				break;
			case 5:
				rot.lTr(3);
				break;
			}
			for (int j=0; j<4; j++) {
				//rot.print();
				
				// each whole-cube rotation is uniquely defined by the new location of the
				// y-face of the ygr-cubie:
				Integer ygrkey = rot.get_fcol(ygrCubie.loc[0]);
				CubeState tS = csFactory.makeCubeState(rot);	// IMPORTANT: We have to make a copy of rot, so that
							// each this.put(key,tS) below really stores a different value!! (Otherwise 
							// just a reference to the (mutable) object rot is stored in the HashMap.)

				//tS.recalc_sloc(); // -- no longer needed --
				tS.assert_fcol_sloc(" key="+key);

				this.put(key, tS);	// put (key,value) = (Integer, CubeState) into HashMap
				map_ygr_wholeKey.put(ygrkey,key);
				map_inv_wholeCube.put(key,CubeStateMap.invk[key]);

				rot.uTr(1); // prepare for next for-loop pass
				key++;
			}
		}
	}
	
	public void print() {
		DecimalFormat form = new DecimalFormat("00");
		for (Map.Entry<Integer, CubeState> entry : this.entrySet()) {
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
					form.format(entry.getKey()) + " --> " +
							(entry.getValue()).toString()
			);
		}
	}
	
	public int countDifferentStates() {
	    HashSet<CubeState> set = new HashSet<>();
		for (Map.Entry<Integer, CubeState> entry : this.entrySet()) {
		    set.add(entry.getValue());
		    		// if set.add(value) is called for a CubeState value which is already
		    		// present in the set (i.e. value.equals(other) is true for at least  
		    		// one CubeState other in set), then it will not be added to the set. Thus 
		    		// set.size() will return the # of truly different values in HashMap this.
        } 		    
		return set.size();
	}
	
	public static int countDifferentStates(ArrayList D) {
	    Iterator<CubeState> it1 = (Iterator<CubeState>) D.iterator();
	    HashSet<CubeState> set = new HashSet<>();
	    while (it1.hasNext()) {
		    set.add(it1.next());
		    		// if set.add(value) is called for a CubeState value which is already
		    		// present in the set (i.e. value.equals(other) is true for at least  
		    		// one CubeState other in set), then it will not be added to the set. Thus 
		    		// set.size() will return the # of truly different values in HashMap this.
        } 		    
		return set.size();
	}
	
	public int countYgrHomeStates() {
		int counter = 0;
		for (Map.Entry<Integer, CubeState> entry : this.entrySet()) {
		    CubeState cS = entry.getValue();
		    if (cS.get_fcol(12)==3 && cS.get_fcol(16)==4 && cS.get_fcol(20)==5) counter++;
        } 		    
		return counter;
		
	}
	
} // class CubeStateMap

