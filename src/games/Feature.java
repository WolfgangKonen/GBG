package games;

/**
 * 
 * Interface Feature translates game states into feature vectors.<p>
 * 
 * The abstract method {@link #prepareInputVector(StateObservation)} is 
 * defined in child classes of Evaluator and returns the feature vector. 
 * Child classes have usually constructors accepting a single argument 
 * {@code featmode}. The acceptable values for {@code featmode} in a 
 * certain child class are retrieved with {@link #getAvailFeatmode()}.
 *
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public interface Feature {
	public double[] prepareInputVector(StateObservation so);
	public String stringRepr(double[] featVec);
	public int getFeatmode();
	public int[] getAvailFeatmode();
}
