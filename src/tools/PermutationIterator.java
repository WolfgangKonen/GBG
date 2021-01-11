package tools;

import java.util.*;

/**
 * Class to iterate over permutations.
 * <p>
 *
 * from Community's answer in
 * https://stackoverflow.com/questions/2799078/permutation-algorithm-without-recursion-java/10117424#10117424
 *
 */
public class PermutationIterator <T> implements Iterator <List <T>> {

    private int  current = 0;
    private final List <T> lilio;
    public final long last;			// total number of permutations

    /**
     * @param llo	the list for which an iterator over all permutations is desired
     */
    public PermutationIterator (final List <T> llo) {
        lilio = llo;
//        long product = 1;
//        for (long p = 1; p <= llo.size(); ++p) 
//            product *= p; 
//        last = product;
        last = fac(llo.size());
    }

    public boolean hasNext () {
        return current != last;
    }

    public List <T> next () {
        ++current;
        return get (current - 1, lilio);
    }

    public void remove () {
        ++current;
    }

    private long fac (long l) 
    {
        for (long i = l - 1L; i > 1L; --i)
            l *= i; 
        return l;
    }

    /**
     * Return the {@code code}'th permutation of list {@code list}
     * <p>
     * New version, which produces permutations in increasing order.
     */
    private List <T> get (final long code, final List <T> list) {
        if (list.isEmpty ()) 
            return list;
        else
        {								// Example:	
            int len = list.size ();     // len = 4
            long max = fac (len);       // max = 24
            long divisor = max / len;   // divisor = 6
            int i = (int) (code / divisor); // i = 2
            List <T> second = new ArrayList <T> (list.size ());
            second.addAll (list);
            T el = second.remove (i);
            List <T> tt = new ArrayList <T> ();
            tt.add (el);
            tt.addAll (get (code - divisor * i, second));
            return tt;
        }
    }

    /**
     * Use this method to pick a certain permutation by index {@code code}.  
     */
    public List <T> get (final int code) 
    {
        return get (code, lilio);
    }
}

