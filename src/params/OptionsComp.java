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
	//private JLabel lsingleComp;
	private JLabel lPlayUntil;
	private JLabel lNumGames;
	private JLabel lNumCompetitions;
	//private JLabel lOpponents;
	//private JLabel lFirstPlayer;
	//private JLabel lSecondPlayer;

	//private JButton ok;
	private OptionsComp m_par;

	private JCheckBox cbUseCurBoard;
	private JCheckBox cbLogValues;
	private JCheckBox cbswapPlayers;

	//private TextField tNumPieces;
	private JTextField tNumGames;
	private JTextField tNumCompetitions;
	
	private Choice cFirstPlayer;
	private Choice cSecondPlayer;

	public OptionsComp() {
		super("(Multi-)Competition Parameters");
		setSize(320, 700);
		setBounds(0, 0, 320, 700);
		setLayout(new BorderLayout(10, 10));
		add(new JLabel(" "), BorderLayout.SOUTH); 

		//lsingleComp = new JLabel("Multi-Competition");
		//lsingleComp.setFont(new Font("Times New Roman", Font.BOLD, 14));
		
		lNumGames = new JLabel("# games/competition");
		lNumCompetitions = new JLabel("# competitions (only MULTI)");

		lPlayUntil = new JLabel("Stop Game after x Moves: ");
		tNumGames = new JTextField("3", 3);
		tNumCompetitions = new JTextField("10", 3);

		//ok = new JButton("OK");
		m_par = this;

		cbUseCurBoard = new JCheckBox("Use current board");
		cbUseCurBoard.setSelected(true);

		cbLogValues = new JCheckBox("Log value tables of opponents");
		cbLogValues.setSelected(true);
		
		cbswapPlayers = new JCheckBox("Swap players (only MULTI)");
		cbswapPlayers.setSelected(false);
		
		//tNumPieces = new TextField("42", 3);
		
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
		

//		ok.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_par.setVisible(false);
//			}
//		});

		Panel p = new Panel();
		p.setLayout(new GridLayout2(0, 1, 5, 5));

		//p.add(lsingleComp);
		//p.add(new Canvas());
		
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

		p.add(lNumCompetitions);
		p.add(tNumCompetitions);

		p.add(cbUseCurBoard);
		//p.add(new Canvas());

		//p.add(lPlayUntil);
		//p.add(tNumPieces);

		p.add(cbLogValues);
		//p.add(new Canvas());
		
		p.add(cbswapPlayers);
		//p.add(new Canvas());

		//p.add(ok);
		//p.add(new Canvas());

		add(p);

		pack();
		setVisible(false);
	}
	
	public void showOptionsComp(Arena ticGame,boolean isVisible) {
		// place window winCompOptions on the right side of the XArenaTabs window
		this.setVisible(isVisible);
		int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
		int y = ticGame.m_xab.getLocation().y;
		if (ticGame.m_LaunchFrame!=null) {
			x = ticGame.m_LaunchFrame.getX() + ticGame.m_LaunchFrame.getWidth() + 1;
			y = ticGame.m_LaunchFrame.getY();
		}
		x += ticGame.m_tabs.getX() + 1;
		ticGame.m_xab.winCompOptions.setLocation(x,y);
		ticGame.m_xab.winCompOptions.setSize(Types.GUI_WINCOMP_WIDTH,Types.GUI_WINCOMP_HEIGHT);	
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

//	public int getNumPieces() {
//		return Integer.valueOf(tNumPieces.getText()).intValue();
//	}
	
	public int getNumGames() {
		return Integer.valueOf(tNumGames.getText()).intValue();
	}
	
	public int getNumCompetitions() {
		return Integer.valueOf(tNumCompetitions.getText()).intValue();
	}
	
	public int getFirstPlayer() {
		return cFirstPlayer.getSelectedIndex();
	}
	
	public int getSecondPlayer() {
		return cSecondPlayer.getSelectedIndex();
	}
}
