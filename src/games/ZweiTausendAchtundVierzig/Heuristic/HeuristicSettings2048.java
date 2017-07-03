package games.ZweiTausendAchtundVierzig.Heuristic;

import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import games.Evaluator;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.StateObserver2048;
import params.MCTSExpectimaxParams;
import tools.Types;

import java.util.Random;

/**
 * Created by Johannes on 27.06.2017.
 */
public class HeuristicSettings2048 {
    private int geneLength = 15;
    private double[] genes;
    public int fitness;
    private Random random = new Random();

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

    public void calcFitness () {
        MCTSExpectimaxParams mctseParams = new MCTSExpectimaxParams();
        mctseParams.setHeuristicSettings2048(this);
        mctseParams.setNumIter(100);
        mctseParams.setEnableHeuristics(true);

        MCTSExpectimaxAgt mctseAgt = new MCTSExpectimaxAgt("MCTSE", mctseParams);

        int totScore = 0;


        for(int i = 0; i < ConfigEvaluator.NUMBEREVALUATIONS; i++) {
            StateObserver2048 so = new StateObserver2048();

            while (!so.isGameOver()) {
                Types.ACTIONS action = mctseAgt.getNextAction(so, false, new double[so.getNumAvailableActions() + 1], true);
                so.advance(action);
            }

            totScore += so.score;
        }

        fitness = totScore;
    }

    public HeuristicSettings2048 crossover(HeuristicSettings2048 partner) {
        HeuristicSettings2048 child = new HeuristicSettings2048();

        int midpoint = random.nextInt(genes.length);

        for (int i = 0; i < genes.length; i++) {
            if (i > midpoint) {
                child.genes[i] = genes[i];
            }
            else {
                child.genes[i] = partner.genes[i];
            }
        }
        return child;
    }

    public void mutate(double mutationRate) {
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < mutationRate) {
                genes[i] = random.nextDouble();
            }
        }
    }

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