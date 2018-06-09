package games.CFour;

/**
 * Interface, used for Operations on Lists
 * 
 * @author Markus Thill
 * 
 */
public interface ListOperation {
	public static enum Player {
		X, O
	};

	public void indexChanged(int newIndex);

	public void playerChanged(Player player);
}
