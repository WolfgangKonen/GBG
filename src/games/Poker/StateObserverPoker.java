package src.games.Poker;

import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types.ACTIONS;

import java.util.*;

/**
 * Class StateObservation observes the current state of the game, it has utility functions for
 * <ul>
 * <li> returning the available actions ({@link #getAvailableActions()}), 
 * <li> advancing the state of the game with a specific action ({@link #advance(ACTIONS)}),
 * <li> copying the current state
 * <li> signaling end, score and winner of the game
 * </ul>
 *
 */
public class StateObserverPoker extends ObserverBase implements StateObsNondeterministic {

	// Housekeeping
	public static final int ROYAL_FLUSH = 0;
	public static final int STRAIGHT_FLUSH = 1;
	public static final int FOUR_OF_A_KIND = 2;
	public static final int FULL_HOUSE = 3;
	public static final int FLUSH = 4;
	public static final int STRAIGHT = 5;
	public static final int THREE_OF_A_KIND = 6;
	public static final int TWO_PAIR = 7;
	public static final int ONE_PAIR = 8;
	public static final int HIGH_CARD = 9;
	public static final int KICKER = 10;

	private static final double REWARD_NEGATIVE = -1.0;
    private static final double REWARD_POSITIVE =  1.0;

	public static final int NUM_PLAYER = 4;
	private static final int START_CHIPS = 100;
	private static final int SMALLBLIND = 5;
	private static final int BIGBLIND = 2*SMALLBLIND;

    private int m_Player;			// player who makes the next move
	protected ArrayList<ACTIONS> availableActions = new ArrayList<>();	// holds all available actions

	private CardDeck m_deck;
	private double[] chips;
	private PlayingCard[][] holeCards; //[player][card]
	private PlayingCard[] communityCards;
	private int dealer;

	private short m_phase;

	private final boolean[] activePlayers;
	private final boolean[] playingPlayers;
	private final boolean[] foldedPlayers;
	private Queue<Integer> openPlayers;

	private boolean isNextActionDeterministic;
	private ACTIONS nextNondeterministicAction;
	protected ArrayList<Integer> availableRandoms = new ArrayList<>();


	private boolean GAMEOVER;

	private Pots pots;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .gamelog containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public StateObserverPoker() {

		GAMEOVER = false;
		dealer = 0;

		// information about the player:
		chips =  new double[NUM_PLAYER];
		activePlayers = new boolean[NUM_PLAYER];
		foldedPlayers = new boolean[NUM_PLAYER];
		playingPlayers = new boolean[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			chips[i] = START_CHIPS;
			activePlayers[i] = true;
			playingPlayers[i] = true;
			foldedPlayers[i] = false;
		}

		initRound();

		m_Player = openPlayers.remove();
		setAvailableActions();
		isNextActionDeterministic = true;
	}

	public StateObserverPoker(StateObserverPoker other)	{
		////PokerLog.gameLog.log(Level.WARNING," brand after blueprint!");

		isNextActionDeterministic = other.isNextActionDeterministic;
		nextNondeterministicAction = other.nextNondeterministicAction;

		availableRandoms = new ArrayList<>(other.availableRandoms);

		GAMEOVER = other.GAMEOVER;
		this.dealer = other.dealer;

		m_Player = other.m_Player;

		chips =  new double[NUM_PLAYER];
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		this.pots = new Pots(other.pots);

		this.m_deck = other.m_deck.copy();
		this.m_phase = other.m_phase;

		openPlayers = new LinkedList<>(other.openPlayers);

		chips =  new double[NUM_PLAYER];
		activePlayers = new boolean[NUM_PLAYER];
		playingPlayers = new boolean[NUM_PLAYER];
		foldedPlayers = new boolean[NUM_PLAYER];
		for(int i = 0;i<NUM_PLAYER;i++){
			this.holeCards[i] = other.holeCards[i];
			this.chips[i] = other.chips[i];
			this.activePlayers[i] = other.activePlayers[i];
			this.playingPlayers[i] = other.playingPlayers[i];
			this.foldedPlayers[i] = other.foldedPlayers[i];
		}

		//System.arraycopy would create shallow copy of cards which might be an issue at a later point?
		//noinspection ManualArrayCopy
		for(int i=0;i<communityCards.length;i++)
			this.communityCards[i]=other.communityCards[i];

		setAvailableActions();
	}
	
	public StateObserverPoker copy() {
		return new StateObserverPoker(this);
	}

	public void initRound(){
		m_deck = new CardDeck();
		m_phase = 0;
		// everybody with chips plays again

		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": InitRound()");

		for(int i = 0;i<NUM_PLAYER;i++){
			if(chips[i]<=0) {
				playingPlayers[i] = false;
				activePlayers[i] = false;
				foldedPlayers[i] = true;
			}else{
				activePlayers[i] = true;
				foldedPlayers[i] = false;
			}
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) +
			//		": Player ["+Types.GUI_PLAYER_NAME[i]+"]" +
			//		" Playing["+playingPlayers[i]+"]"+
			//		" Active[" + activePlayers[i] +"]"+
			//		" Chips[" + chips[i] +"]"
			//		);
		}

		if(getNumPlayingPlayers()==1){
			GAMEOVER = true;
			return;
		}

		// cards
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		openPlayers = new LinkedList<>();

		pots = new Pots();

		// turn order

		while(!playingPlayers[dealer])
			dealer = (dealer+1)%NUM_PLAYER;


		//PokerLog.gameLog.log(Level.INFO, Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[dealer]+" is the dealer." );

		int smallBlind = (dealer+1)%NUM_PLAYER;
		while(!playingPlayers[smallBlind])
			smallBlind = (smallBlind+1)%NUM_PLAYER;
		//PokerLog.gameLog.log(Level.INFO, Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[smallBlind]+" is small blind.");

		int bigBlind = (smallBlind+1)%NUM_PLAYER;
		while(!playingPlayers[bigBlind])
			bigBlind = (bigBlind+1)%NUM_PLAYER;
		//PokerLog.gameLog.log(Level.INFO, Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[bigBlind]+" is big blind.");

		if(chips[smallBlind]>SMALLBLIND) {
			chips[smallBlind] -= SMALLBLIND;
			pots.add(SMALLBLIND,smallBlind);
		}else{
			pots.add(chips[smallBlind],smallBlind,true);
			chips[smallBlind] = 0;
			//PokerLog.gameLog.log(Level.INFO, Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[smallBlind]+" does not have sufficient chips - all in with small blind.");
		}

		if(chips[bigBlind]>BIGBLIND) {
			chips[bigBlind] -= BIGBLIND;
			pots.add(BIGBLIND,bigBlind);
		}else{
			pots.add(chips[bigBlind],bigBlind,true);
			chips[bigBlind] = 0;
			//PokerLog.gameLog.log(Level.INFO, Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[bigBlind]+" does not have sufficient chips - all in with big blind.");
		}

		int t = (bigBlind+1)%NUM_PLAYER;
		while(!playingPlayers[t]) {
			t=(t+1)%NUM_PLAYER;
		}

		for(int i = t;i<NUM_PLAYER;i++)
			if(playingPlayers[i])
				openPlayers.add(i);

		for(int i = 0;i<t;i++)
			if(playingPlayers[i])
				openPlayers.add(i);


		//small & big blind
		dealCards();

		dealer = (dealer+1)%NUM_PLAYER;
	}

	@Override
	public boolean isGameOver() {
		return GAMEOVER;
	}

    @Override
	public boolean isDeterministicGame() {
		return false;
	}
	
    @Override
	public boolean isFinalRewardGame() {
		return true;
	}

    @Override
	public boolean isLegalState() {
		return true;
	}
	
	public boolean isLegalAction(ACTIONS act) {
		System.out.print(act);
		return true;
	}

	@Deprecated
    public String toString() {
    	return stringDescr();
    }
	
	@Override
    public String stringDescr() {
		return "";
	}


	public boolean isNextActionDeterministic() {
		return isNextActionDeterministic;
	}

	public ACTIONS getNextNondeterministicAction() {
		setNextNondeterministicAction();
		return nextNondeterministicAction;
	}

	private void setNextNondeterministicAction() {
		if(isNextActionDeterministic) {
			throw new RuntimeException("next Action is Deterministic");
		} else if(nextNondeterministicAction != null) {
			return;
		}
		System.out.print("setNextNondeterministicAction");
	}

	//TODO: implement correct randoms.
	public ArrayList<ACTIONS> getAvailableRandoms() {
		ArrayList<ACTIONS> availRan = new ArrayList<>();
		for(int viableMove : availableRandoms) {
			availRan.add(ACTIONS.fromInt(viableMove));
		}
		return availRan;

	}

	public int getNumAvailableRandoms() {
		return availableRandoms.size();
	}

	public double getProbability(ACTIONS action) {
		int iAction = action.toInt();
		int numEmptyTiles = iAction/2;
		double prob = (iAction%2==0) ? 0.9 : 0.1;
		return prob/numEmptyTiles;
	}

	/**
	 * 
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win() {
		return GAMEOVER;
	}


	/**
	 * @return 	the game score, i.e. the sum of rewards for the current state. 
	 * 			For TTT only game-over states have a non-zero game score. 
	 * 			It is the reward from the perspective of {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		//return Math.random();
		StateObserverPoker sop = (StateObserverPoker)refer;
		return sop.getPot();
	}


	@Override
	public double getReward(StateObservation referringState, boolean rewardIsGameScore){
		return Math.random();
	}

	public double getMinGameScore() { return REWARD_NEGATIVE; }

	public double getMaxGameScore() { return REWARD_POSITIVE; }

	public String getName() { return "Poker";	}

	public void dealCards(){

		// shuffling the deck to make sure we have a random deck
		m_deck.shuffle();
		if(this.m_phase==0){
			// set small blind
			// set big blind
			// deal holecards
			if(m_deck.getTotalCards()<52){
				//PokerLog.gameLog.severe("SOMETHINGS WRONG!");
				throw new RuntimeException("round starts with less than 52 cards");
			}
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Deal Holecards.");
			for(int i = 0;i<NUM_PLAYER;i++){
				this.holeCards[i][0] = this.m_deck.draw();
				this.holeCards[i][1] = this.m_deck.draw();
				//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Player ["+ Types.GUI_PLAYER_NAME[i]+"] - "+this.holeCards[i][0].toString() +"/"+this.holeCards[i][1]);
			}
		}

		if(m_phase == 1){
			// deal flop
			this.communityCards[0] = this.m_deck.draw();
			this.communityCards[1] = this.m_deck.draw();
			this.communityCards[2] = this.m_deck.draw();
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Deal Flop in "+Integer.toString(System.identityHashCode(this))+" : "+this.communityCards[0]+"/"+this.communityCards[1]+"/"+this.communityCards[2]) ;
		}

		if(m_phase == 2){
			// deal turn
			this.communityCards[3] = this.m_deck.draw();
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Deal Turn in "+Integer.toString(System.identityHashCode(this))+" : "+this.communityCards[3]);

		}

		if(m_phase == 3){
			// deal river
			this.communityCards[4] = this.m_deck.draw();
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Deal River in "+Integer.toString(System.identityHashCode(this))+" : "+this.communityCards[4]);
		}

		if(m_phase > 3){
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Showdown - finding the winner...");
			long[] scores = determineWinner();
			boolean[][] claims = pots.getClaims();
			for(int p = 0;p<claims.length;p++){
				long maxScore = 0;
				ArrayList<Integer> winners = new ArrayList<>();
				for(int i = 0;i<scores.length;i++) {
					if(maxScore == scores[i])
						winners.add(i);
					if(claims[p][i]&&maxScore<scores[i]){
						maxScore = scores[i];
						winners.clear();
						winners.add(i);
					}
				}
				int numWinners = winners.size();
				for(int winner:winners){
					//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Integer.toString(winner)+" has won Pot ("+Integer.toString(p)+") with "+Integer.toString(pots.getPotSize(p)/numWinners) +" chips");
					chips[winner] += pots.getPotSize(p)/numWinners;
				}
			}

			initRound();
			setAvailableActions();
		}else {
			m_phase++;
		}
	}

	public long[] determineWinner(){
		ArrayList<PlayingCard> hand;
		long[] scoreOfHand = new long[NUM_PLAYER];
		for(int i = 0; i < NUM_PLAYER;i++){
			if(!foldedPlayers[i]){
				hand = new ArrayList<>();
				hand.add(holeCards[i][0]);
				hand.add(holeCards[i][1]);
				hand.add(communityCards[0]);
				hand.add(communityCards[1]);
				hand.add(communityCards[2]);
				hand.add(communityCards[3]);
				hand.add(communityCards[4]);
				//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[i]+" has...");
				int[] bestHand = findBestHand(hand);
				scoreOfHand[i] = findMaxScore(bestHand);
				//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[i]+" has a score of "+Long.toString(scoreOfHand[i]));
			}else{
				//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[i]+" has folded.");
				scoreOfHand[i] = -1;
			}
		}
		return scoreOfHand;
	}

	/*
		ROYAL_FLUSH     = {0-1}   [2]   13^12
		STRAIGHT_FLUSH  = {5-12}  [8]   13^11
		FOUR_OF_A_KIND  = {0-12}  [13]  13^10
		FULL_HOUSE      = {0-181} [182] 13^8
		FLUSH           = {0-3}   [4];  13^7
		STRAIGHT        = {5-12}  [8];  13^6
		THREE_OF_A_KIND = {0-12}  [13]; 13^5
		TWO_PAIR        = {0-181} [182];13^3
		ONE_PAIR        = {0-12}  [13]; 13^2
		HIGH_CARD       = {0-12}  [13]; 13^1
		KICKER          = {0-12}  [13]; 13^0
	 */
	private long findMaxScore(int[] scores){
		int[] exponent = { 12, 11, 10, 8, 7, 6, 5, 3, 2, 1, 0};
		for(int i=0;i<scores.length;i++){
			if(scores[i]>0) {
				return scores[i] * (long) Math.pow(14, exponent[i]) + scores[10];
			}
		}
		return 0;
	}

	public int[] findBestHand(ArrayList<PlayingCard> cards){

		int[] suits = new int[4];
		int[] ranks = new int[13];

		for(PlayingCard c : cards){
			suits[c.getSuit()]++;
			ranks[c.getRank()]++;
		}

		// Checking for "high card"
		ArrayList<Integer> highCards= new ArrayList<>();
		for(int i = 12 ; i>=0 ; i--) {
			if (ranks[i] > 0) {
				highCards.add(i);
			}
		}

		// Checking for "pair", "three of a kind" and "four of a kind"
		int pair = -1;
		int threeOfAKind = -1;
		int fourOfAKind = -1;

		ArrayList<Integer> pairs = checkForMultiples(ranks, 2);
		if(pairs.size()>0) {
			pair = pairs.get(0);

			ArrayList<Integer> threeOfAKinds = checkForMultiples(ranks, 3);
			if(threeOfAKinds.size()>0) {
				threeOfAKind = threeOfAKinds.get(0);

				ArrayList<Integer> fourOfAKinds = checkForMultiples(ranks, 4);
				if(fourOfAKinds.size()>0) {
					fourOfAKind = fourOfAKinds.get(0);
				}
			}
		}

		// Checking for "Flush"
		int flush = -1;
		for(int i = 0 ; i < suits.length ; i++) {
			if(suits[i]>4){
				flush = i;
				break;
			}
		}

		// Checking for "street"
		int straight = -1;
		int streetSize = 0;

		// Check if there is an ace that can start a street (Ace,2,3,4,5)
		if(ranks[12]>0)
			streetSize++;
		for(int i = 0;i<13;i++){
			if(ranks[i]>0) {
				streetSize++;
				if(streetSize>4) {
					straight = i;
				}
			} else {
				streetSize = 0;
			}
		}

		int[] score = new int[11];

		// Check for Royal & Straight Flush
		if(flush>-1&&straight>-1){
			short checkStraightFlush = 0;
			for(PlayingCard c : cards){
				if(c.getSuit()==flush &&
						straight-5<c.getRank()&&c.getRank()<=straight){
					checkStraightFlush++;
				}
			}
			if(checkStraightFlush>=5) {
				if (straight == 12) {
					//straight flush!
					score[ROYAL_FLUSH] = flush+1;
					//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Royal Flush!");
					return score;
				}
				score[STRAIGHT_FLUSH] = straight+1;
				return score;
			}
		}

		if(fourOfAKind>0){
			score[FOUR_OF_A_KIND] = fourOfAKind+1;
			highCards.remove(Integer.valueOf(fourOfAKind));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Four of a Kind with "+PlayingCard.rankAsString(fourOfAKind) + " and " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker");
			return score;
		}

		// Checking for "full house"
		if(threeOfAKind>0){
			pairs.remove(Integer.valueOf(threeOfAKind));
			if(pairs.size()>0){
				score[FULL_HOUSE] = 15*(threeOfAKind+1)+pairs.get(0)+1;
				//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Fullhouse with "+PlayingCard.rankAsString(threeOfAKind) + " and " + PlayingCard.rankAsString(pairs.get(0) ));

				return score;
			}
		}

		if(flush>-1){
			//TODO: If more than one player has a flush the player with the highest card not available to all players wins.
			score[FLUSH] = flush+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Flush with " + PlayingCard.suitAsString(flush));

			return score;
		}

		if(straight>-1){
			score[STRAIGHT] = straight+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Straight till " + PlayingCard.rankAsString(straight) + "");

			return score;
		}

		if(threeOfAKind>-1){
			score[THREE_OF_A_KIND] = threeOfAKind+1;
			highCards.remove(Integer.valueOf(threeOfAKind));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Three of a kind " + PlayingCard.rankAsString(threeOfAKind) + " and " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");
			return score;
		}

		if(pairs.size()>1){
			score[TWO_PAIR] = (pairs.get(0)+1)*15 + pairs.get(1) + 1;
			//highCards.remove(Integer.valueOf(pairs.get(0)));
			//highCards.remove(Integer.valueOf(pairs.get(1)));
			highCards.remove(pairs.get(0));
			highCards.remove(pairs.get(1));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Two pairs " + PlayingCard.rankAsString(pairs.get(0)) + " and " + PlayingCard.rankAsString(pairs.get(1)) + " with " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");

			return score;
		}

		if(pair>0){
			score[ONE_PAIR] = pair+1;
			highCards.remove(Integer.valueOf(pair));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      A pair " + PlayingCard.rankAsString(pairs.get(0)) + " with " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");

			return score;
		}

		score[HIGH_CARD] = highCards.get(0)+1;
		score[KICKER] = highCards.get(1)+1;
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      High Card " + PlayingCard.rankAsString(highCards.get(0))  + " with " + PlayingCard.rankAsString(highCards.get(1)) + " as a kicker.");

		return score;
	}

	private ArrayList<Integer> checkForMultiples(int[] ranks, int multiple){
		ArrayList<Integer> multiples = new ArrayList<>();
		for(int i = 12;i>=0;i--){
			if(ranks[i]>=multiple){
				multiples.add(i);
			}
		}
		return multiples;
	}

	public void fold(){
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": 'FOLD'");
		activePlayers[m_Player] = false;
		foldedPlayers[m_Player] = true;
	}

	public void check(){
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": 'CHECK'");
	}

	public void allIn(){
		double sizeOfBet = chips[m_Player];
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": ALL IN ("+Integer.toString(sizeOfBet)+")");
		boolean raise = sizeOfBet > pots.getOpenPlayer(m_Player);

		pots.add(sizeOfBet, m_Player,true);
		chips[m_Player] = 0;

		if(raise){
			openPlayers.clear();
			for (int i = m_Player + 1; i < NUM_PLAYER; i++)
				if (playingPlayers[i]&&activePlayers[i])
					openPlayers.add(i);

			for (int i = 0; i < m_Player; i++)
				if (playingPlayers[i]&&activePlayers[i])
					openPlayers.add(i);
		}
		activePlayers[m_Player] = false;
	}

	public void bet(){
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": BET ("+Integer.toString(BIGBLIND)+")");
		betSub(BIGBLIND);
	}

	public void raise(){
		int raise = pots.getOpenPlayer(m_Player)+BIGBLIND;
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": 'RAISE' ("+Integer.toString(raise)+")");
		betSub(raise);
	}

	public void betSub(double sizeOfBet){

		pots.add(sizeOfBet, m_Player);
		chips[m_Player] -= sizeOfBet;

		//add all but the active player to open players
		openPlayers.clear();
		for (int i = m_Player + 1; i < NUM_PLAYER; i++)
			if (playingPlayers[i]&&activePlayers[i])
				openPlayers.add(i);

		for (int i = 0; i < m_Player; i++)
			if (playingPlayers[i]&&activePlayers[i])
				openPlayers.add(i);

	}

	public void call(){
		double toCall = pots.getOpenPlayer(m_Player);
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": CALL ("+Integer.toString(toCall)+")");

		// shouldn't happen - action is only available if player has more chips.
		if(toCall > chips[m_Player]){
			//PokerLog.gameLog.severe("Player calls with not enough chips.");
			toCall = chips[m_Player];
		}
		pots.add(toCall,m_Player);
		chips[m_Player] -= toCall;

		if(chips[m_Player]==0) {
			activePlayers[m_Player] = false;
		}
	}




	/**
	 * Advance the current state with 'action' to a new state
	 * Actions:
	 * - 0 -> FOLD	: give up the current round
	 * - 1 -> CHECK	: pass for the current action (wait for someone else to do something to react on)
	 * - 2 -> BET	: bet the "Big Blind"
	 * - 3 -> CALL	: bet the same amount as the previous player bet
	 * - 4 -> RAISE	: raise the last bet by the "Big Blind"
	 * - 5 -> ALL IN: bet all remaining chips
	 * @param action to advance the state of the game
	 */
	public void advance(ACTIONS action) {
		int iAction = action.toInt();

		switch (iAction) {
			case 0 -> fold();
			case 1 -> check();
			case 2 -> bet();
			case 3 -> call();
			case 4 -> raise();
			case 5 -> allIn();
		}

		// only one active player left and game can progress to showdown
		if(getNumActivePlayers()<2&&openPlayers.size()==0){
			while(m_phase<4)
				dealCards();
			dealCards();
		}else {
			if(getNumPlayingPlayers()- getNumFoldedPlayers()==1&&openPlayers.size()==1){
				while(m_phase<4)
					dealCards();
				dealCards();
			}else {
				// If there are no persons left with an open action the dealer will reveal the next card(s)
				if (openPlayers.size() == 0) {
					// next player from the last action will have the next action if it's not a showdown
					m_Player = (m_Player + 1) % NUM_PLAYER;
					for (int i = m_Player; i < NUM_PLAYER; i++)
						if (playingPlayers[i] && activePlayers[i])
							openPlayers.add(i);
					for (int i = 0; i < m_Player; i++)
						if (playingPlayers[i] && activePlayers[i])
							openPlayers.add(i);
					dealCards();
				}
			}
		}
		if(!GAMEOVER) {
			// next player becomes the active one
			m_Player = openPlayers.remove();
			setAvailableActions();
		}
	}

	public void advanceDeterministic(ACTIONS action) {
		if(!isNextActionDeterministic) {
			throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
		}

		int iAction = action.toInt();

		switch (iAction) {
			case 0 -> fold();
			case 1 -> check();
			case 2 -> bet();
			case 3 -> call();
			case 4 -> raise();
			case 5 -> allIn();
		}

		if(getNumActivePlayers() <2 && openPlayers.size() == 0){
			isNextActionDeterministic = false;
		}else {
			isNextActionDeterministic = !((openPlayers.size() == 0 || getNumPlayingPlayers() - getNumFoldedPlayers() == 1&& openPlayers.size() == 1));
		}
		if(!GAMEOVER) {
			// next player becomes the active one
			m_Player = openPlayers.remove();
			setAvailableActions();
		}
	}

	public void advanceNondeterministic(ACTIONS action) {
		advanceNondeterministic();
	}

	public void advanceNondeterministic() {
		if(isNextActionDeterministic) {
			throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
		}
		// only one active player left and game can progress to showdown
		if(getNumActivePlayers()<2&&openPlayers.size()==0){
			while(m_phase<4)
				dealCards();
			dealCards();
		}else {
			// If there are no persons left with an open action the dealer will reveal the next card(s)
			if (openPlayers.size() == 0) {

				// next player from the last action will have the next action if it's not a showdown
				m_Player = (m_Player + 1) % NUM_PLAYER;
				for (int i = m_Player; i < NUM_PLAYER; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				for (int i = 0; i < m_Player; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				dealCards();
			}
		}
		if(!GAMEOVER) {
			// next player becomes the active one
			m_Player = openPlayers.remove();
			setAvailableActions();
		}
		isNextActionDeterministic = true;
	}

    /**
     * Return the afterstate preceding {@code this}.
     */
    public StateObservation getPrecedingAfterstate() {
    	return this;
    }

    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> allActions = new ArrayList<>();
		allActions.add(ACTIONS.fromInt(0)); // FOLD
		allActions.add(ACTIONS.fromInt(1)); // CHECK
		allActions.add(ACTIONS.fromInt(2)); // BET
		allActions.add(ACTIONS.fromInt(3)); // CALL
		allActions.add(ACTIONS.fromInt(4)); // RAISE
		allActions.add(ACTIONS.fromInt(5)); // ALL-IN
        return allActions;
    }
    
	public ArrayList<ACTIONS> getAvailableActions() {
		return availableActions;
	}
	
	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Set them in member ACTIONS[] actions.
	 */
	public void setAvailableActions() {
		availableActions.clear();

		// You always have the option to fold.
		availableActions.add(ACTIONS.fromInt(0)); // FOLD

		// You can only check if nobody has bet before
		if(pots.getOpenPlayer(m_Player)==0)
			availableActions.add(ACTIONS.fromInt(1)); // CHECK

		// You can only bet if nobody has bet before (otherwise it's a raise)
		if(pots.getOpenPlayer(m_Player)==0&&chips[m_Player]>BIGBLIND)
			availableActions.add(ACTIONS.fromInt(2)); // BET

		// You can only call if somebody has bet before
		if(pots.getOpenPlayer(m_Player)>0&&chips[m_Player]>pots.getOpenPlayer(m_Player))
			availableActions.add(ACTIONS.fromInt(3)); // CALL

		// You can only raise if somebody has bet before
		if(pots.getOpenPlayer(m_Player)>0&&chips[m_Player]>pots.getOpenPlayer(m_Player)+BIGBLIND)
			availableActions.add(ACTIONS.fromInt(4)); // RAISE

		// You always have the option to go All In.
				availableActions.add(ACTIONS.fromInt(5)); // All In
	}
	
	public ACTIONS getAction(int i) {
		return availableActions.get(i);
	}

	public int[][] getTable() {
		return new int[1][1];
	}

	/**
	 * @return the id of the active player.
	 */
	public int getPlayer() {
		return m_Player;
	}
	
	public int getNumPlayers() {
		return NUM_PLAYER;
	}

	public double[] getChips(){
		return chips;
	}

	public boolean[] getActivePlayers(){
		return activePlayers;
	}

	public boolean[] getFoldedPlayers(){
		return foldedPlayers;
	}

	public boolean[] getPlayingPlayers(){
		return playingPlayers;
	}

	public int getPot(){
		return pots.getSize();
	}

	public int getNumActivePlayers(){
		int p = 0;
		for (boolean activePlayer : activePlayers)
			if (activePlayer)
				p++;
		return p;
	}

	public int getNumPlayingPlayers(){
		int p = 0;
		for (boolean playingPlayer : playingPlayers)
			if (playingPlayer)
				p++;
		return p;
	}

	public int getNumFoldedPlayers(){
		int p = 0;
		for(int i = 0;i<playingPlayers.length;i++)
			if(foldedPlayers[i])
				p++;
		return p;
	}

	public PlayingCard[] getCommunityCards(){
		return communityCards;
	}

	public PlayingCard[] getHoleCards(int player){
		return holeCards[player];
	}

	public PlayingCard[] getHoleCards(){
		if(holeCards!=null)
			return holeCards[m_Player];
		return null;
	}

	public double getOpenPlayer(int player){
		return pots.getOpenPlayer(player);
	}

}
