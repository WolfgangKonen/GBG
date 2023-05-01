package games;

import agentIO.AgentLoader;
import controllers.PlayAgent;

/**
 * Evaluates the performance of a {@link PlayAgent} in a game.<p>
 * 
 * Evaluator is an abstract class which evaluates when an agent meets a certain 
 * criterion. This criterion is met once when the
 * method {@link EvalResult} {@link #evalAgent(PlayAgent)} returns {@code true} in its member {@code success}.
 * This abstract method {@link #evalAgent(PlayAgent)} is
 * defined in child classes of Evaluator. The criterion might be for example a success rate better
 * than -0.15 in a certain set of episodes against MaxNAgent (where 0 may be the ideal success rate).
 * <p>  
 * This class is a base class; derived classes should implement concrete versions of
 * {@link #evalAgent(PlayAgent)}.
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2023
 */
// * If the Evaluator state stays true for {@code stopEval} calls, then
//		* 		{@link Evaluator#goalReached(int)}
//		* sends a {@code true} signal to the caller, which the caller might use to terminate
//		* the training prematurely.
//		* <p>
abstract public class Evaluator {
	private EvalResult thisEval;
	private int m_counter;			// counts the number of successful calls to eval().
    private AgentLoader agtLoader = null;
	//private int gnumTrue;			// obsolete
	//private int m_stopEval;		// obsolete now (stopEval only used with new meaning in ParOther)

	// these variables may be used by derived classes:
	//
	protected PlayAgent m_PlayAgent;
	protected int verbose=1;
	/**
	 * Derived classes write the result (average success rate) of the last call to method 
	 * {@link #evalAgent(PlayAgent)} to this variable {@code #lastResult}
	 */
	protected double lastResult=0.0;
	/**
	 * Derived classes write a one-line info string describing the result of the last call to method
	 * {@link #evalAgent(PlayAgent)} to this variable {@code #m_msg}
	 */
	protected String m_msg="";
	/**
	 * The mode of the evaluator, to be used by derived classes
	 */
	protected int m_mode=0;
	/**
	 * Needed in {@link #getTDReferee()} to access game-specific properties. 
	 * May be used in derived classes for the same purposes. 
	 */
    protected GameBoard m_gb;

	/**
	 *  @param e_PlayAgent    the agent to evaluate
	 *
	 */
	public Evaluator(PlayAgent e_PlayAgent, GameBoard gb, int mode) {
		initEvaluator(e_PlayAgent, gb, mode);
	}
	public Evaluator(PlayAgent e_PlayAgent, GameBoard gb, int mode, int verbose) {
		initEvaluator(e_PlayAgent, gb, mode);
		this.verbose = verbose;
	}
	private void initEvaluator(PlayAgent e_PlayAgent, GameBoard gb, int mode) {
		m_PlayAgent = e_PlayAgent;
		m_gb = gb;
		if (!isAvailableMode(mode)) 
			throw new RuntimeException(this.getClass().getSimpleName()+": Value mode = "+mode+" is not allowed!");
		m_mode = mode;
		thisEval = new EvalResult();
		m_counter=0;
		//m_stopEval=stopEval;
		//gnumTrue=-2;
	}
	
	/**
	 * Calls {@link #evalAgent(PlayAgent) evalAgent} which returns {@link EvalResult}. It counts
	 * in member {@code m_counter} the number of consecutive successes.
	 * @return
	 * 		the evaluation result from {@link #evalAgent(PlayAgent)}
	 */
	public EvalResult eval(PlayAgent playAgent) {
		thisEval = evalAgent(playAgent);
		if (thisEval.isSuccess()) {
			m_counter++;
		} else {
			m_counter=0;
		}
		return thisEval;
	}

	/**
	 * This function has to be implemented by the derived classes. It implements the evaluation
	 * of playAgent.
	 * It returns its results in a {@link EvalResult} object.
	 * 
	 * @param playAgent the agent to be evaluated
	 * @return
	 *  	the evaluation result object (holds double result, String msg and more)
	 *  
	 * @see EvalResult
	 */
	abstract protected EvalResult evalAgent(PlayAgent playAgent);

	// --- obsolete, we strip off stopTest functionality ---
//	/**
//	 * Set member gnumTrue to gameNum if {@link #eval(PlayAgent)} has returned true for
//	 * {@code stopEval} consecutive calls.
//	 * @param gameNum	number of training games
//	 * @return true if Evaluator stays true for {@code stopEval} calls, false else
//	 */
//	public boolean goalReached(int gameNum) {
//		if (m_counter>=m_stopEval) {
//			gnumTrue=gameNum;
//			return true;
//		}
//		return false;
//	}

	/**
	 * @return how often the call to {@link #eval(PlayAgent)} returned with {@code success == true}
	 */
	public int getCounter() {
		return m_counter;
	}

	/**
	 * 
	 * @return the agent stored in TDReferee.agt.zip 
	 * @throws RuntimeException if TDReferee.agt.zip is not found (in the game-specific directory)
	 */
	public PlayAgent getTDReferee() throws RuntimeException {
		if (agtLoader==null) agtLoader = new AgentLoader(m_gb.getArena(),"TDReferee.agt.zip");
		if (agtLoader.getAgent() == null) 
			throw new RuntimeException(agtLoader.getLoadMsg());
		return agtLoader.getAgent();
	}

	// --- now obsolete, we use EvalResult.getResult() and EvalResult.getMsg() instead ---
//	/**
//	 * @return
//	 *  	the result from the last call to {@link #evalAgent(PlayAgent)}, which might be for example
//	 *  	the average success rate of games played against a MaxNAgent player.
//	 */
// 	public double getLastResult(){
// 		return lastResult;
// 	}
//
//	/**
//	 * @return long message of evaluator result (may be multi-line) for {@code System.out}
//	 */
//	public String getMsg(){
// 		return m_msg;
// 	}

 	// --- never used ---
//	/**
//	 * If not overridden by derived class, this method returns {@link #getMsg()}.
//	 *
//	 * @return short message of evaluator result (one line) for status window
//	 */
//	public String getShortMsg() {
//		return getMsg();
//	}

	// --- obsolete, we strip off stopTest functionality ---
//	public String getGoalMsg(int gameNum) {
//		String msg;
//		if (gnumTrue==-2) {
//			msg = getClass().getName()+": Goal not yet reached";
//		} else {
//			msg = ", training stopped at gameNum="+gameNum;
//			msg = getClass().getName()+": Goal reached (stopEval="+m_stopEval+")"+msg;
//		}
//		return msg;
//	}
	
	/**
	 * @return the allowed values for parameter {@code mode} in a call to Evaluator constructor or 
	 *     to {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int) Arena.makeEvaluator(...)}.  <br>
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
	
	public int getMode() {
		return m_mode;
	}
	
	/**
	 * If not overridden by derived class, this method returns 0.
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
	
	/**
	 * A one-line info string characterizing the Evaluator and its specific mode {@code m_mode}.
	 * Is used to <b>build</b> the evaluation message on {@link #m_msg} (combining with the evaluation result), but does
	 * <b>not</b> contain the evaluation result itself.
	 * @return a one-line info string
	 */
	abstract public String getPrintString();
	
	/**
	 * Derived classes return a tooltip string with this method. Use 
	 * <pre>
	 *    "&lt;html&gt; ... &lt;br&gt; ... &lt;/html&gt;"  </pre>
	 * to get multi-line tooltip text 
	 * @return the tooltip string for evaluator boxes in "Other pars"
	 */
	// Use "<html> ... <br> ... </html>" to get multi-line tooltip text
	abstract public String getTooltipString();
	abstract public String getPlotTitle();
	
}

