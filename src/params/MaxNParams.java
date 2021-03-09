package params;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.Serial;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;

/**
 * This class realizes the parameter settings (GUI tab) for  
 * {@link MaxNAgent}, {@link ExpectimaxNAgent}.
 * These parameters and their [defaults] are: <ul>
 * <li> <b>tree depth</b>: 	[10] initial strength of learning parameter 
 * <li> <b>useHashMap</b>: 	[true] (only MaxN) whether to store calculated values in a hash map or not 
 * </ul> 
 * The defaults are defined in {@link ParMaxN}. 
 * 
 * @see ParMaxN
 * @see MaxNAgent
 */
public class MaxNParams extends Frame 
{
	@Serial
	private static final long serialVersionUID = 1L;

	JLabel maxnDepth_L;
	JLabel maxnUseHm_L;
	public JTextField maxnDepth_T;
	public JCheckBox maxnUseHmTrue;
	public JCheckBox CBStopOnRoundOver;

	JPanel mPanel;
	
	public MaxNParams() {
		super("MaxN Parameter");
		
		maxnDepth_L = new JLabel("Tree Depth");
		maxnDepth_T = new JTextField(ParMaxN.DEFAULT_MAXN_TREE_DEPTH+"");				// 
		maxnUseHm_L = new JLabel("MaxN Hashmap ");
		maxnUseHmTrue = new JCheckBox("use hashmap",ParMaxN.DEFAULT_MAXN_USE_HASHMAP);
		CBStopOnRoundOver = new JCheckBox("StopOnRoundOver", ParMaxN.DEFAULT_STOPONROUNDOVER);
		mPanel = new JPanel();		// put the inner buttons into panel mPanel. This panel
									// can be handed over to a tab of a JTabbedPane object
									// (see class XArenaTabs)
		
		maxnDepth_L.setToolTipText("Tree depth (for MaxN or ExpectimaxN)");
		maxnUseHm_L.setToolTipText("MaxN: use hashmap to save values of visited states");
		
		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		mPanel.setLayout(new GridLayout(0,2,10,10));

		JPanel iterPanel = new JPanel(new GridLayout(0,2,10,10));
		iterPanel.add(maxnDepth_L);
		iterPanel.add(maxnDepth_T);
		mPanel.add(iterPanel);
		mPanel.add(new Canvas());

		JPanel numbPanel = new JPanel(new GridLayout(0,2,10,10));
		numbPanel.add(maxnUseHm_L);
		numbPanel.add(maxnUseHmTrue);
		mPanel.add(numbPanel);
		mPanel.add(new Canvas());

		mPanel.add(CBStopOnRoundOver);
		mPanel.add(new Canvas());

		mPanel.add(new Canvas());
		mPanel.add(new Canvas());

		add(mPanel,BorderLayout.CENTER);
				
		pack();
		setVisible(false);
		
	} // constructor MaxNParams()	
	
	public JPanel getPanel() {
		return mPanel;
	}

	public void enableHashmapPart(boolean enable) {
		maxnUseHm_L.setEnabled(enable);
		maxnUseHmTrue.setEnabled(enable);
	}
	
	public int getMaxNDepth() {
		return Integer.parseInt(maxnDepth_T.getText());
	}
	public boolean getMaxNUseHashmap() {
		return maxnUseHmTrue.isSelected();
	}
	public boolean getStopOnRoundOver() {
		return CBStopOnRoundOver.isSelected();
	}

	public void setMaxNDepth(int value) {
		maxnDepth_T.setText(value+"");
	}
	public void setMaxNUseHashmap(boolean bval) {
		maxnUseHmTrue.setSelected(bval);
	}
	public void setStopOnRoundOver(boolean value) {
		CBStopOnRoundOver.setSelected(value);
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param mp  ParMaxN of the re-loaded agent
	 */
	public void setFrom(MaxNParams mp) {
		this.setMaxNDepth(mp.getMaxNDepth());
		this.setMaxNUseHashmap(mp.getMaxNUseHashmap());
		this.setStopOnRoundOver(mp.getStopOnRoundOver());
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param mp  ParMaxN of the re-loaded agent
	 */
	public void setFrom(ParMaxN mp) {
		this.setMaxNDepth(mp.getMaxNDepth());
		this.setMaxNUseHashmap(mp.getMaxNUseHashmap());
		setStopOnRoundOver(mp.getStopOnRoundOver());
	}

	// --- not needed anymore, we have ParMaxN.setParamDefaults ---
//	/**
//	 * Set sensible parameters for a specific agent and specific game. By "sensible
//	 * parameters" we mean parameter producing good results. Likewise, some parameter
//	 * choices may be enabled or disabled.
//	 *
//	 * @param agentName currently only "MaxN"
//	 * @param gameName the string from {@link games.StateObservation#getName()}
//	 * @param numPlayers number of players
//	 */
//	@Deprecated
//	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
//		switch (agentName) {
//		case "MaxN":
//		case "Max-N":
//			switch (gameName) {
//			case "Sim":
//				maxnDepth_T.setText("15");
//				maxnUseHmTrue.setSelected(true);
//				break;
//			}
//			break;
//		}
//	}

//	/**
//	 * @return	the {@link ParMaxN} representation of {@code this}
//	 */
//	public ParMaxN getParMaxN() {
//		return new ParMaxN(this);
//	}

} // class MaxNParams
