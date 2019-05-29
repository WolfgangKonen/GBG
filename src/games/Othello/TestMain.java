package games.Othello;

import games.Othello.Edax.CommandLineInteractor;

public class TestMain {

	public static void main(String[] args)
	{		
		System.out.flush();
		/**
		 * The First test with my test.exe to test timing behaviours and timeouts
		 */
		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "Test.exe");
		System.out.println(cli.sendAndAwait("mode 1"));
		System.out.println(cli.sendAndAwait("10000"));
		cli.sendAndAwait("f");
		cli.stop();

		/**
		 * The second test with edax.exe, to test the first three moves
		 */
//		CommandLineInteractor cli = new CommandLineInteractor("agents\\Othello\\Edax", "edax.exe", ".*[eE]dax plays ([A-z][0-8]).*", 1);
//		System.out.println(cli.sendAndAwait("mode 1"));
//		System.out.println(cli.sendAndAwait("f6"));
//		System.out.println(cli.sendAndAwait("f4"));
//		cli.stop();
	}
}
