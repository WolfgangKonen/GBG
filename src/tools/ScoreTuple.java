package tools;

import java.io.Serializable;
import java.text.DecimalFormat;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;
import controllers.PlayAgent;
import games.LogManager;
import games.StateObservation;
import games.Nim.BoutonAgent;

/**
 *	ScoreTuple: a tuple with scores (game values) for all players.
 *  <p>
 *  ScoreTuple implements the {@link Serializable} interface, because this is needed for {@link LogManager}.
 * 
 *	@see MaxNAgent
 *	@see BoutonAgent
 */
public class ScoreTuple implements Serializable {
	public enum CombineOP {AVG,MIN,MAX,DIFF,SUM};
	/**
	 * the tuple values
	 */
	public double[] scTup;
	/**
	 * for CombineOP=MIN (MAX): how many of the tuples that were combined into  {@code this} have
	 * the same minValue (maxValue) in component playNum.
	 * This is needed in {@link MaxNAgent} and {@link ExpectimaxNAgent} to break ties.
	 */
	public int count = 1;
	//private double minValue = Double.MAX_VALUE;
	//private double maxValue = -Double.MAX_VALUE;
	
	/**
	 * @param N number of players
	 */
	public ScoreTuple(int N) {
		this.scTup = new double[N];
	}
	public ScoreTuple(StateObservation sob) {
		this.scTup = new double[sob.getNumPlayers()];
	}
	public ScoreTuple(StateObservation sob,boolean lowest) {
		this.scTup = new double[sob.getNumPlayers()];
		for (int i=0; i<scTup.length; i++) scTup[i]=-Double.MAX_VALUE;
	}
	public ScoreTuple(double [] res) {
		this.scTup = res.clone();
	}
	public ScoreTuple(ScoreTuple other) {
		this.scTup = other.scTup.clone();
	}

	/**
	 * Build a new score tuple from a state and maxScore for the player to move.
	 * Used by various getNextAction2 methods.
	 * @param so
	 * @param maxScore
	 */
	public ScoreTuple(StateObservation so, double maxScore) {
		this.scTup = so.getStoredBestScoreTuple().scTup.clone();
		// This is the previous tuple, only relevant in case N>=3. If so.getStoredBestScoreTuple() encounters
		// null pointers, it returns an all-zeros-tuple with length so.getNumPlayers().

		this.scTup[so.getPlayer()] = maxScore;
		if (so.getNumPlayers()==2) {			// the following holds for 2-player, zero-sum games:
			int opponent = 1-so.getPlayer();
			this.scTup[opponent] = -maxScore;
		}
	}

	public ScoreTuple copy() { return new ScoreTuple(this); }
	
	public double max() {
		double f = -Double.MAX_VALUE;
		for (int i=0; i<scTup.length; i++) f = (scTup[i]>f) ? scTup[i] : f;
		return f;
	}

	public int argmax() {
		double f = -Double.MAX_VALUE;
		int ind=0;
		for (int i=0; i<scTup.length; i++) 
			if (scTup[i]>f) {
				f = scTup[i];
				ind = i;
			}
		return ind;
	}

	public boolean equals(ScoreTuple other) {
		for (int i=0; i<scTup.length; i++)
			if (this.scTup[i] != other.scTup[i]) return false;
		return true;
	}

	public ScoreTuple shift(int k) {
		ScoreTuple shiftedTuple = new ScoreTuple(this);
		for (int i=0; i<scTup.length; i++) shiftedTuple.scTup[(i+k)%scTup.length] = scTup[i];
		return shiftedTuple;    		
	}
	
	public String toString() {
		String cs = "(";
		//double f = StateObserver2048.MAXSCORE;		// only temporarily
		double f = 1.0;
		for (int i=0; i<scTup.length-1; i++) cs = cs + scTup[i]*f + ", ";
		cs = cs + scTup[scTup.length-1]*f + ")";
		return(cs);
	}
	
	public String toStringFrm() {
		DecimalFormat frm = new DecimalFormat("#0.000");
		String cs = "(";
		for (int i=0; i<scTup.length-1; i++) cs = cs + frm.format(scTup[i]) + ", ";
		cs = cs + frm.format(scTup[scTup.length-1]) + ")";
		return(cs);
	}

	public String printEpisodeWinner(int k, StateObservation so) {
		String sMsg;
		String[] player2Names = {"X","O"};
		DecimalFormat frm = new DecimalFormat("###000");
		
		int winner = this.argmax();
		if (this.max()==0.0) winner = -2;	// tie indicator
		
		switch (winner) {
		case (-2):
			sMsg = k+": Tie";
			break;
		default:
			sMsg = switch (scTup.length) {
				case (1) -> k + ": score = " + frm.format(so.getGameScoreRaw(0));
				case (2) -> k + ": " + player2Names[winner] + " wins";
				default -> k + ": P" + winner + " wins";
			};
		} // switch(winner)
		
		return sMsg;
	}
	
	/**
	 * Combine {@code this} {@link ScoreTuple} with the information in the other {@link ScoreTuple}  
	 * {@code tuple2nd}. The combination is done according to operator {@code cOP}:
	 * <ul>
	 * <li> <b>AVG</b>: weighted average or expectation value with probability weight 
	 * 		{@code currProbab}. The probability weights of all combined tuples should sum
	 *   	up to 1.
	 * <li> <b>MIN</b>: combine by retaining this {@link ScoreTuple}, which has the
	 * 		minimal value in {@code scTup[playNum]}, the score for player {@code playNum}
	 * <li> <b>MAX</b>: combine by retaining this {@link ScoreTuple}, which has the
	 * 		maximal value in {@code scTup[playNum]}, the score for player {@code playNum}
	 * <li> <b>DIFF</b>: subtract from {@code this} all values in the other {@link ScoreTuple}  
	 * 		{@code tuple2nd}.
	 * <li> <b>SUM</b>: add to {@code this} all values in the other {@link ScoreTuple}
	 * 		{@code tuple2nd}.
	 * </ul>
	 * The resulting information is returned in {@code this} {@link ScoreTuple}. 
	 * 
	 * @param tuple2nd 		the new {@link ScoreTuple} 
	 * @param cOP			combine operator 	
	 * @param playNum		player number (needed only for {@code cOP}==MIN,MAX)
	 * @param currProbab	probability weight of {@code tuple2nd} (needed only for {@code cOP}==AVG)
	 */
	public void combine(ScoreTuple tuple2nd, ScoreTuple.CombineOP cOP, int playNum, double currProbab)
	{
		switch(cOP) {
		case AVG: 
			// form a weighted average of all combined tuples where the weight of tuple2nd is currProbab:
    		for (int i=0; i<scTup.length; i++) scTup[i] += currProbab*tuple2nd.scTup[i];
    		break;
		case MIN:
			if (tuple2nd.scTup[playNum]<this.scTup[playNum]) {
				//minValue = tuple2nd.scTup[playNum];
				this.scTup = tuple2nd.scTup.clone();
				count=1;
			}  else if (tuple2nd.scTup[playNum]==this.scTup[playNum]) {
				count++;
			}  	
			break;
		case MAX:
			if (tuple2nd.scTup[playNum]>this.scTup[playNum]) {
				//maxValue = tuple2nd.scTup[playNum];
				this.scTup = tuple2nd.scTup.clone();
				count=1;
			}  else if (tuple2nd.scTup[playNum]==this.scTup[playNum]) {
				count++;
			}
			break;
		case DIFF: 
    		for (int i=0; i<scTup.length; i++) scTup[i] = scTup[i] - tuple2nd.scTup[i];
    		break;
		case SUM:
			for (int i=0; i<scTup.length; i++) scTup[i] = scTup[i] + tuple2nd.scTup[i];
			break;
		}
	}
} // ScoreTuple