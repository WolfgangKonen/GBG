package games;

public interface PartialStateObserverND extends StateObsNondeterministic {
    /**
     *
     * @param snd       the full state (from the environment)
     * @param nPlayer    the player for which we want the partial state observation
     * @return  a state where all information from the full state which would be not visible to
     *          player nPlayer is deleted
     */
    StateObsNondeterministic partialState(StateObsNondeterministic snd, int nPlayer);
}
