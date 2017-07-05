package params;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.ntuple.TDNTupleAgt;

/**
 *  N-tuple parameters and TC (temporal coherence) parameters for agent {@link TDNTupleAgt}
 *  
 *  @see ParNT
 *  @see TDNTupleAgt
 */
public class NTParams extends Frame implements Serializable {
	private static final String TIPRANDL = "If checked, generate random n-tuples. If not, take a hard-coded n-tuple set (int[][] nTuple in TDSNPlayer)";
	private static final String TIPNTUPLETYPE = "n-tuple generation method: random walk or random point";
	private static final String TIPNTUPLENUML = "How many n-tuples to generate in case of 'randomness'";
	private static final String TIPNTUPLESIZEL = "maxTupleLen: Every generated n-tuple has a size 2,...,maxTupleLen";
	private static final String TIPUSESYMMETRY = "If checked, use symmetries when training nTuple agent in TDSNPlayer)";

	private static String[] tcFactorString = { "Immediate", "Accumulating" };
	private static String[] ntTupleTypeString={"RandomWalk","RandomPoint"};

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	JLabel TempCoL;
	JLabel tcFactorL;
	JLabel NTupleL;
	JLabel NTupleNumL;
	JLabel NTupleSizeL;
	JLabel RandL;
	JLabel InitL;
	JLabel tcIntervalL;
	//JLabel EvalL;
	JLabel NTupleType;
	JLabel UseSymL;
	JPanel tcPanel;
	// JLabel NTupleMaxL;

	public JTextField InitT;
	public JTextField tcIntervalT;
	public JTextField nTupleNumT;
	public JTextField nTupleMaxT;
	//public JTextField EvalT;

	public JCheckBox TempCoC;
	public JCheckBox RandomnessC;
	public JCheckBox UseSymmetryC;

	public JComboBox tcFactorType;
	public JComboBox TupleType;

	JButton ok;

	NTParams c_par;

	public NTParams() {
		// label names
		super("Temporal Coherence Parameters");
		TempCoL = new JLabel("TC");
		tcFactorL = new JLabel("TC factor type");
		InitL = new JLabel("INIT");
		tcIntervalL = new JLabel("Episodes");
		NTupleL = new JLabel("nTuple:");
		NTupleNumL = new JLabel("# of nTuples");
		NTupleNumL.setToolTipText(TIPNTUPLENUML);

		NTupleSizeL = new JLabel("nTuple size");
		NTupleSizeL.setToolTipText(TIPNTUPLESIZEL);
		RandL = new JLabel("randomness");
		RandL.setToolTipText(TIPRANDL);
		//EvalL = new JLabel("Evaluting Interval");
		NTupleType=new JLabel("nTuple generation");
		NTupleType.setToolTipText(TIPNTUPLETYPE);

		UseSymL = new JLabel("USESYMMETRY");
		UseSymL.setToolTipText(TIPUSESYMMETRY);
		
		// These are the initial defaults 
		// (Other game- and agent-specific defaults are in setParamDefaults, which is called
		// whenever one of the agent choice boxes changes to an agent requiring NTParams)
		//
		InitT = new JTextField("0.0001");
		InitT.setEnabled(false);
		tcIntervalT = new JTextField("2");
		tcIntervalT.setEnabled(false);
		nTupleNumT = new JTextField("10");
		nTupleNumT.setEnabled(false);
		nTupleMaxT = new JTextField("6");
		nTupleMaxT.setEnabled(false);
		//EvalT = new JTextField("100");
		//EvalT.setEnabled(false);

		TempCoC = new JCheckBox();
		TempCoC.setSelected(false);
		TempCoC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableTcPart();
			}

		});
		RandomnessC = new JCheckBox();
		RandomnessC.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				enableRandomPart();
			}
		});

		UseSymmetryC = new JCheckBox();
		
		tcFactorType = new JComboBox(tcFactorString);
		tcFactorType.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				enableTcImmPart();
			}
		});
		
		TupleType= new JComboBox(ntTupleTypeString);
		TupleType.setEnabled(false);
		ok = new JButton("ok");
		c_par = this;
		tcPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class TicTacToeTabs)

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				c_par.setVisible(false);
			}
		});

		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		tcPanel.setLayout(new GridLayout(0, 4, 10, 10)); // rows,columns,hgap,vgap

		// first row
		tcPanel.add(TempCoL);
		tcPanel.add(TempCoC);
		tcPanel.add(InitL);
		tcPanel.add(InitT);

		// second row
		tcPanel.add(tcFactorL);
		tcPanel.add(tcFactorType);
		tcPanel.add(tcIntervalL);
		tcPanel.add(tcIntervalT);

		// third row
		tcPanel.add(NTupleL);
		tcPanel.add(RandL);
		tcPanel.add(RandomnessC);
		tcPanel.add(new Canvas());
		
		//forth row
		tcPanel.add(NTupleType);
		tcPanel.add(TupleType);
		tcPanel.add(new Canvas());
		tcPanel.add(new Canvas());
		// fifth row
		tcPanel.add(NTupleNumL);
		tcPanel.add(nTupleNumT);
		tcPanel.add(NTupleSizeL);
		tcPanel.add(nTupleMaxT);

		// sixth row
//		tcPanel.add(EvalL);
//		tcPanel.add(EvalT);
		tcPanel.add(UseSymL);
		tcPanel.add(UseSymmetryC);
		tcPanel.add(new Canvas());
		tcPanel.add(new Canvas());
		
		add(tcPanel,BorderLayout.CENTER);
		add(ok,BorderLayout.SOUTH);
	
		enableTcPart();
		
		pack();
		setVisible(false);
	} // constructor NTParams()
	
	public NTParams(NTParams ntPar) {
		this();
		this.setFrom(ntPar);
	}

	private void enableTcPart() {
		if (getTc()==false){
			InitL.setEnabled(false);
			InitT.setEnabled(false);
			tcFactorL.setEnabled(false);
			tcFactorType.setEnabled(false);
			tcIntervalL.setEnabled(false);
			tcIntervalT.setEnabled(false);
		}else{
			InitL.setEnabled(true);
			InitT.setEnabled(true);
			tcFactorL.setEnabled(true);
			tcFactorType.setEnabled(true);
			tcIntervalL.setEnabled(true);
			tcIntervalT.setEnabled(true);
			enableTcImmPart();
		}
	}

	private void enableTcImmPart() {
		if(getTcImm()==false){
			tcIntervalL.setEnabled(true);
			tcIntervalT.setEnabled(true);
		} else {
			tcIntervalL.setEnabled(false);
			tcIntervalT.setEnabled(false);
		}					
	}
	
	private void enableRandomPart() {
		if(getRandomness()==true){
			nTupleNumT.setEnabled(true);
			nTupleMaxT.setEnabled(true);
			TupleType.setEnabled(true);
		} else {
			nTupleNumT.setEnabled(false);
			nTupleMaxT.setEnabled(false);
			TupleType.setEnabled(false);
		}
	}
	
	public JPanel getPanel() {
		return tcPanel;
	}
	public double getINIT() {
		return Double.valueOf(InitT.getText()).doubleValue();
	}

	public int getTcInterval() {
		return Integer.parseInt(tcIntervalT.getText());
	}

	public int getNtupleNumber() {
		return Integer.parseInt(nTupleNumT.getText());
	}

	public int getNtupleMax() {
		int dummy = Integer.parseInt(nTupleMaxT.getText());
		return dummy;
	}

// /WK/02/2015: never used:
//	public double getTestInterval() {
//		return Double.valueOf(EvalT.getText()).doubleValue();
//	}

	public boolean getTc() {
		return TempCoC.isSelected();
	}

	public void setTc(boolean state) {
		TempCoC.setSelected(state);
	}

	public void setTcImmediate(String strg) {
		tcFactorType.setSelectedItem(strg);
	}

	public String getTcImmediate() {
		return (String) tcFactorType.getSelectedItem();
	}

	public void setTcInterval(String strg) {
		tcIntervalT.setText(strg);
	}
	
	public boolean getTcImm() {
		Object Type = tcFactorType.getSelectedItem();
		if (Type == "Immediate")
			return true;
		return false;
	}

	public void setTcImm(boolean tcImm) {
		tcFactorType.setSelectedItem(tcImm ? 0 : 1);
	}

	public boolean getRandomness() {
		return RandomnessC.isSelected();
	}
	public boolean getRandomWalk() {
		//Object Type=TupleType.getSelectedItem();			// /WK/ Bug fix: this did not work, need to test on String equalness
		//if(Type==ntTupleTypeString[0]) // "RandomWalk"
		String Type2 = (String) TupleType.getSelectedItem();
		if(Type2.equals(ntTupleTypeString[0])) // "RandomWalk"
			return true;
		return false;
	}
	public boolean getUseSymmetry() {
		return UseSymmetryC.isSelected();
	}
	// public void setINIT(double value) {
	// InitT.setText(value+"");
	// }
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param nt  of the re-loaded agent
	 */
	public void setFrom(NTParams nt) {
		setTc(nt.getTc());
		setTcInterval(""+nt.getTcInterval());
		setTcImmediate(nt.getTcImmediate());
		InitT.setText(""+nt.getINIT());
		//EvalT = nt.EvalT;
		RandomnessC.setSelected(nt.getRandomness());
		int ntindex= nt.getRandomWalk()?0:1;
		TupleType.setSelectedIndex(ntindex);
		nTupleNumT.setText(nt.getNtupleNumber()+"");
		nTupleMaxT.setText(nt.getNtupleMax()+"");
		UseSymmetryC.setSelected(nt.getUseSymmetry());
		enableTcPart();
		enableRandomPart();
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param nt  of the re-loaded agent
	 */
	public void setFrom(ParNT nt) {
		setTc(nt.getTc());
		setTcInterval(""+nt.getTcInterval());
		setTcImmediate(nt.getTcImm()==true ? tcFactorString[0] : tcFactorString[1]);
		InitT.setText(""+nt.getTcInit());
		RandomnessC.setSelected(nt.getRandomness());
		int ntindex= (nt.getRandomWalk() ? 0 : 1);
		TupleType.setSelectedIndex(ntindex);
		nTupleNumT.setText(nt.getNtupleNumber()+"");
		nTupleMaxT.setText(nt.getNtupleMax()+"");
		UseSymmetryC.setSelected(nt.getUseSymmetry());
		enableTcPart();
		enableRandomPart();
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results.
	 * 
	 * @param agentName currently only "TD-Ntuple" (for {@link TDNTupleAgt}), 
	 * 				all other strings are without any effect
	 * @param gameName
	 */
	public void setParamDefaults(String agentName, String gameName) {
		// currently we have here only the sensible defaults for one game (TTT)
		// and one agent ("TD-Ntuple" = class TDNTupleAgt):
		switch (agentName) {
		case "TD-Ntuple": 
			TempCoC.setSelected(false);			// consequence: disable InitT, tcIntervalT
			InitT.setText("0.0001");
			InitT.setEnabled(false);
			tcIntervalT.setText("2");
			tcIntervalT.setEnabled(false);
			RandomnessC.setSelected(false);		// consequence: disable TupleType, nTupleNumT, nTupleMaxT
			nTupleNumT.setText("10");
			nTupleMaxT.setText("6"); 
			enableTcPart();
			enableRandomPart();
			break;
		}
		
	}
	
}
