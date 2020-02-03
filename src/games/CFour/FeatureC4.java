package games.CFour;

import java.io.Serializable;

import games.Feature;
import games.StateObservation;
import tools.Types;


/**
 * Implementation of {@link Feature} for game C4.<p>
 * 
 * Method {@link #prepareFeatVector(StateObservation)} returns the feature vector. 
 * The constructor accepts argument {@code featmode} to construct different types 
 * of feature vectors. The acceptable values for {@code featmode} are
 * retrieved with {@link #getAvailFeatmode()}.
 * 
 * @author Wolfgang Konen, TH Koeln, Nov'18
 */
public class FeatureC4 implements Feature, Serializable {
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public FeatureC4(int featmode) {
//		super("", featmode);
	}
	
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert (sob instanceof StateObserverC4) : "Input 'sob' is not of class StateObserverC4";
		StateObserverC4 so = (StateObserverC4) sob;
//		int[][] table = so.getTable();
		int player = Types.PLAYER_PM[so.getPlayer()];
		// note that TicTDBase.prepareInputVector requires the player who
		// **made** the last move, therefore '-player':
//		double[] input = super.prepareInputVector(-player, table);
		double[] input = new double[5]; /* DUMMY */
		return input;
	}

	@Override
	public String stringRepr(double[] featVec) {
//		return inputToString(featVec);
		return ""; /* DUMMY */
	}

	@Override
	public int[] getAvailFeatmode() {
		int[] featlist = {0,1,2,3,4,5,9};
		return featlist;
	}

	@Override
	public int getFeatmode() {
//		return super.getFeatmode();
		return 0; /* DUMMY */
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
    
//	@Override
	public double getScore(StateObservation sob) {
		// Auto-generated method stub (just needed because AgentBase,
		// the superclass of TicTDBase, requires it)
		return 0;
	}

}
