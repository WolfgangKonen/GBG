package tools;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class PermutationIteratorTest {

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

    public static void main (String[] args) {
        List <Integer> il = new ArrayList <Integer> ();
        List <Integer> il2 = new ArrayList <Integer> ();
        for (int c = 0; c < 3; ++c)
        {
            il.add (c);
            if (c==0) il2.add(4);
            if (c==1) il2.add(9);
        }
//      il2.addAll(il);
//	    il.removeAll(objects);

        PermutationIterable <Integer> pi = new PermutationIterable <Integer> (il);
        PermutationIterable <Integer> pi2 = new PermutationIterable <Integer> (il2);
        for (List<Integer> li: pi)
//            for (List<Integer> li2: pi2) {
                show (li); 
//                show(li2);            	
//            }
        System.out.println ("-again-");
        // do it a second time: 
        for (List <Integer> li: pi)
            show (li);
        // test the inverse, that is, find the index (in the iterator) of certain permutations:
        System.out.println ("for (2,1,0) expecting 5 ?= " + pi.invers (genList (2, 1, 0)));
        System.out.println ("for (2,0,1) expecting 4 ?= " + pi.invers (genList (2, 0, 1)));
        System.out.println ("for (1,0,2) expecting 3 ?= " + pi.invers (genList (1, 2, 0)));
        System.out.println ("for (1,2,0) expecting 2 ?= " + pi.invers (genList (1, 0, 2)));
        System.out.println ("for (0,2,1) expecting 1 ?= " + pi.invers (genList (0, 2, 1)));
        System.out.println ("for (0,1,2) expecting 0 ?= " + pi.invers (genList (0, 1, 2)));
        // pick randomly some (10) permutations:
        Random r = new Random ();
        PermutationIterator <Integer> pitor = (PermutationIterator  <Integer>) pi.iterator ();
        for (int i = 0; i < 10; ++i)
        {
            int rnd = r.nextInt ((int) pitor.last); 
            List <Integer> rli = pitor.get (rnd);
            show (rli);
        }
    }
}