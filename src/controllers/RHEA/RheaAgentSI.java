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

public class RheaAgentSI extends AgentBase implements PlayAgent, Serializable {

  private static boolean debug = false;

  private static final String GEN_OP = "BOTH"; // CROSSOVER, MUTATION, BOTH
  private static final String SELECTION_TYPE = "ROULETTE"; //ROULETTE, RANK, TOURNAMENT
  private static final String CROSSOVER_TYPE = "POSITION_BASED"; // Recombination of permutations (ORDER_BASED)
  private static final String MUTATION_TYPE = "UNIFORM_PERMUTATION"; //Mutation type

  private static final int POP_SIZE = 50;
  private static final int SIM_DEPTH = 25;
  private static final int GENERATIONS = 50;
  private static final int AGGRESSIVENESS = 6; // basically the turns (of all players) the Bot will look in advancement to prevent an opponent from winning
  private static final double FIRST_ACTION_WEIGHT = 0.0001;
  private static final boolean ELITISM_PROMOTION = true;
  private static Random rand;
  private static boolean isElitistChosen = false;

  private ArrayList<Types.ACTIONS>[] POP_ACTIONS;


  private boolean isPopInitialized;


  public RheaAgentSI(String name, StateObservation so) {
    super(name);
    super.setAgentState(AgentState.TRAINED);
    rand = new Random();
    isPopInitialized = false;
  }

  @Override
  public Types.ACTIONS_VT getNextAction2(StateObservation so, boolean random, boolean silent) {

    StateObservation soCopy = so.copy();
    initPop(so);
    Pair<Integer, Double> bestAct = bestAction(so);
    List<Types.ACTIONS> actions = so.getAvailableActions();

    double[] VTable, vtable;
    vtable = new double[actions.size()];
    VTable = new double[actions.size() + 1];  // only for inner communication into method act()
    double bestScore = VTable[actions.size()];
    ScoreTuple scBest = new ScoreTuple(so, bestScore);

    Types.ACTIONS_VT actBestVT = new Types.ACTIONS_VT(POP_ACTIONS[bestAct.getKey()].get(0).toInt(), false, vtable, bestScore, scBest);
    soCopy.advance(POP_ACTIONS[bestAct.getKey()].get(0));
    System.out.println("Playing Turn with: " + POP_ACTIONS[bestAct.getKey()].get(0).toInt() + " from Individual: " + bestAct.getKey() + " with a Score of: " + bestAct.getValue() + " Board: " + soCopy.stringDescr());
    return actBestVT;
  }

  private Pair<Integer, Double> bestAction(StateObservation so) {

    double score;
    ArrayList<Pair<Integer, Double>> sortedScores = new ArrayList<>();

    for (int j = 0; j < GENERATIONS; j++) {
      sortedScores = new ArrayList<>();
      for (int i = 0; i < POP_SIZE; i++) {
        score = simulate(so, i, j);
        sortedScores.add(new Pair<>(i, score));
      }
      sortedScores = rankActions(sortedScores);

      //TODO no competition between multiple best cases yet
      sortedScores = insertionSort(sortedScores);
      isElitistChosen = false;
      if (debug) System.out.println("Generation: " + j + " Scoremap: " + sortedScores);
      switch (GEN_OP) {
        case "CROSSOVER":
          crossover(sortedScores);
          break;
        case "MUTATION":
          mutation(sortedScores);
          break;
        case "BOTH":
          crossover(sortedScores);
          mutation(sortedScores);
          break;

        default:
          System.out.println("No valid Selection Operator selected");
          break;
      }


    }

    sortedScores = insertionSort(sortedScores);
    return sortedScores.get(0);
  }

  private ArrayList<Pair<Integer, Double>> rankActions(ArrayList<Pair<Integer, Double>> sortedScores) {
    Map<Types.ACTIONS, Integer> countedActs = new HashMap<>();
    ArrayList<Pair<Integer, Double>> rankActions = new ArrayList<>();
    for (Pair<Integer, Double> pair : sortedScores) {
      if (!countedActs.containsKey(POP_ACTIONS[pair.getKey()].get(0))) {
        countedActs.put(POP_ACTIONS[pair.getKey()].get(0), 1);
      } else {
        countedActs.replace(POP_ACTIONS[pair.getKey()].get(0), countedActs.get(POP_ACTIONS[pair.getKey()].get(0)) + 1);
      }
    }
    for (Pair<Integer, Double> pair : sortedScores) {
      rankActions.add(new Pair<Integer, Double>(pair.getKey(), pair.getValue() + (countedActs.get(POP_ACTIONS[pair.getKey()].get(0)) * FIRST_ACTION_WEIGHT)));
    }
    if (debug) System.out.println(countedActs);
    if (debug) System.out.println(rankActions);

    return rankActions;
  }

  private ArrayList<Pair<Integer, Double>> insertionSort(ArrayList<Pair<Integer, Double>> sortedScores) {
    for (int i = 1; i < sortedScores.size(); ++i) {
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

  private void mutation(ArrayList<Pair<Integer, Double>> sortedScores) {
    ArrayList<Types.ACTIONS>[] mutationActions = new ArrayList[POP_SIZE];
    //elitism promotion
    if (!isElitistChosen) {
      mutationActions[0] = new ArrayList<>(POP_ACTIONS[sortedScores.get(0).getKey()]);
    } else {
      mutationActions[0] = new ArrayList<>(POP_ACTIONS[0]);
    }

    ArrayList<Pair<Integer, Integer>> changeActions;

    for (int i = 1; i < sortedScores.size(); i++) {
      changeActions = new ArrayList<>();
      switch (MUTATION_TYPE) {
        case "UNIFORM_PERMUTATION":
          mutationActions[i] = new ArrayList<>(POP_ACTIONS[i]);
          for (int j = 0; j < mutationActions[i].size(); j++) {
            if (rand.nextInt(mutationActions[i].size()) == 0 && mutationActions[i].size() > 1) {
              int changeSpot;
              do {
                changeSpot = rand.nextInt(mutationActions[i].size());
              } while (changeSpot == j);
              changeActions.add(new Pair<>(j, changeSpot));
            }
          }
          if (debug) System.out.println("ActionSwap A <-> B: " + changeActions);
          if (debug) System.out.println("Action before Swap: " + mutationActions[i]);
          for (Pair<Integer, Integer> spots : changeActions) {
            Types.ACTIONS tempA = mutationActions[i].get(spots.getFirst());
            Types.ACTIONS tempB = mutationActions[i].get(spots.getSecond());
            mutationActions[i].set(spots.getSecond(), tempA);
            mutationActions[i].set(spots.getFirst(), tempB);
          }
          if (debug) System.out.println("Action after Swap: " + mutationActions[i]);

          break;
      }
    }

    POP_ACTIONS = mutationActions;

  }

  private void crossover(ArrayList<Pair<Integer, Double>> sortedScores) {
    ArrayList<Pair<Integer, Double>> parents;
    ArrayList<Types.ACTIONS>[] crossoverActions = new ArrayList[POP_SIZE];

    //elitism promotion
    crossoverActions[0] = new ArrayList<>(POP_ACTIONS[sortedScores.get(0).getKey()]);
    isElitistChosen = true;
    for (int i = 1; i < sortedScores.size(); i++) {
      parents = parentSelection(sortedScores);

      if (debug) System.out.println("Parents Selected: " + parents);
      switch (CROSSOVER_TYPE) {
        case "POSITION_BASED":
          ArrayList<Types.ACTIONS> changeActions = new ArrayList<>();
          crossoverActions[i] = new ArrayList<>(POP_ACTIONS[parents.get(0).getKey()]);
          int[] s = new int[POP_ACTIONS[parents.get(0).getKey()].size()];
          int k = 0;
          if (debug) System.out.println("Selection of P1 Actions: ");
          for (Types.ACTIONS action : crossoverActions[i]) {
            if ((s[k] = rand.nextInt(2)) == 0) {
              changeActions.add(action);
              crossoverActions[i].set(k, null);
            }
            if (debug) System.out.print(s[k] + ", ");
            k++;
          }
          if (debug) System.out.println();
          if (debug) System.out.println("Parent 1: " + POP_ACTIONS[parents.get(0).getKey()]);
          if (debug) System.out.println("Parent 2: " + POP_ACTIONS[parents.get(1).getKey()]);
          if (debug) System.out.println("Crossover Parent 1: " + crossoverActions[i]);

          for (int l = 0; l < crossoverActions[i].size(); l++) {
            if (crossoverActions[i].get(l) == null) {
              for (int j = 0; j < POP_ACTIONS[parents.get(0).getKey()].size(); j++) {
                if (changeActions.contains(POP_ACTIONS[parents.get(1).getKey()].get(j))) {
                  crossoverActions[i].set(l, POP_ACTIONS[parents.get(1).getKey()].get(j));
                  changeActions.remove(POP_ACTIONS[parents.get(1).getKey()].get(j));
                  j = POP_ACTIONS[parents.get(0).getKey()].size();
                }
              }
            }
          }

          if (debug) System.out.println("Crossover Parent 2: " + crossoverActions[i]);

          break;
      }
    }
    POP_ACTIONS = crossoverActions;
  }

  private ArrayList<Pair<Integer, Double>> parentSelection(ArrayList<Pair<Integer, Double>> sortedScores) {
    double weight = 0;
    int[] parents = new int[2];
    ArrayList<Pair<Integer, Double>> returnParents = new ArrayList<>();
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
        for (Pair<Integer, Double> pairs : sortedScores) {
          if (pairs.getKey() == parents[0] || pairs.getKey() == parents[1]) {
            returnParents.add(pairs);
          }
        }
        break;
    }

    return returnParents;
  }

  private double simulate(StateObservation so, int indiv, int gen) {

    StateObservation soCopy1 = so.copy();
    if (debug) System.out.println("Individual " + indiv + " Action Sequence = " + POP_ACTIONS[indiv]);
    int depth = 0;
    for (; depth < SIM_DEPTH; depth++) {
      if (soCopy1.getAvailableActions().contains(POP_ACTIONS[indiv].get(depth))) {
        if (debug) {
          System.out.println("Use Action: " + POP_ACTIONS[indiv].get(depth));
          System.out.println("Available Actions = " + soCopy1.getAvailableActions());
          System.out.println("Advance State");
        }
        soCopy1.advance(POP_ACTIONS[indiv].get(depth));
      }

      if (soCopy1.isGameOver()) {

        if (debug) {
          System.out.println("Game Over");
          System.out.println(soCopy1.getGameScoreTuple());
        }
        break;
      }
    }
    double score;
    score = Math.pow(0.95, depth) * soCopy1.getReward(so.getPlayer(), true);

    if (score < 0) {
      score *= -1;
      score += (0.06 * AGGRESSIVENESS);
      if (GENERATIONS - 1 == gen) {
        Types.ACTIONS temp1 = POP_ACTIONS[indiv].get(0);
        Types.ACTIONS temp2 = POP_ACTIONS[indiv].get(depth);
        POP_ACTIONS[indiv].set(0, temp2);
        POP_ACTIONS[indiv].set(depth, temp1);
      }
    } else if (score == 1) {
      score = 10;
    }

    if (!so.isDeterministicGame() && soCopy1.isGameOver()) {
      score = -10;
    }
    if (debug) System.out.println("Player Score with depth : " + score);

    return score;
  }

  private void initPop(StateObservation so) {
    //ArrayList to shuffle actions  //TODO better implementation
    ArrayList<Types.ACTIONS> actList = so.getAvailableActions();
    //getAllAvailableActions() untersuchen - OoB problem lösen
    if (POP_ACTIONS != null && so.getNumAvailableActions() > POP_ACTIONS[0].size()) {
      if (debug) System.out.println("!!!REINITIALIZATION!!! due to corrupt state");
      isPopInitialized = false;
    }
    if (isPopInitialized) {
      if (so.isDeterministicGame()) {
        for (int i = 0; i < POP_SIZE; i++) {
          for (int j = 0; j < POP_ACTIONS[i].size(); j++) {
            if (!actList.contains(POP_ACTIONS[i].get(j))) {
              if (debug) System.out.println("REMOVE HAPPENING: " + POP_ACTIONS[i].get(j));
              POP_ACTIONS[i].remove(j--);
              if (debug) System.out.println(POP_ACTIONS[i]);
            }
          }
        }
      }
    } else {
      if (debug) System.out.println(so.isDeterministicGame());
      POP_ACTIONS = new ArrayList[POP_SIZE];
      if (so.isDeterministicGame()) {
        for (int i = 0; i < POP_SIZE; i++) {
          Collections.shuffle(actList);
          POP_ACTIONS[i] = new ArrayList<>(actList);
        }
      } else {
        if (debug) System.out.print("Random Action Index = ");
        for (int i = 0; i < POP_SIZE; i++) {
          POP_ACTIONS[i] = new ArrayList<>();
          for (int j = 0; j < SIM_DEPTH; j++) {
            if (debug) System.out.print(rand.nextInt(actList.size()) + ", ");
            POP_ACTIONS[i].add(actList.get(rand.nextInt(actList.size())));
          }
        }

      }
      if (debug) System.out.println("Pop Initialized");
      isPopInitialized = true;
    }
  }
}
