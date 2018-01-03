package params;

import java.io.Serializable;

import controllers.MC.MCAgentConfig;
import controllers.MC.MCAgentN;

/**
 * MC (Monte Carlo) parameters for board games.<p>
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

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public ParMC() {	}
    
    public ParMC(ParMC tp) {
		this.numIters = tp.getNumIter();
		this.numAgents = tp.getNumAgents();
		this.rolloutDepth = tp.getRolloutDepth();
		this.calcCertainty = tp.getCalcCertainty();
    }
    
    public ParMC(MCParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(MCParams tp) {
		this.numIters = tp.getNumIter();
		this.numAgents = tp.getNumAgents();
		this.rolloutDepth = tp.getRolloutDepth();
		this.calcCertainty = tp.getCalcCertainty();
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

	public void setIterations(int numIters) {
		this.numIters = numIters;
	}
	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
	}
	public void setRolloutDepth(int rolloutDepth) {
		this.rolloutDepth = rolloutDepth;
	}
	public void setCalcCertainty(boolean calcCertainty) {
		this.calcCertainty = calcCertainty;
	}

}
