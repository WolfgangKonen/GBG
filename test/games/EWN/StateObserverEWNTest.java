package games.EWN;

import games.BlackJack.StateObserverBlackJack;
import games.CFour.StateObserverC4;
import games.BoardVector;
import games.EWN.config.ConfigEWN;
import games.Hex.StateObserverHex;
import games.KuhnPoker.StateObserverKuhnPoker;
import games.Nim.StateObserverNim;
import games.Nim.StateObserverNim3P;
import games.Othello.StateObserverOthello;
import games.Poker.StateObserverPoker;
import games.RubiksCube.CubeConfig;
import games.RubiksCube.StateObserverCube;
import games.Sim.StateObserverSim;
import games.StateObservation;
import games.TicTacToe.StateObserverTTT;
import games.Yavalath.StateObserverYavalath;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import org.junit.Test;
import tools.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class StateObserverEWNTest {


    StateObserverEWN soEWN2P = new StateObserverEWN();
    StateObserverEWN soEWN3P = new StateObserverEWN();

    private void initSO2P(int size) {
        ConfigEWN.BOARD_SIZE = size;
        ConfigEWN.NUM_PLAYERS = 2;
        soEWN2P = new StateObserverEWN();
    }
    private void initSO3P(int size){
        ConfigEWN.BOARD_SIZE = size;
        ConfigEWN.NUM_PLAYERS = 3;
        soEWN3P = new StateObserverEWN();
    }


    /**
     * Apply xnf.symmetryVectors to a non-mirror-symmetric state borig --> result bvecs.
     * Apply it again to bvecs[1] --> result bback.
     * Check that borig == bback[1]
     */
    @Test
    public void testMirrorState(){
        XNTupleFuncsEWN xnf = new XNTupleFuncsEWN();
        initSO2P(3);
        soEWN2P.advance(new Types.ACTIONS(102), null);    // two advances to generate a state which is
        soEWN2P.advance((new Types.ACTIONS(704)), null);  // not mirror-symmetric

        BoardVector borig = xnf.getBoardVector(soEWN2P);
        BoardVector[] bvecs = xnf.symmetryVectors(borig, 2);
        BoardVector[] bback = xnf.symmetryVectors(bvecs[1], 2);
        assert bback[1].toString().equals(borig.toString()) :
                "bback[1] = " + bback[1].toString() + " and borig = " + borig.toString() + " differ!";
    }

    /**
     * Check getAllAvailActions: Printout size, optional printout of all actions, check that it does not contain doublets.
     */
    @Test
    public void testAllAvailActions(){
        HashSet<Types.ACTIONS> hset = new HashSet<>();
        initSO2P(3);
        ArrayList<Types.ACTIONS> ar = soEWN2P.getAllAvailableActions();
        System.out.println("ar.size = "+ar.size());
        for (Types.ACTIONS act : ar) {
            //System.out.println(act.toInt());
            hset.add(act);      // if there are doublets, hset will contain finally fewer elements than ar
        }

        assert ar.size()==hset.size() : "Error: getAllAvailableActions has doublets, since ArrayList has size "+ar.size()+
               ", but HashSet has only size "+hset.size();
    }

    /**
     * Check getAllAvailActions for different StateObservation actions: Are the actions sorted ascendingly
     */
    @Test
    public void checkSortingAllAvailActions(){
        ArrayList<StateObservation> soArr = new ArrayList<>();
        HashSet<Types.ACTIONS> hset = new HashSet<>();
        Types.ACTIONS act = null;
        soArr.add(new StateObserverBlackJack());
        soArr.add(new StateObserverC4());
        initSO2P(3);
        soArr.add(soEWN2P);
        soArr.add(new StateObserverHex());
        soArr.add(new StateObserverKuhnPoker());
        soArr.add(new StateObserverNim());
        soArr.add(new StateObserverNim3P());
        soArr.add(new StateObserverOthello());
        soArr.add(new StateObserverPoker());
        CubeConfig.cubeSize = CubeConfig.CubeSize.RUBIKS;
        CubeConfig.twistType = CubeConfig.TwistType.QTM;
        soArr.add(new StateObserverCube());
        soArr.add(new StateObserverSim());
        soArr.add(new StateObserverTTT());
        soArr.add(new StateObserverYavalath());
        soArr.add(new StateObserver2048());

        for (StateObservation so : soArr) {
            hset.clear();
            ArrayList<Types.ACTIONS> ar = so.getAllAvailableActions();
            ArrayList<Types.ACTIONS> sortedAr = (ArrayList<Types.ACTIONS>) ar.clone();
            Collections.sort(sortedAr);
            System.out.println("\n"+ so.getClass().getName());
            System.out.println("ar.size = "+ar.size());
            for (int i=0; i<ar.size(); i++) {
                act = ar.get(i);
                System.out.println(i + ": "+ act.toInt());
                assert act.toInt()==sortedAr.get(i).toInt() : "Error: ar is not sorted at index i="+i;
                hset.add(act);      // if there are doublets, hset will contain finally fewer elements than ar
            }
            if (act.toInt()==ar.size()-1) {
                System.out.println("... is consecutive");
            } else {
                System.out.println(("... is NOT consecutive"));
            }

            assert ar.size()==hset.size() : "Error: getAllAvailableActions has doublets, since ArrayList has size "+ar.size()+
                    ", but HashSet has size "+hset.size();
        }
    }
}
