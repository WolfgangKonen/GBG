package games.TicTacToe;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;

import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import controllers.RandomAgent;
import controllers.TD.TDAgent;
import games.ArenaTrain;
import games.Evaluator;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.XArenaButtons;
import games.XArenaFuncs;
//import params.OtherParams;
import params.ParOther;
import params.ParTD;
import tools.LineChartSuccess;
import tools.Measure;
import tools.ScoreTuple;
import tools.Types;

/**
 * Wrapper class used to start TicTacToe in class {@link ArenaTrain} via a <b>main method</b>: <br> 
 * If no arguments are given, the GUI is launched; if an argument 1, 2 or 3 is given, 
 * start batch1, batch2 or batch3 from the main routine w/o starting the GUI. 
 *  
 * @author Wolfgang Konen, TH Cologne, Apr'08-Feb'09
 * 
 * @see ArenaTrain
 * @see XArenaFuncs
 *  
 *
 */
public class TicTacToeBatch extends ArenaTrainTTT {

	private static final long serialVersionUID = 1L;
	public ArenaTrainTTT t_Game;
	private static TicTacToeBatch t_Batch=null;

	public  boolean m_NetIsLinear = false;
	public  boolean m_NetHasSigmoid = false;
	public	PlayAgent m_PlayAgentX;
	public	PlayAgent m_PlayAgentO;
	private RandomAgent random_agent = new RandomAgent("Random");
	private MinimaxAgent minimax_agent = new MinimaxAgent("Minimax");
	protected Evaluator m_evaluator1=null;
	protected Evaluator m_evaluator2=null;
	protected Evaluator m_evaluator3=null;
	protected String lastMsg="";
	
	protected Random rand;
	protected XYSeries series; 
	protected LineChartSuccess lChart;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		TicTacToeBatch t_Frame = new TicTacToeBatch("General Board Game Playing");

		if (args.length==0) {
			throw new RuntimeException("[TicTacToeBatch.main] needs an args argument.");
		} else {
			// just a quick hack to start a batch run without any window
			if (args[0].equals("1")) {
				t_Batch.batch1(25,1.0,0.1,1.0,
						t_Frame.t_Game.m_xab,t_Frame.t_Game.getGameBoard());
			} else if (args[0].equals("2")) {
				t_Batch.batch2(25,1.0,0.1,1.0,
						t_Frame.t_Game.m_xab,t_Frame.t_Game.getGameBoard()); 
			} else if (args[0].equals("4")) {				
				t_Batch.batch4(10,0.5,0.001,0.6,
						t_Frame.t_Game.m_xab,t_Frame.t_Game.getGameBoard()); 
//			} else if (args[0].equals("3")) {
//				CMAParams cma_par = new CMAParams();		// see default par settings in CMAParams
//				t_Frame.t_Game.m_TTT.batch3(25,1.0,1.0,cma_par);
//			} else if (args[0].equals("4")) {				// starter for call from SPO
//				String propsFile = ((args.length==2) ? args[1] : "props.txt");
//				new TicTacToeSPO(propsFile);
			} else {
				throw new RuntimeException("[TicTacToeFrame.main] args="+args+" not allowed.");
			}
		}

	}

	/**
	 * Initialize the frame and {@link #t_Game}.
	 */
	public void init()
	{	
		addWindowListener(new WindowClosingAdapter());
		t_Game.init();
		setSize(465,400);		
		setBounds(0,0,465,400);
		//pack();
		setVisible(true);
	}	

	public TicTacToeBatch(String title) {
		super(title);
		t_Batch = this;
//		t_Game = new ArenaTrainTTT(this);
//		setLayout(new BorderLayout(10,10));
//		setJMenuBar(t_Game.m_menu);
//		add(t_Game,BorderLayout.CENTER);
//		add(new Label(" "),BorderLayout.SOUTH);	// just a little space at the bottom
		
	}
	
	protected static class WindowClosingAdapter
	extends WindowAdapter
	{
		public WindowClosingAdapter()  {  }

		public void windowClosing(WindowEvent event)
		{
			event.getWindow().setVisible(false);
			event.getWindow().dispose();
			System.exit(0);
		}
	}

	/**
	 * Perform measurements in 'batch' mode (different nets (LIN/BP), with and w/o sigmoid,  
	 * always TDAgent, always feature set T3). Write results to file TicTacToe.batch.csv.
	 * @param trainNum	how many agents to train
	 * @param alpha		learn parameter (its start value, decreasing) 
	 * @param alphaFinal	its final value
	 * @param lambda	
	 */
	public void batch1(int trainNum, double alpha, double alphaFinal, double lambda,
					   XArenaButtons xab,	GameBoard gb) {
		int stopEval = 0;
		PrintWriter f; 
		DecimalFormat frm = new DecimalFormat("#0.000");
		int featmode = 3;
		int maxGameArr[] = {10,20,50,100,200,500,1000,2000,5000,10000};
		boolean linArr[] = {true,true,false,false};	// true: output activation is linear fct of input; false: ... is backprop
		boolean sigArr[] = {false,true,false,true};	// false: output = output activation; true: output = sigmoid(output activation)
		
		double result[][] = new double[linArr.length][maxGameArr.length];
		ParTD tdPar = new ParTD();
		tdPar.setAlpha(alpha);
		tdPar.setAlphaFinal(alphaFinal);
		tdPar.setLambda(lambda);
		tdPar.setFeatmode(featmode);
		ParOther oPar = new ParOther();

		System.out.println("*** Starting TicTacToe.batch1 with trainNum = "+trainNum+" ***");

		// loop over the different net configurations:
		for (int p=0; p<linArr.length; p++) {	
			tdPar.setLinearNet(linArr[p]);
			tdPar.setSigmoid(sigArr[p]);
			// loop over the number of games each agent gets for training:
			for (int k=0; k<maxGameArr.length; k++) {		 
				double optimCount=0;			// sum of success rates of all trained agents
				int maxGameNum = maxGameArr[k];
				//double alphaChangeRatio = Math.pow(alphaFinal/alpha, 1.0/maxGameNum);
				// loop over the agent realizations:
				for (int i=0; i<trainNum; i++) {
					Feature feat = xab.m_game.makeFeatureClass(tdPar.getFeatmode());
					m_PlayAgentX = new TDAgent("TDS", tdPar, oPar, feat, maxGameNum);
					//m_PlayAgentX = new TDPlayerTTT("TDS",tdPar, maxGameNum);
			        m_evaluator1 = xab.m_game.makeEvaluator(m_PlayAgentX,gb,stopEval,9,1);

					if (i==0) {
						String pa_string = m_PlayAgentX.getClass().getName();
						System.out.println(m_PlayAgentX.stringDescr());
					}
					m_PlayAgentX.setMaxGameNum(maxGameNum);
					m_PlayAgentX.setGameNum(0);
					int player; 

					while (m_PlayAgentX.getGameNum()<m_PlayAgentX.getMaxGameNum())
					{							
						StateObservation so = gb.chooseStartState(null);
						m_PlayAgentX.trainAgent(so);

					}	
					m_evaluator1.eval(m_PlayAgentX);
					double e = m_evaluator1.getLastResult();
					System.out.println(e);
					optimCount += e;								
				} // for (i)
				result[p][k] = (double)optimCount/trainNum;
				System.out.println(maxGameNum + ": Avg. Success rate (evalAgent): "+frm.format(result[p][k]));
			} // for (k)
		} // for (p)
		try {
			String filename = "TicTacToe.batch.csv";
			f = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			f.println("alpha=" + alpha + ";  lambda=" + lambda + "; trained agents=" + trainNum); 
			f.println();
			f.println("maxGameNum; successLinLin; successLinSig; successBPLin; successBPSig");
			for (int k=0; k<maxGameArr.length; k++) {
				f.print(maxGameArr[k] + "; ");
				for (int p=0; p<linArr.length; p++) f.print(result[p][k] + "; ");
				f.println();
			}
			f.close();
			System.out.println("batch1: Output written to " + filename);
		} catch (IOException e) {
			System.out.println("IO-Exception in TicTacToe::batch1()");
		}
	} // batch1

	/**
	 * Perform measurements in 'batch' mode for {@link TDAgent} (different feature vectors, 
	 * always backprop).
	 * Write results to file TicTacToe.feat.csv and TicTacToe.feat.detail.csv. 
	 * These files can be loaded into Excel for subsequent analysis (CAUTION: the  
	 * region settings must be "," for the decimal separator to match the German Locale).  
	 * @param trainNum		how many agents to train
	 * @param alpha			learn parameter (its start value, decreasing) 
	 * @param alphaFinal	its final value
	 * @param lambda	
	 */
	public void batch2(int trainNum, double alpha, double alphaFinal, double lambda,
						XArenaButtons xab,	GameBoard gb	) {
		int stopEval = 0;
		DecimalFormat frm = new DecimalFormat("#0.000");
		DecimalFormat frm5 = new DecimalFormat("+0.00000;-0.00000");
		//int maxGameArr[] = {10,20,50,100,200,500,1000,2000,5000,10000,20000,50000};
		int maxGameArr[] = {20000};
		boolean linArr[] = {false,false};	
						// true: output activation is linear fct of input; false: ... is backprop
		boolean sigArr[] = {false,true};	
						// false: output = output activation; true: output = sigmoid(output activation)
		int featArr[] = {2}; //{1,2,3,4,9};
		//double EPS = 0.0;	 	// @deprecated, use epsilon
		double epsilon = 0.3;   // controls explorative moves, see TDAgent
		ParTD tdPar = new ParTD();
		tdPar.setAlpha(alpha);
		tdPar.setAlphaFinal(alphaFinal);
		tdPar.setLambda(lambda);
		tdPar.setEpsilon(epsilon);
		tdPar.setEpsilonFinal(0.0);
		ParOther oPar = new ParOther();
		
		System.out.println("*** Starting TicTacToe.batch2 with trainNum = "+trainNum+" ***");

		int player, maxGameNum=10000;
		//ValItPlayer valit_agent = trainedValItPlayer(maxGameNum);

		try {
			String filename = "TicTacToe.feat.csv";
			String filedetail = "TicTacToe.feat.detail.csv";
			PrintWriter f;
			boolean append=true;
			// header for summary file:
			f = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			f.println("alpha=" + alpha + ";  lambda=" + lambda + "; trained agents=" + trainNum +  "; EPS=" + epsilon + ";" ); 
			f.println();
			f.println("nettype; sigmoid; feature; maxGameNum; sucEval; sucRand; sucMinimax; success_C; time; stdEval; stdRand; stdMinimax; std_S_C");
			f.close(); 
			// header for detail file:
			f = new PrintWriter(new BufferedWriter(new FileWriter(filedetail)));
			f.println("alpha=" + alpha + "; alphaFinal=" + alphaFinal + "; lambda=" + lambda + "; trained agents=" + trainNum + "; EPS=" + epsilon + ";" );  
			f.println();
			f.println("i;nettype; sigmoid; feature; maxGameNum; sucEval; sucRand; sucMinimax; success_C; time");
			f.close(); 

			// k-loop over the number of games each agent gets for training:
			for (int k=0; k<maxGameArr.length; k++) {		 
				maxGameNum = maxGameArr[k];
				//double alphaChangeRatio = Math.pow(alphaFinal/alpha, 1.0/maxGameNum);
				
				// p-loop over the different net configurations:
				for (int p=0; p<sigArr.length; p++) {	
				// r-loop over the different feature modes:
				for (int r=0; r<featArr.length; r++) {	
					tdPar.setSigmoid(sigArr[p]);
					tdPar.setLinearNet(linArr[p]); 		
					tdPar.setFeatmode(featArr[r]);
					Measure oe = new Measure();			// evalAgent-success rates of all trained agents
					Measure om = new Measure();			// competeBoth-success rates against MinimaxPlayer
					Measure or = new Measure();			// competeBoth-success rates against RandomPlayer
					//Measure ov = new Measure();			// competeBoth-success rates against ValItPlayer
					Measure oC = new Measure();			// overall success measure S_C as def'd in GECCO'2009 paper
					long starttime = System.currentTimeMillis();
					
					// i-loop over the agent realizations:
					for (int i=0; i<trainNum; i++) {
						long istarttime = System.currentTimeMillis();
						//m_PlayAgentX = new TDPlayerTTT("TDS",tdPar,maxGameNum);
						Feature feat = xab.m_game.makeFeatureClass(tdPar.getFeatmode());
						m_PlayAgentX = new TDAgent("TDS", tdPar, oPar, feat, maxGameNum);
				        m_evaluator1 = xab.m_game.makeEvaluator(m_PlayAgentX,gb,stopEval,9,1);
						if (i==0) {
							System.out.println(m_PlayAgentX.stringDescr());
						}
						m_PlayAgentX.setMaxGameNum(maxGameNum);
						m_PlayAgentX.setGameNum(0);
	
						while (m_PlayAgentX.getGameNum()<m_PlayAgentX.getMaxGameNum())
						{							
							StateObservation so = gb.chooseStartState(null);
							m_PlayAgentX.trainAgent(so);
	
						}			
						m_evaluator1.eval(m_PlayAgentX);
				 		StateObservation so = gb.getDefaultStartState();
						oe.add(m_evaluator1.getLastResult());								
						or.add(t_Game.m_xfun.competeNPlayerAllRoles(new PlayAgtVector(m_PlayAgentX, random_agent), so, 100, 0).scTup[0]);
						om.add(t_Game.m_xfun.competeNPlayerAllRoles(new PlayAgtVector(m_PlayAgentX, minimax_agent), so, 1, 0).scTup[0]);
						//ov.add(competeBoth(m_PlayAgentX, valit_agent, 100));
						//oC.add(1+(or.getVal()-0.9+om.getVal()+ov.getVal())/3.0);
						oC.add(1+(or.getVal()-0.9+om.getVal())/2.0);
	
						f = new PrintWriter(new BufferedWriter(new FileWriter(filedetail,append)));
						f.print(i + "; " +  (linArr[p]?"LIN":" BP") + "; " + (sigArr[p]?"with":" w/o") + "; " 
								+ featArr[r] + "; "+ maxGameArr[k] + "; " + frm5.format(oe.getVal()) + "; "
								+ frm5.format(or.getVal()) + "; " + frm5.format(om.getVal()) + "; " 
								+ frm5.format(oC.getVal()) + "; ");
						f.print(((System.currentTimeMillis()-istarttime)/1000) + "; ");
						f.println();
						f.close();
					} // for (i)
					
					System.out.println(p+","+r+","+maxGameNum + ": Avg. success rate (evalAgent): "+frm.format(oe.getMean()));
					f = new PrintWriter(new BufferedWriter(new FileWriter(filename,append)));
					f.print((linArr[p]?"LIN":" BP") + "; " + (sigArr[p]?"with":" w/o") + "; " 
							+ featArr[r] + "; "+ maxGameArr[k] + "; " + frm5.format(oe.getMean()) + "; "
							+ frm5.format(or.getMean()) + "; " + frm5.format(om.getMean()) + "; " 
							+ frm5.format(oC.getMean()) + "; ");
					f.print(((System.currentTimeMillis()-starttime)/1000) + "; ");
					f.print(frm5.format(oe.getStd()) + "; " + frm5.format(or.getStd()) + "; " + 
							frm5.format(om.getStd()) + "; " + frm5.format(oC.getStd()) + "; ");
					f.println();
					f.close();
	
				} // for (r)
				} // for (p)
			} // for (k)
	
			System.out.println("batch2: Output written to out/" + filename + " and " + filedetail);
		} catch (IOException e) {
			System.out.println("IO-exception in TicTacToe::batch2()");
		}
	} // batch2
	
//	/**
//	 * Perform measurements in 'batch' mode for {@link CMAPlayer} (different feature 
//	 * vectors, linear or feedforward ('backprop') net, different fitness variants).
//	 * Write results to file TicTacToe.batch3.csv and TicTacToe.batch3.detail.csv. 
//	 * These files can be loaded into Excel for subsequent analysis (CAUTION: the  
//	 * region settings must be "," for the decimal separator to match the German Locale).  
//	 * @param trainNum	how many agents to train
//	 * @param alpha		learn parameter (not used)
//	 * @param lambda	learn parameter (not used)
//	 * @param cma_par	parameters for TTT_fit3 and TTT_fit4: nGames, nbRuns, nPool
//	 * 
//	 * @see CMAParams
//	 */
//	public void batch3(int trainNum, double alpha, double lambda, CMAParams cma_par) {
//		int stopEval = 0;
//		DecimalFormat frm = new DecimalFormat("#0.000");		
//		DecimalFormat frm5 = new DecimalFormat("+0.00000;-0.00000");
//		//int maxGameArr[] = {3,5,13,25,50};//,100};		// this line for nbRuns=4 -- OLD
//		int maxGameArr[] = {10,20,50,100,200};//,500,1000,2000};	
//						// max number of generations is maxGameArr (k-loop below)
//		int featArr[] = {1,2,3,4,9};//};
//						// feature typ (r-loop below)
//		boolean sigArr[] = {true,false,true,false};	
//						// false: output = output activation; true: output = sigmoid(output activation)
//		boolean linArr[] = {false,false,true,true};	
//						// true: output activation is linear fct of input; false: ... is backprop
//		int fitArr[] = {2,2,2,2};
//						// selector for fitness function: 1=TTT_fit1, 2=TTT_fit2
//		double mdArr[] = {0,0,0,0};
//						// m_mixDelta for Evaluator.evalAgent (relevant only for TTT_fit1)  
//		// linArr,fitArr, mdArr have to be at least of the same length as sigArr
//		
//		int nGames = Integer.valueOf(cma_par.nGamesT.getText()).intValue();
//						// number of games for TTT_fit2, _fit3, _fit4				
//		int nbRuns = Integer.valueOf(cma_par.nbRunT.getText()).intValue();
//						// number of (re)starts for TTT_fit4
//		int nPool = Integer.valueOf(cma_par.nPoolT.getText()).intValue();
//						// number of individuals in opponent pool for TTT_fit4	
//		
//		System.out.println("*** Starting TicTacToe.batch3 with trainNum = "+trainNum+" ***");
//
//		int player, maxGameNum=10000;
//		CMAPlayer cma_agent;
//		//ValItPlayer valit_agent = trainedValItPlayer(maxGameNum);
//
//		try {
//			String filename = "TicTacToe.batch3.csv";
//			String filedetail = "TicTacToe.batch3.detail.csv";
//			PrintWriter f;
//			boolean append=true;
//			// header for summary file:
//			f = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
//			String startline = "alpha=" + alpha + ";  lambda=" + lambda + "; trained agents=" + trainNum  
//			 	+ "; restarts=" + nbRuns + "; nGames=" + nGames + "; nPool=" + nPool; 
//			f.println(startline); f.println();
//			f.println("fitfun; mixDelta; nettype; sigmoid; feature; maxGameNum; sucEval; sucRand; sucMinimax; success_C; time; stdEval; stdRand; stdMinimax; std_S_C; idxBestEver; countEval; stopConditions:n.");
//			f.close(); 
//			// header for detail file:
//			f = new PrintWriter(new BufferedWriter(new FileWriter(filedetail)));
//			f.println(startline); f.println();
//			f.println("i;fitfun; mixDelta; nettype; sigmoid; feature; maxGameNum; sucEval; sucRand; sucMinimax; success_C; time; idxBestEver; countEval;");
//			f.close(); 
//			
//			// k-loop over the number of generations each agent gets for training:
//			for (int k=0; k<maxGameArr.length; k++) {		 
//				maxGameNum = maxGameArr[k];
//				if (maxGameNum<nbRuns)
//					throw new RuntimeException("[batch3] maxGameNum="+maxGameNum+" is too small for nbRuns="+nbRuns);
//				
//				// p-loop over the different net configurations:
//				for (int p=0; p<sigArr.length; p++) {	
//				// r-loop over the different feature modes:
//				for (int r=0; r<featArr.length; r++) {	
//					m_NetIsLinear=linArr[p];
//					m_NetHasSigmoid=sigArr[p];
//					int fitfunmode = fitArr[p];
//					int featmode = featArr[r];
//					boolean stop;
//					
//					Measure oe = new Measure();			// evalAgent-success rates of all trained agents
//					Measure om = new Measure();			// competeBoth-success rates against MinimaxPlayer
//					Measure or = new Measure();			// competeBoth-success rates against RandomPlayer
//					//Measure ov = new Measure();			// competeBoth-success rates against ValItPlayer
//					Measure oC = new Measure();			// overall success measure S_C as def'd in GECCO'2009 paper
//					long ibe,idxBestEver=0;				// index of best-ever solution (single i, mean over trainNum)
//					long cev,countEval=0;				// evaluation count (singel i, mean over trainNum)
//					long starttime = System.currentTimeMillis();
//					
//					//double alphaChangeRatio = Math.pow(alphaFinal/alpha, 1.0/maxGameNum);
//					// stopMap stores the stop reasons as key and their occurence-count during 
//					// trainNum agent realizations as Integer value:
//					HashMap<String,Integer> stopMap = new HashMap<String,Integer>();
//					
//					// loop over the agent realizations:
//					for (int i=0; i<trainNum; i++) {
//						long istarttime = System.currentTimeMillis();
//						m_PlayAgentX = new CMAPlayer(alpha, lambda, cma_par, m_NetHasSigmoid, m_NetIsLinear, featmode);
//				        m_evaluator = new Evaluator1(m_PlayAgentX,stopEval);
//						cma_agent = (CMAPlayer) m_PlayAgentX;
//						
//						if (i==0) {
//							System.out.println(m_PlayAgentX.printStatus());
//						}
//						
//						// Loop over runs (restarts): if nbRuns>1, we have multiple (nbRuns) restarts with
//						// potentially changing fitness function (TTT_fit4) and in total maxGameNum generations.
//						// If nbRuns==1, this is the version w/o restarts
//						for (int irun=0; irun<nbRuns; irun++) {
//							cma_agent.setMaxGameNum((irun+1)*maxGameNum/nbRuns);
//		
//							// loop over CMA-generations (of a single run)
//							while (m_PlayAgentX.getGameNum()<m_PlayAgentX.getMaxGameNum())
//							{							
//								m_evaluator.set_MixDelta(mdArr[p]);				// only relevant for TTT_fit1 (evalAgent)
//								player = ( rand.nextDouble()>0.5 ? 1 : -1);
//								stop = m_PlayAgentX.trainAgent(player);
//								// we have now in m_PlayAgent the best-ever solution
//								if (stop) {
//									//System.out.println("[TicTacToe.train] Stopped by agent's stop condition");
//									break; 		// out of while loop
//								}
//		
//							}
//							addToStopMap(cma_agent,stopMap);		// add the stopping reason to stopMap
//							
//							if (irun<nbRuns-1) {
//								// call for all but the last pass through irun-loop:
//								cma_agent.updateForRestart(irun);
//							}
//							//System.out.println("irun="+irun+", eval="+cma_agent.getCountEval());
//							System.out.println("(irun: minimax, valit,random) = ("+irun+": " 
//								+ frm5.format(competeBoth(m_PlayAgentX, minimax_agent, 1)) + ", "
//								//+ frm5.format(competeBoth(m_PlayAgentX, valit_agent, 100)) + ", "
//								+ frm5.format(competeBoth(m_PlayAgentX, random_agent, 100)) + ")");
//						} // for (irun)
//						
//						m_evaluator.set_MixDelta(0.0);
//						oe.add(m_evaluator.evalAgent1(m_PlayAgentX,true));
//						or.add(competeBoth(m_PlayAgentX, random_agent, 100));
//						om.add(competeBoth(m_PlayAgentX, minimax_agent, 1));
//						//ov.add(competeBoth(m_PlayAgentX, valit_agent, 100));
//						//oC.add(1+(or.getVal()-0.9+om.getVal()+ov.getVal())/3.0);
//						oC.add(1+(or.getVal()-0.9+om.getVal())/2.0);
//						ibe = cma_agent.getBestEvaluationNumber();
//						idxBestEver += ibe;
//						cev = cma_agent.getCountEval();
//						countEval += cev;
//
//						f = new PrintWriter(new BufferedWriter(new FileWriter(filedetail,append)));
//						f.print(i + "; " + fitfunmode + "; " + mdArr[p] + "; " + (linArr[p]?"LIN":" BP") + "; " + (sigArr[p]?"with":" w/o") + "; " 
//								+ featArr[r] + "; "+ maxGameArr[k] + "; " + frm5.format(oe.getVal()) + "; "
//								+ frm5.format(or.getVal()) + "; " + frm5.format(om.getVal()) + "; " + frm5.format(oC.getVal()) + "; ");
//						f.print(((System.currentTimeMillis()-istarttime)/1000) + "; ");
//						f.println(ibe + "; " + cev + "; ");
//						f.close();
//
//					} // for (i)
//					idxBestEver /= trainNum;
//					countEval /= trainNum;
//					System.out.println(p+","+r+","+maxGameNum + ": Avg. success rate (evalAgent): "+frm.format(oe.getMean()));
//					f = new PrintWriter(new BufferedWriter(new FileWriter(filename,append)));
//					f.print(fitfunmode + "; " + mdArr[p] + "; " + (linArr[p]?"LIN":" BP") + "; " + (sigArr[p]?"with":" w/o") + "; " 
//							+ featArr[r] + "; "+ maxGameArr[k] + "; " + frm5.format(oe.getMean()) + "; "
//							+ frm5.format(or.getMean()) + "; " + frm5.format(om.getMean()) + "; " + frm5.format(oC.getMean()) + "; ");
//					f.print(((System.currentTimeMillis()-starttime)/1000) + "; ");
//					f.print(frm5.format(oe.getStd()) + "; " + frm5.format(or.getStd()) + "; " + 
//							frm5.format(om.getStd()) + "; " + frm5.format(oC.getStd()) + "; ");
//					f.print(idxBestEver + "; " + countEval + "; ");
//					f.println(stopMap2String(stopMap));
//					f.close();
//				} // for (r)
//				} // for (p)
//			} // for (k)
//		
//			System.out.println("batch3: Output written to out/" + filename + " and " + filedetail );
//		} catch (IOException e) {
//			System.out.println("IO-exception in TicTacToe::batch3()");
//		}
//	} // batch3
	
	/**
	 * Perform measurements in 'batch' mode for {@link TDAgent} (different lambda, epochs 
	 * always lin).
	 * Write results to file TicTacToe.elig.csv and TicTacToe.elig.detail.csv. 
	 * These files can be loaded into Excel for subsequent analysis (CAUTION: the  
	 * region settings must be "," for the decimal separator to match the German Locale).  
	 * @param trainNum		how many agents to train
	 * @param alpha			learn parameter (its start value, decreasing) 
	 * @param alphaFinal	its final value
	 * @param lambda	
	 */
	public void batch4(int trainNum, double alpha, double alphaFinal, double lambda,
					   XArenaButtons xab,	GameBoard gb) {
		int stopTest=0;			// 0: do not call Evaluator during training; 
								// >0: call Evaluator after every stopTest training games
		int stopEval=0;			// 0: do not stop on Evaluator; 
								// >0: stop, if Evaluator stays true for stopEval games
		LineChartSuccess lChartB=null;
		XYSeries seriesB; 

		DecimalFormat frm = new DecimalFormat("#0.000");
		String pat5 = "+0.00000;-0.00000";
		//DecimalFormat frm5 = new DecimalFormat(pat5);
		DecimalFormat frm5 = new DecimalFormat();				
		frm5 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		frm5.applyPattern(pat5);		// this makes the number appear as 12.345 and not 12,345, even in Locale Germany

		//int maxGameArr[] = {10,20,50,100,200,500,1000,2000,5000,10000,20000,50000};
		int maxGameArr[] = {10000};
		boolean linArr[] = {false,false};	
						// true: output activation is linear fct of input; false: ... is neural net
		boolean sigArr[] = {false,true};	
						// false: output = output activation; true: output = sigmoid(output activation)
		double lamdArr[] = {0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9}; //,0.99}; 
		int epochArr[] = {0,1,10}; 
		int featArr[] = {4}; //{1,2,3,4,9};
		//double EPS = 0.1;	// @deprecated, use epsilon 
		double epsilon = 0.3;   // controls explorative moves, see TDAgent (0.3 for linear, 0.1 for NN)
		int verbose=0;		// verbosity of m_evaluator2
		ParTD tdPar = new ParTD();
		tdPar.setAlpha(alpha);
		tdPar.setAlphaFinal(alphaFinal);
		tdPar.setEpsilon(epsilon);
		tdPar.setEpsilonFinal(0.0);
		ParOther oPar = new ParOther();
		
		System.out.println("*** Starting TicTacToe.batch4 with trainNum = "+trainNum+
						   ", alpha="+alpha+", alphaFinal="+alphaFinal+ " ***");

		int player, maxGameNum=10000;
		//ValItPlayer valit_agent = trainedValItPlayer(maxGameNum);
		
		String title = "Batch 4: featSet="+featArr[0]+", eps="+epsilon;
		lChartB=new LineChartSuccess(title,"lambda","success against Minimax",
									 true,true);
		lChartB.clear();

		try {
			String filename = "TicTacToe.elig.csv";
			String filedetail = "TicTacToe.elig.detail.csv";
			PrintWriter f;
			boolean append=true;
			// header for summary file:
			f = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			f.println("alpha=" + alpha + "; alphaFinal=" + alphaFinal + "; trained agents=" + trainNum +  "; EPS=" + epsilon + ";" ); 
			f.println();
			f.println("nettype; sigmoid; lambda; epochs; maxGameNum; sucEval; sucRand; sucMinimax; success_C; time; stdEval; stdRand; stdMinimax; std_S_C");
			f.close(); 
			// header for detail file:
			f = new PrintWriter(new BufferedWriter(new FileWriter(filedetail)));
			f.println("alpha=" + alpha + "; alphaFinal=" + alphaFinal + "; trained agents=" + trainNum + "; EPS=" + epsilon + ";" );  
			f.println();
			f.println("i;nettype; sigmoid; lambda; epochs; maxGameNum; sucEval; sucRand; sucMinimax; success_C; time");
			f.close(); 

			// k-loop over the number of games each agent gets for training:
			for (int k=0; k<maxGameArr.length; k++) {		 
				maxGameNum = maxGameArr[k];
				//double alphaChangeRatio = Math.pow(alphaFinal/alpha, 1.0/maxGameNum);
				
				// p-loop over the different net configurations:
				for (int p=0; p<sigArr.length; p++) {	
				// r-loop over the different feature modes:
				for (int r=0; r<featArr.length; r++) {	
				// e-loop over different epoch modes
				for (int e=0; e<epochArr.length; e++) {
					String str = sigArr[p]+"/"+epochArr[e];
					seriesB = new XYSeries(str);		// str is the key of the XYSeries object
					lChartB.addSeries(seriesB);
					//dat.addSeries(seriesB);

					// l-loop over different lambda modes
					for (int l=0; l<lamdArr.length; l++) {
					tdPar.setSigmoid(sigArr[p]);
					tdPar.setLinearNet(linArr[p]); 		
					tdPar.setFeatmode(featArr[r]);
					m_NetHasSigmoid=sigArr[p];
					m_NetIsLinear=linArr[p]; 
					int featmode = featArr[r];
					lambda=lamdArr[l];
					int epochMax = epochArr[e];
					
					Measure oe = new Measure();			// evalAgent-success rates of all trained agents
					Measure om = new Measure();			// competeBoth-success rates against MinimaxPlayer
					Measure or = new Measure();			// competeBoth-success rates against RandomPlayer
					//Measure ov = new Measure();			// competeBoth-success rates against ValItPlayer
					Measure oC = new Measure();			// overall success measure S_C as def'd in GECCO'2009 paper
					long starttime = System.currentTimeMillis();
					
					// i-loop over the agent realizations:
					for (int i=0; i<trainNum; i++) {
						long istarttime = System.currentTimeMillis();
						tdPar.setLambda(lambda);
						tdPar.setEpochs(epochMax);
						//m_PlayAgentX = new TDPlayerTTT("TDS",tdPar,maxGameNum);
						Feature feat = xab.m_game.makeFeatureClass(tdPar.getFeatmode());
						m_PlayAgentX = new TDAgent("TDS", tdPar, oPar, feat, maxGameNum);
				        m_evaluator1 = xab.m_game.makeEvaluator(m_PlayAgentX,gb,stopEval,9,1);
				        		
				        m_evaluator2 = xab.m_game.makeEvaluator(m_PlayAgentX,gb,stopEval,2,1);

						if (i==0) {
							System.out.println(m_PlayAgentX.stringDescr());
						}
						m_PlayAgentX.setMaxGameNum(maxGameNum);
						m_PlayAgentX.setGameNum(0);
	
						while (m_PlayAgentX.getGameNum()<m_PlayAgentX.getMaxGameNum())
						{							
							StateObservation so = gb.chooseStartState(null);
							m_PlayAgentX.trainAgent(so);
	
						}		
						m_evaluator1.eval(m_PlayAgentX);
						m_evaluator2.eval(m_PlayAgentX);
				 		StateObservation so = gb.getDefaultStartState();
						oe.add(m_evaluator1.getLastResult());								
						or.add(t_Game.m_xfun.competeNPlayerAllRoles(new PlayAgtVector(m_PlayAgentX, random_agent), so, 100, 0).scTup[0]);
						//om.add(competeBoth(m_PlayAgentX, minimax_agent, 1, gb));
						om.add(m_evaluator2.getLastResult());
						//ov.add(competeBoth(m_PlayAgentX, valit_agent, 100, gb));
						//oC.add(1+(or.getVal()-0.9+om.getVal()+ov.getVal())/3.0);
						oC.add(1+(or.getVal()-0.9+om.getVal())/2.0);
	
						f = new PrintWriter(new BufferedWriter(new FileWriter(filedetail,append)));
						f.print(i + "; " +  (linArr[p]?"LIN":" BP") + "; " + (sigArr[p]?"with":" w/o") + "; " 
								+ lamdArr[l] + "; "+ epochArr[e] + "; "+ maxGameArr[k] + "; " + frm5.format(oe.getVal()) + "; "
								+ frm5.format(or.getVal()) + "; " + frm5.format(om.getVal()) + "; " 
								+ frm5.format(oC.getVal()) + "; ");
						f.print(((System.currentTimeMillis()-istarttime)/1000) + "; ");
						f.println();
						f.close();
					} // for (i)
					
					System.out.println(l+","+e+","+maxGameNum + ": Avg. success rate (Evaluator2): "+frm5.format(om.getMean()));
					f = new PrintWriter(new BufferedWriter(new FileWriter(filename,append)));
					f.print((linArr[p]?"LIN":" BP") + "; " + (sigArr[p]?"with":" w/o") + "; " 
							+ lamdArr[l] + "; "+ epochArr[e] + "; "+ maxGameArr[k] + "; " + frm5.format(oe.getMean()) + "; "
							+ frm5.format(or.getMean()) + "; " + frm5.format(om.getMean()) + "; " 
							+ frm5.format(oC.getMean()) + "; ");
					f.print(((System.currentTimeMillis()-starttime)/1000) + "; ");
					f.print(frm5.format(oe.getStd()) + "; " + frm5.format(or.getStd()) + "; " + 
							frm5.format(om.getStd()) + "; " + frm5.format(oC.getStd()) + "; ");
					f.println();
					f.close();

					seriesB.add((double)lambda, om.getMean());
					//lChart.addOrUpdate("Batch4", (double)lambda, om.getMean());					
					lChartB.plot();

					} // for (l)
				} // for (e)
				} // for (r)
				} // for (p)
			} // for (k)
	
			System.out.println("batch4: Output written to out/" + filename + " and " + filedetail);
		} catch (IOException e) {
			System.out.println("IO-exception in TicTacToe::batch4()");
		}
	} // batch4
	
	// dummy stub only (as long as the old batchTC is not implemented for SourceGBG)
	public double[] batchTC(boolean silent, XArenaButtons xab) throws IOException {
		return new double[1];
	} 

//	/**
//	 * Perform many trainings to test TC (temporal coherence)  
//	 * TDSNPlayer (N-tuple with TC) vs. Minimax; different alpha, different TC settings 
//	 * @param silent
//	 * @param tgb		used only for reading parameter values from GUI members 
//	 * @return			double[3], the percentage of games with X-win, tie, O-win 
//	 * 					(averaged over all competitions) 
//	 * @throws IOException 
//	 */
//	public double[] batchTC(boolean silent, TicGameButtons tgb) throws IOException 
//	{
//		DecimalFormat frm = new DecimalFormat("#0.000");
//		int verbose=1;
//		int stopEval = 0;
//		
//		// take settings from GUI tgb
//		int competeNum=Integer.valueOf(tgb.CompeteNumT.getText()).intValue();
//		int competitionNum=Integer.valueOf(tgb.CompetitionsT.getText()).intValue();
//		int maxGameNum = Integer.parseInt(tgb.GameNumT.getText());
//		double alpha = Double.valueOf(tgb.tdPar.alphaT.getText()).doubleValue();
//		double lambda = Double.valueOf(tgb.tdPar.lambdaT.getText()).doubleValue();
//		
//		int gameNum;
//		int test=100;
//		double optimCountX=0.0,optimCountO=0.0;
//		double[] alphaArr = {0.001,0.01,0.1}; //{0.00001,0.0001,0.001};
//		double[][] successC = new double[competitionNum*(maxGameNum/test)*alphaArr.length*3][4];
//		double[][] winrateC = new double[competitionNum][3];
//		double[][] evalC = new double[competitionNum][2];
//		double[] winrate = new double[3];
//		double m_om;
//		TDSNPlayer paX=null;
//		PlayAgent paO=null;
//
//		int z=0;
//		for (int TCval=0; TCval<3; TCval++) {
//		for (int a=0; a<alphaArr.length; a++) {
//		for (int c=0; c<competitionNum; c++) {
//			int player;
//			alpha=alphaArr[a];
//			
//			tgb.tdPar.setAlpha(alpha);
//			tgb.tdPar.setAlphaFinal(alpha);
//			switch (TCval) {
//			case 0: tgb.tcPar.setTC(false); break;
//			case 1: tgb.tcPar.setTC(true); tgb.tcPar.setTCImm("Immediate"); break;
//			case 2: tgb.tcPar.setTC(true); tgb.tcPar.setTCImm("Array"); break;
//			}
//			paX = new TDSNPlayer(tgb.tdPar, tgb.tcPar,maxGameNum);
//			paX.setMaxGameNum(maxGameNum);
//			paX.setGameNum(0);
//			
//			
//	        m_evaluator2 = new Evaluator2(paX,stopEval,2);
//				while (paX.getGameNum()<paX.getMaxGameNum())
//				{							
//					player = ( rand.nextDouble()>0.5 ? 1 : -1);
//					paX.trainNet(player);
//					
//					gameNum = paX.getGameNum();
//					if (gameNum%test==0) {
//						System.out.println("alpha="+frm.format(paX.getAlpha()) 
//								+ ", epsilon="+frm.format(paX.getEpsilon())
//								+ ", "+gameNum + " games");
//						tgb.GameNumT.setText(Integer.toString(gameNum ) );
//						assert(z<successC.length);
//						successC[z][0] = TCval;
//						successC[z][1] = alpha;
//						successC[z][2] = gameNum;
//						successC[z][3] = m_evaluator2.evaluateAgent2(paX);
//						z++;
//					}
//					
//
//				}
//				
//			paO = new MinimaxAgent(alpha,lambda);
//			
//			int startPlayer=+1;
//			int[][] startTable = new int[3][3];
//			winrateC[c] = compete(paX,paO,startPlayer,startTable,competeNum,0);
//			
//			for (int i=0; i<3; i++) winrate[i] += winrateC[c][i];				
//			if (!silent) {
//				System.out.print(c + ": ");
//				for (int i=0; i<3; i++) System.out.print(" "+frm.format(winrateC[c][i]));
//				System.out.println();
//			}
//		} // for (c)
//		} // for (a)
//		} // for (TCval)
//
//		
//		for (int i=0; i<3; i++) winrate[i] = winrate[i]/competitionNum;
//		if (!silent) {
//			System.out.println("*** Competition results: ***");
//			System.out.print("Avg. winrate: ");
//			for (int i=0; i<3; i++) System.out.print(" "+frm.format(winrate[i]));
//			System.out.println();
//			System.out.print("win rates: ");
//			for (int i=0; i<3; i++) System.out.print(frm.format(winrate[i])+"  ");
//			System.out.println(" (X/Tie/O)");
//		}
//		try {
//			String filename = "TicTacToe.TC.csv";
//			PrintWriter f; 
//			f = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
//			f.println("TCval, alpha, gameNum, successC");
//			for (int i=0; i<successC.length; i++) {
//				int j;
//				for (j=0; j<successC[0].length-1;j++)
//					f.print(successC[i][j]+", ");
//				f.println(successC[i][j]);
//			}
//			System.out.println("batchTC: successC written to bin/" + filename);
//			f.close();
//		} catch (IOException e) {
//			System.out.println("IO-exception in TicTacToe::batchTC()");
//		}
//				
//		return winrate;
//		
//	} // batchTC

}
