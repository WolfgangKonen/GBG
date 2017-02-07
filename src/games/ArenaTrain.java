package games;

import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingConstants;

import controllers.AgentBase;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.Arena.Task;
import games.TicTacToe.LaunchAppletTTT;
import params.TDParams;
import tools.MessageBox;
import tools.StatusBar;
import tools.Types;


/**
 * This class contains the GUI for the arena with train capabilities.
 * It extends the task dispatcher of {@link Arena} with the function 
 * {@link #performArenaDerivedTasks()} which contains tasks to trigger functions for
 * agent learning, parameterization, inspection and so on. <p>
 * The functions are implemented in {@link XArenaFuncs}.
 * The GUI for buttons and choice boxes is in {@link XArenaButtons}. <p>
 *
 * Run this class either as applet (from {@link LaunchAppletTTT}) or as main (from 
 * {@link LaunchTrainTTT}) for the TicTacToe game.
 * 
 * @see XArenaButtons 
 * @see XArenaFuncs
 * @see LaunchAppletTTT 
 * @see LaunchTrainTTT 
 * 
 * @author Wolfgang Konen, TH Köln, Nov'16
 *
 */
abstract public class ArenaTrain extends Arena  
{
	private static final long serialVersionUID = 1L;
	private Thread playThreadAT = null;
	
	public ArenaTrain() {
		super();
		initArenaTrain();
	}
	public ArenaTrain(JFrame frame) {
		super(frame);
		initArenaTrain();
	}
	
	private void initArenaTrain() {
		m_title.setText("ArenaTrain");
		int n = this.m_xab.choiceAgent.length;
		for (int i=0; i<n; i++) {
			this.m_xab.choiceAgent[i].setEnabled(true);
			this.m_xab.mParam[i].setVisible(true);
			this.m_xab.mTrain[i].setVisible(true);
		}
		this.m_xab.GameNumT.setEnabled(true);	
		this.m_xab.TrainNumT.setEnabled(true);		
		this.enableButtons(true);
	}
	/**
	 * This method uses the member taskState from parent class {@link Arena}, 
	 * performs several actions only appropriate for {@link ArenaTrain} 
	 * and - importantly - changes taskState back to IDLE (when appropriate)
	 * 
	 * @see Arena
	 */
	@Override
	public void performArenaDerivedTasks() {
		String agentN;
		int n;
		switch (taskState) {
		case PARAM: 
			n = m_xab.getNumParamBtn();
			agentN = m_xab.getSelectedAgent(n);
			setStatusMessage("Params for "+agentN+ " ...");
			m_tabs.showParamTabs(this,agentN);
			taskState = Task.IDLE; 
			break;
		case TRAIN: 
			n = m_xab.getNumTrainBtn();
			agentN = m_xab.getSelectedAgent(n);
			if(!agentN.equals("MCTS") & !agentN.equals("Human")) {
				enableButtons(false);	
				setStatusMessage("Training "+agentN+"-Player X ...");

				try {
					m_xfun.m_PlayAgents[n] = m_xfun.train(agentN, m_xab, gb);
				} catch (IOException e2) {
					e2.printStackTrace();
				}

				if (m_xfun.m_PlayAgents[n] != null) {
					Evaluator m_evaluator2 = makeEvaluator(m_xfun.m_PlayAgents[n],gb,0,2,1);
					m_evaluator2.eval();
					m_xfun.m_PlayAgents[n].setAgentState(AgentState.TRAINED);
					setStatusMessage(m_evaluator2.getMsg());
				} else {
					setStatusMessage("Done.");
				}

				enableButtons(true);
			}
			taskState = Task.IDLE; 
			break;
		case MULTTRN:
			enableButtons(false);
			
	        setStatusMessage("MultiTrain started ...");
			try {
				
				m_xfun.multiTrain(m_xab.getSelectedAgent(0), m_xab, gb);
			
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (m_xfun.m_PlayAgents[0]==null) {
		        setStatusMessage("Done.");
			} else {
				m_xfun.m_PlayAgents[0].setAgentState(AgentState.TRAINED);
		        setStatusMessage("MultiTrain finished: "+ m_xfun.getLastMsg());
			}

			enableButtons(true);
			taskState = Task.IDLE; 
			UpdateBoard();
			break;	
//		case INSPECTNTUP:
//			gb.clearBoard(false,true);
//			InspectNtup();
//			state = Task.IDLE; 
//			break;
		
		}
		
		performArenaTrainDerivedTasks();

	}
	
	/**
	 * This (empty) method is called from {@link #performArenaDerivedTasks()} and it 
	 * may to be overridden by classes derived from {@link ArenaTrain}. <p>
	 * 
	 * It allows to add additional tasks to the task switch.
	 * 
	 * This method will use member {@code taskState} from {@link Arena}. 
	 * It performs several actions appropriate for the derived class 
	 * and - importantly - changes taskState back to IDLE (when appropriate)
	 * 
	 * @see Arena
	 */
	public void performArenaTrainDerivedTasks() {
		
	}

// *TODO* --- this may be integrated later in the general interface ---
//
//	/**
//	 * Inspect the N-tuple states and their LUT weights for agents using N-tuples 
//	 * (currently {@link TDSNPlayer}, {@link TD_NTPlayer} or (deprecated) {@link TDSPlayer} with featmode==8).
//	 * <p>
//	 * Based on the current N-tuple situation, construct and display a {@link NTupleShow} object.
//	 * 
//	 * @see NTupleShow
//	 * @see TicGameButtons#nTupShowAction()
//	 */
//	protected void InspectNtup() {
//		String pa_string = m_TTT.m_PlayAgentX.getClass().getName();
//		System.out.println("[InspectNtup] "+pa_string);
//		NTuple[] nTuples = null;
//		if (pa_string=="TicTacToe.TDSNPlayer") {
//			nTuples = ((TDSNPlayer) m_TTT.m_PlayAgentX).getNTuples();
//		}
//		if (pa_string=="TicTacToe.TD_NTPlayer") {
//			((TD_NTPlayer) m_TTT.m_PlayAgentX).copyWeights();
//			nTuples = ((TD_NTPlayer) m_TTT.m_PlayAgentX).getNTuples();
//		}
//		if (pa_string=="controllers.TD.TDPlayerTTT" && m_TTT.m_PlayAgentX.getFeatmode()==8) {
//			((TDSPlayer) m_TTT.m_PlayAgentX).copyWeights();
//			nTuples = ((TicTDBase) m_TTT.m_PlayAgentX).getNTuples();
//		}
//		if (nTuples!=null) {
//			m_xab.ntupleShow = new NTupleShow(nTuples,m_xab);
//			m_xab.nTupShowAction();
//		}
//		else System.out.println("[InspectNtup] Warning: nTuples==null!");
//
//	}
	
}


