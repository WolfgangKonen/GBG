package games.ZweiTausendAchtundVierzig;

import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Johannes on 18.11.2016.
 */
public class GameBoard2048 extends JFrame implements GameBoard {
    private JPanel boardPanel;
    private JPanel buttonPanel;
    private JPanel vButtonPanel;
    private JPanel gameInfo;
    private double[] vTable;
    private JLabel leftInfo = new JLabel("");
    private JLabel rightInfo = new JLabel("");
    private Arena m_Arena;
    private StateObserver2048 m_so;
    private boolean arenaActReq=false;


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
    private JLabel realScore;
    private JLabel highestTileInCorner;
    private JLabel highestTileValue;
    private JLabel emptyTiles;
    private JLabel rowLength;
    private JLabel rowValue;
    private JLabel mergeValue;



    private void initGameBoard(Arena ztavGame) {
        m_Arena         = ztavGame;
        buttons         = new JButton[4];
        vBoard          = new JLabel[4];
        board           = new JLabel[ConfigGame.ROWS * ConfigGame.COLUMNS];
        scoreLabel      = new JLabel();
        boardPanel      = initBoard();
        buttonPanel     = initButton();
        vButtonPanel    = initvBoard();
        gameInfo        = initGameInfo();

        vTable          = new double[4];
        m_so		    = new StateObserver2048();


        JPanel titlePanel = new JPanel();
        JLabel Blank=new JLabel(" ");		// a little bit of space
        JLabel Title=new JLabel("2048",SwingConstants.CENTER);
        Title.setForeground(Color.black);
        Font font=new Font("Arial",1,20);
        Title.setFont(font);
        titlePanel.add(Blank);
        titlePanel.add(Title);

        JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        boardPanel.add(this.boardPanel);
        boardPanel.add(new Label("    "));		// some space


        JPanel rightPanelLeft = new JPanel();
        rightPanelLeft.setLayout(new GridLayout(2,1,30,30));
        rightPanelLeft.add(buttonPanel);
        rightPanelLeft.add(vButtonPanel);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(1,2,30,30));
        rightPanel.add(rightPanelLeft);
        rightPanel.add(gameInfo);
        boardPanel.add(rightPanel);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        leftInfo.setFont(font);
        rightInfo.setFont(font);
        infoPanel.add(leftInfo);
        infoPanel.add(rightInfo);
        infoPanel.setSize(100,10);

        setLayout(new BorderLayout(10,0));
        add(titlePanel,BorderLayout.NORTH);
        add(boardPanel,BorderLayout.CENTER);
        add(infoPanel,BorderLayout.SOUTH);
        pack();
        setVisible(false);
    }

    private JPanel initBoard() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(ConfigGame.ROWS, ConfigGame.COLUMNS,20,20));
        for(int pos = 0; pos < ConfigGame.ROWS * ConfigGame.COLUMNS; pos++) {
            board[pos] = new JLabel();
            board[pos].setOpaque(true);
            updateBoardLabel(pos);
            board[pos].setForeground(Color.black);
            board[pos].setBorder(BorderFactory.createLineBorder(Color.black, 2));
            Font font = new Font("Consolas", 1, 22);
            board[pos].setFont(font);
            panel.add(board[pos]);
        }
        return panel;
    }

    private JPanel initButton() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,3,10,10));
        buttons[0] = new JButton("left");
        buttons[1] = new JButton("up");
        buttons[2] = new JButton("right");
        buttons[3] = new JButton("down");

        Font font = new Font("Arial",1,22);

        for(int i = 0; i < 4; i++) {
            buttons[i].setBackground(Color.white);
            buttons[i].setForeground(Color.black);
            buttons[i].setFont(font);
            buttons[i].setEnabled(false);
            buttons[i].addActionListener(
                    new ActionHandler(i)  // constructor copies (i) to members (x,y)
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            Arena.Task aTaskState = m_Arena.taskState;
                            if (aTaskState == Arena.Task.PLAY)
                            {
                                HGameMove(move);		// i.e. make human move (i), if buttons[i] is clicked
                            }
                            if (aTaskState == Arena.Task.INSPECTV)
                            {
                                InspectMove(move);	// i.e. update inspection, if buttons[i] is clicked
                            }
                        }
                    }
            );
        }
        panel.add(new Label());
        panel.add(new Label());
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
        panel.setLayout(new GridLayout(4,3,10,10));
        for(int i = 0; i < 4; i++) {
            vBoard[i]= new JLabel();
            vBoard[i].setText(" ");
            vBoard[i].setOpaque(true);
            vBoard[i].setBackground(Color.orange);
            vBoard[i].setForeground(Color.black);
            Font font=new Font("Consolas",1,22);
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
        panel.setLayout(new GridLayout(8,2,10,0));
        Font font = new Font("Arial",1,16);

        Label ScoreDescription = new Label("Score:");
        ScoreDescription.setFont(font);
        panel.add(ScoreDescription);
        scoreLabel = new JLabel("");
        scoreLabel.setFont(font);
        panel.add(scoreLabel);

        Label realScoreDescription = new Label("modified Score:");
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
        panel.add(mergeValue);

        return panel;
    }



    private void updateBoardLabel(int pos) {
        int value = 0;
        if(m_so != null) {
            value = m_so.getTileValue(pos);
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

    @Override
    public void clearBoard(boolean boardClear, boolean vClear) {
        if(boardClear) {
            m_so = new StateObserver2048();
            for(int pos = 0; pos< ConfigGame.ROWS * ConfigGame.COLUMNS; pos++) {
                updateBoardLabel(pos);
            }
            scoreLabel.setText("");
            leftInfo.setText("");
            rightInfo.setText("");
            realScore.setText("");
            highestTileInCorner.setText("");
            highestTileValue.setText("");
            emptyTiles.setText("");
        }
        if(vClear) {
            vTable = new double[4];
            for(int i = 0; i < 4; i++) {
                vTable[i] = Double.NaN;
                vBoard[i].setText(" ");
                vBoard[i].setBackground(Color.orange);
                vBoard[i].setForeground(Color.black);
            }
        }
    }

    @Override
    public void updateBoard(StateObservation so, boolean showStoredV, boolean enableOccupiedCells) {
                
        if(so != null) {
    		if (so instanceof StateObserver2048) {
                StateObserver2048 soZTAV = (StateObserver2048) so;
                // ** activate next line only if m_so is of class StateObserver2048: **
                m_so = soZTAV.copy();
                
                if (showStoredV && soZTAV.storedValues!=null) {
                    for(int i=0;i<4;i++) {
                        vTable[i] = Double.NaN;
                    }
                    for(int i = 0; i < soZTAV.storedValues.length; i++) {
                        Types.ACTIONS action = soZTAV.storedActions[i];
                        int iAction = action.toInt();
                        vTable[iAction] = soZTAV.storedValues[i];
                    }
                }
    		} else {
    			throw new RuntimeException("StateObservation 'so' is not an instance of StateObserver2048");
    		}

            if(so.isGameOver()) {
                int win = so.getGameWinner().toInt();
                switch(win) {
                    case(+1):
                        leftInfo.setText("You Won!");
                        break;
                    case(-1):
                        leftInfo.setText("You Lost!");
                        break;
                }
            }

        }
        guiUpdateBoard();
    }

    private void guiUpdateBoard() {
        for(int i = 0; i < 4; i++) {
            if(m_so.availableMoves.contains(i)) {
                buttons[i].setEnabled(true);
            }
            else {
                buttons[i].setEnabled(false);
            }
        }



        double score, maxscore=Double.NEGATIVE_INFINITY;
        int imax = 0;

        for(int pos = 0; pos< ConfigGame.ROWS * ConfigGame.COLUMNS; pos++) {
            updateBoardLabel(pos);
        }

        for(int i = 0; i < 4; i++) {
            if(vTable == null) {
                score = Double.NaN;
            }
            else {
                score = vTable[i];
            }
            if(Double.isNaN(score)) {
                vBoard[i].setText("   ");
                vBoard[i].setBackground(Color.red);
            } else {
                double realScore = score*m_so.MAXSCORE;
                String txt = null;
                if(realScore >= 1000000) {
                    txt = ""+(String.format("%.1f",realScore));
                } else if(realScore >= 100000) {
                    txt = ""+(String.format("%.2f",realScore));
                } else if(realScore >= 10000) {
                    txt = ""+(String.format("%.3f",realScore));
                } else if(realScore >= 1000) {
                    txt = ""+(String.format("%.4f",realScore));
                } else if(realScore >= 100) {
                    txt = ""+(String.format("%.5f",realScore));
                } else if(realScore >= 10) {
                    txt = ""+(String.format("%.6f",realScore));
                } else if(realScore >= 0) {
                    txt = ""+(String.format("%.7f",realScore));
                } else if(realScore <= -1000000) {
                    txt = ""+(String.format("%.0f",realScore));
                } else if(realScore <= -100000) {
                    txt = ""+(String.format("%.1f",realScore));
                } else if(realScore <= -10000) {
                    txt = ""+(String.format("%.2f",realScore));
                } else if(realScore <= -1000) {
                    txt = ""+(String.format("%.3f",realScore));
                } else if(realScore <= -100) {
                    txt = ""+(String.format("%.4f",realScore));
                } else if(realScore <= -10) {
                    txt = ""+(String.format("%.5f",realScore));
                } else if(realScore == 0) {
                    txt = ""+realScore;
                }


                vBoard[i].setText(txt);
                vBoard[i].setBackground(Color.orange);
                if (score>maxscore) {
                    maxscore=score;
                    imax=i;
                }
            }
        }
        vBoard[imax].setBackground(Color.yellow);

        scoreLabel.setText("" + m_so.getScore());

        realScore.setText("" + Math.round(m_so.getGameScore()*m_so.MAXSCORE));
        rowValue.setText("" + (m_so.rowValue));
        mergeValue.setText("" + (m_so.mergeValue));
        highestTileInCorner.setText("" + m_so.highestTileInCorner);
        emptyTiles.setText("" + m_so.emptyTiles.size());
        highestTileValue.setText("" + m_so.highestTileValue);
        rowLength.setText("" + (m_so.rowLength));




        super.repaint(0,0,0,850,550);
    }

    @Override
    public void showGameBoard(Arena ztavGame) {
        this.setVisible(true);
        // place window with game board below the main window
        int x = ztavGame.m_xab.getX() + ztavGame.m_xab.getWidth() + 8;
        int y = ztavGame.m_xab.getLocation().y;
        if (ztavGame.m_TicFrame!=null) {
            x = ztavGame.m_TicFrame.getX();
            y = ztavGame.m_TicFrame.getY() + ztavGame.m_TicFrame.getHeight() +1;
            this.setSize(1300,550);
        }
        this.setLocation(x,y);
    }

    @Override
    public boolean isActionReq() {
        return arenaActReq;
    }

    @Override
    public void setActionReq(boolean actionReq) {
        arenaActReq=actionReq;
    }

    @Override
    public void enableInteraction(boolean enable) {

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

    @Override
    public StateObservation chooseStartState01() {
        return null;
    }

    public GameBoard2048(Arena ztavGame) {
        initGameBoard(ztavGame);
    }



    class ActionHandler implements ActionListener {
        int move;

        ActionHandler(int move)
        {
            this.move = move;
        }
        public void actionPerformed(ActionEvent e){}
    }

    private void HGameMove(int move)
    {
        Types.ACTIONS act = Types.ACTIONS.fromInt(move);
        assert m_so.isLegalAction(act) : "Desired action is not legal";
        m_so.advance(act);
        arenaActReq = true;			// ask Arena for next action
    }

    private void InspectMove(int move)
    {
        Types.ACTIONS act = Types.ACTIONS.fromInt(move);
        assert m_so.isLegalAction(act) : "Desired action is not legal";
        m_so.advance(act);
        arenaActReq = true;
    }

	@Override
	public String getSubDir() {
		return null;
	}
}
