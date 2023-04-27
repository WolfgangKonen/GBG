package games.ZweiTausendAchtundVierzig;

import java.util.ArrayList;
import controllers.AgentBase;
import controllers.PlayAgent;
import games.*;
import games.ZweiTausendAchtundVierzig.Evaluator2048.EResult;
import starters.GBGLaunch;
import tools.Types;

/**
 * This class is used to make a batch evaluation of 2048 agents. 
 * See {@link #main(String[])} for details on the command line arguments
 * <p>
 * This program should normally not require any GUI facilities, so it should run on machines having no graphical
 * user interface system like X11 or Win. <br>
 * If there should be any X11 incompatibilities, the program can be run anyhow on Ubuntu consoles w/o X11 
 * if you use the command
 * <pre>
 *    xvfb-run java -jar GBGBatch2048.jar ...
 * </pre>
 *  
 * @author Wolfgang Konen, TH Koeln, 2020
 * 
 * @see GBGLaunch
 * @see Arena
 * @see XArenaFuncs
 *  
 *
 */
public class GBGBatch2048 {

	private static final long serialVersionUID = 1L;
	/**
	 * The default csv filename for the different batch facilities (batch1, batch2, batch3)
	 */
	public static String[] csvNameDef = {"eResult.csv","test1.csv","test2.csv"};
	public static Arena t_Game;
	private static GBGBatch2048 t_Batch = null;

	protected Evaluator m_evaluatorQ = null;
	protected Evaluator m_evaluatorT = null;
	
	/**
	 * Syntax:
	 * <pre>
	 * GBGBatch2048 agentFile [ csvFile nPlyMax] </pre>
	 * 	           
	 * This program evaluates {@code agentFile} for nPly = 0,1,...,{@code nPlyMax} and writes results to {@code csvFile}.
	 * <p>
	 * @param args <br>
	 * 			[0] {@code agentFile}: e.g. "tdntuple3.agt.zip". This agent is loaded from
	 *          	{@code agents/}{@link Types#GUI_DEFAULT_DIR_AGENT}{@code /2048/}. 
	 *          	It specifies the agent type and all its parameters for multi-evaluation 
	 *          	in {@link #batch1(String, XArenaButtons, String, int) batch1}.<br>
	 *          [1] (optional) {@code csvFile}: filename for CSV results (default: "eResult.csv"). <br> 
	 *          [2] (optional) {@code nPlyMax}: upper value for nPly (default 2). <br>
	 *          	
	 * @see Evaluator2048         
	 */
	public static void main(String[] args)  {
		t_Batch = new GBGBatch2048("General Board Game Playing");
		
		if (args.length<1) {
			System.err.println("[GBGBatch2048.main] needs at least 1 argument.");
			System.exit(1);
		}

		t_Game = new Arena2048("",false,true);

		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+t_Game.getGameName();
		String subDir = t_Game.getGameBoard().getSubDir();
		if (subDir != null) strDir += "/"+subDir;
		String filePath = strDir + "/" +args[0]; //+ "tdntuple3.agt.zip";

		String csvName = "eResult.csv";
		if (args.length>=2) csvName = args[1];

		int upper = 2; 
		try {
			if (args.length>=3) upper = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			e.printStackTrace(System.err);
			System.err.println("[GBGBatch2048.main]: args[2]='"+args[2]+"' is not a number!");
			System.exit(1);
		}

		// start a batch run without any window
		t_Batch.batch1(filePath,t_Batch.t_Game.m_xab,csvName,upper);

		System.exit(0);
	}

	public GBGBatch2048(String title) {
//		super(title);
		t_Batch = this;
	}
	
	/**
	 * Perform quick evaluation. Write results to file {@code csvName}.
	 * @param filePath		full path of the agent file	
	 * @param xab			arena buttons object, to assess parameters	
	 * @param csvName		filename for CSV results
	 * @param upper			upper value for nPly
	 */
	public void batch1(String filePath, XArenaButtons xab,	String csvName, int upper) {
		// load an agent to fill xab with the appropriate parameter settings
		PlayAgent pa = null;
		PlayAgent[] paVector;
		Evaluator2048 qEvaluator = null;
		EResult eResult;
		ArrayList<EResult> erList = new ArrayList<>();
		String str;
		boolean res = this.t_Game.loadAgent(0, filePath);		
		if (!res) {
			System.err.println("\n[GBGBatch2048.batch1] Aborted (no agent found).");
			return;
		}
		
		int[] nPlyArr = new int[upper+1];
		for (int i=0; i<nPlyArr.length; i++ ) nPlyArr[i]=i;

		for (int nPly : nPlyArr) {

			xab.oPar[0].setWrapperNPly(nPly);	// deprecated
			xab.wrPar[0].setWrapperNPly(nPly);
			String sAgent = xab.getSelectedAgent(0);
			xab.m_arena.m_xfun.m_PlayAgents = xab.m_arena.m_xfun.fetchAgents(xab);
			AgentBase.validTrainedAgents(xab.m_arena.m_xfun.m_PlayAgents, 1);
			paVector = xab.m_arena.m_xfun.wrapAgents(xab.m_arena.m_xfun.m_PlayAgents,
					xab, xab.m_arena.getGameBoard().getStateObs());
			pa = paVector[0];
			if (pa == null) throw new RuntimeException("Could not fetch Agent 0 = " + sAgent);

			// run Quick Evaluation
			try {
				int nPly1 = xab.wrPar[0].getWrapperNPly();
				int nPly2 = pa.getParWrapper().getWrapperNPly();
				assert nPly == nPly1 : "Ooops, nPly  and nPly1 differ! " + nPly + " != " + nPly1;
				assert nPly1 == nPly2 : "Ooops, nPly1 and nPly2 differ! " + nPly1 + " != " + nPly2;
				System.out.println("nPly: " + nPly1);
				int qem = xab.oPar[0].getQuickEvalMode();
				int verb = 0;
				qEvaluator = (Evaluator2048) xab.m_arena.makeEvaluator(pa, xab.m_arena.getGameBoard(), 0, qem, verb);
				EvalResult eRes = qEvaluator.eval(pa);
				eResult = qEvaluator.eResult;
				erList.add(eResult);
				str = eRes.getMsg();
				System.out.println(str);
			} catch (RuntimeException e) {
				e.printStackTrace(System.err);
				return;
			}
		}
		
		qEvaluator.eResult.printEResultList(csvName, erList, pa, xab.m_arena, "", "");
		System.out.println("Results written to "+csvName);
	} // batch1

}
