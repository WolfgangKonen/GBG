package controllers.TD;
import java.util.Random;
import java.io.*;

import org.jfree.data.xy.XYSeries;

import tools.LineChartSuccess;

/**

<b> Nonlinear TD/Backprop: </b><p> 

This Java implementation follows closely the pseudo C++ code given
by Allen Bonde Jr. and Rich Sutton in April 1992. 

Updated in June and August 1993. 
Copyright 1993 GTE Laboratories Incorporated. All rights reserved. 
Permission is granted to make copies and changes, with attribution,
for research and educational purposes.
http://www.cs.ualberta.ca/~sutton/td-backprop-pseudo-code.text

The network is structured using simple array data structures as follows:
<PRE>
                    OUTPUT
   
                  ()  ()  ()  y[k]
                 /  \/  \/  \      k=0...m-1
                /   w[j][k]  \         eligibilities ew[j][k]
               /              \
              ()  ()  ()  ()  ()     h[j]     j=0...num_hidden
               \              /        
                \   v[i][j]  /         eligibilities ev[i][j][k]      
                 \  /\  /\  /
                  ()  ()  ()  x[i]
                                   i=0...n
                     INPUT
</PRE>

where x, h, and y are (arrays holding) the activity levels of the input,
hidden, and output units respectively, v and w are the first and second
layer weights, and ev and ew are the eligibility traces for the first
and second layers (see Sutton, 1989). Not explicitly shown in the figure
are the biases or threshold weights. The first layer bias is provided by
a dummy nth input unit, and the second layer bias is provided by a dummy
(num-hidden)th hidden unit. The activities of both of these dummy units
are held at a constant value (BIAS), see {@link getScore}.

*/
public class TD_NNet implements TD_func, Serializable {
    protected Random rand;

    /* Experimental Parameters: */

    int    n, num_hidden, m; /* number of inputs, hidden, and output units */
    int    time_steps;  		/* number of time steps to simulate */
    protected double   EPS=0.5;	   	/* random weights init scale */	
    protected double   BIAS=1.0;   	/* strength of the bias (constant input) */
    protected double   ALPHA=0.1;  	/* 1st layer learning rate (typically 1/n) */
    protected double   BETA=0.1;   	/* 2nd layer learning rate (typically 1/num_hidden) */
    protected double   GAMMA=1.0;  	/* discount-rate parameter (typically 0.9) */
    protected double   LAMBDA=0.0; 	/* trace decay parameter (should be <= gamma) */
    protected boolean  withSigmoid=false;	
    protected boolean  rpropLrn=false;
    //protected int 	   epochMax=1;	 
    //protected int 	   epochCounter=0;

    /* Network Data Structure: */

    protected double[] y;		// output layer
    protected double[] x;		// input layer
    protected double[] h;		// hidden layer
    protected double[][] w;		// weights hidden-to-output
    protected double[][] v;		// weights input-to-hidden
    
    /* Learning Data Structure: */

    double  old_y[];
    double  old_Input[];		// only for DEBG==true
    double  ev[][][]; 	/* hidden trace */
    double  ew[][]; 	/* output trace */
    double  error[];  	/* TD error */
    double  EtSum = 0;			// the sum of the loss function over all epoch iterations
    double  gradSumV[][][];		// the sum of the gradients of the loss function 0.5 error^2 
    double  gradSumW[][];		// w.r.t. the weights (sum over all epoch iterations)
    
    //protected RPROP rp;
	protected double[] gradients;

    protected int m_InputNum = 3;
    protected int m_NeuronsNum = 3;
    protected int m_TimePeriod = 10;
    protected double[] m_LambdaPower;	// currently not in use
    protected double m_AlphaChangeRatio = 0.9998; //0.998;

	/* only visual debugging */
    protected transient LineChartSuccess lChart=null;
    protected transient XYSeries series;
    protected int 	   iterates=0;
	protected double[] flatWeights;

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

    private void initNet() {
    	prepareLambdaPower();
    	rand = new Random(System.currentTimeMillis());
    	y = new double[m];
    	old_y = new double[m];
    	error = new double[m];
    	x = new double[n+1];
    	old_Input = new double[n];		// omit the bias neuron [n]
    	h = new double[num_hidden+1];
    	v = new double[n+1][num_hidden+1];
    	w = new double[num_hidden+1][m];
    	ev = new double[n+1][num_hidden+1][m];
    	ew = new double[num_hidden+1][m];
    	gradSumV = new double[n+1][num_hidden+1][m];
    	gradSumW = new double[num_hidden+1][m];
    	initWeights();

    	int flatD = (n+1)*(num_hidden+1)+(num_hidden+1)*m;
//    	RPROP.RPROPType mRPROP = RPROP.RPROPType.RPROPm;
//    	rp = new RPROP(flatD);
//    	rp.setRPROPType(mRPROP);
    	gradients = new double[flatD];
    	flatWeights = new double[flatD];
    }
    
/*   
    public TD_NNet() {
        initNet();
    }
 */
	public TD_NNet(int InputSize, int HiddenLayerSize, boolean withSig) {
	    n = InputSize;
	    num_hidden = HiddenLayerSize;
	    m = 1;			// currently always *one* output neuron
	    withSigmoid = withSig;
	    initNet();
	}
	
	/**
	 *  Accumulate the gradient of the loss function 0.5*error[k]*error[k]
	 */
	private void TDlearn() {
		int i,j,k;

		for (k=0;k<m;k++)
		{
			for (j=0;j<=num_hidden;j++)
			{
				// note that ew and ev contain the gradient of y w.r.t. the weights, see updateElig()
				gradSumW[j][k]+=error[k]*ew[j][k];
				for (i=0;i<=n;i++)
					gradSumV[i][j][k]+=error[k]*ev[i][j][k];
			}
			EtSum += 0.5*error[k]*error[k];			// needed only for rpropLrn==true
		}
	}/* end TDlearn */
	
	private void TDchangeWeights() {
		int i,j,k;

		for (k=0;k<m;k++)
		{
			for (j=0;j<=num_hidden;j++)
			{
				// gradSum contains the sum over all epoch iterations of 
				// - gradient of loss function 0.5 error^2 
				w[j][k]+=BETA*gradSumW[j][k];
				for (i=0;i<=n;i++) {
					v[i][j]+=ALPHA*gradSumV[i][j][k];
					gradSumV[i][j][k]=0;		// reset gradient sum (for next epoch)
				}
				gradSumW[j][k]=0;				// reset gradient sum (for next epoch)
			}
		}
	}/* end TDchangeWeights */
	
    public void resetElig() {
		int i,j,k;
		for (k=0;k<m;k++)
			for (j=0;j<=num_hidden;j++)
				for (i=0;i<=n;i++)
					ev[i][j][k] = 0.0;
	}
    
    public void calcScoresAndElig(double[] Input) {
    	getScore(Input);	// writes score on y[k]
        updateElig(Input);	// copy to old_y[k] & update eligibilities
    }

    /**
     * Update weights based on current state {reward, Input} and based on the output 
     * {@code old_y} of the previous step.<br>
     * Side effect: In the end call {@link calcScoresAndElig} to prepare {@code old_y} for the 
     * next pass and to update the eligibility traces.
     * @param reward	reward of the current board position
     * @param Input		feature vector derived from Table, the current board position
     * @param finished	is the current board position an end state?
     * @return 			the vector of error signals on all output units
     */
    public double[] updateWeights(double reward, double[] Input, boolean finished, boolean wghtChange) {
        boolean DEBG = false;
        
        getScore(Input);	// forward pass - compute activities on y[k]
    	double target = (finished ? reward : GAMMA*y[0]);
    	for (int k=0;k<m;k++) 
           error[k] = target - old_y[k];
        this.TDlearn();			// backward pass - learning: accumulate weight changes
        if (wghtChange) this.TDchangeWeights();		// apply weight changes
        if (DEBG & finished) {
            getScore(old_Input);	
        	double new_error = target - y[0];
        	System.out.println("error:"+error[0]);
        	System.out.println("new_e:"+new_error);
        	for (int i=0;i<old_Input.length;++i) System.out.print((int)((3+old_Input[i])%3));
        	System.out.print(" ");
        	System.out.println("err decrease: "+100*(1-new_error/error[0])+"% "+error[0]+"  "
        						+target+"   "+reward + (finished ? "*" : ""));
        }

        calcScoresAndElig(Input);
    	// forward pass must be done twice to form TD errors: calculate scores with new weights,
    	// copy current scores to old_y[k] & update eligibilities
    	// (for use in next cycle's TD errors) 
        return error;
    }

    /**
     * Copy current output to {@code old_y} and update eligibilities (gradient) {@code ev, ew}
     * for next pass through loop. In the case {@code LAMBDA=0} the eligibility
     * {@code ew[j][k]} (hidden-to-output) is just the gradient of the output function y[k] w.r.t. 
     * to the weight w[j][k]. This amounts to <p>
     *  {@code ew[j][k] = h[j]} in the case w/o sigmoid  and <p>
     *  {@code ew[j][k] = y[k]*(1-y[k])*h[j]} in the case with Fermi sigmoid fct.
     *  
     *  @see #getScore(double[]) getScore(double), which defines the Fermi sigmoid fct.
     */
    public void updateElig(double[] Input) {
    	int i,j,k;
    	double temp[] = new double[m];

		for (i=0;i<n;i++) 				
			old_Input[i] = Input[i];	// only for DEBG
		
    	for (k=0;k<m;k++)
    		old_y[k] = y[k];

    	if (withSigmoid) {
    		if (FERMI_FCT) {
    			// this is the derivative for Fermi fct sigmoid:
        		for (k=0;k<m;k++)	temp[k]=y[k]*(1-y[k]);    			
    		} else {
    			// this is the derivative for tanh sigmoid:
    			for (k=0;k<m;k++)	temp[k]=(1-y[k]*y[k]);
    		}
    	} else {
    		for (k=0;k<m;k++)	temp[k]=1.0;
    	}

    	for (j=0;j<=num_hidden;j++)
    	{
    		for (k=0;k<m;k++)
    		{
    			ew[j][k]=LAMBDA*GAMMA*ew[j][k]+temp[k]*h[j];  // /WK/02/2015 added GAMMA (!)
    			for (i=0;i<=n;i++)
    			{
    				ev[i][j][k]=LAMBDA*GAMMA*ev[i][j][k]+temp[k]*w[j][k]*h[j]*(1-h[j])*x[i];
    				// /WK/02/2015 added GAMMA (!) 
    			}
    		}
    	}
    }/* end UpdateElig */

	/**
	 *  Adjust learn parameters ALPHA and BETA. <p>
	 *  
	 *  Known caller: {@link TDAgent#trainAgent}
	 */
    public void finishUpdateWeights() {
        ALPHA = ALPHA * m_AlphaChangeRatio;
        BETA = BETA * m_AlphaChangeRatio;
    }
    public double getAlphaChangeRatio() {
        return m_AlphaChangeRatio;
    }
    public int getHiddenLayerSize() {
        return num_hidden;
    }
    public double getLambda() {
        return LAMBDA;
    }
    public double getAlpha() {
        return ALPHA;
    }
    public double getBeta() {
        return BETA;
    }
    /**calculate net output for given Input (fct Response in [SuttonBonde93])
     * <p> net output = V(s_t): the higher, the more likely is s_t a win position for player +1 (white)
     * @param Input		feature vector derived from Table, the current board position
     * @return			the output of the output neuron, y[0]
     * 					(which is also changed as class variable)
     */
    public double getScore(double[] Input) {
    	int i,j,k;
    	
        // initialize input layer & bias neurons:	
    	if (Input.length != n) throw new RuntimeException("Wrong length");
    	System.arraycopy(Input, 0, x, 0, Input.length);
    	x[n] = BIAS;
    	h[num_hidden]=BIAS;

    	for (j=0;j<num_hidden;j++)
    	{
    		h[j]=0.0;
    		for (i=0;i<=n;i++)
    		{
    			h[j]+=x[i]*v[i][j];
    		}
    		h[j]=1.0/(1.0+Math.exp(-h[j])); /* asymmetric sigmoid */
    	}
    	for (k=0;k<m;k++)
    	{
    		y[k]=0.0;
    		for (j=0;j<=num_hidden;j++)
    		{
    			y[k]+=h[j]*w[j][k];
    		}
    		if (withSigmoid)
    			if (FERMI_FCT) {
        			y[k]=1.0/(1.0+Math.exp(-y[k])); /* asymmetric sigmoid (Fermi fct) \in [0,1] */
//        			y[k]=2.0*y[k]-1.0;				
        			/* uncomment the line above to map to symmetric sigmoid \in [-1,1]. This
        			 * needs a factor 2 in the withSigmoid-branch of updateElig() */     				
    			} else {
    				y[k] = Math.tanh(y[k]);
    			}
    	}
    	return(y[0]);
    }/* end getScore */

    // initialize weights & bias units
    private void initWeights() {
    	int j,k,i;

    	x[n]=BIAS;
    	h[num_hidden]=BIAS;
    	for (j=0;j<=num_hidden;j++)
    	{
    		for (k=0;k<m;k++)
    		{
    			w[j][k]= EPS*(rand.nextDouble() * 2 - 1);
    		    ew[j][k]=0.0;
    			old_y[k]=0.0;
    		}
    		for (i=0;i<=n;i++)
    		{
    			v[i][j]= EPS*(rand.nextDouble() * 2 - 1);
    		    for (k=0;k<m;k++)
    		    	ev[i][j][k]=0.0;
    		}
    	}

    } /* end initWeights */
    
   /**
     *  Set the weights and bias from one long double vector wv. Given H=(n+1)*num_hidden:<ul>
     *  <li> wv[0..n] = weights from inputs (incl. bias neuron) to 1st hidden
     *  <li> ...
     *  <li> wv[H-(n+1)..H-1] = weights from inputs (incl. bias neuron) to (num_hidden)th hidden
     *  <li> wv[H..H+num_hidden] = weights from hidden (incl. bias) to 1st output
     *  <li> ...
     *  <li> wv[wv.length-1] = activity of bias neurons
     *  </ul>
     */ 
    public void setWeights(double[] wv) {
    	int i,j,k,kv;
    	int sz = (n+1)*num_hidden+(num_hidden+1)*m+1;

    	assert wv.length>=sz : "length of wv = "+wv.length+" is smaller than size of net: "+sz;
    	
    	x[n]=wv[sz-1];
    	h[num_hidden]=wv[sz-1];
    	for (j=0,kv=0;j<num_hidden;j++)
    	{
    		for (i=0;i<=n;i++,kv++)
    		{
    			v[i][j]= wv[kv];
    		    for (k=0;k<m;k++) ev[i][j][k]=0.0;
    		}
    	}
		for (k=0;k<m;k++)
		{
			for (j=0;j<=num_hidden;j++,kv++)
    		{
    			w[j][k]= wv[kv];
    		    ew[j][k]=0.0;
    			old_y[k]=0.0;
    		}
    	}
    
    	assert kv==sz-1 : "count error kv";

    } /* end setWeights */
    
    /**
     *  Get the weights (inc. bias weight) into one long double vector wv of size (n+1)*m+1. <ul>
     *  <li> wv[0..n] = weights from inputs (incl. bias neuron) to 1st hidden
     *  <li> ...
     *  <li> wv[H-(n+1)..H-1] = weights from inputs (incl. bias neuron) to (num_hidden)th hidden
     *  <li> wv[H..H+num_hidden] = weights from hidden (incl. bias) to 1st output
     *  <li> ...
     *  <li> wv[wv.length-1] = activity of bias neuron
     *  </ul>
     */ 
    public double[] getWeights() {
    	int i,j,k,kv;
    	int sz = (n+1)*num_hidden+(num_hidden+1)*m+1;
    	double[] wv = new double[sz];

    	
    	wv[sz-1] = x[n];
    	for (j=0,kv=0;j<num_hidden;j++)
    	{
    		for (i=0;i<=n;i++,kv++)
    		{
    			wv[kv]=v[i][j];
    		}
    	}
    	for (k=0;k<m;k++)
    	{
			for (j=0;j<=num_hidden;j++,kv++)
    		{
				wv[kv]=w[j][k];
    		}
    	}

    	assert kv==sz-1 : "count error kv";

    	return wv;
    } /* end getWeights */

    /** 
     *  Get number of free parameters (weights + bias).
     */
    public int getDimensions() {
    	return (n+1)*num_hidden+(num_hidden+1)*m+1;
    }
    
    private void prepareLambdaPower() {
        m_LambdaPower = new double[m_TimePeriod];
        int i;

        for (i = 0; i < m_TimePeriod; i++) {
            m_LambdaPower[i] = Math.pow(LAMBDA, i);
        }
    }
    
//    public static TD_NNet loadNet(String FileName) {
//        TD_NNet Net = null;
//
//        if (FileName != null) {
//            try {
//                ObjectInputStream in = new ObjectInputStream(new FileInputStream(FileName));
//                Net = new TD_NNet(1,1,false);
///*
//                Net.m_OutputLayer = in.readDouble();
//                Net.m_InputLayer = (double[]) in.readObject();
//                Net.m_HiddenLayerOutputs = (double[]) in.readObject();
//                Net.m_HiddenLayerWeights = (double[]) in.readObject();
//                Net.m_OutputLayerWeights = (double[]) in.readObject();
//                Net.m_InputNum = in.readInt();
//                Net.m_NeuronsNum = in.readInt();
//                Net.m_TimePeriod = in.readInt();
//                Net.ALPHA = in.readDouble();
//                Net.m_LambdaPower = (double[]) in.readObject();
//                Net.LAMBDA = in.readDouble();
//                Net.m_AlphaChangeRatio = in.readDouble();
// */                
//                in.close();
//            } catch (Exception E) {
//				Net = null;
//            }
//        }
//        return Net;
//    }

//    public void saveNet(String FileName) {
//        if (FileName != null) {
//            try {
//                ObjectOutputStream out =
//                    new ObjectOutputStream(new FileOutputStream(FileName, false));
///*                
//                out.writeDouble(m_OutputLayer);
//                out.writeObject(m_InputLayer);
//                out.writeObject(m_HiddenLayerOutputs);
//                out.writeObject(m_HiddenLayerWeights);
//                out.writeObject(m_OutputLayerWeights);
//                out.writeInt(m_InputNum);
//                out.writeInt(m_NeuronsNum);
//                out.writeInt(m_TimePeriod);
//                out.writeDouble(ALPHA);
//                out.writeObject(m_LambdaPower);
//                out.writeDouble(LAMBDA);
//                out.writeDouble(m_AlphaChangeRatio);
// */                
//                out.close();
//            } catch (Exception E) {
//
//            }
//        }
//    }

    public void setAlphaChangeRatio(double newAlphaChangeRatio) {
        m_AlphaChangeRatio = newAlphaChangeRatio;
    }

    public void setLambda(double newLambda) {
        LAMBDA = newLambda;
        prepareLambdaPower();
    }
    public void setAlpha(double newStartAlpha) {
        ALPHA = newStartAlpha;
    }
    public void setBeta(double newStartBeta) {
        BETA = newStartBeta;
    }
    public void setGamma(double newStartGamma) {
        GAMMA = newStartGamma;
    }
//    public void setEpochs(int epochs) {
//    	epochMax = epochs;
//    }
	public void setRpropLrn(boolean hasRpropLrn) {
		rpropLrn = hasRpropLrn;
	}
	public void setRpropInitDelta(double initDelta) {
		//rp.setInitDelta(initDelta);
	}
    
}
