package games.KuhnPoker;

import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

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
public class StateObserverKuhnPoker extends ObserverBase implements StateObsNondeterministic {

	//<editor-fold desc="variables">
	//debug
	public boolean debug = false;

	public static final int NUM_PLAYER = 2;
	private static final int START_CHIPS = 10;
	private static final int BIGBLIND = 1;

	private static final double REWARD_NEGATIVE = 0;
	private static final double REWARD_POSITIVE =  START_CHIPS*NUM_PLAYER;

    private int m_Player;			// player who makes the next move
	protected ArrayList<ACTIONS> availableActions = new ArrayList<>();	// holds all available actions
	private ArrayList<ACTIONS> availableRandomActions;
	public ACTIONS lastAction;


	private double[] chips;
	private PlayingCard[][] holeCards; //[player][card]
	private PlayingCard[] communityCards;

	private ArrayList<Integer> cards;

	// folded players are those folded in a game round
	private final boolean[] foldedPlayers;

	int start_player;

	private Boolean[] checkedPlayer;

	private double[] gamescores;

	private boolean isNextActionDeterministic;
	private ACTIONS nextNondeterministicAction;

	protected ArrayList<String> lastActions;

	private boolean GAMEOVER;
	boolean isPartialState;

	private Pots pots;
	private Random rand;

	private boolean newRound;

	/**
	 * change the version ID for serialization only if a newer version is no longer
	 * compatible with an older one (older .gamelog containing this object will become
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	private int gameround;

	//</editor-fold>

	//<editor-fold desc="constructor">

	public StateObserverKuhnPoker() {
		start_player = 1;
		m_Player = 1;
		isPartialState = false;

		gameround = 0;
		newRound = true;
		lastActions = new ArrayList<>();
		rand = new Random(System.currentTimeMillis());
		GAMEOVER = false;
		setRoundOver(false);
		availableRandomActions = new ArrayList<>();
		// information about the player:
		chips =  new double[NUM_PLAYER];
		gamescores = new double[NUM_PLAYER];

		for(int i = 0;i<NUM_PLAYER;i++){
			chips[i] = START_CHIPS;
			gamescores[i] = START_CHIPS;
		}

		foldedPlayers = new boolean[2];

		checkedPlayer = new Boolean[2];

		initRound();
		isNextActionDeterministic = true;
	}

	public StateObserverKuhnPoker(StateObserverKuhnPoker other)	{
		super(other);

		start_player = other.start_player;

		newRound = other.newRound;
		gameround = other.gameround;
		isPartialState = other.isPartialState;
		rand = new Random(System.currentTimeMillis());

		isNextActionDeterministic = other.isNextActionDeterministic;
		nextNondeterministicAction = other.nextNondeterministicAction;
		lastActions = new ArrayList<>();
		for(String entry:other.lastActions){
			addToLog(entry);
		}
		if(other.lastAction!=null)
			lastAction = ACTIONS.fromInt(other.lastAction.toInt());
		GAMEOVER = other.GAMEOVER;

		m_Player = other.m_Player;

		chips =  new double[NUM_PLAYER];
		holeCards = new PlayingCard[NUM_PLAYER][2];

		this.pots = new Pots(other.pots);

		availableRandomActions = new ArrayList<>();
		cards = new ArrayList<>();
		for(int card:other.cards){
			cards.add(card);
			availableRandomActions.add(ACTIONS.fromInt(card));
		}

		chips =  new double[NUM_PLAYER];
		gamescores = new double[NUM_PLAYER];
		for(int i = 0;i<NUM_PLAYER;i++){
			if(other.holeCards[i][0]!=null) {
				this.holeCards[i][0] = other.holeCards[i][0].copy();
			}
			this.chips[i] = other.chips[i];
			this.gamescores[i] = other.gamescores[i];
		}

		checkedPlayer = new Boolean[2];
		foldedPlayers = new boolean[2];
		checkedPlayer[0] = other.checkedPlayer[0];
		checkedPlayer[1] = other.checkedPlayer[1];
		foldedPlayers[0] = other.foldedPlayers[0];
		foldedPlayers[1] = other.foldedPlayers[1];
		setAvailableActions();
	}
	//</editor-fold>

	//<editor-fold desc="helper">

	private void initCardDeck(){
		cards = new ArrayList<>();
		availableRandomActions = new ArrayList<>();

		for(int i=10;i<13;i++){
			cards.add(i);
			availableRandomActions.add(ACTIONS.fromInt(i));
		}
	}

	private int dealCard(){
		int randomCard = cards.remove(rand.nextInt(cards.size()));
		availableRandomActions.remove(ACTIONS.fromInt(randomCard));
		return randomCard;
	}

	public void initRound(){
		gameround++;
		addToLog("------------------New Round ("+gameround+")------------------");

		initCardDeck();
		setRoundOver(false);

		// cards
		holeCards = new PlayingCard[NUM_PLAYER][1];

		pots = new Pots();

		//next player becomes the active one
		start_player = (start_player+1)%2;
		m_Player = start_player;

		//Add Ante
		pots.add(1,0);
		pots.add(1,1);

		chips[0] -= 1;
		chips[1] -= 1;

		//Deal Card
		holeCards[0][0] = new PlayingCard(dealCard());
		holeCards[1][0] = new PlayingCard(dealCard());

		checkedPlayer[0] = null;
		checkedPlayer[1] = null;

		foldedPlayers[0] = false;
		foldedPlayers[1] = false;

		lastAction = null;

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

	//</editor-fold>

	//<editor-fold desc="advance">

	/**
	 * Advance the current state with 'action' to a new state combining the deterministic and the non-deterministic actions in one method.
	 * Actions:
	 * - 0 -> FOLD	: give up the current round
	 * - 1 -> CHECK	: pass for the current action (wait for someone else to do something to react on)
	 * - 2 -> BET	: bet the "Big Blind"
	 * - 3 -> CALL	: bet the same amount as the previous player bet
	 * @param action to advance the state of the game
	 */
	public void advance(ACTIONS action){
		if(isNextActionDeterministic()){
			advanceDeterministic(action);
		}
		if(!isNextActionDeterministic()){
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
	 * @param action to advance the state of the game
	 */
	public void advanceDeterministic(ACTIONS action) {
		super.advanceBase(action);		//		includes addToLastMoves(action)
		if(isRoundOver())
			return;

		if(!isNextActionDeterministic) {
			throw new RuntimeException("Next action is nondeterministic but called advanceDeterministic()");
		}

		incrementMoveCounter();
		lastAction = action;
		int iAction = action.toInt();

		switch (iAction) {
			case 0 -> fold();
			case 1 -> check();
			case 2 -> bet();
			case 3 -> call();
		}

		if(foldedPlayers[(m_Player)]||(checkedPlayer[0]!=null&&checkedPlayer[1]!=null&&checkedPlayer[0].equals(checkedPlayer[1]))) {
			isNextActionDeterministic = false;
		}else{
			isNextActionDeterministic=true;
		}

		m_Player = (m_Player+1)%2;
		setAvailableActions();
	}

	public void advanceNondeterministic(ACTIONS randAction) {
		if(isRoundOver())
			return;
		if (isNextActionDeterministic) {
			throw new RuntimeException("Next action is deterministic but called advanceNondeterministic()");
		}
		setRoundOver(true);
		addToLog("Showdown - finding the winner...");
		boolean[][] claims = pots.getClaims();
		for(int p = 0;p<claims.length;p++) {
			int winner;
			if(foldedPlayers[0]){
				winner = 1;
				addToLog("Player 0 has folded therefore player 1 wins the round.");
			}else{
				if(foldedPlayers[1]){
					winner = 0;
					addToLog("Player 1 has folded therefore player 0 wins the round.");
				}else{
					if(isPartialState){
						if(holeCards[0][0]==null){
							holeCards[0][0] = new PlayingCard(dealCard());
						}else{
							holeCards[1][0] = new PlayingCard(dealCard());
						}
					}
					winner = holeCards[0][0].getRank()>holeCards[1][0].getRank() ? 0:1;
					if(winner==0){
						addToLog("Player 0 wins the round with "+holeCards[0][0].getLongRank()+" over "+holeCards[1][0].getLongRank()+".");
					}else {
						addToLog("Player 1 wins the round with "+holeCards[1][0].getLongRank()+" over "+holeCards[0][0].getLongRank()+".");
					}
				}
			}
			addToLog(winner +" has won Pot ("+ p +") with "+ (int)pots.getPotSize(p)  +" chips");
			chips[winner] += pots.getPotSize(p);
		}

		//update scores
		gamescores = chips;

		addToLog("-----------------End Round("+gameround+")------------------");
		for(int i = 0 ; i <getNumPlayers() ; i++)
			addToLog("Chips "+Types.GUI_PLAYER_NAME[i]+": "+(int)chips[i] +" ");

		isNextActionDeterministic = true;

		if(chips[0]<1||chips[1]<1)
			GAMEOVER = true;

	}

	public void advanceNondeterministic() {
		ArrayList<ACTIONS> possibleRandoms = getAvailableRandoms();
		advanceNondeterministic(possibleRandoms.get(rand.nextInt(possibleRandoms.size())));
	}

	//</editor-fold>

	//<editor-fold desc="GBG">

	public StateObserverKuhnPoker copy() {
		return new StateObserverKuhnPoker(this);
	}

	@Override
	public ArrayList<ACTIONS> getAllAvailableActions() {
		ArrayList<ACTIONS> allActions = new ArrayList<>();
		allActions.add(ACTIONS.fromInt(0)); // FOLD
		allActions.add(ACTIONS.fromInt(1)); // CHECK
		allActions.add(ACTIONS.fromInt(2)); // BET
		allActions.add(ACTIONS.fromInt(3)); // CALL
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

		// Options Player 1:

			// You can only fold if there is a bet in place
			if(pots.getOpenPlayer(m_Player)>0)
				availableActions.add(ACTIONS.fromInt(0)); // FOLD

			// You can only call if somebody has bet before
			if(pots.getOpenPlayer(m_Player)>0&&chips[m_Player]>=pots.getOpenPlayer(m_Player))
				availableActions.add(ACTIONS.fromInt(3)); // CALL

		// Options Player 0:
			// You can only check if nobody has bet before
			if(pots.getOpenPlayer(m_Player)==0)
				availableActions.add(ACTIONS.fromInt(1)); // CHECK

			// You can only bet if nobody has bet before
			if(pots.getOpenPlayer(m_Player)==0&&chips[m_Player]>=BIGBLIND)
				availableActions.add(ACTIONS.fromInt(2)); // BET

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
		return availableRandomActions;
	}

	public int getNumAvailableRandoms() {
		return cards.size();
	}

	public double getProbability(ACTIONS action) {
		return 1.0/getNumAvailableRandoms();
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
		foldedPlayers[m_Player] = true;
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'fold'");
		checkedPlayer[m_Player]=false;
	}

	public void check(){
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'check'");
		checkedPlayer[m_Player]=true;
	}

	public void bet(){
		betSub(BIGBLIND);
		addToLog(""+Types.GUI_PLAYER_NAME[m_Player]+": 'bet' ("+BIGBLIND+")");
		checkedPlayer[m_Player]=false;
	}

	public void call(){
		double toCall = pots.getOpenPlayer(m_Player);

		// shouldn't happen - action is only available if player has more chips.
		if(toCall > chips[m_Player]){
			//PokerLog.gameLog.severe("Player calls with not enough chips.");
			toCall = chips[m_Player];
		}
		pots.add(toCall,m_Player);
		chips[m_Player] -= toCall;

		if(chips[m_Player]==0) {
			addToLog(Types.GUI_PLAYER_NAME[m_Player]+": call ("+(int)toCall+") - ALL IN");
		}else{
			addToLog(Types.GUI_PLAYER_NAME[m_Player]+": call ("+(int)toCall+")");
		}
		checkedPlayer[m_Player]=false;
	}

	public void betSub(double sizeOfBet){

		pots.add(sizeOfBet, m_Player);
		chips[m_Player] -= sizeOfBet;

	}
	//</editor-fold>

	public double getOpenPlayer(int player){
		return pots.getOpenPlayer(player);
	}

	public PlayingCard[] getHoleCards(int player){
		if(holeCards!=null)
			return holeCards[player];
		return null;
	}

	public PlayingCard[] getHoleCards(){
		return getHoleCards(m_Player);
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

	public int getGameround(){
		return gameround;
	}

	public int getPotSize(){
		return pots.getSize();
	}
	//</editor-fold>

	public void setHoleCards(int player, PlayingCard first){
		holeCards[player][0] = first;
	}


	@Override
	public StateObserverKuhnPoker partialState(){
		StateObserverKuhnPoker partialState = this.copy();
		int player = partialState.getPlayer();
		partialState.setPartialState(true);
		m_roundOver = false;
		for (int i = 0; i < partialState.holeCards.length; i++) {
			if(i==player)
				continue;
			if(partialState.holeCards[i][0]!=null) {

				partialState.cards.add(partialState.holeCards[i][0].getId());
				partialState.availableRandomActions.add(ACTIONS.fromInt(partialState.holeCards[i][0].getId()));

				partialState.holeCards[i][0] = null;
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

}
