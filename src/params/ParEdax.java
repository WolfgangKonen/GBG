package params;

import java.io.Serializable;

import javax.swing.JPanel;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.Othello.Edax.Edax2;

/**
 * This class realizes Edax parameter settings (specific to Othello). 
 * <p>
 * These parameters and their [defaults] are:
 * <ul>
 * <li><b>Depth</b>: Edax search depth (command 'level', range 0,1,..., default 21)
 * <li><b>Time</b>: Time per move in seconds (command 'move-time', default 10.0)
 * </ul>
 * 
 * @see Edax2
 * @see EdaxParams
 * @see games.XArenaButtons
 */
public class ParEdax implements Serializable {
    public static int DEFAULT_DEPTH = 21;
    public static double DEFAULT_MOVE_TIME = 10.0;

    private int depth = DEFAULT_DEPTH;
    private double moveTime = DEFAULT_MOVE_TIME;
    
    /**
     * This member is only constructed when the constructor {@link #ParEdax(boolean) ParEdax(boolean withUI)} 
     * called with {@code withUI=true}. It holds the GUI for {@link ParEdax}.
     */
    private transient EdaxParams edparams = null;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;
	
	public ParEdax() {	}
    
	public ParEdax(boolean withUI) {
		if (withUI)
			edparams = new EdaxParams();
	}
	
	public ParEdax(ParEdax ep) {
    	this.setFrom(ep);
	}
	
    public ParEdax(EdaxParams ep) { 
    	this.setFrom(ep);
    }
    
	public void setFrom(ParEdax ep) {
		this.depth = ep.getDepth();
		this.moveTime = ep.getMoveTime();
		
		if (edparams!=null)
			edparams.setFrom(this);
	}
	
	public void setFrom(EdaxParams ep) {
		this.depth = ep.getDepth();
		this.moveTime = ep.getMoveTime();
		
		if (edparams!=null)
			edparams.setFrom(this);
	}
	
	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromEdaxParams() {
		if (edparams!=null)
			this.setFrom(edparams);
	}
	
	public JPanel getPanel() {
		if (edparams!=null)		
			return edparams.getPanel();
		return null;
	}

	public int getDepth() {
		return depth;
	}

	public double getMoveTime() {
		return moveTime;
	}

	public void setDepth(int num) {
		this.depth=num;
		if (edparams!=null)
			edparams.setDepth(num);
	}

	public void setMoveTime(double val) {
		this.moveTime=val;
		if (edparams!=null)
			edparams.setMoveTime(val);
	}
	
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. If with UI, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "TDS" (for {@link TDAgent})
	 * @param gameName the string from {@link Arena#getGameName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		switch (gameName) {
		default:	//  all other
			break;
		}
		switch (agentName) {
		default: 
			this.setDepth(21);
			this.setMoveTime(10.0);
			break;
		}
	}
	
}
