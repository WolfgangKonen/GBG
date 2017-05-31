package controllers.MC;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import params.MCParams;
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

    public MCParams mcParams;


    
    public MCAgent(MCParams mcParams) {
        this("MC", mcParams);
    }

    public MCAgent(String name, MCParams mcParams)
    {
        super(name);
        this.mcParams = mcParams;
        setAgentState(AgentState.TRAINED);
    }

    @Override
    public Types.ACTIONS getNextAction(StateObservation sob, boolean random, double[] vtable, boolean silent) {
        int iterations = mcParams.getIterations();
        int numberAgents = mcParams.getNumberAgents();
        int depth = mcParams.getRolloutdepth();

        if (mcParams.getCalcCertainty()) {
        	StateObserver2048 sobZTAV = (StateObserver2048) sob;
        	if (sobZTAV.getNumEmptyTiles()==10) {
            	int NC = ConfigEvaluator.NC;
                double cert0, cert1,cert2=0,cert4=0,cert6=0;
                cert0 = calcCertainty(sob, vtable,1,false, NC, iterations, depth);
                NC = 20;
                cert1 = calcCertainty(sob, vtable,1,false, NC, iterations, depth);
                cert2 = calcCertainty(sob, vtable,2,false, NC, iterations, depth);
                cert6 = calcCertainty(sob, vtable,6,false, NC, iterations, depth);
                System.out.println("n="+sob.getNumAvailableActions()+": certainty ="
                        + cert0+","+cert1
                        +" / "+ cert2
                        +" / "+ cert6);
        	}
        }

        if(mcParams.getNumberAgents() > 1) {
            //more than one agent (majority vote)
            return getNextActionMultipleAgents(sob, vtable, iterations, numberAgents, depth);
        } else {
            //only one agent
            return getNextAction(sob, vtable, iterations, depth);
        }
    }

    /**
     * Get the best next action and return it (multi-core version)
     * 
     * @param sob			current game state (not changed on return)
     * @param vtable		must be an array of size n+1 on input, where
     * 						n=sob.getNumAvailableActions(). On output,
     * 						elements 0,...,n-1 hold the score for each available
     * 						action (corresponding to sob.getAvailableActions())
     * 						In addition, vtable[n] has the score for the
     * 						best action.
     * @return nextAction	the next action
     */
    private Types.ACTIONS getNextAction (StateObservation sob, double[] vtable, int iterations, int depth) {
        //die Funktionen, welche auf die verschiedenen Kerne verteilt
        //werden sollen
        List<Callable<ResultContainer>> callables = new ArrayList<>();
        //das Ergebnis dieser Funktionen
        List<ResultContainer> resultContainers = new ArrayList<>();
        //alle fuer den Spielzustand verfuegbaren Aktionen
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations;
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
        for (int j = 0; j < iterations; j++) {
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
                    agent.startAgent(newSob, depth);			// contains BUG1 fix

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
            //vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore();	
        	// /WK/ **BUG2** in line above: referingState was missing. Use instead next line:
        	vtable[resultContainer.firstAction] += resultContainer.sob.getGameScore(sob);
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
            vtable[i] /= iterations;

            //es wurde eine Aktion mit einer hoeheren Score gefunden
            if (nextActionScore < vtable[i]) {
                nextAction = actions.get(i);
                nextActionScore = vtable[i];
            }
        }

        //die beste Aktion wird zurueckgegeben
        return nextAction;
    }

    private Types.ACTIONS getNextActionMultipleAgents (StateObservation sob, double[] vtable, int iterations, int numberAgents, int depth) {
        List<Types.ACTIONS> actions = sob.getAvailableActions();

        nRolloutFinished = 0;
        nIterations = sob.getNumAvailableActions()* iterations * numberAgents;
        totalRolloutDepth = 0;

        if(sob.getNumAvailableActions() == 1) {
            return actions.get(0);
        }
        for (int i=0; i < vtable.length; i++) vtable[i]=0; // /WK/ bug fix, was missing before

        for (int i = 0; i < numberAgents; i++) {
            int nextAction = 0;
            double nextActionScore = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < sob.getNumAvailableActions(); j++) {
                double averageScore = 0;

                for (int k = 0; k < iterations; k++) {
                    StateObservation newSob = sob.copy();

                    newSob.advance(actions.get(j));

                    RandomSearch agent = new RandomSearch();
                    agent.startAgent(newSob, depth);			// contains BUG1 fix

                    //averageScore += newSob.getGameScore();	
                	// /WK/ **BUG2** in line above: referingState was missing. 
                    // Use instead next line:
                    averageScore += newSob.getGameScore(sob);
                    if (newSob.isGameOver()) nRolloutFinished++;
                    totalRolloutDepth += agent.getRolloutDepth();
                }

                averageScore /= iterations;
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
        
        //for (int i = 0; i < vtable.length-1; i++) System.out.print((int)vtable[i]+",");
        //System.out.println(""+sob.stringDescr());

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
    
    /**
     * Calculate the certainty by repeating the next-action calculation 
     * NC times and calculating the relative frequency of the most frequent
     * next action
     * 
     * @param sob
     * @param vtable
     * @param numberAgents	=1: use getNextAction (parallel version) <br>
     *                      >1: use getNextActionMultipleAgents with NUMBERAGENTS=numberAgents
     * @param silent
     * @param NC			number of repeats for next-action calculation
     * @param iterations    rollout repeats (for each available action)
     * @param depth			rollout depth
     * @return the certainty (highest bin in the relative-frequency histogram of possible actions)
     */
    public double calcCertainty(StateObservation sob, double[] vtable, int numberAgents, boolean silent, int NC, int iterations, int depth) {
        double[] wtable = new double[4];
        double highestBin;
        int nextAction;
        
        if(sob.getNumAvailableActions() == 1) {
            return 1.0;
        }

        for (int i = 0; i < NC; i++) {
        	if (numberAgents==1) {
        		nextAction = getNextAction(sob,vtable,iterations, depth).toInt();
        	} else {
        		nextAction = getNextActionMultipleAgents(sob,vtable,iterations,numberAgents, depth).toInt();
        		if (!silent) System.out.print(".");
        	}
            wtable[nextAction]++;
        }
        if (numberAgents!=1 & !silent) System.out.println("");

        highestBin = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < wtable.length; i++) {
            if (highestBin < wtable[i]) {
                highestBin = wtable[i];
            } 
        }
        double cert = highestBin/NC;
        
    	return cert;
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