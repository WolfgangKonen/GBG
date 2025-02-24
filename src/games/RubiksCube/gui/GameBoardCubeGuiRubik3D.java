package games.RubiksCube.gui;

import ch.randelshofer.rubik.*;
import ch.randelshofer.rubik.parser.ScriptPlayer;
import games.Arena;
import games.RubiksCube.CubeConfig;
import games.RubiksCube.GameBoardCube;
import games.RubiksCube.StateObserverCube;
import tools.Types;
import javax.swing.*;
import java.awt.*;

public class GameBoardCubeGuiRubik3D extends GameBoardCubeGui {
    // Window dimensions
    protected static final int RUBIKSGAMEHEIGHT = 400;
    protected static final int RUBIKSGAMEWIDTH = 600;

    private JCubeCanvasIdx3D cubeCanvas;
    private RubiksCubeIdx3D rubiksCube3D;
    private ScriptPlayer scriptPlayer;

    public GameBoardCubeGuiRubik3D(GameBoardCube gameBoardCube) {
        super(gameBoardCube);
        // Set dimensions for 3D view
        boardX = 12;
        boardY = 9;
        buttonX = 3;  // 0 hides the button panel
        buttonY = 6;  // 0 hides the button panel

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
        rubiksCube3D = new RubiksCubeIdx3D();
        //rubiksCube3D.setStickerBeveling(0.005859375F);

        // Create script player
        scriptPlayer = new ScriptPlayer();
        scriptPlayer.setCube3D(rubiksCube3D);
        scriptPlayer.setCube(new RubiksCube());
        scriptPlayer.setResetCube(new RubiksCube());

        // Create and configure canvas
        cubeCanvas = new JCubeCanvasIdx3D();
        cubeCanvas.setCamera("Front");
        cubeCanvas.setCube3D(rubiksCube3D);
        cubeCanvas.setPreferredSize(new Dimension(400, 300));

        // Add canvas to content panel
        contentPanel.add(cubeCanvas, BorderLayout.CENTER);

        // Create controls panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.add(scriptPlayer.getControlPanelComponent());

        // Add panels to main panel
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(controlsPanel, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    protected void guiUpdateBoard(boolean enable, boolean showValueOnGameboard) {
        // TODO: what even lmao
        // Update 3D model state
        int faceColorLength = gameBoardCube.stateObserverCube.getCubeState().get_fcol_length();
        //                    U          L         F          D         R        B
        Color[] colors = {Color.white, colBlue, colOrang, colYellow, colGreen, colRed};

        // Update cube state
        if (rubiksCube3D != null && scriptPlayer != null) {
            RubiksCube cube = (RubiksCube) scriptPlayer.getCube();
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
    }

    @Override
    public void dispose() {
        if (cubeCanvas != null) {
            cubeCanvas.dispose();
        }
        super.dispose();
    }
}