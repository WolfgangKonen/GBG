package games.Poker;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class XNTupleFuncsPokerSummary extends XNTuplePoker implements XNTupleFuncs, Serializable {

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


	/**
	 * This XNTupleFunction represents the current boardstate by summarizing the information and not showing all the details.
	 * For example instead of:<br>♥7/♦7/♦6/A♦/♣3/♠8/♥K<br> it would represent it by:
	 * <ul> <li>the High Card (A)</li>
	 * <li>number of suited cards: 3 (♦)</li>
	 * <li>number of connected cards: 3 (6,7,8)</li>
	 * <li>pairs: 7 </li>
	 * <li>...</li>
	 */
	public XNTupleFuncsPokerSummary(){

	}

	/**
	 * Representing the summary of the available cards as a hand.
	 */
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

	private Hand updateHand(PlayingCard[] cards){
		Hand hand = new Hand();
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
		return hand;
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

	@Override
	public int getNumCells() {
		return 6 + numPlayers - 1;
	}

	@Override
	public int getNumPositionValues() {
		return 6;
	}

	@Override
	public int[] getPositionValuesVector() {

		int[] positionValues = new int[getNumCells()];
		int i = 0;

		positionValues[i++] = 6;
		positionValues[i++] = 6;
		positionValues[i++] = 6;
		positionValues[i++] = 6;
		positionValues[i++] = 6;
		positionValues[i++] = 6;

		for (int x = 0; x < numPlayers-1; x++) {
			positionValues[i++] = 5;
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

		PlayingCard[] myCards = m_so.getHoleCards();          // what's my hand ?
		PlayingCard[] sharedCards = m_so.getCommunityCards(); // community cards?

		PlayingCard[] cards = new PlayingCard[myCards.length+sharedCards.length];
		cards[0] = myCards[0];
		cards[1] = myCards[1];

		for (int i = 0;i<sharedCards.length;i++) {
			cards[2+i] =  sharedCards[i];
		}

		Hand hand = updateHand(cards);

		int tmp = 0;

		// number of connected cards
		if(hand.connected>5)
			bvec[tmp++] = 5;
		else
			bvec[tmp++] = hand.connected;

		// number of suited cards
		if(hand.suited>5)
			bvec[tmp++] = 5;
		else
			bvec[tmp++] = hand.suited;

		// high card
		if(hand.high>10) {
			bvec[tmp++] = 4;
		}else if(hand.high>7){
			bvec[tmp++] = 3;
		}else if(hand.high>4){
			bvec[tmp++] = 2;
		}else{
			bvec[tmp++] = 1;
		}

		// pairs
		if(hand.pairs.size()>0){
			if(hand.pairs.size()>1) {
				bvec[tmp++] = 5;
			}else{
				if(hand.pairs.get(0)>10) {
					bvec[tmp++] = 4;
				}else if(hand.pairs.get(0)>7){
					bvec[tmp++] = 3;
				}else if(hand.pairs.get(0)>4){
					bvec[tmp++] = 2;
				}else{
					bvec[tmp++] = 1;
				}
			}
		}else{
			bvec[tmp++] = 0;
		}

		// triple
		if(hand.triples.size()>0){
			if(hand.triples.get(0)>10) {
				bvec[tmp++] = 5;
			}else if(hand.triples.get(0)>7){
				bvec[tmp++] = 4;
			}else if(hand.triples.get(0)>4){
				bvec[tmp++] = 3;
			}else if(hand.triples.get(0)>2){
				bvec[tmp++] = 2;
			}else{
				bvec[tmp++] = 1;
			}
		}else{
			bvec[tmp++] = 0;
		}

		// four of a kind
		if(hand.four.size()>0){
			if(hand.four.get(0)>10) {
				bvec[tmp++] = 5;
			}else if(hand.four.get(0)>7){
				bvec[tmp++] = 4;
			}else if(hand.four.get(0)>4){
				bvec[tmp++] = 3;
			}else if(hand.four.get(0)>2){
				bvec[tmp++] = 2;
			}else{
				bvec[tmp++] = 1;
			}
		}else{
			bvec[tmp++] = 0;
		}

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
		int[][] all = new int[][]{
				{0,1,2,3,4,5,6}
		};

		switch (mode) {
			case 1:    return all;
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

	@Override
	public String getDescription(){
		return "Three tuples with: " +
				"{ Wholecards }," +
				"{ River }," +
				"{ Blind + Turn }" +
				"";
	}


}