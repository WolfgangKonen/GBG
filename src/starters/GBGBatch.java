package starters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import controllers.*;
import games.*;
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
 * @see Arena
 * @see XArenaFuncs
 *  
 *
 */
public class GBGBatch extends SetupGBG {

	/**
	 * The default csv filenames for the different batch facilities (batch00, batch01, ..., batch09, batch10)
	 */
	public static String[] csvNameDef = {"multiTrain.csv","multiTrain.csv","multiTrainAlphaSweep.csv","multiTrainLambdaSweep.csv"
			,"multiTrainIncAmountSweep.csv","multiTrain","multiCompeteOthelloSweep.csv"
			,"multiCompeteOthello.csv","symmIterCube.csv","multiTrainCube.csv","multiEvalCube.csv"};
	private static GBGBatch t_Batch = null;
	protected static Arena arenaTrain;
	protected static String filePath = null;
	protected static String savePath = null;
	protected static String propsNameDef = "src/starters/props_batch.txt";	// fallback, if prosName is not found

	protected MTrainSweep mTrainSweep = new MTrainSweep();

	/**
	 * Syntax:
	 * <pre>
	 * GBGBatch gameName n agentFile [ nruns maxGameNum csvFile scaPar0 scaPar1 scaPar2 propsName] </pre>
	 * <p>
	 * Examples:
	 * <pre>
	 * GBGBatch Hex 1 td3new_10-6.agt.zip 1 50000 multiTest.csv 4
	 * GBGBatch ConnectFour 1 TCL-EXP-NT3-al37-lam000-6000k-epsfin0.stub.agt.zip 10 6000000 multiTrain-noFA.csv
	 * </pre>         	
	 * @param args <br>
	 * 			[0] {@code gameName}: name of the game, suitable as subdirectory name in the 
	 *         		{@code agents} directory <br>
	 *          [1] {@code n}: 0,1,2,3,...,7,8,9,10  to call either
	 *          	{@link #batch00(int, int, String, XArenaButtons, GameBoard, String) batch00} (multiTrain) or <br>
	 *              {@link #batch01(int, int, Properties, String, String, XArenaButtons, GameBoard, String)  batch01} (multiTrain_M) or <br>
	 * 	            {@link #batch02(int, int, String, XArenaButtons, GameBoard, String) batch02} (multiTrainAlphaSweep) or <br>
	 *              {@link #batch03(int, int, String, XArenaButtons, GameBoard, String) batch03} (multiTrainLambdaSweep) or <br>
	 *              {@link #batch04(int, int, String, XArenaButtons, GameBoard, String) batch04} (multiTrainIncAmountSweep) or <br>
	 *              {@link #batch05(int, int, Properties, String, String, XArenaButtons, GameBoard, String) batch05} (multiTrainSweep) or <br>
	 *              {@link #batch06(int, Properties, String, GameBoard, String) batch06} (multiCompeteSweep) or <br>
	 *              {@link #batch07(int, Properties, String, GameBoard, String) batch07} (multiCompete) or <br>
	 *              {@link #batch08(String, String, int, Properties) batch08} ({@link MCubeIterSweep}) or <br>
	 *              {@link #batch09(int, int, Properties, String, String, XArenaButtons, GameBoard) batch09} (multiTrainSweepCube) or <br>
	 *              {@link #batch10(int, Properties, String[], String, String, XArenaButtons, GameBoard, String) batch10}
	 *             		   (multiTrainSweepCube).<br>
	 *              The values 5,6,7 are only for game Othello, values 8,9,10 are only for game RubiksCube.
	 *              <br>
	 *          [2] {@code agentFile}: e.g. "tdntuple3.agt.zip". This agent is loaded from
	 *          	{@code agents/}{@link Types#GUI_DEFAULT_DIR_AGENT}{@code /gameName/}  (+ a suitable subdir, if 
	 *          	applicable). It specifies the agent type and all its parameters for multi-training in
	 *          	{@link #batch01(int, int, Properties, String, String, XArenaButtons, GameBoard, String) batch01},
	 *          	{@link #batch02(int, int, String, XArenaButtons, GameBoard, String) batch02}  or
	 *          	{@link #batch03(int, int, String, XArenaButtons, GameBoard, String) batch03}.
	 *          	In case of batch06 or batch08, this arguments codes the directory where to search for agent files.<br>
	 *          [3] (optional) {@code nruns}: how many agents to train (default -1). In case of batch06 or batch07, this argument
	 *              contains {@code iterMCTS}.  In case of batch08, this argument codes whether pTwist levels are averaged
	 *              ({@code nruns!=1}) or whether they are evaluated and reported for each pTwist ({@code nruns==1})  <br>
	 *          [4] (optional) {@code maxGameNum}: maximum number of training episodes (default -1: take the parameter stored
	 *              in the loaded agent file.) Irrelevant in case of batch06,07,08,10. <br>
	 *          [5] (optional) {@code csvFile}: filename for CSV results (defaults: "multiTrain.csv" or
	 *          	"multiTrainAlphaSweep.csv" or ..., see {@link #csvNameDef}).
	 *          	In case of batch05, this argument codes {@code trainOutDir} (where to store trained agents). <br>
	 *          [6] (optional) {@code scaPar0}: scalable parameter 0 <br>
	 *          [7] (optional) {@code scaPar1}: scalable parameter 1 <br>
	 *          [8] (optional) {@code scaPar2}: scalable parameter 2 <br>
	 *          [9] (optional) {@code propsName}: filename with properties (further parameters)
	 *          <p>   <pre></pre>
	 *
	 * If {@code nruns} or {@code maxGameNum} are -1, their respective values stored in {@code agentFile} are taken.
	 * <p>
	 * Side effect: If it is a training batch run, the last trained agent is stored to {@code <csvName>.agt.zip},
	 * where {@code <csvname>} is {@code args[5]} w/o {@code .csv}.
	 * <p>
	 * {@code scaPar0,1,2} contain the scalable parameters of a game (if a game supports such parameters). Example: The game
	 * Hex has the board size (4,5,6,...) as scalable parameter {@code scaPar0}. If no scalable parameter is given as
	 * command line argument, the defaults from {@link #setDefaultScaPars(String)} apply.
	 * <p>
	 * The file {@code csvname} is written to the game-specific csv directory
	 * <pre>  {@link Types#GUI_DEFAULT_DIR_AGENT agents}{@code /<gameName>[/subDir]/csv/} </pre>
	 *
	 * where the optional {@code subdir} for games with different flavors is formed from the scalable parameters
	 * {@code scaPar0,1,2}.
	 * <p>
	 * If {@code args[9]} is not given, the default for {@code propsName} is "props_batch.txt".
	 * {@code propsName} is first searched in the game-specific agents directory {@code agents/<gameName>[/subDir]/}.
	 * If that file is not found, try "src/starters/props_batch.txt". In properties files, scalar or
	 * vector (array) properties are coded for example as
	 * <pre>
	 * par1 = 1.5 <br>
	 * parVec = 4 8 16
	 *  </pre>
	 * (whitespace as separator between vector elements).
	 *
	 * @throws IOException if s.th. goes wrong when loading the agent or the properties file or when saving the csv file.
	 */
	 //* {@code extraPar} is only relevant for {@code batch01} (param {@code numEval}) and {@code batch08}
	 //* (param {@code c_puct}).
	public static void main(String[] args) throws IOException {
		t_Batch = new GBGBatch();
		int nruns = -1;
		int maxGameNum = -1;
		String propsName = "props_batch.txt";	// searched in agents/<gameDir>
		String csvName = "";
		//int numEval = -1;
		//double c_puct = 1.0;
		
		if (args.length<3) {
			System.err.println("[GBGBatch.main] needs at least 3 arguments.");
			System.exit(1);
		}

		try {
			csvName = csvNameDef[Integer.parseInt(args[1])];
		} catch(NumberFormatException e) {
			e.printStackTrace(System.err);
			System.err.println("[GBGBatch.main]: args[1]='"+args[1]+"' is an invalid number or not a number!");
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

//		if (args.length>=10) {
//			if (args[1].equals("1")) numEval = Integer.parseInt(args[9]);
//			if (args[1].equals("8")) c_puct = Double.parseDouble(args[9]);
//		}
		if (args.length>=10) propsName = args[9];

		arenaTrain = setupSelectedGame(selectedGame, scaPar,"",false,true);

		Properties prop = readProperties(propsName,propsNameDef);

		String agtFile = args[2];
		setupPaths(agtFile,csvName);		// builds filePath

		if (args[1].equals("5") || args[1].equals("6") || args[1].equals("7")) {
			assert(selectedGame.equals("Othello")) : "batch05,06,07 only allowed for game Othello";
			if (!args[1].equals("5")) {
				Properties sysprops   = System.getProperties();
				String os = sysprops.getProperty("os.name");
				assert(os.startsWith("Windows")) : "batch06,07 only allowed on Windows OS (uses edax.exe)";
			}
		}

		if (args[1].equals("8") || args[1].equals("9") || args[1].equals("10"))
			assert(selectedGame.equals("RubiksCube")) : "batch08,09,10 only allowed for game RubiksCube";

		// start a batch run without any GUI elements
		XArenaButtons xab = arenaTrain.m_xab;
		GameBoard gb = arenaTrain.getGameBoard();
		switch (args[1]) {
			case "0" -> t_Batch.batch00(nruns, maxGameNum, filePath, xab, gb, csvName);
			case "1" -> t_Batch.batch01(nruns, maxGameNum, prop, agtFile, filePath, xab, gb, csvName);
			case "2" -> t_Batch.batch02(nruns, maxGameNum, filePath, xab, gb, csvName);
			case "3" -> t_Batch.batch03(nruns, maxGameNum, filePath, xab, gb, csvName);
			case "4" -> t_Batch.batch04(nruns, maxGameNum, filePath, xab, gb, csvName);
			case "5" -> t_Batch.batch05(nruns, maxGameNum, prop, agtFile, filePath, xab, gb, csvName);
			case "6" -> t_Batch.batch06(nruns, prop, agtFile, gb, csvName);
			case "7" -> t_Batch.batch07(nruns, prop, agtFile, gb, csvName);
			case "8" -> t_Batch.batch08(agtFile, csvName, nruns, prop);
			case "9" -> t_Batch.batch09(nruns, maxGameNum, prop, agtFile, filePath, xab, gb);
			case "10"-> t_Batch.batch10(nruns, prop, scaPar, agtFile, filePath, xab, gb, csvName);
			default -> {
				System.err.println("[GBGBatch.main] args[1]=" + args[1] + " not allowed.");
				System.exit(1);
			}
		}

		System.exit(0);
	}

	/**
	 * Read properties file. Requires that {@link #arenaTrain} is set up for the selected game.
	 *
	 * @param propsName		the file to look for in {@code agents/<gameDir>/}
	 * @param propsNameDef  if {@code propsName} is not found, take {@code propsNameDef} as fallback
	 * @return	the properties read
	 * @throws IOException  if properties file not found
	 */
	protected static Properties readProperties(String propsName, String propsNameDef) throws IOException {
		Properties prop = new Properties();
		String strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + arenaTrain.getGameName();
		String subDir = arenaTrain.getGameBoard().getSubDir();
		if (subDir != null) strDir += "/" + subDir;
		try {
			FileInputStream fis = new FileInputStream(strDir + "/" + propsName);
			prop.load(fis);
			fis.close();
			System.out.println("Properties prop read from " + strDir + "/" + propsName);
		} catch (IOException e) {
			System.err.println("WARNING: File '" + strDir + "/" + propsName + "' not found, trying " + propsNameDef + " ...");
			try {
				FileInputStream fis = new FileInputStream(propsNameDef);
				prop.load(fis);
				fis.close();
				System.out.println("Properties prop read from " + propsNameDef);
			} catch (IOException e2) {
				System.err.println("ERROR: Neither file '" + strDir + "/" + propsName + "' nor file '" + propsNameDef +
						"' found, no properties read");
				throw (e2);
			}
		} // try
		return prop;
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
	public void batch00(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) {
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch01] Aborted (no agent found).");
			return;
		}
		
		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		
		// run multiTrain
		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = arenaTrain.m_xfun.multiTrain(0, xab.getSelectedAgent(0), xab, gb, csvName);
		System.out.println("[GBGBatch.main] multiTrain finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}
	} // batch00

	/**
	 * Perform multi-training. Write results to file {@code csvName}.
	 * <p>
	 * {@code batch01} differs from {@code batch00} in calling
	 * {@link MTrainSweep#multiTrain_M(int, String, Arena, XArenaButtons, GameBoard, String, String) MTrainSweep.multiTrain_M}
	 * instead of {@link XArenaFuncs#multiTrain(int, String, XArenaButtons, GameBoard, String) XArenaFuncs.multiTrain}
	 * and in reading parameters {@code numEval, trainOutDir} from {@code prop}. The difference is that
	 * {@link MTrainSweep#multiTrain_M(int, String, Arena, XArenaButtons, GameBoard, String, String) MTrainSweep.multiTrain_M}
	 * saves all trained agents to a yet unused filenam in {@code trainOutDir}.
	 *
	 * @param trainNum		how many agents to train
	 * @param maxGameNum	maximum number of training games
	 * @param prop			properties, for {@code numEval}	(evaluate agent every {@code numEval} episodes) and for
	 *                      {@code trainOutDir}
	 * @param agtFile		agent file name
	 * @param filePath		full path of the agent file
	 * @param xab			arena buttons object, to assess parameters
	 * @param gb			game board object, needed by multiTrain for evaluators and start state selection
	 * @param csvName		filename for CSV results
	 * <p>
	 * If {@code trainNum}, {@code maxGameNum} or {@code numEval} are -1, the values stored in {@code xab} are taken.
	 */
	public void batch01(int trainNum, int maxGameNum, Properties prop, String agtFile, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) {
		int numEval = getIntegerFromProps(prop,"numEval");
		String trainOutDir = getStringFromProps(prop, "trainOutDir");

		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch01] Aborted (no agent found).");
			return;
		}
		// overwrite trainNum, maxGameNum or numEval in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		if (numEval!=-1) xab.oPar[0].setNumEval(numEval);

		PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];
		pa.setAgentFile(agtFile);
		PlayAgent qa = arenaTrain.m_xfun.wrapAgentTrain(pa, pa.getParOther(), pa.getParWrapper(), null, gb.getDefaultStartState());
		pa.setWrapperParamsO(xab.oPar[0]);

		long startTime = System.currentTimeMillis();

		MTrainSweep mts = new MTrainSweep();
		try {
			mts.multiTrain_M(0,agtFile,arenaTrain,xab,gb,csvName,trainOutDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch01] Results written to "+csvName);
		System.out.println("[GBGBatch.batch01] multiTrain_M finished in "+elapsedTime+" sec. ");
	} // batch01

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
	public void batch02(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
		double[] alphaArr = {1.0, 2.5, 3.7, 5.0, 7.5, 10.0};
		double[] alphaFinalArr = alphaArr.clone();
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch02] Aborted (no agent found).");
			return;
		}
		
		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		
		// run multiTrainAlphaSweep
		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainAlphaSweep(0, alphaArr, alphaFinalArr, arenaTrain, xab, gb, csvName);
		System.out.println("[GBGBatch.main] multiTrainAlphaSweep finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}
	} // batch02

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
	public void batch03(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) throws IOException {
		double[] lambdaArr = {0.00, 0.04, 0.09, 0.16, 0.25};
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch03] Aborted (no agent found).");
			return;
		}
		
		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);
		
		// run multiTrainLambdaSweep
		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainLambdaSweep(0, lambdaArr, arenaTrain, xab, gb, csvName);
		System.out.println("[GBGBatch.main] multiTrainLambdaSweep finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}
		
	} // batch03

	/**
	 * Perform multi-training with incAmount sweep.
	 * Only relevant for RubiksCube in case bReplayBuf==true, see DAVI3Agent.
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
	public void batch04(int trainNum, int maxGameNum, String filePath,
					   XArenaButtons xab, GameBoard gb, String csvName) {
		double[] incAmountArr = {+0.5, 0.00, -0.03, -0.10, -0.5};
		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch04] Aborted (no agent found).");
			return;
		}

		// overwrite trainNum or maxGameNum in xab, if they are specified here
		if (trainNum!=-1) xab.setTrainNumber(trainNum);
		if (maxGameNum!=-1) xab.setGameNumber(maxGameNum);

		xab.m_arena.taskState=Arena.Task.MULTTRN;
		arenaTrain.m_xfun.m_PlayAgents[0] = mTrainSweep.multiTrainIncAmountSweep(0, incAmountArr, arenaTrain, xab, gb, csvName);
		System.out.println("[GBGBatch.main] multiTrainIncAmountSweep finished: Results written to "+csvName);
		res = arenaTrain.saveAgent(0, savePath);
		if (res) {
			System.out.println("[GBGBatch.main] last agent saved to "+savePath);
		} else {
			System.err.println("[GBGBatch.main] could not save agent!");
		}

	} // batch04

	/**
	 * Perform <strong>Othello</strong> multi-training. In each run, agent {@code pa} is constructed anew (to get
	 * different random tuples) and then trained.
	 * <p>
	 * If {@code batchSizeArr} (read from {@code prop}) is non-null, sweep additionally replay buffer's parameter
	 * {@code batchSize} (see {@code m_xab.rbPar[0].batchSize}) over all values given in {@code batchSizeArr}.
	 * <p>
	 * Write results to directory {@code agents/Othello/multiTrain}: (a) trained agents and (b) CSV-file
	 * <agtBase>*.csv with data for training curves.
	 * @param nruns			number of training runs
	 * @param maxGameNum	maximum number of training games. If -1, take maxGameNum from loaded agent
	 * @param prop			properties, for batchSizeArr
	 * @param agtFile		agent filename with parameters (maybe a stub)
	 * @param filePath		full file path to agent
	 * @param xab			arena buttons object, to assess parameters
	 * @param gb			game board object, needed for start state selection
	 * @param trainOutDir	where to store trained agents
	 *
	 * @see MCompeteSweep#multiTrainSweepOthello(PlayAgent, String, int, int, Arena, GameBoard, String, int[])
	 * 		MCompeteSweep.multiTrainSweepOthello
	 */
	public void batch05(int nruns, int maxGameNum, Properties prop, String agtFile, String filePath,
					   XArenaButtons xab, GameBoard gb, String trainOutDir) {

		//batchSizeArr:	either null or the RB batch size values to sweep over
		//int[] batchSizeArr = null;
		//int[] batchSizeArr = {0,100,200,400};
		//int[] batchSizeArr = {50,400};
		int[] batchSizeArr = getIntegerArrOrNullFromProps(prop, "batchSizeArr");

		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch05] Aborted (no agent found).");
			return;
		}
		PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];
		PlayAgent qa = arenaTrain.m_xfun.wrapAgentTrain(pa, pa.getParOther(), pa.getParWrapper(), null, gb.getDefaultStartState());
		pa.setWrapperParamsO(xab.oPar[0]);

		long startTime = System.currentTimeMillis();

		MCompeteSweep mcmw = new MCompeteSweep();
		mcmw.multiTrainSweepOthello(qa,agtFile,maxGameNum,nruns,arenaTrain,gb,trainOutDir,batchSizeArr);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch05] multiTrainSweep finished in "+elapsedTime+" sec. ");
	} // batch05

	/**
	 * Perform Othello multi-competition for all agents found in directory {@code agents/Othello/<agtDir>}, first with
	 * the base agent, then wrapped by MCTSWrapperAgent with iterMCTS iterations. In both cases, compete
	 * in all roles against Edax at different Edax depth levels.
	 * <p>
	 * Write results to file {@code csvName}.
	 * @param iterMCTS		how many iterations in MCTSWrapperAgent.
	 * @param prop			properties, for depthArr
	 * @param agtDir		directory with Othello agents
	 * @param gb			game board object, needed by multiCompeteSweep for start state selection
	 * @param csvName		filename for CSV results
	 */
	public void batch06(int iterMCTS, Properties prop, String agtDir, GameBoard gb, String csvName) {
		int[] depthArr = getIntegerArrOrNullFromProps(prop, "depthArr");
		long startTime = System.currentTimeMillis();

		MCompeteSweep mcmw = new MCompeteSweep();
		mcmw.multiCompeteSweepOthello(iterMCTS,depthArr,agtDir,arenaTrain,gb,csvName);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch06] multiCompeteSweep finished in "+elapsedTime+" sec: Results written to "+csvName);
	} // batch06

	/**
	 * Perform Othello multi-competition with MCTSWrapperAgent wrapped around agent in {@code agtFile} against
	 * Edax with different depth levels. Both agents perform in both roles (1st and 2nd).
	 * <p>
	 * The difference to {@link #batch06(int, Properties, String, GameBoard, String) batch06} is that only
	 * <i>one</i> {@code agtFile} is used.
	 * <p>
	 * Write results to file {@code csvName}.
	 * @param iterMCTS		how many iterations in MCTSWrapperAgent.
	 * @param prop			properties, for depthArr
	 * @param agtFile		agent filename
	 * @param gb			game board object, needed by multiCompeteSweep for start state selection
	 * @param csvName		filename for CSV results
	 */
	public void batch07(int iterMCTS, Properties prop, String agtFile,
					   GameBoard gb, String csvName) {
		int[] depthArr = getIntegerArrOrNullFromProps(prop, "depthArr");

		int nruns=1;		// 1 for deterministic agent, >=1 for nondeterministic agent

		PlayAgent pa = arenaTrain.loadAgent(agtFile);	// load agent

		long startTime = System.currentTimeMillis();

		MCompeteSweep mcmw = new MCompeteSweep();
		mcmw.multiCompeteOthello(pa,iterMCTS,depthArr,nruns,arenaTrain,gb,csvName);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch07] multiCompete finished in "+elapsedTime+" sec: Results written to "+csvName);
	} // batch07

	/**
	 * Perform RubiksCube evaluation for all agents found in directory {@code agents/RubiksCube/<gameDir>/<agtDir>}. <br>
	 * Each agent is wrapped by MCTSWrapperAgent with {@code iter} iterations for all {@code iter} : {@code iterMWrapArr}.
	 * <p>
	 * Write results to file {@code agents/<gameDir>/csv/<csvName>}.
	 * @param agtDir		directory with RubikCube agents
	 * @param csvName		filename for CSV results
	 * @param pMode			switch for pTwist level
	 * @param prop			properties, for iterMWrapArr, c_puct, pMinEval, pMaxEval
	 */
	public void batch08(String agtDir, String csvName, int pMode, Properties prop) {

		//int[] iterMWrapArr = {0,100,800}; //{0, 50, 100, 200, 400, 800};			// now read from prop
		int[] iterMWrapArr = getIntegerArrFromProps(prop, "iterMWrapArr");
		double c_puct = getDoubleFromProps(prop, "c_puct");

		long startTime = System.currentTimeMillis();

		MCubeIterSweep mcis = new MCubeIterSweep();
		if (pMode!=1) {
			int pMinEval = getIntegerFromProps(prop,"pMinEval_avg");
			int pMaxEval = getIntegerFromProps(prop,"pMaxEval_avg");
			mcis.symmIterTest3x3x3(iterMWrapArr,pMinEval,pMaxEval,c_puct,agtDir,arenaTrain,csvName);
		} else {
			int pMinEval = getIntegerFromProps(prop,"pMinEval_single");
			int pMaxEval = getIntegerFromProps(prop,"pMaxEval_single");
			mcis.symmIterSingle3x3x3(iterMWrapArr,pMinEval,pMaxEval,c_puct,agtDir,arenaTrain,csvName);
		}

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch08] symmIterTest3x3x3 finished in "+elapsedTime+" sec: Results written to "+csvName);
	} // batch08

	/**
	 * Perform <strong>Rubik's Cube</strong> multi-training. In each run, agent {@code pa} is constructed anew (to get
	 * different random tuples) and then trained. <br>
	 * In contrast to
	 * {@link #batch01(int, int, Properties, String, String, XArenaButtons, GameBoard, String)  batch01} this method
	 * loops over all settings of {@code rewardPosArr} and {@code stepRewardArr} as specified in {@code prop}.
	 * <p>
	 * Write results to directory {@code agents/<gameDir>/<trainOutDir>} with {@code trainOutDir} specified in
	 * {@code prop}: (a) trained agents and (b) CSV-file <agtBase>*.csv with training curves.
	 *
	 * @param nruns			number of training runs
	 * @param maxGameNum	maximum number of training games. If -1, take maxGameNum from loaded agent
	 * @param prop			properties, for rewardPosArr, stepRewardArr
	 * @param agtFile		agent filename with parameters (maybe a stub)
	 * @param filePath		full file path to agent
	 * @param xab			arena buttons object, to assess parameters
	 * @param gb			game board object, needed for start state selection
	 *
	 * @see MCubeIterSweep#multiTrainSweepCube(PlayAgent, String, int, int, double[], double[], Arena, GameBoard, String)
	 * 		MCubeIterSweep.multiTrainSweepCube
	 */
	public void batch09(int nruns, int maxGameNum, Properties prop, String agtFile, String filePath,
					   XArenaButtons xab, GameBoard gb) {
		//double[] rewardPosArr = {0.0001, 1.0, 10.0}; //{0.1, 1.0, 10.0};  {9.0};		// now read from prop
		//double[] stepRewardArr = {-0.04, -0.1, -1.0};// {-0.9}; {-0.04, -0.1, -1.0};
		double[] rewardPosArr = getDoubleArrFromProps(prop, "rewardPosArr");
		double[] stepRewardArr = getDoubleArrFromProps(prop, "stepRewardArr");
		String trainOutDir = getStringFromProps(prop, "trainOutDir_09");

		// load an agent to fill xab with the appropriate parameter settings
		boolean res = arenaTrain.loadAgent(0, filePath);
		if (!res) {
			System.err.println("\n[GBGBatch.batch09] Aborted (no agent found).");
			return;
		}
		PlayAgent pa = arenaTrain.m_xfun.m_PlayAgents[0];
		PlayAgent qa = arenaTrain.m_xfun.wrapAgentTrain(pa, pa.getParOther(), pa.getParWrapper(), null, gb.getDefaultStartState());
		pa.setWrapperParamsO(xab.oPar[0]);

		long startTime = System.currentTimeMillis();

		MCubeIterSweep mcis = new MCubeIterSweep();
		mcis.multiTrainSweepCube(qa,agtFile,maxGameNum,nruns,rewardPosArr,stepRewardArr,arenaTrain,gb,trainOutDir);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch09] multiTrainSweep finished in "+elapsedTime+" sec. ");
	} // batch09

	/**
	 * Evaluate the (non-wrapped and wrapped) performance of specific trained agents on <b>RubiksCube</b>.
	 * The wrapper is {@link controllers.MCTSWrapper.MCTSWrapperAgent MCTSWrapperAgent}. <br>
	 * In contrast to
	 * {@link #batch08(String, String, int, Properties) batch08} this method
	 * loops over all settings of {@code iterMWrapArr} and {@code cPuctArr} as specified in {@code prop}. It reads also
	 * values for {@code maxDepth, ee, pMin, pMax} from {@code prop}.
	 * <p>
	 * Write results to file {@code agents/<gameDir>/csv/<csvName>}.
	 *
	 * @param nruns			number of evaluation runs
	 * @param prop			properties, for iterMWrapArr, cPuctArr, maxDepth, ee, pMin and pMax
	 * @param agtFile		trained agent filename with parameters
	 * @param filePath		full file path to agent
	 * @param xab			arena buttons object, to assess parameters
	 * @param gb			game board object, needed for start state selection
	 * @param csvName		filename for CSV results
	 *
	 */
	public void batch10(int nruns, Properties prop, String[] scaPar, String agtFile, String filePath,
						XArenaButtons xab, GameBoard gb, String csvName) {
		int[] iterMWrapArr = getIntegerArrFromProps(prop, "iterMWrapArr_10");
		double[] cPuctArr = getDoubleArrFromProps(prop, "cPuctArr");
		int maxDepth = getIntegerFromProps(prop, "maxDepth"); // 25, 50, -1
		int ee =  getIntegerFromProps(prop, "ee");       // 20 or 50: eval-epiLength
		int pMin = getIntegerFromProps(prop,"pMinEval_10");
		int pMax = getPMaxFromProps(prop, scaPar);

		long startTime = System.currentTimeMillis();

		MCubeIterSweep mcis = new MCubeIterSweep();
		mcis.evalRubiksCube(scaPar,agtFile,iterMWrapArr,cPuctArr,maxDepth,ee,pMin,pMax, nruns, csvName);

		double elapsedTime = (System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("[GBGBatch.batch10] evalRubiksCube finished in "+elapsedTime+" sec. ");
	} // batch10

	// several helper methods to read properties:

	protected int getPMaxFromProps(Properties prop, String[] scaPar){
		int pMax;
		switch (scaPar[0]) {
			case "2x2x2" -> {
				switch (scaPar[2]) {
					case "HTM" -> pMax = getIntegerFromProps(prop, "pMaxEval_HTM_2x2x2");
					case "QTM" -> pMax = getIntegerFromProps(prop, "pMaxEval_QTM_2x2x2");
					default -> throw new RuntimeException("Not allowed value " + scaPar[2] + " for scaPar[2]");
				}
			}
			case "3x3x3" -> {
				switch (scaPar[2]) {
					case "HTM" -> pMax = getIntegerFromProps(prop, "pMaxEval_HTM_3x3x3");
					case "QTM" -> pMax = getIntegerFromProps(prop, "pMaxEval_QTM_3x3x3");
					default -> throw new RuntimeException("Not allowed value " + scaPar[2] + " for scaPar[2]");
				}
			}
			default -> throw new RuntimeException("Not allowed value " + scaPar[0] + " for scaPar[0]");
		}
		return pMax;
	}

	protected String getStringFromProps(Properties prop,String str) {
		String s = prop.getProperty(str);
		if (s==null) throw new RuntimeException("Property "+str+" not found in prop");
		return s;
	}

	protected int getIntegerFromProps(Properties prop,String str) {
		String s = prop.getProperty(str);
		if (s==null) throw new RuntimeException("Property "+str+" not found in prop");
		return Integer.parseInt(s);
	}

	protected double getDoubleFromProps(Properties prop,String str) {
		String s = prop.getProperty(str);
		if (s==null) throw new RuntimeException("Property "+str+" not found in prop");
		return Double.parseDouble(s);
	}

	protected int[] getIntegerArrFromProps(Properties prop, String str) {
		String s = prop.getProperty(str);
		if (s==null) throw new RuntimeException("Property "+str+" not found in prop");
		String[] result = s.split("\\s");	// split the string in tokens separated by whitespace
		int[] intArr = new int[result.length];
		for (int i=0; i<result.length; i++)
			intArr[i] = Integer.parseInt(result[i]);
		return intArr;
	}

	protected int[] getIntegerArrOrNullFromProps(Properties prop, String str) {
		String s = prop.getProperty(str);
		if (s==null) return null;
		String[] result = s.split("\\s");	// split the string in tokens separated by whitespace
		int[] intArr = new int[result.length];
		for (int i=0; i<result.length; i++)
			intArr[i] = Integer.parseInt(result[i]);
		return intArr;
	}

	protected double[] getDoubleArrFromProps(Properties prop, String str) {
		String s = prop.getProperty(str);
		if (s==null) throw new RuntimeException("Property "+str+" not found in prop");
		String[] result = s.split("\\s");	// split the string in tokens separated by whitespace
		double[] doubleArr = new double[result.length];
		for (int i=0; i<result.length; i++)
			doubleArr[i] = Double.parseDouble(result[i]);
		return doubleArr;
	}

}
