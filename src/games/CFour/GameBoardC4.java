package games.CFour;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controllers.PlayAgent;
import games.GameBoard;
import games.StateObservation;
import games.Arena;
import games.Arena.Task;
import games.ArenaTrain;
import tools.Types;

/**
 * Class GameBoardC4 implements interface GameBoard for Connect Four.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Köln, May'18
 *
 */
public class GameBoardC4 extends JFrame implements GameBoard {

	private int C4GAMEHEIGHT=512;
	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel(""); 
	protected Arena  m_Arena;		// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	/**
	 * The clickable representation of the board in the GUI. The buttons of {@link #Board} will 
	 * be enabled only when "Play" or "Inspect V" are clicked. During "Play" and "Inspect V"  
	 * only unoccupied columns are enabled. The value function of each column is displayed 
	 * in the value bar (bottom).
	 */
	private C4GameGui c4GameBoard;
	private StateObserverC4 m_so;
	private int[][] m_board;		
	private int[][] last_board;		
	private double[] VTable;
	private boolean arenaActReq=false;
	
	// the colors of the TH Köln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);
	
	public GameBoardC4(Arena arGame) {
		super("Connect Four");
		initGameBoard(arGame);
	}
	
    @Override
    public void initialize() {}

    private void initGameBoard(Arena arGame) 
	{
		m_Arena		= arGame;
		c4GameBoard	= new C4GameGui(this);
		VTable		= new double[C4Base.COLCOUNT];
		m_so		= new StateObserverC4();	// empty table
		m_board 	= m_so.getBoard();
        rand 		= new Random(System.currentTimeMillis());	

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
		Font font=new Font("Arial",0,(int)(1.2*Types.GUI_HELPFONTSIZE));			
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

	// called from initGame() 
	private JPanel InitBoard()
	{
		JPanel panel=new JPanel();
		//JButton b = new JButton();
		panel.setLayout(new GridLayout(3,3,2,2));
//		int buSize = (int)(50*Types.GUI_SCALING_FACTORX);
//		Dimension minimumSize = new Dimension(buSize,buSize); //controls the button sizes
		return panel;
	}
	
	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			c4GameBoard.setInitialBoard();
			m_so = new StateObserverC4();			// empty Table
			last_board = m_so.getBoard();
		}
		if (vClear) {
			VTable		= new double[C4Base.COLCOUNT];
			for(int i=0;i<C4Base.COLCOUNT;i++){
					VTable[i] = Double.NaN;
			}
			c4GameBoard.printValueBar(null, VTable, null);
		}
	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param so	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in in state {@code so}).
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;
		if (so!=null) {
	        assert (so instanceof StateObserverC4)
			: "StateObservation 'so' is not an instance of StateObserverC4";
			StateObserverC4 soT = (StateObserverC4) so;
			m_so = soT.copy();
			m_board = soT.getBoard();
			int Player=Types.PLAYER_PM[soT.getPlayer()];
			switch(Player) {
			case(+1): 
				leftInfo.setText("X to move   "); break;
			case(-1):
				leftInfo.setText("O to move   "); break;
			}
			if (so.isGameOver()) {
				int win = so.getGameWinner().toInt();
				int check = Player*win;
				switch(check) {
				case(+1): 
					leftInfo.setText("X has won   "); break;
				case(-1):
					leftInfo.setText("O has won   "); break;
				case(0):
					leftInfo.setText("Tie         "); break;
				}
				
			}
			
			if (showValueOnGameboard && soT.getStoredValues()!=null) {
				for(i=0;i<C4Base.COLCOUNT;i++){
					VTable[i] = Double.NaN;
				}
				
				for (int k=0; k<soT.getStoredValues().length; k++) {
					Types.ACTIONS action = soT.getStoredAction(k);
					int iAction = action.toInt();
					VTable[iAction] = soT.getStoredValues()[k];					
				}	
				if (showValueOnGameboard) {
					String splus = (m_Arena.taskState == Arena.Task.INSPECTV) ? "X" : "O";
					String sminus= (m_Arena.taskState == Arena.Task.INSPECTV) ? "O" : "X";
					switch(Player) {
					case(+1): 
						rightInfo.setText("    Score for " + splus); break;
					case(-1):
						rightInfo.setText("    Score for " + sminus); break;
					}					
				} else {
					rightInfo.setText("");					
				}
			} 
		} // if(so!=null)
		
		guiUpdateBoard(withReset,showValueOnGameboard);
		repaint();
	}

	/**
	 * Update the play board and the associated values (labels) to the state in m_so.
	 * The labels contain the values (scores) for each unoccupied board position (raw score*100).  
	 * Occupied board positions have black/white background color, unoccupied positions are orange.
	 * 
	 * @param withReset If true, update the GUI to the current board state with a prior reset. 
	 * If false, update the GUI assuming that it is in the previous board state (faster and allows
	 * to mark the last move).
	 */ 
	private void guiUpdateBoard(boolean withReset, boolean showValueOnGameboard)
	{		
		if (withReset) guiUpdateBoard1();
		else guiUpdateBoard2();
		
		if (showValueOnGameboard) {
//			double[] value = new double[C4Base.COLCOUNT];
//			String[] valueTxt = new String[C4Base.COLCOUNT];
//			for(int i=0;i<C4Base.COLCOUNT;i++){
//				if (VTable==null) { 
//					// HumanPlayer and MCTSAgentT do not have a VTable (!)
//					value[i] = Double.NaN;
//				} else {
//					value[i] = VTable[i];					
//				}
//				
//				if (Double.isNaN(value[i])) {
//					valueTxt[i] = "   ";
//				} else {
//					valueTxt[i] = " "+(int)(value[i]*100);
//					if (value[i]<0) valueTxt[i] = ""+(int)(value[i]*100);
//				}
//			}
//			c4GameBoard.printValueBar(null, value, null);
			c4GameBoard.printValueBar(null, VTable, null);
		}
		this.repaint();
	}		

	private void guiUpdateBoard1() {
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
	
	private void guiUpdateBoard2() {
		// --- this alternative updates only the last move and has the last move 
		// --- marked. It is faster, but does not work for the LogManager (does not allow to 
		// --- move back & requires the board to be already in the previous position).
		for(int j=0;j<C4Base.ROWCOUNT;j++){
			for(int i=0;i<C4Base.COLCOUNT;i++){
				if(m_board[i][j]!=last_board[i][j]) {
					c4GameBoard.setPiece(i,j, (m_board[i][j]-1));
					last_board[i][j] = m_board[i][j];
				}
			}
		}		
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
	 * @param	actReq true : GameBoard requests an action from Arena 
	 * 			(see {@link #isActionReq()})
	 */
	@Override
	public void setActionReq(boolean actReq) {
		arenaActReq=actReq;
	}

	@Override
	public void enableInteraction(boolean enable) {
		c4GameBoard.enableInteraction(enable);
		if (enable) {
	        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else {
//	        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		}
	}

	/**
	 * This class is needed for each ActionListener of {@code Board[i][j]} in 
	 * {@link #InitBoard()}
	 *
	 */
	class ActionHandler implements ActionListener
	{
		int x,y;
		
		ActionHandler(int num1,int num2)			
		{		
			x=num1;
			y=num2;
		}
		public void actionPerformed(ActionEvent e){}			
	}
	
	protected void HGameMove(int x, int y)
	{
		int iAction = x;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
//		assert m_so.isLegalAction(act) : "Desired action is not legal";
		if (m_so.isLegalAction(act)) {
			System.out.println(m_so.stringDescr());
			m_so.advance(act);			// perform action (optionally add random elements from game 
										// environment - not necessary in ConnectFour)
			(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
//			updateBoard(null,false,false);
			arenaActReq = true;			// ask Arena for next action
		}
	}
	
	private void InspectMove(int x, int y)
	{
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!m_so.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			m_Arena.setStatusMessage("Desired action is not legal");
			return;
		} else {
			m_Arena.setStatusMessage("Inspecting the value function ...");
		}
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in TicTacToe)
//		updateBoard(null,false,false);
		arenaActReq = true;		
	}
	
	public void showGameBoard(Arena ticGame, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
			int y = ticGame.m_xab.getLocation().y;
			if (ticGame.m_LaunchFrame!=null) {
				x = ticGame.m_LaunchFrame.getX();
				y = ticGame.m_LaunchFrame.getY() + ticGame.m_LaunchFrame.getHeight() +1;
				this.setSize(ticGame.m_LaunchFrame.getWidth(),
							 (int)(Types.GUI_SCALING_FACTOR_Y*C4GAMEHEIGHT));	
			}
			this.setLocation(x,y);	
		}		
//		System.out.println("GameBoardC4 size = " +super.getWidth() + " * " + super.getHeight());
//		System.out.println("Arena size = " +ticGame.m_LaunchFrame.getWidth() + " * " + ticGame.m_LaunchFrame.getHeight());

	}

	public StateObservation getStateObs() {
		return m_so;
	}

	/**
	 * @return the 'empty-board' start state
	 */
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
		clearBoard(true, true);			// m_so is in default start state 
		if (rand.nextDouble()>0.5) {
			// choose randomly one of the possible actions in default 
			// start state and advance m_so by one ply
			ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
			int i = (int) (rand.nextInt(acts.size()));
			m_so.advance(acts.get(i));
		}
		return m_so;
	}

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
    

	@Override
	public String getSubDir() {
		return null;
	}
	
    @Override
    public Arena getArena() {
        return m_Arena;
    }
    
   @Override
	public void toFront() {
    	super.setState(JFrame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

}
