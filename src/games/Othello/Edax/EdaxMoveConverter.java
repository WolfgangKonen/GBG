package games.Othello.Edax;

import games.Othello.ConfigOthello;
import tools.Types.ACTIONS_VT;

public class EdaxMoveConverter 
{
	public static String ConverteIntToEdaxMove(int move)
	{
		int j = move % ConfigOthello.BOARD_SIZE;
		int i = (move - j) / ConfigOthello.BOARD_SIZE;
		return "" + intToLetterConverter(j) + "" + (i+1);
	}
	
	private static char intToLetterConverter(int integer)
	{	
		return (char)(65 + integer);
	}
	
	private static int letterToIntConverter(char character)
	{
		return (int)(character) - 65;
	}
	
	public static int converteEdaxToInt(String move)
	{
		move = move.trim();
		System.out.println(move);
		int j = letterToIntConverter(move.charAt(0));
		int i = Integer.parseInt("" + move.charAt(1));
		return (i-1) * ConfigOthello.BOARD_SIZE + j;
	}
}
