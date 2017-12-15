package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;
import controllers.MinimaxAgent;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import games.Feature;

/**
 * This class realizes the parameter settings (GUI tab) for for {@link MinimaxAgent}, 
 * {@link MaxNAgent}, {@link ExpectimaxNAgent}.
 * These parameters and their [defaults] are: <ul>
 * <li> <b>tree depth</b>: 	[10] initial strength of learning parameter 
 * <li> <b>useHashMap</b>: 	[true] (only Minimax) whether to store calculated values in a hash map or not 
 * </ul> 
 * 
 * @see ParMaxN
 * @see MaxNAgent
 * @see games.XArenaButtons
 */
public class MaxNParams extends Frame 
{
	private static final long serialVersionUID = 1L;

	JLabel maxnDepth_L;
	JLabel miniUseHm_L;
	public JTextField maxnDepth_T;
	public Checkbox miniUseHmTrue;

	Button ok;
	JPanel oPanel;
	MaxNParams m_par;
	
	public MaxNParams() {
		super("MaxN Parameter");
		
		maxnDepth_T = new JTextField("10");				// 
		maxnDepth_L = new JLabel("Tree Depth");
		miniUseHm_L = new JLabel("Minimax Hash ");
		miniUseHmTrue = new Checkbox("use hashmap",true);
		ok = new Button("OK");
		m_par = this;
		oPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class TicTacToeTabs)
		
		maxnDepth_L.setToolTipText("Tree depth (for MaxN, Minimax or ExpectimaxN)");
		miniUseHm_L.setToolTipText("Minimax: use hashmap to save values of visited states");
		
		ok.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						m_par.setVisible(false);
					}
				}					
		);

		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		oPanel.setLayout(new GridLayout(0,4,10,10));		
		
		oPanel.add(maxnDepth_L);
		oPanel.add(maxnDepth_T);
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		oPanel.add(miniUseHm_L);
		oPanel.add(miniUseHmTrue);
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

//		oPanel.add(new Canvas());
//		oPanel.add(new Canvas());
//		oPanel.add(new Canvas());
//		oPanel.add(new Canvas());

		add(oPanel,BorderLayout.CENTER);
		add(ok,BorderLayout.SOUTH);
				
		pack();
		setVisible(false);
		
	} // constructor OtherParams()	
	
	public JPanel getPanel() {
		return oPanel;
	}

	public void setMaxnDepth(int value) {
		maxnDepth_T.setText(value+"");
	}
	
	public int getMaxnDepth() {
		return Integer.valueOf(maxnDepth_T.getText()).intValue();
	}

	public boolean useMinimaxHashmap() {
		return miniUseHmTrue.getState();
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param mp  ParMaxN of the re-loaded agent
	 */
	public void setFrom(ParMaxN mp) {
		this.setMaxnDepth(mp.getMaxnDepth());
		this.miniUseHmTrue.setState(mp.useMinimaxHashmap());
	}

} // class MaxNParams
