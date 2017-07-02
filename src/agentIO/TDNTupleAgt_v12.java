package agentIO;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import controllers.AgentBase;
import controllers.PlayAgent.AgentState;
import controllers.TD.ntuple.NTupleValueFunc;
import controllers.TD.ntuple.TDNTupleAgt;
import games.StateObservation;
import params.NTParams;
import params.ParTD;
import params.TDParams;

/**
 * This class is only needed for the one-time transformation of TD-NTuple agents, 
 * see {@link agentIO.TransformTdAgents}. <p>
 *   
 * (We need just a class with a new name for proper serialization.) 
 */
public class TDNTupleAgt_v12 extends AgentBase implements Serializable {
	private Random rand; // generate random Numbers 
	
	private static final long  serialVersionUID = 12L;

	private double m_epsilon = 0.1;
	private double m_EpsilonChangeDelta = 0.001;
	private double MaxScore;
	private boolean TC; //true: using Temporal Coherence algorithm
	private int tcIn; 	//temporal coherence interval: after tcIn games tcFactor will be updates
	private boolean tcImm=true;		//true: immediate TC update, false: batch update (epochs)
	private boolean randomness=false; //true: ntuples are created randomly (walk or points)
	private boolean randWalk=true; 	//true: random walk is used to generate nTuples
									//false: random points is used to generate nTuples//samine//

	private NTupleValueFunc m_Net;

	protected boolean USESYMMETRY = true; 	// Use symmetries (rotation, mirror) in NTuple-System
	private boolean NORMALIZE = false; 
	private boolean RANDINITWEIGHTS = false;// Init Weights of Value-Function
											// randomly
	private boolean PRINTTABLES = false;	// /WK/ control the printout of tableA, tableN, epsilon
	
	//
	// from TDAgent
	//
	private int numFinishedGames = 0;
	private boolean randomSelect = false;
	
	/**
	 * Members {@link #m_tdPar} {@link #m_ntPar} are only needed for saving and loading
	 * the agent (to restore the agent with all its parameter settings)
	 */
	private TDParams m_tdPar;
	private NTParams m_ntPar;
	
	/**
	 * Default constructor for {@link TDNTupleAgt_v12}, needed for loading a serialized version
	 */
	public TDNTupleAgt_v12() throws IOException {
		super();
		TDParams tdPar = new TDParams();
		NTParams ntPar = new NTParams();
//		initNet(ntPar, tdPar, null, null, 1000);
	}

	/**
	 * Create a new {@link TDNTupleAgt_v12} just by copying all members from 
	 * {@link TDNTupleAgt} which is assumed to be in the old v12 version. (We need 
	 * just a class with a new name for proper serialization.) 
	 * 
	 * @param tdagt			agent of the old v12 version 
	 */
	public TDNTupleAgt_v12(TDNTupleAgt tdagt) {
		super(tdagt.getName());
		int maxGameNum = tdagt.getMaxGameNum();
		m_tdPar = new TDParams();
		m_tdPar.setFrom(tdagt.getTDParams());
		m_ntPar = new NTParams();
		m_ntPar.setFrom(tdagt.getNTParams());
		rand = new Random(42); //(System.currentTimeMillis());		
		tcIn=tdagt.getNTParams().getTcInterval();
		TC=tdagt.getNTParams().getTc();
		USESYMMETRY=tdagt.getNTParams().getUseSymmetry();
		NORMALIZE=tdagt.getTDParams().getNormalize();
		tcImm=tdagt.getNTParams().getTcImm();
		
		randomness=tdagt.getNTParams().getRandomness();
		randWalk=tdagt.getNTParams().getRandomWalk();
		int numTuple=tdagt.getNTParams().getNtupleNumber();
		int maxTupleLen=tdagt.getNTParams().getNtupleMax();
		int posVals = tdagt.getNTupleValueFunc().getXnf().getNumPositionValues();
		int numCells = tdagt.getNTupleValueFunc().getXnf().getNumCells();
		
		m_Net = tdagt.getNTupleValueFunc();
		
		setTDParams(tdagt.getTDParams(), maxGameNum);
		m_epsilon = tdagt.getTDParams().getEpsilon();
		m_EpsilonChangeDelta = (m_epsilon - tdagt.getTDParams().getEpsilonFinal()) / maxGameNum;
		
		this.setAgentState(tdagt.getAgentState());		
		this.setMaxGameNum(tdagt.getMaxGameNum());
		this.setEpochMax(tdagt.getEpochMax());
		this.setNumEval(tdagt.getNumEval());

	}

	public void setTDParams(TDParams tdPar, int maxGameNum) {
		m_Net.setLambda(tdPar.getLambda());
		m_Net.setGamma(tdPar.getGamma());

		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		//m_Net.setRpropInitDelta( tdPar.getAlpha() / inpSize[m_feature.getFeatmode()] );
	}

	public void setTDParams(ParTD tdPar, int maxGameNum) {
		m_Net.setLambda(tdPar.getLambda());
		m_Net.setGamma(tdPar.getGamma());

		double alpha = tdPar.getAlpha();
		double alphaFinal = tdPar.getAlphaFinal();
		double alphaChangeRatio = Math
				.pow(alphaFinal / alpha, 1.0 / maxGameNum);
		m_Net.setAlpha(tdPar.getAlpha());
		m_Net.setAlphaChangeRatio(alphaChangeRatio);

		m_Net.setRpropLrn(tdPar.hasRpropLrn());
		//m_Net.setRpropInitDelta( tdPar.getAlpha() / inpSize[m_feature.getFeatmode()] );
	}

	public TDParams getTDParams() {
		return m_tdPar;
	}
	public NTParams getNTParams() {
		return m_ntPar;
	}

	public double getEpsilon() {
		return m_epsilon;
	}
	
	public NTupleValueFunc getNTupleValueFunc() {
		return m_Net;
	}
	
	@Override
	public double getScore(StateObservation sob) {
		// TODO Auto-generated method stub
		return 0;
	}

}
