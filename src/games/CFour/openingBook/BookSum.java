package games.CFour.openingBook;

import java.io.IOException;

/**
 * @author Markus Thill
 * 
 *         Load all opening Books in this class.
 */
public class BookSum {

	private Book openingBook = null;
	private Book openingBookDeep = null;
	private Book openingBookDeepDist = null;

	public BookSum() {
	}

	public Book getOpeningBook() {

		if (openingBook == null) {
			openingBook = new Book(Book.NORMALBOOK);
			try {
				openingBook.openBook();
				openingBook.readBook();
				openingBook.closeBook();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return openingBook;
	}

	public Book getOpeningBookDeep() {
		if (openingBookDeep == null) {
			openingBookDeep = new Book(Book.DEEPBOOK);
			try {
				openingBookDeep.openBook();
				openingBookDeep.readBook();
				openingBookDeep.closeBook();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return openingBookDeep;
	}

	public Book getOpeningBookDeepDist() {
		if (openingBookDeepDist == null) {
			openingBookDeepDist = new Book(Book.DISTDEEPBOOK);
			try {

				openingBookDeepDist.openBook();
				openingBookDeepDist.readBook();
				openingBookDeepDist.closeBook();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return openingBookDeepDist;
	}
}
