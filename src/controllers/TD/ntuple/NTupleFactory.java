package controllers.TD.ntuple;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import games.StateObservation;
import params.NTParams;

public class NTupleFactory {
	private int numCells=9; 			// specific for TicTacToe
	private int POSVALUES = 3; 			// Possible values for each board field
	private boolean PRINTNTUPLES = false;	// /WK/ control the file printout of n-tuples

	// Needed for generating random n-Tuples
	private Random rand = new Random(42);

	public int[][] makeNTupleSet(NTParams ntPar, StateObservation so) throws Exception {
		int nTuples[][]; 
		boolean randomness=ntPar.getRandomness();
		boolean randWalk=ntPar.getRandWalk();
		int numTuple=ntPar.getNtupleNumber();
		int maxTupleLen=ntPar.getNtupleMax();
		int numCells = so.getNumCells();
		POSVALUES = so.getNumPositionValues();
		numCells = so.getNumCells();
		//samine//
		if(randomness==true){
			
			if (maxTupleLen > numCells) 
				throw new RuntimeException("Requested random tuple length is greater than "
						+"the number of board cells ("+numCells+")");
			
			if(randWalk==true){
				//random walk
				nTuples = generateRandomWalkNTuples(maxTupleLen,numTuple,POSVALUES,numCells);
			}else{
				//random point
				nTuples = generateRandomPointNTuples(maxTupleLen,numTuple,POSVALUES,numCells);
			}
			
		}else{
			//given ntuples
			nTuples = so.fixedNTuples();
			
			checkNtuples(nTuples,numCells);
		}

		return nTuples;
	}
	
	/**
	 * Generate a set of random n-tuples (random WALK).
	 * 
	 * @param maxTupleLen
	 *            length of each n-tuple 
	 * @param numTuple
	 *            number of tuples that shall be generated
	 * @param POSVALUES
	 *            possible values/field of the board (TicTacToe: 3)
	 * @param numCells
	 *            number of cells on the board (TicTacToe: 9)
	 * @throws IOException may only happen if PRINTNTUPLES=true
	 */
	private int[][] generateRandomWalkNTuples(int maxTupleLen,
			int numTuple,int POSVALUES,int numCells) throws IOException {
		int nTuples[][] = new int[numTuple][];

		for (int i = 0; i < numTuple; i++) {
			int numWalkSteps = maxTupleLen;
			int[] nTuple;
			int[] tmpTuple = new int[numWalkSteps];
			int tupleLen = 1;
			int lastMove = rand.nextInt(numCells); // start position
			tmpTuple[0] = lastMove;

			for (int j = 0; j < numWalkSteps - 1; j++) {
				boolean madeMove = false;
				do {
					int sp = rand.nextInt(6); 	// For TicTacToe only
											  	// /WK/ why 6 and not 9 ??
					switch (sp) {
					case 0:
						if (lastMove / 3 != 2) {
							madeMove = true;
							lastMove = lastMove + 3;
						}
						break;
					case 1:
						if (lastMove / 3 != 0) {
							madeMove = true;
							lastMove = lastMove - 3;
						}
						break;
					case 2:
						if (lastMove % 3 != 2) {
							madeMove = true;
							lastMove = lastMove + 1;
						}
						break;
					case 3:
						if (lastMove % 3 != 0) {
							madeMove = true;
							lastMove = lastMove - 1;
						}
						break;
					case 4:
						if (lastMove / 3 != 2 && lastMove % 3 != 2) {
							madeMove = true;
							lastMove = lastMove + 4;
						}
						break;
					case 5:
						if (lastMove / 3 != 0 && lastMove % 3 != 0) {
							madeMove = true;
							lastMove = lastMove - 4;
						}
						break;
					}
				} while (!madeMove);
				int k;
				for (k = 0; k < tupleLen && tmpTuple[k] != lastMove; k++)
					;
				if (k == tupleLen) {
					tmpTuple[tupleLen] = lastMove;
					tupleLen++;
				}
			}
			// TODO: remove??
			if (tupleLen == numWalkSteps) {
				nTuple = new int[tupleLen];
				for (int j = 0; j < tupleLen; j++)
					nTuple[j] = tmpTuple[j];
				nTuples[i] = nTuple;
			} else
				i--;			
		}
		
		if (PRINTNTUPLES) {
			for(int h=0;h<numTuple;h++)
				   print(nTuples[h]);		// may throw IOException
		}
		
		return nTuples;
	}

	/**
	 * Generate a set of random n-tuples (random POINTS).
	 * 
	 * @param maxTupleLen
	 *            length of each n-tuple 
	 * @param numTuple
	 *            number of tuples that shall be generated
	 * @param POSVALUES
	 *            possible values/field of the board (TicTacToe: 3)
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
							break;
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

}
