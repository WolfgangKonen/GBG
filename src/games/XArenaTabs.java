package games;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tools.Types;

/**
 *  The GUI element (JTabbedPane) holding the parameter tabs 
 */
public class XArenaTabs extends JFrame 
{
	JTabbedPane outer;
	JTabbedPane[] tp;
	int opIndex = 6;		// index of tab "Other pars"
	
	public XArenaTabs(Arena arena) {
		super(arena.getGameName()+" Parameters");
		//addWindowListener(new WindowClosingAdapter(true));

		int numPlayer = arena.gb.getDefaultStartState().getNumPlayers();
		tp = new JTabbedPane[numPlayer];
		outer = new JTabbedPane();
		
		for (int i=0; i<numPlayer; i++) {
			tp[i] = new JTabbedPane();
			tp[i].addTab("TD pars", arena.m_xab.tdPar[i].getPanel());					// 0
			tp[i].addTab("NT pars", arena.m_xab.ntPar[i].getPanel());					// 1
			tp[i].addTab("MaxN pars", arena.m_xab.maxnParams[i].getPanel());			// 2		
			tp[i].addTab("MC pars", arena.m_xab.mcParams[i].getPanel());    			// 3
			tp[i].addTab("MCTS pars", arena.m_xab.mctsParams[i].getPanel());			// 4
			tp[i].addTab("MCTSE pars", arena.m_xab.mctseParams[i].getPanel()); 			// 5
			if (arena.getGameName().equals("Othello")) {
				tp[i].addTab("Edax pars", arena.m_xab.edParams[i].getPanel());			// 6
				tp[i].setToolTipTextAt(6, "Edax (Othello-specific)");
				tp[i].addTab("Other pars", arena.m_xab.oPar[i].getPanel());				// 7
				opIndex=7;
			} else {
				tp[i].addTab("Other pars", arena.m_xab.oPar[i].getPanel());				// 6
				opIndex=6;
			}
			tp[i].setSize(getMinimumSize());
			tp[i].setEnabledAt(i, true); 			// do we need this?
			tp[i].setToolTipTextAt(0, "Temporal Difference & Sarsa");
			tp[i].setToolTipTextAt(1, "N-tuple & Temporal Coherence");
			tp[i].setToolTipTextAt(2, "Max-N & Expectimax-N");
			tp[i].setToolTipTextAt(3, "Monte Carlo");
			tp[i].setToolTipTextAt(4, "Monte Carlo Tree Search");
			tp[i].setToolTipTextAt(5, "MCTS-Expectimax");
			tp[i].setToolTipTextAt(opIndex, "Evaluator, Wrapper & General Training");


			String str = (numPlayer==2) ? Types.GUI_2PLAYER_NAME[i] : Types.GUI_PLAYER_NAME[i];
			outer.addTab(str, tp[i]);
			if (!arena.hasTrainRights()) {
				arena.m_xab.tdPar[i].enableAll(false);				
				arena.m_xab.ntPar[i].enableAll(false);				
			}
		} // for (i9
		getContentPane().add(outer, BorderLayout.CENTER);
	}

	// never used??
	class NextTabActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			int tab = tp[0].getSelectedIndex();
			tab = (tab >= tp[0].getTabCount() - 1 ? 0 : tab + 1);
			tp[0].setSelectedIndex(tab);
			((JPanel)tp[0].getSelectedComponent()).requestDefaultFocus();
		}
	}

	public void setEnabledAt(int k) {
		tp[k].setEnabledAt(k, true);
	}
	
	/**
	 * 
	 * @param arena	the arena
	 * @param isVisible	whether to make the tabs window visible
	 * @param n			number of selected agent
	 * @param selectedAgent	name of selected agent
	 */
	public void showParamTabs(Arena arena,boolean isVisible,int n, String selectedAgent) {
		arena.m_tabs.setVisible(isVisible);
		arena.m_tabs.setState(Frame.NORMAL);	// if window is iconified, display it normally
		arena.m_tabs.toFront();
		// place window TD params on the right side of the main window
		int x = arena.m_xab.getX() + arena.m_xab.getWidth() + 8;
		int y = arena.m_xab.getLocation().y;
		if (arena.m_LaunchFrame!=null) {
			x = arena.m_LaunchFrame.getX() + arena.m_LaunchFrame.getWidth() + 1;
			y = arena.m_LaunchFrame.getY();
		}
		arena.m_tabs.setLocation(x,y);
		arena.m_tabs.setSize(Types.GUI_PARAMTABS_WIDTH,Types.GUI_PARAMTABS_HEIGHT);	
		outer.setSelectedIndex(n);
		tp[n].setSelectedIndex(0);
		if (selectedAgent.equals("TDS")) tp[n].setSelectedIndex(0);
//		if (selectedAgent.equals("TD-Ntuple")) tp[n].setSelectedIndex(1);
		if (selectedAgent.equals("TD-Ntuple-2")) tp[n].setSelectedIndex(1);
		if (selectedAgent.equals("TD-Ntuple-3")) tp[n].setSelectedIndex(1);
		if (selectedAgent.equals("Sarsa")) tp[n].setSelectedIndex(1);
//		if (selectedAgent.equals("Sarsa-2")) tp[n].setSelectedIndex(1);
		if (selectedAgent.equals("Minimax")) tp[n].setSelectedIndex(2);
		if (selectedAgent.equals("Max-N")) tp[n].setSelectedIndex(2);
		if (selectedAgent.equals("Expectimax-N")) tp[n].setSelectedIndex(2);
		if (selectedAgent.equals("MC")) tp[n].setSelectedIndex(3);
		if (selectedAgent.equals("MC-N")) tp[n].setSelectedIndex(3);
//		if (selectedAgent.equals("MCTS0")) tp[n].setSelectedIndex(4);
		if (selectedAgent.equals("MCTS")) tp[n].setSelectedIndex(4);
		if (selectedAgent.equals("MCTS Expectimax")) tp[n].setSelectedIndex(5);
		if (selectedAgent.equals("Random")) tp[n].setSelectedIndex(opIndex);	// OtherParams
		if (selectedAgent.equals("Human")) tp[n].setSelectedIndex(opIndex);		// OtherParams
		if (selectedAgent.equals("AlphaBeta")) tp[n].setSelectedIndex(opIndex);	// OtherParams
		if (selectedAgent.equals("Edax")) tp[n].setSelectedIndex(6);		// EdaxParams
		if (selectedAgent.equals("Edax2")) tp[n].setSelectedIndex(6);		// EdaxParams		
	}

}
