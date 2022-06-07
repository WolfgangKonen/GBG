package games.RubiksCube;

import java.text.DecimalFormat;

/**
 * A representation of a corner cubie
 */
abstract public class CubieTriple {
	public enum Orientation {COUNTER, CLOCK}
	int[] loc;
	int[] col;
	Orientation ori;

	/**
	 * If {@code i} is the sticker location for the y-face of the ygr-cubie, then {@code right[i]} is the sticker location
	 * for the g-face of the ygr-cubie (marching around the cubie in clockwise orientation).
	 * And {@code right[right[i]]} is the sticker location of the r-face of the ygr-cubie.
	 */
	public static int[] right= null;

	/**
	 * If {@code i} is a sticker location, then {@code ccolo[i]} is the default color for this location
	 */
	protected static int[] ccolo=null;

	public CubieTriple() {
		// empty, just a stub for  derived classes
	}

	public CubieTriple(CubieTriple other) {
		this.loc = other.loc.clone();
		this.col = other.col.clone();
		this.ori = other.ori;
	}

	protected void initialize(int i) {
		this.loc = new int[] {i, right[i], right[right[i]]};
		this.col = new int[] {ccolo[i], ccolo[right[i]], ccolo[right[right[i]]]};
		this.ori = Orientation.CLOCK;
	}

	public CubieTriple print() {
		System.out.println(this); // calls toString()
		return this;
	}
	
	public String toString() {
		DecimalFormat form = new DecimalFormat("00");
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i=0;i<3;i++) {
			sb.append(form.format(this.loc[i]));
			if (i!=2) sb.append(",");
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		assert (other instanceof CubieTriple) : "Object other is not of class CubieTriple";
		//System.out.println("Passing CubieTriple::equals()");
		CubieTriple cOther = (CubieTriple) other;
		for (int i=0; i<col.length; i++) {
			if (this.col[i]!=cOther.col[i]) return false;
			if (this.loc[i]!=cOther.loc[i]) return false;
		}     		
		return true;
		
	}
} // class CubieTriple

