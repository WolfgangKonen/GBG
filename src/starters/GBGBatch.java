package starters;

import java.io.IOException;


import controllers.*;
import controllers.PlayAgent.AgentState;
import games.*;
import games.CFour.ArenaTrainC4;
import games.EWN.ArenaEWN;
import games.EWN.ArenaTrainEWN;
import games.Hex.ArenaHex;
import games.Hex.ArenaTrainHex;
import games.KuhnPoker.ArenaTrainKuhnPoker;
import games.Nim.ArenaNim2P;
import games.Nim.ArenaNim3P;
import games.Nim.ArenaTrainNim2P;
import games.Nim.ArenaTrainNim3P;
import games.Othello.ArenaTrainOthello;
import games.RubiksCube.ArenaTrainCube;
import games.Sim.ArenaSim;
import games.Sim.ArenaTrainSim;
import games.TicTacToe.ArenaTrainTTT;
import games.ZweiTausendAchtundVierzig.ArenaTrain2048;
import tools.Types;

/**
 * This class is used to start GBG for batch runs. 
 * See {@link #main(String[])} for details on the command line arguments.
 * <p>
 * This program should normally not require any GUI facilities, so it can be run on machines having no graphical
 * user interface system like X11 or Win. <br>
 * If there should be any X11 incompatibilities, the program can be run anyhow on Ubuntu consoles w/o X11 
 * if you use the command
 * <pre>
 *    xvfb-run java -jar GBGBatch.jar ...
 * </pre>
 *  
 * @author Wolfgang Konen, TH Koeln, 2020
 * 
 * @see GBGLaunch
 * @see ArenaTrain
 * @see XArenaFuncs
 *  
 *
 */
public class GBGBatch { 

	/**
	 * The default csv filename for the different batch facilities (batch1, batch2, ..., batch7)
	 */
	public static String[] csvNameDef = {"multiTrain.csv","multiTrainAlphaSweep.csv"
			,"multiTrainLambdaSweep.csv","multiTrainIncAmountSweep.csv"
			,"multiTrainOthello.csv","multiCompeteOthelloSweep.csv","multiCompeteOthello.csv"};
	private static GBGBatch t_Batch = null;
	protected static ArenaTrain arenaTrain;
	protected static String filePath = null;
	protected static String savePath = null;

	protected MTrainSweep mTrainSweep;

	/**
	 * Syntax:
	 * <pre>
	 * GBGBatch gameName n agentFile [ nruns maxGameNum csvFile scaPar0 scaPar1 scaPar2 ] </pre>
	 * <p>
	 * Examples:
	 * <pre>
	 * GBGBatch Hex 1 td3new_10-6.agt.zip 1 50000 multiTest.csv 4
	 * GBGBatch ConnectFour 1 TCL-EXP-NT3-al37-lam000-6000k-epsfin0.stub.agt.zip 10 6000000 multiTrain-noFA.csv
	 * </pre>         	
	 * @param args <br>
	 * 			[0] {@code gameName}: name of the game, suitable as subdirectory name in the 
	 *         		{@code agents} directory <br>
	 *          [1] {@code n}: 1,2,3,...,7  to call either
	 *          	{@link #batch1(int, int, String, XArenaButtons, GameBoard, String) batch1} (multiTrain) or
	 *              {@link #batch2(int, int, String, XArenaButtons, GameBoard, String) batch2} (multiTrainAlphaSweep) or
	 *              {@link #batch3(int, int, String, XArenaButtons, GameBoard, String) batch3} (multiTrainLambdaSweep) or
	 *              {@link #batch4(int, int, String, XArenaButtons, GameBoard, String) batch4} (multiTrainIncAmountSweep) or
	 *              {@link #batch5(int, String, String, GameBoard) batch5} (multiTrainSweep) or
	 *              {@link #batch6(int, String, GameBoard, String) batch6} (multiCompeteSweep) or
	 *              {@link #batch7(int, String, GameBoard, String) batch7} (multiCompete).
	 *              The last three options 5,6,7 are only for game Othello.
	 *              <br>
	 *          [2] {@code agentFile}: e.g. "tdntuple3.agt.zip". This agent is loaded from
	 *          	{@code agents/}{@link Types#GUI_DEFAULT_DIR_AGENT}{@code /gameName/}  (+ a suitable subdir, if 
	 *          	applicable). It specifies the agent type and all its parameters for multi-training 
	 *          	in {@link #batch1(int, int, String, XArenaButtons, GameBoard, String) batch1},
	 *          	{@link #batch2(int, int, String, XArenaButtons, GameBoard, String) batch2}  or 
	 *          	{@link #batch3(int, int, String, XArenaButtons, GameBoard, String) batch3}.
	 *          	In case of batch6, this arguments codes the directory where to search for agent files.<br>
	 *          [3] (optional) nruns: how many agents to train (default -1). In case of batch6 or batch7, this argument
	 *              contains {@code iterMCTS}.   <br>
	 *          [4] (optional) maxGameNum: maximum number of training episodes (default -1: take the parameter stored
	 *              in the loaded agent file.) <br>
	 *          [5] (optional) csvFile: filename for CSV results (defaults: "multiTrain.csv" or 
	 *          	"multiTrainAlphaSweep.csv" or "multiTrainLambdaSweep.csv" or "multiTrainIncAmountSweep.csv",
	 *          	see {@link #csvNameDef}) <br>
	 *          [6] (optional) scaPar0: scalable parameter 0 <br>
	 *          [7] (optional) scaPar1: scalable parameter 1 <br>
	 *          [8] (optional) scaPar2: scalable parameter 2 <br>
	 *          <p>
	 *          
	 * If <b>nruns</b> or <b>maxGameNum</b> are -1, their respective values stored in {@code agentFile} are taken.
	 * <p>
	 * Side effect: the last trained agent is stored to {@code <csvName>.agt.zip}, where
	 * {@code <csvname>} is {@code args[5]} w/o {@code .csv}
	 * <p>
	 * <b>scaPar0,1,2</b> contain the scalable parameters of a game (if a game supports such parameters). Example: The game 
	 * Hex has the board size (4,5,6,...) as scalable parameter scaPar0. If no scalable parameter is given as 
	 * command line argument, the defaults from {@link #setDefaultScaPars(String)} apply.
	 * 
	 * @throws IOException if s.th. goes wrong when loading the agent or saving the csv file.
	 */
	public static void main(String[] args) throws IOException {
		t_Batch = new GBGBatch();
		int nruns = -1;
		int maxGameNum = -1;
		String csvName = "";
		
		if (args.length<3) {
			System.err.println("[GBGBatch.main] needs at least 3 arguments.");
			System.exit(1);
		}

		try {
			csvName = csvNameDef[Integer.parseInt(args[1])-1];
		} catch(NumberFormatException e) {
			e.printStackTrace(System.err);
			System.err.println("[GBGBatch.main]: args[1]='"+args[1]+"' is not a number!");
			System.exit(1);
		}

		try {
			if (args.length>=4) nruns = Integer.parseInt(args[3]);
		} catch(NumberFormatException e) {
			e.printStackTrace(System.err);
			System.err.println("[GBGBatch.main]: args[3]='"+args[3]+"' is not a number!");
			System.exit(1);
		}
		try {
			if (args.length>=5) maxGameNum = Integer.parseInt(args[4]);
		} catch(NumberFormatException e) {
			e.printStackTrace(System.err);
			System.err.println("[GBGBatch.main]: args[4]='"+args[4]+"' is not a number!");
			System.exit(1);
		}
		if (args.length>=6) csvName = args[5];

		String selectedGame = args[0];
		String[] scaPar = setDefaultScaPars(selectedGame);
		for (int i = 0; i < 3; i++)
			if (args.length >= i + 7) scaPar[i] = args[i + 6];

		arenaTrain = setupSelectedGame(selectedGame, scaPar);

		String agtFile = args[2];
		setupPaths(agtFile,csvName);		// builds filePath

		if (args[1].equals("5") || args[1].equals("6") || args[1].equals("7"))
			assert(selectedGame.equals("Othello")) : "batch5,6,7 only allowed for game Othello (uses Edax2)";

		// start a batch run without any GUI elements
		switch (args[1]) {
			case "1" -> t_Batch.batch1(nruns, maxGameNum, filePath, arenaTrain.m_xab, arenaTrain.getGameBoard(), csvName);
			case "2" -> t_Batch.batch2(nruns, maxGameNum, filePath, arenaTrain.m_xab, arenaTrain.getGameBoard(), csvName);
			case "3" -> t_Batch.batch3(nruns, maxGameNum, filePath, arenaTrain.m_xab, arenaTrain.getGameBoard(), csvName);
			case "4" -> t_Batch.batch4(nruns, maxGameNum, filePath, arenaTrain.m_xab, arenaTrain.getGameBoard(), csvName);
			case "5" -> t_Batch.batch5(nruns, agtFile, filePath, arenaTrain.getGameBoard());
			case "6" -> t_Batch.batch6(nruns, agtFile, arenaTrain.getGameBoard(), csvName);
			case "7" -> t_Batch.batch7(nruns, agtFile, arenaTrain.getGameBoard(), csvName);
			default -> {
				System.err.println("[GBGBatch.main] args[1]=" + args[1] + " not allowed.");
				System.exit(1);
			}
		}

		System.exit(0);
	}

	public static ArenaTrain setupSelectedGame(String selectedGame, String[] scaPar){

		switch (selectedGame) {
			case "2048":
				return new ArenaTrain2048("", false);
			case "ConnectFour":
				return new ArenaTrainC4("", false);
			case "Hex":
				// Set HexConfig.BOARD_SIZE *prior* to calling constructor ArenaTrainHex,
				// which will directly call Arena's constructor where the game board and
				// the Arena buttons are constructed
				ArenaHex.setBoardSize(Integer.parseInt(scaPar[0]));
				return new ArenaTrainHex("", false);
			case "Nim":
				// Set NimConfig.{NUMBER_HEAPS,HEAP_SIZE,MAX_MINUS} *prior* to calling constructor
				// ArenaTrainNim2P, which will directly call Arena's constructor where the game board and
				// the Arena buttons are constructed
				ArenaNim2P.setNumHeaps(Integer.parseInt(scaPar[0]));
				ArenaNim2P.setHeapSize(Integer.parseInt(scaPar[1]));
				ArenaNim2P.setMaxMinus(Integer.parseInt(scaPar[2]));
				return new ArenaTrainNim2P("", false);
			case "Nim3P":
				// Set NimConfig.{NUMBER_HEAPS,HEAP_SIZE,MAX_MINUS,EXTRA_RULE} *prior* to calling constructor
				// ArenaNimTrainNim3P, which will directly call Arena's constructor where the game board and
				// the Arena buttons are constructed
				ArenaNim3P.setNumHeaps(Integer.parseInt(scaPar[0]));
				ArenaNim3P.setHeapSize(Integer.parseInt(scaPar[1]));
				ArenaNim3P.setMaxMinus(Integer.parseInt(scaPar[1]));    // Nim3P: always MaxMinus == HeapSize (!)
				ArenaNim3P.setExtraRule(Boolean.parseBoolean(scaPar[2]));
				return new ArenaTrainNim3P("", false);
			case "Othello":
				return new ArenaTrainOthello("", false);
			case "RubiksCube":
				// Set CubeConfig.{cubeType,boardVecType,twistType} *prior* to calling constructor
				// ArenaTrainCube, which will directly call Arena's constructor where the game board and
				// the Arena buttons are constructed
				ArenaTrainCube.setCubeType(scaPar[0]);
				ArenaTrainCube.setBoardVecType(scaPar[1]);
				ArenaTrainCube.setTwistType(scaPar[2]);
				return new ArenaTrainCube("", false);
			case "Sim":
				// Set ConfigSim.{NUM_PLAYERS,NUM_NODES} *prior* to calling constructor ArenaTrainSim,
				// which will directly call Arena's constructor where the game board and
				// the Arena buttons are constructed
				ArenaSim.setNumPlayers(Integer.parseInt(scaPar[0]));
				ArenaSim.setNumNodes(Integer.parseInt(scaPar[1]));
				ArenaSim.setCoalition(scaPar[2]);
				return new ArenaTrainSim("", false);
			case "TicTacToe":
				return new ArenaTrainTTT("", false);
			case "EWN":
				ArenaEWN.setConfig(scaPar[0]);
				ArenaEWN.setCellCoding(scaPar[1]);
				ArenaEWN.setRandomStartingPosition(scaPar[2]);
				return new ArenaTrainEWN("", false);
			case "KuhnPoker":
				return new ArenaTrainKuhnPoker("",false);
			default:
				System.err.println("[GBGBatch.main] args[0]=" + selectedGame + ": This game is unknown.");
				System.exit(1);
				return null;
		}
	}

	/**
	 * Set default values for the scalable parameters.
	 * <p>
	 * This is for the case where GBGBatch is started without {@code args[6], args[7], args[8]}. See {@link #main(String[])}.
	 */
	public static String[] setDefaultScaPars(String selectedGame) {
		String[] scaPar = new String[3];
		switch(selectedGame) {
		case "Hex": 
			scaPar[0]="6";		// the initial (recommended) value
			break;
		case "Nim": 
			scaPar[0]="3";		// 	
			scaPar[1]="5";		// the initial (recommended) values	
			scaPar[2]="5";		// 
			break;
		case "Nim3P":
			scaPar[0]="3";		// 	
			scaPar[1]="5";		// the initial (recommended) values	
			scaPar[2]="true";	// 
			break;
		case "Sim": 
			scaPar[0]="2";		 	
			scaPar[1]="6";			
			scaPar[2]="False";
			break;
		case "RubiksCube": 
			scaPar[0]="2x2x2";		 	
			scaPar[1]="CSTATE";			
			scaPar[2]="ALL";
			break;
		case "EWN":
			scaPar[0]="3x3 2-Player";
			scaPar[1]="[0,..,n]";
			scaPar[2]="False";
			break;
		case "2048":
		case "ConnectFour": 
		case "Othello": 
		case "TicTacToe":
		case "KuhnPoker":
			//
			// games with no scalable parameters
			//
			break;
		default: 
			System.err.println("[GBGBatch] "+selectedGame+": This game is unknown.");
			System.exit(1);
		}
		return scaPar;
	}

	protected static void setupPaths(String agtFile, String csvFile){
		String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
		String subDir = arenaTrain.getGameBoard().getSubDir();
		if (subDir != null) strDir += "/" + subDir;

		filePath = strDir + "/" + agtFile; //+ "tdntuple3.agt.zip";

		savePath = csvFile.replaceAll("csv", "agt.zip");
		savePath = strDir + "/" + savePath;
	}

	public GBGBatch() {
		t_Batch = this;
	}
	
	/**
	 * Perform multi-training. Write results to file {@code csvName}.
	 * @param trainNum		how many agents to train
	 * @param maxGameNum	maximum number of training games 
	 * @param filePath		full path of the agent file	
	 * @param xab			arena buttons object, to assess parameters	
	 * @param gb			game board object, needed by multiTrain for evaluators and start state selection
	 * @param csvName		filename for CSV results
	 * <p>
	 * If trainNum or maxGameNum are -1, the values stored in {@code xab} are taken.
	 */
	public void batch1(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab,	GameBoard gb, String csvName) {
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch1] Aborted (no agent found).");
			return;
		}
		
		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		
		// run multiTrain
		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = arenaTrain.m_xfun.multiTrain(0, xab.getSelectedAgent(0), xab, gb, csvName);
		arenaTrain.m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
		System.out.println("[GBGBatch.main] multiTrain finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}
	} // batch1

	/**
	 * Perform multi-training with alpha sweep. The alpha values to sweep are coded in
	 * arrays {@code alphaArr} and {@code alphaFinalArr}. <br> 
	 * Write results to file {@code csvName}. 
	 * @param trainNum		how many agents to train for each alpha
	 * @param maxGameNum	maximum number of training games 
	 * @param filePath		full path of the agent file	
	 * @param xab			arena buttons object, to assess parameters	
	 * @param gb			game board object, needed by multiTrain for evaluators and start state selection
	 * @param csvName		filename for CSV results
	 * <p>
	 * If trainNum or maxGameNum are -1, the values stored in {@code xab} are taken.
	 */
	public void batch2(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
		double[] alphaArr = {1.0, 2.5, 3.7, 5.0, 7.5, 10.0};
		double[] alphaFinalArr = alphaArr.clone();
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch2] Aborted (no agent found).");
			return;
		}
		
		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		
		// run multiTrainAlphaSweep
		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainAlphaSweep(0, alphaArr, alphaFinalArr, arenaTrain, xab, gb, csvName);
		arenaTrain.m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
		System.out.println("[GBGBatch.main] multiTrainAlphaSweep finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}
	} // batch2

	/**
	 * Perform multi-training with lambda sweep. The lambda values to sweep are coded in
	 * array {@code lambdaArr} in this method. <br> Write results to file {@code csvName}. 
	 * @param trainNum		how many agents to train for each lambda
	 * @param maxGameNum	maximum number of training games 
	 * @param filePath		full path of the agent file	
	 * @param xab			arena buttons object, to assess parameters	
	 * @param gb			game board object, needed by multiTrain for evaluators and start state selection
	 * @param csvName		filename for CSV results
	 * <p>
	 * If trainNum or maxGameNum are -1, the values stored in {@code xab} are taken.
	 */
	public void batch3(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
		double[] lambdaArr = {0.00, 0.04, 0.09, 0.16, 0.25};
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch3] Aborted (no agent found).");
			return;
		}
		
		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		
		// run multiTrainLambdaSweep
		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainLambdaSweep(0, lambdaArr, arenaTrain, xab, gb, csvName);
		arenaTrain.m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
		System.out.println("[GBGBatch.main] multiTrainLambdaSweep finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}
		
	} // batch3

	/**
	 * Perform multi-training with incAmount sweep (RubiksCube only).
	 * The incAmount values to sweep are coded in
	 * array {@code incAmountArr} in this method. <br> Write results to file {@code csvName}.
	 * @param trainNum		how many agents to train for each lambda
	 * @param maxGameNum	maximum number of training games
	 * @param filePath		full path of the agent file
	 * @param xab			arena buttons object, to assess parameters
	 * @param gb			game board object, needed by multiTrain for evaluators and start state selection
	 * @param csvName		filename for CSV results
	 * <p>
	 * If trainNum or maxGameNum are -1, the values stored in {@code xab} are taken.
	 */
	public void batch4(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
		double[] incAmountArr = {+0.5, 0.00, -0.03, -0.10, -0.5};
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch4] Aborted (no agent found).");
			return;
		}

		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);

		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainIncAmountSweep(0, incAmountArr, arenaTrain, xab, gb, csvName);
		arenaTrain.m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
		System.out.println("[GBGBatch.main] multiTrainIncAmountSweep finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}

	} // batch4

	/**
	 * Perform Othello multi-training. In each run, agent {@code pa} is constructed anew (to get different random tuples)
	 * and then trained.
	 * <p>
	 * Write results to directory {@code agents/Othello/multiTrain}.
	 * @param nruns		number of training runs
	 * @param filePath		agent filename
	 * @param gb			game board object, needed by multiCompeteSweep for start state selection
	 *
	 * @see MCompeteSweep#multiTrainSweepOthello(PlayAgent, String, int, int, ArenaTrain, GameBoard)  MCompeteMWrap.multiTrainSweepOthello
	 */
	public void batch5(int nruns, String agtFile, String filePath,
					   GameBoard gb) {

		int maxTrainGameNum=-1;		// number of episodes in each training. If -1, take maxGameNum from loaded agent

		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch6] Aborted (no agent found).");
			return;
		}
		PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];

		long startTime = System.currentTimeMillis();

		MCompeteSweep mcmw = new MCompeteSweep();
		mcmw.multiTrainSweepOthello(pa,agtFile,maxTrainGameNum,nruns,arenaTrain,gb);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch5] multiTrainSweep finished in "+elapsedTime+" sec. ");
	} // batch5

	/**
	 * Perform Othello multi-competition for all agents found in directory {@code agents/Othello/<agtDir>}, first with
	 * the base agent, then wrapped by MCTSWrapperAgent with iterMCTS iterations. In both cases, compete
	 * in all roles against Edax at different Edax depth levels.
	 * <p>
	 * Write results to file {@code csvName}.
	 * @param iterMCTS		how many iterations in MCTSWrapperAgent.
	 * @param agtDir
	 * @param gb			game board object, needed by multiCompeteSweep for start state selection
	 * @param csvName		filename for CSV results
	 */
	public void batch6(int iterMCTS, String agtDir, GameBoard gb, String csvName) {

		long startTime = System.currentTimeMillis();

		MCompeteSweep mcmw = new MCompeteSweep();
		mcmw.multiCompeteSweepOthello(iterMCTS,agtDir,arenaTrain,gb,csvName);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch6] multiCompeteSweep finished in "+elapsedTime+" sec: Results written to "+csvName);
	} // batch6

	/**
	 * Perform Othello multi-competition with MCTSWrapperAgent wrapped around agent in agtFile against
	 * Edax with different depth levels.
	 * <p>
	 * Write results to file {@code csvName}.
	 * @param iterMCTS		how many iterations in MCTSWrapperAgent.
	 * @param agtFile		agent filename
	 * @param gb			game board object, needed by multiCompeteSweep for start state selection
	 * @param csvName		filename for CSV results
	 */
	public void batch7(int iterMCTS, String agtFile,
					   GameBoard gb, String csvName) {

		int nruns=1;		// 1 for deterministic agent, >=1 for nondeterministic agent

		PlayAgent pa = arenaTrain.loadAgent(agtFile);	// load agent

		long startTime = System.currentTimeMillis();

		MCompeteSweep mcmw = new MCompeteSweep();
		mcmw.multiCompeteOthello(pa,iterMCTS,nruns,arenaTrain,gb,csvName);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch7] multiCompete finished in "+elapsedTime+" sec: Results written to "+csvName);
	} // batch7

}
