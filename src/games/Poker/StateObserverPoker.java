package games.Poker;

import games.KuhnPoker.KuhnPokerConfig;
import games.ObsNondetBase;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
public class StateObserverPoker extends ObsNondetBase implements StateObsNondeterministic {

	//<editor-fold desc="variables">
	//debug
	public boolean debug = false;

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

	public static final int NUM_PLAYER = PokerConfig.NUM_PLAYERS;
	private static final int START_CHIPS = PokerConfig.START_CHIPS;
	private static final int SMALLBLIND = PokerConfig.SMALLBLIND;
	private static final int BIGBLIND = 2*SMALLBLIND;

	//private static final double REWARD_NEGATIVE = 0;
	//private static final double REWARD_POSITIVE =  START_CHIPS*NUM_PLAYER;
	private static final double REWARD_NEGATIVE = -START_CHIPS;
	private static final double REWARD_POSITIVE =  START_CHIPS*(NUM_PLAYER-1);


    private int m_Player;			// player who makes the next move
	protected ArrayList<ACTIONS> availableActions = new ArrayList<>();	// holds all available actions
	private ArrayList<ACTIONS> availableRandomActions = new ArrayList<>();

	private double[] chips;
	private double[] setStartChips;
	private PlayingCard[][] holeCards; //[player][card]
	private PlayingCard[] communityCards;
	private int dealer;
	private int dealtCards;

	private ArrayList<Integer> cards;

	private short m_phase;

	// active players are those making game actions (i.e. playing & not folded & not all-in)
	private final boolean[] activePlayers;
	// playing players are those still having chips and participating in the game
	private final boolean[] playingPlayers;
	// folded players are those folded in a game round
	private final boolean[] foldedPlayers;
	// open players are those who still need to make game actions for the game to move into the next "phase"
	private Queue<Integer> openPlayers;

	private double[] gamescores;

	private boolean isNextActionDeterministic;
	private ACTIONS nextNondeterministicAction;

	protected ArrayList<String> lastActions;

	private boolean GAMEOVER;
	boolean isPartialState;

	private Pots pots;
	private Random rand;

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .gamelog containing this object will become
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	private int gameround;
	private int turns;

	//</editor-fold>

	//<editor-fold desc="constructor">
	// *** never used *** /WK/
	public void restart(){
		gameround = 0;
		turns = 0;
		isPartialState = false;
		//rand = new Random(System.currentTimeMillis());
		rand = new Random(ThreadLocalRandom.current().nextInt());
		dealtCards = 0;
		setRoundOver(false);
		lastActions = new ArrayList<>();
		GAMEOVER = false;

		dealer = 0;

		// information about the player:
		gamescores = new double[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			chips[i] = START_CHIPS;
			activePlayers[i] = true;
			playingPlayers[i] = true;
			foldedPlayers[i] = false;
			gamescores[i] = START_CHIPS;
		}

		if(setStartChips!=null)
			chips = setStartChips;

		initRound();

		isNextActionDeterministic = true;
	}

	public void setStartChips(double[] chips){
		setStartChips = chips;
	}

	public StateObserverPoker() {
		gameround = 0;
		turns = 0;
		isPartialState = false;
		//rand = new Random(System.currentTimeMillis());
		rand = new Random(ThreadLocalRandom.current().nextInt());
		dealtCards = 0;
		lastActions = new ArrayList<>();
		GAMEOVER = false;
		setRoundOver(false);
		dealer = 0;

		// information about the player:
		chips =  new double[NUM_PLAYER];
		activePlayers = new boolean[NUM_PLAYER];
		foldedPlayers = new boolean[NUM_PLAYER];
		playingPlayers = new boolean[NUM_PLAYER];

		gamescores = new double[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			chips[i] = START_CHIPS;
			activePlayers[i] = true;
			playingPlayers[i] = true;
			foldedPlayers[i] = false;
			//gamescores[i] = START_CHIPS;
			gamescores[i] = 0;
		}

		initRound();

		isNextActionDeterministic = true;
	}

	public StateObserverPoker(StateObserverPoker other)	{
		super(other);
		if(other.setStartChips!=null){
			setStartChips = new double[other.setStartChips.length];
			for(int i = 0;i<other.setStartChips.length;i++)
				setStartChips[i] = other.setStartChips[i];
		}

		gameround = other.gameround;
		turns = other.turns;
		isPartialState = other.isPartialState;
		//rand = new Random(System.currentTimeMillis());
		rand = new Random(ThreadLocalRandom.current().nextInt());

		dealtCards = other.dealtCards;
		////PokerLog.gameLog.log(Level.WARNING," brand after blueprint!");
		debug = other.debug;
		lastActions = new ArrayList<>();
		for(String entry:other.lastActions){
			addToLog(entry);
		}

		isNextActionDeterministic = other.isNextActionDeterministic;
		nextNondeterministicAction = other.nextNondeterministicAction;

		availableRandomActions = new ArrayList<>();
		cards = new ArrayList<>();
		for(int card:other.cards){
			cards.add(card);
			availableRandomActions.add(ACTIONS.fromInt(card));
		}

		GAMEOVER = other.GAMEOVER;
		this.dealer = other.dealer;

		m_Player = other.m_Player;

		chips =  new double[NUM_PLAYER];
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		this.pots = new Pots(other.pots);

		this.m_phase = other.m_phase;

		openPlayers = new LinkedList<>(other.openPlayers);

		chips =  new double[NUM_PLAYER];
		activePlayers = new boolean[NUM_PLAYER];
		playingPlayers = new boolean[NUM_PLAYER];
		foldedPlayers = new boolean[NUM_PLAYER];
		gamescores = new double[NUM_PLAYER];
		for(int i = 0;i<NUM_PLAYER;i++){
			if(other.holeCards[i][0]!=null) {
				this.holeCards[i][0] = other.holeCards[i][0].copy();
				this.holeCards[i][1] = other.holeCards[i][1].copy();
			}
			this.chips[i] = other.chips[i];
			this.activePlayers[i] = other.activePlayers[i];
			this.playingPlayers[i] = other.playingPlayers[i];
			this.foldedPlayers[i] = other.foldedPlayers[i];
			this.gamescores[i] = other.gamescores[i];
		}

		//System.arraycopy would create shallow copy of cards which might be an issue at a later point?
		//no inspection ManualArrayCopy
		for(int i=0;i<communityCards.length;i++)
			if(other.communityCards[i]!=null)
				this.communityCards[i]=other.communityCards[i].copy();

		setAvailableActions();
	}
	//</editor-fold>

	//<editor-fold desc="helper">

	private void initCardDeck(){
		cards = new ArrayList<>();
		availableRandomActions = new ArrayList<>();

		for(int i=1;i<53;i++){
			cards.add(i);
			availableRandomActions.add(ACTIONS.fromInt(i));
		}
	}

	public void dealCard(int id) {

		// remove from cards and available random actions
		cards.remove(Integer.valueOf(id));
		availableRandomActions.remove(ACTIONS.fromInt(id));

		this.communityCards[dealtCards++] = new PlayingCard(id);
		if(dealtCards == 3) {
			m_phase = 1;
			addToLog("Deal Flop : " + this.communityCards[0] + "/" + this.communityCards[1] + "/" + this.communityCards[2]);
		} else if(dealtCards == 4) {
			m_phase = 2;
			addToLog("Deal Turn : " + this.communityCards[3]);
		} else if(dealtCards == 5) {
			m_phase = 3;
			addToLog("Deal River : " + this.communityCards[4]);
		}
	}

	private int dealCard(){
		//int randomCard = cards.remove(rand.nextInt(cards.size()));
		int randomCard = cards.remove(ThreadLocalRandom.current().nextInt(cards.size()));

		availableRandomActions.remove(ACTIONS.fromInt(randomCard));
		return randomCard;
	}

	public void initRound(){
		turns = 0;
		gameround++;
		addToLog("----------New Round ("+gameround+")---------");

		initCardDeck();
		setRoundOver(false);

		m_phase = 0;
		dealtCards = 0;

		// cards
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		openPlayers = new LinkedList<>();

		pots = new Pots();

		// everybody with chips plays again

		for(int i = 0;i<NUM_PLAYER;i++){
			if(chips[i]<=0) {
				playingPlayers[i] = false;
				activePlayers[i] = false;
				foldedPlayers[i] = false;
			}else{
				activePlayers[i] = true;
				foldedPlayers[i] = false;
				holeCards[i][0] = new PlayingCard(dealCard());
				holeCards[i][1] = new PlayingCard(dealCard());
			}
		}

		if(getNumPlayingPlayers()==1){
			GAMEOVER = true;
			return;
		}


		// turn order
		while(!playingPlayers[dealer])
			dealer = (dealer+1)%NUM_PLAYER;

		addToLog(Types.GUI_PLAYER_NAME[dealer]+" is the dealer.");
		int smallBlind = (dealer+1)%NUM_PLAYER;
		while(!playingPlayers[smallBlind])
			smallBlind = (smallBlind+1)%NUM_PLAYER;
		addToLog(Types.GUI_PLAYER_NAME[smallBlind]+" is small blind.");

		int bigBlind = (smallBlind+1)%NUM_PLAYER;
		while(!playingPlayers[bigBlind])
			bigBlind = (bigBlind+1)%NUM_PLAYER;
		addToLog(Types.GUI_PLAYER_NAME[bigBlind]+" is big blind.");

		if(chips[smallBlind]>SMALLBLIND) {
			chips[smallBlind] -= SMALLBLIND;
			pots.add(SMALLBLIND,smallBlind);
		}else{
			pots.add(chips[smallBlind],smallBlind,true);
			chips[smallBlind] = 0;
		}

		if(chips[bigBlind]>BIGBLIND) {
			chips[bigBlind] -= BIGBLIND;
			pots.add(BIGBLIND,bigBlind);
		}else{
			pots.add(chips[bigBlind],bigBlind,true);
			chips[bigBlind] = 0;
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

		dealer = (dealer+1)%NUM_PLAYER;
		m_Player = openPlayers.remove();
		setAvailableActions();
	}

	/**
	 * function to reset the "log" (text version of what happened in GUI) of the game.
	 */
	public void resetLog(){
		lastActions = new ArrayList<>();
	}

	/**
	 * function to add entry to log (text version of what happened in GUI)
	 * */
	public void addToLog(String log){
		lastActions.add(log);
	}

	public long[] determineWinner(){
		ArrayList<PlayingCard> hand;
		long[] scoreOfHand = new long[NUM_PLAYER];

		if(isPartialState){
			for(int i = 0; i < NUM_PLAYER;i++){
				if(holeCards[i][0]==null){
					holeCards[i][0] = new PlayingCard(dealCard());
					holeCards[i][1] = new PlayingCard(dealCard());
				}
			}
		}

		for(int i = 0; i < NUM_PLAYER;i++){
			if(!foldedPlayers[i]&&playingPlayers[i]){
				hand = new ArrayList<>();
				hand.add(holeCards[i][0]);
				hand.add(holeCards[i][1]);
				hand.add(communityCards[0]);
				hand.add(communityCards[1]);
				hand.add(communityCards[2]);
				hand.add(communityCards[3]);
				hand.add(communityCards[4]);

				addToLog(Types.GUI_PLAYER_NAME[i]+" with "+holeCards[i][0]+"/"+holeCards[i][1]);
				int[] bestHand = findBestHand(hand);
				scoreOfHand[i] = findMaxScore(bestHand);
			}else{

				scoreOfHand[i] = -1;
			}
		}
		return scoreOfHand;
	}

	private long findMaxScore(int[] scores){
	/*
		Name:			Possible Values:		Numberspace:
		ROYAL_FLUSH     [4]   					14^12
		STRAIGHT_FLUSH  [8]   					14^11
		FOUR_OF_A_KIND  [13]  					14^10
		FULL_HOUSE      [13*12] 				14^8
		FLUSH           [8] 					14^7
		STRAIGHT        [8] 					14^6
		THREE_OF_A_KIND [13]					14^5
		TWO_PAIR        [13*12]					14^3
		ONE_PAIR        [13]					14^2
		HIGH_CARD       [13]					14^1
		KICKER          [13]					14^0


		ROYAL_FLUSH     14^12
		STRAIGHT_FLUSH  14^11
		FOUR_OF_A_KIND  14^10
		FULL_HOUSE      14^8
		FLUSH           14^7
		STRAIGHT        14^6
		THREE_OF_A_KIND 14^5
		TWO_PAIR        14^3
		ONE_PAIR        14^2
		HIGH_CARD       14^1
		KICKER          14^0
	 */
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
		//TODO: adjust flush logic! 
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
					addToLog("Royal Flush!");
					return score;
				}
				score[STRAIGHT_FLUSH] = straight+1;
				addToLog("Straight Flush!");
				return score;
			}
		}

		if(fourOfAKind>0){
			score[FOUR_OF_A_KIND] = fourOfAKind+1;
			highCards.remove(Integer.valueOf(fourOfAKind));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Four of a Kind with "+PlayingCard.rankAsString(fourOfAKind) + " and " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker");
			addToLog("Four of a Kind with "+PlayingCard.rankAsString(fourOfAKind) + " and " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker");
			return score;
		}

		// Checking for "full house"
		if(threeOfAKind>0){
			pairs.remove(Integer.valueOf(threeOfAKind));
			if(pairs.size()>0){
				score[FULL_HOUSE] = 15*(threeOfAKind+1)+pairs.get(0)+1;
				//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Fullhouse with "+PlayingCard.rankAsString(threeOfAKind) + " and " + PlayingCard.rankAsString(pairs.get(0) ));
				addToLog("Fullhouse with "+PlayingCard.rankAsString(threeOfAKind) + " and " + PlayingCard.rankAsString(pairs.get(0) ));

				return score;
			}
		}

		if(flush>-1){
			//TODO: If more than one player has a flush the player with the highest card not available to all players wins.
			score[FLUSH] = flush+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Flush with " + PlayingCard.suitAsString(flush));
			addToLog("Flush with " + PlayingCard.suitAsString(flush));

			return score;
		}

		if(straight>-1){
			score[STRAIGHT] = straight+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Straight till " + PlayingCard.rankAsString(straight) + "");
			addToLog("Straight till " + PlayingCard.rankAsString(straight) + "");

			return score;
		}

		if(threeOfAKind>-1){
			score[THREE_OF_A_KIND] = threeOfAKind+1;
			highCards.remove(Integer.valueOf(threeOfAKind));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      Three of a kind " + PlayingCard.rankAsString(threeOfAKind) + " and " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");
			addToLog("Three of a kind " + PlayingCard.rankAsString(threeOfAKind) + " and " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");
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
			addToLog("Two pairs " + PlayingCard.rankAsString(pairs.get(0)) + " and " + PlayingCard.rankAsString(pairs.get(1)) + " with " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");

			return score;
		}

		if(pair>-1){
			score[ONE_PAIR] = pair+1;
			highCards.remove(Integer.valueOf(pair));
			score[KICKER] = highCards.get(0)+1;
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      A pair " + PlayingCard.rankAsString(pairs.get(0)) + " with " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");
			addToLog("A pair " + PlayingCard.rankAsString(pairs.get(0)) + " with " + PlayingCard.rankAsString(highCards.get(0)) + " as a kicker.");

			return score;
		}

		score[HIGH_CARD] = highCards.get(0)+1;
		score[KICKER] = highCards.get(1)+1;
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ":      High Card " + PlayingCard.rankAsString(highCards.get(0))  + " with " + PlayingCard.rankAsString(highCards.get(1)) + " as a kicker.");
		addToLog("High Card " + PlayingCard.rankAsString(highCards.get(0))  + " with " + PlayingCard.rankAsString(highCards.get(1)) + " as a kicker.");
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
	//</editor-fold>

	//<editor-fold desc="advance">

	/**
	 * Advance the current state with 'action' to a new state combining the deterministic and the non-deterministic actions in one method.
	 * Actions:
	 * - 0 -> FOLD	: give up the current round
	 * - 1 -> CHECK	: pass for the current action (wait for someone else to do something to react on)
	 * - 2 -> BET	: bet the "Big Blind"
	 * - 3 -> CALL	: bet the same amount as the previous player bet
	 * - 4 -> RAISE	: raise the last bet by the "Big Blind"
	 * - 5 -> ALL IN: bet all remaining chips
	 * @param action to advance the state of the game
	 */
	public void advance(ACTIONS action){
		if(isNextActionDeterministic()){
			advanceDeterministic(action);
		}
		while(!isNextActionDeterministic() && !isRoundOver()){
			advanceNondeterministic();
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
	public void advanceDeterministic(ACTIONS action) {
		super.advanceBase(action);		//		includes addToLastMoves(action)
		if(isRoundOver())
			return;

		incrementMoveCounter();
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
			// getNumPlayingPlayers() - getNumFoldedPlayers() == 1 && openPlayers.size() == 1 => everybody except one player has folded
			// openPlayers.size() == 0 => no player has to make a move
			isNextActionDeterministic = !(
					openPlayers.size() == 0 ||
					(getNumPlayingPlayers() - getNumFoldedPlayers() == 1 &&
					openPlayers.size() == 1)
			);
		}
		if(!GAMEOVER && openPlayers.size() > 0) {
			// next player becomes the active one
			m_Player = openPlayers.remove();
			setAvailableActions();
		}
		if(!isNextActionDeterministic)
			turns++;
	}

	/**
	 * Advance the current state with 'action' to a new state
	 * Actions:
	 * - 1-52: deal the card with ID
	 * 	- 1 -> ♥ 2
	 * 	- 2 -> ♥ 3
	 * 	...
	 * - 53 -> determine winner of the round
	 * - 53 -> determine winner of the round
	 * @param randAction to advance the state of the game
	 */
	public ACTIONS advanceNondeterministic(ACTIONS randAction) {
		if(isRoundOver())
			return randAction;
		if (isNextActionDeterministic) {
			throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
		}
		int action = randAction.toInt();
		isNextActionDeterministic = true;
		if (action < 53){
			dealCard(action);
			// only one active player left and game can progress to showdown
			// OR
			// only two cards have been dealt
			if (getNumActivePlayers() < 2 || dealtCards < 3) {
				isNextActionDeterministic = false;
			}
			if(!GAMEOVER&&isNextActionDeterministic) {
				// next player from the last action will have the next action if it's not a showdown
				m_Player = (m_Player + 1) % NUM_PLAYER;
				for (int i = m_Player; i < NUM_PLAYER; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);
				for (int i = 0; i < m_Player; i++)
					if (playingPlayers[i] && activePlayers[i])
						openPlayers.add(i);

				if(openPlayers.size()==0)
					throw new RuntimeException();

				// next player becomes the active one
				m_Player = openPlayers.remove();
				setAvailableActions();
			}
		}else{
			//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + "Showdown - finding the winner...");
			addToLog("Showdown - finding the winner...");

			//printing community cards:
			StringBuilder cc = new StringBuilder();
			for (PlayingCard card: communityCards) {
				cc.append(card.toString());
			}
			addToLog(cc.toString());

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
					addToLog(winner +" has won Pot ("+ p +") with "+ pots.getPotSize(p) / numWinners +" chips");
					chips[winner] += pots.getPotSize(p)/numWinners;
				}
			}

			//update scores
			updateGamescores();
			//gamescores = chips;

			addToLog("-----------End Round("+gameround+")--------");
			for(int i = 0 ; i <getNumPlayers() ; i++)
				addToLog("Chips "+Types.GUI_PLAYER_NAME[i]+": "+chips[i] +" " +playingPlayers[i]);

			setRoundOver(true);
		}
		return randAction;
	}

	private void updateGamescores(){
		for(int i = 0;i< chips.length;i++)
			gamescores[i] = chips[i]-START_CHIPS;
	}

	public ACTIONS advanceNondeterministic() {
		ArrayList<ACTIONS> possibleRandoms = getAvailableRandoms();
		//advanceNondeterministic(possibleRandoms.get(rand.nextInt(possibleRandoms.size())));
		// above doesn't provide sufficiently random results
		ACTIONS act = possibleRandoms.get(ThreadLocalRandom.current().nextInt(possibleRandoms.size()));
		advanceNondeterministic(act);

		return act;
	}

	//</editor-fold>

	//<editor-fold desc="GBG">

	public StateObserverPoker copy() {
		return new StateObserverPoker(this);
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

	public int getNumAvailableActions() {
		return availableActions.size();
	}

	/**
	 * Return the afterstate preceding {@code this}.
	 */
	@SuppressWarnings("unused")
	public StateObservation getPrecedingAfterstate() {
		return this;
	}

	public ArrayList<ACTIONS> getAvailableRandoms() {
		if(dealtCards < 5) {
			return availableRandomActions;
		}
		ArrayList<ACTIONS> availRan = new ArrayList<>();
		availRan.add(ACTIONS.fromInt(53));
		return availRan;
	}

	public int getNumAvailableRandoms() {
		return cards.size();
	}

	public double getProbability(ACTIONS action) {
		return 1.0/getNumAvailableRandoms();
	}

	/**
	 * For Poker the possible completions are the available random actions (the cards that can be dealt)
	 */
	public ArrayList<ACTIONS> getAvailableCompletions() {
		return getAvailableRandoms();
	}
	public ArrayList<ACTIONS> getAvailableCompletions(int p) {
		return getAvailableRandoms();
	}
	/**
	 *
	 * @return true, if the current position is a win (for either player)
	 */
	public boolean win() {
		return GAMEOVER;
	}

	/**
	 * @return 	returns the sum of rewards in state so, seen from the perspective of the player to move in state refer. {@code refer}.
	 */
	public double getGameScore(StateObservation refer) {
		/*if(isGameOver()) {
			if(chips[refer.getPlayer()]>0)
				return 1;
			return -1;
		}
		return 0;
		 */
		return gamescores[refer.getPlayer()];
	}

	@Override
	public double getGameScore(int player){
		return gamescores[player];
	}

	@Override
	public double getReward(StateObservation referringState, boolean rewardIsGameScore){
		return gamescores[referringState.getPlayer()];
	}


	@Override
	public double getReward(int player, boolean rewardIsGameScore){
		return gamescores[player];
	}

	public double getMinGameScore() {
		return REWARD_NEGATIVE;
	}

	public double getMaxGameScore() {
		return REWARD_POSITIVE;
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
	public boolean isImperfectInformationGame() { return true; }

	@Override
	public boolean isLegalState() {
		return true;
	}


	@SuppressWarnings("unused")
	public boolean isLegalAction(ACTIONS act) {
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

	public String getName() { return "Poker";	}

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


	//</editor-fold>

	//<editor-fold desc="actions">
	public void fold(){
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": 'FOLD'");
		activePlayers[m_Player] = false;
		foldedPlayers[m_Player] = true;
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'fold'");
	}

	public void check(){
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": 'CHECK'");
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'check'");
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
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'all in' ("+sizeOfBet+")");
	}

	public void bet(){
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": BET ("+Integer.toString(BIGBLIND)+")");
		betSub(BIGBLIND);
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'bet' ("+BIGBLIND+")");
	}

	public void raise(){
		double raise = pots.getOpenPlayer(m_Player)+BIGBLIND;
		//PokerLog.gameLog.info(Integer.toString(System.identityHashCode(this)) + ": " + Types.GUI_PLAYER_NAME[m_Player]+": 'RAISE' ("+Integer.toString(raise)+")");
		betSub(raise);
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'raise' ("+raise+")");
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
			addToLog(Types.GUI_PLAYER_NAME[m_Player]+": call ("+toCall+") - ALL IN");
		}else{
			addToLog(Types.GUI_PLAYER_NAME[m_Player]+": call ("+toCall+")");
		}
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
	//</editor-fold>

	//<editor-fold desc="get">
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
		if(holeCards!=null)
			return holeCards[player];
		return null;
	}

	public PlayingCard[] getHoleCards(){
		return getHoleCards(m_Player);
	}

	public double getOpenPlayer(int player){
		return pots.getOpenPlayer(player);
	}

	public double getOpen(){
		return getOpenPlayer(m_Player);
	}


	public String getLastAction(){
		if(lastActions.size()>0)
			return lastActions.get(lastActions.size()-1)+"\r\n";
		else
			return "";
	}

	public ArrayList<String> getLastActions(){
		return lastActions;
	}

	public ACTIONS getAction(int i) {
		return availableActions.get(i);
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

	public int getBigblind(){return BIGBLIND;}

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

	public boolean[] getOpenPlayers(){
		Object[] tmp = openPlayers.toArray();
		boolean[] openPlayersArray = new boolean[NUM_PLAYER];
		for(Object x : tmp) {
			openPlayersArray[(int)x] = true;
		}
		return openPlayersArray;
	}

	public double getPlayerSate(int m_Player){
		if(!playingPlayers[m_Player])
			return 1;
		if(foldedPlayers[m_Player])
			return 2;
		if(getOpenPlayers()[m_Player])
			return 3;
		return 4;
	}

	public int getGameround(){
		return gameround;
	}

	public int getPotSize(){
		return pots.getSize();
	}
	//</editor-fold>

	public void setHoleCards(int player, PlayingCard first, PlayingCard sec){
		holeCards[player][0] = first;
		holeCards[player][1] = sec;
	}

	public boolean isRoundOver(){
		return m_roundOver;
	}

	@Override
	public boolean isRoundBasedGame() {
		return true;
	}

	@SuppressWarnings("unused")
	public void setCommunityCards(PlayingCard[] oCommunityCards){
		int tmp = 0;
		for(PlayingCard card:oCommunityCards)
			communityCards[tmp++] = card;
	}

	@Override
	public StateObserverPoker partialState(){
		StateObserverPoker partialState = this.copy();
		int player = partialState.getPlayer();
		partialState.setPartialState(true);
		m_roundOver = false;
			for (int i = 0; i < partialState.holeCards.length; i++) {
				if(i==player)
					continue;
				if(partialState.holeCards[i][0]!=null) {

					partialState.cards.add(partialState.holeCards[i][0].getId());
					partialState.availableRandomActions.add(ACTIONS.fromInt(partialState.holeCards[i][0].getId()));

					partialState.cards.add(partialState.holeCards[i][1].getId());
					partialState.availableRandomActions.add(ACTIONS.fromInt(partialState.holeCards[i][1].getId()));


					partialState.holeCards[i][0] = null;
					partialState.holeCards[i][1] = null;
				}
			}
		return partialState;
	}

	@Override
	public boolean isPartialState(){
		return isPartialState;
	}

	@Override
	public void setPartialState(boolean partialState) {
		isPartialState = partialState;
	}

	public int getMoveCounter() {
		return m_counter;
	}


	public Types.ACTIONS[] getStoredActions(){
		return storedActions;
	}

	public void resetCards(){
		//reset card deck
		initCardDeck();

		// reset cards
		holeCards = new PlayingCard[NUM_PLAYER][2];
		communityCards = new PlayingCard[5];

		// everybody with chips plays again

		for(int i = 0;i<NUM_PLAYER;i++){
			if(chips[i]<=0) {

			}else{
				//deal new hole cards
				holeCards[i][0] = new PlayingCard(dealCard());
				holeCards[i][1] = new PlayingCard(dealCard());
			}
		}
	}

	public void randomizeState(){
		resetCards();
	}

	public boolean needsRandomization(){
		return true;
	}

	public int getPhase(){
		return m_phase;
	}

	public int getTurns(){
		return turns;
	}

}
