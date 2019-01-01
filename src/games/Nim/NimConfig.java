package games.Nim;

public class NimConfig {
    final static double REWARD_NEGATIVE = -1.0;
    final static double REWARD_POSITIVE = 1.0;

    /**
     * Number of heaps
     */
    public final static int NUMBER_HEAPS = 2;

    /**
     * Initial heap size (maximum number of items in each heap)
     */
    public final static int HEAP_SIZE = 5;

    /**
     * Maximum number of items to subtract from a heap in one move.
     * May not be bigger than HEAP_SIZE.
     */
    public final static int MAX_SUB = 3;

}
