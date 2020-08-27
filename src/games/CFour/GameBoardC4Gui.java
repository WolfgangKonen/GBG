package games.CFour;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.Arena;
import games.Arena.Task;
import tools.ScoreTuple;
import tools.Types;

/**
 * Class GameBoardC4Gui has the board game GUI. 
 * <p>
 * It shows board game states, optionally the values of possible next actions,
 * has an internal member of class {@link C4GameGui} for the low-level member functions.
 * <p>
 * [The user interaction method HGameMove (used to enter legal moves during game play or to enter board 
 * positions during 'Inspect') stays in {@link GameBoardC4}, since it needs access to its members. 
 * HGameMove is called from {@link C4GameGui}'s {@code handleMouseClick(int,int)}.]
 * 
 * @author Wolfgang Konen, TH Koeln, 2018-2020
 *
 */
public class GameBoardC4Gui extends JFrame {
	private final int C4GAMEHEIGHT=512;
	private int[][] m_board;		
//	private int[][] last_board;		//was only needed for now deprecated guiUpdateBoard2
	private double[] VTable;
	private final JLabel leftInfo=new JLabel("");
	private final JLabel rightInfo=new JLabel("");
//	// the colors of the TH Koeln logo (currently not used):
//	private Color colTHK1 = new Color(183,29,13);
//	private Color colTHK2 = new Color(255,137,0);
//	private Color colTHK3 = new Color(162,0,162);
	
	/**
	 * a reference to the 'parent' {@link GameBoardC4} object
	 */
	private GameBoardC4 m_gb=null;
	/**
	 * The clickable representation of the board in the GUI. The buttons of {@link #m_board} will 
	 * be enabled only when "Play" or "Inspect V" are clicked. During "Play" and "Inspect V"  
	 * only unoccupied columns are enabled. The value function of each column is displayed 
	 * in the value bar (bottom).
	 */
	private C4GameGui c4GameBoard;

	public GameBoardC4Gui(GameBoardC4 gb) throws HeadlessException {
		super("Connect Four");
		m_gb = gb;
		initGui("");
	}

//	public GameBoardC4Gui(GraphicsConfiguration arg0) {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}
//
//	public GameBoardC4Gui(String arg0) throws HeadlessException {
//		super(arg0);
//		// TODO Auto-generated constructor stub
//	}
//
//	public GameBoardC4Gui(String arg0, GraphicsConfiguration arg1) {
//		super(arg0, arg1);
//		// TODO Auto-generated constructor stub
//	}
	
	private void initGui(String title) {
    	c4GameBoard	= new C4GameGui(m_gb);
		m_board 	= ((StateObserverC4) m_gb.getStateObs()).getBoard();
//		last_board	= ((StateObserverC4) m_gb.getStateObs()).getBoard();
		VTable		= new double[C4Base.COLCOUNT];

    	JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
//		JLabel Blank=new JLabel(" ");		// a little bit of space
//		titlePanel.add(Blank);
		
		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		boardPanel.add(c4GameBoard);
		boardPanel.setBackground(Types.GUI_BGCOLOR);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.setBackground(Types.GUI_BGCOLOR);
//		System.out.println("leftInfo size = " +leftInfo.getFont().getSize());
		Font font=new Font("Arial",Font.PLAIN,(int)(1.2*Types.GUI_HELPFONTSIZE));
		leftInfo.setFont(font);	
		rightInfo.setFont(font);	
//		System.out.println("leftInfo size = " +leftInfo.getFont().getSize());
		infoPanel.add(leftInfo);
		infoPanel.add(rightInfo);
//		infoPanel.setSize(100,10);
		
		setLayout(new BorderLayout(10,0));
		add(titlePanel,BorderLayout.NORTH);				
		add(boardPanel,BorderLayout.CENTER);
		add(infoPanel,BorderLayout.SOUTH);
		pack();
		setVisible(false);		// note that the true size of this component is set in 
								// this.showGameBoard(Arena,boolean)
		
	}

	public void clearBoard(boolean boardClear, boolean vClear, StateObserverC4 m_so) {
		if (boardClear) {
			c4GameBoard.setInitialBoard();
//			last_board = m_so.getBoard();
		}
		if (vClear) {
			//VTable		= new double[C4Base.COLCOUNT];
			for(int i=0;i<C4Base.COLCOUNT;i++){
					VTable[i] = Double.NaN;
			}
			c4GameBoard.printValueBar(null, VTable, null);
		}
	}
	
	/**
	 * Update the play board and the associated values (labels) to the state in soT.
	 * The labels contain the values (scores) for each unoccupied board position (raw score*100).  
	 * Occupied board positions have black/white background color, unoccupied positions are orange.
	 * 
	 * @param withReset If true, update the GUI to the current board state with a prior reset. 
	 * If false, update the GUI assuming that it is in the previous board state (faster and allows
	 * to mark the last move).
	 */ 
	public void guiUpdateBoard(StateObserverC4 soT, Task taskState, 
			boolean withReset, boolean showValueOnGameboard)
	{	
		
		if (soT!=null) {
			m_board = soT.getBoard();
			int Player=Types.PLAYER_PM[soT.getPlayer()];
			switch(Player) {
			case(+1):
				leftInfo.setText("X to move   "); break;
			case(-1):
				leftInfo.setText("O to move   "); break;
			}
			if (soT.isGameOver()) {
				ScoreTuple sc = soT.getGameScoreTuple();
				int winner = sc.argmax();
				if (sc.max()==0.0) winner = -2;	// tie indicator
				switch(winner) {
				case( 0): 
					leftInfo.setText("X has won   "); break;
				case( 1):
					leftInfo.setText("O has won   "); break;
				case(-2):
					leftInfo.setText("Tie         "); break;
				}
				// old code, we want to make getGameWinner obsolete
//				int win = so.getGameWinner().toInt();
//				int check = Player*win;
//				switch(check) {
//				case(+1): 
//					leftInfo.setText("X has won   "); break;
//				case(-1):
//					leftInfo.setText("O has won   "); break;
//				case(0):
//					leftInfo.setText("Tie         "); break;
//				}
				
			}
			
			if (showValueOnGameboard && soT.getStoredValues()!=null) {
				for(int i=0;i<C4Base.COLCOUNT;i++){
					VTable[i] = Double.NaN;
				}
				
				for (int k=0; k<soT.getStoredValues().length; k++) {
					Types.ACTIONS action = soT.getStoredAction(k);
					int iAction = action.toInt();
					VTable[iAction] = soT.getStoredValues()[k];					
				}

				String splus = (taskState == Arena.Task.INSPECTV) ? "X" : "O";
				String sminus= (taskState == Arena.Task.INSPECTV) ? "O" : "X";
				switch(Player) {
					case(+1):
						rightInfo.setText("    Score for " + splus); break;
					case(-1):
						rightInfo.setText("    Score for " + sminus); break;
				}
			}
			if (!showValueOnGameboard) {
				rightInfo.setText("");
			}

		} // if(soT!=null)

		if (withReset) guiUpdateBoard1(m_board);
		else guiUpdateBoard3(soT);
//		else guiUpdateBoard2(m_board);
		
		if (showValueOnGameboard) {
			c4GameBoard.printValueBar(null, VTable, null);
		}
		this.repaint();
	}		

	private void guiUpdateBoard1(int[][] m_board) {
		// --- this alternative works for the LogManager (it allows moving back & to set an   
		// --- arbitrary board), but is slower and has not the last move marked.
		c4GameBoard.resetBoard();
		for(int j=0;j<C4Base.ROWCOUNT;j++){
			for(int i=0;i<C4Base.COLCOUNT;i++){
				if(m_board[i][j]==C4Base.PLAYER1)
				{
					c4GameBoard.unMarkMove(i,j, (C4Base.PLAYER1-1));
				}					
				else if(m_board[i][j]==C4Base.PLAYER2)
				{
					c4GameBoard.unMarkMove(i,j, (C4Base.PLAYER2-1));
				}
			}
		}		
	}

	// this method is deprecated, because it does not work correctly if doing "Inspect V" for a few moves and then
	// starting "Play". Use guiUpdateBoard3 instead.
	@Deprecated
//	private void guiUpdateBoard2(int[][] m_board) {
//		// --- this alternative updates only the last move and has the last move
//		// --- marked. It is faster, but does not work for the LogManager (does not allow to
//		// --- move back & requires the board to be already in the previous position).
//		for(int j=0;j<C4Base.ROWCOUNT;j++){
//			for(int i=0;i<C4Base.COLCOUNT;i++){
//				if(m_board[i][j]!=last_board[i][j]) {
//					c4GameBoard.setPiece(i,j, (m_board[i][j]-1));
//					last_board[i][j] = m_board[i][j];
//				}
//			}
//		}
//	}

	private void guiUpdateBoard3(StateObserverC4 soT) {
		// --- this alternative updates only the last move and has the last move
		// --- marked. It is faster, but does not work for the LogManager (does not allow to
		// --- move back & requires the board to be already in the previous position).
		//
		// This NEW method works with LastCell lastCell, prevCell stored in soT. It is simpler to maintain than
		// guiUpdateBoard2 and solves the InspectV-Play-bug (the former guiUpdateBoard2 displayed wrong moves when doing
		// several "Inspect V" moves and then starting "Play")
		if (soT!=null)
			c4GameBoard.setPiece3(soT);
	}

	public void enableInteraction(boolean enable) {
		c4GameBoard.enableInteraction(enable);
		if (enable) {
	        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
//		else {
//	        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//		}
	}

	public void showGameBoard(Arena ticGame, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
			int y = ticGame.m_xab.getLocation().y;
			if (ticGame.m_ArenaFrame!=null) {
				x = ticGame.m_ArenaFrame.getX();
				y = ticGame.m_ArenaFrame.getY() + ticGame.m_ArenaFrame.getHeight() +1;
				this.setSize(ticGame.m_ArenaFrame.getWidth(),
							 (int)(Types.GUI_SCALING_FACTOR_Y*C4GAMEHEIGHT));	
			}
			this.setLocation(x,y);	
		}		
//		System.out.println("GameBoardC4 size = " +super.getWidth() + " * " + super.getHeight());
//		System.out.println("Arena size = " +ticGame.m_ArenaFrame.getWidth() + " * " + ticGame.m_ArenaFrame.getHeight());

	}

	public void toFront() {
    	super.setState(JFrame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

   public void destroy() {
	   this.c4GameBoard.setVisible(false);
	   this.setVisible(false);
	   this.dispose();
   }
}
