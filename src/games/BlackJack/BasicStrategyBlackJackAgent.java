package games.BlackJack;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.*;
import javax.swing.plaf.nimbus.State;

public class BasicStrategyBlackJackAgent extends AgentBase implements PlayAgent {


    public BasicStrategyBlackJackAgent(){
        super("BSBJA");
        setAgentState(AgentState.TRAINED);
    }


    @Override
    public boolean isTrainable(){
        return false;
    }

    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
        StateObserverBlackJack m_so;
        int actVal = -1;

        if(sob instanceof StateObserverBlackJack) {
            m_so = (StateObserverBlackJack) sob;
        }else{
            throw new RuntimeException("Agent can only play BlackJack");
        }

        double vtable[] = new double[m_so.getNumAvailableActions()];

        for(int i = 0; i < vtable.length; i++){
            vtable[i] = 0;
        }

        /**
         *  Basic Strategy does not give any intell on how much players should bet.
         *  Betsize will be random
         */
        if(m_so.getCurrentPhase() == StateObserverBlackJack.gamePhase.BETPHASE){
            Random r = new Random();
            actVal = r.ints(0, m_so.getAvailableActions().size()).findFirst().getAsInt();
            vtable = setBestMoveInVtable(m_so, vtable, actVal);
            return new Types.ACTIONS_VT(actVal, true, vtable, 100);
        }

        /**
         *  Players should never take insurance
         */
        if(m_so.getCurrentPhase() == StateObserverBlackJack.gamePhase.ASKFORINSURANCE){
            actVal = StateObserverBlackJack.BlackJackActionDet.NOINSURANCE.getAction();
            vtable = setBestMoveInVtable(m_so, vtable, actVal);
            return new Types.ACTIONS_VT(actVal, false, vtable, 100);
        }


        Player me = m_so.getCurrentPlayer();
        if(me.getActiveHand().isPair()){
            actVal = lookUpBestMovePair(m_so);
            vtable = setBestMoveInVtable(m_so, vtable, actVal);
            return new Types.ACTIONS_VT(actVal, false, vtable, 100);
        }
        else if(me.getActiveHand().isSoft()){
            actVal = lookUpBestMoveSoft(m_so);
            vtable = setBestMoveInVtable(m_so, vtable, actVal);
            return new Types.ACTIONS_VT(actVal, false, vtable, 100);
        }
        else{//hard
            actVal = lookUpBestMoveHard(m_so);
            vtable = setBestMoveInVtable(m_so, vtable, actVal);
            return new Types.ACTIONS_VT(actVal, false, vtable, 100);
        }

    }

    public double[] setBestMoveInVtable(StateObserverBlackJack so, double vTable[], int actionVal){
        ArrayList<Types.ACTIONS> availableActions = so.getAvailableActions();
        for(int i = 0; i<availableActions.size(); i++){
            if(availableActions.get(i).toInt() == actionVal) {
                vTable[i] = 100;
                break;
            }
        }
        return vTable;
    }

    public int lookUpBestMoveSoft(StateObserverBlackJack so){
        Hand myHand = so.getCurrentPlayer().getActiveHand();
        Hand dealersHand = so.getDealer().getActiveHand();
        if(myHand.getHandValue() < 13){
            return lookUpBestMoveHard(so);
        }
        //read from chart, 2 dimensional Array starts at i = 0 j = 0 so indicies need a substraction
        BasicStrategyChart.Move move = BasicStrategyChart.softHand[myHand.getHandValue() - 13][dealersHand.getCards().get(0).rank.getValue() - 2];
        return getLegalAction(so, move);
    }

    public int lookUpBestMoveHard(StateObserverBlackJack so){
        Hand myHand = so.getCurrentPlayer().getActiveHand();
        Hand dealersHand = so.getDealer().getActiveHand();

        BasicStrategyChart.Move move = BasicStrategyChart.hardHand[myHand.getHandValue() - 5][dealersHand.getCards().get(0).rank.getValue() - 2];
        return getLegalAction(so, move);
    }

    public int lookUpBestMovePair(StateObserverBlackJack so){
        Hand myHand = so.getCurrentPlayer().getActiveHand();
        Hand dealersHand = so.getDealer().getActiveHand();
        //if split is not possible (not enough chips) we can treat it as a normal hand
        if(!so.getAvailableActions().contains(Types.ACTIONS.fromInt(StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction()))){
            // rare case hand = 2 , 2 not enough chips for a split
            // we can not pass this to the hard hand chart because we substract 5 from the handvalue
            if(myHand.getCards().get(0).rank == Card.Rank.TWO){
                return StateObserverBlackJack.BlackJackActionDet.HIT.getAction();
            }
            lookUpBestMoveHard(so);
        }

        BasicStrategyChart.Move move = BasicStrategyChart.pairHand[myHand.getCards().get(0).rank.getValue()-2][dealersHand.getCards().get(0).rank.getValue() - 2];
        return getLegalAction(so, move);
    }

    public int getLegalAction(StateObserverBlackJack so, BasicStrategyChart.Move move){
         /*
            H	Hit
            S	Stand
            P	Split
            Dh	Double Down if possible, otherwise Hit
            Ds	Double Down if possible, otherwise Stand
            Rh 	Surrender if possible, otherwise Hit
        */
        int numAction = -1;
        switch(move){
            case H:
                numAction = StateObserverBlackJack.BlackJackActionDet.HIT.getAction();
                break;
            case S:
                numAction = StateObserverBlackJack.BlackJackActionDet.STAND.getAction();
                break;
            case P:
                numAction = StateObserverBlackJack.BlackJackActionDet.SPLIT.getAction();
                break;
            case Dh:
                if(so.getAvailableActions().contains(Types.ACTIONS.
                        fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))) {
                    numAction = StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction();
                }else{
                    numAction = StateObserverBlackJack.BlackJackActionDet.HIT.getAction();
                }
                break;
            case Ds:
                if(so.getAvailableActions().contains(Types.ACTIONS.
                        fromInt(StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction()))) {
                    numAction = StateObserverBlackJack.BlackJackActionDet.DOUBLEDOWN.getAction();
                }else{
                    numAction = StateObserverBlackJack.BlackJackActionDet.STAND.getAction();
                }
                break;
            case Rh:
                if(so.getAvailableActions().contains(Types.ACTIONS.
                        fromInt(StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction()))) {
                    numAction = StateObserverBlackJack.BlackJackActionDet.SURRENDER.getAction();
                }else{
                    numAction = StateObserverBlackJack.BlackJackActionDet.HIT.getAction();
                }
                break;
        }

        if(!so.getAvailableActions().contains(Types.ACTIONS.fromInt(numAction))){
            throw new RuntimeException("Basic Strategy Agent could not find his desired action");
        }
        return numAction;
    }

}
