package games.Othello.Gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.Othello.GameBoardOthello;
import games.Othello.StateObserverOthello;
import tools.Types;



/**
 * Displaying the game information based on the current game state.
 * Amount of discs each player owns for Black {@code #blackCount}
 * for white {@code #whiteCount}. The Player which has to place the 
 * next disc {@code #nextMove} and the information about the turn
 * {@code #turnCount}. Note that the maximal turn count is 60, which is
 * the last possible move. 
 */
public class GameStats extends JPanel{

	private int turn;
	
	private JLabel nextMove, whiteCount, blackCount, turnCount;
	
	public GameStats() {
		nextMove = createLabel("Next move: Black");
		whiteCount = createLabel("White: 2");
		blackCount = createLabel("Black: 2");
		turnCount = createLabel("Turn: 0");
		setLayout(new FlowLayout(FlowLayout.CENTER,(int) (Types.GUI_SCALING_FACTOR_X * 15), (int) (Types.GUI_SCALING_FACTOR_Y * 7)));
		   		   // FlowLayout(aligh,hgap,vgap), where the gaps are between components and between components and border
		setBackground(Types.GUI_BGCOLOR);
		this.add(blackCount);
		this.add(whiteCount);
		this.add(turnCount);
		this.add(nextMove);
	}
	
	
	
	public void setWhiteCount(int amountOfWhiteDiscs) {
		this.whiteCount.setText("White: " + amountOfWhiteDiscs);
	}
	
	public void setBlackCount(int amountOfBlackDiscs) {
		this.blackCount.setText("Black: " + amountOfBlackDiscs);
	}
	
	public void changeNextMove(String str) {
		 nextMove.setText(str);
	}
	
	public void setTurnCount(int turnCount) {
		this.turnCount.setText("Turn: " + turnCount);
	}
	
	/**
	 * Used for creating the JLabel objects with same constraints.
	 * @param str first message the label has to hold.
	 * @return a new label
	 */
	private JLabel createLabel(String str) {
		JLabel dummy = new JLabel(str);
		dummy.setFont(new Font("Arial",Font.CENTER_BASELINE,(int)(Types.GUI_DIALOGFONTSIZE*1.7)));
		return dummy;
	}
	
}
