package games.Nim;

import java.io.IOException;

/**
 * A simple main program to calculate the number of states in the game of NIM.
 *  
 * @author Wolfgang Konen, TH Köln , Jan'18
 */
public class CountNim {

	public int nHeap = 10;
	public int nPiece = 5;
	public int gCount1 = 0, gCount2=0;
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		CountNim nim = new CountNim();

		nim.calculate();

		if (args.length==0) {
			;
		} else {
			throw new RuntimeException("[CountNim.main] args="+args+" not allowed.");
		}
	}
	
	private void calculate() {
		// unordered NIM states
		double nUnordered = Math.pow(nPiece+1, nHeap);
		
		// exhaustive counting of *ordered* NIM states (a heap to the left is equal or higher)
		int counter1=0;
		int[] heap = new int[nHeap];
		for (int k=0; k<heap.length; k++) heap[k]=nPiece;
		while (heap[0]>0) {		// while leftmost heap has pieces left
			counter1++;
			printHeap(heap);
			heap = decreaseHeaps(heap, nHeap-1);
		}
		counter1++; // one more for the last state (0 0 ... 0)
		printHeap(heap);
		double perc1 = counter1/nUnordered;
		
		// recursive calculation
		int counter2 = gFunc(nHeap,nPiece);
		double perc2 = counter2/nUnordered;
		
		System.out.println("count  NIM unordered states: " + ((int) nUnordered));
		System.out.println("count1 NIM ordered states: " + counter1+ " ("+perc1*100+"% of unordered states)");
		System.out.println("count2 NIM ordered states: " + counter2+ " ("+perc2*100+"% of unordered states)");
		System.out.println("recursive calls (with summation): " + gCount1 + " ("+gCount2+")");
	}
	
	/**
	 * 
	 * @param heap	the heaps
	 * @param d		the heap to decrease 
	 * @return		the modified heaps. If heap d has >0 pieces, decrease it by 1.
	 * 				If heap d has 0 pieces, then set it and all heaps to the right to 
	 * 				heap[d-1]-1 (to the left). 
	 * 				Then call decreaseHeaps(heap,d-1) recursively, which finally 
	 * 				decreases heap[d-1] by one as well.
	 */
	private int[] decreaseHeaps(int[] heap, int d) {
		if (heap[d]==0) {
			for (int k=d; k<heap.length; k++) heap[k]=heap[d-1]-1;
			return decreaseHeaps(heap,d-1);
		}
		heap[d]--;
		return heap;
		
	}
	
	private void printHeap(int[] heap) {
		System.out.print("(");
		for (int k=0; k<heap.length; k++) System.out.print(heap[k]+" ");
		System.out.println(")");
	}
	
	/**
	 * Recursive calculation of the number of ordered states in NIM
	 *  
	 * @param nHeap number of heaps
	 * @param left  number of pieces in the leftmost (highest) heap
	 * @return count of ordered states in this heap collection
	 */
	private int gFunc(int nHeap, int left) {
		gCount1++;
		if (left==0) return 1;			// only one state (0 0 ... 0)
		if (nHeap==1) return (left+1);  // sub-states (0), (1), ..., (left)
		gCount2++;
		int s=0;
		for (int i=0; i<(left+1); i++) s+= gFunc(nHeap-1,i);
		// gFunc(nHeap-1,i) is the sub-state count for the remaining heaps, if the leftmost
		// heap is in a state with i pieces. Sum over i to get the total count s
		return s;
	}


}
