package tools;

/**
 * Created by diego on 26/02/14.
 */

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Class ElapsedCpuTimer allowes time measurements according to TimerType: 
 * 		WALL_TIME, CPU_TIME, USER_TIME
 *   
 * Usage: class SingleTreeNode uses remainingTimeMillis() and elapsedMillis()
 * For remainingTimeMillis() to work, the member maxTime has to be set beforehand 
 * via {@link setMaxTimeMillis}. Default is maxTime=40 (ms).
 * 
 * @see controllers.MCTS.SingleTreeNode
 */
public class ElapsedCpuTimer {

    // allows for easy reporting of elapsed time
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    long oldTime;
    long maxTime = -1;			// time in nanoseconds	

    public enum TimerType {
        WALL_TIME, CPU_TIME, USER_TIME
    };

    public TimerType type = TimerType.WALL_TIME;

    public ElapsedCpuTimer(TimerType type) {
        this.type = type;
        oldTime = getTime();
    }

    public ElapsedCpuTimer() {
        oldTime = getTime();
    }

    /**
     * @return elapsed time in nanoseconds
     */
    public long elapsed() {
        return getTime() - oldTime;
    }


    public long elapsedNonos() {
        return (long) elapsed() ;
    }

    public long elapsedMicros() {
        return (long) (elapsed() / 1000.0);
    }

    public long elapsedMillis() {
        return (long) (elapsed() / 1000000.0);
    }

    public double elapsedSeconds() {
        return elapsedMillis()/1000.0;
    }

    public double elapsedMinutes() {
        return elapsedMillis()/1000.0/60.0;
    }


    public double elapsedHours() {
        return elapsedMinutes()/60.0;
    }

    public void reset() {
        oldTime = getTime();
    }

    public String toString() {
        // now resets the timer...
        String ret = elapsed() / 1000000.0 + " ms elapsed";
        reset();
        return ret;
    }

    private long getTime() {
        switch (type) {
            case WALL_TIME:
                return getWallTime();

            case CPU_TIME:
                return getCpuTime();

            case USER_TIME:
                return getUserTime();

            default:
                break;
        }
        return getCpuTime();
    }

    private long getWallTime() {
        return System.nanoTime();
    }

    private long getCpuTime() {

        if (bean.isCurrentThreadCpuTimeSupported()) {
            return bean.getCurrentThreadCpuTime();
        } else {
        	throw new RuntimeException("CpuTime NOT Supported");
        }
    }

    private long getUserTime() {
        if (bean.isCurrentThreadCpuTimeSupported()) {
            return bean.getCurrentThreadUserTime();
        } else {
        	throw new RuntimeException("UserTime NOT Supported");
        }

    }

    /**
     * 
     * @param time the desired maxTime in milliseconds
     */
    public void setMaxTimeMillis(long time) {
        maxTime = time * 1000000;

    }

    public long remainingTimeMillis()
    {
    	assert maxTime > 0 : "ElapsedCpuTimer: maxTime not set!";
        long diff = maxTime - elapsed();
        //System.out.println("diff,maxtime,elapsed ="+(diff/1e6)+", "+(maxTime/1e6)+", "+(maxTime-diff)/1e6);
        return (long) (diff / 1000000.0);
    }

    public boolean exceededMaxTime() {
    	assert maxTime > 0 : "ElapsedCpuTimer: maxTime not set!";
        if (elapsed() > maxTime) {
        	return false;   // /WK/
            //return true;
        }
        return false;
    }

}
