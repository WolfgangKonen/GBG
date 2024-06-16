package games.Poker;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

import java.io.Serial;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * the Random {@link PlayAgent}
 *
 * @author Wolfgang Konen, TH Koeln, 2016
 *
 */
public class PokerAgent extends AgentBase implements PlayAgent {


    // --- never used ---
//    public static final int ROYAL_FLUSH = 0;
//    public static final int STRAIGHT_FLUSH = 1;
//    public static final int FOUR_OF_A_KIND = 2;
//    public static final int FULL_HOUSE = 3;
//    public static final int FLUSH = 4;
//    public static final int STRAIGHT = 5;
//    public static final int THREE_OF_A_KIND = 6;
//    public static final int TWO_PAIR = 7;
//    public static final int ONE_PAIR = 8;
//    public static final int HIGH_CARD = 9;
//    public static final int KICKER = 10;

    //private Random rand;
    //private final int[][] m_trainTable = null;
    //private final double[][] m_deltaTable = null;


    private Hand hand;

    private double chips;
    private double toCall;
    private PlayingCard[] cards;
    private ArrayList<Types.ACTIONS> acts;

    /**
     * change the version ID for serialization only if a newer version is no longer
     * compatible with an older one (older .agt.zip will become unreadable, or you have
     * to provide a special version transformation)
     */
    @Serial
    private static final long  serialVersionUID = 12L;


    public PokerAgent(String name) {
        super(name);
        setAgentState(AgentState.TRAINED);
    }

    /**
     * Get the best next action and return it
     * (NEW version: returns ACTIONS_VT and has a recursive part for multi-moves)
     *
     * @param so            current game state (is returned unchanged)
     * @param random        allow random action selection with probability m_epsilon
     * @param deterministic
     * @param silent        execute silently without outputs
     * @return actBest		the best action. If several actions have the same
     * 						score, break ties by selecting one of them at random.
     * <p>
     * actBest has predicate isRandomAction()  (true: if action was selected
     * at random, false: if action was selected by agent).<br>
     * actBest has also the members vTable to store the value for each available
     * action (as returned by so.getAvailableActions()) and vBest to store the value for the best action actBest.
     */
    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean deterministic, boolean silent) {

        if(!(so instanceof StateObserverPoker)){
            throw new RuntimeException("This Agent is only suitable for KuhnPoker.");
        }

        Types.ACTIONS actBest = getAction(so);
        Types.ACTIONS_VT actBestVT;
        double[] vtable = new double[acts.size()];
        double maxScore = -Double.MAX_VALUE;

        //actBest = acts.get(rand.nextInt(acts.size()));
        // check if action is valid
        boolean check = false;
        for(Types.ACTIONS act:acts){
            if(actBest.toInt() == act.toInt()){
                check = true;
                break;
            }
        }
        if(!check) {
            throw new RuntimeException("best action is not available");
        }
        actBestVT = new Types.ACTIONS_VT(actBest.toInt(), true, vtable, maxScore);
        return actBestVT;
    }

    private Types.ACTIONS getAction(StateObservation so){

        StateObserverPoker sop = (StateObserverPoker) so;
        acts = so.getAvailableActions();

        // check if state is legit
        assert so.isLegalState() : "Not a legal state";
        assert acts.size()>0 : "No available actions";

        // gather some information about the current state for the agent

        toCall = sop.getOpen();                     // how much do I need to call?
        chips = sop.getChips()[sop.getPlayer()];    // how many chips do I have?
        //double pot = sop.getPotSize();                     // how big is the pot?
        hand = new Hand();

            PlayingCard[] myCards = sop.getHoleCards();          // what's my hand ?
            PlayingCard[] sharedCards = sop.getCommunityCards(); // community cards?

            cards = new PlayingCard[myCards.length+sharedCards.length];
            cards[0] = myCards[0];
            cards[1] = myCards[1];
        int cardsDealt = 0;

            for (int i = 0;i<sharedCards.length;i++) {
                cards[2+i] =  sharedCards[i];
                cardsDealt += sharedCards[i] == null?0:1;
            }

        // determine what to do
        if(cardsDealt ==0) {

            // if the user only has the wholecards we are using tables to estimate the value
            return play(evaluateWholeCards(cards)+1);

        }else{

            updateHand();

            if(hand.four.size()>0)
                return play(10);

            if(hand.fullHouse)
                return play(10);

            if(hand.flush)
                return play(10);

            if(hand.straight)
                return play(10);

            if(hand.triples.size()>0)
                return play(9);

            if(hand.pairs.size()>1)
                return play(8);

            if(hand.pairs.size()>0)
                return play(8);

            // there is still one card open
            if(hand.connected>3&&hand.suited>3){
                return play(7);
            }

            if(hand.connected>3||hand.suited>3){
                return play(5);
            }

            if(hand.high>10)
                return play(6);

            if(hand.high>8)
                return play(4);

        }
        return play(7- cardsDealt);
    }

    private void updateHand(){
        int[] suits = new int[4];
        int[] ranks = new int[13];

        int max = 0;

        for(PlayingCard c : cards){
            if(c == null)
                break;
            suits[c.getSuit()]++;
            ranks[c.getRank()]++;

            if(c.getRank()>max)
                max = c.getRank();
        }

        hand.high = max;

        // update amount of cards with the same suit
        int maxSuits = 0;
        for(int val:suits){
            if(maxSuits<val)
                maxSuits=val;
        }
        hand.suited = maxSuits;

        // update amount of conneceted cards;
        int connected = 0;
        int maxConnected = 0;
        if(ranks[12]>0)
            connected++;
        for(int i = 0;i<13;i++){
            if(ranks[i]>0) {
                connected++;
                if(maxConnected<connected)
                    maxConnected = connected;
            } else {
                connected = 0;
            }
        }
        hand.connected = maxConnected;

        // check for pairs
        ArrayList<Integer>[] multiples = checkForMultiples(ranks);
        hand.pairs = multiples[0];
        hand.triples = multiples[1];
        hand.four = multiples[2];

        hand.straight = connected > 4;
        hand.flush = maxSuits > 4;
        if(hand.triples.size()>0) {
            hand.pairs.remove(hand.triples.get(0));
            if (hand.pairs.size()>0){
                hand.fullHouse = true;
            }
            hand.pairs.add(hand.triples.get(0));
        }
    }

    private ArrayList<Integer>[] checkForMultiples(int[] ranks){
        ArrayList<Integer>[] multiples = new ArrayList[3];
        multiples[0] = new ArrayList<>(); // pairs
        multiples[1] = new ArrayList<>(); // three of a kind
        multiples[2] = new ArrayList<>(); // four  of a kind

        for(int i = 12;i>=0;i--){
            if(ranks[i]>=2){
                multiples[0].add(i);
                if(ranks[i]>=3){
                    multiples[1].add(i);
                    if(ranks[i]>=3){
                        multiples[2].add(i);
                    }
                }
            }
        }
        return multiples;
    }

    public Types.ACTIONS play(int confidence) {

        // if there is a huge bet to call
        if(toCall>chips-20){
            if(confidence>7){
                //ALL-IN
                if(acts.contains(Types.ACTIONS.fromInt(5)))
                    return Types.ACTIONS.fromInt(5);
            }else if(confidence>7){
                if(ThreadLocalRandom.current().nextDouble()<0.2) {
                    if (acts.contains(Types.ACTIONS.fromInt(5)))
                        return Types.ACTIONS.fromInt(5);
                }
            }
            //FOLD
            if(acts.contains(Types.ACTIONS.fromInt(0)))
                return Types.ACTIONS.fromInt(0);
        }

        // if there is a normal bet
        if(toCall>5){
            if(confidence>8){
                if(ThreadLocalRandom.current().nextDouble()<0.1) {
                    if (acts.contains(Types.ACTIONS.fromInt(5)))
                        return Types.ACTIONS.fromInt(5);
                }
                // RAISE
                if(acts.contains(Types.ACTIONS.fromInt(4)))
                    return Types.ACTIONS.fromInt(4);
            }else if(confidence>5){
                // CALL
                if(acts.contains(Types.ACTIONS.fromInt(3)))
                    return Types.ACTIONS.fromInt(3);
            }else{
                //FOLD
                if(acts.contains(Types.ACTIONS.fromInt(0)))
                    return Types.ACTIONS.fromInt(0);
            }
        }

        // if there is a small bet (small blind?)
        if(toCall>0){
            if(confidence>8){
                // RAISE
                if(acts.contains(Types.ACTIONS.fromInt(4)))
                    return Types.ACTIONS.fromInt(4);
            }else if(confidence>2){
                // CALL
                if(acts.contains(Types.ACTIONS.fromInt(3)))
                    return Types.ACTIONS.fromInt(3);
            }
        }

        // Agent's time to make the first bet
        if(confidence>9){
            // 30% chance to go all in with very good hand
            if(ThreadLocalRandom.current().nextDouble()<0.3){
                if(acts.contains(Types.ACTIONS.fromInt(5)))
                    return Types.ACTIONS.fromInt(5);
            }
        }
        if(confidence>7){
            // 10% chance to go all in with good hand
            if(ThreadLocalRandom.current().nextDouble()<0.1){
                if(acts.contains(Types.ACTIONS.fromInt(5)))
                    return Types.ACTIONS.fromInt(5);
            }
        }
        if(confidence>5){
            // BET
            if(acts.contains(Types.ACTIONS.fromInt(2)))
                return Types.ACTIONS.fromInt(2);
        }

        // check if possible before fold

        //CHECK
        if(acts.contains(Types.ACTIONS.fromInt(1)))
            return Types.ACTIONS.fromInt(1);


        //FOLD
        if(acts.contains(Types.ACTIONS.fromInt(0)))
            return Types.ACTIONS.fromInt(0);


        return(acts.get(ThreadLocalRandom.current().nextInt(acts.size())));
    }

    // --- never used ---
//    public Types.ACTIONS play2(int confidence) {
//        /*
//            - 0 -> FOLD	: give up the current round
//            - 1 -> CHECK	: pass for the current action (wait for someone else to do something to react on)
//            - 2 -> BET	: bet the "Big Blind"
//            - 3 -> CALL	: bet the same amount as the previous player bet
//            - 4 -> RAISE	: raise the last bet by the "Big Blind"
//            - 5 -> ALL IN : bet all remaining chips
//        */
//
//        if(confidence == 10) {
//
//            //RAISE
//            if(acts.contains(Types.ACTIONS.fromInt(4)))
//                return Types.ACTIONS.fromInt(4);
//
//            //BET
//            if(acts.contains(Types.ACTIONS.fromInt(2)))
//                return Types.ACTIONS.fromInt(2);
//
//            //CALL
//            if(acts.contains(Types.ACTIONS.fromInt(3)))
//                return Types.ACTIONS.fromInt(3);
//
//            //ALL-IN
//            if(acts.contains(Types.ACTIONS.fromInt(5)))
//                return Types.ACTIONS.fromInt(5);
//
//        }
//        if(confidence == 9) {
//            //RAISE
//            if(acts.contains(Types.ACTIONS.fromInt(4)))
//                return Types.ACTIONS.fromInt(4);
//
//            //BET
//            if(acts.contains(Types.ACTIONS.fromInt(2)))
//                return Types.ACTIONS.fromInt(2);
//
//            //CALL
//            if(acts.contains(Types.ACTIONS.fromInt(3)))
//                return Types.ACTIONS.fromInt(3);
//
//            //ALL-IN
//            if(acts.contains(Types.ACTIONS.fromInt(5)))
//                return Types.ACTIONS.fromInt(5);
//        }
//        if(confidence == 8) {
//            //RAISE
//            if(acts.contains(Types.ACTIONS.fromInt(4)))
//                return Types.ACTIONS.fromInt(4);
//
//            //BET
//            if(acts.contains(Types.ACTIONS.fromInt(2)))
//                return Types.ACTIONS.fromInt(2);
//
//            //CALL
//            if(acts.contains(Types.ACTIONS.fromInt(3)))
//                return Types.ACTIONS.fromInt(3);
//
//        }
//        if(confidence == 7) {
//
//            //BET
//            if(acts.contains(Types.ACTIONS.fromInt(2)))
//                return Types.ACTIONS.fromInt(2);
//
//            //CALL
//            if(acts.contains(Types.ACTIONS.fromInt(3)))
//                return Types.ACTIONS.fromInt(3);
//
//        }
//        if(confidence == 6) {
//
//            //CHECK
//            if(acts.contains(Types.ACTIONS.fromInt(1)))
//                return Types.ACTIONS.fromInt(1);
//
//            //CALL
//            if(acts.contains(Types.ACTIONS.fromInt(3)))
//                return Types.ACTIONS.fromInt(3);
//
//        }
//        if(confidence == 5) {
//
//            //CHECK
//            if(acts.contains(Types.ACTIONS.fromInt(1)))
//                return Types.ACTIONS.fromInt(1);
//
//            //CALL
//            if(acts.contains(Types.ACTIONS.fromInt(3)))
//                return Types.ACTIONS.fromInt(3);
//
//        }
//        if(confidence == 4) {
//
//            //CHECK
//            if(acts.contains(Types.ACTIONS.fromInt(1)))
//                return Types.ACTIONS.fromInt(1);
//
//            //FOLD
//            if(acts.contains(Types.ACTIONS.fromInt(0)))
//                return Types.ACTIONS.fromInt(0);
//
//        }
//        if(confidence == 3) {
//
//            //CHECK
//            if(acts.contains(Types.ACTIONS.fromInt(1)))
//                return Types.ACTIONS.fromInt(1);
//
//            //FOLD
//            if(acts.contains(Types.ACTIONS.fromInt(0)))
//                return Types.ACTIONS.fromInt(0);
//
//        }
//        if(confidence == 2) {
//
//            //CHECK
//            if(acts.contains(Types.ACTIONS.fromInt(1)))
//                return Types.ACTIONS.fromInt(1);
//
//            //FOLD
//            if(acts.contains(Types.ACTIONS.fromInt(0)))
//                return Types.ACTIONS.fromInt(0);
//
//        }
//        if(confidence == 1) {
//
//            //CHECK
//            if(acts.contains(Types.ACTIONS.fromInt(1)))
//                return Types.ACTIONS.fromInt(1);
//
//            //FOLD
//            if(acts.contains(Types.ACTIONS.fromInt(0)))
//                return Types.ACTIONS.fromInt(0);
//
//        }
//
//        // backup: using a random action
//        return(acts.get(ThreadLocalRandom.current().nextInt(acts.size())));
//    }

//    @Override
//    public double getScore(StateObservation sob) {
//        return rand.nextDouble();
//    }

    /*
    public WholeCards checkWholeCards(PlayingCard[] cards){
        if(cards.length>2)
            throw new RuntimeException("More than two whole cards");
        if(cards.length<2)
            throw new RuntimeException("Less than two whole cards");

        WholeCards wc = new WholeCards();
        if(cards[0].getRank() > cards[1].getRank()){
            wc.valueHigh = cards[0].getRank();
            wc.valueLow = cards[1].getRank();
        }else{
            wc.valueHigh = cards[1].getRank();
            wc.valueLow = cards[0].getRank();
        }

        // pair?
        wc.pair = cards[0].getRank() == cards[1].getRank();

        // connected?
        wc.connected =  wc.valueHigh - wc.valueLow == 1;

        // suited?
        wc.suited = cards[0].getSuit() == cards[1].getSuit();

        return wc;
    }
    */

    private int evaluateWholeCards(PlayingCard[] cards){

        if(cards[0]==null||cards[1]==null||cards[2]!=null)
            throw new RuntimeException("Less than two whole cards");

        int high;
        int low;

        if(cards[0].getRank() > cards[1].getRank()){
            high = cards[0].getRank();
            low = cards[1].getRank();
        }else{
            high = cards[1].getRank();
            low = cards[0].getRank();
        }

        boolean suited = cards[0].getSuit()==cards[1].getSuit();

        int confidence;

        if(suited){
            confidence = wholeCardLookup.wholeCardLookupSuited[high][low];
        }else{
            confidence = wholeCardLookup.wholeCardLookupOffSuit[high][low];
        }
        return confidence;
    }

    private class Hand {

        int high;
        int suited;
        int connected;
        ArrayList<Integer> pairs;
        ArrayList<Integer> triples;
        ArrayList<Integer> four;

        boolean fullHouse;
        boolean flush;
        boolean straight;

        Hand(){

        }
    }

    // --- never used ---
//    private class WholeCards {
//        boolean connected;
//        boolean suited;
//        boolean pair;
//        int valueHigh;
//        int valueLow;
//        int suit0;
//        int suit1;
//
//        WholeCards(){
//            connected = false;
//            suited = false;
//            pair = false;
//            valueHigh = 0;
//            valueLow = 0;
//            suit0 = 0;
//            suit1 = 0;
//        }
//    }

    //public void stuff(){
//
    //    if(toCall == 0){
    //        if(ThreadLocalRandom.current().nextDouble()<0.5){
    //            // BET
    //            actBest = Types.ACTIONS.fromInt(2);
    //        }else{
    //            // CHECK
    //            actBest = Types.ACTIONS.fromInt(1);
    //        }
    //    }else if(toCall < chips/2){
    //        // CALL
    //        actBest = Types.ACTIONS.fromInt(3);
    //    }else{
    //        if(ThreadLocalRandom.current().nextDouble()<0.5){
    //            // ALL IN
    //            actBest = Types.ACTIONS.fromInt(5);
    //        }else{
    //            // FOLD
    //            actBest = Types.ACTIONS.fromInt(0);
    //        }
    //    }
    //}

    /*
    OLD WHOLE CARD
         WholeCards wc = checkWholeCards(myCards);
            if(wc.suited){
                if(wc.valueHigh==12){
                    //ACE
                    if(wc.valueLow>7){
                        //yeah
                        return play(10);
                    }else if(wc.valueLow>3){
                        //meh
                        return play(7);
                    }else{
                        //nah
                        return play(5);
                    }
                }

                if(wc.valueHigh==11){
                    //KING
                    if(wc.valueLow>7){
                        //yeah
                        return play(10);
                    }else if(wc.valueLow>6){
                        //meh
                        return play(7);
                    }else{
                        //nah
                        return play(5);
                    }
                }

                if(wc.valueHigh==10){
                    //QUEEN
                    if(wc.valueLow>7){
                        //yeah
                        return play(10);
                    }else if(wc.valueLow>5){
                        //meh
                        return play(7);
                    }else{
                        //hell nah
                        return play(2);
                    }
                }

                if(wc.valueHigh==9){
                    //JACK
                    if(wc.valueLow>6){
                        //yeah
                        return play(10);
                    }else if(wc.valueLow>5){
                        //meh
                        return play(7);
                    }else if(wc.valueLow>4){
                        //nah
                        return play(5);
                    }else{
                        //hell nah
                        return play(2);
                    }
                }

                if(wc.valueHigh==8){
                    //TEN
                    if(wc.valueLow>6){
                        //yeah
                        return play(10);
                    }else if(wc.valueLow>5){
                        //meh
                        return play(7);
                    }else if(wc.valueLow>3){
                        //nah
                        return play(5);
                    }else{
                        //hell nah
                        return play(2);
                    }
                }

                if(wc.valueHigh==7){
                    //NINE
                    if(wc.valueLow>5){
                        //meh
                        return play(7);
                    }else if(wc.valueLow>3){
                        //nah
                        return play(5);
                    }else if(wc.valueLow>3){
                        //hell nah
                        return play(2);
                    }
                }

            }
            else{
                if(wc.pair){
                    if(wc.valueHigh>4){
                        //Go for it
                        return play(10);
                    }else if(wc.valueHigh>2){
                        // meh
                        return play(8);
                    }else{
                        // nah?
                        return play(7);
                    }
                }else{
                    if(wc.valueHigh==12){
                        //ACE
                        if(wc.valueLow>7){
                            //yeah
                            return play(10);
                        }else if(wc.valueLow>4){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else if(wc.valueHigh==11) {
                        //KING
                        if(wc.valueLow>8){
                            //yeah
                            return play(10);
                        }else if(wc.valueLow>7){
                            //meh
                            return play(7);
                        }else if(wc.valueLow>6){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else if(wc.valueHigh==10){
                        //QUEEN
                        if(wc.valueLow>7){
                            //meh
                            return play(7);
                        }else if(wc.valueLow>6){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else if(wc.valueHigh==9) {
                        //JACK
                        if(wc.valueLow>7){
                            //meh
                            return play(7);
                        }else if(wc.valueLow>5){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else if(wc.valueHigh==8) {
                        //TEN
                        if(wc.valueLow>5){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else if(wc.valueHigh==7) {
                        //NINE
                        if(wc.valueLow>4){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else if(wc.valueHigh==6) {
                        //EIGHT
                        if(wc.valueLow>4){
                            //nah
                            return play(5);
                        }else{
                            //hell nah
                            return play(2);
                        }
                    }else{
                        //HELL NAH
                        return play(2);
                    }

                }
            }

     */
}//