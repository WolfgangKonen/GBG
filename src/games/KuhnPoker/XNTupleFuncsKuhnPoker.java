package games.KuhnPoker;

import games.BoardVector;
import games.Poker.PokerConfig;
import games.Poker.StateObserverPoker;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

import java.io.Serializable;
import java.util.HashSet;

public class XNTupleFuncsKuhnPoker extends XNTupleBase implements XNTupleFuncs, Serializable {


	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .gamelog or .agt.zip containing this object will
	 * become unreadable or you have to provide a special version transformation)
	 * <p>
	 * [We need this strange number here, because serialVersionUID was not present before,
	 * and the so far stored agents had this automatically created serialVersionUID.]
	 */
	private static final long serialVersionUID = 7556763505414386566L;

	private final int numPlayers = KuhnPokerConfig.NUM_PLAYERS;
	private final int startChips = KuhnPokerConfig.START_CHIPS;

	public XNTupleFuncsKuhnPoker() {

	}

	@Override
	public int getNumCells() {
		return 4;
	}


	@Override
	// Do I need the generic position values?
	public int getNumPositionValues() {
		return 8;
	}


	@Override
	public int[] getPositionValuesVector() {
		//
		// 0 -> whole card
		// 1 -> first move
		// 2 -> second move
		// 3 -> third move

		int[] positionValues = new int[getNumCells()];
		int i = 0;

		//wholecard
		positionValues[0] = 3;

		//moves
		positionValues[1]=5;
		positionValues[2]=5;
		positionValues[3]=5;

		return positionValues;
	}

	@Override
	public int getNumPlayers() {
		return KuhnPokerConfig.NUM_PLAYERS;
	}

	@Override
	public int getNumSymmetries() {
		return 1;
	}

	@Override
	public BoardVector getBoardVector(StateObservation so) {
		// Values:
		// Wholecard: 0,10,11,12
		// moves: 0, 1,2,3,4

		StateObserverKuhnPoker m_so = (StateObserverKuhnPoker) so.copy();
		int[] bvec = new int[getNumCells()];

		int player = m_so.getPlayer();

		//wholecard
		PlayingCard pc = m_so.getHoleCards(so.getPlayer())[0];

		bvec[0] = pc==null?0:pc.getRank();

		//moves
		bvec[1]=0;
		bvec[2]=0;
		bvec[3]=0;
		for(int i = 0;i<m_so.getLastRoundMoves().toArray().length;i++)
			bvec[i+1] = m_so.getLastRoundMoves().get(i)==null?0:m_so.getLastRoundMoves().get(i)+1;

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

		switch (mode) {

			case 1:
				return new int[][]{
						all
				};
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
