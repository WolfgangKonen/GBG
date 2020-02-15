package params;

import params.GridLayout2;
import tools.Types;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import games.Arena;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

/**
 * 
 * Competition Options
 *
 */
public class OptionsComp extends JFrame {
	private static final long serialVersionUID = 1L;
	private JLabel lPlayUntil;
	private JLabel lNumGames;
	//private JLabel lOpponents;
	//private JLabel lFirstPlayer;
	//private JLabel lSecondPlayer;

	private JCheckBox cbUseCurBoard;
	private JCheckBox cbLogValues;
	private JCheckBox cbswapPlayers;

	private JTextField tNumGames;
	
	private Choice cFirstPlayer;
	private Choice cSecondPlayer;

	public OptionsComp(int competeNumber) {
		super("Competition Options");
//		setSize(320, 500);
//		setBounds(0, 0, 320, 500);
		setLayout(new BorderLayout(10, 10));
		add(new JLabel(" "), BorderLayout.SOUTH); 

		lNumGames = new JLabel("# games/competition");

		lPlayUntil = new JLabel("Stop Game after x Moves: ");
		tNumGames = new JTextField(""+competeNumber, 3);

		cbUseCurBoard = new JCheckBox("Use current board");
		cbUseCurBoard.setSelected(true);

		cbLogValues = new JCheckBox("Log value tables of opponents");
		cbLogValues.setSelected(true);
		
		cbswapPlayers = new JCheckBox("Swap players (only MULTI)");
		cbswapPlayers.setSelected(false);
		
//		lOpponents = new JLabel("Opponents");
//		lFirstPlayer = new JLabel("First player");
//		lSecondPlayer = new JLabel("Second player");
//		cFirstPlayer = new Choice();
//		cSecondPlayer = new Choice();
//		cFirstPlayer.add("Agent X");
//		cFirstPlayer.add("Agent O");
//		//cFirstPlayer.add("Agent Eval");
//		cSecondPlayer.add("Agent X");
//		cSecondPlayer.add("Agent O");
//		//cSecondPlayer.add("Agent Eval");
//		cFirstPlayer.select(0);
//		cSecondPlayer.select(1);
		

		Panel p = new Panel();
		p.setLayout(new GridLayout2(0, 1, 5, 5));

//		p.add(lOpponents);
//		p.add(new Canvas());
//		
//		p.add(lFirstPlayer);
//		p.add(lSecondPlayer);
//		
//		p.add(cFirstPlayer);
//		p.add(cSecondPlayer);
		
		p.add(lNumGames);
		p.add(tNumGames);

		p.add(cbUseCurBoard);

		//p.add(lPlayUntil);

		p.add(cbLogValues);
		
		add(p);

		pack();
		setVisible(false);
	}
	
	public void showOptionsComp(Arena arena,boolean isVisible) {
		if (arena.hasGUI()) {
			// place window winCompOptions on the right side of the XArenaTabs window
			this.setVisible(isVisible);
			int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
			int y = arena.m_xab.getLocation().y;
			if (arena.m_ArenaFrame!=null) {
				x = arena.m_ArenaFrame.getX() + arena.m_ArenaFrame.getWidth() + 1;
				y = arena.m_ArenaFrame.getY();
			}
			if (arena.m_tabs!=null) x += arena.m_tabs.getX() + 1;
			arena.m_xab.winCompOptions.setLocation(x,y);
			arena.m_xab.winCompOptions.setSize(Types.GUI_WINCOMP_WIDTH,Types.GUI_WINCOMP_HEIGHT);	
		}
	}

	public boolean swapPlayers() {
		return cbswapPlayers.isSelected();
	}

	public boolean useCurBoard() {
		return cbUseCurBoard.isSelected();
	}

	public boolean logValues() {
		return cbLogValues.isSelected();
	}

	public int getNumGames() {
		return Integer.valueOf(tNumGames.getText()).intValue();
	}
	
	public void setNumGames(int competeNumber) {
		tNumGames.setText(""+competeNumber);
	}
	
	public int getFirstPlayer() {
		return cFirstPlayer.getSelectedIndex();
	}
	
	public int getSecondPlayer() {
		return cSecondPlayer.getSelectedIndex();
	}
}
