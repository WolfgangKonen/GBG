package games.Othello.Gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

import games.Othello.ConfigOthello;
import tools.Types;

/**
 * Displaying the legend of the game board in the southern Frame.
 */
public class Legend extends JPanel {

	private JLabel redMark, greenMark, textRedMark, textGreenMark;
	
	public Legend() {
		setLayout(new FlowLayout(FlowLayout.CENTER,(int) Types.GUI_SCALING_FACTOR_Y * 20,
				(int) Types.GUI_SCALING_FACTOR_Y * 10));
		setBackground(Types.GUI_BGCOLOR);
		//text for the legend
		textRedMark = new JLabel("last Move: ");
		textGreenMark = new JLabel("possible Moves: ");
		// red mark
		redMark = new JLabel("  ");
		redMark.setBackground(ConfigOthello.LASTMOVECOLOR);
		redMark.setOpaque(true);
		//green mark
		greenMark = new JLabel("  ");
		greenMark.setBackground(ConfigOthello.POSSIBLEMOVECOLOR);
		greenMark.setOpaque(true);
		add(textRedMark);
		add(redMark);
		add(textGreenMark);
		add(greenMark);
	}
}
