package games.Othello;

import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import games.GameBoard;
import games.Othello.Edax.CommandLineInteractor;
import games.Othello.Edax.Edax2;
import games.StateObservation;
import games.XArenaFuncs;
import games.XStateObs;
import org.junit.Test;
import params.ParEdax;
import params.ParOther;
import starters.GBGBatch;
import starters.MCompeteMWrap;
import starters.SetupGBG;
import tools.ScoreTuple;

import java.util.ArrayList;

public class TestMain extends GBGBatch {
	String csvFile = "test.csv";

	public static void main(String[] args)
	{		
		int z =0 ;
		for(int i = 0; i < 64;i++) {
			
				z += Math.abs(ConfigOthello.BENCHMARKPLAYERMAPPING[1][i]);
			
		}
		System.out.println(z);

		check_edax_interface();

	}

	@Test
	public void quickOthelloEvalTest() {
		long startTime = System.currentTimeMillis();
		String[] agtFileA = {"TCL4-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip"};
		ArrayList<MCompeteMWrap> mcList= new ArrayList<>();

		int iter=1000;
		int edaxDepth = 5;
		quickOthelloEval(agtFileA, iter, edaxDepth, mcList);

		double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("quickEvalTest finished in "+elapsedTime+" sec.");
	}

	@Test
	public void sweepOthelloEvalTest() {
		long startTime = System.currentTimeMillis();
		int iter=1000;
		String[] agtFileA = {"TCL4-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip"};
		String mCsvFile = "test_EWN_branch_i"+iter+".csv";
		String userTitle1 = "nMWrap", userTitle2 = "nEdax2";	// number of pieces MWrap and Edax in final state
		ArrayList<MCompeteMWrap> mcList= new ArrayList<>();

		for (int edaxDepth = 0; edaxDepth<9; edaxDepth++) {
			quickOthelloEval(agtFileA, iter, edaxDepth, mcList);
		}

		String sAgent = arenaTrain.m_xab.getSelectedAgent(0);
		PlayAgent pa = arenaTrain.m_xfun.fetchAgent(0,sAgent, arenaTrain.m_xab);
		MCompeteMWrap.printMultiCompeteList(mCsvFile, mcList, pa, arenaTrain, userTitle1, userTitle2);
		System.out.println("Results written to "+mCsvFile);

		MCompeteMWrap.printMultiCompeteList(mCsvFile, mcList, pa, arenaTrain, userTitle1, userTitle2);

		double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("sweepEvalTest finished in "+elapsedTime+" sec.");
	}

	/**
	 * Make a quick Othello evaluation of a vector af agents stored in {@code agtFile} against Edax. Both
	 * agents play one episode in each role.
	 * <p>
	 * The agents are wrapped by {@link MCTSWrapperAgent} with {@code iter} iterations (and {@code c_puct} as
	 * specified in source code). <br>
	 * Edax is parametrised with {@code edaxDepth}.
	 *
	 * @param agtFile       vector of agent files
	 * @param iter			iterations of {@link MCTSWrapperAgent}
	 * @param edaxDepth		depth for Edax
	 * @param mcList		list with partial MCompeteMWrap results, a new one will be added
	 */
	public void quickOthelloEval(String[] agtFile, int iter, int edaxDepth, ArrayList<MCompeteMWrap> mcList) {
		String selectedGame = "Othello";
		PlayAgent pa,qa,edaxAgent;

		String[] scaPar = SetupGBG.setDefaultScaPars(selectedGame);
		arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
		GameBoard gb = arenaTrain.makeGameBoard();		// needed for chooseStartState()
		double winrate;
		long startTime;
		double deltaTime;
		double elapsedTime = 0.0;


		for (String filename : agtFile) {
			setupPaths(filename, csvFile);     // builds filePath

			boolean res = arenaTrain.loadAgent(0, filePath);
			assert res : "\n[TestMain] Aborted: agtFile = " + filename + " not found!";

			String sAgent = arenaTrain.m_xab.getSelectedAgent(0);
			pa = arenaTrain.m_xfun.fetchAgent(0, sAgent, arenaTrain.m_xab);
			ParEdax edaxPar = new ParEdax();
			edaxPar.setDepth(edaxDepth);
			edaxAgent = new Edax2("Edax", edaxPar);
			double c_puct = 1.0;
			if (iter == 0) qa = pa;
			else qa = new MCTSWrapperAgent(iter, c_puct,
					new PlayAgentApproximator(pa),
					"MCTS-wrapped " + pa.getName(),
					-1, new ParOther());

			PlayAgtVector paVector = new PlayAgtVector(qa, edaxAgent);
			MCompeteMWrap mCompete;
			for (int p_MWrap : new int[]{0, 1}) {     // p_MWrap: whether agent qa is player 0 or player 1
				ArrayList<XStateObs> finalSobList = new ArrayList<>();
				startTime = System.currentTimeMillis();
				StateObservation so = gb.getDefaultStartState(null);
				ScoreTuple sc;
				sc = XArenaFuncs.competeNPlayer(paVector, p_MWrap, so, 1, 0, null, finalSobList, null, false);
				winrate = (sc.scTup[p_MWrap] + 1) / 2;
				deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
				StateObserverOthello soO = (StateObserverOthello) finalSobList.get(0).getFinalState();
				mCompete = new MCompeteMWrap(0, filename, 1, edaxDepth, iter,
						1e-8, p_MWrap, c_puct, winrate,
						(p_MWrap==0) ? soO.getCountBlack() :soO.getCountWhite(),
						(p_MWrap==0) ? soO.getCountWhite() :soO.getCountBlack());
				System.out.println("EPS=" + 1e-8 + ", iter=" + iter + ", dEdax=" + edaxDepth + ", p=" + p_MWrap + ", winrate=" + winrate);
				System.out.println("# Black = "+ soO.getCountBlack()+", # White = "+ soO.getCountWhite());

				mcList.add(mCompete);
				elapsedTime += deltaTime;
			} // for (p_MWrap)

			System.out.println("[quickOthelloEval," + filename + "] all done (elapsed time = "+elapsedTime+" sec).");
		} // for (filename)
	}

	private static void check_edax_interface() {

		System.out.flush();

		/*
		 * The first test with edax.exe to test timing behaviours and timeouts
		 */
		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "Test.exe");
		System.out.println(cli.sendAndAwait("mode 1"));
		System.out.println(cli.sendAndAwait("10000"));
		System.out.println(cli.sendAndAwait("f"));
		cli.stop();

		/*
		 * The second test with edax.exe, to test the first three moves
		 */
		cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
//		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe");
		System.out.println(cli.sendAndAwait("mode 1"));
		System.err.println("-----");
		System.out.println(cli.sendAndAwait("f6"));
		System.err.println("-----");
		System.out.println(cli.sendAndAwait("f4"));
		System.err.println("-----");
		System.out.println(cli.sendAndAwait("c5"));
		System.err.println("-----");
		System.out.println(cli.sendAndAwait("d7"));
		System.err.println("-----");
		System.out.println(cli.sendAndAwait("b3"));
//		System.out.println(cli.sendAndAwait("d6"));
//		System.out.println(cli.sendAndAwait("d6"));
		cli.stop();

		/*
		 * The third test with edax.exe, to test certain commands
		 */
		cli = new CommandLineInteractor("", "");
		System.out.println(cli.sendAndAwait("ipconfig"));
		System.out.println("...........");
		System.out.println(cli.sendAndAwait("help"));
		System.out.println("...........");
		System.out.println(cli.sendAndAwait("dir"));
		System.out.println("...........");
		System.out.println(cli.sendAndAwait("ipconfig"));
	}
}
