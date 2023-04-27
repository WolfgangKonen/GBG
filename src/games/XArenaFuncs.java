package games;

import TournamentSystem.TSTimeStorage;
import TournamentSystem.tools.TSGameDataTransfer;
import controllers.*;
import controllers.MC.MCAgentN;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpWrapper.MctseWrapperAgent;
import controllers.MCTSExpWrapper.stateApproximation2.PlayAgentApproximator2;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.MCTSWrapper.MCTSWrapperAgent;
import controllers.MCTSWrapper.stateApproximation.PlayAgentApproximator;
import controllers.RHEA.RheaAgentSI;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.NTupleBase;
import controllers.TD.ntuple2.NTupleFactory;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import controllers.TD.ntuple4.*;
import games.BlackJack.BasicStrategyBlackJackAgent;
import games.CFour.AlphaBetaAgent;
import games.CFour.openingBook.BookSum;
import games.KuhnPoker.KuhnPokerAgent;
import games.Nim.BoutonAgent;
import games.Nim.DaviNimAgent;
import games.Othello.BenchmarkPlayer.BenchMarkPlayer;
import games.Othello.Edax.Edax2;
import games.RubiksCube.*;
import tools.TStats.TAggreg;
import gui.DeviationWeightsChart;
import gui.LineChartSuccess;
import params.*;
import starters.MTrain;
import tools.*;
import tools.Types.ACTIONS;

import javax.swing.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Class {@link XArenaFuncs} contains several methods to train, evaluate and
 * measure the performance of agents.
 * <ul>
 * <li>train: train an agent one time for maxGameNum episodes and evaluate it
 * with evalAgent
 * <li>multiTrain: train an agent multiple times and evaluate it with evalAgent
 * <li>compete: one competition 'X vs. O', several episodes, measure
 * win/tie/loose rate
 * <li>competeBoth call compete for pair (pa,opponent) in both roles, X and O
 * <li>multiCompete: many competitions, measure win/tie/loose rate and avg.
 * correct moves
 * <li>eval: (as part of the protected {@link Evaluator} elements) measure agent
 * success
 * </ul>
 * --- Batch methods are now in TicTacToeBatch ---
 * <p>
 * Known classes having {@link XArenaFuncs} objects as members: {@link Arena},
 * {@link XArenaButtons}
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 * 
 */
public class XArenaFuncs {
	public PlayAgent[] m_PlayAgents;
	private final Arena m_Arena;
	protected Evaluator m_evaluatorQ = null;
	protected Evaluator m_evaluatorT = null;
	protected String lastMsg = "";
	protected int numPlayers;

	protected Random rand;
	protected LineChartSuccess lChart = null;
	protected DeviationWeightsChart wChart = null;


	/**
	 * percentiles for weight chart plot on wChart (only relevant for
	 * TDNTuple2Agt)
	 */
	double[] per = { 5, 25, 50, 75, 95 };

	public XArenaFuncs(Arena arena) {
		m_Arena = arena;
		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();
		m_PlayAgents = new PlayAgent[numPlayers];
		rand = new Random(System.currentTimeMillis());
		if (m_Arena.hasGUI()) {
			lChart = new LineChartSuccess("Training Progress", "gameNum", "", true, false);
			wChart = new DeviationWeightsChart("", "gameNum", "", true, false);
		}
	}

	public void destroy() {
		if (lChart != null) lChart.destroy();
		if (wChart != null)	wChart.destroy();
	}

	// Helper for constructAgent and fetchAgent
	private void updateParams(int n, XArenaButtons m_xab) {

		// If there is a GUI: be sure to get the latest changes from the GUI's param tabs (!)
		m_xab.tdPar[n].pushFromTDParams();
		m_xab.ntPar[n].pushFromNTParams();
		m_xab.oPar[n].pushFromOTParams();
		m_xab.maxnPar[n].pushFromMaxNParams();
		m_xab.mcPar[n].pushFromMCParams();
		m_xab.mctsPar[n].pushFromMCTSParams();
		m_xab.mctsePar[n].pushFromMCTSEParams();
		m_xab.edPar[n].pushFromEdaxParams();
		m_xab.rbPar[n].pushFromRBParams();
		m_xab.wrPar[n].pushFromWrParams();

		m_xab.m_arena.getGameBoard().updateParams();		// for RubiksCube: update 5 params in CubeConfig from agent settings

		// update element pMaxValue in GameBoardCubeGUI, if present:
		if (m_xab.m_arena.getGameBoard() instanceof GameBoardCube) {
			((GameBoardCube)m_xab.m_arena.getGameBoard()).setPMin(CubeConfig.pMin);
			((GameBoardCube)m_xab.m_arena.getGameBoard()).setPMax(CubeConfig.pMax);
		}

	}


	/**
	 * Construct and return a new {@link PlayAgent}, based on the settings in
	 * {@code sAgent} and {@code m_xab}.
	 * <p>
	 * The difference to {@link #fetchAgent(int, String, XArenaButtons)} is that
	 * the {@link PlayAgent} is a new agent (i.e. not yet trained for trainable
	 * agents, agent state INIT).
	 * <p>
	 * 
	 * @param n
	 *            the player number
	 * @param sAgent
	 *            the string from the agent-select box
	 * @param m_xab
	 *            used only for reading parameter values from GUI members
	 * @return a new {@link PlayAgent} (initialized, but not yet trained)
	 *
	 * @see #fetchAgents(XArenaButtons)
	 */
	public PlayAgent constructAgent(int n, String sAgent, XArenaButtons m_xab) {
		PlayAgent pa = null;
		int maxGameNum = m_xab.getGameNumber();
//		int featmode = m_xab.tdPar[n].getFeatmode();

		// push the params from Param Tabs onto the relevant ParXX in m_xab & call gb.updateParams():
		updateParams(n,m_xab);

		try {
			if (sAgent.equals("TDS")) {
				Feature feat = m_xab.m_arena.makeFeatureClass(m_xab.tdPar[n].getFeatmode());
				pa = new TDAgent(sAgent, m_xab.tdPar[n], m_xab.oPar[n], feat, maxGameNum);
			} else if (sAgent.equals("RHEA-SI")) {
				pa = new RheaAgentSI(sAgent, null);
			} else if (sAgent.equals("TD-Ntuple-3")) {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new TDNTuple3Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, maxGameNum);
			} else if (sAgent.equals("TD-Ntuple-4")) {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new TDNTuple4Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n],m_xab.rbPar[n], m_xab.wrPar[n], nTuples, xnf, maxGameNum);
				//((TDNTuple4Agt) pa).setRewardParsFromParTD();
				m_xab.m_arena.gb.updateParams();
			} else if (sAgent.equals("Sarsa")) {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				// int numOutputs =
				// m_xab.m_game.gb.getDefaultStartState().getAllAvailableActions().size();
				ArrayList<ACTIONS> allAvailActions = m_xab.m_arena.gb.getDefaultStartState().getAllAvailableActions();
				pa = new SarsaAgt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, allAvailActions, maxGameNum);
			} else if (sAgent.equals("Sarsa-4")) {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				ArrayList<ACTIONS> allAvailActions = m_xab.m_arena.gb.getDefaultStartState().getAllAvailableActions();
				pa = new Sarsa4Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, allAvailActions, maxGameNum);
			} else if (sAgent.equals("Qlearn-4")) {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				ArrayList<ACTIONS> allAvailActions = m_xab.m_arena.gb.getDefaultStartState().getAllAvailableActions();
				pa = new QLearn4Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, allAvailActions, maxGameNum);
			} else if (sAgent.equals("Max-N")) {
				pa = new MaxNAgent(sAgent, m_xab.maxnPar[n], m_xab.oPar[n]);
			} else if (sAgent.equals("Expectimax-N")) {
				pa = new ExpectimaxNAgent(sAgent, m_xab.maxnPar[n], m_xab.oPar[n]);
			} else if (sAgent.equals("Random")) {
				pa = new RandomAgent(sAgent, m_xab.oPar[n]);
			} else if (sAgent.equals("MCTS")) {
				pa = new MCTSAgentT(sAgent, null, m_xab.mctsPar[n], m_xab.oPar[n]);
			} else if (sAgent.equals("MCTS Expectimax")) {
				pa = new MCTSExpectimaxAgt(sAgent, m_xab.mctsePar[n], m_xab.oPar[n]);
			} else if (sAgent.equals("Human")) {
				pa = new HumanPlayer(sAgent);
			} else if (sAgent.equals("MC-N")) {
				pa = new MCAgentN(sAgent, m_xab.mcPar[n], m_xab.oPar[n]);
			} else if (sAgent.equals("AlphaBeta")) {// CFour only, see gui_agent_list in XArenaButtonsGui
				AlphaBetaAgent alphaBetaStd = new AlphaBetaAgent(new BookSum()); 	  // no search for distant losses
				alphaBetaStd.instantiateAfterLoading();
				pa = alphaBetaStd;
			} else if (sAgent.equals("AlphaBeta-DL")) {
				AlphaBetaAgent alphaBetaStd = new AlphaBetaAgent(new BookSum(),1000); // search for distant losses
				alphaBetaStd.instantiateAfterLoading();
				pa = alphaBetaStd;
			} else if (sAgent.equals("Bouton")) { 	// Nim only, see gui_agent_list in XArenaButtonsGui
				pa = new BoutonAgent(sAgent);
			} else if (sAgent.equals("DaviNim")) { 	// Nim3P only, see gui_agent_list in XArenaButtonsGui
				pa = new DaviNimAgent(sAgent, m_xab.oPar[n]);
			} else if (sAgent.equals("HeurPlayer")) { // Othello only, see gui_agent_list in XArenaButtonsGui
				pa = new BenchMarkPlayer("HeurPlayer", 0);
			} else if (sAgent.equals("BenchPlayer")) { // Othello only, see gui_agent_list in XArenaButtonsGui
				pa = new BenchMarkPlayer("BenchPlayer", 1);
			} else if (sAgent.equals("Edax2")) {// Othello only, see gui_agent_list in XArenaButtonsGui
				pa = new Edax2(sAgent, m_xab.edPar[n]);
			} else if (sAgent.equals("DAVI2")) { // RubiksCube only, see gui_agent_list in XArenaButtonsGui
				pa = new DAVI2Agent(sAgent, m_xab.oPar[n]);
			} else if (sAgent.equals("DAVI3")) { // RubiksCube only, see gui_agent_list in XArenaButtonsGui
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new DAVI3Agent(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, maxGameNum);
			} else if (sAgent.equals("DAVI4")) { // RubiksCube only, see gui_agent_list in XArenaButtonsGui
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new DAVI4Agent(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, maxGameNum, m_xab.m_arena);
			} else if(sAgent.equals("BSBJA")) { // Black Jack only, see gui_agent_list in XArenaButtonsGui
				pa = new BasicStrategyBlackJackAgent();
			} else if(sAgent.equals("KuhnOptimal")) { // KuhnPoker only, see gui_agent_list in XArenaButtonsGui
				pa = new KuhnPokerAgent("KuhnOptimal");
			}
		} catch (Exception e) {
			m_Arena.showMessage(e.getClass().getName() + ": " + e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}

		return pa;
	}

	/**
	 * Fetch the {@link PlayAgent} vector from {@link Arena}. For agents who
	 * do not need training, construct a new one according to the selected
	 * choice and parameter settings. For agents who need training, see, if
	 * {@link #m_PlayAgents}[n] has already an agent of this type. If so, return
	 * it, if not:
	 * <ul>
	 * <li>	if {@link #m_PlayAgents}[n]==null, construct a new agent and
	 * 		initialize it, but do not yet train it.
	 * <li>	else, fetch the agent, check if it has the proper class matching {@code sAgent}. Then, check agent state:
	 * 		<ul>
	 * 		<li> If state INIT, then take over the relevant parameters from Arena param tabs (useful for saving a stub).
	 * 		<li> If state TRAINED, then take the agent 'as-is' and take over only the wrapper parameters (useful when evaluating
	 * 		  	 a trained agent with other wrapper settings)
	 * 		</ul>
	 * </ul>
	 * @param n
	 *            the player number
	 * @param sAgent
	 *            the string from the agent-select box
	 * @param m_xab
	 *            used only for reading parameter values from GUI members
	 * @return the {@link PlayAgent} for the {@code n}th player
	 * @throws RuntimeException if agent cannot be fetched
	 * 
	 * @see #fetchAgents(XArenaButtons)
	 * @see #constructAgent(int, String, XArenaButtons)
	 */
	public PlayAgent fetchAgent(int n, String sAgent, XArenaButtons m_xab) {
		PlayAgent pa = null;

		// push the params from Param Tabs onto the relevant ParXX in m_xab & call gb.updateParams():
		updateParams(n,m_xab);

		if (sAgent.equals("Max-N")) {
			pa = new MaxNAgent(sAgent, m_xab.maxnPar[n], m_xab.oPar[n]);
		} else if (sAgent.equals("RHEA-SI")) {
			pa = new RheaAgentSI(sAgent, null);
		} else if (sAgent.equals("Expectimax-N")) {
			pa = new ExpectimaxNAgent(sAgent, m_xab.maxnPar[n], m_xab.oPar[n]);
		} else if (sAgent.equals("Random")) {
			pa = new RandomAgent(sAgent, m_xab.oPar[n]);
		} else if (sAgent.equals("MCTS")) {
			pa = new MCTSAgentT(sAgent, null, m_xab.mctsPar[n], m_xab.oPar[n]);
		} else if (sAgent.equals("MCTS Expectimax")) {
			pa = new MCTSExpectimaxAgt(sAgent, m_xab.mctsePar[n], m_xab.oPar[n]);
		} else if (sAgent.equals("Human")) {
			pa = new HumanPlayer(sAgent);
//		} else if (sAgent.equals("MC")) {
//			pa = new MCAgent(sAgent, m_xab.mcPar[n], m_xab.oPar[n]);
		} else if (sAgent.equals("MC-N")) {
			pa = new MCAgentN(sAgent, m_xab.mcPar[n], m_xab.oPar[n]);
		} else if (sAgent.equals("AlphaBeta")) {			// CFour only, see gui_agent_list in XArenaButtonsGui
			AlphaBetaAgent alphaBetaStd = new AlphaBetaAgent(new BookSum()); 	  // no search for distant losses
			alphaBetaStd.instantiateAfterLoading();
			pa = alphaBetaStd;
		} else if (sAgent.equals("AlphaBeta-DL")) {				// CFour only, see gui_agent_list in XArenaButtonsGui
			AlphaBetaAgent alphaBetaStd = new AlphaBetaAgent(new BookSum(),1000); // search for distant losses
			alphaBetaStd.instantiateAfterLoading();
			pa = alphaBetaStd;
		} else if (sAgent.equals("Bouton")) { 	// Nim only, see gui_agent_list in XArenaButtonsGui
			pa = new BoutonAgent(sAgent);
		} else if (sAgent.equals("HeurPlayer")) { 	// Othello only, see gui_agent_list in XArenaButtonsGui
			pa = new BenchMarkPlayer("HeurPlayer", 0);
		} else if (sAgent.equals("BenchPlayer")) { 	// Othello only, see gui_agent_list in XArenaButtonsGui
			pa = new BenchMarkPlayer("BenchPlayer", 1);
//		} else if (sAgent.equals("Edax")) { // Othello only, see gui_agent_list in XArenaButtons
//			pa = new Edax();
		} else if (sAgent.equals("Edax2")) { 	// Othello only, see gui_agent_list in XArenaButtons
			pa = new Edax2(sAgent, m_xab.edPar[n]);
		} else if(sAgent.equals("BSBJA")) { // Black Jack only, see gui_agent_list in XArenaButtonsGui
			pa = new BasicStrategyBlackJackAgent();
		} else if(sAgent.equals("KuhnOptimal")) { // KuhnPoker only, see gui_agent_list in XArenaButtonsGui
			pa = new KuhnPokerAgent("KuhnOptimal");
		} else { // all the trainable agents:
			if (m_PlayAgents[n] == null) {

				pa = fetchTrainableAgent(n, sAgent, m_xab);

			} else { // i.e. if m_PlayAgents[n]!=null
				PlayAgent inner_pa = m_PlayAgents[n];
				if (m_PlayAgents[n].isWrapper())
					inner_pa = m_PlayAgents[n].getWrappedPlayAgent();
				if (!sAgent.equals(inner_pa.getName()))
					throw new RuntimeException("Current agent for player " + n + " is " + m_PlayAgents[n].getName()
							+ " but selector for player " + n + " requires " + sAgent + ".");
				pa = m_PlayAgents[n]; // take the n'th current agent, whose state is either INIT or TRAINED

				switch (pa.getAgentState()) {
					case INIT:
						// When fetching an agent in state INIT (useful when saving a stub), we want to take over all
						// settings from the param tabs (that the user might have just set) and save them with pa to disk
						if (pa instanceof NTuple4Base) {
							((NTuple4Base) pa).setParTD(m_xab.tdPar[n]);
							((NTuple4Base) pa).setParNT(m_xab.ntPar[n]);
						} else if (pa instanceof NTupleBase) {
							((NTupleBase) pa).setParTD(m_xab.tdPar[n]);
							((NTupleBase) pa).setParNT(m_xab.ntPar[n]);
						}
						//
						// TODO: extend this for other agents and their param tabs
						//

						pa.setParWrapper(m_xab.wrPar[n]); // for all agents
						pa.setWrapperParamsOfromWr(pa.getParWrapper());
						pa.setParOther(m_xab.oPar[n]);   // for all agents
						pa.setParReplay(m_xab.rbPar[n]); // for all agents
						break;
					case TRAINED:
						// When fetching an agent in state TRAINED, we want to take it 'as-is' (it was perhaps trained
						// for a longer time) and use it with all its current settings. The only exceptions are:
						//
						// Wrapper MaxN: { nPly}, Wrapper MCTS: {iterations, PUCT, depth}, and stopEval (EpiLength Eval) are the
						// ONLY parameters from tab 'Other pars' which may be changed by the user for a TRAINED agent.
						// (All the other opar parameters may be only
						// set/changed BEFORE training a trainable agent.) The following line of code was missing before 2020-08-11
						// and caused the bug that a Wrapper nPly set for a trained agent was not saved to disk.
						// Now it will be saved:
						pa.setParWrapper(m_xab.wrPar[n]);
						pa.setWrapperParamsOfromWr(pa.getParWrapper());
						//pa.setWrapperParamsO(m_xab.oPar[n]);		// obsolete

						pa.setParReplay(m_xab.rbPar[n]);
						break;
					default:
						throw new RuntimeException("Not supported AgentState");
				}

				// --- this is now done by updateParams above
//				if (pa instanceof NTuple4Base) {
//					((NTuple4Base) pa).setRewardParsFromParTD();
//				}

			}
		}
		if (pa == null)
			throw new RuntimeException("Could not construct/fetch agent = " + sAgent);

		return pa;
	} // fetchAgent

	// helper function for fetchAgent: construct a new trainable agent if the current one is null
	private PlayAgent fetchTrainableAgent(int n, String sAgent, XArenaButtons m_xab) {
		PlayAgent pa = null;
		int maxGameNum = m_xab.getGameNumber();

		if (sAgent.equals("TDS")) {
			Feature feat = m_xab.m_arena.makeFeatureClass(m_xab.tdPar[n].getFeatmode());
			pa = new TDAgent(sAgent, m_xab.tdPar[n], m_xab.oPar[n], feat, maxGameNum);
		} else if (sAgent.equals("TD-Ntuple-3")) {
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new TDNTuple3Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, maxGameNum);
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		} else if (sAgent.equals("TD-Ntuple-4")) {
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new TDNTuple4Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n],m_xab.rbPar[n], m_xab.wrPar[n], nTuples, xnf, maxGameNum);
				//((TDNTuple4Agt) pa).setRewardParsFromParTD();
				m_xab.m_arena.gb.updateParams();
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		} else if (sAgent.equals("Sarsa")) {
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				// int numOutputs =
				// m_xab.m_game.gb.getDefaultStartState().getAllAvailableActions().size();
				ArrayList<ACTIONS> allAvailActions = m_xab.m_arena.gb.getDefaultStartState()
						.getAllAvailableActions();
				pa = new SarsaAgt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, allAvailActions, maxGameNum);
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning Sarsa", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		} else if (sAgent.equals("Sarsa-4")) {
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				ArrayList<ACTIONS> allAvailActions = m_xab.m_arena.gb.getDefaultStartState()
						.getAllAvailableActions();
				pa = new Sarsa4Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, allAvailActions, maxGameNum);
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning Sarsa-4", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		} else if (sAgent.equals("Qlearn-4")) {
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				ArrayList<ACTIONS> allAvailActions = m_xab.m_arena.gb.getDefaultStartState()
						.getAllAvailableActions();
				pa = new QLearn4Agt(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, allAvailActions, maxGameNum);
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning Qlearn-4", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		} else if (sAgent.equals("DaviNim")) { 	// Nim3P only, see gui_agent_list in XArenaButtonsGui
			pa = new DaviNimAgent(sAgent, m_xab.oPar[n]);
		} else if (sAgent.equals("DAVI2")) { // RubiksCube only, see gui_agent_list in XArenaButtonsGui
			pa = new DAVI2Agent(sAgent, m_xab.oPar[n]);
		} else if (sAgent.equals("DAVI3")) { // RubiksCube only, see gui_agent_list in XArenaButtonsGui
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTupleFactory ntupfac = new NTupleFactory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new DAVI3Agent(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, maxGameNum);
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		} else if (sAgent.equals("DAVI4")) { // RubiksCube only, see gui_agent_list in XArenaButtonsGui
			try {
				XNTupleFuncs xnf = m_xab.m_arena.makeXNTupleFuncs();
				NTuple4Factory ntupfac = new NTuple4Factory();
				int[][] nTuples = ntupfac.makeNTupleSet(m_xab.ntPar[n], xnf);
				pa = new DAVI4Agent(sAgent, m_xab.tdPar[n], m_xab.ntPar[n],
						m_xab.oPar[n], nTuples, xnf, maxGameNum, m_Arena);
			} catch (Exception e) {
				m_Arena.showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
				// e.printStackTrace();
			}
		}

		return pa;
	} // fetchTrainableAgent

	/**
	 * Fetch the vector of all {@link PlayAgent}s from {@link Arena}. See
	 * {@link #fetchAgent(int, String, XArenaButtons)} for the rules how to
	 * fetch.
	 * 
	 * @param m_xab
	 *            where to read the settings from
	 * @return the vector {@code m_PlayAgents} of all agents in the arena
	 * @throws RuntimeException if agents cannot be fetched
	 * 
	 * @see #constructAgent(int, String, XArenaButtons)
	 * @see #fetchAgent(int, String, XArenaButtons)
	 */
	public PlayAgent[] fetchAgents(XArenaButtons m_xab) throws RuntimeException {
		if (m_PlayAgents == null)
			m_PlayAgents = new PlayAgent[numPlayers];
		for (int n = 0; n < numPlayers; n++) {
			String sAgent = m_xab.getSelectedAgent(n);
			m_PlayAgents[n] = fetchAgent(n, sAgent, m_xab);
		}
		return m_PlayAgents;
	}

	/**
	 * Given the selected agents in {@code paVector}, call
	 * {@link #wrapAgent(PlayAgent, ParOther, ParWrapper, ParMaxN, StateObservation)}
	 * for every {@code n}.
	 *
	 * @param paVector
	 *            the (unwrapped) agents for each player
	 * @param m_xab
	 *            for access to m_xab.oPar, the vector of {@link ParWrapper},
	 *            containing {@code nply = wrPar[n].getWrapperNPly()} for each
	 *            agent separately. And for access to m_xab.maxnParams, the
	 *            vector of {@link ParMaxN} containing
	 *            {@code boolean maxNHashMap}.
	 * @param so
	 *            needed only to detect whether game is deterministic or not.
	 * @return the vector of unwrapped agents ({@code paVector} if {@code nply==0 and iter==0};
	 *         else a vector of wrapped agents
	 * 
	 * @see MaxN2Wrapper
	 * @see ExpectimaxNWrapper
	 */
	public PlayAgent[] wrapAgents(PlayAgent[] paVector, XArenaButtons m_xab, StateObservation so)
	{
		PlayAgent[] qaVector = new PlayAgent[numPlayers];
		for (int n = 0; n < numPlayers; n++) {
			qaVector[n] = wrapAgent(paVector[n], m_xab.oPar[n], m_xab.wrPar[n], m_xab.maxnPar[n], so);
		} // for (n)
		return qaVector;
	}

	// a similar function, just needed by TS
	public PlayAgent[] wrapAgents_TS(PlayAgent[] paVector, XArenaButtons m_xab, StateObservation so) {
		PlayAgent[] qaVector = new PlayAgent[numPlayers];
		for (int n = 0; n < numPlayers; n++) {
			qaVector[n] = wrapAgent(paVector[n], paVector[n].getParOther(), m_xab.wrPar[n], m_xab.maxnPar[n], so);
		} // for (n)
		return qaVector;
	}


	/**
	 * Given the selected agents {@code pa}, do nothing if both {@code nply==0 and iter==0}
	 * (where {@code nply = wrPar.getWrapperNPly()} and {@code iter = wrPar.getWrapperMCTSIterations()}).
	 * <p>
	 * If {@code nply>0}, wrap them by an n-ply look-ahead tree search. The tree is of type {@link MaxN2Wrapper} for deterministic
	 * games and of type {@link ExpectimaxNWrapper} for nondeterministic games.
	 * Caution: Larger values for {@code nply}, e.g. greater 5, may lead to long
	 * execution times and large memory requirements!
	 * <p>
	 * If {@code iter>0}, wrap them by an MCTS wrapper. The wrapper is of type {@link MCTSWrapperAgent} for deterministic
	 * games and of type {@link MctseWrapperAgent}  nondeterministic games.
	 * <p>
	 * An exception is thrown if both {@code nply>0 and iter>0}. No wrapping occurs for agent {@link HumanPlayer}.
	 *
	 * @param pa    the inner agent
	 * @param oPar  needed for the wrapper parameter
	 * @param wrPar	wrapper parameter
	 * @param mPar  (currently not used)
	 * @param so    needed only to detect whether game is deterministic or not.
	 * @return	the wrapper agent enclosing the wrapped (inner) agent
	 */
	public PlayAgent wrapAgent(PlayAgent pa, ParOther oPar, ParWrapper wrPar, ParMaxN mPar, StateObservation so) {
		int iter = wrPar.getWrapperMCTS_iterations();
		return innerWrapAgent(pa, oPar, wrPar, iter, mPar, so);
	}
	/**
	 * Same as {@link #wrapAgent(PlayAgent, ParOther, ParWrapper, ParMaxN, StateObservation) wrapAgent}, but with
	 * {@code iter = wrPar.getWrapperMCTS_iter_train()}
	 */
	public PlayAgent wrapAgentTrain(PlayAgent pa, ParOther oPar, ParWrapper wrPar, ParMaxN mPar, StateObservation so) {
		int iter = wrPar.getWrapperMCTS_iter_train();
		return innerWrapAgent(pa, oPar, wrPar, iter, mPar, so);
	}
	private PlayAgent innerWrapAgent(PlayAgent pa, ParOther oPar, ParWrapper wrPar, int iter, ParMaxN mPar, StateObservation so) {
		PlayAgent qa;
		int nply = wrPar.getWrapperNPly();
		if (nply!=0 && iter!=0) {
			throw new RuntimeException("Both Wrapper Iter MCTS and Wrapper nPly are non-zero!");
		}
		if (nply > 0 && !(pa instanceof HumanPlayer)) {
			if (so.isDeterministicGame()) {
				qa = new MaxN2Wrapper(pa, nply, oPar); // oPar has other params
				//ParMaxN wrap_mPar = new ParMaxN();		// make a copy! (bug fix 02-2020)
				//wrap_mPar.setMaxNDepth(nply);
				//wrap_mPar.setMaxNUseHashmap(mPar.getMaxNUseHashmap());
				// qa = new MaxNWrapper(pa, wrap_mPar, oPar); // wrap_mPar has useMaxNHashMap
			} else {
				qa = new ExpectimaxNWrapper(pa, nply);
			}
		} else {
			qa = pa;
		}

		// Wrap the agent with MCTS lookahead if 'Wrapper MCTS' is greater than zero.
		if(iter > 0 && !(pa instanceof HumanPlayer)){
			if (so.isDeterministicGame()) {
				qa = new MCTSWrapperAgent(
						iter,
						wrPar.getWrapperMCTS_PUCT(),
						new PlayAgentApproximator(qa),
						"MCTS-wrapped "+qa.getName(),
						wrPar.getWrapperMCTS_depth(),
						oPar
				);
			} else {
				qa = new MctseWrapperAgent(
						iter,
						wrPar.getWrapperMCTS_PUCT(),
						new PlayAgentApproximator2(qa),
						"Mctse-wrapped "+qa.getName(),
						wrPar.getWrapperMCTS_depth(),
						oPar
				);
			}
		}
		qa.setParWrapper(wrPar);		// important bug fix Feb'2023 (!!): set the wrapper params for the wrapper

		return qa;
	}

	/**
	 * Perform one training of a {@link PlayAgent} sAgent with maxGameNum
	 * episodes.
	 * 
	 * @param n
	 *            index of agent to train
	 * @param sAgent
	 *            a string containing the class name of the agent
	 * @param xab
	 *            used only for reading parameter values from members td_par,
	 *            cma_par
	 * @param gb
	 *            the game board
	 * @return the trained PlayAgent
	 */
	public PlayAgent train(int n, String sAgent, XArenaButtons xab, GameBoard gb)  {
		int stopTest; // 0: do not call Evaluator during training;
						// >0: call Evaluator after every stopTest training
						// games
		int stopEval; // 0: do not stop on Evaluator;
						// >0: stop, if Evaluator stays true for stopEval games
		int maxGameNum; // maximum number of training games
		int numEval; // evaluate the trained agent every numEval games
		long elapsedMs;
		int gameNum = 0;
		EvalResult 	eresQ = new EvalResult(),
					eresT = new EvalResult();

		maxGameNum = xab.getGameNumber();

		boolean doTrainStatistics;		// doTrainStatistics is mainly useful for RubiksCube, but can be activated for other games as well, if needed
		doTrainStatistics = (m_Arena.getGameName().equals("RubiksCube"));
		ArrayList<TStats> tsList = new ArrayList<>();
		ArrayList<TAggreg> taggList;

		PlayAgent pa;
		PlayAgent qa;

		try {
			pa = this.constructAgent(n, sAgent, xab);
			if (pa == null)
				throw new RuntimeException("Could not construct agent = " + sAgent);
			qa = wrapAgentTrain(pa, xab.oPar[n], xab.wrPar[n], xab.maxnPar[n], gb.getDefaultStartState());

			if (qa == null)
				throw new RuntimeException("Could not wrap agent = " + sAgent);
			pa = qa;
			pa.setWrapperParamsO(xab.oPar[n]);
		} catch (RuntimeException e) {
			m_Arena.showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
			return null;
		}

		// bug fix: numEval needs to be set *after* constructAgent (which contains xab.oPar[n].pushFromOTParams)
		numEval = xab.oPar[n].getNumEval();
		if (numEval == 0)
			numEval = 500; // just for safety, to avoid ArithmeticException in
							// 'gameNum%numEval' below

		String pa_string = pa.getClass().getName();
		if (!pa.isTrainable()) {
			System.out.println(pa_string + " is not trainable");
			return pa;
		}

		// initialization weight distribution plot:
		if (wChart != null)	{
			int plotWeightMode = xab.ntPar[n].getPlotWeightMethod();
			wChart.initializeChartPlot(xab, pa, plotWeightMode);
		}

		System.out.println(pa.stringDescr());
		System.out.println(pa.stringDescr2());
		pa.setMaxGameNum(maxGameNum);
		pa.setNumEval(numEval);
		pa.setGameNum(0);
		System.out.println(pa.printTrainStatus());

		stopTest = xab.oPar[n].getStopTest();
		stopEval = xab.oPar[n].getStopEval();
		int qem = xab.oPar[n].getQuickEvalMode();
		m_evaluatorQ = xab.m_arena.makeEvaluator(pa, gb, stopEval, qem, 1);
		int tem = xab.oPar[n].getTrainEvalMode();
		//
		// doTrainEvaluation flags whether Train Evaluator is executed:
		// Evaluator m_evaluatorT is only constructed and evaluated, if in tab
		// 'Other pars'
		// the choice box 'Train Eval Mode' is not -1 ("none").
		boolean doTrainEvaluation = (tem != -1);
		if (doTrainEvaluation) {
			m_evaluatorT = xab.m_arena.makeEvaluator(pa, gb, stopEval, tem, 1);
		}

		// initialization line chart plot:
		if (lChart != null) lChart.initializeChartPlot(xab, m_evaluatorQ, doTrainEvaluation);

		// Debug only: direct debug output to file debug.txt
		// TDNTupleAgt.pstream = System.out;
		// TDNTupleAgt.pstream = new PrintStream(new
		// FileOutputStream("debug-TDNT.txt"));

		long startTime = System.currentTimeMillis();
		gb.initialize();
		while (pa.getGameNum() < pa.getMaxGameNum()) {
			StateObservation so = soSelectStartState(gb, xab.oPar[n].getChooseStart01(), pa);

			pa.trainAgent(so);

			if (doTrainStatistics)
				collectTrainStats(tsList, pa, so);

			gameNum = pa.getGameNum();
			int liveSignal = (so instanceof StateObserverCube) ? 10000 :
					         (!pa.isWrapper()) ? 500 : 50;
			if (gameNum % liveSignal == 0) {
				int exploMode = pa.getParWrapper().getWrapperMCTS_ExplorationMode();	// just as sanity check
				System.out.println("gameNum: "+gameNum+"   EX"+exploMode);
			}
			if (gameNum % numEval == 0) {
				elapsedMs = (System.currentTimeMillis() - startTime);
				pa.incrementDurationTrainingMs(elapsedMs);
				double elapsedTime = (double) elapsedMs / 1000.0;
				System.out.println(pa.printTrainStatus() + ", " + elapsedTime + " sec");
				startTime = System.currentTimeMillis();

				xab.setGameNumber(gameNum);

				qa = pa;
				// --- OLD, no longer needed, since pa is already wrapped, if wrapping is activated: ---
				// construct 'qa' anew (possibly wrapped agent for eval)
				//qa = wrapAgent(pa, xab.oPar[n], xab.wrPar[n], xab.maxnPar[n], gb.getStateObs());

				eresQ = m_evaluatorQ.eval(qa);		// throws RuntimeException, if TDReferee.agt.zip is not found
				if (doTrainEvaluation) {
					eresT = m_evaluatorT.eval(qa);	// throws RuntimeException, if TDReferee.agt.zip is not found
				}

				// update line chart plot:
				if (lChart != null) 
					lChart.updateChartPlot(gameNum, eresQ, eresT, doTrainEvaluation, false);

				// update weight / TC factor distribution plot:
				if (wChart != null)	wChart.updateChartPlot(gameNum, pa, per);

				elapsedMs = (System.currentTimeMillis() - startTime);
				pa.incrementDurationEvaluationMs(elapsedMs);

				// enable premature exit if TRAIN button is pressed again:
				if (xab.m_arena.taskState != Arena.Task.TRAIN) {
					m_Arena.showMessage("Training stopped prematurely", "Warning", JOptionPane.WARNING_MESSAGE);
					break; // out of while
				}

				startTime = System.currentTimeMillis();
			}

			// --- obsolete, we strip off stopTest ---
//			if (stopTest > 0 && (gameNum - 1) % numEval == 0 && stopEval > 0) {
//				qa = pa;
//				// --- OLD, no longer needed, since pa is already wrapped, if wrapping is activated: ---
//				// construct 'qa' anew (possibly wrapped agent for eval)
//				//qa = wrapAgent(pa, xab.oPar[n], xab.wrPar[n], xab.maxnPar[n], gb.getStateObs());
//
//				if (doTrainEvaluation) {
//					m_evaluatorT.eval(qa);
//					m_evaluatorT.goalReached(gameNum);
//				}
//
//				m_evaluatorQ.eval(qa);
//
//				elapsedMs = (System.currentTimeMillis() - startTime);
//				pa.incrementDurationEvaluationMs(elapsedMs);
//				startTime = System.currentTimeMillis();
//
//				if (m_evaluatorQ.goalReached(gameNum))
//					break; // out of while
//
//			}
		} // while

		// Debug only
		// TDNTupleAgt.pstream.close();

		if (doTrainStatistics) {
			taggList = aggregateTrainStats(tsList);
			System.out.println("--- Train Statistics ---");
			TStats.printTAggregList(taggList);
		}

		// xab.GameNumT.setText(Integer.toString(maxGameNum) ); // restore
		// initial value (maxGameNum)
		// not sensible in case of premature stop;
		// and in case of normal end, it will be maxGameNum anyhow

		// samine
		int test = 2000;
		if (gameNum % test != 0)
			System.out.println(pa.printTrainStatus());

		// -- only debug
		// m_evaluator2.eval();
		// Evaluator2 m_evaluator2New = new Evaluator2(pa,0,2);
		// m_evaluator2New.eval();

		// --- deprecated ---
//		if (stopTest > 0 && stopEval > 0) {
//			System.out.println(m_evaluatorQ.getGoalMsg(gameNum));
//			if (doTrainEvaluation)
//				System.out.println(m_evaluatorT.getGoalMsg(gameNum));
//		}

		System.out.println("final " + eresQ.getMsg());
		if (doTrainEvaluation && eresT.getMsg() != null)
			System.out.println("final " + eresT.getMsg());
		// getMsg() might return null if evaluator mode = -1 (no evaluation)

		long actionNum = pa.getNumLrnActions();
		double totalTrainSec = (double) pa.getDurationTrainingMs() / 1000.0;
						// time [sec] spent in trainAgent since start of this training 
						// (only self-play, excluding evaluations)
		double movesSecond   = actionNum / totalTrainSec;    
						// average number of moves per second since start of this training 
						// (counting only training time, excluding evaluation time)
		DecimalFormat frm1 = new DecimalFormat("#0.00");
		System.out.println("moves/s: "+frm1.format(movesSecond)+" [(#trainAgent-calls)/(total time spent in trainAgent)]. ");
		double totalEvalSec = (double) pa.getDurationEvaluationMs() / 1000.0;
		System.out.println("total train + eval time: "+frm1.format(totalTrainSec)+" + " + frm1.format(totalEvalSec)+" sec ");

		return pa;
	} // train

	private StateObservation soSelectStartState(GameBoard gb, boolean chooseStart01, PlayAgent pa) {
		StateObservation so;
		if (chooseStart01) {
			so = gb.chooseStartState(pa);
		} else {
			so = gb.getDefaultStartState();
		}
		return so;
	}

	private void collectTrainStats(ArrayList<TStats> tsList, PlayAgent pa, StateObservation so) {
		int n = pa.getGameNum();
		int p = so.getMinEpisodeLength();
		int moveNum = pa.getMoveCounter();
		int epiLength = pa.getParOther().getEpisodeLength();
		if (epiLength==-1) epiLength=Integer.MAX_VALUE;
		TStats tstats = new TStats(n, p, moveNum, epiLength);
		tsList.add(tstats);
	}

	private ArrayList<TAggreg> aggregateTrainStats(ArrayList<TStats> tsList) {
		ArrayList<TAggreg> taggList = new ArrayList<>();
		TStats tstats;
		TAggreg tagg;
		HashSet<Integer> pvalues = new HashSet<>();
		for (TStats tStats : tsList) {
			tstats = tStats;
			pvalues.add(tstats.p);
		}
		for (Integer pvalue : pvalues) {
			//int p = (int) pvalue;
			tagg = new TAggreg(tsList, pvalue);
			taggList.add(tagg);
		}
		return taggList;
	}

	/**
	 * Perform trainNum runs of training and evaluation for PlayAgent, and
	 * perform each self-play training with maxGameNum training games. Both
	 * trainNum and maxGameNum are inferred from {@code xab}.<br>
	 * Write results to <b>{@code agents/<gameDir>/csv/<csvName>}</b>, see below.
	 * 
	 * @param n			index of agent to train (the current GUI will call multiTrain
	 *            		always with n=0
	 * @param sAgent    a string containing the class name of the agent
	 * @param xab       used only for reading parameter values from members td_par et al.
	 * @param gb        the game board, needed for evaluators and start state selection
	 * @param csvName   results are written to this filename
	 * @return          the (last) trained agent
	 * <p>
	 * Side effect: writes results of multi-training to 
	 * <b>{@code agents/<gameDir>/csv/<csvName>}</b>. This file has the columns: <br>
	 * 
	 *     {@code run, gameNum, evalQ, evalT, actionNum, trnMoves, totalTrainSec, movesSecond, userValue1, userValue2}. <br>
	 *     
	 * See {@link MTrain} for further info on these columns. The contents may be visualized with one of the 
	 * R-scripts found in {@code resources\R_plotTools}.
	 * 
	 * @see MTrain
	 */
	public PlayAgent multiTrain(int n, String sAgent, XArenaButtons xab, GameBoard gb, String csvName)
	{
		DecimalFormat frm3 = new DecimalFormat("+0.000;-0.000");
		DecimalFormat frm = new DecimalFormat("#0.000");
		DecimalFormat frm2 = new DecimalFormat("+0.00;-0.00");
		DecimalFormat frm1 = new DecimalFormat("#0.00");
		String userTitle1 = "", userTitle2 = "";
		double userValue1 = 0., userValue2 = 0.0;
		long elapsedMs;
		int stopEval = 0;
		boolean doTrainEvaluation = false;
		EvalResult 	eresQ = new EvalResult(),
					eresT = new EvalResult();

		int trainNum = xab.getTrainNumber();
		int maxGameNum = xab.getGameNumber();
		PlayAgent pa = null, qa;

		System.out.println("*** Starting multiTrain with trainNum = " + trainNum + " ***");

		Measure oQ = new Measure(); // quick eval measure
		Measure oT = new Measure(); // train eval measure
		MTrain mTrain;
		double evalQ, evalT=0.0;
		ArrayList<MTrain> mtList = new ArrayList<>();

		for (int i = 0; i < trainNum; i++) {
			int gameNum;
			long actionNum, trnMoveNum;
			double totalTrainSec, elapsedTime, movesSecond;

			xab.setTrainNumberText(trainNum, (i + 1) + "/" + trainNum);

			// --- DON'T use this anymore! Use instead GBGBatch.multiTrainAlphaSweep or 
			// --- GBGBatch.multiTrainLambdaSweep.
			//
			// //add here - if wanted - user-specific code which varies for each i some of the
			// //parameters and writes them to userValue*:
			// //*** DON'T FORGET to comment it out again if you want to have normal behavior back
			// //*** (same settings for all runs i)
			// double lambda = i*0.1;
			// userTitle1="lambda"; userValue1=lambda;
			// userTitle2="null"; userValue2=0.0;
			// xab.tdPar[0].setLambda(i*0.1);

			try {
				pa = constructAgent(n, sAgent, xab);
				if (pa == null)
					throw new RuntimeException("Could not construct AgentX = " + sAgent);
				qa = wrapAgentTrain(pa, xab.oPar[n], xab.wrPar[n], xab.maxnPar[n], gb.getDefaultStartState());

				if (qa == null)
					throw new RuntimeException("Could not wrap agent = " + sAgent);
				pa = qa;
				pa.setWrapperParamsO(xab.oPar[n]);
			} catch (RuntimeException e) {
				m_Arena.showMessage(e.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
				return pa;
			}

			// bug fix: numEval needs to be set *after* constructAgent (which contains xab.oPar[n].pushFromOTParams)
			int numEval = xab.oPar[n].getNumEval();
			if (numEval == 0)
				numEval = 500; // just for safety, to avoid ArithmeticException in
								// 'gameNum%numEval' below
			
			int qem = xab.oPar[n].getQuickEvalMode();
			m_evaluatorQ = xab.m_arena.makeEvaluator(pa, gb, stopEval, qem, 1);
			int tem = xab.oPar[n].getTrainEvalMode();
			//
			// doTrainEvaluation flags whether Train Evaluator is executed:
			// Evaluator m_evaluatorT is only constructed and evaluated, if in
			// tab 'Other pars'
			// the choice box 'Train Eval Mode' is not -1 ("none").
			doTrainEvaluation = (tem != -1);
			if (doTrainEvaluation)
				m_evaluatorT = xab.m_arena.makeEvaluator(pa, gb, stopEval, tem, 1);

			// if (i==0) {
			System.out.println(pa.stringDescr());
			System.out.println(pa.stringDescr2());
			// }
			pa.setMaxGameNum(maxGameNum);
			pa.setGameNum(0);
			long startTime = System.currentTimeMillis();
			gb.initialize();
			while (pa.getGameNum() < pa.getMaxGameNum()) {
				StateObservation so = soSelectStartState(gb, xab.oPar[n].getChooseStart01(), pa);

				pa.trainAgent(so);

				gameNum = pa.getGameNum();
				if (gameNum % numEval == 0) { // || gameNum==1) {
					elapsedMs = (System.currentTimeMillis() - startTime);	// ms spent in last numEval trainAgent calls
					pa.incrementDurationTrainingMs(elapsedMs);
					elapsedTime = (double) elapsedMs / 1000.0;
					// elapsedTime: time [sec] for the last numEval training
					// episodes (not counting the evaluation time)

					startTime = System.currentTimeMillis();

					xab.setGameNumber(gameNum);

					qa = pa;
					// --- OLD, no longer needed, since pa is already wrapped, if wrapping is activated: ---
					// construct 'qa' anew (possibly wrapped agent for eval)
					//qa = wrapAgent(pa, xab.oPar[n],  xab.wrPar[n], xab.maxnPar[n], gb.getStateObs());

					eresQ = m_evaluatorQ.eval(qa);			// throws RuntimeException, if TDReferee.agt.zip is not found
					evalQ = eresQ.getResult();
					if (doTrainEvaluation) {
						eresT = m_evaluatorT.eval(qa);		// throws RuntimeException, if TDReferee.agt.zip is not found
						evalT = eresT.getResult();
					}

					// gather information for later printout to
					// agents/gameName/csv/multiTrain.csv.
					actionNum = pa.getNumLrnActions();
					trnMoveNum = pa.getNumTrnMoves();
					totalTrainSec = (double) pa.getDurationTrainingMs() / 1000.0;
									// time [sec] spent in trainAgent since start of this training run
									// (only self-play, excluding evaluations)
					movesSecond   = actionNum / totalTrainSec;    
									// average number of moves per second since start of this training run
									// (counting only training time, excluding evaluation time)
					mTrain = new MTrain(i, gameNum, evalQ, evalT, actionNum, trnMoveNum, totalTrainSec,
							movesSecond, userValue1, userValue2);
					mtList.add(mTrain);
					
					System.out.println(pa.printTrainStatus() + ", " + elapsedTime + " sec, " + frm1.format(movesSecond) +" moves/s");

					elapsedMs = (System.currentTimeMillis() - startTime); 	// ms spent for evaluation
					pa.incrementDurationEvaluationMs(elapsedMs);

					// enable premature exit if MULTITRAIN button is pressed
					// again:
					if (xab.m_arena.taskState != Arena.Task.MULTTRN) {
						m_Arena.showMessage("MultiTraining stopped prematurely", "Warning",
								JOptionPane.WARNING_MESSAGE);
						break; // out of while
					}

					startTime = System.currentTimeMillis();		// start the timer for next numEval train episodes
				}
			} // end while

			//
			// things to do at the end of a training run:
			//

			qa = pa;
			// --- OLD, no longer needed, since pa is already wrapped, if wrapping is activated: ---
			// construct 'qa' anew (possibly wrapped agent for eval)
			//qa = wrapAgent(pa, xab.oPar[n], xab.wrPar[n], xab.maxnPar[n], gb.getStateObs());

			// final evaluation:
			eresQ = m_evaluatorQ.eval(qa);
			oQ.add(eresQ.getResult());
			if (doTrainEvaluation) {
				eresT = m_evaluatorT.eval(qa);
				oT.add(eresT.getResult());
			}

			elapsedMs = (System.currentTimeMillis() - startTime);	// ms spent for final evaluation
			pa.incrementDurationEvaluationMs(elapsedMs);
			pa.setAgentState(PlayAgent.AgentState.TRAINED);

			// print the full list mtList after finishing each run i
			// (overwrites the file written from previous run i-1)
			MTrain.printMultiTrainList(csvName, mtList, pa, m_Arena, userTitle1, userTitle2);

			if (xab.m_arena.taskState != Arena.Task.MULTTRN) {
				break; // out of for
			}
		} // for (i)

		if (eresQ.getMode() != (-1))
		// m_mode=-1 signals: 'no evaluation done' --> oT did not receive
		// evaluation results
		{
			System.out.println("Avg. " + eresQ.getMsg() + frm3.format(oQ.getMean()) + " +- "
					+ frm.format(oQ.getStd()));
		}
		if (doTrainEvaluation && eresT.getMode() != (-1))
		// m_mode=-1 signals: 'no evaluation done' --> oT did not receive
		// evaluation results
		{
			System.out.println("Avg. " + eresT.getMsg() + frm3.format(oT.getMean()) + " +- "
					+ frm.format(oT.getStd()));
		}
		if (eresQ.getMode() == (-1)) {
			this.lastMsg = "Warning: No evaluation done (Quick Eval Mode = -1)";
		} else {
			this.lastMsg = (eresQ.getMsg() + frm2.format(oQ.getMean()) + " +- "
					+ frm1.format(oQ.getStd()) + "");
		}

		xab.setTrainNumber(trainNum);
		return pa;

	} // multiTrain

	// --- the generalization of old method compete() to arbitrary N players ---
	/**
	 * Perform a competition of the agents in {@code paVector}, consisting of
	 * {@code competeNum} episodes, starting from StateObservation
	 * {@code startSO}.
	 * 
	 * @param paVector a vector with N agents for an N-player game. This vector is NOT shifted.
	 * @param startSO
	 *            the start board position for the game
	 * @param competeNum
	 *            the number of episodes to play
	 * @param verbose
	 *            0: silent, 1,2: more print-out
	 * @param nextTimes
	 *            storage to save time measurements, null if not needed
	 *            (currently only used by tournament system). If
	 *            {@code nextTimes} is not null, some extra info for the
	 *            tournament log is printed to {@code System.out}.
	 * @return a score tuple which holds in the kth position the average score
	 *         of the kth agent from all {@code competeNum} episodes.
	 *
	 * @see #competeNPlayerAllRoles(PlayAgtVector, StateObservation, int, int)
	 */
	public static ScoreTuple competeNPlayer(PlayAgtVector paVector, StateObservation startSO, int competeNum,
			int verbose, TSTimeStorage[] nextTimes) {
		int numPlayers = paVector.getNumPlayers();
		ScoreTuple sc, scMean = new ScoreTuple(numPlayers);
		double sWeight = 1 / (double) competeNum;
		double moveCount = 0.0;
		DecimalFormat frm = new DecimalFormat("#0.000");
		boolean nextMoveSilent = (verbose < 2);
		StateObservation so;
		Types.ACTIONS_VT actBest;
		String sMsg;

		String[] pa_string = new String[numPlayers];
		for (int i = 0; i < numPlayers; i++)
			pa_string[i] = paVector.pavec[i].stringDescr();

		switch (numPlayers) {
			case (1) -> sMsg = "Competition, " + competeNum + " episodes: \n" + pa_string[0];
			case (2) -> sMsg = "Competition, " + competeNum + " episodes: \n" +
					"      X: " + pa_string[0] + " \n" + "   vs O: " + pa_string[1];
			default -> {
				sMsg = "Competition, " + competeNum + " episodes: \n";
				for (int n = 0; n < numPlayers; n++) {
					sMsg = sMsg + "    P" + n + ": " + pa_string[n];
					if (n < numPlayers - 1)
						sMsg = sMsg + ", \n";
				}
			}
		}
		if (verbose > 0)
			System.out.println(sMsg);

		if (nextTimes != null) {
			// some diagnostic info to print when called via tournament system:
			System.out.println("Competition: " + competeNum + " episodes " + pa_string[0] + " vs "
					+ pa_string[1]/*
			 * +" with "+rndmStartMoves+
			 * " random startmoves"
			 */);
			String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH.mm.ss"));
			System.out.println("Episode Start @ " + currDateTime);
			System.out.println("start state: " + startSO);
		}

		for (int k = 0; k < competeNum; k++) {
			for (int i = 0; i < numPlayers; i++)
				paVector.pavec[i].resetAgent();

			int player = startSO.getPlayer();
			so = startSO.copy();

			if(so.needsRandomization()) {
				// Randomizing the start state in case a game already is initialized with a state to make sure there is a fair competition.
				so.randomizeStartState();
			}

			while (true) {
				long startTNano = System.nanoTime();
				actBest = paVector.pavec[player].getNextAction2(so.partialState(), false, nextMoveSilent);
				long endTNano = System.nanoTime();
				if (nextTimes != null)
					nextTimes[player].addNewTimeNS(endTNano - startTNano);
				so.advance(actBest);
				so.storeBestActionInfo(actBest);	// /WK/ added 2021-09-10, but probably never needed


				if (so.isGameOver()) {
					sc = so.getGameScoreTuple();
					scMean.combine(sc, ScoreTuple.CombineOP.AVG, 0, sWeight);
					moveCount += so.getMoveCounter();
					if (verbose > 0)
						System.out.println(sc.printEpisodeWinner(k));

					break; // out of while

				} // if (so.isGameOver())
				if(so.isRoundOver()&&!so.isGameOver()) {
					so.initRound();
					assert !so.isRoundOver() : "Error: initRound() did not reset round-over-flag";
				}

				player = so.getPlayer();
			} // while(true)

		} // for (k)

		moveCount /= competeNum;

		if (verbose > 0) {
			if (verbose > 1) {
				System.out.print("Avg ScoreTuple for all players: ");
				System.out.println("   " + scMean.toStringFrm());
			}
			System.out.println("Avg # moves in " + competeNum + " episodes = " + frm.format(moveCount));
		}

		return scMean;
	}

	// --- the generalization of old competeBoth to arbitrary N players ---
	/**
	 * Perform a competition of the agents in {@code paVector}. This competition consists of
	 * {@code competeNum}*{@code N} episodes, all starting from StateObservation
	 * {@code startSO}. Here, {@code N} is the number of players and each agent
	 * plays cyclically each role: the 1st, 2nd, ... {@code N}th player. The
	 * results are mapped back to the score tuple of the 1st role
	 * 
	 * @param paVector a vector with N agents for an N-player game
	 * @param startSO
	 *            the start board position for the game
	 * @param competeNum
	 *            the number of episodes to play
	 * @param verbose
	 *            0: silent, 1,2: more print-out
	 * @return a score tuple which holds in the kth position the average score
	 *         for the kth agent from all {@code competeNum}*{@code N} episodes.
	 *
	 * @see #competeNPlayer(PlayAgtVector, StateObservation, int, int, TSTimeStorage[])
	 */
	public static ScoreTuple competeNPlayerAllRoles(PlayAgtVector paVector, StateObservation startSO, int competeNum,
			int verbose) {
		int N = startSO.getNumPlayers();
		double sWeight = 1 / (double) N;
		ScoreTuple sc, shiftedTuple, scMean = new ScoreTuple(N);
		PlayAgtVector qaVector;
		for (int k = 0; k < N; k++) {
			qaVector = paVector.shift(k);
			sc = competeNPlayer(qaVector, startSO, competeNum, verbose, null);
			shiftedTuple = sc.shift(N - k);
			scMean.combine(shiftedTuple, ScoreTuple.CombineOP.AVG, 0, sWeight);
		}
		return scMean;
	}
	/**
	 * Does the main work for menu items 'Single Compete', 'Swap Compete' and
	 * 'Compete All Roles'. These menu items set enum {@link Arena#taskState} to
	 * either COMPETE or SWAPCMP or BOTHCMP. Then the appropriate cases of
	 * {@code switch} in Arena.run() will call competeDispatcher.
	 * <p>
	 * 'Single Compete' performs {@code competeNum} competitions of the agents
	 * in the indicated places. 'Swap Compete' (only 2-player games) performs
	 * competeNum competitions AgentX as O vs. AgentO as X. 'Compete All Roles'
	 * lets each agent play in each place or role 0, 1, ..., N. The results are
	 * shifted back to the agents' initial placement.
	 * <p>
	 * The agents are fetched from {@code xab} and are assumed to be trained
	 * (!). The parameters for the agents are fetched from the param tabs. The
	 * parameter {@code competeNum} is fetched from the Competition options
	 * window ("# of games per competition").
	 * 
	 * @param swap
	 *            {@code false} for 'Compete' and {@code true} for 'Swap
	 *            Compete' (only 2-player games)
	 * @param allRoles
	 *            {@code true} for 'Compete All Roles' ({@code swap} is then
	 *            irrelevant)
	 * @param xab
	 *            used only for reading parameter values from GUI members
	 * @param gb
	 *            needed for start state
	 * @return the fitness of the first agent, which is in the range
	 *         [-1.0,+1.0]: +1.0 if the first agent always wins, 0.0 if always
	 *         tie or if #win=#loose, and -1.0 if AgentX always looses.
	 */
	protected double competeDispatcher(boolean swap, boolean allRoles, XArenaButtons xab, GameBoard gb) {
		int competeNum = xab.winCompOptions.getNumGames();
		StateObservation startSO = gb.getDefaultStartState(); // empty board
		int numPlayers = startSO.getNumPlayers();

		try {
			for (int i = 0; i < numPlayers; i++)
				if (xab.getSelectedAgent(i).equals("Human")) {
					m_Arena.showMessage("No compete for agent Human", "Error", JOptionPane.ERROR_MESSAGE);
					return 0.0;
				}

			PlayAgent[] paVector = fetchAgents(xab);

			AgentBase.validTrainedAgents(paVector, numPlayers); // may throw
																// RuntimeException

			PlayAgent[] qaVector = wrapAgents(paVector, xab, startSO);

			int verbose = 1;

			if (allRoles) {
				ScoreTuple sc = competeNPlayerAllRoles(new PlayAgtVector(qaVector), startSO, competeNum, verbose);
				System.out.println("Avg score for all players: " + sc.toStringFrm());
				return sc.scTup[0];
			} else {
				if (swap) {
					ScoreTuple sc = competeNPlayer(new PlayAgtVector(qaVector[1], qaVector[0]), startSO, competeNum,
							verbose, null);
					System.out.println("Avg score for all players: " + sc.toStringFrm());
					return sc.scTup[1];
				} else {
					ScoreTuple sc = competeNPlayer(new PlayAgtVector(qaVector), startSO, competeNum, verbose, null);
					System.out.println("Avg score for all players: " + sc.toStringFrm());
					return sc.scTup[0];
				}
			}

		} catch (RuntimeException ex) {
			m_Arena.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return 0;
		}
	} // competeDispatcher

	public double singleCompete(XArenaButtons xab, GameBoard gb) {
		return this.competeDispatcher(false, false, xab, gb);
	}

	public double swapCompete(XArenaButtons xab, GameBoard gb) {
		return this.competeDispatcher(true, false, xab, gb);
	}

	public double allCompete(XArenaButtons xab, GameBoard gb) {
		return this.competeDispatcher(false, true, xab, gb);
	}

	/**
	 * This is an adapted version of
	 * {@link XArenaFuncs#competeDispatcher(boolean, boolean, XArenaButtons, GameBoard)
	 * XArenaFuncs.competeDispatcher()} which is called for each match during a
	 * tournament. The tournament parameters {@code dataTS} are added. Each
	 * match is currently fixed to one episode per match ({@code competeNum=1})
	 * 
	 * @param gb
	 *            game board to play on
	 * @param xab
	 *            used to access the param tabs for standard agents
	 * @param dataTS
	 *            helper class containing data and settings for the next match
	 *            in the tournament
	 * @return info who wins [(0/1/2) if (X/tie/O) wins] or error code (&gt;40)
	 */
	protected int competeDispatcherTS(GameBoard gb, XArenaButtons xab, TSGameDataTransfer dataTS) {
		int competeNum = 1;// xab.winCompOptions.getNumGames(); | if value > 1
							// then adjust competeTS()!
		int numPlayers = gb.getStateObs().getNumPlayers();
		// double[] c = {}; // win rate how often = [0]:agentX wins [1]: ties
		// [2]: agentO wins
		ScoreTuple sc;
		final String TAG = "[XArenaFuncs] ";

		try {
			String AgentX = dataTS.nextTeam[0].getAgentType();
			String AgentO = dataTS.nextTeam[1].getAgentType();
			if (numPlayers != 2) {
				throw new RuntimeException("competeDispatcherTS() only for N==2");
			}
			if (AgentX.equals("Human") | AgentO.equals("Human")) {
				throw new RuntimeException("No compete for agent Human, select different agent");
			} else {
				StateObservation startSO = gb.getDefaultStartState(); // empty
																		// board

				// manipulation of selected standard agent in XArenaButtons!
				xab.enableTournamentRemoteData(dataTS.nextTeam);

				// prepare agents
				PlayAgent[] paVector;
				PlayAgent[] qaVector;
				if (dataTS.nextTeam[0].isHddAgent() && dataTS.nextTeam[1].isHddAgent()) {
					paVector = new PlayAgent[2];
					paVector[0] = dataTS.nextTeam[0].getPlayAgent();
					paVector[1] = dataTS.nextTeam[1].getPlayAgent();
					AgentBase.validTrainedAgents(paVector, numPlayers); // may
																		// throw
																		// RuntimeException
					qaVector = wrapAgents_TS(paVector, xab, startSO);
				} else {
					paVector = fetchAgents(xab);
					if (dataTS.nextTeam[0].isHddAgent()) {
						paVector[0] = dataTS.nextTeam[0].getPlayAgent();
					}
					if (dataTS.nextTeam[1].isHddAgent()) {
						paVector[1] = dataTS.nextTeam[1].getPlayAgent();
					}
					AgentBase.validTrainedAgents(paVector, numPlayers); // may
																		// throw
																		// RuntimeException
					qaVector = wrapAgents(paVector, xab, startSO);
				}

				sc = competeNPlayer(new PlayAgtVector(qaVector), dataTS.startSO, competeNum, 0, dataTS.nextTimes);

				xab.disableTournamentRemoteData();
			}

		} catch (RuntimeException ex) {
			m_Arena.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(TAG + "ERROR :: RuntimeException :: " + ex.getMessage());
			return 43;
		}

		if (sc.scTup[0] > sc.scTup[1]) // X wins (more often)
			return 0;
		if (sc.scTup[0] == sc.scTup[1]) // tie or equal number of X and O wins
			return 1;
		if (sc.scTup[0] < sc.scTup[1]) // O wins (more often)
			return 2;

		// we should never arrive here
		throw new RuntimeException("Ooops! in competeDispatcherTS");
	} // competeDispatcherTS

	public String getLastMsg() {
		return lastMsg;
	}

}
