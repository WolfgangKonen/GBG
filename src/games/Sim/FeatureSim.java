package games.Sim;

import java.io.Serializable;
import java.text.DecimalFormat;

import games.Feature;
import games.StateObservation;
import tools.Types;

public class FeatureSim implements Feature{

	
	int featMode = 0;
	
	public FeatureSim(int featmode) {
		this.featMode = featmode;
	}
	
	public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		throw new RuntimeException("FeatureSim does not implement getNextAction2");
	}
	
	private String inputToString(double[] input)
	{
		String str="";
		DecimalFormat frmI = new DecimalFormat("+0;-0");
		for (int i=0;i<input.length;i++)
		{
				str = str + frmI.format(input[i]);
		}
		return  str; 
	}
	
	@Override
	public String stringRepr(double[] featVec) {
		return inputToString(featVec);
	}

	@Override
	public int getFeatmode() {
		return featMode;
	}

	@Override
	public int[] getAvailFeatmode() {
		return new int[]{0};
	}

	@Override
	public int getInputSize(int featmode) {
		// inpSize[i] has to match the length of the vector which
    	// TicTDBase.prepareInputVector() returns for featmode==i:
		switch (featMode)
		{
    	case 0:
    		if(ConfigSim.NUM_PLAYERS > 2)
    			return getEdgesCount() * ConfigSim.NUM_PLAYERS + 3;
    		else
    			return getEdgesCount();
    	default:
    		throw new RuntimeException("Unknown featmode: " + featMode);
		}
	}

	private int getEdgesCount()
	{
		int count = (ConfigSim.NUM_NODES * (ConfigSim.NUM_NODES - 1)) / 2;
		return count;
	}
	@Override
	public double[] prepareFeatVector(StateObservation so) {
		StateObserverSim som = (StateObserverSim) so;

    	switch (featMode){
        	case 0:
        		return feature0Vector(som);
        	default:
        		throw new RuntimeException("Unknown featmode: " + featMode);
    	}
	}
	
	private double [] feature0Vector(StateObserverSim som)
	{
		if(ConfigSim.NUM_PLAYERS > 2)
			return intToDoubleArray3Player(som.getNodes(), som.getPlayer());
		else
			return intToDoubleArray2Player(som.getNodes());
	}
	
	private double[] intToDoubleArray3Player(Node [] nodes, int player)
	{
		double input[] = new double[getInputSize(featMode)];
		int k = 0;
		int pl = 0;
		
		input[player] = 1.0;
		input[(player+1)%3] = 0.0;
		input[(player+2)%3] = 0.0;
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				pl = nodes[i].getLinkPlayerPos(j);
				switch(pl)
				{
				case 0:
					input[k] = 0.0;
					input[k + 1] = 0.0;
					input[k + 2] = 0.0;
					k+=3;
					break;
				case 1:
					input[k] = 1.0;
					input[k + 1] = 0.0;
					input[k + 2] = 0.0;
					k+=3;
					break;
				case 2:
					input[k] = 0.0;
					input[k + 1] = 1.0;
					input[k + 2] = 0.0;
					k+=3;
					break;
				case 3:
					input[k] = 0.0;
					input[k + 1] = 0.0;
					input[k + 2] = 1.0;
					k+=3;
					break;
				}
			}
		}
		
		return input; 
	}
	
	
	private double[] intToDoubleArray2Player(Node [] nodes)
	{
		double input[] = new double[getInputSize(featMode)];
		int k = 0,pl = 0;
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				pl = nodes[i].getLinkPlayerPos(j);
				switch(pl)
				{
				case 0:
					input[k] = 0.0;
					k++;
					break;
				case 1:
					input[k] = 1.0;
					k++;
					break;
				case 2:
					input[k] = -1.0;
					k++;
					break;
				}
			}
		}
		
		return input; 
	}
}
