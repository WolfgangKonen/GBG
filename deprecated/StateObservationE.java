package games;

import tools.Types;

//
// not really needed, we override equals(Object arg0), if we need it
// 

/**
 * Interface StateObservationE extends StateObservation by
 * <ul>
 * <li> equality metho ({@link #isEqualTo()}), 
 * </ul><p>
 * 
 * @author Wolfgang Konen, TH K�ln, Feb'17
 */
public interface StateObservationE extends StateObservation {

	/**
	 * Check whether the game state of {@code this} is equal to the game state of {@code so}
	 * @param so
	 * @return true, if game states are the same 
	 */
	public boolean isEqualTo(StateObservation so);

}
