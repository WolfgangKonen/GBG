package games.Nim;

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
import games.ArenaTrain;
import tools.Types;

/**
 * Class GameBoardNim implements interface GameBoard for Nim.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Köln, Dec'18
 *
 */
public class GameBoardNim extends JFrame implements GameBoard {

	private int TICGAMEHEIGHT=280;
	private JPanel BoardPanel;
	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel(""); 
	protected Arena  m_Arena;		// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	/**
	 * The clickable representation of the board in the GUI. The buttons of {@link #Board} will 
	 * be enabled only when "Play" or "Inspect V" are clicked. During "Play" and "Inspect V"  
	 * only unoccupied fields are enabled. The value function of each {@link #Board} position
	 * is displayed as its label.
	 */
	protected JLabel[] Heap;  
	protected JButton[][] Board;
	protected JLabel[][] VBoard;
	private StateObserverNim m_so;
	private int[][] Table;			// =1: position occupied by "X" player
									//=-1: position occupied by "O" player
	private double[][] VTable;
	private boolean arenaActReq=false;
	
	// the colors of the TH Köln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);
	
	public GameBoardNim(Arena nimGame) {
		super("Nim");
		initGameBoard(nimGame);
		clearBoard(true,true);
	}
	
    @Override
    public void initialize() {}

    private void initGameBoard(Arena nimGame) 
	{
		m_so		= new StateObserverNim();	// heaps according to NimConfig
		m_Arena		= nimGame;
		Heap		= new JLabel[NimConfig.NUMBER_HEAPS];
		Board       = new JButton[NimConfig.NUMBER_HEAPS][NimConfig.MAX_SUB];
		VBoard      = new JLabel[NimConfig.NUMBER_HEAPS][NimConfig.MAX_SUB];
		VTable		= new double[NimConfig.NUMBER_HEAPS][NimConfig.MAX_SUB];
		BoardPanel	= InitBoard();
        rand 		= new Random(System.currentTimeMillis());	

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank=new JLabel(" ");		// a little bit of space
		titlePanel.add(Blank);
		
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
		panel.setLayout(new GridLayout(1,NimConfig.NUMBER_HEAPS,15,15));
		panel.setBackground(Types.GUI_BGCOLOR);
		int buSize = (int)(30*Types.GUI_SCALING_FACTOR_X);
		Dimension minimumSize = new Dimension(buSize,buSize); //controls the button sizes
		Font font=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
		for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
			JPanel hPanel=new JPanel();
			hPanel.setLayout(new BorderLayout(10,0));
			Font hFont=new Font("Arial",Font.BOLD,4*Types.GUI_HELPFONTSIZE);
			Heap[i] = new JLabel(" ");
			Heap[i].setFont(hFont);
			Heap[i].setText(m_so.getHeaps()[i]+"");
			Heap[i].setHorizontalAlignment(SwingConstants.CENTER);
			hPanel.add(Heap[i],BorderLayout.CENTER);
			JPanel bPanel=new JPanel();
			bPanel.setLayout(new GridLayout(2,NimConfig.MAX_SUB,2,2));
			for(int j=0;j<NimConfig.MAX_SUB;j++){
				Board[i][j] = new JButton("  ");
				Board[i][j].setBackground(colTHK2);
				Board[i][j].setForeground(Color.white);
				Board[i][j].setMargin(new Insets(0,0,0,0));  // sets zero margin between the button's
															 // border and the label (so that there 
															 // is room for big labels)
		        Board[i][j].setFont(font);
		        Board[i][j].setText((j+1)+"");
				Board[i][j].setEnabled(false);
				Board[i][j].setPreferredSize(minimumSize); 
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
				bPanel.add(Board[i][j]);
			} // for(j)
			for(int j=0;j<NimConfig.MAX_SUB;j++){
				VBoard[i][j] = new JLabel("  ");
				VBoard[i][j].setForeground(colTHK1);
				VBoard[i][j].setHorizontalAlignment(SwingConstants.CENTER);
//				VBoard[i][j].setMargin(new Insets(0,0,0,0));  // sets zero margin between the button's
//															 // border and the label (so that there 
//															 // is room for big labels)
				VBoard[i][j].setFont(font);
				VBoard[i][j].setEnabled(true);
				VBoard[i][j].setPreferredSize(minimumSize); 
				bPanel.add(VBoard[i][j]);
			} // for(j)
			hPanel.add(bPanel,BorderLayout.SOUTH);
			panel.add(hPanel);

		}
		return panel;
	}
	
	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			m_so = new StateObserverNim();			// heaps according to NimConfig
			for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
				Heap[i].setText(m_so.getHeaps()[i]+"");
				for(int j=0;j<NimConfig.MAX_SUB;j++){
			        Board[i][j].setText((j+1)+"");
					Board[i][j].setBackground(colTHK2);
					Board[i][j].setForeground(Color.white);
					Board[i][j].setEnabled(false);
				}
			}
			ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
			for (int k=0; k<acts.size(); k++) {
				int iAction = acts.get(k).toInt();
				int j=iAction%NimConfig.MAX_SUB;
				int i=(iAction-j)/NimConfig.MAX_SUB;		
				Board[i][j].setEnabled(true);
			}
				
		}
		if (vClear) {
			for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
				for(int j=0;j<NimConfig.MAX_SUB;j++){
					VTable[i][j] = Double.NaN;
				}
			}
		}
	}

	/**
	 * Update the play board and the associated values (labels) to the new state {@code so}.
	 * 
	 * @param so	the game state. If {@code null}, call only {@link #guiUpdateBoard(boolean, boolean)}.
	 * @param withReset  if true, reset the board prior to updating it to state {@code so}
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;
		if (so!=null) {
	        assert (so instanceof StateObserverNim)
			: "StateObservation 'so' is not an instance of StateObserverNim";
			StateObserverNim soT = (StateObserverNim) so;
			m_so = ((StateObserverNim) so).copy();
			int Player=Types.PLAYER_PM[m_so.getPlayer()];
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
				for(i=0;i<NimConfig.NUMBER_HEAPS;i++)
					for(j=0;j<NimConfig.MAX_SUB;j++)
						VTable[i][j]=Double.NaN;	
				
				for (int k=0; k<soT.getStoredValues().length; k++) {
					Types.ACTIONS action = soT.getStoredAction(k);
					int iAction = action.toInt();
					j=iAction%NimConfig.MAX_SUB;
					i=(iAction-j)/NimConfig.MAX_SUB;		
					VTable[i][j] = soT.getStoredValues()[k];					
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
		for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
			Heap[i].setText(m_so.getHeaps()[i]+"");
		}
		for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
			for(int j=0;j<NimConfig.MAX_SUB;j++){
				Board[i][j].setEnabled(false);

				value = VTable[i][j];	
				if (Double.isNaN(value)) {
					valueTxt = "   ";
				} else {
					valueTxt = " "+(int)(value*100);
					if (value<0) valueTxt = ""+(int)(value*100);
				}
				if (showValueOnGameboard) VBoard[i][j].setText(valueTxt);
			}
		}
		ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
		for (int k=0; k<acts.size(); k++) {
			int iAction = acts.get(k).toInt();
			int j=iAction%NimConfig.MAX_SUB;
			int i=(iAction-j)/NimConfig.MAX_SUB;		
			Board[i][j].setEnabled(true);
		}
		this.repaint();
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
		// reverse: iAction = MAX_SUB*heap + j
		int iAction = NimConfig.MAX_SUB*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in Nim)
		(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
//		updateBoard(null,false,false);
		arenaActReq = true;			// ask Arena for next action
	}
	
	private void InspectMove(int x, int y)
	{
		// reverse: iAction = MAX_SUB*heap + j
		int iAction = NimConfig.MAX_SUB*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		if (!m_so.isLegalAction(act)) {
			System.out.println("Desired action is not legal!");
			m_Arena.setStatusMessage("Desired action is not legal");
			return;
		} else {
			m_Arena.setStatusMessage("Inspecting the value function ...");
		}
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in Nim)
//		updateBoard(null,false,false);
		arenaActReq = true;		
	}
	
	public void showGameBoard(Arena nimGame, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = nimGame.m_xab.getX() + nimGame.m_xab.getWidth() + 8;
			int y = nimGame.m_xab.getLocation().y;
			if (nimGame.m_LaunchFrame!=null) {
				x = nimGame.m_LaunchFrame.getX();
				y = nimGame.m_LaunchFrame.getY() + nimGame.m_LaunchFrame.getHeight() +1;
				this.setSize(nimGame.m_LaunchFrame.getWidth(),
							 (int)(Types.GUI_SCALING_FACTOR_Y*TICGAMEHEIGHT));	
			}
			this.setLocation(x,y);	
		}		
//		System.out.println("GameBoardNim size = " +super.getWidth() + " * " + super.getHeight());
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
