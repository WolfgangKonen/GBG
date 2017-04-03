package controllers.MC;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import tools.Types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 08.12.2016.
 */
public class MCAgent extends AgentBase implements PlayAgent {
    private Random random = new Random();
    private ExecutorService executorService = Executors.newWorkStealingPool();

    private int totalRolloutDepth = 0;  // saves the average rollout depth for the mc Agent
    private int nRolloutFinished = 0; 	// counts the number of rollouts ending with isGameOver==true
    private int nIterations = 0; 		// counts the total number of iterations

    public MCAgent() {
        this("MC");
    }

    public MCAgent(String name)
    {
        super(name);
        setAgentState(AgentState.TRAINED);
    }

    @Override
    public Types.ACTIONS getNextAction(StateObservation sob, boolean random, double[] vtable, boolean silent) {
        return getNextAction(sob, vtable);
    }

/**
 * Get the best next action and return it
 * @param sob			current game state (not changed on return)
 * @param vtable		must be an array of size n+1 on input, where
 * 						n=sob.getNumAvailableActions(). On output,
 * 						elements 0,...,n-1 hold the score for each available
 * 						action (corresponding to sob.getAvailableActions())
 * 						In addition, vtable[n] has the score for the
 * 						best action.
 * @return nextAction	the next action
 */




    public Types.ACTIONS getNextAction (StateObservation sob, double[] vtable) {
        //die Funktionen, welche auf die verschiedenen Kerne verteilt
        //werden sollen
        List<Callable<ResultContainer>> callables = new ArrayList<>();
        //das Ergebnis dieser Funktionen
        List<ResultContainer> resultContainers = new ArrayList<>();
        //alle fuer den Spielzustand verfuegbaren Aktionen
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()*Config.ITERATIONS;
        totalRolloutDepth = 0;

        //es ist nur eine Aktion verfuegbar, diese kann sofort
        //zurueckgegeben werden
        if(sob.getNumAvailableActions() == 1) {
            return actions.get(0);
        }

        //die Funktionen, welche anschliessend auf die verschiedenen
        //Kerne verteilt werden sollen, werden erstellt
        //fuer jede Iteration wird eine Funktion mit jeder verfuegbaren
        //Aktion erstellt
        for (int j = 0; j < Config.ITERATIONS; j++) {
            for (int i = 0; i < sob.getNumAvailableActions(); i++) {

                //eine Kopie des Spielzustandes wird erstellt
                StateObservation newSob = sob.copy();

                //die Kennzeichnung der ersten Aktion muss
                //Zwischengespeichert werden, da auf den aktuellen
                //Wert der for-Schleife zum Zeitpunkt der Ausfuehrung
                //des callables nicht mehr zugegriffen werden kann
                int firstActionIdentifier = i;

                //Die callables, also die Funktionen die spaeter auf
                //mehreren Kernen parallel ausgefuehrt werden, werden
                //erstellt. Die callables werden hier nur erstellt und
                //erst mit dem Ausfuehren der Funktion
                //invokeAll(callables) auf executorService in Zeile 79
                //durchlaufen
                callables.add(() -> {

                    //die erste Aktion wird ermittelt und auf dem
                    //Spielzustand ausgefuehrt
                    Types.ACTIONS firstAction = actions.get(firstActionIdentifier);
                    newSob.advance(firstAction);

                    //der Random Agent wird erstellt und simuliert
                    //ein Spiel
                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob);

                    //das Ergebnis der Simulation wird in einem
                    //ResultContainer zurueckgegeben
                    return new ResultContainer(firstActionIdentifier, newSob, agent.getRolloutDepth());
                });
            }
        }

        try {
            //Der executorService wird aufgerufen und verteilt die
            //zuvor erstellen Simulationen auf alle Kerne der CPU.
            //Die Ergebnisse der Simulationen werden in einen stream
            //geschrieben.
            executorService.invokeAll(callables).stream().map(future -> {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }

            //jedes Ergebnis das in den stream geschrieben wurde wird
            //in der Liste resultContainers gespeichert
            }).forEach(resultContainers::add);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //jeder resultContainer in der Liste resultContainers wird
        //der zugehoerigen Aktion im vtable hinzuadiert
        for(ResultContainer resultContainer : resultContainers) {
            vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore();
            totalRolloutDepth += resultContainer.rolloutDepth;
            if(resultContainer.sob.isGameOver()) {
                nRolloutFinished++;
            }
        }

        //hier wird die naechste Aktion sowie ihre Bewertung
        //gespeichert
        Types.ACTIONS nextAction = null;
        double nextActionScore = Double.NEGATIVE_INFINITY;

        //es wird jede im vtable gespeicherte Aktion durchlaufen
        for (int i = 0; i < sob.getNumAvailableActions(); i++) {

            //es wird die durchschnittliche Score jeder Aktion gebildet
            vtable[i] /= Config.ITERATIONS;

            //es wurde eine Aktion mit einer hoeheren Score gefunden
            if (nextActionScore < vtable[i]) {
                nextAction = actions.get(i);
                nextActionScore = vtable[i];
            }
        }

        //die beste Aktion wird zurueckgegeben
        return nextAction;
    }

    public Types.ACTIONS getNextActionMultipleAgents (StateObservation sob, double[] vtable) {
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()*Config.ITERATIONS*Config.NUMBERAGENTS;
        totalRolloutDepth = 0;

        if(sob.getNumAvailableActions() == 1) {
            return actions.get(0);
        }

        for (int i = 0; i < Config.NUMBERAGENTS; i++) {
            int nextAction = 0;
            double nextActionScore = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < sob.getNumAvailableActions(); j++) {
                double averageScore = 0;

                for (int k = 0; k < Config.ITERATIONS; k++) {
                    StateObservation newSob = sob.copy();

                    newSob.advance(actions.get(j));
                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob);

                    averageScore += newSob.getGameScore();
                    if (newSob.isGameOver()) nRolloutFinished++;
                    totalRolloutDepth += agent.getRolloutDepth();
                }

                averageScore /= Config.ITERATIONS;
                if (nextActionScore <= averageScore) {
                    nextAction = j;
                    nextActionScore = averageScore;
                }
            }
            vtable[nextAction]++;
        }

        List<Types.ACTIONS> nextActions = new ArrayList<>();
        double nextActionScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            if (nextActionScore < vtable[i]) {
                nextActions.clear();
                nextActions.add(actions.get(i));
                nextActionScore = vtable[i];
            } else if(nextActionScore == vtable[i]) {
                nextActions.add(actions.get(i));
            }
        }

        return nextActions.get(random.nextInt(nextActions.size()));
    }

    @Override
    public double getScore(StateObservation sob) {
        double[] vtable = new double[sob.getNumAvailableActions()+1];
        double nextActionScore = Double.NEGATIVE_INFINITY;

        getNextAction(sob, true, vtable, true);

        for (int i = 0; i < sob.getNumAvailableActions(); i++) {
            if (nextActionScore <= vtable[i]) {
                nextActionScore = vtable[i];
            }
        }

        return nextActionScore;
    }

    public double getAverageRolloutDepth() {
        return totalRolloutDepth/nIterations;
    }

    public int getNRolloutFinished() {
        return nRolloutFinished;
    }

    public int getNIterations() {
        return nIterations;
    }

    @Override
    public boolean wasRandomAction() {
        return false;
    }

    @Override
    public String stringDescr() {
        String cs = getClass().getName();
        return cs;
    }
}

class ResultContainer {
    public int firstAction;
    public StateObservation sob;
    public double rolloutDepth;

    public ResultContainer(int firstAction, StateObservation sob, double rolloutDepth) {
        this.firstAction = firstAction;
        this.sob = sob;
        this.rolloutDepth = rolloutDepth;
    }
}