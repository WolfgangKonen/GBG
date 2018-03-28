package games.RubiksCube;

import java.io.Serializable;

import games.Feature;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;
import tools.Types;


/**
 * Implementation of {@link Feature} for game RubiksCube.<p>
 * 
 * --- This is at present just a dummy class ---
 * 
 */
public class FeatureCube implements Feature, Serializable {
    int featMode = 0;
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	public FeatureCube(int featmode) {
        this.featMode = featMode;
	}
	
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		double[] input = new double[3];		// dummy only
		return input;
	}

	@Override
	public String stringRepr(double[] featVec) {
        StringBuilder sb = new StringBuilder();
        for (double aFeatVec : featVec) {
            sb.append(aFeatVec);
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
	}

	@Override
	public int[] getAvailFeatmode() {
		int[] featlist = {0};
		return featlist;
	}

	@Override
	public int getFeatmode() {
	       return featMode;
	}

    @Override
	public int getInputSize(int featmode) {
    	return 3; // dummy only
    }
    
}
