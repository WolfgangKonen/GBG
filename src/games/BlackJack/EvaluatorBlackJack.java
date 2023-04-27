package games.BlackJack;

import controllers.MC.MCAgentN;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.EvalResult;
import games.Evaluator;
import games.GameBoard;
import tools.Types;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class EvaluatorBlackJack extends Evaluator {
    /**
     * Evaluator for the game Othello. Depending on the value of parameter {@code mode} in constructor:
     * <ul>
     * <li>  0: number of times agent chooses move from Basic-Strategy on a pre-constructed game state
     * <li>  1: average payoff of agent
     * <li>  2: number of times agent chooses move from Basic-Strategy on a random game state
     * <li>  3: how many times agent is not choosing insurance
     * <li>  4: percentage of times where agent did chose HIT (40 simple pre-constructed situations) (best = 1.0)
     * <li>  5: percentage of times where agent did chose STAND (60 simple pre-constructed situations) (best = 1.0)
     * </ul>
     *
     */

    public final int NUM_ITER = 500;
    double avgPayOff = 0;
    int countInsuranceTaken = 0;
    int possibleInsuranceWins = 0;
    int countNoInsuranceTaken = 0;
    int insuranceSuccess = 0;
    int noInsuranceButBlackJack = 0;
    int moves = 0;
    int movesFromBasicStrategy = 0;
    int playerBlackJack = 0;
    int dealerBlackJack = 0;
    private final String dir = "src/games/BlackJack/Stats";
    private final DecimalFormat frm = new DecimalFormat("#0.0000");

    public EvaluatorBlackJack(PlayAgent e_PlayAgent, GameBoard gb, int mode, int stopEval, int verbose) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
    }


    /**
     * Creates a specific game state (mid round scenario).
     * @param firstCardPlayer first card of the players hand
     * @param secondCardPlayer second card of the players hand
     * @param upCardDealer dealers upcard
     * @return the specific game state
     */

    public StateObserverBlackJack initSpecificSo(Card firstCardPlayer, Card secondCardPlayer, Card upCardDealer){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER + 30);
        so.getCurrentPlayer().bet(10);
        so.getCurrentPlayer().setChips((NUM_ITER+30)*10);
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
     * <p>
     * This method is used for quick-evaluation.
     * Agent gets 20 pre-constructed game states and needs to choose the next best action.
     * Result of this evaluation is the percentage of how many times an agent
     * chooses an action suggested by basic strategy.
     * Taking the move suggested by basic strategy everytime results a perfect score of 1 (best)
     * Never taking the move suggested by basic strategy results in a score of 0 (worst)
     * <p>
     * @param playAgent agent that gets evaluated
     * @return evaluation score
     */
    public double simulateFixedMovesFromBasicStrategy(PlayAgent playAgent){

        StateObserverBlackJack []sos = {
                initSpecificSo(new Card(Card.Rank.FIVE, Card.Suit.SPADE), new Card(Card.Rank.FOUR, Card.Suit.SPADE),
                        new Card(Card.Rank.SEVEN, Card.Suit.CLUB)), //bestmove hit
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
                initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.TWO, Card.Suit.SPADE),
                        new Card(Card.Rank.TWO, Card.Suit.CLUB)), //bestmove HHIT
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
        m_msg = "";
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
     * Agent will play 40 most simple pre-constructed States. In every case Hit should be the obvious choice.
     * This should represent the most basic understanding of BlackJack
     * @param playAgent agent that gets evaluated
     * @return evaluation score
     */
    public double simulateVerySimpleSituationsHit(PlayAgent playAgent){
        StateObserverBlackJack []sos = new StateObserverBlackJack[40];
        int index = 0;
        //------------------------ handvalue 5 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.TWO, Card.Suit.SPADE), new Card(Card.Rank.THREE, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //--------------------------handvalue 6 vs any dealerupcard (not splitable)-------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.TWO, Card.Suit.SPADE), new Card(Card.Rank.FOUR, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //--------------------------handvalue 7 vs any dealerupcard (not splitable)------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.THREE, Card.Suit.SPADE), new Card(Card.Rank.FOUR, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //--------------------------handvalue 8 vs any dealerupcard (not splitable)------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.THREE, Card.Suit.SPADE), new Card(Card.Rank.FIVE, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }

        int agentChoseHit = 0;
        for(StateObserverBlackJack so: sos){
            int nextActAgent = playAgent.getNextAction2(so.partialState(), false, true).toInt();
            if (nextActAgent == StateObserverBlackJack.BlackJackActionDet.HIT.getAction())
                agentChoseHit++;
        }
        return (double)agentChoseHit/(double)sos.length;
    }

    /**
     * Agent will play 60 most simple pre-constructed States. In every case STAND should be the obvious choice.
     * This should represent the most basic understanding of BlackJack
     * @param playAgent agent that gets evaluated
     * @return evaluation score
     */
    public double simulateVerySimpleSituationsStand(PlayAgent playAgent){
        StateObserverBlackJack []sos = new StateObserverBlackJack[60];
        int index = 0;
        //------------------------ handvalue 17 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.KING, Card.Suit.SPADE), new Card(Card.Rank.SEVEN, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //------------------------ handvalue 18 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.KING, Card.Suit.SPADE), new Card(Card.Rank.EIGHT, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //------------------------ handvalue 19 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.KING, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //------------------------ handvalue 20 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.KING, Card.Suit.SPADE), new Card(Card.Rank.QUEEN, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //------------------------ handvalue soft 19 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.EIGHT, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }
        //------------------------ handvalue soft 20 vs any Dealer upcard ---------------------------------------
        for(int i = 1; i < 11; i++){
            sos[index++] = initSpecificSo(new Card(Card.Rank.ACE, Card.Suit.SPADE), new Card(Card.Rank.NINE, Card.Suit.SPADE),
                    new Card(Card.Rank.getRankFromValue(i), Card.Suit.CLUB));
        }

        int agentChoseStand = 0;
        for(StateObserverBlackJack so: sos){
            int nextActAgent = playAgent.getNextAction2(so.partialState(), false, true).toInt();
            if (nextActAgent == StateObserverBlackJack.BlackJackActionDet.STAND.getAction())
                agentChoseStand++;
        }
        return (double)agentChoseStand/(double)sos.length;
    }



    /**
     * Agent will play NUM_ITER hands
     * evaluation result is the percentage of how many times an agent took insurance when he had
     * the chance to do so. Since taking insurance is a losing bet long term, the evaluation result gets better the
     * less often an Agent took insurance.
     * Not taking insurance ever results in a score of 1 (best)
     * Taking insurance every time results in a score of 0 (worst)
     * @param playAgent agent that gets evaluated
     * @return evaluation score
     */

    public double simulateInsurance(PlayAgent playAgent){

        countInsuranceTaken = 0; possibleInsuranceWins = 0;
        countNoInsuranceTaken = 0; insuranceSuccess = 0;
        noInsuranceButBlackJack = 0;
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER+30);
        so.getCurrentPlayer().setChips((NUM_ITER+30) * 10);

        for(int i = 0; i < NUM_ITER; i++) {

            while (!so.isRoundOver()) {
                int act = playAgent.getNextAction2(so.partialState(), false, true).toInt();
                if (act == 6) {
                    countInsuranceTaken++;
                    if (so.getDealer().getActiveHand().checkForBlackJack() && so.getDealer().getActiveHand().getCards().get(0).rank == Card.Rank.ACE) {
                        insuranceSuccess++;
                    }
                } else if (act == 7) {
                    countNoInsuranceTaken++;
                    if (so.getDealer().getActiveHand().checkForBlackJack() && so.getDealer().getActiveHand().getCards().get(0).rank == Card.Rank.ACE) {
                        noInsuranceButBlackJack++;
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
        if((countInsuranceTaken+countNoInsuranceTaken) != 0)
            return  ((double)countNoInsuranceTaken/((double)countInsuranceTaken+(double)countNoInsuranceTaken));
        return 1;
    }

    /**
     * Agent will play NUM_ITER hands
     * each action the Agent takes in phase (ASKFORINSURANCE and PLAYERONACTION) gets compared to what
     * the basic strategy suggests. Result of this evaluation is the percentage of how many times an agent
     * chooses an action suggested by basic strategy in a random game state.
     * Taking the move suggested by basic strategy everytime results a perfect score of 1 (best)
     * Never taking the move suggested by basic strategy results in a score of 0 (worst)
     * @param playAgent agent that gets evaluated
     * @return evaluation score
     */
    public double simulateRandomMovesFromBasicStrategy(PlayAgent playAgent){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER+30);
        so.getCurrentPlayer().setChips((NUM_ITER+30) *10);
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
     * Agent will play NUM_ITER hands
     * the result of this evaluation is the average payoff the agent achieves
     * there are no best and worst results yet. However, the higher, the better.
     * BasicStrategyBlackJackAgent should achieve the best score
     * RandomAgent should achieve the worst score
     * @param playAgent agent that gets evaluated
     * @return evaluation score
     */
    public double simulateAvgPayOff(PlayAgent playAgent){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER + 30);
        so.getCurrentPlayer().setChips((NUM_ITER+30)*10);
        avgPayOff = 0;
        playerBlackJack = 0;
        dealerBlackJack = 0;

        for(int i = 0; i < NUM_ITER; i++) {
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

    public EvalResult evalAgentAvgPayoff(PlayAgent playAgent, double thresh){
        lastResult = simulateAvgPayOff(playAgent);
        m_msg = "\nAgent has an average Pay-Off of : " + lastResult;
        m_msg += "\nAgent had : " + playerBlackJack + " Black Jacks ";
        m_msg += "\nthe dealer had : " + dealerBlackJack + " Black Jacks ";
        return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }

    public EvalResult evalAgentRandomMovesFromBasicStrategy(PlayAgent playAgent, double thresh){
        lastResult = simulateRandomMovesFromBasicStrategy(playAgent);
        m_msg = "\nnumber of moves : " + moves;
        m_msg += "\nnumber of moves suggested by Basic Strategy : " + movesFromBasicStrategy;

        return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }

    public EvalResult evalAgentFixedMovesFromBasicStrategy(PlayAgent playAgent, double thresh){
        lastResult = simulateFixedMovesFromBasicStrategy(playAgent);
        m_msg += "\nnumber of moves :" + moves;
        m_msg += "\nnumber of moves took suggested by Basic Strategy : " + movesFromBasicStrategy;

        return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }

    public EvalResult evalAgentInsurance(PlayAgent playAgent, double thresh){
        lastResult = simulateInsurance(playAgent);
        m_msg += "Agent took insurance : " + countInsuranceTaken + " times -> this cost : " + countInsuranceTaken + " * 10 = " + (countInsuranceTaken*10);
        m_msg += "\nAgent did not take insurance : " + countNoInsuranceTaken + " times when he had the opportunity to do so";
        m_msg += "\nPossible insurance-wins : " + possibleInsuranceWins + " times";
        m_msg += "\nAgent took no insurance but dealer showed Black Jack : " + noInsuranceButBlackJack + " times";
        m_msg += "\nAgent took insurance and dealer showed Black Jack : " + insuranceSuccess + " times -> this payed back : " + insuranceSuccess +  " * 30  = " + (insuranceSuccess *30);
        System.out.println("last-Result" + lastResult);
        return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }

    public EvalResult evalAgentSimpleHitMoves(PlayAgent playAgent, double thresh){
        lastResult = simulateVerySimpleSituationsHit(playAgent);
        return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }

    public EvalResult evalAgentSimpleStandMoves(PlayAgent playAgent, double thresh){
        lastResult = simulateVerySimpleSituationsStand(playAgent);
        return new EvalResult(lastResult, lastResult>thresh, m_msg, m_mode, thresh);
    }

    @Override
    protected EvalResult evalAgent(PlayAgent playAgent) {
        switch (m_mode){
            case -1:
                m_msg = "No evaluation done ";
                lastResult = 0.0;
                return new EvalResult(lastResult, true, m_msg, m_mode, Double.NaN);
            case 0:
                return evalAgentFixedMovesFromBasicStrategy(playAgent, 0.8);
            case 1:
                return evalAgentAvgPayoff(playAgent, 0.5);
            case 2:
                return evalAgentRandomMovesFromBasicStrategy(playAgent, 0.8);
            case 3:
                return evalAgentInsurance(playAgent, 0.8);
            case 4:
                return evalAgentSimpleHitMoves(playAgent, 0.9);
            case 5:
                return evalAgentSimpleStandMoves(playAgent, 0.9);
            case 10:
                //create statistics
                return logStatistics(playAgent);
            default:
                throw new RuntimeException("Invalid m_mode = "+m_mode);
        }
    }


    /**
     * Writes multiple evaluations (different {@code j = m_mode}) into .csv. Output is written to ./Stats
     * @param playAgent the agent to evaluate
     * @return  the last evaluation result
     */
    public EvalResult logStatistics(PlayAgent playAgent){
        //TODO: generalize more

        StringBuilder sb = new StringBuilder();
        File directory = new File(dir);
        if (!directory.exists()){
            directory.mkdir();
        }
        if(!(Files.exists(Path.of(directory.getPath() , playAgent.getName()+".csv")))) {
            sb.append("agent ");
            sb.append(',');
            sb.append("num-iteration");
            sb.append(',');
            sb.append("eval-mode");
            sb.append(',');
            sb.append("eval-result");
            sb.append(',');
            sb.append("date");
            sb.append(',');
            sb.append(getParStringHeaders(playAgent));
            sb.append('\n');
        }

        EvalResult eRes = new EvalResult();
        for(int j = 4; j < 6; j++) {
            // ten evaluations
            for (int i = 0; i < 10; i++) {
                sb.append(playAgent.getName());
                sb.append(',');
                sb.append(i);
                sb.append(',');
                sb.append(j);
                sb.append(',');
                switch (j) {
                    case 0 -> eRes = evalAgentFixedMovesFromBasicStrategy(playAgent, 0.8);
                    case 1 -> eRes = evalAgentAvgPayoff(playAgent, 0.5);
                    case 2 -> eRes = evalAgentRandomMovesFromBasicStrategy(playAgent, 0.8);
                    case 3 -> eRes = evalAgentInsurance(playAgent, 0.8);
                    case 4 -> eRes = evalAgentSimpleHitMoves(playAgent, 0.9);
                    case 5 -> eRes = evalAgentSimpleStandMoves(playAgent, 0.9);
                }
                sb.append(frm.format(eRes));
                sb.append(',');
                sb.append(getCurrentTimeStamp());
                sb.append(',');
                sb.append(getParValueString(playAgent));
                sb.append('\n');
            }
            sb.append('\n');
        }
        try {
            this.write(sb.toString(), playAgent.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return eRes;

    }

    /**
     * helper function to get agent-settings-headlines as String. (used to write them in .csv)
     * @param playAgent that gets evaluated
     * @return Agent-settings-headlines as String
     */
    public String getParStringHeaders(PlayAgent playAgent){

        if(playAgent instanceof MCAgentN){
            return "iterations,rollout-depth,number-agents,stop-on-round-over";
        }
        if(playAgent instanceof MCTSExpectimaxAgt){
            return "iterations,tree-depth,rollout-depth,stop-on-round-over";
        }
        if(playAgent instanceof TDNTuple3Agt){
            return "alpha-init,alpha-final,epsilon-init,epsilon-final,gamma,lambda";
        }

        return "";
    }

    /**
     * helper function to get agent-settings as String. (used to write them in .csv)
     * @param playAgent that gets evaluated
     * @return Agent-settings as String
     */
    public String getParValueString(PlayAgent playAgent){
        if(playAgent instanceof MCAgentN){
            MCAgentN agent = (MCAgentN) playAgent;
            return "" + agent.m_mcPar.getNumIter() + "," + agent.m_mcPar.getRolloutDepth() + "," + agent.m_mcPar.getNumAgents()
                    +","+ agent.m_mcPar.getStopOnRoundOver();
        }
        if(playAgent instanceof MCTSExpectimaxAgt){
            MCTSExpectimaxAgt agent = (MCTSExpectimaxAgt) playAgent;
            return "" + agent.getParMCTSE().getNumIter() + "," + agent.getParMCTSE().getTreeDepth() + "," +
                    agent.getParMCTSE().getRolloutDepth()
                    +","+ agent.getParMCTSE().getStopOnRoundOver();

        }
        if(playAgent instanceof TDNTuple3Agt){
            //TODO: get params from config
            return "alpha-init,alpha-final,epsilon-init,epsilon-final,gamma,lambda";
        }
        return "";
    }

    @Override
    public int[] getAvailableModes() {
        return new int[] {-1, 0, 1, 2, 3, 4, 5, 10};
    }

    @Override
    public int getQuickEvalMode() {
        return 4;
    }

    @Override
    public int getTrainEvalMode() {
        return 5;
    }


    @Override
    public String getPrintString() {
        return switch (m_mode) {
            case 0 -> "percentage of moves took suggested by Basic-Strategy in pre-constructed States(best = 1.0): ";
            case 1 -> "average payoff of agent (the higher the better): ";
            case 2 -> "percentage of moves took suggested by Basic-Strategy in random States(best = 1.0): ";
            case 3 -> "percentage of times where agent did not chose insurance (best = 1.0): ";
            case 4 -> "percentage of times where agent did chose HIT (40 simple pre-constructed situations) (best = 1.0): ";
            case 5 -> "percentage of times where agent did chose STAND (60 simple pre-constructed situations) (best = 1.0):";
            case 10-> "log statistics";
            default -> "no evaluation done ";
        };
    }


    @Override
    public String getTooltipString() {
        return  "<html>"
                + "-1: no evaluation<br>"
                + "0 : percentage of moves took suggested by Basic-Strategy in pre-constructed States(best = 1.0)<br>"
                + "1 : average payoff of agent (the higher the better)<br>"
                + "2 : percentage of moves took suggested by Basic-Strategy in random States(best = 1.0)<br>"
                + "3 : percentage of times where agent did not chose insurance (best = 1.0)<br>"
                + "4 : percentage of times where agent did chose HIT (40 simple pre-constructed situations) (best = 1.0)<br>"
                + "5 : percentage of times where agent did chose STAND (60 simple pre-constructed situations) (best = 1.0)<br>"
                + "10: log statistics"
                + "</html>";
    }

    @Override
    public String getPlotTitle() {
        return switch (m_mode) {
            case 0 -> "moves from Basic Strategy fixed";
            case 1 -> "Average payoff";
            case 2 -> "moves from Basic Strategy random ";
            case 3 -> "Insurance";
            case 4 -> "simple HIT moves";
            case 5 -> "simple STAND moves";
            case 10-> "log statistics";
            default -> "no evaluation done ";
        };
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date now = new Date();
        return sdfDate.format(now);
    }

    // --- never used ---
//    /**
//     * fixes multiline-String for .csv output
//     * @param data string that needs to get fixed
//     * @return fixed data String
//     */
//    public String fixString(String data) {
//        String fix = data.replaceAll("\\R", "_");
//        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
//            data = data.replace("\"", "\"\"");
//            fix = "\"" + data + "\"";
//        }
//        return fix;
//    }

    /**
     * writes an evaluation to agentName.csv
     * @param s evaluation data
     * @param agentName name of agent that got evaluated
     */
    private void write(final String s, String agentName) throws IOException {
            Files.writeString(
                    Path.of(dir, agentName + ".csv"), s,
                    CREATE, APPEND
            );
    }
}
