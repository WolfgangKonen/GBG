package games.Poker;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsPokerSimple extends XNTuplePoker implements XNTupleFuncs, Serializable {

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


	@Override
	public int getNumCells() {
		return 7;
	}

	@Override
	public int getNumPositionValues() {
		return 52;
	}

	@Override
	public int[] getPositionValuesVector() {

		int[] positionValues = new int[getNumCells()];

		int i = 0;

		// Whole Cards
		positionValues[i++] = 52;
		positionValues[i++] = 52;

		// Blind
		positionValues[i++] = 52;
		positionValues[i++] = 52;
		positionValues[i++] = 52;

		// River
		positionValues[i++] = 52;

		// Turn
		positionValues[i++] = 52;

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

		for(PlayingCard c : m_so.getHoleCards()){
			if(c == null) {
				bvec[tmp++] = 0;
			} else {
				bvec[tmp++] = c.getRank();
			}
		}

		for(PlayingCard c : m_so.getCommunityCards()){
			if(c == null) {
				bvec[tmp++] = 0;
			} else {
				bvec[tmp++] = c.getRank();
			}
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
		int[][] slim = new int[][]{
				{0,1},
				{2,3,4},
				{5,6}
		};

		switch (mode) {
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

	@Override
	public String getDescription(){
		return "Three tuples with: " +
				"{ Wholecards }," +
				"{ River }," +
				"{ Blind + Turn }" +
				"";
	}
}