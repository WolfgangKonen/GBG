package games;

import controllers.TD.ntuple2.NTuple2ValueFunc;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.TDNTuple2Agt;

import java.util.HashSet;


/**
 * Interface for the n-tuple implementation in {@link TDNTuple2Agt} and 
 * {@link NTuple2ValueFunc}. Specifies game-specific functions for producing a 
 * board vector, symmetric board vectors (if any) and a fixed n-tuple set. <p>
 * 
 * Note: The six methods {@link #getNumCells()}, {@link #getNumPositionValues()}, 
 * {@link #getBoardVector(StateObservation)}, {@link #symmetryVectors(int[])}, 
 * {@link #fixedNTuples(int)} and {@link #adjacencySet(int)} are only required for the 
 * n-tuple interface. 
 * If an implementing game does not need that part (i. e. if it does not plan to 
 * use {@link TDNTuple2Agt}), it may just code stubs returning 0, {@code null}, 
 * or throwing a {@link RuntimeException}.
 *
 * @author Wolfgang Konen, TH Köln, Feb'17
 */
public interface XNTupleFuncs {
	/**
	 * @return the number of board cells
	 */
	public int getNumCells();
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have (e. g. P=3 for TicTacToe with 0:"O", 1=empty, 2="X") 
	 */
	public int getNumPositionValues();
	
	/**
	 * @return the number of players in this game 
	 */
	public int getNumPlayers();
	
	/**
	 * @param so the state
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value 0, 1, 2,..., P-1 in state {@code so}.
	 */
	public int[] getBoardVector(StateObservation so);

	/**
	 * Given a board vector and given that the game has s symmetries,  
	 * return an array which holds s symmetric board vectors: 
	 * <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other s-1 rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * Symmetric board vectors are board vectors with the same value and with equivalent best
	 * next actions. For example, TicTacToe has 8 symmetries: 4 rotations (0°, 90°, 180°, 270°)
	 * times 2 mirror reflections.
	 * 
	 * @param boardVector e.g. from {@link #getBoardVector(StateObservation)}
	 * @return a (s x length(boardVector))-matrix 
	 */
	public int[][] symmetryVectors(int[] boardVector);
	
	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, 
	 * generate the array of equivalent action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed for Q-learning and Sarsa.
	 * 
	 * @param actionKey
	 * 				the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b>
	 * 				array of the equivalent actions' keys. 
	 * <p>
	 * equivAction[i] is the key of the action equivalent to actionKey in the
	 * i'th equivalent board vector equiv[i] = {@link #symmetryVectors(int[])}[i]
	 */
	public int[] symmetryActions(int actionKey);
	
	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different lengths. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.<p>
	 * 
	 * Other options than fixed n-tuples (that is, the generation of random n-tuples) are 
	 * provided by {@link NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)}
	 * 
	 * @param mode one of the values from {@link #getAvailFixedNTupleModes()}
	 * @return nTuples[numTuples][]
	 * 
	 * @see NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)
	 */
	public int[][] fixedNTuples(int mode);
	
	public int[] getAvailFixedNTupleModes();

	/**
	 * Return all neighbors of {@code iCell}
	 * 
	 * @param iCell
	 * @return a list of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	public HashSet adjacencySet(int iCell);
}
