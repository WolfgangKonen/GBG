package games.Sim;

public class ConfigSim {

	/**
	 *  Number of nodes (graph size)
	 */
	public static int NUM_NODES = 6; //12;
	
	/**
	 *  Number of players
	 */
	public static int NUM_PLAYERS = 2; //2; 3;
	
	/**
	 *  A dummy state, needed as reference in XNTupleFuncsSim
	 */
	public static final StateObserverSim SO = new StateObserverSim();
}
