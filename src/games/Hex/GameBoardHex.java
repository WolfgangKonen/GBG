package games.Hex;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import static games.Hex.HexConfig.PLAYER_ONE;


public class GameBoardHex implements GameBoard {
    private HexPanel gamePanel;
    private Arena arena;
    private StateObserverHex stateObs;
    protected Random rand;

    final static Color COLOR_PLAYER_ONE = Color.BLACK;
    final static Color COLOR_PLAYER_TWO = Color.WHITE;
    private final int WINDOW_HEIGHT;
    private final int WINDOW_WIDTH;

    final static Color COLOR_CELL =  Color.ORANGE;
    final static Color COLOR_GRID =  Color.GRAY;

    private boolean actionRequired = false;

    final static Color BACKGROUND_COLOR = Color.lightGray;

    public GameBoardHex(Arena arena){
        this.arena = arena;

        //Board size +2 to account for offset on top and bottom of the window
        WINDOW_HEIGHT = HexConfig.HEX_SIZE*(HexConfig.BOARD_SIZE+1)+HexConfig.OFFSET*2;

        //Increasing the board size by one increases total width of board by 3 times side length
        WINDOW_WIDTH = (int)(HexConfig.BOARD_SIZE*3*(HexUtils.getSideLengthFromHeight(HexConfig.HEX_SIZE)));

        rand = new Random(System.currentTimeMillis());
        stateObs = new StateObserverHex();

        createAndShowGUI();
    }

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if (boardClear) {
            stateObs = new StateObserverHex();
        } else if (vClear){
            stateObs.clearTileValues();
        }
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

        drawBoardToPanel((Graphics2D) gamePanel.getGraphics(), true);

        /*int longestChain = HexUtils.getLongestChain(soHex.getBoard(), HexUtils.getOpponent(so.getPlayer()))[0];
        int featureVectorP1[] = HexUtils.getLongestChain(soHex.getBoard(), PLAYER_ONE);
        int featureVectorP2[] = HexUtils.getLongestChain(soHex.getBoard(), PLAYER_TWO);
        //System.out.println("Longest chain for player "+so.getPlayer()+": "+longestChain);
        System.out.println("---------------------------------");
        System.out.println("Longest chain for player BLACK: "+featureVectorP1[0]);
        System.out.println("Longest chain for player WHITE: "+featureVectorP2[0]);
        System.out.println("Free adjacent tiles for player BLACK: "+featureVectorP1[1]);
        System.out.println("Free adjacent tiles for player WHITE: "+featureVectorP2[1]);*/
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
        //Not called anywhere
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
        g2.setFont(new Font("TimesRoman", Font.PLAIN, HexConfig.HEX_SIZE/4));

        //draw borders of the game board
        HexUtils.drawOutlines(HexConfig.BOARD_SIZE, COLOR_PLAYER_ONE, COLOR_PLAYER_TWO, g2, stateObs.getBoard());

        HexTile lastPlaced = stateObs.getLastUpdatedTile();

        //draw hexes
        for (int i=0;i<HexConfig.BOARD_SIZE;i++) {
            for (int j=0;j<HexConfig.BOARD_SIZE;j++) {
                HexTile tile = stateObs.getBoard()[i][j];
                Color cellColor = getTileColor(tile, showValues);
                HexUtils.drawHex(tile, g2, cellColor, false);
                HexUtils.drawTileValueText(tile, g2, HexConfig.BOARD_SIZE);
            }
        }

        //draw last placed tile again so it's highlighting overlaps all other tiles
        if (lastPlaced != null) {
            Color cellColor = getTileColor(lastPlaced, showValues);
            HexUtils.drawHex(lastPlaced, g2, cellColor, true);
            HexUtils.drawTileValueText(lastPlaced, g2, HexConfig.BOARD_SIZE);
        }
    }

    private Color getTileColor(HexTile tile, boolean showValues){
        double tileValue = tile.getValue();
        Color cellColor = COLOR_CELL;
        if (tile.getPlayer() == PLAYER_ONE){
            cellColor = GameBoardHex.COLOR_PLAYER_ONE;
        } else if (tile.getPlayer() == HexConfig.PLAYER_TWO){
            cellColor = GameBoardHex.COLOR_PLAYER_TWO;
        } else if (showValues && !Double.isNaN(tileValue)){
            cellColor = HexUtils.calculateTileColor(tileValue);
        }

        return cellColor;
    }

    private void createAndShowGUI()
    {
        gamePanel = new HexPanel();
        JFrame frame = new JFrame("Hex - GBG");
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        frame.getContentPane().setBackground(Color.black);
        Container content = frame.getContentPane();
        content.add(gamePanel);
        //frame.setSize( WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
    }

    public class HexPanel extends JPanel
    {
        boolean showValues = true;
        HexPanel(){
            setBackground(GameBoardHex.BACKGROUND_COLOR);

            MyMouseListener ml = new MyMouseListener();
            addMouseListener(ml);
        }

        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            super.paintComponent(g2);

            if (arena.taskState != Arena.Task.TRAIN) {
                drawBoardToPanel(g2, showValues);
            }
        }

        class MyMouseListener extends MouseAdapter {
            public void mouseReleased(MouseEvent e) {
                if (actionRequired || (arena.taskState != Arena.Task.PLAY && arena.taskState != Arena.Task.INSPECTV)){
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

	@Override
    public String getSubDir() {
        DecimalFormat form = new DecimalFormat("00");
        return form.format(HexConfig.BOARD_SIZE);
    }
}
