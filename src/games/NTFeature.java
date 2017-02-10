package games;

/**
 * Interface {@link NTFeature} is an extension of interface {@link Feature} 
 * for n-tuple sets.<p>
 * 
 * Classes implementing {@link NTFeature} have a different implementation of 
 * {@link Feature#stringRepr(double[])} (the full printout would be too long)
 * and have different, n-tuple specific {@code featmode} options. 
 *
 * @author Wolfgang Konen, TH Köln, Feb'16
 */
public interface NTFeature extends Feature {

}
