package games.BlackJack;
import controllers.MC.MCAgentN;
import games.StateObservation;
import org.junit.Test;
import params.ParMC;
import tools.Types;

public class MCNRoundOverTrueFalse {
    public final int NUM_ITERATIONS = 31;

    public StateObserverBlackJack initSo(Card firstCardPlayer, Card secondCardPlayer, Card upCardDealer){
        StateObserverBlackJack so = new StateObserverBlackJack(1, NUM_ITERATIONS);
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

    @Test
    public void getValues(){
        ParMC stop = new ParMC();
        stop.setStopOnRoundOver(true);
        stop.setIterations(3000);
        stop.setNumAgents(1);
        stop.setCalcCertainty(false);
        ParMC noStop = new ParMC(stop);
        noStop.setStopOnRoundOver(false);



        StateObserverBlackJack so = initSo(new Card(Card.Rank.THREE, Card.Suit.SPADE), new Card(Card.Rank.TWO, Card.Suit.SPADE),
                new Card(Card.Rank.SIX, Card.Suit.CLUB)); //bestmove hit

        for(int i = 0; i < NUM_ITERATIONS; i++){
            noStop.setRolloutDepth(i);
            MCAgentN MCnoStop = new MCAgentN(noStop);
            MCAgentN MCStop = new MCAgentN(stop);
            Types.ACTIONS_VT resultNoStop = MCnoStop.getNextAction2(so, true, true);
            Types.ACTIONS_VT resultStop = MCStop.getNextAction2(so, true, true);
            //for(int f = 1; f < 2; f++){
            //    System.out.println(resultNoStop.getVTable()[f]+ "," + StateObserverBlackJack.BlackJackActionDet.values()[so.getAvailableActions().get(f).toInt()].name()+","+i);
            //}

               // System.out.println(resultStop.toInt());
            //System.out.println(resultNoStop.getVBest()+ "," + StateObserverBlackJack.BlackJackActionDet.values()[resultNoStop.toInt()].name()+","+i);
            System.out.println(resultStop.getVBest()+ "," + StateObserverBlackJack.BlackJackActionDet.values()[resultStop.toInt()].name()+","+i);

        }

    }
}
