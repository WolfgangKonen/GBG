package games.Sim;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import controllers.PlayAgent;
import games.GameBoard;
import games.StateObservation;
import games.Hex.StateObserverHex;
import games.Othello.GameBoardOthello;
import games.Sim.Point;
import games.TicTacToe.StateObserverTTT;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import games.Arena;

/**
 * Class GameBoardSimGui has the board game GUI. 
 * <p>
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Percy Wuensch, Wolfgang Konen, TH Koeln, 2019-2020
 */
public class GameBoardSimGui {

	/**
	 * SerialNumber
	 */
	private static final long serialVersionUID = 12L;

	/**
	 * a reference to the 'parent' {@link GameBoardSim} object
	 */
	private GameBoardSim m_gb=null;
	
	GameBoardSimGui m_gbsg;
	JFrame frame;
	BoardPanel board;
	
	public GameBoardSimGui(GameBoardSim gb)
	{
		m_gb = gb;
		m_gbsg = this;
		
		setupGUI();
	}
	
	private void setupGUI()
	{
		frame = new JFrame("Sim");
		frame.setSize(600, 400);
		frame.setLocation(550, 0);
		 
		board = new BoardPanel(m_gb.m_so.getNodes(),m_gb);
		board.addMouseListener(new Mouse());
		frame.add(board);	           
	}
	
	
	public void clearBoard(boolean boardClear, boolean vClear) {
		board.clearLastNodes();
	    frame.repaint();
	}

	public void updateBoard(StateObserverSim soS, boolean withReset, boolean showValueOnGameboard) {
		if (soS!=null) {
			// not needed anymore:
//	        assert (so instanceof StateObserverSim)
//			: "StateObservation 'so' is not an instance of StateObserverSim";
//			StateObserverSim soS = (StateObserverSim) so;
			
			board.setShowValueOnGameBoard(showValueOnGameboard);
			
			board.setNodesCopy(m_gb.m_so.getNodes());		// we could use soS instead of m_gb.m_so
			
			if (soS.hasLost(soS.getCreatingPlayer())) {
				board.markLosingTriangle(soS.getLastNodes());
			}
			
			// not needed anymore:
//			if(soS.getStoredValues() != null && showValueOnGameboard)
//			{				
//				//for(int i = 0; i < som.getStoredValues().length; i++)
//					//System.out.println(som.getStoredValues()[i]);
//				board.setAvailableActions(soS.getAvailableActions());
//				board.setActionValues(soS.getStoredValues());
//			}
//			if(!showValueOnGameboard)
//			{
//				board.setActionValues(null);
//			}			
		}		
		frame.repaint();
	}

	public void enableInteraction(boolean enable) {  	}

	public void showGameBoard(Arena simGame, boolean alignToMain) {
		frame.setVisible(true);
	}

	public void toFront() {
		frame.setState(Frame.NORMAL);	// if window is iconified, display it normally
		board.toFront();
		//input.toFront();
	}
	
    public void destroy() {
		frame.setVisible(false);
		frame.dispose();
    }


	public class Mouse implements MouseListener
	{
//		Point[] circles;
		int node;
		
		public Mouse()
		{
			node = 0;
//			setupCircles(ConfigSim.NUM_NODES);	// bug fix WK: this had a 6 instead of NUM_NODES before
		}

		// use instead board.circles and use board.isInsideCircles() in mouseClicked().
//		private void setupCircles(int size)
//		{
//			int CENTERX = 275;
//			int CENTERY = 115;
//			int radius = 150;
//			int degree = 360 / size;
//			circles = new Point[size];
//			
//			for(int i = 0; i < size; i++)
//				circles[i] = new Point(calculateCirclePositionX(radius, degree * i, CENTERX, CENTERY),
//						   			   calculateCirclePositionY(radius, degree * i, CENTERX, CENTERY));			
//		}
		
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
			
			for(int i = 0; i < m_gb.m_so.getNodesLength(); i++)
			{
				if(board.isInsideCircle(i, x, y))
				{
					setInput(i);
				}
			}
		}
		
		/**
		 * Mark a link by clicking at its two nodes: 
		 * <ul>
		 * <li> If {@code node}==0, the click is to the first node and {@code node} is set to i+1
		 * <li> If {@code node}==i+1, this is a resetting click: {@code node} is reset to 0
		 * <li> Else, the link (and its associated action) is set from the two clicked nodes.
		 * </ul>
		 * @param i	the circle index \in [0,...,K-1] where K is the number of nodes
		 */
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
//				board.setInputNode2(i);
			}
			
			board.setNodesCopy(m_gb.m_so.getNodes());		// copy the new action link
			frame.repaint();
		}
		
		private void setAction(int i)
		{
			Types.ACTIONS act = Types.ACTIONS.fromInt(m_gb.m_so.inputToActionInt(node, i+1));
			if(m_gb.m_so.isLegalAction(act))
			{
				m_gb.m_so.advance(act);
				
				m_gb.setActionReq(true);
				node = 0;
			}
			else
			{
				System.out.println("action is not legal!");
				node = 0;
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {	}

		@Override
		public void mouseReleased(MouseEvent e) { 	}

		@Override
		public void mouseEntered(MouseEvent e) {	}

		@Override
		public void mouseExited(MouseEvent e) {		}
		
	} // class Mouse

}
