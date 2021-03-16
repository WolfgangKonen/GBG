package games.BlackJack;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import tools.Types;

import java.util.ArrayList;

public class EvaluatorBlackJack extends Evaluator {
    /**
     * Evaluator for the game Othello. Depending on the value of parameter {@code mode} in constructor:
     * <ul>
     * <li>  0: number of times agent chooses move from Basic-Strategy on a preconstructed game state
     * <li>  1: average payoff of agent
     * <li>  2: number of times agent chooses move from Basic-Strategy on a random game state
     * <li>  3: how many times agent is not choosing insurance
     * </ul>
     *
     */

    public final int NUM_ITER = 100;
    double avgPayOff = 0;
    int countInsuranceTaken = 0;
    int possibleInsuranceWins = 0;
    int countNoInsuranceTaken = 0;
    int insurenceSuccess = 0;
    int noInsurenceButBlackJack = 0;
    int moves = 0;
    int movesFromBasicStrategy = 0;
    int playerBlackJack = 0;
    int dealerBlackJack = 0;

    public EvaluatorBlackJack(PlayAgent e_PlayAgent, GameBoard gb, int mode, int stopEval, int verbose) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
    }


    /**
     * params both cards of player and upcard of dealer
     * returns a specific StateObserver
     */

    public StateObserverBlackJack initSpecificSo(Card firstCardPlayer, Card secondCardPlayer, Card upCardDealer){
        StateObserverBlackJack so = new StateObserverBlackJack(1, 15);
        so.getCurrentPlayer().bet(10);
        so.getCurrentPlayer().addCardToActiveHand(firstCardPlayer);
        so.getCurrentPlayer().addCardToActiveHand(secondCardPlayer);
        // Players Turn
        so.setgPhase(StateObserverBlackJack.gamePhase.PLAYERONACTION);
        so.getDealer().addCardToActiveHand(upCardDealer);
        so.getDealer().addCardToActiveHand(new Card(Card.Rank.X, Card.Suit.X));
        so.setPartialState(true);
        so.setAvailableActions();
        return so;
    }

    /**
     * This method is used for quick-evaluation.
     * Agent gets 20 preconstructed gamestates and needs to choose the next best action.
     * Result of this evaluation is the percentage of how many times an agent
     * chooses an action suggested by basic strategy.
     * Taking the move suggested by basic strategy everytime results a perfect score of 1 (best)
     * Never taking the move suggested by basic strategy results in a score of 0 (worst)
     */
    public double simulateFixedMovesFromBasicStrategy(PlayAgent playAgent){

        StateObserverBlackJack []sos = {
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                        new Card(Card.Rank.FIVE, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                        new Card(Card.Rank.EIGHT, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.TWO, Card.Suit.SPADE), new Card(Card.Rank.THREE, Card.Suit.SPADE),
                        new Card(Card.Rank.FIVE, Card.Suit.CLUB)), //bestmove HIT
                initSpecificSo(new Card(Card.Rank.TWO, Card.Suit.SPADE), new Card(Card.Rank.THREE, Card.Suit.SPADE),
                        new Card(Card.Rank.TEN, Card.Suit.CLUB)), //bestmove HIT
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SIX, Card.Suit.SPADE),
                        new Card(Card.Rank.FOUR, Card.Suit.CLUB)), //bestmove DOUBLEDOWN
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.SIX, Card.Suit.SPADE),
                        new Card(Card.Rank.TEN, Card.Suit.CLUB)), //bestmove DOUBLEDOWN
                initSpecificSo(new Card(Card.Rank.TEN, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                        new Card(Card.Rank.THREE, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.TEN, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                        new Card(Card.Rank.TEN, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.TEN, Card.Suit.SPADE), new Card(Card.Rank.SIX, Card.Suit.SPADE),
                        new Card(Card.Rank.SIX, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.KING, Card.Suit.SPADE), new Card(Card.Rank.SIX, Card.Suit.SPADE),
                        new Card(Card.Rank.TEN, Card.Suit.CLUB)), //bestmove SURRENDER
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.TEN, Card.Suit.SPADE),
                        new Card(Card.Rank.NINE, Card.Suit.CLUB)), //bestmove HIT
                initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.FOUR, Card.Suit.SPADE),
                        new Card(Card.Rank.FIVE, Card.Suit.CLUB)), //bestmove DOUBLEDOWN
                initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                        new Card(Card.Rank.TEN, Card.Suit.CLUB)), //bestmove HIT
                initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                        new Card(Card.Rank.TEN, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.SIX, Card.Suit.SPADE), new Card(Card.Rank.SIX, Card.Suit.SPADE),
                        new Card(Card.Rank.THREE, Card.Suit.CLUB)), //bestmove SPLIT
                initSpecificSo(new Card(Card.Rank.EIGHT, Card.Suit.SPADE), new Card(Card.Rank.EIGHT, Card.Suit.SPADE),
                        new Card(Card.Rank.ACE, Card.Suit.CLUB)), //bestmove SPLIT
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                        new Card(Card.Rank.EIGHT, Card.Suit.CLUB)), //bestmove DOUBLEDOWN
                initSpecificSo(new Card(Card.Rank.TEN, Card.Suit.SPADE), new Card(Card.Rank.TEN, Card.Suit.SPADE),
                        new Card(Card.Rank.JACK, Card.Suit.CLUB)), //bestmove STAND
                initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.ACE, Card.Suit.SPADE),
                        new Card(Card.Rank.NINE, Card.Suit.CLUB)), //bestmove SPLIT
                initSpecificSo(new Card(Card.Rank.TEN, Card.Suit.SPADE), new Card(Card.Rank.FOUR, Card.Suit.SPADE),
                        new Card(Card.Rank.NINE, Card.Suit.CLUB)), //bestmove HIT
                initSpecificSo(new Card(Card.Rank.SIX, Card.Suit.SPADE), new Card(Card.Rank.TWO, Card.Suit.SPADE),
                        new Card(Card.Rank.EIGHT, Card.Suit.CLUB)) //bestmove HIT
        };

        moves = sos.length;
        movesFromBasicStrategy = 0;

        PlayAgent bsbja = new BasicStrategyBlackJackAgent();

        String chosenMove;
        String bestMove;
        for(StateObserverBlackJack so: sos){
            int nextActAgent = playAgent.getNextAction2(so.partialState(), false, true).toInt();
            int nextActionByBasicStrategy = bsbja.getNextAction2(so.partialState(), false, true).toInt();
            chosenMove = StateObserverBlackJack.BlackJackActionDet.values()[nextActAgent].name();
            bestMove = StateObserverBlackJack.BlackJackActionDet.values()[nextActionByBasicStrategy].name();
            if (nextActAgent == nextActionByBasicStrategy)
                movesFromBasicStrategy++;
            else{
                m_msg += "missed move on " + so.getCurrentPlayer().getActiveHand() + " vs dealer "
                        + so.getDealer().getActiveHand() + " -> best Move: " +
                        bestMove + " agents choice : " + chosenMove + "\n";
            }
        }
        return (double)movesFromBasicStrategy/(double)moves;

    }

    /**
     * Agent will play NUM_ITER hands (between 1000 and 10000 Hands)
     * evaluation result is the percentage of how many times an agent took insurance when he had
     * the chance to do so. Since taking insurance is a losing bet long term, the evaluation result gets better the
     * less often an Agent took insurance.
     * Not taking insurance ever results in a score of 1 (best)
     * Taking insurance every time results in a score of 0 (worst)
     */

    public double simulateInsurance(PlayAgent playAgent){

        countInsuranceTaken = 0; possibleInsuranceWins = 0;
        countNoInsuranceTaken = 0; insurenceSuccess = 0;
        noInsurenceButBlackJack = 0;
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER);
        so.getCurrentPlayer().setChips(1000000);

        for(int i = 0; i < NUM_ITER; i++) {

            while (!so.isRoundOver()) {
                int act = playAgent.getNextAction2(so.partialState(), false, true).toInt();
                if (act == 6) {
                    countInsuranceTaken++;
                    if (so.getDealer().getActiveHand().checkForBlackJack() && so.getDealer().getActiveHand().getCards().get(0).rank == Card.Rank.ACE) {
                        insurenceSuccess++;
                    }
                } else if (act == 7) {
                    countNoInsuranceTaken++;
                    if (so.getDealer().getActiveHand().checkForBlackJack() && so.getDealer().getActiveHand().getCards().get(0).rank == Card.Rank.ACE) {
                        noInsurenceButBlackJack++;
                    }
                }
                so.advance(Types.ACTIONS.fromInt(act));
            }
            if (so.getDealer().getActiveHand().checkForBlackJack() && so.getDealer().getActiveHand().getCards().get(0).rank == Card.Rank.ACE) {
                possibleInsuranceWins++;
            }
            avgPayOff += so.getCurrentPlayer().getRoundPayoff();
            so.initRound();
        }
        return  ((double)countNoInsuranceTaken/((double)countInsuranceTaken+(double)countNoInsuranceTaken));
    }

    /**
     * Agent will play NUM_ITER hands (between 1000 and 10000 Hands)
     * each action the Agent takes in phase (ASKFORINSURANCE and PLAYERONACTION) gets compared to what
     * the basic strategy suggests. Result of this evaluation is the percentage of how many times an agent
     * chooses an action suggested by basic strategy in a random gamestate.
     * Taking the move suggested by basic strategy everytime results a perfect score of 1 (best)
     * Never taking the move suggested by basic strategy results in a score of 0 (worst)
     */
    public double simulateRandomMovesFromBasicStrategy(PlayAgent playAgent){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER);
        so.getCurrentPlayer().setChips(1000000);
        PlayAgent bsbja = new BasicStrategyBlackJackAgent();

        moves = 0;
        movesFromBasicStrategy = 0;


        for(int i = 0; i < NUM_ITER; i++) {
            while (!so.isRoundOver()) {
                int act = playAgent.getNextAction2(so.partialState(), false, true).toInt();
                if(bsbja.getNextAction2(so.partialState(), false, true).toInt() == act &&
                        (so.getCurrentPhase() == StateObserverBlackJack.gamePhase.ASKFORINSURANCE ||
                so.getCurrentPhase() == StateObserverBlackJack.gamePhase.PLAYERONACTION ))
                    movesFromBasicStrategy++;
                if((so.getCurrentPhase() == StateObserverBlackJack.gamePhase.ASKFORINSURANCE ||
                        so.getCurrentPhase() == StateObserverBlackJack.gamePhase.PLAYERONACTION )) {
                    moves++;
                }
                so.advance(Types.ACTIONS.fromInt(act));
            }
            so.initRound();
        }
        return ((double)movesFromBasicStrategy)/((double)moves);
    }

    /**
     * Agent will play NUM_ITER hands (between 1000 and 10000 Hands)
     * the result of this evaluation is the average payoff the agent achieves
     * there are no best and worst results yet. However the higher the better.
     * BasicStrategyBlackJackAgent should achieve the best score
     * RandomAgent should achieve the worst score
     */
    public double simulateAvgPayOff(PlayAgent playAgent){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER);
        so.getCurrentPlayer().setChips(1000000);
        avgPayOff = 0;
        playerBlackJack = 0;
        dealerBlackJack = 0;

        for(int i = 0; i < NUM_ITER; i++) {
            System.out.println("iteration : " +i);
            while (!so.isRoundOver()) {
                int act = playAgent.getNextAction2(so.partialState(), false, true).toInt();
                so.advance(Types.ACTIONS.fromInt(act));
            }
            avgPayOff += so.getCurrentPlayer().getRoundPayoff();
            if(so.getCurrentPlayer().getActiveHand().checkForBlackJack())
                playerBlackJack++;
            if(so.getDealer().getActiveHand().checkForBlackJack())
                dealerBlackJack++;

            so.initRound();
        }
        return avgPayOff /= NUM_ITER;
    }

    public double evalAgentAvgPayoff(PlayAgent playAgent){
        lastResult = simulateAvgPayOff(playAgent);
        m_msg += "Agent has an average Pay-Off of : " + lastResult;
        m_msg += "\n he also had : " + playerBlackJack + " Black Jacks ";
        m_msg += "\nthe dealer had : " + dealerBlackJack + "Black Jacks ";
        return lastResult;
    }

    public double evalAgentRandomMovesFromBasicStrategy(PlayAgent playAgent){
        lastResult = simulateRandomMovesFromBasicStrategy(playAgent);
        m_msg += "number of moves :" + moves;
        m_msg += "number of moves suggested by Basic Strategy : " + movesFromBasicStrategy;

        return lastResult;
    }

    public double evalAgentFixedMovesFromBasicStrategy(PlayAgent playAgent){
        lastResult = simulateFixedMovesFromBasicStrategy(playAgent);
        m_msg += "number of moves :" + moves;
        m_msg += "\nnumber of moves took suggested by Basic Strategy : " + movesFromBasicStrategy;

        return lastResult;
    }

    public double evalAgentInsurance(PlayAgent playAgent){
        lastResult = simulateInsurance(playAgent);
        m_msg += "Agent took insurance : " + countInsuranceTaken + " times -> this cost : " + countInsuranceTaken + " * 10 = " + (countInsuranceTaken*10);
        m_msg += "\nAgent did not take insurance : " + countNoInsuranceTaken + " times when he had the oppertunity to do so";
        m_msg += "\nPossible insurance-wins : " + possibleInsuranceWins + " times";
        m_msg += "\nAgent took no insurance but dealer showed Black Jack : " + noInsurenceButBlackJack + " times";
        m_msg += "\nAgent took insurance and dealer showed Black Jack : " + insurenceSuccess + " times -> this payed back : " + insurenceSuccess +  " * 30  = " + (insurenceSuccess*30);
        return lastResult;
    }

    @Override
    protected boolean evalAgent(PlayAgent playAgent) {
        switch (m_mode){
            case 0:
                return evalAgentFixedMovesFromBasicStrategy(playAgent) > 0.9;
            case 1:
                return evalAgentAvgPayoff(playAgent) > -0.2;
            case 2:
                return evalAgentRandomMovesFromBasicStrategy(playAgent) > 0.9;
            case 3:
                return evalAgentInsurance(playAgent) > 0.9;
            default:
                m_msg = "no evaluation done";
                break;
        }
        return false;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[] {-1, 0, 1, 2, 3};
    }

    @Override
    public int getQuickEvalMode() {
        return 0;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }


    @Override
    public String getPrintString() {
        switch (m_mode) {
            case 0: return "percentage of moves took suggested by Basic-Strategy in perconstructed States(best = 1.0): ";
            case 1: return "average payoff of agent (the higher the better): ";
            case 2: return "percentage of moves took suggested by Basic-Strategy in random States(best = 1.0): ";
            case 3: return "percentage of times where agent did not chose insurance (best = 1.0): ";
            default: return "no evaluation done ";
        }
    }

    @Override
    public String getTooltipString() {
        return  "<html>"
                + "0 : percentage of moves took suggested by Basic-Strategy in perconstructed States(best = 1.0)<br>"
                + "1 : average payoff of agent (the higher the better)<br>"
                + "2 : percentage of moves took suggested by Basic-Strategy in random States(best = 1.0)<br>"
                + "3 : percentage of times where agent did not chose insurance (best = 1.0)<br>"
                + "</html>";
    }

    @Override
    public String getPlotTitle() {
        switch (m_mode) {
            case 0: return "moves from Basic Strategy fixed";
            case 1: return "Average payoff";
            case 2: return "moves from Basic Strategy random ";
            case 3: return "Insurance";
            default: return "no evaluation done ";
        }
    }
}
