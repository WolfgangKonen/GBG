package games.ZweiTausendAchtundVierzig;

import javax.swing.*;

import games.Arena;
import games.ArenaTrain;
import games.XArenaFuncs;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import tools.Types;

/**
 * Launch class used to start game 2048 in class {@link ArenaTrain} via 
 * a <b>main method</b>. <br> 
 *  
 * @author Wolfgang Konen, TH Köln, Nov'16
 * 
 * @see Arena
 * @see ArenaTrain
 * @see XArenaFuncs
 */
public class LaunchTrain2048 extends JFrame {
    private static final long serialVersionUID = 1L;
    public ArenaTrain2048 m_Arena;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	
//    	testLinkedList();
    	
        LaunchTrain2048 t_Frame = new LaunchTrain2048("General Board Game Playing");
        if (args.length == 0) {
            t_Frame.init();
        } else {
            throw new RuntimeException("[LaunchTrain2048.main] args=" + args + " not allowed. Use batch facility.");
        }
    }

    /**
     * Initialize the frame and {@link #m_Arena}.
     */
    public void init() {
        addWindowListener(new WindowClosingAdapter());
        m_Arena.init();
        setSize(Types.GUI_ARENATRAIN_WIDTH, Types.GUI_ARENATRAIN_HEIGHT);
        setBounds(0, 0, Types.GUI_ARENATRAIN_WIDTH, Types.GUI_ARENATRAIN_HEIGHT);
        //pack();
        setVisible(true);
    }

    public LaunchTrain2048(String title) {
        super(title);
        m_Arena = new ArenaTrain2048(this);
        setLayout(new BorderLayout(0, 0));
        setJMenuBar(m_Arena.m_menu);
        add(m_Arena, BorderLayout.CENTER);
    }

    protected static class WindowClosingAdapter
            extends WindowAdapter {
        public WindowClosingAdapter() {
        }

        public void windowClosing(WindowEvent event) {
            event.getWindow().setVisible(false);
            event.getWindow().dispose();
            System.exit(0);
        }
    }

    // this is just a test function for the LinkedList part in NTuple2ValueFunc
    private static void testLinkedList() {
    	
    	int h=2;
    	LinkedList sList = new LinkedList();
    	sList.clear();
    	Integer elem;	// Integer object is just a surrogate for the afterstate object s'_t 
    	
    	
    	for (int t=1; t<5; t++) {
    		// add element t at head of list and remove the element 
    		// 'beyond horizon' t_0 = t-h (if any)
    		elem = new Integer(t);
    		sList.addFirst(elem);
    		if (sList.size()>(h+1)) sList.pollLast();
    		
    		// iterate and print all elements in horizon: h+1 elements from t down to t_0
    		ListIterator<Integer> iter = sList.listIterator();
    		while(iter.hasNext()) {
    			elem=iter.next();
    			System.out.print(elem+" ");
    		}
    		System.out.println("");
    		
    	}
    	
    	
    }
}