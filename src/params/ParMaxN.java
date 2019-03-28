package params;

import java.io.Serializable;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;
import controllers.MinimaxAgent;

/**
 * Parameters for {@link MinimaxAgent}, {@link MaxNAgent}, {@link ExpectimaxNAgent}
 * <ul>
 * <li> <b> Tree Depth</b>: [10] depth of search tree
 * <li> <b> Max-N Hashmap</b>: [true] (only MaxN and Minimax) whether to hash already visited states or not
 * </ul>
 *
 * @see MaxNParams
 */
public class ParMaxN implements Serializable {

    public static int DEFAULT_MAXN_TREE_DEPTH = 10;
    
    private int maxNTreeDepth = DEFAULT_MAXN_TREE_DEPTH;
    private boolean maxNUseHashmap = true;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;
	
	public ParMaxN() {	}
    
	public ParMaxN(ParMaxN op) {
		this.maxNTreeDepth = op.getMaxNDepth();
		this.maxNUseHashmap = op.useMaxNHashmap();	
	}
	
    public ParMaxN(MaxNParams op) { 
    	this.setFrom(op);
    }
    
	public void setFrom(MaxNParams op) {
		this.maxNTreeDepth = op.getMaxnDepth();
		this.maxNUseHashmap = op.useMaxNHashmap();	
	}
	
	public void setMaxNDepth(int treeDepth) {
		this.maxNTreeDepth = treeDepth;
	}

	public void setMaxNUseHashmap(boolean useHM) {
		this.maxNUseHashmap = useHM;
	}

	public int getMaxNDepth() {
		return maxNTreeDepth;
	}

	public boolean useMaxNHashmap() {
		return maxNUseHashmap;
	}

}
