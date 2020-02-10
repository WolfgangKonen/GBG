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
import tools.ScoreTuple;
import tools.Types;

/**
 * Class GameBoardC4 implements interface GameBoard for Connect Four.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Koeln, May'18
 *
 */
public class GameBoardC4 implements GameBoard {

	protected Arena  m_Arena;		// a reference to the Arena object, needed to 
									// infer the current taskState
	protected Random rand;
	private GameBoardC4Gui m_gameFrame;
	private StateObserverC4 m_so;
	private double[] VTable;
	private boolean arenaActReq=false;
	
	public GameBoardC4(Arena arGame) {
		initGameBoard(arGame);
	}
	
    @Override
    public void initialize() {}

    private void initGameBoard(Arena arGame) 
	{
		m_Arena		= arGame;
		VTable		= new double[C4Base.COLCOUNT];
		m_so		= new StateObserverC4();	// empty table
        rand 		= new Random(System.currentTimeMillis());	
        if (m_Arena.withUI) {
        	m_gameFrame = new GameBoardC4Gui(this);
        }

	}

    // --- obsolete? ---
//	private JPanel InitBoard()
//	{
//		JPanel panel=new JPanel();
//		//JButton b = new JButton();
//		panel.setLayout(new GridLayout(3,3,2,2));
////		int buSize = (int)(50*Types.GUI_SCALING_FACTORX);
////		Dimension minimumSize = new Dimension(buSize,buSize); //controls the button sizes
//		return panel;
//	}
	
	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			m_so = new StateObserverC4();			// empty Table
		}
		if (vClear) {
			//VTable		= new double[C4Base.COLCOUNT];
			for(int i=0;i<C4Base.COLCOUNT;i++){
					VTable[i] = Double.NaN;
			}
		}
		if (m_gameFrame!=null)
			m_gameFrame.clearBoard(boardClear, vClear, m_so, VTable);
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
		StateObserverC4 soT = null;
		
		if (so!=null) {
	        assert (so instanceof StateObserverC4)
			: "StateObservation 'so' is not an instance of StateObserverC4";
			soT = (StateObserverC4) so;
			m_so = soT.copy();
			
			if (showValueOnGameboard && soT.getStoredValues()!=null) {
				for(i=0;i<C4Base.COLCOUNT;i++){
					VTable[i] = Double.NaN;
				}
				
				for (int k=0; k<soT.getStoredValues().length; k++) {
					Types.ACTIONS action = soT.getStoredAction(k);
					int iAction = action.toInt();
					VTable[iAction] = soT.getStoredValues()[k];					
				}	
			} 
		} // if(so!=null)
		
		if (m_gameFrame!=null)
			m_gameFrame.guiUpdateBoard(soT, VTable, m_Arena.taskState, withReset,showValueOnGameboard);
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

	// --- obsolete? ---
//	/**
//	 * This class is needed for each ActionListener of {@code Board[i][j]} in 
//	 * {@link #InitBoard()}
//	 *
//	 */
//	class ActionHandler implements ActionListener
//	{
//		int x,y;
//		
//		ActionHandler(int num1,int num2)			
//		{		
//			x=num1;
//			y=num2;
//		}
//		public void actionPerformed(ActionEvent e){}			
//	}
	
	protected void HGameMove(int x, int y)
	{
		int iAction = x;
		Types.ACTIONS act = Types.ACTIONS.fromInt(iAction);
//		assert m_so.isLegalAction(act) : "Desired action is not legal";
		if (m_so.isLegalAction(act)) {
			m_so.advance(act);			// perform action (optionally add random elements from game 
										// environment - not necessary in ConnectFour)
			System.out.println(m_so.stringDescr());
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
	public void enableInteraction(boolean enable) {
		if (m_gameFrame!=null)
			m_gameFrame.enableInteraction(enable);
	}

	@Override
	public void showGameBoard(Arena ticGame, boolean alignToMain) {
		if (m_gameFrame!=null)
			m_gameFrame.showGameBoard(ticGame, alignToMain);
	}

   @Override
	public void toFront() {
		if (m_gameFrame!=null)
			m_gameFrame.toFront();
	}

   @Override
   public void destroy() {
		if (m_gameFrame!=null)
			m_gameFrame.destroy();
   }

}
