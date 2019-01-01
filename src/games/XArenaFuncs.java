package games;

import TournamentSystem.TSTimeStorage;
import TournamentSystem.tools.TSGameDataTransfer;
import controllers.*;
import controllers.MC.MCAgent;
import controllers.MC.MCAgentN;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent.AgentState;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.TStats.TAggreg;
import params.*;
import tools.*;
import tools.Types.ACTIONS;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Class {@link XArenaFuncs} contains several methods to train, evaluate and measure the 
 * performance of agents. <ul>
 * <li> train:		train an agent one time for maxGameNum games and evaluate it with evalAgent
 * <li> multiTrain: train an agent multiple times and evaluate it with evalAgent
 * <li> compete:	one competition 'X vs. O', several games, measure win/tie/loose rate
 * <li> competeBoth call compete for pair (pa,opponent) in both roles, X and O  
 * <li> multiCompete: many competitions, measure win/tie/loose rate and avg. correct moves
 * <li> eval: 	(as part of the protected {@link Evaluator} elements) measure agent success
 * </ul> 
 * --- Batch methods are now in TicTacToeBatch ---
 * <p>
 * Known classes having {@link XArenaFuncs} objects as members: 
 * 		{@link Arena}, {@link XArenaButtons} 
 * 
 * @author Wolfgang Konen, TH K�ln, Nov'16
 * 
 */
public class XArenaFuncs 
{
	public  PlayAgent[] m_PlayAgents;
	private Arena m_Arena;
	protected Evaluator m_evaluatorT=null;
	protected Evaluator m_evaluatorQ=null;
//	protected Evaluator m_evaluatorM=null;
	protected String lastMsg="";
	protected int numPlayers;
	
	protected Random rand;
	protected LineChartSuccess lChart;

	private final String TAG = "[XArenaFuncs] ";

	protected DeviationWeightsChart wChart;
	/**
	 * percentiles for weight chart plot on wChart (only relevant for TDNTuple2Agt)
	 */
	double[] per = {5,25,50,75,95};

	public XArenaFuncs(Arena arena)
	{
		m_Arena = arena;
		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		m_PlayAgents = new PlayAgent[numPlayers];
		//m_PlayAgents[0] = new MinimaxAgent(sMinimax);
        rand = new Random(System.currentTimeMillis());
        lChart=new LineChartSuccess("Training Progress","gameNum","",true,false);
        wChart=new DeviationWeightsChart("","gameNum","",true,false);
	}
	
	/**
	 * Construct and return a new {@link PlayAgent}, based on the settings in 
	 * {@code sAgent} and {@code m_xab}. 
	 * <p>
	 * @param sAgent	the string from the agent-select box
	 * @param m_xab		used only for reading parameter values from GUI members 
	 * @return			a new {@link PlayAgent} (initialized, but not yet trained)
	 * @throws IOException 
	 */
	protected PlayAgent constructAgent(int n, String sAgent, XArenaButtons m_xab) throws IOException {
		PlayAgent pa = null;
		int maxGameNum=Integer.parseInt(m_xab.GameNumT.getText());
		int featmode = m_xab.tdPar[n].getFeatmode();
		
		if (sAgent.equals("TDS")) {
			Feature feat = m_xab.m_game.makeFeatureClass(m_xab.tdPar[n].getFeatmode());
			pa = new TDAgent(sAgent, new ParTD(m_xab.tdPar[n]), new ParOther(m_xab.oPar[n]), feat, maxGameNum);
			//pa = m_xab.m_game.makeTDSAgent(sAgent,m_xab.tdPar,maxGameNum); 
				// new TDPlayerTTT(sAgent,m_xab.tdPar,maxGameNum);
		} else if (sAgent.equals("TD-Ntuple-2")) {
			try {
				XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory(); 
				int[][] nTuples = ntupfac.makeNTupleSet(new ParNT(m_xab.ntPar[n]), xnf);
				pa = new TDNTuple2Agt(sAgent, new ParTD(m_xab.tdPar[n]), new ParNT(m_xab.ntPar[n]), 
									  new ParOther(m_xab.oPar[n]), nTuples, xnf, maxGameNum);
			} catch (Exception e) {
				MessageBox.show(m_xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				//e.printStackTrace();
				pa=null;			
			}
		} else if (sAgent.equals("TD-Ntuple-3")) {
			try {
				XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory(); 
				int[][] nTuples = ntupfac.makeNTupleSet(new ParNT(m_xab.ntPar[n]), xnf);
				pa = new TDNTuple3Agt(sAgent, new ParTD(m_xab.tdPar[n]), new ParNT(m_xab.ntPar[n]), 
									  new ParOther(m_xab.oPar[n]), nTuples, xnf, maxGameNum);
			} catch (Exception e) {
				MessageBox.show(m_xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				//e.printStackTrace();
				pa=null;			
			}
		} else if (sAgent.equals("Sarsa")) {
			try {
				XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory(); 
				int[][] nTuples = ntupfac.makeNTupleSet(new ParNT(m_xab.ntPar[n]), xnf);
				int numOutputs = m_xab.m_game.gb.getDefaultStartState().getAllAvailableActions().size();
				pa = new SarsaAgt(sAgent, new ParTD(m_xab.tdPar[n]), new ParNT(m_xab.ntPar[n]), 
									  new ParOther(m_xab.oPar[n]), nTuples, xnf, numOutputs, maxGameNum);
			} catch (Exception e) {
				MessageBox.show(m_xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				//e.printStackTrace();
				pa=null;			
			}
		} else if (sAgent.equals("Minimax")) {
			pa = new MinimaxAgent(sAgent, new ParMaxN(m_xab.maxnParams[n]), new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("Max-N")) {
			pa = new MaxNAgent(sAgent, new ParMaxN(m_xab.maxnParams[n]), new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("Expectimax-N")) {
			pa = new ExpectimaxNAgent(sAgent, new ParMaxN(m_xab.maxnParams[n]), new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("Random")) {
			pa = new RandomAgent(sAgent, new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("MCTS")) {
			pa = new MCTSAgentT(sAgent, null, new ParMCTS(m_xab.mctsParams[n]), new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("MCTS Expectimax")) {
			pa= new MCTSExpectimaxAgt(sAgent, new ParMCTSE(m_xab.mctsExpectimaxParams[n]), new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("Human")) {
			pa = new HumanPlayer(sAgent);
		} else if (sAgent.equals("MC")) {
			pa = new MCAgent(sAgent, new ParMC(m_xab.mcParams[n]), new ParOther(m_xab.oPar[n]));
		} else if (sAgent.equals("MC-N")) {
			pa = new MCAgentN(sAgent, new ParMC(m_xab.mcParams[n]), new ParOther(m_xab.oPar[n]));
		}
		return pa;
	}

	/**
	 * Fetch the {@link PlayAgent} vector from {@link Arena}. For agents which do 
	 * not need to be trained, construct a new one according to the selected choice
	 * and parameter settings. For agents which do need training, see, if 
	 * {@link #m_PlayAgents}[n] has already an agent of this type. 
	 * If so, return it, if not: 
	 * <ul>
	 * <li> if {@link #m_PlayAgents}[n]==null, construct a new agent and initialize 
	 *      it, but do not yet train it. 
	 * <li> else, throw a RuntimeException
	 * </ul>     
	 * @param m_xab where to read the settings from
	 * @return the vector m_PlayAgents of all agents in the arena
	 * @throws RuntimeException
	 */
	protected PlayAgent[] fetchAgents(XArenaButtons m_xab) 
			throws RuntimeException
	{
		if (m_PlayAgents==null) m_PlayAgents=new PlayAgent[numPlayers];
		PlayAgent pa=null;
		int maxGameNum=Integer.parseInt(m_xab.GameNumT.getText());
		for (int n=0; n<numPlayers; n++) {
			String sAgent = m_xab.getSelectedAgent(n);
			if (sAgent.equals("Minimax")) {
				pa= new MinimaxAgent(sAgent, new ParMaxN(m_xab.maxnParams[n]), new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("Max-N")) {
				pa= new MaxNAgent(sAgent, new ParMaxN(m_xab.maxnParams[n]), new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("Expectimax-N")) {
				pa = new ExpectimaxNAgent(sAgent, new ParMaxN(m_xab.maxnParams[n]), new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("Random")) {
				pa= new RandomAgent(sAgent, new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("MCTS")) {
				pa= new MCTSAgentT(sAgent,null,new ParMCTS(m_xab.mctsParams[n]), new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("MCTS Expectimax")) {
				pa= new MCTSExpectimaxAgt(sAgent, new ParMCTSE(m_xab.mctsExpectimaxParams[n]), new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("Human")) {
				pa= new HumanPlayer(sAgent);
			} else if (sAgent.equals("MC")) {
				pa= new MCAgent(sAgent, new ParMC(m_xab.mcParams[n]), new ParOther(m_xab.oPar[n]));
			} else if (sAgent.equals("MC-N")) {
				pa= new MCAgentN(sAgent, new ParMC(m_xab.mcParams[n]), new ParOther(m_xab.oPar[n]));
			}else { // all the trainable agents:
				if (m_PlayAgents[n]==null) {
					if (sAgent.equals("TDS")) {
						Feature feat = m_xab.m_game.makeFeatureClass(m_xab.tdPar[n].getFeatmode());
						pa = new TDAgent(sAgent, new ParTD(m_xab.tdPar[n]), new ParOther(m_xab.oPar[n]), feat, maxGameNum);
					} else if (sAgent.equals("TD-Ntuple-2")) {
						try {
							XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
							NTupleFactory ntupfac = new NTupleFactory(); 
							int[][] nTuples = ntupfac.makeNTupleSet(new ParNT(m_xab.ntPar[n]),xnf);
							pa = new TDNTuple2Agt(sAgent, new ParTD(m_xab.tdPar[n]), new ParNT(m_xab.ntPar[n]), 
									              new ParOther(m_xab.oPar[n]), nTuples, xnf, maxGameNum);
						} catch (Exception e) {
							MessageBox.show(m_xab, 
									e.getMessage(), 
									"Warning", JOptionPane.WARNING_MESSAGE);
							//e.printStackTrace();
							pa=null;			
						}
					} else if (sAgent.equals("TD-Ntuple-3")) {
						try {
							XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
							NTupleFactory ntupfac = new NTupleFactory(); 
							int[][] nTuples = ntupfac.makeNTupleSet(new ParNT(m_xab.ntPar[n]),xnf);
							pa = new TDNTuple3Agt(sAgent, new ParTD(m_xab.tdPar[n]), new ParNT(m_xab.ntPar[n]), 
									              new ParOther(m_xab.oPar[n]), nTuples, xnf, maxGameNum);
						} catch (Exception e) {
							MessageBox.show(m_xab, 
									e.getMessage(), 
									"Warning", JOptionPane.WARNING_MESSAGE);
							//e.printStackTrace();
							pa=null;			
						}
					} else if (sAgent.equals("Sarsa")) {
						try {
							XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
							NTupleFactory ntupfac = new NTupleFactory(); 
							int[][] nTuples = ntupfac.makeNTupleSet(new ParNT(m_xab.ntPar[n]),xnf);
							int numOutputs = m_xab.m_game.gb.getDefaultStartState().getAllAvailableActions().size();
							pa = new SarsaAgt(sAgent, new ParTD(m_xab.tdPar[n]), new ParNT(m_xab.ntPar[n]), 
											  new ParOther(m_xab.oPar[n]), nTuples, xnf, numOutputs, maxGameNum);
						} catch (Exception e) {
							MessageBox.show(m_xab, 
									e.getMessage(), 
									"Warning Sarsa", JOptionPane.WARNING_MESSAGE);
							//e.printStackTrace();
							pa=null;			
						}
					}					
				} else {
					PlayAgent inner_pa = m_PlayAgents[n];
					if (m_PlayAgents[n].getName()=="ExpectimaxWrapper") 
						inner_pa = ((ExpectimaxWrapper) inner_pa).getWrappedPlayAgent();
					if (!sAgent.equals(inner_pa.getName()))
						throw new RuntimeException("Current agent for player "+n+" is "+m_PlayAgents[n].getName()
								+" but selector for player "+n+" requires "+sAgent+".");
					pa = m_PlayAgents[n];		// take the n'th current agent, which 
												// is *assumed* to be trained (!)
				}
			} 
			if (pa==null) 
				throw new RuntimeException("Could not construct/fetch agent = "+sAgent);
			
			m_PlayAgents[n] = pa;
		} // for (n)
		return m_PlayAgents;
	}

	/**
	 * Given the selected agents in {@code paVector}, do nothing if their {@code nply==0}. 
	 * But if their {@code nply>0}, wrap them by an n-ply look-ahead tree search. 
	 * The tree is of type Max-N for deterministic games and of type
	 * Expectimax-N for nondeterministic games. No wrapping occurs for agent {@link HumanPlayer}.
	 * <p>
	 * Caution: Larger values for {@code nply}, e.g. greater 5, may lead to long execution times!
	 * 
	 * @param paVector	the (unwrapped) agents for each player 
	 * @param oPar		the vector of {@link OtherParams}, needed to access 
	 * 					{@code nply = oPar[n].getWrapperNPly()} for  each agent separately
	 * @param so		needed only to detect whether game is deterministic or not.
	 * @return a vector of agents ({@code paVector} itself if {@code nply==0}; wrapped agents 
	 * 					if {@code nply>0})
	 * 
	 * @see MaxNWrapper
	 * @see ExpectimaxWrapper
	 */
	protected PlayAgent[] wrapAgents(PlayAgent[] paVector, OtherParams[] oPar, StateObservation so) 
	{
		PlayAgent[] qaVector = new PlayAgent[numPlayers];
		for (int n=0; n<numPlayers; n++) {
			qaVector[n] = wrapAgent(n, paVector[n], oPar, so);
		} // for (n)
		return qaVector;
	}

	protected PlayAgent wrapAgent(int n, PlayAgent pa, OtherParams[] oPar, StateObservation so) 
	{
		PlayAgent qa;
		qa=pa;
		int nply = oPar[n].getWrapperNPly();
		if (nply>0 && !(pa instanceof HumanPlayer)) {
			if (so.isDeterministicGame()) {
				qa = new MaxNWrapper(pa,nply);
			} else {
				qa = new ExpectimaxWrapper(pa,nply);
			}
		}
		return qa;
	}

	protected PlayAgent[] wrapAgents(PlayAgent[] paVector, StateObservation so) 
	{
		PlayAgent[] qaVector = new PlayAgent[numPlayers];
		for (int n=0; n<numPlayers; n++) {
			PlayAgent pa = paVector[n];
			PlayAgent qa = pa;
			int nply = paVector[n].getParOther().getWrapperNPly();
			if (nply>0 && !(pa instanceof HumanPlayer)) {
				if (so.isDeterministicGame()) {
					qa = new MaxNWrapper(pa,nply);
				} else {
					qa = new ExpectimaxWrapper(pa,nply);
				}
			}
			qaVector[n] = qa;
		} // for (n)
		return qaVector;
	}


	/**
	 * Perform one training of a {@link PlayAgent} sAgent with maxGameNum episodes. 
	 * @param n			index of agent to train
	 * @param sAgent	a string containing the class name of the agent
	 * @param xab		used only for reading parameter values from members td_par, cma_par
	 * @param gb		the game board
	 * @return	the trained PlayAgent
	 * @throws IOException 
	 */
	public PlayAgent train(int n, String sAgent, XArenaButtons xab, GameBoard gb) throws IOException {
		int stopTest;			// 0: do not call Evaluator during training; 
								// >0: call Evaluator after every stopTest training games
		int stopEval;			// 0: do not stop on Evaluator; 
								// >0: stop, if Evaluator stays true for stopEval games
		int maxGameNum;			// maximum number of training games
		int numEval;			// evaluate the trained agent every numEval games
		boolean learnFromRM;	// if true, learn from random moves during training
		int gameNum=0;
		int verbose=2;

		maxGameNum = Integer.parseInt(xab.GameNumT.getText());
		numEval = xab.oPar[n].getNumEval();
		if (numEval==0) numEval=500; // just for safety, to avoid ArithmeticException in 'gameNum%numEval' below

		boolean doTrainStatistics=true;
		ArrayList tsList = new ArrayList<TStats>();
		ArrayList taggList = new ArrayList<TAggreg>();

		DecimalFormat frm = new DecimalFormat("#0.0000");
		PlayAgent pa = null;
		PlayAgent qa = null;

		try {
			pa = this.constructAgent(n,sAgent, xab);
			if (pa==null) throw new RuntimeException("Could not construct agent = " + sAgent);
			
		}  catch(RuntimeException e) {
			MessageBox.show(xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return pa;			
		} 
		
		String pa_string = pa.getClass().getName();
		if (!pa.isTrainable()) {
			System.out.println(pa_string + " is not trainable");
			return pa;
		}
			
		
		// initialization weight distribution plot:
		int plotWeightMode = xab.ntPar[n].getPlotWeightMethod();
		wChart.initializeChartPlot(xab,pa,plotWeightMode);
		
		System.out.println(pa.stringDescr());
		pa.setMaxGameNum(maxGameNum);
		pa.setNumEval(numEval);
		pa.setGameNum(0);
		System.out.println(pa.printTrainStatus());
		
		stopTest = xab.oPar[n].getStopTest();
		stopEval = xab.oPar[n].getStopEval();
		learnFromRM = xab.oPar[n].useLearnFromRM();
		int qem = xab.oPar[n].getQuickEvalMode();
        m_evaluatorQ = xab.m_game.makeEvaluator(pa,gb,stopEval,qem,1);
		int tem = xab.oPar[n].getTrainEvalMode();
		//
		// doTrainEvaluation flags whether Train Evaluator is executed:
		// Evaluator m_evaluatorT is only constructed and evaluated, if in tab 'Other pars' 
		// the choice box 'Train Eval Mode' is not -1 ("none").
		boolean doTrainEvaluation = (tem!=-1);
		if (doTrainEvaluation) {
	        m_evaluatorT = xab.m_game.makeEvaluator(pa,gb,stopEval,tem,1);
		}

		// initialization line chart plot:
		lChart.initializeChartPlot(xab,m_evaluatorQ,doTrainEvaluation);

		// Debug only: direct debug output to file debug.txt
		//TDNTupleAgt.pstream = System.out;
		//TDNTupleAgt.pstream = new PrintStream(new FileOutputStream("debug-TDNT.txt"));
		
		//{
		
		long startTime = System.currentTimeMillis();
		gb.initialize();
		while (pa.getGameNum()<pa.getMaxGameNum())
		{		
			StateObservation so = soSelectStartState(gb,xab.oPar[n].useChooseStart01(), pa); 

			// --- only debug TTT -----
//			so.advance(new ACTIONS(4));
//			so.advance(new ACTIONS(5));
//			so.advance(new ACTIONS(8));
//			so.advance(new ACTIONS(7));
			
			pa.trainAgent(so);
			
			if (doTrainStatistics) collectTrainStats(tsList,pa,so);
				
			gameNum = pa.getGameNum();
			if (gameNum%numEval==0 ) { //|| gameNum==1) {
				double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
				System.out.println(pa.printTrainStatus()+", "+elapsedTime+" sec");
				xab.GameNumT.setText(Integer.toString(gameNum ) );
				
				// construct 'qa' anew (possibly wrapped agent for eval)
				qa = wrapAgent(n, pa, xab.oPar, gb.getStateObs());

		        m_evaluatorQ.eval(qa);
				if (doTrainEvaluation)
					m_evaluatorT.eval(qa);

				// update line chart plot:
				lChart.updateChartPlot(gameNum, m_evaluatorQ, m_evaluatorT, doTrainEvaluation, false);

				// update weight / TC factor distribution plot:
				wChart.updateChartPlot(gameNum,pa,per);

				// enable premature exit if TRAIN button is pressed again:
				if (xab.m_game.taskState!=Arena.Task.TRAIN) {
					MessageBox.show(xab,
							"Training stopped prematurely",
							"Warning", JOptionPane.WARNING_MESSAGE);
					break; //out of while
				}

				startTime = System.currentTimeMillis();
			}
			
			if (stopTest>0 && (gameNum-1)%numEval==0 && stopEval>0) {
				// construct 'qa' anew (possibly wrapped agent for eval)
				qa = wrapAgent(n, pa, xab.oPar, gb.getStateObs());
		        
				if (doTrainEvaluation) {
					m_evaluatorT.eval(qa);
					m_evaluatorT.goalReached(gameNum);
				}
				
				m_evaluatorQ.eval(qa); 
				if(m_evaluatorQ.goalReached(gameNum)) break;  // out of while
				
			}
		} // while
		
		// Debug only
		//TDNTupleAgt.pstream.close();
			
		//} // if(sAgent)..else
		
		if (doTrainStatistics) {
			taggList = aggregateTrainStats(tsList);
			System.out.println("--- Train Statistics ---");
			TStats.printTAggregList(taggList);
		}
		
		//xab.GameNumT.setText(Integer.toString(maxGameNum) );		// restore initial value (maxGameNum)
														// not sensible in case of premature stop;
														// and in case of normal end, it will be maxGameNum anyhow

		//samine
		int test=2000;
		if (gameNum%test!=0) 
			System.out.println(pa.printTrainStatus());

//-- only debug
//		m_evaluator2.eval(); 
//        Evaluator2 m_evaluator2New = new Evaluator2(pa,0,2);
//        m_evaluator2New.eval();
		if (stopTest>0 && stopEval>0) {
			System.out.println(m_evaluatorQ.getGoalMsg(gameNum));
			if (doTrainEvaluation) System.out.println(m_evaluatorT.getGoalMsg(gameNum));
		}
		
		System.out.println("final "+m_evaluatorQ.getMsg());
		if (doTrainEvaluation && m_evaluatorT.getMsg()!=null) System.out.println("final "+m_evaluatorT.getMsg());
								// getMsg() might be null if evaluator mode = -1 (no evaluation)

		return pa;
	}
	
	private StateObservation soSelectStartState(GameBoard gb, boolean chooseStart01, PlayAgent pa) {
		StateObservation so; 
		if (chooseStart01) {
			so = gb.chooseStartState(pa);
		} else {
			so = gb.getDefaultStartState();  
		}					
		return so;
	}
	
	private void collectTrainStats(ArrayList<TStats> tsList, PlayAgent pa, StateObservation so) {
		int n=pa.getGameNum();
		int p=so.getMinEpisodeLength();
		int moveNum=pa.getMoveCounter();
		int epiLength = pa.getParOther().getEpisodeLength();
        TStats tstats = new TStats(n,p,moveNum,epiLength);
        tsList.add(tstats);
	}
	
	private ArrayList<TAggreg> aggregateTrainStats(ArrayList<TStats> tsList) {
		ArrayList taggList = new ArrayList<TAggreg>();
		TStats tstats;
		TAggreg tagg;
		HashSet pvalues = new HashSet();
		Iterator it = tsList.iterator();
	    while (it.hasNext()) {
		    tstats = (TStats)it.next();
	    	pvalues.add(tstats.p);
	    }
		Iterator itp = pvalues.iterator();
	    while (itp.hasNext()) {
	    	int p=(int)itp.next();
 			tagg = new TAggreg(tsList,p);
 			taggList.add(tagg);
	    }
		return taggList;
	}

	/**
	 * Perform trainNum cycles of training and evaluation for PlayAgent, each 
	 * training with maxGameNum games. Record results in {@code multiTrain.csv}, see below.
	 * 
	 * @param n			index of agent to train (the current GUI will call multiTrain 
	 * 					always with n=0 
	 * @param sAgent	a string containing the class name of the agent
	 * @param xab		used only for reading parameter values from members td_par, cma_par
	 * @throws IOException if something goes wrong with {@code multiTrain.csv}, see below
	 * <p>
	 * Side effect: writes results of multi-training to <b>{@code agents/<gameDir>/csv/multiTrain.csv}</b>.
	 * This file has the columns: <br>
	 * {@code run, gameNum, evalQ, evalT, evalM, actionNum, trnMoves}. <br>
	 * The contents may be visualized with one of the R-scripts in {@code resources\R_plotTools}.
	 */
	public PlayAgent multiTrain(int n, String sAgent, XArenaButtons xab, GameBoard gb) throws IOException {
		DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
		DecimalFormat frm = new DecimalFormat("#0.000");
		DecimalFormat frm2 = new DecimalFormat("+0.00;-0.00");
		DecimalFormat frm1 = new DecimalFormat("#0.00");
		int verbose=1;
		int stopEval = 0;

		int trainNum=Integer.valueOf(xab.TrainNumT.getText()).intValue();
		int maxGameNum=Integer.parseInt(xab.GameNumT.getText());
		boolean learnFromRM = xab.oPar[n].useLearnFromRM();
		PlayAgent pa = null, qa= null;
		
		boolean doTrainEvaluation=false;
		boolean doMultiEvaluation=false;

		System.out.println("*** Starting multiTrain with trainNum = "+trainNum+" ***");

		Measure oQ = new Measure();			// quick eval measure
		Measure oT = new Measure();			// train eval measure
//		Measure oM = new Measure();			// multiTrain eval measure
		MTrain mTrain;
		double evalQ=0.0, evalT=0.0, evalM=0.0;
		ArrayList<MTrain> mtList = new ArrayList<MTrain>();
		int maxGameNumV=10000;
		
		for (int i=0; i<trainNum; i++) {
			xab.TrainNumT.setText(Integer.toString(i+1)+"/"+Integer.toString(trainNum) );

			try {
				pa = constructAgent(n,sAgent, xab);
				if (pa==null) throw new RuntimeException("Could not construct AgentX = " + sAgent);				
			}  catch(RuntimeException e) 
			{
				MessageBox.show(xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				return pa;			
			} 


			int qem = xab.oPar[n].getQuickEvalMode();
	        m_evaluatorQ = xab.m_game.makeEvaluator(pa,gb,stopEval,qem,1);
			int tem = xab.oPar[n].getTrainEvalMode();
			//
			// doTrainEvaluation flags whether Train Evaluator is executed:
			// Evaluator m_evaluatorT is only constructed and evaluated, if the choice
			// boxes 'Quick Eval Mode' and 'Train Eval Mode' in tab 'Other pars' have
			// different values. 
			doTrainEvaluation = (tem!=qem);
			if (doTrainEvaluation)
		        m_evaluatorT = xab.m_game.makeEvaluator(pa,gb,stopEval,tem,1);
			
//			int mem = m_evaluatorQ.getMultiTrainEvalMode();
//			//
//			// doMultiEvaluation flags whether Multi Train Evaluator is executed:
//			// Evaluator m_evaluatorM is only constructed and evaluated, if the value of
//			// getMultiTrainEvalMode() differs from the values in choice boxes
//			// 'Quick Eval Mode' and 'Train Eval Mode' in tab 'Other pars' . 
//			doMultiEvaluation = ((mem!=qem) && (mem!=tem));
//			if (doMultiEvaluation)
//		        m_evaluatorM = xab.m_game.makeEvaluator(pa,gb,stopEval,mem,1);

			if (i==0) {
				String pa_string = pa.getClass().getName();
				System.out.println(pa.stringDescr());
			}
			pa.setMaxGameNum(maxGameNum);
			pa.setGameNum(0);
			int player; 
			int numEval = xab.oPar[n].getNumEval();
			int gameNum;
			long actionNum, trnMoveNum;
			PlayAgent[] paVector;
			
			{
				long startTime = System.currentTimeMillis();
				gb.initialize();
				while (pa.getGameNum()<pa.getMaxGameNum())
				{		
					StateObservation so = soSelectStartState(gb,xab.oPar[n].useChooseStart01(), pa); 

					pa.trainAgent(so);
					
					gameNum = pa.getGameNum();
					if (gameNum%numEval==0 ) { //|| gameNum==1) {
						double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
						System.out.println(pa.printTrainStatus()+", "+elapsedTime+" sec");
						xab.GameNumT.setText(Integer.toString(gameNum ) );
						
						// construct 'qa' anew (possibly wrapped agent for eval)
						qa = wrapAgent(0, pa, xab.oPar, gb.getStateObs());
				        
						m_evaluatorQ.eval(qa);
						evalQ = m_evaluatorQ.getLastResult();
						if (doTrainEvaluation) {
							m_evaluatorT.eval(qa);
							evalT = m_evaluatorT.getLastResult();
						}
//						if (doMultiEvaluation) {
//							m_evaluatorM.eval(qa);
//							evalM = m_evaluatorM.getLastResult();
//						}
						
                        // gather information for later printout to agents/gameName/csv/multiTrain.csv.
						actionNum = pa.getNumLrnActions();	
						trnMoveNum = pa.getNumTrnMoves();
						mTrain = new MTrain(i,gameNum,evalQ,evalT,/*evalM,*/
											actionNum,trnMoveNum);
						mtList.add(mTrain);

						// enable premature exit if MULTITRAIN button is pressed again:
						if (xab.m_game.taskState!=Arena.Task.MULTTRN) {
							MessageBox.show(xab,
									"MultiTraining stopped prematurely",
									"Warning", JOptionPane.WARNING_MESSAGE);
							break; //out of while
						}

						startTime = System.currentTimeMillis();
					}
				}
				
			} // if(sAgent)..else
			
			// construct 'qa' anew (possibly wrapped agent for eval)
			qa = wrapAgent(0, pa, xab.oPar, gb.getStateObs());

	        // evaluate again at the end of a training run:
			m_evaluatorQ.eval(qa);
			oQ.add(m_evaluatorQ.getLastResult());
			if (doTrainEvaluation) {
				m_evaluatorT.eval(qa);
				oT.add(m_evaluatorT.getLastResult());								
			}
//			if (doMultiEvaluation) {
//				m_evaluatorM.eval(qa);
//				oM.add(m_evaluatorM.getLastResult());
//			}
			
			if (xab.m_game.taskState!=Arena.Task.MULTTRN) {
				break; //out of for
			}
		} // for (i)
		System.out.println("Avg. "+ m_evaluatorQ.getPrintString()+frm3.format(oQ.getMean()) + " +- " + frm.format(oQ.getStd()));
		if (doTrainEvaluation && m_evaluatorT.getPrintString()!=null) 
								 // getPrintString() may be null, if evalMode=-1
		{
		  System.out.println("Avg. "+ m_evaluatorT.getPrintString()+frm3.format(oT.getMean()) + " +- " + frm.format(oT.getStd()));
		}
//		if (doMultiEvaluation)
//		  System.out.println("Avg. "+ m_evaluatorM.getPrintString()+frm3.format(oM.getMean()) + " +- " + frm.format(oM.getStd()));
		this.lastMsg = (m_evaluatorQ.getPrintString() + frm2.format(oQ.getMean()) + " +- " + frm1.format(oQ.getStd()) + "");
		
		MTrain.printMultiTrainList(mtList, pa, m_Arena);
		
		xab.TrainNumT.setText(Integer.toString(trainNum) );
		return pa;
		
	} // multiTrain

	/**
	 * Test player pa by playing competeNum games against opponent, both as X and as O.
	 * Start each game with an empty board.
	 * @param pa		a trained agent
	 * @param opponent	a trained agent
	 * @param competeNum
	 * @param verbose TODO
	 * @param gb		needed to get a default start state
	 * @return the fitness of pa, which is +1 if pa always wins, 0 if always tie or if #win=#loose
	 *         and -1 if pa always looses.  
	 * 
	 * @see XArenaButtons
	 */
	public static double competeBoth(PlayAgent pa, PlayAgent opponent, StateObservation startSO,
									 int competeNum, int verbose, GameBoard gb) {
		double[] res;
		double resX, resO;

		// now passed as parameter
		//StateObservation startSO = gb.getDefaultStartState();  // empty board

		res = XArenaFuncs.compete(pa, opponent, startSO, competeNum, verbose);
		resX  = res[0] - res[2];		// X-win minus O-win percentage, \in [-1,1]
										// resp. \in [-1,0], if opponent never looses.
										// +1 is best for pa, -1 worst for pa.
		res = XArenaFuncs.compete(opponent, pa, startSO, competeNum, verbose);
		resO  = res[2] - res[0];		// O-win minus X-win percentage, \in [-1,1]
										// resp. \in [-1,0], if opponent never looses.
										// +1 is best for pa, -1 worst for pa.
		return (resX+resO)/2.0;
	}
	
	/**
	 * Perform a competition paX vs. paO consisting of competeNum games, starting from StateObservation startSO.
	 * @param paX	PlayAgent,	a trained agent
	 * @param paO	PlayAgent,	a trained agent
	 * @param startSO	the start board position for the game
	 * @param competeNum		the number of games to play
	 * @param verbose			0: silent, 1,2: more print-out
	 * @return		double[3], the percentage of games with X-win, tie, O-win
	 */
	public static double[] compete(PlayAgent paX, PlayAgent paO, StateObservation startSO, int competeNum, int verbose) {
		double[] winrate = new double[3];
		int xwinCount=0, owinCount=0, tieCount=0;
		DecimalFormat frm = new DecimalFormat("#0.000");
		boolean nextMoveSilent = (verbose<2 ? true : false);
		StateObservation so;
		Types.ACTIONS actBest;
		String[] playersWithFeatures = {"TicTacToe.ValItPlayer","controllers.TD.TDAgent","TicTacToe.CMAPlayer"}; 
		
		String paX_string = paX.stringDescr();
		String paO_string = paO.stringDescr();
		if (verbose>0) System.out.println("Competition: "+competeNum+" games "+paX_string+" vs "+paO_string);
		for (int k=0; k<competeNum; k++) {
			int Player=Types.PLAYER_PM[startSO.getPlayer()];			
			so = startSO.copy();

			while(true)
			{

				if(Player==1){		// make a X-move
					int n=so.getNumAvailableActions();
					actBest = paX.getNextAction2(so, false, nextMoveSilent);
					so.advance(actBest);
					Player=-1;
				}
				else				// i.e. O-Move
				{
					int n=so.getNumAvailableActions();
					actBest = paO.getNextAction2(so, false, nextMoveSilent);
					so.advance(actBest);
					Player=+1;
				}
				if (so.isGameOver()) {
					int res = so.getGameWinner().toInt();
					//  res is +1/0/-1  for X/tie/O win
					int player = Types.PLAYER_PM[so.getPlayer()];
					switch (res*player) {
						case -1:
							if (verbose>0) System.out.println(k+": O wins");
							owinCount++;
							break;
						case 0:
							if (verbose>0) System.out.println(k+": Tie");
							tieCount++;
							break;
						case +1:
							if (verbose>0) System.out.println(k+": X wins");
							xwinCount++;
							break;
					}

					break; // out of while

				} // if (so.isGameOver())
			}	// while(true)

		} // for (k)
		winrate[0] = (double)xwinCount/competeNum;
		winrate[1] = (double)tieCount/competeNum;
		winrate[2] = (double)owinCount/competeNum;

		if (verbose>0) {
			System.out.print("win rates: ");
			for (int i=0; i<3; i++) System.out.print(frm.format(winrate[i])+"  ");
			System.out.println(" (X/Tie/O)");
		}

		return winrate;
	} // compete

	/**
	 * This is an adapted version of {@link XArenaFuncs#compete(PlayAgent, PlayAgent, StateObservation, int, int)}. Tournament parameters were added.
	 * @param paX PlayAgent, a trained agent
	 * @param paO PlayAgent, a trained agent
	 * @param startSO    the start board position for the game
	 * @param competeNum the number of games to play (always 1)
	 * @param nextTimes timestorage to save measurements
	 * @param rndmStartMoves count of random startmoves at beginning
	 * @return code with information wh won
	 */
	public static double[] competeTS(PlayAgent paX, PlayAgent paO, StateObservation startSO, int competeNum, TSTimeStorage[] nextTimes, int rndmStartMoves) {
		double[] winrate = new double[3];
		int xwinCount=0, owinCount=0, tieCount=0;
		DecimalFormat frm = new DecimalFormat("#0.000");
		boolean nextMoveSilent = true;
		StateObservation so;
		Types.ACTIONS actBest;

		String paX_string = paX.stringDescr();
		String paO_string = paO.stringDescr();
		System.out.println("Competition: "+competeNum+" games "+paX_string+" vs "+paO_string+" with "+rndmStartMoves+" random startmoves");
		/*
		if (rndmStartMoves>0) {
			RandomAgent raX = new RandomAgent("Random Agent X");
			//RandomAgent raO = new RandomAgent("Random Agent O");
			for (int n = 0; n < rndmStartMoves; n++) {
				startSO.advance(raX.getNextAction2(startSO, false, true));
				//startSO.advance(raO.getNextAction2(startSO, false, true));
			}
			System.out.println("RandomStartState: "+startSO);
		}
		*/
		String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH.mm.ss"));
		System.out.println("Episode Start @ "+currDateTime);
		System.out.println("(Random)StartState: "+startSO);

		for (int k=0; k<competeNum; k++) { // ist im TS immer 1
			int Player = Types.PLAYER_PM[startSO.getPlayer()];
			so = startSO.copy();

			//ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			//System.out.println("threadMXBean.isCurrentThreadCpuTimeSupported() "+threadMXBean.isCurrentThreadCpuTimeSupported());

			while(true) {
				System.out.print("#"); // simple progressbar so user knows something is happening
				if(Player==1){		// make a X-move
					int n = so.getNumAvailableActions();

					//long startT = System.currentTimeMillis();
					//StopWatch stopwatch = new StopWatch(); // teil des apache common lang3 pakets - verwendet intern aber auch nur System.nanoTime()
					//stopwatch.start();
					long startTNano = System.nanoTime();
					//long startTNanoThread = threadMXBean.getCurrentThreadCpuTime();
					//long startTNanoInstant = Instant.now().getNano();
					//long startTNanoInstant = Instant.now().toEpochMilli();

					actBest = paX.getNextAction2(so, false, nextMoveSilent); // agent moves!
					//long endT = System.currentTimeMillis();
					//stopwatch.stop();
					long endTNano = System.nanoTime();
					//long endTNanoThread = threadMXBean.getCurrentThreadCpuTime();
					//long endTNanoInstant = Instant.now().getNano(); // ca selbe ergebnis wie System.nanoTime
					//long endTNanoInstant = Instant.now().toEpochMilli(); // bei zeiten <1ms eigentlich immer 0
					// Debug Printlines
					//System.out.println("paX.getNextAction2(so, false, true); processTime: "+(endT-startT)+"ms");
					//System.out.println("paX.getNextAction2(so, false, true); processTime: "+(endTNano-startTNano)+"ns | "+(endTNano-startTNano)/(1*Math.pow(10,6))+"ms (aus ns)");
					nextTimes[0].addNewTimeNS(endTNano-startTNano);
					//System.out.println("Time nanoTime:::: "+ (endTNano-startTNano)+"ns = "+ timeDiffNStoMS(startTNano,endTNano)+"ms");
					//System.out.println("Time ThreadNano:: "+ (endTNanoThread-startTNanoThread)+"ns = "+ timeDiffNStoMS(startTNanoThread,endTNanoThread)+"ms");
					//System.out.println("Time InstantNano: "+ (endTNanoInstant-startTNanoInstant)+"ns = "+ timeDiffNStoMS(startTNanoInstant,endTNanoInstant)+"ms");
					//System.out.println("Time StWatchNano: "+ (stopwatch.getNanoTime())+"ns = "+ timeNStoMS(stopwatch.getNanoTime())+"ms");
					//System.out.println("-----------------");

					so.advance(actBest);
					Player = -1;
				}
				else				// i.e. O-Move
				{
					int n = so.getNumAvailableActions();

					//long startT = System.currentTimeMillis();
					long startTNano = System.nanoTime();
					actBest = paO.getNextAction2(so, false, nextMoveSilent); // agent moves!
					//long endT = System.currentTimeMillis();
					long endTNano = System.nanoTime();
					// Debug Printlines
					//System.out.println("paO.getNextAction2(so, false, true); processTime: "+(endT-startT)+"ms");
					//System.out.println("paO.getNextAction2(so, false, true); processTime: "+(endTNano-startTNano)+"ns | "+(endTNano-startTNano)/(1*Math.pow(10,6))+"ms (aus ns)");
					nextTimes[1].addNewTimeNS(endTNano-startTNano);

					so.advance(actBest);
					Player = +1;
				}
				if (so.isGameOver()) {
					System.out.println(); // make new line to not put text at end of ##### progressbar (1st line of while)
					int res = so.getGameWinner().toInt();
					//  res is +1/0/-1  for X/tie/O win
					int player = Types.PLAYER_PM[so.getPlayer()];
					switch (res*player) {
					case -1:
						System.out.println(k+": O wins");
						owinCount++;
						break;
					case 0:
						System.out.println(k+": Tie");
						tieCount++;
						break;
					case +1:
						System.out.println(k+": X wins");
						xwinCount++;
						break;
					}

					break; // out of while

				} // if (so.isGameOver())
			}	// while(true)

		} // for (k)
		winrate[0] = (double)xwinCount/competeNum;
		winrate[1] = (double)tieCount/competeNum;
		winrate[2] = (double)owinCount/competeNum;

		System.out.print("win rates: ");
		for (int i=0; i<3; i++)
			System.out.print(frm.format(winrate[i])+"  ");
		System.out.println(" (X/Tie/O)");

		return winrate;
	} // competeTS

	private static double timeDiffNStoMS(long startT, long endT) {
		double s = startT;
		double e = endT;
		double r = e - s;
		return r/1000000;
	}
	private static double timeNStoMS(long startT) {
		double r = startT;
		return r/1000000;
	}
	
	/**
	 * Does the main work for menu items 'Single Compete', 'Swap Compete' and 'Compete Both'.
	 * These items set enum {@link Arena#taskState} to either COMPETE or SWAPCMP or BOTHCMP.
	 * Then the appropriate cases of {@code switch} in Arena.run() will call competeBase. 
	 * 'Compete' performs competeNum competitions AgentX as X vs. AgentO as O. 
	 * 'Swap Compete' performs competeNum competitions AgentX as O vs. AgentO as X. 
	 * 'Compete Both' combines 'Compete' and 'Swap Compete'.
	 * The agents AgentX and AgentO are fetched from {@code xab} and are assumed to be
	 * trained (!). The parameters for X and O are fetched from the param tabs.
	 *  
	 * @param swap {@code false} for 'Compete' and {@code true} for 'Swap Compete'
	 * @param both {@code true} for 'Compete Both' ({@code swap} is then irrelevant)
	 * @param xab	used only for reading parameter values from GUI members
	 * @param gb	needed for {@code competeBoth}
	 * @return the fitness of AgentX, which is +1 if AgentX always wins, 0 if always tie
	 *         or if #win=#loose and, -1 if AgentX always looses.
	 */
	protected double competeBase(boolean swap, boolean both, XArenaButtons xab, GameBoard gb) {
		int competeNum = xab.winCompOptions.getNumGames();
		int numPlayers = gb.getStateObs().getNumPlayers();
		if (numPlayers!=2) {
			MessageBox.show(xab, 
					"Single/Swap Compete only available for 2-player games!", 
					"Error", JOptionPane.ERROR_MESSAGE);	
			return 0.0;
		}

		try {
			String AgentX = xab.getSelectedAgent(0);
			String AgentO = xab.getSelectedAgent(1);
			if (AgentX.equals("Human") | AgentO.equals("Human")) {
				MessageBox.show(xab, "No compete for agent Human", "Error", JOptionPane.ERROR_MESSAGE);
				return 0.0;
			} else {
				StateObservation startSO = gb.getDefaultStartState();  // empty board

				PlayAgent[] paVector = fetchAgents(xab);

				AgentBase.validTrainedAgents(paVector,numPlayers); // may throw RuntimeException
				
				PlayAgent[] qaVector = wrapAgents(paVector,xab.oPar,startSO);

				int verbose=1;

				if (both) {
					return competeBoth(qaVector[0],qaVector[1],startSO,competeNum,verbose,gb);
				} else {
					double[] res;
					if (swap) {
						res = compete(qaVector[1],qaVector[0],startSO,competeNum,verbose);
						System.out.println(Arrays.toString(res));
						return res[2] - res[0];
					} else {
						res = compete(qaVector[0],qaVector[1],startSO,competeNum,verbose);
						System.out.println(Arrays.toString(res));
						return res[0] - res[2];
					}
				}
			}
					
		} catch(RuntimeException ex) {
			MessageBox.show(xab, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return 0;
		}
	} // competeBase

	public double singleCompete(XArenaButtons xab, GameBoard gb) {
		return this.competeBase(false, false, xab, gb);
	}

	public double swapCompete(XArenaButtons xab, GameBoard gb) {
		return this.competeBase(true, false, xab, gb);
	}

	public double bothCompete(XArenaButtons xab, GameBoard gb) {
		return this.competeBase(false, true, xab, gb);
	}

	/**
	 * This is an adapted version of {@link XArenaFuncs#competeBoth(PlayAgent, PlayAgent, StateObservation, int, int, GameBoard)}. The tournament parameters were added.
	 * @param gb gameboard to play on
	 * @param xab GUI params for standard agents
	 * @param dataTS helper class containing data and settings for the next game
	 * @return info who wins or error code
	 */
	protected int singleCompeteBaseTS(GameBoard gb, XArenaButtons xab, TSGameDataTransfer dataTS) { // return who wins (agent1, tie, agent2) [0;2]
		// protected void competeBase(boolean swap, XArenaButtons xab, GameBoard gb)
		int competeNum = 1;//xab.winCompOptions.getNumGames(); | falls wert != 1 dann competeTS() anpassen!
		int numPlayers = gb.getStateObs().getNumPlayers();
		double[] c = {}; // winrate how often = [0]:agentX wins [1]: ties [2]: agentO wins

		try {
			String AgentX = dataTS.nextTeam[0].getAgentType();
			String AgentO = dataTS.nextTeam[1].getAgentType();
			if (AgentX.equals("Human") | AgentO.equals("Human")) {
				//MessageBox.show(xab, "No compete for agent Human", "Error", JOptionPane.ERROR_MESSAGE);
				System.out.println(TAG+"ERROR :: No compete for agent Human, select different agent");
			} else {
				StateObservation startSO = gb.getDefaultStartState();  // empty board

				// manipulation of selected standard agent in XrenaButtons!
				xab.enableTournamentRemoteData(dataTS.nextTeam);

				// prepare agents
				PlayAgent[] paVector;
				PlayAgent[] qaVector;
				/*
				if (dataTS.nextTeam[0].isHddAgent() && dataTS.nextTeam[1].isHddAgent()) {
					paVector = new PlayAgent[2];
					paVector[0] = dataTS.nextTeam[0].getPlayAgent();
					paVector[1] = dataTS.nextTeam[1].getPlayAgent();
					AgentBase.validTrainedAgents(paVector,numPlayers); // may throw RuntimeException
//					OtherParams[] hddPar = new OtherParams[2];
//					hddPar[0] = new OtherParams();
//					hddPar[0].setWrapperNPly(paVector[0].getParOther().getWrapperNPly());
//					hddPar[1] = new OtherParams();
//					hddPar[1].setWrapperNPly(paVector[1].getParOther().getWrapperNPly());
//					qaVector = wrapAgents(paVector,hddPar,startSO);
					qaVector = wrapAgents(paVector,startSO);
				} else {
					if (dataTS.nextTeam[0].isHddAgent() || dataTS.nextTeam[1].isHddAgent()) {
						System.out.println(TAG+"ERROR :: dont mix standard and hdd agents!");
						return 44;
					}
					paVector = fetchAgents(xab);
					AgentBase.validTrainedAgents(paVector,numPlayers); // may throw RuntimeException
					qaVector = wrapAgents(paVector,xab.oPar,startSO);
				}
				*/
				if (dataTS.nextTeam[0].isHddAgent() && dataTS.nextTeam[1].isHddAgent()) {
					paVector = new PlayAgent[2];
					paVector[0] = dataTS.nextTeam[0].getPlayAgent();
					paVector[1] = dataTS.nextTeam[1].getPlayAgent();
					AgentBase.validTrainedAgents(paVector,numPlayers); // may throw RuntimeException
//					OtherParams[] hddPar = new OtherParams[2];
//					hddPar[0] = new OtherParams();
//					hddPar[0].setWrapperNPly(paVector[0].getParOther().getWrapperNPly());
//					hddPar[1] = new OtherParams();
//					hddPar[1].setWrapperNPly(paVector[1].getParOther().getWrapperNPly());
//					qaVector = wrapAgents(paVector,hddPar,startSO);
					qaVector = wrapAgents(paVector,startSO);
				} else {
					paVector = fetchAgents(xab);
					if (dataTS.nextTeam[0].isHddAgent()) {
						paVector[0] = dataTS.nextTeam[0].getPlayAgent();
					}
					if (dataTS.nextTeam[1].isHddAgent()) {
						paVector[1] = dataTS.nextTeam[1].getPlayAgent();
					}
					AgentBase.validTrainedAgents(paVector, numPlayers); // may throw RuntimeException
					qaVector = wrapAgents(paVector, xab.oPar, startSO);
				}

				//c = competeTS(qaVector[0], qaVector[1], startSO, competeNum, dataTS.nextTimes, dataTS.rndmStartMoves);
				c = competeTS(qaVector[0], qaVector[1], dataTS.startSO, competeNum, dataTS.nextTimes, dataTS.rndmStartMoves);
				//System.out.println(Arrays.toString(c));

				xab.disableTournamentRemoteData();
			}

		} catch(RuntimeException ex) {
			MessageBox.show(xab, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(TAG+"ERROR :: RuntimeException :: "+ex.getMessage());
			return 43;
		}

		if (c[0]==1.0)
			return 0;
		if (c[1]==1.0)
			return 1;
		if (c[2]==1.0)
			return 2;
		return 42;
	}

	/**
	 * Perform many (competitionNum) competitions between agents of type AgentX and agents 
	 * of type AgentO. The agents (if trainable) are trained anew before each competition. 
	 * @param silent
	 * @param xab		used only for reading parameter values from GUI members 
	 * @return			double[3], the percentage of games with X-win, tie, O-win 
	 * 					(averaged over all competitions) 
	 * @throws IOException 
	 */
	public double[] multiCompete(boolean silent, XArenaButtons xab, GameBoard gb) 
			throws IOException {
		DecimalFormat frm = new DecimalFormat("#0.000");
		int verbose=1;
		int stopEval = 0;
		double[] winrate = new double[3];
		
		int numPlayers = gb.getStateObs().getNumPlayers();
		if (numPlayers!=2) {
			MessageBox.show(xab, 
					"Multi-Competition only available for 2-player games!", 
					"Error", JOptionPane.ERROR_MESSAGE);	
			return winrate;
		}
		
		try {
		// take settings from GUI xab
		String AgentX = xab.getSelectedAgent(0);  // enthalten AgentNamen als String (tools.Types.GUI_AGENT_LIST)
		String AgentO = xab.getSelectedAgent(1);
		int competeNum=xab.winCompOptions.getNumGames();
		int competitionNum=xab.winCompOptions.getNumCompetitions();
		int maxGameNum = Integer.parseInt(xab.GameNumT.getText());
		Evaluator m_evaluatorX=null;
		Evaluator m_evaluatorO=null;
		
		double optimCountX=0.0,optimCountO=0.0;
		double[][] winrateC = new double[competitionNum][3];
		double[][] evalC = new double[competitionNum][2];
		PlayAgent paX=null, paO=null, qa=null;
		if (verbose>0) System.out.println("Multi-Competition: "+competitionNum+" competitions with "
										 +competeNum+" games each, "+AgentX+" vs "+AgentO);

		if (AgentX.equals("Human") | AgentO.equals("Human")) {
			MessageBox.show(xab, 
					"No multiCompete for agent Human", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return winrate;
		} 
		for (int c=0; c<competitionNum; c++) { // durchlauf der einzelnen competitions
			int player;

			// beide agenten werden fuer die games jeder competition neu initialisiert
			try {
				paX = this.constructAgent(0,AgentX, xab);
				if (paX==null) throw new RuntimeException("Could not construct AgentX = " + AgentX);
			}  catch(RuntimeException e) 
			{
				MessageBox.show(xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				return winrate;			
			} 
			paX.setMaxGameNum(maxGameNum);
			paX.setGameNum(0);
			
			try {
				paO = this.constructAgent(1,AgentO, xab);
				if (paO==null) throw new RuntimeException("Could not construct AgentO = " + AgentO);
			}  catch(RuntimeException e) 
			{
				MessageBox.show(xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				return winrate;			
			} 
			paO.setMaxGameNum(maxGameNum);
			paO.setGameNum(0);
			
			// take the evaluator mode qem from the choice box 'Quick Eval Mode'
			// in tab 'Other pars':
			int qem = xab.oPar[0].getQuickEvalMode();
			m_evaluatorX = xab.m_game.makeEvaluator(paX,gb,stopEval,qem,1);
			
			if (paX.getAgentState()!=AgentState.TRAINED) {
				while (paX.getGameNum()<paX.getMaxGameNum())
				{							
					StateObservation so = soSelectStartState(gb,xab.oPar[0].useChooseStart01(), paX); 

					paX.trainAgent(so);
				}
				paX.setAgentState(AgentState.TRAINED);
			} 

			// construct 'qa' anew (possibly wrapped agent for eval)
			qa = wrapAgent(0, paX, xab.oPar, gb.getStateObs());
			m_evaluatorX.eval(qa);
			evalC[c][0] = m_evaluatorX.getLastResult();
			optimCountX += evalC[c][0];
			
			qem = xab.oPar[1].getQuickEvalMode();
			m_evaluatorO = xab.m_game.makeEvaluator(paO,gb,stopEval,qem,1);
			
			if (paO.getAgentState()!=AgentState.TRAINED) {
				while (paO.getGameNum()<paO.getMaxGameNum())
				{							
					StateObservation so = soSelectStartState(gb,xab.oPar[1].useChooseStart01(), paO); 

					paO.trainAgent(so);
				}
				paO.setAgentState(AgentState.TRAINED);				
			} 

			// construct 'qa' anew (possibly wrapped agent for eval)
			qa = wrapAgent(1, paO, xab.oPar, gb.getStateObs());
			m_evaluatorO.eval(qa);
			evalC[c][1] = m_evaluatorO.getLastResult();
			optimCountO += evalC[c][1];

			StateObservation startSO = gb.getDefaultStartState();  // empty board
			
			winrateC[c] = compete(paX,paO,startSO,competeNum,0);
			
			for (int i=0; i<3; i++) winrate[i] += winrateC[c][i];				
			if (!silent) {
				System.out.print(c + ": ");
				for (int i=0; i<3; i++) System.out.print(" "+frm.format(winrateC[c][i]));
				System.out.println();
			}
		} // for (c)
		
		for (int i=0; i<3; i++) winrate[i] = winrate[i]/competitionNum;
		if (!silent) {
			System.out.println("*** Competition results: ***");
			System.out.println("Agent X: Avg. "+m_evaluatorX.getPrintString()+": "+frm.format((double)optimCountX/competitionNum));
			System.out.println("Agent O: Avg. "+m_evaluatorO.getPrintString()+": "+frm.format((double)optimCountO/competitionNum));
			//--- this is done below ---
			//System.out.print("Avg. win rate: ");
			//for (int i=0; i<3; i++) System.out.print(" "+frm.format(winrate[i]));
			//System.out.println(" (X/Tie/O)");
		}

		//
		// write multiCompete statistics to "Arena.comp.csv"
		//
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+xab.m_game.getGameName()+"/";
		String filename = "Arena.comp.csv";
		tools.Utils.checkAndCreateFolder(strDir);
		try {
			PrintWriter f; 
			f = new PrintWriter(new BufferedWriter(new FileWriter(strDir+filename)));
			
			// TODO: needs to be generalized to other agents but TDAgent: 
			for (int i=0; i<2; i++) {
				double alpha = xab.tdPar[i].getAlpha();
				double lambda = xab.tdPar[i].getLambda();
				f.println("alpha["+i+"]=" + alpha + ";  lambda["+i+"]=" + lambda + "; trained agents=" + competitionNum 
						  + ",  maxGameNum=" +maxGameNum);
			}
			f.print(AgentX);
			if (paX instanceof TDAgent) 
				f.print("("+((TDAgent)paX).getFeatmode()+")");
			f.print(" vs. ");  
			f.print(AgentO);
			if (paO instanceof TDAgent) 
				f.print("("+((TDAgent)paO).getFeatmode()+")");
			f.println(); f.println();
			f.println("C; X-win; tie; O-win; X success rate; O success rate");
			for (int c=0; c<competitionNum; c++) {
				f.print(c + "; ");
				for (int p=0; p<3; p++) f.print(winrateC[c][p] + "; ");
				for (int p=0; p<2; p++) f.print(evalC[c][p] + "; ");
				f.println();
			}
			f.println();
			f.println("Averages:");
			f.print(";");
			for (int p=0; p<3; p++) f.print(winrate[p] + "; ");
			f.print((double)optimCountX/competitionNum+"; ");
			f.print((double)optimCountO/competitionNum+"; ");
			f.println();
			f.close();
			System.out.println("multiCompete: Output written to " + strDir + filename);
		} catch (IOException e) {
			System.out.println("Could not write to "+strDir+filename+" in XArenaFuncs::multiCompete()");
		}
		
//		if (!silent) {
			System.out.print("Avg. win rates: ");
			for (int i=0; i<3; i++) System.out.print(frm.format(winrate[i])+"  ");
			System.out.println(" (X/Tie/O)");
//		}
		
		} catch(RuntimeException ex) {
			MessageBox.show(xab, 
					ex.getMessage(), 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return winrate;
		
	} // multiCompete
	
	
	public String getLastMsg() {
		return lastMsg;
	}

}


