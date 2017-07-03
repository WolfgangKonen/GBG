package games.ZweiTausendAchtundVierzig.Heuristic;

import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.Evaluator;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import params.MCTSExpectimaxParams;
import tools.Types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Johannes on 27.06.2017.
 */
public class HeuristicSettings2048 implements Serializable{
    private int geneLength = 15;
    private double[] genes;
    public int fitness;
    private Random random = new Random();
    private ExecutorService executorService = Executors.newFixedThreadPool(6);

    private static final long serialVersionUID = 1;

    //empty tiles
    public boolean enableEmptyTiles = true;
    public double emptyTilesWeighting0 = 1;
    public double emptyTilesWeighting1 = 1;
    public double emptyTilesWeighting2 = 1;
    public int emptyTilesMethod = 2;            //valid methods are 0 => numeEmptyTiles^weighting, 1 => highestTileValue * numEmptyTile * weighting, 2 => score * numEmptyTile * Weighting

    //highest tile in corner
    public boolean enableHighestTileInCorner = true;
    public double highestTileIncornerWeighting = 1;

    //row
    public boolean enableRow = true;
    public double rowWeighting0 = 1;
    public double rowWeighting1 = 1;
    public int rowMethod = 1;                   //valid methods are 0 => tile in a row has to be lower then the previous tile, 1 => tile in a row is exactly half of the previous tile

    //merge
    public boolean enableMerge = true;
    public double mergeWeighting = 1;

    //rollout
    public boolean enableRollout = true;
    public double rolloutWeighting = 1;

    public HeuristicSettings2048() {
        genes = new double[geneLength];
        for (int i = 0; i < genes.length; i++) {
            genes[i] = random.nextDouble();
        }

        applyGenes();
    }

    /**
     * calculate the fitness
     */
    public void calcFitness () {
        List<Callable<StateObserver2048>> callables = new ArrayList<>();
        MCTSExpectimaxParams mctseParams = new MCTSExpectimaxParams();
        mctseParams.setHeuristicSettings2048(this);
        mctseParams.setNumIter(4000);
        mctseParams.setEnableHeuristics(true);

        int totScore = 0;


        for(int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
            callables.add(() -> {
                MCTSExpectimaxAgt mctseAgt = new MCTSExpectimaxAgt("MCTSE", mctseParams);

                StateObserver2048 so = new StateObserver2048();

                while (!so.isGameOver()) {
                    Types.ACTIONS action = mctseAgt.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true);
                    so.advance(action);
                }

                return so;
            });
        }

        List<StateObserver2048> stateObservers = new ArrayList<>();

        try {
            executorService.invokeAll(callables).stream().map(future -> {
                try {
                    return future.get();
                }
                catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).forEach(stateObservers::add);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (StateObserver2048 so: stateObservers) {
            totScore += so.score;
        }

        fitness = totScore;
    }

    /**
     * crossover this genes with the genes of a partner and create a new genecombination
     * all genes for one Heuristic are chosen from the same genepool
     *
     * @param partner the partner
     * @return the new genecombination
     */
    public HeuristicSettings2048 crossover(HeuristicSettings2048 partner) {
        HeuristicSettings2048 child = new HeuristicSettings2048();

        //empty tiles
        if(random.nextDouble() < 0.5) {
            child.genes[0] = genes[0];
            child.genes[1] = genes[1];
            child.genes[2] = genes[2];
            child.genes[3] = genes[3];
            child.genes[4] = genes[4];
        } else {
            child.genes[0] = partner.genes[0];
            child.genes[1] = partner.genes[1];
            child.genes[2] = partner.genes[2];
            child.genes[3] = partner.genes[3];
            child.genes[4] = partner.genes[4];
        }

        //highest tile in corner
        if(random.nextDouble() < 0.5) {
            child.genes[5] = genes[5];
            child.genes[6] = genes[6];
        } else {
            child.genes[5] = partner.genes[5];
            child.genes[6] = partner.genes[6];
        }

        //row
        if(random.nextDouble() < 0.5) {
            child.genes[7] = genes[7];
            child.genes[8] = genes[8];
            child.genes[9] = genes[9];
            child.genes[10] = genes[10];
        } else {
            child.genes[7] = partner.genes[7];
            child.genes[8] = partner.genes[8];
            child.genes[9] = partner.genes[9];
            child.genes[10] = partner.genes[10];
        }

        //merge
        if(random.nextDouble() < 0.5) {
            child.genes[11] = genes[11];
            child.genes[12] = genes[12];
        } else {
            child.genes[11] = partner.genes[11];
            child.genes[12] = partner.genes[12];
        }

        //rollout
        if(random.nextDouble() < 0.5) {
            child.genes[13] = genes[13];
            child.genes[14] = genes[14];
        } else {
            child.genes[13] = partner.genes[13];
            child.genes[14] = partner.genes[14];
        }

        return child;
    }

    /**
     * mutate some randomly selected genes
     *
     * @param mutationRate the mutation rate
     */
    public void mutate(double mutationRate) {
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < mutationRate) {
                genes[i] = random.nextDouble();
            }
        }
    }

    /**
     * set the settings accordingly to the current genes
     */
    private void applyGenes() {
        enableEmptyTiles = genes[0] < 0.5;
        emptyTilesWeighting0 = genes[1];
        emptyTilesWeighting1 = genes[2];
        emptyTilesWeighting2 = genes[3];
        emptyTilesMethod = (int) (genes[4] * 3);

        enableHighestTileInCorner = genes[5] < 0.5;
        highestTileIncornerWeighting = genes[6];

        enableRow = genes[7] < 0.5;
        rowWeighting0 = genes[8];
        rowWeighting1 = genes[9];
        rowMethod = (int) (genes[10] * 2);

        enableMerge = genes[11] < 0.5;
        mergeWeighting = genes[12];

        enableRollout = genes[13] < 0.5;
        rolloutWeighting = genes[14];
    }

    /**
     * create a string which containes the current settings
     *
     * @return the string
     */
    public String toString() {
        String string = "enableEmptyTiles: " + enableEmptyTiles +
                "\nemptyTilesMethod: " + emptyTilesMethod;
        switch (emptyTilesMethod) {
            case 0:
                string += "\nemptyTilesWeighting: " + emptyTilesWeighting0;
                break;
            case 1:
                string += "\nemptyTilesWeighting: " + emptyTilesWeighting1;
                break;
            case 2:
                string += "\nemptyTilesWeighting: " + emptyTilesWeighting2;
                break;
        }

        string += "\nenableHighestTileInCorner: " + enableHighestTileInCorner +
                "\nhighestTileIncornerWeighting: " + highestTileIncornerWeighting;

        string += "\nenableRow: " + enableRow +
                "\nrowMethod: " + rowMethod;

        switch (rowMethod) {
            case 0:
                string += "\nrowWeighting: " + rowWeighting0;
                break;
            case 1:
                string += "\nrowWeighting: " + rowWeighting1;
                break;
        }

        string += "\nenableMerge: " + enableMerge +
                "\nmergeWeighting: " + mergeWeighting;

        string += "\nenableRollout: " + enableRollout +
                "\nrolloutWeighting: " + rolloutWeighting;

        return string;
    }
}