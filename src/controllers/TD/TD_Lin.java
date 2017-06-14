package controllers.TD;

import java.util.Random;
import java.io.*;

import org.jfree.data.xy.XYSeries;

import tools.LineChartSuccess;

/** 

<b> Linear TD: </b><p> 
The linear network is trained with a perceptron learning rule.

The linear network is structured using simple array data structures as follows:

<pre>
                    OUTPUT
   
                  ()  ()  ()  y[k]
                 /   / \    \      k=0...m-1
        		/    |  \    \
               |     |   |   |         
                \   v[i][k]  /
                 \  /\  /\  /      eligibilities ev[i][k] 
                  ()  ()  ()  x[i]
                                   i=0...n
                     INPUT

</pre>
where x and y are (arrays holding) the activity levels of the input,
and output units respectively, v are the I-O weights, and ev are the 
eligibility traces (see Sutton, 1989). Not explicitly shown in the figure
are the biases or threshold weights. The bias is provided by
a dummy nth input unit. The activities of both of these dummy units
are held at a constant value (BIAS).

*/

public class TD_Lin implements TD_func, Serializable {
    protected Random rand;

    /* Experimental Parameters: */

    int    n, m; 		/* number of inputs,  output units */
    int    time_steps;  		/* number of time steps to simulate */
    protected double   EPS=0.05;	   	/* random weights init scale */	
    protected double   BIAS=1.0;   	/* strength of the bias (constant input) */
    protected double   ALPHA=0.1;  	/* 1st layer learning rate (typically 1/n) */
    protected double   GAMMA=1.0;  	/* discount-rate parameter (typically 0.9) */
    protected double   LAMBDA=0.0; 	/* trace decay parameter (should be <= gamma) */
    protected boolean  withSigmoid=false;	
    protected boolean  rpropLrn=false;

    /* Network Data Structure: */

    protected double[] y;		// output layer
    protected double[] x;		// input layer
    protected double[][] v;		// weights input-to-output [n][m]
    
    /* Learning Data Structure: */

    double  old_y[];
    double  old_Input[];
    double  ev[][]; 	/* trace */
    double  error[];  	/* TD error */
    double  EtSum = 0;			// the sum of the loss function over all epoch iterations
    double  gradSum[][];		// the sum of the gradients of the loss function 0.5 error^2 
    							// w.r.t. the weights (sum over all epoch iterations)
    //protected RPROP rp;
	protected double[] gradients;

    protected int m_InputNum = 3;
    protected int m_NeuronsNum = 3;
    protected int m_TimePeriod = 10;
    protected double m_AlphaChangeRatio = 0.9998; //0.998;
    
	/* only visual debugging */
    protected boolean VISU_DBG = false;
    protected transient static LineChartSuccess lChart=null;
    protected transient XYSeries series0;
    protected transient XYSeries series1;
    protected transient XYSeries seriesN;
    protected transient static LineChartSuccess wChart=null;
    protected transient XYSeries wSeries0;
    protected transient XYSeries wSeries1;
    protected transient XYSeries wSeriesN;
    protected int 	   iterates=0;
	protected double[] flatWeights;
	protected double[] wOpt = {-0.127757,0.254308, 0.095779,-0.107197,-0.015401, 0.144247
							  ,-0.108901,0.003043,-0.063511, 0.004583,-0.004598,-0.00304
							  ,-0.00417,-0.00304 ,-0.00115 ,-0.002621,-0.004138,-0.002689
							  ,-0.003712,0.496125, 1.00000};
	// weights wOpt are near-optimal settings for feature set 3, used only for debugging in
	// this.setRpropLrn()

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	

    private void initNet() {
        rand = new Random(System.currentTimeMillis());
        y = new double[m];
        old_y = new double[m];
        error = new double[m];
        x = new double[n+1];
        old_Input = new double[n];		// omit the bias neuron [n]
        v = new double[n+1][m];
        ev = new double[n+1][m];
        gradSum = new double[n+1][m];
        initWeights();
        
        int flatD = (n+1)*m;
//      RPROP.RPROPType mRPROP = RPROP.RPROPType.iRPROPp;
//		rp = new RPROP(flatD);
//		rp.setRPROPType(mRPROP);
		gradients = new double[flatD];
		flatWeights = new double[flatD];
		
		/* only visual debugging */
		if (VISU_DBG) {
			if (lChart==null) lChart=new LineChartSuccess("Step Sizes","iterates","step size",
					  true,false);
			lChart.clear();
			series0 = new XYSeries("step size 0");		
			lChart.addSeries(series0);
			series1 = new XYSeries("step size 1");		
			lChart.addSeries(series1);
			seriesN = new XYSeries("step size N");		
			lChart.addSeries(seriesN);

			if (wChart==null) wChart=new LineChartSuccess("Weights","iterates","weight",
					  true,false);
			wChart.clear();
			wChart.setLocation(400, 0);
			wSeries0 = new XYSeries("weight 0");		
			wChart.addSeries(wSeries0);
			wSeries1 = new XYSeries("weight 1");		
			wChart.addSeries(wSeries1);
			wSeriesN = new XYSeries("bias weight");		
			wChart.addSeries(wSeriesN);
		}

    }
    
/*   
    public TD_Lin() {
        initNet();
    }
 */
	public TD_Lin(int InputSize, boolean withSig) {
	    n = InputSize;
	    m = 1;			// currently always *one* output neuron
	    withSigmoid = withSig;
	    initNet();
	}
	
	/**
	 *  Accumulate the gradient of the loss function 0.5*error[k]*error[k]
	 */
	private void TDlearn() {
		int i,k;

    	// assert ev[0][0]==0.0 : "ev[0][0] is not 0";
		// This assertion is only for TicTacToe diagnostics and first n-Tuple = {0,1,2} or similar:
		// in this case ev[0][0] corresponds to the weight for "OOO" which is a final 
		// state and as such should never be trained.

		for (k=0;k<m;k++) {
			for (i=0;i<=n;i++)
			{
				// note that ev contains the gradient of y w.r.t. the weights, see updateElig()
				gradSum[i][k]+=error[k]*ev[i][k];
			}
			EtSum += 0.5*error[k]*error[k];			// needed only for rpropLrn==true
		}
		
	}/* end TDlearn */
	
	private void TDchangeWeights() {		
			int i,k;
//			if (rpropLrn) {
//				int index=0;
//				double weightChange;
//				for (k=0;k<m;k++)
//					for (i=0;i<=n;i++)
//					{
//						// gradSum contains the sum over all epoch iterations of 
//						// - gradient of loss function 0.5 error^2 
//						gradients[index] =  - gradSum[i][k];			// note the "-" sign !!
//						weightChange = rp.updateWeight(gradients, index, EtSum);
//						v[i][k] += weightChange;
//						
//						// only debug
//						//if (i==1) System.out.println("v[1]="+v[i][k]+", stepsz="+rp.getLastDelta()[1]+", wghtChng="+ weightChange);
//
//						gradSum[i][k]=0;				// reset gradient sum (for next epoch)
//						EtSum=0;
//						index++;
//					}
//
//				/* only visual debugging */
//				if (VISU_DBG) {
//					series0.add((double)iterates, rp.getLastDelta()[0]);
//					series1.add((double)iterates, rp.getLastDelta()[1]);
//					seriesN.add((double)iterates, rp.getLastDelta()[rp.getLastDelta().length-1]);
//					lChart.plot();
//					wSeries0.add((double)iterates, this.getWeights()[0]);
//					wSeries1.add((double)iterates, this.getWeights()[1]);
//					wSeriesN.add((double)iterates, this.getWeights()[this.getWeights().length-2]);
//					wChart.plot();
//				}
//
//			} else 
			{
				int index=0;
				for (k=0;k<m;k++)
				{
					for (i=0;i<=n;i++,index++) {
						// gradSum contains the sum over all epoch iterations of 
						// - gradient of loss function 0.5 error^2 
						v[i][k]+=ALPHA*gradSum[i][k];

						// only debugging
						//if (i==1) System.out.println("v[1]="+v[i][k]+", wghtChng="+ ALPHA*gradSum[i][k]);
						gradients[index] = - gradSum[i][k];		// note the "-" sign !!
						flatWeights[index] = v[i][k];			

						gradSum[i][k]=0;				// reset gradient sum (for next epoch)
					}

				}
				/* only visual debugging */
				if (VISU_DBG) {
					series0.add((double)iterates, this.getAlpha());
					lChart.plot();
					wSeries0.add((double)iterates, this.getWeights()[0]);
					wSeries1.add((double)iterates, this.getWeights()[1]);
					wSeriesN.add((double)iterates, this.getWeights()[this.getWeights().length-2]);
					wChart.plot();
				}
			}
			
		//} // trainCounter
			
		// only for debugger stop
		if (iterates % 100 == 0) {
			int dummy=0;
		}

	}
	
    public void resetElig() {
		int i,k;
		for (k=0;k<m;k++)
			for (i=0;i<=n;i++)
				ev[i][k] = 0.0;
	}
    
   public void calcScoresAndElig(double[] Input) {
    	getScore(Input);	// writes score on y[k]
        updateElig(Input);	// copy to old_y[k] & update eligibilities
    }

    /**
     * update weights based on current reward, current input and on the output old_y 
     * of the previous step
     * @param reward	reward of the current board position
     * @param Input		feature vector derived from Table, the current board position
     * @param finished	is the current board position an end state?
     * @param wghtChange	should we only accumulate weight changes [false] or apply them also
     * 					to the weights [true]?
     * @return 			the vector of error signals on all output units
     */
    public double[] updateWeights(double reward, double[] Input, boolean finished, boolean wghtChange) {
        boolean DEBG = false;
        getScore(Input);	// forward pass - compute activities on y[k] (=V(s_t+1) = f(w_t,s_t+1))
    	double target = (finished ? reward : GAMMA*y[0]);
    	for (int k=0;k<m;k++) 
           error[k] = target - old_y[k];
        this.TDlearn();			// backward pass - learning: accumulate weight changes
        if (wghtChange) this.TDchangeWeights();		// apply weight changes
        if (DEBG) {
            getScore(old_Input);	
        	double new_error = target - y[0];
//        	System.out.println("error:"+error[0]);
//        	System.out.println("new_e:"+new_error);
        	for (int i=0;i<old_Input.length;++i) System.out.print((int)old_Input[i]);
        	System.out.print(" ");
        	System.out.println("err quotient: "+100*(new_error/error[0])+"% "+error[0]+"  "
        						+target+"   "+reward + (finished ? "*" : ""));
        }
        
        calcScoresAndElig(Input);
    	// forward pass must be done twice to form TD errors: calculate score with new weights,
    	// copy it to old_y[k] for next pass & update eligibilities
    	// (for use in next cycle's TD errors) 
        
        iterates++;
        
        return error;
    }

    /**
     * Copy current output to {@code old_y} and update eligibilities (gradient) {@code ev}
     * for next pass through loop. In the case {@code LAMBDA=0} the eligibility
     * {@code ev[i][k]} is just the gradient of the output function y[k] w.r.t. 
     * to the weight v[i][k]. This amounts to <p>
     *  {@code ev[i][k] = x[i]} in the linear case w/o sigmoid  and <p>
     *  {@code ev[i][k] = y[k]*(1-y[k])*x[i]} in the linear case with Fermi sigmoid fct.
     *  
     *  @see #getScore(double[]) getScore(double), which defines the Fermi sigmoid fct.
     */
    public void updateElig(double[] Input) {
    	int i,k;
    	double temp[] = new double[m];

		for (i=0;i<n;i++) {				
			old_Input[i] = Input[i];	// only for DEBG
		}
    	for (k=0;k<m;k++)
    		old_y[k] = y[k];

    	if (withSigmoid) {
    		for (k=0;k<m;k++)	temp[k]=y[k]*(1-y[k]);
    	} else {
    		for (k=0;k<m;k++)	temp[k]=1.0;
    	}

    	for (k=0;k<m;k++)
    	{
    		for (i=0;i<=n;i++)
    		{
    			ev[i][k]=LAMBDA*GAMMA*ev[i][k]+temp[k]*x[i];		// /WK/02/2015 added GAMMA (!) 
    		}
    	}
    }/* end UpdateElig */

	/**
	 *  Adjust learn parameter ALPHA. <p>
	 *  
	 *  Known caller: {@link TDAgent#trainAgent}
	 */
    public void finishUpdateWeights() {
        ALPHA = ALPHA * m_AlphaChangeRatio;
    }
    public double getAlphaChangeRatio() {
        return m_AlphaChangeRatio;
    }
    public int getHiddenLayerSize() {
        return 0;
    }
    public double getLambda() {
        return LAMBDA;
    }
    public double getAlpha() {
        return ALPHA;
    }
    public double getBeta() {
        return 0;
    }
    /**
     * Calculate net output for given Input (fct Response in [SuttonBonde93])
     * <p> net output = V(s_t): the higher, the more likely is s_t a win position for player +1 (white)
     * @param Input		feature vector derived from Table, the current board position
     * @return			the output of the output neuron, y[0]
     * 					(which is also changed as class variable)
     */
    public double getScore(double[] Input) {
    	int j,k;
    	
        // initialize input layer & bias neurons:	
    	if (Input.length != n) throw new RuntimeException("Wrong length (Input.length != n)");
    	System.arraycopy(Input, 0, x, 0, Input.length);
    	x[n] = BIAS;

    	for (k=0;k<m;k++)
    	{
    		y[k]=0.0;
    		for (j=0;j<=n;j++)
    		{
    			y[k]+=x[j]*v[j][k];
    		}
    		if (withSigmoid)
    			y[k]=1.0/(1.0+Math.exp(-y[k])); /* asymmetric sigmoid (Fermi fct) \in [0,1] */
//    			y[k]=2.0*y[k]-1.0;				
    			/* uncomment the line above to map to symmetric sigmoid \in [-1,1]. This
    			 * needs a factor 2 in the withSigmoid-branch of updateElig() */ 

    	}
    	return(y[0]);
    }/* end getScore */

    // initialize weights with random numbers from [-EPS,EPS]; 
    // initialize bias unit with BIAS
    private void initWeights() {
    	int k,i;

    	x[n]=BIAS;
    	for (k=0;k<m;k++)
    	{
    		old_y[k]=0.0;
    		for (i=0;i<=n;i++)
    		{
    			v[i][k]= EPS*(rand.nextDouble() * 2 - 1);
    			ev[i][k]=0.0;
    		}
    	}

    } /* end initWeights */
    
    /**
     *  Set the weights and bias from one long double vector wv of size (n+1)*m+1. <ul>
     *  <li> wv[0..n] = weights from inputs (incl. bias neuron) to 1st output
     *  <li> wv[n+1..2n+1] = weights from inputs (incl. bias neuron) to 2nd output
     *  <li> ...
     *  <li> wv[wv.length-1] = activity of bias neuron
     *  </ul>
     */ 
    public void setWeights(double[] wv) {
    	int i,k,kv;
    	int sz = (n+1)*m+1;

    	assert wv.length>=sz : "length of w = "+wv.length+" is smaller than size of net: "+sz;
    	
    	x[n]=wv[sz-1];
    	for (k=0,kv=0;k<m;k++)
    	{
    		old_y[k]=0.0;
    		for (i=0;i<=n;i++,kv++)
    		{
    			v[i][k]= wv[kv];
    			ev[i][k]=0.0;
    		}
    	}

    	assert kv==sz-1 : "count error kv";
    	
    } /* end setWeights */

    /**
     *  Get the weights (inc. bias weight) into one long double vector wv of size (n+1)*m+1. <ul>
     *  <li> wv[0..n] = weights from inputs (incl. bias neuron) to 1st output
     *  <li> wv[n+1..2n+1] = weights from inputs (incl. bias neuron) to 2nd output
     *  <li> ...
     *  <li> wv[wv.length-2] = weight of bias neuron
     *  <li> wv[wv.length-1] = activity of bias neuron (usually = 1.0)
     *  </ul>
     */ 
    public double[] getWeights() {
    	int i,k,kv;
    	int sz = (n+1)*m+1;
    	double[] wv = new double[sz];

    	
    	wv[sz-1] = x[n];
    	for (k=0,kv=0;k<m;k++)
    	{
    		for (i=0;i<=n;i++,kv++)
    		{
    			wv[kv] = v[i][k];
    		}
    	}

    	assert kv==sz-1 : "count error kv";

    	return wv;
    } /* end getWeights */

    /** 
     *  Get number of free parameters (weights + bias).
     */
    public int getDimensions() {
    	return (n+1)*m+1;
    }
    
    public static TD_Lin loadNet(String FileName) {
        TD_Lin Net = null;

        if (FileName != null) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(FileName));
                Net = new TD_Lin(1,false);
/*
                Net.m_OutputLayer = in.readDouble();
                Net.m_InputLayer = (double[]) in.readObject();
                Net.m_HiddenLayerOutputs = (double[]) in.readObject();
                Net.m_HiddenLayerWeights = (double[]) in.readObject();
                Net.m_OutputLayerWeights = (double[]) in.readObject();
                Net.m_InputNum = in.readInt();
                Net.m_NeuronsNum = in.readInt();
                Net.m_TimePeriod = in.readInt();
                Net.ALPHA = in.readDouble();
                Net.LAMBDA = in.readDouble();
                Net.m_AlphaChangeRatio = in.readDouble();
 */                
                in.close();
            } catch (Exception E) {
				Net = null;
            }
        }
        return Net;
    }

    public void saveNet(String FileName) {
        if (FileName != null) {
            try {
                ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(FileName, false));
/*                
                out.writeDouble(m_OutputLayer);
                out.writeObject(m_InputLayer);
                out.writeObject(m_HiddenLayerOutputs);
                out.writeObject(m_HiddenLayerWeights);
                out.writeObject(m_OutputLayerWeights);
                out.writeInt(m_InputNum);
                out.writeInt(m_NeuronsNum);
                out.writeInt(m_TimePeriod);
                out.writeDouble(ALPHA);
                out.writeDouble(LAMBDA);
                out.writeDouble(m_AlphaChangeRatio);
 */                
                out.close();
            } catch (Exception E) {

            }
        }
    }

    public void setAlphaChangeRatio(double newAlphaChangeRatio) {
        m_AlphaChangeRatio = newAlphaChangeRatio;
    }

    public void setLambda(double newLambda) {
        LAMBDA = newLambda;
    }
    public void setAlpha(double newStartAlpha) {
        ALPHA = newStartAlpha;
    }
    public void setBeta(double newStartBeta) {
        // dummy
    }
    public void setGamma(double newStartGamma) {
        GAMMA = newStartGamma;
    }
	public void setRpropLrn(boolean hasRpropLrn) {
		rpropLrn = hasRpropLrn;
		
		// only debugging: 
		// Does RPROP stay stable if we start from optimal weights?
		//if (rpropLrn) {
		//	this.setWeights(wOpt);
		//}
	}
	public void setRpropInitDelta(double initDelta) {
		//rp.setInitDelta(initDelta);
	}
    
}
