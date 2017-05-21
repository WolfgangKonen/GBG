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

        if (showStoredV && stateObs.storedValues!=null) {
            for(int i=0;i<HexConfig.BOARD_SIZE;i++){
                for(int j=0;j<HexConfig.BOARD_SIZE;j++) {
                    stateObs.getBoard()[i][j].setValue(Double.NaN);
                }
            }

            for (int k=0; k<stateObs.storedValues.length; k++) {
                Types.ACTIONS action = stateObs.storedActions[k];
                int actionInt = action.toInt();
                int j = actionInt % HexConfig.BOARD_SIZE;
                int i = (actionInt - j) / HexConfig.BOARD_SIZE;
                stateObs.getBoard()[i][j].setValue(stateObs.storedValues[k]);
            }
        }

        gamePanel.repaint();
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

    private void createAndShowGUI()
    {
        gamePanel = new HexPanel();
        JFrame frame = new JFrame("HexGame - GBG");
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
        HexPanel(){
            setBackground(GameBoardHex.BACKGROUND_COLOR);

            MyMouseListener ml = new MyMouseListener();
            addMouseListener(ml);
        }

        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            super.paintComponent(g2);
            //draw borders of the game board
            HexUtils.drawOutlines(HexConfig.BOARD_SIZE, COLOR_PLAYER_ONE, COLOR_PLAYER_TWO, g2, stateObs.getBoard());

            //draw hexes
            for (int i=0;i<HexConfig.BOARD_SIZE;i++) {
                for (int j=0;j<HexConfig.BOARD_SIZE;j++) {
                    HexUtils.drawHex(stateObs.getBoard()[i][j],g2 , HexConfig.BOARD_SIZE);
                }
            }
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
