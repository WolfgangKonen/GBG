package controllers.Decorators;

import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

public final class UseMoveTimesOfAgent extends PlayAgentByDelegate {

    private final UseMoveTime useMoveTime;

    /**
     * @param agent PlayAgent instance whose move times should be used (e.g. for logging).
     */
    public UseMoveTimesOfAgent(final PlayAgent agent, final UseMoveTime useMoveTime) {
        super(agent);
        this.useMoveTime = useMoveTime;
    }

    @Override
    public Types.ACTIONS_VT getNextAction2(final StateObservation sob, final boolean random, final boolean silent) {
        var startTime = System.nanoTime();
        var nextAction = super.getNextAction2(sob, random, silent);
        useMoveTime.use(
                new MoveTime(System.nanoTime() - startTime)
        );
        return nextAction;
    }

    /**
     * Represents an action which is applied to the move time.
     */
    public interface UseMoveTime {
        void use(final MoveTime moveTime);
    }

    public final class MoveTime {

        private final long timeInNanoSeconds;

        public MoveTime(final long timeInNanoSeconds) {
            this.timeInNanoSeconds = timeInNanoSeconds;
        }

        public double inSeconds() {
            return timeInNanoSeconds / 1e+9;
        }
    }
}