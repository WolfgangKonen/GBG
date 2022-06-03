package games.RubiksCube;

import java.text.DecimalFormat;

public class CubieTriple {
	public enum Orientation {COUNTER, CLOCK}
	int[] loc;
	int[] col;
	Orientation ori;

	/**
	 * If {@code i} is the sticker location for the y-face of the ygr-cubie, then {@code right[i]} is the sticker location
	 * for the g-face of the ygr-cubie (marching around the cubie in clockwise orientation).
	 * And {@code right[right[i]]} is the sticker location of the r-face of the ygr-cubie.
	 */
	              //            0           4          8          12         16          20
	public static int[] right= {8,18,23,5,  0,22,15,9, 4,14,19,1, 16,10,7,21,20,2,11,13, 12,6,3,17};

	/**
	 * If {@code i} is a sticker location, then {@code ccolo[i]} is the default color for this location
	 */
	public static int[] ccolo= {0,0,0,0,    1,1,1,1,   2,2,2,2,   3,3,3,3,   4,4,4,4,    5,5,5,5  };

	/**
	 * The ygr-cubie in its default location {12,16,20} with colors {3,4,5}={y,g,r}
	 */
	public CubieTriple() {
//		final int[] loc = {12,16,20};
//		final int[] col = {3,4,5};
//		this.loc = loc.clone();
//		this.col = col.clone();
//		this.ori = Orientation.CLOCK;
		initialize(12);
	}

	/**
	 * The cubie with its first sticker at position {@code i} in its default location
	 * @param i sticker position
	 */
	public CubieTriple(int i) {
		initialize(i);
	}

	private void initialize(int i) {
		this.loc = new int[] {i, right[i], right[right[i]]};
		this.col = new int[] {ccolo[i], ccolo[right[i]], ccolo[right[right[i]]]};
		this.ori = Orientation.CLOCK;
	}

	public CubieTriple(CubieTriple other) {
		this.loc = other.loc.clone();
		this.col = other.col.clone();
		this.ori = other.ori;
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

