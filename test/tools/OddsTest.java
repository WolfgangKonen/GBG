package tools;

import org.junit.Test;
import java.util.Random;

public class OddsTest {

    /**
     * Stochastic simulation of the "Last-6 problem" and comparison with odds strategy.
     * <p>
     * A dice is tossed S times. A gambler sees the sequence step-by-step. He has to predict the appearance of the last
     * '6' at the time when it appears. He wins if he announces correctly the index of the last '6' and loses otherwise.
     * (He loses also if the sequence has no '6' at all.)
     * <p>
     * Simulate {@code trials} sequences, calculate the true index {@code trueI} of the last '6' and the estimated index
     * {@code foundI} according to the odds algorithm. Is the success rate (number of sequences with {@code trueI==foundI}
     * equal to the theoretic expectations?
     */
    @Test
    public void oddsTestLast6() {
        Random rand = new Random(System.currentTimeMillis());

        int trials=1000000;
        int S=12;   // sequence length
        int[] seq = new int[S];
        double[] p = new double[S];     // p[i]: probability of opportunity in step i
        double[] q = new double[S];     // q[i]: probability of NOT opportunity in step i
        double[] r = new double[S];     // r[i]: the odds of step i
        double R=0;
        double Q=1;
        double W;
        int stop=-1;
        int success=0;
        int trueI,foundI;
        boolean tfound;
        boolean silent=true;

        for (int i=S-1; i>=0; i--) {
            p[i]=1.0/6;                 // probability of opportunity, i. e. tossing a '6' in step i
            q[i]=1-p[i];
            r[i]=p[i]/q[i];
            R += r[i];
            Q *= q[i];
            if (R>=1) {
                stop=i;                 // the optimal stopping index
                break; // out of for
            }
        }
        W = R*Q;

        for (int t=0; t<trials; t++) {
            trueI=-1; foundI=-1;

            // build the sequence and find the true optimal index trueI
            tfound=false;
            for (int i=S-1; i>=0; i--) {
                seq[i] = rand.nextInt(6)+1;
                if (!tfound && seq[i]==6) {
                    trueI = i;          // true index of last '6' in sequence
                    tfound=true;
                }
            }

            // find the optimal index according to odds algorithm
            for (int i=stop; i<S; i++) {
                if (seq[i]==6) {
                    foundI = i;
                    break;
                }
            }

            //if (trueI==foundI) success++;         // wrong, this would count sequences w/o '6' as success --> gives
            // too high empiric success probability (51%)
            if (trueI==foundI && trueI!=-1) success++;      // correct, if a sequence has no '6' (trueI==-1), then count
            // it as failure, because the gambler would have had to say 'no 6'
            // prior to seeing the first number of the sequence, which he didN't.
            // This choice gives the correct empiric success probability near 40%.

            if (!silent) {
                for (int i=0; i<S; i++) System.out.print(seq[i]+" ");
                System.out.println(" trueI="+trueI+" foundI="+foundI);
            }

        }

        double sucRate = success/(double) trials;
        System.out.println("Stop Index = "+stop);
        System.out.println("Empirical success rate = "+sucRate);
        System.out.println("Success probability = "+W);
        System.out.println("Delta = " + Math.abs(W-sucRate));  // with trials=1000000, Delta usually < 1e-3
    }

    /**
     * Stochastic simulation of the "secretary problem" and comparison with odds strategy.
     * <p>
     * Select the best candidate out of S rankable candidates at the time you see him. That is you know the value of the
     * candidate and all before him, but not the value of all candidates still to come.
     * <p>
     * Simulate {@code trials} sequences, calculate the true index {@code trueI} of the best candidate and the estimated index
     * {@code foundI} according to the odds algorithm. Is the success rate (number of sequences with {@code trueI==foundI}
     * equal to the theoretic expectations?
     */
    @Test
    public void oddsTestSecretary() {
        Random rand = new Random(System.currentTimeMillis());

        int trials=1000000;
        int S=9;   // sequence length
        int[] seq = new int[S];
        double[] p = new double[S];     // p[i]: probability of opportunity in step i
        double[] q = new double[S];     // q[i]: probability of NOT opportunity in step i
        double[] r = new double[S];     // r[i]: the odds of step i
        double R=0;
        double Q=1;
        double W;
        int stop=-1, stop_1_e;
        int success=0;
        int trueI,foundI;
        boolean silent=true;

        // find the optimal stop index according to odds
        for (int i=S-1; i>=0; i--) {
            p[i]=1.0/i;                 // probability of opportunity, i. e. that candidate i is better than all before
            q[i]=1-p[i];
            r[i]=p[i]/q[i];
            R += r[i];
            Q *= q[i];
            if (R>=1) {
                stop=i;
                break; // out of for
            }
        }
        W = R*Q;

        // alternative stopping rule due to the (1/e)-law --> results in the same stop index
        stop_1_e = (int)Math.floor(S/Math.exp(1.0))+1;

        for (int t=0; t<trials; t++) {
            trueI=-1; foundI=-1;

            // build the sequence and find the optimal candidate
            int max=-1;
            for (int i=S-1; i>=0; i--) {
                seq[i] = rand.nextInt(100)+1;
                if (seq[i]>=max) {
                    max = seq[i];
                    trueI=i;            // true index of the max (if there are many such indices, select the first one)
                }
            }

            // find the optimal candidate according to odds algorithm
            int up_to_s_max=-1;
            for (int i=0; i<stop-1; i++) {
                if (seq[i] >= up_to_s_max) {
                    up_to_s_max = seq[i];           // the max of the first stop-1 candidates
                }
            }
            for (int i=stop-1; i<S; i++) {
                if (seq[i] > up_to_s_max) {
                    foundI=i;
                    break;      // out of for
                }
            }

            if (trueI==foundI ) success++;

            if (!silent) {
                for (int i=0; i<S; i++) System.out.print(seq[i]+" ");
                System.out.println(" trueI="+trueI+" foundI="+foundI);
            }

        }

        double sucRate = success/(double) trials;
        System.out.println("Stop index = "+stop+",   Stop index 1/e = "+stop_1_e);
        System.out.println("Empirical success rate = "+sucRate);
        System.out.println("Success probability = "+W);
        System.out.println("Delta = " + Math.abs(W-sucRate));  // with trials=1000000, Delta usually < 0.005 (for S=9)
    }
}
