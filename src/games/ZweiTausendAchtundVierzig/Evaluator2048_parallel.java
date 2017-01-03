package games.ZweiTausendAchtundVierzig;

import controllers.PlayAgent;
import games.Evaluator;
import games.GameBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Johannes on 02.12.2016.
 */
public class Evaluator2048_parallel extends Evaluator {
    private double averageScore;
    private int minScore = Integer.MAX_VALUE;
    private int maxScore = 0;

    public Evaluator2048_parallel(PlayAgent e_PlayAgent, GameBoard gb, int stopEval, int mode, int verbose) {
        super(e_PlayAgent, stopEval, verbose);
    }

    @Override
    protected boolean eval_Agent() {

        System.out.println("Starting evaluation of " + Config.NUMBEREVALUATIONS + " games, this may take a while...");

        List<Input> inputs = new ArrayList<Input>();
        for(int i = 0; i < Config.NUMBEREVALUATIONS; i++) {
            inputs.add(new Input(new StateObserver2048(), m_PlayAgent));
        }

        List<Output> outputs = new ArrayList<Output>();
        try {
            outputs = playGames(inputs);
        } catch (InterruptedException e) {
            System.err.println("IndexOutOfBoundsException: " + e);
        } catch (ExecutionException e) {
            System.err.println("Caught IOException: " + e);
        }

        for (Output output : outputs) {
            averageScore += output.score;
            if(output.score < minScore) {
                minScore = output.score;
            }
            if(output.score > maxScore) {
                maxScore = output.score;
            }
        }

        averageScore/=Config.NUMBEREVALUATIONS;

        return true;
    }

    @Override
    public double getLastResult() {
        return averageScore;
    }

    @Override
    public String getMsg() {
        return "\nAverage Score is: " + averageScore +
                "\nLowest Score is: " + minScore +
                "\nHighest Score is: " + maxScore +
                "\n\n";
    }

    //http://stackoverflow.com/questions/5686200/parallelizing-a-for-loop
    private List<Output> playGames(List<Input> inputs) throws InterruptedException, ExecutionException {

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(threads);

        List<Future<Output>> futures = new ArrayList<Future<Output>>();
        for (final Input input : inputs) {
            Callable<Output> callable = new Callable<Output>() {
                public Output call() throws Exception {
                    while (!input.so.isGameOver()) {
                        input.so.advance(input.agent.getNextAction(input.so, false, new double[input.so.getNumAvailableActions() + 1], true));
                    }
                    Output output = new Output(input.so.score);
                    System.out.println("Finished game with Score: " + output.score);
                    return output;
                }
            };
            futures.add(service.submit(callable));
        }

        service.shutdown();

        List<Output> outputs = new ArrayList<Output>();
        for (Future<Output> future : futures) {
            outputs.add(future.get());
        }
        return outputs;
    }


}

class Output {
    public int score;

    public Output(int score) {
        this.score = score;
    }
}

class Input {
    public StateObserver2048 so;
    public PlayAgent agent;

    public Input(StateObserver2048 so, PlayAgent agent) {
        this.so = so;
        this.agent = agent;
    }
}

