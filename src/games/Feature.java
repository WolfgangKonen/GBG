package games;

/**
 * 
 * Interface Feature translates game states into feature vectors.<p>
 * 
 * The abstract method {@link #prepareFeatVector(StateObservation)} is 
 * defined in child classes of {@link Feature} and returns the feature vector. 
 * Child classes have usually constructors accepting a single argument 
 * {@code featmode}. The argument {@code featmode} allows to construct 
 * different flavors of {@link Feature} objects.
 * The acceptable values for {@code featmode} in a 
 * certain child class are retrieved with {@link #getAvailFeatmode()}.
 *
 * @author Wolfgang Konen, TH Koeln, Feb'16
 */
public interface Feature {
	public double[] prepareFeatVector(StateObservation so);
	public String stringRepr(double[] featVec);
	public int getFeatmode();
	public int[] getAvailFeatmode();
	public int getInputSize(int featmode);
}
