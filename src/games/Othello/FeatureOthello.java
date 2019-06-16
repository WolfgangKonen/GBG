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

	int featmode, player, opponent;
	int countP = 0, countO = 0;
	

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

	/**
	 * For better readability the 64 places get sorted
	 * @param sob
	 * @return
	 */
	public double[] prepareInputVector1(StateObservation sob)
	{
		assert (sob instanceof StateObserverOthello);
		StateObserverOthello so = (StateObserverOthello) sob;
		
		countP = 0;
		countO = 0;
		this.opponent = so.getPlayer();
		this.player = so.getOpponent(opponent);
		double[] retVal = new double[18];
		int[][] cgs = so.getCurrentGameState();
		// first 4 moves try to play in middle box 4 x 4
		int discsPlaced = allDiscs(cgs,player, opponent);
			
		if(discsPlaced < 17) {
			// Own possible moves, try to hold em small at start;
			retVal[0] = -so.getAvailableActions().size()/discsPlaced;
			retVal[1] = inMainBlock(cgs,player);
			//Do not place on risky shit
			retVal[2] = dangerForSide(cgs, player);
			// shoot negative if not in mainblock
			retVal[3] = notInMainBlock(cgs, player);
		}else {

		
			// Take COrner;
		
		//Take Side depending on may opponent can take corner;
		
		
		//controlling whole side 
		retVal[4] = controllSideLate(cgs, player, 0);
		retVal[5] = controllSideLate(cgs, player, 1);
		retVal[6] = controllSideLate(cgs, player, 2);
		retVal[7] = controllSideLate(cgs, player, 3);
		

		
		
		}
		retVal[10] = cornerCheckLate(cgs, player, opponent, 0);
		retVal[11] = cornerCheckLate(cgs, player, opponent, 1);
		retVal[12] = cornerCheckLate(cgs, player, opponent, 2);
		retVal[13] = cornerCheckLate(cgs, player, opponent, 3);
		retVal[14] = dangerForSide(cgs, player);
		retVal[15] = isDanger(cgs, player);
		retVal[16] = captureCorner(cgs, player);
		retVal[17] = captureSide(cgs, player);
		retVal[8] = countP / discsPlaced;
		retVal[9] = -countO / discsPlaced;
		return retVal;
	}
	
	
	public double cornerCheckLate(int[][] cgs, int player,int opponent, int corner) {
		switch(corner) {
		case 0:
			if(cgs[0][0] == ConfigOthello.EMPTY) {
				if(cgs[0][1] == player) {
					for(int i = 2; i < 8; i++) {
						if(cgs[0][i] == opponent) {
							return -1.0;
						}
						if(cgs[0][i] == ConfigOthello.EMPTY) break;
					}	
				}
				if(cgs[1][0] == player) {
					for(int i = 2; i < 8; i++) {
						if(cgs[i][0] == opponent) {
							return -1.0;
						}
						if(cgs[i][0] == ConfigOthello.EMPTY) break;
					}
				}
				if(cgs[1][1] == player) {
					int i = 2, j = 2;
					while(i < 8) {
						if(cgs[i][j] == opponent) {
							return -1.0;
						}
						if(cgs[i][j] == ConfigOthello.EMPTY) break;
						i++;
						j++;
					}
				}
				return 1;
			}
			case 1:
				if(cgs[0][7] == ConfigOthello.EMPTY) {
					if(cgs[0][6] == player) {
						for(int i = 5; i > -1; i--) {
							if(cgs[0][i] == opponent) {
								return -1.0;
							}
							if(cgs[0][i] == ConfigOthello.EMPTY) break;
						}	
					}
					if(cgs[1][7] == player) {
						for(int i = 2; i < 8; i++) {
							if(cgs[i][7] == opponent) {
								return -1.0;
							}
							if(cgs[i][7] == ConfigOthello.EMPTY) break;
						}
					}
					if(cgs[1][6] == player) {
						int i = 2, j = 5;
						while(i < 8) {
							if(cgs[i][j] == opponent) {
								return -1.0;
							}
							if(cgs[i][j] == ConfigOthello.EMPTY) break;
							i++;
							j--;
						}
					}
					return 1;	
				}
				
			case 2:
				if(cgs[7][0] == ConfigOthello.EMPTY) {
					if(cgs[6][0] == player) {
						for(int i = 5; i > -1; i--) {
							if(cgs[i][0] == opponent) {
								return -1.0;
							}
							if(cgs[i][0] == ConfigOthello.EMPTY) break;
						}	
					}
					if(cgs[7][1] == player) {
						for(int i = 2; i < 8; i++) {
							if(cgs[7][i] == opponent) {
								return -1.0;
							}
							if(cgs[i][7] == ConfigOthello.EMPTY) break;
						}
					}
					if(cgs[6][1] == player) {
						int i = 5, j = 2;
						while(i > -0) {
							if(cgs[i][j] == opponent) {
								return -1.0;
							}
							if(cgs[i][j] == ConfigOthello.EMPTY) break;
							i--;
							j++;
						}
					}
					return 1;	
				}
			case 3:
				if(cgs[7][7] == ConfigOthello.EMPTY) {
					if(cgs[6][7] == player) {
						for(int i = 5; i > -1; i--) {
							if(cgs[i][7] == opponent) {
								return -1.0;
							}
							if(cgs[i][7] == ConfigOthello.EMPTY) {
								break;
							}
							
						}	
					}
					if(cgs[7][6] == player) {
						for(int i = 5; i > -1; i--) {
							if(cgs[7][i] == opponent) {
								return -1.0;
							}
							if(cgs[i][7] == ConfigOthello.EMPTY) {
								break;
							}
						}
					}
					if(cgs[6][6] == player) {
						int i = 5, j = 5;
						while(i > -0) {
							if(cgs[i][j] == opponent) {
								return -1.0;
							}
							if(cgs[i][j] == ConfigOthello.EMPTY) break;
							i--;
							j--;
						}
					}
					return 1;	
				}
		}
		
		
		return 0.0;
	}
	
	/**
	 * 
	 * @param cgs
	 * @param player
	 * @param side 0 = top; 1= right bot = 2 left = 3
	 * @return
	 */
	public double controllSideLate(int[][] cgs, int player,int side) {
		double retVal = 0.0;
		
		
		for(int i = 0; i < 8; i++) {
			switch(side) {
			case 0:
				if(cgs[0][i] == player) retVal++;
				continue;
			case 1:
				if(cgs[i][7] == player) retVal++;
				continue;
			case 2:
				if(cgs[7][i] == player) retVal++;
				continue;
			case 3:
				if(cgs[i][0] == player) retVal++;
				continue;
			}
		}
		
		return retVal/8;
	}
	
	

	
	
	public int allDiscs(int[][] cgs, int player, int opponent) {
		int retVal = 0;
		for(int i = 0; i < cgs.length; i++) {
			for(int j = 0; j < cgs[i].length;j++) {
				if(cgs[i][j] != ConfigOthello.EMPTY) retVal++;
				if(cgs[i][j] == player) countP++;
				if(cgs[i][j] == opponent) countO++;
			}
		}
		return retVal;
	}
	
	//Danger in medium state
		public double dangerForSide(int[][] cgs, int player) {
			double retVal = 0.0;
			int i,j, jj;
			j = 1;
			jj = 6;
			for(i = 2; i < 6; i++) {
				if(cgs[j][i] == player) {	
					retVal--;
				}
				if(cgs[jj][i] == player) {	
					retVal--;
				}
			}
			j = 1;
			jj = 6;
			for( i = 2; i < 6; i++) {
				if(cgs[i][j] == player) {	
					retVal--;
				}
				if(cgs[i][jj] == player) {	
					retVal--;
				}
			}
		return retVal/24;
		}
	
	public double captureCorner(int[][] cgs, int player) {
		double retVal = 0.0;
		if(cgs[0][0] == player) retVal++;
		if(cgs[7][0] == player) retVal++;
		if(cgs[0][7] == player) retVal++;
		if(cgs[7][7] == player) retVal++;
		return retVal/4;
	}
	

	
	public double captureSide(int[][] cgs, int player) {
		double retVal = 0.0;
		for(int i = 1; i < 8; i++) {
			if(cgs[i][0] == player) retVal++;
			if(cgs[i][7] == player) retVal++;
			if(cgs[0][i] == player) retVal++;
			if(cgs[7][i] == player) retVal++;
		}
		return retVal/24;
	}
	
	//Helper
	public boolean isSide(int i, int j) {
		return (i == 0 || i == 7 || j == 0 || j == 7);
	}
	
	//medium
	public double isDanger(int[][] cgs, int player) {
		double retVal = 0.0;
	
	
		if(cgs[0][1] == player) retVal--;
		if(cgs[1][1] == player) retVal--;
		if(cgs[1][0] == player) retVal--;
	
		if(cgs[0][6] == player) retVal--;
		if(cgs[1][6] == player) retVal--;
		if(cgs[1][7] == player) retVal--;
		
	
		if(cgs[6][0] == player) retVal--;
		if(cgs[6][1] == player) retVal--;
		if (cgs[7][1] == player) retVal--;
			
		
		if(cgs[6][7] == player) retVal--;
		if(cgs[6][6] == player) retVal--;
		if(cgs[7][6] == player) retVal--;
		
		return retVal/12;
	}
	
	
	// positive if in main
	public double inMainBlock(int[][] cgs, int player) {
		double retVal = 0;
		for(int i = 2; i < 6; i++) {
			for(int j = 2; j < 6; j++) {
				if(cgs[i][j] == player) retVal++;
			}
		}
		return retVal / 16;
	}
	
	public double notInMainBlock(int[][] cgs, int player) {
		double retVal = 0;
		int x, y;
		
		x = 1;
		y = 6;
		for(int i = 1; i < 7;i++) {
			if(cgs[x][i] == player) retVal--;
			if(cgs[y][i] == player) retVal--;
		}
		for(int i = 2; i < 6; i++) {
			if(cgs[i][y] == player) retVal--;
			if(cgs[i][x] == player) retVal--;
		}
		return retVal/20;
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
		int[] inputSize = {18,5,64};
		return inputSize[featmode];
	}






}