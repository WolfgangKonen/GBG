package games.SimpleGame;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.Feature;
import games.StateObservation;
import tools.Types;

import java.io.Serializable;


/**
 * Implementation of {@link Feature} for game TicTacToe.<p>
 * 
 * Method {@link #prepareFeatVector(StateObservation)} returns the feature vector. 
 * The constructor accepts argument {@code featmode} to construct different types 
 * of feature vectors. The acceptable values for {@code featmode} are
 * retrieved with {@link #getAvailFeatmode()}.
 * 
 * @author Wolfgang Konen, TH Koeln, Nov'16
 */
public class FeatureSG implements Feature, Serializable {
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public FeatureSG(int featmode) {

	}
	
	/**
	 * This dummy stub is just needed here, because {@link FeatureSG} is derived from
	 * {@link AgentBase}, which implements {@link PlayAgent} and thus requires this method. 
	 * It should not be called. If called, it throws a RuntimeException.
	 */
	public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		throw new RuntimeException("FeatureTTT does not implement getNextAction2");
	}
	
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert (sob instanceof StateObserverSG) : "Input 'sob' is not of class StateObserverTTT";
		StateObserverSG so = (StateObserverSG) sob;
		int[][] table = so.getTable();
		int player = Types.PLAYER_PM[so.getPlayer()];
		// note that TicTDBase.prepareInputVector requires the player who
		// **made** the last move, therefore '-player':
		double[] input = new double[3]; // dummy
		return input;
	}

	@Override
	public String stringRepr(double[] featVec) {
		return "FeatureSG.stringRepr not yet implemented";
	}

	@Override
	public int[] getAvailFeatmode() {
		int[] featlist = {0};
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
    
	public double getScore(StateObservation sob) {
		// Auto-generated method stub (just needed because AgentBase,
		// the superclass of TicTDBase, requires it)
		return 0;
	}

}
