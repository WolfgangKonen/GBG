package games.Poker;

import games.Arena;
import games.Othello.Gui.Legend;
import tools.Types;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GameBoardPokerGuiOld extends JFrame {

    // GB
    private final GameBoardPoker m_gb;

    CardPanel[] communityCardsPanels;
    CardPanel[] holeCardsPanels;

    // Dummy
    StateObserverPoker test;

    // Log Panel
    private JScrollPane scrollPaneLog;
    private JTextArea log;

    // Action Panel
    private JButton checkButton;
    private JButton betButton;
    private JButton callButton;
    private JButton raiseButton;
    private JButton foldButton;
    private JButton allInButton;
    private JButton continueButton;

    private JLabel currentPlayerChipsLabel;

    // Pot Panel
    JLabel pot;

    // info panel
    JLabel[] playerChips;
    JPanel[] playerNamePanel;
    JLabel[] playerCall;

    // Pause workaround
    boolean pause;

    // color scheme
    Color foldedColor = new Color(246, 81, 29);
    Color inactiveColor = new Color(128, 128, 128);
    Color lostColor = new Color(13, 44, 84);
    Color waitingColor = new Color(127, 184, 0);
    Color currentColor = new Color(0, 166, 237);
    Color openColor = new Color(255, 180, 0);
    Color allInColor = new Color(98, 143, 0);

    boolean check = true;

    GameBoardPokerGuiOld(GameBoardPoker gb){
        super("Poker");

        m_gb = gb;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // set Layout
        this.setLayout(new BorderLayout());

        // NORTH
        this.add(initPotPanel(),BorderLayout.NORTH);

        // EAST
        this.add(getInfoPanel(),BorderLayout.WEST);

        // CENTER
        this.add(getCardPanel(),BorderLayout.CENTER);

        // EAST
        this.add(getLogPanel(),BorderLayout.EAST);

        // SOUTH
        this.add(initActionButtonsPanel(),BorderLayout.SOUTH);

        this.pack();
        this.setMinimumSize(this.getSize());
        this.setVisible(false);
    }

    private Panel initActionButtonsPanel(){

        checkButton     = new JButton("check");
        betButton       = new JButton("bet");
        callButton      = new JButton("call");
        raiseButton     = new JButton("raise");
        foldButton      = new JButton("fold");
        allInButton     = new JButton("all in");
        continueButton  = new JButton("continue");

        checkButton.addActionListener( e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                m_gb.HGameMove(1);
        });
        betButton.addActionListener(e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                m_gb.HGameMove(2);
        });
        callButton.addActionListener(e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                m_gb.HGameMove(3);
        });
        raiseButton.addActionListener(e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                m_gb.HGameMove(4);
        });
        foldButton.addActionListener(e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                m_gb.HGameMove(0);
        });
        allInButton.addActionListener(e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                m_gb.HGameMove(5);
        });

        continueButton.addActionListener(e -> {
            if (m_gb.getArena().taskState == Arena.Task.PLAY)
                continueWithTheGame();
        });

        currentPlayerChipsLabel = new JLabel("Chips: ");

        Panel actionPanel = new Panel(new FlowLayout());
        actionPanel.add(currentPlayerChipsLabel);
        actionPanel.add(checkButton);
        actionPanel.add(betButton  );
        actionPanel.add(callButton );
        actionPanel.add(raiseButton);
        actionPanel.add(foldButton );
        actionPanel.add(allInButton);
        actionPanel.add(continueButton);
        return actionPanel;

    }

    private Panel getCardPanel(){
        Panel centerPanel = new Panel();
        centerPanel.setLayout(new GridLayout(2,1));
        Panel centerPanelTop = new Panel();
        Panel centerPanelBottom = new Panel();

        // Community Cards
        communityCardsPanels = new CardPanel[5];
        for(int i = 0;i<5;i++) {
            communityCardsPanels[i] = new CardPanel(150);
        }

        holeCardsPanels = new CardPanel[5];
        for(int i = 0;i<2;i++) {
            holeCardsPanels[i] = new CardPanel(150);
        }

        //FLOP
        Panel flopPanel = new Panel();
        flopPanel.setLayout(new BoxLayout(flopPanel,BoxLayout.PAGE_AXIS));
        Panel flopCards = new Panel();

        flopCards.add(communityCardsPanels[0]);
        flopCards.add(communityCardsPanels[1]);
        flopCards.add(communityCardsPanels[2]);

        flopPanel.add(flopCards);
        JLabel flopLabel = new JLabel("Flop Cards");
        flopLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        flopPanel.add(flopLabel);

        //TURN
        Panel turnPanel = new Panel();
        turnPanel.setLayout(new BoxLayout(turnPanel,BoxLayout.PAGE_AXIS));
        Panel turnCards = new Panel();

        turnCards.add(communityCardsPanels[3]);
        turnPanel.add(turnCards);

        JLabel turnLabel = new JLabel("Turn Cards");
        turnLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        turnPanel.add(turnLabel);

        //RIVER
        Panel riverPanel = new Panel();
        riverPanel.setLayout(new BoxLayout(riverPanel,BoxLayout.PAGE_AXIS));
        Panel riverCards = new Panel();

        riverCards.add(communityCardsPanels[4]);
        riverPanel.add(riverCards);

        JLabel riverLabel = new JLabel("River Cards");
        riverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        riverPanel.add(riverLabel);

        centerPanelTop.add(flopPanel);
        centerPanelTop.add(turnPanel);
        centerPanelTop.add(riverPanel);

        Panel holePanel = new Panel();
        holePanel.setLayout(new BoxLayout(holePanel,BoxLayout.PAGE_AXIS));

        Panel holeCards = new Panel();
        holeCards.add(holeCardsPanels[0]);
        holeCards.add(holeCardsPanels[1]);

        JLabel holeLabel = new JLabel("Hole Cards");
        holeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        holePanel.add(holeCards);
        holePanel.add(holeLabel);

        centerPanelBottom.add(holePanel);

        centerPanel.add(centerPanelTop);
        centerPanel.add(centerPanelBottom);

        return  centerPanel;
    }

    private Panel initPotPanel(){
        Panel potPanel = new Panel();
        pot = new JLabel("Pot: 0");
        potPanel.add(pot);
        return potPanel;
    }

    public void updatePotValue(double newValue){
        if(pot!=null)
            pot.setText("Pot: "+newValue);
    }

    private Panel getLogPanel(){
        Panel logPanel = new Panel();
        log = new JTextArea(30, 20);
        log.setEditable(false);
        scrollPaneLog = new JScrollPane(log,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logPanel.add(scrollPaneLog);

        return logPanel;
    }

    private Panel getInfoPanel(){
        Panel infoPanel = new Panel();

        int count = StateObserverPoker.NUM_PLAYER;

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS));
        playerChips        = new JLabel[count];
        playerNamePanel    = new JPanel[count];
        playerCall         = new JLabel[count];
        //JPanel[] playerPanel = new JPanel[StateObserverPoker.NUM_PLAYER];

        for(int i = 0 ; i < count ; i++){
            // Define JPanel for name
            JPanel nameBox = new JPanel();
            nameBox.setLayout(new GridLayout(1,1));
            JLabel name = new JLabel(getPlayerName(i% StateObserverPoker.NUM_PLAYER),
                    SwingConstants.CENTER);
            nameBox.setAlignmentY(Component.CENTER_ALIGNMENT);
            nameBox.setBorder(BorderFactory.createLineBorder(Color.black));
            nameBox.add(name);
            nameBox.setMinimumSize(new Dimension(50, 50));
            nameBox.setPreferredSize(new Dimension(50, 50));
            nameBox.setMaximumSize(new Dimension(50, 50));

            playerNamePanel[i] = nameBox;

            playerCall[i] = new JLabel("To Call: 0");
            playerChips[i] = new JLabel("Chips: 0");

            playerCall[i].setBorder(new EmptyBorder(0, 5, 0, 0));
            playerChips[i].setBorder(new EmptyBorder(0, 5, 0, 0));

            JPanel chipsData = new JPanel();
            chipsData.setLayout(new GridLayout(2,1));
            chipsData.setBorder(BorderFactory.createLineBorder(Color.black));
            chipsData.setPreferredSize(new Dimension(150, 50));
            chipsData.setMaximumSize(new Dimension(150, 50));
            chipsData.add(playerCall[i]);
            chipsData.add(playerChips[i]);

            JPanel playerData = new JPanel();
            playerData.add(nameBox);
            playerData.add(chipsData);

            playerData.setLayout(new BoxLayout(playerData,BoxLayout.LINE_AXIS));
            playerData.setPreferredSize(new Dimension(150, 50));
            playerData.setMaximumSize(new Dimension(150, 50));
            //playerPanel[i] = playerData;
            infoPanel.add(playerData);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        JPanel legendTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel legendLabel = new JLabel("Legend:",SwingConstants.LEFT);
        legendTitlePanel.add(legendLabel);
        legendTitlePanel.setPreferredSize(new Dimension(150,25));
        legendTitlePanel.setMaximumSize(new Dimension(150,25));
        legendPanel.add(legendTitlePanel);
        legendPanel.add(new LegendPanel(currentColor,"Current"));
        legendPanel.add(new LegendPanel(waitingColor,"Done"));
        legendPanel.add(new LegendPanel(openColor,"Open"));
        legendPanel.add(new LegendPanel(foldedColor,"Folded"));
        legendPanel.add(new LegendPanel(allInColor,"All In"));
        legendPanel.add(new LegendPanel(lostColor,"Lost"));

        infoPanel.add(legendPanel);
        infoPanel.add(Box.createVerticalGlue());

        return infoPanel;
    }

    class LegendPanel extends  JPanel{
        LegendPanel(Color color, String label){
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(color);
            JLabel labelPanel = new JLabel(label);
            this.add(colorPanel);
            this.add(labelPanel);
            this.setPreferredSize(new Dimension(150,25));
            this.setMaximumSize(new Dimension(150,25));
        }
    }

    private String getPlayerName(int i){
        return Types.GUI_PLAYER_NAME[i];
    }

    private void updateActions(StateObserverPoker SoP){
        if(SoP.isRoundOver()){
            pause = true;
            continueButton.setEnabled(true);
            foldButton.setEnabled(false);
            checkButton.setEnabled(false);
            betButton.setEnabled(false);
            callButton.setEnabled(false);
            raiseButton.setEnabled(false);
            allInButton.setEnabled(false);

            while(pause){
                try {
                    Thread.sleep(500);
                } catch (Exception e){

                }
            }

        }else{
            ArrayList<Types.ACTIONS> allActions = SoP.getAllAvailableActions();
            for (Types.ACTIONS action : allActions) {
                    switch (action.toInt()) {
                        // FOLD
                        case 0 -> foldButton.setEnabled(SoP.getAvailableActions().contains(action));
                        // CHECK
                        case 1 -> checkButton.setEnabled(SoP.getAvailableActions().contains(action));
                        // BET
                        case 2 -> betButton.setEnabled(SoP.getAvailableActions().contains(action));
                        // CALL
                        case 3 -> callButton.setEnabled(SoP.getAvailableActions().contains(action));
                        // RAISE
                        case 4 -> raiseButton.setEnabled(SoP.getAvailableActions().contains(action));
                        // ALL IN
                        case 5 -> allInButton.setEnabled(SoP.getAvailableActions().contains(action));
                    }
            }
            currentPlayerChipsLabel.setText("Chips: "+SoP.getChips()[SoP.getPlayer()]);
            if(betButton.isEnabled())
                betButton.setText("bet ("+SoP.getBigblind()+")");
            if(raiseButton.isEnabled())
                raiseButton.setText("raise ("+(SoP.getOpenPlayer(SoP.getPlayer())+SoP.getBigblind())+")");
            if(callButton.isEnabled())
                callButton.setText("call ("+SoP.getOpenPlayer(SoP.getPlayer())+")");
            if(allInButton.isEnabled())
                allInButton.setText("all in ("+SoP.getChips()[SoP.getPlayer()]+")");
            continueButton.setEnabled(false);
        }

    }

    public void addToLog(String line){
        log.append(line+"\r\n");
    }

    public void updateBoard(StateObserverPoker soT,
                            boolean withReset, boolean showValueOnGameboard) {

        if(withReset) {
            System.out.println("with reset is called");
        }

        if(showValueOnGameboard)
            System.out.println("show value on gameboard");
        else
            System.out.println("don't show value on gameboard");

        updateLog(soT);
        updateCards(soT);
        updatePotValue(soT.getPotSize());
        updatePlayerInfo(soT);
        updateActions(soT);

        //repaint();
    }

    public void resetLog(){
        if(log!=null){
            log.setText("");
        }
    }
    private void updateLog(StateObserverPoker sop){

        // update log
        if(sop.getLastActions()!=null)
            for(String entry:sop.getLastActions())
                addToLog(entry);
        sop.resetLog();

        // scoll to the end of the log
        scrollPaneLog.getVerticalScrollBar().setValue(
                scrollPaneLog.getVerticalScrollBar().getMaximum()
        );
    }

    private void updateCards(StateObserverPoker sop){
        PlayingCard[] communityCards = sop.getCommunityCards();
        for(int i = 0;i<communityCards.length;i++){
            if(communityCards[i]!=null){
                communityCardsPanels[i].setCard(communityCards[i].getImagePath());
            }else{
                communityCardsPanels[i].reset();
            }
        }

        PlayingCard[] holeCards = sop.getHoleCards();
        for(int i = 0;i<holeCards.length;i++){
            if(holeCards[i]!=null){
                holeCardsPanels[i].setCard(holeCards[i].getImagePath());
            }else{
                communityCardsPanels[i].reset();
            }
        }
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

    private void updatePlayerInfo(StateObserverPoker sop){
        double[] chips = sop.getChips();
        boolean[] active = sop.getActivePlayers();
        boolean[] playing = sop.getPlayingPlayers();
        boolean[] folded = sop.getFoldedPlayers();
        boolean[] open = sop.getOpenPlayers();
        for(int i = 0 ; i < StateObserverPoker.NUM_PLAYER ; i++){

            if(!playing[i]){
                playerNamePanel[i].setBackground(lostColor);
                playerChips[i].setText("Chips: -");
                playerCall[i].setText("To call: -");
            }else {
                playerChips[i].setText("Chips: " + chips[i]);

                if (folded[i]) {
                    playerNamePanel[i].setBackground(foldedColor);
                    playerCall[i].setText("To call: -");
                    continue;
                }

                if (!active[i] && !folded[i]) {
                    playerNamePanel[i].setBackground(allInColor);
                    playerCall[i].setText("To call: -");
                    continue;
                }

                playerCall[i].setText("To call: " + m_gb.m_so.getOpenPlayer(i));

                playerNamePanel[i].setBackground(waitingColor);

                //Current Player
                if (i == m_gb.m_so.getPlayer()) {
                    playerNamePanel[i].setBackground(currentColor);
                }

                //Player who needs to make a move
                if (open[i]) {
                    playerNamePanel[i].setBackground(openColor);
                }



            }
            //playerActive[i].setText(Boolean.toString(active[i]));
        }
    }

    public void enableInteraction(boolean enable) {
        if(enable)
            System.out.println("enable interaction!");
    }

    private void continueWithTheGame(){
        pause = false;
    }
}
