package games.KuhnPoker;

import games.Feature;
import games.StateObservation;

import java.io.Serializable;


/**
 * Implementation of {@link Feature} for game Poker.<p>
 *
 * Method {@link #prepareFeatVector(StateObservation)} returns the feature vector.
 * The constructor accepts argument {@code featmode} to construct different types
 * of feature vectors. The acceptable values for {@code featmode} are
 * retrieved with {@link #getAvailFeatmode()}.
 *
 * @author Tim Zeh, TH Koeln, Nov'20
 */

public class FeatureKuhnPoker implements Feature, Serializable {
	int featMode;

	private int vectorSize0 = 4;

	public FeatureKuhnPoker(int featMode) {
		this.featMode = featMode;
	}

	/*
		Features of a Kuhn Poker game:

			wholecard {11,12,13}
			previous moves {-1;0;1;2;3}

	 */
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert sob instanceof StateObserverKuhnPoker : "Input 'so' is not of class StateObserverKuhnPoker";
		StateObserverKuhnPoker so = (StateObserverKuhnPoker) sob;

		switch (featMode) {
			case 0 -> {
				double[] featureVector = new double[vectorSize0];

				//wholecard
				PlayingCard pc = so.getHoleCards(so.getPlayer())[0];

				featureVector[0] = pc==null?-1:pc.getRank();

				//moves
				featureVector[1]=-1;
				featureVector[2]=-1;
				featureVector[3]=-1;
				for(int i = 0;i<so.getLastRoundMoves().toArray().length;i++)
					featureVector[i+1] = so.getLastRoundMoves().get(i)==null?-1:so.getLastRoundMoves().get(i);
				return featureVector;
			}
			default -> throw new RuntimeException("Unknown featmode: " + featMode);
		}
	}

	@Override
	public String stringRepr(double[] featVec) {
		StringBuilder sb = new StringBuilder();
		for (double aFeatVec : featVec) {
			sb.append(aFeatVec);
			sb.append(", ");
		}
		sb.delete(sb.length()-2, sb.length());
		return sb.toString();
	}

	@Override
	public int getFeatmode() {
		return featMode;
	}

	@Override
	public int[] getAvailFeatmode() {
		return new int[]{0};
	}

	@Override
	public int getInputSize(int featmode) {
		switch (featmode) {
			case 0 -> {
				return vectorSize0	;
			}
			default -> throw new RuntimeException("Unknown featmode: " + featmode);
		}
	}

}
