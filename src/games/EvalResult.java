package games;

import controllers.PlayAgent;
import games.ZweiTausendAchtundVierzig.EResult2048;

/**
 * Result of a call to {@link Evaluator#eval(PlayAgent)}, it holds
 * <ul>
 *     <li> double result
 *     <li> boolean success (whether result &gt thresh)
 *     <li> String message
 *     <li> int mode (the {@link Evaluator} mode)
 *     <li> double thresh (for success test)
 * </ul>
 */
public class EvalResult {
    protected double result = 0.0;        // the evaluation result
    protected boolean success = false;    // whether result > thresh
    protected String msg = null;          // the evaluation message
    protected int mode = 0;               // the mode of the Evaluator
    protected double thresh = 0.0;        // the threshold for this mode, passed in from Evaluator

    /**
     * The default constructor has {@code this.msg = null} which signals that this {@code EvalResult} object
     * is a placeholder and was not constructed by an {@link Evaluator}.
     */
    public EvalResult() {
        this.msg = null;
    }

    public EvalResult(double result, boolean success, String msg, int mode, double thresh) {
        this.result = result;
        this.success = success;
        this.msg = msg;
        this.mode = mode;
        this.thresh = thresh;
    }

    public double getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * @return one-line string describing evaluation result
     */
    public String getMsg() {
        return msg;
    }

    /**
     * The default is to return {@code msg}. May be overridden by derived classes to return a multi-line report
     * of the evaluation result.
     * @return  multi-line string (report) of evaluation result
     * @see EResult2048
     */
    public String getReport() {
        return msg;
    }

    public int getMode() {
        return mode;
    }

    public double getThresh() {
        return thresh;
    }
}
