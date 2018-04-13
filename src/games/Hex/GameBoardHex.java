package games.Hex;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import javax.swing.*;

import controllers.PlayAgent;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import static games.Hex.HexConfig.PLAYER_ONE;
import static games.Hex.HexConfig.PLAYER_TWO;

/**
 * The class GameBoardHex manages the Hex GUI.
 * It creates an instance of the child class HexPanel, which is the actual GUI.
 * GameBoardHex implements all functions required by the interface GameBoard.
 * It also contains configuration for the colors used by the GUI.
 */

public class GameBoardHex implements GameBoard {
    final static boolean GRAYSCALE = false; //Used to take screenshots of board states without using color
    final static Color COLOR_PLAYER_ONE = Color.BLACK;
    final static Color COLOR_PLAYER_TWO = Color.WHITE;
    final static Color COLOR_CELL = GRAYSCALE ? Color.LIGHT_GRAY : Color.ORANGE;
    final static Color COLOR_GRID = Color.GRAY;
    final static Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
    final boolean verbose = false;
    private final int WINDOW_HEIGHT;
    private final int WINDOW_WIDTH;
    protected Random rand;
    private HexPanel gamePanel;
    private JFrame m_frame = new JFrame("Hex - GBG");
    private Arena m_Arena;
    private StateObserverHex m_so;
    private boolean arenaActReq = false;


    public GameBoardHex(Arena arena) {
        this.m_Arena = arena;

        //Board size +2 to account for offset on top and bottom of the window
        WINDOW_HEIGHT = HexConfig.HEX_SIZE * (HexConfig.BOARD_SIZE + 1) + HexConfig.OFFSET * 2;

        //Increasing the board size by one increases total width of board by 3 times side length
        WINDOW_WIDTH = (int) (HexConfig.BOARD_SIZE * 3 * (HexUtils.getSideLengthFromHeight(HexConfig.HEX_SIZE)));

        rand = new Random(System.currentTimeMillis());
        m_so = new StateObserverHex();

        createAndShowGUI();
    }

    @Override
    public void initialize() {}
    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if (boardClear) {
            m_so = new StateObserverHex();
        } else if (vClear) {
            m_so.clearTileValues();
        }
        //gamePanel.repaint();
        m_frame.paint(m_frame.getGraphics());
    }

    @Override
    public void updateBoard(StateObservation so,  
							boolean enableOccupiedCells, boolean showValueOnGameboard) {
    	gamePanel.setShowValues(showValueOnGameboard);
    	
        if (so == null) {
            //gamePanel.repaint();
            m_frame.paint(m_frame.getGraphics());
            return;
        }

        assert (so instanceof StateObserverHex)
                : "StateObservation 'so' is not an instance of StateObserverHex";
        StateObserverHex soHex = (StateObserverHex) so;

        m_so = soHex.copy();
        
        if (m_Arena.taskState != Arena.Task.PLAY) {
            gamePanel.repaint();
            //--- the above line results in a buggy behavior in connection with delay slider: not 
            //--- every call to repaint will issue an update, so that after two delays all of a 
            //--- sudden two moves would be displayed. The right thing is to call the paint() 
            //--- method of the enclosing JFrame:         	
        } else {
    		m_frame.paint(m_frame.getGraphics());
    		// this works well with the slider, with the little down-side of paint(): GameBoard 
    		// tends to flicker when moves are displayed.
    		// But it has problems when training: The Gameboard flickers AND part of the 
    		// JFreeChart are 'hidden' --> so we do not use this in training.
        }


        if (verbose) {
            double featureVectorP1[] = HexUtils.getFeature3ForPlayer(soHex.getBoard(), PLAYER_ONE);
            double featureVectorP2[] = HexUtils.getFeature3ForPlayer(soHex.getBoard(), PLAYER_TWO);
            System.out.println("---------------------------------");
            System.out.println("Longest chain for player BLACK: " + featureVectorP1[0]);
            System.out.println("Longest chain for player WHITE: " + featureVectorP2[0]);
            System.out.println("Free adjacent tiles for player BLACK: " + featureVectorP1[1]);
            System.out.println("Free adjacent tiles for player WHITE: " + featureVectorP2[1]);
            System.out.println("Virt. Con. for player BLACK: " + featureVectorP1[2]);
            System.out.println("Virt. Con. for player WHITE: " + featureVectorP2[2]);
            System.out.println("Weak Con. for player BLACK: " + featureVectorP1[3]);
            System.out.println("Weak Con. for player WHITE: " + featureVectorP2[3]);
            System.out.println("Dir. Con. for player BLACK: " + featureVectorP1[4]);
            System.out.println("Dir. Con. for player WHITE: " + featureVectorP2[4]);
        }
    }

    @Override
    public void showGameBoard(Arena arena, boolean alignToMain) {
        gamePanel.setVisible(true);
        //gamePanel.repaint();
    }

	/**
	 * @return  true: if an action is requested from Arena or ArenaTrain
	 * 			false: no action requested from Arena, next action has to come 
	 * 			from GameBoard (e.g. user input / human move) 
	 */
    @Override
    public boolean isActionReq() {
        return arenaActReq;
    }

	/**
	 * @param	actionReq true : GameBoard requests an action from Arena 
	 * 			(see {@link #isActionReq()})
	 */
    @Override
    public void setActionReq(boolean actionReq) {
        arenaActReq = actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {
        //Not called anywhere
    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public StateObservation getDefaultStartState() {
        clearBoard(true, true);
        return m_so;
    }

	/**
	 * @return a start state which is with probability 0.5 the empty board 
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
    @Override
    public StateObservation chooseStartState() {
        clearBoard(true, true);
        if (rand.nextDouble() > 0.5) {
            // choose randomly one of the possible actions in default
            // start state and advance m_so by one ply
            ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
            int i = rand.nextInt(acts.size());
            m_so.advance(acts.get(i));
        }
        return m_so;
    }

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
	
    private void createAndShowGUI() {
        gamePanel = new HexPanel();
        m_frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        m_frame.getContentPane().setBackground(Color.black);
        Container content = m_frame.getContentPane();
        content.add(gamePanel);
        m_frame.getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        // this is just a try instead of the 4 lines above: does it stop the "GUI hangs" bug? - not really
//        m_frame.add(gamePanel);
//        m_frame.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        
        m_frame.pack();
        m_frame.setResizable(false);
        m_frame.setLocationRelativeTo(null);
        m_frame.setVisible(true);
    }

    @Override
    public String getSubDir() {
        DecimalFormat form = new DecimalFormat("00");
        return form.format(HexConfig.BOARD_SIZE);
    }

    @Override
    public Arena getArena() {
        return m_Arena;
    }
    

    @Override
	public void toFront() {
		gamePanel.toFront();
	}

    /**
     * Class HexPanel is used as the GUI for the game Hex. It extends the class JPanel.
     * It is responsible for drawing the game state to the screen.
     * Includes a child class HexMouseListener to process user input.
     */
    public class HexPanel extends JPanel {
        boolean showValues = true;

        HexPanel() {
            setBackground(GameBoardHex.BACKGROUND_COLOR);

            HexMouseListener ml = new HexMouseListener();
            addMouseListener(ml);
        }
        
    	public void toFront() {
        	m_frame.setState(Frame.NORMAL);	// if window is iconified, display it normally
    		super.setVisible(true);
    	}

    	public void setShowValues(boolean showValueOnGameboard) {
        	showValues = showValueOnGameboard;
        }

        /**
         * Update the GUI based on current game state each time the window is repainted
         *
         * @param g The graphics context
         */
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            super.paintComponent(g2);

            //Don't draw the game board while training to save CPU cycles
            if (   m_Arena.taskState != Arena.Task.TRAIN 
           		&& m_Arena.taskState != Arena.Task.MULTTRN
            		) {
                drawBoardToPanel(g2, showValues);
            }
        }

        /**
         * Draw the current game state to the HexPanel. State information is read via
         * the StateObserverHex instance that is managed by the parent class.
         * Requires static HexUtils class.
         *
         * @param g2         The graphics context required for drawing to the screen.
         * @param showValues Whether tile values should be visible or not.
         */
        private void drawBoardToPanel(Graphics2D g2, boolean showValues) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("TimesRoman", Font.PLAIN, HexConfig.HEX_SIZE / 4));

            //draw borders of the game board
            HexUtils.drawOutlines(HexConfig.BOARD_SIZE, COLOR_PLAYER_ONE, COLOR_PLAYER_TWO, g2, m_so.getBoard());

            HexTile lastPlaced = m_so.getLastUpdatedTile();

            //draw hexes
            if (GRAYSCALE) {
                showValues = false;
            }
            for (int i = 0; i < HexConfig.BOARD_SIZE; i++) {
                for (int j = 0; j < HexConfig.BOARD_SIZE; j++) {
                    HexTile tile = m_so.getBoard()[i][j];
                    Color cellColor = getTileColor(tile, showValues);
                    HexUtils.drawHex(tile, g2, cellColor, false);
                    if (showValues && !GRAYSCALE) {
                        HexUtils.drawTileValueText(tile, g2, cellColor, HexConfig.BOARD_SIZE);
                    }
                }
            }

            //draw last placed tile again so its highlighting overlaps all other tiles
            if (lastPlaced != null && !GRAYSCALE) {
                Color cellColor = getTileColor(lastPlaced, showValues);
                HexUtils.drawHex(lastPlaced, g2, cellColor, true);
                if (showValues)
                	HexUtils.drawTileValueText(lastPlaced, g2, cellColor, HexConfig.BOARD_SIZE);
            }
        }

        /**
         * Determines the color the tile should be drawn in. Color depends on if the tile is
         * player-owned or currently unused. Color settings for player-owned tiles are set by
         * the parent class, the color of unused tiles is calculated in HexUtils.
         *
         * @param tile       The subject tile for which the color should be determined.
         * @param showValues Whether tile values should be visible or not.
         * @return The color to draw the tile in.
         */
        private Color getTileColor(HexTile tile, boolean showValues) {
            double tileValue = tile.getValue();
            Color cellColor = COLOR_CELL;
            if (tile.getPlayer() == PLAYER_ONE) {
                cellColor = GameBoardHex.COLOR_PLAYER_ONE;
            } else if (tile.getPlayer() == HexConfig.PLAYER_TWO) {
                cellColor = GameBoardHex.COLOR_PLAYER_TWO;
            } else if (showValues && !Double.isNaN(tileValue)) {
                cellColor = HexUtils.calculateTileColor(tileValue);
            }

            return cellColor;
        }

        /**
         * Converts the pixel that was clicked to the tile that is at that exact location,
         * taking into account the non-rectangular geometry of the tiles. Calculation done in HexUtils.
         */
        class HexMouseListener extends MouseAdapter {
            public void mouseReleased(MouseEvent e) {
                if (arenaActReq || (m_Arena.taskState != Arena.Task.PLAY && m_Arena.taskState != Arena.Task.INSPECTV)) {
                    return;
                }
                Point p = new Point(HexUtils.pxtoHex(e.getX(), e.getY(), m_so.getBoard(), HexConfig.BOARD_SIZE));
                if (p.x < 0 || p.y < 0 || p.x >= HexConfig.BOARD_SIZE || p.y >= HexConfig.BOARD_SIZE) return;

        		Types.ACTIONS act = Types.ACTIONS.fromInt(p.x * HexConfig.BOARD_SIZE + p.y);
                m_so.advance(act);
                if (m_Arena.taskState == Arena.Task.PLAY) {
                	// only do this when passing here during 'PLAY': add a log entry in case of Human move
                	// Do NOT do this during 'INSPECT', because then we have (currently) no valid log session ID
            		(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());                	
                }
                updateBoard(null, false, false);
                setActionReq(true);
            }
        }
    }
}
