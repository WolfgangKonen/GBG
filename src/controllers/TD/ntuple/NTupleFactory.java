package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import games.StateObservation;
import games.XNTupleFuncs;
import params.NTParams;

/**
 * This class generates n-tuple sets (fixed for a specific game, by random walk or by 
 * random points)
 * 
 * @see XNTupleFuncs
 * @author Wolfgang Konen, Samineh Bagheri, THK, Feb'2017
 *
 */
public class NTupleFactory {
	private boolean PRINTNTUPLES = false;	// /WK/ control the file printout of n-tuples

	// Needed for generating random n-Tuples
	private Random rand = new Random(); //new Random(42);

	/**
	 * 
	 * @param ntPar n-tuple parameter (+ TC parameter)
	 * @param xnf	holds game-specific functions for n-tuples
	 * @return {@code int[m][n]} array for the cell positions of m n-tuples with length n. 
	 * 		(The real n-tuples in {@link NTupleValueFunc} are later an array 
	 * 		{@code NTuple[p][m]}, one n-tuple set for each player.) 
	 * @throws Exception
	 */
	public int[][] makeNTupleSet(NTParams ntPar, XNTupleFuncs xnf) throws Exception {
		int nTuplesI[][]; 
		boolean randomness=ntPar.getRandomness();
		boolean randWalk=ntPar.getRandWalk();
		int numTuple=ntPar.getNtupleNumber();
		int maxTupleLen=ntPar.getNtupleMax();
		int numCells = xnf.getNumCells();
		int POSVALUES = xnf.getNumPositionValues();
		
		if(randomness==true){
			
			if (maxTupleLen > numCells) 
				throw new RuntimeException("Requested random tuple length is greater than "
						+"the number of board cells ("+numCells+")");
			
			if(randWalk==true){
				//random walk
				nTuplesI = generateRandomWalkNTuples(maxTupleLen,numTuple,POSVALUES,xnf);
			}else{
				//random point
				nTuplesI = generateRandomPointNTuples(maxTupleLen,numTuple,POSVALUES,numCells);
			}
			
		}else{
			//given ntuples
			nTuplesI = xnf.fixedNTuples();
			
			checkNtuples(nTuplesI,numCells);
		}

		return nTuplesI;
	}
	
	/**
	 * Generate a set of random n-tuples (random WALK).<p>
	 * 
	 * 'Random walk' means that adjacent cells within the n-tuple are also adjacent cells
	 * on the game board.<p>
	 * 
	 * Note: This method ensures each n-tuple contains no cell twice. It may however
	 * happen that two different n-tuples have the same cells.
	 * 
	 * @param numWalkSteps
	 *            length of each n-tuple = steps of random walk
	 * @param numTuple
	 *            number of tuples that shall be generated
	 * @param POSVALUES
	 *            possible values/field of the board (currently not used)
	 * @param numCells
	 *            number of cells on the board (TicTacToe: 9)
	 * @throws IOException may only happen if PRINTNTUPLES=true
	 */
	private int[][] generateRandomWalkNTuples(int numWalkSteps,
			int numTuple,int POSVALUES,XNTupleFuncs xnf) throws IOException {
		int nTuples[][] = new int[numTuple][];
		int numCells = xnf.getNumCells();

		for (int i = 0; i < numTuple; i++) {
			int[] nTuple;
			int[] tmpTuple = new int[numWalkSteps];
			int tupleLen = 1;
			int lastMove = rand.nextInt(numCells); // start position
			tmpTuple[0] = lastMove;
			boolean first = true;
			HashSet tupleSet = new HashSet();

			for (int j = 0; j < numWalkSteps; j++) {
				if (first) {
					int sp = rand.nextInt(numCells); 			// pick a random board cell
					tupleSet.add(sp);
					first=false;
				} else {
					boolean foundStep = false;
					while(!foundStep) {
						//
						// look from a randomly picked cell of the so-far tupleSet
						// whether it has neighbors which do not belong yet to tupleSet: 
						// If so, pick one of these neighbors. 
						// If not, foundStep remains false and another loop 
						// through while picks another cell from tupleSet
						//
						int iCell=pickElement(tupleSet);
						HashSet adjSet = xnf.adjacencySet(iCell);
						Iterator it = tupleSet.iterator();
						while (it.hasNext()) {
							adjSet.remove((int)it.next());
						}
						if (adjSet.size()>0) {
							iCell = pickElement(adjSet);
							foundStep=true;
							tupleSet.add(iCell);
						}
					}
				}
			}  // for j
			
			assert (tupleSet.size()==numWalkSteps);
			//System.out.println("tuplSet.size = "+tupleSet.size()+", numWalkSteps:"+numWalkSteps);
			nTuple = new int[numWalkSteps];
			Iterator it = tupleSet.iterator();
			for (int j = 0; j < numWalkSteps; j++)
				nTuple[j] = (int)it.next();
			nTuples[i] = nTuple;
			//System.out.println("tuple "+i+": "+Arrays.toString(nTuple));
			
		}  // for i
		
		if (PRINTNTUPLES) {
			for(int h=0;h<numTuple;h++)
				   print(nTuples[h]);		// may throw IOException
		}
		
		return nTuples;
	}

	private int pickElement(HashSet tSet) {
		int sp = rand.nextInt(tSet.size()); // pick a random element from set
		Iterator it = tSet.iterator();
		int iCell=0,count=0;
		while (it.hasNext()) {
			iCell=(int)it.next();
			count++;
			if (count>sp) break;
		}
		return iCell;
	}

	/**
	 * Generate a set of random n-tuples (random POINTS).<p>
	 * 
	 * 'Random points' means that adjacent cells within the n-tuple can be from any point 
	 * (cell) of the board. It is guarenteed that no cell appears twice in one n-tuple. 
	 * It might (theoretically) happen that two n-tuples cover the same set of points (board
	 * cells) although this is extremely unlikely for larger values of {@code maxTupleLen}.<p>
	 * 
	 * Note: This method ensures each n-tuple contains no cell twice. It may however
	 * happen that two different n-tuples have the same cells. 
	 * 
	 * @param maxTupleLen
	 *            length of each n-tuple 
	 * @param numTuple
	 *            number of tuples that shall be generated
	 * @param POSVALUES
	 *            possible values/field of the board (currently not used)
	 * @param numCells
	 *            number of cells on the board (TicTacToe: 9)
	 * @throws IOException may only happen if PRINTNTUPLES=true
	 */
	private int[][] generateRandomPointNTuples(int maxTupleLen,
			int numTuple,int POSVALUES,int numCells) throws IOException {
		int nTuples[][] = new int[numTuple][];
		for (int i = 0; i < numTuple; i++) {
			//int tupleLen = rand.nextInt(maxTupleLen - 1) + 2; // min. length is 2
			//samine// just commented the last line to change tupleLen to the given length
			//to get rid of that 
			int tupleLen=maxTupleLen;													
			int[] tuple = new int[tupleLen];
			boolean isUnique;
			for (int j = 0; j < tupleLen; j++) {
				int sp;
				do {
					isUnique = true;
					sp = rand.nextInt(numCells); 
					for (int k = 0; k < j; k++)
						if (tuple[k] == sp) {
							isUnique = false;
							break;  // out of for
						}
				} while (!isUnique);

				tuple[j] = sp;
			}
			nTuples[i] = tuple;
		}
		
		if (PRINTNTUPLES) {
			//samine//printing the generated n-tuples
			for(int h=0;h<numTuple;h++)
				print(nTuples[h]);		// may throw IOException
		}

		return nTuples;
	}
	
	private boolean checkNtuples(int[][] nTuples, int numCells) throws Exception {
		int numTuples = nTuples.length;
		for (int i=0; i<numTuples; i++)  {
			for (int j=0; j<nTuples[i].length; j++) {
				if (nTuples[i][j]<0) throw new RuntimeException("Negative index " 
						+nTuples[i][j]+" in nTuples["+i+"]["+j+"]");
				if (nTuples[i][j]>numCells) throw new RuntimeException("Too large index " 
						+nTuples[i][j]+" in nTuples["+i+"]["+j+"]");
			}
		}
		return true;
	}

	//
	// Debug only: 
	//
	private void print(int[] is) throws IOException {
		PrintWriter randNtuple = new PrintWriter(new FileWriter("randNtuple",true));
		randNtuple.println("" +Arrays.toString(is));
		randNtuple.close();
	}

//	/**
//	 * Generate a set of random n-tuples (random WALK).<p>
//	 * 
//	 * 'Random walk' means that adjacent cells within the n-tuple are also adjacent cells
//	 * on the game board.
//	 * 
//	 * @param maxTupleLen
//	 *            length of each n-tuple 
//	 * @param numTuple
//	 *            number of tuples that shall be generated
//	 * @param POSVALUES
//	 *            possible values/field of the board (currently not used)
//	 * @param numCells
//	 *            number of cells on the board (TicTacToe: 9)
//	 * @throws IOException may only happen if PRINTNTUPLES=true
//	 */
//	private int[][] generateRandomWalkNTuplesOLD(int maxTupleLen,
//			int numTuple,int POSVALUES,int numCells) throws IOException {
//		int nTuples[][] = new int[numTuple][];
//
//		for (int i = 0; i < numTuple; i++) {
//			int numWalkSteps = maxTupleLen;
//			int[] nTuple;
//			int[] tmpTuple = new int[numWalkSteps];
//			int tupleLen = 1;
//			int lastMove = rand.nextInt(numCells); // start position
//			tmpTuple[0] = lastMove;
//
//			for (int j = 0; j < numWalkSteps - 1; j++) {
//				boolean madeMove = false;
//				do {
//					int sp = rand.nextInt(6); 	// For TicTacToe only
//											  	// /WK/ why 6 and not 9 ??
//					switch (sp) {
//					case 0:
//						if (lastMove / 3 != 2) {
//							madeMove = true;
//							lastMove = lastMove + 3;
//						}
//						break;
//					case 1:
//						if (lastMove / 3 != 0) {
//							madeMove = true;
//							lastMove = lastMove - 3;
//						}
//						break;
//					case 2:
//						if (lastMove % 3 != 2) {
//							madeMove = true;
//							lastMove = lastMove + 1;
//						}
//						break;
//					case 3:
//						if (lastMove % 3 != 0) {
//							madeMove = true;
//							lastMove = lastMove - 1;
//						}
//						break;
//					case 4:
//						if (lastMove / 3 != 2 && lastMove % 3 != 2) {
//							madeMove = true;
//							lastMove = lastMove + 4;
//						}
//						break;
//					case 5:
//						if (lastMove / 3 != 0 && lastMove % 3 != 0) {
//							madeMove = true;
//							lastMove = lastMove - 4;
//						}
//						break;
//					}
//				} while (!madeMove);
//				int k;
//				for (k = 0; k < tupleLen && tmpTuple[k] != lastMove; k++)
//					;
//				if (k == tupleLen) {
//					tmpTuple[tupleLen] = lastMove;
//					tupleLen++;
//				}
//			}
//			
//			if (tupleLen == numWalkSteps) {
//				nTuple = new int[tupleLen];
//				for (int j = 0; j < tupleLen; j++)
//					nTuple[j] = tmpTuple[j];
//				nTuples[i] = nTuple;
//			} else
//				i--;			
//		}
//		
//		if (PRINTNTUPLES) {
//			for(int h=0;h<numTuple;h++)
//				   print(nTuples[h]);		// may throw IOException
//		}
//		
//		return nTuples;
//	}

}
