package games.TicTacToe;

import java.awt.BorderLayout;
import java.awt.Color;
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
import games.CFour.GameBoardC4;
import games.CFour.StateObserverC4;
import games.ArenaTrain;
import tools.ScoreTuple;
import tools.Types;

/**
 * Class GameBoardTTTGui has the board game GUI. 
 * <p>
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 *
 */
public class GameBoardTTTGui extends JFrame {

	private int TICGAMEHEIGHT=280;
	private JPanel BoardPanel;
	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel(""); 
//	protected Arena  m_Arena;		// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	/**
	 * The clickable representation of the board in the GUI. The buttons of {@link #Board} will 
	 * be enabled only when "Play" or "Inspect V" are clicked. During "Play" and "Inspect V"  
	 * only unoccupied fields are enabled. The value function of each {@link #Board} position
	 * is displayed as its label.
	 */
	protected JButton[][] Board;
//	private StateObserverTTT m_so;
	private int[][] Table;			// =1: position occupied by "X" player
									//=-1: position occupied by "O" player
	private double[][] VTable;
	
	/**
	 * a reference to the 'parent' {@link GameBoardTTT} object
	 */
	private GameBoardTTT m_gb=null;
	
	// the colors of the TH Koeln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);
	
	public GameBoardTTTGui(GameBoardTTT gb) {
		super("Tic Tac Toe");
		m_gb = gb;
		initGui("");
	}
	
    private void initGui(String title) 
	{
		Board       = new JButton[3][3];
		BoardPanel	= InitBoard();
		Table       = new int[3][3];
		VTable		= new double[3][3];
//		m_so		= new StateObserverTTT();	// empty table
        rand 		= new Random(System.currentTimeMillis());	

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank=new JLabel(" ");		// a little bit of space
		//JLabel Title=new JLabel("Tic Tac Toe",SwingConstants.CENTER);
//		JLabel Title=new JLabel("   ",SwingConstants.CENTER);  // no title, it appears sometimes in the wrong place
//		Title.setForeground(Color.black);	
//		Title.setFont(font);	
		titlePanel.add(Blank);
//		titlePanel.add(Title);
		
		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		boardPanel.add(BoardPanel);
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
								// showGameBoard(Arena,boolean)
	}

	// called from initGame() 
	private JPanel InitBoard()
	{
		JPanel panel=new JPanel();
		//JButton b = new JButton();
		panel.setLayout(new GridLayout(3,3,2,2));
		int buSize = (int)(50*Types.GUI_SCALING_FACTOR_X);
		Dimension minimumSize = new Dimension(buSize,buSize); //controls the button sizes
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				Board[i][j] = new JButton("  ");
				Board[i][j].setBackground(colTHK2);
				Board[i][j].setForeground(Color.white);
				Board[i][j].setMargin(new Insets(0,0,0,0));  // sets zero margin between the button's
															 // border and the label (so that there 
															 // is room for big labels)
				Font font=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
		        Board[i][j].setFont(font);
				Board[i][j].setEnabled(false);
				Board[i][j].setPreferredSize(minimumSize); 
				Board[i][j].addActionListener(					
						new ActionHandler(i,j)  // constructor copies (i,j) to members (x,y)
						{
							public void actionPerformed(ActionEvent e)
							{
								Arena.Task aTaskState = m_gb.m_Arena.taskState;
								if (aTaskState == Arena.Task.PLAY)
								{
									m_gb.HGameMove(x,y);		// i.e. make human move (i,j), if Board[i][j] is clicked								
								}
								if (aTaskState == Arena.Task.INSPECTV)
								{
									m_gb.InspectMove(x,y);	// i.e. update inspection, if Board[i][j] is clicked								
								}	
								int dummy=1;
							}
						}
				);
				panel.add(Board[i][j]);
			}
		}
		return panel;
	}
	
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
//			m_so = new StateObserverTTT();			// empty Table
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					Board[i][j].setText("  ");
					Board[i][j].setBackground(colTHK2);
					Board[i][j].setForeground(Color.white);
					Board[i][j].setEnabled(false);
				}
			}
		}
		if (vClear) {
			VTable		= new double[3][3];
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					VTable[i][j] = Double.NaN;
				}
			}
		}
	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param soT	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverTTT soT, 
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;
		if (soT!=null) {
			//Table = soT.getTable();
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
				for(i=0;i<3;i++)
					for(j=0;j<3;j++) 
						VTable[i][j]=Double.NaN;	
				
				for (int k=0; k<soT.getStoredValues().length; k++) {
					Types.ACTIONS action = soT.getStoredAction(k);
					int iAction = action.toInt();
					j=iAction%3;
					i=(iAction-j)/3;
					VTable[i][j] = soT.getStoredValues()[k];					
				}	
				if (showValueOnGameboard) {
					String splus = (m_gb.m_Arena.taskState == Arena.Task.INSPECTV) ? "X" : "O";
					String sminus= (m_gb.m_Arena.taskState == Arena.Task.INSPECTV) ? "O" : "X";
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
		
		guiUpdateBoard(false,showValueOnGameboard);
	}

	/**
	 * Update the play board and the associated values (labels) to the state in m_so.
	 * The labels contain the values (scores) for each unoccupied board position (raw score*100).  
	 * Occupied board positions have black/white background color, unoccupied positions are orange.
	 * 
	 * @param enable As a side effect the buttons Board[i][j] which are occupied by either "X" or "O"
	 * will be set into enabled state <code>enable</code>. (All unoccupied positions will get state 
	 * <code>true</code>.)
	 */ 
	private void guiUpdateBoard(boolean enable, boolean showValueOnGameboard)
	{		
		double value, maxvalue=Double.NEGATIVE_INFINITY;
		String valueTxt;
		int imax=0,jmax=0;
		int[][] table = m_gb.m_so.getTable();
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				if (VTable==null) { 
					// HumanPlayer and MCTSAgentT do not have a VTable (!)
					value = Double.NaN;
				} else {
					value = VTable[i][j];					
				}
				
				if (Double.isNaN(value)) {
					valueTxt = "   ";
				} else {
					valueTxt = " "+(int)(value*100);
					if (value<0) valueTxt = ""+(int)(value*100);
					if (value>maxvalue) {
						maxvalue=value;
						imax=i;
						jmax=j;
					}
				}
				if(table[i][j]==1)
				{
					Board[i][j].setText("X");				
					Board[i][j].setEnabled(enable);
					Board[i][j].setBackground(Color.black);
					Board[i][j].setForeground(Color.white);
				}					
				else if(table[i][j]==-1)
				{
					Board[i][j].setText("O");				
					Board[i][j].setEnabled(enable);
					Board[i][j].setBackground(Color.white);
					Board[i][j].setForeground(Color.black);
				}
				else
				{
					Board[i][j].setText("  ");
					Board[i][j].setEnabled(true);
					Board[i][j].setForeground(Color.black);
					Board[i][j].setBackground(colTHK2);
				}
				if (showValueOnGameboard) Board[i][j].setText(valueTxt);
			}
		}
		this.repaint();
//		paint(this.getGraphics());   // this sometimes leave one of the buttons un-painted
	}		

	public void enableInteraction(boolean enable) {

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
	
	public void showGameBoard(Arena arena, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
			int y = arena.m_xab.getLocation().y;
			if (arena.m_ArenaFrame!=null) {
				x = arena.m_ArenaFrame.getX();
				y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() +1;
				this.setSize(arena.m_ArenaFrame.getWidth(),
							 (int)(Types.GUI_SCALING_FACTOR_Y*TICGAMEHEIGHT));	
			}
			this.setLocation(x,y);	
		}		
//		System.out.println("GameBoardTTT size = " +super.getWidth() + " * " + super.getHeight());
//		System.out.println("Arena size = " +ticGame.m_ArenaFrame.getWidth() + " * " + ticGame.m_ArenaFrame.getHeight());

	}

	public void toFront() {
    	super.setState(JFrame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

   public void destroy() {
		this.setVisible(false);
		this.dispose();
   }

}
