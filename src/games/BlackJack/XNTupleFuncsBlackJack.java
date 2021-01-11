package games.BlackJack;

import java.io.Serializable;
import java.util.HashSet;

import games.BoardVector;
import games.StateObservation;
import games.XNTupleBase;
import games.XNTupleFuncs;

public class XNTupleFuncsBlackJack extends XNTupleBase implements XNTupleFuncs, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int getNumCells() {
        return 2;
    }

    @Override
    public int getNumPositionValues() {
        // TODO Auto-generated method stub
        return 52;
    }

    @Override
    public int getNumPlayers() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public int getNumSymmetries() {
        // TODO Auto-generated method stub
        return 5;
    }

    @Override
    public BoardVector getBoardVector(StateObservation so) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BoardVector[] symmetryVectors(BoardVector boardVector, int n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] symmetryActions(int actionKey) {
        // TODO Auto-generated method stub
        return null;
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

    private static final int[] fixedModes = { 1, 2 };

    public int[] fixedNTupleModesAvailable() {
        return fixedModes;
    }

    @SuppressWarnings("rawtypes")
    public HashSet adjacencySet(int iCell) {
        return null;
    }

}
