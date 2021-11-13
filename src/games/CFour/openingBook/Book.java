package games.CFour.openingBook;

import java.io.*;

/**
 * There are 3 different opning-books available: <br>
 *         1. Opening book with all positions with 8 pieces (win/draw/loss) <br>
 *         2. Opening book with all positions with 12 pieces (win/draw/loss) <br>
 *         3. Opening book with all positions with 12 pieces (win/draw/loss with
 *         exact distance) <br>
 * 
 *         One of these books can be selected by setting the bookNr to 0,1 or 2.
 *         
 * @author Markus Thill 
 */
public class Book {

	// Constants
	private static final String BOOKPATH[] = { "book.dat", "bookDeep.dat",
			"bookDeepDist.dat" };
	private static final int BOOKSIZE[] = { 34286, 1735945, 4200899 };
	private static final int STONENUM[] = { 8, 12, 12 };
	private static final int MASKPOSITION = 0xFFFFFFFC;
	private static final int MASKVALUE = 0x3;
	private static final long MSBINT = 0x80000000L;

	public static final int NORMALBOOK = 0;
	public static final int DEEPBOOK = 1;
	public static final int DISTDEEPBOOK = 2;

	// Selected book-Nr
	private int bookNr;

	// Input-stream for reading the book
	private InputStream file = null;

	// All rows of the opening-book. Each row is coded in a special format
	// (exact 24- or 32-Bit)
	private int book[];

	// Only for Deep-book with Exact Distance
	byte vals[];

	/**
	 * @param bookNr
	 *            Selected book
	 */
	public Book(int bookNr) {
		this.bookNr = bookNr;
	}

	/**
	 * Open the selected book from the selected path
	 * 
	 * @throws IOException
	 */
	public void openBook() throws IOException {
		// old form where book*.dat was in src/games/CFour/openingBook and made the JAR files big:
		//file = getClass().getResourceAsStream(BOOKPATH[bookNr]);

		// new form where book*.dat is in agents/ConnectFour/AlphaBetaAgent/openingBook:
		String path = "agents/ConnectFour/AlphaBetaAgent/openingBook/"+BOOKPATH[bookNr];
		try {
			file = new BufferedInputStream(new FileInputStream(path));
		} catch (IOException e) {
			throw (new IOException("Could not open file "+path));
		}
		if (file == null)
			throw (new IOException("Could not open file "+path));
	}

	/**
	 * Close the selected book
	 * 
	 * @throws IOException
	 */
	public void closeBook() throws IOException {
		file.close();
	}

	/**
	 * Read the book from the HDD and save in the RAM.
	 * 
	 * @throws IOException
	 */
	public void readBook() throws IOException {
		book = new int[BOOKSIZE[bookNr]];

		if (bookNr == DISTDEEPBOOK)
			vals = new byte[BOOKSIZE[bookNr]];

		int shiftNum, temp;
		for (int i = 0; i < BOOKSIZE[bookNr]; i++) {
			shiftNum = (bookNr == NORMALBOOK ? 16 : 24);
			temp = 0;
			for (; shiftNum >= 0; shiftNum -= 8) {
				int b = file.read();
				temp |= (b << shiftNum);
			}
			book[i] = temp;

			if (bookNr == DISTDEEPBOOK) {
				byte b = (byte) file.read();
				vals[i] = b;
			}
		}
	}

	/**
	 * Search in the opening book for the coded board and return the value for
	 * this board. A fast binary-search is used.
	 * 
	 * @param codedPos
	 *            Position coded in an Integer (see class ConnectFour for
	 *            details)
	 * @param codedPosMirrored
	 *            Mirrored Position coded in an Integer (see class ConnectFour
	 *            for details)
	 * @return Game-Theoretic Value for this board
	 */
	public int getValue(int codedPos, int codedPosMirrored) {
		int code = 0, code2 = 0, pos, pos2, step;
		pos = pos2 = step = BOOKSIZE[bookNr] - 1;

		// Binary Search
		while (step > 0) {
			step = (step != 1 ? (step + (step & 1)) >> 1 : 0);
			if (pos < BOOKSIZE[bookNr] && pos >= 0)
				code = book[pos] & MASKPOSITION;
			if (pos2 < BOOKSIZE[bookNr] && pos2 >= 0)
				code2 = book[pos2] & MASKPOSITION;

			if (codedPos < code)
				pos -= step;
			else if (codedPos > code)
				pos += step;
			else if (codedPos == code)
				if (bookNr != DISTDEEPBOOK)
					return (book[pos] & MASKVALUE);
				else
					return vals[pos];

			if (codedPosMirrored < code2)
				pos2 -= step;
			else if (codedPosMirrored > code2)
				pos2 += step;
			else if (codedPosMirrored == code2)
				if (bookNr != DISTDEEPBOOK)
					return (book[pos2] & MASKVALUE);
				else
					return vals[pos2];
		}
		return 2; //Value was not found in database, must be a win for X
	}

	/**
	 * Get a Board and its value at an specified index of this opening-book
	 * 
	 * @param index
	 *            Position in the opening-book
	 * @param board
	 *            Return-Value: Contains the board
	 * @return Value for the board
	 */
	public int getBoard(int index, int board[][]) {
		int hCode = book[index];
		int col = 0, row = 0;

		long b1 = 0, b2 = 0;

		if (board == null)
			board = new int[7][6];
		if (bookNr == NORMALBOOK)
			hCode <<= 8; // Move to Front of Integer
		for (int i = 0; i < STONENUM[bookNr] + 6; i++) {
			b1 = hCode & MSBINT;
			hCode <<= 1;
			// Column is Full
			if (b1 == 0L) {
				col++;
				row = 0;
				continue;
			}

			b2 = hCode & MSBINT;
			hCode <<= 1;
			if (b2 != 0L)
				board[col][row++] = 2;
			else
				board[col][row++] = 1;
		}
		if (bookNr != DISTDEEPBOOK)
			return book[index] & MASKVALUE;
		return vals[index];
	}

	/**
	 * @return Size of the opening-book in bytes
	 */
	public int getBookSize() {
		return BOOKSIZE[bookNr];
	}
}
