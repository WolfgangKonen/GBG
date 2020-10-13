package games.RubiksCube;

import games.BoardVector;
import org.junit.Test;

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

}
