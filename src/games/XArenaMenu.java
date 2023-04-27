package games;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import TournamentSystem.TSAgentManager;
import TournamentSystem.TSResultStorage;
import TournamentSystem.TSSettingsGUI2;
import controllers.AgentBase;
import controllers.HumanPlayer;
import controllers.PlayAgent;
import tools.ShowBrowser;
import tools.Types;

/**
 * Main menu for {@link Arena}.
 * 
 * @author Wolfgang Konen, TH Koeln, 2016-2020
 */
public class XArenaMenu extends JMenuBar {

	private static final String TIPEVALUATE = "";

	private static final String TIPPARAMTABS = "Show the tabbed window with parameter settings";

	private static final String TIPINSPECTLUT = "Inspect the lookup-tables (LUT) for the selected "
			+ "N-Tuple-Agent. The selected agent also must be trained for this to work";

	private static final String TIPSETNTUPLES = "<html><body>Show and change the N-Tuples or a N-Tuple-TD-Agent "
			+ "This button doesn't work for other agent-types. The selected agent also must <br>"
			+ "be initialized for this to work.  If  the used N-Tuples change, "
			+ "then the agent also must be initialized again!</body></html>";

	private static final String TIPSHOWNTUPLE = "<html><body>Show the N-Tuples for a N-Tuple-TD-Agent. This button "
			+ "doesn't work for other agent-types. The selected agent also must <br>"
			+ "be trained for this to work</body></html>";

	private static final String TIPSAVE = "<html><body>Save agent to disk. All lookup-tables and configurations of the agent "
			+ "will be saved to HDD. Make sure, that enough <br>hdd-memory is available (all lookup-tables "
			+ "can get huge sometimes)</body></html>";

	private static final String TIPLOAD = "Load agent from disk. All lookup-tables and configurations of the agent "
			+ "will be loaded and assigned to the selected agent";

	@Serial
	private static final long serialVersionUID = -7247378225730684090L;

	private final Arena m_arena;

	// private JRadioButtonMenuItem rbMenuItem;
	// private JCheckBoxMenuItem cbMenuItem;
	//private JFrame m_frame;
	// private final int selectedAgent = 0;
	// private boolean winCompVisible = false;
	private final int numPlayers;
	private TSSettingsGUI2 mTSSettingsGUI2 = null;

	public XArenaMenu(Arena arena) {
		m_arena = arena;
		numPlayers = arena.getGameBoard().getStateObs().getNumPlayers();


		generateFileMenu();
		generateAgentMenu();
		generateCompetitionMenu();
		generateTournamentMenu();
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
				m_arena.m_tabs.showParamTabs(m_arena,true,0,m_arena.m_xab.getSelectedAgent(0));
			}
		});
		menuItem.setToolTipText(TIPPARAMTABS);
		if (!m_arena.hasTrainRights())
			menuItem.setEnabled(false);			// no param tabs for Arena
		menu.add(menuItem);
		// ==============================================================
		// Exit Game
		// ==============================================================
		menuItem = new JMenuItem("Exit Game", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.destroy();
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
						m_arena.loadAgent(j,null);						
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
					m_arena.saveAgent(j,null);
				}
			});
			menuItem.setToolTipText(TIPSAVE);
			if (!m_arena.hasTrainRights())
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
			// Evaluate TD-Agent Agent i (Quick Eval)
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
//				winCompVisible = !m_arena.m_xab.winCompOptions.isVisible();
				m_arena.m_xab.winCompOptions.showOptionsComp(m_arena,true);
			}
		});
		menuItem.setToolTipText("Choose the options for single- and multi-competitions");
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
		menuItem.setEnabled(numPlayers==2);
		menu.add(menuItem);
		menuItem = new JMenuItem("Compete In All Roles");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_arena.taskState = Arena.Task.ALLCMP;
				String str = "[Start Competition All Roles]";
				printStatus(str);
			}
		});
		menuItem.setToolTipText("<html><body>Start a single competition between the selected Agents "
				+ "in all roles. The results <br>"
				+ "are printed to the console</body></html>");
		menuItem.setEnabled(numPlayers>1);
		menu.add(menuItem);

		// --- Multi-Competition is deprecated now                                 --- 
		// --- (use Tournament System or MultiTrain with suitable evaluator modes) ---
//		// ==============================================================
//		// Multi-Competition
//		// ==============================================================
//		menuItem = new JMenuItem("Multi-Competition");
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_arena.taskState = Arena.Task.MULTCMP;
//				String str = "[Start multi-Competition]";
//				printStatus(str);
//			}
//		});
//		menuItem.setToolTipText("<html><body>Start a multi-competition between the selected "
//				+ "Agents (do 'Competions' times: agent training + single competition). The results are <br>"
//				+ "printed to the console and saved to bin/TicTacToe.comp.csv.</body></html>");
//		menuItem.setEnabled(numPlayers>1);
//		menu.add(menuItem);


		// --- Multi-Training *in menu* is obsolete (we have the MultiTrain button in ArenaTrain) ---
//		menu.addSeparator();
//		
//		submenu = new JMenu("Multi-Training");
//		// ==============================================================
//		// Options for Multi-Training
//		// ==============================================================
//		menuItem = new JMenuItem("Options");
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				//ticGame.c4Buttons.setWindowPos(ticGame.winMultiTrainOptions);
//			}
//		});
//		menuItem.setToolTipText("Set Options for the multi-training. The agent that shall "
//				+ "be trained (TD) and the opponent must be selected here.");
//		if (!(m_arena instanceof ArenaTrain))
//			menuItem.setEnabled(false);			// no multi-training for Arena
//		submenu.add(menuItem);
//		if (!(m_arena instanceof ArenaTrain))
//			submenu.setEnabled(false);			// no multi-training for Arena
//
//		// ==============================================================
//		// Start Multi-Training
//		// ==============================================================
//		menuItem = new JMenuItem("Start Multi-Training");
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_arena.taskState = Arena.Task.MULTTRN;
//				String str = "[Start Multi-Training of TD-Agent]";
//				printStatus(str);
//			}
//		});
//		menuItem.setToolTipText("<html><body>Start the multi-training of the TD-Agent. Be sure that <br>"
//				+ "the options were choosen correctly and that all agents are initialized</body></html>");
//		if (!(m_arena instanceof ArenaTrain))
//			menuItem.setEnabled(false);			// no multi-training for Arena
//		submenu.add(menuItem);
//
//		menu.add(submenu);

		add(menu);
	}

	public void generateTournamentMenu() {
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("Tournament");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("GBG Tournament System");

		/*
		menuItem = new JMenuItem("Start TS GUI");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//m_arena.m_xab.helpFunction();
				//JOptionPane.showMessageDialog(null, "menu item #1 clicked");
				if(tournamentSystemGUI == null) {
					tournamentSystemGUI = new TournamentSystemGUI(m_arena.getGameBoard());
				} else {
					tournamentSystemGUI.show();
				}
			}
		});
		menuItem.setToolTipText("<html><body>Start menu item #1</body></html>");
		menu.add(menuItem);
		*/

		menuItem = new JMenuItem("Start Tournament System");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//JOptionPane.showMessageDialog(null, "menu item #2 clicked");
				if(mTSSettingsGUI2 == null) {
					mTSSettingsGUI2 = new TSSettingsGUI2(/*m_arena.logManager, m_arena.getGameBoard()*/m_arena);
				} else {
					mTSSettingsGUI2.show();
				}
			}
		});
		menuItem.setToolTipText("<html><body>Start Tournament System GUI</body></html>");
		menu.add(menuItem);
		//add(menuItem);

		menuItem = new JMenuItem("Load&Show Results from Disk");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//JOptionPane.showMessageDialog(null, "menu item #3 clicked");
				int numPlayers = m_arena.getGameBoard().getStateObs().getNumPlayers();
				TSAgentManager mTSAgentManager = new TSAgentManager(numPlayers);
				try {
					TSResultStorage tsr = m_arena.tdAgentIO.loadGBGTSResult(null);
					mTSAgentManager.loadAndShowTSFromDisk(tsr);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "ERROR : file could not be loaded");
				}
			}
		});
		menuItem.setToolTipText("<html><body>Load and Visualize saved Tournament Data from Disk</body></html>");
		menu.add(menuItem);

		add(menu);
	}

	// --- generateOptionsMenu() currently not needed ---
//	private void generateOptionsMenu() {
//		JMenu menu;
//		JMenuItem menuItem;
//
//		menu = new JMenu("Options");
//		menu.setMnemonic(KeyEvent.VK_A);
//		menu.getAccessibleContext().setAccessibleDescription(
//				"Options for Competition");
//
//		// ==============================================================
//		// Options for Game-Theoretic Values
//		// ==============================================================
//		menuItem = new JMenuItem("Game-Theoretic Values");
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				//ticGame.m_xab.setWindowPos(ticGame.winOptionsGTV);
//			}
//		});
//		menuItem.setToolTipText("(currently not implemented)");
//		menu.add(menuItem);
//
//		add(menu);
//	}

	private void generateHelpMenu() {
		JMenu menu;
		JMenuItem menuItem, menuItem2;

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("Help");

//		menuItem = new JMenuItem("Show Help-File");
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				m_arena.m_xab.helpFunction();
//				//java.net.URL url = getClass().getResource("/doc/index.htm");
//				//ShowBrowser.openURL(url);			
//			}
//		});
//		menuItem.setToolTipText("<html><body>Show Help File</body></html>");
//		menu.add(menuItem);
		
		menuItem = new JMenuItem("Help File in Browser");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResource("/HelpGUI-Arena-GBG.htm");
			}
		});
		menuItem.setToolTipText("<html><body>Show help file in your default browser</body></html>");
		menu.add(menuItem);

		menuItem = new JMenuItem("Help File as PDF");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResource("/HelpGUI-Arena-GBG.pdf");
			}
		});
		menuItem.setToolTipText("<html><body>Show help file in your default PDF viewer</body></html>");
		menu.add(menuItem);

		menuItem = new JMenuItem("Game Rules");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResource("/GameRules-GBG.htm");
			}
		});
		menuItem.setToolTipText("<html><body>Show game rules in your default browser</body></html>");
		menu.add(menuItem);

		menuItem = new JMenuItem("Game Rules as PDF");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResource("/GameRules-GBG.pdf");
			}
		});
		menuItem.setToolTipText("<html><body>Show game rules in your default PDF viewer</body></html>");
		menu.add(menuItem);

		menuItem = new JMenuItem("Show TR-GBG.pdf");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showResource("/TR-GBG.pdf");
				// --- this is now in showResource ---
//				java.net.URL url = getClass().getResource("/TR-GBG.pdf");  
//									// getResource() will find .pdf in ../resources/
//		    	InputStream is = getClass().getResourceAsStream("/TR-GBG.pdf");  
//				System.out.println("URL = " + url);
//				if (url == null) {
//					m_arena.showMessage("ERROR: Could not locate URL with " + 
//							"getClass().getResource(\"/TR-GBG.pdf\")", 
//							"XArenaMenu",JOptionPane.ERROR_MESSAGE);
//					printStatus("[ERROR: Could not locate help file URL]");
//				} else {
//					ShowBrowser.openURL(url,is);
//				}
			}
		});
		menuItem.setToolTipText("<html><body>Show Technical Report on GBG in your default PDF viewer</body></html>");
		menu.add(menuItem);


		add(menu);
	}
	
	private void showResource(String resourceName) {
		java.net.URL url = getClass().getResource(resourceName);  
				// getResource() will find resourceName in ../bin, it is automatically copied from ../resources/
				// to ../bin when Refresh - Build Project is issued
		InputStream is = getClass().getResourceAsStream(resourceName);  
		System.out.println("URL = " + url);
		if (url == null) {
			m_arena.showMessage("ERROR: Could not locate URL with " + 
					"getClass().getResource(\""+resourceName+"\")", 
					"XArenaMenu",JOptionPane.ERROR_MESSAGE);
					printStatus("ERROR: Could not locate help file URL");
		} else {
			ShowBrowser.openURL(url,is);
		}		
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

	/**
	 * Do quick evaluation
	 * 
	 * @param index		number of the player (agent)
	 */
	private void evaluate(int index) {
		// ensure that m_PlayAgents has the agents selected
		String str = "[Start Quick Evaluation of PlayAgent "+index+"]";
		printStatus(str);
		System.out.println(str);
		PlayAgent[] paVector;

		try {
			// ensure with fetchAgents that for agents like MCTS a new agent with the 
			// latest settings in the MCTS parameter tab is constructed:
			m_arena.m_xfun.m_PlayAgents = m_arena.m_xfun.fetchAgents(m_arena.m_xab);
			AgentBase.validTrainedAgents(m_arena.m_xfun.m_PlayAgents,numPlayers);
			paVector = m_arena.m_xfun.wrapAgents(m_arena.m_xfun.m_PlayAgents, 
					m_arena.m_xab, m_arena.gb.getStateObs());
		} catch (RuntimeException e) {
			m_arena.showMessage( e.getMessage(), 
					"Error", JOptionPane.ERROR_MESSAGE);
			printStatus("Done");
			return;
		}
		PlayAgent pa = paVector[index];
		if (pa instanceof HumanPlayer) {
			m_arena.showMessage( "No evaluation for HumanPlayer", 
					"Error", JOptionPane.ERROR_MESSAGE);			
			printStatus("Done");
		} else {
			printStatus("Running Quick Evaluation of PlayAgent "+index+" ...");
			// problem: this text only appears in the status bar when the action handler is left
			// ... and then it is usually obsolete, since it is already overwritten by 
			// ... printStatus(eRes.getMsg()) (see below)
			
			// measure the number of moves per second for agent pa by calling pa.getNextAction2()
			// as often as possible within one second.
			// --- deprecated, we measure now the moves/second during training or multi-train --- 
//	        int n=1;
//	        long startTime = System.currentTimeMillis();
//	        long elapsedTime = 0;
//	        long upperTime = n*1000;
//	        long moveCount = 0;
//	        StateObservation newSO, so = m_arena.gb.getDefaultStartState();
//	        ArrayList actList = so.getAvailableActions();
//	        if (so.getName()=="RubiksCube") {		  // Moves/second works for RubiksCube & MCTS only
//	        	so.advance((ACTIONS) actList.get(4)); // if 'so' is not the default start state (otherwise
//	        }										  // all children are null and an assertion fires)	
//	        while (elapsedTime<upperTime) {
//	        	pa.getNextAction2(so.partialState(), false, true);
//	        	newSO = so.copy();							// just for comparable time measurement: 
//	        	newSO.advance((ACTIONS) actList.get(0)); 	// do a dummy advance
//	        	moveCount++;
//	        	elapsedTime = System.currentTimeMillis() - startTime;
//	        	//System.out.println("elapsed: "+elapsedTime);
//	        }
//			DecimalFormat form = new DecimalFormat("0.00");
//	        double movesPerSecond = ((double)moveCount)/n/((double)elapsedTime/1000);
//	        System.out.println("Moves/second for "+ pa.getName() + ": "+form.format(movesPerSecond));
	        
			try {
				long startTime = System.currentTimeMillis();

				System.out.println(pa.stringDescr2());
				int qem = m_arena.m_xab.oPar[index].getQuickEvalMode();
				int epiLength = m_arena.m_xab.oPar[index].getStopEval();
				pa.setStopEval(epiLength);
				int verb = 0;
				Evaluator qEvaluator = m_arena.m_xab.m_arena.makeEvaluator(pa,m_arena.gb,0,qem,verb);
		        EvalResult eRes = qEvaluator.eval(pa);
				str = eRes.getMsg();
				System.out.println(str);
				printStatus(eRes.getMsg());

				long elapsedMs = (System.currentTimeMillis() - startTime);
				double elapsedTime = (double) elapsedMs / 1000.0;
				System.out.println("Quick eval runtime:  " + elapsedTime + " sec,     lastResult="+eRes.getResult());
			} catch (RuntimeException e) {
				m_arena.showMessage( e.getMessage(), 
						"Error", JOptionPane.ERROR_MESSAGE);
				printStatus("Done");
				return;
			}
		}
	}

	private void printStatus(String str) {
		m_arena.setStatusMessage(str);
	}

}
