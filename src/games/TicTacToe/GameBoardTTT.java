package games.TicTacToe;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.GameBoard;
import games.StateObservation;
import games.Arena;
import games.ArenaTrain;
import tools.Types;

/**
 * Class GameBoardTTT implements interface GameBoard for TicTacToe.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 *
 */
public class GameBoardTTT extends JFrame implements GameBoard {

	private JPanel BoardPanel;
	private JPanel VBoardPanel; 
	private JLabel leftInfo=new JLabel(" left ");
	private JLabel rightInfo=new JLabel(" right "); 
	private Arena 	m_Arena;	// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	/**
	 * The clickable representation of the board in the GUI. The buttons of Board will be enabled only
	 * when "Play" or "Inspect V" are clicked. During "Play" only unoccupied fields are enabled,
	 * during "Inspect V" all fields are enabled and they change with each click in a ring:
	 * {@literal   " " -> "X" -> "O" -> " "}.
	 */
	protected Button[][] Board;
	/**
	 * The representation of the value function corresponding to the current 
	 * {@link #Board} position.
	 */
	protected Label[][] VBoard;		
	private StateObserverTTT m_so;
	private int[][] Table;			// =1: position occupied by "X" player
									//=-1: position occupied by "O" player
	private double[][] VTable;
	private boolean arenaActReq=false;
	
	public GameBoardTTT(Arena ticGame) {
		initGameBoard(ticGame);
	}
	
	private void initGameBoard(Arena ticGame) 
	{
		m_Arena		= ticGame;
		Board       = new Button[3][3];
		VBoard		= new Label[3][3];
		BoardPanel	= InitBoard();
		VBoardPanel = InitVBoard();
		Table       = new int[3][3];
		VTable		= new double[3][3];
		m_so		= new StateObserverTTT();	// empty table
        rand 		= new Random(System.currentTimeMillis());	

		JPanel titlePanel = new JPanel();
		JLabel Blank=new JLabel(" ");		// a little bit of space
		JLabel Title=new JLabel("Tic Tac Toe",SwingConstants.CENTER);
		Title.setForeground(Color.black);	
		Font font=new Font("Arial",1,20);			
		Title.setFont(font);	
		titlePanel.add(Blank);
		titlePanel.add(Title);
		
		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		boardPanel.add(BoardPanel);
		boardPanel.add(new Label("    "));		// some space
		boardPanel.add(VBoardPanel);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		Font font2=new Font("Arial",1,10);			
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

	// called from initGame() 
	private JPanel InitBoard()
	{
		JPanel panel=new JPanel();
		panel.setLayout(new GridLayout(3,3,14,10));
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				Board[i][j] = new Button("  ");
				Board[i][j].setBackground(Color.black);
				Board[i][j].setForeground(Color.white);
				Font font=new Font("Arial",1,14);
		        Board[i][j].setFont(font);
				Board[i][j].setEnabled(false);
				Board[i][j].addActionListener(					
						new ActionHandler(i,j)  // constructor copies (i,j) to members (x,y)
						{
							public void actionPerformed(ActionEvent e)
							{
								Arena.Task aTaskState = m_Arena.taskState;
								if (aTaskState == Arena.Task.PLAY)
								{
									HGameMove(x,y);		// i.e. make human move (i,j), if Board[i][j] is clicked								
								}
								if (aTaskState == Arena.Task.INSPECTV)
								{
									InspectMove(x,y);	// i.e. update inspection, if Board[i][j] is clicked								
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
	
	private JPanel InitVBoard()
	{
		JPanel panel=new JPanel();
		panel.setLayout(new GridLayout(3,3,10,10));
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				VBoard[i][j] = new Label("    ");
				VBoard[i][j].setBackground(Color.orange);
				VBoard[i][j].setForeground(Color.black);
				Font font=new Font("Arial",1,12);
				VBoard[i][j].setFont(font);
				panel.add(VBoard[i][j]);
			}
		}
		return panel;
	}

//	@Override
//	public void clearBoard() {
//		clearBoard(true, true);
//	}
//
//	private void ClearBoard()
//	{
//		clearBoard(true, false);
//	}
//			
//	public void ClearVBoard()
//	{
//		clearBoard(false, true);
//	}
	
	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			m_so = new StateObserverTTT();			// empty Table
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					Board[i][j].setLabel("  ");
					Board[i][j].setBackground(Color.black);
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
					VBoard[i][j].setText("   ");
					VBoard[i][j].setBackground(Color.orange);
					VBoard[i][j].setForeground(Color.black);
				}
			}
		}
	}

//	@Override
//	public void updateBoard() {
//		updateBoard(null,false,false);
//
//	}
//	@Override
//	public void updateBoard(StateObservation so) {
//		updateBoard(so,false,false);
//	}

	/**
	 * Update the play board and the associated VBoard.
	 * 
	 * @param so	the game state
	 * @param showStoredV	if true, show the values (scores) for the available actions
	 * 						(only if they are stored in 'so')
	 * @param enableOccupiedCells  if true, then allow user interaction on occupied 
	 * 						cells (needed for inspecting the value function)
	 */
	@Override
	public void updateBoard(StateObservation so, boolean showStoredV,
							boolean enableOccupiedCells) {
		int i,j;
		if (so!=null) {
	        assert (so instanceof StateObserverTTT)
			: "StateObservation 'so' is not an instance of StateObserverTTT";
			StateObserverTTT soT = (StateObserverTTT) so;
			m_so = soT.copy();
			//Table = soT.getTable();
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
			
			if (showStoredV && soT.storedValues!=null) {
				for(i=0;i<3;i++)
					for(j=0;j<3;j++) 
						VTable[i][j]=Double.NaN;	
				
				for (int k=0; k<soT.storedValues.length; k++) {
					Types.ACTIONS action = soT.storedActions[k];
					int iAction = action.toInt();
					j=iAction%3;
					i=(iAction-j)/3;
					VTable[i][j] = soT.storedValues[k];					
				}	
				switch(Player) {
				case(+1): 
					rightInfo.setText("    Score for X"); break;
				case(-1):
					rightInfo.setText("    Score for O"); break;
				}

			} else {
				rightInfo.setText("");
			}
		} // if(so!=null)
		
		guiUpdateBoard(enableOccupiedCells);
	}

	/**
	 * Update the play board and the associated VBoard to the state in m_so.
	 * VBoard contains the values (scores) for each unoccupied board position (raw score*100).  
	 * Occupied board positions have green background color, unoccupied positions are orange and
	 * the best position has a yellow background color.
	 * 
	 * @param enable As a side effect the buttons Board[i][j] which are occupied by either "X" or "O"
	 * will be set into enabled state <code>enable</code>. (All unoccupied positions will get state 
	 * <code>true</code>.)
	 */ 
	private void guiUpdateBoard(boolean enable)
	{		
		double score, maxscore=Double.NEGATIVE_INFINITY;
		int imax=0,jmax=0;
		int[][] table = m_so.getTable();
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				if(table[i][j]==1)
				{
					Board[i][j].setLabel("X");				
					Board[i][j].setEnabled(enable);
				}					
				else if(table[i][j]==-1)
				{
					Board[i][j].setLabel("O");				
					Board[i][j].setEnabled(enable);
				}
				else
				{
					Board[i][j].setLabel("-"); //("  ");
					Board[i][j].setEnabled(true);
				}
				if (VTable==null) { 
					// HumanPlayer and MCTSAgentT do not have a VTable (!)
					score = Double.NaN;
				} else {
					score = VTable[i][j];					
				}
				if (Double.isNaN(score)) {
					VBoard[i][j].setText("   ");
					VBoard[i][j].setBackground(Color.green);						
				} else {
					String txt = " "+(int)(score*100);
					if (score<0) txt = ""+(int)(score*100);
					VBoard[i][j].setText(txt);
					if (table[i][j]==0) {
						VBoard[i][j].setBackground(Color.orange);
					} else {
						VBoard[i][j].setBackground(Color.green);						
					}
					if (score>maxscore) {
						maxscore=score;
						imax=i;
						jmax=j;
					}
				}
			}
		}
		VBoard[imax][jmax].setBackground(Color.yellow);
		paint(this.getGraphics());
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
		// TODO Auto-generated method stub

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
	
	private void HGameMove(int x, int y)
	{
//		int pieceCount=0;
//		for (int i=0; i<3; i++) 
//			for (int j=0; j<3; j++)
//				pieceCount += Table[i][j];
//		Table[x][y]=((pieceCount%2==0) ? 1 : -1);
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act);			// this allows m_so to add random elements from game 
									// environment (not necessary in TicTacToe)
		updateBoard(null,false,false);
		arenaActReq = true;			// ask Arena for next action
	}
	
	private void InspectMove(int x, int y)
	{
		int[][] table = m_so.getTable();
		int hvalue = ((table[x][y] + 2) % 3) -1;	// i.e. state change 0->1, 1->-1, -1->0 
		table[x][y]=hvalue;
		m_so = new StateObserverTTT(table);
		arenaActReq = true;		
	}
	
	public void showGameBoard(Arena ticGame) {
		this.setVisible(true);
		// place window with game board below the main window
		int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
		int y = ticGame.m_xab.getLocation().y;
		if (ticGame.m_TicFrame!=null) {
			x = ticGame.m_TicFrame.getX();
			y = ticGame.m_TicFrame.getY() + ticGame.m_TicFrame.getHeight() +1;
			this.setSize(ticGame.m_TicFrame.getWidth(),250);	
		}
		this.setLocation(x,y);
		
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
	public StateObservation chooseStartState01() {
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

}
