package games.Othello;

import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.PlayAgent;
import controllers.PlayAgtVector;
import games.GameBoard;
import games.Othello.Edax.Edax2;
import games.StateObservation;
import games.XArenaFuncs;
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
//		
//		System.out.flush();
//		int move = 26;
//		
//		/**
//		 * The First test with my test.exe to test timing behaviours and timeouts
//		 */
////		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "Test.exe");
////		System.out.println(cli.sendAndAwait("mode 1"));
////		System.out.println(cli.sendAndAwait("10000"));
////		System.out.println(cli.sendAndAwait("f"));
////		cli.stop();
//
//		/**
//		 * The second test with edax.exe, to test the first three moves
//		 */
////		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
//////		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe");
////		System.out.println(cli.sendAndAwait("mode 1"));
////		System.err.println("-----");
////		System.out.println(cli.sendAndAwait("f6"));
////		System.err.println("-----");
////		System.out.println(cli.sendAndAwait("f4"));
////		System.err.println("-----");
////		System.out.println(cli.sendAndAwait("c5"));
////		System.err.println("-----");
////		System.out.println(cli.sendAndAwait("d7"));
////		System.err.println("-----");
////		System.out.println(cli.sendAndAwait("b3"));
//////		System.out.println(cli.sendAndAwait("d6"));
//////		System.out.println(cli.sendAndAwait("d6"));
////		cli.stop();
//		
//		CommandLineInteractor cli = new CommandLineInteractor("", "");
//		System.out.println(cli.sendAndAwait("ipconfig"));
//		System.out.println("...........");
//		System.out.println(cli.sendAndAwait("help"));
//		System.out.println("...........");
//		System.out.println(cli.sendAndAwait("dir"));
//		System.out.println("...........");
//		System.out.println(cli.sendAndAwait("ipconfig"));
	}

	@Test
	public void quickOthelloEvalTest() {
		long startTime = System.currentTimeMillis();
		String[] agtFileA = {"TCL4-100_7_250k-lam05_P4_nPly2-FAm_A.agt.zip"};

		quickOthelloEval(agtFileA);

		double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		System.out.println("quickEvalTest finished in "+elapsedTime+" sec.");
	}

	/**
	 * Make a quick Othello evaluation of a vector af agents stored in {@code agtFile} against Edax. Both
	 * agents play one episode in each role.
	 * <p>
	 * The agents are wrapped by {@link MCTSWrapperAgent} with {@code iter} iterations and {@code c_puct} as
	 * specified in source code. <br>
	 * Edax parametrised as specified in source code.
	 *
	 * @param agtFile       vector of agent files
	 */
	public void quickOthelloEval(String[] agtFile) {
		String selectedGame = "Othello";
		PlayAgent pa,qa,edaxAgent;
		ArrayList<MCompeteMWrap> mcList= new ArrayList<>();

		String[] scaPar = SetupGBG.setDefaultScaPars(selectedGame);
		arenaTrain = SetupGBG.setupSelectedGame(selectedGame,scaPar,"",false,true);
		GameBoard gb = arenaTrain.makeGameBoard();		// needed for chooseStartState()
		double winrate;
		long startTime;
		double deltaTime;
		double elapsedTime = 0.0;


		for (int k=0; k< agtFile.length; k++) {
			setupPaths(agtFile[k],csvFile);     // builds filePath

			boolean res = arenaTrain.loadAgent(0, filePath);
			assert res : "\n[TestMain] Aborted: agtFile = "+ agtFile[k] + " not found!";

			String sAgent = arenaTrain.m_xab.getSelectedAgent(0);
			pa = arenaTrain.m_xfun.fetchAgent(0,sAgent, arenaTrain.m_xab);
			int edaxDepth = 5;
			ParEdax edaxPar = new ParEdax();
			edaxPar.setDepth(edaxDepth);
			edaxAgent = new Edax2("Edax",edaxPar);
			int iter=1000;
			double c_puct=1.0;
			if (iter == 0) qa = pa;
			else qa = new MCTSWrapperAgent(iter, c_puct,
					new PlayAgentApproximator(pa),
					"MCTS-wrapped " + pa.getName(),
					-1, new ParOther());

			PlayAgtVector paVector = new PlayAgtVector(qa, edaxAgent);
			MCompeteMWrap mCompete;
			for (int p_MWrap : new int[]{0, 1}) {     // p_MWrap: whether agent qa is player 0 or player 1
				startTime = System.currentTimeMillis();
				StateObservation so = gb.getDefaultStartState();
				ScoreTuple sc;
				sc = XArenaFuncs.competeNPlayer(paVector.shift(p_MWrap), so, 1, 0, null);
				winrate = (sc.scTup[p_MWrap] + 1) / 2;
				deltaTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
				mCompete = new MCompeteMWrap(0, agtFile[k], 1, edaxDepth, iter,
						1e-8, p_MWrap, c_puct, winrate,
						deltaTime, 0.0);
				System.out.println("EPS=" + 1e-8 + ", iter=" + iter + ", dEdax=" + edaxDepth + ", p=" + p_MWrap + ", winrate=" + winrate);
				mcList.add(mCompete);
				elapsedTime += deltaTime;
			} // for (p_MWrap)


			System.out.println("[quickOthelloEval,"+agtFile[k]+"] all done.");
		} // for (k)
	}


}
