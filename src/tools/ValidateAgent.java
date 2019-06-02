package tools;

import java.io.IOException;

import javax.swing.JOptionPane;

import controllers.PlayAgent;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.XArenaFuncs;
import games.StateObservation;
import games.Othello.ArenaOthello;
import games.Othello.GameBoardOthello;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.Types.ACTIONS;
import tools.Types.ScoreTuple;

/**
 * Class {@link ValidateAgent} performs certain consistency checks of an agent class. These checks 
 * test whether certain methods run through successfully, produce the expected results and so on.
 * <p>
 * Run these checks by constructing in {@link #main(String[])} a certain {@link Arena} {@code ar} object, 
 * a {@link StateObservation} {@code sob} object  and a {@link PlayAgent} {@code p} object of the agent 
 * class you want to validate. Then perform the checks by calling 
 * {@link #ValidateAgent(PlayAgent, StateObservation)}.
 */
public class ValidateAgent {

	public ValidateAgent(PlayAgent pa, StateObservation sob) {
		//
		// Check if getScoreTuple and estimateGameValueTuple are correctly implemented and return valid numbers.
		// Check with different StateObservation objects so that there is at least one state for 
		// every player. (Of course this test is not exhaustive.)
		//		
		ScoreTuple sc;
		sc = pa.getScoreTuple(sob);
		checkScoreTuple(sc,sob);
		sc = pa.estimateGameValueTuple(sob);
		checkScoreTuple(sc,sob);
		for (int i=1; i<sob.getNumPlayers(); i++) {
			ACTIONS a = sob.getAllAvailableActions().get(0);
			sob.advance(a);
			sc = pa.getScoreTuple(sob);
			checkScoreTuple(sc,sob);
			sc = pa.estimateGameValueTuple(sob);
			checkScoreTuple(sc,sob);
		}
		System.out.println("getScoreTuple check ... OK");
		
		// check if a training episode runs through successfully 
		System.out.print("Starting pa.trainAgent ... ");
		int num = pa.getGameNum();
		pa.trainAgent(sob);
		assert (pa.getGameNum()-num == 1) : "Game counter not correctly incremented!";
		int dummy = 1;
		System.out.println("OK");
	}

	private boolean checkScoreTuple(ScoreTuple sc, StateObservation sob) {
		double scMin = sob.getMinGameScore();
		double scMax = sob.getMaxGameScore();
		System.out.print(sc);
		System.out.println("     "+scMin+"-->"+ scMax);
		for (int i=0; i<sc.scTup.length; i++) {
			assert !Double.isNaN(sc.scTup[i]);
			assert Double.isFinite(sc.scTup[i]);		
			assert (scMin <= sc.scTup[i]);
			assert (sc.scTup[i] <= scMax) : "ScoreTuple > getMaxScore() : "+sc.scTup[i]+" > "+scMax;
		}
		return true;
	}
	
	public static void main(String[] args) {
		Arena ar = new ArenaOthello();
//		GameBoard gb = new GameBoardOthello();		// this gives null pointer exception
		
		//
		// choose an agent to validate
		//
		PlayAgent p;
//		p = new BenchMarkPlayer("bp",1);
		try {
			p = ar.m_xfun.constructAgent(0, "TD-Ntuple-3", ar.m_xab);
		} catch (IOException e1) {
			e1.printStackTrace();
			p=null;			
		}
		
		//
		// start validation
		//
		if (p==null) {
			System.out.println("PlayAgent p is null!");
		} else {
			StateObservation sob = ar.getGameBoard().getDefaultStartState();
			ValidateAgent va = new ValidateAgent(p,sob);
			
			System.out.println("ValidateAgent finished successfully");
		}

	}

}
