package params;

import java.io.Serial;
import java.io.Serializable;

import javax.swing.JPanel;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;

/**
 * Parameters for {@link MaxNAgent} and {@link ExpectimaxNAgent}
 * <ul>
 * <li> <b> Tree Depth</b>: [10] depth of search tree
 * <li> <b> Max-N Hashmap</b>: [true] (only MaxN) whether to hash already visited states or not
 * </ul>
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String, int)}.
 *
 * @see MaxNParams
 */
public class ParMaxN implements Serializable {

    public static int DEFAULT_MAXN_TREE_DEPTH = 10;
    public static boolean DEFAULT_MAXN_USE_HASHMAP = true;
	public static boolean DEFAULT_STOPONROUNDOVER = true;

    private int maxNTreeDepth = DEFAULT_MAXN_TREE_DEPTH;
    private boolean maxNUseHashmap = DEFAULT_MAXN_USE_HASHMAP;
	private boolean stopOnRoundOver = DEFAULT_STOPONROUNDOVER;

    /**
     * This member is only constructed when the constructor {@link #ParMaxN(boolean) ParMaxN(boolean withUI)}
     * called with {@code withUI=true}. It holds the GUI for {@link ParMaxN}.
     */
    private transient MaxNParams mnparams = null;

    /**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
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
		this.stopOnRoundOver = op.getStopOnRoundOver();

		if (mnparams!=null)
			mnparams.setFrom(this);
	}
	
	public void setFrom(MaxNParams op) {
		this.maxNTreeDepth = op.getMaxNDepth();
		this.maxNUseHashmap = op.getMaxNUseHashmap();
		this.stopOnRoundOver = op.getStopOnRoundOver();

		if (mnparams!=null)
			mnparams.setFrom(this);
	}
	
	public void enableHashmapPart(boolean enable) {
		if (mnparams!=null)
			mnparams.enableHashmapPart(enable);
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
	public boolean getStopOnRoundOver() {
		return stopOnRoundOver;
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

	public void setStopOnRoundOver(boolean stopOnRoundOver) {
		this.stopOnRoundOver = stopOnRoundOver;
		if (mnparams!=null)
			mnparams.setStopOnRoundOver(stopOnRoundOver);
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. If withUI, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName "Max-N" or "Expectimax-N" (see Types.GUI_AGENT_LIST)
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 * @param numPlayers currently not used
	 */
	public void setParamDefaults(String agentName, String gameName, int numPlayers) {
		switch (agentName) {
		case "MaxN": 
		case "Max-N":
		case "Expectimax-N":
			switch (gameName) {
				case "Nim", "Nim3P", "Sim" -> {
					this.setMaxNDepth(15);
					this.setMaxNUseHashmap(true);
				}
				case "RubiksCube" -> {
					this.setMaxNDepth(3);
					this.setMaxNUseHashmap(false);
					this.enableHashmapPart(false);
				}
				case "Poker","BlackJack" -> {
					this.setMaxNDepth(5);
					this.setMaxNUseHashmap(true);
				}
				case "EWN","EWS" -> {
					this.setMaxNUseHashmap(false);
					this.enableHashmapPart(false);
				}
				default -> {
					this.setMaxNDepth(DEFAULT_MAXN_TREE_DEPTH);
					this.setMaxNUseHashmap(DEFAULT_MAXN_USE_HASHMAP);
				}
			}
			break;
		}
		if (mnparams!=null)
			mnparams.setFrom(this);
	}	
}
