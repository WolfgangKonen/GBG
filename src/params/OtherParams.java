package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import games.Feature;

/**
 * This class realizes other parameter settings for board games.<p>
 * 
 * These parameters and their [defaults] are: <ul>
 * <li> <b>numEval</b>: 	[100] During training: Call the evaluators every NumEval episodes  
 * <li> <b>stopTest</b>: 	[  0] whether to perform the stop test during training. If
 * 		{@literal> 0}, then m_evaluator2 is checked during training whether its goal is reached
 * <li> <b>stopEval</b>: 	[100] During training: How many successful evaluator 
 * 							calls are needed to stop training prematurely?
 * <li> <b>MinimaxDepth</b>	[ 10] Minimax Tree Depth
 * <li> <b>use Hashmap</b>	[true] Minimax: whether to use hashmap or not
 * </ul> 
 * 
 * @see controllers.TD.TDAgent
 * @see games.XArenaButtons
 */
public class OtherParams extends Frame 
{
	private static final long serialVersionUID = 1L;
	
	JLabel evalQ_L;
	public Choice choiceEvalQ;
	JLabel evalT_L;
	public Choice choiceEvalT;

	JLabel numEval_L;
	JLabel epiLeng_L;
	JLabel stopTest_L;
	JLabel stopEval_L;
	JLabel batchL;
	JLabel miniDepth_L;
	JLabel miniUseHm_L;
	public JTextField numEval_T;
	public JTextField epiLeng_T;
	public JTextField stopTest_T;
	public JTextField stopEval_T;
	public JTextField miniDepth_T;
	//CheckboxGroup cbgUseHashmap;
	public Checkbox miniUseHmTrue;
	//public Checkbox miniUseHmFalse;
	Choice choiceBatch;
	Button ok;
	JPanel oPanel;
	OtherParams m_par;
	
	public OtherParams(int batchMax) {
		super("Other Parameter");
		
		evalQ_L = new JLabel("Quick Eval Mode");
		this.choiceEvalQ = new Choice();
		evalT_L = new JLabel("Train Eval Mode");
		this.choiceEvalT = new Choice();

		numEval_T = new JTextField("500");				// 
		epiLeng_T = new JTextField("-1");				// 
		stopTest_T = new JTextField("0");				// the defaults
		stopEval_T = new JTextField("100");				// 
		miniDepth_T = new JTextField("10");				// 
		numEval_L = new JLabel("numEval");
		epiLeng_L = new JLabel("Episode Length");
		stopTest_L = new JLabel("stopTest");
		stopEval_L = new JLabel("stopEval");
		batchL = new JLabel("BatchNum");
		miniDepth_L = new JLabel("Minimax Depth");
		miniUseHm_L = new JLabel("Minimax Hash ");
		//cbgUseHashmap = new CheckboxGroup();
		miniUseHmTrue = new Checkbox("use hashmap",true);
		//miniUseHmFalse = new Checkbox("false",cbgUseHashmap,false);
		ok = new Button("OK");
		m_par = this;
		oPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class TicTacToeTabs)
		
		evalQ_L.setToolTipText("'Quick Evaluation' evaluator has this mode");
		evalT_L.setToolTipText("additional evaluator with this mode during training (none if mode is the same as Quick Eval Mode)");
		numEval_L.setToolTipText("During training: Call the evaluators every NumEval episodes");
		epiLeng_L.setToolTipText("During training: Maximum number of moves in an episode. If reached, game terminates prematurely. -1: never terminate.");
		stopTest_L.setToolTipText("During training: If >0 then perform stop test");
		stopEval_L.setToolTipText("During training: How many successfull evaluator calls are needed to stop training prematurely?");
		miniDepth_L.setToolTipText("Minimax tree depth");
		miniUseHm_L.setToolTipText("Minimax: use hashmap to save values of visited states");
		
		ok.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						m_par.setVisible(false);
					}
				}					
		);

		this.choiceBatch = new Choice();
		for (int i=1; i<=batchMax; i++) choiceBatch.add(i+""); 
		choiceBatch.select(batchMax+"");

		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		oPanel.setLayout(new GridLayout(0,4,10,10));		
		
		oPanel.add(evalQ_L);
		oPanel.add(choiceEvalQ);
		oPanel.add(evalT_L);
		oPanel.add(choiceEvalT);

		oPanel.add(numEval_L);			
		oPanel.add(numEval_T);
		oPanel.add(epiLeng_L);			
		oPanel.add(epiLeng_T);

		oPanel.add(stopTest_L);
		oPanel.add(stopTest_T);
		oPanel.add(stopEval_L);
		oPanel.add(stopEval_T);

		oPanel.add(new Canvas());			// add two empty rows to balance height of fields
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		oPanel.add(miniDepth_L);
		oPanel.add(miniDepth_T);
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
//		oPanel.add(batchL);
//		oPanel.add(choiceBatch);

		oPanel.add(miniUseHm_L);
		oPanel.add(miniUseHmTrue);
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());
		oPanel.add(new Canvas());

		add(oPanel,BorderLayout.CENTER);
		add(ok,BorderLayout.SOUTH);
				
		pack();
		setVisible(false);
	} // constructor OtherParams()	
	
	public JPanel getPanel() {
		return oPanel;
	}
	public int getQuickEvalMode() {
		String s = choiceEvalQ.getSelectedItem();
		return Integer.valueOf(s).intValue();
	}
	public int getTrainEvalMode() {
		String s = choiceEvalT.getSelectedItem();
		return Integer.valueOf(s).intValue();
	}
	public int getStopTest() {
		return Integer.valueOf(stopTest_T.getText()).intValue();
	}
	public int getStopEval() {
		return Integer.valueOf(stopEval_T.getText()).intValue();
	}
	public int getMinimaxDepth() {
		return Integer.valueOf(miniDepth_T.getText()).intValue();
	}
	public int getBatchMode() {
		return Integer.parseInt(choiceBatch.getSelectedItem());
	}
	public int getNumEval() {
		return Integer.valueOf(numEval_T.getText()).intValue();
	}
	public int getEpiLength() {
		int elen = Integer.valueOf(epiLeng_T.getText()).intValue();
		if (elen==-1) elen=Integer.MAX_VALUE;
		return elen;
	}
	public boolean usesHashmap() {
		return miniUseHmTrue.getState();
	}
	
	public void setQuickEvalMode(int qEvalmode) {
		//If the mode list has not been initialized, add the selected mode to the list
		if (choiceEvalQ.getItemCount() == 0){
			choiceEvalQ.add(Integer.toString(qEvalmode));
		}
		choiceEvalQ.select(qEvalmode+"");
	}
	public void setQuickEvalList(int[] modeList){
		for (int i : modeList) choiceEvalQ.add(Integer.toString(i));
	}
	public void setTrainEvalMode(int tEvalmode) {
		//If the mode list has not been initialized, add the selected mode to the list
		if (choiceEvalT.getItemCount() == 0){
			choiceEvalT.add(Integer.toString(tEvalmode));
		}
		choiceEvalT.select(tEvalmode+"");
	}
	public void setTrainEvalList(int[] modeList){
		for (int i : modeList) choiceEvalT.add(Integer.toString(i));
	}
	
	public void setStopTest(double value) {
		stopTest_T.setText(value+"");
	}
	public void setStopEval(double value) {
		stopEval_T.setText(value+"");
	}
	public void setNumEval(double value) {
		numEval_T.setText(value+"");
	}
	public void setEpiLength(double value) {
		numEval_T.setText(value+"");
	}
	public void setMinimaxDepth(int value) {
		miniDepth_T.setText(value+"");
	}
} // class OtherParams
