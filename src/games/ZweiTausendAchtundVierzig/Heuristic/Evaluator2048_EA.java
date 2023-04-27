package games.ZweiTausendAchtundVierzig.Heuristic;

import controllers.PlayAgent;
import games.EvalResult;
import games.Evaluator;
import games.GameBoard;
import games.ZweiTausendAchtundVierzig.Arena2048;
import games.ZweiTausendAchtundVierzig.ConfigEvaluator;
import games.ZweiTausendAchtundVierzig.Evaluator2048;
import games.ZweiTausendAchtundVierzig.Evaluator2048_BoardPositions;
import tools.cmaes.CMAEvolutionStrategy;

/**
 * Evolutionary algorithm (EA) evaluator for 2048: 
 * <p>
 * Note that the mode-selection for 2048 evaluators is done in 
 * {@link Arena2048#makeEvaluator(PlayAgent, GameBoard, int, int, int) Arena[Train]2048.makeEvaluator(...)}.
 * <p>
 * Created by Johannes Kutsch, TH Koeln, 2016-12.
 * 
 * @see Evaluator2048
 * @see Evaluator2048_BoardPositions
 */
public class Evaluator2048_EA extends Evaluator {
    private HeuristicSettings2048 fitfun = new HeuristicSettings2048();
    private CMAEvolutionStrategy cma = new CMAEvolutionStrategy();

    public Evaluator2048_EA(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int verbose) {
        super(e_PlayAgent, gb, 2, stopEval, verbose);
    }

    @Override
    protected EvalResult evalAgent(PlayAgent pa) {
    	m_PlayAgent = pa;
        cma.setDimension(15);
        cma.setInitialX(0.5);
        cma.setInitialStandardDeviation(0.2);

        cma.options.stopMaxFunEvals = 240;

        double[] fitness = cma.init();

        double[][] sam = cma.samplePopulation();

   /*     System.out.println("sample population: ");
        for(int i = 0; i < sam.length; i++) {
            for(int j = 0; j < sam[i].length; j++) {
                System.out.print(sam[i][j] + "  |  ");
            }
            System.out.print("\n\n");
        }*/

        while(cma.stopConditions.getNumber() == 0) {

            // core iteration step
            double[][] pop = cma.samplePopulation(); // get a new population of solutions
            for (int i = 0; i < pop.length; ++i) {    // for each candidate solution i
                System.out.println("\nStarting Evaluation: " + (cma.getCountEval() + i));
                while (!fitfun.isFeasible(pop[i]))   //    test whether solution is feasible,
                    pop[i] = cma.resampleSingle(i);  //       re-sample solution until it is feasible
                fitness[i] = fitfun.valueOf(pop[i]); //    compute fitness value, where fitfun
            }                                         //    is the function to be minimized
            cma.updateDistribution(fitness);         // pass fitness array to update search distribution

            // output to console and files
            cma.writeToDefaultFiles();
            int outmod = 150;
            if (cma.getCountIter() % (15 * outmod) == 1)
                cma.printlnAnnotation(); // might write file as well
            if (cma.getCountIter() % outmod == 1)
                cma.println();


            lastResult = (1-cma.getBestFunctionValue())*100000;
            cma.println("\nbest fitness " + lastResult
                    + " at evaluation " + cma.getBestEvaluationNumber());

            System.out.println("best settings are: ");
            double[] best = cma.getBestX();
            for (int i = 0; i < best.length; i++) {
                System.out.println(i + ": " + best[i]);
            }
        }

        lastResult = (1-cma.getBestFunctionValue())*100000;

        cma.writeToDefaultFiles(1);
        cma.println();
        cma.println("\nTerminated due to");
        for (String s : cma.stopConditions.getMessages())
            cma.println("  " + s);
        cma.println("best fitness " + lastResult
                + " at evaluation " + cma.getBestEvaluationNumber());

        System.out.println("best settings are: ");
        double[] best = cma.getBestX();
        for (int i = 0; i < best.length; i++) {
            System.out.println(i + ": " + best[i]);
        }

        return new EvalResult();
    }

 	// --- implemented by Evaluator ---
//    @Override
//    public double getLastResult() {
//        return lastResult;
//    }

    public String getMsg() {
        return "";
    }

 	// --- implemented by Evaluator ---
//    @Override
//    public boolean isAvailableMode(int mode) {
//        switch (mode) {
//        	case -1: 
//        	case  0:
//            case  1:
//            case  2:
//                return true;
//            default:
//                return false;
//        }
//    }

    @Override
    public int[] getAvailableModes() {
        return new int[]{-1, 0, 1, 2};
    }

    @Override
    public int getQuickEvalMode() {
        return ConfigEvaluator.DEFAULTEVALUATOR;
    }

    @Override
    public int getTrainEvalMode() {
        return 0;
    }

//    @Override
//    public int getMultiTrainEvalMode() {
//        return 0;
//    }

    @Override
    public String getPrintString() {
        return "success rate";
    }

	@Override
	public String getTooltipString() {
		// use "<html> ... <br> ... </html>" to get multi-line tooltip text
		return "<html>-1: none<br>"
				+ "0: avg score from 50 games<br>"
				+ "1: Evaluator2048_BoardPositions<br>"
				+ "2: Evaluator2048_EA<br>"
				+ "</html>";
	}

    @Override
    public String getPlotTitle() {
        return "success";
    }
}
