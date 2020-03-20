package games.Sim.Gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import games.Sim.StateObserverSim;
import tools.Types;

/**
 * Display the game status based on the current game state.
 * The Player which has to move next {@code #nextMove} and the 
 * information about the turn {@code #turnCount}.
 */
public class GameStatsSim extends JPanel{

	private JLabel nextMove, turnCount;
	
	public GameStatsSim() {
		setLayout(new FlowLayout(FlowLayout.CENTER,(int) (Types.GUI_SCALING_FACTOR_X * 20),(int) (Types.GUI_SCALING_FACTOR_Y * 10)));
				   // FlowLayout(aligh,hgap,vgap), where the gaps are between components and between components and border
		setBackground(Types.GUI_BGCOLOR);
		turnCount = createLabel("Turn: 0");
		nextMove = createLabel("Next move: " + Types.GUI_PLAYER_COLOR_NAME[0]);
		this.add(turnCount);
		this.add(nextMove);
	}
	
	private JLabel createLabel(String str) {
		JLabel lbl = new JLabel(str);
		//lbl.setFont(new Font("Arial",Font.CENTER_BASELINE,Types.GUI_DIALOGFONTSIZE));
		lbl.setFont(new Font("Arial",Font.BOLD,(int)(1.5*Types.GUI_DIALOGFONTSIZE)));
		return lbl;
	}	
	
	public void changeNextMove(StateObserverSim so) {
		String str =  "Next move: " + Types.GUI_PLAYER_COLOR_NAME[so.getPlayer()];
		nextMove.setText(str); 
	}
	
	public void setTurnCount(int turnCount) {
		this.turnCount.setText("Turn: " + turnCount);
	}
	
}
