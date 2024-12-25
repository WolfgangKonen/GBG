package controllers.SB3;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

public class SB3Agent extends AgentBase implements PlayAgent  {
    @Override
    public Types.ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean deterministic, boolean silent) {
        return null;
    }
}
