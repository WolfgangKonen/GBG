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
	 *  Whether a coalition should be formed (only 3-player variant).
	 *  Currently only coalition "1-2" vs. "0".
	 */
	public static String COALITION = "None"; //"None"; "1-2";
	
	/**
	 *  A dummy state, needed as reference in XNTupleFuncsSim
	 */
	public static final StateObserverSim SO = new StateObserverSim();
}
