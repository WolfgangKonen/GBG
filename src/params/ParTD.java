package params;

import java.io.Serializable;

public class ParTD implements Serializable {
	
    public static int DEFAULT_EPOCHS = 1;
    public static double DEFAULT_ALPHA=0.001;  // 0.1
    public static double DEFAULT_ALFIN=0.001;  
    public static double DEFAULT_EPSIL=0.3;
    public static double DEFAULT_EPFIN=0.0;  
    public static double DEFAULT_GAMMA=1.0;  
    public static double DEFAULT_LAMBDA=0.0;  
    
    private double alpha = DEFAULT_ALPHA;		// initial learn step size
    private double alfin = DEFAULT_ALFIN;		// final learn step size
    private double epsil = DEFAULT_EPSIL;		// initial random move rate 
    private double epfin = DEFAULT_EPFIN;		// final random move rate
    private double gamma = DEFAULT_GAMMA;		// discount-rate parameter (typically 1.0 or 0.9) 
    private double lambda= DEFAULT_LAMBDA; 		// eligibility trace decay parameter (should be <= GAMMA) 
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

	public ParTD() {	}
    
    public ParTD(TDParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(TDParams tp) {
		this.alpha = tp.getAlpha();
		this.alfin = tp.getAlphaFinal();
		this.epsil = tp.getEpsilon();
		this.epfin = tp.getEpsilonFinal();
		this.lambda = tp.getLambda();
		this.gamma = tp.getGamma();
		this.epochs = tp.getEpochs();
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

	public int getEpochs() {
		return epochs;
	}

	public int getFeatmode() {
		return featmode;
	}

	public boolean getNormalize() {
		return useNormalize;
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
