package controllers.TD;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import agentIO.LoadSaveGBG;
import params.ParOther;
import params.ParTD;
//import params.TDParams;
//import params.OtherParams;
import tools.Types;
import controllers.TD.TD_Lin;
import controllers.TD.TD_NNet;
import controllers.TD.TD_func;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.Arena;
import games.Feature;
import games.GameBoard;
import games.StateObservation;
import games.XArenaMenu;
import games.Sim.StateObserverSim;

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
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
//abstract 
public class TDAgent extends AgentBase implements PlayAgent,Serializable {
	protected TD_func[] m_Net;
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

	// --- inpSize now obsolete (replaced by m_feature.getInputSize(int featmode)) --- :
	// size of feature input vector for each featmode
	// (featmode def'd in TicTDBase. If featmode==8, use
	// TicTDBase.getInputSize8())
	// private int inpSize[] = { 6, 6, 10, 19, 13, 19, 0, 0, 0, 9 };
	protected int hiddenSize = 15; // size of hidden layer (only for TD_NNet)
	private Random rand;
//	private int[][] m_trainTable = null;
//	private double[][] m_deltaTable = null;
	private int numFinishedGames = 0;
	private boolean randomSelect = false;
//	private boolean m_hasLinearNet;
//	private boolean m_hasSigmoid;
//	private boolean learnFromRM = false;    // use now m_oPar.useLearnFromRM() - don't store/maintain value twice
	private boolean NORMALIZE = false; 
	// if NEW_GNA==true: use the new function getNextAction2,3 in getNextAction;
	// if NEW_GNA==false: use the old function getNextAction1 in getNextAction;
	private static boolean NEW_GNA=true;	
	
	protected Feature m_feature;
	
	/**
	 * Members {@link #m_tdPar} and {@link AgentBase#m_oPar} are needed for saving and loading
	 * the agent (to restore the agent with all its parameter settings)
	 */
	private ParTD m_tdPar;		// TODO transform to ParTD
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
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

	// --- never used ---
//	/**
//	 * Construct new {@link TDAgent}, setting everything from tdPar and set default
//	 * maxGameNum=1000
//	 * 
//	 * @param tdPar
//	 */
//	public TDAgent(String name, ParTD tdPar, ParOther oPar, Feature feature) {
//		super(name);
//		initNet(tdPar, oPar, feature, 1000);
//	}

	/**
	 * Construct new {@link TDAgent}, setting everything from tdPar and from maxGameNum
	 * 
	 * @param tdPar
	 * @param maxGameNum
	 */
	public TDAgent(String name, ParTD tdPar, ParOther oPar, Feature feature, int maxGameNum) {
		super(name);
		initNet(tdPar, oPar, feature, maxGameNum);
	}

	/**
	 * 
	 * @param tdPar
	 * @param maxGameNum
	 */
	private void initNet(ParTD tdPar, ParOther oPar, Feature feature, int maxGameNum) {
		m_tdPar = new ParTD(tdPar);
		m_oPar = new ParOther(oPar);  		// AgentBase::m_oPar
		m_feature = feature; 
		m_Net = new TD_func[3];
		//super.setFeatmode(tdPar.getFeatmode());
		//super.setEpochMax(tdPar.getEpochs());
		for(int i = 0; i < 3; i++)
		{
			if (m_feature.getFeatmode() > 99) {
				m_Net[i] = null;
			} else {
				if (tdPar.hasLinearNet()) {
					m_Net[i] = new TD_Lin(m_feature.getInputSize(m_feature.getFeatmode()),
							//OLD (and wrong): getInputSize(m_feature.getFeatmode()),
							tdPar.hasSigmoid());
				} else {
					m_Net[i] = new TD_NNet(m_feature.getInputSize(m_feature.getFeatmode()),
							//OLD (and wrong): getInputSize(m_feature.getFeatmode()),
							hiddenSize, tdPar.hasSigmoid());
				}
				// set alpha,beta,gamma,lambda & epochMax,rpropLrn from the TDpars
				// tab
				this.setTDParams(tdPar, maxGameNum, i);
			}
		}
		
		// m_EPS=eps;
		m_epsilon = tdPar.getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdPar.getEpsilonFinal()) / maxGameNum;
		NORMALIZE=tdPar.getNormalize();
//		m_hasSigmoid = tdPar.hasSigmoid();
//		m_hasLinearNet = tdPar.hasLinearNet();
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
		if (this.getParOther() == null ) 
			this.setDefaultParOther();
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
		m_arena.m_xab.setTdParFrom(n, this.getParTD() );
		m_arena.m_xab.setOParFrom(n, this.getParOther() );
	}
	
	/**
	 * Get the best next action and return it 
	 * (NEW version: ACTIONS_VT and recursive part for multi-moves)
	 * 
	 * @param so			current game state (is returned unchanged)
	 * @param random		allow random action selection with probability m_epsilon
	 * @param silent
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
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		StateObservation NewSO;
		int count = 1; // counts the moves with same BestScore
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
		BestScore = -Double.MAX_VALUE;
		double[] VTable;
       
		int player = Types.PLAYER_PM[refer.getPlayer()]; 	 
	
        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
	
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        VTable = new double[acts.size()];  
        
        assert actions.length>0 : "Oops, no available action";
        for(i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);		
	        
            CurrentScore = g3_Evaluate(so,actions[i],refer,silent);
				
			// just a debug check:
			if (Double.isInfinite(CurrentScore)) {
				System.out.println("getScore(NewSO) is infinite!");
			}
			
			CurrentScore = normalize2(CurrentScore,so);					
			
			VTable[i] = CurrentScore;
			if (BestScore < CurrentScore) 
			{
				BestScore = CurrentScore;
				actBest = actions[i];
				count = 1;
			}
			else if (BestScore == CurrentScore) 
			{
			
				count++;
				if (rand.nextDouble() < 1.0/count) 
					actBest = actions[i];
			}
        } 

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
		double CurrentScore = 0; 	// NetScore*Player, the quantity to be
									// maximized
		StateObservation NewSO;
		int count = 1; // counts the moves with same BestScore
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
		BestScore = -Double.MAX_VALUE;
		double[] VTable;
       
		int player = refer.getPlayer(); 	 
	
		// ??????
        randomSelect = false;
		if (random) {
			randomSelect = (rand.nextDouble() < m_epsilon);
		}
		
		// get the best (or eps-greedy random) action
        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
        Types.ACTIONS[] actions = new Types.ACTIONS[acts.size()];
        VTable = new double[acts.size()];  
        
        assert actions.length>0 : "Oops, no available action";
        for(i = 0; i < actions.length; ++i)
        {
            actions[i] = acts.get(i);		
	        
            CurrentScore = g4_Evaluate(so,actions[i], refer, silent);
				
			// just a debug check:
//			if (Double.isInfinite(CurrentScore)) {
//				System.out.println("getScore(NewSO) is infinite!");
//			}
			
			CurrentScore = normalize3(CurrentScore, so, player);					
			
			//
			// fill VTable, calculate BestScore and actBest:
			//
			VTable[i] = CurrentScore;
			if (BestScore < CurrentScore) 
			{
				BestScore = CurrentScore;
				actBest = actions[i];
				count = 1;
			} 
			else if (BestScore == CurrentScore) 
			{
				// If there are 'count' possibilities with the same score BestScore, 
				// each one has the probability 1/count of being selected.
				// 
				// (To understand formula, think recursively from the end: the last one is
				// obviously selected with prob. 1/count. The others have the probability 
				//      1 - 1/count = (count-1)/count 
				// left. The previous one is selected with probability 
				//      ((count-1)/count)*(1/(count-1)) = 1/count
				// and so on.) 
				count++;
				if (rand.nextDouble() < 1.0/count) {
					actBest = actions[i];
					
				}
			}
        } // for

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

		//
		// the recursive part (only for deterministic games) is for the case of 
		// multi-moves: the player who just moved gets from StateObservation 
		// the signal for one (or more) additional move(s)
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

		if (NewSO.isGameOver()) 
		{
			CurrentScore = NewSO.getGameScore(so);
		} 
		else 
		{
			CurrentScore = getGamma()*player * getScore(NewSO);
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
	// (g3_Evaluate is helper function for getNextAction3)
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
		if (NewSO.isGameOver()) 
			CurrentScore = NewSO.getGameScore(so);
		else 
			CurrentScore = getGamma()* m_Net[player].getScore(m_feature.prepareFeatVector(so));;
									   // here we ask this agent for its score estimate on NewSO
		if (!silent) {
			System.out.println(NewSO.stringDescr()+", "+(2*CurrentScore*player-1));
			//print_V(Player, NewSO.getTable(), 2 * CurrentScore * Player - 1);
		}

		return CurrentScore;
    }

	/**
	 * Return the agent's estimate of the score for that after state.
	 * 
	 * @param so			the current game state;
	 * @return V(), the prob. that X (Player +1) wins from that after state.
	 *         Player*V() is the quantity to be maximized by getNextAction2.
	 */
	public double getScore(StateObservation so) {
		double score = m_Net[0].getScore(m_feature.prepareFeatVector(so));
		return score;
	}


	/**
	 * Train the Agent for one complete game episode. <p>
	 * Side effects: Increment m_GameNum by +1. Change the agent's internal  
	 * parameters (weights and so on).
	 * @param so		the state from which the episode is played (usually the
	 * 					return value of {@link GameBoard#chooseStartState(PlayAgent)} to get
	 * 					some exploration of different game paths)
// --- epiLength, learnFromRM are now available via the AgentBase's member ParOther m_oPar: ---
//	 * @param epiLength	maximum number of moves in an episode. If reached, stop training 
//	 * 					prematurely.  
//	 * @param learnFromRM if true, learn from random moves during training
	 * @return			true, if agent raised a stop condition (only CMAPlayer)	 
	 */
	public boolean trainAgent(StateObservation so) 
	{
		if(so.getNumPlayers() > 2)
			return trainAgent3player(so);
		else
			return trainAgent2player(so);
	}
	
	private boolean trainAgent2player(StateObservation so)
	{
		double[] VTable = null;
		double reward = 0.0;
		boolean randomMove;
		boolean finished = false;
		boolean wghtChange = false;
		boolean DEBG = false;
		double Input[], oldInput[];
		String S_old, I_old = null;   // only as debug info
		int player;
		Types.ACTIONS_VT actBest;
		StateObservation oldSO;
		boolean isNtuplePlayer = (m_feature.getFeatmode() == 8
				|| this.getClass().getName().equals("TD_NTPlayer"));

		boolean learnFromRM = m_oPar.useLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;

		player = Types.PLAYER_PM[so.getPlayer()];

		m_Net[0].resetElig(); // reset the elig traces before starting a new game
							// /WK/ NEW/02/2015
		oldInput = m_feature.prepareFeatVector(so);
		S_old = so.stringDescr();   
		//S_old = tableToString(-Player, table);
		if (!isNtuplePlayer)
			I_old = m_feature.stringRepr(oldInput);
		m_Net[0].calcScoresAndElig(oldInput);
		if (DEBG) {
			for (int i = 0; i < oldInput.length; ++i)
				System.out.print((int) ((3 + oldInput[i]) % 3));
			System.out.println();
		}
		int counter=0;		// count the number of moves
		while (true) {
//			VTable = new double[so.getNumAvailableActions()+1];
			actBest = this.getNextAction2(so, true, true);
			randomMove = actBest.isRandomAction();
			oldSO = so.copy();
			so.advance(actBest);

			if (so.isGameOver()) {
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
				reward = player*so.getGameScore(oldSO);
				
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
				reward=estimateGameValue(so);
				//epiCount++;
				finished = true; 
			}
			Input = m_feature.prepareFeatVector(so);
			if (randomMove && !finished && !learnFromRM) {
				// no training, go to next move
				m_Net[0].calcScoresAndElig(Input); // calculate score, write it to
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
				m_Net[0].updateWeights(reward, Input, finished, wghtChange);
				// contains afterwards a m_Net.calcScoresAndElig(Input);

			}

			oldInput = Input; 
			
			if (finished) {
				if (DEBG)
					if (randomMove)
						System.out.println("Terminated by random move");
				break;
			}
			if (DEBG) {
				for (int i = 0; i < Input.length; ++i)
					System.out.print((int) ((3 + Input[i]) % 3));
				System.out.println();
			}

			player = Types.PLAYER_PM[so.getPlayer()];   // advance to the next player
		}
		m_Net[0].finishUpdateWeights(); // adjust learn params ALPHA & BETA
		m_epsilon = m_epsilon - m_EpsilonChangeDelta; 		// linear decrease of m_epsilon 

		incrementGameNum();
		return false;
	}
	
	private boolean trainAgent3player(StateObservation so)
	{
		boolean randomMove;
		boolean wghtChange = false;
		boolean DEBG = false;
		boolean firstRound = true;
		double input[];
		double lastInput[][] = new double [3][m_feature.getInputSize(0)];
		//String S_old, I_old = null;   // only as debug info
		int player = so.getPlayer();;
		Types.ACTIONS_VT actBest;
		boolean learnFromRM = m_oPar.useLearnFromRM();
		int epiLength = m_oPar.getEpisodeLength();
		if (epiLength==-1) epiLength = Integer.MAX_VALUE;
		
		for(TD_func net : m_Net)
			net.resetElig(); // reset the elig traces before starting a new game
		
		int counter=0;		// count the number of moves
		while (true) {
			
			if(firstRound)
			{
				m_Net[player].calcScoresAndElig(m_feature.prepareFeatVector(so));
				lastInput[player] = m_feature.prepareFeatVector(so);
				if(player == 2)
					firstRound = false;
			}
			
			actBest = this.getNextAction2(so, true, true);
			randomMove = actBest.isRandomAction();
			so.advance(actBest);
			
			counter++;
			if (counter==epiLength) {
				break;
			}
			
			if (randomMove && !so.isGameOver() && !learnFromRM) 
			{
				// no training, go to next move
				lastInput[player] = m_feature.prepareFeatVector(so);
				m_Net[player].calcScoresAndElig(lastInput[player]); // calculate score, write it to
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
					if (so.isGameOver()) numFinishedGames++;
					wghtChange = (so.isGameOver() && (numFinishedGames % eMax) == 0);
				}
				
				if(so.isGameOver())
				{
					double rew;
					for(int i = 0; i < 3; i++)
					{
						rew = so.getGameScore(i);
						rew = normalize3(rew,so,so.getPlayer());
						//input = m_feature.prepareFeatVector(so);
						m_Net[i].updateWeights(rew, lastInput[i], so.isGameOver(), wghtChange);
					}
					break;
				}
				else
				{
					//input = m_feature.prepareFeatVector(so);
					m_Net[player].updateWeights(0.0, lastInput[player], so.isGameOver(), wghtChange);
					lastInput[player] = m_feature.prepareFeatVector(so);
				}
			}
			
		
			player = so.getPlayer();   // advance to the next player
		}
		for(TD_func net : m_Net)
			net.finishUpdateWeights(); // adjust learn params ALPHA & BETA
		m_epsilon = m_epsilon - m_EpsilonChangeDelta; 		// linear decrease of m_epsilon 

		incrementGameNum();
		return false;
	}
	
	private double normalize2(double score, StateObservation so) {
		if (NORMALIZE) {
			// Normalize to [0,+1] (the appropriate range for Fermi-fct-sigmoid)
			// or to [-1,+1] (the appropriate range for tanh-sigmoid):
			double lower = (m_Net[0].FERMI_FCT ? 0.0 : -1.0);
			double upper = (m_Net[0].FERMI_FCT ? 1.0 :  1.0);
			
			score = normalize(score,so.getMinGameScore(),
							  		so.getMaxGameScore(),lower,upper);
		}
		return score;
	}
	
	
	private double normalize3(double score, StateObservation so, int player) {
		if (NORMALIZE) {
			// Normalize to [0,+1] (the appropriate range for Fermi-fct-sigmoid)
			// or to [-1,+1] (the appropriate range for tanh-sigmoid):
			double lower = (m_Net[player].FERMI_FCT ? 0.0 : -1.0);
			double upper = (m_Net[player].FERMI_FCT ? 1.0 :  1.0);
			
			score = normalize(score,so.getMinGameScore(),
							  		so.getMaxGameScore(),lower,upper);
		}
		return score;
	}
	
	public void setTDParams(ParTD tdPar, int maxGameNum, int i) {
		m_Net[i].setLambda(tdPar.getLambda());
		m_Net[i].setGamma(tdPar.getGamma());
		if (m_feature.getFeatmode() == 8) {
			m_Net[i].setAlpha(tdPar.getAlpha());
		} else {
			// set ALPHA and BETA in TD_NNet (TD_Lin) inverse proportional to
			// the fan-in,
			// i.e. divide by the number of neurons on the input side of the
			// weights:
			m_Net[i].setAlpha( tdPar.getAlpha() / m_feature.getInputSize(m_feature.getFeatmode()) );
					//OLD (and wrong): inpSize[m_feature.getFeatmode()] );
		}
		m_Net[i].setBeta(tdPar.getAlpha() / hiddenSize); 	// only relevant for
														// TD_NNet
		m_Net[i].setAlphaChangeRatio(Math.pow(
				tdPar.getAlphaFinal() / tdPar.getAlpha(), 1.0 / maxGameNum));
		//m_Net.setEpochs(tdPar.getEpochs());  // now we use epochs over whole games
		m_Net[i].setRpropLrn(tdPar.hasRpropLrn());
		m_Net[i].setRpropInitDelta( tdPar.getAlpha() / m_feature.getInputSize(m_feature.getFeatmode()));
					//OLD (and wrong): inpSize[m_feature.getFeatmode()] );
	}

	public void setAlpha(double alpha) {
		m_Net[0].setAlpha(alpha);
	}

	public double getAlpha() {
		// only for debug & testing
		// super.counStates(1);
		int verbose1 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		//super.analyze_hmC(getGameNum(), verbose1);
		int verbose2 = 1; // 0: skip analyze_hmX, 1: one-line output, 2:
							// multi-line output in analyse_hmX
		//super.analyze_hmX(verbose2);

		return m_Net[0].getAlpha();
	}

	public double getEpsilon() {
		return m_epsilon;
	}
	
	public double getGamma() {
		return m_tdPar.getGamma();
	}
	
	public String stringDescr() {
		String cs = getClass().getName();
		String str = cs + ": " + (m_tdPar.hasLinearNet()?"LIN":"BP")
						+ ", " + (m_tdPar.hasSigmoid()?"with sigmoid":"w/o sigmoid")
						+ ", NORMALIZE:" + (NORMALIZE?"true":"false")
						+ ", lambda:" + m_Net[0].getLambda()
						+ ", features:" + m_feature.getFeatmode()
						+ ", learnFromRM: " + (m_oPar.useLearnFromRM()?"true":"false");
		return str;
	}
	
	public String stringDescr2() {
		String cs = getClass().getName();
		String str = cs + ": alpha_init->final:" + m_tdPar.getAlpha() + "->" + m_tdPar.getAlphaFinal()
						+ ", epsilon_init->final:" + m_tdPar.getEpsilon() + "->" + m_tdPar.getEpsilonFinal()
						+ ", gamma: " + m_tdPar.getGamma() +", MODE_3P: "+ m_tdPar.getMode3P();
		return str;
	}
		
	public String printTrainStatus() {
		DecimalFormat frm = new DecimalFormat("#0.0000");
		DecimalFormat frme= new DecimalFormat();
		frme = (DecimalFormat) NumberFormat.getNumberInstance(Locale.UK);		
		frme.applyPattern("0.0E00");  

		String cs = ""; //getClass().getName() + ": ";   // optional class name
		String str = cs + "alpha="+frm.format(m_Net[0].getAlpha()) 
				   + ", epsilon="+frm.format(getEpsilon())
				   //+ ", lambda:" + m_Net.getLambda()
				   + ", "+getGameNum() + " games"
				   + " ("+frme.format(getNumLrnActions()) + " learn actions)";
		return str;
	}
	
	@Override
	public boolean isTrainable() { return true; }

// --- obsolete (replaced by m_feature.getInputSize(int featmode) ):
//	private int getInputSize(int featmode) {
//			return inpSize[featmode];
//	}

	public int getHiddenSize() {
		return hiddenSize;
	}

	public ParTD getParTD() {
		return m_tdPar;
	}
	
	public long getNumLrnActions() {
		return m_Net[0].getNumLearnActions();
	}

	public void resetNumLearnActions() {
		m_Net[0].resetNumLearnActions();
	}
	
	public int getFeatmode() {
		return m_feature.getFeatmode();
	}

	
	/**
	 * Factory pattern method: make a new Feature object. This object has the 
	 * game-specific method prepareInputVector(StateObservation so) which 
	 * returns a feature vector for the current game state. 
	 * @param 	featmode	different modi of features to generate
	 * @return	the Feature object
	 */
	//abstract public Feature makeFeatureClass(int featmode);
}