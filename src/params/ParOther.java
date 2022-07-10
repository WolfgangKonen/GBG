package params;

import java.io.Serial;
import java.io.Serializable;

import javax.swing.JPanel;

import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;

/**
 *  Other parameters for all agents 
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 *  
 *  @see NTParams
 *  @see TDNTuple3Agt
 *  @see SarsaAgt
 */
public class ParOther implements Serializable {
    public static int DEFAULT_QUICK_EVAL_MODE = 0;
    public static int DEFAULT_TRAIN_EVAL_MODE = 0;
    public static int DEFAULT_NUM_EVAL = 500;
    public static int DEFAULT_EPISODE_LENGTH = -1;
    public static int DEFAULT_STOP_TEST = 0;
    public static int DEFAULT_STOP_EVAL = 100;
    public static int DEFAULT_WRAPPER_NPLY = 0;
    public static int DEFAULT_WRAPPER_MCTS_ITERATIONS = 0;
    public static double DEFAULT_WRAPPER_MCTS_PUCT = 1;
	public static int DEFAULT_WRAPPER_MCTS_DEPTH = 100;
	public static int DEFAULT_PMIN_RUBIKS = 1;
    public static int DEFAULT_PMAX_RUBIKS = 6;

    private int quickEvalMode = DEFAULT_QUICK_EVAL_MODE;
    private int trainEvalMode = DEFAULT_TRAIN_EVAL_MODE;
    private int numEval = DEFAULT_NUM_EVAL;
    private int episodeLength = DEFAULT_EPISODE_LENGTH;
	private int stopTest = DEFAULT_STOP_TEST;
    private int stopEval = DEFAULT_STOP_EVAL; 		// new meaning: max episode length during eval & play
    private int wrapperNply = DEFAULT_WRAPPER_NPLY;
	private int wrapperMCTSIterations = DEFAULT_WRAPPER_MCTS_ITERATIONS;
	private String moveLogDirectory = null;
	private double wrapperMCTS_PUCT = DEFAULT_WRAPPER_MCTS_PUCT;
	private int wrapperMCTS_depth = DEFAULT_WRAPPER_MCTS_DEPTH;
	private int pMinRubiks = DEFAULT_PMIN_RUBIKS;	// only relevant for RubiksCube, see CubeConfig.pMin
	private int pMaxRubiks = DEFAULT_PMAX_RUBIKS;	// only relevant for RubiksCube, see CubeConfig.pMax
	private boolean chooseStart01 = false;
    private boolean learnFromRM = false;
	private boolean bReplayBuf = false;	// only relevant for RubiksCube: whether to use a replay buffer or not
	private double incAmount = 0;		// only relevant for RubiksCube in case bReplayBuf==true, see DAVI3Agent
    private boolean rewardIsGameScore = true;
    private AgentState aState = AgentState.RAW;
    private String agtFile = null;
    
    /**
     * This member is only constructed when the constructor 
     * {@link #ParOther(boolean,Arena) ParOther(boolean withUI,Arena)}
     * called with {@code withUI=true}. It holds the GUI for {@link ParOther}.
     */
    private transient OtherParams otparams = null;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 1L;
	
	public ParOther() {
		aState = AgentState.RAW;
	}
    
	public ParOther(boolean withUI, Arena m_arena) {
		aState = AgentState.RAW;
		if (withUI)
			otparams = new OtherParams(m_arena);
	}
	
	public ParOther(ParOther op) {
    	this.setFrom(op);
	}
	
    public ParOther(OtherParams op) { 
    	this.setFrom(op);
    }
    
	public void setFrom(ParOther op) {
		this.quickEvalMode = op.getQuickEvalMode();
		this.trainEvalMode = op.getTrainEvalMode();
		this.numEval = op.getNumEval();
		this.episodeLength = op.getEpisodeLength();
		this.stopTest = op.getStopTest();
		this.stopEval = op.getStopEval();
		this.wrapperNply = op.getWrapperNPly();
		this.wrapperMCTSIterations = op.getWrapperMCTSIterations();
		this.moveLogDirectory = op.getMoveLogDirectory();
		this.wrapperMCTS_PUCT = op.getWrapperMCTS_PUCT();
		this.wrapperMCTS_depth = op.getWrapperMCTS_depth();
		this.pMinRubiks = op.getpMinRubiks();
		this.pMaxRubiks = op.getpMaxRubiks();
		this.chooseStart01 = op.getChooseStart01();
		this.learnFromRM = op.getLearnFromRM();
		this.bReplayBuf = op.getReplayBuffer();
		this.incAmount = op.getIncAmount();
		this.rewardIsGameScore = op.getRewardIsGameScore();
		this.aState = op.getAgentState();

		if (otparams!=null)
			otparams.setFrom(this);
	}
	
	public void setFrom(OtherParams op) {
		this.quickEvalMode = op.getQuickEvalMode();
		this.trainEvalMode = op.getTrainEvalMode();
		this.numEval = op.getNumEval();
		this.episodeLength = op.getEpisodeLength();
		this.stopTest = op.getStopTest();
		this.stopEval = op.getStopEval();
		this.wrapperNply = op.getWrapperNPly();
		this.wrapperMCTSIterations = op.getWrapperMCTSIterations();
		this.moveLogDirectory = op.getMoveLogDirectory();
		this.wrapperMCTS_PUCT = op.getWrapperMCTS_PUCT();
		this.wrapperMCTS_depth = op.getWrapperMCTS_depth();
		this.pMinRubiks = op.getpMinRubiks();
		this.pMaxRubiks = op.getpMaxRubiks();
		this.chooseStart01 = op.getChooseStart01();
		this.learnFromRM = op.getLearnFromRM();
		this.bReplayBuf = op.getReplayBuffer();
		this.incAmount = op.getIncAmount();
		this.rewardIsGameScore = op.getRewardIsGameScore();
		this.aState = op.getAgentState();
		
		if (otparams!=null)
			otparams.setFrom(this);
	}
	
	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromOTParams() {
		if (otparams!=null)
			this.setFrom(otparams);
	}
	
	public JPanel getPanel() {
		if (otparams!=null)		
			return otparams.getPanel();
		return null;
	}

	public void enableChoosePart(boolean enable) {
		if (otparams!=null)
			otparams.enableChoosePart(enable);
	}

	public void enableRgsPart(boolean enable) {
		if (otparams!=null)
			otparams.enableRgsPart(enable);
	}


	public int getQuickEvalMode() {
		return quickEvalMode;
	}

	public int getTrainEvalMode() {
		return trainEvalMode;
	}

	/**
	 * @return During training: Call evaluator after this number of training games
	 */
	public int getNumEval() {
		return numEval;
	}

	public int getEpisodeLength() {
		return episodeLength;
	}

	public int getStopTest() {
		return stopTest;
	}

	// new meaning: max episode length during eval & play
	public int getStopEval() {
		return stopEval;
	}

	public int getWrapperNPly() {
		return wrapperNply;
	}
	
	public int getWrapperMCTSIterations() {
		return wrapperMCTSIterations;
	}

	public String getMoveLogDirectory() {
		return moveLogDirectory;
	}

	public double getWrapperMCTS_PUCT() {
		return wrapperMCTS_PUCT;
	}

	public int getWrapperMCTS_depth() { return wrapperMCTS_depth; }

	public int getpMinRubiks() { return pMinRubiks;	}

	public int getpMaxRubiks() { return pMaxRubiks;	}

	public double getIncAmount() { return incAmount; }

	public boolean getChooseStart01() {
		return chooseStart01;
	}

	public boolean getLearnFromRM() {
		return learnFromRM;
	}

	public boolean getReplayBuffer() {
		return bReplayBuf;
	}

	public boolean getRewardIsGameScore() {
		return rewardIsGameScore;
	}

	public AgentState getAgentState() {
		return aState;
	}

	public String getAgentFile() {
		return agtFile;
	}

	public void setAgentFile(String aFile) {
		this.agtFile = aFile;
	}

	public void setQuickEvalMode(int qem) {
		this.quickEvalMode=qem;
		if (otparams!=null)
			otparams.setQuickEvalMode(qem);
	}

	public void setTrainEvalMode(int tem) {
		this.trainEvalMode=tem;
		if (otparams!=null)
			otparams.setTrainEvalMode(tem);
	}

	public void setQuickEvalList(int[] modeList) {
		if (otparams!=null)
			otparams.setQuickEvalList(modeList);
	}

	public void setTrainEvalList(int[] modeList) {
		if (otparams!=null)
			otparams.setTrainEvalList(modeList);
	}

	public void setQuickEvalTooltip(String str) {
		if (otparams!=null)
			otparams.setQuickEvalTooltip(str);
	}

	public void setTrainEvalTooltip(String str) {
		if (otparams!=null)
			otparams.setTrainEvalTooltip(str);
	}

	public void setNumEval(int num)
	{
		this.numEval=num;
		if (otparams!=null)
			otparams.setNumEval(num);
	}

	public void setEpisodeLength(int epilen) {
		this.episodeLength = epilen;
		if (otparams!=null)
			otparams.setEpisodeLength(epilen);
	}

	// new meaning: max episode length during eval & play
	public void setStopEval(int epilen) {
		this.stopEval = epilen;
		if (otparams!=null)
			otparams.setStopEval(epilen);
	}

	public void setWrapperNPly(int nply) {
		this.wrapperNply=nply;
		if (otparams!=null)
			otparams.setWrapperNPly(nply);
	}

	public void setWrapperMCTSIterations(final int iterations) {
		this.wrapperMCTSIterations =iterations;
		if (otparams!=null)
			otparams.setWrapperMCTSIterations(iterations);
	}

	public void setWrapperMCTS_PUCT(final double puct) {
		this.wrapperMCTS_PUCT =puct;
		if (otparams!=null)
			otparams.setWrapperMCTS_PUCT(puct);
	}

	public void setWrapperMCTS_depth(final int depth) {
		this.wrapperMCTS_depth =depth;
		if (otparams!=null)
			otparams.setWrapperMCTS_depth(depth);
	}

	public void setpMinRubiks(int pMinRubiks) {
		this.pMinRubiks = pMinRubiks;
		if (otparams!=null)
			otparams.setpMinRubiks(pMinRubiks);
	}

	public void setpMaxRubiks(int pMaxRubiks) {
		this.pMaxRubiks = pMaxRubiks;
		if (otparams!=null)
			otparams.setpMaxRubiks(pMaxRubiks);
	}

	public void setChooseStart01(boolean bChooseStart01) {
		this.chooseStart01=bChooseStart01;
		if (otparams!=null)
			otparams.setChooseStart01(bChooseStart01);
	}
	
	public void setLearnFromRM(boolean bLearnFromRM) {
		this.learnFromRM=bLearnFromRM;
		if (otparams!=null)
			otparams.setLearnFromRM(bLearnFromRM);
	}

	public void setReplayBuffer(boolean bReplayBuf) {
		this.bReplayBuf =bReplayBuf;
		if (otparams!=null)
			otparams.setLearnFromRM(bReplayBuf);
	}

	public void setIncAmount(double incAmount) {
		this.incAmount = incAmount;
	}

	public void setRewardIsGameScore(boolean bRGS) {
		this.rewardIsGameScore=bRGS;
		if (otparams!=null)
			otparams.setRewardIsGameScore(bRGS);
	}

	public void setAgentState(AgentState as) {
		this.aState=as;
		if (otparams!=null)
			otparams.setAgentState(as);
	}


	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "Sarsa" (for {@link SarsaAgt})
	 * @param gameName the string from {@link games.Arena#getGameName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		switch (agentName) {
			case "Sarsa":
				this.setLearnFromRM(true);
				break;
			default:
				this.setLearnFromRM(false);
				break;
		}
		switch (gameName) {
			case "RubiksCube":
				this.setChooseStart01(true);		// always select a non-solved cube as start state
				enableChoosePart(false);
				this.setRewardIsGameScore(false);
				enableRgsPart(false);
				this.setEpisodeLength(12);
				this.setStopEval(50);
				this.setpMinRubiks(1);
				this.setpMaxRubiks(9);
				this.setReplayBuffer(false);
				this.setQuickEvalMode(1);
				this.setTrainEvalMode(-1);
				this.setNumEval(10000);
			case "TicTacToe":
				this.setQuickEvalMode(2);
				this.setTrainEvalMode(1);
				switch (agentName) {
					case "Sarsa":
					case "Sarsa-4":
					case "Qlearn-4":
					case "TD-Ntuple-3":
					case "TD-Ntuple-4":
						this.setLearnFromRM(true);
						break;
					default:
						this.setLearnFromRM(false);
						break;
				}
				break;
			default:								//  all other
				this.setEpisodeLength(-1);
				this.setStopEval(-1);
				break;
		}
	}
	
}
