package games;

import controllers.PlayAgent;

/**
 * Evaluates the performance of a {@link PlayAgent} in a game.<p>
 * 
 * Evaluator is an abstract class which evaluates when an agent meets a certain 
 * criterion for stopEval evaluator calls in succession. This criterion is met once when the
 * method boolean {@link #eval_Agent()} returns ‘true’. This abstract method {@link #eval_Agent()} is 
 * defined in child classes of Evaluator. The criterion might be for example a success 
 * rate better than -0.15 in a certain game against Minimax (where 0 is the ideal success rate).
 * <p>  
 * If the Evaluator state stays true for {@code stopEval} calls, then 
 * 		{@link Evaluator#goalReached(int)} 
 * sends a {@code true} signal to the caller, which the caller might use to terminate
 * the training prematurely. 
 * <p> 
 * This class is a base class; derived classes should implement concrete versions of
 * {@link #eval_Agent()}.
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
abstract public class Evaluator {
	private boolean prevEval;
	private boolean thisEval;
	private int gnumTrue;
	private int m_stopEval;
	private int m_counter;
	protected PlayAgent m_PlayAgent;
	protected int verbose=1;
	
	/**
	 * 
	 * @param e_PlayAgent	the agent to evaluate
	 * @param stopEval		how many successfull calls to {@link #eval()} are needed
	 * 						until {@link #goalReached(int)} returns true
	 */
	public Evaluator(PlayAgent e_PlayAgent, int stopEval) {
		initEvaluator(e_PlayAgent,stopEval);
	}
	public Evaluator(PlayAgent e_PlayAgent, int stopEval,int verbose) {
		initEvaluator(e_PlayAgent,stopEval);
		this.verbose = verbose;
	}
	private void initEvaluator(PlayAgent e_PlayAgent, int stopEval) {
		m_PlayAgent = e_PlayAgent;
		prevEval = false;
		thisEval = false;
		m_stopEval=stopEval;
		m_counter=0;
		gnumTrue=-2;
	}
	
	/**
	 * Calls {@link #eval_Agent} which returns a boolean predicate (fail/success). It counts
	 * the number of consecutive successes. If this number reaches m_stopEval, the 
	 * method {@link #goalReached(int)} will return true.
	 * @return
	 * 		boolean predicate from {@link #eval_Agent}
	 */
	public boolean eval() {
		thisEval = eval_Agent();
		if (thisEval) {
			m_counter++;
		} else {
			m_counter=0;
		}
		return thisEval;
	}

	/**
	 * This function needs to be implemented in derived classes.
	 * 
	 * @return
	 *  	a boolean predicate (fail/success) for the result of the evaluation. 
	 *  	Might be for example (avg.success > -0.15) when playing TTT against Minimax.
	 */
	abstract protected boolean eval_Agent();

	/**
	 * This function needs to be implemented in derived classes.
	 * 
	 * @return
	 *  	the result from the last call to {@link #eval_Agent()}, which might be for example
	 *  	the average success rate of games played against a Minimax player.
	 */
 	abstract public double getLastResult();

	/**
	 * Set member gnumTrue to gameNum if {@link #eval()} has returned true for 
	 * {@code stopEval} consecutive calls. 
	 * @param gameNum	number of training games
	 * @return true if Evaluator stays true for {@code stopEval} calls, false else
	 */
	public boolean goalReached(int gameNum) {			
//		if (thisEval && !prevEval) gnumTrue=gameNum;
//		if (!thisEval) gnumTrue=-1;
//		prevEval = thisEval;
//		if (gnumTrue>0 && gameNum>gnumTrue+m_stopEval-1) {
		if (m_counter>=m_stopEval) {
			gnumTrue=gameNum;
			return true;
		}
		return false;
	}
	public boolean setState(boolean stateE) { thisEval = stateE; return stateE; }
	public boolean getState() { return thisEval; }
	
	abstract public String getMsg(); 
//	{
//		// dummy, to fulfill the interface (should be overridden by derived classes)
//		return getGoalMsg(0);
//	}
	
	public String getGoalMsg(int gameNum) {
		String msg;
		if (gnumTrue==-2) {
			msg = getClass().getName()+": Goal not yet reached";
		} else {
			msg = ", training stopped at gameNum="+gameNum;
			msg = getClass().getName()+": Goal reached (stopEval="+m_stopEval+")"+msg;
		}
		return msg;
	}
	
	/**
	 * 
	 * @param mode
	 * @return true, if {@code mode} is in {@link #getAvailableModes} or 
	 * 		if Evaluator does not use {@code mode}.
	 */
	abstract public boolean isAvailableMode(int mode);
	
	/**
	 * @return the allowed values for parameter {@code mode} in a call 
	 *     to {@code Arena.makeEvaluator}.     
	 *     If an Evaluator does not use {@code mode}, it returns null.
	 */
	abstract public int[] getAvailableModes();
	
	public static int getDefaultEvalMode() 
	{
		return 0;
	}
	
	/**
	 * @return the initial mode selected in OtherPars choice box 'Quick Eval Mode'
	 */
	abstract public int getQuickEvalMode();
	/**
	 * @return the initial mode selected in OtherPars choice box 'Train Eval Mode'
	 */
	abstract public int getTrainEvalMode();
	
	/**
	 * @return the optional third evaluator mode which may be used in multiTrain.
	 * 		If the mode returned here equals to the mode of one of the other two 
	 * 		Evaluator objects, the third Evaluator will be skipped.
	 */
	abstract public int getMultiTrainEvalMode();
	
	abstract public String getPrintString();
	abstract public String getPlotTitle();
	
}

