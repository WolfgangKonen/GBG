package src.games.Poker;

import games.Arena;
import tools.Types;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameBoardPokerGui extends JFrame {

	private final GameBoardPoker m_gb;
	private pokerForm pf;

	private JLabel[] playerChips;
	private JLabel[] playerActive;

	public GameBoardPokerGui(GameBoardPoker gb) {
		super("Poker");
		m_gb = gb;
		initGui();
	}

	private void initPlayerGUI(){
		JLabel[] playerNames = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerChips = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerActive = new JLabel[StateObserverPoker.NUM_PLAYER];
		JLabel[] playerCall = new JLabel[StateObserverPoker.NUM_PLAYER];
		JPanel[] playerData = new JPanel[StateObserverPoker.NUM_PLAYER];

		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			playerNames[i] = new JLabel("Player "+Types.GUI_PLAYER_NAME[i]+": ");
			playerActive[i] = new JLabel();
			playerCall[i] = new JLabel();
			playerChips[i] = new JLabel();

			playerData[i] = new JPanel();

			playerData[i].add(playerNames[i]);
			playerData[i].add(playerActive[i]);
			playerData[i].add(playerCall[i]);
			playerData[i].add(playerChips[i]);

			playerData[i].setLayout(new GridLayout(2,2));
			pf.addPlayerData(playerData[i]);
		}
		updatePlayerInfo();
	}


    private void initGui()
	{
		pf = new pokerForm(m_gb,StateObserverPoker.NUM_PLAYER);
		initPlayerGUI();
		add(pf.gameBoardPanel);
		pack();
		setVisible(false);		// note that the true size of this component is set in 
								// showGameBoard(Arena,boolean)
	}

	public void updatePlayerInfo(){
		double[] chips = m_gb.m_so.getChips();
		boolean[] active = m_gb.m_so.getActivePlayers();
		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			playerChips[i].setText(Double.toString(chips[i]));
			playerActive[i].setText(Boolean.toString(active[i]));
		}
	}

	/**
	 * Update the play board and the associated values (labels).
	 * 
	 * @param soT	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverPoker soT,
                            boolean withReset, boolean showValueOnGameboard) {
		if(withReset)
			System.out.println("with reset");
		else
			System.out.println("without reset");

		if(showValueOnGameboard)
			System.out.println("show value on gameboard");
		else
			System.out.println("don't show value on gameboard");

		updatePlayerInfo();
		pf.updatePot(soT.getPot());
		pf.updateHoleCards(soT.getHoleCards());
		pf.updateCommunityCards(soT.getCommunityCards());
		pf.disableButtons();
		ArrayList<Types.ACTIONS> actions =soT.getAvailableActions();
		for (Types.ACTIONS action : actions) {
			switch (action.toInt()) {
				// FOLD
				case 0 -> pf.enableFold();
				// CHECK
				case 1 -> pf.enableCheck();
				// BET
				case 2 -> pf.enableBet();
				// CALL
				case 3 -> pf.enableCall();
				// RAISE
				case 4 -> pf.enableRaise();
				// ALL IN
				case 5 -> pf.enableAllIn();
			}
		}
		repaint();
	}



	public void enableInteraction(boolean enable) {
		if(enable)
			System.out.println("enable interaction!");
	}

	public void showGameBoard(Arena arena, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 20;
			int y = arena.m_xab.getLocation().y;
			if (arena.m_ArenaFrame!=null) {
				x = arena.m_ArenaFrame.getX();
				y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() +1;
				this.setSize(1200,400);
			}
			this.setLocation(x,y);	
		}
	}

	public void toFront() {
    	super.setState(JFrame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}

   public void destroy() {
		this.setVisible(false);
		this.dispose();
   }

}
