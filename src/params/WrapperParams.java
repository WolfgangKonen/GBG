package params;

import java.awt.*;
import java.io.Serial;

import javax.swing.*;

import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.MaxN2Wrapper;

/**
 * This class realizes <b>wrapper parameter</b> settings for board games.
 * <p>
 * These parameters and their [defaults] are:
 * <ul>
 * <li><b>Wrapper type</b>: [0] 0: none, 1: (Expecti)Max-N wrapper, 2: MCTS(E)Wrapper
 * <li><b>Wrapper nPly</b>: [1] n for (Expecti)Max-N wrapper: wrap the agent with an (Expecti)Max-N wrapper with
 * n plies of look-ahead. CAUTION: n &gt; 5 can dramatically slow down computation.
 * </ul>
 * The following parameters are only for {@link MCTSWrapperAgent}:
 * <ul>
 * <li><b>Wrapper MCTS</b>: [100] i for MCTSWrapper: wrap the agent with a {@link MCTSWrapperAgent} with i iterations.
 * <li><b>PUCT for Wrapper MCTS</b>: [1] PUCT parameter from [0,1] for {@link MCTSWrapperAgent}
 * <li><b>Depth for Wrapper MCTS</b>: [100] Depth parameter for {@link MCTSWrapperAgent}
 * <li><b>Exploration Mode</b>: [0] 0: none, 1: proportional to visit counts, 2: epsilon-greedy, only for {@link MCTSWrapperAgent}
 * <li><b>epsilon init</b>: [0.2] initial epsilon, only for Exploration Mode = 2
 * <li><b>epsilon final</b>: [0.1] final epsilon, only for Exploration Mode = 2
 * <li><b>USESOFTMAX</b>: [true] <br>
 *      A switch for {@link PlayAgentApproximator}:
 *      <ul>
 *          <li> If false (recommended setting for RubiksCube 2x2x2), do not use softmax squashing for move probabilities.
 *          <li> If true (recommended setting for Othello, ConnectFour, RubiksCube 3x3x3), use softmax squashing.
 *      </ul>
 * <li><b>USELASTMCTS</b>: [true] <br>
 *      A switch for {@link MCTSWrapperAgent}:
 *      <ul>
 *          <li>  If false (recommended setting for ConnectFour), force tree re-build in every call.
 *          <li>  If true (recommended setting for Othello, RubiksCube), re-use the tree (i.e. as in JS's master code) in
 *                  subsequent calls during one episode.
 *      </ul>
 * </ul>
 *
 * @see MCTSWrapperAgent
 * @see MaxN2Wrapper
 * @see ParWrapper
 */
public class WrapperParams extends Frame {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String[] wrapperList = {"None","MaxNWrapper","MCTSWrapper"};
    private final String[] exploModeList = {"None","proportional","eps-greedy"};
    JLabel wrapper_L;
    public JComboBox<String> choiceWrapper=new JComboBox<>(wrapperList);
    JLabel notyetready_L;
    JLabel wNply_L;
    JLabel wMCTSiter_L;
    JLabel wMCTSpUCT_L;
    JLabel wMCTSdepth_L;
    JLabel wMCTSexplorationMode_L;
    JLabel wMCTSepsInit_L;
    JLabel wMCTSepsFinal_L;
    JLabel wMCTSuseSoft_L;
    JLabel wMCTSuseLast_L;
    public JTextField wNply_T;
    public JTextField wMCTSiter_T;
    public JTextField wMCTSpUCT_T;
    public JTextField wMCTSdepth_T;
    public JComboBox<String> choiceExplorationMode=new JComboBox<>(exploModeList);
    public JTextField wMCTSepsInit_T;
    public JTextField wMCTSepsFinal_T;
    public Checkbox wMCTSuseSoftMax;
    public Checkbox wMCTSuseLastMCTS;

    Button ok;
    JPanel wPanel;
    WrapperParams m_par;
    //Arena m_arena;

    public WrapperParams(/*Arena m_arena*/) {
        super("Wrapper Parameter");

        //this.m_arena = m_arena;

        notyetready_L = new JLabel("NOT YET READY!");
        wrapper_L = new JLabel("Wrapper type");
        wNply_L = new JLabel("Wrapper nPly");
        wMCTSiter_L = new JLabel("Wrapper MCTS");
        wMCTSpUCT_L = new JLabel("PUCT for WrapM");
        wMCTSdepth_L = new JLabel("Depth for WrapM");
        wMCTSexplorationMode_L = new JLabel("Exploration Mode");
        wMCTSepsInit_L = new JLabel("epsilon init");
        wMCTSepsFinal_L = new JLabel("epsilon final");
        wMCTSuseSoft_L = new JLabel("USESOFTMAX");
        wMCTSuseLast_L = new JLabel("USELASTMCTS");
        wNply_T = new JTextField("0"); 		//
        wMCTSiter_T = new JTextField("0"); 		//
        wMCTSpUCT_T = new JTextField("1"); 		//
        wMCTSdepth_T = new JTextField("-1"); 		//
        wMCTSepsInit_T = new JTextField("0.1"); 		//
        wMCTSepsFinal_T = new JTextField("0.1"); 		//
        wMCTSuseSoftMax = new Checkbox("", false);
        wMCTSuseLastMCTS = new Checkbox("", false);
        ok = new Button("OK");
        m_par = this;
        wPanel = new JPanel();  // put the inner buttons into panel oPanel. This panel
                                // can be handed over to a tab of a JTabbedPane
                                // (see class XArenaTabs)

        choiceWrapper.setToolTipText("<html>0: None <br>1: MCTS Wrapper <br>2: MaxN Wrapper<\\html>");
        wNply_L.setToolTipText(
                "<html>Wrapper n-ply look ahead <br>(for play, compete, eval). <br>CAUTION: Numbers >5 can take long!</html>");
        wMCTSiter_L.setToolTipText(
                "<html>Wrapper MCTS iterations <br>(for play, compete, eval tasks)</html>");
        wMCTSpUCT_L.setToolTipText("PUCT value for MCTS Wrapper");
        wMCTSdepth_L.setToolTipText("max depth value for MCTS Wrapper. -1: no max depth");
        wMCTSexplorationMode_L.setToolTipText(
                "<html>Wrapper MCTS exploration mode (EXPLO_MODE)</html>");
        wMCTSepsInit_L.setToolTipText("initial random move rate for EXPLO_MODE==2");
        wMCTSepsFinal_L.setToolTipText("final random move rate for EXPLO_MODE==2");
        notyetready_L.setToolTipText("<html><b>Wrapper pars is not yet integrated!</b></html>");


        // the following lambda's, where e is an ActionEvent, are a simpler replacement for anonymous action listeners:

        ok.addActionListener( e -> m_par.setVisible(false) );

        choiceWrapper.addActionListener( e -> { enableWrapMCTSPart(); enableWrapNplyPart(); } );

        choiceExplorationMode.addActionListener( e -> enableWrapMCTSepsilonPart() );

        setLayout(new BorderLayout(10, 0)); // rows,columns,hgap,vgap
        wPanel.setLayout(new GridLayout(0, 4, 10, 10));

        wPanel.add(wrapper_L);
        wPanel.add(choiceWrapper);

        wPanel.add(notyetready_L);
        //wPanel.add(new Canvas());
        wPanel.add(new Canvas());

        wPanel.add(wNply_L);
        wPanel.add(wNply_T);

        wPanel.add(wMCTSiter_L);
        wPanel.add(wMCTSiter_T);

        wPanel.add(wMCTSpUCT_L);
        wPanel.add(wMCTSpUCT_T);

        wPanel.add(wMCTSdepth_L);
        wPanel.add(wMCTSdepth_T);

        wPanel.add(wMCTSuseSoft_L);
        wPanel.add(wMCTSuseSoftMax);
        wPanel.add(wMCTSuseLast_L);
        wPanel.add(wMCTSuseLastMCTS);

        wPanel.add(wMCTSexplorationMode_L);
        wPanel.add(choiceExplorationMode);
        wPanel.add(new Canvas());
        wPanel.add(new Canvas());

        wPanel.add(wMCTSepsInit_L);
        wPanel.add(wMCTSepsInit_T);
        wPanel.add(wMCTSepsFinal_L);
        wPanel.add(wMCTSepsFinal_T);

        add(wPanel, BorderLayout.CENTER);
        add(ok, BorderLayout.SOUTH);

        pack();
        setVisible(false);

        enableWrapNplyPart();
        enableWrapMCTSPart();

    } // constructor WrapperParams()

    private void enableWrapNplyPart() {
        boolean enable = (this.getWrapperMode()==1);
        wNply_L.setEnabled(enable);
        wNply_T.setEnabled(enable);
    }

    private void enableWrapMCTSPart() {
        boolean enable = (this.getWrapperMode()==2);
        wMCTSiter_L.setEnabled(enable);
        wMCTSiter_T.setEnabled(enable);
        wMCTSdepth_L.setEnabled(enable);
        wMCTSdepth_T.setEnabled(enable);
        wMCTSpUCT_L.setEnabled(enable);
        wMCTSpUCT_T.setEnabled(enable);
        wMCTSexplorationMode_L.setEnabled(enable);
        choiceExplorationMode.setEnabled(enable);
        wMCTSuseSoft_L.setEnabled(enable);
        wMCTSuseSoftMax.setEnabled(enable);
        wMCTSuseLast_L.setEnabled(enable);
        wMCTSuseLastMCTS.setEnabled(enable);

        enableWrapMCTSepsilonPart();
    }

    private void enableWrapMCTSepsilonPart() {
        boolean enable = (this.getWrapperMCTS_ExplorationMode()==2);
        wMCTSepsInit_L.setEnabled(enable);
        wMCTSepsInit_T.setEnabled(enable);
        wMCTSepsFinal_L.setEnabled(enable);
        wMCTSepsFinal_T.setEnabled(enable);
    }

    public JPanel getPanel() {
        return wPanel;
    }

    public int getWrapperMode() {
        return choiceWrapper.getSelectedIndex();    // 0: none, 1: proportional, 2: eps-greedy
    }

    public int getWrapperNPly() { return Integer.parseInt(wNply_T.getText()); }

    public int getWrapperMCTSIterations() {
        return Integer.parseInt(wMCTSiter_T.getText());
    }

    public double getWrapperMCTS_PUCT() {
        return Double.parseDouble(wMCTSpUCT_T.getText());
    }

    public int getWrapperMCTS_depth() {
        return Integer.parseInt(wMCTSdepth_T.getText());
    }

    public int getWrapperMCTS_ExplorationMode() {
        return choiceExplorationMode.getSelectedIndex();
    }

    public double getWrapperMCTS_epsInit() {
        return Double.parseDouble(wMCTSepsInit_T.getText());
    }

    public double getWrapperMCTS_epsFinal() {
        return Double.parseDouble(wMCTSepsFinal_T.getText());
    }

    public boolean getUseSoftMax() {
        return wMCTSuseSoftMax.getState();
    }

    public boolean getUseLastMCTS() { return wMCTSuseLastMCTS.getState(); }

    public void setWrapperMode(int wrapperMode) {
        choiceWrapper.setSelectedIndex(wrapperMode);
    }

    public void setWrapperNPly(int value) {
        wNply_T.setText(value + "");
    }

    public void setWrapperMCTSIterations(final int value) {
        wMCTSiter_T.setText(value + "");
    }

    public void setWrapperMCTS_PUCT(final double value) {
        wMCTSpUCT_T.setText(value + "");
    }

    public void setWrapperMCTS_depth(final int value) {
        wMCTSdepth_T.setText(value + "");
    }

    public void setWrapperMCTS_ExplorationMode(int exploMode) {
        choiceExplorationMode.setSelectedIndex(exploMode);
    }

    public void setWrapperMCTS_epsInit(final double value) {
        wMCTSepsInit_T.setText(value + "");
    }

    public void setWrapperMCTS_epsFinal(final double value) {
        wMCTSepsFinal_T.setText(value + "");
    }

    public void setUseSoftMax(boolean bUseSoft) {
        wMCTSuseSoftMax.setState(bUseSoft);
    }

    public void setUseLastMCTS(boolean bUseLast) {
        wMCTSuseLastMCTS.setState(bUseLast);
    }

    /**
     * Needed to restore the param tab with the parameters from a re-loaded
     * agent
     *
     * @param wp
     *            ParWrapper of the re-loaded agent
     */
    public void setFrom(ParWrapper wp) {
        this.setWrapperMode(wp.getWrapperMode());
        this.setWrapperNPly(wp.getWrapperNPly());
        this.setWrapperMCTSIterations(wp.getWrapperMCTS_iterations());
        this.setWrapperMCTS_PUCT(wp.getWrapperMCTS_PUCT());
        this.setWrapperMCTS_depth(wp.getWrapperMCTS_depth());
        this.setWrapperMCTS_ExplorationMode(wp.getWrapperMCTS_ExplorationMode());
        this.setWrapperMCTS_epsInit(wp.getWrapperMCTS_epsInit());
        this.setWrapperMCTS_epsFinal(wp.getWrapperMCTS_epsFinal());
        this.setUseSoftMax(wp.getUseSoftMax());
        this.setUseLastMCTS(wp.getUseLastMCTS());

        enableWrapNplyPart();
        enableWrapMCTSPart();
    }

} // class WrapperParams
