package games.Poker;

import games.BlackJack.BlackJackConfig;
import games.BlackJack.Hand;
import games.BlackJack.Player;
import games.BlackJack.StateObserverBlackJack;
import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class XNTupleFuncsPokerRanksNew extends XNTupleBase implements XNTupleFuncs, Serializable {

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .gamelog or .agt.zip containing this object will
	 * become unreadable or you have to provide a special version transformation)
	 * <p>
	 * [We need this strange number here, because serialVersionUID was not present before,
	 * and the so far stored agents had this automatically created serialVersionUID.]
	 */
	private static final long serialVersionUID = 7556763505414386566L;

	private final int numPlayers = PokerConfig.NUM_PLAYERS;
	private final int startChips = PokerConfig.START_CHIPS;

	private Hand hand;

	private class Hand {

		int high;
		int suited;
		int connected;
		ArrayList<Integer> pairs;
		ArrayList<Integer> triples;
		ArrayList<Integer> four;

		boolean fullHouse;
		boolean flush;
		boolean straight;

		Hand(){

		}
	}

	private void updateHand(PlayingCard[] cards){
		int[] suits = new int[4];
		int[] ranks = new int[13];

		int max = 0;

		for(PlayingCard c : cards){
			if(c == null)
				break;
			suits[c.getSuit()]++;
			ranks[c.getRank()]++;

			if(c.getRank()>max)
				max = c.getRank();
		}

		hand.high = max;

		// update amount of cards with the same suit
		int maxSuits = 0;
		for(int val:suits){
			if(maxSuits<val)
				maxSuits=val;
		}
		hand.suited = maxSuits;

		// update amount of conneceted cards;
		int connected = 0;
		int maxConnected = 0;
		if(ranks[12]>0)
			connected++;
		for(int i = 0;i<13;i++){
			if(ranks[i]>0) {
				connected++;
				if(maxConnected<connected)
					maxConnected = connected;
			} else {
				connected = 0;
			}
		}
		hand.connected = maxConnected;

		// check for pairs
		ArrayList[] multiples = checkForMultiples(ranks);
		hand.pairs = multiples[0];
		hand.triples = multiples[1];
		hand.four = multiples[2];

		hand.straight = connected > 4;
		hand.flush = maxSuits > 4;
		if(hand.triples.size()>0) {
			hand.pairs.remove(Integer.valueOf(hand.triples.get(0)));
			if (hand.pairs.size()>0){
				hand.fullHouse = true;
			}
			hand.pairs.add(hand.triples.get(0));
		}
	}

	private ArrayList<Integer>[] checkForMultiples(int[] ranks){
		ArrayList<Integer>[] multiples = new ArrayList[3];
		multiples[0] = new ArrayList<>(); // pairs
		multiples[1] = new ArrayList<>(); // three of a kind
		multiples[2] = new ArrayList<>(); // four  of a kind

		for(int i = 12;i>=0;i--){
			if(ranks[i]>=2){
				multiples[0].add(i);
				if(ranks[i]>=3){
					multiples[1].add(i);
					if(ranks[i]>=3){
						multiples[2].add(i);
					}
				}
			}
		}
		return multiples;
	}

	public XNTupleFuncsPokerRanksNew() {

	}

	@Override
	public int getNumCells() {
		return 13 + numPlayers - 1;
	}


	@Override
	public int getNumPositionValues() {
		return 5;
	}


	@Override
	public int[] getPositionValuesVector() {

		int[] positionValues = new int[getNumCells()];
		int i = 0;

		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;
		positionValues[i++] = 5;

		for (int x = 0; x < numPlayers-1; x++) {
			positionValues[i++] = 4;
		}
		return positionValues;
	}

	@Override
	public int getNumPlayers() {
		return PokerConfig.NUM_PLAYERS;
	}

	@Override
	public int getNumSymmetries() {
		return 1;
	}

	@Override
	public BoardVector getBoardVector(StateObservation so) {
		StateObserverPoker m_so = (StateObserverPoker) so.copy();
		int[] bvec = new int[getNumCells()];


		int[] suits = new int[4];
		int[] ranks = new int[13];

		for(PlayingCard c : m_so.getHoleCards()){
			if(c == null)
				break;
			suits[c.getSuit()]++;
			ranks[c.getRank()]++;
		}

		for(PlayingCard c : m_so.getCommunityCards()){
			if(c == null)
				break;
			suits[c.getSuit()]++;
			ranks[c.getRank()]++;
		}

		int tmp = 0;

		// Ranks
		for(int i = 0;i<ranks.length;i++)
			bvec[tmp++] = ranks[i];

		for (int i = 0; i < numPlayers; i++) {
			if(i != so.getPlayer())
				bvec[tmp++] = (int) m_so.getPlayerSate(i);
		}

		return new BoardVector(bvec);
	}



	@Override
	public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
		return new BoardVector[] {boardVector};
	}

	@Override
	public int[] symmetryActions(int actionKey) {
		return new int[] {actionKey};       // /WK/ default implementation for 'no symmetries' (except self)
	}

	@Override
	public int[][] fixedNTuples(int mode) {
		int[] all = new int[getNumCells()];
		for(int x=0;x<all.length;x++)
			all[x]=x;

		int[][] slim = new int[][]{
				//{0,1,2,3,4},
				//{5,6,7,8,9},
				{2,3,4,5,6,7,8,9,10,11,12,13}
		};

		switch (mode) {
			// 0 => amount to call
			// 1 => chips
			// 2 => potsize
			// 3,4 => hand cards in ascending order
			// 5,6,7 => community cards in ascending order
			// 8 ... 8+(n-2) => status of opponents

			case 1:    return slim;
		}
		return null;
	}

	@Override
	public String fixedTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>" + "1: 40 best chosen 4-tuples" + "<br>" + "2: all the straight 3-tuples" + "</html>";
	}

	private static final int[] fixedModes = {1};

	public int[] fixedNTupleModesAvailable() {
		return fixedModes;
	}

	@Override
	public HashSet adjacencySet(int iCell) {
		HashSet<Integer> adjacencySet = new HashSet<>();
		for(int x=0;x<getNumCells();x++)
			adjacencySet.add(x);
		return adjacencySet;
	}


}