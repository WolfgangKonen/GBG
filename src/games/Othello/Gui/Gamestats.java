package games.Othello.Gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.prism.paint.Color;

import games.Othello.GameBoardOthello;
import games.Othello.StateObserverOthello;
import javafx.scene.layout.Border;
import tools.Types;



/**
 * Displaying the game information based on the current game state.
 * Amount of discs each player owns for Black {@code #blackCount}
 * for white {@code #whiteCount}. The Player which has to place the 
 * next disc {@code #nextMove} and the information about the turn
 * {@code #turnCount}. Remind the maximal turnCount is 60, which is
 * the last possible move. After each field has been occupied by a player.
 */
public class Gamestats extends JPanel{

	private int turn;
	
	private JLabel nextMove, whiteCount, blackCount, turnCount;
	
	public Gamestats() {
		nextMove = createLabel("Next move: Black");
		whiteCount = createLabel("White: 2");
		blackCount = createLabel("Black: 2");
		turnCount = createLabel("Turn: 1");
		setLayout(new FlowLayout(FlowLayout.CENTER,(int) Types.GUI_SCALING_FACTOR_Y * 20,
				(int) Types.GUI_SCALING_FACTOR_Y * 10));
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
	 * @return
	 */
	private JLabel createLabel(String str) {
		JLabel dummy = new JLabel(str);
		dummy.setFont(new Font("Arial",Font.CENTER_BASELINE,Types.GUI_DIALOGFONTSIZE));
		return dummy;
	}
	
}
