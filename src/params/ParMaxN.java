package params;

import java.io.Serializable;

import javax.swing.JPanel;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;

/**
 * Parameters for {@link MaxNAgent}, {@link ExpectimaxNAgent}
 * <ul>
 * <li> <b> Tree Depth</b>: [10] depth of search tree
 * <li> <b> Max-N Hashmap</b>: [true] (only MaxN) whether to hash already visited states or not
 * </ul>
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 *
 * @see MaxNParams
 */
public class ParMaxN implements Serializable {

    public static int DEFAULT_MAXN_TREE_DEPTH = 10;
    public static boolean DEFAULT_MAXN_USE_HASHMAP = true;
    
    private int maxNTreeDepth = DEFAULT_MAXN_TREE_DEPTH;
    private boolean maxNUseHashmap = DEFAULT_MAXN_USE_HASHMAP;

    /**
     * This member is only constructed when the constructor {@link #ParTD(boolean) ParTD(boolean withUI)} 
     * called with {@code withUI=true}. It holds the GUI for {@link ParTD}.
     */
    private transient MaxNParams mnparams = null;

    /**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;
	
	public ParMaxN() {	}
    
	public ParMaxN(boolean withUI) {
		if (withUI)
			mnparams = new MaxNParams();
	}
	
	public ParMaxN(ParMaxN op) {
    	this.setFrom(op);
	}
	
    public ParMaxN(MaxNParams op) { 
    	this.setFrom(op);
    }
    
	public void setFrom(ParMaxN op) {
		this.maxNTreeDepth = op.getMaxNDepth();
		this.maxNUseHashmap = op.getMaxNUseHashmap();	
		
		if (mnparams!=null)
			mnparams.setFrom(this);
	}
	
	public void setFrom(MaxNParams op) {
		this.maxNTreeDepth = op.getMaxNDepth();
		this.maxNUseHashmap = op.getMaxNUseHashmap();	
		
		if (mnparams!=null)
			mnparams.setFrom(this);
	}
	
	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromMaxNParams() {
		if (mnparams!=null)
			this.setFrom(mnparams);
	}
	
	public JPanel getPanel() {
		if (mnparams!=null)		
			return mnparams.getPanel();
		return null;
	}

	public int getMaxNDepth() {
		return maxNTreeDepth;
	}

	public boolean getMaxNUseHashmap() {
		return maxNUseHashmap;
	}

	public void setMaxNDepth(int treeDepth) {
		this.maxNTreeDepth = treeDepth;
		if (mnparams!=null)
			mnparams.setMaxNDepth(treeDepth);
	}

	public void setMaxNUseHashmap(boolean useHM) {
		this.maxNUseHashmap = useHM;
		if (mnparams!=null)
			mnparams.setMaxNUseHashmap(useHM);
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. If withUI, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName currently only "MaxN" 
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 * @param numPlayers
	 */
	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
		switch (agentName) {
		case "MaxN": 
		case "Max-N": 
			switch (gameName) {
			case "Sim": 
				this.setMaxNDepth(15);		
				this.setMaxNUseHashmap(true);
				break;
			}
			break;
		}
		if (mnparams!=null)
			mnparams.setFrom(this);
	}	
}
