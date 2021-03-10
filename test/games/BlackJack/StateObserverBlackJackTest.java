package games.BlackJack;

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
                so.advance(so.getAction(action));
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
}
