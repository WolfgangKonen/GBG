package tools;

import org.junit.Test;
import java.io.*;
import org.apache.commons.math3.util.CombinatoricsUtils;

import static java.lang.Math.max;

/**
 * This class realizes different solutions for PE problem 888 (1249 Nim).
 *   - calc_big_piles:  solution WK
 *   - calc_big_wim59:  solution user wim59
 *
 * The Java implementation solves the 'big' S(12491249, 1249) = 227429102 (mod p) in only 793 sec = 13.2 min.
 * Speedup factor 127 (!!) compared to Python's 28 hours.
 */
public class Nim1249Test {
    //int N=124; //12491249;
    //int m=9; //1249;
    long modu = 912491249;
    long[][] MQ;
    static int[] gval_big = new int[11382];
    static long x, y;

    void load_gval_big() {
        BufferedReader fread;
        String line;
        String fname = "test/tools/P888_gval_big.csv";
        try {
            fread = new BufferedReader(new FileReader(fname));
            fread.readLine();    // skip first line (header)
            int k = 0;
            while ((line = fread.readLine()) != null) {
                try {
                    gval_big[k++] = Integer.parseInt(line);
                } catch (NumberFormatException e) {}
            } // while
            fread.close();
            // --- only as check needed: ---
            //int[] index = new int[] {0, 5, 11382-1};
            //for (int i2 : index)
            //    System.out.println(i2 +": " + gval_big[i2]);
        } catch (IOException e) {
            System.out.println("Error reading file: " + fname);
            System.out.println(e.getMessage());
        }
    }

    int gval(int n) {
        if (n >= 11382)
            n = (n-11382) % 11060 + 322;
        return gval_big[n];
    }

    public long calc_big_piles(int N, int m) {
        int verbose = 1;
        int runs = 10;
        long starttime = System.currentTimeMillis();

        load_gval_big();

        for (int r=0; r<runs; r++) {
            MQ = new long[16][m+1];

            long fstart = System.currentTimeMillis();
            for (int f=N; f>0; f--) {
                if (f % 100000 == 0) {
                    long ftime = System.currentTimeMillis();
                    System.out.println("Starting with f = " + f + ", time= " + (ftime - fstart) / 1000. + " ...");
                    fstart = ftime;
                }
                int gval_f = gval(f);
                MQ[gval_f][1] +=1;
                for (int q=2; q<=m; q++) {
                    for (int H=0; H<16; H++) {
                        MQ[H][q] = (MQ[H][q] + MQ[H ^ gval_f][q - 1]) % modu;
                    }
                }
            }
        }
        if (verbose >= 1) {
            System.out.println("MQ[0,m] = " + MQ[0][m]);
            System.out.println("elapsed calc_big_piles time : "+ (System.currentTimeMillis() - starttime)/runs/1000.0
                    + " sec [(N,m)=("+N+","+m+")]");
            // S(    2000, 1249) =   63582523 in   0.35 sec
            // S(12491249, 1249) =  227429102 in   13.2 min
        }
        return MQ[0][m];
    }

    long gcdExtended(long a, long b) {
        // Base Case
        if (a == 0) {
            x = 0;
            y = 1;
            return b;
        }
        // To store results of recursive call
        long gcd = gcdExtended(b % a, a);
        long x1 = x;
        long y1 = y;
        // Update x and y using results of recursive call:
        long div = b/a;
        x = y1 - div * x1; // TODO: integer division b // a
        y = x1;
        return gcd;
    }

    long modInverse(long A, long M) {
        long g = gcdExtended(A, M);
        long res;
        if (g != 1) {
            System.out.println("Inverse doesn't exist");
            res = -1;
        } else {
            // m is added to handle negative x
            res = (x % M + M) % M;
        }
        return res;
    }

    public class BinomMod {
        long modu;
        long[] fac;
        long[] invfac;
        public BinomMod(long mod, int nfac) {
            modu = mod;
            setfac(nfac);
        }
        public void setfac(int nfac) {
            fac = new long[nfac+1];
            invfac = new long[nfac+1];
            fac[0] = 1;
            for (int n=1; n<=nfac; n++) {
                fac[n] = (n*fac[n-1]) % modu;
            }
            invfac[nfac] = modInverse(fac[nfac], modu);
            for (int n=nfac; n>0; n--) {
                invfac[n-1] = (n*invfac[n]) % modu;
            }
        }

        public long binom(int n, int m) {
            if (n < m)
                return 0L;
            if (m == 0 || m == n)
                return 1L;
            long invden = (invfac[m]*invfac[n-m]) % modu;
            return (fac[n]*invden) % modu;
        }
    }

    /**
     * Test that the modular inverse works as expected and that BinomMod calculates binomial coefficients modulo
     * modu correctly
     */
    @Test
    public void calc_mod() {
        long x1 = modInverse(10, modu);
        System.out.println(1000/10 + ", " + x1);
        System.out.println((1000*x1) % modu);

        BinomMod bm = new BinomMod(modu, 50);
        CombinatoricsUtils cu;
        int n = 49; int m = 25;
        long cu_binom = CombinatoricsUtils.binomialCoefficient(n,m) % modu;
        System.out.println(bm.binom(n,m)+" = "+cu_binom);      // 75730100 for n=40, m=25
        assert bm.binom(n,m) == cu_binom : "Binomial coefficients are different!";
    }

    public long calc_big_wim59(int N, int m) {
        return calc_big_wim59(N, m, 2000);
    }
    public long calc_big_wim59(int N, int m, int tick) {
        int NNIM = 16;
        int verbose = 1;
        long starttime = System.currentTimeMillis();

        System.out.println("Group nims across full range...");
        load_gval_big();
        long fstart = System.currentTimeMillis();
        int[] numnim = new int[NNIM];
        for (int n=1; n<=N; n++) {
            numnim[gval(n)] += 1;
        }
        System.out.println("elapsed group nims time : "+(System.currentTimeMillis()-fstart)/1000.0+" sec");

        int numnimmax = 0;
        for (int nim=0; nim<NNIM; nim++) {
            numnimmax = max(numnimmax, numnim[nim]);
            // System.out.println("numnim["+nim+"] = "+numnim[nim]);
        }

        System.out.println("Compute factorials and inverses...");
        BinomMod bm = new BinomMod(modu, numnimmax + m - 1);  // m: number of piles = 1249

        System.out.println("DP ...");
        long[][] dp = new long[m + 1][NNIM];  // dp[1250][16]
        dp[0][0] = 1;
        for (int nim=0; nim<NNIM; nim++) {
            int num = numnim[nim];
            if (num == 0)
                continue;
            long[][] dp2 = new long[m + 1][NNIM];
            fstart = System.currentTimeMillis();
            for (int k=0; k<=m; k++) {
                if (k>0 && k % tick == 0) {
                    long ftime = System.currentTimeMillis();
                    System.out.println("Starting with (nim,k) = ("+nim+","+k+"), time="+(ftime-fstart)/1000+" sec ...");
                    fstart = ftime;
                }
                long bin = bm.binom (num + k - 1, k);
                int  nimk = (k % 2 == 0) ? 0 : nim;
                for (int k2=0, kpk2=k; kpk2<=m; k2++, kpk2++) {     // kpk2 = k+k2
                    for (int nim2=0; nim2<NNIM; nim2++) {
                        int xor = nimk ^ nim2;
                        dp2[kpk2][xor] = (dp2[kpk2][xor] + dp[k2][nim2] * bin) % modu;
                        //dp2[kpk2][xor] %= modu;
                    }
                }
            }   // for (k)

            // seams to be not very efficient ...
            for (int p=0; p<dp.length; p++)
                System.arraycopy(dp2[p],0,dp[p],0,dp[p].length);
            // ... but is actually not slower than the one-liner with Arrays.stream:
            //dp = Arrays.stream(dp2).map(long[]::clone).toArray(long[][]::new);
            // ... probably because the arraycopy-loop is only called 16 times [passes through for (nim)]
        } // for (nim)


        if (verbose >= 1) {
            System.out.println("dp[m][0]) = " + dp[m][0]);
            System.out.println("elapsed calc_big_wim59 time : "+ (System.currentTimeMillis() - starttime)/1000.0
                    + " sec [(N,m)=("+N+","+m+")]");
            // S(     124,    9) =  792687133 in   0.023 sec
            // S(     124,  100) =  530326718 in   0.061 sec
            // S(     124,  200) =  714064916 in   0.120 sec
            // S(    2000, 1249) =   63582523 in   0.967 sec
            // S(12491249, 1249) =  227429102 in   1.055 sec
            // S(12491249, 2498) =    2845084 in   3.888 sec    (all answers modulo 912491249)
            //
            // --> algorithm is O(N^0 m^2), i.e. constant time in N, quadratic in m
            // --> algorithm is 70 times (!) faster in Java than in Python
        }
        return dp[m][0];
    }

    public static void main(String[] args) {
        Nim1249Test n1249 = new Nim1249Test();
        System.out.println(n1249.calc_big_piles(2000, 1249));
        // S(    2000, 1249) =   63582523 in   0.35 sec
        // S(12491249, 1249) =  227429102 in   13.2 min
        System.out.println(n1249.calc_big_wim59(12491249, 1249, 3000));
        // S(    2000, 1249) =   63582523 in   0.967 sec
        // S(12491249, 1249) =  227429102 in   1.055 sec
        // S(12491249, 2498) =    2845084 in   3.888 sec    (all answers modulo 912491249)
    }

}
