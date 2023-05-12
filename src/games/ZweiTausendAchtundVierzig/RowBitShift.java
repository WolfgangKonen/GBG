package games.ZweiTausendAchtundVierzig;

import games.StateObservation;
import tools.Types;

/**
 * RowBitShift represents an row of the 2048 board in the four lowest hex digits of
 * {@code int rowB}. Digit 3 is the leftmost tile, digit 0 the rightmost tile.  <br>
 * (RowBitShift represents as well columns of the 2048 board, then digit 3 is the highest tile,
 * digit 0 is the lowest tile of a column.) <p>
 *
 * The hex value for each digit is {@code exp} in tile {@code 2^exp}. <p>
 *
 * RowBitShift has methods {@link RowBitShift#lAction()} and {@link RowBitShift#rAction()}
 * for left and right move action according to the rules of 2048. On first pass through
 * these methods, static transposition tables {@code tabLeft} and {@code tabRight} are filled
 * which contain for each possible row value the resulting row. <br>
 * Likewise, static transposition tables {@code scoreLeft} and {@code scoreRight} are filled
 * which contain for each possible row value the resulting score. <p>
 *
 * This speeds up the calculation in {@link StateObservation#advance(Types.ACTIONS, java.util.Random)} by a
 * factor of 10 as compared to {@link StateObservation#advance(Types.ACTIONS, java.util.Random)}.
 *
 * @author Wolfgang Konen, THK
 */
class RowBitShift {
    int rowB;	// the four lowest hex digits (16 bit) of this 32-bit int are used
    int score=0;
    static int[] tabRight = null;
    static int[] tabLeft = null;
    static int[] scoreRight = null;
    static int[] scoreLeft = null;

    public RowBitShift(int row) {
        this.rowB = row;
    }

    // never used
//	public RowBitShift(RowBitShift rbs) {
//		this.rowB = rbs.rowB;
//		this.score = rbs.score;
//	}

    /**
     * Extract the k-th hexadecimal digit
     * @param k	one out of {3,2,1,0}, where 3 is the highest digit
     * @return an int holding the k-th hexadecimal digit
     */
    public int d(int k) {
        if (k>3 || k<0) throw new RuntimeException("k"+k+" is not in allowed range {0,1,2,3}");
        return (rowB >> (k*4)) & 0x0F;
    }

    /**
     * Shift digits from 3 to {@code lower} by one hex digit (4 bit) to the right,
     * but leave the digits below {@code lower} untouched.
     *
     * @param lower one out of {3,2,1,0}, where 3 is the highest digit
     * @return the shifted row
     */
    public RowBitShift rShift(int lower) {
        if (lower>3 || lower<0) throw new RuntimeException("lower"+lower+" is not in allowed range {0,1,2,3}");
        int[] andS = {0xffff, 0xfff0, 0xff00, 0xf000};
        int[] andR = {0x0000, 0x000f, 0x00ff, 0x0fff};
        int shift = (rowB >> 4)  & andS[lower];
        rowB = shift + (rowB & andR[lower]);

        return this;
    }

    /**
     * Shift digits from 0 to {@code higher} by one hex digit (4 bit) to the left,
     * but leave the digits above {@code higher} untouched.
     *
     * @param higher one out of {3,2,1,0}, where 3 is the highest digit
     * @return the shifted row
     */
    public RowBitShift lShift(int higher) {
        if (higher>3 || higher<0) throw new RuntimeException("higher"+higher+" is not in allowed range {0,1,2,3}");
        int[] andS = {0x000f, 0x00ff, 0x0fff, 0xffff};
        int[] andR = {0xfff0, 0xff00, 0xf000, 0x0000};
        int shift = (rowB << 4)  & andS[higher];
        rowB = shift + (rowB & andR[higher]);

        return this;
    }

    /**
     * Merge hex digits {@code r+1} and {@code r} on digit {@code r} (right merge),
     * assuming that they both contain the same, non-zero value.
     * Shift digits above {@code r} accordingly.
     * Leave digits below {@code r} untouched.
     *
     * @param r one out of {2,1,0}
     * @return the merged row
     */
    public RowBitShift rMerge(int r) {
        if (r>2 || r<0) throw new RuntimeException("r="+r+" is not in allowed range {0,1,2}");
        int exp = this.d(r);
        if (exp!=this.d(r+1)) throw new RuntimeException("Digits "+(r+1)+" and "+r+" are not the same");
        if (exp==0) throw new RuntimeException("Digit "+r+" must be greater than zero");

        // andR is a bit mask which lets all digits pass except the two to-be-merged digits:
        int[] andR = {0xff00, 0xf00f, 0x00ff};

        // since each digit holds the exponent exp of tile 2^exp, merging two tiles (doubling)
        // is the same as adding 1 to the exponent:
        int newd = exp+1;

        // the score delta is 2^newd:
        this.score += (1 << newd);

        // shift the merged result back to digit r and add the 'passed' digits:
        rowB = (newd << (4*r)) + (rowB & andR[r]);

        this.rShift(r+1);
        return this;
    }

    /**
     * Merge hex digits {@code r} and {@code r-1} on digit {@code r} (left merge),
     * assuming that they both contain the same, non-zero value.
     * Shift digits below {@code r} accordingly.
     * Leave digits above {@code r} untouched.
     *
     * @param r one out of {3,2,1}
     * @return the merged row
     */
    public RowBitShift lMerge(int r) {
        if (r>3 || r<1) throw new RuntimeException("r="+r+" is not in allowed range {1,2,3}");
        int exp = this.d(r);
        if (exp!=this.d(r-1)) throw new RuntimeException("Digits "+r+" and "+(r-1)+" are not the same");
        if (exp==0) throw new RuntimeException("Digit "+r+" must be greater than zero");

        // andR is a bit mask which lets all digits pass except the two to-be-merged digits:
        int[] andR = {0x0000, 0xff00, 0xf00f, 0x00ff};

        // since each digit holds the exponent exp of tile 2^exp, merging two tiles (doubling)
        // is the same as adding 1 to the exponent:
        int newd = exp+1;

        // the score delta is 2^newd:
        this.score += (1 << newd);

        // shift the merged result back to digit r and add the 'passed' digits:
        rowB = (newd << (4*r)) + (rowB & andR[r]);

        this.lShift(r-1);
        return this;
    }

    /**
     * Perform a "right" action. <br>
     * Use the static transposition table {@code tabRight} to do the job fast. An equivalent
     * but slow version (w/o {@code tabRight}) is in {@link RowBitShift#rActionSlow()}.
     * @return the resulting row object
     */
    public RowBitShift rAction( ) {
        if (tabRight==null) calcTabRight();
        this.score = scoreRight[rowB];
        this.rowB = tabRight[rowB];
        return this;
    }
    private void calcTabRight() {
        int sz = (1 << 16);
        tabRight = new int[sz];
        scoreRight = new int[sz];
        RowBitShift rbs = new RowBitShift(0);
        for (int i=0; i<sz; i++) {
            rbs.rowB=i;
            rbs.score=0;
            tabRight[i]=rbs.rActionSlow().getRow();
            scoreRight[i]=rbs.score;
        }
    }
    private RowBitShift rActionSlow( ) {
        // remove the 'holes' (0-tiles) from left to right:
        for (int k=2; k>=0; k--)
            if (this.d(k)==0) this.rShift(k);

        // merge adjacent same-value tiles from right to left:
        for (int r=0; r<3; r++)
            if (this.d(r+1)==this.d(r) && this.d(r)>0) this.rMerge(r);

        return this;
    }

    /**
     * Perform a "left" action. <br>
     * Use the static transposition table {@code tabLeft} to do the job fast. An equivalent
     * but slow version (w/o {@code tabLeft}) is in {@link RowBitShift#lActionSlow()}.
     * @return the resulting row object
     */
    public RowBitShift lAction( ) {
        if (tabLeft==null) calcTabLeft();
        this.score = scoreLeft[rowB];
        this.rowB = tabLeft[rowB];
        return this;
    }
    private void calcTabLeft() {
        int sz = (1 << 16);
        tabLeft = new int[sz];
        scoreLeft = new int[sz];
        RowBitShift rbs = new RowBitShift(0);
        for (int i=0; i<sz; i++) {
            rbs.rowB=i;
            rbs.score=0;
            tabLeft[i]=rbs.lActionSlow().getRow();
            scoreLeft[i]=rbs.score;
        }
    }
    private RowBitShift lActionSlow( ) {
        // remove the 'holes' (0-tiles) from right to left:
        for (int k=1; k<4; k++)
            if (this.d(k)==0) this.lShift(k);

        // merge adjacent same-value tiles from left to right:
        for (int r=3; r>0; r--)
            if (this.d(r-1)==this.d(r) && this.d(r)>0) this.lMerge(r);

        return this;
    }


    public int getRow() {
        return rowB;
    }
}

