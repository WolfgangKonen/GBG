package params;

import java.io.Serializable;

import javax.swing.JPanel;

import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.Nim.ArenaNim;
import games.Nim.NimConfig;

/**
 *  N-tuple parameters and TC (temporal coherence) parameters for TD agents 
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 *  
 *  @see NTParams
 *  @see TDNTuple3Agt
 *  @see SarsaAgt
 */
public class ParNT implements Serializable {
    public static double DEFAULT_TC_INIT=0.001;  
    public static int DEFAULT_TC_INTERVAL=2;  
    public static int DEFAULT_TC_TRANSFER=0;   	// 0: id(), 1: TCL-EXP
    public static int DEFAULT_TC_ACCUMUL=1;   	// 0: delta, 1: recommended weight change
    public static double DEFAULT_TC_BETA=2.7;
    public static int DEFAULT_NTUPLE_NUM=10;  
    public static int DEFAULT_NTUPLE_LEN=6;  
    public static int DEFAULT_FIXED_NTUPLE=1;  
    
    private double tcInit = DEFAULT_TC_INIT;
    private boolean tc = false;
    private boolean tcImm = true;					// recommended choice: true
    private int tcInterval = DEFAULT_TC_INTERVAL; 	// obsolete if tcImm==true
    private int tcTransfer = DEFAULT_TC_TRANSFER;	// 0: id(), 1: TC EXP
    private int tcAccumul = DEFAULT_TC_ACCUMUL;		// 0: delta, 1: recommended weight change
    private double tcBeta = DEFAULT_TC_BETA;
    private boolean randomness = false;
    private boolean randomWalk = true;
    private int numTuple = DEFAULT_NTUPLE_NUM;
    private int maxTupleLen = DEFAULT_NTUPLE_LEN;
    private int fixedNtupleMode= DEFAULT_FIXED_NTUPLE;
    private boolean useSymmetry = true;
    private int nSym= 0;							// 0: use all symmetries, if useSymmetry==true
    private boolean afterState = false;
    
    /**
     * This member is only constructed when the constructor {@link #ParNT(boolean) ParNT(boolean withUI)} 
     * called with {@code withUI=true}. It holds the GUI for {@link ParNT}.
     */
    private transient NTParams ntparams = null;

    /**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public ParNT() {	}
    
	public ParNT(boolean withUI) {
		if (withUI)
			ntparams = new NTParams();
	}
	
    public ParNT(ParNT tp) { 
    	this.setFrom(tp);
    }
    
    public ParNT(NTParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(ParNT nt) {
		this.tc = nt.getTc();
		this.tcInit = nt.getTcInit();
		this.tcImm = nt.getTcImm();
		this.tcInterval = nt.getTcInterval();
		this.tcTransfer = nt.getTcTransferMode();
		this.tcBeta = nt.getTcBeta();
		this.tcAccumul = nt.getTcAccumulMode();
		this.randomness = nt.getRandomness();
		this.randomWalk = nt.getRandomWalk();
		this.numTuple = nt.getNtupleNumber();
		this.maxTupleLen = nt.getNtupleMax();
		this.fixedNtupleMode = nt.getFixedNtupleMode();
		this.useSymmetry = nt.getUSESYMMETRY();
		this.nSym = nt.getNSym();
		this.afterState = nt.getAFTERSTATE();

		if (ntparams!=null)
			ntparams.setFrom(this);
	}

	public void setFrom(NTParams nt) {
		this.tc = nt.getTc();
		this.tcInit = nt.getTcInit();
		this.tcImm = nt.getTcImm();
		this.tcInterval = nt.getTcInterval();
		this.tcTransfer = nt.getTcTransferMode();
		this.tcBeta = nt.getTcBeta();
		this.tcAccumul = nt.getTcAccumulMode();
		this.randomness = nt.getRandomness();
		this.randomWalk = nt.getRandomWalk();
		this.numTuple = nt.getNtupleNumber();
		this.maxTupleLen = nt.getNtupleMax();
		this.fixedNtupleMode = nt.getFixedNtupleMode();
		this.useSymmetry = nt.getUSESYMMETRY();
		this.nSym = nt.getNSym();
		this.afterState = nt.getAFTERSTATE();

		if (ntparams!=null)
			ntparams.setFrom(this);
	}

	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromNTParams() {
		if (ntparams!=null)
			this.setFrom(ntparams);
	}
	
	/**
	 * Needed for {@link Arena} (which has no train rights) to disable this param tab 
	 * @param enable
	 */
	public void enableAll(boolean enable) {
		if (ntparams!=null)
			ntparams.enableAll(enable);
	}
	
	public JPanel getPanel() {
		if (ntparams!=null)		
			return ntparams.getPanel();
		return null;
	}

	public double getTcInit() {
		return tcInit;
	}

	public double getTcBeta() {
		return tcBeta;
	}

	public boolean getTc() {
		return tc;
	}

	public boolean getTcImm() {
		return tcImm;
	}

	public int getTcInterval() {
		return tcInterval;
	}

	public int getTcTransferMode() {
		return tcTransfer;
	}

	public int getTcAccumulMode() {
		return tcAccumul;
	}

	public boolean getRandomness() {
		return randomness;
	}

	public boolean getRandomWalk() {
		return randomWalk;
	}

	public int getNtupleNumber() {
		return numTuple;
	}

	public int getNtupleMax() {
		return maxTupleLen;
	}

	public int getFixedNtupleMode() {
		return fixedNtupleMode;
	}

	public boolean getUSESYMMETRY() {
		return useSymmetry;
	}
	
	public int getNSym() {
		return nSym;
	}

	public boolean getAFTERSTATE() {
		return afterState;
	}

	public int getPlotWeightMethod() {
		if (ntparams!=null)
			return ntparams.getPlotWeightMethod();
		return 0;
	}

	public void setTc(boolean tc) {
		this.tc = tc;
		if (ntparams!=null)
			ntparams.setTc(tc);
	}

	public void setTcInit(double tcInit) {
		this.tcInit = tcInit;
		if (ntparams!=null)
			ntparams.setTcInit(tcInit);
	}

	public void setTcImm(boolean tcImm) {
		this.tcImm = tcImm;
		if (ntparams!=null)
			ntparams.setTcImm(tcImm);
	}

	public void setTcInterval(int tcInterval) {
		this.tcInterval = tcInterval;
		if (ntparams!=null)
			ntparams.setTcInterval(""+tcInterval);
	}

	public void setTcTransferMode(int tcTransfer) {
		this.tcTransfer = tcTransfer;
		if (ntparams!=null)
			ntparams.setTcTransfer(""+tcTransfer);
	}

	public void setTcBeta(double tcBeta) {
		this.tcBeta = tcBeta;
		if (ntparams!=null)
			ntparams.setTcBeta(""+tcBeta);
	}

	public void setTcAccumulMode(int tcAccumul) {
		this.tcAccumul = tcAccumul;
		if (ntparams!=null)
			ntparams.setTcAccumul(""+tcAccumul);
	}

	public void setRandomness(boolean randomness) {
		this.randomness = randomness;
		if (ntparams!=null)
			ntparams.setRandomness(randomness);
	}

	public void setRandomWalk(boolean randomWalk) {
		this.randomWalk = randomWalk;
	}

	public void setNumTuple(int numTuple) {
		this.numTuple = numTuple;
	}

	public void setMaxTupleLen(int maxTupleLen) {
		this.maxTupleLen = maxTupleLen;
	}

	public void setFixedNtupleMode(int fixedNtupleMode) {
		this.fixedNtupleMode = fixedNtupleMode;
	}

	public void setFixedCoList(int[] modeList, String tooltipString) {
		if (ntparams!=null)
			ntparams.setFixedCoList(modeList, tooltipString);
	}
	
	public void setUSESYMMETRY(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}

	public void setNSym(int nSym) {
		this.nSym = nSym;
	}

	public void setAFTERSTATE(boolean afterState) {
		this.afterState = afterState;
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results.
	 * 
	 * @param agentName currently only "TD-Ntuple-2","TD-Ntuple-3","Sarsa"
	 * 				all other strings are without any effect
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		switch (agentName) {
		case "TD-Ntuple-2": 
		case "TD-Ntuple-3": 
		case "Sarsa":
			this.setTc(false);			// consequence: disable InitT, tcIntervalT
			this.setTcInit(0.0001);
			this.setTcImm(true);		// "Immediate"
			this.setTcInterval(2);
			this.setTcTransferMode(1);	// "TC EXP"
			this.setTcBeta(2.7);
			this.setTcAccumulMode(1);	// "rec wght change"
			this.setRandomness(true);		// consequence: enable TupleType, nTupleNumT, nTupleMaxT
			this.setRandomWalk(true);
			this.setNumTuple(10);
			this.setMaxTupleLen(6);	
			this.setFixedNtupleMode(1);
			this.setUSESYMMETRY(true);
			this.setAFTERSTATE(false);		// disable AFTERSTATE for all deterministic games
			switch (gameName) {
			case "2048": 
				this.setNumTuple(3);
				this.setAFTERSTATE(true);
				break;
			case "ConnectFour":
				this.setTc(true);
				this.setRandomness(false);
				break;
			case "Nim": 
				this.setNumTuple(1);
				this.setMaxTupleLen(ArenaNim.getNumberHeaps());	
				this.setRandomness(false);	// use fixed n-tuples, mode==1
				break;
			case "RubiksCube":
				this.setUSESYMMETRY(false);
				break;
			case "Sim":
				this.setUSESYMMETRY(false);
				this.setNSym(10);
				break;
			case "TicTacToe": 
				this.setNumTuple(1);
				this.setMaxTupleLen(9);	
				break;
			}
			break;
		}
		
		if (ntparams!=null)
			ntparams.setFrom(this);
	}
	

}
