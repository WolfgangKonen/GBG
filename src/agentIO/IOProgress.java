package agentIO;

/**
 * Member {@link #get()} returns the fraction of 'actual/estimated'
 */
public class IOProgress {
    private final long estimated;

    private volatile long actual; // initialized with 0

    public IOProgress(long estimated) {
        this.estimated = estimated;
    }

    public long getEstimated() {
        return estimated;
    }

    public long getActual() {
        return actual;
    }

    public void update(long step) {
        this.actual += step;
    }

    public float get() {
        return actual * 1f / estimated;
    }
}