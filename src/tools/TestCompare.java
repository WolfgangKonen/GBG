package tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

/**
 *  A test class: Given class Items with a key and a value, we want to check in 
 *  a set of Items whether it contains an item which is equal to a certain Items.
 *  The equalness shall be user-defined (in this case: equality of keys)
 *
 */
public class TestCompare {
	
	public class Items  {
		public int key;
		public int value;

		public Items(int i, int j) {
			key=i;
			value=j;
		}

		public boolean isEqualTo(Object arg0) {
			Items aItem = null;
			if (arg0 instanceof Items) 
				aItem = (Items) arg0; 
			return (this.key == aItem.key);
		}
		
		// override dos not work, for unclear reasons
		@Override
		public boolean equals(Object arg0) {
			Items aItem = null;
			if (arg0 instanceof Items) 
				aItem = (Items) arg0; 
			return (this.key == aItem.key);
			
		}
		
	}

	public boolean containsState(HashSet itemSet, Items arg0) {
	    Iterator it = itemSet.iterator();
	    while (it.hasNext()) {
	           if (((Items)it.next()).isEqualTo(arg0))
	        	   return true;
	    }
	    return false;
		
	}
	public void operate() {
	    Items it1 = new Items(1,10);
	    Items it2 = new Items(2,20);
	    Items it3 = new Items(3,30);
	    Items it4 = new Items(4,40);
	    Items it5 = new Items(5,50);
	    Items it6 = new Items(4,-40);
		
		HashSet itemSet = new HashSet();
		itemSet.add(it1);
		itemSet.add(it2);
		itemSet.add(it3);
		itemSet.add(it4);
		
		// We want to check whether tupleSet contains an Items object with a certain key
		
		//
		// this does *not* work, since contains() does not operate with compareTo() but 
		// instead with equals() from class Object. Therefore the second 'contains(it6)' 
		// will return false, although the key 4 of it6 is in tupleSet. 
		//
		// For unclear reason it does not help to override equals() from class Object (see above).
		//
		if (itemSet.contains(it1)) 
			System.out.println("contains element with key 1");
		if (itemSet.contains(it6)) 
			System.out.println("contains element with key 4");
		if (!itemSet.contains(it5))
			System.out.println("does not contain element with key 5");
		
		// 
		// this instead works: we call our own method containsState to check for containment
		// and in this method we call our own method isEqualTo which exclusively looks 
		// to the key.
		//
		System.out.println();
		if (containsState(itemSet,it1)) 
			System.out.println("containsState: has element with key 1");
		if (containsState(itemSet,it6)) 
			System.out.println("containsState: has element with key 4");
		if (!containsState(itemSet,it5))
			System.out.println("containsState: does not have element with key 5");
		
	}
    
    public static void main(String[] args) throws IOException
    {
    	TestCompare tc = new TestCompare();
    	tc.operate();
    }

}
