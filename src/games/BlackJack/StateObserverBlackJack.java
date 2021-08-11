package games.BlackJack;

import java.util.ArrayList;
import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types.ACTIONS;
import tools.Types;
import static games.BlackJack.BlackJackConfig.*;

public class StateObserverBlackJack extends ObserverBase implements StateObsNondeterministic {


    private static final long serialVersionUID = 1L;
    private ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>();
    private Player currentPlayer;
    private boolean isNextActionDeterministic = true;
    private ArrayList<Integer> availableRandoms = new ArrayList<Integer>();
    private Dealer dealer;
    private Player players[];
    private int playersTurn;
    private gamePhase gPhase = gamePhase.BETPHASE;
    private boolean playerActedInPhase[];
    private ArrayList<String> log = new ArrayList<String>();
    private int currentSleepDuration = 0;
    private int episode = 0;
    private final double maximumBetSize = 10;
    private int epiLength;
    private int MAXDELTASCORE = 100;

    public StateObserverBlackJack() {
        this(NUM_PLAYERS, episodeLength);
    }


    /**
     * used to create StateObservers for test cases.
     * the default constructor will call this and set the params from BlackJackConfig
     * @param num_players number of players participating
     * @param epiLength episode length (number of rounds played until the game is over)
     */
    public StateObserverBlackJack(int num_players, int epiLength){
        players = new Player[num_players];
        playerActedInPhase = new boolean[num_players];
        this.epiLength = epiLength;
        // defaultState
        // adding dealer and player/s
        dealer = new Dealer("dealer");
        for (int i = 0; i < players.length; i++) {
            this.players[i] = new Player("p" + i);
        }
        playersTurn = 0;
        currentPlayer = getCurrentPlayer();
        setAvailableActions();
        log.add(logHeadEntry());
    }


    /**
     * copy-constructor
     * @param other StateObserverBlackJack
     */
    public StateObserverBlackJack(StateObserverBlackJack other) {
        super(other);
        this.players = new Player[other.players.length];
        this.playerActedInPhase = new boolean[other.playerActedInPhase.length];
        this.playersTurn = other.playersTurn;
        this.dealer = new Dealer(other.dealer);
        this.availableRandoms = new ArrayList<>(other.availableRandoms);
        this.availableActions = new ArrayList<>(other.availableActions);
        this.isNextActionDeterministic = other.isNextActionDeterministic;
        this.currentPlayer = getCurrentPlayer();
        for (int i = 0; i < players.length; i++) {
            this.players[i] = new Player(other.players[i]);
        }
        this.gPhase = other.gPhase;
        this.playerActedInPhase = other.playerActedInPhase.clone();
        this.log = new ArrayList<>(other.log);
        this.currentSleepDuration = other.currentSleepDuration;
        this.episode = other.episode;
        this.epiLength = other.epiLength;
    }


    /**
     * mapping Deterministic actions to ENUMS to get better readable code
     */
    public enum BlackJackActionDet {
        BET10(0), HIT(1), STAND(2), DOUBLEDOWN(3), SPLIT(4),
        SURRENDER(5), INSURANCE(6), NOINSURANCE(7);

        private int action;

        private BlackJackActionDet(int action) {
            this.action = action;
        }

        public int getAction() {
            return this.action;
        }
    }

    /**
     *  mapping NonDeterministic actions to ENUMS
     */
    enum BlackJackActionNonDet {
        DEALCARD(0), DEALERPLAYS(1), PAYPLAYERS(2), PEEKFORBLACKJACK(3);

        private int action;

        private BlackJackActionNonDet(int action) {
            this.action = action;
        }

        public int getAction() {
            return this.action;
        }
    }



    /**
     * Enum Gamephases to keep track of the phase the game is in
     * <ul>
     * <li>BPHASE -> Players need to place a bet before they get cards</li>
     * <li>DEALPHASE -> Players and Dealer get dealt 2 cards</li>
     * <li>PLAYERONACTION -> Players play their hand(s)</li>
     * <li>DEALERONACTION -> Dealer Plays his hand (nondetermenistic)</li>
     * <li>PAYOUT -> Determin which players won against the dealer and paying them</li>
     * </ul>
     */
    enum gamePhase {
        BETPHASE(0), DEALPHASE(1), ASKFORINSURANCE(2) ,PEEKFORBLACKJACK(3),
        PLAYERONACTION(4), DEALERONACTION(5), PAYOUT(6);

        private int value;

        private gamePhase(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public gamePhase getNext() {
            return gamePhase.values()[(value + 1) % gamePhase.values().length];
        }
    }

    enum results {
        WIN, PUSH, LOSS, SURRENDER, BLACKJACK;
    }


    @Override
    public boolean isImperfectInformationGame() { return true; }

    enum PartialStateMode {
        THIS_PLAYER, WHATS_ON_TABLE, FULL;
    }

    /**
     * returns a partial state
     * possible modes THIS_PLAYER (what the current player would see), WHATS_ON_TABLE (what an observer would see),
     * FULL (every information in game can be accesed)
     * @param mode
     * @return resulting partial state
     */
    public StateObservation partialState(PartialStateMode mode) {
        switch (mode) {
            case THIS_PLAYER:
            case WHATS_ON_TABLE:
                if (gPhase != gamePhase.DEALERONACTION && gPhase != gamePhase.PAYOUT) {

                    StateObserverBlackJack p_so = new StateObserverBlackJack(this);
                    p_so.setPartialState(true);
                    if (dealer.hasHand() && dealer.getActiveHand().size() > 1) {
                        p_so.dealer.activeHand.getCards().remove(1);
                        p_so.dealer.activeHand.getCards().add(new Card(Card.Rank.X, Card.Suit.X));
                    }
                    return p_so;
                } else {
                    return new StateObserverBlackJack(this);
                }
            case FULL:
                return new StateObserverBlackJack(this);
        }
        return new StateObserverBlackJack(this);
    }

    /**
     * completes face-down-card of the dealer depending on the phase the gamestate is in
     * @return resulting GameState (never used)
     */
    @Override
    public StateObservation randomCompletion(){
        //setPartialState(false); // maybe not needed. maybe we should introduce a flag completed
        // if the dealer has no hand there is nothing to complete
        if(!this.isPartialState()){
            throw new RuntimeException("A state that is not a partial State should not be completed");
        }

        if(dealer.getActiveHand() == null){
            return this;
        }
        // if the second card of the dealer is unknown we need completion
        if(dealer.getActiveHand().getCards().get(1).rank == Card.Rank.X){
            if(gPhase.getValue() < gamePhase.PLAYERONACTION.getValue()){
                //check for Black did not happen. We can complete with any Card.
                return completeRandom();
            }else{
                /** in this case the dealer peeked already for a BlackJack. If the dealer had a Black Jack
                 * the round would have ended already. So in this Case the completion cant result in a
                 * Black Jack for the dealer, this would be an illegal Game State.
                 * We need to restrict the completion if the upcard of the dealer
                 * is an A, K, Q, J, or a Ten.
                 */
                if(dealer.getActiveHand().getCards().get(0).rank.getValue() == 10 || dealer.getActiveHand().getCards().get(0).rank.getValue() == 1){
                    return completeRestricted();
                }
                return completeRandom();
            }

        }
        return this;

    }

    /**
     * completes this gamestate randomly by: dealers face-down-card
     * @return resulting GameState (never used)
     */
    private StateObservation completeRandom(){
        dealer.getActiveHand().getCards().remove(1);
        dealer.getActiveHand().addCard(ArenaBlackJack.deck.draw());
        if(dealer.getActiveHand().size() != 2)
            throw new RuntimeException("Dealers handsize must be 2 after completion!");
        return this;
    }

    /**
     * completes this gamestate restricted by: dealers face-down-card.
     * this completion cannot result in a Natural-Blackjack for the dealer.
     * @return resulting GameState (never used)
     */
    private StateObservation completeRestricted(){

        do{
            dealer.getActiveHand().getCards().remove(1);
            dealer.getActiveHand().addCard(ArenaBlackJack.deck.draw());
        }while(dealer.getActiveHand().checkForBlackJack());
        if(dealer.getActiveHand().size() != 2)
            throw new RuntimeException("Dealers handsize must be 2 after completion!");
        return this;
    }

    @Override
    public StateObservation partialState() {
        return partialState(PartialStateMode.THIS_PLAYER);
    }


    @Override
    public boolean isGameOver() {
        if(episode >= epiLength)
            return true;
        for (Player p : players){
            if(p.getChips() >= 10 || p.betOnActiveHand() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public StateObservation precedingAfterstate() { return null; }

    @Override
    public boolean isDeterministicGame() {
        return false;
    }

    @Override
    public boolean isFinalRewardGame() {
        return false;
    }

    @Override
    public boolean isLegalState() {
        for (Player p : players){
            if(p.getChips() < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean stopInspectOnGameOver() {
        return false;
    }

    @Override
    public String stringDescr() {
        String result = "";
        for (Player p : players) {
            result += "( " + p + " ) ";
        }
        Hand dhand = dealer.getActiveHand();
        String str = (dhand==null) ? " (dealer | hand : " + dhand + " )"
                : " (dealer | hand : " + dhand + " = "+ dhand.getHandValue() + " ) ";
        return result+str;
    }
    public String stringDescrSM() {
        String result = "\n";
        for (Player p : players) {
            result += "( " + p + " ) ";
        }
        result += "\ngamePhase: " + gPhase.name();
        if(gPhase == gamePhase.BETPHASE || gPhase == gamePhase.ASKFORINSURANCE || gPhase == gamePhase.PLAYERONACTION ) {
            result += " player to move: " + players[getPlayer()].name + "\nhis available actions : ";
            for (Types.ACTIONS a : getAvailableActions()) {
                result += BlackJackActionDet.values()[a.toInt()].name() + " - ";
            }
        }
        result += "\ndealer | hand : " + dealer.getActiveHand();
        return result;
    }

    @Override
    public String stringActionDescr(ACTIONS act) {
        return BlackJackActionDet.values()[act.toInt()].name();
    }

    // provisionally TODO: refactor
    @Override
    public double getGameScore(StateObservation referringState) {
        return getGameScore(referringState.getPlayer());
    }

    // provisionally TODO: refactor
    @Override
    public double getGameScore(int player) {
        if(gPhase == gamePhase.PAYOUT)
            return players[player].getChips();
        return players[player].getChips() + players[player].getSumAllBets();
    }


    // /WK/ added this. getGameScore(i) needs still to be reshaped.
    @Override
    public ScoreTuple getRewardTuple(boolean rewardIsGameScore) {
        ScoreTuple sc = new ScoreTuple(this);
        double denom = (rewardIsGameScore) ? 1.0 : MAXDELTASCORE;
        for (int i=0; i<this.getNumPlayers(); i++) {
            sc.scTup[i]=this.getGameScore(i)/denom;
        }
        return sc;
    }

    // /WK/ BUG FIX 2021-04-02 this method was missing
    @Override
    public double getReward(int player, boolean rewardIsGameScore) {
        double denom = (rewardIsGameScore) ? 1.0 : MAXDELTASCORE;
        return this.getGameScore(player)/denom;
    }
    @Override
    public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
        double denom = (rewardIsGameScore) ? 1.0 : MAXDELTASCORE;
        return this.getGameScore(referringState.getPlayer())/denom;
    }

// /WK/ questionable: stepReward is only if each step should get a reward
//    public ScoreTuple getStepRewardTuple() {
//        ScoreTuple sc = new ScoreTuple(this);
//        for (int i=0; i<this.getNumPlayers(); i++) {
//            sc.scTup[i]=players[i].getRoundPayoff();
//        }
//        return sc;
//    }

    /**
     * GameScore = chips
     * minimum chips is 0
     *  */
    @Override
    public double getMinGameScore() {
        return 0;
    }

    /**
     *  GameScore = chips
     *  */
    @Override
    public double getMaxGameScore() {
        return START_CHIPS*10;
    }


    @Override
    public String getName() {
        return "BlackJack";
    }

    @Override
    public void advance(ACTIONS action) {
        // splittinig the advance into
        // Deterministic
        if (isNextActionDeterministic()) {
            advanceDeterministic(action);
        }
        // and NonDeterministic
        while (!isNextActionDeterministic() && !isRoundOver()) {
            advanceNondeterministic();
        }
    }

    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> allActions = new ArrayList<ACTIONS>();
        for (BlackJackActionDet a : BlackJackActionDet.values()) {
            allActions.add(ACTIONS.fromInt(a.getAction()));
        }
        return allActions;

    }

    @Override
    public ArrayList<ACTIONS> getAvailableActions() {
        return availableActions;
    }

    @Override
    public int getNumAvailableActions() {
        return availableActions.size();
    }

    @Override
    public void setAvailableActions() {
        availableActions.clear();
        currentPlayer = getCurrentPlayer();
        if (gPhase == gamePhase.BETPHASE) {
            // sets the available betting options, u always bet before get dealt a hand.
            // Checks
            // the highest possible betamount first. Actions are mapped to Enums.
            if(currentPlayer.getChips() >= 10){
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.BET10.getAction()));
            }
            else if(!isGameOver()){
                advance(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.getAction()));
                return;
            }

        } else if(gPhase == gamePhase.ASKFORINSURANCE){
            if(currentPlayer.insuranceAmount() == 0
                    && currentPlayer.betOnActiveHand() < currentPlayer.getChips()){
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.INSURANCE.getAction()));
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.NOINSURANCE.getAction()));
            }else{
                advance(Types.ACTIONS.fromInt(BlackJackActionDet.NOINSURANCE.getAction()));
                return;
            }

        }else if (gPhase == gamePhase.PLAYERONACTION) {
            //Skips player who has 0 Chips but still sits at the "table"
            if(currentPlayer.getActiveHand() == null){
                advance(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.getAction()));
                return;
            }
            if (!currentPlayer.getActiveHand().isHandFinished()) {
                // enters after Player has placed his bet
                // player is not bust nor got 21 nor got a blackjack because the hand is not
                // fnished

                // Stand - Player wants no more cards for this hand
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.getAction()));
                // Hit - Player wants one more card for this Hand
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.HIT.getAction()));
                // Split - Player wants to split a pair -
                // Condition: (Handsize == 2) and both cards are from
                // the same Rank e.g. 8 8, player has enough chips to do so
                ArrayList<Card> playersHand = currentPlayer.getActiveHand().getCards();
                if (playersHand.size() == 2 && playersHand.get(0).rank == playersHand.get(1).rank
                        && currentPlayer.betOnActiveHand() <= currentPlayer.getChips() && currentPlayer.getHands().size() < 3) {
                    availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.SPLIT.getAction()));
                }
                // Doubledown - Player gets dealt exactly one more card and doubles the
                // betamount for this hand
                // Condition: Handsize == 2, player has enough chips to do so
                if (playersHand.size() == 2 && currentPlayer.betOnActiveHand() <= currentPlayer.getChips()) {
                    availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.DOUBLEDOWN.getAction()));
                }
                // Surrender - player surrenders his hand and gets half of his chips back
                // Condition: needs to be first action of hand
                if(playersHand.size() == 2 && !currentPlayer.hasSplitHand()) {
                    availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.SURRENDER.getAction()));
                }
            } else { // The hand is finished, the player can only stand/passToNext
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.getAction()));
            }
        }

    }


    @Override
    public ACTIONS getAction(int i) {
        return availableActions.get(i);
    }

    @Override
    public int getPlayer() {
        return playersTurn;
    }

    @Override
    public void setPlayer(int p) {
        playersTurn = p;
    }


    @Override
    public int getNumPlayers() {
        return players.length;
    }

    public void setgPhase(gamePhase gPhase) {
        this.gPhase = gPhase;
    }

    public gamePhase getgPhase() {
        return gPhase;
    }

    @Override
    public void advanceDeterministic(ACTIONS action) {
        if (this.isGameOver()) {    // /WK/ sanity check (should usually not fire )
            System.err.println("Error [StateObsBJ]: advanceDet for a game-over state!!");
        }
        if (this.isRoundOver()) {
            System.err.println("Error [StateObsBJ]: advanceDet for a round-over state!!");
        }
        currentPlayer = getCurrentPlayer();
        // convert action to enum
        BlackJackActionDet a = BlackJackActionDet.values()[action.toInt()];
        // store as last move
        addToLastMoves(action);
        if(!currentPlayer.hasLost()) {
            log.add(currentPlayer.name + " chose action : " + a);
        }
        isNextActionDeterministic = true;

        // this switch only changes player attributes caused by the action
        switch (a) {

            case BET10:
                currentPlayer.bet(10);
                break;
            case DOUBLEDOWN:
                currentPlayer.bet(currentPlayer.betOnActiveHand());
                break;
            case SPLIT:
                currentPlayer.splitHand();
                break;
            case SURRENDER:
                currentPlayer.surrender();
                break;
            case INSURANCE:
                currentPlayer.insurance();
                break;
            default:
                break;
        }
        // this switch determins if the next action is determinisic or nondeterministic,
        // if it is the next players turn and if the gamePhase advences
        switch (a) {
            case BET10:
            case STAND:
            case INSURANCE:
            case NOINSURANCE:
            case SURRENDER:
                if (currentPlayer.setNextHandActive() != null) { // has player more hands? only important in case stand
                    isNextActionDeterministic = false; // if yes the next hand is active now, Split hands always have
                                                       // only one card so the next hand needs to get dealt one more
                                                       // card. Next action is nondeterministic because the card dealt
                                                       // is random
                } else { // player has no more hands
                    passToNextPlayer(); // pass to next
                    isNextActionDeterministic = !everyPlayerActed(); // if every player acted == true, its dealers turn.
                                                                     // Dealers turn is nondeterministic. Otherwise its
                                                                     // next players turn, next action is deterministic
                    if (everyPlayerActed()) { // advance the gamePhase
                        advancePhase();
                    }
                }
                break;
            case HIT:
            case DOUBLEDOWN:
            case SPLIT:
                isNextActionDeterministic = false;
                break;
            default:
                break;
        }
        if (isNextActionDeterministic) { // we only need to set actions if the next action is deterministic
            setAvailableActions();
        }

    }

    /**
     * sets the next nonDeterministic action
     */
    public void setAvailableRandoms() { // the gamephase determines which nondeterministic action is triggered

        availableRandoms.clear();
        switch (gPhase) {
            case DEALPHASE:
            case PLAYERONACTION:
                availableRandoms.add(BlackJackActionNonDet.DEALCARD.getAction());
                break;
            case DEALERONACTION:
                availableRandoms.add(BlackJackActionNonDet.DEALERPLAYS.getAction());
                break;
            case PAYOUT:
                availableRandoms.add(BlackJackActionNonDet.PAYPLAYERS.getAction());
                break;
            case PEEKFORBLACKJACK:
                availableRandoms.add(BlackJackActionNonDet.PEEKFORBLACKJACK.getAction());
            default:
                break;
        }

    }

    /**
     * helper method to advance the gPhase (circular)
     */
    public void advancePhase() {
        gPhase = gPhase.getNext();
        playerActedInPhase = new boolean[players.length];
        setPlayer(0);
    }


    @Override
    public void advanceNondeterministic() {
        advanceNondeterministic(getNextNondeterministicAction());
    }

    @Override
    public void advanceNondeterministic(ACTIONS randAction) {
        if (isNextActionDeterministic) {
            throw new RuntimeException("Next action should be deterministic");
        }
        if (this.isGameOver()) {        // /WK/ sanity check (should usually not fire)
            System.err.println("Error [StateObsBJ]: advanceNondet for a game-over state!!");
        }
        if (this.isRoundOver()) {
            System.err.println("Error [StateObsBJ]: advanceNondet for a round-over state!!");
        }

        BlackJackActionNonDet a = BlackJackActionNonDet.values()[randAction.toInt()];

        switch (a) {
            case DEALCARD: // a card gets dealt
                currentPlayer = getCurrentPlayer();
                switch (gPhase) {

                    case DEALPHASE:
                        // in dealphase the cards get dealt 1 by 1
                        if (everyPlayerActed()) { // if true dealer gets a card
                            if(this.isPartialState() && dealer.getActiveHand() != null){
                                dealer.addCardToActiveHand(new Card(Card.Rank.X, Card.Suit.X));
                            }else{
                                dealer.addCardToActiveHand(ArenaBlackJack.deck.draw());
                            }

                            playerActedInPhase = new boolean[players.length];
                            if (dealer.getActiveHand().size() == 2) { // if dealer has 2 cards dealing is over advance
                                                                      // into next phase (next action det)
                                advancePhase(); //next phase = askforinsurance
                                isNextActionDeterministic = true;
                                if(dealer.getActiveHand().getCards().get(0).rank != Card.Rank.ACE){
                                    //if the dealer's faceup card is not an Ace we can skip asking for insurance
                                    advancePhase(); //next phase = peekforblackJack
                                    isNextActionDeterministic = false;
                                    if(dealer.getActiveHand().getCards().get(0).rank.getValue() != 10){
                                        //if dealers Hand does not contain 10, J, K Q, A we dont need to peek for Blackjack
                                        advancePhase(); //next phase = playersonaction
                                        isNextActionDeterministic = true;
                                    }
                                }

                            }
                        } else { // every player gets one card
                            if(currentPlayer.betOnActiveHand() > 0) {
                                currentPlayer.addCardToActiveHand(ArenaBlackJack.deck.draw());
                            }
                            passToNextPlayer();
                            isNextActionDeterministic = false;
                        }
                        break;

                    case PLAYERONACTION:
                        // nonDeterministic advance that a player triggered by his last action
                        Hand currentHand = currentPlayer.getActiveHand();
                        int f = getLastMove();
                        // get the last deterministic action
                        Types.ACTIONS i = Types.ACTIONS.fromInt(f);
                        BlackJackActionDet lastAction = BlackJackActionDet.values()[i.toInt()];
                        // every NonDeterministicAdvance triggered by a players action is about getting
                        // a card dealt
                        Card newCard = ArenaBlackJack.deck.draw();
                        log.add(currentPlayer.name + " gets a card dealt: " + newCard);
                        currentHand.addCard(newCard);

                        // consequence of the card being dealt and the last action
                        switch (lastAction) {
                            case DOUBLEDOWN:
                                currentHand.setHandFinished();
                                addToLastMoves(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.action));
                                break;
                        }

                        if (currentHand.isHandFinished()) { // checking if the hand is finished, manualy finished or
                                                            // handvalue > 20
                            if (currentPlayer.setNextHandActive() != null) { // check for more hands
                                isNextActionDeterministic = false; // if more hands, the next hand needs to get dealt
                                                                   // one more card for sure (nondet)
                            } else { // no more hands, pass to next
                                passToNextPlayer();
                                isNextActionDeterministic = true;
                                if (everyPlayerActed()) { // if everyone acted the advance to next gamePhase
                                    advancePhase();
                                    isNextActionDeterministic = false; // dealer will play his hand, envoirement nondet
                                }
                            }
                        } else { // players hand is not finished, still same players turn next action det
                            isNextActionDeterministic = true;
                        }
                        break;

                    default:
                        System.out.print("err ");
                        break;

                }
                break;

            case PEEKFORBLACKJACK:
                String logEntry = "Dealer peeks for Blackjack -> ";
                if(this.isPartialState()){
                    randomCompletion();
                }
                if(dealer.getActiveHand().checkForBlackJack()){
                    // dealer has a blackjack, players cant act anymore
                    logEntry += "Blackjack!";
                    gPhase = gamePhase.PAYOUT;
                    isNextActionDeterministic = false;
                }else{
                    advancePhase(); // next phase = playersonaction
                    isNextActionDeterministic = true;
                    logEntry += "no Blackjack";
                    if(this.isPartialState()) {
                        dealer.getActiveHand().getCards().remove(1);
                        dealer.getActiveHand().addCard(new Card(Card.Rank.X, Card.Suit.X));
                    }
                }
                log.add(logEntry);
                break;

            case DEALERPLAYS:
                if(this.isPartialState()){
                    randomCompletion();
                }
                log.add("Dealer reaveals card " + dealer.getActiveHand().getCards().get(1));
                while (dealer.getActiveHand().getHandValue() < 17) {
                    // The dealer will always hit under 17 and will always stay on 17 or higher,
                    // even if the opponent got 18, dealer got 17 and there is only this one
                    // opponent
                    Card newCard = ArenaBlackJack.deck.draw();
                    log.add("Dealer gets a card dealt: " + newCard);
                    dealer.activeHand.addCard(newCard);

                }
                advancePhase(); // dealer finished advance to next gamePhase
                isNextActionDeterministic = false;
                break;

            case PAYPLAYERS:
                for (Player p : players) {
                    if (p.hasSurrender()) { // if the player surrendered the payout already happened
                        log.add(p.name + " " + results.SURRENDER + " with hand: " + p.getActiveHand() + " vs dealer: " + dealer.getActiveHand()
                                + " handvalue: " + dealer.getActiveHand().getHandValue() +
                                " | payoff: " + ((p.betOnActiveHand()/2) -p.betOnActiveHand())
                                + " chips");
                        p.collect(p.getBetAmountForHand(p.getActiveHand()) / 2);
                        p.addPayOff((p.betOnActiveHand()/2) -p.betOnActiveHand());
                    } else {

                        if(p.insuranceAmount() > 0) {
                            if (dealer.getActiveHand().checkForBlackJack()) {
                                p.collect(p.insuranceAmount() * 3);
                            }
                            log.add(p.name + " insurancePayoff: " +
                                    (dealer.getActiveHand().checkForBlackJack() ? 2 : -1) * p.insuranceAmount());
                            p.addPayOff((dealer.getActiveHand().checkForBlackJack() ? 2 : -1) * p.insuranceAmount());
                        }

                        for (Hand h : p.getHands()) {
                            // case blackjack
                            results r = results.LOSS;
                            double amountToCollect = 0;
                            double bet = p.getBetAmountForHand(h);

                            if (h.checkForBlackJack() && !p.hasSplitHand()) { // player has blackjack, if player got a
                                                                              // blackjack in a split hand it does not
                                                                              // count
                                                                              // as blackjack
                                amountToCollect = bet; // both have blackjack push/draw player gets bet back
                                r = results.PUSH;
                                if (!dealer.getActiveHand().checkForBlackJack()) { // dealer has no blackjack
                                    amountToCollect = bet * 2.5;
                                    r = results.BLACKJACK;
                                }
                            } else { // player has no blackjack
                                if (!h.isBust()) { // player not bust
                                    if (dealer.getActiveHand().isBust()) {// dealer is bust player not
                                        amountToCollect = bet * 2;
                                        r = results.WIN;
                                    } else if(!dealer.getActiveHand().checkForBlackJack()){
                                        if (h.getHandValue() > dealer.getActiveHand().getHandValue()) { // player wins
                                            amountToCollect = bet * 2;
                                            r = results.WIN;
                                        } else if (h.getHandValue() == dealer.getActiveHand().getHandValue()) { // push/draw
                                                                                                                // player
                                                                                                                // gets
                                                                                                                // his
                                                                                                                // bet
                                                                                                                // back
                                            amountToCollect = bet;
                                            r = results.PUSH;
                                        }
                                    }
                                }
                            }

                            p.collect(amountToCollect);
                            log.add(p.name + " -> hand: " + h + " val=" + h.getHandValue() + " " + r + " vs. dealer: "
                                    + dealer.getActiveHand() + " val=" + dealer.getActiveHand().getHandValue()
                                    + " | handPayoff: " + (amountToCollect - p.getBetAmountForHand(h)) + " chips");
                            p.addPayOff((amountToCollect - p.getBetAmountForHand(h)));

                        }

                    }
                    if(!p.hasLost() && p.hasSplitHand() || p.insuranceAmount() > 0){
                        log.add(p.name + " summed payoff this round: " + p.getRoundPayoff());
                    }
                }

                this.episode++;
                setRoundOver(true);
                //initRound();
                break;
        }
        if (isNextActionDeterministic)
            setAvailableActions();
    }


    /**
     * inits a new round after a roundOver situation is reached
     */
    @Override
    public void initRound(){
        // Setup new Round
        setRoundOver(false);
        log.add("-------------New Round---------------");
        for (Player p : players) {
            p.clearHand();
        }
        log.add(logHeadEntry());
        dealer.clearHand();
        advancePhase();
        isNextActionDeterministic = true;
        setAvailableActions();
        //temporal change for large simulations
        //increasing ArrayList size will slow down the copy speed
        if(log.size() > 300)
            log.clear();
        lastMoves.clear();
    }

    @Override
    public boolean isNextActionDeterministic() {
        return isNextActionDeterministic;

    }

    @Override
    public ACTIONS getNextNondeterministicAction() {
        if (isNextActionDeterministic) {
            return null;
        }
        setAvailableRandoms();
        if (availableRandoms.size()==0) {
            System.err.println("/WK/ Something is wrong: availableRandoms has size==0");
            return null;  // /WK/ added this to avoid exception in next line, but not sure whether the right thing to do
        }
        return Types.ACTIONS.fromInt(availableRandoms.remove(0));
    }

    @Override
    public ArrayList<ACTIONS> getAvailableRandoms() {
        ArrayList<ACTIONS> availableRans = new ArrayList<>();
        for(int i : availableRandoms){
            availableRans.add(ACTIONS.fromInt(i));
        }
        return availableRans;
    }

    @Override
    public int getNumAvailableRandoms() {
        return getAvailableRandoms().size();
    }

    @Override
    public double getProbability(ACTIONS action) {
        return 1;
    }

    @Override
    public StateObsNondeterministic copy() {
        return new StateObserverBlackJack(this);
    }

    public Player getCurrentPlayer() {
        return players[getPlayer()];
    }

    public Dealer getDealer() {
        return dealer;
    }

    public Player[] getPlayers() {
        return players;
    }

    @Override
    public void passToNextPlayer() {
        playerActedInPhase[getPlayer()] = true;
        setPlayer(getNextPlayer());
    }

    /**
     * @return current phase the game is in
     */
    public StateObserverBlackJack.gamePhase getCurrentPhase() {
        return gPhase;
    }

    /**
     * @return if every player acted in the current phase
     */
    public boolean everyPlayerActed() {
        for (boolean a : playerActedInPhase) {
            if (!a) {
                return false;
            }
        }
        return true;
    }

    public boolean dealersTurn() {
        return (gPhase == gamePhase.DEALERONACTION || gPhase == gamePhase.PAYOUT || gPhase == gamePhase.PEEKFORBLACKJACK);
    }


    public ArrayList<String> getHandHistory() {
        return log;
    }


    private String logHeadEntry(){
        String logHead ="";
        for(Player p: players){
            if(!p.hasLost()) {
                logHead += "( " + p + " ) ";
            }
        }
        return logHead;
    }

}
