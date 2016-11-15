package games.TicTacToe;

import java.io.Serializable;

import games.Feature;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;


/**
 * Implementation of {@link Feature} for game TicTacToe.<p>
 * 
 * Method {@link #prepareInputVector(StateObservation)} returns the feature vector. 
 * The constructor accepts argument {@code featmode} to construct different types 
 * of feature vectors. The acceptable values for {@code featmode} are
 * retrieved with {@link #getAvailFeatmode()}.
 *
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class FeatureTTT extends TicTDBase implements Feature, Serializable {
	// class FeatureTTT is derived from TicTDBase in order to access the protected
	// method TicTDBase.prepareInputVector.	
	
	public FeatureTTT(int featmode) {
		super("", featmode);
	}
	
	@Override
	public double[] prepareInputVector(StateObservation sob) {
		assert (sob instanceof StateObserverTTT) : "Input 'sob' is not of class StateObserverTTT";
		StateObserverTTT so = (StateObserverTTT) sob;
		int[][] table = so.getTable();
		int player = so.getPlayerPM();
		// note that TicTDBase.prepareInputVector requires the player who
		// **made** the last move, therefore '-player':
		double[] input = super.prepareInputVector(-player, table);
		return input;
	}

	@Override
	public String stringRepr(double[] featVec) {
		return inputToString(featVec);
	}

	@Override
	public int[] getAvailFeatmode() {
		int[] featlist = {0,1,2,3,4,5,9};
		return featlist;
	}

	@Override
	public int getFeatmode() {
		return super.getFeatmode();
	}

	@Override
	public double getScore(StateObservation sob) {
		// TODO Auto-generated method stub
		return 0;
	}

}
