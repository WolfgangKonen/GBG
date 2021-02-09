package games.KuhnPoker;

import games.Feature;
import games.StateObservation;


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

public class FeatureKuhnPoker implements Feature{
	int featMode;

	public FeatureKuhnPoker(int featMode) {
		this.featMode = featMode;
	}


	/*
		Features of a Poker game:
			player
				states = {not playing, folded, active, open}
				chips = #
				toCall = #

			pot
				size = #

			cards
				activePlayer = ids
				community = ids

			ActivePlayer
	 */
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert sob instanceof StateObserverKuhnPoker : "Input 'so' is not of class StateObserverPoker";
		StateObserverKuhnPoker so = (StateObserverKuhnPoker) sob;

		// Pot
		// Cards
		switch (featMode) {
			case 0 -> {
				int numPlayers = so.getNumPlayers();
				double[] featureVector = new double[getInputSize(featMode)];
				for (int i = 0; i < numPlayers; i++) {

					featureVector[i + numPlayers] = so.getChips()[i];
					//TODO: inconsistent why do I use here no array? Should be standardized.
					featureVector[i + numPlayers] = so.getOpenPlayer(i);
				}
				int tmp = numPlayers * 3;
				featureVector[tmp++] = so.getPotSize();
				PlayingCard tmpCard;
				featureVector[tmp++] = ((tmpCard = so.getHoleCards()[0]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getHoleCards()[1]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp] = so.getPlayer();
				return featureVector;
			}
			case 1 -> throw new RuntimeException("Placeholder");
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
				int numPlayers = 4;
				return numPlayers * 3 + 1 + 2 + 5;
			}
			case 1 -> throw new RuntimeException("Placeholder");
			default -> throw new RuntimeException("Unknown featmode: " + featmode);
		}
	}

}
