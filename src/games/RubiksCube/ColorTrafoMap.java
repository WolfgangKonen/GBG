package games.RubiksCube;


import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import games.RubiksCube.CubeState.Twist;
import games.RubiksCube.CubeState.Type;

/**
 * A map of {@link ColorTrafo} objects. The key is just an integer counter to 
 * distinguish every ColorTrafo when marching through them (e.g. in {@code ColorTrafoMap(AllColorTrafos)}).
 * <p>
 * Member {@code key} carries the same number as the corresponding whole-cube rotation.
 * See {@link CubeStateMap#CubeStateMap(CubeStateMap.CsMapType)} for numbering convention.
 *
 * @see ColorTrafo
 */
public class ColorTrafoMap extends Hashtable<Integer,ColorTrafo> implements Serializable {
	/**
	 * (currently only {@code AllColorTrafos})
	 */
	public enum ColMapType {AllColorTrafos}

	/**
	 * A map of 24 {@link ColorTrafo} objects that contains all valid color trafos
	 * of the cube. <br>
	 * See {@link CubeStateMap#CubeStateMap(CubeStateMap.CsMapType)} for trafo numbering.
	 */
	public static ColorTrafoMap allCT = new ColorTrafoMap(ColMapType.AllColorTrafos);

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long  serialVersionUID = 12L;

	public ColorTrafoMap() {
		super();
	}

	public ColorTrafoMap(ColMapType csType) {
		if (csType == ColMapType.AllColorTrafos) {        // currently, ColMapType has only one type
			allColorTrafos();
		} else {
			throw new RuntimeException("Case " + csType + " not allowed!");
		}
	}
	
	private void allColorTrafos() {
		int key=0;
		//int[] loc = new int[3];
		for (int i=0; i<6; i++) {
			ColorTrafo rot = new ColorTrafo(key);
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
				ColorTrafo tS = new ColorTrafo(key,rot);	// IMPORTANT: We have to make a copy of rot,
					// so that each this.put(iCnt,tS) below really stores a different value!! (Otherwise 
					// just a reference to the (mutable) object rot is stored in the Hashtable.)

				this.put(key, tS);	// put (key,value) = (Integer, ColorTrafo) into HashMap
				rot.uTr(1);
				key++;
			}
		}
	}
	
	public void print() {
		DecimalFormat form = new DecimalFormat("00");
		for (Map.Entry<Integer, ColorTrafo> entry : this.entrySet()) {
			System.out.println(
					form.format(entry.getKey()) + " --> " +
							(entry.getValue()).toString()
			);
		}
	}
	
	public int countDifferentStates() {
	    Iterator<Map.Entry<Integer, ColorTrafo>> it1 = this.entrySet().iterator();
	    HashSet<ColorTrafo> set = new HashSet<>();
	    while (it1.hasNext()) {
		    Map.Entry<Integer, ColorTrafo> entry = it1.next();
		    set.add(entry.getValue());
		    		// if set.add(value) is called for a ColorTrafo value which is already
		    		// present in the set (i.e. value.equals(other) is true for at least  
		    		// one ColorTrafo other in set), then it will not be added to the set. Thus 
		    		// set.size() will return the truly different values in HashMap this.
        } 		    
		return set.size();
	}

	/**
	 * Given a cube state in cS, apply all color transformation of {@code this} to it.
	 * <p>
	 *     Deprecated, use {@link CubeState#applyCT(ColorTrafoMap)} instead
	 * </p>
	 * 
	 * @param cS		a cube state of type POCKET or RUBIKS	
	 * @param hmRots	helper, all whole-cube rotations, to bring ygr-cubie 'home'
	 * @return	a map with all states which are color-symmetric equivalent to cS
	 */
	@Deprecated
	public CubeStateMap applyColSymm(CubeState cS, CubeStateMap hmRots) {
		CubeStateFactory csFactory = new CubeStateFactory();
		CubieTripleFactory ctFactory = new CubieTripleFactory();
		assert(cS.type==Type.COLOR_P || cS.type==Type.COLOR_R) : "Wrong cS.type in applyColSymm(cS) !";
		//int[] loc = new int[3];
		//CubieTriple cub = new CubieTriple();
		CubieTriple ygr = ctFactory.makeCubieTriple();
		CubeStateMap newMap = new CubeStateMap();
		//for (int i=0; i<3; i++) cub.loc[i]=0;		// cub never used

		for (Map.Entry<Integer, ColorTrafo> entry : this.entrySet()) {
			Integer key = entry.getKey();
			ColorTrafo cT = entry.getValue();
			CubeState dS = csFactory.makeCubeState(cS);
			dS.applyCT(cT);                            // apply color trafo to dS
			//
			// it is (currently) difficult to give the right values for
			// dS.lastTwist and dS.twistSequence, so we set them to empty values ('not known'):
			dS.lastTwist = Twist.ID;
			dS.twistSeq = "";

			// this part should have been done already by the (new) applyCT-call above
//			CubieTriple where = dS.locate(ygr);        // find new location of ygr-cubie
//			CubeState trafo = (CubeState) hmRots.get(where.loc[0]);
//			if (trafo == null) throw new RuntimeException("Key 'where' not found!");
//			dS.apply(trafo);        // apply the right whole-cube rotation to bring ygr-cubie 'home'

			newMap.put(key, dS);    // add dS to newMap
			//dS.print();
		}
		return newMap;
	}
	

} // class ColorTrafoMap