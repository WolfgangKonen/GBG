package params;

import java.io.Serial;
import java.io.Serializable;

import javax.swing.JPanel;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.NTuple2ValueFunc;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;

/**
 *  TD (temporal difference) parameters for agents {@link TDAgent},  {@link TDNTuple3Agt} and {@link SarsaAgt}
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 *  
 *  @see TDParams
 *  @see TDAgent
 *  @see TDNTuple3Agt
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
//    private int mode3P = DEFAULT_MODE_3P;
    private int eligMode = DEFAULT_ELIG_MODE;
    private int epochs = DEFAULT_EPOCHS; 
    private int featmode = 0;
    private boolean useNormalize = false;
    private boolean hasLinNet = true;
    private boolean hasRprop = false;
    private boolean hasSigmoid = false;
	private boolean hasStopOnRoundOver = false;

    /**
     * This member is only constructed when the constructor {@link #ParTD(boolean) ParTD(boolean withUI)} 
     * called with {@code withUI=true}. It holds the GUI for {@link ParTD}.
     */
    private transient TDParams tdparams = null;
    
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	public ParTD() { 	}
    
	public ParTD(boolean withUI) {
		if (withUI)
			tdparams = new TDParams();
	}
	
	public ParTD(ParTD tp) {
		this.setFrom(tp);
	}

	public ParTD(TDParams tp) { 
    	this.setFrom(tp);
    }
    
	public void setFrom(ParTD tp) {
		this.alpha = tp.getAlpha();
		this.alfin = tp.getAlphaFinal();
		this.epsil = tp.getEpsilon();
		this.epfin = tp.getEpsilonFinal();
		this.gamma = tp.getGamma();
		this.lambda = tp.getLambda();
		this.horCut = tp.getHorizonCut();
		this.epochs = tp.getEpochs();
		this.nply = tp.getNPly();
		this.hasSigmoid = tp.hasSigmoid();
		this.hasStopOnRoundOver = tp.hasStopOnRoundOver();
		this.useNormalize = tp.getNormalize();
		this.eligMode = tp.getEligMode();
		this.featmode = tp.getFeatmode();
		this.hasLinNet = tp.hasLinearNet();
		this.hasRprop = tp.hasRpropLrn();
//		this.mode3P = tp.getMode3P();
		
		if (tdparams!=null)
			tdparams.setFrom(this);
	}
	
	public void setFrom(TDParams tp) {
		this.alpha = tp.getAlpha();
		this.alfin = tp.getAlphaFinal();
		this.epsil = tp.getEpsilon();
		this.epfin = tp.getEpsilonFinal();
		this.gamma = tp.getGamma();
		this.lambda = tp.getLambda();
		this.horCut = tp.getHorizonCut();
		this.epochs = tp.getEpochs();
		this.nply = tp.getNPly();
		this.hasSigmoid = tp.hasSigmoid();
		this.hasStopOnRoundOver = tp.hasStopOnRoundOver();
		this.useNormalize = tp.getNormalize();
		this.eligMode = tp.getEligMode();
		this.featmode = tp.getFeatmode();
		this.hasLinNet = tp.hasLinearNet();
		this.hasRprop = tp.hasRpropLrn();
//		this.mode3P = tp.getMode3P();
		
		if (tdparams!=null)
			tdparams.setFrom(this);
	}

	/**
	 * Call this from XArenaFuncs constructAgent or fetchAgent to get the latest changes from GUI
	 */
	public void pushFromTDParams() {
		if (tdparams!=null)
			this.setFrom(tdparams);
	}
	
	public void enableAll(boolean enable) {
		if (tdparams!=null)
			tdparams.enableAll(enable);
	}
//	public void enableMode3P(boolean enable) {
//		if (tdparams!=null)
//			tdparams.enableMode3P(enable);
//	}
//	@Deprecated
//	public void enableNPly(boolean enable) {
//		if (tdparams!=null)
//			tdparams.enableNPly(enable);
//	}
	
	public JPanel getPanel() {
		if (tdparams!=null)		
			return tdparams.getPanel();
		return null;
	}

	// We abandon here in all the following getters the fetching of latest changes from the GUI.
	// Because this leads to bugs, if we call these getters from PlayAgent's fillParamTabsAfterLoading.
	//   
	// If we want to push GUI param changes from member tdparams up to this ParTD (since we have no
	// event listeners for every element of tdparams), we should call pushFromTdparams() 
	// explicitly (e.g. when constructing an agent prior to training it)
	
	public double getAlpha() {
//		if (tdparams!=null)					// commented out, use instead pushFromTdparams() 
//			alpha = tdparams.getAlpha(); 	// to get the latest changes from GUI (!)
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

//	public int getMode3P() {
//		return mode3P;
//	}

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

	public boolean hasStopOnRoundOver() {
		return hasStopOnRoundOver;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
		if (tdparams!=null)
			tdparams.setAlpha(alpha);
	}

	public void setAlphaFinal(double alfin) {
		this.alfin = alfin;
		if (tdparams!=null)
			tdparams.setAlphaFinal(alfin);
	}

	public void setEpsilon(double epsil) {
		this.epsil = epsil;
		if (tdparams!=null)
			tdparams.setEpsilon(epsil);
	}

	public void setEpsilonFinal(double epfin) {
		this.epfin = epfin;
		if (tdparams!=null)
			tdparams.setEpsilonFinal(epfin);
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
		if (tdparams!=null)
			tdparams.setGamma(gamma);
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
		if (tdparams!=null)
			tdparams.setLambda(lambda);
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
		if (tdparams!=null)
			tdparams.setHorizonCut(horCut);
	}

//	public void setNPly(int nply) {
//		this.nply=nply;
//		if (tdparams!=null)
//			tdparams.setNPly(nply);
//	}
	
//	public void setMode3P(int mode3P) {
//		this.mode3P=mode3P;
//		if (tdparams!=null)
//			tdparams.setMode3P(mode3P);
//	}
	
	public void setEligMode(int eligMode) {
		this.eligMode=eligMode;
		if (tdparams!=null)
			tdparams.setEligMode(eligMode);
	}
	
	public void setEpochs(int epochs) {
		this.epochs = epochs;
		if (tdparams!=null)
			tdparams.setEpochs(epochs);
	}

	public void setFeatmode(int featmode) {
		this.featmode = featmode;
		if (tdparams!=null)
			tdparams.setFeatmode(featmode);
	}
	public void setFeatList(int[] featList){
		if (tdparams!=null)
			tdparams.setFeatList(featList);
	}

	public void setNormalize(boolean useNormalize) {
		this.useNormalize = useNormalize;
		if (tdparams!=null)
			tdparams.setNormalize(useNormalize);
	}

	public void setLinearNet(boolean hasLinNet) {
		this.hasLinNet = hasLinNet;
		if (tdparams!=null)
			tdparams.setLinearNet(hasLinNet);
	}

	public void setRprop(boolean hasRprop) {
		this.hasRprop = hasRprop;
		if (tdparams!=null)
			tdparams.setRprop(hasRprop);
	}

	public void setSigmoid(boolean hasSigmoid) {
		this.hasSigmoid = hasSigmoid;
		if (tdparams!=null)
			tdparams.setSigmoid(hasSigmoid);
	}

	public void setStopOnRoundOver(boolean hasStopOnRound) {
		this.hasStopOnRoundOver = hasStopOnRound;
		if (tdparams!=null)
			tdparams.setStopOnRoundOver(hasStopOnRound);
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameters producing good results. If withUI, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName one out of
	 * 			"TD-Ntuple-3" ({@link TDNTuple3Agt}),
	 * 			"Sarsa" ({@link SarsaAgt}) or "TDS" ({@link TDAgent})
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		// Currently we have here only the sensible defaults for some games and
		// for three agents ("TD-Ntuple[-2,-3]" = class TDNTuple[2,3]Agt and "TDS" = class TDAgent).
		//
		// If later good parameters for other games are found, they should be
		// added with suitable nested switch(gameName). 
		// Currently we have only one switch(gameName) on the initial featmode (=3 for 
		// TicTacToe, =2 for Hex, and =0 for all others)
		this.setHorizonCut(0.1); 			
		switch (agentName) {
//		case "TD-Ntuple-2":
		case "TD-Ntuple-3": 
		case "Sarsa":
			switch (agentName) {
//			case "TD-Ntuple-2":
			case "TD-Ntuple-3": 
				switch (gameName) {
				case "ConnectFour":
					this.setAlpha(5.0);
					this.setAlphaFinal(5.0);
					this.setHorizonCut(0.01);
					break;
				default: 
					this.setAlpha(0.2);
					this.setAlphaFinal(0.2);				
				}
				break;
			case "Sarsa": 
				this.setAlpha(1.0);
				this.setAlphaFinal(0.5);
				this.setEpsilon(0.1);
				break;
			default:	//  all other
				this.setEpsilon(0.3);				
				break;
			}
			this.setEpsilonFinal(0.0);
			this.setLambda(0.0);
			this.setGamma(1.0);
			this.setEpochs(1);
			this.setSigmoid(true);		// tanh
			this.setNormalize(false); 
			switch (gameName) {
			case "2048": 
				this.setEpsilon(0.0);				
				break;
			}
			break;
		case "TDS":
			this.setAlpha(0.1);
			this.setAlphaFinal(0.001);
			this.setEpsilonFinal(0.0);
			this.setLambda(0.9);
			this.setHorizonCut(0.1);
			this.setGamma(1.0);
			this.setEpochs(1);
			this.setSigmoid(false);		// Fermi fct
			this.setNormalize(false); 
			switch (gameName) {
			case "TicTacToe": 
				setFeatmode(3);
				break;
			case "Hex": 
				setFeatmode(2);
				this.setLambda(0.0);				
				break;
			default:	//  all other
				setFeatmode(0);
				this.setLambda(0.0);				
				break;
			}
			switch (gameName) {
			case "2048": 
				this.setEpsilon(0.0);				
				break;
			default:	//  all other
				this.setEpsilon(0.3);				
				break;
			}
			break;
		}	
		
		if (tdparams!=null)
			tdparams.setFrom(this, agentName);
	}
	
}
