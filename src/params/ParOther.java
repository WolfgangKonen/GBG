package params;

import java.io.Serializable;

import javax.swing.JPanel;

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
    public static int DEFAULT_PMAX_RUBIKS = 6;

    private int quickEvalMode = DEFAULT_QUICK_EVAL_MODE;
    private int trainEvalMode = DEFAULT_TRAIN_EVAL_MODE;
    private int numEval = DEFAULT_NUM_EVAL;
    private int episodeLength = DEFAULT_EPISODE_LENGTH;
	private int stopTest = DEFAULT_STOP_TEST;
    private int stopEval = DEFAULT_STOP_EVAL; 		// new meaning: max episode length during eval
    private int wrapperNply = DEFAULT_WRAPPER_NPLY;
	private int wrapperMCTSIterations = DEFAULT_WRAPPER_MCTS_ITERATIONS;
	private int pMaxRubiks = DEFAULT_PMAX_RUBIKS;	// only relevant for RubiksCube, see CubeConfig.pMax
	private boolean chooseStart01 = false;
    private boolean learnFromRM = false;
	private boolean bReplayBuf = false;	// only relevant for RubiksCube: whether to use a replay buffer or not
	private double incAmount = 0;		// only relevant for RubiksCube in case bReplayBuf==true, see DAVI3Agent
    private boolean rewardIsGameScore = true;
    
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
	private static final long serialVersionUID = 1L;
	
	public ParOther() {	}
    
	public ParOther(boolean withUI, Arena m_arena) {
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
		this.pMaxRubiks = op.getpMaxRubiks();
		this.chooseStart01 = op.getChooseStart01();
		this.learnFromRM = op.getLearnFromRM();
		this.bReplayBuf = op.getReplayBuffer();
		this.incAmount = op.getIncAmount();
		this.rewardIsGameScore = op.getRewardIsGameScore();
		
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
		this.pMaxRubiks = op.getpMaxRubiks();
		this.chooseStart01 = op.getChooseStart01();
		this.learnFromRM = op.getLearnFromRM();
		this.bReplayBuf = op.getReplayBuffer();
		this.incAmount = op.getIncAmount();
		this.rewardIsGameScore = op.getRewardIsGameScore();
		
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

	public int getStopEval() {
		return stopEval;
	}

	public int getWrapperNPly() {
		return wrapperNply;
	}
	
	public int getWrapperMCTSIterations() {
		return wrapperMCTSIterations;
	}

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
	

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "Sarsa" (for {@link SarsaAgt})
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		// Currently we have here only the sensible defaults for one game ("RubiksCube"):
		switch (gameName) {
		case "RubiksCube": 
			this.setChooseStart01(true);		// always select a non-solved cube as start state
			enableChoosePart(false);
			enableRgsPart(false);
			this.setEpisodeLength(12);
			this.setStopEval(50);
			this.setpMaxRubiks(9);
			this.setReplayBuffer(false);
			this.setQuickEvalMode(1);
			this.setTrainEvalMode(-1);
			this.setNumEval(10000);
			this.setRewardIsGameScore(false);
			break;
		default:								//  all other
			this.setEpisodeLength(-1);
			this.setStopEval(-1);
			break;
		}
		switch (agentName) {
		case "Sarsa":
		case "TD-NTuple-3":
			this.setLearnFromRM(true);
			break;
		default: 
			this.setLearnFromRM(false);
			break;
		}
	}
	
}
