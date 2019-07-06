package params;

import java.io.Serializable;

public class ParEdax implements Serializable {
    public static int DEFAULT_DEPTH = 21;
    public static double DEFAULT_MOVE_TIME = 10.0;

    private int depth = DEFAULT_DEPTH;
    private double moveTime = DEFAULT_MOVE_TIME;
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;
	
	public ParEdax() {	}
    
	public ParEdax(ParEdax ep) {
		this.depth = ep.getDepth();
		this.moveTime = ep.getMoveTime();
	}
	
    public ParEdax(EdaxParams ep) { 
    	this.setFrom(ep);
    }
    
	public void setFrom(EdaxParams ep) {
		this.depth = ep.getDepth();
		this.moveTime = ep.getMoveTime();
	}
	
	public void setDepth(int num)
	{
		this.depth=num;
	}

	public void setMoveTime(double val) {
		this.moveTime=val;
	}
	

	public int getDepth() {
		return depth;
	}

	public double getMoveTime() {
		return moveTime;
	}

}
