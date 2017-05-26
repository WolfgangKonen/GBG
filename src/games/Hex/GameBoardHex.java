package games.Hex;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;


public class GameBoardHex implements GameBoard {
    private HexPanel gamePanel;
    private Arena arena;
    private StateObserverHex stateObs;
    protected Random rand;

    private final int HEX_SIZE = 60; //size of hexagons in px (from one side to the opposite one)
    final static int OFFSET = 15; //offset in px from top and left borders of the window
    final static Color COLOR_PLAYER_ONE = Color.WHITE;
    final static Color COLOR_PLAYER_TWO = Color.BLACK;
    private final int WINDOW_HEIGHT;
    private final int WINDOW_WIDTH;

    final static Color COLOR_CELL =  Color.ORANGE;
    final static Color COLOR_GRID =  Color.GRAY;

    private boolean actionRequired = false;

    private boolean inputEnabled = true;

    final static Color BACKGROUND_COLOR = Color.lightGray;

    public GameBoardHex(Arena arena){
        this.arena = arena;

        //Board size +2 to account for offset on top and bottom of the window
        WINDOW_HEIGHT = HEX_SIZE*(HexConfig.BOARD_SIZE+2);

        //Increasing the board size by one increases total width of board by 3 times side length
        WINDOW_WIDTH = (int)(HexConfig.BOARD_SIZE*3*(HexUtils.getSideLengthFromHeight(HEX_SIZE)));

        rand = new Random(System.currentTimeMillis());
        stateObs = new StateObserverHex(HEX_SIZE);

        createAndShowGUI();
    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        stateObs = new StateObserverHex(HEX_SIZE);
        gamePanel.repaint();
    }

    @Override
    public void updateBoard(StateObservation so, boolean showStoredV, boolean enableOccupiedCells) {
        if (so == null) {
            gamePanel.repaint();
            return;
        }

        assert (so instanceof StateObserverHex)
                : "StateObservation 'so' is not an instance of StateObserverHex";
        StateObserverHex soHex = (StateObserverHex) so;

        stateObs = soHex.copy();

        gamePanel.showValues = showStoredV;
        drawBoardToPanel((Graphics2D) gamePanel.getGraphics(), showStoredV);
    }

    @Override
    public void showGameBoard(Arena arena) {

    }

    @Override
    public boolean isActionReq() {
        return actionRequired;
    }

    @Override
    public void setActionReq(boolean actionReq) {
        actionRequired = actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {

    }

    @Override
    public StateObservation getStateObs() {
        return stateObs;
    }

    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true, true);
        return stateObs;
    }

    @Override
    public StateObservation chooseStartState01() {
        clearBoard(true, true);
        if (rand.nextDouble()>0.5) {
            // choose randomly one of the possible actions in default
            // start state and advance m_so by one ply
            ArrayList<Types.ACTIONS> acts = stateObs.getAvailableActions();
            int i = rand.nextInt(acts.size());
            stateObs.advance(acts.get(i));
        }
        return stateObs;
    }

    private void drawBoardToPanel(Graphics2D g2, boolean showValues){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        //draw borders of the game board
        HexUtils.drawOutlines(HexConfig.BOARD_SIZE, COLOR_PLAYER_ONE, COLOR_PLAYER_TWO, g2, stateObs.getBoard());

        HexTile lastPlaced = stateObs.getLastUpdatedTile();

        //draw hexes
        for (int i=0;i<HexConfig.BOARD_SIZE;i++) {
            for (int j=0;j<HexConfig.BOARD_SIZE;j++) {
                HexTile tile = stateObs.getBoard()[i][j];
                double tileValue = tile.getValue();

                HexUtils.drawHex(tile, g2, false);

                if (showValues && !Double.isNaN(tileValue)){
                    Color cellColor = null;
                    if (!tile.equals(lastPlaced)) {
                        cellColor = HexUtils.calculateTileColor(tileValue);
                    }
                    HexUtils.drawTileValues(tile, g2, HexConfig.BOARD_SIZE, cellColor);
                }
            }
        }

        //Draw last placed hex again to highlight it
        if (lastPlaced != null) {
            HexUtils.drawHex(lastPlaced, g2, true);
            if (showValues && !Double.isNaN(lastPlaced.getValue())) {
                HexUtils.drawTileValues(lastPlaced, g2, HexConfig.BOARD_SIZE, null);
            }
        }
    }

    private void createAndShowGUI()
    {
        gamePanel = new HexPanel();
        JFrame frame = new JFrame("Hex - GBG");
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        frame.getContentPane().setBackground(Color.black);
        Container content = frame.getContentPane();
        content.add(gamePanel);
        frame.setSize( WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
    }

    public class HexPanel extends JPanel
    {
        boolean showValues = false;
        HexPanel(){
            setBackground(GameBoardHex.BACKGROUND_COLOR);

            MyMouseListener ml = new MyMouseListener();
            addMouseListener(ml);
        }

        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            super.paintComponent(g2);
            drawBoardToPanel(g2, showValues);
        }

        class MyMouseListener extends MouseAdapter {
            public void mouseReleased(MouseEvent e) {
                if (!inputEnabled || arena.taskState != Arena.Task.PLAY){
                    return;
                }
                Point p = new Point( HexUtils.pxtoHex(e.getX(),e.getY(), stateObs.getBoard(), HexConfig.BOARD_SIZE) );
                if (p.x < 0 || p.y < 0 || p.x >= HexConfig.BOARD_SIZE || p.y >= HexConfig.BOARD_SIZE) return;

                stateObs.advance(Types.ACTIONS.fromInt(p.x*HexConfig.BOARD_SIZE+p.y));
                updateBoard(null, false, false);
                setActionReq(true);
            }
        }
    }
}
