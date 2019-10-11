package tools;

import java.text.DecimalFormat;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;
import games.StateObservation;
import games.Nim.BoutonAgent;

/**
 *	ScoreTuple: a tuple with scores (game values) for all players.
 * 
 *	@see MaxNAgent
 *	@see BoutonAgent
 */
public class ScoreTuple {
	public enum CombineOP {AVG,MIN,MAX,DIFF};
	/**
	 * the tuple values
	 */
	public double[] scTup;
	/**
	 * for CombineOP=MIN (MAX): how many of the tuple combined have minValue (maxValue). 
	 * This is needed in {@link MaxNAgent} and {@link ExpectimaxNAgent} to break ties.
	 */
	public int count;
	private double minValue = Double.MAX_VALUE;
	private double maxValue = -Double.MAX_VALUE;
	
	/**
	 * @param N number of players
	 */
	public ScoreTuple(int N) {
		this.scTup = new double[N];
	}
	public ScoreTuple(StateObservation sob) {
		this.scTup = new double[sob.getNumPlayers()];
	}
	public ScoreTuple(double [] res) {
		this.scTup = res.clone();
	}
	public ScoreTuple(ScoreTuple other) {
		this.scTup = other.scTup.clone();
	}
	
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

	public String printEpisodeWinner(int k) {
		String sMsg;
		String[] player2Names = {"X","O"};
		
		int winner = this.argmax();
		if (this.max()==0.0) winner = -2;	// tie indicator
		
		switch (winner) {
		case (-2):
			sMsg = k+": Tie";
			break;
		default: 
			switch(scTup.length) {
			case (2):
				sMsg = k+": "+player2Names[winner]+" wins";
				break;
			default: 
				sMsg = k+": P"+winner+" wins";
				break;
			}
		} // switch(winner)
		
		return sMsg;
	}
	
	/**
	 * Combine {@code this} {@link ScoreTuple} with the information in the other {@link ScoreTuple}  
	 * {@code tuple2nd}. Combine according to operator {@code cOP}:
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
	 * </ul>
	 * 
	 * @param tuple2nd 		the new {@link ScoreTuple} 
	 * @param cOP			combine operator 	
	 * @param playNum		player number (needed only for {@code cOP}==MIN,MAX)
	 * @param currProbab	probability weight of {@code tuple2nd} (needed for {@code cOP}==AVG)
	 */
	public void combine(ScoreTuple tuple2nd, ScoreTuple.CombineOP cOP, int playNum, double currProbab)
	{
		switch(cOP) {
		case AVG: 
			// form a weighted average of all combined tuples where the weight of tuple2nd is currProbab:
    		for (int i=0; i<scTup.length; i++) scTup[i] += currProbab*tuple2nd.scTup[i];
    		break;
		case MIN:
			if (tuple2nd.scTup[playNum]<minValue) {
				minValue = tuple2nd.scTup[playNum];
				this.scTup = tuple2nd.scTup.clone();
				count=1;
			}  else if (tuple2nd.scTup[playNum]==minValue) {
				count++;
			}  	
			break;
		case MAX:
			if (tuple2nd.scTup[playNum]>maxValue) {
				maxValue = tuple2nd.scTup[playNum];
				this.scTup = tuple2nd.scTup.clone();
				count=1;
			}  else if (tuple2nd.scTup[playNum]==maxValue) {
				count++;
			}
			break;
		case DIFF: 
    		for (int i=0; i<scTup.length; i++) scTup[i] = scTup[i] - tuple2nd.scTup[i];
    		break;
		}
	}
} // ScoreTuple