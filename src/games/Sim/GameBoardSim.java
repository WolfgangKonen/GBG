package games.Sim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import controllers.PlayAgent;
import games.GameBoard;
import games.StateObservation;
import games.Hex.StateObserverHex;
import games.Sim.Point;
import games.TicTacToe.StateObserverTTT;
import tools.Types;
import games.Arena;

public class GameBoardSim implements GameBoard {

	//Framework
	protected Arena m_arena;
	private StateObserverSim m_so;
	private boolean arenaActReq = false;
	
	//JFrame
	JFrame frame;
	
	//panels
	InputPanel input;
	BoardPanel board;
	
	public GameBoardSim(Arena simGame)
	{
		//Framework
		m_arena = simGame;
		m_so = new StateObserverSim();
		arenaActReq = false;
		
		//GUI
		setupGUI();
	}
	
	private void setupGUI()
	{
		//frame
		frame = new JFrame("Sim");
		frame.setSize(600, 430);
		frame.setLocation(550, 0);
		
		input = new InputPanel();
		frame.add(input);
		
		board = new BoardPanel(m_so.getNodes());
		frame.add(board);
	}
	
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearBoard(boolean boardClear, boolean vClear) {
		 if (boardClear) {
	            m_so = new StateObserverSim();
	            //board.setNodesCopy(m_so.getNodes());
	        } if (vClear) {
	            
	        }
	        frame.repaint();
	}

	@Override
	public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard) {
		if (so!=null) {
	        assert (so instanceof StateObserverSim)
			: "StateObservation 'so' is not an instance of StateObserverSim";
			StateObserverSim som = (StateObserverSim) so;
			m_so = som.copy();
			//m_so.setState(som);
			int player = som.getPlayer();
			switch(player) {
			case(0): 
				System.out.println("1 to move   "); break;
			case(1):
				System.out.println("2 to move   "); break;
			case(2):
				System.out.println("3 to move   "); break;
			}
			
			if (so.isGameOver()) {
				int looser = (som.getNumPlayers() > 3) ? som.getGameWinner3player() : som.getGameWinner().toInt();
				if(looser == -1)
					System.out.println("Tie");
				else
					System.out.println(looser+1  + " has won");
				
			}
		}
		
		board.setNodesCopy(m_so.getNodes());
		frame.repaint();
	}

	@Override
	public void showGameBoard(Arena simGame, boolean alignToMain) {
		frame.setVisible(true);
	}

	@Override
	public boolean isActionReq() 
	{
		return arenaActReq;
	}

	@Override
	public void setActionReq(boolean actionReq) 
	{
		arenaActReq = actionReq;
	}

	@Override
	public void enableInteraction(boolean enable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StateObservation getStateObs() {
		return m_so;
	}

	
	@Override
	public String getSubDir() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Arena getArena() 
	{
		return m_arena;
	}

	@Override
	public StateObservation getDefaultStartState() {
		clearBoard(true, true);
		return m_so;
	}

	@Override
	public StateObservation chooseStartState(PlayAgent pa) {
		return chooseStartState();
	}

	@Override
	public StateObservation chooseStartState() {
		clearBoard(true, true);			// m_so is in default start state 
		return m_so;
	}

	@Override
	public void toFront() {
		frame.setState(Frame.NORMAL);	// if window is iconified, display it normally
		board.toFront();
		input.toFront();
	}
	
	public class InputPanel extends JPanel implements ActionListener {
		
		JLabel inputLabel1, inputLabel2;
		JTextField inputText1, inputText2; 
		JButton okButton;
		
		InputPanel()
		{
			//setup Panel
			this.setLayout(new FlowLayout(FlowLayout.CENTER));
			this.setBounds(0, 0, 600, 30);
			this.setBackground(Color.LIGHT_GRAY);
			
			//setup components
			inputLabel1 = new JLabel("Node 1");
			inputLabel2 = new JLabel("Node 2");
			inputText1 = new JTextField(2);
			inputText2 = new JTextField(2);
			okButton = new JButton("ok");
			okButton.setPreferredSize(new Dimension(40,22));
			okButton.addActionListener(this);
			
			//add components
			this.add(inputLabel1);
			this.add(inputText1);
			this.add(inputLabel2);
			this.add(inputText2);
			this.add(okButton);
		}
		
		public void toFront() {
			super.setVisible(true);
		}

		public void move(int m)
		{
			Types.ACTIONS act = Types.ACTIONS.fromInt(m);
			assert m_so.isLegalAction(act) : "Desired action is not legal";
			m_so.advance(act);			// perform action (optionally add random elements from game 
										// environment - not necessary in TicTacToe)
			arenaActReq = true;	
		}
		
		public void inspectMove(int m)
		{
			Types.ACTIONS act = Types.ACTIONS.fromInt(m);
			if (!m_so.isLegalAction(act)) 
			{
				System.out.println("Desired action is not legal!");
				return;
			} 
			m_so.advance(act);			// perform action (optionally add random elements from game 						// environment - not necessary in TicTacToe)
//			updateBoard(null,false,false);
			arenaActReq = true;		
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() == okButton)
			{
				String text1 = inputText1.getText();
				String text2 = inputText2.getText();
				Arena.Task aTaskState = m_arena.taskState;
				
				if (aTaskState == Arena.Task.PLAY)
				{
					move(m_so.inputToAction(text1, text2));		
				}
				
			}
		}
		
	}


}
