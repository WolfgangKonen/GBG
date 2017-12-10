package games;

import javax.swing.JMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import controllers.AgentBase;
import controllers.ExpectimaxNAgent;
import controllers.HumanPlayer;
import controllers.MaxNAgent;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.MC.MCAgent;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTSExpectimax.MCTSExpectimaxAgt;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import tools.MessageBox;
import tools.ShowBrowser;
import tools.Types;

/**
 * Main menu for {@link Arena} and {@link ArenaTrain}.
 * 
 * @author Wolfgang Konen, TH K�ln, Nov'16
 * 
 */

public class XArenaMenu extends JMenuBar {

	private static final String TIPEVALUATE = "";

	private static final String TIPINSPECTLUT = "Inspect the lookup-tables (LUT) for the selected "
			+ "N-Tuple-Agent. The selected agent also must be trained for this to work";

	private static final String TIPSETNTUPLES = "<html><body>Show and change the N-Tuples or a N-Tuple-TD-Agent "
			+ "This button doesn't work for other agent-types. The selected agent also must <br>"
			+ "be initialized for this to work.  If  the used N-Tuples change, "
			+ "then the agent also must be initialized again!</body></html>";

	private static final String TIPSHOWNTUPLE = "<html><body>Show the N-Tuples for a N-Tuple-TD-Agent. This button "
			+ "doesn't work for other agent-types. The selected agent also must <br>"
			+ "be trained for this to work</body></html>";

	private static final String TIPSAVE = "<html><body>Save a COMPLETE TD-Agent. All lookup-tables and configurations of the agent "
			+ "will be saved to HDD. Make sure, that enough <br>hdd-memory is available (all lookup-tables "
			+ "can get huge sometimes)</body></html>";

	private static final String TIPLOAD = "Open a COMPLETE TD-Agent. All lookup-tables and configurations of the agent "
			+ "will be loaded and assigned to the selected agent";

	private static final long serialVersionUID = -7247378225730684090L;

	private Arena m_arena;

	// private JRadioButtonMenuItem rbMenuItem;
	// private JCheckBoxMenuItem cbMenuItem;
	private JFrame m_frame;
	private int selectedAgent = 0;
	private int numPlayers;
	private boolean winCompVisible=false;

	// private final String agentList[] = { "Human", "Minimax", "TDS", "Random"
	// };

	XArenaMenu(Arena arena, JFrame m_TicFrame) {
		m_arena = arena;
		m_frame = m_TicFrame;
		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();

		generateFileMenu();
		generateAgentMenu();
		generateCompetitionMenu();
		//generateOptionsMenu();
		generateHelpMenu();
	}

	private void generateFileMenu() {
		JMenu menu;
		JMenuItem menuItem;

		// Build the first menu.
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("File Options");

		menuItem = new JMenuItem("Param Tabs");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.m_tabs.showParamTabs(m_arena,!m_arena.m_tabs.isVisible(),m_arena.m_xab.getSelectedAgent(0));
			}
		});
		menuItem.setToolTipText(TIPEVALUATE);
		if (!(m_arena instanceof ArenaTrain))
			menuItem.setEnabled(false);			// no param tabs for Arena
		menu.add(menuItem);
		// ==============================================================
		// Quit Program
		// ==============================================================
		menuItem = new JMenuItem("Quit Program", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_frame.setVisible(false);
				m_frame.dispose();
				System.exit(0);
			}
		});
		menuItem.setToolTipText(TIPEVALUATE);
		menu.add(menuItem);

		add(menu);
	}

	private void generateAgentMenu() {
		JMenu menu, submenu;
		JMenuItem menuItem;

		menu = new JMenu("TD-Agents");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
				"Options for TD-Agent");
		for (int i = 0; i < numPlayers; i++) {
			final int j = i;
			String strLoad, strSave;
			String strAgent;
			if (numPlayers==2) {
				strLoad = "Load Agent "+Types.GUI_2PLAYER_NAME[j];
				strSave = "Save Agent "+Types.GUI_2PLAYER_NAME[j];
				strAgent = "Agent-"+Types.GUI_2PLAYER_NAME[j];
			} else {
				strLoad = "Load Agent "+Types.GUI_PLAYER_NAME[j];
				strSave = "Save Agent "+Types.GUI_PLAYER_NAME[j];
				strAgent = "Agent-"+Types.GUI_PLAYER_NAME[j];
			}
			// ==============================================================
			// Agent i Submenu
			// ==============================================================
			submenu = new JMenu(strAgent);
			// submenu.setMnemonic(KeyEvent.VK_S);

			// ==============================================================
			// Load Agent i
			// ==============================================================
			menuItem = new JMenuItem(strLoad);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						loadAgent(j);						
					} catch (Exception exc) {	
						System.out.println(exc.getMessage());
					}
				}
			});
			menuItem.setToolTipText(TIPLOAD);
			submenu.add(menuItem);

			// ==============================================================
			// Save Agent i
			// ==============================================================
			menuItem = new JMenuItem(strSave);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAgent(j);
				}
			});
			menuItem.setToolTipText(TIPSAVE);
			if (!(m_arena instanceof ArenaTrain))
				menuItem.setEnabled(false);			// no agent save for Arena
			submenu.add(menuItem);
			submenu.addSeparator();

			// ==============================================================
			// Show N-Tuples Agent i
			// ==============================================================
			menuItem = new JMenuItem("Show N-Tuples");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//showNTuples(j);
				}
			});
			menuItem.setToolTipText(TIPSHOWNTUPLE);
			menuItem.setEnabled(false);
			submenu.add(menuItem);

			// ==============================================================
			// Set N-Tuples of Agent i
			// ==============================================================
			menuItem = new JMenuItem("Set / Change N-Tuples");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//setNTuples(j);
				}
			});
			menuItem.setToolTipText(TIPSETNTUPLES);
			menuItem.setEnabled(false);
			submenu.add(menuItem);

			// ==============================================================
			// Show LUT of Agent i
			// ==============================================================
			menuItem = new JMenuItem("Inspect LUT");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//inspectLUT(j);
				}
			});
			menuItem.setToolTipText(TIPINSPECTLUT);
			menuItem.setEnabled(false);
			submenu.add(menuItem);

			// ==============================================================
			// Evaluate TD-Agent Agent i
			// ==============================================================
			menuItem = new JMenuItem("Quick Evaluation");
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					evaluate(j);
				}
			});

			menuItem.setToolTipText(TIPEVALUATE);
			submenu.add(menuItem);
			
			if (numPlayers>3) {
				// more than 3 agents: make a "TD-Agents" menu with submenus
				menu.add(submenu);
			} else {
				// up to 3 agents: make Agent-i menus
				add(submenu);
				
			}
		} // for (i)

		if (numPlayers>3) add(menu);
	}

	private void generateCompetitionMenu() {
		JMenu menu, submenu;
		JMenuItem menuItem;

		menu = new JMenu("Competition");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
				"Options for Competition");

		submenu = new JMenu("Competition");

		// ==============================================================
		// Competition-Options
		// ==============================================================
		menuItem = new JMenuItem("Competition Options");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				winCompVisible = !m_arena.m_xab.winCompOptions.isVisible();
				m_arena.m_xab.winCompOptions.showOptionsComp(m_arena,winCompVisible);
			}
		});
		menuItem.setToolTipText("Choose the options for single- and multi-Competitions");
		menu.add(menuItem);

		// ==============================================================
		// Single-Competition
		// ==============================================================
		menuItem = new JMenuItem("Single Compete");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.taskState = Arena.Task.COMPETE;
				String str = "[Start Single Competition]";
				printStatus(str);
			}
		});
		menuItem.setToolTipText("<html><body>Start a single competition between the selected Agents "
				+ " The results are printed to <br>"
				+ "the console</body></html>");
		menuItem.setEnabled(numPlayers>1);
		menu.add(menuItem);
		menuItem = new JMenuItem("Swap Compete");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.taskState = Arena.Task.SWAPCMP;
				String str = "[Start Swap Competition]";
				printStatus(str);
			}
		});
		menuItem.setToolTipText("<html><body>Start a single competition between the selected Agents "
				+ "in swapped order. The results <br>"
				+ "are printed to the console</body></html>");
		menuItem.setEnabled(numPlayers>1);
		menu.add(menuItem);

		// ==============================================================
		// Multi-Competition
		// ==============================================================
		menuItem = new JMenuItem("Multi-Competition");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.taskState = Arena.Task.MULTCMP;
				//ticGame.changeState(State.MULTICOMPETE);
				String str = "[Start multi-Competition]";
				printStatus(str);
			}
		});
		menuItem.setToolTipText("<html><body>Start a multi-competition between the selected "
				+ "Agents (do 'Competions' times a single competition). The results are printed to <br>"
				+ "the console and saved to bin/TicTacToe.comp.csv.</body></html>");
		menuItem.setEnabled(numPlayers>1);
		menu.add(menuItem);

		
		menu.addSeparator();
		
		submenu = new JMenu("Multi-Training");
		// ==============================================================
		// Options for Multi-Training
		// ==============================================================
		menuItem = new JMenuItem("Options");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//ticGame.c4Buttons.setWindowPos(ticGame.winMultiTrainOptions);
			}
		});
		menuItem.setToolTipText("Set Options for the multi-training. The agent that shall "
				+ "be trained (TD) and the opponent must be selected here.");
		if (!(m_arena instanceof ArenaTrain))
			menuItem.setEnabled(false);			// no multi-training for Arena
		submenu.add(menuItem);
		if (!(m_arena instanceof ArenaTrain))
			submenu.setEnabled(false);			// no multi-training for Arena

		// ==============================================================
		// Start Multi-Training
		// ==============================================================
		menuItem = new JMenuItem("Start Multi-Training");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//ticGame.changeState(TicGame.State.MULTTRN);
				String str = "[Start Multi-Training of TD-Agent]";
				printStatus(str);
			}
		});
		menuItem.setToolTipText("Start the multi-training of the TD-Agent. Be sure thtat the options "
				+ "where choosen correctly and that both agents (TD and opponent) are initialized");
		if (!(m_arena instanceof ArenaTrain))
			menuItem.setEnabled(false);			// no multi-training for Arena
		submenu.add(menuItem);

		menu.add(submenu);

		add(menu);
	}

	private void generateOptionsMenu() {
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("Options");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
				"Options for Competition");

		// ==============================================================
		// Options for Game-Theoretic Values
		// ==============================================================
		menuItem = new JMenuItem("Game-Theoretic Values");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//ticGame.m_xab.setWindowPos(ticGame.winOptionsGTV);
			}
		});
//		menuItem.setToolTipText("<html><body>1. Set the Size of the Hash-Table "
//				+ "for the Agent that determines the game-theoretic <br>"
//				+ "values in a lot of situations. If the Hash-Size is made "
//				+ "smaller more time is needed to calculate the score <br>"
//				+ "for a position. <br> "
//				+ "2.Choose which databases shall be used "
//				+ "for the Agent that determines the game-theoretic values in a lot of <br>"
//				+ "Situations. The usage of the databases needes more memory but"
//				+ " fastens the agent a lot <br>"
//				+ "3. Set the Search Depth for the Agent "
//				+ "that determines the game-theoretic values. The Search-depth <br>"
//				+ "should be predefined value</body></html>");
		menuItem.setToolTipText("(currently not implemented)");
		menu.add(menuItem);

		add(menu);
	}

	private void generateHelpMenu() {
		JMenu menu;
		JMenuItem menuItem, menuItem2;

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Help");

		menuItem = new JMenuItem("Show Help-File");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.m_xab.helpFunction();
				//java.net.URL url = getClass().getResource("/doc/index.htm");
				//ShowBrowser.openURL(url);			
			}
		});
		menuItem.setToolTipText("<html><body>Show Help File</body></html>");
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Help-File in Browser");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//java.net.URL url = getClass().getResource("/doc/HelpGUI-Arena-GBG.htm");
				java.net.URL url = getClass().getResource("/HelpGUI-Arena-GBG.htm");  
									// getResource() will find .htm in ../resources/
				System.out.println("URL = " + url);
				if (url == null) {
					MessageBox.show(m_frame,"ERROR: Could not locate URL with " + 
							"getClass().getResource(\"/HelpGUI-Arena-GBG.htm\")", 
							"XArenaMenu",JOptionPane.ERROR_MESSAGE);
					printStatus("[ERROR: Could not locate help file URL]");
				} else {
					ShowBrowser.openURL(url);
				}
			}
		});
		menuItem.setToolTipText("<html><body>Show Help-File in your default browser</body></html>");
		menu.add(menuItem);

		menuItem = new JMenuItem("Show TR-GBG.pdf");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.net.URL url = getClass().getResource("/TR-GBG.pdf");  
									// getResource() will find .pdf in ../resources/
				System.out.println("URL = " + url);
				if (url == null) {
					MessageBox.show(m_frame,"ERROR: Could not locate URL with " + 
							"getClass().getResource(\"/TR-GBG.pdf\")", 
							"XArenaMenu",JOptionPane.ERROR_MESSAGE);
					printStatus("[ERROR: Could not locate help file URL]");
				} else {
					ShowBrowser.openURL(url);
				}
			}
		});
		menuItem.setToolTipText("<html><body>Show Technical Report on GBG in your default PDF viewer</body></html>");
		menu.add(menuItem);


		add(menu);
	}
	
	void loadAgent(int index) {
		String str=TIPEVALUATE;
		PlayAgent td=null;
		try {
			td = m_arena.tdAgentIO.loadTDAgent();			
		} catch(IOException e) {
			str = e.getMessage();			
		} catch(ClassNotFoundException e) {
			str = e.getMessage();			
		} 
		
		if (td == null) {
			str = "No Agent loaded!";
			MessageBox.show(m_arena,"ERROR: " + str,
					"Load Error", JOptionPane.ERROR_MESSAGE);

		} else {
			// enable / disable certain parameter settings according to 
			// the agent name and the active game (before setting the specific
			// parameters in certain tabs with 'setFrom' from the agent loaded):
			m_arena.m_xab.setParamDefaults(td.getName(), m_arena.getGameName());
			
			if (td instanceof TDAgent) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.tdPar.setFrom( ((TDAgent) td).getTDParams() );
				if (((TDAgent) td).getOtherPar() == null ) 
					((TDAgent) td).setDefaultOtherPar();
				m_arena.m_xab.oPar.setFrom( ((TDAgent) td).getOtherPar() );
			}
//			else if (td instanceof TDNTupleAgt) {
//				// set the agent parameters in XArenaTabs:
//				m_arena.m_xab.tdPar.setFrom( ((TDNTupleAgt) td).getTDParams() );
//				m_arena.m_xab.ntPar.setFrom( ((TDNTupleAgt) td).getNTParams() );
//				if (((TDNTupleAgt) td).getOtherPar() == null ) 
//					((TDNTupleAgt) td).setDefaultOtherPar();
//				m_arena.m_xab.oPar.setFrom( ((TDNTupleAgt) td).getOtherPar() );
//				//
//				// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
//				// (WK bug fix 08/2017, they would stay otherwise at their default values, would not 
//				// get the loaded values)
//				// (may be obsolete now, 09/2017, since m_Net has no longer these params, but
//				// it doesn't hurt
//				((TDNTupleAgt) td).setTDParams(((TDNTupleAgt) td).getTDParams(), td.getMaxGameNum());
//				((TDNTupleAgt) td).setNTParams(((TDNTupleAgt) td).getNTParams());
//			}
			else if (td instanceof TDNTuple2Agt) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.tdPar.setFrom( ((TDNTuple2Agt) td).getTDParams() );
				m_arena.m_xab.ntPar.setFrom( ((TDNTuple2Agt) td).getNTParams() );
				if (((TDNTuple2Agt) td).getOtherPar() == null ) 
					((TDNTuple2Agt) td).setDefaultOtherPar();
				m_arena.m_xab.oPar.setFrom( ((TDNTuple2Agt) td).getOtherPar() );
				//
				// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
				// (WK bug fix 08/2017, they would stay otherwise at their default values, would not 
				// get the loaded values)
				// (may be obsolete now, 09/2017, since m_Net has no longer these params, but
				// it doesn't hurt
				((TDNTuple2Agt) td).setTDParams(((TDNTuple2Agt) td).getTDParams(), td.getMaxGameNum());
				((TDNTuple2Agt) td).setNTParams(((TDNTuple2Agt) td).getNTParams());
				//m_arena.m_xab.oPar.numEval_T.setText(""+((TDNTuple2Agt) td).getOtherPar().getNumEval());
			}
			else if (td instanceof MCTSAgentT) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.mctsParams.setFrom( ((MCTSAgentT) td).getParMCTS() );
				if (((MCTSAgentT) td).getOtherPar() == null ) 
					((MCTSAgentT) td).setDefaultOtherPar();
				m_arena.m_xab.oPar.setFrom( ((MCTSAgentT) td).getOtherPar() );
			}
			else if (td instanceof MCTSExpectimaxAgt) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.mctsExpectimaxParams.setFrom( ((MCTSExpectimaxAgt) td).getParMCTSE() );
				if (((MCTSExpectimaxAgt) td).getOtherPar() == null ) 
					((MCTSExpectimaxAgt) td).setDefaultOtherPar();
				m_arena.m_xab.oPar.setFrom( ((MCTSExpectimaxAgt) td).getOtherPar() );
			}
			else if (td instanceof MCAgent) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.mcParams.setFrom( ((MCAgent) td).getMCPar() );
				if (((MCAgent) td).getOtherPar() == null ) 
					((MCAgent) td).setDefaultOtherPar();
				m_arena.m_xab.oPar.setFrom( ((MCAgent) td).getOtherPar() );
			}
			else if (td instanceof MinimaxAgent) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.maxnParams.setMaxnDepth( ((MinimaxAgent) td).getDepth() );
			}
			else if (td instanceof MaxNAgent) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.maxnParams.setMaxnDepth( ((MinimaxAgent) td).getDepth() );
			}
			else if (td instanceof ExpectimaxNAgent) {
				// set the agent parameters in XArenaTabs:
				m_arena.m_xab.maxnParams.setMaxnDepth( ((MinimaxAgent) td).getDepth() );
			}
			
			if (td instanceof TDAgent || td instanceof TDNTuple2Agt /* || td instanceof TDNTupleAgt */) {
				// If it is one of the trainable agents: set maxGameNum and 
				// numEval according to the settings in the loaded agent
				// (at least maxGameNum is relevant for training): 
				m_arena.m_xab.GameNumT.setText(""+td.getMaxGameNum());
				m_arena.m_xab.oPar.numEval_T.setText(""+td.getNumEval());
			}
			if (td instanceof TDNTuple2Agt && TDNTuple2Agt.VER_3P && !(TDNTuple2Agt.MODE_3P==0)) {
				m_arena.m_xab.oPar.enableNPly(true);
				// if it is one of the older agents (before nply was added to oPar), it will
				// have nply=0. Then set nply=1: 
				if (m_arena.m_xab.oPar.getNPly()==0) {
					m_arena.m_xab.oPar.setNPly(1);
					((TDNTuple2Agt) td).getOtherPar().setNPly(1);
				}
			} else {
				m_arena.m_xab.oPar.enableNPly(false);
			}
			
			// set selector according to class loaded:
			m_arena.m_xab.setSelectedAgent(index, td.getName());
			
			m_arena.m_xfun.m_PlayAgents[index] = td;
			String strAgent = (numPlayers==2) ? "Agent-"+Types.GUI_2PLAYER_NAME[index] :
												"Agent-"+Types.GUI_PLAYER_NAME[index];
			str = "Agent "+td.getName()+" succesfully loaded to "
				+ strAgent + "!";
		}
		printStatus(str);
		System.out.println("[LoadAgent] "+str);
	}

	void saveAgent(int index) {
		try {
			// fetching the agents ensures that the actual parameters from the tabs
			// are all taken (!)
			m_arena.m_xfun.m_PlayAgents = m_arena.m_xfun.fetchAgents(m_arena.m_xab);
			AgentBase.validTrainedAgents(m_arena.m_xfun.m_PlayAgents,numPlayers);
		} catch (RuntimeException e) {
			MessageBox.show(m_arena, e.getMessage(), 
					"Error", JOptionPane.ERROR_MESSAGE);
			printStatus("Done");
			return;
		}

		PlayAgent td = m_arena.m_xfun.m_PlayAgents[index];
		String str = TIPEVALUATE;
		try {
			m_arena.tdAgentIO.saveTDAgent(td);
			str = "Saved Agent!";
		} catch(IOException e) {
			str = e.getMessage();
		}
		printStatus(str);
		System.out.println("[SaveAgent] "+str);
	}

	
/*
	private void showNTuples(int index) {
		selectedAgent = index;
		String str = "[Stop Show-Ntuples]";
		if (ticGame.state != State.SHOWNTUPLE) {
			ticGame.changeState(State.SHOWNTUPLE);
			str = "[Show N-Tuples]";
		} else
			ticGame.changeState(State.IDLE);
		printStatus(str);
	}

	private void setNTuples(int index) {
		selectedAgent = index;
		if (ticGame.state != State.SETNTUPLE) {
			ticGame.changeState(State.SETNTUPLE);
			String str = "[Set N-Tuples]";
			printStatus(str);
		} else {
			ticGame.winConfigTuples.setVisible(false);
			ticGame.changeState(State.IDLE);
		}
	}

	private void inspectLUT(int index) {
		selectedAgent = index;
		if (ticGame.state != State.INSPNTUPLE) {
			ticGame.changeState(State.INSPNTUPLE);
			String str = "[Inspect N-Tuple LUTs]";
			printStatus(str);
		} else {
			ticGame.winLUTShow.setVisible(false);
			ticGame.changeState(State.IDLE);
		}
	}
*/

	private void evaluate(int index) {
		// ensure that m_PlayAgents has the agents selected
		String str = "[Start Evaluation of PlayAgent "+index+"]";
		printStatus(str);
		PlayAgent[] paVector;
		int wrappedNPly=m_arena.m_xab.oPar.getWrapperNPly();

		try {
			// ensure with fetchAgents that for agents like MCTS a new agent with the 
			// latest settings in the MCTS pars tab is constructed:
			m_arena.m_xfun.m_PlayAgents = m_arena.m_xfun.fetchAgents(m_arena.m_xab);
			AgentBase.validTrainedAgents(m_arena.m_xfun.m_PlayAgents,numPlayers);
			paVector = m_arena.m_xfun.wrapAgents(m_arena.m_xfun.m_PlayAgents,wrappedNPly,m_arena.gb.getStateObs());
		} catch (RuntimeException e) {
			MessageBox.show(m_arena, e.getMessage(), 
					"Error", JOptionPane.ERROR_MESSAGE);
			printStatus("Done");
			return;
		}
		PlayAgent pa = paVector[index];
		if (pa instanceof HumanPlayer) {
			MessageBox.show(m_arena, "No evaluation for HumanPlayer", 
					"Error", JOptionPane.ERROR_MESSAGE);			
			printStatus("Done");
		} else {
			printStatus("Running Quick Evaluation ...");
			int qem = m_arena.m_xab.oPar.getQuickEvalMode();
			Evaluator qEvaluator = m_arena.m_xab.m_game.makeEvaluator(pa,m_arena.gb,0,qem,0);
	        qEvaluator.eval();
			str = qEvaluator.getMsg();
			System.out.println(str);
			printStatus(qEvaluator.getShortMsg());
		}
	}

	private void printStatus(String str) {
		m_arena.setStatusMessage(str);
	}

}
