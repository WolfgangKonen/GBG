package games.Othello;

import games.Othello.Edax.CommandLineInteractor;

public class TestMain {

	public static void main(String[] args)
	{		
		
		
		System.out.flush();
		int move = 26;
		
		/**
		 * The First test with my test.exe to test timing behaviours and timeouts
		 */
//		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "Test.exe");
//		System.out.println(cli.sendAndAwait("mode 1"));
//		System.out.println(cli.sendAndAwait("10000"));
//		System.out.println(cli.sendAndAwait("f"));
//		cli.stop();

		/**
		 * The second test with edax.exe, to test the first three moves
		 */
//		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
////		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe");
//		System.out.println(cli.sendAndAwait("mode 1"));
//		System.err.println("-----");
//		System.out.println(cli.sendAndAwait("f6"));
//		System.err.println("-----");
//		System.out.println(cli.sendAndAwait("f4"));
//		System.err.println("-----");
//		System.out.println(cli.sendAndAwait("c5"));
//		System.err.println("-----");
//		System.out.println(cli.sendAndAwait("d7"));
//		System.err.println("-----");
//		System.out.println(cli.sendAndAwait("b3"));
////		System.out.println(cli.sendAndAwait("d6"));
////		System.out.println(cli.sendAndAwait("d6"));
//		cli.stop();
		
		CommandLineInteractor cli = new CommandLineInteractor("", "");
		System.out.println(cli.sendAndAwait("ipconfig"));
		System.out.println("...........");
		System.out.println(cli.sendAndAwait("help"));
		System.out.println("...........");
		System.out.println(cli.sendAndAwait("dir"));
		System.out.println("...........");
		System.out.println(cli.sendAndAwait("ipconfig"));
	}
}
