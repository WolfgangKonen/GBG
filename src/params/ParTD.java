package params;

import java.io.Serializable;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.NTuple2ValueFunc;
import controllers.TD.ntuple2.TDNTuple2Agt;

/**
 *  TD (temporal difference) parameters for agents {@link TDAgent} and {@link TDNTuple2Agt}
 *  
 *  @see TDParams
 *  @see TDAgent
 *  @see TDNTuple2Agt
 */
public class ParTD implements Serializable {
    public static int DEFAULT_EPOCHS = 1;
    public static double DEFAULT_ALPHA=0.001;  // 0.1
    public static double DEFAULT_ALFIN=0.001;  
    public static double DEFAULT_EPSIL=0.3;
    public static double DEFAULT_EPFIN=0.0;  
    public static double DEFAULT_GAMMA=1.0;  
    public static double DEFAULT_LAMBDA=0.0;  
    public static double DEFAULT_HORIZONCUT = 0.1;
    public static int DEFAULT_NPLY = 1;
    public static int DEFAULT_MODE_3P = 2;
    public static int DEFAULT_ELIG_MODE = 0;	// 0:[et], 1:[res], 2:[rep], 3:[rr], see [Thill14]
    
    private double alpha = DEFAULT_ALPHA;		// initial learn step size
    private double alfin = DEFAULT_ALFIN;		// final learn step size
    private double epsil = DEFAULT_EPSIL;		// initial random move rate 
    private double epfin = DEFAULT_EPFIN;		// final random move rate
    private double gamma = DEFAULT_GAMMA;		// discount-rate parameter (typically 1.0 or 0.9) 
    private double lambda= DEFAULT_LAMBDA; 		// eligibility trace decay parameter (should be <= GAMMA)
    private double horCut= DEFAULT_HORIZONCUT;	// horizon cut for TD eligibility trace
    private int nply = DEFAULT_NPLY; 
    private int mode3P = DEFAULT_MODE_3P; 
    private int eligMode = DEFAULT_ELIG_MODE;
    private int epochs = DEFAULT_EPOCHS; 
    private int featmode = 0;
    private boolean useNormalize = false;
    private boolean hasLinNet = true;
    private boolean hasRprop = false;
    private boolean hasSigmoid = false;
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	public ParTD() { 	}
    
	public ParTD(ParTD tp) {
		this.alpha = tp.getAlpha();
		this.alfin = tp.getAlphaFinal();
		this.epsil = tp.getEpsilon();
		this.epfin = tp.getEpsilonFinal();
		this.lambda = tp.getLambda();
		this.horCut = tp.getHorizonCut();
		this.gamma = tp.getGamma();
		this.epochs = tp.getEpochs();
		this.nply = tp.getNPly();
		this.mode3P = tp.getMode3P();
		this.eligMode = tp.getEligMode();
		this.featmode = tp.getFeatmode();
		this.useNormalize = tp.getNormalize();
		this.hasLinNet = tp.hasLinearNet();
		this.hasRprop = tp.hasRpropLrn();
		this.hasSigmoid = tp.hasSigmoid();
	}

	public ParTD(TDParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(TDParams tp) {
		this.alpha = tp.getAlpha();
		this.alfin = tp.getAlphaFinal();
		this.epsil = tp.getEpsilon();
		this.epfin = tp.getEpsilonFinal();
		this.lambda = tp.getLambda();
		this.horCut = tp.getHorizonCut();
		this.gamma = tp.getGamma();
		this.epochs = tp.getEpochs();
		this.nply = tp.getNPly();
		this.mode3P = tp.getMode3P();
		this.eligMode = tp.getEligMode();
		this.featmode = tp.getFeatmode();
		this.useNormalize = tp.getNormalize();
		this.hasLinNet = tp.hasLinearNet();
		this.hasRprop = tp.hasRpropLrn();
		this.hasSigmoid = tp.hasSigmoid();
	}

	public double getAlpha() {
		return alpha;
	}

	public double getAlphaFinal() {
		return alfin;
	}

	public double getEpsilon() {
		return epsil;
	}

	public double getEpsilonFinal() {
		return epfin;
	}

	public double getGamma() {
		return gamma;
	}

	public double getLambda() {
		return lambda;
	}

	/**
	 * The parameter {@code C = } {@link #getHorizonCut()} controls the horizon in eligibility traces: 
	 * Retain only those elements in the TD-update equation where {@code lambda^(t-k)} &ge; {@code C}.  
	 * <br>(The horizon {@code h = t-k} runs from 0,1,..., to the appropriate {@code h = ceil(log_lambda(C))}.
	 * 
	 * @see NTuple2ValueFunc#setHorizon()
	 */
	public double getHorizonCut() {
		return horCut;
	}

	public int getEpochs() {
		return epochs;
	}

	public int getFeatmode() {
		return featmode;
	}

	public boolean getNormalize() {
		return useNormalize;
	}

	public int getNPly() {
		return nply;
	}

	public int getMode3P() {
		return mode3P;
	}

	public int getEligMode() {
		return eligMode;
	}

	public boolean hasLinearNet() {
		return hasLinNet;
	}

	public boolean hasRpropLrn() {
		return hasRprop;
	}

	public boolean hasSigmoid() {
		return hasSigmoid;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public void setAlphaFinal(double alfin) {
		this.alfin = alfin;
	}

	public void setEpsilon(double epsil) {
		this.epsil = epsil;
	}

	public void setEpsilonFinal(double epfin) {
		this.epfin = epfin;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * The parameter {@code C = horCut} controls the horizon in eligibility traces: 
	 * Retain only those elements in the TD-update equation where {@code lambda^(t-k)} &ge; {@code C}.  
	 * <br>(The horizon {@code h = t-k} runs from 0,1,..., to the appropriate {@code h = ceil(log_lambda(C))}.
	 * 
	 * @see NTuple2ValueFunc#setHorizon()
	 */
	public void setHorizonCut(double horCut) {
		this.horCut = horCut;
	}

	public void setNPly(int nply) {
		this.nply=nply;
	}
	
	public void setMode3P(int mode3P) {
		this.mode3P=mode3P;
	}
	
	public void setEligMode(int eligMode) {
		this.eligMode=eligMode;
	}
	
	public void setEpochs(int epochs) {
		this.epochs = epochs;
	}

	public void setFeatmode(int featmode) {
		this.featmode = featmode;
	}

	public void setNormalize(boolean useNormalize) {
		this.useNormalize = useNormalize;
	}

	public void setLinearNet(boolean hasLinNet) {
		this.hasLinNet = hasLinNet;
	}

	public void setRprop(boolean hasRprop) {
		this.hasRprop = hasRprop;
	}

	public void setSigmoid(boolean hasSigmoid) {
		this.hasSigmoid = hasSigmoid;
	}

}
