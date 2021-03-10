package games.BlackJack;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;
import tools.Types;

public class EvaluatorBlackJack extends Evaluator {
    /**
     * Evaluator for the game Othello. Depending on the value of parameter {@code mode} in constructor:
     * <ul>
     * <li>  0: average payoff of agent
     * <li>  1: number of times agent choses move from Basic-Strategy
     * <li>  2: how many times agent is not choosing insurance
     * </ul>
     *
     */

    public final int NUM_ITER = 10000;
    double avgPayOff = 0;
    int countInsuranceTaken = 0;
    int possibleInsuranceWins = 0;
    int countNoInsuranceTaken = 0;
    int insurenceSuccess = 0;
    int noInsurenceButBlackJack = 0;
    int moves = 0;
    int movesFromBasicStrategy = 0;
    PlayAgent lastSimulated;

    public EvaluatorBlackJack(PlayAgent e_PlayAgent, GameBoard gb, int mode, int stopEval, int verbose) {
        super(e_PlayAgent, gb, mode, stopEval, verbose);
    }


    public double simulate(PlayAgent playAgent){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITER);
        so.getCurrentPlayer().setChips(1000000);
        PlayAgent bsbja = new BasicStrategyBlackJackAgent();

        lastSimulated = playAgent;

        for(int i = 0; i < NUM_ITER; i++) {
            if (i % 1000 == 0)
                System.out.println("Iteraton" + i);
            while (!so.isRoundOver()) {
                int act = playAgent.getNextAction2(so.partialState(), false, true).toInt();
                if(bsbja.getNextAction2(so.partialState(), false, true).toInt() == act)
                    movesFromBasicStrategy++;
                moves++;
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
            avgPayOff += so.getGameScore(0);
            so.initRound();
        }
        return avgPayOff /= NUM_ITER;
    }

    public double evalAgentAvgPayoff(PlayAgent playAgent){
        if(!(playAgent == lastSimulated))
            simulate(playAgent);
        return lastResult = avgPayOff;
    }

    public double evalAgentMovesFromBasicStrategy(PlayAgent playAgent){
        if(!playAgent.equals(lastSimulated))
            simulate(playAgent);
        m_msg += "number of moves :" + moves;
        m_msg += "number of moves suggested by Basic Strategy : " + movesFromBasicStrategy;

        return lastResult = ((double)movesFromBasicStrategy)/((double)moves);
    }

    public double evalAgentInsurance(PlayAgent playAgent){
        m_msg += "Insurance-count : " + countInsuranceTaken + " this cost " + countNoInsuranceTaken + " * 10 = " + (countNoInsuranceTaken*10);
        m_msg += "noinsuranceCount: " + countNoInsuranceTaken;
        m_msg += "possible insuranceWins: " + possibleInsuranceWins;
        m_msg += "noInsuranceButBlackJack " + noInsurenceButBlackJack;
        m_msg += "insuranceSuccess : " + insurenceSuccess + " this payed back" + insurenceSuccess +  " * 30  = " + (insurenceSuccess*30);
        if(!playAgent.equals(lastSimulated))
            simulate(playAgent);

        return lastResult = 1 - (countNoInsuranceTaken/(countInsuranceTaken+countNoInsuranceTaken));
    }

    @Override
    protected boolean evalAgent(PlayAgent playAgent) {
        switch (m_mode){
            case 0:
                return evalAgentAvgPayoff(playAgent) > -0.2;
            case 1:
                return evalAgentMovesFromBasicStrategy(playAgent) > 0.9;
            case 2:
                return evalAgentInsurance(playAgent) > 0.9;
            default:
                m_msg = "no evaluation done";
                break;
        }
        return false;
    }

    @Override
    public int[] getAvailableModes() {
        return new int[] {0, 1, 2};
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
        return  "null";
    }

    @Override
    public String getTooltipString() {
        return "null";
    }

    @Override
    public String getPlotTitle() {
        return "null";
    }
}
