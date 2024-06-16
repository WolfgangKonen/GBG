package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Minimal code PE problem 888 (1249 Nim) for PE post
 */
public class Nim1249 {
    long modu = 912491249;
    long[][] MQ;
    static int[] gval_big = new int[11382];

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
        load_gval_big();

        MQ = new long[16][m+1];

        for (int f=N; f>0; f--) {
            MQ[gval(f)][1] +=1;
            for (int q=2; q<=m; q++)
                for (int H=0; H<16; H++)
                    MQ[H][q] = (MQ[H][q] + MQ[H ^ gval(f)][q - 1]) % modu;
        }
        return MQ[0][m];
    }

    public static void main(String[] args) {
        Nim1249 n1249 = new Nim1249();
        System.out.println(n1249.calc_big_piles(2000, 1249));
        // S(    2000, 1249) =   63582523 in   0.35 sec
        // S(12491249, 1249) =  227429102 in   13.2 min
    }
}
