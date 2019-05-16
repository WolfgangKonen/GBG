package games.Othello;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.math3.exception.OutOfRangeException;

import controllers.TD.ntuple2.NTupleFactory;
import games.StateObservation;
import games.XNTupleFuncs;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class XNTupleFuncsOthello implements XNTupleFuncs, Serializable {

	private int[] actionVector = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8 , 9, 10, 11, 12, 13, 14, 15 , 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 , 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 , 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63};;	
	private int[][] symmetryActions; //Gives a 2D representation of all SymmetryVectors
	private int[][] actionPositions; //Given a action key, it gives all symmetric actions

	public XNTupleFuncsOthello() {
		symmetryActions = symmetryVectors(actionVector);
		actionPositions = new int[actionVector.length][symmetryActions.length];
		for (int i = 0; i < actionPositions.length; i++) 
		{
			for(int j = 0; j < symmetryActions.length; j++)
			{
				actionPositions[i][j] = indexOf(symmetryActions[j], i);
			}
		}
	}

	private int indexOf(int[] arr, int j) {
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i] == j) return i;
		}
		throw new RuntimeException("indexOf: Arr does not contain " + j);
	}

	private void calcActionVector() {
		for(int i = 0; i < getNumCells(); i++)
		{

		}
	}

	/**
	 * @return integer of total board cells
	 */
	@Override
	public int getNumCells() {
		return ConfigOthello.BOARD_SIZE * ConfigOthello.BOARD_SIZE;
	}

	/**
	 * @return integer total of different board values
	 */
	@Override
	public int getNumPositionValues() {
		return 3;
	}

	/**
	 * @return Number of Players
	 */
	@Override
	public int getNumPlayers() {
		return 2;
	}
	/**
	 * The board vector is an {@code int[]} vector where each entry corresponds to one 
	 * cell of the board. In the case of Othello the mapping is
	 * <pre>
	 * 00 01 02 03 04 05 06 07			
	 * 08 09 10 11 12 13 14 15			
	 * 16 17 18 19 20 21 22 23			
	 * 24 25 26 27 28 29 30 31 
	 * 32 33 34 35 36 37 38 39	
	 * 40 41 42 43 44 45 46 47	
	 * 48 49 50 51 52 53 54 55
	 * 56 57 58 59 60 61 62 63
	 * </pre>
	 * @param The StateObservation of the current game state
	 * @return a vector of length {@link #getNumCells()}, holding for each board cell its 
	 * position value with 0 = empty , 1 = white, -1 = black.
	 */
	@Override
	public int[] getBoardVector(StateObservation so) {
		assert ( so instanceof StateObserverOthello);
		int[][] gameState = ((StateObserverOthello) so).getCurrentGameState();
		int[] retVal = new int[getNumCells()];
		for(int i = 0, n = 0; i < ConfigOthello.BOARD_SIZE; i++){
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++, n++)
				retVal[n] = gameState[i][j];
		}
		return retVal;
	}

	/**
	 * Given a board vector from {@link #getBoardVector(StateObservation)} and given that the 
	 * game has s symmetries, return an array which holds s symmetric board vectors: <ul>
	 * <li> the first row {@code boardArray[0]} is the board vector itself
	 * <li> the other rows are the board vectors when transforming {@code boardVector}
	 * 		according to the s-1 other symmetries (e. g. rotation, reflection, if applicable).
	 * </ul>
	 * In the case of Othello we have s=8 symmetries (4 board rotations * 2 board vertical Mirrors)
	 * 
	 * @param boardVector
	 * @return boardArray
	 */
	@Override
	public int[][] symmetryVectors(int[] boardVector) {
		//int s = 16; // Read the comment above!
		int s = 8;
		int[][] symmetryVectors = new int[s][boardVector.length];
		symmetryVectors[0] = boardVector;

		for(int i = 1; i < 4; i++) {
			symmetryVectors[i] = rotate(symmetryVectors[i-1]);
		}
		symmetryVectors[4] = mirrorHorizontally(symmetryVectors[0]);
		for(int j = 5; j < 8; j++)
		{
			symmetryVectors[j] = rotate(symmetryVectors[j-1]);
		}
		return symmetryVectors;
	}

	/**
	 * Helper function for  {@link #symmetryVectors(int[])}: 
	 * Rotates the given boardVector 90 degrees clockwise
	 * 
	 * <pre>
	 * 
	 * 00 01 02 03 04 05 06 07			56 48 40 32 24 16 08 00
	 * 08 09 10 11 12 13 14 15			57 49 41 33 25 17 09 01
	 * 16 17 18 19 20 21 22 23			58 50 42 34 26 18 10 02
	 * 24 25 26 27 28 29 30 31  ---> 	59 51 43 35 27 19 11 03
	 * 32 33 34 35 36 37 38 39			60 52 44 36 28 20 12 04
	 * 40 41 42 43 44 45 46 47			61 53 45 37 29 21 13 05
	 * 48 49 50 51 52 53 54 55			62 54 46 38 30 22 14 06
	 * 56 57 58 59 60 61 62 63			63 55 47 39 31 23 15 07
	 * 
	 * </pre>
	 * @param boardVector
	 * @return rotatedBoard
	 */
	private int[] rotate(int[] boardVector)
	{	    
		//		int[] rotationIndex = new int[] { 56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 03, 
		//			60, 52, 44, 36, 28, 20, 12, 4, 61, 53, 45, 37, 29, 21, 13, 5, 62, 54, 46, 38, 30, 22, 14, 6, 63, 55, 47, 39, 31, 23, 15, 7 };

		int[] result = new int[boardVector.length];
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				int oldPosition = i * ConfigOthello.BOARD_SIZE + j;
				int newPosition = (ConfigOthello.BOARD_SIZE * ConfigOthello.BOARD_SIZE - ConfigOthello.BOARD_SIZE) + i - (j * ConfigOthello.BOARD_SIZE);

				result[newPosition] = boardVector[oldPosition];
			}	
		}
		for(int i = 0; i < result.length / 2; i++)
		{
			int temp = result[i];
			result[i] = result[result.length - 1 - i];
			result[result.length - 1 - i] = temp;
		}

		return result;

	}

	/**
	 * Helper function for  {@link #symmetryVectors(int[])}: 
	 * Mirrors the board along its diagonal from top left to bottom right
	 * 
	 * <h4>Unused</h4>
	 * 
	 * <pre>
	 * 
	 * 00 01 02 03 04 05 06 07			00 08 16 24 32 40 48 56
	 * 08 09 10 11 12 13 14 15			01 09 17 25 32 41 49 57
	 * 16 17 18 19 20 21 22 23			02 10 18 26 33 42 50 58
	 * 24 25 26 27 28 29 30 31  ---> 	03 11 19 27 34 43 51 59
	 * 32 33 34 35 36 37 38 39			04 12 20 28 35 44 52 60
	 * 40 41 42 43 44 45 46 47			05 13 21 29 36 45 53 61
	 * 48 49 50 51 52 53 54 55			06 14 22 30 37 46 54 62
	 * 56 57 58 59 60 61 62 63			07 15 23 31 38 47 55 63
	 * 
	 * </pre>
	 * @param boardVector
	 * @return
	 */
	private int[] mirrorDiagonally(int[] boardVector)
	{
		int[] result = new int[boardVector.length];
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				int oldPosition = i * ConfigOthello.BOARD_SIZE + j;
				int newPosition = j * ConfigOthello.BOARD_SIZE + i;

				result[newPosition] = boardVector[oldPosition];
			}	
		}
		return result;
	}
	/**
	 * Helper function for  {@link #symmetryVectors(int[])}: 
	 * Mirrors the board along its horizontal central axis
	 * 
	 * <pre>
	 * 
	 * 00 01 02 03 04 05 06 07			56 57 58 59 60 61 62 63																												
	 * 08 09 10 11 12 13 14 15			48 49 50 51 52 53 54 55
	 * 16 17 18 19 20 21 22 23			40 41 42 43 44 45 46 47
	 * 24 25 26 27 28 29 30 31  ---> 	32 33 34 35 36 37 38 39
	 * 32 33 34 35 36 37 38 39			24 25 26 27 28 29 30 31
	 * 40 41 42 43 44 45 46 47			16 17 18 19 20 21 22 23
	 * 48 49 50 51 52 53 54 55			08 09 10 11 12 13 14 15
	 * 56 57 58 59 60 61 62 63			00 01 02 03 04 05 06 07
	 * 
	 * </pre>
	 * @param boardVector
	 * @return
	 */
	private int[] mirrorHorizontally(int[] boardVector)
	{
		int[] result = new int[boardVector.length];
		for(int i = 0; i < ConfigOthello.BOARD_SIZE; i++)
		{
			for(int j = 0; j < ConfigOthello.BOARD_SIZE; j++)
			{
				int oldPosition = (ConfigOthello.BOARD_SIZE - 1 - i) * ConfigOthello.BOARD_SIZE + (ConfigOthello.BOARD_SIZE - 1 - j);
				result[i * ConfigOthello.BOARD_SIZE + j] = boardVector[oldPosition];
			}
		}
		return result;
	}

	/**
	 * Given a certain board array of symmetric (equivalent) states for state <b>{@code so}</b> 
	 * and a certain action to be taken in <b>{@code so}</b>, generate the array of equivalent 
	 * action keys {@code equivAction} for the symmetric states.
	 * <p>
	 * This method is needed only for Q-learning and Sarsa.
	 * 
	 * @param actionKey the key of the action to be taken in <b>{@code so}</b> 
	 * @return <b>equivAction</b> array of the equivalent actions' keys. 
	 * <p>
	 * equivAction[i] is the key of the action equivalent to actionKey in the
	 * i'th equivalent board vector equiv[i] = {@link #symmetryVectors(int[])}[i]
	 */
	@Override
	public int[] symmetryActions(int actionKey) 
	{
		return actionPositions[actionKey];
	}

	/** 
	 * Return a fixed set of {@code numTuples} n-tuples suitable for that game. 
	 * Different n-tuples may have different lengths. An n-tuple {0,1,4} means a 3-tuple 
	 * containing the cells 0, 1, and 4.<p>
	 * 
	 * Other options than fixed n-tuples (that is, the generation of random n-tuples) are 
	 * provided by {@link NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)}
	 * 
	 * The first N-Tuple Architecture is the Network proposed by Wojciech Jaskowski in his paper "Systematic N-Tuple Networks for Othello position evaluation" (2014)
	 * 
	 * @param mode one of the values from {@link #getAvailFixedNTupleModes()}
	 * @return nTuples[numTuples][]
	 * 
	 * @see NTupleFactory#makeNTupleSet(params.ParNT, XNTupleFuncs)
	 */
	@Override
	public int[][] fixedNTuples(int mode) 
	{
		switch(mode)
		{
		//--- 10 1-Tuples
		case 0:	return new int[][] {
			{0}, 
			{8}, {9},
			{16}, {17}, {18},
			{24}, {25}, {26}, {27}
		};
		//--- 32 Straight 2-Tuple
		case 1: return new int[][] {
			{0, 8}, {0, 9}, {1, 9}, {2, 10}, {3, 11},
			{8, 16}, {8, 17}, {9, 17}, {9, 18}, {10, 18}, {11, 19},
			{16, 24}, {16, 25}, {17, 25}, {17, 26}, {18, 26}, {18, 27}, {19, 27},
			{24, 32}, {24, 33}, {25, 33}, {25, 34}, {26, 34}, {26, 35}, {27, 35}, {27, 36},
			{32, 41}, {33, 42}, {34, 43},
			{40, 49}, {41, 50},
			{48, 57}
		};
		//--- 24 Straight 3-Tuple
		case 2: return new int[][] {
			{0, 8, 16}, {0, 9, 18}, {1, 9, 17}, {3, 10, 18}, {4, 11, 19},
			{8, 16, 24}, {8, 17, 26}, {9, 17, 25}, {9, 18, 27}, {10, 18, 26}, {11, 19, 27},
			{16, 24, 32}, {16, 25, 34}, {17, 25, 33}, {17, 26, 35}, {18, 27, 36}, {18, 28, 38}, {19, 28, 37},
			{24, 33, 42}, {25, 34, 43}, {26, 35, 44},
			{32, 41, 50}, {33, 42, 51},
			{40, 49, 58}
		};
		//--- 8 Random snakey bakey 4-Tuple
		case 3: return new int[][] {
			{0, 1, 8, 9}, {2, 11, 10, 3}, 
			{11, 18, 17, 24}, {15, 14, 21, 20},
			{18, 19, 26, 35}, 
			{33, 32, 40, 48},
			{44, 36, 43, 51}, {45, 53, 54, 62}
		};
		default: throw new OutOfRangeException(mode, 0, 3);
		}
	}

	@Override
	public String fixedTooltipString() {
		return "<html>"	+ "1: TODO"	+ "</html>";
	}

	@Override
	public int[] getAvailFixedNTupleModes() {
		return fixedModes;
	}

	@Override
	public HashSet adjacencySet(int iCell) 
	{
		HashSet<Integer> neighbours = new HashSet<Integer>();
		int cellX = iCell / ConfigOthello.BOARD_SIZE;
		int cellY = iCell - cellX * ConfigOthello.BOARD_SIZE;
		for(int i = -1; i < 2; i++)
		{
			for(int j = -1; j < 2; j++) 
			{
				if(i == 0 && j == 0)
					continue; 

				int x = cellX + i;
				int y = cellY + j;
				if(x >= ConfigOthello.BOARD_SIZE || y >= ConfigOthello.BOARD_SIZE)
					continue;

				if(x < 0 || x > ConfigOthello.BOARD_SIZE || y < 0 || y > ConfigOthello.BOARD_SIZE) {

				}
				else {
					neighbours.add(x * ConfigOthello.BOARD_SIZE + y);
				}
			}
		}
		return neighbours;
	}

	private static int[] fixedModes = {0, 1, 2, 3};



}