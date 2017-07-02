package controllers.TD;

/**
 * The abstract interface for the value function approximators.
 * <p>
 * Known implementations: {@link TD_Lin}, {@link TD_NNet}
 * <p>
 * Known classes using TD_func: <ul>
 * <li> {@link TDAgent}
 * </ul>
 * 
 * @author Wolfgang Konen, TH Köln, Apr'08-Nov'16
 *
 */
public interface TD_func {
	
	public boolean FERMI_FCT = false; // if false, take tanh instead
	
	abstract public void resetElig();
	abstract public void calcScoresAndElig(double[] Input);
	public double[] updateWeights(double reward, double[] Input, boolean finished, boolean wghtChange);
	public void finishUpdateWeights();
	public int getHiddenLayerSize();			// only for TD_NNet
    public int getDimensions(); 				// number of free parameters (weights + biases)
	public double getAlphaChangeRatio();
	public double getAlpha();
	public double getBeta();
	public double getLambda();
	public double getScore(double[] Input);
	public void updateElig(double[] Input);
	public void setAlphaChangeRatio(double newAlphaChangeRatio);
	public void setAlpha(double newStartAlpha);
	public void setBeta(double newStartBeta);	// only for TD_NNet
	public void setGamma(double newGamma);
	public void setLambda(double newLambda);
	//public void setEpochs(int epochs);
	public void setRpropLrn(boolean hasRpropLrn);
	public void setRpropInitDelta(double initDelta);
    public void setWeights(double[] wv);
    public double[] getWeights();
}
