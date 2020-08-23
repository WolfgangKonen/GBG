package tools;

import java.io.IOException;
import java.util.ArrayList;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.BoardVector;
import games.StateObsWithBoardVector;
import games.XNTupleFuncs;
import games.StateObservation;
import games.Othello.ArenaTrainOthello;
import games.TicTacToe.ArenaTrainTTT;
import tools.Types.ACTIONS;
import util.action.Action;

/**
 * Class {@link ValidateAgent} performs certain consistency checks of an agent class. These checks 
 * test whether certain methods run through successfully, produce the expected results and so on.
 * <p>
 * Run these checks by constructing in {@link #main(String[])} a certain {@link Arena} {@code ar} object, 
 * a {@link StateObservation} {@code sob} object  and a {@link PlayAgent} {@code p} object of the agent 
 * class you want to validate. Then perform the checks by calling 
 * {@link #runTests(PlayAgent, StateObservation, Arena)}.
 * <p>
 * IMPORTANT: Run {@link #main(String[])} with VM argument {@code -ea} (enable assertions).
 *
 * This class is now DEPRECATED, use ValidateAgentTest and ValidateAgentOthelloTest
 */
@Deprecated
public class ValidateAgent {

    private static String gbgAgentPath = "C:\\Users\\wolfgang\\Documents\\GitHub\\GBG\\agents\\Othello\\TCL3-100_7_250k-lam05_P4_nPly2-FAm.agt.zip";

    public ValidateAgent() {};
	
	public boolean runTests(PlayAgent pa, StateObservation sob, Arena ar) {
		//
		// Check if pa.getScoreTuple and pa.estimateGameValueTuple run correctly through and return valid
		// numbers. Check this with different StateObservation objects so that there is at least one 
		// state for every player. (Of course this test is not exhaustive.)
		//		
		boolean verbose=true;
		ScoreTuple sc;
		sc = pa.getScoreTuple(sob, null);
		checkScoreTuple(sc,sob,verbose);
		sc = pa.estimateGameValueTuple(sob, null);
		checkScoreTuple(sc,sob,verbose);
		for (int i=1; i<sob.getNumPlayers(); i++) {
			ACTIONS a = sob.getAvailableActions().get(0);
			sob.advance(a);
			sc = pa.getScoreTuple(sob, null);
			checkScoreTuple(sc,sob,verbose);
			sc = pa.estimateGameValueTuple(sob, null);
			checkScoreTuple(sc,sob,verbose);
		}
		System.out.println("getScoreTuple check ... OK");
		
		// 
		// Check if the final score tuple (which usually contains non-zero rewards) has a sum of zero 
		// for 2-player games, i.e. 2 opposite entries:
		//
		while (!sob.isGameOver()) {
			ArrayList<ACTIONS> arr = sob.getAvailableActions();
			ACTIONS a = pa.getNextAction2(sob, false, true);
			sob.advance(a);
		}
		sc = pa.getScoreTuple(sob, null);
		checkScoreTuple(sc,sob,verbose);
		sc = sob.getGameScoreTuple();
		checkScoreTuple(sc,sob,verbose);
		System.out.println("final getScoreTuple check ... OK");
		
		//
		// check if a training episode runs through successfully 
		//
		System.out.print("train pa for one episode ... ");
		int num = pa.getGameNum();
		sob = ar.getGameBoard().getDefaultStartState();
		pa.trainAgent(sob);
		assert (pa.getGameNum()-num == 1) : "Game counter not correctly incremented!";
		int dummy = 1;
		System.out.println("OK");
		
		// 
		// construct a board vector bv where each element is different and check that each board 
		// vector returned by  xnf.symmetryVectors(bv) is different from all the others.
		// 
		XNTupleFuncs xnf = ar.makeXNTupleFuncs();
		BoardVector bv = xnf.makeBoardVectorEachCellDifferent();
		StateObsWithBoardVector curSOWB = new StateObsWithBoardVector(sob,bv);
		BoardVector[] sym = xnf.symmetryVectors(curSOWB,0);
		boolean testPassed=true;
		for (int i=0; i<sym.length; i++) {
			if (assertEachCellDifferent(sym[i])==false) {
				System.out.println("Error: some cell numbers in state sym["+i+"] appear not exactly once");
				testPassed=false;				
			}
			for (int j=i+1; j<sym.length; j++) {
				if (assertBvDifferent(sym[i],sym[j])==false) {
					System.out.println("Error: symmetry states identical: "+i+", "+j);
					testPassed=false;
				}
			}
		}
		if (testPassed) {
			System.out.println("symmetryVectors check ... OK");
		} else {
			throw new RuntimeException("symmetryVectors check ... FAILED");
		}
		
		return true;
	}
	
	private boolean assertEachCellDifferent(BoardVector bv1) {
		BoardVector bcount = new BoardVector(new int[bv1.bvec.length]);
		for (int i=0; i<bv1.bvec.length; i++) bcount.bvec[bv1.bvec[i]]++; 
		for (int i=0; i<bcount.bvec.length; i++) 
			if (bcount.bvec[i]!=1) return false;
		
		return true;
	}
	
	private boolean assertBvDifferent(BoardVector bv1, BoardVector bv2) {
		for (int i=0; i<bv1.bvec.length; i++) 
			if (bv1.bvec[i]!=bv2.bvec[i]) return true;
		
		return false;
	}

	/**
	 * Check that {@code sc}'s values are valid, finite numbers, lie between min and max score
	 * and that they sum to 0.0.
	 * 
	 * @param sc
	 * @param sob
	 * @param verbose
	 * @return
	 */
	private boolean checkScoreTuple(ScoreTuple sc, StateObservation sob, boolean verbose) {
		double scMin = sob.getMinGameScore();
		double scMax = sob.getMaxGameScore();
		double scSum = 0.0;
		if (verbose) {
			System.out.print(sc);
			System.out.println("     "+scMin+"-->"+ scMax);
		}
		System.out.println(sob + " : " + sob.isGameOver());
		for (int i=0; i<sc.scTup.length; i++) {
			scSum += sc.scTup[i];
			assert !Double.isNaN(sc.scTup[i]);
			assert Double.isFinite(sc.scTup[i]);	
			if (!sob.isGameOver()) {	// there are cases (TDNT3+Othello) where a game-over state produces sc with values outside interval [scMin,scMax] --> we exclude such cases from the test 
				assert (scMin <= sc.scTup[i]) : "ScoreTuple < getMinScore() : "+sc.scTup[i]+" < "+scMin +" for StateObservation "+sob;
				assert (sc.scTup[i] <= scMax) : "ScoreTuple > getMaxScore() : "+sc.scTup[i]+" > "+scMax +" for StateObservation "+sob;
			}
		}
		assert (Math.abs(scSum) < 1e-20) : "ScoreTuple does not sum to 0: "+scSum;
		return true;
	}
	
	private static PlayAgent constructTDNTuple3Agt(Arena ar) {
		PlayAgent p;
		try {
			p = ar.m_xfun.constructAgent(0, "TD-Ntuple-3", ar.m_xab);
		} catch (IOException e1) {
			e1.printStackTrace();
			p=null;			
		} 
		return p;
	}
	
	public static void main(String[] args) {
		//
		// choose an Arena
		//
		Arena ar = new ArenaTrainOthello("",true);
//		Arena ar = new ArenaTrainTTT("",true);
		
		//
		// choose an agent to validate - select one of the following options for constructing pa:
		//
		PlayAgent pa = null;
		try {									// try-catch is for loadGBGAgent which may throw exceptions
//			pa = new BenchMarkPlayer("bp",1);
//			pa = constructTDNTuple3Agt(ar);
			pa = ar.tdAgentIO.loadGBGAgent(gbgAgentPath);	// check if a TDNT3-agent reloaded from disk passes all test
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//
		// start validation
		//
		if (pa==null) {
			System.out.println("PlayAgent p is null!");
		} else {
			StateObservation sob = ar.getGameBoard().getDefaultStartState();
			ValidateAgent va = new ValidateAgent();
			try {
				va.runTests(pa,sob, ar);				
				System.out.println("ValidateAgent finished successfully");
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("ValidateAgent finished with errors");
			} catch (AssertionError e2) {
				e2.printStackTrace();
				System.out.println("ValidateAgent finished with errors");
			}
			
		}
		ar.destroy();
	}

}

