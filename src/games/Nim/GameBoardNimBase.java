package games.Nim;

import java.util.ArrayList;

import controllers.PlayAgent;
import games.Arena;
import games.GameBoard;
import games.StateObservation;
import tools.Types.ACTIONS;


/**
 * We need this purely abstract class just to have a common ground for member {@code m_gb} in {@link GameBoardNimGui}.
 */
public abstract class GameBoardNimBase implements GameBoard {

	public GameBoardNimBase() {	}

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
	abstract public void clearBoard(boolean boardClear, boolean vClear);

	@Override
	abstract public void destroy();

	@Override
	abstract public void updateBoard(StateObservation so, boolean withReset, boolean showValueOnGameboard);

	@Override
	abstract public void showGameBoard(Arena arena, boolean alignToMain);

	@Override
	abstract public void toFront();

	@Override
	abstract public boolean isActionReq();

	@Override
	abstract public void setActionReq(boolean actionReq);
	
	@Override
	abstract public void enableInteraction(boolean enable);

	@Override
	abstract public StateObservation getStateObs();

	@Override
	abstract public String getSubDir();

	@Override
	abstract public Arena getArena();

	@Override
	abstract public StateObservation getDefaultStartState();

	@Override
	abstract public StateObservation chooseStartState(PlayAgent pa);

	@Override
	abstract public StateObservation chooseStartState();
}
