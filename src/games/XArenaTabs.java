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
	
	public XArenaTabs(Arena arena) {
		super(arena.getGameName()+" Parameters");
		//addWindowListener(new WindowClosingAdapter(true));

		int numPlayer = arena.gb.getDefaultStartState().getNumPlayers();
		tp = new JTabbedPane[numPlayer];
		outer = new JTabbedPane();
		
		for (int i=0; i<1 /*numPlayer*/; i++) {
			tp[i] = new JTabbedPane();
			tp[i].addTab("TD pars", arena.m_xab.tdPar.getPanel());					// 0
			tp[i].addTab("NT pars", arena.m_xab.ntPar.getPanel());					// 1
			tp[i].addTab("MaxN pars", arena.m_xab.maxnParams.getPanel());			// 2		
			tp[i].addTab("MC pars", arena.m_xab.mcParams.getPanel());    			// 3
			tp[i].addTab("MCTS pars", arena.m_xab.mctsParams.getPanel());			// 4
			tp[i].addTab("MCTSE pars", arena.m_xab.mctsExpectimaxParams.getPanel());// 5
			tp[i].addTab("Other pars", arena.m_xab.oPar.getPanel());				// 6
			tp[i].setSize(getMinimumSize());
			tp[i].setEnabledAt(i, true); 			// do we need this?
			// was before: tp.setEnabledAt(1, true);

//			String s = tp[i].getTitleAt(1);
//			System.out.println("Title tab 5: " + s);
//			System.out.println("Index of 'Other pars' tab:" + tp[i].indexOfTab("Other pars"));

			getContentPane().add(tp[i], BorderLayout.CENTER);
			//outer.addTab(""+i, tp[i]);
		}
		//getContentPane().add(outer, BorderLayout.CENTER);
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
		// was before: tp.setEnabledAt(k, true);
	}
	
	/**
	 * 
	 * @param ticGame	the arena
	 * @param isVisible	whether to make the tabs window visible
	 * @param n			number of selected agent
	 * @param selectedAgent	name of selected agent
	 */
	public void showParamTabs(Arena ticGame,boolean isVisible,int n, String selectedAgent) {
		ticGame.m_tabs.setVisible(isVisible);
		ticGame.m_tabs.setState(Frame.NORMAL);	// if window is iconified, display it normally
		ticGame.m_tabs.toFront();
		// place window TD params on the right side of the main window
		int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
		int y = ticGame.m_xab.getLocation().y;
		if (ticGame.m_TicFrame!=null) {
			x = ticGame.m_TicFrame.getX() + ticGame.m_TicFrame.getWidth() + 1;
			y = ticGame.m_TicFrame.getY();
		}
		ticGame.m_tabs.setLocation(x,y);
		ticGame.m_tabs.setSize(Types.GUI_PARAMTABS_WIDTH,Types.GUI_PARAMTABS_HEIGHT);	
		tp[n].setSelectedIndex(0);
		if (selectedAgent.equals("TDS")) tp[n].setSelectedIndex(0);
		if (selectedAgent.equals("TD-Ntuple")) tp[n].setSelectedIndex(1);
		if (selectedAgent.equals("TD-Ntuple-2")) tp[n].setSelectedIndex(1);
		if (selectedAgent.equals("Minimax")) tp[n].setSelectedIndex(2);
		if (selectedAgent.equals("Max-N")) tp[n].setSelectedIndex(2);
		if (selectedAgent.equals("Expectimax-N")) tp[n].setSelectedIndex(2);
		if (selectedAgent.equals("MC")) tp[n].setSelectedIndex(3);
		if (selectedAgent.equals("MCTS")) tp[n].setSelectedIndex(4);
		if (selectedAgent.equals("MCTS Expectimax")) tp[n].setSelectedIndex(5);
	}

}
