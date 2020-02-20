package games.ZweiTausendAchtundVierzig;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import javax.swing.*;

import controllers.PlayAgent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class GameBoard2048Gui has the board game GUI. 
 * <p>
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Johannes Kutsch, Wolfgang Konen, TH Koeln, 2016-2020
 */
public class GameBoard2048Gui extends JFrame {
    private JPanel boardPanel;
    private JPanel buttonPanel;
    private JPanel vButtonPanel;
    private JPanel gameInfo;
    private double[] vTable;
    private JLabel leftInfo = new JLabel("");
    //private JLabel rightInfo = new JLabel("");


    /**
     * The clickable buttons in the GUI, used to controll the Gameboard (Left, Up, Right, Down). The buttons will be enabled only
     * when "Play" or "Inspect V" are clicked.
     */
    protected JButton[] buttons;

    /**
     * The representation of the value function corresponding to the current board
     * {@link #buttons} position.
     */
    private JLabel[] vBoard;

    /**
     * The representation of the gameBoard
     */
    protected JLabel[] board;

    /**
     * The gamescore
     */
    private JLabel scoreLabel;

    /**
     * Informations about Scoremodifiers and the hidden gamescore
     */
   /* private JLabel realScore;
    private JLabel highestTileInCorner;
    private JLabel highestTileValue;
    private JLabel emptyTiles;
    private JLabel rowLength;
    private JLabel rowValue;
    private JLabel mergeValue; */

	/**
	 * a reference to the 'parent' {@link GameBoard2048} object
	 */
	private GameBoard2048 m_gb=null;
	
    public GameBoard2048Gui(GameBoard2048 gb) {
    	super("2048");
		m_gb = gb;
	       initGui("2048");
    }

    private void initGui(String title) {
        buttons = new JButton[4];
        vBoard = new JLabel[4];
        board = new JLabel[ConfigGame.ROWS * ConfigGame.COLUMNS];
        scoreLabel = new JLabel();
        boardPanel = initBoard();
        buttonPanel = initButton();
        vButtonPanel = initvBoard();
        gameInfo = initGameInfo();

        vTable = new double[4];


        JPanel titlePanel = new JPanel();
        JLabel Blank = new JLabel(" ");        // a little bit of space
        JLabel Title = new JLabel(title, SwingConstants.CENTER);
        Title.setForeground(Color.black);
        Font font = new Font("Arial", 1, (int)(20*Types.GUI_SCALING_FACTOR_X));
        Title.setFont(font);
        titlePanel.add(Blank);
        titlePanel.add(Title);

        JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        boardPanel.add(this.boardPanel);
        boardPanel.add(new Label("    "));        // some space


        JPanel rightPanelLeft = new JPanel();
        rightPanelLeft.setLayout(new GridLayout(2, 1, 30, 30));
        rightPanelLeft.add(buttonPanel);
        rightPanelLeft.add(vButtonPanel);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(1, 2, 30, 30));
        rightPanel.add(rightPanelLeft);
        //rightPanel.add(gameInfo);
        boardPanel.add(rightPanel);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        leftInfo.setFont(font);
        //rightInfo.setFont(font);
        infoPanel.add(leftInfo);
        //infoPanel.add(rightInfo);
        infoPanel.setSize(100, 10);

        setLayout(new BorderLayout(10, 0));
        add(titlePanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        pack();
        setVisible(false);
    }

    private JPanel initBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(ConfigGame.ROWS, ConfigGame.COLUMNS, 2, 2));
        for (int pos = 0; pos < ConfigGame.ROWS * ConfigGame.COLUMNS; pos++) {
            board[pos] = new JLabel();
            board[pos].setOpaque(true);
            updateBoardLabel(pos);
            board[pos].setForeground(Color.black);
            board[pos].setBorder(BorderFactory.createLineBorder(Color.black, 2));
            Font font = new Font("Consolas", 1, (int)(1.1*Types.GUI_TITLEFONTSIZE));  // 22
            board[pos].setFont(font);
            panel.add(board[pos]);
        }
        return panel;
    }

    private JPanel initButton() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 3, 10, 10));
        buttons[0] = new JButton("left");
        buttons[1] = new JButton("up");
        buttons[2] = new JButton("right");
        buttons[3] = new JButton("down");

        Font font = new Font("Arial", 1, (int)(1.1*Types.GUI_TITLEFONTSIZE));  //22

        for (int i = 0; i < 4; i++) {
            buttons[i].setBackground(Color.white);
            buttons[i].setForeground(Color.black);
            buttons[i].setFont(font);
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(
                    new ActionHandler(i)  // constructor copies (i) to member 'move'
                    {
                        public void actionPerformed(ActionEvent e) {
                            Arena.Task aTaskState = m_gb.m_Arena.taskState;
                            if (aTaskState == Arena.Task.PLAY) {
                                m_gb.HGameMove(move);        // i.e. make human move (i), if buttons[i] is clicked
                            }
                            if (aTaskState == Arena.Task.INSPECTV) {
                            	m_gb.InspectMove(move);    // i.e. update inspection, if buttons[i] is clicked
                            }
                        }
                    }
            );
        }
        Label ScoreDescription = new Label("Score:");
        ScoreDescription.setFont(font);
        panel.add(ScoreDescription);
        scoreLabel = new JLabel("");
        scoreLabel.setFont(font);
        panel.add(scoreLabel);
        panel.add(new Label());

        panel.add(new Label());
        panel.add(buttons[1]);
        panel.add(new Label());
        panel.add(buttons[0]);
        panel.add(new Label());
        panel.add(buttons[2]);
        panel.add(new Label());
        panel.add(buttons[3]);
        panel.add(new Label());


        return panel;
    }

    private JPanel initvBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 3, 10, 10));
        for (int i = 0; i < 4; i++) {
            vBoard[i] = new JLabel();
            vBoard[i].setText(" ");
            vBoard[i].setOpaque(true);
            vBoard[i].setBackground(Color.orange);
            vBoard[i].setForeground(Color.black);
            Font font = new Font("Consolas", 1, (int)(1.1*Types.GUI_TITLEFONTSIZE));  //22
            vBoard[i].setFont(font);
        }
        panel.add(new Label());
        panel.add(vBoard[1]);
        panel.add(new Label());
        panel.add(vBoard[0]);
        panel.add(new Label());
        panel.add(vBoard[2]);
        panel.add(new Label());
        panel.add(vBoard[3]);
        panel.add(new Label());
        panel.add(new Label());
        panel.add(new Label());
        panel.add(new Label());
        return panel;
    }

    private JPanel initGameInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2, 10, 0));
        Font font = new Font("Arial", 1, (int)(1.2*Types.GUI_HELPFONTSIZE)); // 16

     /*   Label ScoreDescription = new Label("Score:");
        ScoreDescription.setFont(font);
        panel.add(ScoreDescription);
        scoreLabel = new JLabel("");
        scoreLabel.setFont(font);
        panel.add(scoreLabel);*/

   /*     Label realScoreDescription = new Label("modified Score:");
        realScoreDescription.setFont(font);
        panel.add(realScoreDescription);

        realScore = new JLabel("");
        realScore.setFont(font);
        panel.add(realScore);

        Label highestTileInCornerDescription = new Label("highest Tile in Corner:");
        highestTileInCornerDescription.setFont(font);
        panel.add(highestTileInCornerDescription);

        highestTileInCorner = new JLabel("");
        highestTileInCorner.setFont(font);
        panel.add(highestTileInCorner);

        Label highestTileValueDescription = new Label("highest Tile Value:");
        highestTileValueDescription.setFont(font);
        panel.add(highestTileValueDescription);

        highestTileValue = new JLabel("");
        highestTileValue.setFont(font);
        panel.add(highestTileValue);

        Label emptyTilesDescription = new Label("empty Tiles:");
        emptyTilesDescription.setFont(font);
        panel.add(emptyTilesDescription);

        emptyTiles = new JLabel("");
        emptyTiles.setFont(font);
        panel.add(emptyTiles);

        Label rowLengthDescription = new Label("row Length:");
        rowLengthDescription.setFont(font);
        panel.add(rowLengthDescription);

        rowLength = new JLabel("");
        rowLength.setFont(font);
        panel.add(rowLength);

        Label rowValueDescription = new Label("row Value:");
        rowValueDescription.setFont(font);
        panel.add(rowValueDescription);

        rowValue = new JLabel("");
        rowValue.setFont(font);
        panel.add(rowValue);

        Label mergeValueDescription = new Label("merge Value:");
        mergeValueDescription.setFont(font);
        panel.add(mergeValueDescription);

        mergeValue = new JLabel("");
        mergeValue.setFont(font);
        panel.add(mergeValue); */

        return panel;
    }


    private void updateBoardLabel(int pos) {
        int value = 0;
        if (m_gb.m_so != null) {
            value = m_gb.m_so.getTileValue(pos);
        }

        switch (value) {
            case 0:
                board[pos].setText("<html><br><font color='#eee4da'>......</font><br><br></html>");
                board[pos].setBackground(Color.decode("#eee4da"));
                break;
            case 1:
                board[pos].setText("<html><br><font color='#eee4da'>......</font><br><br></html>");
                board[pos].setBackground(Color.decode("#eee4da"));
                break;
            case 2:
                board[pos].setText("<html><br><font color='#eee4da'>...</font>" + value + "<font color='#eee4da'>...</font><br><br></html>");
                board[pos].setBackground(Color.decode("#eee4da"));
                break;
            case 4:
                board[pos].setText("<html><br><font color='#ede0c8'>...</font>" + value + "<font color='#ede0c8'>...</font><br><br></html>");
                board[pos].setBackground(Color.decode("#ede0c8"));
                break;
            case 8:
                board[pos].setText("<html><br><font color='#f2b179'>...</font>" + value + "<font color='#f2b179'>...</font><br><br></html>");
                board[pos].setBackground(Color.decode("#f2b179"));
                break;
            case 16:
                board[pos].setText("<html><br><font color='#f59563'>..</font>" + value + "<font color='#f59563'>...</font><br><br></html>");
                board[pos].setBackground(Color.decode("#f59563"));
                break;
            case 32:
                board[pos].setText("<html><br><font color='#f67c5f'>..</font>" + value + "<font color='#f67c5f'>...</font><br><br></html>");
                board[pos].setBackground(Color.decode("#f67c5f"));
                break;
            case 64:
                board[pos].setText("<html><br><font color='#f65e3b'>..</font>" + value + "<font color='#f65e3b'>...</font><br><br></html>");
                board[pos].setBackground(Color.decode("#f65e3b"));
                break;
            case 128:
                board[pos].setText("<html><br><font color='#edcf72'>..</font>" + value + "<font color='#edcf72'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#edcf72"));
                break;
            case 256:
                board[pos].setText("<html><br><font color='#edcc61'>..</font>" + value + "<font color='#edcc61'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#edcc61"));
                break;
            case 512:
                board[pos].setText("<html><br><font color='#edc850'>..</font>" + value + "<font color='#edc850'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#edc850"));
                break;
            case 1024:
                board[pos].setText("<html><br><font color='#edc53f'>.</font>" + value + "<font color='#edc53f'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#edc53f"));
                break;
            case 2048:
                board[pos].setText("<html><br><font color='#edc22e'>.</font>" + value + "<font color='#edc22e'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#edc22e"));
                break;
            case 4096:
                board[pos].setText("<html><br><font color='#3c3a32'>.</font><font color='#ffffff'>" + value + "</font><font color='#3c3a32'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#3c3a32"));
                break;
            case 8192:
                board[pos].setText("<html><br><font color='#3c3a32'>.</font><font color='#ffffff'>" + value + "</font><font color='#3c3a32'>..</font><br><br></html>");
                board[pos].setBackground(Color.decode("#3c3a32"));
                break;
            default:
                board[pos].setText("<html><br><font color='#3c3a32'>.</font><font color='#ffffff'>" + value + "</font><font color='#3c3a32'>.</font><br><br></html>");
                board[pos].setBackground(Color.decode("#3c3a32"));
                break;
        }
    }

    public void clearBoard(boolean boardClear, boolean vClear) {
        if (boardClear) {
        	m_gb.m_so = new StateObserver2048();
            for (int pos = 0; pos < ConfigGame.ROWS * ConfigGame.COLUMNS; pos++) {
                updateBoardLabel(pos);
            }
            scoreLabel.setText("");
            leftInfo.setText("");
            //rightInfo.setText("");
        }
        if (vClear) {
            vTable = new double[4];
            for (int i = 0; i < 4; i++) {
                vTable[i] = Double.NaN;
                vBoard[i].setText(" ");
                vBoard[i].setBackground(Color.orange);
                vBoard[i].setForeground(Color.black);
            }
        }
    }

    public void updateBoard(StateObserver2048 soZTAV,  
    						boolean withReset, boolean showValueOnGameboard) {
        if (soZTAV != null) {

                if (showValueOnGameboard && soZTAV.getStoredValues() != null) {
                    for (int i = 0; i < 4; i++) {
                        vTable[i] = Double.NaN;
                    }
                    for (int i = 0; i < soZTAV.getStoredValues().length; i++) {
                        Types.ACTIONS action = soZTAV.getStoredAction(i);
                        int iAction = action.toInt();
                        vTable[iAction] = soZTAV.getStoredValues()[i];
                    }
                }

                if (soZTAV.isGameOver()) {
                    int win = soZTAV.getWinState();
                    switch (win) {
                        case (+1):
                            leftInfo.setText("You Won!");
                            break;
                        case (-1):
                            leftInfo.setText("You Lost!");
                            break;
                    }
            		//System.out.println("leftInfo size = " +leftInfo.getFont().getSize());
                }

        }
        guiUpdateBoard(showValueOnGameboard);
    }

    private void guiUpdateBoard(boolean showValueOnGameboard) {
        for (int i = 0; i < 4; i++) {
            if (m_gb.m_so.availableMoves.contains(i)) {
                buttons[i].setEnabled(true);
            } else {
                buttons[i].setEnabled(false);
            }
        }

        double score, maxscore = Double.NEGATIVE_INFINITY;
        int imax = 0;

        for (int pos = 0; pos < ConfigGame.ROWS * ConfigGame.COLUMNS; pos++) {
            updateBoardLabel(pos);
        }

        if (showValueOnGameboard) {
            for (int i = 0; i < 4; i++) {
            	score = (vTable == null) ? Double.NaN : vTable[i];
                
                if (Double.isNaN(score)) {
                    vBoard[i].setText("   ");
                    vBoard[i].setBackground(Color.red);
                } else {
                    double realScore = score * m_gb.m_so.MAXSCORE;
                    String txt = null;
                    if (realScore >= 1000000) {
                        txt = "" + (String.format("%.1f", realScore));
                    } else if (realScore >= 100000) {
                        txt = "" + (String.format("%.2f", realScore));
                    } else if (realScore >= 10000) {
                        txt = "" + (String.format("%.3f", realScore));
                    } else if (realScore >= 1000) {
                        txt = "" + (String.format("%.4f", realScore));
                    } else if (realScore >= 100) {
                        txt = "" + (String.format("%.5f", realScore));
                    } else if (realScore >= 10) {
                        txt = "" + (String.format("%.6f", realScore));
                    } else if (realScore >= 0) {
                        txt = "" + (String.format("%.7f", realScore));
                    } else if (realScore <= -1000000) {
                        txt = "" + (String.format("%.0f", realScore));
                    } else if (realScore <= -100000) {
                        txt = "" + (String.format("%.1f", realScore));
                    } else if (realScore <= -10000) {
                        txt = "" + (String.format("%.2f", realScore));
                    } else if (realScore <= -1000) {
                        txt = "" + (String.format("%.3f", realScore));
                    } else if (realScore <= -100) {
                        txt = "" + (String.format("%.4f", realScore));
                    } else if (realScore <= -10) {
                        txt = "" + (String.format("%.5f", realScore));
                    } else if (realScore == 0) {
                        txt = "" + realScore;
                    }


                    vBoard[i].setText(txt);
                    vBoard[i].setBackground(Color.orange);
                    if (score > maxscore) {
                        maxscore = score;
                        imax = i;
                    }
                }
            }
            vBoard[imax].setBackground(Color.yellow);
        } 
        else {  // i.e. if (!showValueOnGameboard)
        	Color colTHK2 = new Color(255,137,0);
            for (int i = 0; i < 4; i++) {
                vBoard[i].setText("   ");
                vBoard[i].setBackground(colTHK2);
            }        	
        }

        scoreLabel.setText("" + m_gb.m_so.getScore());

      /*  realScore.setText("" + Math.round(m_so.getGameScore(m_so) * m_so.MAXSCORE));
        rowValue.setText("" + (m_so.rowValue));
        mergeValue.setText("" + (m_so.mergeValue));
        highestTileInCorner.setText("" + m_so.highestTileInCorner);
        emptyTiles.setText("" + m_so.emptyTiles.size());
        highestTileValue.setText("" + m_so.highestTileValue);
        rowLength.setText("" + (m_so.rowLength));*/


        super.repaint(0, 0, 0, 850, 550);
    }

    class ActionHandler implements ActionListener {
        int move;

        ActionHandler(int move) {
            this.move = move;
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    public void enableInteraction(boolean enable) {  }

    public void showGameBoard(Arena ztavGame, boolean alignToMain) {
        this.setVisible(true);
        this.toFront();
        if (alignToMain) {
            // place window with game board below the main window
            int x = ztavGame.m_xab.getX() + ztavGame.m_xab.getWidth() + 8;
            int y = ztavGame.m_xab.getLocation().y;
            if (ztavGame.m_ArenaFrame != null) {
                x = ztavGame.m_ArenaFrame.getX();
                y = ztavGame.m_ArenaFrame.getY() + ztavGame.m_ArenaFrame.getHeight() + 1;
                this.setSize(850, 550);
            }
            this.setLocation(x, y);        	
        }
    }

	public void toFront() {
    	super.setState(Frame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

	public void destroy() {
	   this.setVisible(false);
	   this.dispose();
	}


}
