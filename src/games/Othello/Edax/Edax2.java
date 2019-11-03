package games.Othello.Edax;

import java.io.Serializable;
import java.util.ArrayList;

import agentIO.LoadSaveGBG;
import controllers.AgentBase;
import controllers.PlayAgent;
import games.Arena;
import games.StateObservation;
import games.XArenaMenu;
import games.Othello.StateObserverOthello;
import params.ParEdax;
import params.ParOther;
import tools.ScoreTuple;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

/**
 * 
 * Edax2 replaces the former agent Edax. 
 * It has a simpler interface and it has no need for initialization
 *
 */
public class Edax2 extends AgentBase implements PlayAgent, Serializable
{
	private static final long serialVersionUID = 13L;
	private String lastEdaxMove;
	
	private ParEdax edaxPar;
	
	transient private CommandLineInteractor commandLineInteractor;
	
	public Edax2()
	{
		super("Edax2");
		edaxPar = new ParEdax();
		System.out.println("creating edax2");
		super.setAgentState(AgentState.TRAINED);
		initializeCLI();
	}
	
	public Edax2(String agentName, ParEdax ePar) {
		super(agentName);
		edaxPar = new ParEdax(ePar);
		System.out.println("creating edax2");
		super.setAgentState(AgentState.TRAINED);
		initializeCLI();
	}
	
	private void initializeCLI() {
		commandLineInteractor = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
		commandLineInteractor.sendCommand("mode 3"); // no automatic moves
		commandLineInteractor.sendCommand("level "+edaxPar.getDepth()); 		//  set search depth 
		commandLineInteractor.sendCommand("move-time "+edaxPar.getMoveTime()); //  set time per move		
	}
	
	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		// since member commandLineInteractor is transient (not serializable), it will be null
		// when loading a saved Edaxs2 from disk  -->  we need to restore it (with the help of 
		// member edaxPar) as it is done in constructor 
		if (commandLineInteractor==null)
			initializeCLI();
		
		return true;
	}
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see XArenaMenu#loadAgent
	 * @see XArenaTabs
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setEdaxParFrom(n, this.getParEdax() );
//		m_arena.m_xab.setOParFrom(n, this.getParOther() );		// do or don't?
	}
	
	/**
	 * Given state {@code sob}, let Edax find the best next action.  
	 * Set {@code vTable} of returned ACTIONS_VT in such a way that it is 1.0 for the action chosen
	 * by Edax, 0.0 for all other available actions (needed for display in InspectV).	 
	 */
	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		assert sob instanceof StateObserverOthello: "sob not instance of StateObserverOthello";
		StateObserverOthello so = (StateObserverOthello) sob;
		ArrayList<ACTIONS> availActions = so.getAvailableActions();
		
//		commandLineInteractor.sendCommand("init"); 
		commandLineInteractor.sendCommand("setboard "+so.toEdaxString()); 
		lastEdaxMove = commandLineInteractor.sendAndAwait("go");  // force Edax to play now			
		
		int actInteger = EdaxMoveConverter.converteEdaxToInt(lastEdaxMove);
		double[] vTable = new double[availActions.size()+1];
		for (int i=0; i<availActions.size(); i++) {
			if (availActions.get(i).toInt()==actInteger) vTable[i]=1.0;
		}
		vTable[availActions.size()]=1.0;
		
		ACTIONS_VT action = new ACTIONS_VT(actInteger, false, vTable);

		if(!so.getAvailableActions().contains(action))
		{
			System.err.println(this.getName() + " IST AM SCHUMMELN!");
		}
				
		return action;
	}
	
    @Override
	public String stringDescr() {
		String cs = getClass().getSimpleName();
		String str = cs + ": depth:" + edaxPar.getDepth()
						+ ", moveTime:" + edaxPar.getMoveTime();
		return str;
	}
		
	@Override
	public double getScore(StateObservation sob) {
		// Edax has no estimates for score values, so we return 0 ...
		return 0;
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation sob, ScoreTuple prevTuple) {
		// ... and so also no real score tuple
		return new ScoreTuple(sob.getNumPlayers());
	}

	public ParEdax getParEdax() {
		return edaxPar;
	}
}
