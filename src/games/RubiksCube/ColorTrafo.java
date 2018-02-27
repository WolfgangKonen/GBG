package games.RubiksCube;

import java.io.Serializable;
import java.text.DecimalFormat;
import games.RubiksCube.CubeState.Type;

/**
 * A color transformation assigns each cubie face a new color in such a way that
 * it is still a valid cube.
 * <p>
 * Color coding: 0:w, 1:b, 2:o, 3:y, 4:g, 5:r.
 * <p>
 * What is a <b>valid cube</b>? - After the color transformation the set of
 * cubies, e.g. (wbo,ygr,...), must be still the same. <br>
 * A valid transformation is accomplished by taking a solved cube (which has
 * up-face w=white and left-face b=blue and so on), and making any one of the 24
 * whole-cube rotations with it. The color of the new up-face is the new color
 * for white, and so on.
 * 
 * @see ColorTrafoMap
 */
public class ColorTrafo implements Serializable {
	public int[] fcol; // fcol[i] holds the new color for current color no. i
	public int[] T = null; // the forward trafo

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public ColorTrafo() {
		this.fcol = new int[] { 0, 1, 2, 3, 4, 5 };
	}

	public ColorTrafo(int[] fcol) {
		assert (fcol.length == 6) : "fcol has not length 6";
		this.fcol = fcol.clone();
	}

	/**
	 * Copy constructor
	 * 
	 * @param cs
	 */
	public ColorTrafo(ColorTrafo cs) {
		this.fcol = cs.fcol.clone();
	}

	/**
	 * Whole-cube rotation counter-clockwise around the u-face
	 */
	private ColorTrafo uTr() {
		int i;
		// fcol[invT[i]] is the color which cubie faces with color i get after
		// transformation:
		int[] invT = { 0, 5, 1, 3, 2, 4 };
		int[] tmp = this.fcol.clone();
		for (i = 0; i < invT.length; i++)
			tmp[i] = this.fcol[invT[i]];
		for (i = 0; i < invT.length; i++)
			this.fcol[i] = tmp[i];
		return this;
	}

	/**
	 * Whole-cube rotation counter-clockwise around the f-face
	 */
	private ColorTrafo fTr() {
		int i;
		// fcol(invT[i]) is the color which cubie faces with color i get after
		// transformation:
		int[] invT = { 4, 0, 2, 1, 3, 5 };
		int[] tmp = this.fcol.clone();
		for (i = 0; i < invT.length; i++)
			tmp[i] = this.fcol[invT[i]];
		for (i = 0; i < invT.length; i++)
			this.fcol[i] = tmp[i];
		return this;
	}

	/**
	 * Whole-cube rotation counter-clockwise around the l-face
	 */
	private ColorTrafo lTr() {
		return this.fTr(1).uTr(3).fTr(3);
	}

	/**
	 * Whole-cube rotation, {@code times} * 90° counter-clockwise around the u-face
	 */
	public ColorTrafo uTr(int times) {
		for (int i = 0; i < times; i++)	this.uTr();
		return this;
	}

	/**
	 * Whole-cube rotation, {@code times} * 90° counter-clockwise around the l-face
	 */
	public ColorTrafo lTr(int times) {
		for (int i = 0; i < times; i++)	this.lTr();
		return this;
	}

	/**
	 * Whole-cube rotation, {@code times} * 90° counter-clockwise around the f-face
	 */
	public ColorTrafo fTr(int times) {
		for (int i = 0; i < times; i++) this.fTr();
		return this;
	}

	//
	/**
	 * Set the forward trafo T for {@code this}
	 */
	public void setT() {
		this.T = new int[6];
		for (int k = 0; k < 6; k++)
			T[fcol[k]] = k;
	}

	public ColorTrafo print() {
		System.out.println(this.toString());
		return this;
	}

	public String toString() {
		DecimalFormat form = new DecimalFormat("0");
		String s = "";
		for (int i = 0; i < fcol.length; i++) {
			if (i % 6 == 0)
				s = s + "|";
			s = s + form.format(fcol[i]);
		}
		s = s + "|";
		return s;
	}

	/**
	 * Checks whether elements of members fcol and type are the same in
	 * {@code this} and {@code other}. (This differs from
	 * {@link Object#equals(Object)}, since the latter tests, whether the
	 * objects are the same, not their content.)
	 */
	public boolean isEqual(ColorTrafo other) {
		for (int i = 0; i < fcol.length; i++) {
			if (this.fcol[i] != other.fcol[i])
				return false;
		}
		return true;
	}

	/**
	 * It is important that {@link Object#equals(Object)} is overwritten here,
	 * so that objects of class ColorTrafo which have the same elements in
	 * fcol[] are counted as equal. The operation equals is the one that
	 * HashSet::add() relies on.
	 * 
	 * @see #hashCode()
	 * @see ColorTrafoMap#countDifferentStates()
	 */
	@Override
	public boolean equals(Object other) {
		assert (other instanceof ColorTrafo) : "Object other is not of class ColorTrafo";
		return isEqual((ColorTrafo) other);
	}

	/**
	 * Like with {@link ColorTrafo#equals(Object)}, it is equally important that
	 * {@link Object#hashCode()} is overwritten here in such a way that it
	 * returns the same hash code for objects with the same content. Since the
	 * equality check for inserting an object into a Set (HashSet) is based on
	 * sameness of equals() AND hashCode() (!!)
	 * <p>
	 * See <a href=
	 * "https://stackoverflow.com/questions/6187294/java-set-collection-override-equals-method/11577351">
	 * https://stackoverflow.com/questions/6187294/java-set-collection-override-
	 * equals-method/11577351</a>
	 * 
	 * @see Object#hashCode()
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
} // class ColorTrafo
