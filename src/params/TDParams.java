package params;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;

/**
 * This class realizes the parameter settings (GUI tab) for TD players 
 * (agents {@link TDAgent},  {@link TDNTuple3Agt} and {@link SarsaAgt}).
 * These parameters and their [defaults] are: <ul>
 * <li> <b>alpha</b>: 		[0.001] initial strength of learning parameter 
 * <li> <b>alphaFinal</b>: 	[0.001] final strength of learning parameter  
 * <li> <b>epsilon</b>: 	[0.1] initial probability of random move 
 * <li> <b>epsilonFinal</b>:[0.0] final probability of random move  
 * <li> <b>lambda</b>: 		[0.0] eligibility trace parameter 
 * <li> <b>gamma</b>: 		[1.0] discount parameter 
 * </ul> 
 * The defaults are defined in class {@link ParTD}. <br>
 * 
 * @see ParTD
 * @see TDAgent
 * @see TDNTuple3Agt
 */
public class TDParams extends Frame implements Serializable
{
	private static final String TIPGAMMAL = "Discount factor in range [0,1] ";
	private static final String TIPEPOCHL = "Only TDS: Accumulate gradient for Epochs iterations, then update weights";
	private static final String TIPNORMALIZEL = "Normalize StateObservation's game score to the range of the agent's sigmoid function";
	private static final String TIPALPHA1L = "Initial learn step size";
	private static final String TIPALPHA2L = "Final learn step size";
	private static final String TIPEPSIL1L = "Initial random move rate in [0,1]";
	private static final String TIPEPSIL2L = "Final random move rate in [0,1]";
	private static final String TIPLAMBDAL = "Eligibility trace parameter in [0,1]";
	private static final String TIPHORCUTL = "Horizon cut: neglect eligibility terms with lambda^k < cut";
	private static final String TIPELIGTYPEL = "Eligibility trace type: normal or reset on random move";
	private static final String TIPELIGMODE = "ET: normal, RESET: reset on random move";
	private static final String TIPSTOPONROUNDL = "stop training episode on round over";
	private static final String TIPNETTYPEL = "(only TDS)";
	private static final String TIPFEATTDSL = "(only TDS) select feature mode";

	
	private final static String[] lrnTypeString = { "backprop","RPROP" };
	private final static String[] neuralNetString = { "linear","neural net" };
	private final static String[] eligModeString = { "ET","RESET"}; // ,"REPLACE","RES+REP" };
	
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip containing this object will become 
	 * unreadable, or you have to provide a special version transformation)
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	JLabel alphaL;
	JLabel alfinL;
	JLabel epsilL;
	JLabel epfinL;
	JLabel lambdaL;
	JLabel horcutL;
	JLabel gammaL;
	JLabel epochL;
	JPanel tdPanel;
	private final JTextField alphaT;
	private final JTextField alfinT;
	private final JTextField epsilT;
	private final JTextField epfinT;
	private final JTextField lambdaT;
	private final JTextField horcutT;
	private final JTextField gammaT;
	private final JTextField epochT;
//	private final JTextField tNply_T;

	JLabel SigTypeL;
	JLabel NormalizeL;
	JLabel StopOnRoundL;
	JLabel NetTypeL;
	JLabel LrnTypeL;
	JLabel eligTypeL;
	public JCheckBox withSigType;
	public JCheckBox normalize;
	public JCheckBox cbStopOnRoundOver;
	public JComboBox<String> choiceLrnType;
	public JComboBox<String> choiceNetType;
	public JComboBox<String> eligModeType;
	JLabel FeatTDS_L;
	public JComboBox<String> choiceFeatTDS;

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
//		tNply_T = new JTextField(ParTD.DEFAULT_NPLY+"");			//
		alphaL = new JLabel("Alpha init");
		alfinL = new JLabel("Alpha final");
		epsilL = new JLabel("Epsilon init");
		epfinL = new JLabel("Epsilon final");
		lambdaL = new JLabel("Lambda");
		horcutL = new JLabel("Horizon cut");
		gammaL = new JLabel("Gamma");
		epochL = new JLabel("Epochs");
		FeatTDS_L = new JLabel("Feature set");
		alphaL.setToolTipText(TIPALPHA1L);
		alfinL.setToolTipText(TIPALPHA2L);
		lambdaL.setToolTipText(TIPLAMBDAL);
		horcutL.setToolTipText(TIPHORCUTL);
		epsilL.setToolTipText(TIPEPSIL1L);
		epfinL.setToolTipText(TIPEPSIL2L);
		gammaL.setToolTipText(TIPGAMMAL);
		epochL.setToolTipText(TIPEPOCHL);

		withSigType = new JCheckBox();
		normalize = new JCheckBox();
		cbStopOnRoundOver = new JCheckBox();

		NetTypeL = new JLabel("Network Type: ");
		SigTypeL = new JLabel("Output Sigmoid: ");
		NormalizeL = new JLabel("Normalize: ");
		StopOnRoundL = new JLabel("Stop round over: ");
		LrnTypeL = new JLabel("Learning rule: ");
		eligTypeL = new JLabel("Eligibility: ");
		eligModeType = new JComboBox<>(eligModeString);
		NetTypeL.setToolTipText(TIPNETTYPEL);
		FeatTDS_L.setToolTipText(TIPFEATTDSL);
		NormalizeL.setToolTipText(TIPNORMALIZEL);
		StopOnRoundL.setToolTipText(TIPSTOPONROUNDL);
		eligTypeL.setToolTipText(TIPELIGTYPEL);
		eligModeType.setToolTipText(TIPELIGMODE);

		choiceNetType = new JComboBox<>(neuralNetString);

		choiceLrnType = new JComboBox<>(lrnTypeString);
		//for (String s : lrnTypeString) choiceLrnType.addItem(s);
		
		this.choiceFeatTDS = new JComboBox<>();

		// the following lambda, where e is an ActionEvent, is a simpler replacement for anonymous action listeners:
		lambdaT.addActionListener( e -> enableLambdaPart() );

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
		tdPanel.add(epochL);
		tdPanel.add(epochT);

//		tdPanel.add(LrnTypeL);
//		tdPanel.add(choiceLrnType);
		tdPanel.add(eligTypeL);
		tdPanel.add(eligModeType);		
		tdPanel.add(SigTypeL);
		tdPanel.add(withSigType);

		tdPanel.add(NetTypeL);
		tdPanel.add(choiceNetType);
		tdPanel.add(NormalizeL);
		tdPanel.add(normalize);

		tdPanel.add(FeatTDS_L);
		tdPanel.add(choiceFeatTDS);
		tdPanel.add(StopOnRoundL);
		tdPanel.add(cbStopOnRoundOver);

//		tdPanel.add(new Canvas());					// fill one grid place with empty canvas
//		tdPanel.add(new Canvas());					// fill one grid place with empty canvas

		add(tdPanel,BorderLayout.CENTER);
		
		pack();
		setVisible(false);
		
//		boolean enabMode3P =  (TDNTuple2Agt.VER_3P);
//		this.enableNPly(enabMode3P);
//		this.enableMode3P(enabMode3P);
		
	} // constructor TDParams()	
	
//	public TDParams(TDParams tdPar) {
//		this();
//		this.setFrom(tdPar);
//	}
	
	/**
	 * Needed for {@link Arena} (which has no train rights) to disable this param tab 
	 * @param enable whether to enable or not
	 */
	public void enableAll(boolean enable) {
		this.alphaT.setEnabled(enable);
		this.alfinT.setEnabled(enable);
		this.epsilT.setEnabled(enable);
		this.epfinT.setEnabled(enable);
		this.lambdaT.setEnabled(enable);
		this.eligModeType.setEnabled(enable);
		this.horcutL.setEnabled(enable);
		this.horcutT.setEnabled(enable);
		this.gammaT.setEnabled(enable);
		this.epochT.setEnabled(enable);
		this.choiceNetType.setEnabled(enable);
		this.normalize.setEnabled(enable);
		this.cbStopOnRoundOver.setEnabled(enable);
		this.withSigType.setEnabled(enable);
		this.choiceFeatTDS.setEnabled(enable);
	}
	
	private void enableAgentPart(String agentName) {
		boolean featureEnable= (agentName.equals("TDS"));
		boolean netTypeEnable= (agentName.equals("TDS"));
//		boolean mode3PEnable= ((agentName=="TD-Ntuple-2" && TDNTuple2Agt.VER_3P )? true : false);
		FeatTDS_L.setEnabled(featureEnable);
		choiceFeatTDS.setEnabled(featureEnable);		
		epochL.setEnabled(netTypeEnable);			// epochs currently only for TDS
		epochT.setEnabled(netTypeEnable);
		NetTypeL.setEnabled(netTypeEnable);
		SigTypeL.setEnabled(true);    // NEW
		choiceNetType.setEnabled(netTypeEnable);
		LrnTypeL.setEnabled(netTypeEnable);			// LrnType currently not shown
		choiceLrnType.setEnabled(netTypeEnable);
		if (!netTypeEnable) 
			choiceNetType.setSelectedIndex(0);		// n-tuple agents: always linear net
	}
	
	private void enableLambdaPart() {
		if (getLambda()==0.0){
			this.eligTypeL.setEnabled(false);
			this.eligModeType.setEnabled(false);
			this.horcutL.setEnabled(false);
			this.horcutT.setEnabled(false);
		}else{
			this.eligTypeL.setEnabled(true);
			this.eligModeType.setEnabled(true);
			this.horcutL.setEnabled(true);
			this.horcutT.setEnabled(true);
		}
	}

	public JPanel getPanel() {
		return tdPanel;
	}
	public double getAlpha() {
		return Double.parseDouble(alphaT.getText());
	}
	public double getAlphaFinal() {
		return Double.parseDouble(alfinT.getText());
	}
	public double getEpsilon() {
		return Double.parseDouble(epsilT.getText());
	}
	public double getEpsilonFinal() {
		return Double.parseDouble(epfinT.getText());
	}
	public double getLambda() {
		return Double.parseDouble(lambdaT.getText());
	}
	public double getHorizonCut() {
		return Double.parseDouble(horcutT.getText());
	}
	public double getGamma() {
		return Double.parseDouble(gammaT.getText());
	}
	public int getEpochs() {
		return Integer.parseInt(epochT.getText());
	}
	public int getFeatmode() {
		String s = (String) choiceFeatTDS.getSelectedItem();
		if (s==null) return 0;
		//int i = Integer.valueOf(s).intValue();
		return Integer.parseInt(s);
	}
	public boolean hasSigmoid() {
		return withSigType.isSelected();
	}
	public boolean getNormalize() {
		return normalize.isSelected();
	}
	public boolean hasStopOnRoundOver() {
		return cbStopOnRoundOver.isSelected();
	}
	public boolean hasLinearNet() {
		String Type = (String) choiceNetType.getSelectedItem();
		return Objects.equals(Type, "linear");
	}
	public boolean hasRpropLrn() {
		String Type = (String) choiceLrnType.getSelectedItem();
		return Objects.equals(Type, "RPROP");
	}
	public int getNPly() {
		return 0; //Integer.valueOf(tNply_T.getText()).intValue();
	}
//	public int getMode3P() {
//		return 0; //Integer.valueOf(mode3P_T.getText()).intValue();
//	}
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
	public void setStopOnRoundOver(boolean state) {
		cbStopOnRoundOver.setSelected(state);
	}
	public void setLinearNet(boolean state) {
		choiceNetType.setSelectedItem(state ? 0 : 1);
	}
	public void setRprop(boolean state) {
		choiceLrnType.setSelectedItem(state ? 1 : 0);
	}
	public void setEligMode(int value) {
		this.eligModeType.setSelectedIndex(value);
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded agent
	 * @param tp  ParTD of the re-loaded agent
	 */
	public void setFrom(ParTD tp) {
		setFrom(tp,null);
	}
	/**
	 * Needed to restore the param tab after {@link ParTD#setParamDefaults(String, String)} has been
	 * called with a certain agent name
	 * @param tp		the param object
	 * @param agentName the agent string
	 */
	public void setFrom(ParTD tp, String agentName) {
		setAlpha(tp.getAlpha());
		setAlphaFinal(tp.getAlphaFinal());
		setEpsilon(tp.getEpsilon());
		setEpsilonFinal(tp.getEpsilonFinal());
		setGamma(tp.getGamma());
		setLambda(tp.getLambda());
		setHorizonCut(tp.getHorizonCut());
		setEpochs(tp.getEpochs());
		setSigmoid(tp.hasSigmoid());
		setNormalize(tp.getNormalize());
		setStopOnRoundOver(tp.hasStopOnRoundOver());
		setEligMode(tp.getEligMode());
		setFeatmode(tp.getFeatmode());
		setLinearNet(tp.hasLinearNet());
		setRprop(tp.hasRpropLrn());
//		setNPly(tp.getNPly());
//		setMode3P(tp.getMode3P());
		
		enableLambdaPart();
		if (agentName!=null) 
			enableAgentPart(agentName);
	}


} // class TDParams
