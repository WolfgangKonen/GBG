package games.RubiksCube;

import java.text.DecimalFormat;

public class CubieTriple {
	public enum Orientation {COUNTER, CLOCK};
	int[] loc = new int[3];
	int[] col = new int[3];
	Orientation ori;
	
	/**
	 * The ygr-cubie in its default location
	 */
	public CubieTriple() {
		final int[] loc = {12,16,20};
		final int[] col = {3,4,5};
		this.loc = loc.clone();
		this.col = col.clone();
		this.ori = Orientation.CLOCK;
	}
	
	public CubieTriple(int[] loc, int[] col, Orientation ori) {
		this.loc = loc.clone();
		this.col = col.clone();
		this.ori = ori; 
	}
	
	public CubieTriple(CubieTriple other) {
		this.loc = other.loc.clone();
		this.col = other.col.clone();
		this.ori = other.ori;
	}
	
	public CubieTriple print() {
		System.out.println(this.toString());
		return this;
	}
	
	public String toString() {
		DecimalFormat form = new DecimalFormat("00");
		String s = "";
		s = s + "(";
		for (int i=0;i<3;i++) {
			s = s + form.format(this.loc[i]);
			if (i!=2) s = s + ",";
		}
		s = s + ")";
		return s;
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

