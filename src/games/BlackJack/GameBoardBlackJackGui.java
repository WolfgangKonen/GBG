package games.BlackJack;

import games.Arena;
import games.BlackJack.StateObserverBlackJack.BlackJackActionDet;
import params.GridLayout2;
import tools.Types;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class GameBoardBlackJackGui extends JFrame {
    //scrollpane for GameLog
    JScrollPane scrollPaneLog;
    int verticalScrollBarMaximumValue;

    // actionButtons and panels that get displayed in actionZone
    ArrayList<ActionButton> currentButtons = new ArrayList<>();
    ArrayList<ButtonValueJPanel> currentButtonPanels = new ArrayList<>();


    //Colors
    Color red = new Color(70, 1, 1);
    Color green = new Color(1, 70, 1);
    Color blue = new Color(1, 79, 160);

    //autoContinue Button
    private JButton continueButton;
    private JCheckBox autoContinue = new JCheckBox("autoContinue", true);
    boolean hold_flag = false;


    class ActionHandler implements ActionListener {
        int action;

        ActionHandler(int action) {
            this.action = action;
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    private static final long serialVersionUID = 1L;
    GameBoardBlackJack m_gb = null;
    StateObserverBlackJack m_so = null;

    public GameBoardBlackJackGui(GameBoardBlackJack gb) {
        super("BlackJack");
        m_gb = gb;
        initGui();
    }

    JPanel actionZone, dealerZone, playerZone;

    /**
     * inits BlackJack GUI
     */
    public void initGui() {

        JPanel window = new JPanel();
        SpringLayout spr = new SpringLayout();
        window.setLayout(spr);

        dealerZone = new JPanel();
        dealerZone.setBorder(getTitledBorder("Dealer und Gamelog"));
        playerZone = new JPanel();
        playerZone.setBorder(getTitledBorder("Players"));
        actionZone = new JPanel();
        actionZone.setBorder(getTitledBorder("Actions"));

        // Set West and East bounds
        spr.putConstraint(SpringLayout.EAST, dealerZone, 0, SpringLayout.EAST, window);
        spr.putConstraint(SpringLayout.WEST, actionZone, 0, SpringLayout.WEST, window);
        spr.putConstraint(SpringLayout.EAST, playerZone, 0, SpringLayout.WEST, dealerZone);
        spr.putConstraint(SpringLayout.EAST, actionZone, 0, SpringLayout.WEST, playerZone);

        // Set South bounds
        spr.putConstraint(SpringLayout.SOUTH, actionZone, 0, SpringLayout.SOUTH, window);
        spr.putConstraint(SpringLayout.SOUTH, dealerZone, 0, SpringLayout.SOUTH, window);
        spr.putConstraint(SpringLayout.SOUTH, playerZone, 0, SpringLayout.SOUTH, window);

        // Set North bounds
        spr.putConstraint(SpringLayout.NORTH, playerZone, 0, SpringLayout.NORTH, window);
        spr.putConstraint(SpringLayout.NORTH, dealerZone, 0, SpringLayout.NORTH, window);
        spr.putConstraint(SpringLayout.NORTH, actionZone, 0, SpringLayout.NORTH, window);

        dealerZone.setBackground(red);
        playerZone.setBackground(green);
        actionZone.setBackground(blue);

        window.add(actionZone);
        window.add(playerZone);
        window.add(dealerZone);

        playerZone.setPreferredSize(new Dimension(550, 800));
        actionZone.setPreferredSize(new Dimension(200, 800));
        dealerZone.setPreferredSize(new Dimension(450, 800));

        m_so = (StateObserverBlackJack) m_gb.getStateObs();
        actionZone.setLayout(new GridLayout(1, 1));

        this.add(window);
        this.setVisible(true);
        this.revalidate();
        this.repaint();


    }

    /**
     * updates BlackJack GUI
     * @param so updated StateObserver
     * @param withReset never used
     * @param showValueOnGameboard boolean, if true, show the game values for the available actions
     * (only if they are stored in state {@code so}).
     */
    public void update(StateObserverBlackJack so, boolean withReset, boolean showValueOnGameboard) {
        clear();
        m_so = so;
        playerZone.setLayout(new GridLayout(so.getNumPlayers(), 1));
        actionZone.add(getActionZone(so));
        for (Player p : so.getPlayers()) {
            playerZone.add(playerPanel(p));
        }
        dealerZone.add(dealerPanel(so.getDealer()));
        dealerZone.add(handHistoryPanel(so));
        toggleButtons(so);
        if(showValueOnGameboard)
            showValueOnButtons(so);
        revalidate();
        repaint();

        if(so.isRoundOver() && !autoContinue.isSelected()) {
            stopAfterUpdate();
        }
    }

    public TitledBorder getTitledBorder(String title){
        TitledBorder b1 = BorderFactory.createTitledBorder(title);
        b1.setTitleColor(Color.WHITE);
        b1.setBorder(BorderFactory.createEmptyBorder());
        return b1;
    }

    /**
     * displays the Game-State at roundOver situation
     */
    public void stopAfterUpdate(){
        for(JButton b : currentButtons) {
            b.setEnabled(false);
        }
        continueButton.setEnabled(true);
        hold_flag = true;
        while (true) {
            if (!hold_flag) {
                break;
            } else{
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("Error while halting the game with Thread.sleep()");
                }
            }
        }
    }

    /**
     * clears the 3 main-panels
     */
    public void clear() {
        dealerZone.removeAll();
        playerZone.removeAll();
        actionZone.removeAll();
    }


    /**
     * enables buttons corresponding to available actions from
     * @param so corresponding gamestate
     */
    public void toggleButtons(StateObserverBlackJack so){
        ArrayList<Types.ACTIONS> availableActions = so.getAvailableActions();
        for(ActionButton b : currentButtons){
            b.setEnabled(availableActions.contains(Types.ACTIONS.fromInt(b.getiAction())));
        }
    }

    /**
     *  show the game values for the available actions
     *  (only if they are stored in state {@code so}).
     */
    public void showValueOnButtons(StateObserverBlackJack so){
        if (so.getStoredValues() != null) {
            int counter = 0;
            double vTable[] = so.getStoredValues();
            for(int i = 0; i < vTable.length; i++){
                for(int j = 0; j < currentButtons.size(); j++){
                    ActionButton currentButton = currentButtons.get(j);
                    if(currentButton.getiAction() == so.getStoredAction(i).toInt()){
                        currentButton.setActionValue(vTable[i]);
                        JPanel p = new JPanel();
                        p.add(new JLabel(""+(Math.round(vTable[i]*100.0)/100.0)));
                        currentButtonPanels.get(j).setValuePanel(p);
                        currentButtonPanels.get(j).update();
                        break;
                    }
                }
            }

            for(double d: vTable){
                System.out.print("value "+d + " ");
                System.out.print(so.getStoredAction(counter)+ " ");
                System.out.println(BlackJackActionDet.values()[so.getStoredAction(counter++).toInt()].name());
            }

        }
    }

    /**
     * Factory method: creates JLabel containing a card
     * @param c card
     * @return JLable containing the card
     */
    public JLabel getCard(Card c) {
        BufferedImage cardPicture = null;
        //setPreferredSize(new Dimension(75, 105));
        try{
            cardPicture = ImageIO.read(getClass().getClassLoader().getResource("images/cards/"+c.getImagePath()));
        }catch(IOException e){
            System.out.println("Error while reading Image files");
        }
        JLabel card = new JLabel(new ImageIcon(cardPicture.getScaledInstance( 75,105, Image.SCALE_AREA_AVERAGING)));

        return card;
    }

    /**
     * Factory method: creates the actionZone. All available actions and the continue button will get grouped into it
     * @return JPanel containing all actionButtons
     */
    public JPanel getActionZone(StateObserverBlackJack so) {
        JPanel p = new JPanel();
        currentButtons.clear();
        currentButtonPanels.clear();
        p.setLayout(new GridLayout2(BlackJackActionDet.values().length+1, 1));
        for (BlackJackActionDet a: BlackJackActionDet.values()) {
            String nameOfAction = a.name();
            if(nameOfAction.equals("NOINSURANCE"))
                nameOfAction = "NO INSURANCE";
            ActionButton buttonToAdd = new ActionButton(nameOfAction, a.getAction());
            buttonToAdd.addActionListener(new ActionHandler(a.getAction()) {
                public void actionPerformed(ActionEvent e) {
                    if (m_gb.m_Arena.taskState == Arena.Task.PLAY) {
                        m_gb.humanMove(action);
                    } else if (m_gb.m_Arena.taskState == Arena.Task.INSPECTV) {
                        m_gb.inspectMove(action);
                    }
                }
            });
            currentButtons.add(buttonToAdd);
            buttonToAdd.setEnabled(false);
            ButtonValueJPanel bvPanel = new ButtonValueJPanel(buttonToAdd);
            p.add(bvPanel);
            currentButtonPanels.add(bvPanel);

        }
        JPanel continueHelper = new JPanel();
        continueHelper.setLayout(new GridLayout(1,2));

        continueButton = getContinueButton();
        continueHelper.add(continueButton);
        continueHelper.add(autoContinue);
        p.add(continueHelper);
        return p;
    }

    public JButton getContinueButton(){
        ActionButton result = new ActionButton("Continue", -1);
        result.addActionListener(new ActionHandler(0) {
            public void actionPerformed(ActionEvent e) {
                hold_flag = false;
                System.out.println("continue pressed");
            }
        });
        result.setEnabled(false);
        currentButtons.add(result);
        return result;
    }


    /**
     * Factory method: creates the handHistoryPanel (Gamelog).
     * @param so Game State
     * @return JPanel containing the gamelog
     */
    public JPanel handHistoryPanel(StateObserverBlackJack so) {
        JPanel p = new JPanel();
        JTextArea log = new JTextArea(22,33);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        log.setEditable(false);
        scrollPaneLog = new JScrollPane(log,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        verticalScrollBarMaximumValue = scrollPaneLog.getVerticalScrollBar().getMaximum();
        scrollPaneLog.getVerticalScrollBar().addAdjustmentListener(
                e -> {
                    if ((verticalScrollBarMaximumValue - e.getAdjustable().getMaximum()) == 0)
                        return;
                    e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                    verticalScrollBarMaximumValue = scrollPaneLog.getVerticalScrollBar().getMaximum();
                });
        p.add(scrollPaneLog);
        for(String s : so.getHandHistory()) {
            log.append(s + "\r\n");
        }
        return p;
    }

    /**
     * Factory method: creates the handPanel.
     * @param h hand
     * @param backgroundColor
     * @return JPanel containing cardlabels representing a hand
     */
    public JPanel getHandPanel(Hand h, Color backgroundColor) {
        JPanel handPanel = new JPanel();
        handPanel.setBackground(backgroundColor);
        handPanel.setLayout(new FlowLayout());
        JLabel prefix = new JLabel("Hand: ");
        prefix.setForeground(Color.WHITE);
        handPanel.add(prefix);
        handPanel.setPreferredSize(new Dimension(550,30));

        if (h != null) {
            for (Card c : h.getCards()) {
                handPanel.add(getCard(c));
            }
        }
        JLabel suffix = new JLabel(" Value: " + h.getHandValue());
        suffix.setForeground(Color.WHITE);
        handPanel.add(suffix);
        return handPanel;
    }



    /**
     * Factory method: creates the playerPanel.
     * @return JPanel containing infos about the player and 0, 1, 2, or 3 handPanels
     */
    public JPanel playerPanel(Player p) {
        JPanel playerPanel = new JPanel();

        playerPanel.setBackground(p.hasLost() ? new Color(109, 3, 50) : green);
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
        playerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerPanel.add(createLabel(p.name + " with chips: " + p.getChips(), Color.white));

        if (p.equals(m_so.getCurrentPlayer()) && !m_so.dealersTurn()) {
            playerPanel.setBorder(BorderFactory.createLineBorder(new Color( 255, 205, 0), 7));
        } else {
            playerPanel.setBorder(BorderFactory.createLineBorder(Color.white, 1));
        }
        JPanel handPanel = new JPanel();
        // playerPanel.add(handPanel);
        for (Hand h : p.getHands()) {
            handPanel = getHandPanel(h, green);
            if (h.equals(p.getActiveHand()) && p.equals(m_so.getCurrentPlayer()) && !m_so.dealersTurn()) {
                handPanel.setBorder(BorderFactory.createLineBorder(new Color(224, 36, 70), 2));
            }
            playerPanel.add(handPanel);
        }

        return playerPanel;
    }

    /**
     * Factory method: creates the dealerPanel.
     * @return JPanel containing infos about the deaker and 0 or 1 handPanel
     */
    public JPanel dealerPanel(Dealer dealer) {
        JPanel dealerPanel = new JPanel();
        dealerPanel.setBackground(red);
        dealerPanel.setPreferredSize(new Dimension(440, 150));
        dealerPanel.setLayout(new BoxLayout(dealerPanel, BoxLayout.PAGE_AXIS));
        dealerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (m_so.dealersTurn()) {
            dealerPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 250, 0), 4));
        }else{
            dealerPanel.setBorder(BorderFactory.createLineBorder(Color.white, 1));
        }
        dealerPanel.add(createLabel("Dealer", Color.WHITE));
        if (dealer.getActiveHand() != null) {
            dealerPanel.add(getHandPanel(dealer.getActiveHand(), red));
        }
        return dealerPanel;
    }

    public void showGameBoard(Arena arena, boolean alignToMain) {
        this.setVisible(true);
        if (alignToMain) {
            // place window with game board below the main window
            int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
            int y = arena.m_xab.getLocation().y;
            if (arena.m_ArenaFrame != null) {
                x = arena.m_ArenaFrame.getX();
                y = arena.m_ArenaFrame.getY() + arena.m_ArenaFrame.getHeight() + 1;
                this.setSize(1200, 600);
                this.setMinimumSize(new Dimension(1050, 580));
            }
            this.setLocation(x, y);
        }
    }


    /**
     * Factory method: creates a "headline JLabel" used to display player- and dealer- names.
     * @param content of the label
     * @param c foreground color
     * @return JLabel
     */
    public JLabel createLabel(String content, Color c) {
        JLabel result = new JLabel(content);
        result.setForeground(c);
        result.setAlignmentX(Component.CENTER_ALIGNMENT);
        result.setAlignmentY(Component.TOP_ALIGNMENT);
        result.setPreferredSize(new Dimension(450, 35));
        result.setFont(result.getFont().deriveFont((float) (((float)Types.GUI_HELPFONTSIZE) * 1.2)));
        return result;
    }

    public void toFront() {
        super.setState(JFrame.NORMAL); // if window is iconified, display it normally
        super.toFront();
    }

    public void destroy() {
        this.setVisible(false);
        this.dispose();
    }

}
