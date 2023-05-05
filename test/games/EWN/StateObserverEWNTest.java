package games.EWN;

import games.BoardVector;
import games.EWN.config.ConfigEWN;
import org.junit.Test;
import tools.Types;

public class StateObserverEWNTest {


    StateObserverEWN soEWN2P = new StateObserverEWN();
    StateObserverEWN soEWN3P = new StateObserverEWN();

    private void initSO2P(int size) {
        ConfigEWN.BOARD_SIZE = size;
        ConfigEWN.NUM_PLAYERS = 2;
        soEWN2P = new StateObserverEWN();
    }
    private void initSO3P(){
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
        soEWN2P.advance(new Types.ACTIONS(102));    // two advances to generate a state which is
        soEWN2P.advance((new Types.ACTIONS(704)));  // not mirror-symmetric

        BoardVector borig = xnf.getBoardVector(soEWN2P);
        BoardVector[] bvecs = xnf.symmetryVectors(borig, 2);
        BoardVector[] bback = xnf.symmetryVectors(bvecs[1], 2);
        assert bback[1].toString().equals(borig.toString()) :
                "bback[1] = " + bback[1].toString() + " and borig = " + borig.toString() + " differ!";
    }

}
