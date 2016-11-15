package games.TicTacToe;

import tools.Types;

import java.io.Serializable;

import controllers.PlayAgent;
import controllers.TD.TDAgent;
import games.Feature;
import params.TDParams;


/**
 * The TD-Learning {@link PlayAgent} for TicTacToe. It borrows all functionality
 * from the general class {@link TDAgent}. It only overrides the abstract
 * method {@link TDAgent#makeFeatureClass(int)} such that this factory method
 * returns an object of class {@link FeatureTTT}.
 * 
 * @see PlayAgent
 * @see TDAgent
 * @see FeatureTTT
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 */
public class TDPlayerTTT extends TDAgent implements PlayAgent,Serializable {
	
	/**
	 * Default constructor for TDPlayerTTT, needed for loading a serialized version
	 */
	public TDPlayerTTT() {
		super();
	}

	/**
	 * Construct new {@link TDPlayerTTT}, setting everything from tdPar and set default
	 * maxGameNum=1000
	 * 
	 * @param tdPar
	 */
	public TDPlayerTTT(String name, TDParams tdPar) {
		super(name, tdPar);
	}

	/**
	 * Construct new {@link TDPlayerTTT}, setting everything from tdPar and from maxGameNum
	 * 
	 * @param tdPar
	 * @param maxGameNum
	 */
	public TDPlayerTTT(String name, TDParams tdPar, int maxGameNum) {
		super(name, tdPar, maxGameNum);
	}

	public Feature makeFeatureClass(int featmode) {
		return new FeatureTTT(featmode);
	}

}