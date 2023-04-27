package games;

import controllers.PlayAgent;

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
    private double result = 0.0;        // the evaluation result
    private boolean success = false;    // whether result > thresh
    private String msg = null;          // the evaluation message
    private int mode = 0;               // the mode of the Evaluator
    private double thresh = 0.0;        // the threshold for this mode, passed in from Evaluator

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

    public String getMsg() {
        return msg;
    }

    public int getMode() {
        return mode;
    }

    public double getThresh() {
        return thresh;
    }
}
