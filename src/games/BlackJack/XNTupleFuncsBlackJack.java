package games.BlackJack;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;

import controllers.TD.ntuple2.NTupleFactory;
import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;
import metadata.graphics.board.Board;

public class XNTupleFuncsBlackJack extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final int maxHandSize = 6;

    //assuming a hand in Blackjack has not more than 6 cards most of the time, 3 splits allowed, plus dealers hand

    @Override
    public int getNumCells() {
        return 3*maxHandSize+1;
    }

    // all card values Two...A = 13 cards + no card = 14 possible values
    @Override
    public int getNumPositionValues() {
        return (52/4)+1; // = 14
    }

    @Override
    public int getNumPlayers() {
        return BlackJackConfig.NUM_PLAYERS;
    }

    @Override
    public int getNumSymmetries() {
        return 1;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so) {
        // TODO Auto-generated method stub
        StateObserverBlackJack m_so = (StateObserverBlackJack) so.copy().partialState();
        int[] bvec = new int[getNumCells()];
        Arrays.fill(bvec, 13);

        if (m_so.getCurrentPlayer().hasHand()) {
            //sort hand of players and dealer to reduce symmetries to one

            for (Hand h : m_so.getCurrentPlayer().getHands()) {
                h.sortHand();
            }

            for (int j = 0; j < 3; j++) {
                int offSet = j * (maxHandSize);
                for (int f = 0; f < maxHandSize; f++) {
                    if (j < m_so.getCurrentPlayer().getHands().size()) {
                        if (f < m_so.getCurrentPlayer().getHands().get(j).getCards().size()) {
                            bvec[offSet + f] = m_so.getCurrentPlayer().getHands().get(j).getCards().get(f).rank.getSortValue();
                        }
                    }
                }
            }
            int offSet = (maxHandSize * 3);
            bvec[offSet] = m_so.getDealer().getActiveHand().getCards().get(0).rank.getSortValue();

        }

        return new BoardVector(bvec);
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        return new BoardVector[] {boardVector};
    }

    @Override
    public int[] symmetryActions(int actionKey) {
        // TODO Auto-generated method stub
        throw new RuntimeException("symmetryAction not implemented");
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        switch (mode) {
            //         Hand1        Hand2          Hand3       Dealers upcard
            // bvev = _ _ _ _ _ _ | _ _ _ _ _ _ | _ _ _ _ _ _ | _
            //        0 1 2 3 4 5   6 7 8 9 . .  12 ...   .... 18
            case 1:
                return new int[][]{
                        {1, 2, 18}, {6, 7, 18}, {12, 13, 18}
                };
            case 2:
                return new int[][]{
                        {1, 2, 3, 18}, {6, 7, 8, 18}, {12, 13, 14, 18}
                };
            case 3:
                return new int[][]{
                        {1, 2, 3, 4, 18}, {6, 7, 8, 9 ,18}, {12, 13, 14, 15, 18}
                };

        }
        return null;
    }

    @Override
    public String fixedTooltipString() {
        // use "<html> ... <br> ... </html>" to get multi-line tooltip text
        return "<html>" + "1: 40 best chosen 4-tuples" + "<br>" + "2: all the straight 3-tuples" + "</html>";
    }

    private static final int[] fixedModes = { 1, 2, 3};

    public int[] fixedNTupleModesAvailable() {
        return fixedModes;
    }

    @SuppressWarnings("rawtypes")
    public HashSet adjacencySet(int iCell) {
         //         Hand1        Hand2          Hand3       Dealers upcard
        // bvev = _ _ _ _ _ _ | _ _ _ _ _ _ | _ _ _ _ _ _ | _
        //        0 1 2 3 4 5   6 7 8 9 . . . ...
        HashSet<Integer> adjacencySet = new HashSet<>();
        //icell = dealers upcard
        if(iCell == maxHandSize*3) {
            //probebly not needed but if every cell got dealers upcard as neighbour, dealers upcard should have every
            //cell as neighbour
            for(int i = 0; i < maxHandSize*3 ; i++){
                adjacencySet.add(i);
            }
            return adjacencySet;

        }

        //any cell will have the other cells that build that hand as neighbour + dealers upcard
        int offSet = (iCell/maxHandSize) * maxHandSize;
        for (int i = offSet; i < offSet + maxHandSize; i++){
            if(i != iCell){
                adjacencySet.add(i);
            }
        }
        //add dealers upcard
        adjacencySet.add(maxHandSize*3);

        return adjacencySet;
    }

}
