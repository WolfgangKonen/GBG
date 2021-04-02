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
        return 10;
    }

    // no hand = 0 | handvalue from 4 to 21 = 1 to 18 | handvalue > 21 = 19
    @Override
    public int getNumPositionValues() {
        return 22;
    }


    @Override
    public int[] getPositionValuesVector() {
       return new int[]{
               22, 2, 2, 22, 2, 2, 22, 2, 2, 22
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
        //                Hand1                        Hand2                  Hand3
        //       Handvalue  isSoft  Split  Hanvalue  isSoft  Split     Handvalue  isSoft  Split      Dealers handvalue
        // bvev =    _        _       _   |   _        _       _   |        _       _       _   |       _
        //           0        1       2       3        4       5            6       7       8           9
        Player currentPlayer = m_so.getCurrentPlayer();
        if (currentPlayer.hasHand()) {
            //sort hand of players and dealer to reduce symmetries to one

            for (int j = 0; j < 9; j = j + 3) {
                if(j/2 < currentPlayer.getHands().size()) {
                    Hand currentHand = currentPlayer.getHands().get(j / 3);
                    if (currentHand.getHandValue() <= 21) {
                        //smallest handvalue for a player with 2 cards is 4
                        //however if a hand gets split the 2nd hand has only one card until it gets played
                        //so the smallest hand for a player can be 2. We substract 1 from the handvalue
                        bvec[j] = currentHand.getHandValue() - 1;
                        if(currentHand.isSoft())
                            bvec[j + 1] = 1;
                        if(currentHand.isPair() && currentPlayer.getHands().size() < 3 &&
                                currentPlayer.getChips() > currentPlayer.betOnActiveHand())
                            bvec[j + 2] = 1;
                    }
                    else{
                        bvec[j] = 21;
                    }

                }
            }

            //smallest handvalue for dealer is 2 when he has one unknown card so we only substract 1
            bvec[9] = 21;
            if(m_so.getDealer().getActiveHand().getHandValue() <= 21) {
                bvec[9] = m_so.getDealer().getActiveHand().getHandValue() - 1;
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
        switch (mode) {
            //                Hand1                        Hand2                  Hand3
            //       Handvalue  isSoft  Split  Hanvalue  isSoft  Split     Handvalue  isSoft  Split      Dealers upcard
            // bvev =    _        _       _   |   _        _       _   |        _       _       _   |       _
            //           0        1       2       3        4       5            6       7       8           9
            case 1:
                return new int[][]{
                        {0, 1, 2, 9}, {3, 4, 5, 9}, {6, 7, 8, 9}
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
        //                Hand1                        Hand2                  Hand3
        //       Handvalue  isSoft  Split  Hanvalue  isSoft  Split     Handvalue  isSoft  Split      Dealers upcard
        // bvev =    _        _       _   |   _        _       _   |        _       _       _   |       _
        //           0        1       2       3        4       5            6       7       8           9
        HashSet<Integer> adjacencySet = new HashSet<>();
        //icell = dealers upcard
        if(iCell == 9) {
            //probebly not needed but if every cell got dealers upcard as neighbour, dealers upcard should have every
            //cell as neighbour
            for(int i = 0; i < 9 ; i++){
                adjacencySet.add(i);
            }
            return adjacencySet;

        }

        //any hand has value and if its soft or not (usable ace)
        int offSet = (iCell/3) * 3;
        for (int i = offSet; i < offSet + 3; i++){
            if(i != iCell){
                adjacencySet.add(i);
            }
        }
        //add dealers upcard
        adjacencySet.add(9);

        return adjacencySet;
    }

}
