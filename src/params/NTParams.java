package params;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.Serial;
import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;

/**
 *  N-tuple parameters and TC (temporal coherence) parameters for TD agents. 
 *  This class realizes the parameter settings (GUI tab). <br>
 *  The defaults are defined in clas {@link ParNT}.
 *  
 *  @see ParNT
 *  @see TDNTuple3Agt
 *  @see SarsaAgt
 */
public class NTParams extends Frame implements Serializable {
	private static final String TIPTC = "whether to use Temporal Coherence (TC) or not";
	private static final String TIPTCINIT = "initial value for TC accumulators N and A";
	private static final String TIPTCTRANSFER = "transfer function for tcFactor: id or TC EXP";
	private static final String TIPTCACCUMUL = "what to accumulate in counters: error signal delta or recommended weight change";
	private static final String TIPRANDL = "n-tuple randomness: If checked, generate random n-tuples. If not, take fixed n-tuple set acc. to 'n-tuple fixed mode'";
	private static final String TIPFIXEDL = "n-tuple fixed mode: Select one of the available fixed modes";
	private static final String TIPPLOTW = "whether to plot weight or TC factor distribution during training";
	private static final String TIPNTUPLETYPE = "n-tuple generation method: random walk or random point";
	private static final String TIPNTUPLENUML = "How many n-tuples to generate in case of 'randomness'";
	private static final String TIPNTUPLESIZEL = "maxTupleLen: Every generated n-tuple has a size 2,...,maxTupleLen";
	private static final String TIPUSESYMMETRY = "If checked, use symmetries when training n-tuple agent";
	private static final String TIPAFTERSTATE = "If checked, use afterstate logic [Jaskowski16] when training n-tuple agent";
	private static final String TIPNSYMMETRY = "number of symmetries to use (0: all symmetries)";

	private final static String[] tcFactorString = { "Immediate", "Accumulating" };
	private final static String[] tcTransferString = { "id", "TC EXP" };
	private final static String[] tcAccumulString = { "delta", "rec wght change" };
	private final static String[] ntTupleTypeString={"RandomWalk","RandomPoint"};
	private final static String[] fixedTupleModeString={"1","2"};
	private final static String[] plotWghtString = { "none", "wght distr", "tcFactor distr" };

	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable, or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	JLabel TempCoL;
	JLabel tcFactorL;
	JLabel tcTransferL;
	JLabel tcAccumulL;
	JLabel tcInitL;
	JLabel tcIntervalL;
	JLabel tcBetaL;
	JLabel RandomnessL;
	JLabel NTupleFixL;
	JLabel NTupleNumL;
	JLabel NTupleSizeL;
	JLabel NTupleTypeL;
	JLabel PlotWghtL;
	JLabel UseSymmetryL;
	JLabel NSymL;
	JLabel AfterStateL;

	public JTextField tcInitT;
	public JTextField tcIntervalT;
	public JTextField tcBetaT;
	public JTextField NTupleNumT;
	public JTextField NTupleSizeT;
	public JTextField NSymT;

	public JCheckBox TempCoC;
	public JCheckBox RandomnessC;
	public JCheckBox UseSymmetryC;
	public JCheckBox AfterStateC;

	public JComboBox<String> tcFactorType;
	public JComboBox<String> tcTransferType;
	public JComboBox<String> tcAccumulType;
	public JComboBox<String> NTupleTypeCo;
	public JComboBox<String> NTupleFixCo;
	public JComboBox<String> PlotWghtCo;

	JPanel ntPanel;

	public NTParams() {
		// label names
		super("Temporal Coherence Parameters");
		TempCoL = new JLabel("TC");
		TempCoL.setToolTipText(TIPTC);
		tcFactorL = new JLabel("TC factor type");
		tcTransferL = new JLabel("TC transfer");
		tcTransferL.setToolTipText(TIPTCTRANSFER);
		tcAccumulL = new JLabel("TC accumulate");
		tcAccumulL.setToolTipText(TIPTCACCUMUL);
		tcInitL = new JLabel("INIT");
		tcInitL.setToolTipText(TIPTCINIT);
		tcIntervalL = new JLabel("Episodes");
		tcBetaL = new JLabel("TC EXP beta");
 
		RandomnessL = new JLabel("nTuple randomness");
		RandomnessL.setToolTipText(TIPRANDL);
		NTupleTypeL=new JLabel("nTuple generation");
		NTupleTypeL.setToolTipText(TIPNTUPLETYPE);
		NTupleNumL = new JLabel("# of nTuples");
		NTupleNumL.setToolTipText(TIPNTUPLENUML);
		NTupleSizeL = new JLabel("nTuple size");
		NTupleSizeL.setToolTipText(TIPNTUPLESIZEL);
		NTupleFixL = new JLabel("nTuple fixed mode:");
		NTupleFixL.setToolTipText(TIPFIXEDL);
		
		PlotWghtL=new JLabel("PLOT WEIGHTS");
		PlotWghtL.setToolTipText(TIPPLOTW);
		UseSymmetryL = new JLabel("USESYMMETRY");
		UseSymmetryL.setToolTipText(TIPUSESYMMETRY);
		AfterStateL = new JLabel("AFTERSTATE");
		AfterStateL.setToolTipText(TIPAFTERSTATE);
		NSymL = new JLabel("nSym");
		NSymL.setToolTipText(TIPNSYMMETRY);
		
		// These are the initial defaults 
		// (Other game- and agent-specific defaults are in setParamDefaults, which is called
		// whenever one of the agent choice boxes changes to an agent requiring NTParams)
		//
		tcInitT = new JTextField(ParNT.DEFAULT_TC_INIT+"");
		tcInitT.setEnabled(false);
		tcIntervalT = new JTextField(ParNT.DEFAULT_TC_INTERVAL+"");
		tcIntervalT.setEnabled(false);
		tcBetaT = new JTextField(ParNT.DEFAULT_TC_BETA+"");
		tcBetaT.setEnabled(false);
		NTupleNumT = new JTextField(ParNT.DEFAULT_NTUPLE_NUM+"");
		NTupleNumT.setEnabled(false);
		NTupleSizeT = new JTextField(ParNT.DEFAULT_NTUPLE_LEN+"");
		NTupleSizeT.setEnabled(false);
		NTupleFixL.setEnabled(true);

		TempCoC = new JCheckBox();
		TempCoC.setSelected(false);
		RandomnessC = new JCheckBox();
		UseSymmetryC = new JCheckBox();

		// the following lambda's, where e is an ActionEvent, are a simpler replacement for anonymous action listeners:
		TempCoC.addActionListener( e -> enableTcPart() );
		RandomnessC.addActionListener( e -> enableRandomPart() );
		UseSymmetryC.addActionListener( e -> enableSymmetryPart() );

		NSymT = new JTextField(0+"");
		NSymT.setEnabled(false);
		AfterStateC = new JCheckBox();
		
		tcFactorType = new JComboBox<>(tcFactorString);
		tcTransferType = new JComboBox<>(tcTransferString);

		// the following lambda's, where e is an ActionEvent, are a simpler replacement for anonymous action listeners:
		tcFactorType.addActionListener( e -> enableTcImmPart() );
		tcTransferType.addActionListener( e -> enableTcTransferPart() );
		
		tcAccumulType = new JComboBox<>(tcAccumulString);
		
		NTupleTypeCo= new JComboBox<>(ntTupleTypeString);
		NTupleTypeCo.setEnabled(false);
		NTupleFixCo = new JComboBox<>(fixedTupleModeString);
		NTupleFixCo.setEnabled(true); 
		PlotWghtCo = new JComboBox<>(plotWghtString);
		PlotWghtCo.setEnabled(true); 
		
		ntPanel = new JPanel();		// put the inner buttons into panel ntPanel. This panel
									// can be handed over to a tab of a JTabbedPane object
									// (see class XArenaTabs)

		setLayout(new BorderLayout(10,0));				 // hgap,vgap
		ntPanel.setLayout(new GridLayout(0, 4, 10, 10)); // rows,columns,hgap,vgap

		// first row
		ntPanel.add(TempCoL);
		ntPanel.add(TempCoC);
		ntPanel.add(tcInitL);
		ntPanel.add(tcInitT);

		// second row
//		ntPanel.add(tcFactorL);
//		ntPanel.add(tcFactorType);
//		ntPanel.add(tcIntervalL);
//		ntPanel.add(tcIntervalT);
		ntPanel.add(tcTransferL);
		ntPanel.add(tcTransferType);
		ntPanel.add(tcBetaL);
		ntPanel.add(tcBetaT);

		// third row
		ntPanel.add(tcAccumulL);
		ntPanel.add(tcAccumulType);
		ntPanel.add(AfterStateL);
		ntPanel.add(AfterStateC);
		
		//forth row
		ntPanel.add(RandomnessL);
		ntPanel.add(RandomnessC);
		ntPanel.add(NTupleFixL);
		ntPanel.add(NTupleFixCo);
		
		//fifth row
		ntPanel.add(NTupleTypeL);
		ntPanel.add(NTupleTypeCo);
		ntPanel.add(PlotWghtL);
		ntPanel.add(PlotWghtCo);
		
		// sixth row
		ntPanel.add(NTupleNumL);
		ntPanel.add(NTupleNumT);
		ntPanel.add(UseSymmetryL);
		ntPanel.add(UseSymmetryC);

		// seventh row
		ntPanel.add(NTupleSizeL);
		ntPanel.add(NTupleSizeT);
		ntPanel.add(NSymL);
		ntPanel.add(NSymT);
//		ntPanel.add(new Canvas());
//		ntPanel.add(new Canvas());
		
		add(ntPanel,BorderLayout.CENTER);
	
		enableTcPart();
		enableRandomPart();
		enableSymmetryPart();
		pack();
		setVisible(false);
	} // constructor NTParams()
	
//	public NTParams(NTParams ntPar) {
//		this();
//		this.setFrom(ntPar);
//	}

	public void enableAll(boolean enable) {
		TempCoC.setEnabled(enable);
		tcInitT.setEnabled(enable);
		tcFactorType.setEnabled(enable);
		tcTransferType.setEnabled(enable);
		tcAccumulType.setEnabled(enable);
		tcIntervalT.setEnabled(enable);
		tcBetaT.setEnabled(enable);
		
		RandomnessC.setEnabled(enable);
		NTupleNumT.setEnabled(enable);
		NTupleSizeT.setEnabled(enable);
		NTupleTypeCo.setEnabled(enable);
		NTupleFixCo.setEnabled(enable);
		
		PlotWghtCo.setEnabled(enable);
		UseSymmetryC.setEnabled(enable);
		NSymL.setEnabled(enable);
		NSymT.setEnabled(enable);
		AfterStateC.setEnabled(enable);
		AfterStateL.setEnabled(true);

	}
	
	private void enableTcPart() {
		if (getTc()==false){
			tcInitL.setEnabled(false);
			tcInitT.setEnabled(false);
			tcFactorL.setEnabled(false);
			tcFactorType.setEnabled(false);
			tcTransferL.setEnabled(false);
			tcTransferType.setEnabled(false);
			tcAccumulL.setEnabled(false);
			tcAccumulType.setEnabled(false);
			tcIntervalL.setEnabled(false);
			tcIntervalT.setEnabled(false);
			tcBetaL.setEnabled(false);
			tcBetaT.setEnabled(false);
		}else{
			tcInitL.setEnabled(true);
			tcInitT.setEnabled(true);
			tcFactorL.setEnabled(true);
			tcFactorType.setEnabled(true);
			tcTransferL.setEnabled(true);
			tcTransferType.setEnabled(true);
			tcAccumulL.setEnabled(true);
			tcAccumulType.setEnabled(true);
			tcIntervalL.setEnabled(true);
			tcIntervalT.setEnabled(true);
			enableTcImmPart();
			enableTcTransferPart();
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
	
	private void enableTcTransferPart() {
		String transfer = (String) tcTransferType.getSelectedItem();
		if(transfer.equals(tcTransferString[0])) { // "id"
			tcBetaL.setEnabled(false);
			tcBetaT.setEnabled(false);
		} else {
			tcBetaL.setEnabled(true);
			tcBetaT.setEnabled(true);
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

	private void enableSymmetryPart() {
		if(getUSESYMMETRY()==true){
			NSymL.setEnabled(true);
			NSymT.setEnabled(true);
		} else {
			NSymL.setEnabled(false);
			NSymT.setEnabled(false);
		}					
	}
	
	private void enableAfterState(boolean enable) {
		AfterStateL.setEnabled(enable);
		AfterStateC.setEnabled(enable);
	}

	public JPanel getPanel() {
		return ntPanel;
	}
	
	public boolean getTc() {
		return TempCoC.isSelected();
	}

	public double getTcInit() {
		return Double.parseDouble(tcInitT.getText());
	}

	public int getTcInterval() {
		return Integer.parseInt(tcIntervalT.getText());
	}

	public double getTcBeta() {
		return Double.parseDouble(tcBetaT.getText());
	}

	public String getTcImmediate() {
		return (String) tcFactorType.getSelectedItem();
	}

	public boolean getTcImm() {
		Object Type = tcFactorType.getSelectedItem();
		if (Type == "Immediate")
			return true;
		return false;
	}

	public String getTcTransfer() {
		return (String) tcTransferType.getSelectedItem();
	}

	public int getTcTransferMode() {
		return tcTransferType.getSelectedIndex();
	}

	public String getTcAccumul() {
		return (String) tcAccumulType.getSelectedItem();
	}

	public int getTcAccumulMode() {
		return tcAccumulType.getSelectedIndex();
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
	
	public boolean getUSESYMMETRY() {
		return UseSymmetryC.isSelected();
	}
	
	public int getNSym() {
		int nsym = Integer.parseInt(NSymT.getText());
		return nsym;		
	}
	public boolean getAFTERSTATE() {
		return AfterStateC.isSelected();
	}
	
	public int getNtupleNumber() {
		return Integer.parseInt(NTupleNumT.getText());
	}

	public int getNtupleMax() {
		int ntmax = Integer.parseInt(NTupleSizeT.getText());
		return ntmax;
	}

	public int getFixedNtupleMode() {
		return Integer.parseInt((String) NTupleFixCo.getSelectedItem());
	}

	/**
	 * @return 	0: none, 1: plot weight distribution
	 * 			2: plot tcFactor distribution
	 */
	public int getPlotWeightMethod() {
		return PlotWghtCo.getSelectedIndex();
	}

	public void setTc(boolean tc) {
		TempCoC.setSelected(tc);
	}

	public void setTcInit(double tcInit) {
		tcInitT.setText(""+tcInit);
	}

	public void setTcTransfer(String strg) {
		tcTransferType.setSelectedItem(strg);
	}

	public void setTcAccumul(String strg) {
		tcAccumulType.setSelectedItem(strg);
	}

	public void setTcInterval(String strg) {
		tcIntervalT.setText(strg);
	}
	
	public void setTcBeta(String strg) {
		tcBetaT.setText(strg);
	}
	
	public void setTcImm(boolean tcImm) {
		tcFactorType.setSelectedIndex(tcImm ? 0 : 1);
	}

	public void setTcImmediate(String strg) {
		tcFactorType.setSelectedItem(strg);
	}

	public void setRandomness(boolean state) {
		RandomnessC.setSelected(state);
	}
	
	public void setRandomWalk(boolean randomWalk) {
		NTupleTypeCo.setSelectedIndex(randomWalk ? 0 : 1);
	}
	
	public void setUSESYMMETRY(boolean useSymmetry) {
		UseSymmetryC.setSelected(useSymmetry);
		this.enableSymmetryPart();
	}

	public void setNSym(int nSym) {
		NSymT.setText(""+nSym);
	}

	public void setAFTERSTATE(boolean useAfterstate) {
		AfterStateC.setSelected(useAfterstate);
	}
	
	public void setNtupleNumber(int numTuple) {
		NTupleNumT.setText(""+numTuple);
	}

	public void setNtupleMax(int maxTupleLen) {
		NTupleSizeT.setText(""+maxTupleLen);
	}

	public void setFixedNtupleMode(int fixedNtupleMode) {
		NTupleFixCo.setSelectedItem(""+fixedNtupleMode);
	}

	/**
	 * Set the combo box list for fixed n-tuple modes
	 * @param modeList
	 */
	public void setFixedCoList(int[] modeList, String tooltipString) {
		NTupleFixCo.removeAllItems();
		for (int i : modeList)
			NTupleFixCo.addItem(Integer.toString(i));
		NTupleFixCo.setToolTipText(tooltipString);
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param nt  of the re-loaded agent
	 */
	public void setFrom(NTParams nt) {
		setTc(nt.getTc());
		setTcInit(nt.getTcInit());
		setTcImmediate(nt.getTcImm()==true ? tcFactorString[0] : tcFactorString[1]);
		setTcInterval(""+nt.getTcInterval());
		setTcTransfer(tcTransferString[nt.getTcTransferMode()]);
		setTcAccumul(tcAccumulString[nt.getTcAccumulMode()]);
		setTcBeta(nt.getTcBeta()+"");
		setRandomness(nt.getRandomness());
		setRandomWalk(nt.getRandomWalk());
		setNtupleNumber(nt.getNtupleNumber());
		setNtupleMax(nt.getNtupleMax());
		setFixedNtupleMode(nt.getFixedNtupleMode());
		setUSESYMMETRY(nt.getUSESYMMETRY());
		setAFTERSTATE(nt.getAFTERSTATE());

		enableAfterState(nt.getAFTERSTATE());
		enableTcTransferPart();
		enableTcPart();
		enableRandomPart();
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param nt  of the re-loaded agent
	 */
	public void setFrom(ParNT nt) {
		setTc(nt.getTc());
		setTcInit(nt.getTcInit());
		setTcImmediate(nt.getTcImm()==true ? tcFactorString[0] : tcFactorString[1]);
		setTcInterval(""+nt.getTcInterval());
		setTcTransfer(tcTransferString[nt.getTcTransferMode()]);
		setTcAccumul(tcAccumulString[nt.getTcAccumulMode()]);
		setTcBeta(nt.getTcBeta()+"");
		setRandomness(nt.getRandomness());
		setRandomWalk(nt.getRandomWalk());
		setNtupleNumber(nt.getNtupleNumber());
		setNtupleMax(nt.getNtupleMax());
		setFixedNtupleMode(nt.getFixedNtupleMode());
		setUSESYMMETRY(nt.getUSESYMMETRY());
		setNSym(nt.getNSym());
		setAFTERSTATE(nt.getAFTERSTATE());
		
		enableAfterState(nt.getAFTERSTATE());
		enableTcTransferPart();
		enableTcPart();
		enableRandomPart();
	}

//	/**
//	 * DEPRECATED: use {@link ParNT#setParamDefaults(String, String)} instead.
//	 * <p>
//	 * Set sensible parameters for a specific agent and specific game. By "sensible
//	 * parameters" we mean parameter producing good results.
//	 * 
//	 * @param agentName currently only "TD-Ntuple-2","TD-Ntuple-3","Sarsa"
//	 * 				all other strings are without any effect
//	 * @param gameName the string from {@link games.StateObservation#getName()}
//	 */
//	@Deprecated
//	public void setParamDefaults(String agentName, String gameName) {
//		switch (agentName) {
//		case "TD-Ntuple-2": 
//		case "TD-Ntuple-3": 
//		case "Sarsa":
//			TempCoC.setSelected(false);			// consequence: disable InitT, tcIntervalT
//			tcInitT.setText("0.0001");
//			setTcImmediate(tcFactorString[0]);	// "Immediate"
//			tcIntervalT.setText("2");
//			setTcTransfer(tcTransferString[1]);	// "TC EXP"
//			tcBetaT.setText("2.7");
//			setTcAccumul(tcAccumulString[1]);	// "rec wght change"
//			RandomnessC.setSelected(true);		// consequence: enable TupleType, nTupleNumT, nTupleMaxT
//			NTupleTypeCo.setSelectedIndex(0);
//			NTupleNumT.setText("10");
//			NTupleSizeT.setText("6");	
//			NTupleFixCo.setSelectedItem(""+1);
//			UseSymmetryC.setSelected(true);
//			AfterStateC.setSelected(false);		// disable AFTERSTATE for all deterministic games
//			enableAfterState(false);
//			switch (gameName) {
//			case "TicTacToe": 
//				NTupleNumT.setText("1");
//				NTupleSizeT.setText("9");	
//				break;
//			case "Nim": 
//				NTupleNumT.setText("1");
//				NTupleSizeT.setText(ArenaNim.getNumberHeaps()+"");	
//				RandomnessC.setSelected(false);	// use fixed n-tuples, mode==1
//				break;
//			case "2048": 
//				NTupleSizeT.setText("3");
//				AfterStateC.setSelected(true);
//				enableAfterState(true);
//				break;
//			case "ConnectFour":
//				TempCoC.setSelected(true);
//				RandomnessC.setSelected(false);
//				break;
//			}
//			enableTcTransferPart();
//			enableTcPart();
//			enableRandomPart();
//			break;
//		}
//		
//	}
	
}
