package games.Poker;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class XNTupleFuncsPokerAbstract extends XNTuplePoker implements XNTupleFuncs, Serializable {

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
	 * </ul>
	 */
	public XNTupleFuncsPokerAbstract(){

	}

	@Override
	public int getNumCells() {
		return 14;
	}

	@Override
	public int getNumPositionValues() {
		return 6;
	}

	@Override
	public int[] getPositionValuesVector() {

		int[] positionValues = new int[getNumCells()];
		int i = 0;

		positionValues[i++] = 5; // 0: 2
		positionValues[i++] = 5; // 1: 3
		positionValues[i++] = 5; // 2: 4
		positionValues[i++] = 5; // 3: 5
		positionValues[i++] = 5; // 4: 6
		positionValues[i++] = 5; // 5: 7
		positionValues[i++] = 5; // 6: 8
		positionValues[i++] = 5; // 7: 9
		positionValues[i++] = 5; // 8: 10
		positionValues[i++] = 5; // 9: J
		positionValues[i++] = 5; // 10: Q
		positionValues[i++] = 5; // 11: K
		positionValues[i++] = 5; // 12: A

		positionValues[i++] = 6; // 13: Suited

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
		int tmp = 0;

		int[] suited = new int[4];

		for(PlayingCard c : m_so.getHoleCards()){
			if(c == null) {
				break;
			} else {
				bvec[c.getRank()]++;
				suited[c.getSuit()]++;
			}
		}

		for(PlayingCard c : m_so.getCommunityCards()){
			if(c == null) {
				break;
			} else {
				bvec[c.getRank()]++;
				suited[c.getSuit()]++;
			}
		}

		int maxSuit = 0;
		for(int i = 0;i<suited.length;i++){
			if(suited[i]>maxSuit)
				maxSuit=suited[i];
		}
		if(maxSuit>5)
			maxSuit=5;

		bvec[13] = maxSuit;

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
				{0,1,2,3,4, 13},
				{1,2,3,4,5, 13},
				{2,3,4,5,6, 13},
				{3,4,5,6,7, 13},
				{4,5,6,7,8, 13},
				{5,6,7,8,9, 13},
				{6,7,8,9,10, 13},
				{7,8,9,10,11, 13},
				{8,9,10,11,12, 13}
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
		return "Representing the board as an overview about number of cards per rank and the number of suited cards." +
				"";
	}

}