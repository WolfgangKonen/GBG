package games.Yavalath.GUI;

import games.Arena;
import games.Hex.HexUtils;
import games.Yavalath.*;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static games.Yavalath.ConfigYavalath.*;

public class GameBoardGUIYavalath {
    static Color COLOR_PLAYER_ZERO = Color.BLACK;
    static Color COLOR_PLAYER_ONE = Color.WHITE;
    final static Color COLOR_PLAYER_TWO = Color.BLUE;
    final static Color CELL_BACKGROUND = Color.YELLOW;
    final static Color CELL_LINING = Color.ORANGE;
    final static Color BACKGROUND_COLOR = Color.GRAY;

    //TODO: Adjust window size based on board size
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
        COLOR_PLAYER_ZERO = Color.BLACK;
        COLOR_PLAYER_ONE = Color.WHITE;
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
                    if(y.getPlayer() == EMPTY){
                        g2.setColor(calculateTileColor(y,drawValues));
                        g2.fillPolygon(y.getPoly());
                        g2.setColor(CELL_LINING);
                        g2.drawPolygon(y.getPoly());
                        if(y.getThreateningMove()) threateningMove = y;
                    } else if(y.getPlayer() == PLAYER_ZERO || y.getPlayer() == PLAYER_ONE || y.getPlayer() == PLAYER_TWO){
                        g2.setColor(CELL_BACKGROUND);
                        g2.fillPolygon(y.getPoly());
                        g2.setColor(CELL_LINING);
                        g2.drawPolygon(y.getPoly());

                        //Draw game-piece
                        if(y.getPlayer() == PLAYER_ZERO) g2.setColor(COLOR_PLAYER_ZERO);
                        else if(y.getPlayer() == PLAYER_ONE) g2.setColor(COLOR_PLAYER_ONE);
                        else if(y.getPlayer() == PLAYER_TWO) g2.setColor(COLOR_PLAYER_TWO);
                        g2.fillOval(y.getTileCenter().x - GAME_PIECE_RADIUS,
                                y.getTileCenter().y - GAME_PIECE_RADIUS, GAME_PIECE_RADIUS * 2,
                                GAME_PIECE_RADIUS * 2);
                    }
                    if(y.getPlayer() != INVALID_FIELD && drawValues){
                        Double value = y.getValue();
                        if (Double.isNaN(value)) continue;
                        String tileValueText = Long.toString(Math.round(value * 1000));
                        g2.setColor(Color.GRAY);
                        g2.drawString(tileValueText, y.getTileCenter().x - GAME_PIECE_RADIUS / 2, y.getTileCenter().y);
                    }
                }
            }
            //Mark the game-piece that was last placed on the board
            if(gb.m_so.getMoveList().size() > 0){
                TileYavalath lastPlayed = gb.m_so.getMoveList().get(0);
                g2.setColor(Color.RED);
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

                Types.ACTIONS humanMove = ConfigYavalath.getActionFromTileValue(clickedTile.getX()* getMaxRowLength() + clickedTile.getY());
                gb.HGameMove(humanMove);
                updateBoard(null, false, false);
            }
        }

        /**
         * Calculates the color for a tile, depending on if its player owned or not.
         * If it isn't it gets a color gradient depending on the value of the tile (Uses the same method that Hex does).
         *
         * @param tile The tile to calculate the color for.
         * @param showValues Whether tile values should be visible.
         */
        private Color calculateTileColor(TileYavalath tile, boolean showValues){
            if(tile.getPlayer() == PLAYER_ZERO){
                return COLOR_PLAYER_ZERO;
            } else if(tile.getPlayer() == PLAYER_ONE){
                return COLOR_PLAYER_ONE;
            } else if (tile.getPlayer() == PLAYER_TWO){
                return COLOR_PLAYER_TWO;
            } else if (showValues && !Double.isNaN(tile.getValue())){
                return HexUtils.calculateTileColor(tile.getValue());
            }
            return CELL_BACKGROUND;
        }
    }

    public class InfoPanel extends JPanel{

        private JLabel turn;
        private JLabel playerZeroColor;
        private JLabel playerOneColor;
        private JLabel playerTwoColor;
        private JLabel swapRuleLabel;
        private JButton swapRuleButton;

        InfoPanel(){
            Font font = new Font("Arial", Font.BOLD, Types.GUI_HELPFONTSIZE);
            setSize(800,100);
            turn = new JLabel();
            turn.setFont(font);

            playerZeroColor = new JLabel();
            playerZeroColor.setFont(font);

            playerOneColor = new JLabel();
            playerOneColor.setFont(font);

            playerTwoColor = new JLabel();
            playerTwoColor.setFont(font);

            swapRuleLabel = new JLabel("Swap Rule Available!");
            swapRuleLabel.setFont(font);
            swapRuleLabel.setVisible(false);

            swapRuleButton = new JButton("Use it!");
            swapRuleButton.setVisible(false);
            swapRuleButton.addActionListener(e -> {
                gb.useSwapRule();
                COLOR_PLAYER_ZERO = Color.BLACK;
                COLOR_PLAYER_ONE = Color.WHITE;
            });

            setBackground(BACKGROUND_COLOR);

            this.setLayout(new GridLayout(3,3));
            add(new JLabel(""));
            add(turn);
            add(new JLabel(""));
            add(playerZeroColor);
            add(playerOneColor);
            add(playerTwoColor);
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
                    turn.setForeground(COLOR_PLAYER_ZERO);
                    switch (ConfigYavalath.getPlayers()){
                        case 2 ->turn.setText("Player X to move");
                        case 3 ->turn.setText("Player 0 to move");
                    }
                }
                case 1 -> {
                    turn.setForeground(COLOR_PLAYER_ONE);
                    switch (ConfigYavalath.getPlayers()){
                        case 2 -> turn.setText("Player O to move");
                        case 3 -> turn.setText("Player 1 to move");
                    }
                }
                case 2 -> {
                    turn.setForeground(COLOR_PLAYER_TWO);
                    turn.setText("Player 2 to move");
                }
            }

            swapRuleLabel.setVisible(gb.m_so.getMoveCounter()==1 || gb.m_so.swapRuleUsed());
            swapRuleButton.setVisible(gb.m_so.getMoveCounter() == 1);

            if(gb.m_so.swapRuleUsed()) swapRuleLabel.setText("Swap Rule was used!");

            if(COLOR_PLAYER_ZERO == Color.WHITE){
                switch (ConfigYavalath.getPlayers()){
                    case 2 -> playerZeroColor.setText("Player X is WHITE");
                    case 3 -> playerZeroColor.setText("Player 0 is WHITE");
                }
                playerZeroColor.setForeground(Color.WHITE);

            }else if(COLOR_PLAYER_ZERO == Color.BLACK){
                switch (ConfigYavalath.getPlayers()){
                    case 2 -> playerZeroColor.setText("Player X is BLACK");
                    case 3 -> playerZeroColor.setText("Player 0 is BLACK");
                }
                playerZeroColor.setForeground(Color.BLACK);
            }

            if(COLOR_PLAYER_ONE == Color.BLACK){
                switch(ConfigYavalath.getPlayers()){
                    case 2 -> playerOneColor.setText("Player O is BLACK");
                    case 3 -> playerOneColor.setText("Player 1 is BLACK");
                }
                playerOneColor.setForeground(Color.BLACK);

            }else if(COLOR_PLAYER_ONE == Color.WHITE){
                switch(ConfigYavalath.getPlayers()){
                    case 2 -> playerOneColor.setText("Player O is WHITE");
                    case 3 -> playerOneColor.setText("Player 1 is WHITE");
                }
                playerOneColor.setForeground(Color.WHITE);
            }

            playerTwoColor.setText("Player 2 is BLUE");
            playerTwoColor.setForeground(COLOR_PLAYER_TWO);
            playerTwoColor.setVisible(gb.m_so.getNumPlayers() == 3);

        }

        public void toFront() {
            frame.setState(Frame.NORMAL);
            super.setVisible(true);
        }
    }
}
