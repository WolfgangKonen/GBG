package games.BlackJack;

import games.StateObservation;
import org.junit.Test;

import java.util.Random;

public class StateObserverBlackJackTest {

    // tests if anytime the dealer holds an ace the ASKFORINSURANCE phase is entered
    @Test
    public void testAskForInsurance(){
        StateObserverBlackJack so = new StateObserverBlackJack(1, 100000);
        Random m_rnd = new Random();
        int askForInsuranceCounter = 0;
        int dealerHoldsAce = 0;
        for(int i = 0; i < 100000; i++){
            while(!so.isRoundOver()){
                int action = m_rnd.nextInt(so.getNumAvailableActions());
                so.advance(so.getAction(action), null);
                if(so.getCurrentPhase() == StateObserverBlackJack.gamePhase.ASKFORINSURANCE)
                    askForInsuranceCounter++;
            }
            if(so.getDealer().getActiveHand().getCards().get(0).rank == Card.Rank.ACE){
               dealerHoldsAce++;
            }
            so.initRound();
        }

        System.out.println("askForInsuranceCounter:  "  + askForInsuranceCounter);
        System.out.println("dealerHoldsAceCounter:  "  + dealerHoldsAce);
        assert askForInsuranceCounter == dealerHoldsAce: "must be same";
    }

    @Test
    public void testGameScore(){
        Random m_rnd = new Random();
        StateObserverBlackJack so = new StateObserverBlackJack(1, 1000);
        so.getPlayers()[0].setChips(10000);
        for(int i = 0; i < 1000; i++){
            while(!so.isRoundOver()){
                int randomAction = m_rnd.nextInt(so.getNumAvailableActions());
                so.advance(so.getAction(randomAction), null);
            }
            assert so.getGameScoreTuple().scTup.length == 1: "scoretuple should have length 1";
            double scoreOnRoundOver = so.getGameScoreTuple().scTup[0];
            so.initRound();
            double scoreAfterInitRound = so.getGameScoreTuple().scTup[0];
            assert so.getGameScoreTuple().scTup.length == 1: "scoretuple should have length 1";
            assert scoreOnRoundOver == scoreAfterInitRound: "scores at roundover and score after init round should be equal";
            StateObservation aCopy = so.copy();
            assert so.getGameScoreTuple().scTup[0] == so.getGameScoreTuple().scTup[0]: "a copy should have the same gamescore";

        }

    }
}
