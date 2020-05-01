package params;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controllers.TD.TDAgent;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Feature;
import games.Othello.Edax.Edax2;

/**
 * This class realizes Edax parameter settings (specific to Othello). 
 * <p>
 * These parameters and their [defaults] are:
 * <ul>
 * <li><b>Depth</b>: Edax search depth (command 'level', range 0,1,..., default 21)
 * <li><b>Time</b>: Time per move in seconds (command 'move-time', default 10.0)
 * </ul>
 * 
 * @see Edax2
 * @see games.XArenaButtons
 */
public class EdaxParams extends Frame {
	private static final long serialVersionUID = 1L;

	JLabel depth_L;
	JLabel mtime_L;
	public JTextField depth_T;
	public JTextField mtime_T;
//	public Checkbox chooseS01;
//	public Checkbox learnRM;
//	public Checkbox rewardIsGameScore;

	Button ok;
	JPanel ePanel;
	EdaxParams e_par;

	public EdaxParams() {
		super("Other Parameter");

		depth_T = new JTextField("21"); //
		mtime_T = new JTextField("10.0"); //
		depth_L = new JLabel("depth");
		mtime_L = new JLabel("move time");
		ok = new Button("OK");
		e_par = this;
		ePanel = new JPanel(); 	// put the inner buttons into panel oPanel. This
								// panel
								// can be handed over to a tab of a JTabbedPane
								// (see class TicTacToeTabs)

		depth_L.setToolTipText("Search depth (range 0,1,.... Default=21");
		mtime_L.setToolTipText("Time per move in seconds.");

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				e_par.setVisible(false);
			}
		});

		setLayout(new BorderLayout(10, 0)); // rows,columns,hgap,vgap
		ePanel.setLayout(new GridLayout(0, 4, 10, 10));

		ePanel.add(depth_L);
		ePanel.add(depth_T);
		ePanel.add(new Canvas());
		ePanel.add(new Canvas());

		ePanel.add(mtime_L);
		ePanel.add(mtime_T);
		ePanel.add(new Canvas());
		ePanel.add(new Canvas());

		ePanel.add(new Canvas());
		ePanel.add(new Canvas());
		ePanel.add(new Canvas());
		ePanel.add(new Canvas());


		add(ePanel, BorderLayout.CENTER);
		add(ok, BorderLayout.SOUTH);

		pack();
		setVisible(false);

	} // constructor OtherParams()

	public JPanel getPanel() {
		return ePanel;
	}

	public int getDepth() {
		return Integer.valueOf(depth_T.getText()).intValue();
	}

	public double getMoveTime() {
		double mtime = Double.valueOf(mtime_T.getText()).doubleValue();
		return mtime;
	}

	public void setDepth(int value) {
		depth_T.setText(value + "");
	}

	public void setMoveTime(double value) {
		mtime_T.setText(value + "");
	}

	/**
	 * Needed to restore the param tab with the parameters from a re-loaded
	 * agent
	 * 
	 * @param ep
	 *            ParOther of the re-loaded agent
	 */
	public void setFrom(ParEdax ep) {
		this.setDepth(ep.getDepth());
		this.setMoveTime(ep.getMoveTime());
	}

//	/**
//	 * Set sensible parameters for a specific agent and specific game. By "sensible
//	 * parameters" we mean parameter producing good results. Likewise, some parameter
//	 * choices may be enabled or disabled.
//	 * 
//	 * @param agentName either "TD-Ntuple-3" (for {@link TDNTuple3Agt}) or "TDS" (for {@link TDAgent})
//	 * @param gameName the string from {@link games.StateObservation#getName()}
//	 */
//	@Deprecated
//	public void setParamDefaults(String agentName, String gameName) {
//		switch (gameName) {
//		default:	//  all other
//			break;
//		}
//		switch (agentName) {
//		default: 
//			this.setDepth(21);
//			this.setMoveTime(10.0);
//			break;
//		}
//	}
	
} // class EdaxParams
