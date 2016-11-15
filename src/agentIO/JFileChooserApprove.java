package agentIO;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class JFileChooserApprove extends JFileChooser {
	private static final long serialVersionUID = -4357018954363879945L;

	public JFileChooserApprove() {
		Action refreshAction = new AbstractAction("Refresh") {
			private static final long serialVersionUID = 7149056014455068572L;
			public void actionPerformed(ActionEvent e) {
				rescanCurrentDirectory();
			}
		};
		
		getActionMap().put("Refresh", refreshAction);
		InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "Refresh");
	}
	

	@Override
	public void approveSelection() {
		File f = getSelectedFile();
		if (f.exists() && getDialogType() == SAVE_DIALOG) {
			int result = JOptionPane.showConfirmDialog(this,
					"The file exists, overwrite?", "Existing file",
					JOptionPane.YES_NO_CANCEL_OPTION);
			switch (result) {
			case JOptionPane.YES_OPTION:
				super.approveSelection();
				return;
			case JOptionPane.NO_OPTION:
				return;
			case JOptionPane.CLOSED_OPTION:
				return;
			case JOptionPane.CANCEL_OPTION:
				cancelSelection();
				return;
			}
		}
		super.approveSelection();
	}
}
