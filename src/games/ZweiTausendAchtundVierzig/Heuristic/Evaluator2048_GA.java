package games.ZweiTausendAchtundVierzig.Heuristic;

import controllers.PlayAgent;
import games.Evaluator;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.Evaluator2048;
import params.MCTSExpectimaxParams;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Johannes on 29.06.2017.
 */
public class Evaluator2048_GA extends Evaluator {
    private int verbose;
    private HeuristicSettings2048[] population = new HeuristicSettings2048[ConfigEvaluator.TOTALPOPULATION];
    private Random random = new Random();
    private int totalfitness = 0;
    private int bestFitness = 0;
    private int bestHeuristicSettings2048 = 0;

    public Evaluator2048_GA(PlayAgent e_PlayAgent, int stopEval, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
        this.verbose = verbose;
    }

    @Override
    protected boolean eval_Agent() {
        for(int i = 0; i < population.length; i++) {
            population[i] = new HeuristicSettings2048();
        }
        if(verbose == 0) {
            System.out.println("Generated a population of " + ConfigEvaluator.TOTALPOPULATION + " new heuristic settings.");
        }

        while(true) {
            newGeneration();
        }

        //return true;
    }

    private void newGeneration() {
        totalfitness = 0;
        bestFitness = 0;
        if(verbose == 0) {
            System.out.println("\nStarted evaluation of the new generation.");
        }

        for (int i = 0; i < population.length; i++) {
            population[i].calcFitness();
            totalfitness += population[i].fitness;
            if(verbose == 0) {
                System.out.println("Evaluated member " + (i+1) + " with fitness " + population[i].fitness + ".");
                //System.out.println(population[i].toString() + "\n");
            }

            if(population[i].fitness >= bestFitness) {
                bestHeuristicSettings2048 = i;
            }
        }

        if(verbose == 0) {
            System.out.println("\nFinished evaluation of the new generation. Average fitness is: " + getLastResult() + ", total fitness is " + totalfitness + ".");
            System.out.println("Best new member is " + bestHeuristicSettings2048 + " with fitness " + population[bestHeuristicSettings2048].fitness + " and this settings:");
            System.out.println(population[bestHeuristicSettings2048].toString());
        }

        HeuristicSettings2048[] childPopulation = new HeuristicSettings2048[ConfigEvaluator.TOTALPOPULATION];

        for (int i = 0; i < population.length; i++) {
            HeuristicSettings2048 child = getWeightedRandomPop().crossover(getWeightedRandomPop());
            child.mutate(ConfigEvaluator.MUTATIONRATE);
            childPopulation[i] = child;
        }

        population = childPopulation;

        if(verbose == 0) {
            System.out.print("\nGenerated a new population.");
        }
    }

    private HeuristicSettings2048 getWeightedRandomPop() {
        double value = Math.random() * totalfitness;

        for (HeuristicSettings2048 pop : population) {
            value -= pop.fitness;
            if (value <= 0.0d) {
                return pop;
            }
        }

        throw new RuntimeException("Something is wrong with getWeightedRandomPop()");
    }

    @Override
    public double getLastResult() {
        return (double)totalfitness / (double)population.length;
    }

    @Override
    public String getMsg() {
        return null;
    }

    @Override
    public boolean isAvailableMode(int mode) {
        switch (mode) {
            case 1:
                return true;
            case 2:
                return true;
            case 3:
                return true;
            default:
                return false;
        }
    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{0, 1, 2};
    }

    @Override
    public int getQuickEvalMode() {
        return ConfigEvaluator.DEFAULTEVALUATOR;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

    @Override
    public int getMultiTrainEvalMode() {
        return 0;
    }

    @Override
    public String getPrintString() {
        return"success rate";
    }

    @Override
    public String getPlotTitle() {
        return "success";
    }
}
