package params;

import java.io.Serializable;

import controllers.MC.MCAgentConfig;

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

	public void setNumIter(int numIters) {
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
