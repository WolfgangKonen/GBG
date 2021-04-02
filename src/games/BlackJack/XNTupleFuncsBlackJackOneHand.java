package games.BlackJack;

import java.io.Serializable;
import java.util.HashSet;
import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsBlackJackOneHand extends XNTupleBase implements XNTupleFuncs, Serializable {

    public XNTupleFuncsBlackJackOneHand() {
    }

    private static final long serialVersionUID = 1L;


    @Override
    public int getNumCells() {
        return 3;
    }

    // no hand = 0 | handvalue from 4 to 21 = 1 to 18
    @Override
    public int getNumPositionValues() {
        return 22;
    }


    @Override
    public int[] getPositionValuesVector() {
        return new int[]{
                22, 2, 22
        };
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
        StateObserverBlackJack m_so = (StateObserverBlackJack) so.copy();
        int[] bvec = new int[getNumCells()];
        //                activehand
        //       Handvalue     isSoft     Dealers handvalue
        // bvev =    _          _     |        _
        //           0          1              2
        Player currentPlayer = m_so.getCurrentPlayer();
        if (currentPlayer.getActiveHand() != null) {
            //sort hand of players and dealer to reduce symmetries to one
            bvec[0] = 21;
            if (currentPlayer.getActiveHand().getHandValue() <= 21) {
                bvec[0] = currentPlayer.getActiveHand().getHandValue() - 1;
                bvec[1] = currentPlayer.getActiveHand().isSoft() ? 1 : 0;
            }
            bvec[2] = 21;
           if(m_so.getDealer().getActiveHand().getHandValue() <= 21) {
               bvec[2] = m_so.getDealer().getActiveHand().getHandValue() - 1;
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
        return new int[]{actionKey};
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        switch (mode) {
            //                activehand
            //       Handvalue     isSoft     Dealers upcard
            // bvev =    _          _     |        _
            //           0          1              2
            case 1:
                return new int[][]{
                        {0, 1, 2}
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

    @SuppressWarnings("rawtypes")
    public HashSet adjacencySet(int iCell) {
        //                activehand
        //       Handvalue     isSoft     Dealers upcard
        // bvev =    _          _     |        _
        //           0          1              2
        HashSet<Integer> adjacencySet = new HashSet<>();
        //icell = dealers upcard
        if(iCell == 2) {
            //probebly not needed but if every cell got dealers upcard as neighbour, dealers upcard should have every
            //cell as neighbour
                adjacencySet.add(0);
                adjacencySet.add(1);

            return adjacencySet;

        }

        if(iCell == 0)
            adjacencySet.add(1);
        else
            adjacencySet.add(2);

        //add dealers upcard
        adjacencySet.add(2);

        return adjacencySet;
    }

}
