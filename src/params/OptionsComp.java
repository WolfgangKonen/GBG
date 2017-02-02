package params;

import params.GridLayout2;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class OptionsComp extends Frame {
	private static final long serialVersionUID = 1L;
	private Label lsingleComp;
	private Label lPlayUntil;
	private Label lNumGames;
	private Label lOpponents;
	private Label lFirstPlayer;
	private Label lSecondPlayer;

	private JButton ok;
	private OptionsComp m_par;

	private Checkbox cbUseCurBoard;
	private Checkbox cbLogValues;
	private Checkbox cbswapPlayers;

	private TextField tNumPieces;
	private TextField tNumGames;
	
	private Choice cFirstPlayer;
	private Choice cSecondPlayer;

	public OptionsComp() {
		super("Multi-Competition-Parameters");
		setSize(120, 700);
		setBounds(0, 0, 120, 700);
		setLayout(new BorderLayout(10, 10));
		add(new Label(" "), BorderLayout.SOUTH); 

		lsingleComp = new Label("Multi-Competition");
		lsingleComp.setFont(new Font("Times New Roman", Font.BOLD, 14));
		
		lNumGames = new Label("Number of Competitions (only MULTI)");

		lPlayUntil = new Label("Stop Game after x Moves: ");
		tNumGames = new TextField("10", 3);

		ok = new JButton("OK");
		m_par = this;

		cbUseCurBoard = new Checkbox("Use current Board");
		cbUseCurBoard.setState(true);

		cbLogValues = new Checkbox("Log Value-Tables of Opponents");
		cbLogValues.setState(true);
		
		cbswapPlayers = new Checkbox("Swap Players after each Competition (only MULTI)");
		cbswapPlayers.setState(false);
		
		tNumPieces = new TextField("42", 3);
		
		lOpponents = new Label("Opponents");
		lFirstPlayer = new Label("First Player");
		lSecondPlayer = new Label("Second Player");
		cFirstPlayer = new Choice();
		cSecondPlayer = new Choice();
		cFirstPlayer.add("Agent X");
		cFirstPlayer.add("Agent O");
		cFirstPlayer.add("Agent Eval");
		cSecondPlayer.add("Agent X");
		cSecondPlayer.add("Agent O");
		cSecondPlayer.add("Agent Eval");
		
		cFirstPlayer.select(0);
		cSecondPlayer.select(1);
		

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_par.setVisible(false);
			}
		});

		Panel p = new Panel();
		p.setLayout(new GridLayout2(10, 2, 5, 5));

		p.add(lsingleComp);
		p.add(new Canvas());
		
		p.add(lOpponents);
		p.add(new Canvas());
		
		p.add(lFirstPlayer);
		p.add(lSecondPlayer);
		
		p.add(cFirstPlayer);
		p.add(cSecondPlayer);
		
		p.add(lNumGames);
		p.add(tNumGames);

		p.add(cbUseCurBoard);
		p.add(new Canvas());

		p.add(lPlayUntil);
		p.add(tNumPieces);

		p.add(cbLogValues);
		p.add(new Canvas());
		
		p.add(cbswapPlayers);
		p.add(new Canvas());

		p.add(ok);
		p.add(new Canvas());

		add(p);

		pack();
		setVisible(false);
	}
	
	public boolean swapPlayers() {
		return cbswapPlayers.getState();
	}

	public boolean useCurBoard() {
		return cbUseCurBoard.getState();
	}

	public boolean logValues() {
		return cbLogValues.getState();
	}

	public int getNumPieces() {
		return Integer.valueOf(tNumPieces.getText()).intValue();
	}
	
	public int getNumGames() {
		return Integer.valueOf(tNumGames.getText()).intValue();
	}
	
	public int getFirstPlayer() {
		return cFirstPlayer.getSelectedIndex();
	}
	
	public int getSecondPlayer() {
		return cSecondPlayer.getSelectedIndex();
	}
}
