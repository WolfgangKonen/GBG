package games.Sim;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
		frame.setSize(600, 400);
		frame.setLocation(550, 0);
		 
		board = new BoardPanel(m_so.getNodes());
		board.addMouseListener(new Mouse());
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
			
			if (so.isGameOver()) 
			{
				
					int winner = som.getGameWinner3player();
					if(winner == -1)
						System.out.println("Tie");
					else
						System.out.println(winner + 1  + " has won");
					
			}
			if(som.getStoredValues() != null )
			{
				System.out.println(som.getStoredValues().length);
				System.out.println(showValueOnGameboard);
			}
			//ShowValue bug, because the updateGameboad Method is called twice.
			if(som.getStoredValues() != null && showValueOnGameboard)
			{
				
				//for(int i = 0; i < som.getStoredValues().length; i++)
					//System.out.println(som.getStoredValues()[i]);
				board.setActionValues(som.getStoredValues());
			}
			else
			{
				System.out.println("else");
				//board.setActionValues(null);
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
		//input.toFront();
	}

	public class Mouse implements MouseListener
	{
		Point[] circles;
		int node;
		
		public Mouse()
		{
			node = 0;
			setupCircles(6);
		}

		private void setupCircles(int size)
		{
			int radius = 150;
			int degree = 360 / size;
			circles = new Point[size];
			
			for(int i = 0; i < size; i++)
				circles[i] = new Point(calculateCirclePositionX(radius, degree * i, 280, 100),calculateCirclePositionY(radius, degree * i, 280, 100));
			
		}
		
		private int calculateCirclePositionX(int radius, int degree, int posX, int posY)
		{
			double t = Math.toRadians((double) degree);
			
			return (int)(posX + radius * Math.cos(t));
		}
		
		private int calculateCirclePositionY(int radius, int degree, int posX, int posY)
		{
			double t = Math.toRadians((double) degree);
			
			return (int)((posX + radius * Math.sin(t)) - posY);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) 
		{
			int x = e.getX();
			int y = e.getY();
			
			for(int i = 0; i < m_so.getNodesLength(); i++)
			{
				if(x  > circles[i].getX() && x < circles[i].getX() + 30 && y > circles[i].getY() && y < circles[i].getY() + 30)
				{
					setInput(i);
				}
			}
		}
		
		private void setInput(int i)
		{
			if(node == 0)
			{
				node = i + 1;
				board.setInputNode1(i);
				board.setInputNode2(-1);
			}
			else if(node == i + 1)
			{
				node = 0;
				board.setInputNode1(-1);
				frame.repaint();
				return;
			}
			else
			{
				setAction(i);
				board.setInputNode2(i);
			}
			
			frame.repaint();
		}
		
		private void setAction(int i)
		{
			Types.ACTIONS act = Types.ACTIONS.fromInt(m_so.inputToActionInt(node, i+1));
			if(m_so.isLegalAction(act))
			{
				m_so.advance(act);
				
				arenaActReq = true;
				node = 0;
			}
			else
			{
				System.out.println("action is not legal!");
				node = 0;
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
