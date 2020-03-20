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
 * Display an optional info message (e.g. "Not a legal move!").
 */
public class GameInfoSim extends JPanel{

	private JLabel msgLbl;
	
	public GameInfoSim() {
		setLayout(new FlowLayout(FlowLayout.CENTER,(int) (Types.GUI_SCALING_FACTOR_X * 20),(int) (Types.GUI_SCALING_FACTOR_Y * 10)));
				   // FlowLayout(aligh,hgap,vgap), where the gaps are between components and between components and border
		setBackground(Types.GUI_BGCOLOR);
		msgLbl = createLabel("" );
		this.add(msgLbl);
	}
	
	private JLabel createLabel(String str) {
		JLabel lbl = new JLabel(str);
		lbl.setFont(new Font("Arial",Font.BOLD,(int)(1.5*Types.GUI_DIALOGFONTSIZE)));
		return lbl;
	}	
	
	public void changeMessage(String str) {
		msgLbl.setText(str); 
	}
	
}
