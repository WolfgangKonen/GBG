package games.Nim;

import controllers.TD.ntuple4.TDNTuple4Agt;

/**
 * Configurations for the games Nim and Nim3P.
 * <p>
 * There are {@link #NUMBER_HEAPS} heaps, each having initially {@link #HEAP_SIZE} items. There
 * are two players and each player removes between 1 and {@link #MAX_MINUS} items from one heap 
 * in each move. The player who removes the last item wins. 
 */
public class NimConfig {
    final static double REWARD_NEGATIVE = -1.0;
    final static double REWARD_POSITIVE = 1.0;

    /**
     * Number of heaps
     */
    public static int NUMBER_HEAPS = 3;

    /**
     * Initial heap size (number of items in each heap).
     * If == -1, a special heap structure is used (needed for {@link ludiiInterface.games.Nim.SystemConversionNim},
     * because Ludii supports only this special heap structure):
     * The heap size for the central heap is {@link #NUMBER_HEAPS} and decreases by 1 for each neighbouring heap.
     * For example, with {@link #NUMBER_HEAPS} = 5 the heap structure is [3 4 5 4 3].
     */
    public static int HEAP_SIZE = 5; // 50

    /**
     * Maximum number of items to subtract ('minus') from a heap in one move.
     * May not be bigger than {@link #HEAP_SIZE}. If == {@link #HEAP_SIZE}, then each heap can be 
     * cleared in one move.
     */
    public static int MAX_MINUS = 3;

    /**
     * If {@code true}, then invoke the project mechanism in {@link StateObserverNim#project()} that sorts the heaps
     * according to their heap size in ascending order. <br>
     * If {@code false}, do nothing.
     * @see TDNTuple4Agt
     */
    public static boolean PROJECT = true;

	/**
	 * Only for Nim3P: If true, activate this optional extra rule: In addition to the winner, who gets reward 1: 
	 * The player who is the successor of the winner, gets the extra reward 0.2 (instead of 0).
	 */
	protected static boolean EXTRA_RULE = true;
}
