package games.BlackJack;

import java.io.Serializable;
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
    private final int maxHandSize = 11; //is 11 really needed?

    //assuming a hand in Blackjack has at most 6 cards, 3 splits allowed, plus dealers hand
    // possible biggest hand A + A + A + A + A + A + A + A + A + A + A = 11 cards
    @Override
    public int getNumCells() {
        return getNumPlayers()*3*maxHandSize+maxHandSize;
    }

    // all card values Two...A = 13 cards + uknown card + no card = 15 possible values
    @Override
    public int getNumPositionValues() {
        return (52/4)+1+1; // = 15
    }

    @Override
    public int getNumPlayers() {
        // TODO Auto-generated method stub
        return BlackJackConfig.NUM_PLAYERS;
    }

    @Override
    public int getNumSymmetries() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so) {
        // TODO Auto-generated method stub
        StateObserverBlackJack m_so = (StateObserverBlackJack) so.copy().partialState();
        int[] bvec = new int[getNumCells()];
        //sort hand of players and dealer to reduce symmetries to zero
        for(Player p : m_so.getPlayers()){
            for(Hand h : p.getHands()){
                h.sortHand();
            }
        }
        if(m_so.getDealer().hasHand())
            m_so.getDealer().getActiveHand().sortHand();

        for(int i = 0; i < m_so.getNumPlayers(); i++){
            for(int j = 0; j < 3; j++){
                int offSet = i * 3 * maxHandSize + j*maxHandSize;
                for(int f = 0; f < maxHandSize; f++){
                    if(j < m_so.getPlayers()[i].getHands().size()) {
                        if (f < m_so.getPlayers()[i].getHands().get(j).getCards().size()) {
                            bvec [offSet + f] = m_so.getPlayers()[i].getHands().get(j).getCards().get(f).rank.getSortValue();
                        }
                        else{
                            bvec [offSet + f] = 14;
                        }
                    }else{
                        bvec [offSet + f] = 14;
                    }
                }
            }
        }
        int offSet = (getNumPlayers()*maxHandSize*3);
        for(int i = 0; i < maxHandSize; i++){
            if(m_so.getDealer().hasHand()){
                if(i < m_so.getDealer().getActiveHand().size()){
                    bvec [offSet + i] = m_so.getDealer().getActiveHand().getCards().get(i).rank.getSortValue();
                }
                else {
                    bvec [offSet + i] = 14;
                }
            }
            else{
                bvec [offSet + i] = 14;
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
        // TODO Auto-generated method stub
        throw new RuntimeException("symmetryAction not implemented");
    }

    @Override
    public int[][] fixedNTuples(int mode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String fixedTooltipString() {
        // use "<html> ... <br> ... </html>" to get multi-line tooltip text
        return "<html>" + "1: 40 best chosen 4-tuples" + "<br>" + "2: all the straight 3-tuples" + "</html>";
    }

    private static final int[] fixedModes = { 1 };

    public int[] fixedNTupleModesAvailable() {
        return fixedModes;
    }

    @SuppressWarnings("rawtypes")
    public HashSet adjacencySet(int iCell) {
        HashSet<Integer> adjacencySet = new HashSet<>();
        int x1 = iCell -1;
        int x2 = iCell +1;
        if(x1 >= 0 && x1 <= getNumCells() && iCell/maxHandSize == x1/maxHandSize){
            adjacencySet.add(x1);
        }
        if(x2 >= 0 && x2 <= getNumCells() && iCell/maxHandSize == x2/maxHandSize){
            adjacencySet.add(x2);
        }
        return adjacencySet;
    }

}
