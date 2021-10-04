package games;

import tools.Types;

import java.util.ArrayList;

abstract public class ObsNondetBase extends ObserverBase implements StateObsNondeterministic {
    public ObsNondetBase() {
        super();
    }

    public ObsNondetBase(ObsNondetBase other) {
        super(other);
    }

    /**
     * Default implementation for perfect-information games: the partial state that the player-to-move observes is
     * identical to {@code this}. <br>
     * Games with imperfect information have to override this method.
     *
     * @return the partial information state (here: identical to {@code this})
     */
    public StateObsNondeterministic partialState() { return this; }

    /**
     * Default implementations for perfect-information games (e.g. 2048)
     */
    public ArrayList<Types.ACTIONS> getAvailableCompletions() {
        throw new RuntimeException("[getAvailableCompletions] Games with imperfect information (partial states) need to override this method. "+
                "Games without partial states should never call it");
    }
    public ArrayList<Types.ACTIONS> getAvailableCompletions(int p) {
        throw new RuntimeException("[getAvailableCompletions] Games with imperfect information (partial states) need to override this method. "+
                "Games without partial states should never call it");
    }


}
