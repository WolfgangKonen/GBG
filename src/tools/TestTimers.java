package tools;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *  A test class: Given class Items with a key and a value, we want to check in 
 *  a set of Items whether it contains an item which is the same as a certain Item.
 *  The sameness shall be user-defined (in this case: equality of keys)
 *  
 *  Optional: Test a linked list (function testLinkedList)
 *
 */
public class TestTimers {
	

    public static void main(String[] args) throws IOException
    {
    	ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    	System.out.println("CpuTimeSupported():"+threadMXBean.isCurrentThreadCpuTimeSupported());
    	long startTNanoThread = threadMXBean.getCurrentThreadCpuTime();
    	long startTNanoInstant = Instant.now().getNano();
    	long startTNano = System.nanoTime();
    	
    	long NN=100000;
    	double dummy;
    	double sum=0.0;
    	for (long i=0; i<NN; i++)
    		sum=sum+Math.sqrt(Math.abs(Math.sin(0.3)));
    	
    	// the following time enters System.nanoTime, Instant.* but NOT threadMXBean-time 
    	// (the current thread sleeps, so it does not get the time passing by added)
		try {
	    	Thread.sleep(0);
			// waiting time between agent-agent actions in milliseconds
		} catch (Exception e) {
			System.out.println("Thread 1");
		}
    	
    	long endTNano = System.nanoTime();
    	long endTNanoThread = threadMXBean.getCurrentThreadCpuTime();
    	long endTNanoInstant = Instant.now().getNano();
    	System.out.println("Time    nanoTime: "+(endTNano-startTNano));
    	System.out.println("Time  ThreadNano: "+(endTNanoThread-startTNanoThread));
    	System.out.println("Time InstantNano: "+(endTNanoInstant-startTNanoInstant));
    	System.out.println(startTNanoThread+","+endTNanoThread);
    	System.out.println(startTNanoInstant+","+endTNanoInstant);
    	long offset = 0;
    	System.out.println("Nanos per op: "+((double)(endTNano-startTNano-offset))/NN);
    	

    }

}
