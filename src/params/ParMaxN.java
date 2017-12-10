package params;

import java.io.Serializable;

import controllers.ExpectimaxNAgent;
import controllers.MaxNAgent;
import controllers.MinimaxAgent;

/**
 * Parameter for {@link MinimaxAgent}, {@link MaxNAgent}, {@link ExpectiimaxNAgent}
 *
 */
public class ParMaxN implements Serializable {

    public static int DEFAULT_MAXN_TREE_DEPTH = 10;
    
    private int maxNTreeDepth = DEFAULT_MAXN_TREE_DEPTH;
    private boolean maxNHashmap = true;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;
	
	public ParMaxN() {	}
    
	public ParMaxN(ParMaxN op) {
		this.maxNTreeDepth = op.getMaxnDepth();
		this.maxNHashmap = op.useMinimaxHashmap();	
	}
	
    public ParMaxN(MaxNParams op) { 
    	this.setFrom(op);
    }
    
	public void setFrom(MaxNParams op) {
		this.maxNTreeDepth = op.getMaxnDepth();
		this.maxNHashmap = op.useMinimaxHashmap();	
	}
	
	public int getMaxnDepth() {
		return maxNTreeDepth;
	}

	public boolean useMinimaxHashmap() {
		return maxNHashmap;
	}

}
