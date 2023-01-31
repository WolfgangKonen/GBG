package tools;

import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import static org.junit.Assert.*;

/**
 *  A test class: Given class Items with a key and a value, we want to check in
 *  a set of Items whether it contains an item which is the same as a certain Item.
 *  The sameness shall be user-defined (in this case: equality of keys)
 *
 *  Optional: Test a linked list (function testLinkedList)
 *
 */
public class CompareItemsTest {

    Items it1 = new Items(1, 10);
    Items it2 = new Items(2, 20);
    Items it3 = new Items(3, 30);
    Items it4 = new Items(4, 40);
    Items it5 = new Items(5, 50);
    Items it6 = new Items(4, -40);

    public class Items {
        public int key;
        public int value;

        public Items(int key, int value) {
            this.key = key;
            this.value = value;
        }

        public boolean isEqualTo(Object arg0) {
            Items aItem = null;
            if (arg0 instanceof Items)
                aItem = (Items) arg0;
            return (this.key == aItem.key);
        }

        // overriding method equals dos not work for Set.contains(), for unclear reasons.
        // But it works for our method containsState2().
        @Override
        public boolean equals(Object arg0) {
            Items aItem = null;
            if (arg0 instanceof Items)
                aItem = (Items) arg0;
            return (this.key == aItem.key);

        }
    } //class Items

    public boolean containsState(HashSet itemSet, Items arg0) {
        Iterator it = itemSet.iterator();
        while (it.hasNext()) {
            if (((Items) it.next()).isEqualTo(arg0))
                return true;
        }
        return false;
    }

    public boolean containsState2(HashSet itemSet, Items arg0) {
        Iterator it = itemSet.iterator();
        while (it.hasNext()) {
            if (((Items) it.next()).equals(arg0))
                return true;
        }
        return false;
    }

    @Test
    public void testStringSplit() {
        String fname = "myFile-stu.agt.zip";
        String[] arr = fname.split("-stub");
        StringBuilder out= new StringBuilder();
        for (String s : arr) out.append(s);
        System.out.println(out.toString());
    }

    @Test
    public void testContains() {
        String str;

        HashSet itemSet = new HashSet();
        itemSet.add(it1);
        itemSet.add(it2);
        itemSet.add(it3);
        itemSet.add(it4);

        // We want to check whether tupleSet contains an Items object with a certain key

        //
        // this does *not* work, since contains() does not operate with compareTo() but
        // instead with equals() from class Object. Therefore the second 'contains(it6)'
        // will return false, although the key 4 of it6 is in tupleSet (but not the element it6 itself.
        //
        // For unclear reason it does not help to override equals() from class Object (see above).
        // It works however (see containsState2 below), if we call equals() directly. So it
        // must be that contains() does not call the same equals(), for some unclear reason.
        //
        str = ((itemSet.contains(it1)) ? "contains " : "DOES NOT contain ") + "element with key " + it1.key;
        System.out.println(str);
        str = ((itemSet.contains(it6)) ? "contains " : "DOES NOT contain ") + "element with key " + it6.key;
        System.out.println(str);
        str = ((itemSet.contains(it5)) ? "contains " : "DOES NOT contain ") + "element with key " + it5.key;
        System.out.println(str);

        //
        // this instead works: we call our own method containsState to check for containment
        // and in this method we call our own method isEqualTo which exclusively looks
        // to the key.
        //
        System.out.println();
        str = "containState: " + ((containsState(itemSet, it1)) ? "has " : "DOES NOT have ") + "element with key " + it1.key;
        System.out.println(str);
        str = "containState: " + ((containsState(itemSet, it6)) ? "has " : "DOES NOT have ") + "element with key " + it6.key;
        System.out.println(str);
        str = "containState: " + ((containsState(itemSet, it5)) ? "has " : "DOES NOT have ") + "element with key " + it5.key;
        System.out.println(str);
        assert containsState(itemSet, it1);
        assert containsState(itemSet, it6);
        assert !containsState(itemSet, it5);

        //
        // this instead works as well: we call our own method containsState2 to check for containment
        // and in this method we call equals(Object arg0) explicitly, which is our overriding
        // method which exclusively looks to the key.
        //
        System.out.println();
        str = "containState2: " + ((containsState2(itemSet, it1)) ? "has " : "DOES NOT have ") + "element with key " + it1.key;
        System.out.println(str);
        str = "containState2: " + ((containsState2(itemSet, it6)) ? "has " : "DOES NOT have ") + "element with key " + it6.key;
        System.out.println(str);
        str = "containState2: " + ((containsState2(itemSet, it5)) ? "has " : "DOES NOT have ") + "element with key " + it5.key;
        System.out.println(str);
        assert containsState2(itemSet, it1);
        assert containsState2(itemSet, it6);
        assert !containsState2(itemSet, it5);

    }

    @Test
    public void testLinkedList() {
        // this is just a test function for the LinkedList part in NTuple2ValueFunc

        int horizon = 2;
        LinkedList sList = new LinkedList();
        sList.clear();
        Integer elem;    // Integer object is just a surrogate for the afterstate object s'_t

        System.out.println();
        for (int t = 1; t < 5; t++) {
            // add element t at head of list and remove the element
            // 'beyond horizon' t_0 = t-h (if any)
            elem = Integer.valueOf(t);
            sList.addFirst(elem);
            if (sList.size() > (horizon + 1)) sList.pollLast();

            // iterate and print all elements in horizon: h+1 elements from t down to t_0
            ListIterator<Integer> iter = sList.listIterator();
            while (iter.hasNext()) {
                elem = iter.next();
                System.out.print(elem + " ");
            }
            System.out.println(" last=" + sList.getLast());
            int valueOfLast = Math.max(t - horizon, 1);
            assert (Integer) sList.getLast() == valueOfLast;

        }

    }
}