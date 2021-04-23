package games.Poker;

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

public class FeaturePoker implements Feature, Serializable {
	int featMode;
	int numPlayers = 4;

	public FeaturePoker(int featMode) {
		this.featMode = featMode;
	}


	/*
		Features of a Poker game:
			player
				states = {not playing, folded, active, open}
				toCall = #

			pot
				size = #

			cards
				activePlayer = ids
				community = ids
	 */
	@Override
	public double[] prepareFeatVector(StateObservation sob) {
		assert sob instanceof StateObserverPoker : "Input 'so' is not of class StateObserverPoker";
		StateObserverPoker so = (StateObserverPoker) sob;

		// Pot
		// Cards
		switch (featMode) {
			case 0 -> {

				int numPlayers = so.getNumPlayers();

				double[] featureVector = new double[getInputSize(featMode)];

				// I think it makes sense to normalize the Feature Vector so that the active player is always at #0

				// 0 => amount to call
				// 1 => chips
				// 2 => potsize
				// 3,4 => hand cards in ascending order
				// 5,6,7 => community cards in ascending order
				// 8 ... 8+(n-2) => status of opponents

				int tmp = 0;
				featureVector[tmp++] = so.getOpenPlayer(so.getPlayer());
				featureVector[tmp++] = so.getChips()[so.getPlayer()];
				featureVector[tmp++] = so.getPotSize();

				//todo "sort" cards in order
				PlayingCard tmpCard;
				featureVector[tmp++] = ((tmpCard = so.getHoleCards()[0]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getHoleCards()[1]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getCommunityCards()[0]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getCommunityCards()[1]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getCommunityCards()[2]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getCommunityCards()[3]) != null) ? tmpCard.getId() : 0;
				featureVector[tmp++] = ((tmpCard = so.getCommunityCards()[4]) != null) ? tmpCard.getId() : 0;

				for (int i = 0; i < numPlayers; i++) {
					if(i != so.getPlayer())
						featureVector[tmp++] = so.getPlayerSate(i);
				}

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
				return 8 + numPlayers - 1;
			}
			case 1 -> throw new RuntimeException("Placeholder");
			default -> throw new RuntimeException("Unknown featmode: " + featmode);
		}
	}

}
