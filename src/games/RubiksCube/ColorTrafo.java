package games.RubiksCube;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;

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
 * <p>
 * Member {@code key} carries the same number as the corresponding whole-cube rotation.
 * See {@link CubeStateMap#CubeStateMap(CubeStateMap.CsMapType)} for numbering convention.
 * 
 * @see ColorTrafoMap
 * @see CubeStateMap
 */
public class ColorTrafo implements Serializable {
	private int key;
	private int[] ccol; 			// ccol[i] holds the new color for current color no. i
	private int[] T = new int[6]; 	// the forward trafo  		// TODO: Do we need this?

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable, or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long  serialVersionUID = 12L;

	public ColorTrafo(int key) {
		this.key = key;
		this.ccol = new int[] { 0, 1, 2, 3, 4, 5 };
		setT();
	}

	// --- never used ---
//	public ColorTrafo(int[] currentCol) {
//		assert (currentCol.length == 6) : "currentCol has not length 6";
//		this.ccol = currentCol.clone();
//		setT();
//	}

	/**
	 * Copy constructor
	 * 
	 * @param other	the trafo to copy
	 */
	public ColorTrafo(ColorTrafo other) {
		this.key = other.key;
		this.ccol = other.ccol.clone();
		this.T = other.T.clone();
	}

	/**
	 * Copy constructor, but with new key
	 *
	 * @param key 	the new key (may be different from {@code other.key})
	 * @param other	the trafo to copy
	 */
	public ColorTrafo(int key, ColorTrafo other) {
		this.key = key;
		this.ccol = other.ccol.clone();
		this.T = other.T.clone();
	}

	/**
	 * Whole-cube rotation counter-clockwise around the u-face
	 */
	private ColorTrafo uTr() {
		int i;
		// ccol[invT[i]] is the color which cubie faces with color i get after
		// transformation:
		int[] invT = { 0, 5, 1, 3, 2, 4 };
		int[] tmp = new int[ccol.length];
		for (i = 0; i < invT.length; i++)
			tmp[i] = this.ccol[invT[i]];
		for (i = 0; i < invT.length; i++)
			this.ccol[i] = tmp[i];
		return this;
	}

	/**
	 * Whole-cube rotation counter-clockwise around the f-face
	 */
	private ColorTrafo fTr() {
		int i;
		// ccol(invT[i]) is the color which cubie faces with color i get after
		// transformation:
		int[] invT = { 4, 0, 2, 1, 3, 5 };
		int[] tmp = new int[ccol.length];
		for (i = 0; i < invT.length; i++)
			tmp[i] = this.ccol[invT[i]];
		for (i = 0; i < invT.length; i++)
			this.ccol[i] = tmp[i];
		return this;
	}

	/**
	 * Whole-cube rotation counter-clockwise around the l-face
	 */
	private ColorTrafo lTr() {
		return this.fTr(1).uTr(3).fTr(3);
	}

	/**
	 * Whole-cube rotation, {@code times} * 90 degree counter-clockwise around the u-face
	 * @param times		how many times
	 * @return			{@code this}
	 */
	public ColorTrafo uTr(int times) {
		for (int i = 0; i < times; i++)	this.uTr();
		setT();
		return this;
	}

	/**
	 * Whole-cube rotation, {@code times} * 90 degree counter-clockwise around the f-face
	 * @param times		how many times
	 * @return			{@code this}
	 */
	public ColorTrafo fTr(int times) {
		for (int i = 0; i < times; i++) this.fTr();
		setT();
		return this;
	}

	/**
	 * Whole-cube rotation, {@code times} * 90 degree counter-clockwise around the l-face
	 * @param times		how many times
	 * @return			{@code this}
	 */
	public ColorTrafo lTr(int times) {
		for (int i = 0; i < times; i++)	this.lTr();
		setT();
		return this;
	}

	//
	/**
	 * Set the forward trafo T for {@code this}
	 * TODO: Do we need this?
	 */
	private void setT() {
		for (int k = 0; k < 6; k++)
			T[ccol[k]] = k;
	}

	public int getKey() {
		return key;
	}

	public int getCCol(int i) {
		return ccol[i];
	}

	public ColorTrafo print() {
		System.out.println(this);
		return this;
	}

	public String toString() {
		DecimalFormat form = new DecimalFormat("0");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ccol.length; i++) {
			if (i % 6 == 0)
				sb.append("|");
			sb.append(form.format(ccol[i]));
		}
		sb.append("|");
		sb.append(" (key:"+key+")");
		return sb.toString();
	}

	/**
	 * Checks whether elements of members ccol and T are the same in
	 * {@code this} and {@code other}. (This differs from
	 * {@link Object#equals(Object)}, since the latter tests, whether the
	 * objects are the same, not their content.)
	 * @param other the other trafo
	 * @return true, if contents is the same
	 */
	public boolean isEqual(ColorTrafo other) {
		if (this.key != other.key) return false;
		for (int i = 0; i < ccol.length; i++) {
			if (this.ccol[i] != other.ccol[i]) return false;
			if (this.T[i] != other.T[i]) return false;
		}
		return true;
	}

	/**
	 * It is important that {@link Object#equals(Object)} is overwritten here,
	 * so that objects of class ColorTrafo which have the same elements in
	 * ccol[] are counted as equal. The operation equals is the one that
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
