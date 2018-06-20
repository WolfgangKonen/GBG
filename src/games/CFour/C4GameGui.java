package games.CFour;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controllers.PlayAgent;
import tools.MessageBox;

//import openingBook.BookSum;
//
//import c4.AlphaBetaAgent;
//import c4.AlphaBetaTDSAgent;
//import c4.ConnectFour;
//import c4.MoveList;
//import c4.Agent;

/**
 * Contains the GUI
 * 
 * @author Markus Thill
 * 
 */
public class C4GameGui extends JPanel implements ListOperation {

	private static final long serialVersionUID = 12L;

//	// the game buttons and text fields on the right of the window
//	public C4Buttons c4Buttons;
//
//	public C4Menu c4Menu;
	protected GameBoardC4 gameBoardC4;
//	private Thread playThread = null;

	// The Panel including the board and value-bar on the left of the window
	private JPanel boardPanel;

	// The 42 fields of the connect-four board
	private JPanel playingBoardPanel;
	private ImgShowComponent playingBoard[][];

	// The value-panel
	private JLabel[][] valueBoard;

	// Needed, for performing standard connect-four operations (finding
	// win-rows, check if legal move, etc.)
	protected C4Base c4 = new C4Base();

	// All moves during a game are stored in a movelist, so that moves can be
	// taken back or repeated again
	private MoveList mvList = new MoveList();

	// Because the process creates parallel running threads, sometime a
	// semaphore is needed to guarantee exclusive access on some objects
	private Semaphore mutex = new Semaphore(1);

	// Labels for the Values
	JLabel lValueTitle;
	JLabel lValueGTV;
	JLabel lValueAgent;
//	JLabel lValueEval;

	// The opening-books are loaded only once to save memory. All agents, that
	// need them, use the same books.
//	private final BookSum books = new BookSum();

//	// Possible states
//	protected enum State {
//		TRAIN_X, TRAIN_O, TRAIN_EVAL, PLAY, COMPETE, MULTICOMPETE, IDLE, SETBOARD, TESTVALUEFUNC, TESTBESTMOVE, SHOWNTUPLE /* unused */, SETNTUPLE, INSPNTUPLE, MULTITRAIN, EVALUATE, SAVE_X, SAVE_O, SAVE_EVAL, LOAD_X, LOAD_O, LOAD_EVAL, SAVE_WEIGHTS_X, SAVE_WEIGHTS_O, SAVE_WEIGHTS_EVAL, LOAD_WEIGHTS_X, LOAD_WEIGHTS_O, LOAD_WEIGHTS_EVAL
//	};
//
//	protected State state = State.IDLE;
//
//	// Possible Actions
//	protected enum Action {
//		NOACTION, MOVEBACK, NEXTMOVE, DELETE, CHANGE
//	};
//
//	protected Action action = Action.NOACTION;

	// Standard Alpha-Beta-Agent
//	public AlphaBetaAgent alphaBetaStd = null;

	// Players
	protected PlayAgent[] players = new PlayAgent[3];
	private int curPlayer;		// current player, either 0 or 1 

	// Agent for the game-theoretic-values (perfect minimax-agent)
	private PlayAgent GTVab = null;

	// Flag that is set, when a game is won by a player or drawn.
//	@Deprecated
//	private boolean gameOver = false;		// use isGameOver() instead

	// Show gameTheoretic Values
	protected boolean showGTV = false;
	protected boolean showAgentVals = false;
	protected boolean showEvalVals = false;

	// the colors of the TH Köln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);

//	// Competition
//	private Competition comp;
//
//	// Progress for some Functions
//	private Progress progress = null;
//
//	// Other Windows
//	protected OptionsMinimax winOptionsGTV = new OptionsMinimax(
//			AlphaBetaAgent.TRANSPOSBYTES);
//	protected OptionsComp winCompOptions = new OptionsComp();
//	protected ResultOverview winCompResult = new ResultOverview();
//	protected OptionsValueFunc winValueFuncOptions = new OptionsValueFunc();
//	// Not needed anymore: protected ShowNTuples winNTupleWindow = new
//	// ShowNTuples(this);
//	protected ShowTupleList winConfigTuples = new ShowTupleList(this, gameBoardC4,
//			this, "N-Tuple Configuration");
//	protected LUTShow winLUTShow = new LUTShow(this);
//	protected OptionsMultiTrain winMultiTrainOptions = new OptionsMultiTrain();

	// Options-Windows for the current agent-type
	// params[0]:Player X
	// params[1]:Player O
	// params[2]:Player Eval
	protected final JFrame params[] = new JFrame[3];

	// For Setting N-Tuples manually
	protected ArrayList<ArrayList<Integer>> nTupleList = new ArrayList<ArrayList<Integer>>();

	// Evaluation for TD-Agents
//	Evaluate eval;

	// If the status-message was changed, this flag will be set, to indicate,
	// that the statusbar has to be updated
	protected boolean syncStatusBar = false;

	// Flag, that is set, when the Step-Button is selected. This causes the
	// current agent to make a move
	private boolean playStep = false;

	// Saving/Loading of Agents is done with this
//	private LoadSaveTD tdAgentIO;

	public C4GameGui() {
		initGame();
	}

	public C4GameGui(GameBoardC4 frame) {
		gameBoardC4 = frame;
		initGame();
	}

	public void init() {
		// Start a thread
//		playThread = new Thread(this);
//		playThread.start();
	}

	private void initGame() {
		playingBoard = new ImgShowComponent[7][6];
		valueBoard = new JLabel[2][7];

		playingBoardPanel = initPlayingBoard();

		lValueTitle = new JLabel("Overall Result of the Value-Function");
		lValueTitle.setFont(new Font("Times New Roman", 1, 18));
		lValueGTV = new JLabel("Hallo");
		lValueAgent = new JLabel("Hallo");
//		lValueEval = new JLabel("Hallo");

		lValueGTV.setToolTipText("Value for the game-theoretic value");
		lValueAgent.setToolTipText("Value for the selected Agent");
//		lValueEval.setToolTipText("Value for the Evaluation");

		JLabel Title;
		Title = new JLabel("Connect Four",JLabel.CENTER);
		Title.setForeground(Color.black);
		Font font = new Font("Times New Roman", 1, 18);
		Title.setFont(font);

		setLayout(new BorderLayout(10, 10));
		setBackground(Color.white);
		boardPanel = new JPanel();
		boardPanel.add(playingBoardPanel);

		boardPanel.setLayout(new GridBagLayout());
		JPanel z = initValuePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		boardPanel.add(z, c);

		c.gridy++;
		boardPanel.add(lValueTitle, c);

		c.gridy++;
		boardPanel.add(lValueGTV, c);

		c.gridy++;
		boardPanel.add(lValueAgent, c);

//		c.gridy++;
//		boardPanel.add(lValueEval, c);

//		add(c4Buttons, BorderLayout.EAST);
		add(boardPanel, BorderLayout.CENTER);
		add(Title, BorderLayout.NORTH);

//		changeState(State.IDLE);

//		printCurAgents();

//		// Init the Standard Alpha-Beta-Agent
//		// Until yet, there were no changes of the Options
//		OptionsMinimax min = winOptionsGTV;
//		alphaBetaStd = new AlphaBetaAgent(books);
//		alphaBetaStd.resetBoard();
//		alphaBetaStd.setTransPosSize(min.getTableIndex());
//		alphaBetaStd.setBooks(min.useNormalBook(), min.useDeepBook(),
//				min.useDeepBookDist());
//		alphaBetaStd.setDifficulty(min.getSearchDepth());
//		alphaBetaStd.randomizeEqualMoves(min.randomizeEqualMoves());
//
//		// Init evaluation
//		eval = new Evaluate(alphaBetaStd);
	}

	private JPanel initValuePanel() {
		JPanel z = new JPanel();
		z.setLayout(new GridLayout(1, 7, 2, 2));
		z.setBackground(Color.black);
		for (int i = 0; i < C4Base.COLCOUNT; i++) {
			JPanel vb = new JPanel();
			vb.setLayout(new GridLayout(2, 0, 2, 2));
			vb.setBackground(colTHK2);
			for (int j = 0; j < 2; j++) {
				valueBoard[j][i] = new JLabel("0.0", JLabel.CENTER);
				valueBoard[j][i].setOpaque(true);			// only *then* the following setBackground has any effect (!)
				valueBoard[j][i].setBackground(colTHK2);
				vb.add(valueBoard[j][i]);
			}
			valueBoard[0][i].setToolTipText("Values for the single columns "
					+ "of the current board (game-theoretic value)");
			valueBoard[1][i].setToolTipText("Values for the single columns "
					+ "of the current board (selected Agent)");
//			valueBoard[2][i].setToolTipText("Values for the single columns "
//					+ "of the current board (Evaluation)");
			z.add(vb);
		}
		return z;
	}

	public void enableInteraction(boolean enable) {
		playingBoardPanel.setEnabled(enable);
	}
	
	private JPanel initPlayingBoard() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 7, 2, 2));
		panel.setBackground(Color.BLACK);

		for (int i = 0; i < 42; i++)
			panel.add(new Canvas());

		// Add Playing Field
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				playingBoard[i][j] = replaceImage(playingBoard[i][j],
						ImgShowComponent.EMPTY0, i, j);
				panel.remove((5 - j) * 7 + i);
				panel.add(playingBoard[i][j], (5 - j) * 7 + i);
			}
		}

		return panel;
	}

	public boolean isGameOver() {
		return gameBoardC4.getStateObs().isGameOver();
	}

	private void handleMouseClick(int x, int y) {
		gameBoardC4.HGameMove(x, y);
//		switch (state) {
//		case PLAY:
//			if (players[curPlayer] == null && !gameOver)
//				makeCompleteMove(x, "You");
//			break;
////		case SETBOARD:
////			makeCompleteMove(x, "HuiBuh");
////			break;
////		case SETNTUPLE:
////			int index = winConfigTuples.getSelectedIndex();
////			ArrayList<Integer> nTuple = nTupleList.get(index);
////			int find = findInNTuple(x * 6 + y, nTuple);
////			if (find <= -1) {
////				nTuple.add(x * 6 + y);
////				putPiece(x, y, 2);
////			} else {
////				nTuple.remove(find);
////				removePiece(x, y, 2);
////			}
////			action = Action.CHANGE;
////			break;
//		default:
//			break;
//		}
	}

	private class MouseHandler implements MouseListener {
		int x, y;

		MouseHandler(int num1, int num2) {
			x = num1;
			y = num2;
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}

	//
	// this is needed for interface ListOperation
	//
	@Override
	public void indexChanged(int newIndex) {
		// not needed in this class yet
	}
	@Override
	public void playerChanged(Player player) {
		// not needed in this class yet
	}
	
	private ImgShowComponent replaceImage(ImgShowComponent oldImg,
			int imgIndex, int num1, int num2) {
		ImgShowComponent imgShComp = ImgShowComponent.replaceImg(oldImg,imgIndex);
//		ImgShowComponent imgShComp = oldImg.replaceImg2(imgIndex);
		if (oldImg != imgShComp)
			imgShComp.addMouseListener(new MouseHandler(num1, num2) {
				public void mouseClicked(MouseEvent e) {
					handleMouseClick(x, y);
				}
			});
		return imgShComp;
	}

	/**
	 * Call {@link #setPiece(int,int)} for x and player (after checking for win and, if so,
	 * issuing a message box), then test on draw. Print value bar, if game not over.
	 * 
	 * @param x			the column (0,...,COLCOUNT-1) 
	 * @param player	usually {@link #curPlayer}, either 0 or 1
	 * @param sPlayer	player-name string for message box in case of win
	 * @return true, if it is a legal move, false else
	 */
	private boolean makeCompleteMove(int x, int player, String sPlayer) {
		if (!isGameOver() && c4.getColHeight(x) != 6) {
			checkWin(x, sPlayer);
			setPiece(x,player);
			swapPlayer();
			if (c4.isDraw() && !isGameOver()) {
				int col = mvList.readPrevMove();
				if (!c4.canWin(2, col, c4.getColHeight(col) - 1)) {
//					gameOver = true;
					MessageBox.show(gameBoardC4.m_Arena, "Draw!!!       ", "Game Over", JOptionPane.INFORMATION_MESSAGE);
				}
			}
			if (!isGameOver())
				printValueBar();
			return true;
		}
		return false; // error
	}
	
	protected boolean makeCompleteMove(int x, String sPlayer) {
		return makeCompleteMove(x, curPlayer, sPlayer);
	}

	/**
	 * Set and mark the piece of {@code player} in column i, row j. 
	 * Marking is done by adding small white corners in the cell.
	 * Unmark the previously marked cell, if any.
	 * 
	 * @param i			the column (0,...,COLCOUNT-1) 
	 * @param j			the row
	 * @param player	0 or 1
	 */
	protected void setPiece(int i, int j, int player) {
		c4.putPiece(player + 1, i);
		markMove(i,j, player);
		int last = mvList.readPrevMove();
		if (last != -1) {
			int ii = last;
			int jj = c4.getColHeight(ii) - 1;
			if (i == ii)
				jj--;
			unMarkMove(ii, jj, (player+1)%2);
		}

		mvList.putMove(i);
	}

	/**
	 * Set and mark the piece of {@code player} in column x by calling {@link #putPiece(int, int)}. 
	 * Marking is done by adding small white corners in the cell.
	 * Unmark the previously marked cell, if any.
	 * 
	 * @param x			the column (0,...,COLCOUNT-1) 
	 * @param player	0 or 1
	 */
	protected void setPiece(int x, int player) {
		putPiece(x, player);
		int last = mvList.readPrevMove();
		if (last != -1) {
			int i = last;
			int j = c4.getColHeight(i) - 1;
			if (x == i)
				j--;
			unMarkMove(i, j, (player+1)%2);
		}

		mvList.putMove(x);
	}

	private void setPiece(int x) {
		setPiece(x, curPlayer);
	}

	/**
	 * Put and mark the piece of {@code player} in column x.
	 * 
	 * @param x			the column (0,...,COLCOUNT-1) 
	 * @param player	0 or 1
	 */
	private void putPiece(int x, int player) {
		int y = c4.getColHeight(x);
		c4.putPiece(player + 1, x);
		markMove(x, y, player);
	}

	protected void markMove(int x, int y, int player) {
		int imgIndex = (player == 0 ? ImgShowComponent.YELLOW_M
				: ImgShowComponent.RED_M);
		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
				y);

		if (newImg != playingBoard[x][y]) {
			playingBoardPanel.remove((5 - y) * 7 + x);
			playingBoard[x][y] = newImg;
			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
			playingBoardPanel.invalidate();
			playingBoardPanel.validate();
		}
	}

	protected void unMarkMove(int x, int y, int player) {
		int imgIndex = (player == 0 ? ImgShowComponent.YELLOW
				: ImgShowComponent.RED);
		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
				y);
		if (newImg != playingBoard[x][y]) {
			playingBoardPanel.remove((5 - y) * 7 + x);
			playingBoard[x][y] = newImg;
			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
			playingBoardPanel.invalidate();
			playingBoardPanel.validate();
		}
	}

	// --- exactly the same as unMarkMove(int,int,int) ---
//	private void putPiece(int x, int y, int player) {
//		int imgIndex = (player == 0 ? ImgShowComponent.YELLOW
//				: ImgShowComponent.RED);
//		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
//				y);
//		if (newImg != playingBoard[x][y]) {
//			playingBoardPanel.remove((5 - y) * 7 + x);
//			playingBoard[x][y] = newImg;
//			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
//			playingBoardPanel.invalidate();
//			playingBoardPanel.validate();
//		}
//	}

	private void removePiece(int x, int player) {
		int y = c4.getColHeight(x) - 1;
		c4.removePiece(player + 1, x);
		removePiece(x, y, player);
	}

	private void removePiece(int x, int y, int player) {
		int imgIndex = ImgShowComponent.EMPTY0;
		ImgShowComponent newImg = replaceImage(playingBoard[x][y], imgIndex, x,
				y);
		if (newImg != playingBoard[x][y]) {
			playingBoardPanel.remove((5 - y) * 7 + x);
			playingBoard[x][y] = newImg;
			playingBoardPanel.add(playingBoard[x][y], (5 - y) * 7 + x);
			playingBoardPanel.invalidate();
			playingBoardPanel.validate();
		}
	}

	// ==============================================================
	// Button: Move Back
	// ==============================================================
	private void moveBack() {
		if (!mvList.isEmpty()) {
//			gameOver = false;
			int col = mvList.getPrevMove();
			swapPlayer();
			removePiece(col, curPlayer);
			col = mvList.readPrevMove();
			if (col != -1)
				markMove(col, c4.getColHeight(col) - 1, 1 - curPlayer);
//			c4Buttons.setEnabledPlayStep(players[curPlayer] != null);
			printValueBar();
		}
	}

	// ==============================================================
	// Button: Next Move
	// ==============================================================
	private void nextMove() {
		if (mvList.isNextMove()) {
			int prevCol = mvList.readPrevMove();
			if (prevCol != -1)
				unMarkMove(prevCol, c4.getColHeight(prevCol) - 1, curPlayer);
			int col = mvList.getNextMove();
//			if (c4.canWin(curPlayer + 1, col))
//				gameOver = true;
			putPiece(col, curPlayer);
			swapPlayer();
//			c4Buttons.setEnabledPlayStep(players[curPlayer] != null);
			printValueBar();
		}

	}

	protected void resetBoard() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 6; j++) {
				int imgIndex = ImgShowComponent.EMPTY0;
				ImgShowComponent newImg = replaceImage(playingBoard[i][j],
						imgIndex, i, j);
				if (newImg != playingBoard[i][j]) {
					playingBoardPanel.remove((5 - j) * 7 + i);
					playingBoard[i][j] = newImg;
					playingBoardPanel.add(playingBoard[i][j], (5 - j) * 7 + i);
					playingBoardPanel.invalidate();
					playingBoardPanel.validate();
				}
			}
		}
		c4.resetBoard();
		mvList.reset();
//		gameOver = false;
		curPlayer = 0;
	}

	private void checkWin(int x, String sPlayer) {
		checkWin(curPlayer, x, sPlayer);
	}

	private void checkWin(int player, int x, String sPlayer) {
		if (c4.canWin(player + 1, x)) {
			if (sPlayer != null)
				MessageBox.show(gameBoardC4.m_Arena, sPlayer + " Win!!       ", "Game Over", JOptionPane.INFORMATION_MESSAGE);
			else
				MessageBox.show(gameBoardC4.m_Arena,"Game Over!!!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
//			gameOver = true;
		}
	}

	/**
	 * swap {@link #curPlayer} from 0 <--> 1
	 */
	private void swapPlayer() {
		curPlayer = (1 - curPlayer);
	}

	public void setInitialBoard() {
		mvList.reset();
		resetBoard();
		curPlayer = 0;
	}

	private void initPlay() {
		curPlayer = c4.countPieces() % 2;
//		gameOver = false;
	}

	protected void printValueBar() {
		new Thread("") {
			public void run() {
				double[] realVals = new double[7];
				double[] agentVals = new double[7];
				double[] evalVals = new double[7];

				PlayAgent pa = null;
				if (showAgentVals)
					pa = players[curPlayer];

				boolean useSigmoid = false;

				if (pa != null) {
//					agentvals = pa.getNextVTable(c4.getBoard(), false);
				}

				if (agentVals != null)
					for (int i = 0; i < agentVals.length; i++)
						if (Math.abs(agentVals[i]) > 1.0) {
							useSigmoid = true;
							break;
						}

				if (useSigmoid && agentVals != null)
					for (int i = 0; i < agentVals.length; i++)
						agentVals[i] = Math.tanh(agentVals[i]);
				if (showGTV) {
//					realVals = getGTV();
				}
				if (showEvalVals && players[2] != null) {
//					evalVals = players[2].getNextVTable(c4.getBoard(), true);
				}
				printValueBar(realVals,agentVals,evalVals);
				System.gc();
			}
		}.start();
	}

	protected void printValueBar(double[] realVals, double[] agentVals, double[] evalVals) 
	{

				// Reset Labels
				lValueAgent.setText("Agent: ");
//				lValueEval.setText("Eval: ");
				lValueGTV.setText("GTV: ");

				PlayAgent pa = null;
				if (showAgentVals)
					pa = players[curPlayer];

				boolean useSigmoid = false;
				double agentValsMax = max(agentVals);

				if (agentVals!=null) {
					int val = (int) (max(agentVals) * 100);
					lValueAgent.setText("Agent:     " + val);

					for (int i = 0; i < agentVals.length; i++)
						if (Math.abs(agentVals[i]) > 1.0) {
							useSigmoid = true;
							break;
						}
					if (useSigmoid && agentVals != null)
						for (int i = 0; i < agentVals.length; i++)
							agentVals[i] = Math.tanh(agentVals[i]);
				}

				if (realVals!=null) {
					String valGTV = "GTV:        "+ (int) (max(realVals) * 100) + "";
					lValueGTV.setText(valGTV);
					lValueGTV.setPreferredSize(getMaximumSize());
				}
//				if (evalVals!=null) {
//					int val = (int) (max(evalVals) * 100);
//					lValueEval.setText("Eval:        " + val + "");
//				}
				for (int i = 0; i < C4Base.COLCOUNT; i++) {
					if (realVals != null)
						valueBoard[0][i]
								.setText((int) (realVals[i] * 100) + "");
					if (agentVals != null) {
						if (Double.isNaN(agentVals[i])) {
							valueBoard[1][i].setText(" ");						
						} else {
							valueBoard[1][i].setText((int) (agentVals[i] * 100)+ "");						
						}
						if (agentVals[i]==agentValsMax) {
							valueBoard[1][i].setBackground(new Color(46,158,121));		
							valueBoard[1][i].setForeground(Color.WHITE);		
						} else {
							valueBoard[1][i].setBackground(colTHK2);
							valueBoard[1][i].setForeground(Color.BLACK);		
						}
					}
//					if (evalVals != null)
//						valueBoard[2][i]
//								.setText((int) (evalVals[i] * 100) + "");
				}
	}

	private double max(double[] array) {
		double amax = -Double.MAX_VALUE;
		for (int i=0; i<array.length; i++) 
			if (array[i]>amax) amax = array[i];
		return amax;
	}
	
	/*
	private double[] getGTV() {
		if (GTVab == null)
			GTVab = initGTVAgent();
		double[] vals = GTVab.getNextVTable(c4.getBoard(), true);
		return vals;
	}

	private double getSingleGTV() {
		if (GTVab == null)
			GTVab = initGTVAgent();
		return GTVab.getScore(c4.getBoard(), true);
	}


	private PlayAgent initGTVAgent() {
		if (!winOptionsGTV.usePresetting()) {
			AlphaBetaAgent ab = new AlphaBetaAgent(books);
			ab.resetBoard();
			// New
			OptionsMinimax min = winOptionsGTV;
			ab.setTransPosSize(min.getTableIndex());
			ab.setBooks(min.useNormalBook(), min.useDeepBook(),
					min.useDeepBookDist());
			ab.setDifficulty(min.getSearchDepth());
			ab.randomizeEqualMoves(min.randomizeEqualMoves());
			return ab;
		}
		return alphaBetaStd;
	}

	protected PlayAgent initAlphaBetaAgent(int player) {
		if (params[player] == null
				|| !params[player].getClass().equals(OptionsMinimax.class))
			params[player] = new OptionsMinimax(AlphaBetaAgent.TRANSPOSBYTES);
		OptionsMinimax min = (OptionsMinimax) params[player];
		if (!min.usePresetting()) {
			AlphaBetaAgent ab = new AlphaBetaAgent(books);
			ab.resetBoard();

			ab.setTransPosSize(min.getTableIndex());
			ab.setBooks(min.useNormalBook(), min.useDeepBook(),
					min.useDeepBookDist());
			ab.setDifficulty(min.getSearchDepth());
			ab.randomizeEqualMoves(min.randomizeEqualMoves());

			// Using N-Tuple-System for move-Ordering
			// ab.setTDAgent((TDSAgent) players[2]);
			return ab;
		}
		return alphaBetaStd;
	}


	protected PlayAgent initAlphaBetaTDSAgent(int player) {
		if (params[player] == null
				|| !params[player].getClass().equals(OptionsMinimax.class))
			params[player] = new OptionsMinimax(AlphaBetaAgent.TRANSPOSBYTES);
		OptionsMinimax min = (OptionsMinimax) params[player];

		// Evaluation must be a TDS-Player
		AlphaBetaTDSAgent abTDS = new AlphaBetaTDSAgent(books,
				(TDSAgent) players[2]);
		abTDS.resetBoard();

		abTDS.setTransPosSize(min.getTableIndex());
		abTDS.setBooks(min.useNormalBook(), min.useDeepBook(),
				min.useDeepBookDist());
		abTDS.setDifficulty(min.getSearchDepth());
		abTDS.randomizeEqualMoves(min.randomizeEqualMoves());

		return abTDS;
	}

	protected PlayAgent initTDSAgent(int player) {
		TDParams tdPar;
		if (params[player] == null
				|| !params[player].getClass().equals(OptionsTD.class))
			params[player] = new OptionsTD(new TDParams());
		OptionsTD opTD = (OptionsTD) params[player];
		tdPar = opTD.getTDParams();
		AlphaBetaAgent ab = null;
		if (tdPar.stopAfterMoves < 40)
			ab = alphaBetaStd;

		TDSAgent tds = new TDSAgent(tdPar, ab);

		// Set N-Tuples in the Options
		Integer nTuples[][] = tds.m_Net.getNTuples1Dim();
		opTD.setNTuples(nTuples);

		return tds;
	}

	protected PlayAgent initMCTSAgent(int player) {
		if (params[player] == null
				|| !params[player].getClass().equals(OptionsMCTS.class))
			params[player] = new OptionsMCTS(new MCTSParams());
		OptionsMCTS opMCTS = (OptionsMCTS) params[player];
		MCTSPlayer mctsPlayer = new MCTSPlayer(opMCTS.getMCTSParams());

		return mctsPlayer;
	}

    */
}
