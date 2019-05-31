package games.Othello;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import games.Feature;
import games.StateObservation;
import tools.Types;
import tools.Types.ACTIONS;
import tools.Types.ACTIONS_VT;

public class FeatureOthello implements Feature, Serializable{

	int featmode;


	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long serialVersionUID = 12L;

	public FeatureOthello(int featmode)
	{
		this.featmode = featmode;
	}

	@Override
	public double[] prepareFeatVector(StateObservation so) {
		switch(featmode) {
		case 0: return prepareInputVector1(so);
		case 1: return prepareInputVector2(so);
		case 2:	return prepareInputVector3(so);
		default: throw new RuntimeException ("Sorry, the selected featmode is not available");
		}
	}

	public double[] prepareInputVector1(StateObservation sob)
	{
		assert (sob instanceof StateObserverOthello);
		StateObserverOthello so = (StateObserverOthello) sob;
		double[] retVal = new double[11];
		retVal[0] = controllStartingBlock(so.getCurrentGameState(), so.getPlayer());
		retVal[1] = controllVerticallyLine(so.getCurrentGameState(), so.getPlayer());
		retVal[2] = controllHorizontally(so.getCurrentGameState(), so.getPlayer());
		retVal[3] = controllCornerBlockThreeXThree(so.getCurrentGameState(), so.getPlayer());
		retVal[4] = controllStartingBlock(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[5] = controllVerticallyLine(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[6] = controllHorizontally(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[7] = controllCornerBlockThreeXThree(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[8] = Double.valueOf(so.getAvailableActions().size());
		retVal[9] = controllCornerBlockTwoXFive(so.getCurrentGameState(), so.getPlayer());
		retVal[10] = controllCornerBlockTwoXFive(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		return retVal;
	}
	/**
	 * Takes only the information of own color
	 * @param sob
	 * @return
	 */
	public double[] prepareInputVector2(StateObservation sob) {
		assert (sob instanceof StateObserverOthello);
		StateObserverOthello so = (StateObserverOthello) sob;
		double[] retVal = new double[5];
		retVal[0] = controllStartingBlock(so.getCurrentGameState(), so.getPlayer()) - controllStartingBlock(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[1] = controllVerticallyLine(so.getCurrentGameState(), so.getPlayer()) - controllVerticallyLine(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[2] = controllHorizontally(so.getCurrentGameState(), so.getPlayer()) - controllHorizontally(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[3] = controllCornerBlockThreeXThree(so.getCurrentGameState(), so.getPlayer()) - controllCornerBlockThreeXThree(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		retVal[4] = controllCornerBlockTwoXFive(so.getCurrentGameState(), so.getPlayer()) - controllCornerBlockTwoXFive(so.getCurrentGameState(), BaseOthello.getOpponent(so.getPlayer()));
		return retVal;
	}

	public double countDiscs(int[][] table, int player) {
		double retVal= 0;
		for(int i = 0; i < 8; i ++) {
			for(int j = 0; j < 8; j++) {
				if(table[i][j] == player) retVal++;
			}
		}
		return retVal;
	}

	/**
	 * Helper methods for features.
	 * Due to testing it is much simpler treating each
	 * vector like a part of a construction kit
	 */
	public double controllStartingBlock(int[][] table, int player) {
		double retVal = 0;
		if(table[3][3] == player)retVal++;
		if(table[3][4] == player)retVal++;
		if(table[4][3] == player)retVal++;
		if(table[4][4] == player)retVal++;
		return retVal;
	}


	public double controllHorizontally(int[][] table, int player) {
		double retVal = 0;
		for(int i = 0; i < 8; i++) {
			if(table[0][i] == player) {
				int countHori = 0;
				for( int j = 1; j < 8; j ++) {
					if(table[j][i] == table[j-1][i]) countHori++;
				}
				if(countHori == 7) retVal++;
			}
		}
		return retVal * 2;
	}


	public double controllVerticallyLine(int[][] table, int player) {
		double retVal = 0;
		for(int i = 0; i < 8; i++) {
			int countVerti = 0;
			if(table[i][0] == player) 
			{
				for(int j = 1; j < 8; j++) {
					if(table[i][j] == table[i][j-1]) countVerti++;
				}
				if(countVerti == 7) retVal++;
			}
		}
		return retVal;
	}


	/**
	 *  Player occupied the 3x3 corner blocks
	 * @param table
	 * @param player
	 * @return
	 */
	public double controllCornerBlockThreeXThree(int[][] table, int player) {
		double retVal = 0;
		if(table[0][0] == player)
		{
			if((table[0][0] == table[0][1] && table[0][1] == table[0][2])
					&& (table[0][0] == table[1][0] && table[1][0] == table[2][0])
					&& (table[1][0] == table[1][1] && table[1][1] == table[1][2])
					&& (table[2][0] == table[2][1] && table[2][1] == table[2][2]))
			{
				retVal++;
			}
		}

		if(table[0][7] == player) 
		{
			if((table[0][7] == table[0][6] && table[0][6] == table[0][5])
					&& (table[0][7] == table[1][7] && table[1][7] == table[2][7])
					&& (table[1][7] == table[1][6] && table[1][6] == table[1][5])
					&& (table[2][7] == table[2][6] && table[2][6] == table[2][5]))
			{
				retVal++;
			}
		}

		if(table[7][0] == player)
		{
			if((table[7][0] == table[7][1] && table[7][1] == table[7][2])
					&& (table[7][0] == table[6][0] && table[6][0] == table[5][0])
					&& (table[6][0] == table[6][1] && table[6][1] == table[6][2])
					&& (table[5][0] == table[5][1] && table[5][1] == table[5][2]))
			{
				retVal++;
			}
		}

		if(table[7][7] == player) 
		{
			if((table[7][7] == table[7][6] && table[7][6] == table[7][5])
					&& (table[7][7] == table[6][7] && table[6][7] == table[5][7])
					&& (table[6][7] == table[6][6] && table[6][6] == table[6][5])
					&& (table[5][7] == table[5][6] && table[5][6] == table[5][5]))
			{
				retVal++;
			}
		}
		return retVal;
	}

	/**
	 * Player occupied 2x5 blocks on corner positions
	 * @param table
	 * @param player
	 * @return
	 */
	public double controllCornerBlockTwoXFive(int[][] table, int player) {
		double retVal= 0;
		if(table[0][0] == player) {
			if((table[0][0] == table[0][1] && table[0][1] == table[0][2] 
					&& table[0][2] == table[0][3] && table[0][3] == table[0][4])
					&& (table[0][0] == table[1][0])
					&& (table[1][0] == table[1][1] && table[1][1] == table[1][2] 
							&& table[1][2] == table[1][3] && table[1][3] == table[1][4]))
			{
				retVal++;
			}
			if((table[0][0] == table[1][0] && table[1][0] == table[2][0] 
					&& table[2][0] == table[3][0] && table[3][0] == table[4][0])
					&& (table[0][0] == table[0][1])
					&& (table[0][1] == table[1][1] && table[1][1] == table[2][1] 
							&& table[2][1] == table[3][1] && table[3][1] == table[4][1]))
			{
				retVal++;
			}
		}
		if(table[0][7] == player) {
			if((table[0][7] == table[0][6] && table[0][6] == table[0][5] 
					&& table[0][5] == table[0][4] && table[0][4] == table[0][3])
					&& (table[0][7] == table[1][7])
					&& (table[1][7] == table[1][6] && table[1][6] == table[1][5] 
							&& table[1][5] == table[1][4] && table[1][4] == table[1][3]))
			{
				retVal++;
			}
			if((table[0][7] == table[1][7] && table[1][7] == table[2][7] 
					&& table[2][7] == table[3][7] && table[3][7] == table[4][7])
					&& (table[0][7] == table[0][6])
					&& (table[0][6] == table[1][6] && table[1][6] == table[2][6] 
							&& table[2][6] == table[3][6] && table[3][6] == table[4][6]))
			{
				retVal++;
			}
		}

		if(table[7][0] == player) {
			if((table[7][0] == table[6][0] && table[6][0] == table[5][0] 
					&& table[5][0] == table[4][0] && table[4][0] == table[3][0])
					&& (table[7][0] == table[7][1])
					&& (table[7][0] == table[6][1] && table[6][1] == table[5][1] 
							&& table[5][1] == table[4][1] && table[4][1] == table[3][1]))
			{
				retVal++;
			}
			if((table[7][0] == table[7][1] && table[7][1] == table[7][2] 
					&& table[7][3] == table[7][3] && table[7][3] == table[7][4])
					&& (table[7][0] == table[6][0])
					&& (table[6][0] == table[6][1] && table[6][1] == table[6][2] 
							&& table[6][2] == table[6][3] && table[6][3] == table[6][4]))
			{
				retVal++;
			}
		}


		if(table[7][7] == player) {
			if((table[7][7] == table[6][7] && table[6][7] == table[5][7] 
					&& table[5][7] == table[4][7] && table[4][7] == table[3][7])
					&& (table[7][7] == table[7][6])
					&& (table[7][6] == table[6][6] && table[6][6] == table[5][6] 
							&& table[5][6] == table[4][6] && table[4][6] == table[3][6]))
			{
				retVal++;
			}
			if((table[7][7] == table[7][6] && table[7][6] == table[7][5] 
					&& table[7][5] == table[7][4] && table[7][4] == table[7][3])
					&& (table[7][7] == table[6][7])
					&& (table[6][7] == table[6][6] && table[6][6] == table[6][5] 
							&& table[6][5] == table[6][4] && table[6][4] == table[6][3]))
			{
				retVal++;
			}
		}
		return retVal;

	}

	public double controllFixedDiscs(StateObserverOthello so){
		double retVal = 0;
		return retVal;
	}

	/**
	 *	RAW-feature 
	 **/
	public double[] prepareInputVector3(StateObservation sob) {
		assert (sob instanceof StateObserverOthello);
		StateObserverOthello so = (StateObserverOthello) sob;
		double[] retVal = new double[64];
		for(int i=0, z=0; i < ConfigOthello.BOARD_SIZE; i++) {
			for(int j=0; j < ConfigOthello.BOARD_SIZE; j++, z++) {
				retVal[z] = so.getCurrentGameState()[i][j];
			}
		}
		return retVal;
	}


	@Override
	public String stringRepr(double[] featVec) {
		StringBuilder sb = new StringBuilder();
		for(double d : featVec) {
			sb.append(d);
		}
		return sb.toString();
	}

	@Override
	public int getFeatmode() {
		return featmode;
	}

	@Override
	public int[] getAvailFeatmode() {
		return new int[] {0,1,2};
	}

	@Override
	public int getInputSize(int featmode) {
		int[] inputSize = {11,5,64};
		return inputSize[featmode];
	}






}