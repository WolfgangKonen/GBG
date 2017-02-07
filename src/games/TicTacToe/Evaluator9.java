package games.TicTacToe;

import java.text.DecimalFormat;

import controllers.PlayAgent;
import controllers.TD.TDAgent;
import controllers.MinimaxAgent;
import games.Evaluator;
import tools.Types;


/**
 * Evaluate for TicTacToe when the actually trained agent starts to play perfect. 
 * This is a concrete implementation of class {@link Evaluator} using the rate of successful
 * decisions on certain states and the game function delta to a perfect minimax player on 
 * these states, see documentation of {@link #evalAgent1(PlayAgent,boolean)} for details.
 */
public class Evaluator9 extends Evaluator {
	private StateStorage m_ss1;				// state storage for evalAgent1()			
	private StateStorage m_ss2;				// state storage for evalAgent2()
	private double m_MixDelta;				// see evalAgent
	private String m_msg;
	protected double m_thresh=0.85;
	protected double m_res;

	/**
	 * StateStorage contains variables to speed up {@code #evalAgent1(PlayAgent,boolean)}  & 
	 * {@link #evalAgent2(PlayAgent, boolean)} for all but the first pass.
	 * (Because evalAgent-functions might be heavily used in CMAPlayer as fitness calculator)
	 */
	class StateStorage {
		public boolean first_eval = true;	// true on first pass through innerEval(...,StateStorage,...)
		public String[] m_state = null;		// storage for each state in innerEval(...,StateStorage,...)
		public String[] m_refState;			// storage for each next state the referee suggests in innerEval(...,StateStorage,...)		
		public double[] m_VM_s;				// storage for the referee score for m_refState
	}
									
	public Evaluator9(PlayAgent e_PlayAgent, int stopEval) {
		super(e_PlayAgent, stopEval);
        m_ss1 = new StateStorage();
        m_ss2 = new StateStorage();
        m_MixDelta = 0.2; 
	}
	
//	public boolean eval() {
//		return setState(eval_Agent());
//	}
	
	@Override
	public boolean eval_Agent() {
		return evalAgent1(m_PlayAgent,true)>m_thresh;
	}

	/**
	 * Evaluate the performance of the given {@code PlayAgent pa} by measuring its
	 * reaction on certain game states {@code state[k]} <ul>
	 *  <li> Is the score V_pa(s) returned for s=state[k] close to the desired value 
	 *  	 V_ref(s) = score from minimax agent? Same for (n)=(next state).
	 *  <li> n_pa: the next state suggested by pa, n_ref: the next state suggested by minimax agent
	 *  <li> Is the next move suggested for state[k] a <b>success</b>, i.e. has the next state the   
	 *       same minimax score as the minimax score V_m(state[k])? If so, then result = OK.
	 *       success = percentage of states with result = OK.
	 *  <li> <b>delta</b>: avg. absolute difference between V_pa(s) and V_ref(s). If m_MixDelta \> 0 
	 *   	 then 1-delta is mixed with success by this proportion in the return value   
	 *  <li> no.: the number of the state
	 *  <li> if pa is a ValItPlayer or TDSPlayer, then 'C: c0,c1' shows the counters for that state
	 *       (c0: how often visited during training, c1: how often trained (updated))     
	 *  <li> if pa is a TDSPlayer, then 'Div: (0/0/8) OK' shows the result from 
	 *  	 {@link games.TicTDBase#diversityCheck(String)}: Does the feature vector corresponding to 
	 *  	 state s have a non-diverging set of scores?    
	 *  </ul> 
	 * Note that with this definition of result we can handle in a consistent way cases 
	 * with <b>several</b> best moves (!) <p>
	 * (The success-criterion is by far more important than the V scores, the function prints 
	 * on System.out the success rate averaged over all states)
	 * @param pa	the PlayAgent to evaluate
	 * @param silent
	 * @return 		(1 - m_MixDelta)*(the success rate averaged over all states)
	 * 				+  m_MixDelta*(1-delta)  
	 * <p>with class member m_MixDelta
	 */
	public double evalAgent1(PlayAgent pa, boolean silent) {
		String state[] = {"XX-oo-X--","oo-XX-X--","-o-oXX-X-",
						  "ooXXX-o--","oo-XX-oX-","-X-Xoo---",
						  "X--------","o--XoX-X-","-X--oX-o-",
						  "----X----","---------","X---X-Xoo",
						  "XoXXo---o","XXoXo----","XoX------",
						  "---XoX---","------XoX","X---o---X",
						  "X-oo-XX-o","-o--X----","----X--o-",
						  "----Xo---","oo-oXX-X-","-ooXXo-X-"};
		return innerEval(pa, silent, state, m_ss1);
	}
	
	/**
	 * Same as {@link #evalAgent1(PlayAgent, boolean)}, but the point-reflections of states are 
	 * also added to the state set.
	 * @see #evalAgent1(PlayAgent, boolean) 
	 */
	public double evalAgent2(PlayAgent pa, boolean silent) {
		String state[] = {"XX-oo-X--","oo-XX-X--","-o-oXX-X-",
						  "ooXXX-o--","oo-XX-oX-","-X-Xoo---",
						  "X--------","o--XoX-X-","-X--oX-o-",
						  "----X----","---------","X---X-Xoo",
						  "XoXXo---o","XXoXo----","XoX------",
						  "---XoX---","------XoX","X---o---X",
						  "X-oo-XX-o","-o--X----","----X--o-",
						  "----Xo---","oo-oXX-X-","-ooXXo-X-"};
		if (m_ss2.first_eval==true) {
			m_ss2.m_state = addMirrorReflection(state);
		}
		return innerEval(pa, silent, m_ss2.m_state, m_ss2);
	}
	
	/**
	 *  Internal routine for the public {@link #evalAgent1(PlayAgent, boolean)} 
	 *  & {@link #evalAgent2(PlayAgent, boolean)}
	 *	@see #evalAgent1(PlayAgent, boolean) 
	 */
	private double innerEval(PlayAgent pa, boolean silent, String state[], StateStorage m_ss) {
		String res,newState,refState;
		DecimalFormat frm = new DecimalFormat("+0.000;-0.000");
		DecimalFormat frM = new DecimalFormat("+0.0;-0.0");
		DecimalFormat frk = new DecimalFormat("00");
		String sMinimax = Types.GUI_AGENT_LIST[1];
		MinimaxAgent referee = new MinimaxAgent(sMinimax);
		double[] VTable = null;
		Types.ACTIONS actBest;
		int[][] rtable = new int[3][3];	// referee (Minimax) table for next move
		int[][] stable = new int[3][3];	// save copy of table, needed for getCounters
		double VM_s, VM_n;				// scores that Minimax assigns to state[k], next_state[k]
		double V_s, V_n;				// scores that Agent pa assigns to state[k], next_state[k]
		double delta=0.0;				// avg. absolute difference between VM_s and V_s
		int count=0;
		int table[][] = new int[3][3];
		//long nStart = System.currentTimeMillis();
		
		boolean is01player = false;		// signals a player with score V in 0..1 (else: -1..1)
		if (pa.getClass().getName().equals("controllers.TD.TDAgent")) is01player=true;
		if (pa.getClass().getName().equals("TicTacToe.ValItPlayer")) is01player=true;
		if (pa.getClass().getName().equals("TicTacToe.CMAPlayer")) is01player=true;
		if (m_ss.first_eval) {
			m_ss.m_VM_s = new double[state.length];
			m_ss.m_refState = new String[state.length];
		}
		
		// header line for the first 30 detailed results:
		if (!silent)
			System.out.println("state s: V_ref(s) V_pa(s) V_ref(n) V_pa(n) >> "+
					"n_pa (ref: n_ref) result no.");
		
		for (int k=0; k<state.length; ++k) {
			int player = string2table(state[k],table);
			// table now contains the initial state (state[k])

			StateObserverTTT SO = new StateObserverTTT(table,player);
			StateObserverTTT rSO = SO.copy();
			StateObserverTTT sSO = SO.copy();
			
			if (m_ss.first_eval) {
				VM_s = referee.getScore(SO);
				m_ss.m_VM_s[k] = VM_s;
				int n=rSO.getNumAvailableActions();
				VTable	= new double[n+1];
				actBest = referee.getNextAction(rSO, false, VTable, true);
				rSO.advance(actBest);
				rtable = rSO.getTable();
				m_ss.m_refState[k] = table2string(rtable);
			} else {
				VM_s = m_ss.m_VM_s[k];
			}
			refState = m_ss.m_refState[k];
			V_s = pa.getScore(SO);
			if (Double.isNaN(V_s)) {
				//
				// CAUTION: delta may become NaN if V_s returns NaN (may happen for neural net + lambda=1.0
				// where lambda=0.99 is o.k.) --> we fix this symptomatically with a warning:
				//
				System.out.println(" *** Warning: agent pa returns NaN score in Evaluator1::innerEval() ***");
				System.out.println(" *** --> will change it to a very low score -10000");
				V_s = -10000;
			}
			delta += Math.abs(VM_s-V_s);
			if (is01player) V_s=2*V_s-1;				// map score to -1..+1
			if (TicTDBase.Win(table,-1)) V_s=-1.0;
			if (TicTDBase.Win(table,+1)) V_s=+1.0;
			int n=SO.getNumAvailableActions();
			VTable	= new double[n+1];
			actBest = pa.getNextAction(SO, false, VTable, true);	 
			SO.advance(actBest);			// SO contains now the after state,
											// after pa made its move
			
			VM_n = referee.getScore(SO);	// we have to calculate VM_n each time anew,
											// since state n = pa.getNextMove() can 
											// change from call to call			
			V_n = pa.getScore(SO);
			if (is01player) V_n=2*V_n-1;				// map score to -1..+1
			if (TicTDBase.Win(table,-1)) V_n=-1.0;
			if (TicTDBase.Win(table,+1)) V_n=+1.0;
			newState=table2string(table);
				
			if (VM_s==VM_n) {		// does referee (Minimax) assign state[k] and next_state[k] the same value?
				res="OK";			//		If so, then the move of agent pa was a correct one.
				count++;			//		(it does not need to be the same move as Minimax suggest, due to symmetries)
			} else {
				res=" F";
				SO = sSO.copy();	// go back to initial state
				//double [][] Vtab = pa.getNextMove(table,player,true);
			}
			
			if (k<30 & !silent) {
				String sfinal = "";
				System.out.print(state[k] + ": " + frM.format(VM_s) + " " + frm.format(V_s) + " " + 
						frM.format(VM_n) + " " + frm.format(V_n) + 
						" >>  " + newState + "  (ref: " + refState +  " )  " + res + " " + frk.format(k));
//				if (pa instanceof TDPlayerTT2) {
//					int[] cnt = ((TDPlayerTT2) pa).getCounters(player,stable); // counters for initial state
//					sfinal = " C:" + cnt[0] +", " + cnt[1] + ((TDPlayerTT2) pa).diversityCheck(state[k]); 
//				}
				System.out.println(sfinal);				
			}
		} // for (k)
		
		
		//long nFinish = 	System.currentTimeMillis();
		//long elapsed_msec = (nFinish-nStart); 
		//System.out.println("elapsed msec = " + elapsed_msec);
		// --> measured time diff: 1300 msec for first_eval=true, 232 msec for first_eval=false
		// --> resp. for evalAgent2: 2300 msec for first_eval=true, 125 msec thereafter 
		m_ss.first_eval=false;
		
		double suc=(double)count/state.length;
		delta /= (2*state.length);				// now delta is between 0 (best) and 1 (worst)
		m_msg = "Evaluator1: success rate = " + frm.format(suc) + 
				"  (" + count +" of " + state.length + " states)" +
				", delta = " + frm.format(delta) + ", mixDelta = " + m_MixDelta;
		if (!silent) {
			if (pa instanceof TDAgent) {
				// side effect: getAlpha prints some  
				// debug info, if verbose1,2>0 in source code getAlpha:
				System.out.println("alpha final: "+	
									frm.format(((TDAgent) pa).getAlpha()));
				System.out.println(m_msg);
			}
		}

		m_res = (1-m_MixDelta)*suc+m_MixDelta*(1-delta);
		return m_res;
	}
	
	private String[] addMirrorReflection(String[] state) {
		String[] state2 = new String[2*state.length];
		int i,j;
		for (i=0,j=state.length; i<state.length; i++,j++) {
			state2[i] = state[i];
			state2[j] = new StringBuffer(state[i]).reverse().toString();
		}
		return state2;
	}
	
	/** Transform a 3x3 board table into a string like "Xo-X---o-" 
	 * 
	 * @param table		on input an allocated memory area, on output it contains the board position
	 * @return			a string like "Xo-X---o-" coding a board position
	 */
	private String table2string(int[][] table) {
		String newState,s;
		newState="";
		for (int i=0;i<3;++i)
			for (int j=0;j<3;++j) {
				if (table[i][j]==-1) s="o";
				else if (table[i][j]==+1) s="X";
				else s = "-";
				newState = newState+s;
			}
		return newState;
	}
	
	/** Transform a string like "Xo-X---o-" into its board position and the player 
	 *  who makes the next move 
	 * 
	 * @param state_k	a string like "Xo-X---o-" coding a board position
	 * @param table		on input an allocated memory area, on output it contains the board position
	 * @return			+1 or -1 if either X or O makes the next move
	 */
	public static int string2table(String state_k, int table[][]) {
		int Xcount=0, Ocount=0;
		for (int i=0;i<3;++i)
			for (int j=0;j<3;++j) {
				if (state_k.charAt(3*i+j)=='X') {
					table[i][j]=+1;
					Xcount++;
				}
				else if (state_k.charAt(3*i+j)=='o') {
					table[i][j]=-1;
					Ocount++;
				}
				else {
					table[i][j]=0;					
				}
			}
		int player = (Ocount-Xcount)*2+1;	// the player who makes the next move
		if (player!=-1 && player!=1)
			throw new RuntimeException("invalid state!!");
		return player;
	}
	
	public double get_MixDelta() {
		return m_MixDelta;
	}

	public void set_MixDelta(double mixDelta) {
		m_MixDelta = mixDelta;
	}
	
 	@Override
 	public double getLastResult() { return m_res; }
 	
 	@Override
 	public String getMsg() { return m_msg; } 
}

