package tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PermutationIterable <T> implements Iterable <List <T>> {

    private List <T> lilio; 

    public PermutationIterable (List <T> llo) {
        lilio = llo;
    }

    public Iterator <List <T>> iterator () {
        return new PermutationIterator <T> (lilio);
    }

    /**
     * This method generates the opposite of {@link PermutationIterator#get(int)}: 
     * The index of a certain permutation. 
     * @param pattern	the permutation to search for
     * @param matcher	the list over which {@link PermutationIterator} iterates 
     */
    private long invers (final List <T> pattern, final List <T> matcher)
    {
        if (pattern.isEmpty ())
            return 0L;
        T first = pattern.get (0);
        int idx = matcher.indexOf (first);
        long l = (pattern.size () - 1L) * idx;
        pattern.remove (0);
        matcher.remove (idx);
        return l + invers (pattern, matcher);
    }
    /**
      make a deep copy, since the called method will destroy the parameters
    */
    public long invers (final List <T> lt)
    {
        List <T> copy = new ArrayList <T> (lilio.size ());
        copy.addAll (lilio);
        return invers (lt, copy); 
    }   
}

