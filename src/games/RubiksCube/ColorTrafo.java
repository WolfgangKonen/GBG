package games.RubiksCube;

import java.text.DecimalFormat;

import games.RubiksCube.CubeState.Type;

public class ColorTrafo {
	public int[] fcol;   // fcol[i] holds the new color for current color no. i 
	
	public ColorTrafo() {
		this.fcol = new int[] {0,1,2,3,4,5};
//		switch(type) {
//		case POCKET: 
//			this.fcol = new int[] {0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5};
//			break;
//		case RUBIKS:
//			this.fcol = new int[] {0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,
//								   3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5};
//			break;
//		case TRAFO_P:
//			// fcol[i] holds the parent of location i under the trafo represented by this ColorTrafo object.
//			// Initially this trafo is the id transformation for the pocket cube.
//			this.fcol = new int[24];
//			for (int i=0; i<fcol.length; i++) this.fcol[i] = i;
//			break;
//		case TRAFO_R:
//			// fcol[i] holds the parent of location i under the trafo represented by this ColorTrafo object.
//			// Initially this trafo is the id transformation for the Rubik's cube.
//			this.fcol = new int[48];
//			for (int i=0; i<fcol.length; i++) this.fcol[i] = i;
//			break;
//		}
	}
	
	public ColorTrafo(int[] fcol) {
		assert(fcol.length==6) : "fcol has not length 6";
		this.fcol = fcol.clone();
	}
	
	/**
	 * Copy constructor
	 * @param cs
	 */
	public ColorTrafo(ColorTrafo cs) {
		this.fcol = cs.fcol.clone();
	}
	
	/**
	 * Whole-cube rotation counter-clockwise around the u-face
	 */
	public ColorTrafo uTr() {
		int i;
		// fcol(invT[i]) is the color which cubie faces with color i get after transformation:
		int[] invT = {0,5,1,3,2,4};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		return this;
	}

	/**
	 * Whole-cube rotation counter-clockwise around the f-face
	 */
	public ColorTrafo fTr() {
		int i;
		// fcol(invT[i]) is the color which cubie faces with color i get after transformation:
		int[] invT = {4,0,2,1,3,5};
		int[] tmp = this.fcol.clone();
		for (i=0; i<invT.length; i++) tmp[i] = this.fcol[invT[i]];
		for (i=0; i<invT.length; i++) this.fcol[i] = tmp[i]; 
		return this;
	}

	/**	
	 * Whole-cube rotation counter-clockwise around the l-face
	 */
	public ColorTrafo lTr() {
		return this.fTr().uTr().fTr().fTr().fTr();
	}
	
	public ColorTrafo print() {
		System.out.println(this.toString());
		return this;
	}
	
	public String toString() {
		DecimalFormat form = new DecimalFormat("0");
		String s = "";
   		for (int i=0; i<fcol.length; i++) {
			if (i%6==0) s = s + "|";
			s = s + form.format(fcol[i]);
		}
 		s = s + "|";  
		return s;	
	}
	
	/**
	 * Checks whether elements of members fcol and type are the same in {@code this} and {@code other}.
	 * (This differs from Object::equals(), since this tests, whether the Objects are the
	 * same, not their content.)
	 */
	public boolean isEqual(ColorTrafo other) {
		for (int i=0; i<fcol.length; i++) {
			if (this.fcol[i]!=other.fcol[i]) return false;
		}     		
		return true;
	}
	
	/**
	 * It is important that Object::equals is overwritten here, so that objects
	 * of class ColorTrafo which have the same elements in fcol[] are counted as
	 * equal. The operation equals is the one that HashSet::add() relies on
	 * (see implementation CubeStateMap::countDifferentStates())
	 */
	public boolean equals(Object other) {
		assert (other instanceof ColorTrafo) : "Object other is not of class ColorTrafo";
		return isEqual((ColorTrafo) other);
	}
	
} // class ColorTrafo
