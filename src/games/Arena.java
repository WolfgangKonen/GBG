package games;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import agentIO.LoadSaveTD;
import tools.Progress;
import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.Arena.Task;
import games.TicTacToe.LaunchTrainTTT;
import tools.MessageBox;
import tools.StatusBar;
import tools.Types;

//import agentIO.Progress;

/**
 * This class contains the GUI and the task dispatcher for the game. The GUI for 
 * buttons and choice boxes is in {@link XArenaButtons}.
 * 
 * Run this class from the {@code main} in {@link LaunchArenaTTT}) for the 
 * TicTacToe game.
 * 
 * @author Wolfgang Konen, TH K�ln, Nov'16
 */
abstract public class Arena extends JPanel implements Runnable {
	public enum Task {PARAM, TRAIN, MULTTRN, PLAY, INSPECTV
		 //, INSPECTNTUP, BAT_TC, BATCH
		 , COMPETE, SWAPCMP, MULTCMP, IDLE  };
	public XArenaFuncs m_xfun;
	public JFrame m_TicFrame = null;  
	public XArenaMenu m_menu = null;
	public XArenaTabs m_tabs = null;
	public XArenaButtons m_xab;	// the game buttons and text fields
	private Thread playThread = null;	
	private Progress progress = null; // progress for some functions
	public LoadSaveTD tdAgentIO;	// saving/loading of agents
	protected JLabel m_title;
	protected GameBoard gb;		
	protected StatusBar statusBar = new StatusBar();
	public Task taskState = Task.IDLE;

	public Arena() {
		initGame();
	}
	public Arena(JFrame frame) {
		m_TicFrame = frame; 
		initGame();
	}
	
	protected void initGame() {
		gb = makeGameBoard();
		gb.setActionReq(false);
		
		m_xfun 		= new XArenaFuncs(this);
		m_xab    	= new XArenaButtons(m_xfun,this);		// needs a constructed 'gb'		
		tdAgentIO = new LoadSaveTD(this, m_xab, m_TicFrame);

		JPanel titlePanel = new JPanel();
		JLabel Blank=new JLabel(" ");		// a little bit of space
		m_title=new JLabel("Arena",SwingConstants.CENTER);
		m_title.setForeground(Color.black);	
		Font font=new Font("Arial",1,20);			
		m_title.setFont(font);	
		titlePanel.add(Blank);
		titlePanel.add(m_title);
		
		JPanel infoPanel = new JPanel(new BorderLayout(0,0));
		setLayout(new BorderLayout(10,0));
		setBackground(Color.white);
		infoPanel.add(new JLabel(" "),BorderLayout.NORTH); // a little gap
		//infoPanel.add(boardPanel,BorderLayout.CENTER);
		infoPanel.add(statusBar,BorderLayout.SOUTH);	

		m_menu = new XArenaMenu(this, m_TicFrame);
		m_tabs = new XArenaTabs(this);
		add(titlePanel,BorderLayout.NORTH);				
		add(m_xab,BorderLayout.CENTER);	
		add(infoPanel,BorderLayout.SOUTH);	
	}
	
	public void init()
	{	
		// this causes Arena.run() to be executed as a separate thread
		playThread = new Thread(this);
		playThread.start();		
	}	

	public void run() {
		String agentN;
		int n;
		gb.showGameBoard(this);
		
		m_xfun.m_PlayAgents = m_xfun.fetchAgents(m_xab);
		// ensure that m_PlayAgents has the agents selected, even if the first 
		// thing is to issue a save agent command

		while(true)
		{
			switch(taskState) {
			case COMPETE:
				enableButtons(false);
				setStatusMessage("Running Compete ...");
				
				m_xfun.competeBase(false, m_xab, gb);
				
				enableButtons(true);
				setStatusMessage("Compete finished.");
				UpdateBoard();
				taskState = Task.IDLE; 
				break;				
			case SWAPCMP:
				enableButtons(false);
				setStatusMessage("Running Swap Compete ...");
				
				m_xfun.competeBase(true, m_xab, gb);
				
				enableButtons(true);
				setStatusMessage("Swap Compete finished.");
				UpdateBoard();
				taskState = Task.IDLE; 
				break;				
			case MULTCMP:
				boolean silent=false;
				enableButtons(false);
				setStatusMessage("Running Multi Compete ...");
				
				try {
					m_xfun.multiCompete(silent, m_xab, gb);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				enableButtons(true);
				setStatusMessage("Multi Compete finished.");
				UpdateBoard();
				taskState = Task.IDLE; 
				break;				
			case PLAY: 
				enableButtons(false);
				gb.clearBoard(false,true);
				PlayGame();
				enableButtons(true);
				break;
			case INSPECTV:
				gb.clearBoard(false,true);
				gb.setActionReq(true);
				InspectGame();
				break;
			case IDLE:
			default:
				try
				{
					Thread.sleep(100);
				}	
				catch (Exception e){}
			}

			performArenaDerivedTasks();
			// a derived class, e.g. ArenaTrain, may define additional tasks 
			// in this method

		}
	}
	
	protected void UpdateBoard() {
		gb.updateBoard(null, false, false);
	}

	/**
	 * Inspect the value function in m_PlayAgent of {@link #m_xfun}. This agent will 
	 * be initially the one set in {@link XArenaFuncs}'s constructor (usually {@link controllers.MinimaxAgent})
	 * or the agent last trained via button "Train X"
	 */
	protected void InspectGame() {
		String AgentX = m_xab.getSelectedAgent(0);
		StateObservation so;
		Types.ACTIONS actBest;
		double[] vtable = null;
		PlayAgent paX;
		
		int numPlayers = gb.getStateObs().getNumPlayers();
		try 
		{
			paX = (m_xfun.fetchAgents(m_xab))[0];
			AgentBase.validTrainedAgent(paX,AgentX,+1,m_xab);
		} catch(RuntimeException e) 
		{
			MessageBox.show(m_xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;		
			setStatusMessage("Done.");
			return;			
		}

		String pa_string = paX.getClass().getName();
		//String pa_string = m_TTT.m_PlayAgentX.getClass().getName();
		System.out.println("[InspectGame] "+pa_string);
		
		gb.clearBoard(true, true);
		gb.updateBoard(null,false,true);				// enable Board buttons
		while(taskState == Task.INSPECTV)
		{			
			if(gb.isActionReq()){
				gb.setActionReq(false);
				so = gb.getStateObs();
				if (so.isLegalState() && !so.isGameOver()) {
					boolean silent=false;
					vtable = new double[so.getNumAvailableActions()+1];
					actBest = paX.getNextAction(so, false, vtable, true);
					so.storeBestActionInfo(actBest, vtable);
					
					gb.updateBoard(so,true,true);
				} else {
					gb.updateBoard(null,false,true);
					// not a valid play position >> show the initial VBoard (orange)
					gb.clearBoard(false,true);
				}
			}
			else
			{
				try
				{
					Thread.sleep(100);
					//
					// wait until an action in GameBoard gb occurs (see
					// ActionListener in InitBoard()), which will set 
					// gb.isActionReq() to true again. 
				}	
				catch (Exception e){}
			}
		}
		gb.clearBoard(true,true);

	}
	
	/**
	 * Play a game (the agents selected in the combo boxes). One or  
	 * multiple of them may be "Human".
	 * For 2-player games it is a game "X vs. O".
 	 */
	public void PlayGame()
	{
		int Player,player;
		StateObservation so;
		Types.ACTIONS actBest;
		PlayAgent pa;
		double[] vtable = null;
		PlayAgent[] paVector;

		// fetch the agents in a way general for 1-, 2- and n-player games
		int numPlayers = gb.getStateObs().getNumPlayers();
		try 
		{
			paVector = m_xfun.fetchAgents(m_xab);
			AgentBase.validTrainedAgents(paVector,numPlayers);
		} catch(RuntimeException e) 
		{
			MessageBox.show(m_xab, 
					e.getMessage(), 
					"Warning", JOptionPane.WARNING_MESSAGE);
			taskState = Task.IDLE;		
			setStatusMessage("Done.");
			return;			
		}


		String AgentX ="";
		String AgentO ="";


		switch (numPlayers) {
			case (1):
				AgentX = m_xab.getSelectedAgent(0);
				break;
			case (2):
				AgentX = m_xab.getSelectedAgent(0);
				AgentO = m_xab.getSelectedAgent(1);
				break;
			default:
				// TODO: implement s.th. for n-player games (n>2)
				break;
		}

		String sMsg;
		switch (numPlayers) {
		case (1): 
			sMsg = "Playing a game ... [ "+AgentX+" ]"; break;
		case (2):
			sMsg = "Playing a game ... ["+AgentX+" (X) vs. "+AgentO+" (O)]"; break;
		default: 
			sMsg = "Playing an n-player game ..."; break;
		}
		setStatusMessage(sMsg);
		System.out.println(sMsg);
		
		gb.clearBoard(true,true);
		gb.setActionReq(true);
		so = gb.getStateObs();
		assert paVector.length == so.getNumPlayers() : 
			  "Number of agents does not match so.getNumPlayers()!";
		
		while(true)
		{			
			if(gb.isActionReq()){
				so = gb.getStateObs();
				player = so.getPlayer();
				pa = paVector[player];
					if (pa instanceof controllers.HumanPlayer) {
						gb.setActionReq(false);
						gb.updateBoard(so,false,false);		// enable board buttons
					}
					else {
                        vtable = new double[so.getNumAvailableActions() + 1];
                        actBest = pa.getNextAction(so, false, vtable, true);
                        so.storeBestActionInfo(actBest, vtable);
                        so.advance(actBest);
                        try {
                            Thread.sleep(200);
                            // waiting time between agent-agent actions
                        } catch (Exception e) {
                            System.out.println("Thread 1");
                        }
                        gb.updateBoard(so,true,false);
				}


				
			} // if(gb.isActionReq())
			else
			{
				try
				{
					Thread.sleep(100);
					//
					// wait until an action in GameBoard gb occurs (see
					// ActionListener in InitBoard()), which will set 
					// gb.isActionReq() to true again. 
				}	
				catch (Exception e){System.out.println("Thread 3");}
			}
			so = gb.getStateObs();
			if (so.isGameOver()) {
				switch (so.getNumPlayers()) {
				case 1: 
					double gScore = so.getGameScore();
					MessageBox.show(m_TicFrame, "Game finished with score " +gScore, 
							"Game Over", JOptionPane.INFORMATION_MESSAGE );
					break;  // out of switch
				case 2: 
					int win=so.getGameWinner().toInt();
					Player=so.getPlayerPM();
					switch(Player*win) {
					case (+1): 
						MessageBox.show(m_TicFrame, "X ("+AgentX+") wins", 
								"Game Over", JOptionPane.INFORMATION_MESSAGE );
						break;  // out of inner switch
					case (-1):
						MessageBox.show(m_TicFrame, "O ("+AgentO+") wins",
								"Game Over", JOptionPane.INFORMATION_MESSAGE );
						break;  // out of inner switch
					case ( 0):
						MessageBox.show(m_TicFrame, "Tie", 
								"Game Over", JOptionPane.INFORMATION_MESSAGE );
						break;  // out of inner switch
					} // switch(Player*win)
					break;   // out of switch
				default: 
					// TODO: implement s.th. for n-player games (n>2)
					break;  // out of switch
				}
				
				break;			// this is the final bread out of while loop 	
			} // if isGameOver
			
		}	// while(true) [will be left only by the last break above]
		taskState = Task.IDLE;		
		setStatusMessage("Done.");
	}

	public GameBoard getGameBoard() {
		return gb;
	}
	
	public void setStatusMessage(String msg) { statusBar.setMessage(msg); }
	public StatusBar getStatusBar() { return statusBar; }

	
//	@Override
	public void setProgress(tools.Progress p) {
		this.progress = p;
	}

	public void enableButtons(boolean state) { m_xab.enableButtons(state); }

	abstract public String getGameName();
	
	/**
	 * Factory pattern method: make a new GameBoard 
	 * @return	the game board
	 */
	abstract public GameBoard makeGameBoard(); 

	/**
	 * Factory pattern method: make a new Evaluator
	 * @param pa	the agent to evaluate
	 * @param gb	the game board
	 * @param stopEval	the number of successful evaluations needed to reach the 
	 * 					evaluator goal (may be used during training to stop it 
	 * 					prematurely)
	 * @param mode		which evaluator mode
	 * @param verbose	how verbose or silent the evaluator is
	 * @return
	 */
	abstract public Evaluator makeEvaluator(PlayAgent pa, GameBoard gb, int stopEval, int mode, int verbose);

	/**
	 * This method is called from {@link #run()} and it has to be overridden by 
	 * classes derived from {@link Arena} (e.g. {@link ArenaTrain}). <p>
	 * 
	 * It allows to add additional tasks to the task switch.
	 * May be an empty method if no tasks have to be added. <p>
	 * 
	 * This method will use member {@code taskState} from {@link Arena}. 
	 * It performs several actions appropriate for the derived class 
	 * and - importantly - changes taskState back to IDLE (when appropriate).
	 * 
	 * @see ArenaTrain
	 */
	abstract public void performArenaDerivedTasks();

}
