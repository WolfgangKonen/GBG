package games.RubiksCube.GUI;

import games.Arena;
import games.RubiksCube.GameBoardCube;
import games.RubiksCube.StateObserverCube;
import tools.Types;
import javax.swing.*;
import java.awt.*;

public class GameBoardCubeGui3x3x3 extends GameBoardCubeGui {
    // Window dimensions
    protected static final int RUBIKSGAMEHEIGHT = 400;
    protected static final int RUBIKSGAMEWIDTH = 600;

    public GameBoardCubeGui3x3x3(GameBoardCube gameBoardCube) {
        super(gameBoardCube);
        // Set dimensions for 3D view
        boardX = 12;
        boardY = 9;
        buttonX = 0;  // 0 hides the button panel
        buttonY = 0;  // 0 hides the button panel

        initGameBoard();

        // Initialize with default state
        this.updateBoard((StateObserverCube) this.gameBoardCube.getDefaultStartState(null), true, true);
    }

    @Override
    protected JPanel InitBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());  // Different layout for 3D
        panel.setBackground(Types.GUI_BGCOLOR);

        // TODO: Add 3D rendering panel here

        return panel;
    }

    @Override
    protected void guiUpdateBoard(boolean enable, boolean showValueOnGameboard) {
        // Will need to update 3D model state
        int faceColorLength = gameBoardCube.stateObserverCube.getCubeState().get_fcol_length();
        //                   U         L        F         D          R         B
        Color[] colors = {Color.white, colBlue, colOrang, colYellow, colGreen, colRed};

        // TODO: Update 3D model colors and state

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
}