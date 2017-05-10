package params;

import java.awt.*;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
    private static final long serialVersionUID = 1L;
    JLabel LIterations;
    JLabel LDepth;
    JLabel LNumberAgents;
    public JTextField TIterations;
    public JTextField TDepth;
    public JTextField TNumberAgents;
    public Checkbox CBCalcCertainty;
    JPanel mPanel;

    public MCParams() {
        super("MC Parameter");
        LIterations = new JLabel("Iterations");
        LDepth = new JLabel("Depth");
        LNumberAgents = new JLabel("Number Agents (Majority Vote)");
        TIterations = new JTextField(""+MCAgentConfig.ITERATIONS);
        TDepth = new JTextField(""+MCAgentConfig.DEPTH);
        TNumberAgents = new JTextField(""+MCAgentConfig.NUMBERAGENTS);
        CBCalcCertainty = new Checkbox("Calc Certainty", MCAgentConfig.DOCALCCERTAINTY);
        mPanel = new JPanel();

        LIterations.setToolTipText("Number of iterations during MC search");
        LDepth.setToolTipText("MC tree depth");
        LNumberAgents.setToolTipText("Number of agents for majority vote");

        setLayout(new BorderLayout(10,0));
        mPanel.setLayout(new GridLayout(0,4,10,10));

        mPanel.add(LIterations);
        mPanel.add(TIterations);
        mPanel.add(LDepth);
        mPanel.add(TDepth);

        mPanel.add(LNumberAgents);
        mPanel.add(TNumberAgents);
        mPanel.add(new Canvas());
        mPanel.add(new Canvas());

        mPanel.add(new Canvas());
        mPanel.add(CBCalcCertainty);
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
    public int getIterations() {
        return Integer.valueOf(TIterations.getText()).intValue();
    }
    public int getDepth() {
        return Double.valueOf(TDepth.getText()).intValue();
    }
    public int getNumberAgents() {
        return Integer.valueOf(TNumberAgents.getText()).intValue();
    }
    public boolean getCalcCertainty() {
        return CBCalcCertainty.getState();
    }
    public void setIterations(int value) {
        TIterations.setText(value+"");
    }
    public void setDepth(double value) {
        TDepth.setText(value+"");
    }
    public void setNumberAgents(int value) {
        TNumberAgents.setText(value+"");
    }
    public void setCalcCertainty(boolean value) {
        CBCalcCertainty.setState(value);
    }

    /**
     * Needed to restore the param tab with the parameters from a re-loaded agent
     * @param tp  of the re-loaded agent
     */
    public void setFrom(MCParams tp) {
        setIterations(tp.getIterations());
        setDepth(tp.getDepth());
        setNumberAgents(tp.getNumberAgents());
        setCalcCertainty(tp.getCalcCertainty());
    }
}