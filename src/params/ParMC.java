package params;

import java.io.Serial;
import java.io.Serializable;

import javax.swing.JPanel;

import controllers.MC.MCAgentConfig;
import controllers.MC.MCAgentN;

/**
 * Parameters for MC (Monte Carlo) agent {@link MCAgentN}.<p>
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
 * @see MCParams
 */
//--- this is commented out: ---
//* <li> <b>DOCALCCERTAINTY</b>  [false] Calculate certainty while playing
public class ParMC implements Serializable {
    private int numIters = MCAgentConfig.DEFAULT_ITERATIONS;
	private int numAgents = MCAgentConfig.DEFAULT_NUMBERAGENTS;
	private int rolloutDepth = MCAgentConfig.DEFAULT_ROLLOUTDEPTH;
    private boolean calcCertainty = MCAgentConfig.DOCALCCERTAINTY;
	private boolean stopOnRoundOver = MCAgentConfig.STOPONROUNDOVER;

    /**
     * This member is only constructed when the constructor {@link #ParMC(boolean) ParMC(boolean withUI)} 
     * is called with {@code withUI=true}. It holds the GUI for {@link ParMC}.
     */
    private transient MCParams mcparams = null;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	public ParMC() {	}
    
	public ParMC(boolean withUI) {
		if (withUI)
			mcparams = new MCParams();
	}
	
    public ParMC(ParMC tp) {
    	this.setFrom(tp);
    }
    
    public ParMC(MCParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(ParMC tp) {
		this.numIters = tp.getNumIter();
		this.numAgents = tp.getNumAgents();
		this.rolloutDepth = tp.getRolloutDepth();
		this.calcCertainty = tp.getCalcCertainty();
		this.stopOnRoundOver = tp.getStopOnRoundOver();
		
		if (mcparams!=null)
			mcparams.setFrom(this);
	}

	public void setFrom(MCParams tp) {
		this.numIters = tp.getNumIter();
		this.numAgents = tp.getNumAgents();
		this.rolloutDepth = tp.getRolloutDepth();
		this.calcCertainty = tp.getCalcCertainty();
		this.stopOnRoundOver = tp.getStopOnRoundOver();

		if (mcparams!=null)
			mcparams.setFrom(this);
	}

	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromMCParams() {
		if (mcparams!=null)
			this.setFrom(mcparams);
	}
	
    public JPanel getPanel() {
		if (mcparams!=null)		
			return mcparams.getPanel();
		return null;
    }

    public int getNumIter() {
		return numIters;
	}
    public int getNumAgents() {
		return numAgents;
	}
	public int getRolloutDepth() {
		return rolloutDepth;
	}
	public boolean getCalcCertainty() {
		return calcCertainty;
	}
	public boolean getStopOnRoundOver() {
		return stopOnRoundOver;
	}

	public void setIterations(int numIters) {
		this.numIters = numIters;
		if (mcparams!=null)
			mcparams.setNumIter(numIters);
	}
	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
		if (mcparams!=null)
			mcparams.setNumAgents(numAgents);
	}
	
	public void setRolloutDepth(int rolloutDepth) {
		this.rolloutDepth = rolloutDepth;
		if (mcparams!=null)
			mcparams.setRolloutDepth(rolloutDepth);
	}
	
	public void setCalcCertainty(boolean calcCertainty) {
		this.calcCertainty = calcCertainty;
		if (mcparams!=null)
			mcparams.setCalcCertainty(calcCertainty);
	}

	public void setStopOnRoundOver(boolean stopOnRoundOver) {
		this.stopOnRoundOver = stopOnRoundOver;
		if (mcparams!=null)
			mcparams.setStopOnRoundOver(stopOnRoundOver);
	}

}
