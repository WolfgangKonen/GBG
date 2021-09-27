package games;

import java.util.ArrayList;

abstract public class ObsNondetBase extends ObserverBase implements StateObsNondeterministic {
    public ObsNondetBase() {
        super();
    }

    public ObsNondetBase(ObsNondetBase other) {
        super(other);
    }
}
