package games.RubiksCube;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
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

import controllers.PlayAgent;
import games.GameBoard;
import games.StateObservation;
import games.Arena;
import games.Arena.Task;
import games.RubiksCube.CSArrayList.CSAListType;
import games.RubiksCube.CSArrayList.TupleInt;
import games.RubiksCube.CubeState.Twist;
import games.ArenaTrain;
import tools.Types;

/**
 * Class GameBoardCube implements interface GameBoard for RubiksCube.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Köln, Feb'18
 */
public class GameBoardCube extends JFrame implements GameBoard {

	private int TICGAMEHEIGHT=280;
	private JPanel BoardPanel;
	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel(""); 
	protected Arena  m_Arena;		// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	/**
	 * The clickable representation of the board in the GUI. The buttons of Board will be enabled only
	 * when "Play" or "Inspect V" are clicked. During "Play" and "Inspect V" only unoccupied 
	 * fields are enabled.
	 */
	protected Button[][] Board;
	/**
	 * The representation of the state corresponding to the current 
	 * {@link #Board} position.
	 */
	private StateObserverCube m_so;
	private double[][] VTable;
	private CSArrayList[] D;		// the array of distance sets
	private int pMax = 5;			// up to which p this array of distance sets is filled
	private double[][] Xper = 								// 1st index:
			new double[][]{{0.0}, {0.0,1.0}, {0,0.2,1.0}	// [0],[1],[2]
				,{0,0.1,0.2,1.0},{0,0.1,0.2,0.5,1.0}		//,[3],[4]
				,{0,0.05,0.10,0.25,0.5,1.0}					//,[5]
			};
	private boolean arenaActReq=false;
	
	// the colors of the TH Köln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);
	
	public GameBoardCube(Arena ticGame) {
		initGameBoard(ticGame);
		generateDistanceSets();
	}
	
	private void initGameBoard(Arena ticGame) 
	{
		m_Arena		= ticGame;
		Board       = new Button[3][3];
		BoardPanel	= InitBoard();
		VTable		= new double[3][3];
		m_so		= new StateObserverCube();	// empty table
		long seed = 999;
		rand 		= new Random(seed);
//        rand 		= new Random(System.currentTimeMillis());	

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank=new JLabel(" ");		// a little bit of space
		JLabel Title=new JLabel("   ",SwingConstants.CENTER);  // no title, it appears sometimes in the wrong place
		Title.setForeground(Color.black);	
		Font font=new Font("Arial",1,20);			
		Title.setFont(font);	
		titlePanel.add(Blank);
		titlePanel.add(Title);
		
		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		boardPanel.add(BoardPanel);
		boardPanel.setBackground(Types.GUI_BGCOLOR);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.setBackground(Types.GUI_BGCOLOR);
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
		panel.setLayout(new GridLayout(3,3,2,2));
		Dimension minimumSize = new Dimension(50,50); //controls the button sizes
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
				Board[i][j] = new Button("  ");
				Board[i][j].setBackground(colTHK2);
				Board[i][j].setForeground(Color.white);
				Font font=new Font("Arial",Font.BOLD,14);
		        Board[i][j].setFont(font);
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
				panel.add(Board[i][j]);
			}
		}
		return panel;
	}
	
	private void generateDistanceSets() {
		
		System.out.println("\nGenerating distance sets ..");
		//            0          4                   8
		int[] Narr = {0,0,9,54, 321,1847,9992,50136, 50,50,50,50};	// for GenerateNext
//		int[] Narr = {0,0,9,50, 150,600,3000,15000,  50,50,50,50};  // for GenerateNextColSymm
		int[] theoCov = {1,9,54,321,  	// the known maximum sizes for D[0],D[1],D[2],D[3] ...
				1847,9992,50136,227536,	// ... and D[4],D[5],D[6],D[7],
				870072,1887748,623800,	// ... and D[8],D[9],D[10],D[7],
				2644					// ... and D[11]
		};
		boolean silent=false;
		boolean doAssert=true;
//		CSAListType csaType = CSAListType.GenerateNextColSymm;
		CSAListType csaType = CSAListType.GenerateNext;
		ArrayList<TupleInt>[] tintList = new ArrayList[12];
    	D 	= new CSArrayList[12];
		D[0] = new CSArrayList(CSAListType.GenerateD0);
		D[1] = new CSArrayList(CSAListType.GenerateD1);
		//D[1].assertTwistSeqInArrayList();
		for (int p=2; p<=pMax; p++) {			// a preliminary set up to pMax - later we need it up to p=11
			if (p>1) silent=true;
			if (p>3) doAssert=false;
			tintList[p] = new ArrayList();
			//System.out.print("Generating distance set for p="+p+" ..");
			long startTime = System.currentTimeMillis();
			
			D[p] = new CSArrayList(csaType, D[p-1], D[p-2], Narr[p],tintList[p], silent, doAssert);
			
			double elapsedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
			//assert(CubeStateMap.countDifferentStates(D[p])==D[p].size()) : "D["+p+"]: size and # diff. states differ!";
			//D[p].assertTwistSeqInArrayList();
			System.out.println("\nCoverage D["+p+"] = "+D[p].size()+" of "+ theoCov[p]
					+"    Time="+elapsedTime+" sec");
			//CSArrayList.printTupleIntList(tintList[p]);
			//CSArrayList.printLastTupleInt(tintList[p]);
			int dummy=1;
		}
	}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			m_so = new StateObserverCube();			// solved cube
		}
	}

	/**
	 * Update the play board and the associated VBoard.
	 * 
	 * @param so	the game state
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in 'so')
	 * @param enableOccupiedCells  if true, allow user interaction on occupied 
	 * 				cells (may be needed for inspecting the value function)
	 */
	@Override
	public void updateBoard(StateObservation so, 
							boolean enableOccupiedCells, boolean showValueOnGameboard) {
		int i,j;
		if (so!=null) {
	        assert (so instanceof StateObserverCube)
			: "StateObservation 'so' is not an instance of StateObserverCube";
			StateObserverCube soT = (StateObserverCube) so;
			m_so = soT.copy();
			if (so.isGameOver()) {
				int win = so.getGameWinner().toInt();
				leftInfo.setText("You solved it!   "); 				
			}
			
			if (showValueOnGameboard && soT.storedValues!=null) {
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
				rightInfo.setText("");					
			} 
		} // if(so!=null)
		
		guiUpdateBoard(enableOccupiedCells,showValueOnGameboard);
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
	private void guiUpdateBoard(boolean enable, boolean showValueOnGameboard)
	{		
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
		int iAction = 3*x+y;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
		assert m_so.isLegalAction(act) : "Desired action is not legal";
		m_so.advance(act);			// perform action (optionally add random elements from game 
									// environment - not necessary in TicTacToe)
		(m_Arena.getLogManager()).addLogEntry(act, m_so, m_Arena.getLogSessionID());
		updateBoard(null,false,false);
		arenaActReq = true;			// ask Arena for next action
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
									// environment - not necessary in RubiksCube)
		updateBoard(null,false,false);
		arenaActReq = true;		
	}
	
	@Override
	public void showGameBoard(Arena ticGame, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
			int y = ticGame.m_xab.getLocation().y;
			if (ticGame.m_LaunchFrame!=null) {
				x = ticGame.m_LaunchFrame.getX();
				y = ticGame.m_LaunchFrame.getY() + ticGame.m_LaunchFrame.getHeight() +1;
				this.setSize(ticGame.m_LaunchFrame.getWidth(),TICGAMEHEIGHT);	
			}
			this.setLocation(x,y);	
		}		
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
	 * Return a start state depending on {@code pa}'s {@link PlayAgent#getGameNum()} and
	 * {@link PlayAgent#getMaxGameNum()} by randomly selecting from the distance sets in 
	 * {@code this.D}: <br>
	 * Set X=Xper[pMax]. If the proportion of training games is in the first X[1] percent, select 
	 * from D[1], if it is in the first X[2] percent, select from D[2], and so on. In this 
	 * way we realize <b>time-reverse learning</b> (from the cube's end-game to the more complex
	 * cube states) during the training process. The cumulative percentage X is currently 
	 * hard-coded in GameBoardCube.
	 * 
	 * @param 	pa the agent to be trained, we need it here only for its {@link PlayAgent#getGameNum()} 
	 * 			and {@link PlayAgent#getMaxGameNum()}
	 * @return 	the start state for the next training episode
	 */
	@Override
	public StateObservation chooseStartState(PlayAgent pa) {
		int p;
		clearBoard(true, true);			// m_so is in default start state 
		double[] X = Xper[pMax];
		double x = ((double)pa.getGameNum())/pa.getMaxGameNum() + 1e-10;
		for (p=1; p<=pMax; p++) {
			if (X[p-1]<x && x<X[p]) break;
		}
		int index = rand.nextInt(D[p].size());
		CubeState cS = (CubeState)D[p].get(index);
		cS.minTwists = p; 
		cS.lastTwist = Twist.ID;
//		// Each call shall pick a random, but different element from D[p]. 
//		// Therefore we remove from D[p] every element which has already been picked:
//		D[p].remove(cS);

		m_so = new StateObserverCube(cS);
		
		return m_so;
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
    	super.setState(Frame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

   public int getPMax() {
	   return pMax;
   }
   public CSArrayList[] getD() {
	   return D;
   }
   public double[][] getXper() {
	   return Xper;
   }
}
