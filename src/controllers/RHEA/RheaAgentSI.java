package controllers.RHEA;

import controllers.AgentBase;
import controllers.PlayAgent;
import games.StateObservation;
import org.apache.commons.math3.util.Pair;
import tools.ScoreTuple;
import tools.Types;

import java.awt.desktop.SystemSleepEvent;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;


/**
 * RHEA Agent Adaptation for Multiplayer Games with sparse rewards
 *
 * @author Marcel Hartwig, 2022-2023
 **/
public class RheaAgentSI extends AgentBase implements PlayAgent, Serializable {

  private static boolean debug = false;

  private static final String GEN_OP = "BOTH"; // CROSSOVER, MUTATION, BOTH
  private static final String SELECTION_TYPE = "ROULETTE"; //ROULETTE     (more to implement: RANK, TOURNAMENT)
  private static final String CROSSOVER_TYPE = "UNIFORM";
  private static final String MUTATION_TYPE = "UNIFORM";

  private static final int POP_SIZE = 50; // Population Size
  private static final int SIM_DEPTH = 25; // Simulation Depth
  private static final int GENERATIONS = 50;
  private static final double DEPTH_WEIGHT = 0.95; // weight of the depth inside the simulation on the fitness value (0 - infinity)
  private static final boolean ELITISM_PROMOTION = true;
  private static final boolean RANDOM_OPPONENT_TURN = false;
  private static final boolean FIRST_MOVE_OUTSOURCED = true;
  private static Random rand;


  public RheaAgentSI(String name, StateObservation so) {
    super(name);
    super.setAgentState(AgentState.TRAINED);
    rand = new Random();
  }

  @Override
  public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {
    int[][][] genome = new int[so.getAllAvailableActions().size()][POP_SIZE][SIM_DEPTH];
    genome = initPop(so, genome);

    double[] vtable = bestAction(so, genome);
    int bestAction = so.getAvailableActions().get(getMaxVTableAction(vtable)).toInt();
    double bestScore = getMaxVTableScore(vtable);
    ScoreTuple scBest = new ScoreTuple(so, bestScore);

    // System.out.println("BestAction = " + bestAction + " Score = " + bestScore);
    // System.out.println("vtable 0: " + vtable[0]);

    Types.ACTIONS_VT actBestVT = new Types.ACTIONS_VT(bestAction, false, vtable, bestScore, scBest);
    return actBestVT;
  }

  private double getMaxVTableScore(double[] vtable) {
    double maxScore = -100000.0;
    for (int i = 0; i < vtable.length; i++) {
      if (maxScore <= vtable[i]) {
        maxScore = vtable[i];
      }
    }
    return maxScore;
  }

  private int getMaxVTableAction(double[] vtable) {
    double maxScore = -100000.0;
    int entry = 0;
    for (int i = 0; i < vtable.length; i++) {
      if (maxScore < vtable[i]) {
        maxScore = vtable[i];
        entry = i;
      } else if (maxScore == vtable[i] && rand.nextInt(2) == 1) {
        maxScore = vtable[i];
        entry = i;
      }
    }
    return entry;
  }

  private double[] bestAction(StateObservation so, int[][][] genome) {
    double[] vtable = new double[so.getAvailableActions().size()];
    int woAmI = so.getPlayer();
    if (FIRST_MOVE_OUTSOURCED) {
      for (int i = 0; i < vtable.length; i++) {

          StateObservation soCopy = so.copy();
          Types.ACTIONS action = soCopy.getAvailableActions().get(i);
          soCopy.advance(action);

          vtable[i] = simulate(woAmI, soCopy, genome[i]);
          // System.out.println("Normalized Score for Action" + action + ": " + vtable[i]);

      }
    } else {
      vtable = simulate2(woAmI, so, genome[0]);
    }

    return vtable;
  }

  private double[] simulate2(int whoAmI, StateObservation so, int[][] population) {
    double[] vtable = new double[so.getAvailableActions().size()];
//    ArrayList<Pair<Integer, Double>> scoreList = new ArrayList<>();
//    boolean isMultiplayerGame = so.getNumPlayers() > 1;
//    boolean lastTurnRhea;
//
//    for (int i = 0; i < POP_SIZE; i++) {
//      lastTurnRhea = true;
//      StateObservation soCopy = so.copy();
//      int depth = 0;
//      for (; depth < SIM_DEPTH; depth++) {
//        if (soCopy.isGameOver()) {
//          break;
//        }
//        if (isMultiplayerGame && lastTurnRhea && RANDOM_OPPONENT_TURN) {
//          soCopy.advance(soCopy.getAvailableActions().get(rand.nextInt(soCopy.getAvailableActions().size())));
//          lastTurnRhea = false;
//          // System.out.println("enemy");
//        } else {
//          if (population[i][depth] < soCopy.getAvailableActions().size()) {
//            soCopy.advance(soCopy.getAction(population[i][depth]));
//            // System.out.println("RHEA");
//            lastTurnRhea = true;
//          }
//        }
//      }
//      score = Math.pow(DEPTH_WEIGHT, depth) * ((soCopy.getReward(whoAmI, soCopy.isFinalRewardGame())));
//      normalisedScore += score;
//
//      scoreList.add(new Pair<>(i, score));
//
//    }
//
//    scoreList = insertionSort(scoreList);
//    System.out.println("Scoremap Generation 0: " + scoreList);
//
//
//    for (int g = 1; g <= GENERATIONS; g++) {
//      switch (GEN_OP) {
//        case "CROSSOVER":
//          population = crossover(scoreList, population);
//          break;
//        case "MUTATION":
//          population = mutation(scoreList, population, so);
//          break;
//        case "BOTH":
//          population = crossover(scoreList, population);
//          population = mutation(scoreList, population, so);
//          break;
//
//        default:
//          System.out.println("No valid Selection Operator selected");
//          break;
//      }
//      normalisedScore = 0.0;
//      scoreList = new ArrayList<>();
//      for (int i = 0; i < POP_SIZE; i++) {
//        lastTurnRhea = true;
//        StateObservation soCopy = so.copy();
//        int depth = 0;
//        for (; depth < SIM_DEPTH; depth++) {
//          if (soCopy.isGameOver()) {
//            break;
//          }
//          if (isMultiplayerGame && lastTurnRhea && RANDOM_OPPONENT_TURN) {
//            soCopy.advance(soCopy.getAvailableActions().get(rand.nextInt(soCopy.getAvailableActions().size())));
//            lastTurnRhea = false;
//            // System.out.println("enemy");
//          } else {
//            if (population[i][depth] < soCopy.getAvailableActions().size()) {
//              soCopy.advance(soCopy.getAction(population[i][depth]));
//              // System.out.println("RHEA");
//              lastTurnRhea = true;
//            }
//          }
//        }
//        score = Math.pow(DEPTH_WEIGHT, depth) * ((soCopy.getReward(whoAmI, soCopy.isFinalRewardGame())));
//        // System.out.println(soCopy.getReward(whoAmI, soCopy.isFinalRewardGame()));
//        normalisedScore += score;
//        scoreList.add(new Pair<>(i, score));
//      }
//      scoreList = insertionSort(scoreList);
//      System.out.println("Scoremap Generation " + g + ": " + scoreList);
//    }
//
//    if (so.isFinalRewardGame()) {
//      normalisedScore /= POP_SIZE;
//    } else {
//      normalisedScore = scoreList.get(0).getValue();
//    }

    // System.out.println("Normalized Score: " + normalisedScore);
    return vtable;
  }

  private ArrayList<Pair<Integer, Double>> insertionSort(ArrayList<Pair<Integer, Double>> sortedScores) {

    for (int i = 0; i < sortedScores.size(); ++i) {
      Pair<Integer, Double> key = sortedScores.get(i);
      int j = i - 1;
      while (j >= 0 && sortedScores.get(j).getValue() < key.getValue()) {
        sortedScores.set(j + 1, sortedScores.get(j));
        j = j - 1;
      }
      sortedScores.set(j + 1, key);
    }

    return sortedScores;
  }

  private int[][] mutation(ArrayList<Pair<Integer, Double>> sortedScores, int[][] population, StateObservation so) {
    int[][] newPopulation = new int[POP_SIZE][SIM_DEPTH];
    int allActionSize = so.getAllAvailableActions().size();
    //elitism promotion
    if (GEN_OP.equals("BOTH")) {
      newPopulation[0] = population[0];
    } else {
      newPopulation[0] = population[sortedScores.get(0).getKey()];
    }


    for (int i = 1; i < sortedScores.size(); i++) {
      if (GEN_OP.equals("BOTH")) {
        newPopulation[i] = population[i];
      } else {
        newPopulation[i] = population[sortedScores.get(i).getKey()];
      }

      switch (MUTATION_TYPE) {
        case "UNIFORM":
          for (int j = 0; j < SIM_DEPTH; j++) {
            if (rand.nextInt(SIM_DEPTH + 1) == 0) {
              newPopulation[i][j] = rand.nextInt(allActionSize);
            }
          }
          break;
      }
    }

    return newPopulation;
  }

  private int[][] crossover(ArrayList<Pair<Integer, Double>> sortedScores, int[][] population) {
    int[] parents;
    int[][] newPopulation = new int[POP_SIZE][SIM_DEPTH];

    //elitism promotion
    newPopulation[0] = population[sortedScores.get(0).getKey()];
    for (int i = 1; i < sortedScores.size(); i++) {
      parents = parentSelection(sortedScores);

      // System.out.println("Parents Selected: " + parents[0] + ", " + parents[1]);
      switch (CROSSOVER_TYPE) {
        case "UNIFORM":
          for (int j = 0; j < SIM_DEPTH; j++) {
            if (j % 2 == 0) {
              newPopulation[i][j] = population[parents[0]][j];
            } else {
              newPopulation[i][j] = population[parents[1]][j];
            }
          }
          break;
        case "1_POINT":

          break;
      }
    }

    return newPopulation;
  }

  private int[] parentSelection(ArrayList<Pair<Integer, Double>> sortedScores) {
    double weight = 0;
    int[] parents = new int[2];
    switch (SELECTION_TYPE) {
      case "ROULETTE":
        ArrayList<Pair<Integer, Double>> normalizedScores = new ArrayList<>();
        for (Pair<Integer, Double> score : sortedScores) {
          normalizedScores.add(new Pair<>(score.getKey(), score.getValue() + 1));
          weight += score.getValue() + 1;
        }
        ArrayList<Pair<Integer, Double>> tempscores = new ArrayList<>(normalizedScores);
        double tempweight = weight;
        for (int k = 0; k < 2; k++) {
          double chance = 0;
          double random = rand.nextDouble();
          for (int i = tempscores.size() - 1; i >= 0; i--) {
            chance += tempscores.get(i).getValue() / tempweight;
            if (random < chance) {
              parents[k] = tempscores.get(i).getKey();
              tempweight = tempweight - tempscores.get(i).getValue();
              tempscores.remove(i);
              i = -1;
            }
          }
        }
        break;
    }

    return parents;
  }


  private double simulate(int whoAmI, StateObservation so, int[][] population) {
    double normalisedScore = 0.0, score;
    ArrayList<Pair<Integer, Double>> scoreList = new ArrayList<>();
    boolean isMultiplayerGame = so.getNumPlayers() > 1;
    boolean lastTurnRhea;


    for (int i = 0; i < POP_SIZE; i++) {
      lastTurnRhea = true;
      StateObservation soCopy = so.copy();
      int depth = 0;
      for (; depth < SIM_DEPTH; depth++) {
        if (soCopy.isGameOver()) {
          break;
        }
        if (isMultiplayerGame && lastTurnRhea && RANDOM_OPPONENT_TURN) {
          soCopy.advance(soCopy.getAvailableActions().get(rand.nextInt(soCopy.getAvailableActions().size())));
          lastTurnRhea = false;
          // System.out.println("enemy");
        } else {
          if (population[i][depth] < soCopy.getAvailableActions().size()) {
            soCopy.advance(soCopy.getAction(population[i][depth]));
            // System.out.println("RHEA");
            lastTurnRhea = true;
          }
        }
      }
      score = Math.pow(DEPTH_WEIGHT, depth) * ((soCopy.getReward(whoAmI, soCopy.isFinalRewardGame())));
      normalisedScore += score;

      scoreList.add(new Pair<>(i, score));

    }

    scoreList = insertionSort(scoreList);
    System.out.println("Scoremap Generation 0: " + scoreList);


    for (int g = 1; g <= GENERATIONS; g++) {
      switch (GEN_OP) {
        case "CROSSOVER":
          population = crossover(scoreList, population);
          break;
        case "MUTATION":
          population = mutation(scoreList, population, so);
          break;
        case "BOTH":
          population = crossover(scoreList, population);
          population = mutation(scoreList, population, so);
          break;

        default:
          System.out.println("No valid Selection Operator selected");
          break;
      }
      normalisedScore = 0.0;
      scoreList = new ArrayList<>();
      for (int i = 0; i < POP_SIZE; i++) {
        lastTurnRhea = true;
        StateObservation soCopy = so.copy();
        int depth = 0;
        for (; depth < SIM_DEPTH; depth++) {
          if (soCopy.isGameOver()) {
            break;
          }
          if (isMultiplayerGame && lastTurnRhea && RANDOM_OPPONENT_TURN) {
            soCopy.advance(soCopy.getAvailableActions().get(rand.nextInt(soCopy.getAvailableActions().size())));
            lastTurnRhea = false;
            // System.out.println("enemy");
          } else {
            if (population[i][depth] < soCopy.getAvailableActions().size()) {
              soCopy.advance(soCopy.getAction(population[i][depth]));
              // System.out.println("RHEA");
              lastTurnRhea = true;
            }
          }
        }
        score = Math.pow(DEPTH_WEIGHT, depth) * ((soCopy.getReward(whoAmI, soCopy.isFinalRewardGame())));
        // System.out.println(soCopy.getReward(whoAmI, soCopy.isFinalRewardGame()));
        normalisedScore += score;
        scoreList.add(new Pair<>(i, score));
      }
      scoreList = insertionSort(scoreList);
      System.out.println("Scoremap Generation " + g + ": " + scoreList);
    }

    if (so.isFinalRewardGame()) {
      normalisedScore /= POP_SIZE;
    } else {
      normalisedScore = scoreList.get(0).getValue();
    }

    // System.out.println("Normalized Score: " + normalisedScore);
    return normalisedScore;
  }

  private int[][][] initPop(StateObservation so, int[][][] population) {
    int availableActionSize = so.getAvailableActions().size();
    int allActionSize = so.getAllAvailableActions().size();
    for (int i = 0; i < availableActionSize; i++) {
      for (int j = 0; j < POP_SIZE; j++) {
        for (int k = 0; k < SIM_DEPTH; k++) {
          population[i][j][k] = rand.nextInt(allActionSize);
        }
      }
    }
    return population;
  }

}
