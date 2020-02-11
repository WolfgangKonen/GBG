package params;

import java.io.Serializable;

import javax.swing.JPanel;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;

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

    private int quickEvalMode = DEFAULT_QUICK_EVAL_MODE;
    private int trainEvalMode = DEFAULT_TRAIN_EVAL_MODE;
    private int numEval = DEFAULT_NUM_EVAL;
    private int episodeLength = DEFAULT_EPISODE_LENGTH;
	private int stopTest = DEFAULT_STOP_TEST;
    private int stopEval = DEFAULT_STOP_EVAL; 
    private int wrapperNply = DEFAULT_WRAPPER_NPLY; 
    private boolean chooseStart01 = false;
    private boolean learnFromRM = false;
    private boolean rewardIsGameScore = true;
    
    /**
     * This member is only constructed when the constructor {@link #ParOther(boolean) ParOther(boolean withUI)} 
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
    
	public ParOther(boolean withUI) {
		if (withUI)
			otparams = new OtherParams();
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
		this.chooseStart01 = op.getChooseStart01();
		this.learnFromRM = op.getLearnFromRM();
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
		this.chooseStart01 = op.getChooseStart01();
		this.learnFromRM = op.getLearnFromRM();
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

	public boolean getChooseStart01() {
		return chooseStart01;
	}

	public boolean getLearnFromRM() {
		return learnFromRM;
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

	public void setWrapperNPly(int nply) {
		this.wrapperNply=nply;
		if (otparams!=null)
			otparams.setWrapperNPly(nply);
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
			this.setEpisodeLength(50);
			break;
		default:								//  all other
			this.setEpisodeLength(-1);
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
