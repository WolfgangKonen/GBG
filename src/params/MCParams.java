package params;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;

import javax.swing.*;

import controllers.MC.MCAgentConfig;
import controllers.MC.MCAgentN;

/**
 * This class realizes the parameter settings (GUI tab) for {@link MCAgentN}.<p>
 *
 * These parameters and their [defaults] are: <ul>
 * <li> <b>Iterations</b>: 	    [1000]  number of iterations during MC search
 * <li> <b>Rollout Depth</b>: 	[20]    MC rollout depth
 * <li> <b>NumberAgents</b>:    [1]     number agents for Majority Vote
 * </ul>
 * The defaults are defined in {@link MCAgentConfig}.
 *
 * @see MCAgentN
 * @see MCAgentConfig
 * @see ParMC
 */
//--- this is commented out: ---
//* <li> <b>DOCALCCERTAINTY</b>  [false] Calculate Certainty while playing
public class MCParams extends Frame implements Serializable
{
    JLabel LIterations;
    JLabel LRolloutdepth;
    JLabel LNumberAgents;
    JLabel LStopOnRoundOver;
    public JTextField TIterations;
    public JTextField TRolloutdepth;
    public JTextField TNumberAgents;
    public JCheckBox CBCalcCertainty;
    public JCheckBox CBStopOnRoundOver;
    JPanel mPanel;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
    private static final long serialVersionUID = 1L;

   public MCParams() {
        super("MC Parameter");
        LIterations = new JLabel("Iterations");
        LRolloutdepth = new JLabel("Rollout Depth");
        LNumberAgents = new JLabel("Number Agents");
        LStopOnRoundOver = new JLabel("StopOnRoundOver");
        TIterations = new JTextField(""+MCAgentConfig.DEFAULT_ITERATIONS);
        TRolloutdepth = new JTextField(""+MCAgentConfig.DEFAULT_ROLLOUTDEPTH);
        TNumberAgents = new JTextField(""+MCAgentConfig.DEFAULT_NUMBERAGENTS);
        CBCalcCertainty = new JCheckBox("Calc Certainty", MCAgentConfig.DOCALCCERTAINTY);
        CBStopOnRoundOver = new JCheckBox("StopOnRoundOver", MCAgentConfig.STOPONROUNDOVER);
        mPanel = new JPanel();

        LIterations.setToolTipText("Number of iterations during MC search");
        LRolloutdepth.setToolTipText("MC rollout depth");
        LNumberAgents.setToolTipText("Number of agents for majority vote");

        setLayout(new BorderLayout(10,0));
        mPanel.setLayout(new GridLayout(0,2,10,10));

        JPanel iterPanel = new JPanel(new GridLayout(0,2,10,10));
        iterPanel.add(LIterations);
        iterPanel.add(TIterations);
        mPanel.add(iterPanel);
        JPanel rollPanel = new JPanel(new GridLayout(0,2,10,10));
        rollPanel.add(LRolloutdepth);
        rollPanel.add(TRolloutdepth);
        mPanel.add(rollPanel);

        JPanel numbPanel = new JPanel(new GridLayout(0,2,10,10));
        numbPanel.add(LNumberAgents);
        numbPanel.add(TNumberAgents);
        mPanel.add(numbPanel);
        mPanel.add(new Canvas());

        mPanel.add(CBStopOnRoundOver);
        // mPanel.add(CBCalcCertainty);
        mPanel.add(new Canvas());

        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        add(mPanel,BorderLayout.CENTER);

        pack();
        setVisible(false);
    }

    public JPanel getPanel() {
        return mPanel;
    }
    public int getNumIter() {
        return Integer.parseInt(TIterations.getText());
    }
    public int getRolloutDepth() {
        return Double.valueOf(TRolloutdepth.getText()).intValue();
    }
    public int getNumAgents() {
        return Integer.parseInt(TNumberAgents.getText());
    }
    public boolean getCalcCertainty() {
        return CBCalcCertainty.isSelected();
    }
    public boolean getStopOnRoundOver() {
        return CBStopOnRoundOver.isSelected();
    }

    public void setNumIter(int value) {
        TIterations.setText(value+"");
    }
    public void setRolloutDepth(double value) {
        TRolloutdepth.setText(value+"");
    }
    public void setNumAgents(int value) {
        TNumberAgents.setText(value+"");
    }
    public void setCalcCertainty(boolean value) {
        CBCalcCertainty.setSelected(value);
    }
    public void setStopOnRoundOver(boolean value) {
        CBStopOnRoundOver.setSelected(value);
    }

    /**
     * Needed to restore the param tab with the parameters from a re-loaded agent
     * @param tp  of the re-loaded agent
     */
    public void setFrom(MCParams tp) {
        setNumIter(tp.getNumIter());
        setRolloutDepth(tp.getRolloutDepth());
        setNumAgents(tp.getNumAgents());
        setCalcCertainty(tp.getCalcCertainty());
        setStopOnRoundOver(tp.getStopOnRoundOver());
    }
    /**
     * Needed to restore the param tab with the parameters from a re-loaded agent
     * @param tp  of the re-loaded agent
     */
    public void setFrom(ParMC tp) {
        setNumIter(tp.getNumIter());
        setRolloutDepth(tp.getRolloutDepth());
        setNumAgents(tp.getNumAgents());
        setCalcCertainty(tp.getCalcCertainty());
        setStopOnRoundOver(tp.getStopOnRoundOver());
    }
}