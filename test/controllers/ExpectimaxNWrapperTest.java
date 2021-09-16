package controllers;

import games.EWN.GameBoardEWN;
import games.EWN.StateObserverEWN;
import org.junit.Test;
import starters.GBGBatch;
import tools.Types;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ExpectimaxNWrapperTest extends GBGBatch {

    /**
     * This test checks for 2-player 3x3 EWN whether state {@code so} has an ExpectimWrapper_(nply+1) best value that is
     * opposite to the value that we get when we weight-average the afterstate values of an ExpectimaxWrapper_(nply)
     * for all possible next non-deterministic dice values.
     * <p>
     * [The reason for this relation is the recursive nature of the Expectimax tree. Why 'opposite'? - Because EWN is a
     * 2-player zero-sum game.]
     * <p>
     * Why "Wrap3"? - The state {@code so} loops through the 3 states "start state + one of the  3 possible dice values".
     * <p>
     * The test can only succeed, if the wrapped agent is *deterministic*.
     * Here we use a TDNTuple4Agt loaded from disk.
     */
    @Test
    public void expectimaxWrap3Test() {
        PlayAgent pa,qaDeep,qaShallow;
        boolean silent = true;

        String selectedGame = "EWN";
        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);  // for EWN currently: 3x3 2-player
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        String strAgent = Types.GUI_DEFAULT_DIR_AGENT + "/test_TD4_EWN.zip.agt.zip";
        GameBoardEWN gb = new GameBoardEWN(t_Game); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;

        boolean res = t_Game.loadAgent(0, strAgent);
        assert res : "\n[ExpectimaxNWrapperTest] Aborted: agtFile = "+ strAgent + " not found!";

        String sAgent = t_Game.m_xab.getSelectedAgent(0);
        pa = t_Game.m_xfun.fetchAgent(0,sAgent, t_Game.m_xab);
        //pa = new MCAgentN(new ParMC());       // just a check: with a nondeterministic wrapped agent, the test will fail

        for (int nply=1; nply<4; nply++) {
            System.out.println("\n*** Test with nply =  "+nply+ " starts ***");
            qaDeep = new ExpectimaxNWrapper(pa,nply+1);
            qaShallow = new ExpectimaxNWrapper(pa,nply);

            // select a state:
            gb.clearBoard(true,true);
            startSO = (StateObserverEWN) gb.getStateObs();
            ArrayList<Types.ACTIONS> startRandoms = startSO.getAvailableRandoms();

            for (Types.ACTIONS startR : startRandoms) {
                startSO.advanceNondeterministic(startR);


                //System.out.println("\n*** Episode with dice value "+startR.toInt()+ " starts ***");
                //System.out.print(startSO);

                so = (StateObserverEWN) startSO.copy();

                innerEWrapTest(qaDeep,qaShallow,so,silent,"dice value="+startR.toInt());

            }

        }
        System.out.println("[expectimaxWrap3Test] finished");
    }

    /**
     * This test checks for 2-player 3x3 EWN whether state {@code so} has an Expectimax best value that is the opposite
     * of the value that we get when we take the afterstate resulting from the best action and weight-average the
     * afterstate values for all possible next non-deterministic dice values.
     * <p>
     * [The reason for this relation is the recursive nature of the Expectimax tree. Why 'opposite'? - Because EWN is a
     * 2-player zero-sum game.]
     * <p>
     * Why "Wrap9"? - The state {@code so} loops through the 9 states that we get when we advance from "start state +
     * one of the 3 possible dice values" with one of the 3 available actions.
     * <p>
     * The test can only succeed, if the wrapped agent is *deterministic*.
     * Here we use a TDNTuple4Agt loaded from disk.
     */
    @Test
    public void expectimaxWrap9Test() {
        PlayAgent pa,qaDeep,qaShallow;
        boolean silent = true;

        String selectedGame = "EWN";
        String[] scaPar = GBGBatch.setDefaultScaPars(selectedGame);  // for EWN currently: 3x3 2-player
        t_Game = GBGBatch.setupSelectedGame(selectedGame,scaPar);   // t_Game is ArenaTrain object
        String strAgent = Types.GUI_DEFAULT_DIR_AGENT + "/test_TD4_EWN.zip.agt.zip";
        GameBoardEWN gb = new GameBoardEWN(t_Game); //,3,2);		// needed for chooseStartState()
        StateObserverEWN startSO, so;
        DecimalFormat frmAct = new DecimalFormat("0000");

        boolean res = t_Game.loadAgent(0, strAgent);
        assert res : "\n[ExpectimaxNWrapperTest] Aborted: agtFile = "+ strAgent + " not found!";

        String sAgent = t_Game.m_xab.getSelectedAgent(0);
        pa = t_Game.m_xfun.fetchAgent(0,sAgent, t_Game.m_xab);
        //pa = new MCAgentN(new ParMC());       // just a check: with a nondeterministic wrapped agent, the test will fail

        for (int nply=1; nply<4; nply++) {
            System.out.println("\n*** Test with nply =  "+nply+ " starts ***");
            qaDeep = new ExpectimaxNWrapper(pa,nply+1);
            qaShallow = new ExpectimaxNWrapper(pa,nply);

            // select a state:
            gb.clearBoard(true, true);
            startSO = (StateObserverEWN) gb.getStateObs();
            ArrayList<Types.ACTIONS> startRandoms = startSO.getAvailableRandoms();

            for (Types.ACTIONS startR : startRandoms) {
                startSO.advanceNondeterministic(startR);
                ArrayList<Types.ACTIONS> nextActions = startSO.getAvailableActions();

                System.out.print(startSO);

                for (Types.ACTIONS a : nextActions) {
                    //System.out.println("\n*** Episode with action " + frmAct.format(a.toInt()) + " starts ***");

                    so = (StateObserverEWN) startSO.copy();
                    so.advance(a);

                    innerEWrapTest(qaDeep,qaShallow,so,silent,"action=" + frmAct.format(a.toInt()));

                }
            }
        }
        System.out.println("[expectimaxWrap9Test] finished");
    }

    private void innerEWrapTest(PlayAgent qaDeep, PlayAgent qaShallow, StateObserverEWN so, boolean silent, String text) {
        StateObserverEWN afterstate, newSO;
        Types.ACTIONS_VT act_pa = qaDeep.getNextAction2(so.partialState(),false,silent);
        double vA = act_pa.getVBest();
        afterstate = (StateObserverEWN) so.copy();
        afterstate.advance(act_pa);

        ArrayList<Types.ACTIONS> nextRandoms = afterstate.getAvailableRandoms();
        double vNew=0.0;
        double prob;
        for (Types.ACTIONS r : nextRandoms) {
            newSO = (StateObserverEWN) afterstate.copy();
            newSO.advanceNondeterministic(r);
            act_pa = qaShallow.getNextAction2(newSO.partialState(),false,silent);
            prob = afterstate.getProbability(r);
            vNew += act_pa.getVBest()*prob;
        }
        System.out.println(text+":  vA = "+vA+",  vNew = "+vNew);
        assert Math.abs(vA+vNew)<1e-8 : "Assertion vA+vNew=0 failed!";
    }

}
