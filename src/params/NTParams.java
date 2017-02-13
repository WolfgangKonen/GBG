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

//samine//
public class NTParams extends Frame implements Serializable {
	private static final String TIPRANDL = "If checked, generate random n-tuples. If not, take a hard-coded n-tuple set (int[][] nTuple in TDSNPlayer)";
	private static final String TIPNTUPLETYPE = "n-tuple generation method: random walk or random point";
	private static final String TIPNTUPLENUML = "How many n-tuples to generate in case of 'randomness'";
	private static final String TIPNTUPLESIZEL = "maxTupleLen: Every generated n-tuple has a size 2,...,maxTupleLen";
	private static final String TIPUSESYMMETRY = "If checked, use symmetries when training nTuple agent in TDSNPlayer)";

	private static String[] tcFactorString = { "Immediate", "Accumulating" };
	private static String[] ntTupleTypeString={"RandomWalk","RandomPoint"};

	JLabel TempCoL;
	JLabel tcFactorL;
	JLabel NTupleL;
	JLabel NTupleNumL;
	JLabel NTupleSizeL;
	JLabel RandL;
	JLabel InitL;
	JLabel tcIntervalL;
	JLabel EvalL;
	JLabel NTupleType;
	JLabel UseSymL;
	JPanel tcPanel;
	// JLabel NTupleMaxL;

	public JTextField InitT;
	public JTextField tcIntervalT;
	public JTextField NTupleNumT;
	public JTextField nTupleMaxT;
	public JTextField EvalT;

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
		EvalL = new JLabel("Evaluting Interval");
		NTupleType=new JLabel("nTuple generation");
		NTupleType.setToolTipText(TIPNTUPLETYPE);

		UseSymL = new JLabel("USESYMMETRY");
		UseSymL.setToolTipText(TIPUSESYMMETRY);
		
		// default values
		InitT = new JTextField("0.0001");
		InitT.setEnabled(false);
		tcIntervalT = new JTextField("2");
		tcIntervalT.setEnabled(false);
		NTupleNumT = new JTextField("6");
		NTupleNumT.setEnabled(false);
		nTupleMaxT = new JTextField("8");
		nTupleMaxT.setEnabled(false);
		EvalT = new JTextField("100");
		EvalT.setEnabled(false);

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
				if(getRandomness()==true){
					NTupleNumT.setEnabled(true);
					nTupleMaxT.setEnabled(true);
					TupleType.setEnabled(true);
				} else {
					NTupleNumT.setEnabled(false);
					nTupleMaxT.setEnabled(false);
					TupleType.setEnabled(false);
				}
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
		tcPanel.add(NTupleNumT);
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
	}

	private void enableTcPart() {
		if (getTC()==false){
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
		if(getTCFType()==false){
			tcIntervalL.setEnabled(true);
			tcIntervalT.setEnabled(true);
		} else {
			tcIntervalL.setEnabled(false);
			tcIntervalT.setEnabled(false);
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
		return Integer.parseInt(NTupleNumT.getText());
	}

	public int getNtupleMax() {
		return Integer.parseInt(nTupleMaxT.getText());
	}

// /WK/02/2015: never used:
//	public double getTestInterval() {
//		return Double.valueOf(EvalT.getText()).doubleValue();
//	}

	public boolean getTC() {
		return TempCoC.isSelected();
	}

	public void setTC(boolean state) {
		TempCoC.setSelected(state);
	}

	public void setTcImm(String strg) {
		tcFactorType.setSelectedItem(strg);
	}

	public String getTcImm() {
		return (String) tcFactorType.getSelectedItem();
	}

	public void setTcInterval(String strg) {
		tcIntervalT.setText(strg);
	}
	
	public boolean getTCFType() {
		Object Type = tcFactorType.getSelectedItem();
		if (Type == "Immediate")
			return true;
		return false;
	}

	public boolean getRandomness() {
		return RandomnessC.isSelected();
	}
	public boolean getRandWalk() {
		Object Type=TupleType.getSelectedItem();
		if(Type==ntTupleTypeString[0]) // "RandomWalk"
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
		setTC(nt.getTC());
		setTcInterval(""+nt.getTcInterval());
		setTcImm(nt.getTcImm());
		InitT.setText(""+nt.getINIT());
		EvalT = nt.EvalT;
		RandomnessC.setSelected(nt.getRandomness());
		TupleType.setSelectedIndex(nt.getRandWalk()?0:1);
		NTupleNumT.setText(nt.getNtupleNumber()+"");
		nTupleMaxT.setText(nt.getNtupleMax()+"");
		UseSymmetryC.setSelected(nt.getUseSymmetry());
	}

}
