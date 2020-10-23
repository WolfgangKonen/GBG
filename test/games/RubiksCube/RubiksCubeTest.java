package games.RubiksCube;

import org.junit.Test;
import java.util.HashSet;

/**
 * This class derived from {@link PocketCubeTest} is - after a different init() - capable
 * to do the same tests as {@link PocketCubeTest}, but for Rubik's Cube.
 */
public class RubiksCubeTest extends PocketCubeTest {
    protected void init() {
        CubeConfig.cubeType = CubeConfig.CubeType.RUBIKS;

        gb = new GameBoardCube(ar);

        CubeStateFactory.generateInverseTs();
        CubeState.generateForwardTs();
    }

    /**
     *    Test for several randomly twisted CubeStates: Does a transformation from CubeState to STICKER board vector
     *    and back to CubeState result in the original CubeState again?
     */
    @Test
    public void testCS_STICKER_transform() {
        System.out.println("testCS_STICKER_transform not implemented!");
    }

    /**
     * Twist-test:
     *
     * Test that rep x FTw.UTw.LTw followed by rep x the inverse leads to default cube again, where rep is a random int. <br>
     */
    @Test
    public void test_DTw_RTw_BTw() {

        init();

        CubeState def = csFactory.makeCubeState();
        CubeState rot = csFactory.makeCubeState(def);
        int runs = 3;
        for (int r=0; r<runs; r++) {
            int rep = 1+rand.nextInt(5);
            for (int k=0; k<rep; k++) rot.DTw(1).RTw(1).BTw(1);
            for (int k=0; k<rep; k++) rot.BTw(3).RTw(3).DTw(3);
            assert (def.isEqual(rot)) : "def and rot differ after rep x FTw.UTw.LTw!";
        }
    }

    /**
     * Test that some adjacency sets in representation STICKER2 are as expected.
     */
    @Test
    public void testAdjacencySets() {
        HashSet<Integer> h5;
        init();

        XNTupleFuncsCube xnf = new XNTupleFuncsCube();
        CubeConfig.boardVecType = CubeConfig.BoardVecType.STICKER2;
        h5 = xnf.adjacencySet(5);
        assert (!h5.contains(5)) : "adjacency set for cell 5 should not contain cell 5";
        assert (h5.stream().max(Integer::compare).get()<16) : "adjacency set for cell 5 has not all elements < 16";
        System.out.println(h5 + " --> OK");
        h5 = xnf.adjacencySet(20);
        assert (!h5.contains(20)) : "adjacency set for cell 20 should not contain cell 20";
        assert (h5.stream().min(Integer::compare).get()>15) : "adjacency set for cell 20 has not all elements > 15";
        System.out.println(h5 + " --> OK");
    }


}