package games.BlackJack;

import java.io.Serializable;
import java.util.HashSet;
import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsBlackJackSimple extends XNTupleBase implements XNTupleFuncs, Serializable {


    public XNTupleFuncsBlackJackSimple() {
    }

    private static final long serialVersionUID = 1L;


    @Override
    public int getNumCells() {
        return 7;
    }

    // no hand = 0 | handvalue from 4 to 21 = 1 to 18
    @Override
    public int getNumPositionValues() {
        return 19;
    }


    @Override
    public int[] getPositionValuesVector() {
       return new int[]{
               19, 2, 19, 2, 19, 2, 11
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
        //                Hand1                  Hand2                  Hand3
        //       Handvalue     isSoft   Hanvalue     isSoft     Handvalue     isSoft      Dealers upcard
        // bvev =    _          _     |     _           _    |      _          _      |       _
        //           0          1           2           3           4          5              6
        Player currentPlayer = m_so.getCurrentPlayer();
        if (currentPlayer.hasHand()) {
            //sort hand of players and dealer to reduce symmetries to one

            for (int j = 0; j < 6; j = j + 2) {
                if(j/2 < currentPlayer.getHands().size()) {
                    Hand currentHand = currentPlayer.getHands().get(j / 2);
                    if (currentHand.getHandValue() <= 21) {
                        bvec[j] = currentHand.getHandValue() - 3;
                        if (currentHand.isSoft())
                            bvec[j + 1] = 1;
                    }

                }
            }
            m_so.getDealer().getActiveHand().getCards().remove(1);
            bvec[6] = m_so.getDealer().getActiveHand().getCards().get(0).rank.getValue();

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
            //                Hand1                  Hand2                  Hand3
            //       Handvalue     isSoft   Hanvalue     isSoft     Handvalue     isSoft      Dealers upcard
            // bvev =    _          _     |     _           _    |      _          _      |       _
            //           0          1           2           3           4          5              6
            case 1:
                return new int[][]{
                        {0, 1, 6}, {2, 3, 6}, {4, 5, 6}
                };
            case 2:
                return new int[][]{
                        {0, 1}, {2, 3}, {4, 5}, {6}
                };
        }
        return null;
    }

    @Override
    public String fixedTooltipString() {
        // use "<html> ... <br> ... </html>" to get multi-line tooltip text
        return "<html>" + "1: 40 best chosen 4-tuples" + "<br>" + "2: all the straight 3-tuples" + "</html>";
    }

    private static final int[] fixedModes = {1, 2};

    public int[] fixedNTupleModesAvailable() {
        return fixedModes;
    }

    @SuppressWarnings("rawtypes")
    public HashSet adjacencySet(int iCell) {
        //                Hand1                  Hand2                  Hand3
        //       Handvalue     isSoft   Hanvalue     isSoft     Handvalue     isSoft      Dealers upcard
        // bvev =    _          _     |     _           _    |      _          _      |       _
        //           0          1           2           3           4          5              6
        HashSet<Integer> adjacencySet = new HashSet<>();
        //icell = dealers upcard
        if(iCell == 6) {
            //probebly not needed but if every cell got dealers upcard as neighbour, dealers upcard should have every
            //cell as neighbour
            for(int i = 0; i < 6 ; i++){
                adjacencySet.add(i);
            }
            return adjacencySet;

        }

        //any hand has value and if its soft or not (usable ace)
        int offSet = (iCell/2) * 2;
        for (int i = offSet; i < offSet + 2; i++){
            if(i != iCell){
                adjacencySet.add(i);
            }
        }
        //add dealers upcard
        adjacencySet.add(6);

        return adjacencySet;
    }

}
