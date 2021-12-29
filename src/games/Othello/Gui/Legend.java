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
		setLayout(new FlowLayout(FlowLayout.CENTER,(int) (Types.GUI_SCALING_FACTOR_Y * 5),
				(int) (Types.GUI_SCALING_FACTOR_Y * 7)));
		setBackground(Types.GUI_BGCOLOR);
		//text for the legend
		textRedMark = createLabel("last Move: ");
		textGreenMark = createLabel(" possible Moves: ");
		// red mark
		redMark = new JLabel("   ");
		redMark.setBorder(BorderFactory.createLineBorder(ConfigOthello.LASTMOVECOLOR, 2));
		JLabel hgap =new JLabel("  ");
		//green mark
		greenMark = new JLabel("   ");
		greenMark.setBorder(BorderFactory.createLineBorder(ConfigOthello.POSSIBLEMOVECOLOR, 2));
		add(textRedMark);
		add(redMark);
		add(hgap);
		add(textGreenMark);
		add(greenMark);
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