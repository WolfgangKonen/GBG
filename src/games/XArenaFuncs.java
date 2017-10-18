package games;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.text.*; 		// DecimalFormat, NumberFormat
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Random;

import javax.swing.JOptionPane;

import controllers.MC.MCAgent;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import org.jfree.data.xy.XYSeries;

import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.AgentBase;
import controllers.HumanPlayer;
import controllers.MinimaxAgent;
import controllers.MCTS.MCTSAgentT;
import controllers.TD.TDAgent;
import controllers.TD.ntuple.NTupleFactory;
import controllers.TD.ntuple.TDNTupleAgt;
import controllers.TD.ntuple2.TDNTuple2Agt;
import tools.LineChartSuccess;
import tools.Measure;
import tools.MessageBox;
import tools.Types;



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
 * @author Wolfgang Konen, TH Koeln, Nov'16
 * 
 */
public class XArenaFuncs 
{
	//public  boolean m_NetIsLinear = false;
	//public  boolean m_NetHasSigmoid = false;
	//public	PlayAgent m_PlayAgentX;
	//public	PlayAgent m_PlayAgentO;
	public  PlayAgent[] m_PlayAgents;
	private Arena m_Arena;
	//String sRandom = Types.GUI_AGENT_LIST[2];
	//String sMinimax = Types.GUI_AGENT_LIST[1];
	//RandomAgent random_agent = new RandomAgent(sRandom);
	//MinimaxAgent minimax_agent = new MinimaxAgent(sMinimax);
	protected Evaluator m_evaluatorT=null;
	protected Evaluator m_evaluatorQ=null;
	protected Evaluator m_evaluatorM=null;
	protected String lastMsg="";
	protected int numPlayers;
	
	protected Random rand;
	protected XYSeries seriesQ; 
	protected XYSeries seriesT; 
	protected LineChartSuccess lChart;
	
	public XArenaFuncs(Arena arena)
	{
		m_Arena = arena;
		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		m_PlayAgents = new PlayAgent[numPlayers];
		//m_PlayAgents[0] = new MinimaxAgent(sMinimax);
        rand = new Random(System.currentTimeMillis());	
	}
	
	/**
	 * Construct and return a new {@link PlayAgent}, based on the settings in 
	 * {@code sAgent} and {@code m_xab}. 
	 * <p>
	 *  
	 * @param sAgent	the string from the agent-select box
	 * @param m_xab		used only for reading parameter values from GUI members 
	 * @return			a new {@link PlayAgent} (initialized, but not yet trained)
	 * @throws IOException 
	 */
	// OLD:  Side effect: the class members {@link XArenaFuncs#m_NetIsLinear}, {@link XArenaFuncs#m_NetHasSigmoid} are set.
	protected PlayAgent constructAgent(String sAgent, XArenaButtons m_xab) throws IOException {
		PlayAgent pa = null;
		int maxGameNum=Integer.parseInt(m_xab.GameNumT.getText());
		int featmode = m_xab.tdPar.getFeatmode();
//		double alpha = Double.valueOf(m_xab.tdPar.alphaT.getText()).doubleValue();
//		double alphaFinal = Double.valueOf(m_xab.tdPar.alfinT.getText()).doubleValue();
//		double lambda = Double.valueOf(m_xab.tdPar.lambdaT.getText()).doubleValue();
//		double alphaChangeRatio = Math.pow(alphaFinal/alpha, 1.0/maxGameNum);
//		if (sAgent.equals("ValIt")) alphaChangeRatio = 1.0; 
//		this.m_NetIsLinear = m_xab.tdPar.LinNetType.getState();
//		this.m_NetHasSigmoid = m_xab.tdPar.withSigType.getState();
		
		if (sAgent.equals("TDS")) {
			Feature feat = m_xab.m_game.makeFeatureClass(m_xab.tdPar.getFeatmode());
			pa = new TDAgent(sAgent, m_xab.tdPar, m_xab.oPar, feat, maxGameNum);
			//pa = m_xab.m_game.makeTDSAgent(sAgent,m_xab.tdPar,maxGameNum); 
				// new TDPlayerTTT(sAgent,m_xab.tdPar,maxGameNum);
//		} else if (sAgent.equals("TDS2")) {
//			pa = new TDPlayerTT2(sAgent,m_xab.tdPar,maxGameNum);
//		} else if (sAgent.equals("TDS-NTuple-2")) {
//			// deprecated, only as debug check. Use class TD_NTPlayer instead
//			pa = new TDPlayerTTT(m_xab.tdPar,maxGameNum);
//		} else if (sAgent.equals("TD_NT")) {
//			pa = new TD_NTPlayer(m_xab.tdPar,maxGameNum, m_xab.tcPar);
//		} else if(sAgent.equals("TDS-NTuple")) {
//			pa = new TDSNPlayer(m_xab.tdPar, m_xab.tcPar,maxGameNum);
//		} else if (sAgent.equals("ValIt")) {
//			pa = new ValItPlayer(m_xab.tdPar,this.m_NetHasSigmoid,this.m_NetIsLinear,featmode,maxGameNum);
//		} else if (sAgent.equals("CMA-ES")) {
//			pa = new CMAPlayer(alpha,alphaChangeRatio,m_xab.cmaPar,this.m_NetHasSigmoid,this.m_NetIsLinear,featmode);
		} else if (sAgent.equals("TD-Ntuple")) {
			try {
				XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory(); 
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar, xnf);
				pa = new TDNTupleAgt(sAgent, m_xab.tdPar, m_xab.ntPar, m_xab.oPar, nTuples, xnf, maxGameNum);
			} catch (Exception e) {
				MessageBox.show(m_xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				//e.printStackTrace();
				pa=null;			
			}
		} else if (sAgent.equals("TD-Ntuple-2")) {
			try {
				XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory(); 
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar, xnf);
				pa = new TDNTuple2Agt(sAgent, m_xab.tdPar, m_xab.ntPar, m_xab.oPar, nTuples, xnf, maxGameNum);
			} catch (Exception e) {
				MessageBox.show(m_xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				//e.printStackTrace();
				pa=null;			
			}
		} else if (sAgent.equals("Minimax")) {
			pa = new MinimaxAgent(sAgent,m_xab.oPar);
		} else if (sAgent.equals("Random")) {
			pa = new RandomAgent(sAgent);
		} else if (sAgent.equals("MCTS")) {
			pa = new MCTSAgentT(sAgent, null, m_xab.mctsParams, m_xab.oPar);
		} else if (sAgent.equals("MCTS Expectimax")) {
			pa= new MCTSExpectimaxAgt(sAgent, m_xab.mctsExpectimaxParams, m_xab.oPar);
		} else if (sAgent.equals("Human")) {
			pa = new HumanPlayer(sAgent);
		} else if (sAgent.equals("MC")) {
			pa = new MCAgent(sAgent, m_xab.mcParams, m_xab.oPar);
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
				pa= new MinimaxAgent(sAgent,m_xab.oPar);
			} else if (sAgent.equals("Random")) {
				pa= new RandomAgent(sAgent);
			} else if (sAgent.equals("MCTS")) {
				pa= new MCTSAgentT(sAgent,null,m_xab.mctsParams, m_xab.oPar);
			} else if (sAgent.equals("MCTS Expectimax")) {
				pa= new MCTSExpectimaxAgt(sAgent, m_xab.mctsExpectimaxParams, m_xab.oPar);
			} else if (sAgent.equals("Human")) {
				pa= new HumanPlayer(sAgent);
			} else if (sAgent.equals("MC")) {
				pa= new MCAgent(sAgent, m_xab.mcParams, m_xab.oPar);
			}else { // all the trainable agents:
				if (m_PlayAgents[n]==null) {
					if (sAgent.equals("TDS")) {
						//pa = m_xab.m_game.makeTDSAgent(sAgent, m_xab.tdPar, maxGameNum);
						Feature feat = m_xab.m_game.makeFeatureClass(m_xab.tdPar.getFeatmode());
						pa = new TDAgent(sAgent, m_xab.tdPar, m_xab.oPar, feat, maxGameNum);
					} else if (sAgent.equals("TD-Ntuple")) {
						try {
							XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
							NTupleFactory ntupfac = new NTupleFactory(); 
							int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar,xnf);
							pa = new TDNTupleAgt(sAgent, m_xab.tdPar, m_xab.ntPar, m_xab.oPar, nTuples, xnf, maxGameNum);
						} catch (Exception e) {
							MessageBox.show(m_xab, 
									e.getMessage(), 
									"Warning", JOptionPane.WARNING_MESSAGE);
							//e.printStackTrace();
							pa=null;			
						}
					} else if (sAgent.equals("TD-Ntuple-2")) {
						try {
							XNTupleFuncs xnf = m_xab.m_game.makeXNTupleFuncs();
							NTupleFactory ntupfac = new NTupleFactory(); 
							int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar,xnf);
							pa = new TDNTuple2Agt(sAgent, m_xab.tdPar, m_xab.ntPar, m_xab.oPar, nTuples, xnf, maxGameNum);
						} catch (Exception e) {
							MessageBox.show(m_xab, 
									e.getMessage(), 
									"Warning", JOptionPane.WARNING_MESSAGE);
							//e.printStackTrace();
							pa=null;			
						}
					}					
				} else {
					if (!sAgent.equals(m_PlayAgents[n].getName()))
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
	 * Perform one training of a {@link PlayAgent} sAgent with maxGameNum episodes. 
	 * @param sAgent	a string containing the class name of the agent
	 * @param xab		used only for reading parameter values from members td_par, cma_par
	 * @return	the trained PlayAgent
	 * @throws IOException 
	 */
	public PlayAgent train(String sAgent, XArenaButtons xab, GameBoard gb) throws IOException {
		int stopTest;			// 0: do not call Evaluator during training; 
								// >0: call Evaluator after every stopTest training games
		int stopEval;			// 0: do not stop on Evaluator; 
								// >0: stop, if Evaluator stays true for stopEval games
		int maxGameNum;			// maximum number of training games
		int numEval;			// evaluate the trained agent every numEval games
		int epiLength;			// maximum length of an episode
		boolean learnFromRM;	// if true, learn from random moves during training
		int gameNum=0;
		int verbose=2;
		boolean PLOTTRAINEVAL=false;
		maxGameNum = Integer.parseInt(xab.GameNumT.getText());
		numEval = xab.oPar.getNumEval();
		if (numEval==0) numEval=500; // just for safety, to avoid ArithmeticException in 'gameNum%numEval' below

		DecimalFormat frm = new DecimalFormat("#0.0000");
		PlayAgent pa = null;

		try {
			pa = this.constructAgent(sAgent, xab);
			if (pa==null) throw new RuntimeException("Could not construct agent = " + sAgent);
			
		}  catch(RuntimeException e) {
			MessageBox.show(xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return pa;			
		} 
		
		if (lChart==null) 
			lChart=new LineChartSuccess("Training Progress","gameNum","",
													  true,false);
		lChart.clearAndSetXY(xab);
		seriesQ = new XYSeries("Q Eval");		// "Q Eval" is the key of the XYSeries object
		lChart.addSeries(seriesQ);
		
		String pa_string = pa.getClass().getName();
//		if (pa_string.equals("TicTacToe.CMAPlayer")) 
//			pa_string = pa_string + " with fitness " + ((CMAPlayer) pa).getFitfunString() +
//			" and with " + ((CMAPlayer) pa).getNbRuns() + " restarts";
		System.out.println(pa.stringDescr());
		pa.setMaxGameNum(maxGameNum);
		pa.setNumEval(numEval);
		pa.setGameNum(0);
		System.out.println(pa.printTrainStatus());
		
		stopTest = xab.oPar.getStopTest();
		stopEval = xab.oPar.getStopEval();
		epiLength = xab.oPar.getEpiLength();
		learnFromRM = xab.oPar.useLearnFromRM();
		int qem = xab.oPar.getQuickEvalMode();
        m_evaluatorQ = xab.m_game.makeEvaluator(pa,gb,stopEval,qem,1);
        //
        // set Y-axis of existing lChart according to the current Quick Eval Mode
        lChart.setYAxisLabel(m_evaluatorQ.getPlotTitle());
        
		int tem = xab.oPar.getTrainEvalMode();
		//
		// doTrainEvaluation flags whether Train Evaluator is executed:
		// Evaluator m_evaluatorT is only constructed and evaluated, if the choice
		// boxes 'Quick Eval Mode' and 'Train Eval Mode' in tab 'Other pars' have
		// different values. 
		boolean doTrainEvaluation = (tem!=qem);
		if (doTrainEvaluation) {
	        m_evaluatorT = xab.m_game.makeEvaluator(pa,gb,stopEval,tem,1);
	        if (PLOTTRAINEVAL) {
				seriesT = new XYSeries("T Eval");		// "T Eval" is the key of the XYSeries object
				lChart.addSeries(seriesT);	        	
	        }
		}

		// Debug only: direct debug output to file debug.txt
		//TDNTupleAgt.pstream = System.out;
		//TDNTupleAgt.pstream = new PrintStream(new FileOutputStream("debug-TDNT.txt"));
		
// TODO: implement CMAPlayer correctly
//		if (sAgent.equals("CMA-ES")) {
//			((CMAPlayer) pa).trainLoop(maxGameNum,this,xab,verbose);
//		} else 
		{
			long startTime = System.currentTimeMillis();
			while (pa.getGameNum()<pa.getMaxGameNum())
			{		
				StateObservation so = soSelectStartState(gb,xab.oPar.useChooseStart01()); 

				pa.trainAgent(so /*,epiLength,learnFromRM*/);
				
				gameNum = pa.getGameNum();
				if (gameNum%numEval==0 ) { //|| gameNum==1) {
					double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
					System.out.println(pa.printTrainStatus()+", "+elapsedTime+" sec");
					xab.GameNumT.setText(Integer.toString(gameNum ) );
					
					m_evaluatorQ.eval();
					seriesQ.add((double)gameNum, m_evaluatorQ.getLastResult());
					if (doTrainEvaluation) {
						m_evaluatorT.eval();
						if (PLOTTRAINEVAL) 
							seriesT.add((double)gameNum, m_evaluatorT.getLastResult());
					}
					lChart.plot();
					startTime = System.currentTimeMillis();
				}
				
				if (stopTest>0 && (gameNum-1)%numEval==0 && stopEval>0) {
					if (doTrainEvaluation) {
						m_evaluatorT.eval();
						m_evaluatorT.goalReached(gameNum);
					}
					
					m_evaluatorQ.eval(); 
					if(m_evaluatorQ.goalReached(gameNum)) break;  // out of while
					
				}
			}
			
			// Debug only
			//TDNTupleAgt.pstream.close();
			
		} // if(sAgent)..else
		xab.GameNumT.setText(Integer.toString(maxGameNum) );		// restore initial value (maxGameNum)
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
		if (doTrainEvaluation) System.out.println("final "+m_evaluatorT.getMsg());

		return pa;
	}
	
	private StateObservation soSelectStartState(GameBoard gb, boolean chooseStart01) {
		StateObservation so; 
		if (chooseStart01) {
			so = gb.chooseStartState01();
		} else {
			so = gb.getDefaultStartState();  // recommended
		}					
		return so;
	}

	/**
	 * Perform trainNum cycles of training and evaluation for PlayAgent, each 
	 * training with maxGameNum games. 
	 * @param sAgent	a string containing the class name of the agent
	 * @param xab		used only for reading parameter values from members td_par, cma_par
	 * @throws IOException 
	 */
	public PlayAgent multiTrain(String sAgent, XArenaButtons xab, GameBoard gb) throws IOException {
		DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
		DecimalFormat frm = new DecimalFormat("#0.000");
		DecimalFormat frm2 = new DecimalFormat("+0.00;-0.00");
		DecimalFormat frm1 = new DecimalFormat("#0.00");
		int verbose=1;
		int stopEval = 0;

		int trainNum=Integer.valueOf(xab.TrainNumT.getText()).intValue();
		int maxGameNum=Integer.parseInt(xab.GameNumT.getText());
		int epiLength = xab.oPar.getEpiLength();
		boolean learnFromRM = xab.oPar.useLearnFromRM();
		PlayAgent pa = null;
		
		boolean doTrainEvaluation=false;
		boolean doMultiEvaluation=false;

		System.out.println("*** Starting multiTrain with trainNum = "+trainNum+" ***");

		Measure oT = new Measure();			// evalAgent-success rates of all trained agents
		Measure oQ = new Measure();			// competeBoth-success rates against MinimaxPlayer
		Measure oM = new Measure();			// competeBoth-success rates against RandomPlayer
		//Measure ov = new Measure();			// competeBoth-success rates against ValItPlayer
		Measure oC = new Measure();			// overall success measure S_C as def'd in GECCO'2009 paper
		MTrain mTrain;
		double evalT=0.0, evalM=0.0;
		ArrayList<MTrain> mtList = new ArrayList<MTrain>();
		int maxGameNumV=10000;
		//ValItPlayer valit_agent = trainedValItPlayer(maxGameNumV);
		
		for (int i=0; i<trainNum; i++) {
			xab.TrainNumT.setText(Integer.toString(i+1)+"/"+Integer.toString(trainNum) );

			try {
				pa = constructAgent(sAgent, xab);
				if (pa==null) throw new RuntimeException("Could not construct AgentX = " + sAgent);				
			}  catch(RuntimeException e) 
			{
				MessageBox.show(xab, 
						e.getMessage(), 
						"Warning", JOptionPane.WARNING_MESSAGE);
				return pa;			
			} 


			int qem = xab.oPar.getQuickEvalMode();
	        m_evaluatorQ = xab.m_game.makeEvaluator(pa,gb,stopEval,qem,1);
			int tem = xab.oPar.getTrainEvalMode();
			//
			// doTrainEvaluation flags whether Train Evaluator is executed:
			// Evaluator m_evaluatorT is only constructed and evaluated, if the choice
			// boxes 'Quick Eval Mode' and 'Train Eval Mode' in tab 'Other pars' have
			// different values. 
			doTrainEvaluation = (tem!=qem);
			if (doTrainEvaluation)
		        m_evaluatorT = xab.m_game.makeEvaluator(pa,gb,stopEval,tem,1);
			int mem = m_evaluatorQ.getMultiTrainEvalMode();
			//
			// doMultiEvaluation flags whether Multi Train Evaluator is executed:
			// Evaluator m_evaluatorM is only constructed and evaluated, if the value of
			// getMultiTrainEvalMode() differs from the values in choice boxes
			// 'Quick Eval Mode' and 'Train Eval Mode' in tab 'Other pars' . 
			doMultiEvaluation = ((mem!=qem) && (mem!=tem));
			if (doMultiEvaluation)
		        m_evaluatorM = xab.m_game.makeEvaluator(pa,gb,stopEval,mem,1);

			if (i==0) {
				String pa_string = pa.getClass().getName();
//				if (pa_string.equals("TicTacToe.CMAPlayer")) 
//					pa_string = pa_string + " with fitness " + ((CMAPlayer) m_PlayAgentX).getFitfunString() +
//					" and with " + ((CMAPlayer) m_PlayAgentX).getNbRuns() + " restarts";
				System.out.println(pa.stringDescr());
			}
			pa.setMaxGameNum(maxGameNum);
			pa.setGameNum(0);
			int player; 
			int numEval = xab.oPar.getNumEval();
			int gameNum;
			long actionNum, trnMoveNum;
			
// TODO: implement CMAPlayer correctly
//			if (sAgent.equals("CMA-ES")) {
//				((CMAPlayer) m_PlayAgentX).trainLoop(maxGameNum,this,xab,verbose);
//			} else 
			{
				long startTime = System.currentTimeMillis();
				while (pa.getGameNum()<pa.getMaxGameNum())
				{		
					StateObservation so = soSelectStartState(gb,xab.oPar.useChooseStart01()); 

					pa.trainAgent(so /*,epiLength,learnFromRM*/);
					
					gameNum = pa.getGameNum();
					actionNum = pa.getNumLrnActions();
					trnMoveNum = pa.getNumTrnMoves();
					if (gameNum%numEval==0 ) { //|| gameNum==1) {
						double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
						System.out.println(pa.printTrainStatus()+", "+elapsedTime+" sec");
						xab.GameNumT.setText(Integer.toString(gameNum ) );
						
						m_evaluatorQ.eval();
						if (doTrainEvaluation) {
							m_evaluatorT.eval();
							evalT = m_evaluatorT.getLastResult();
						}
						if (doMultiEvaluation) {
							m_evaluatorM.eval();
							evalM = m_evaluatorM.getLastResult();
						}
						
                        // gather information for later printout to agents/gameName/csv/multiTrain.csv.
						mTrain = new MTrain(i,gameNum,m_evaluatorQ.getLastResult(),evalT,evalM,
											actionNum,trnMoveNum);
						mtList.add(mTrain);

						startTime = System.currentTimeMillis();
					}
				}
				
			} // if(sAgent)..else
			m_evaluatorQ.eval();
			oQ.add(m_evaluatorQ.getLastResult());
			if (doTrainEvaluation) {
				m_evaluatorT.eval();
				oT.add(m_evaluatorT.getLastResult());								
			}
			if (doMultiEvaluation) {
				m_evaluatorM.eval();
				oM.add(m_evaluatorM.getLastResult());
			}

			//ov.add(competeBoth(m_PlayAgentX, valit_agent, 100));
			//oC.add(1+(or.getVal()-0.9+om.getVal()+ov.getVal())/3.0);
			oC.add(1+(oM.getVal()-0.9+oQ.getVal())/2.0);

		} // for (i)
		System.out.println("Avg. "+ m_evaluatorQ.getPrintString()+frm3.format(oQ.getMean()) + " +- " + frm.format(oQ.getStd()));
		if (doTrainEvaluation) 
		  System.out.println("Avg. "+ m_evaluatorT.getPrintString()+frm3.format(oT.getMean()) + " +- " + frm.format(oT.getStd()));
		if (doMultiEvaluation)
		  System.out.println("Avg. "+ m_evaluatorM.getPrintString()+frm3.format(oM.getMean()) + " +- " + frm.format(oM.getStd()));
		//System.out.println("Avg. success rate (valiAgent, best is 0.0): "+frm3.format(ov.getMean()) + " +- " + frm.format(ov.getStd()));
		//System.out.println("Avg. success rate (ALL_Agent, best is 1.0): "+frm3.format(oC.getMean()) + " +- " + frm.format(oC.getStd()));
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
	 * @param gb		needed to get a default start state
	 * @return the fitness of pa, which is +1 if pa always wins, 0 if always tie or if #win=#loose
	 *         and -1 if pa always looses.  
	 * 
	 * @see XArenaButtons
	 */
	public static double competeBoth(PlayAgent pa, PlayAgent opponent, int competeNum,
									 GameBoard gb) {
		int verbose=0;
		double[] res;
		double resX, resO;

		StateObservation startSO = gb.getDefaultStartState();  // empty board

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
	public static double[] compete(PlayAgent paX, PlayAgent paO, StateObservation startSO,
			int competeNum, int verbose) {
		double[] winrate = new double[3];
		int xwinCount=0, owinCount=0, tieCount=0;
		DecimalFormat frm = new DecimalFormat("#0.000");
		boolean silent = (verbose==0 ? true : false);
		boolean nextMoveSilent = (verbose<2 ? true : false);
		StateObservation so;
		Types.ACTIONS actBest;
//		double[] VTable = null;
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
//					VTable	= new double[n+1];
					actBest = paX.getNextAction2(so, false, nextMoveSilent);
					so.advance(actBest);
					Player=-1;
				}
				else				// i.e. O-Move
				{
					int n=so.getNumAvailableActions();
//					VTable	= new double[n+1];
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
						if (!silent) System.out.println(k+": O wins");
						owinCount++;
						break;
					case 0:
						if (!silent) System.out.println(k+": Tie");
						tieCount++;
						break;
					case +1: 
						if (!silent) System.out.println(k+": X wins");
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
		
		if (!silent) {
			System.out.print("win rates: ");
			for (int i=0; i<3; i++) System.out.print(frm.format(winrate[i])+"  ");
			System.out.println(" (X/Tie/O)");
		}

		return winrate;
	} // compete
	
	/**
	 * Does the main work for menu items 'Single Compete' and 'Swap Compete'. 
	 * These items set enum {@link Arena#taskState} to either COMPETE or SWAPCMP. 
	 * Then the appropriate cases of {@code switch} in Arena.run() will call competeBase. 
	 * 'Compete' performs competeNum competitions AgentX as X vs. AgentO as O. 
	 * 'Swap Compete' performs competeNum competitions AgentX as O vs. AgentO as X. 
	 * The agents are assumed to be trained (!)
	 *  
	 * @param swap {@code false} for 'Compete' and {@code true} for 'Swap Compete'
	 * @param xab	used only for reading parameter values from GUI members 
	 */
	protected void competeBase(boolean swap, XArenaButtons xab, GameBoard gb) {
		//int competeNum=Integer.valueOf(xab.CompeteNumT.getText()).intValue();
		int competeNum=xab.winCompOptions.getNumGames();
		int numPlayers = gb.getStateObs().getNumPlayers();
		if (numPlayers!=2) {
			MessageBox.show(xab, 
					"Single/Swap Compete only available for 2-player games!", 
					"Error", JOptionPane.ERROR_MESSAGE);	
			return;
		}

		try {
			String AgentX = xab.getSelectedAgent(0);
			String AgentO = xab.getSelectedAgent(1);
			if (AgentX.equals("Human") | AgentO.equals("Human")) {
				MessageBox.show(xab, 
						"No compete for agent Human", 
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				StateObservation startSO = gb.getDefaultStartState();  // empty board

				PlayAgent[] paVector = fetchAgents(xab);

				AgentBase.validTrainedAgents(paVector,numPlayers);  
				// may throw RuntimeException

				int verbose=1;

				if (swap) {
					compete(paVector[1],paVector[0],startSO,competeNum,verbose);
				} else {
					compete(paVector[0],paVector[1],startSO,competeNum,verbose);	
				}
			}
					
		} catch(RuntimeException ex) {
			MessageBox.show(xab, 
					ex.getMessage(), 
					"Error", JOptionPane.ERROR_MESSAGE);

		}
	} // competeBase

	public void singleCompete(XArenaButtons xab, GameBoard gb) {
		this.competeBase(false, xab, gb);
	}
	
	public void swapCompete(XArenaButtons xab, GameBoard gb) {
		this.competeBase(true, xab, gb);
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
		String AgentX = xab.getSelectedAgent(0);
		String AgentO = xab.getSelectedAgent(1);
		//int competeNum=Integer.valueOf(xab.CompeteNumT.getText()).intValue();
		//int competitionNum=Integer.valueOf(xab.CompetitionsT.getText()).intValue();
		int competeNum=xab.winCompOptions.getNumGames();
		int competitionNum=xab.winCompOptions.getNumCompetitions();
		int maxGameNum = Integer.parseInt(xab.GameNumT.getText());
		int epiLength = xab.oPar.getEpiLength();
		boolean learnFromRM = xab.oPar.useLearnFromRM();
		Evaluator m_evaluatorX=null;
		Evaluator m_evaluatorO=null;
		double alpha = xab.tdPar.getAlpha();
		double lambda = xab.tdPar.getLambda();
//		this.m_NetIsLinear = xab.tdPar.LinNetType.getState();
//		this.m_NetHasSigmoid = xab.tdPar.withSigType.getState();
		
		double optimCountX=0.0,optimCountO=0.0;
		double[][] winrateC = new double[competitionNum][3];
		double[][] evalC = new double[competitionNum][2];
		PlayAgent paX=null, paO=null;
		if (verbose>0) System.out.println("Multi-Competition: "+competitionNum+" competitions with "
										 +competeNum+" games each, "+AgentX+" vs "+AgentO);

		if (AgentX.equals("Human") | AgentO.equals("Human")) {
			MessageBox.show(xab, 
					"No multiCompete for agent Human", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return winrate;
		} 
		for (int c=0; c<competitionNum; c++) {
			int player;
			
			try {
				paX = this.constructAgent(AgentX, xab);
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
				paO = this.constructAgent(AgentO, xab);
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
			int qem = xab.oPar.getQuickEvalMode();
			m_evaluatorX = xab.m_game.makeEvaluator(paX,gb,stopEval,qem,1);
			
//TODO: implement CMAPlayer correctly
//			if (AgentX.equals("CMA-ES")) {
//				((CMAPlayer) paX).trainLoop(maxGameNum,this,xab,verbose);
//			} else 
			{
				while (paX.getGameNum()<paX.getMaxGameNum())
				{							
					StateObservation so = soSelectStartState(gb,xab.oPar.useChooseStart01()); 

					paX.trainAgent(so /*,epiLength,learnFromRM*/);
				}
				
			} 

			m_evaluatorX.eval();
			evalC[c][0] = m_evaluatorX.getLastResult();
			optimCountX += evalC[c][0];
			
			m_evaluatorO = xab.m_game.makeEvaluator(paO,gb,stopEval,qem,1);
			
//TODO: implement CMAPlayer correctly
//			if (AgentO.equals("CMA-ES")) {
//				((CMAPlayer) paO).trainLoop(maxGameNum,this,xab,verbose);
//			} else 
			{
				while (paO.getGameNum()<paO.getMaxGameNum())
				{							
					StateObservation so = soSelectStartState(gb,xab.oPar.useChooseStart01()); 

					paX.trainAgent(so /*,epiLength,learnFromRM*/);
				}
				
			} 

			m_evaluatorO.eval();
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

		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+xab.m_game.getGameName()+"/";
		String filename = "Arena.comp.csv";
		checkAndCreateFolder(strDir);
		try {
			PrintWriter f; 
			f = new PrintWriter(new BufferedWriter(new FileWriter(strDir+filename)));
			f.println("alpha=" + alpha + ";  lambda=" + lambda + "; trained agents=" + competitionNum 
					  + ",  maxGameNum=" +maxGameNum);
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

	/**
	 * checks if a folder exists and creates a new one if it doesn't
	 *
	 * @param filePath the folder Path
	 * @return true if folder already exists
	 */
	private static boolean checkAndCreateFolder(String filePath) {
		File file = new File(filePath);
		boolean exists = file.exists();
		if(!file.exists()) {
			file.mkdirs();
		}
		return exists;
	}
	
}


