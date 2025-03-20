package games.RubiksCube.gui;

import ch.randelshofer.cubetwister.doc.NotationModel;
import ch.randelshofer.rubik.*;
import ch.randelshofer.rubik.parser.*;
import games.Arena;
import games.RubiksCube.GameBoardCube;
import games.RubiksCube.StateObserverCube;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameBoardCubeGuiPocket3D extends GameBoardCubeGui {
    // Window dimensions
    protected static final int RUBIKSGAMEHEIGHT = 400;
    protected static final int RUBIKSGAMEWIDTH = 600;

    private JCubeCanvasIdx3D cubeCanvas;
    private PocketCubeIdx3D pocketCube3D;
    private ScriptPlayer scriptPlayer;
    private ScriptParser scriptParser;
    private Notation notation;
    private boolean scriptApplied = false;
    private String lastScrambleApplied = "";
    private String lastSolutionApplied = "";

    public GameBoardCubeGuiPocket3D(GameBoardCube gameBoardCube) {
        super(gameBoardCube);
        // Set dimensions for 3D view
        boardX = 8;
        boardY = 6;
        buttonX = 3;
        buttonY = 3;

        // Set minimum size for the frame
        this.setMinimumSize(new Dimension(600, 500));

        initGameBoard();

        // Pack the frame to properly size components
        this.pack();

        // Initialize with default state
        this.updateBoard((StateObserverCube) this.gameBoardCube.getDefaultStartState(null), true, true);
    }

    @Override
    protected JPanel InitBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());  // Use BorderLayout instead of GridLayout
        panel.setBackground(Types.GUI_BGCOLOR);

        // Create main content panel with specific size
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(500, 400)); // Set preferred size
        contentPanel.setMinimumSize(new Dimension(400, 300));   // Set minimum size

        // Create 3D pocket cube
        pocketCube3D = new PocketCubeIdx3D();

        // Create and configure canvas
        cubeCanvas = new JCubeCanvasIdx3D();
        cubeCanvas.setCube3D(pocketCube3D);
        cubeCanvas.setCamera("Front");
        cubeCanvas.setPreferredSize(new Dimension(400, 300));

        // Add canvas to content panel
        contentPanel.add(cubeCanvas, BorderLayout.CENTER);

        // Initialize notation for the parser - important for 2x2 cube!
        notation = new NotationModel();
        ((NotationModel)notation).setLayerCount(2); // Set to 2 for Pocket Cube

        // Create script parser
        scriptParser = new ScriptParser(notation);

        // Create script player
        scriptPlayer = new ScriptPlayer();
        scriptPlayer.setCube3D(pocketCube3D);
        scriptPlayer.setCube(new PocketCube());
        scriptPlayer.setResetCube(new PocketCube());
        scriptPlayer.setCanvas(cubeCanvas);

        // Create controls panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.add(scriptPlayer.getControlPanelComponent());

        // Add a reset button to reset the view
        JButton resetViewButton = new JButton("Reset View");
        resetViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cubeCanvas != null) {
                    cubeCanvas.reset();
                }
            }
        });
        controlsPanel.add(resetViewButton);

        // Add a button to explicitly parse and play the script
        JButton playScriptButton = new JButton("Play Script");
        playScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyScriptToPlayer();
            }
        });
        controlsPanel.add(playScriptButton);

        // Add panels to main panel
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Explicitly apply the script to the player
     */
    private void applyScriptToPlayer() {
        try {
            String scramble = gameBoardCube.getLastScrambleSequence();
            String solution = gameBoardCube.getLastSolutionSequence();

            // Only process if we have some sequence to show
            if ((scramble != null && !scramble.trim().isEmpty()) ||
                    (solution != null && !solution.trim().isEmpty())) {

                // Combine into a single script with comments and proper formatting
                StringBuilder scriptBuilder = new StringBuilder();

                if (scramble != null && !scramble.trim().isEmpty()) {
                    //scriptBuilder.append("//scramble sequence\n");
                    scriptBuilder.append(scramble).append("\n");
                }

                if (solution != null && !solution.trim().isEmpty()) {
                    //scriptBuilder.append("//solution sequence\n");
                    scriptBuilder.append(solution).append("\n");
                }

                String scriptText = scriptBuilder.toString();
                System.out.println("Applying script: " + scriptText);

                // Parse the script text into a Node
                Node scriptNode = scriptParser.parse(scriptText);

                // Reset the player and set the new script
                scriptPlayer.stop();
                scriptPlayer.reset();
                scriptPlayer.setScript(scriptNode);

                scriptApplied = true;
                System.out.println("Script applied successfully");
            } else {
                System.out.println("No script sequence available to play");
            }
        } catch (Exception e) {
            System.err.println("Error applying script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void guiUpdateBoard(boolean enable, boolean showValueOnGameboard) {
        // Get the latest sequences from gameBoardCube
        String scramble = gameBoardCube.getLastScrambleSequence();
        String solution = gameBoardCube.getLastSolutionSequence();

        // Only apply script if sequences have changed or never applied
        if (!scriptApplied ||
                (scramble != null && !scramble.equals(lastScrambleApplied)) ||
                (solution != null && !solution.equals(lastSolutionApplied))) {

            applyScriptToPlayer();
            lastScrambleApplied = scramble;
            lastSolutionApplied = solution;
            scriptApplied = true;
        }

        // Keep the control buttons updated
        guiUpdateButton(enable, showValueOnGameboard);
    }

    @Override
    public void showGameBoard(Arena arena, boolean alignToMain) {
        this.setVisible(true);
        if (alignToMain) {
            int x = arena.m_ArenaFrame.getX();
            int y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() + 1;
            int width = (int)(Types.GUI_SCALING_FACTOR_Y * RUBIKSGAMEWIDTH);
            int height = (int)(Types.GUI_SCALING_FACTOR_Y * RUBIKSGAMEHEIGHT);
            this.setSize(width, height);
            this.setLocation(x, y);
        }

        // Make sure script gets applied when showing the game board
        scriptApplied = false;
    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        super.clearBoard(boardClear, vClear);

        // Reset script status when clearing the board
        if (boardClear) {
            scriptApplied = false;

            // Reset the 3D view as well
            if (scriptPlayer != null) {
                scriptPlayer.reset();
            }
        }
    }

    @Override
    public void dispose() {
        if (cubeCanvas != null) {
            cubeCanvas.dispose();
        }
        super.dispose();
    }
}