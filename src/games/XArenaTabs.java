package games;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tools.Types;

public class XArenaTabs extends JFrame 
{
	JTabbedPane tp;
	
	public XArenaTabs(Arena arena) {
		super(arena.getGameName()+" Parameters");
		//addWindowListener(new WindowClosingAdapter(true));
		tp = new JTabbedPane();
//		for (int i = 0; i < 2; ++i) {
//	        JPanel panel = new JPanel();
//	         panel.add(new JLabel("Karte " + i));
//	        JButton next = new JButton("Weiter");
//	        next.addActionListener(new NextTabActionListener());
//	        panel.add(next);
//	        tp.addTab("Tab" + i, panel);
//		}

		tp.addTab("TD pars", arena.m_xab.tdPar.getPanel());			// 0
		tp.addTab("NT pars", arena.m_xab.ntPar.getPanel());			// 1	
		tp.addTab("MCTS pars", arena.m_xab.mcPar.getPanel());		// 2
		tp.addTab("Other pars", arena.m_xab.oPar.getPanel());		// 3
//		tp.addTab("RP pars", arena.m_xab.rpPar.getPanel());		// --1
//		tp.addTab("TC pars", arena.m_xab.tcPar.getPanel());		// --2
//		tp.addTab("CMA pars", arena.m_xab.cmaPar.getPanel());		// --4
		tp.setSize(getMinimumSize());
		tp.setEnabledAt(1, true); 
//		String s = tp.getTitleAt(1);
//		System.out.println("Title tab 5: " + s);
//		System.out.println("Index of 'Other pars' tab:" + tp.indexOfTab("Other pars"));
		
		getContentPane().add(tp, BorderLayout.CENTER);
	}

	class NextTabActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			int tab = tp.getSelectedIndex();
			tab = (tab >= tp.getTabCount() - 1 ? 0 : tab + 1);
			tp.setSelectedIndex(tab);
			((JPanel)tp.getSelectedComponent()).requestDefaultFocus();
		}
	}

	public void setEnabledAt(int k) {
		tp.setEnabledAt(k, true);
	}
	public void showParamTabs(Arena ticGame,boolean isVisible,String selectedAgent) {
		ticGame.m_tabs.setVisible(isVisible);
		// place window TD params on the right side of the main window
		int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
		int y = ticGame.m_xab.getLocation().y;
		if (ticGame.m_TicFrame!=null) {
			x = ticGame.m_TicFrame.getX() + ticGame.m_TicFrame.getWidth() + 1;
			y = ticGame.m_TicFrame.getY();
		}
		ticGame.m_tabs.setLocation(x,y);
		ticGame.m_tabs.setSize(Types.GUI_PARAMTABS_WIDTH,Types.GUI_PARAMTABS_HEIGHT);		
		tp.setSelectedIndex(0);
		if (selectedAgent.equals("TD-Ntuple")) tp.setSelectedIndex(1);
		if (selectedAgent.equals("MCTS")) tp.setSelectedIndex(2);
		if (selectedAgent.equals("Minimax")) tp.setSelectedIndex(3);
		//if (selectedAgent.equals("CMA-ES")) tp.setSelectedIndex(4);
	}

}
