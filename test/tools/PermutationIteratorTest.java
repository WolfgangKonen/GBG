package tools;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import tools.PermutationIterable;
import tools.PermutationIterator;

public class PermutationIteratorTest {

    private List <Integer> il = genList (0, 1, 2);

    private PermutationIterable <Integer> pi = new PermutationIterable <Integer> (il);

    public static List <Integer> genList (int... a) {
        List <Integer> li = new ArrayList <Integer> ();
        for (int i: a)
            li.add (i);
        return li;
    }

    public static void show (List <?> lo) {
        System.out.print ("(");
        for (Object o: lo)
            System.out.print (o);
        System.out.println (")");
    }

    @Test
    public void testPermutationIterableInvers() {
        // test pi.invers(), that is, that it finds the right index (in the iterable) of all permutations of List il:
        System.out.println ("PermutationIterable for list il = "+il.toString());
        PermutationIterator <Integer> pitor = (PermutationIterator  <Integer>) pi.iterator ();
        for (int i=0; i<(int)pitor.last; i++) {
            List <Integer> li = pitor.get(i);
            //show(li);
            assert i == pi.invers(li) : "pi.invers failed for i=" + i + " and li=" + pitor.get(i); //somehow 'li.toString();' will not work here
        }
        // this is not really needed, just for clarity:
        assert (pi.invers (genList (0,1,2)) == 0); System.out.println ("(0,1,2) is the 0th element of pi");
        assert (pi.invers (genList (0,2,1)) == 1); System.out.println ("(0,2,1) is the 1st element of pi");
        assert (pi.invers (genList (1,0,2)) == 2); System.out.println ("(1,0,2) is the 2nd element of pi");
        assert (pi.invers (genList (1,2,0)) == 3); System.out.println ("(1,2,0) is the 3rd element of pi");
        assert (pi.invers (genList (2,0,1)) == 4); System.out.println ("(2,0,1) is the 4th element of pi");
        assert (pi.invers (genList (2,1,0)) == 5); System.out.println ("(2,1,0) is the 5th element of pi");
    }

    @Test
    public void testPermutationIterator() {
        // pick randomly some (10) permutations from:
        Random r = new Random ();
        PermutationIterator <Integer> pitor = (PermutationIterator  <Integer>) pi.iterator ();
        for (int i = 0; i < 10; ++i)
        {
            int rnd = r.nextInt ((int) pitor.last);
            List <Integer> rli = pitor.get (rnd);
            System.out.println("random pick no. "+rnd+": "+rli.toString());
        }
    }

}
