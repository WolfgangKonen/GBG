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
	private JPanel[] playerNamePanel;
	private JLabel[] playerCall;
	private JPanel[] playerPanel;

	private JTextArea log;

	public GameBoardPokerGui(GameBoardPoker gb) {
		super("Poker");
		m_gb = gb;
		initGui();
		initLog();
	}

	private void initLog(){
		JFrame logWindow = new JFrame();

		log = new JTextArea(30, 20);
		log.setEditable(false);
		logWindow.add(new JScrollPane(log));

		logWindow.setSize(280,500);
		logWindow.setVisible(true);
	}

	private String getPlayerName(int i){
		return Types.GUI_PLAYER_NAME[i];
	}

	private void initPlayerGUI2(){
		playerChips = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerNamePanel = new JPanel[StateObserverPoker.NUM_PLAYER];
		playerCall = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerPanel = new JPanel[StateObserverPoker.NUM_PLAYER];

		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			// Define JPanel for name
			JPanel nameBox = new JPanel();
			nameBox.setLayout(new GridLayout(1,1));
			JLabel name = new JLabel(getPlayerName(i), SwingConstants.CENTER);
			nameBox.setAlignmentY(Component.CENTER_ALIGNMENT);
			nameBox.setBorder(BorderFactory.createLineBorder(Color.black));
			nameBox.add(name);
			nameBox.setPreferredSize(new Dimension(50, 50));
			nameBox.setMaximumSize(new Dimension(50, 50));

			playerNamePanel[i] = nameBox;

			playerCall[i] = new JLabel("To Call: 0");
			playerChips[i] = new JLabel("Chips: 0");

			JPanel chipsData = new JPanel();
			chipsData.setLayout(new GridLayout(2,1));
			chipsData.setBorder(BorderFactory.createLineBorder(Color.black));
			chipsData.setMaximumSize(new Dimension(1000, 50));
			chipsData.add(playerCall[i]);
			chipsData.add(playerChips[i]);

			JPanel playerData = new JPanel();
			playerData.add(nameBox);
			playerData.add(chipsData);

			playerData.setLayout(new BoxLayout(playerData,BoxLayout.LINE_AXIS));
			playerData.setMaximumSize(new Dimension(1000, 50));
			playerPanel[i] = playerData;
			pf.addPlayerData(playerData);
		}
		updatePlayerInfo();

	}

	private void initPlayerGUI(){
		JLabel[] playerNames = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerChips = new JLabel[StateObserverPoker.NUM_PLAYER];
		playerActive = new JLabel[StateObserverPoker.NUM_PLAYER];
		JLabel[] playerCall = new JLabel[StateObserverPoker.NUM_PLAYER];
		JPanel[] playerData = new JPanel[StateObserverPoker.NUM_PLAYER];

		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			playerNames[i] = new JLabel(getPlayerName(i));
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
		initPlayerGUI2();
		add(pf.gameBoardPanel);
		pack();
		setVisible(false);		// note that the true size of this component is set in 
								// showGameBoard(Arena,boolean)
	}

	public void updatePlayerInfo(){
		double[] chips = m_gb.m_so.getChips();
		boolean[] active = m_gb.m_so.getActivePlayers();
		boolean[] playing = m_gb.m_so.getPlayingPlayers();
		boolean[] folded = m_gb.m_so.getFoldedPlayers();
		boolean[] open = m_gb.m_so.getOpenPlayers();

		for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){
			playerChips[i].setText("Chips: "+ chips[i]);
			playerCall[i].setText("To call: "+m_gb.m_so.getOpenPlayer(i));
			playerNamePanel[i].setBackground(new Color(98, 255, 0));
			if(i == m_gb.m_so.getPlayer()) {
				playerNamePanel[i].setBackground(new Color(255, 111, 0));
			}
			if(open[i]){
				playerNamePanel[i].setBackground(new Color(255, 221, 0));
			}
			if(!active[i]&&!folded[i]){
				playerNamePanel[i].setBackground(new Color(61, 167, 239));
			}
			if(folded[i]){
				playerNamePanel[i].setBackground(new Color(239, 61, 61));
			}
			if(!playing[i]){
				playerNamePanel[i].setBackground(new Color(128, 128, 128));
			}
			//playerActive[i].setText(Boolean.toString(active[i]));
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
		System.out.println("Update Board by: "+soT.getPlayer());

		if(withReset)
			System.out.println("with reset");
		else
			System.out.println("without reset");

		if(showValueOnGameboard)
			System.out.println("show value on gameboard");
		else
			System.out.println("don't show value on gameboard");


		//Update Log
		if(soT.getLastActions()!=null)
			for(String entry:soT.getLastActions())
				log.append(entry+"\r\n");
		soT.resetLog();

		updatePlayerInfo();
		pf.updatePot(soT.getPotSize());
		pf.updateActivePlayer(getPlayerName(soT.getPlayer()));
		pf.updateHoleCards(soT.getHoleCards());
		pf.updateCommunityCards(soT.getCommunityCards());
		pf.disableButtons();
		ArrayList<Types.ACTIONS> actions = soT.getAvailableActions();
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
