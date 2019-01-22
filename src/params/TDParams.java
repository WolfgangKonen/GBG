package params;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;

/**
 * This class realizes the parameter settings (GUI tab) for TD players 
 * (agents {@link TDAgent} and {@link TDNTuple2Agt}).
 * These parameters and their [defaults] are: <ul>
 * <li> <b>alpha</b>: 		[0.001] initial strength of learning parameter 
 * <li> <b>alphaFinal</b>: 	[0.001] final strength of learning parameter  
 * <li> <b>epsilon</b>: 	[0.1] initial probability of random move 
 * <li> <b>epsilonFinal</b>:[0.0] final probability of random move  
 * <li> <b>lambda</b>: 		[0.0] eligibility trace parameter 
 * <li> <b>gamma</b>: 		[1.0] discount parameter 
 * </ul> 
 * The defaults are defined in {@link ParTD}. <br>
 * Game- and agent-specific parameters are set with {@link #setParamDefaults(String, String)}.
 * 
 * @see ParTD
 * @see TDAgent
 * @see TDNTuple2Agt
 */
public class TDParams extends Frame implements Serializable
{
	private static final String TIPGAMMAL = "Discount factor in range [0,1] ";
	private static final String TIPEPOCHL = "Accumulate gradient for Epochs iterations, then update weights";
	private static final String TIPNORMALIZEL = "Normalize StateObservation's game score to the range of the agent's sigmoid function";
	private static final String TIPALPHA1L = "Initial learn step size";
	private static final String TIPALPHA2L = "Final learn step size";
	private static final String TIPEPSIL1L = "Initial random move rate in [0,1]";
	private static final String TIPEPSIL2L = "Final random move rate in [0,1]";
	private static final String TIPLAMBDAL = "Eligibility trace parameter in [0,1]";
	private static final String TIPHORCUTL = "Horizon cut: neglect eligibility terms with lambda^k < cut";
	private static final String TIPELIGTYPE = "Eligibility trace type: normal or reset on random move";
	
	
	private static String[] lrnTypeString = { "backprop","RPROP" };
	private static String[] neuralNetString = { "linear","neural net" };
	private static String[] eligModeString = { "ET","RESET"}; // ,"REPLACE","RES+REP" };
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable or you have to provide a special version transformation)
	 */
	private static final long serialVersionUID = 1L;

	JLabel alphaL;
	JLabel alfinL;
	JLabel epsilL;
	JLabel epfinL;
	JLabel lambdaL;
	JLabel horcutL;
	JLabel gammaL;
	JLabel epochL;
	JLabel tNply_L;
	JLabel mode3P_L;
	JPanel tdPanel;
	private JTextField alphaT;
	private JTextField alfinT;
	private JTextField epsilT;
	private JTextField epfinT;
	private JTextField lambdaT;
	private JTextField horcutT;
	private JTextField gammaT;
	private JTextField epochT;
	private JTextField tNply_T;
	private JTextField mode3P_T;
	
	JLabel SigTypeL;
	JLabel NormalizeL;
	JLabel NetTypeL;
	JLabel LrnTypeL;
	JLabel eligTypeL;
	public JCheckBox withSigType;
	public JCheckBox normalize;
	public JComboBox choiceLrnType;
	public JComboBox choiceNetType;
	public JComboBox eligModeType;
	JLabel FeatTDS_L;
	public JComboBox choiceFeatTDS;
	String FeatTDS;
	
// -- obsolete, they are now stored in AgentBase
//
//	// These two members are here only to save these settings (from other param tabs) 
//	// together with the agent, so that we can restore them later on load (at least 
//	// maxGameNum is relevant for training): 
//	private int maxGameNum;
//	private int numEval;
	
	public TDParams() {
		super("TD Parameter");
		
		// These are the initial defaults 
		// (Other game- and agent-specific defaults are in setParamDefaults, which is called
		// whenever one of the agent choice boxes changes to an agent requiring TDParams)
		//
		alphaT = new JTextField(ParTD.DEFAULT_ALPHA+"");//("0.1")	// 
		alfinT = new JTextField(ParTD.DEFAULT_ALFIN+"");			//
		epsilT = new JTextField(ParTD.DEFAULT_EPSIL+"");			// 
		epfinT = new JTextField(ParTD.DEFAULT_EPFIN+"");			//
		lambdaT = new JTextField(ParTD.DEFAULT_LAMBDA+"");//("0.9")	//  the defaults
		horcutT = new JTextField(ParTD.DEFAULT_HORIZONCUT+"");		//
		gammaT = new JTextField(ParTD.DEFAULT_GAMMA+"");			//
		epochT = new JTextField(ParTD.DEFAULT_EPOCHS+"");			//
		tNply_T = new JTextField(ParTD.DEFAULT_NPLY+"");			//
		mode3P_T = new JTextField(ParTD.DEFAULT_MODE_3P+"");		//
		alphaL = new JLabel("Alpha init");
		alfinL = new JLabel("Alpha final");
		epsilL = new JLabel("Epsilon init");
		epfinL = new JLabel("Epsilon final");
		lambdaL = new JLabel("Lambda");
		horcutL = new JLabel("Horizon cut");
		gammaL = new JLabel("Gamma");
		epochL = new JLabel("Epochs");
		tNply_L = new JLabel("Train nPly");
		mode3P_L = new JLabel("MODE_3P");
		epochL.setToolTipText(TIPEPOCHL);
		alphaL.setToolTipText(TIPALPHA1L);
		alfinL.setToolTipText(TIPALPHA2L);
		lambdaL.setToolTipText(TIPLAMBDAL);
		horcutL.setToolTipText(TIPHORCUTL);
		epsilL.setToolTipText(TIPEPSIL1L);
		epfinL.setToolTipText(TIPEPSIL2L);
		gammaL.setToolTipText(TIPGAMMAL);
		tNply_L.setToolTipText("Train n-ply look ahead (currently only TD-NTuple-2)");
		mode3P_L.setToolTipText("Mode for TD-NTuple-2: 0: multi, 1: single, 2: mixed");
		
		withSigType = new JCheckBox();
		normalize = new JCheckBox();
//		cbgSigType = new CheckboxGroup();
//		wo_SigType = new Checkbox("without",cbgSigType,true);
//		withSigType = new Checkbox("with",cbgSigType,false);

		NetTypeL = new JLabel("Network Type: ");
		SigTypeL = new JLabel("Output Sigmoid: ");
		NormalizeL = new JLabel("Normalize: ");
		LrnTypeL = new JLabel("Learning rule: ");
		eligTypeL = new JLabel("Eligibility: ");
		NormalizeL.setToolTipText(TIPNORMALIZEL);
		eligTypeL.setToolTipText(TIPELIGTYPE);

		eligModeType = new JComboBox(eligModeString);

		choiceNetType = new JComboBox(neuralNetString);

		choiceLrnType = new JComboBox(lrnTypeString);
		//for (String s : lrnTypeString) choiceLrnType.addItem(s);
		
		FeatTDS_L = new JLabel("Feature set");
		this.choiceFeatTDS = new JComboBox();

		tdPanel = new JPanel();		// put the inner buttons into panel oPanel. This panel
									// can be handed over to a tab of a JTabbedPane 
									// (see class XArenaTabs)
		
		setLayout(new BorderLayout(10,0));				// rows,columns,hgap,vgap
		tdPanel.setLayout(new GridLayout(0, 4, 10, 10)); // rows,columns,hgap,vgap
		
		tdPanel.add(alphaL);
		tdPanel.add(alphaT);
		tdPanel.add(epsilL);
		tdPanel.add(epsilT);
		
		tdPanel.add(alfinL);
		tdPanel.add(alfinT);
		tdPanel.add(epfinL);
		tdPanel.add(epfinT);
		
		tdPanel.add(lambdaL);
		tdPanel.add(lambdaT);
		tdPanel.add(gammaL);
		tdPanel.add(gammaT);
		
		tdPanel.add(horcutL);
		tdPanel.add(horcutT);
		tdPanel.add(NormalizeL);
		tdPanel.add(normalize);

		tdPanel.add(NetTypeL);
		tdPanel.add(choiceNetType);
		tdPanel.add(SigTypeL);
		tdPanel.add(withSigType);
//		tdPanel.add(new Canvas());					// fill one grid place with empty canvas
//		tdPanel.add(new Canvas());					// fill one grid place with empty canvas
		
//		tdPanel.add(LrnTypeL);
//		tdPanel.add(choiceLrnType);
		tdPanel.add(eligTypeL);
		tdPanel.add(eligModeType);		
		tdPanel.add(mode3P_L);
		tdPanel.add(mode3P_T);

		tdPanel.add(FeatTDS_L);
		tdPanel.add(choiceFeatTDS);
		tdPanel.add(epochL);
		tdPanel.add(epochT);
		
		add(tdPanel,BorderLayout.CENTER);
		
		pack();
		setVisible(false);
		
		boolean enabMode3P =  (TDNTuple2Agt.VER_3P); 
		this.enableNPly(enabMode3P);
		this.enableMode3P(enabMode3P);
		
	} // constructor TDParams()	
	
//	public TDParams(TDParams tdPar) {
//		this();
//		this.setFrom(tdPar);
//	}
	
	public JPanel getPanel() {
		return tdPanel;
	}
	public double getAlpha() {
		return Double.valueOf(alphaT.getText()).doubleValue();
	}
	public double getAlphaFinal() {
		return Double.valueOf(alfinT.getText()).doubleValue();
	}
	public double getEpsilon() {
		return Double.valueOf(epsilT.getText()).doubleValue();
	}
	public double getEpsilonFinal() {
		return Double.valueOf(epfinT.getText()).doubleValue();
	}
	public double getLambda() {
		return Double.valueOf(lambdaT.getText()).doubleValue();
	}
	public double getHorizonCut() {
		return Double.valueOf(horcutT.getText()).doubleValue();
	}
	public double getGamma() {
		return Double.valueOf(gammaT.getText()).doubleValue();
	}
	public int getEpochs() {
		return Integer.valueOf(epochT.getText()).intValue();
	}
	public int getFeatmode() {
		String s = (String) choiceFeatTDS.getSelectedItem();
		if (s==null) return 0;
		//int i = Integer.valueOf(s).intValue();
		return Integer.valueOf(s).intValue();
	}
	public boolean hasSigmoid() {
		return withSigType.isSelected();
	}
	public boolean getNormalize() {
		return normalize.isSelected();
	}
	public boolean hasLinearNet() {
		String Type = (String) choiceNetType.getSelectedItem();
		if (Type == "linear")
			return true;
		return false;		
	}
	public boolean hasRpropLrn() {
		String Type = (String) choiceLrnType.getSelectedItem();
		if (Type == "RPROP")
			return true;
		return false;
	}
	public int getNPly() {
		return Integer.valueOf(tNply_T.getText()).intValue();
	}
	public int getMode3P() {
		return Integer.valueOf(mode3P_T.getText()).intValue();
	}
	public int getEligMode() {
		return this.eligModeType.getSelectedIndex();
	}
	
	public void setAlpha(double value) {
		alphaT.setText(value+"");
	}
	public void setAlphaFinal(double value) {
		alfinT.setText(value+"");
	}
	public void setEpsilon(double value) {
		epsilT.setText(value+"");
	}
	public void setEpsilonFinal(double value) {
		epfinT.setText(value+"");
	}
	public void setLambda(double value) {
		lambdaT.setText(value+"");
	}
	public void setHorizonCut(double value) {
		horcutT.setText(value+"");
	}
	public void setGamma(double value) {
		gammaT.setText(value+"");
	}
	public void setEpochs(int value) {
		epochT.setText(value+"");
	}
	public void setFeatmode(int featmode) {
		//If the feature list has not been initialized, add the selected featmode to the list
		if (choiceFeatTDS.getItemCount() == 0){
			choiceFeatTDS.addItem(Integer.toString(featmode));
		}
		choiceFeatTDS.setSelectedItem(featmode+"");
	}
	public void setFeatList(int[] featList){
		for (int i : featList) choiceFeatTDS.addItem(Integer.toString(i));
	}
	public void setSigmoid(boolean state) {
		withSigType.setSelected(state);
	}
	public void setNormalize(boolean state) {
		normalize.setSelected(state);
	}
	public void setLinearNet(boolean state) {
		choiceNetType.setSelectedItem(state ? 0 : 1);
	}
	public void setRpropLrn(boolean state) {
		choiceLrnType.setSelectedItem(state ? 1 : 0);
	}
	public void setNPly(int value) {
		tNply_T.setText(value+"");
	}
	public void setMode3P(int value) {
		mode3P_T.setText(value+"");
	}
	public void setEligMode(int value) {
		this.eligModeType.setSelectedIndex(value);
	}
	
	public void enableNPly(boolean enable) {
		tNply_L.setEnabled(enable);
		tNply_T.setEnabled(enable);
	}
	public void enableMode3P(boolean enable) {
		mode3P_L.setEnabled(enable);
		mode3P_T.setEnabled(enable);
	}
//	public void setMaxGameNum(int maxGameNum) {
//		this.maxGameNum = maxGameNum;
//	}
//	public void setNumEval(int numEval) {
//		this.numEval = numEval;
//	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  TDParams of the re-loaded agent
	 */
	public void setFrom(TDParams tp) {
		setAlpha(tp.getAlpha());
		setAlphaFinal(tp.getAlphaFinal());
		setEpsilon(tp.getEpsilon());
		setEpsilonFinal(tp.getEpsilonFinal());
		setGamma(tp.getGamma());
		setLambda(tp.getLambda());
		setHorizonCut(tp.getHorizonCut());
		setLinearNet(tp.hasLinearNet());
		setRpropLrn(tp.hasRpropLrn());
		setSigmoid(tp.hasSigmoid());
		setNormalize(tp.getNormalize());
		setEligMode(tp.getEligMode());
		setEpochs(tp.getEpochs());
		setFeatmode(tp.getFeatmode());
		setNPly(tp.getNPly());
		setMode3P(tp.getMode3P());
//		setMaxGameNum(tp.getMaxGameNum());	// this is now in AgentBase
//		setNumEval(tp.getNumEval());		// this is obsolete now (we have ParOther)
	}
	
	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  TDParams of the re-loaded agent
	 */
	public void setFrom(ParTD tp) {
		setAlpha(tp.getAlpha());
		setAlphaFinal(tp.getAlphaFinal());
		setEpsilon(tp.getEpsilon());
		setEpsilonFinal(tp.getEpsilonFinal());
		setGamma(tp.getGamma());
		setLambda(tp.getLambda());
		setHorizonCut(tp.getHorizonCut());
		setLinearNet(tp.hasLinearNet());
		setRpropLrn(tp.hasRpropLrn());
		setSigmoid(tp.hasSigmoid());
		setNormalize(tp.getNormalize());
		setEligMode(tp.getEligMode());
		setEpochs(tp.getEpochs());
		setFeatmode(tp.getFeatmode());
		setNPly(tp.getNPly());
		setMode3P(tp.getMode3P());
	}
	
	/**
	 * Set sensible parameters for a specific agent and specific game. By "sensible
	 * parameters" we mean parameter producing good results. Likewise, some parameter
	 * choices may be enabled or disabled.
	 * 
	 * @param agentName either "TD-Ntuple-2" (for {@link TDNTuple2Agt}) or "TDS" (for {@link TDAgent})
	 * @param gameName the string from {@link games.StateObservation#getName()}
	 */
	public void setParamDefaults(String agentName, String gameName) {
		// Currently we have here only the sensible defaults for three games ("TicTacToe", "Hex", "2048")
		// and for three agents ("TD-Ntuple[-2]" = class TDNTuple[2]Agt and "TDS" = class TDAgent).
		//
		// If later good parameters for other games are found, they should be
		// added with suitable nested switch(gameName). 
		// Currently we have only one switch(gameName) on the initial featmode (=3 for 
		// TicTacToe, =2 for Hex, and =0 for all others)
		boolean mode3PEnable= (agentName=="TD-Ntuple-2" ? true : false);
		horcutT.setText("0.1"); 			//
		switch (agentName) {
		case "TD-Ntuple": 
		case "TD-Ntuple-2": 
		case "TD-Ntuple-3": 
		case "Sarsa":
			switch (agentName) {
			case "TD-Ntuple": 
				alphaT.setText("0.001");  		// the defaults
				alfinT.setText("0.001");		//
				break;
			case "TD-Ntuple-2": 
			case "TD-Ntuple-3": 
				switch (gameName) {
				case "ConnectFour":
					alphaT.setText("5.0");  		// the defaults
					alfinT.setText("5.0");			//
					horcutT.setText("0.001"); 			//
					break;
				default: 
					alphaT.setText("0.2");  		// the defaults
					alfinT.setText("0.2");			//
				}
				break;
			case "Sarsa": 
				alphaT.setText("1.0");  		// the defaults
				alfinT.setText("0.5");			//
				epsilT.setText("0.1");  		//
				mode3PEnable=false;				//
				break;
			default:	//  all other
				epsilT.setText("0.3");				
				break;
			}
			epfinT.setText("0.0");				//
			lambdaT.setText("0.0"); 			//
			gammaT.setText("1.0");				//
			epochT.setText("1");				//
			withSigType.setSelected(true);		// tanh
			normalize.setSelected(false);		// 
			withSigType.setEnabled(true); // NEW		
			SigTypeL.setEnabled(true);    // NEW
			eligTypeL.setEnabled(true);
			eligModeType.setEnabled(true);
			NetTypeL.setEnabled(false);
			choiceNetType.setEnabled(false);
			LrnTypeL.setEnabled(false);
			choiceLrnType.setEnabled(false);
			FeatTDS_L.setEnabled(false);
			choiceFeatTDS.setEnabled(false);
			epochL.setEnabled(false);
			epochT.setEnabled(false);
			mode3P_L.setEnabled(mode3PEnable);
			mode3P_T.setEnabled(mode3PEnable);
			switch (gameName) {
			case "2048": 
				epsilT.setText("0.0");				
				break;
			}
			break;
		case "TDS":
			alphaT.setText("0.1");				// the defaults
			alfinT.setText("0.001");			//
			epfinT.setText("0.0");				//
			lambdaT.setText("0.9");				//
			horcutT.setText("0.1"); 			//
			gammaT.setText("1.0");				//
			epochT.setText("1");				//
			withSigType.setSelected(false);		// Fermi fct
			normalize.setSelected(false);		// 
			eligTypeL.setEnabled(false);
			eligModeType.setEnabled(false);
			withSigType.setEnabled(true);		
			SigTypeL.setEnabled(true);
			NetTypeL.setEnabled(true);
			choiceNetType.setEnabled(true);
			LrnTypeL.setEnabled(true);
			choiceLrnType.setEnabled(true);
			FeatTDS_L.setEnabled(true);
			choiceFeatTDS.setEnabled(true);
			epochL.setEnabled(true);
			epochT.setEnabled(true);
			mode3P_L.setEnabled(false);
			mode3P_T.setEnabled(false);
			switch (gameName) {
			case "TicTacToe": 
				setFeatmode(3);
				break;
			case "Hex": 
				setFeatmode(2);
				lambdaT.setText("0.0");				
				break;
			default:	//  all other
				setFeatmode(0);
				lambdaT.setText("0.0");				
				break;
			}
			switch (gameName) {
			case "2048": 
				epsilT.setText("0.0");				
				break;
			default:	//  all other
				epsilT.setText("0.3");				
				break;
			}
			break;
		}
		
	}
	
} // class TDParams
