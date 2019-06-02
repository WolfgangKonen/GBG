package tools;

import java.io.IOException;

import controllers.PlayAgent;
import controllers.RandomAgent;
import games.Arena;
import games.StateObservation;
import games.Othello.ArenaOthello;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import tools.Types.ACTIONS;
import tools.Types.ScoreTuple;

/**
 * Class {@link ValidateAgent} performs certain consistency checks of a state observer class. These checks 
 * test whether certain methods run through successfully, produce the expected results and so on.
 * <p>
 * Run these checks by constructing in {@link #main(String[])} a certain {@link Arena} {@code ar} object, 
 * a {@link StateObservation} {@code sob} object  and a {@link PlayAgent} {@code p} object of the agent 
 * class you want to validate. Then perform the checks by calling 
 * {@link #ValidateAgent(PlayAgent, StateObservation)}.
 */
public class ValidateStateObserver {

	public ValidateStateObserver(StateObservation sob, PlayAgent pa) {
		ACTIONS a;
		ScoreTuple sc;
		StateObservation newSob = sob.copy();
		boolean random=true;
		boolean silent=true;
		while(!newSob.isGameOver()) {
			sc = newSob.getGameScoreTuple();
			checkScoreTuple(sc,newSob);
			a = pa.getNextAction2(newSob, random, silent);
			newSob.advance(a);
		}
		sc = newSob.getGameScoreTuple();
		checkScoreTuple(sc,newSob);
		System.out.println("getScoreTuple check ... OK");
	}

	private boolean checkScoreTuple(ScoreTuple sc, StateObservation sob) {
		double scMin = sob.getMinGameScore();
		double scMax = sob.getMaxGameScore();
		System.out.print(sc);
		System.out.println("     "+scMin+"-->"+ scMax);
		for (int i=0; i<sc.scTup.length; i++) {
			assert !Double.isNaN(sc.scTup[i]);
			assert Double.isFinite(sc.scTup[i]);		
			assert (scMin <= sc.scTup[i]) : "ScoreTuple < getMinScore() : "+sc.scTup[i]+" < "+scMin;
			assert (sc.scTup[i] <= scMax) : "ScoreTuple > getMaxScore() : "+sc.scTup[i]+" > "+scMax;
		}
		return true;
	}
	
	public static void main(String[] args) {
		Arena ar = new ArenaOthello();
		PlayAgent p;
//		p = new BenchMarkPlayer("bp",1);
//		p = new RandomAgent("bp",1);
		try {
			p = ar.m_xfun.constructAgent(0, "TD-Ntuple-3", ar.m_xab);
		} catch (IOException e1) {
			e1.printStackTrace();
			p=null;			
		}
		
		//
		// choose a state observer to validate
		//
		StateObservation sob = ar.getGameBoard().getDefaultStartState();
		
		//
		// start validation
		//
		if (p==null) {
			System.out.println("PlayAgent p is null!");
		} else {
			ValidateStateObserver vs = new ValidateStateObserver(sob, p);
			
			System.out.println("ValidateStateObserver finished successfully");
		}
	}

}
