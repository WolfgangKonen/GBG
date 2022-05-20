package controllers.TD;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import agentIO.LoadSaveGBG;
import params.ParOther;
import params.ParTD;
import tools.Types;
import controllers.AgentBase;
import controllers.PlayAgent;
import games.Arena;
import games.Feature;
import games.GameBoard;
import games.StateObservation;

/**
 * The TD-Learning {@link PlayAgent} (Temporal Difference reinforcement learning). 
 * It has either a linear net {@link TD_Lin} or a BP neural net {@link TD_NNet}
 * to model the value function. 
 * The net follows closely the (pseudo-)code by [SuttonBonde93]. 
 * <p>
 * The internal learning rate ALPHA for the net input layer weights is alpha/n,
 * where n=(size of feature vector) and alpha is the constructors' first
 * parameter.
 * <p>
 * Some functionality is packed in the superclass 
 * {@link AgentBase} (gameNum, maxGameNum, AgentState)
 * 
 * @see PlayAgent
 * @see AgentBase
 * 
 * @author Wolfgang Konen, TH Koeln, 2016
 */
//abstract 
public class TDAgent extends AgentBase implements PlayAgent,Serializable {
	protected TD_func m_Net;
	/**
	 * Controls the amount of explorative moves in
	 * {@link #getNextAction2(StateObservation, boolean, boolean)}
	 * during training. <br>
	 * m_epsilon = 0.0: no random moves, <br>
	 * m_epsilon = 0.1 (def.): 10% of the moves are random, and so forth
	 * m_epsilon undergoes a linear change from {@code tdPar.getEpsilon()} 
	 * to {@code tdPar.getEpsilonFinal()}. 
	 * This is realized in {@link TD_Lin#finishUpdateWeights()}.
	 */
	private double m_epsilon = 0.1;
	
	/**
	 * m_EpsilonChangeDelta is the epsilon change per episode.
	 */
	private double m_EpsilonChangeDelta = 0.001;
	
	private double BestScore;

	protected int hiddenSize = 15; // size of hidden layer (only for TD_NNet)
	private Random rand;
	private int numFinishedGames = 0;
	private boolean randomSelect = false;
//	private boolean learnFromRM = false;    // use now m_oPar.useLearnFromRM() - don't store/maintain value twice
	private boolean NORMALIZE = false; 

	protected Feature m_feature;
	
	/**
	 * Members {@code #m_tdPar} and {@link AgentBase#m_oPar} are needed for saving and loading
	 * the agent (to restore the agent with all its parameter settings)
	 */
	private ParTD m_tdPar;		// TODO transform to ParTD
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	@Serial
	private static final long  serialVersionUID = 12L;
	
	//public int epiCount=0;

	/**
	 * Default constructor for TDAgent, needed for loading a serialized version
	 */
	public TDAgent() {
		super();
		ParTD tdPar = new ParTD();
		ParOther oPar = new ParOther();		
		initNet(tdPar, oPar, null, 1000);
	}

	/**
	 * Construct new {@link TDAgent}, setting everything from tdPar and from maxGameNum
	 * 
	 * @param tdPar			TD parameters
	 * @param maxGameNum	max number of training episodes
	 */
	public TDAgent(String name, ParTD tdPar, ParOther oPar, Feature feature, int maxGameNum) {
		super(name);
		initNet(tdPar, oPar, feature, maxGameNum);
	}

	/**
	 *
	 * @param tdPar			TD parameters
	 * @param maxGameNum	max number of training episodes
	 */
	private void initNet(ParTD tdPar, ParOther oPar, Feature feature, int maxGameNum) {
		m_tdPar = new ParTD(tdPar);
		m_oPar = new ParOther(oPar);  		// AgentBase::m_oPar
		m_feature = feature; 
		
			if (m_feature.getFeatmode() > 99) {
				m_Net = null;
			} else {
				if (tdPar.hasLinearNet()) {
					m_Net = new TD_Lin(m_feature.getInputSize(m_feature.getFeatmode()),
							//OLD (and wrong): getInputSize(m_feature.getFeatmode()),
							tdPar.hasSigmoid());
				} else {
					m_Net = new TD_NNet(m_feature.getInputSize(m_feature.getFeatmode()),
							//OLD (and wrong): getInputSize(m_feature.getFeatmode()),
							hiddenSize, tdPar.hasSigmoid());
				}
				// set alpha,beta,gamma,lambda & epochMax,rpropLrn from the TDpars
				// tab
				this.setTDParams(tdPar, maxGameNum);
			}
		
		// m_EPS=eps;
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
		NORMALIZE=tdPar.getNormalize();
		rand = new Random(System.currentTimeMillis());
		setAgentState(AgentState.INIT);
	}

	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		super.instantiateAfterLoading();
		if (this.getParOther() == null )
			this.setDefaultParOther();
		if (this.getParReplay() == null )
			this.setDefaultParReplay();
		return true;
	}
	
	/**
	 * After loading an agent from disk fill the param tabs of {@link Arena} according to the
	 * settings of this agent
	 * 
	 * @param n         fill the {@code n}th parameter tab
	 * @param m_arena	member {@code m_xab} has the param tabs
	 * 
	 * @see Arena#loadAgent
	 */
	public void fillParamTabsAfterLoading(int n, Arena m_arena) { 
		m_arena.m_xab.setTdParFrom(n, this.getParTD() );
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
	}
	
	/**
	 * Get the best next action and return it 
	 * (NEW version: ACTIONS_VT and recursive part for multi-moves)
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent        no printout
	 * @return actBest		the best action. If several actions have the same
	 * 						score, break ties by selecting one of them at random. 
	 * <p>						
	 * actBest has predicate isRandomAction()  (true: if action was selected 
	 * at random, false: if action was selected by agent).<br>
	 * actBest has also the members vTable and vBest to store the value for each available
	 * action (as returned by so.getAvailableActions()) and the value for the best action actBest.
	 */
	@Override
	public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) 
	{
		if (so.getNumPlayers()>2)
			return getNextAction4(so, so, random, silent);
		else
			return getNextAction3(so, so, random, silent);
	}
	private Types.ACTIONS_VT getNextAction3(StateObservation so, StateObservation refer, 
			boolean random, boolean silent) {
		int i;
		double CurrentScore;     	// NetScore*Player, the quantity to be
									// maximized
		StateObservation NewSO;
		Types.ACTIONS thisAct;
        Types.ACTIONS actBest;
        Types.ACTIONS_VT actBestVT;
		BestScore = -Double.MAX_VALUE;
		double[] VTable;
       
		int player = Types.PLAYER_PM[refer.getPlayer()];

		// get the best (or eps-greedy random) action

        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
	
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
		List<Types.ACTIONS> bestActions = new ArrayList<>();

        VTable = new double[acts.size()];

		assert acts.size()>0 : "Oops, no available action";
		for(i = 0; i < acts.size(); ++i)
		{
			thisAct = acts.get(i);

			// this is just relevant for game RubiksCube: If an action is the inverse of the last action, it would
			// lead to the previous state again, resulting in a cycle of 2. We avoid such cycles and continue with
			// next pass through for-loop --> beneficial when searching for the solved cube in play & train.
			// If you do not want to skip any action - e.g. when inspecting states - then enter this method with
			// a 'cleared' state {@link StateObserverCubeCleared} {@code so} (lastAction==9).
			//
			// For all other games, usually no return to the previous state is possible. For those games
			// isEqualToInverseOfLastAction returns always false.
			if (thisAct.isEqualToInverseOfLastAction(so))
				continue;	// with next for-pass

            CurrentScore = g3_Evaluate(so,thisAct,refer,silent);
				
			// just a debug check:
			if (Double.isInfinite(CurrentScore)) {
				System.out.println("g3_Evaluate(so,...) is infinite!");
			}
			
			CurrentScore = normalize2(CurrentScore,so);

			VTable[i] = CurrentScore;
			//
			// Calculate the best value and actBest.
			// If there are multiple best actions, select afterwards one of them randomly
			// (better exploration)
			//
			if (BestScore < CurrentScore) {
				BestScore = CurrentScore;
				bestActions.clear();
				bestActions.add(acts.get(i));
			} else if (BestScore == CurrentScore) {
				bestActions.add(acts.get(i));
			}
        } // for
		actBest = bestActions.get(rand.nextInt(bestActions.size()));
		// if several actions have the same best value, select one of them randomly

        assert actBest != null : "Oops, no best action actBest";
		if (!silent) {
			System.out.print("---Best Move: ");
            NewSO = so.copy();
            NewSO.advance(actBest);
			System.out.println(NewSO.stringDescr()+", "+(2*BestScore*player-1));
		}			
		
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, BestScore);
		return actBestVT;
	}

	private Types.ACTIONS_VT getNextAction4(StateObservation so, StateObservation refer, 
			boolean random, boolean silent) {
		int i;
		double CurrentScore;     	// NetScore*Player, the quantity to be
									// maximized
		StateObservation NewSO;
		Types.ACTIONS thisAct;
		Types.ACTIONS actBest;
		Types.ACTIONS_VT actBestVT;
		BestScore = -Double.MAX_VALUE;
		double[] VTable;
       
		int player = refer.getPlayer();

		// get the best (or eps-greedy random) action

        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
		
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
		List<Types.ACTIONS> bestActions = new ArrayList<>();

        VTable = new double[acts.size()];

		assert acts.size()>0 : "Oops, no available action";
		for(i = 0; i < acts.size(); ++i)
		{
            thisAct = acts.get(i);

			// this is just relevant for game RubiksCube: If an action is the inverse of the last action, it would
			// lead to the previous state again, resulting in a cycle of 2. We avoid such cycles and continue with
			// next pass through for-loop --> beneficial when searching for the solved cube in play & train.
			// If you do not want to skip any action - e.g. when inspecting states - then enter this method with
			// a 'cleared' state {@link StateObserverCubeCleared} {@code so} (lastAction==9).
			//
			// For all other games, usually no return to the previous state is possible. For those games
			// isEqualToInverseOfLastAction returns always false.
			if (thisAct.isEqualToInverseOfLastAction(so))
				continue;	// with next for-pass

            CurrentScore = g4_Evaluate(so,thisAct, refer, silent);
				
			// just a debug check:
//			if (Double.isInfinite(CurrentScore)) {
//				System.out.println("getScore(NewSO) is infinite!");
//			}
			
			CurrentScore = normalize2(CurrentScore, so);

			VTable[i] = CurrentScore;
			//
			// Calculate the best value and actBest.
			// If there are multiple best actions, select afterwards one of them randomly
			// (better exploration)
			//
			if (BestScore < CurrentScore) {
				BestScore = CurrentScore;
				bestActions.clear();
				bestActions.add(acts.get(i));
			} else if (BestScore == CurrentScore) {
				bestActions.add(acts.get(i));
			}
        } // for
		actBest = bestActions.get(rand.nextInt(bestActions.size()));
		// if several actions have the same best value, select one of them randomly

        assert actBest != null : "Oops, no best action actBest";
		if (!silent) {
			System.out.print("---Best Move: ");
            NewSO = so.copy();
            NewSO.advance(actBest);
			System.out.println(NewSO.stringDescr()+", "+(2*BestScore*player-1));
		}			
		
		actBestVT = new Types.ACTIONS_VT(actBest.toInt(), randomSelect, VTable, BestScore);
		return actBestVT;
	}

	// calculate CurrentScore:
	// (g3_Evaluate is helper function for getNextAction3)
	private double g3_Evaluate(	StateObservation so, Types.ACTIONS act, StateObservation refer, boolean silent)
	{
		double CurrentScore;
		int player = Types.PLAYER_PM[refer.getPlayer()]; 	 
		StateObservation NewSO;
		Types.ACTIONS_VT actBestVT;

		if (randomSelect) 
		{
			CurrentScore = rand.nextDouble();
			return CurrentScore;
		} 

		// the normal part for the case of single moves:
		NewSO = so.copy();
		NewSO.advance(act);

		// the recursive part (only for deterministic games) is for the case of 
		// multi-moves: the player who just moved gets from StateObservation 
		// the signal for one (or more) additional move(s)
		// todo: are there round-based games that are deterministic?
		if (so.isDeterministicGame() && so.getNumPlayers()>1 && !NewSO.isGameOver())
		{
			int newPlayer =  Types.PLAYER_PM[NewSO.getPlayer()];
			if (newPlayer==player) 
			{
				actBestVT = getNextAction3(NewSO, refer, false, silent);
				NewSO.advance(actBestVT);
				CurrentScore = actBestVT.getVBest();
				return CurrentScore;
			}
		}

		if (NewSO.isGameOver()|| (NewSO.isRoundOver()&&NewSO.isRoundBasedGame()) )
		{
			CurrentScore = NewSO.getGameScore(so.getPlayer());
		} 
		else 
		{
			CurrentScore = getGamma()*player * m_Net.getScore(m_feature.prepareFeatVector(NewSO));
						   // here we ask this agent for its score estimate on NewSO
		}

		if (!silent) 
		{
			System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*player-1));
			//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
		}

		return CurrentScore;
	}

    // calculate CurrentScore: 
	// (g4_Evaluate is helper function for getNextAction4)
    private double g4_Evaluate(	StateObservation so, Types.ACTIONS act, 
    							StateObservation refer, boolean silent) {
    	double CurrentScore;
		int player = so.getPlayer(); 	 
    	StateObservation NewSO;
		if (randomSelect) {
			CurrentScore = rand.nextDouble();
			return CurrentScore;
		} 
        
    	// the normal part for the case of single moves:
        NewSO = so.copy();
        NewSO.advance(act);
        
        //
        // the recursive part (only for deterministic games) is for the case of 
        // multi-moves: the player who just moved gets from StateObservation 
        // the signal for one (or more) additional move(s)
//        if (so.isDeterministicGame() && so.getNumPlayers()>1 && !NewSO.isGameOver()) {
//            int newPlayer =  Types.PLAYER_PM[NewSO.getPlayer()];
//            if (newPlayer==player) {
//            	actBestVT = getNextAction3(NewSO, refer, false, silent);
//            	NewSO.advance(actBestVT);
//            	CurrentScore = actBestVT.getVBest();
//            	return CurrentScore;
//            }
//        }
//        
        //very inefficient, change later
		if (NewSO.isGameOver()|| (NewSO.isRoundOver()&&NewSO.isRoundBasedGame()) )
			CurrentScore = NewSO.getGameScore(so.getPlayer());
		else 
			CurrentScore = getGamma()* m_Net.getScore(m_feature.prepareFeatVector(NewSO));
									   // here we ask this agent for its score estimate on NewSO
		if (!silent) {
			System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*player-1));
			//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
		}

		return CurrentScore;
    }

//	/**
//	 * Return the agent's estimate of the score for that after state.
//	 *
//	 * @param so			the current game state;
//	 * @return V(), the prob. that X (Player +1) wins from that after state.
//	 *         Player*V() is the quantity to be maximized by getNextAction2.
//	 */
//	public double getScore(StateObservation so) {
//		return m_Net.getScore(m_feature.prepareFeatVector(so));
//	}


	/**
	 * Train the Agent for one complete game episode. <p>
	 * <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @return			true, if agent raised a stop condition (only CMAPlayer)
	 */
	public boolean trainAgent(StateObservation so) { return trainAgent(so,this); }

	/**
	 * Train the agent for one complete game episode <b>using self-play</b>.
	 * <p>
	 * Side effects: Increment m_GameNum and {@code acting_pa}'s gameNum by +1.
	 * Change the agent's internal parameters (weights and so on).
	 * <p>
	 * This method is used by the wrappers: They call it with {@code this} being the wrapped agent (it has the internal
	 * parameters) and {@code acting_pa} being the wrapper.
	 *
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
	 * @param acting_pa the agent to be called when an action is requested ({@code getNextAction2})
	 * @return			true, if agent raised a stop condition (only CMAPlayer - deprecated)
	 */
	public boolean trainAgent(StateObservation so, PlayAgent acting_pa) {
		if(so.getNumPlayers() > 2)
			return trainAgent3player(so,acting_pa);
		else
			return trainAgent2player(so,acting_pa);
	}
	
	private boolean trainAgent2player(StateObservation so, PlayAgent acting_pa)
	{
		double reward;
		boolean randomMove;
		boolean finished = false;
		boolean wghtChange;
		boolean DEBG = false;
		double[] Input, oldInput;
		int player;
		Types.ACTIONS_VT actBest;
		StateObservation oldSO;

		boolean learnFromRM = m_oPar.getLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;

		player = Types.PLAYER_PM[so.getPlayer()];

		m_Net.resetElig(); // reset the elig traces before starting a new game
							// /WK/ NEW/02/2015
		oldInput = m_feature.prepareFeatVector(so);
		m_Net.calcScoresAndElig(oldInput);
		if (DEBG) {
			for (double v : oldInput) System.out.print((int) ((3 + v) % 3));
			System.out.println();
		}
		int counter=0;		// count the number of moves
		while (true) {
			actBest = acting_pa.getNextAction2(so.partialState(), true, true);
			randomMove = actBest.isRandomAction();
			oldSO = so.copy();
			so.advance(actBest);
			so.storeBestActionInfo(actBest);	// /WK/ was missing before 2021-09-10. Now stored ScoreTuple is up-to-date.

			//couldn't we just train till round over?
			//if(so.isRoundOver()&&!so.isGameOver()){
			//	so.initRound();
			//}
			if (so.isGameOver()|| (so.isRoundOver()&&so.isRoundBasedGame()) ) {
				// Fetch a reward and normalize it to the range [0,1], since 
				// TD_NNet may build a value function with a sigmoid function
				// mapping to [0,1]. Then it can use only rewards in [0,1].
				
//				switch (so.getNumPlayers()) {
//				case 1: 
//					reward = so.getGameScore();
//					break;
//				case 2: 
//					reward = (-player)*so.getGameScore();
//					// so.getGameScore() returns -1, if 'player', that is the
//					// one who *made* the move to 'so', has won. If we multiply
//					// this by (-player), we get a reward +1 for a X(player=+1)- 
//					// win and a reward -1 for an O(player=-1)-win.
//					// And a reward 0 for a tie.
//					break;
//				default: 
//					throw new RuntimeException("TDPlayer.trainAgent not yet "+
//							"implementing case so.getNumPlayers()>2");
//				}
				
				// the whole switch-statement above can be replaced with the simpler  
				// logic of so.getGameScore(StateObservation referingState), where  
				// referingState is 'oldSO', the state before so. [This should be  
				// extensible to 3- or 4-player games (!) as well, if we put the 
				// proper logic into method getGameScore(referingState).]  
				reward = player*so.getGameScore(oldSO.getPlayer());
				
				reward = normalize2(reward,so);

				finished = true;
			} else {
				//it is irrelevant what we put into reward here, because it will 
				//not be used in m_Net.updateWeights when finished is not true.
				//
				// ??? has to be re-thought for the case of 2048 and other 1-player games!!!
				reward = 0.0;
			}
			counter++;
			if (counter==epiLength) {
				reward=estimateGameValueTuple(so, null).scTup[so.getPlayer()];
				//epiCount++;
				finished = true; 
			}
			Input = m_feature.prepareFeatVector(so);
			if (randomMove && !finished && !learnFromRM) {
				// no training, go to next move
				m_Net.calcScoresAndElig(Input); // calculate score, write it to
												// old_y[k] for
												// next pass & update
												// eligibilities (NEW
												// WK/02/2015)
				// only for diagnostics
				if (DEBG)
					System.out.println("random move");

			} else {
				// do one training step
				
				// this is the accumulation logic: if eMax>0, then form 
				// mini batches and apply the weight changes only at the end
				// of such mini batches (after eMax complete games)
				int eMax = super.getEpochMax();
				if (eMax==0) {
					wghtChange=true;
				} else {
					if (finished) numFinishedGames++;
					wghtChange = (finished && (numFinishedGames % eMax) == 0);
				}
				
				// either no random move or game is finished >> target signal is
				// meaningful!
				m_Net.updateWeights(reward, Input, finished, wghtChange);
				// contains afterwards a m_Net.calcScoresAndElig(Input);

			}

//			oldInput = Input;
			
			if (finished) {
				if (DEBG)
					if (randomMove)
						System.out.println("Terminated by random move");
				break;
			}
			if (DEBG) {
				for (double v : Input) System.out.print((int) ((3 + v) % 3));
				System.out.println();
			}

			player = Types.PLAYER_PM[so.getPlayer()];   // advance to the next player
		}
		m_Net.finishUpdateWeights(); // adjust learn params ALPHA & BETA
		m_epsilon = m_epsilon - m_EpsilonChangeDelta; 		// linear decrease of m_epsilon 

		incrementGameNum();
		acting_pa.setGameNum(this.getGameNum());
		return false;
	}
	
	private boolean trainAgent3player(StateObservation so, PlayAgent acting_pa)
	{
		boolean randomMove;
		boolean wghtChange;
		boolean DEBG = false;
		boolean firstRound = true;
		double[][] lastInput = new double [3][m_feature.getInputSize(0)];
		//String S_old, I_old = null;   // only as debug info
		int player = so.getPlayer();
		Types.ACTIONS_VT actBest;
		boolean learnFromRM = m_oPar.getLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;
		
		
			m_Net.resetElig(); // reset the elig traces before starting a new game
			m_Net.calcScoresAndElig(m_feature.prepareFeatVector(so));
		int counter=0;		// count the number of moves
		while (true) {
			
			if(firstRound)
			{
				lastInput[player] = m_feature.prepareFeatVector(so);
				if(player == 2)
					firstRound = false;
			}
			
			actBest = acting_pa.getNextAction2(so.partialState(), true, true);
			randomMove = actBest.isRandomAction();
			so.advance(actBest);
			so.storeBestActionInfo(actBest);	// /WK/ was missing before 2021-09-10. Now stored ScoreTuple is up-to-date.

			counter++;
			if (counter==epiLength) {
				break;
			}

			if (randomMove && !so.isGameOver() && !learnFromRM && (!so.isRoundOver()&&so.isRoundBasedGame()))
			{
				// no training, go to next move
				lastInput[player] = m_feature.prepareFeatVector(so);
				m_Net.calcScoresAndElig(lastInput[player]); // calculate score, write it to
												// old_y[k] for
												// next pass & update
												// eligibilities (NEW
												// WK/02/2015)
				// only for diagnostics
				if (DEBG)
					System.out.println("random move");

			} 
			else 
			{
				// do one training step
				
				// this is the accumulation logic: if eMax>0, then form 
				// mini batches and apply the weight changes only at the end
				// of such mini batches (after eMax complete games)
				int eMax = super.getEpochMax();
				if (eMax==0) {
					wghtChange=true;
				} else {
					if (so.isGameOver()||so.isRoundOver()&&so.isRoundBasedGame()) numFinishedGames++;
					wghtChange = ((so.isGameOver()||so.isRoundOver()&&so.isRoundBasedGame()) && (numFinishedGames % eMax) == 0);
				}
				
				if(so.isGameOver()||so.isRoundOver()&&so.isRoundBasedGame())
				{
					double rew;
					for(int i = 0; i < 3; i++)
					{
						rew = so.getGameScore(i);
						rew = normalize2(rew,so);
						//input = m_feature.prepareFeatVector(so);
						m_Net.updateWeights(rew, lastInput[i], so.isGameOver()||so.isRoundOver()&&so.isRoundBasedGame(), wghtChange);
					}
					break;
				}
				else
				{
					//input = m_feature.prepareFeatVector(so);
					m_Net.updateWeights(0.0, lastInput[player], so.isGameOver()||so.isRoundOver()&&so.isRoundBasedGame(), wghtChange);
					lastInput[player] = m_feature.prepareFeatVector(so);
				}
			}
			
		
			player = so.getPlayer();   // advance to the next player
		}
		
			m_Net.finishUpdateWeights(); // adjust learn params ALPHA & BETA
		m_epsilon = m_epsilon - m_EpsilonChangeDelta; 		// linear decrease of m_epsilon 

		incrementGameNum();
		acting_pa.setGameNum(this.getGameNum());
		return false;
	}
	
	private double normalize2(double score, StateObservation so) {
		if (NORMALIZE) {
			// Normalize to [0,+1] (the appropriate range for Fermi-fct-sigmoid)
			// or to [-1,+1] (the appropriate range for tanh-sigmoid):
			double lower = (m_Net.FERMI_FCT ? 0.0 : -1.0);
			double upper = 1.0;
			
			score = normalize(score,so.getMinGameScore(),
							  		so.getMaxGameScore(),lower,upper);
		}
		return score;
	}
	
	
	
	
	public void setTDParams(ParTD tdPar, int maxGameNum) {
		m_Net.setLambda(tdPar.getLambda());
		m_Net.setGamma(tdPar.getGamma());
		if (m_feature.getFeatmode() == 8) {
			m_Net.setAlpha(tdPar.getAlpha());
		} else {
			// set ALPHA and BETA in TD_NNet (TD_Lin) inverse proportional to
			// the fan-in,
			// i.e. divide by the number of neurons on the input side of the
			// weights:
			m_Net.setAlpha( tdPar.getAlpha() / m_feature.getInputSize(m_feature.getFeatmode()) );
					//OLD (and wrong): inpSize[m_feature.getFeatmode()] );
		}
		m_Net.setBeta(tdPar.getAlpha() / hiddenSize); 	// only relevant for
														// TD_NNet
		m_Net.setAlphaChangeRatio(Math.pow(
				tdPar.getAlphaFinal() / tdPar.getAlpha(), 1.0 / maxGameNum));
		//m_Net.setEpochs(tdPar.getEpochs());  // now we use epochs over whole games
		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		m_Net.setRpropInitDelta( tdPar.getAlpha() / m_feature.getInputSize(m_feature.getFeatmode()));
					//OLD (and wrong): inpSize[m_feature.getFeatmode()] );
	}

	public void setAlpha(double alpha) {
		m_Net.setAlpha(alpha);
	}

	public double getAlpha() {
		// only for debug & testing
		// super.counStates(1);
		//int verbose1 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		//super.analyze_hmC(getGameNum(), verbose1);
		//int verbose2 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		//super.analyze_hmX(verbose2);

		return m_Net.getAlpha();
	}

	public double getEpsilon() {
		return m_epsilon;
	}
	
	public double getGamma() {
		return m_tdPar.getGamma();
	}
	
	public String stringDescr() {
		String cs = getClass().getName();
		return cs + ": "+ (m_tdPar.hasLinearNet()?"LIN":"BP")
						+ ", " + (m_tdPar.hasSigmoid()?"with sigmoid":"w/o sigmoid")
						+ ", NORMALIZE:" + (NORMALIZE?"true":"false")
						+ ", lambda:" + m_Net.getLambda()
						+ ", features:" + m_feature.getFeatmode()
						+ ", learnFromRM: " + (m_oPar.getLearnFromRM()?"true":"false");
	}
	
	public String stringDescr2() {
		String cs = getClass().getName();
		return cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
					+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal()
					+ ", gamma: " + m_tdPar.getGamma(); // +", MODE_3P: "+ m_tdPar.getMode3P();
	}
		
	public String printTrainStatus() {
		DecimalFormat frm = new DecimalFormat("#0.0000");
		DecimalFormat frme= (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);
		frme.applyPattern("0.0E00");  

		String cs = ""; //getClass().getName() + ": ";   // optional class name
		return  cs + "alpha="+frm.format(m_Net.getAlpha())
				   + ", epsilon="+frm.format(getEpsilon())
				   //+ ", lambda:" + m_Net.getLambda()
				   + ", "+getGameNum() + " games"
				   + " ("+frme.format(getNumLrnActions()) + " learn actions)";
	}
	
	@Override
	public boolean isTrainable() { return true; }

	public ParTD getParTD() {
		return m_tdPar;
	}
	
	public long getNumLrnActions() {
		return m_Net.getNumLearnActions();
	}

	public int getFeatmode() {
		return m_feature.getFeatmode();
	}

}