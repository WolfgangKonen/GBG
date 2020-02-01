package games.Sim;

public class ConfigSim {

	/**
	 *  Graph size
	 */
	public static final int GRAPH_SIZE = 6; //12;
	
	/**
	 *  Number of players
	 */
	public static int NUM_PLAYERS = 3; //2; 3;
	
	/**
	 *  A dummy state, needed as reference in XNTupleFuncsSim
	 */
	public static final StateObserverSim SO = new StateObserverSim();
}
