package games.Nim;

import java.io.Serializable;

import games.Feature;
import games.StateObservation;
import games.Nim.StateObserverNim;
import tools.Types;


/**
 * Implementation of {@link Feature} for game Nim.<p>
 * 
 * Method {@link #prepareFeatVector(StateObservation)} returns the feature vector. 
 * The constructor accepts argument {@code featmode} to construct different types 
 * of feature vectors. The acceptable values for {@code featmode} are
 * retrieved with {@link #getAvailFeatmode()}.
 * 
 * @author Wolfgang Konen, TH Koeln, Dec'18
 */
public class FeatureNim implements Feature, Serializable {
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public FeatureNim(int featmode) {
		throw new RuntimeException("FeatureNim not yet implemented!");
	}
	
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert (sob instanceof StateObserverNim) : "Input 'sob' is not of class StateObserverNim";
		double[] input = {1.0, 2.0};  // dummy
		return input;
	}

	@Override
	public String stringRepr(double[] featVec) {
		return "12";		// dummy
	}

	@Override
	public int[] getAvailFeatmode() {
		int[] featlist = {0,1,2,3,4,5,9};
		return featlist;
	}

	@Override
	public int getFeatmode() {
		return 0;
	}

    @Override
	public int getInputSize(int featmode) {
    	// inpSize[i] has to match the length of the vector which
    	// TicTDBase.prepareInputVector() returns for featmode==i:
    	int inpSize[] = { 6, 6, 10, 19, 13, 19, 0, 0, 0, 9 };
    	if (featmode>(inpSize.length-1) || featmode<0)
    		throw new RuntimeException("featmode outside allowed range 0,...,"+(inpSize.length-1));
    	return inpSize[featmode];
    }
    
}
