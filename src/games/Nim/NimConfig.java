package games.Nim;

/**
 * A very simple game, mostly used for debugging purposes.
 * <p>
 * There are {@link #NUMBER_HEAPS} heaps, each having initially {@link #HEAP_SIZE} items. There
 * are two players and each player removes between 1 and {@link #MAX_MINUS} items from one heap 
 * in each move. The player who removes the last item wins. 
 *
 */
public class NimConfig {
    final static double REWARD_NEGATIVE = -1.0;
    final static double REWARD_POSITIVE = 1.0;

    /**
     * Number of heaps
     */
    public final static int NUMBER_HEAPS = 3;

    /**
     * Initial heap size (maximum number of items in each heap)
     */
    public final static int HEAP_SIZE = 5;

    /**
     * Maximum number of items to subtract ('minus') from a heap in one move.
     * May not be bigger than {@link #HEAP_SIZE}. If == {@link #HEAP_SIZE}, then each heap can be 
     * erased in one move.
     */
    public final static int MAX_MINUS = 3;

}
