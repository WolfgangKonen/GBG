package games;

import java.io.IOException;

import javax.swing.JOptionPane;

import controllers.PlayAgent;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Othello.ArenaOthello;
import games.Othello.GameBoardOthello;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import params.ParNT;
import params.ParOther;
import params.ParTD;
import tools.MessageBox;
import tools.Types.ACTIONS;
import tools.Types.ScoreTuple;

public class ValidateAgent {

	public ValidateAgent() {
		// TODO Auto-generated constructor stub
	}
	
	public ValidateAgent(PlayAgent pa, StateObservation sob) {
		//
		// Check if getScoreTuple and estimateGameValueTuple are correctly implemented and return valid numbers.
		// Check with different StateObservation objects so that there is at least one state for 
		// every player.
		//
		ScoreTuple sc;
		sc = pa.getScoreTuple(sob);
		checkScoreTuple(sc);
		sc = pa.estimateGameValueTuple(sob);
		checkScoreTuple(sc);
		for (int i=1; i<sob.getNumPlayers(); i++) {
			ACTIONS a = sob.getAllAvailableActions().get(0);
			sob.advance(a);
			sc = pa.getScoreTuple(sob);
			checkScoreTuple(sc);
			sc = pa.estimateGameValueTuple(sob);
			checkScoreTuple(sc);
		}
		
		// check if a training episode runs through successfully 
		pa.trainAgent(sob);
		int dummy = 1;
	}

	private boolean checkScoreTuple(ScoreTuple sc) {
		for (int i=0; i<sc.scTup.length; i++) {
			assert !Double.isNaN(sc.scTup[i]);
			assert Double.isFinite(sc.scTup[i]);			
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
