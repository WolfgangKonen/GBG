package games.CFour;

//import gui.C4Game.Action;
//import gui.C4Game.State;
//import guiOptions.OptionsTD;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * All N-Tuples can be listed and selected by the user.
 * @author Markus Thill
 *
 */
public class ShowTupleList extends Dialog {
	private static final long serialVersionUID = 1L;

	private C4GameGui m_game;
	private DefaultListModel<String> listModel;
	private JList<String> lTupleList;
	private JButton bSave;
	private JButton bDelete;
	private JButton bNewTuple;
	private JButton bDiscard;
	private ShowTupleList m_par;
	private ListOperation listOp;

	public ShowTupleList(C4GameGui game, Window parent, ListOperation lo,
			String title) {
		super(parent, title);

		m_game = game;
		listOp = lo;

		bDelete = new JButton("Delete");
		bNewTuple = new JButton("New");
		bDiscard = new JButton("Discard");

		// ==========================================================
		listModel = new DefaultListModel<String>();
		lTupleList = new JList<String>(listModel);
		lTupleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lTupleList.setLayoutOrientation(JList.VERTICAL);
		lTupleList.setVisibleRowCount(5);
		// =====================================
		JScrollPane listScrollPane = new JScrollPane(lTupleList);

		bSave = new JButton("Save");
		m_par = this;

		// ==========================================================================
		lTupleList.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				listOp.indexChanged(lTupleList.getSelectedIndex());
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}

		});
		
		lTupleList.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				listOp.indexChanged(lTupleList.getSelectedIndex());
			}

			@Override
			public void keyReleased(KeyEvent e) {	
			}

			@Override
			public void keyTyped(KeyEvent e) {	
			}
			
		});

		if (m_game != null) {
			bDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = lTupleList.getSelectedIndex();
					if (index >= 0) {
						listModel.remove(index);
						m_game.nTupleList.remove(index);
						m_game.action = Action.DELETE;
					}

					int size = listModel.getSize();

					if (size == 0) { // Nobody's left, disable delete.
						bDelete.setEnabled(false);

					} else if (index >= 0) { // Select an index.
						if (index == listModel.getSize()) {
							// removed item in last position
							index--;
						}

						lTupleList.setSelectedIndex(index);
						lTupleList.ensureIndexIsVisible(index);
					}
				}
			});

			bNewTuple.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String name = "[]";

					int index = lTupleList.getSelectedIndex(); // get selected
																// index
					if (index == -1) { // no selection, so insert at beginning
						index = 0;
					} else { // add after the selected item
						index++;
					}
					bDelete.setEnabled(true);

					m_game.nTupleList.add(index, new ArrayList<Integer>());

					listModel.insertElementAt(name, index);

					// Select the new item and make it visible.
					lTupleList.setSelectedIndex(index);
					lTupleList.ensureIndexIsVisible(index);
				}
			});

			bSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Integer[][] tp = m_game.getChangedNTuples();
					int index = m_game.c4Menu.getSelectedAgent();
					OptionsTD cf = (OptionsTD) m_game.params[index];
					cf.setNTuples(tp);
					m_par.setVisible(false);
					m_game.changeState(State.IDLE);
				}
			});

			bDiscard.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					m_par.setVisible(false);
					m_game.changeState(State.IDLE);
				}
			});
		} else {
			bDelete.setEnabled(false);
			bNewTuple.setEnabled(false);
			bSave.setEnabled(false);
			bDiscard.setEnabled(false);
		}

		// ==========================================================================

		setLayout(new BorderLayout());
		Panel p = new Panel();
		p.setLayout(new GridLayout2(2, 2, 5, 5));
		p.add(bDelete);
		p.add(bNewTuple);
		p.add(bDiscard);
		p.add(bSave);

		add(listScrollPane, BorderLayout.CENTER);
		add(p, BorderLayout.PAGE_END);

		setSize(300, 500);
		setVisible(false);
		addWindowListener(new WindowClosingAdapter());
	}

	public void setNTuples(String[] tuples) {
		listModel.removeAllElements();
		for (int i = 0; i < tuples.length; i++)
			listModel.addElement(tuples[i]);
		lTupleList.setSelectedIndex(-1);
		lTupleList.ensureIndexIsVisible(-1);
	}

	public void updateNTuple(String tuple, int index) {
		listModel.set(index, tuple);
	}

	public int getSelectedIndex() {
		return lTupleList.getSelectedIndex();
	}

	public void setSelectedIndex(int index) {
		lTupleList.setSelectedIndex(index);
	}

	private class WindowClosingAdapter extends WindowAdapter {
		public WindowClosingAdapter() {
		}

		public void windowClosing(WindowEvent event) {
			m_par.setVisible(false);
			if (m_game != null)
				m_game.changeState(State.IDLE);
		}
	}
}
