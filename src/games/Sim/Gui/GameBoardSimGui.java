package games.Sim.Gui;

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
import games.Sim.GameBoardSim;
import games.Sim.StateObserverSim;
import games.Sim.Gui.GameStatsSim;
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
	private GameStatsSim gameStats; 	// Displaying Game information
	private GameInfoSim gameInfo;
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
		frame.setSize(600, 440);
		frame.setLocation(550, 0);
		 
		gameStats = new GameStatsSim();
		board = new BoardPanel(m_gb);
		board.addMouseListener(new Mouse());
		gameInfo = new GameInfoSim();
		frame.add(gameStats, BorderLayout.NORTH);
		frame.add(board, BorderLayout.CENTER);	      
		frame.add(gameInfo, BorderLayout.SOUTH);	
	}
	
	
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) board.clearLastNodes();
		// Why 'if (boardClear)'? - When a game is over in INSPECTV, then we call clearBoard(false,true) in order
		// to clear the value display. But we do not want to clear lastNodes, because we want that frame.repaint
		// marks the losing triangle.
		
	    frame.repaint();
	}

	public void updateBoard(StateObserverSim soS, boolean withReset, boolean showValueOnGameboard) {
		if (soS!=null) {
			
			board.setShowValueOnGameBoard(showValueOnGameboard);
			
//			board.setNodesCopy(m_gb.m_so.getNodes());		// we could use soS instead of m_gb.m_so
			
			if (soS.hasLost(soS.getCreatingPlayer())) {
				board.markLosingTriangle(soS.getLastNodes());
			}
			
			gameStats.changeNextMove(soS);
			gameStats.setTurnCount(soS.getMoveCounter());
			
			if (soS.isGameOver()) {
				ScoreTuple sc = soS.getGameScoreTuple();
				int winner = sc.argmax();
				if (sc.max()==0.0)
					gameInfo.changeMessage("Tie");
				else
					gameInfo.changeMessage(Types.GUI_PLAYER_COLOR_NAME[winner]  + " has won");
				
			}
		}		
		frame.repaint();
	}

	public void enableInteraction(boolean enable) {  
		board.setEnabled(enable);
	}

	public void showGameBoard(Arena simGame, boolean alignToMain) {
		frame.setVisible(true);
	}

	public void toFront() {
		frame.setState(Frame.NORMAL);	// if window is iconified, display it normally
		board.toFront();
	}
	
    public void destroy() {
		frame.setVisible(false);
		frame.dispose();
    }


	public class Mouse implements MouseListener
	{
		int node;
		
		public Mouse()
		{
			node = -1;
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
			if (!m_gb.isEnabled) return;

			int x = e.getX();
			int y = e.getY();
			
			for(int i = 0; i < m_gb.m_so.getNumNodes(); i++)
				if(board.isInsideCircle(i, x, y))
					setInput(i);
		}
		
		/**
		 * Mark a link by clicking at its two nodes: 
		 * <ul>
		 * <li> If {@code node}==-1, the click is to the first node and {@code node} is set to i
		 * <li> If {@code node}==i, this is a resetting click: {@code node} is reset to -1
		 * <li> Else, the link (and its associated action) is set from the two clicked nodes.
		 * </ul>
		 * @param i	the circle index \in [0,...,K-1] where K is the number of nodes
		 */
		private void setInput(int i)
		{
			if(node == -1)
			{
				node = i;
				board.setInputNode1(i);
				board.setInputNode2(-1);
			}
			else if(node == i)
			{
				node = -1;
				board.setInputNode1(-1);
				frame.repaint();
				return;
			}
			else
			{
				setAction(i);
//				board.setInputNode2(i);
			}
			
//			board.setNodesCopy(m_gb.m_so.getNodes());		// copy the new action link
			frame.repaint();
		}
		
		private void setAction(int i)
		{
			Types.ACTIONS act = Types.ACTIONS.fromInt(m_gb.m_so.inputToActionInt(node, i));
			if(m_gb.m_so.isLegalAction(act))
			{
				m_gb.m_so.advance(act);
				
				m_gb.setActionReq(true);
				gameInfo.changeMessage("");
				node = -1;
			}
			else
			{
				//System.out.println("action is not legal!");
				gameInfo.changeMessage("Not a legal move!");
				node = -1;
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
