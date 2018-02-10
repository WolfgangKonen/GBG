package games.RubiksCube;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import games.RubiksCube.CubieTriple.Orientation;

public class CubeState {
	
	public static enum Type {POCKET,RUBIKS,TRAFO_P,TRAFO_R};
	public static enum Twist {ID,U,L,F};
	
	Type type = Type.POCKET;
	Twist lastTwist = Twist.ID;
	public int[] fcol;   // fcol[i] holds the face color for cubie face no. i 
	
	public CubeState() {
		this(Type.POCKET);
	}
	public CubeState(Type type) {
		this.type = type;
		switch(type) {
		case POCKET: 
			this.fcol = new int[] {0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5};
			break;
		case RUBIKS:
			this.fcol = new int[] {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,
								   3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5};
			break;
		case TRAFO_P:
			// fcol[i] holds the parent of location i under the trafo represented by this CubeState object.
			// Initially this trafo is the id transformation for the pocket cube.
			this.fcol = new int[24];
			for (int i=0; i<fcol.length; i++) this.fcol[i] = i;
			break;
		case TRAFO_R:
			// fcol[i] holds the parent of location i under the trafo represented by this CubeState object.
			// Initially this trafo is the id transformation for the Rubik's cube.
			this.fcol = new int[48];
			for (int i=0; i<fcol.length; i++) this.fcol[i] = i;
			break;
		}
	}
	
	public CubeState(int[] fcol) {
		switch(fcol.length) {
		case 24: 
			this.type = Type.POCKET;
			break;
		case 48:
			this.type = Type.RUBIKS;
		}
		this.fcol = fcol.clone();
	}
	
	/**
	 * Copy constructor
	 * @param cs
	 */
	public CubeState(CubeState cs) {
		this.type = cs.type;
		this.lastTwist = cs.lastTwist;
		this.fcol = cs.fcol.clone();
	}
	
	/**
	 * Whole-cube rotation counter-clockwise around the u-face
	 */
	public CubeState uTr() {
		int i;
		// fcol(invT[i]) is the color which cubie face i gets after transformation:
		int[] invT = {3,0,1,2,22,23,20,21,5,6,7,4,13,14,15,12,10,11,8,9,19,16,17,18};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		return this;
	}

	/**
	 * Whole-cube rotation counter-clockwise around the f-face
	 */
	public CubeState fTr() {
		int i;
		// fcol(invT[i]) is the color which cubie face i gets after transformation:
		int[] invT = {18,19,16,17,1,2,3,0,11,8,9,10,6,7,4,5,15,12,13,14,21,22,23,20};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		return this;
	}

	/**	
	 * Whole-cube rotation counter-clockwise around the l-face
	 */
	public CubeState lTr() {
		return this.fTr().uTr().uTr().uTr().fTr().fTr().fTr();
	}
	
	/**
	 * 	 Counter-clockwise Twist of the U-face
	 */
	public CubeState UTw() {
		int i;
		// fcol(invT[i]) is the color which cubie face i gets after transformation:
		//            0        4          8         12           16          20
		int[] invT = {3,0,1,2, 22,23,6,7, 5,9,10,4, 12,13,14,15, 16,11,8,19, 20,21,17,18};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		this.lastTwist=Twist.U;
		return this;
		//
		// use the following line once on a default TRAFO_P CubeState to generate int[] invT above:
//		return this.lTr().lTr().lTr().FTw().lTr();   	// U(x) = l(F(l^3(x))) 
	}
	
	/**
	 * 	 Counter-clockwise Twist of the L-face
	 */
	public CubeState LTw() {
		int i;
		// fcol(invT[i]) is the color which cubie face i gets after transformation:
		//            0        4        8            12           16           20
		int[] invT = {9,1,2,8, 7,4,5,6, 14,15,10,11, 12,13,21,22, 16,17,18,19, 20,3,0,23};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		this.lastTwist=Twist.L;
		return this;
		//
		// use the following line once on a default TRAFO_P CubeState to generate int[] invT above:
//		return this.uTr().FTw().uTr().uTr().uTr();   	// L(x) = u^3(F(u(x))) 
	}

	/**
	 * 	 Counter-clockwise Twist of the F-face
	 */
	public CubeState FTw() {
		int i;
		// fcol(invT[i]) is the color which cubie face i gets after transformation:
		int[] invT = {18,19,2,3,1,5,6,0,11,8,9,10,12,7,4,15,16,17,13,14,20,21,22,23};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		this.lastTwist=Twist.F;
		return this;
	}
	
	public CubeState UTw(int times) {
		for (int i=0; i<times; i++) {
			this.UTw();
		}
		return this;
	}
	
	public CubeState LTw(int times) {
		for (int i=0; i<times; i++) {
			this.LTw();
		}
		return this;
	}
	
	public CubeState FTw(int times) {
		for (int i=0; i<times; i++) {
			this.FTw();
		}
		return this;
	}
	
	/**
	 * Apply transformation cT to this
	 * @param cT
	 * @return
	 */
	public CubeState apply(CubeState trafo) {
		assert(trafo.type==Type.TRAFO_P || this.type==Type.TRAFO_R) : "Wrong type in apply(trafo) !";
		int i;
		int[] tmp = this.fcol.clone();
		for (i=0; i<fcol.length; i++) tmp[i] = this.fcol[trafo.fcol[i]];
		for (i=0; i<fcol.length; i++) this.fcol[i] = tmp[i]; 
		return this;		
	}
	
	/**
	 * Apply color transformation cT to this
	 * @param cT
	 * @return
	 */
	public CubeState applyCT(ColorTrafo cT) {
		assert(this.type==Type.POCKET || this.type==Type.RUBIKS) : "Wrong type in apply(cT) !";
		for (int i=0; i<fcol.length; i++) this.fcol[i] = cT.fcol[this.fcol[i]];
		return this;		
	}
	
	/**
	 * Locate the cubie with the colors of tri in this
	 * @param tri
	 * @return its member loc carries the location of the cubie with the colors tri
	 */
	public CubieTriple locate(CubieTriple tri) {
		CubieTriple where = new CubieTriple(tri);
		assert(this.type==Type.POCKET || this.type==Type.RUBIKS) : "Wrong type in apply() !";
		//            0           4          8          12         16          20 
		int[] left = {4,11,17,22, 8,3,21,14, 0,7,13,18, 20,19,9,6, 12,23,1,10, 16,15,5,2};
		int[] right= {8,18,23,5, 0,22,15,9, 4,14,19,1, 16,10,7,21, 20,2,11,13, 12,6,3,17};
		int rig;
		switch(tri.ori) {
		case CLOCK: 
			for (int i=0; i<fcol.length; i++) {
				if (fcol[i]==tri.col[0]) {
					where.loc[0]=i;
					rig = right[i];
					if (fcol[rig]==tri.col[1]) {
						where.loc[1]=rig;
						rig = right[rig];
						if (fcol[rig]==tri.col[2]) {
							where.loc[2]=rig;
							return where; 
						}
					}
				}
			}
			break;
		case COUNTER:
			throw new RuntimeException("Not yet implemented");
		}
		throw new RuntimeException("Invalid cube, we should not arrive here!");
	}

	public CubeState print() {
		System.out.println(this.toString());
		return this;
	}
	
	public String toString() {
		DecimalFormat form = new DecimalFormat("00");
		String s = "";
		switch(this.type) {
		case TRAFO_P: 
    		for (int i=0; i<fcol.length; i++) {
    			if (i%4==0) s = s + "|";
    			s = s + form.format(fcol[i]);
    		}
    		break;
		default:
    		for (int i=0; i<fcol.length; i++) {
    			if (i%4==0) s = s + "|";
    			s = s + fcol[i];
    		}
    		break;
		}
		s = s + "|";  
		return s;	
	}
	
	/**
	 * Checks whether elements of members fcol and type are the same in {@code this} and {@code other}.
	 * (This differs from Object::equals(), since this tests, whether the Objects are the
	 * same, not their content.)
	 */
	public boolean isEqual(CubeState other) {
		if (this.type!=other.type) return false;
		for (int i=0; i<fcol.length; i++) {
			if (this.fcol[i]!=other.fcol[i]) return false;
		}     		
		return true;
	}
	
	/**
	 * It is important that {@link Object#equals(Object)} is overwritten here, so that objects
	 * of class CubeState which have the same elements in fcol[] are considered as
	 * equal. The operation equals is the one that HashSet::add() relies on
	 * 
	 * @see CubeStateMap#countDifferentStates()
	 */
	@Override
	public boolean equals(Object other) {
		assert (other instanceof CubeState) : "Object other is not of class CubeState";
		return isEqual((CubeState) other);
	}
	
	/**
	 * It is equally important that {@link Object#hashCode()} is overwritten here in such a way
	 * that it returns the same hash code for objects with the same content. 
	 * Since the equality check for inserting an object into a Set (HashSet) is based on 
	 * sameness of equals() AND hashCode() (!!)  
	 * <p> 
	 * See <a href="https://stackoverflow.com/questions/6187294/java-set-collection-override-equals-method/11577351">
	 *     https://stackoverflow.com/questions/6187294/java-set-collection-override-equals-method/11577351</a>
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
} // class CubeState
   


    // DEPRECATED --------------------------------------------------------------------
//	/**
//	 * Whole-cube rotation counter-clockwise around the u-face
//	 */
//	private CubeState uTr(CubeState cs) {
//		// fcol(invT[i]) is the color which cubie face i gets after transformation:
//		int[] invT = {3,0,1,2,22,23,20,21,5,6,7,4,13,14,15,12,10,11,8,9,19,16,17,18};
//		CubeState tc = new CubeState(cs);
//		for (int i=0; i<invT.length; i++) tc.fcol[i] = cs.fcol[invT[i]];
//		return tc;
//	}
//
//	/**
//	 * Whole-cube rotation counter-clockwise around the f-face
//	 */
//	private CubeState fTr(CubeState cs) {
//		// fcol(invT[i]) is the color which cubie face i gets after transformation:
//		int[] invT = {18,19,16,17,1,2,3,0,11,8,9,10,6,7,4,5,15,12,13,14,21,22,23,20};
//		CubeState tc = new CubeState(cs);
//		for (int i=0; i<invT.length; i++) tc.fcol[i] = cs.fcol[invT[i]];
//		return tc;
//	}
//	
//	/**	
//	 * Whole-cube rotation counter-clockwise around the l-face
//	 */
//	private CubeState lTr(CubeState cs) {
//		return fTr(fTr(fTr(uTr(fTr(cs)))));
//	}
//	
//	/**
//	 * Counter-clockwise Twist of the F-face
//	 */
//	private CubeState FTw(CubeState cs) {
//		// fcol(invT[i]) is the color which cubie face i gets after transformation:
//		int[] invT = {18,19,2,3,1,5,6,0,11,8,9,10,12,7,4,15,16,17,13,14,20,21,22,23};
//		CubeState tc = new CubeState(cs);
//		for (int i=0; i<invT.length; i++) tc.fcol[i] = cs.fcol[invT[i]];
//		return tc;
//	}

//	private int[][] allWholeCubeRotations(CubeState cs) {
//		int[][] allRots = null;
//		allRots = new int[24][];
//		for (int i=0; i<6; i++) {
//			CubeState rot = new CubeState(cs);
//			switch(i) {
//			case 0: 
//				break;
//			case 1: 
//				rot.lTr();
//				break;
//			case 2:
//				rot.lTr().lTr().lTr();
//				break;
//			case 3: 
//				rot.fTr();
//				break;
//			case 4: 
//				rot.fTr().fTr();
//				break;
//			case 5: 
//				rot.fTr().fTr().fTr();
//				break;
//			}
//			//CubeState jRot = new CubeState(rot);
//			for (int j=0; j<4; j++) {
//				//rot.print();
//				allRots[i*4+j] = rot.fcol.clone();			// to be done better!
//				rot.uTr();
//			}
//		}
//
//		return allRots;
//	}
//	

//	private boolean areAllDifferent(int[][] fcolSet) {
//		for (int i=0; i<fcolSet.length; i++) {
//			CubeState cs_i = new CubeState(fcolSet[i]);
//			cs_i.print();
//			for (int j=0; j<i; j++) {
//				CubeState cs_j = new CubeState(fcolSet[j]);
//				if (cs_i.isEqual(cs_j)) 
//					return false; 
//			}
//		}
//		return true;
//	}

