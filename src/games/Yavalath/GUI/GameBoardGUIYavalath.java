package games.Yavalath.GUI;

import games.Arena;
import games.Yavalath.*;
import tools.Types;

import static games.Yavalath.ConfigYavalath.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameBoardGUIYavalath {
    final static Color COLOR_PLAYER_ONE = Color.WHITE;
    final static Color COLOR_PLAYER_TWO = Color.BLACK;
    final static Color COLOR_PLAYER_THREE = Color.RED;
    final static Color CELL_BACKGROUND = Color.YELLOW;
    final static Color CELL_LINING = Color.ORANGE;
    final static Color BACKGROUND_COLOR = Color.GRAY;

    private int WINDOW_HEIGHT;
    private int WINDOW_WIDTH;

    private boolean drawValues;

    private JFrame frame = new JFrame("GBG - Yavalath");
    private YavalathPanel panel;

    private GameBoardYavalath gb;

    public GameBoardGUIYavalath(GameBoardYavalath gb) {
        this.gb = gb;
        initializeFrame();
    }

    private void initializeFrame(){
        panel = new YavalathPanel();

        frame.setVisible(true);
        frame.setSize(800,800);
        frame.add(panel);
    }

    public void updateBoard(StateObserverYavalath so, boolean withReset, boolean showValueOnBoard){
        drawValues = showValueOnBoard;
        frame.repaint();
    }

    public void toFront() {
        panel.toFront();
    }

    public void destroy() {
        frame.setVisible(false);
        frame.dispose();
    }


    /**
     * Places the Yavalath window on the right side of the arena window.
     */
    public void showGameBoard(Arena arena, boolean alignToMain) {
        frame.setVisible(true);
        if(alignToMain){
            int x = arena.m_xab.getX() + arena.m_xab.getWidth();
            int y = arena.m_xab.getLocation().y;

            frame.setLocation(x,y);
        }
    }

    public void clearBoard(boolean boardClear, boolean vClear) {
        frame.repaint();
    }

    /**
     * The element responsible for drawing the GUI of the Yavalath game.
     * Includes the mouseListener for detecting human input into the game.
     */
    public class YavalathPanel extends JPanel {
        private JLabel statusLabel;
        private JLabel swapLabel;

        YavalathPanel() {
            Font font = new Font("Arial", Font.BOLD, Types.GUI_HELPFONTSIZE);

            statusLabel = new JLabel();
            statusLabel.setForeground(Color.WHITE);
            statusLabel.setFont(font);

            swapLabel = new JLabel("Swap Rule available!");
            swapLabel.setForeground(Color.WHITE);
            swapLabel.setFont(font);

            setBackground(BACKGROUND_COLOR);
            MouseListenerYavalath ml = new MouseListenerYavalath();
            addMouseListener(ml);
            add(statusLabel);
            add(swapLabel);
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            drawGameBoard(g2);
        }

        private void drawGameBoard(Graphics2D g2) {

            switch (gb.so.getPlayer()) {
                case 0 -> statusLabel.setText("Player 1 to move.");
                case 1 -> statusLabel.setText("Player 2 to move.");
                case 2 -> statusLabel.setText("Player 3 to move.");
            }

            swapLabel.setVisible(gb.so.getMoveCounter() == 1);

            //Draw all the individual cells
            for (TileYavalath[] x : gb.so.getGameBoard()) {
                for (TileYavalath y : x) {
                    if (y.getPlayer() != INVALID_FIELD) {
                        g2.setColor(CELL_BACKGROUND);
                        g2.fillPolygon(y.getPoly());
                        g2.setColor(CELL_LINING);
                        g2.drawPolygon(y.getPoly());

                        if (y.getPlayer() == PLAYER_ONE || y.getPlayer() == PLAYER_TWO ||
                                y.getPlayer() == PLAYER_THREE) {
                            if (y.getPlayer() == PLAYER_ONE) g2.setColor(COLOR_PLAYER_ONE);
                            else if (y.getPlayer() == PLAYER_TWO) g2.setColor(COLOR_PLAYER_TWO);
                            else if (y.getPlayer() == PLAYER_THREE) g2.setColor(COLOR_PLAYER_THREE);
                            g2.fillOval(y.getTileCenter().x - GAME_PIECE_RADIUS,
                                    y.getTileCenter().y - GAME_PIECE_RADIUS, GAME_PIECE_RADIUS * 2,
                                    GAME_PIECE_RADIUS * 2);
                        }

                        //Draw the values for the individual cells
                        if (drawValues) {
                            Double value = y.getValue();
                            if (Double.isNaN(value)) continue;
                            String tileValueText = Long.toString(Math.round(value * 1000));
                            g2.setColor(Color.RED);
                            g2.drawString(tileValueText, y.getTileCenter().x - GAME_PIECE_RADIUS / 2, y.getTileCenter().y);
                        }
                    }
                }
            }
            //Mark the game-piece that was last placed on the board
            TileYavalath lastPlayed = gb.so.getLastPlayedTile();
            if (lastPlayed == null) return;
            g2.setColor(Color.RED);
            g2.drawOval(lastPlayed.getTileCenter().x - GAME_PIECE_RADIUS,
                    lastPlayed.getTileCenter().y - GAME_PIECE_RADIUS, GAME_PIECE_RADIUS * 2,
                    GAME_PIECE_RADIUS * 2);
        }

        public void toFront() {
            frame.setState(Frame.NORMAL);
            super.setVisible(true);
        }


        /**
         * Calculates if and which tile was clicked on the board and advances the state observer accordingly.
         */
        class MouseListenerYavalath extends MouseAdapter {
            public void mouseReleased(MouseEvent e) {
                if (gb.isActionReq() || gb.getArena().taskState != Arena.Task.PLAY && gb.getArena().taskState != Arena.Task.INSPECTV) {
                    return;
                }
                Point clicked = e.getPoint();
                TileYavalath clickedTile = UtilityFunctionsYavalath.clickedTile(clicked, gb.so.getGameBoard());
                if (clickedTile == null) return;

                Types.ACTIONS humanMove = new Types.ACTIONS(clickedTile.getX()*BOARD_SIZE + clickedTile.getY());
                gb.so.advance(humanMove);

                if(gb.getArena().taskState == Arena.Task.PLAY){
                    (gb.getArena().getLogManager()).addLogEntry(humanMove,gb.so,gb.getArena().getLogSessionID());
                }

                updateBoard(null, false, false);
                gb.setActionReq(true);
            }
        }
    }
}
