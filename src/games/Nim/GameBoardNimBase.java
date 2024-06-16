package games.Nim;

import java.util.Random;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;


/**
 * We need this purely abstract class just to have a common ground for member {@code m_gb} in {@link GameBoardNimGui}.
 */
public abstract class GameBoardNimBase extends GameBoardBase implements GameBoard {

	public GameBoardNimBase(Arena ar) {
		super(ar);
	}

	abstract public int[] getHeaps();
	abstract protected void HGameMove(int x, int y);
	abstract protected void InspectMove(int x, int y);
	
	@Override
	abstract public void initialize();

	/**
	 * update game-specific parameters from {@link Arena}'s param tabs
	 */
	@Override
	public void updateParams() {}

	@Override
	abstract public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand);

	@Override
	abstract public void destroy();

	@Override
	abstract public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard);

	@Override
	abstract public void showGameBoard(Arena arena, boolean alignToMain);

	@Override
	abstract public void toFront();

	@Override
	abstract public void enableInteraction(boolean enable);

	@Override
	abstract public StateObservation getStateObs();

	@Override
	abstract public String getSubDir();

	@Override
	abstract public StateObservation getDefaultStartState(Random cmpRand);

	@Override
	abstract public StateObservation chooseStartState(PlayAgent pa);

	@Override
	abstract public StateObservation chooseStartState();
}
