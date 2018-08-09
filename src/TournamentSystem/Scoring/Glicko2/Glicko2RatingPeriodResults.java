/*
 * Copyright (C) 2013 Jeremy Gooch <http://www.linkedin.com/in/jeremygooch/>
 *
 * The licence covering the contents of this file is described in the file LICENCE.txt,
 * which should have been included as part of the distribution containing this file.
 */
package TournamentSystem.Scoring.Glicko2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class holds the results accumulated over a rating period.
 * 
 * @author Jeremy Gooch
 */
public class Glicko2RatingPeriodResults {
	private List<Glicko2Result> results = new ArrayList<Glicko2Result>();
	private Set<Glicko2Rating> participants = new HashSet<Glicko2Rating>();

	
	/**
	 * Create an empty resultset.
	 */
	public Glicko2RatingPeriodResults() {}
	

	/**
	 * Constructor that allows you to initialise the list of participants.
	 * 
	 * @param participants (Set of Glicko2Rating objects)
	 */
	public Glicko2RatingPeriodResults(Set<Glicko2Rating> participants) {
		this.participants = participants;
	}
	
	
	/**
	 * Add a result to the set.
	 * 
	 * @param winner
	 * @param loser
	 */
	public void addResult(Glicko2Rating winner, Glicko2Rating loser) {
		Glicko2Result result = new Glicko2Result(winner, loser);
		
		results.add(result);
	}
	
	
	/**
	 * Record a draw between two players and add to the set.
	 * 
	 * @param player1
	 * @param player2
	 */
	public void addDraw(Glicko2Rating player1, Glicko2Rating player2) {
		Glicko2Result result = new Glicko2Result(player1, player2, true);
		
		results.add(result);
	}
	
	
	/**
	 * Get a list of the results for a given player.
	 * 
	 * @param player
	 * @return List of results
	 */
	public List<Glicko2Result> getResults(Glicko2Rating player) {
		List<Glicko2Result> filteredResults = new ArrayList<Glicko2Result>();
		
		for ( Glicko2Result result : results ) {
			if ( result.participated(player) ) {
				filteredResults.add(result);
			}
		}
		
		return filteredResults;
	}

	
	/**
	 * Get all the participants whose results are being tracked.
	 * 
	 * @return set of all participants covered by the resultset.
	 */
	public Set<Glicko2Rating> getParticipants() {
		// Run through the results and make sure all players have been pushed into the participants set.
		for ( Glicko2Result result : results ) {
			participants.add(result.getWinner());
			participants.add(result.getLoser());
		}

		return participants;
	}
	
	
	/**
	 * Add a participant to the rating period, e.g. so that their rating will
	 * still be calculated even if they don't actually compete.
	 *
	 * @param rating
	 */
	public void addParticipants(Glicko2Rating rating) {
		participants.add(rating);
	}
	
	
	/**
	 * Clear the resultset.
	 */
	public void clear() {
		results.clear();
	}
}
