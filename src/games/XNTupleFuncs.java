package games;

import controllers.TD.ntuple2.NTuple2ValueFunc;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.SarsaAgt;

import java.util.HashSet;

import agentIO.LoadSaveGBG;
import controllers.TD.ntuple2.TDNTuple3Agt;
import controllers.TD.ntuple4.Sarsa4Agt;
import controllers.TD.ntuple4.TDNTuple4Agt;


/**
 * Interface for the n-tuple implementation in {@link TDNTuple3Agt}, {@link TDNTuple4Agt}, {@link SarsaAgt},
 * {@link Sarsa4Agt} and {@link NTuple2ValueFunc}. Specifies game-specific functions for producing
 * board vectors, symmetric board vectors (if any) and a fixed n-tuple set. <p>
 * 
 * Note: The methods available in this interface are only required for the 
 * n-tuple interface. 
 * If an implementing game does not need that part (i. e. if it does not plan to 
 * use {@link TDNTuple3Agt}, ...), it may just code stubs returning 0, {@code null},
 * or throwing a {@link RuntimeException}.
 *
 * @author Wolfgang Konen, TH Koeln, 2017 -2020
 */
public interface XNTupleFuncs {
	/**
	 * If XNTuple object need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members, setting of defaults for older .agt.zip), put the relevant code in here.
	 * @return true on success
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	boolean instantiateAfterLoading();
	
	/**
	 * @return the number of board cells
	 */
	int getNumCells();
	
	/**
	 * @return the number P of position values 0, 1, 2,..., P-1 that each board cell 
	 * can have (e. g. P=3 for TicTacToe with 0:"O", 1=empty, 2="X") 
	 */
	int getNumPositionValues();

	/**
	 * [This method is only needed for agents {@link TDNTuple4Agt} and {@link Sarsa4Agt}.]
	 *
	 * @return 	a vector of length {@link #getNumCells()} that has in its {@code i}th element the number
	 * 			P of position values 0, 1, 2,..., P-1 that board cell {@code i} can have
	 */
	int[] getPositionValuesVector();

	/**
	 * @return the number of players in this game 
	 */
	int getNumPlayers();
	
	/**
	 * @return the maximum number of symmetries in this game
	 */
	int getNumSymmetries();
	
	/**
	 * @param so the state
	 * @return an object with member {@code int[] bvec}. {@code bvec} is a vector of length {@link #getNumCells()},
	 * holding for each board cell its position value 0, 1, 2,..., P-1 in state {@code so}.<br>
	 * P = {@link #getNumPositionValues()}.
	 */
	BoardVector getBoardVector(StateObservation so);

	/**
	 * @return a board vector where each cell has a different {@code int} 
	 */
	BoardVector makeBoardVectorEachCellDifferent();
	
	/**
	 * Given a board vector and given that the game has s symmetries,  
	 * return an array which holds n (see below) symmetric board vectors: 
	 * <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other n-1 rows are the board vectors when transforming {@code boardVector}
	 * 		according to the n-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * Symmetric board vectors are board vectors with the same value and with equivalent best
	 * next actions. For example, TicTacToe has 8 symmetries: 4 rotations (0�, 90�, 180�, 270�)
	 * times 2 mirror reflections.
	 * <p>
	 * If a game has no symmetries, this method should return a length-1 array {@code BoardVector[] boardArray} with
	 * <pre>
	 * 		boardArray[0] = boardVector </pre>
	 * <p>
	 * If n=0 or n=s, all s symmetry vectors are returned. If {@literal 0 < n < s} then 
	 * the boardVector itself and n-1 (randomly picked) symmetry vectors are returned.
	 * 
	 * @param boardVector e.g. from {@link #getBoardVector(StateObservation)}
	 * @param n number of symmetry vectors to return (n=0 meaning 'all')
	 * @return a vector of n BoardVectors 
	 */
	BoardVector[] symmetryVectors(BoardVector boardVector, int n);
	/**
	 * Same as {@link #symmetryVectors(BoardVector, int)}, but with parameter {@link StateObsWithBoardVector} 
	 * instead of {@link BoardVector}. (Some games need the {@link BoardVector}-creating {@link StateObservation} 
	 * object in order to construct the symmetric board vectors.)
	 */
	BoardVector[] symmetryVectors(StateObsWithBoardVector curSOWB, int n);
	
	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, 
	 * generate the array of equivalent action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed <b>only for Q-learning and Sarsa</b>. Classes implementing this interface
	 * may just throw an exception if they do not need this method.
	 * 
	 * @param actionKey
	 * 				the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b>
	 * 				array of the equivalent actions' keys. 
	 * <p>
	 * equivAction[i] is the key of the action in the i'th equivalent board vector equiv[i], 
	 * which is equivalent to actionKey in equiv[0]. <br>
	 * Here, equiv[i] = {@link #symmetryVectors(BoardVector, int)}{@code [i]}.
	 */
	int[] symmetryActions(int actionKey);
	
	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different lengths. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.<p>
	 * 
	 * Other options than fixed n-tuples (that is, the generation of random n-tuples) are 
	 * provided by {@link NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)}
	 * 
	 * @param mode one of the values from {@link #fixedNTupleModesAvailable()}
	 * @return nTuples[numTuples][]
	 * 
	 * @see NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)
	 */
	int[][] fixedNTuples(int mode);
	
	/**
	 * @return a tooltip string describing the different mode options of {@link #fixedNTuples(int)}
	 */
	String fixedTooltipString();
	
	int[] fixedNTupleModesAvailable();

	/**
	 * Return all neighbors of {@code iCell}
	 * 
	 * @param iCell the cell
	 * @return a list of all cells adjacent to {@code iCell} (referring to the coding in 
	 * 		a board vector) 
	 */
	HashSet adjacencySet(int iCell);
}
