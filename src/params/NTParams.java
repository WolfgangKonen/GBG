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

import controllers.TD.ntuple2.TDNTuple2Agt;

/**
 *  N-tuple parameters and TC (temporal coherence) parameters for agent {@link TDNTuple2Agt}.
 *  <p>
 *  Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 *  
 *  @see ParNT
 *  @see TDNTuple2Agt
 */
public class NTParams extends Frame implements Serializable {
	private static final String TIPRANDL = "n-tuple randomness: If checked, generate random n-tuples. If not, take fixed n-tuple set acc. to 'n-tuple fixed mode'";
	private static final String TIPFIXEDL = "n-tuple fixed mode: Select one of the available fixed modes";
	private static final String TIPNTUPLETYPE = "n-tuple generation method: random walk or random point";
	private static final String TIPNTUPLENUML = "How many n-tuples to generate in case of 'randomness'";
	private static final String TIPNTUPLESIZEL = "maxTupleLen: Every generated n-tuple has a size 2,...,maxTupleLen";
	private static final String TIPUSESYMMETRY = "If checked, use symmetries when training n-tuple agent";
	private static final String TIPAFTERSTATE = "If checked, use afterstate logic [Jaskowski16] when training n-tuple agent";

	private static String[] tcFactorString = { "Immediate", "Accumulating" };
	private static String[] ntTupleTypeString={"RandomWalk","RandomPoint"};
	private static String[] fixedTupleModeString={"1","2"};
	private static String[] plotWghtString = { "none", "wght distr", "tcFactor distr" };

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	JLabel TempCoL;
	JLabel tcFactorL;
	JLabel tcInitL;
	JLabel tcIntervalL;
	JLabel RandomnessL;
	JLabel NTupleFixL;
	JLabel NTupleNumL;
	JLabel NTupleSizeL;
	JLabel NTupleTypeL;
	JLabel PlotWghtL;
	JLabel UseSymmetryL;
	JLabel AfterStateL;
	//JLabel NTupleMaxL;
	//JLabel EvalL;

	public JTextField tcInitT;
	public JTextField tcIntervalT;
	public JTextField NTupleNumT;
	public JTextField NTupleSizeT;
	//public JTextField EvalT;

	public JCheckBox TempCoC;
	public JCheckBox RandomnessC;
	public JCheckBox UseSymmetryC;
	public JCheckBox AfterStateC;

	public JComboBox tcFactorType;
	public JComboBox NTupleTypeCo;
	public JComboBox NTupleFixCo;
	public JComboBox PlotWghtCo;

	JPanel ntPanel;

//	JButton ok;
//	NTParams c_par;

	public NTParams() {
		// label names
		super("Temporal Coherence Parameters");
		TempCoL = new JLabel("TC");
		tcFactorL = new JLabel("TC factor type");
		tcInitL = new JLabel("INIT");
		tcIntervalL = new JLabel("Episodes");
		NTupleNumL = new JLabel("# of nTuples");
		NTupleNumL.setToolTipText(TIPNTUPLENUML);

		NTupleSizeL = new JLabel("nTuple size");
		NTupleSizeL.setToolTipText(TIPNTUPLESIZEL);
		RandomnessL = new JLabel("nTuple randomness");
		RandomnessL.setToolTipText(TIPRANDL);
		NTupleFixL = new JLabel("nTuple fixed mode:");
		NTupleFixL.setToolTipText(TIPFIXEDL);
		NTupleFixL.setEnabled(true);
		NTupleTypeL=new JLabel("nTuple generation");
		NTupleTypeL.setToolTipText(TIPNTUPLETYPE);
		PlotWghtL=new JLabel("PLOT WEIGHTS");
		
		UseSymmetryL = new JLabel("USESYMMETRY");
		UseSymmetryL.setToolTipText(TIPUSESYMMETRY);
		AfterStateL = new JLabel("AFTERSTATE");
		AfterStateL.setToolTipText(TIPAFTERSTATE);
		
		// These are the initial defaults 
		// (Other game- and agent-specific defaults are in setParamDefaults, which is called
		// whenever one of the agent choice boxes changes to an agent requiring NTParams)
		//
		tcInitT = new JTextField(ParNT.DEFAULT_TC_INIT+"");
		tcInitT.setEnabled(false);
		tcIntervalT = new JTextField(ParNT.DEFAULT_TC_INTERVAL+"");
		tcIntervalT.setEnabled(false);
		NTupleNumT = new JTextField(ParNT.DEFAULT_NTUPLE_NUM+"");
		NTupleNumT.setEnabled(false);
		NTupleSizeT = new JTextField(ParNT.DEFAULT_NTUPLE_LEN+"");
		NTupleSizeT.setEnabled(false);
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
		AfterStateC = new JCheckBox();
		
		tcFactorType = new JComboBox(tcFactorString);
		tcFactorType.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				enableTcImmPart();
			}
		});
		
		NTupleTypeCo= new JComboBox(ntTupleTypeString);
		NTupleTypeCo.setEnabled(false);
		NTupleFixCo = new JComboBox(fixedTupleModeString);
		NTupleFixCo.setEnabled(true); 
		PlotWghtCo = new JComboBox(plotWghtString);
		PlotWghtCo.setEnabled(true); 
		
		ntPanel = new JPanel();		// put the inner buttons into panel ntPanel. This panel
									// can be handed over to a tab of a JTabbedPane object
									// (see class XArenaTabs)

//		ok = new JButton("ok");
//		c_par = this;
		
//		ok.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				c_par.setVisible(false);
//			}
//		});

		setLayout(new BorderLayout(10,0));				 // hgap,vgap
		ntPanel.setLayout(new GridLayout(0, 4, 10, 10)); // rows,columns,hgap,vgap

		// first row
		ntPanel.add(TempCoL);
		ntPanel.add(TempCoC);
		ntPanel.add(tcInitL);
		ntPanel.add(tcInitT);

		// second row
		ntPanel.add(tcFactorL);
		ntPanel.add(tcFactorType);
		ntPanel.add(tcIntervalL);
		ntPanel.add(tcIntervalT);

		// third row
		ntPanel.add(RandomnessL);
		ntPanel.add(RandomnessC);
		ntPanel.add(NTupleFixL);
		ntPanel.add(NTupleFixCo);
		
		//forth row
		ntPanel.add(NTupleTypeL);
		ntPanel.add(NTupleTypeCo);
		ntPanel.add(PlotWghtL);
		ntPanel.add(PlotWghtCo);
		
		// fifth row
		ntPanel.add(NTupleNumL);
		ntPanel.add(NTupleNumT);
		ntPanel.add(UseSymmetryL);
		ntPanel.add(UseSymmetryC);

		// sixth row
		ntPanel.add(NTupleSizeL);
		ntPanel.add(NTupleSizeT);
		ntPanel.add(AfterStateL);
		ntPanel.add(AfterStateC);
		
		add(ntPanel,BorderLayout.CENTER);
//		add(ok,BorderLayout.SOUTH);
	
		enableTcPart();
		enableRandomPart();
		
		pack();
		setVisible(false);
	} // constructor NTParams()
	
	public NTParams(NTParams ntPar) {
		this();
		this.setFrom(ntPar);
	}

	private void enableTcPart() {
		if (getTc()==false){
			tcInitL.setEnabled(false);
			tcInitT.setEnabled(false);
			tcFactorL.setEnabled(false);
			tcFactorType.setEnabled(false);
			tcIntervalL.setEnabled(false);
			tcIntervalT.setEnabled(false);
		}else{
			tcInitL.setEnabled(true);
			tcInitT.setEnabled(true);
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
			NTupleNumL.setEnabled(true);
			NTupleSizeL.setEnabled(true);
			NTupleTypeL.setEnabled(true);
			NTupleNumT.setEnabled(true);
			NTupleSizeT.setEnabled(true);
			NTupleTypeCo.setEnabled(true);
			NTupleFixL.setEnabled(false);
			NTupleFixCo.setEnabled(false);
		} else {
			NTupleNumL.setEnabled(false);
			NTupleSizeL.setEnabled(false);
			NTupleTypeL.setEnabled(false);
			NTupleNumT.setEnabled(false);
			NTupleSizeT.setEnabled(false);
			NTupleTypeCo.setEnabled(false);
			NTupleFixL.setEnabled(true);
			NTupleFixCo.setEnabled(true);
		}
	}

	private void enableAfterState(boolean enable) {
		AfterStateL.setEnabled(enable);
		AfterStateC.setEnabled(enable);
	}

	public JPanel getPanel() {
		return ntPanel;
	}
	public double getINIT() {
		return Double.valueOf(tcInitT.getText()).doubleValue();
	}

	public int getTcInterval() {
		return Integer.parseInt(tcIntervalT.getText());
	}

	public int getNtupleNumber() {
		return Integer.parseInt(NTupleNumT.getText());
	}

	public int getNtupleMax() {
		int dummy = Integer.parseInt(NTupleSizeT.getText());
		return dummy;
	}

	public int getFixedNtupleMode() {
		return Integer.parseInt((String) NTupleFixCo.getSelectedItem());
	}

	public int getPlotWeightMethod() {
		return PlotWghtCo.getSelectedIndex();
	}

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
		String Type2 = (String) NTupleTypeCo.getSelectedItem();
		if(Type2.equals(ntTupleTypeString[0])) // "RandomWalk"
			return true;
		return false;
	}
	public boolean getUseSymmetry() {
		return UseSymmetryC.isSelected();
	}
	public boolean getUseAfterState() {
		return AfterStateC.isSelected();
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param nt  of the re-loaded agent
	 */
	public void setFrom(NTParams nt) {
		setTc(nt.getTc());
		setTcInterval(""+nt.getTcInterval());
		setTcImmediate(nt.getTcImmediate());
		tcInitT.setText(""+nt.getINIT());
		//EvalT = nt.EvalT;
		RandomnessC.setSelected(nt.getRandomness());
		int ntindex= nt.getRandomWalk()?0:1;
		NTupleTypeCo.setSelectedIndex(ntindex);
		NTupleNumT.setText(nt.getNtupleNumber()+"");
		NTupleSizeT.setText(nt.getNtupleMax()+"");
		NTupleFixCo.setSelectedItem(""+nt.getFixedNtupleMode());
		UseSymmetryC.setSelected(nt.getUseSymmetry());
		AfterStateC.setSelected(nt.getUseAfterState());
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
		tcInitT.setText(""+nt.getTcInit());
		RandomnessC.setSelected(nt.getRandomness());
		int ntindex= (nt.getRandomWalk() ? 0 : 1);
		NTupleTypeCo.setSelectedIndex(ntindex);
		NTupleNumT.setText(nt.getNtupleNumber()+"");
		NTupleSizeT.setText(nt.getNtupleMax()+"");
		NTupleFixCo.setSelectedItem(""+nt.getFixedNtupleMode());
		UseSymmetryC.setSelected(nt.getUSESYMMETRY());
		AfterStateC.setSelected(nt.getAFTERSTATE());
		enableTcPart();
		enableRandomPart();
	}

	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results.
	 * 
	 * @param agentName currently only "TD-Ntuple-2"
	 * 				(for {@link TDNTuple2Agt}), 
	 * 				all other strings are without any effect
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		// currently we have here only game-specific defaults for two games (2048 , ConnectFour)
		// in the case of two agents ("TD-Ntuple[-2]" = class TDNTuple[2]Agt):
		switch (agentName) {
		case "TD-Ntuple": 
		case "TD-Ntuple-2": 
			TempCoC.setSelected(false);			// consequence: disable InitT, tcIntervalT
			tcInitT.setText("0.0001");
			tcInitT.setEnabled(false);
			tcIntervalT.setText("2");
			tcIntervalT.setEnabled(false);
			RandomnessC.setSelected(true);		// consequence: enable TupleType, nTupleNumT, nTupleMaxT
			NTupleTypeCo.setSelectedIndex(0);
			NTupleNumT.setText("10");
			NTupleFixCo.setSelectedItem(""+1);
			UseSymmetryC.setSelected(true);
			AfterStateC.setSelected(false);
			switch (gameName) {
			case "2048": 
				NTupleSizeT.setText("3");
				AfterStateC.setSelected(true);
				enableAfterState(true);
				break;
			case "ConnectFour":
				RandomnessC.setSelected(false);
			default:	//  all other
				NTupleSizeT.setText("6");	
				enableAfterState(false);		// disable AFTERSTATE for all deterministic games
				break;
			}
			enableTcPart();
			enableRandomPart();
			break;
		}
		
	}
	
	/**
	 * Set the combo box list for fixed n-tuple modes
	 * @param modeList
	 */
	public void setFixedCoList(int[] modeList) {
		NTupleFixCo.removeAllItems();
		for (int i : modeList)
			NTupleFixCo.addItem(Integer.toString(i));
	}

}
