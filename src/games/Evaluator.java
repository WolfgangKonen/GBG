package games;

import controllers.PlayAgent;

/**
 * Evaluates the performance of a {@link PlayAgent} in a game.<p>
 * 
 * Evaluator is an abstract class which evaluates when an agent meets a certain 
 * criterion for stopEval evaluator calls in succession. This criterion is met once when the
 * method boolean {@link #eval_Agent(PlayAgent)} returns ‘true’. This abstract method {@link #eval_Agent(PlayAgent)} is 
 * defined in child classes of Evaluator. The criterion might be for example a success 
 * rate better than -0.15 in a certain game against Minimax (where 0 is the ideal success rate).
 * <p>  
 * If the Evaluator state stays true for {@code stopEval} calls, then 
 * 		{@link Evaluator#goalReached(int)} 
 * sends a {@code true} signal to the caller, which the caller might use to terminate
 * the training prematurely. 
 * <p> 
 * This class is a base class; derived classes should implement concrete versions of
 * {@link #eval_Agent(PlayAgent)}.
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
abstract public class Evaluator {
	private boolean prevEval;
	private boolean thisEval;
	private int gnumTrue;
	private int m_stopEval;
	private int m_counter;
	
	// these variables may be used by derived classes:
	//
	protected PlayAgent m_PlayAgent;
	protected int verbose=1;
	/**
	 * Derived classes write the result (average success rate) of the last call to method 
	 * {@link #eval_Agent(PlayAgent)} to this variable {@link #lastResult}
	 * @see #getLastResult()
	 */
	protected double lastResult=0.0;
	/**
	 * Derived classes write an info string of the last call to method 
	 * {@link #eval_Agent(PlayAgent)} to this variable {@link #m_msg}
	 * @see #getMsg()
	 */
	protected String m_msg="";
	/**
	 * The mode of the evaluator, to be used by derived classes
	 */
	protected int m_mode=0;
	
	/**
	 * 
	 * @param e_PlayAgent	the agent to evaluate
	 * @param stopEval		how many successfull calls to {@link #eval(PlayAgent)} are needed
	 * 						until {@link #goalReached(int)} returns true
	 */
	public Evaluator(PlayAgent e_PlayAgent, int mode, int stopEval) {
		initEvaluator(e_PlayAgent, mode,stopEval);
	}
	public Evaluator(PlayAgent e_PlayAgent, int mode, int stopEval,int verbose) {
		initEvaluator(e_PlayAgent, mode,stopEval);
		this.verbose = verbose;
	}
	private void initEvaluator(PlayAgent e_PlayAgent, int mode, int stopEval) {
		m_PlayAgent = e_PlayAgent;
		if (!isAvailableMode(mode)) 
			throw new RuntimeException(this.getClass().getSimpleName()+": Value mode = "+mode+" is not allowed!");
		m_mode = mode;
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
	public boolean eval(PlayAgent playAgent) {
		thisEval = eval_Agent(playAgent);
		if (thisEval) {
			m_counter++;
		} else {
			m_counter=0;
		}
		return thisEval;
	}

	/**
	 * This function needs to be implemented in derived classes. It implements the evaluation
	 * of playAgent.
	 * Should write its results on protected members {@link #lastResult} and {@link #m_msg}.
	 * 
	 * @return
	 *  	a boolean predicate (fail/success) for the result of the evaluation. 
	 *  	Might be for example (avg.success &gt; -0.15) when playing TTT against Minimax.
	 *  
	 * @see #getLastResult()
	 * @see #getMsg()
	 */
	abstract protected boolean eval_Agent(PlayAgent playAgent);

	/**
	 * Set member gnumTrue to gameNum if {@link #eval(PlayAgent)} has returned true for 
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
	
	/**
	 * @return
	 *  	the result from the last call to {@link #eval_Agent(PlayAgent)}, which might be for example
	 *  	the average success rate of games played against a Minimax player.
	 */
 	public double getLastResult(){ 
 		return lastResult; 
 	};

	/**
	 * @return long message of evaluator result (may be multi-line) for {@code System.out}
	 */
	public String getMsg(){ 
 		return m_msg;
 	}  
	
	/**
	 * If not overridden by derived class, {@link #getShortMsg()} returns {@link #getMsg()}.
	 * 
	 * @return short message of evaluator result (one line) for status window 
	 */
	public String getShortMsg() {
		return getMsg();
	}
	
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
	 * @return the allowed values for parameter {@code mode} in a call to Evaluator constructor or 
	 *     to {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int, int) Arena.makeEvaluator(...)}.  <br>   
	 *     If an Evaluator does not use {@code mode}, it should return an {@code int[]} 
	 *     containing just 0.
	 */
	abstract public int[] getAvailableModes();
	
	/**
	 * 
	 * @param mode the evaluator mode
	 * @return true, if {@code mode} is in {@link #getAvailableModes()}.
	 */
	public boolean isAvailableMode(int mode) {
//		if (getAvailableModes()==null) return true;
		for (int i : getAvailableModes()) {
			if (mode==i) return true;
		}
		return false;

	}
	
	/**
	 * If not overridden by derived class, {@link #getDefaultEvalMode()} returns 0.
	 * 
	 * @return the default evaluation mode for this {@link Evaluator} 
	 */
	public int getDefaultEvalMode() 
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
	
//	/**
//	 * @return the optional third evaluator mode which may be used in multiTrain.
//	 * 		If the mode returned here equals to the mode of one of the other two 
//	 * 		Evaluator objects, the third Evaluator will be skipped.
//	 */
//	abstract public int getMultiTrainEvalMode();
	
	/**
	 * @return a one line info message, depending on the evaluator's mode
	 */
	abstract public String getPrintString();
	
	/**
	 * Derived classes return a tooltip string with this method. Use 
	 * <pre>
	 *    "&lt;html> ... &lt;br> ... &lt;/html>"  </pre>
	 * to get multi-line tooltip text 
	 * @return the tooltip string for evaluator boxes in "Other pars"
	 */
	// Use "<html> ... <br> ... </html>" to get multi-line tooltip text
	abstract public String getTooltipString();
	abstract public String getPlotTitle();
	
}

