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
	
	public boolean eval() {
		thisEval = eval_Agent();
		if (thisEval) {
			m_counter++;
		} else {
			m_counter=0;
		}
		return thisEval;
	}

	abstract protected boolean eval_Agent();

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
	
	public String getMsg() {
		// dummy, to fulfill the interface (should be overridden by derived classes)
		return getMsg(0);
	}
	public String getMsg(int gameNum) {
		String msg;
		if (gnumTrue==-2) {
			msg = getClass().getName()+": Goal not yet reached";
		} else {
			msg = ", training stopped at gameNum="+gameNum;
			msg = getClass().getName()+": Goal reached (stopEval="+m_stopEval+")"+msg;
		}
		return msg;
	}
}

