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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controllers.AgentBase;
import controllers.MaxNAgent;
import games.Arena;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class GameBoardNimGui has the board game GUI. 
 * <p>
 * It shows board game states, optionally the values of possible next actions, and (last row) the optimal values of 
 * the possible next actions, and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is inspected. 
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class GameBoardNimGui extends JFrame {

	private int TICGAMEHEIGHT=580;
	private JPanel BoardPanel;
	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel(""); 
	/**
	 * The clickable representation of the board in the GUI. The buttons of {@link #Board} will 
	 * be enabled only when "Play" or "Inspect V" are clicked. During "Play" and "Inspect V"  
	 * only allowed actions are enabled. The value function of each allowed action 
	 * is displayed below the action button.
	 */
	protected JLabel[] Heap;  
	protected JButton[][] Board;	// the clickable actions
	protected JLabel[][] VBoard;	// the values for each action as estimated by the agent to move
	protected JLabel[][] OptBoard;	// the optimal values for each action (Bouton's theory)
	private double[][] VTable;
	private double[][] OptTable;

	/**
	 * a reference to the 'parent' {@link GameBoardNim2P} object
	 */
	private GameBoardNimBase m_gb=null;
	private int iBest,jBest;
	private double vWorst;
	
	/**
	 * the last row in the 'Show Values' bar is calculated with the help of this agent
	 * (Bouton for 2 players, MaxN for 3 players)
	 */
	private AgentBase optimAgent;
	
	// the colors of the TH Koeln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);
	
	public GameBoardNimGui(GameBoardNimBase gb) {
		super(gb.getArena().getGameName());	// "Nim" or "Nim3P"
		m_gb = gb;
		switch (m_gb.getStateObs().getNumPlayers()) {
		case 2:
			optimAgent = new BoutonAgent("Bouton"); break;
		default:	// i.e. N==3
			int maxDepth=NimConfig.NUMBER_HEAPS*NimConfig.HEAP_SIZE; // maxDepth = max number of possible moves
			optimAgent = new MaxNAgent("MaxN",maxDepth,true);		
		}
		
		initGui("");
//		clearBoard(true,true);
	}
	
    private void initGui(String title) 
	{
		Heap		= new JLabel[NimConfig.NUMBER_HEAPS];
		Board       = new JButton[NimConfig.NUMBER_HEAPS][NimConfig.MAX_MINUS];
		VBoard      = new JLabel[NimConfig.NUMBER_HEAPS][NimConfig.MAX_MINUS];
		OptBoard    = new JLabel[NimConfig.NUMBER_HEAPS][NimConfig.MAX_MINUS];
		VTable		= new double[NimConfig.NUMBER_HEAPS][NimConfig.MAX_MINUS];
		OptTable		= new double[NimConfig.NUMBER_HEAPS][NimConfig.MAX_MINUS];
		BoardPanel	= InitBoard();

		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank=new JLabel(" ");		// a little bit of space
		titlePanel.add(Blank);
		
		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		boardPanel.add(BoardPanel);
		boardPanel.setBackground(Types.GUI_BGCOLOR);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.setBackground(Types.GUI_BGCOLOR);
		Font font=new Font("Arial",0,(int)(1.2*Types.GUI_HELPFONTSIZE));			
		leftInfo.setFont(font);	
		rightInfo.setFont(font);	
		infoPanel.add(leftInfo);
		infoPanel.add(rightInfo);
		
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
		panel.setLayout(new GridLayout(3,(int)(NimConfig.NUMBER_HEAPS/3)+1,15,15));
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
			Heap[i].setText(m_gb.getHeaps()[i]+"");
			Heap[i].setHorizontalAlignment(SwingConstants.CENTER);
			hPanel.add(Heap[i],BorderLayout.CENTER);
			JPanel bPanel=new JPanel();
			bPanel.setLayout(new GridLayout(3,NimConfig.MAX_MINUS,2,2));
			for(int j=0;j<NimConfig.MAX_MINUS;j++){
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
								Arena.Task aTaskState = m_gb.getArena().taskState;
								if (aTaskState == Arena.Task.PLAY)
								{
									m_gb.HGameMove(x,y);	// i.e. make human move (i,j), if Board[i][j] is clicked								
								}
								if (aTaskState == Arena.Task.INSPECTV)
								{
									m_gb.InspectMove(x,y);	// i.e. update inspection, if Board[i][j] is clicked								
								}	
								int dummy=1;
							}
						}
				);
				bPanel.add(Board[i][j]);
			} // for(j)
			for(int j=0;j<NimConfig.MAX_MINUS;j++){
				VBoard[i][j] = new JLabel("  ");
				VBoard[i][j].setForeground(colTHK1);
//				VBoard[i][j].setBackground(Color.DARK_GRAY);	// has no effect - unclear why
				VBoard[i][j].setHorizontalAlignment(SwingConstants.CENTER);
//				VBoard[i][j].setMargin(new Insets(0,0,0,0));  	// sets zero margin between the button's
//															 	// border and the label (so that there 
//															 	// is room for big labels)
				VBoard[i][j].setFont(font);
				VBoard[i][j].setEnabled(true);
				VBoard[i][j].setPreferredSize(minimumSize); 
				bPanel.add(VBoard[i][j]);
			} // for(j)
			for(int j=0;j<NimConfig.MAX_MINUS;j++){
				OptBoard[i][j] = new JLabel("  ");
				OptBoard[i][j].setForeground(colTHK1);
				OptBoard[i][j].setHorizontalAlignment(SwingConstants.CENTER);
				OptBoard[i][j].setFont(font);
				OptBoard[i][j].setEnabled(true);
				OptBoard[i][j].setPreferredSize(minimumSize); 
				bPanel.add(OptBoard[i][j]);
			} // for(j)
			hPanel.add(bPanel,BorderLayout.SOUTH);
			panel.add(hPanel);

		}
		return panel;
	}
	
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
				Heap[i].setText(m_gb.getHeaps()[i]+"");
				for(int j=0;j<NimConfig.MAX_MINUS;j++){
			        Board[i][j].setText((j+1)+"");
					Board[i][j].setBackground(colTHK2);
					Board[i][j].setForeground(Color.white);
					Board[i][j].setEnabled(false);
				}
			}
			ArrayList<Types.ACTIONS> acts = m_gb.getStateObs().getAvailableActions();
			for (int k=0; k<acts.size(); k++) {
				int iAction = acts.get(k).toInt();
				int j=iAction%NimConfig.MAX_MINUS;
				int i=(iAction-j)/NimConfig.MAX_MINUS;		
				Board[i][j].setEnabled(true);
			}
				
		}
		if (vClear) {
			for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
				for(int j=0;j<NimConfig.MAX_MINUS;j++){
					VTable[i][j] = Double.NaN;
					OptTable[i][j] = Double.NaN;
				}
			}
		}
	}

	/**
	 * Update the play board and the associated values (labels) to the new state {@code soN}.
	 * 
	 * @param soN	the game state. If {@code null}, call only {@link #guiUpdateBoard(boolean)}.
	 * @param withReset  if true, reset the board prior to updating it to state {@code so}
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverNim soN, 
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;
		boolean isTaskPlay = (m_gb.getArena().taskState == Arena.Task.PLAY);
		boolean isTaskInspectV = (m_gb.getArena().taskState == Arena.Task.INSPECTV);
		if (soN!=null) {
			
			setInfoText(soN, isTaskInspectV);
			
			if (showValueOnGameboard && soN.getStoredValues()!=null) {
				for(i=0;i<NimConfig.NUMBER_HEAPS;i++)
					for(j=0;j<NimConfig.MAX_MINUS;j++) {
						VTable[i][j]=Double.NaN;	
						OptTable[i][j]=Double.NaN;	
					}
				
				if (soN.isGameOver()) {
					rightInfo.setText("");
				} else {
					// optState is the state for which we want to calculate the optimal values (based on Bouton
					// or MaxN agent) which are shown in the last row of the GUI
					StateObserverNim optState = (soN instanceof StateObserverNim3P) ?
							new StateObserverNim3P((StateObserverNim3P)soN) : new StateObserverNim(soN);
					int[] heaps = soN.getHeaps().clone();
					if (isTaskPlay) {
						// if called from 'Play', then reverse the action actBest in heaps
						// (because we want to store in OptTable the optimal values
						// *before* actBest was taken)
						ACTIONS actBest = soN.getStoredActBest();
						int iAction = actBest.toInt();
						j=iAction%NimConfig.MAX_MINUS;		// j+1: number of items taken
						i=(iAction-j)/NimConfig.MAX_MINUS;	// i  : heap number	
						heaps[i] += (j+1);
						int N = soN.getNumPlayers();
						int prevPlayer = (soN.getPlayer()-1+N)%N;
						optState = (soN instanceof StateObserverNim3P) ?
								new StateObserverNim3P(heaps,prevPlayer) : new StateObserverNim(heaps,prevPlayer);
					}
					
					// Calculate the optimal value of this action according 
					// to Bouton's theory (N=2) or to MaxN. The values OptTable[i][j] are shown 
					// in GUI (last row) if showValueOnGameboard is true.
					Types.ACTIONS_VT optActions = optimAgent.getNextAction2(optState.partialState(), false, true);
			        ArrayList<ACTIONS> acts = optState.getAvailableActions();
					for (int k=0; k<acts.size(); k++) {
						int iAction = acts.get(k).toInt();
						j=iAction%NimConfig.MAX_MINUS;		// j+1: number of items to take
						i=(iAction-j)/NimConfig.MAX_MINUS;	// i  : heap number		
						OptTable[i][j] = optActions.getVTable()[k];
					}

					// Show the stored VTable values in the second-last row of the GUI 
					// if showValueOnGameboard is true.
					vWorst = Double.MAX_VALUE;
					for (int k=0; k<soN.getStoredValues().length; k++) {
						Types.ACTIONS action = soN.getStoredAction(k);
						int iAction = action.toInt();
						j=iAction%NimConfig.MAX_MINUS;		// j+1: number of items to take
						i=(iAction-j)/NimConfig.MAX_MINUS;	// i  : heap number		
						VTable[i][j] = soN.getStoredValues()[k];
						if (action.equals(soN.getStoredActBest())) {
							iBest=i;
							jBest=j; 
						}
						if (VTable[i][j]<vWorst) vWorst=VTable[i][j];						
					}	
					
				}
			} 
			if (!showValueOnGameboard)
				rightInfo.setText("");
				
		} // if(so!=null)
		
		guiUpdateBoard(showValueOnGameboard);
	}

	/**
	 * helper for {@link #updateBoard(StateObserverNim, boolean, boolean)}
	 */
	private void setInfoText(StateObserverNim soN, boolean isTaskInspectV) {
		String[] playerNames2P = {"X","O","-"};
		String[] playerNames3P = {"P0","P1","P2"};
		String[] playerNames = (m_gb instanceof GameBoardNim2P) ? playerNames2P : playerNames3P; 
		String actualPlayer = playerNames[m_gb.getStateObs().getPlayer()];
		leftInfo.setText(actualPlayer+" to move   ");
		if (soN.isGameOver()) {
			ScoreTuple sc = soN.getGameScoreTuple();
			int winner = sc.argmax();
			if (sc.max()==0.0) winner = -2;	// tie indicator (should not happen for Nim & Nim3P)
			switch(winner) {
			case( 0): 
			case( 1):
			case( 2):
				leftInfo.setText(playerNames[winner]+" has won   "); break;
			case(-2):
				//leftInfo.setText("Tie         "); break;
				throw new RuntimeException("No tie for Nim!");
			}
			rightInfo.setText("");				
		} else {
			
			String s_0,s_1,s_2;
			if (m_gb instanceof GameBoardNim2P) {
				s_0 = isTaskInspectV ? "X" : "O";
				s_1 = isTaskInspectV ? "O" : "X";
				s_2 = "";
			} else {
				s_0 = isTaskInspectV ? "P0" : "P2";
				s_1 = isTaskInspectV ? "P1" : "P0";
				s_2 = isTaskInspectV ? "P2" : "P1";	
			}
			switch(m_gb.getStateObs().getPlayer()) {
			case(0): 
				rightInfo.setText("    Score for " + s_0); break;
			case(1):
				rightInfo.setText("    Score for " + s_1); break;
			case(2):
				rightInfo.setText("    Score for " + s_2); break;
			}					
		} // else
	}
	
	/**
	 * Update the play board and the associated values (labels) to the state in m_so.
	 * The labels contain the values (scores) for each unoccupied board position (raw score*100).  
	 * Occupied board positions have black/white background color, unoccupied positions are orange.
	 * 
	 * As a side effect the buttons Board[i][j] which are non-viable actions
	 * will be set into enabled state <code>false</code>. (All other positions will get state 
	 * <code>true</code>.)
	 */ 
	public void guiUpdateBoard(boolean showValueOnGameboard)
	{		
		double value, maxvalue=Double.NEGATIVE_INFINITY;
		int imax=0,jmax=0;
		for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
			Heap[i].setText(m_gb.getHeaps()[i]+"");
		}
		for(int i=0;i<NimConfig.NUMBER_HEAPS;i++){
			for(int j=0;j<NimConfig.MAX_MINUS;j++){
				Board[i][j].setEnabled(false);
				OptBoard[i][j].setText("");

				setValueBoard(VBoard,i,j,VTable[i][j],showValueOnGameboard);
				setValueBoard(OptBoard,i,j,OptTable[i][j],showValueOnGameboard);
				//VBoard[i][j].setBackground(Color.DARK_GRAY); // has no effect - unclear why
			}
		}
		
		if (VTable[iBest][jBest]!=vWorst) {
			// mark the best action in green (but only if not all actions are equal to the worst)
			setValueBoard(VBoard,iBest,jBest,VTable[iBest][jBest],
					showValueOnGameboard,new Color(0,(int)(255/1.5),0));
		}
		
		// for all viable actions: enable the associated action button
		for (ACTIONS action : m_gb.getStateObs().getAvailableActions()) {
			int iAction = action.toInt();
			int j=iAction%NimConfig.MAX_MINUS;		// j+1: number of items to take
			int i=(iAction-j)/NimConfig.MAX_MINUS;	// i  : heap number	
			Board[i][j].setEnabled(true);
		}
		
		this.repaint();
	}		

	private void setValueBoard(JLabel[][] XBoard, int i, int j, double value, 
			boolean showValueOnGameboard) {
		setValueBoard(XBoard,i,j,value,showValueOnGameboard,null);
	}
	/**
	 * Set the values in the JLabel array XBoard.
	 * @param XBoard	either VBoard or OptBoard
	 * @param i
	 * @param j
	 * @param value
	 * @param showValueOnGameboard
	 * @param color  the foreground color. If null, use {@link #calculateXBoardColor(double)}.
	 */
	private void setValueBoard(JLabel[][] XBoard, int i, int j, double value, 
					boolean showValueOnGameboard, Color color) {
		String valueTxt;
		Color col;
								// delete written values if coming from InspectV and showValueOnGameboard is false
		if (Double.isNaN(value) || !showValueOnGameboard) {
			valueTxt = "   ";
		} else {
			valueTxt = " "+Math.round(value*100);
			if (value<0) valueTxt = ""+Math.round(value*100);
			col = (color==null) ? calculateXBoardColor(value) : color;
			XBoard[i][j].setForeground(col);
		}
//		if (showValueOnGameboard) {
			XBoard[i][j].setText(valueTxt);
//		} 

	}
	
    /**
     * Calculate the color for a specific XBoard value (either VBoard or OptBoard).
     * Uses three color stops: Red for value -1, Yellow for value 0, Green for value +1.
     * Colors for values between -1 and 0 are interpolated between red and yellow.
     * Colors for values between 0 and +1 are interpolated between yellow and green.
     *
     * @param xValue Value of the specific VBoard element
     * @return the color that the XBoard element is supposed to have
     */
    public static Color calculateXBoardColor(double xValue) {
        float percentage = (float) Math.abs(xValue);
        float inverse_percentage = 1 - percentage;

        Color colorLow;
        Color colorHigh;
        Color colorNeutral = Color.YELLOW;
        int red, blue, green;

        if (xValue < 0) {
            colorLow = colorNeutral;
            colorHigh = Color.RED;
        } else {
            colorLow = colorNeutral;
            colorHigh = Color.GREEN;
        }

        red = Math.min(Math.max(Math.round(colorLow.getRed() * inverse_percentage
                + colorHigh.getRed() * percentage), 0), 255);
        blue = Math.min(Math.max(Math.round(colorLow.getBlue() * inverse_percentage
                + colorHigh.getBlue() * percentage), 0), 255);
        green = Math.min(Math.max(Math.round(colorLow.getGreen() * inverse_percentage
                + colorHigh.getGreen() * percentage), 0), 255);

        double fac=1.5;	// a factor to make the foreground color a bit darker, so that numbers are 
        				// better readable on the light gray background
        return new Color((int)(red/fac), (int)(green/fac), (int)(blue/fac), 255);
    }

	public void enableInteraction(boolean enable) {  	}

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
	
	public void showGameBoard(Arena nimGame, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = nimGame.m_xab.getX() + nimGame.m_xab.getWidth() + 8;
			int y = nimGame.m_xab.getLocation().y;
			if (nimGame.m_ArenaFrame!=null) {
				x = nimGame.m_ArenaFrame.getX();
				y = nimGame.m_ArenaFrame.getY() + nimGame.m_ArenaFrame.getHeight() +1;
				int aframeWidth = nimGame.m_ArenaFrame.getWidth();
				int nimWidth = 32 * NimConfig.HEAP_SIZE*NimConfig.NUMBER_HEAPS;		// sufficiently wide to cover all butttons
				int width = (aframeWidth>nimWidth) ? aframeWidth : nimWidth;
				this.setSize(width, (int)(Types.GUI_SCALING_FACTOR_Y*TICGAMEHEIGHT));	
			}
			this.setLocation(x,y);	
		}		
//		System.out.println("GameBoardNim size = " +super.getWidth() + " * " + super.getHeight());
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
