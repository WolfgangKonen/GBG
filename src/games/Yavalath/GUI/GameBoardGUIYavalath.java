package games.Yavalath.GUI;

import games.Arena;
import games.Yavalath.GameBoardYavalath;
import games.Yavalath.StateObserverYavalath;
import games.Yavalath.TileYavalath;
import games.Yavalath.UtilityFunctionsYavalath;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static games.Yavalath.ConfigYavalath.*;

public class GameBoardGUIYavalath {
    static Color COLOR_PLAYER_ONE = Color.WHITE;
    static Color COLOR_PLAYER_TWO = Color.BLACK;
    final static Color COLOR_PLAYER_THREE = Color.RED;
    final static Color CELL_BACKGROUND = Color.YELLOW;
    final static Color CELL_LINING = Color.ORANGE;
    final static Color BACKGROUND_COLOR = Color.GRAY;

    private int WINDOW_HEIGHT;
    private int WINDOW_WIDTH;

    private boolean drawValues;

    private JFrame frame = new JFrame("GBG - Yavalath");
    private YavalathPanel yavPanel;
    private InfoPanel infoPanel;

    private GameBoardYavalath gb;

    public GameBoardGUIYavalath(GameBoardYavalath gb) {
        this.gb = gb;
        initializeFrame();
    }

    private void initializeFrame(){
        yavPanel = new YavalathPanel();
        infoPanel = new InfoPanel();

        frame.setVisible(true);
        frame.setSize(800,800);
        frame.setLayout(new BorderLayout());
        frame.add(yavPanel);
        frame.add(infoPanel,BorderLayout.SOUTH);



    }

    public void updateBoard(StateObserverYavalath so, boolean withReset, boolean showValueOnBoard){
        drawValues = showValueOnBoard;
        frame.repaint();
    }

    public void toFront() {
        yavPanel.toFront();
        infoPanel.toFront();
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
        COLOR_PLAYER_ONE = Color.white;
        COLOR_PLAYER_TWO = Color.black;
        frame.repaint();
    }

    /**
     * The element responsible for drawing the GUI of the Yavalath game.
     * Includes the mouseListener for detecting human input into the game.
     */
    public class YavalathPanel extends JPanel {
        YavalathPanel() {

            setBackground(BACKGROUND_COLOR);
            MouseListenerYavalath ml = new MouseListenerYavalath();
            addMouseListener(ml);
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            drawGameBoard(g2);
        }

        private void drawGameBoard(Graphics2D g2) {
            TileYavalath threateningMove = null;
            //Draw all the individual cells
            for (TileYavalath[] x : gb.m_so.getGameBoard()) {
                for (TileYavalath y : x) {
                    if (y.getPlayer() != INVALID_FIELD) {
                        g2.setColor(CELL_BACKGROUND);
                        g2.fillPolygon(y.getPoly());
                        g2.setColor(CELL_LINING);
                        if(y.getThreateningMove()) threateningMove = y;
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
                            Double value;
                            value = y.getValue();
                            if (Double.isNaN(value)) continue;
                            String tileValueText = Long.toString(Math.round(value * 1000));
                            g2.setColor(Color.RED);
                            g2.drawString(tileValueText, y.getTileCenter().x - GAME_PIECE_RADIUS / 2, y.getTileCenter().y);
                        }
                    }
                }
            }
            //Mark the game-piece that was last placed on the board
            if(gb.m_so.getMoveList().size() > 0){
                TileYavalath lastPlayed = gb.m_so.getMoveList().get(0);
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(lastPlayed.getTileCenter().x - GAME_PIECE_RADIUS,
                        lastPlayed.getTileCenter().y - GAME_PIECE_RADIUS, GAME_PIECE_RADIUS * 2,
                        GAME_PIECE_RADIUS * 2);

            }

            //If there is a threatening move on the board, mark it
            if(threateningMove!=null){
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.RED);
                g2.drawPolygon(threateningMove.getPoly());
            }
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
                TileYavalath clickedTile = UtilityFunctionsYavalath.clickedTile(clicked, gb.m_so.getGameBoard());
                if (clickedTile == null) return;

                Types.ACTIONS humanMove = new Types.ACTIONS(clickedTile.getX()*BOARD_SIZE + clickedTile.getY());
                gb.HGameMove(humanMove);
                updateBoard(null, false, false);
            }
        }
    }

    public class InfoPanel extends JPanel{

        private JLabel turn;
        private JLabel playerOneColor;
        private JLabel playerTwoColor;
        private JLabel playerThreeColor;
        private JLabel swapRuleLabel;
        private JButton swapRuleButton;

        InfoPanel(){
            Font font = new Font("Arial", Font.BOLD, Types.GUI_HELPFONTSIZE);
            setSize(800,100);
            turn = new JLabel();
            turn.setFont(font);

            playerOneColor = new JLabel();
            playerOneColor.setFont(font);

            playerTwoColor = new JLabel();
            playerTwoColor.setFont(font);

            playerThreeColor = new JLabel();
            playerThreeColor.setFont(font);

            swapRuleLabel = new JLabel("Swap Rule Available!");
            swapRuleLabel.setFont(font);
            swapRuleLabel.setVisible(false);

            swapRuleButton = new JButton("Use it!");
            swapRuleButton.setVisible(false);
            swapRuleButton.addActionListener(e -> {
                gb.useSwapRule();
                COLOR_PLAYER_ONE = Color.BLACK;
                COLOR_PLAYER_TWO = Color.WHITE;
            });

            setBackground(BACKGROUND_COLOR);

            this.setLayout(new GridLayout(3,3));
            add(new JLabel(""));
            add(turn);
            add(new JLabel(""));
            add(playerOneColor);
            add(playerTwoColor);
            add(playerThreeColor);
            add(swapRuleLabel);
            add(swapRuleButton);

        }

        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            setInfoPanel();
        }

        private void setInfoPanel(){

            switch (gb.m_so.getPlayer()) {
                case 0 -> {
                    turn.setForeground(COLOR_PLAYER_ONE);
                    turn.setText("Player 1 to move");
                }
                case 1 -> {
                    turn.setForeground(COLOR_PLAYER_TWO);
                    turn.setText("Player 2 to move");
                }
                case 2 -> {
                    turn.setForeground(COLOR_PLAYER_THREE);
                    turn.setText("Player 3 to move");
                }
            }

            swapRuleLabel.setVisible(gb.m_so.getMoveCounter()==1 || gb.m_so.swapRuleUsed());
            swapRuleButton.setVisible(gb.m_so.getMoveCounter() == 1);

            if(gb.m_so.swapRuleUsed()) swapRuleLabel.setText("Swap Rule was used!");

            if(COLOR_PLAYER_ONE == Color.WHITE){
                playerOneColor.setText("Player 1 is WHITE");
                playerOneColor.setForeground(Color.WHITE);
            }else if(COLOR_PLAYER_ONE == Color.BLACK){
                playerOneColor.setText("Player 1 is BLACK");
                playerOneColor.setForeground(Color.BLACK);
            }

            if(COLOR_PLAYER_TWO == Color.BLACK){
                playerTwoColor.setText("Player 2 is BLACK");
                playerTwoColor.setForeground(Color.BLACK);
            }else if(COLOR_PLAYER_TWO == Color.WHITE){
                playerTwoColor.setText("Player 2 is WHITE");
                playerTwoColor.setForeground(Color.WHITE);
            }

            playerThreeColor.setText("Player 3 is RED");
            playerThreeColor.setForeground(COLOR_PLAYER_THREE);
            playerThreeColor.setVisible(gb.m_so.getNumPlayers() == 3);

        }

        public void toFront() {
            frame.setState(Frame.NORMAL);
            super.setVisible(true);
        }
    }
}
