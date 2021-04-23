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
import java.util.HashSet;

public class XNTupleFuncsPoker extends XNTupleBase implements XNTupleFuncs, Serializable {

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
	public XNTupleFuncsPoker() {

    }

	@Override
	public int getNumCells() {
		return 7 + numPlayers - 1;
	}


	@Override
	public int getNumPositionValues() {
		return startChips*numPlayers;
	}


	@Override
	public int[] getPositionValuesVector() {
		// I think it makes sense to normalize the Feature Vector so that the active player is always at #0

		// 0 => amount to call // leave out
		// 1 => chips // leave out
		// 2 => potsize // leave out
		// 3,4 => hand cards in ascending order
		// 5,6,7,8,9 => community cards in ascending order
		// 10 ... 10+(n-2) => status of opponents
		int[] positionValues = new int[getNumCells()];
		int i = 0;
		//positionValues[i++] = startChips*numPlayers;
		//positionValues[i++] = startChips*numPlayers;
		//positionValues[i++] = startChips*numPlayers;
		positionValues[i++] = 52;
		positionValues[i++] = 52;
		positionValues[i++] = 52;
		positionValues[i++] = 52;
		positionValues[i++] = 52;
		positionValues[i++] = 52;
		positionValues[i++] = 52;

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


		int player = m_so.getPlayer();

		int tmp = 0;
		//bvec[tmp++] = (int) m_so.getOpenPlayer(so.getPlayer());
		//bvec[tmp++] = (int) m_so.getChips()[so.getPlayer()];
		//bvec[tmp++] = m_so.getPotSize();

		//todo "sort" cards in order
		PlayingCard tmpCard;
		bvec[tmp++] = ((tmpCard = m_so.getHoleCards()[0]) != null) ? tmpCard.getId() : 0;
		bvec[tmp++] = ((tmpCard = m_so.getHoleCards()[1]) != null) ? tmpCard.getId() : 0;
		bvec[tmp++] = ((tmpCard = m_so.getCommunityCards()[0]) != null) ? tmpCard.getId() : 0;
		bvec[tmp++] = ((tmpCard = m_so.getCommunityCards()[1]) != null) ? tmpCard.getId() : 0;
		bvec[tmp++] = ((tmpCard = m_so.getCommunityCards()[2]) != null) ? tmpCard.getId() : 0;
		bvec[tmp++] = ((tmpCard = m_so.getCommunityCards()[3]) != null) ? tmpCard.getId() : 0;
		bvec[tmp++] = ((tmpCard = m_so.getCommunityCards()[4]) != null) ? tmpCard.getId() : 0;

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

		switch (mode) {
			// 0 => amount to call
			// 1 => chips
			// 2 => potsize
			// 3,4 => hand cards in ascending order
			// 5,6,7 => community cards in ascending order
			// 8 ... 8+(n-2) => status of opponents

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
