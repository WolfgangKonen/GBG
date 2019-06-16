package games.Othello;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;

import tools.Types;

public class ConfigOthello {

	/**
	 *  Board size
	 */
	public static final int BOARD_SIZE = 8;
	/**
	 * Tile
	 */
	public static Color BOARDCOLOR = Color.ORANGE;
	public static Color LASTMOVECOLOR = Color.RED;
	public static Color POSSIBLEMOVECOLOR = Color.GREEN;
	public static Color NORMALBORDERCOLOR = Color.YELLOW;
	public static Dimension DIMENSIONTILE = new Dimension(
			(int) Types.GUI_SCALING_FACTOR * 80,
			(int) Types.GUI_SCALING_FACTOR * 80
			);
	
	
	public static int BLACK = 0;
	public static int WHITE = 1;
	public static int EMPTY = 2;

	/**
	 * Values for BenchmarkPlayer
	 */
	public static int[][] BENCHMARKPLAYERMAPPING = {
		
			{
			 100, -25,  10,   5,   5,  10, -25, 100,				
			 -25, -25,   2,   2,   2,   2, -25, -25,
			  10,   2,   5,   1,   1,   5,   2,  10,
			   5,   2,   1,   2,   2,   1,   2,   5,
			   
		       5,   2,   1,   2,   2,   1,   2,   5,
		      10,   2,   5,   1,   1,   5,   2,  10,
			 -25, -25,   2,   2,   2,   2, -25, -25,
			 100, -25,  10,   5,   5,  10, -25, 100
			 }
			,
			{	
			 80, -26,  24,  -1, -5,  28, -18, 76,
			-23, -39, -18,  -9, -6,  -8, -39, -1,
			 46, -16,   4,   1, -3,   6, -20, 52,	
			-13,  -5,   2,  -1,  4,   3, -12, -2,
			 -5,  -6,   1,  -2, -3,   0,  -9, -5,
			 48, -13,  12,   5,  0,   5, -24, 41,
			-27, -53, -11,  -1,-11, -16, -58,-15,
			 87, -25,  27,  -1,  5,  36,  -3, 100
				
			}
	
	};

	public static int[] BENCHMIN = {-888,-1246};
	public static int[] BENCHMAX = {888,1246};
	
	/**
	 * Test cases for Benchmark
	 */
	public static int[][][] DEBUG = 
			{
					{
						{0 ,2, 1, 1, 1, 0, 0, 0 },
						{2, 0, 2, 1, 0, 0, 0, 0 },
						{1, 0, 0, 0 ,1 ,0 ,1, 0 },
						{2, 0, 2, 1, 0, 0, 1, 0 },
						{0, 0, 0, 0, 0, 0, 0, 0 },
						{0, 0 ,0 ,0 ,0 ,0 ,0, 0 },
						{0 ,0, 0, 0, 1, 0 ,0 ,0 },
						{0 ,0, 0, 0, 0 ,0, 0, 0}
					},
					{
						{2, 2, 2, 2, 2, 0, 0, 0 },
						{2, 2, 1, 2, 0, 0, 0, 0 },
						{0, 0, 1, 0, 1, 0, 0, 0 },
						{2, 2, 1, 1, 0, 0, 0, 0 },
						{0, 0, 0, 0, 1, 0, 0, 0 },
						{0, 2, 0, 0, 0, 0, 0, 0 },
						{2, 0, 0, 0, 0, 0, 0, 0 },
						{0, 0, 0, 0, 0, 0, 0, 0 },
					},
					{
						{1, 1, 1, 1, 1, 0, 1,1 },
						{1, 1, 2, 1, 1, 1, 1, 1 },
						{0, 0, 2, 0, 1, 0, 1, 0 },
						{1, 1, 2, 2, 1, 0, 0, 0 },
						{1, 0, 0, 0, 2, 0, 0, 0 },
						{0, 1, 0, 0, 0, 0, 0, 0 },
						{1, 0, 0, 0, 0, 0, 0, 0 },
						{0, 0, 0, 0, 0, 0, 0, 0 },
					}
			};
}
