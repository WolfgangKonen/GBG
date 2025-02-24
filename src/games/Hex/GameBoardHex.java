package games.Hex;

import games.Arena;
import games.GameBoard;
import games.GameBoardBase;
import games.StateObservation;
import tools.Types;

import controllers.PlayAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import static games.Hex.HexConfig.PLAYER_ONE;
import static games.Hex.HexConfig.PLAYER_TWO;

/**
 * This class implements the GameBoard interface for Hex.
 * Its member {@link GameBoardHexGui} {@code m_gameGui} has the game board GUI. 
 * {@code m_gameGui} may be {@code null} in batch runs. 
 * 
 * @author Kevin Galitzki, TH Koeln, 2018
 */
public class GameBoardHex extends GameBoardBase implements GameBoard {
    protected StateObserverHex m_so;
    protected Random rand;
    final boolean verbose = false;

//	/**
//	 * SerialNumber
//	 */
//	@Serial
//    public static final long serialVersionUID = 12L;
	
	private transient GameBoardHexGui m_gameGui = null;
	
    public GameBoardHex(Arena arena) {
        super(arena);
        m_so = new StateObserverHex();
        rand = new Random(System.currentTimeMillis());

        if (getArena().hasGUI() && m_gameGui==null) {
        	m_gameGui = new GameBoardHexGui(this);
        }
    }

    @Override
    public void initialize() {  }

    /**
     * update game-specific parameters from {@link Arena}'s param tabs
     */
    @Override
    public void updateParams() {}

    @Override
    public void clearBoard(boolean boardClear, boolean vClear, Random cmpRand) {
        if (boardClear) {
            m_so = new StateObserverHex();
        } else if (vClear) {
            m_so.clearTileValues();
        }
							// considerable speed-up during training (!)
        if (m_gameGui!=null && getArena().taskState!=Arena.Task.TRAIN)
        	m_gameGui.clearBoard(boardClear, vClear);
    }


    @Override
    public void setStateObs(StateObservation so) {
        StateObserverHex soHex;

        if (so != null) {
            assert (so instanceof StateObserverHex)
                    : "StateObservation 'so' is not an instance of StateObserverHex";
            soHex = (StateObserverHex) so;
            m_so = soHex; //.copy();
        }
    }

    @Override
    public void updateBoard(StateObservation so,  
							boolean withReset, boolean showValueOnGameboard) {
        setStateObs(so);	// asserts that so is StateObserverHex
        StateObserverHex soHex = (StateObserverHex) so;

		if (m_gameGui!=null)
			m_gameGui.updateBoard(soHex, withReset, showValueOnGameboard);

        if (verbose) {
            double featureVectorP1[] = HexUtils.getFeature3ForPlayer(soHex.getBoard(), PLAYER_ONE);
            double featureVectorP2[] = HexUtils.getFeature3ForPlayer(soHex.getBoard(), PLAYER_TWO);
            System.out.println("---------------------------------");
            System.out.println("Longest chain for player BLACK: " + featureVectorP1[0]);
            System.out.println("Longest chain for player WHITE: " + featureVectorP2[0]);
            System.out.println("Free adjacent tiles for player BLACK: " + featureVectorP1[1]);
            System.out.println("Free adjacent tiles for player WHITE: " + featureVectorP2[1]);
            System.out.println("Virt. Con. for player BLACK: " + featureVectorP1[2]);
            System.out.println("Virt. Con. for player WHITE: " + featureVectorP2[2]);
            System.out.println("Weak Con. for player BLACK: " + featureVectorP1[3]);
            System.out.println("Weak Con. for player WHITE: " + featureVectorP2[3]);
            System.out.println("Dir. Con. for player BLACK: " + featureVectorP1[4]);
            System.out.println("Dir. Con. for player WHITE: " + featureVectorP2[4]);
        }
    }

    @Override
    public StateObservation getStateObs() {
        return m_so;
    }

    @Override
    public StateObservation getDefaultStartState(Random cmpRand) {
        clearBoard(true, true, null);
        return m_so;
    }

	/**
	 * @return a start state which is with probability 0.5 the empty board 
	 * 		start state and with probability 0.5 one of the possible one-ply 
	 * 		successors
	 */
    @Override
    public StateObservation chooseStartState() {
        clearBoard(true, true, null);
        if (rand.nextDouble() > 0.5) {
            // choose randomly one of the possible actions in default
            // start state and advance m_so by one ply
            ArrayList<Types.ACTIONS> acts = m_so.getAvailableActions();
            int i = rand.nextInt(acts.size());
            m_so.advance(acts.get(i), null);
        }
        return m_so;
    }

	@Override
    public StateObservation chooseStartState(PlayAgent pa) {
    	return chooseStartState();
    }
	
    @Override
    public String getSubDir() {
        DecimalFormat form = new DecimalFormat("00");
        return form.format(HexConfig.BOARD_SIZE);
    }

	@Override
	public void enableInteraction(boolean enable) {
		if (m_gameGui!=null)
			m_gameGui.enableInteraction(enable);
	}

	@Override
	public void showGameBoard(Arena arena, boolean alignToMain) {
		if (m_gameGui!=null)
			m_gameGui.showGameBoard(arena, alignToMain);
	}

	@Override
	public void toFront() {
		if (m_gameGui!=null)
			m_gameGui.toFront();
	}

	@Override
	public void destroy() {
		if (m_gameGui!=null)
			m_gameGui.destroy();
	}
}
