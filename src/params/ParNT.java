package params;

import java.io.Serializable;

public class ParNT implements Serializable {
	
    public static int DEFAULT_EPOCHS = 1;
    public static double DEFAULT_TC_INIT=0.001;  
    public static int DEFAULT_TC_INTERVAL=2;  
    public static int DEFAULT_NTUPLE_NUM=10;  
    public static int DEFAULT_NTUPLE_LEN=6;  
    public static int DEFAULT_FIXED_NTUPLE=1;  
    
    private double tcInit = DEFAULT_TC_INIT;
    private boolean tc = false;
    private boolean tcImm = false;
    private int tcInterval = DEFAULT_TC_INTERVAL;
    private boolean randomness = false;
    private boolean randomWalk = true;
    private int numTuple = DEFAULT_NTUPLE_NUM;
    private int maxTupleLen = DEFAULT_NTUPLE_LEN;
    private int fixedNtupleMode= DEFAULT_FIXED_NTUPLE;
    private boolean useSymmetry = true;
    private boolean afterState = false;
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public ParNT() {	}
    
    public ParNT(NTParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(NTParams nt) {
		this.tcInit = nt.getINIT();
		this.tc = nt.getTc();
		this.tcImm = nt.getTcImm();
		this.tcInterval = nt.getTcInterval();
		this.randomness = nt.getRandomness();
		this.randomWalk = nt.getRandomWalk();
		this.numTuple = nt.getNtupleNumber();
		this.maxTupleLen = nt.getNtupleMax();
		this.fixedNtupleMode = nt.getFixedNtupleMode();
		this.useSymmetry = nt.getUseSymmetry();
		this.afterState = nt.getUseAfterState();
	}

	public double getTcInit() {
		return tcInit;
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
	public boolean getAFTERSTATE() {
		return afterState;
	}

	public void setTcInit(double tcInit) {
		this.tcInit = tcInit;
	}

	public void setTc(boolean tc) {
		this.tc = tc;
	}

	public void setTcImm(boolean tcImm) {
		this.tcImm = tcImm;
	}

	public void setTcInterval(int tcInterval) {
		this.tcInterval = tcInterval;
	}

	public void setRandomness(boolean randomness) {
		this.randomness = randomness;
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

	public void setUSESYMMETRY(boolean useSymmetry) {
		this.useSymmetry = useSymmetry;
	}

	public void setAFTERSTATE(boolean afterState) {
		this.afterState = afterState;
	}


}
