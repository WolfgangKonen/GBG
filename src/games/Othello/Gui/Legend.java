package games.Othello.Gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BorderFactory;
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
		redMark = new JLabel("   ");
		redMark.setBorder(BorderFactory.createLineBorder(ConfigOthello.LASTMOVECOLOR, 2));
		//green mark
		greenMark = new JLabel("   ");
		greenMark.setBorder(BorderFactory.createLineBorder(ConfigOthello.POSSIBLEMOVECOLOR, 2));
		add(textRedMark);
		add(redMark);
		add(textGreenMark);
		add(greenMark);
	}
}