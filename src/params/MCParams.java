package params;

import java.awt.*;
import java.io.Serializable;

import javax.swing.*;

import controllers.MC.MCAgentConfig;

/**
 * MC (Monte Carlo) parameters for board games.<p>
 *
 * These parameters and their [defaults] are: <ul>
 * <li> <b>Iterations</b>: 	    [1000]  number of iterations during MC search
 * <li> <b>Depth</b>: 	        [20]    MC tree depth
 * <li> <b>NumberAgents</b>:    [1]     Number Agents for Majority Vote
 * <li> <b>DOCALCCERTAINTY</b>  [false] Calculate Certainty while playing
 * </ul>
 * The defaults are defined in {@link MCAgentConfig}.
 *
 * @see controllers.MC.MCAgent
 */
public class MCParams extends Frame implements Serializable
{
    JLabel LIterations;
    JLabel LRolloutdepth;
    JLabel LNumberAgents;
    public JTextField TIterations;
    public JTextField TRolloutdepth;
    public JTextField TNumberAgents;
    public JCheckBox CBCalcCertainty;
    JPanel mPanel;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

   public MCParams() {
        super("MC Parameter");
        LIterations = new JLabel("Iterations");
        LRolloutdepth = new JLabel("Rolloutdepth");
        LNumberAgents = new JLabel("Number Agents (Majority Vote)");
        TIterations = new JTextField(""+MCAgentConfig.DEFAULT_ITERATIONS);
        TRolloutdepth = new JTextField(""+MCAgentConfig.DEFAULT_ROLLOUTDEPTH);
        TNumberAgents = new JTextField(""+MCAgentConfig.DEFAULT_NUMBERAGENTS);
        CBCalcCertainty = new JCheckBox("Calc Certainty", MCAgentConfig.DOCALCCERTAINTY);
        mPanel = new JPanel();

        LIterations.setToolTipText("Number of iterations during MC search");
        LRolloutdepth.setToolTipText("MC Rolloutdepth");
        LNumberAgents.setToolTipText("Number of agents for majority vote");

        setLayout(new BorderLayout(10,0));
        mPanel.setLayout(new GridLayout(0,4,10,10));

        mPanel.add(LIterations);
        mPanel.add(TIterations);
        mPanel.add(LRolloutdepth);
        mPanel.add(TRolloutdepth);

        mPanel.add(LNumberAgents);
        mPanel.add(TNumberAgents);
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        mPanel.add(new Canvas());
       // mPanel.add(CBCalcCertainty);
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        mPanel.add(new Canvas());
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        mPanel.add(new Canvas());
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        add(mPanel,BorderLayout.CENTER);
        //add(ok,BorderLayout.SOUTH);

        pack();
        setVisible(false);
    }

    public JPanel getPanel() {
        return mPanel;
    }
    public int getNumIter() {
        return Integer.valueOf(TIterations.getText()).intValue();
    }
    public int getRolloutDepth() {
        return Double.valueOf(TRolloutdepth.getText()).intValue();
    }
    public int getNumAgents() {
        return Integer.valueOf(TNumberAgents.getText()).intValue();
    }
    public boolean getCalcCertainty() {
        return CBCalcCertainty.isSelected();
    }
    public void setIterations(int value) {
        TIterations.setText(value+"");
    }
    public void setRolloutdepth(double value) {
        TRolloutdepth.setText(value+"");
    }
    public void setNumberAgents(int value) {
        TNumberAgents.setText(value+"");
    }
    public void setCalcCertainty(boolean value) {
        CBCalcCertainty.setSelected(value);
    }

    /**
     * Needed to restore the param tab with the parameters from a re-loaded agent
     * @param tp  of the re-loaded agent
     */
    public void setFrom(MCParams tp) {
        setIterations(tp.getNumIter());
        setRolloutdepth(tp.getRolloutDepth());
        setNumberAgents(tp.getNumAgents());
        setCalcCertainty(tp.getCalcCertainty());
    }
    /**
     * Needed to restore the param tab with the parameters from a re-loaded agent
     * @param tp  of the re-loaded agent
     */
    public void setFrom(ParMC tp) {
        setIterations(tp.getNumIter());
        setRolloutdepth(tp.getRolloutDepth());
        setNumberAgents(tp.getNumAgents());
        setCalcCertainty(tp.getCalcCertainty());
    }
}