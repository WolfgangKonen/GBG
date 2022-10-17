package ludiiInterface.games.Nim;

import games.Nim.NimConfig;
import games.Nim.StateObserverNim;
import other.context.Context;
import other.move.Move;

import java.util.List;

public class StateObserverNimTranslationLayer extends StateObserverNim {

    private Context ludiiContext;
    private int playerID, number_heaps;
    private SystemConversionNim conversion;

    public StateObserverNimTranslationLayer(Context ludiiContext, int playerID, int number_heaps){

        m_heap = new int[number_heaps];

        NimConfig.NUMBER_HEAPS = number_heaps;
        NimConfig.HEAP_SIZE = -1;
        NimConfig.MAX_MINUS = number_heaps;

        if(NimConfig.NUMBER_HEAPS == 3){ // the 3 heap option in Ludii is set up differently than the others
            m_heap[0] = 1;
            m_heap[1] = 2;
            m_heap[2] = 1;
        }
        else {
            int k = NimConfig.NUMBER_HEAPS/2;
            for (int i=0; i<NimConfig.NUMBER_HEAPS; i++) {
                if (k > 0 || k == 0) {
                    m_heap[i] = NimConfig.NUMBER_HEAPS - k;
                } else {
                    m_heap[i] = NimConfig.NUMBER_HEAPS + k;
                }
                k--;
            }
        }
        m_player = 0;

        setAvailableActions();

        this.ludiiContext = ludiiContext;
        this.playerID = playerID;
        this.number_heaps = number_heaps;
        this.conversion = new SystemConversionNim(number_heaps);

        updateGameState();
    }


    private void updateGameState(){

        //List of all moves made in Ludii Context
        List<Move> ludiiContextMoves = ludiiContext.trial().generateCompleteMovesList();

        for(Move x : ludiiContextMoves){

            if(x.isPass()){
                int mover = ludiiContext.trial().numTurns() % 2 == 1 ? 1 : 2;  // using number of turns so far to determine who made the move
                m_player = (conversion.getGBGPlayerFromLudii(mover) == 0 ? 1 : 0);
            } else
                advanceGameState(x);
        }

        setAvailableActions();
    }

    private void advanceGameState(Move move){

        int actionInt = conversion.getGBGIndexFromLudii(move.actions().get(0).to());
        int j = actionInt%NimConfig.MAX_MINUS;
        int heap = (actionInt-j)/NimConfig.MAX_MINUS;
        int subtractor = j+1;

        m_heap[heap] -= subtractor;

        int mover = ludiiContext.trial().numTurns() % 2 == 1 ? 1 : 2;  // using number of turns so far to determine who made the move
        m_player = (conversion.getGBGPlayerFromLudii(mover) == 0 ? 1 : 0);

        super.incrementMoveCounter();

    }
}
