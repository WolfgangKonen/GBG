package games.CFour;

/**
 * Implementation of a move-list. Very useful for "move Back" or "next Move"
 * functionality in games.
 * 
 * @author Markus Thill
 */
public class MoveList {

	private static final int MAXMOVES = 42;
	private int[] moveList = new int[MAXMOVES];
	// Zeigt auf aktuell letzten Zug und gibt damit die aktuelle Situation auf
	// dem Spielfeld wieder
	private int mvPointer = -1;

	/**
	 * Generate an empty move-list
	 */
	public MoveList() {
		reset();
	}

	/**
	 * Reset move-list
	 */
	public void reset() {
		for (int i = 0; i < MAXMOVES; i++) {
			moveList[i] = (-1);
		}
		mvPointer = (-1);
	}

	/**
	 * Add move to the end of the list
	 * 
	 * @param col
	 *            Move made
	 * @return false, if move-list is completly filled (MAXMOVES)
	 */
	public boolean putMove(int col) {
		if (mvPointer < MAXMOVES - 1) {
			moveList[++mvPointer] = col;
			// "Zug Vor" darf nicht mehr erlaubt sein!!!
			for (int i = (int) (mvPointer + 1); i < MAXMOVES; i++) {
				moveList[i] = (-1);
			}
			return true;
		}
		return false;
	}

	/**
	 * Get last move, that was put in the list
	 * 
	 * @return -1, if list is empty
	 */
	public int getPrevMove() {
		return (mvPointer >= 0 ? moveList[mvPointer--] : -1);
	}

	/**
	 * Like getPrevMove. But internal pointer is not moved
	 * 
	 * @return -1, if list is empty
	 */
	public int readPrevMove() {
		return (mvPointer >= 0 ? moveList[mvPointer] : -1);
	}

	/**
	 * Gets next move, if available. To get a value getPrevMove must be executed
	 * at least once.
	 * 
	 * @return -1, if no next move
	 */
	public int getNextMove() {
		if (isNextMove()) {
			return moveList[++mvPointer];
		}
		return (-1);
	}

	/**
	 * @return true, if list is empty.
	 */
	public boolean isEmpty() {
		return mvPointer == -1;
	}

	/**
	 * @return true, if list is full.
	 */
	private boolean isFull() {
		return mvPointer == (MAXMOVES - 1);
	}

	/**
	 * @return true, if there is a next move.
	 */
	public boolean isNextMove() {
		return !isFull() && moveList[mvPointer + 1] != (-1);
	}

	/**
	 * @return number of Elements in the list
	 */
	private int countMoves() {
		int i = 0;
		while (moveList[i] != (-1)) {
			i++;
		}
		return i;
	}

	/**
	 * @return complete move-list as array
	 */
	public int[] getMoveList() {
		int[] arr = new int[countMoves()];
		for (int i = 0; i < arr.length; i++)
			arr[i] = moveList[i];
		return arr;
	}
}
