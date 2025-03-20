package games.RubiksCube;

import controllers.PlayAgent;
import games.*;
import games.RubiksCube.CubeConfig.BoardVecType;
import games.RubiksCube.CubeConfig.CubeSize;
import games.RubiksCube.CubeConfig.TwistType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * {@link Arena} for Rubik's Cube. It borrows all functionality
 * from the general class {@link Arena} . It only overrides
 * the abstract methods <ul>
 * <li> {@link Arena#makeGameBoard()},
 * <li> {@link Arena#makeEvaluator(PlayAgent, GameBoard, int, int)}, and
 * <li> {@link Arena#makeFeatureClass(int)},
 * </ul> such that
 * these factory methods return objects of class {@link GameBoardCube},
 * {@link EvaluatorCube}, and {@link FeatureCube}, respectively.
 *
 * @author Wolfgang Konen, TH Koeln, 2018-2021
 * @see GameBoardCube
 * @see EvaluatorCube
 */
public class ArenaCube extends Arena {

    public ArenaCube(String title, boolean withUI) {
        super(title, withUI);
        initialize();
    }

    public ArenaCube(String title, boolean withUI, boolean withTrainRights) {
        super(title, withUI, withTrainRights);
        initialize();
    }

    private void initialize() {
        CubeStateFactory.generateInverseTs();
        CubeState.generateForwardTs();
        CubeStateMap.allWholeCubeRots = new CubeStateMap(CubeStateMap.CsMapType.AllWholeCubeRotTrafos);
        ColorTrafoMap allCT = new ColorTrafoMap(ColorTrafoMap.ColMapType.AllColorTrafos);
        // we need to recalculate allWholeCubeRots and allCT (in case that CubeConfig.cubeSize has changed (!))
    }

    /**
     * @return a name of the game, suitable as subdirectory name in the
     * {@code agents} directory
     * @see GameBoardCube#getSubDir()
     */
    @Override
    public String getGameName() {
        return "RubiksCube";
    }

    /**
     * Factory pattern method
     */
    @Override
    public GameBoard makeGameBoard() {
        CubeStateFactory.generateInverseTs();        // since makeGameBoard is called via super --> Arena
        CubeState.generateForwardTs();                        // prior to finishing ArenaCube(String,boolean)
        gb = new GameBoardCube(this);

        // optional debug info: print out invU and invL, given invF:
        boolean SHOW_INV = false;
        if (SHOW_INV) {
            CubeStateFactory csfactory = new CubeStateFactory();
            csfactory.makeCubeState().show_invF_invL_invU();
            // once to print out the arrays needed for invL and invU (see CubeState2x2, CubeState3x3)
        }
        return gb;
    }

    /**
     * Factory pattern method: make a new Evaluator
     *
     * @param pa      the agent to evaluate
     * @param gb      the game board
     * @param mode    which evaluator mode: -1,0,1. Throws a runtime exception
     *                if {@code mode} is not in the set {@link Evaluator#getAvailableModes()}.
     * @param verbose how verbose or silent the evaluator is
     * @return the new evaluator
     */
    @Override
    public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int mode, int verbose) {
        return new EvaluatorCube(pa, gb, mode, verbose);
    }

    @Override
    public Feature makeFeatureClass(int featmode) {
        return new FeatureCube(featmode);
    }

    @Override
    public XNTupleFuncs makeXNTupleFuncs() {
        return new XNTupleFuncsCube();
    }

    @Override
    public String gameOverString(StateObservation so, ArrayList<String> agentList) {
        // Always update and save sequences when this method is called,
        // whether from isGameOver() or from a manual timeout
        if (so instanceof StateObserverCube && gb instanceof GameBoardCube) {
            ((GameBoardCube) gb).updateLastSequences((StateObserverCube) so);
            saveCubeSequences(so);
        }

        // Call the original method
        return super.gameOverString(so, agentList);
    }

    /**
     * set the cube type (POCKET or RUBIKS) for Rubik's Cube
     */
    public static void setCubeSize(String sCube) {
        switch (sCube) {
            case "2x2x2" -> CubeConfig.cubeSize = CubeSize.POCKET;
            case "3x3x3" -> CubeConfig.cubeSize = CubeSize.RUBIKS;
            default -> throw new RuntimeException("Cube type " + sCube + " is not known.");
        }
        CubeStateMap.allWholeCubeRots = new CubeStateMap(CubeStateMap.CsMapType.AllWholeCubeRotTrafos);
        ColorTrafoMap allCT = new ColorTrafoMap(ColorTrafoMap.ColMapType.AllColorTrafos);
        // we need to recalculate allWholeCubeRots and allCT if CubeConfig.cubeSize has changed
    }

    /**
     * set the board vector type for Rubik's Cube
     */
    public static void setBoardVecType(String bvType) {
        switch (bvType) {
            case "CSTATE" -> CubeConfig.boardVecType = BoardVecType.CUBESTATE;
            case "CPLUS" -> CubeConfig.boardVecType = BoardVecType.CUBEPLUSACTION;
            case "STICKER" -> CubeConfig.boardVecType = BoardVecType.STICKER;
            case "STICKER2" -> CubeConfig.boardVecType = BoardVecType.STICKER2;
            default -> throw new RuntimeException("Board vector type " + bvType + " is not known.");
        }
    }

    /**
     * set the twist type (HTM = half-turn metric or QTM = quarter-turn metric) for Rubik's Cube
     */
    public static void setTwistType(String tCube) {
        switch (tCube) {
            case "HTM" -> CubeConfig.twistType = TwistType.HTM;
            case "QTM" -> CubeConfig.twistType = TwistType.QTM;
            default -> throw new RuntimeException("Twist type " + tCube + " is not known.");
        }
    }

    public static void setVisualizationType(String visType) {
        switch (visType) {
            case "2D" -> CubeConfig.visualizationType = CubeConfig.VisualizationType.TWOD;
            case "3D" -> CubeConfig.visualizationType = CubeConfig.VisualizationType.THREED;
            default -> throw new RuntimeException("Visualization type " + visType + " is not known.");
        }
    }

    /**
     * Method to save Cube scramble- and played-moves-sequences to a log file
     *
     * @param so stateObserver providing scramble and move sequences
     */
    private void saveCubeSequences(StateObservation so) {
        if (so instanceof StateObserverCube soCube) {
            String scrambleSequence = soCube.getScrambleSequence();
            String solutionSequence = soCube.getMoveSequence();

            // Only proceed if we have at least a scramble sequence
            if (scrambleSequence == null || scrambleSequence.trim().isEmpty()) {
                System.out.println("No scramble sequence available to save");
                return;
            }

            // Create a directory if it doesn't exist
            String dirPath = "logs/" + getGameName();
            if (gb.getSubDir() != null) dirPath += "/" + gb.getSubDir();
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            // Create a file with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = dirPath + "/cube_sequence_" + timestamp + ".txt";

            try (FileWriter writer = new FileWriter(filePath)) {
                // Write both sequences to the file
                writer.write("//Scramble Sequence:\n");
                writer.write(scrambleSequence + "\n\n");

                if (solutionSequence != null && !solutionSequence.trim().isEmpty()) {
                    writer.write("//Solution Sequence:\n");
                    writer.write(solutionSequence + "\n\n");
                } else {
                    writer.write("//No Solution Sequence (Game Timed Out)\n\n");
                }

                // Add some statistics
                writer.write("//Statistics:\n");
                writer.write("//Scramble Length: " + countMoves(scrambleSequence) + " moves\n");
                if (solutionSequence != null && !solutionSequence.trim().isEmpty()) {
                    writer.write("//Solution Length: " + countMoves(solutionSequence) + " moves\n");
                } else {
                    writer.write("//Solution: Failed to solve within move limit\n");
                }
                writer.write("//Game Status: " + (so.isGameOver() ? "Solved" : "Timed Out") + "\n");

                System.out.println("Cube sequences saved to: " + filePath);
            } catch (IOException e) {
                System.err.println("Error saving cube sequences: " + e.getMessage());
            }
        }
    }

    /**
     * Helper method to count the actual number of moves in a cube sequence
     *
     * @param sequence The move sequence to count
     * @return The number of moves in the sequence
     */
    protected int countMoves(String sequence) {
        if (sequence == null || sequence.isEmpty()) {
            return 0;
        }

        // Split by whitespace and count non-empty elements
        String[] moves = sequence.trim().split("\\s+");
        return moves.length;
    }
}