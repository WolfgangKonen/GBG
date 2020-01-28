package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.XArenaMenu;
import games.Arena;
import tools.StatusBar;
import tools.Types;

public class ArenaGui extends JFrame {

	protected JLabel m_title;
	protected StatusBar statusBar = new StatusBar();
	protected XArenaMenu m_menu = null;
	private Arena m_arena = null;
	
	public ArenaGui(Arena arena) throws HeadlessException {
		m_arena = arena;
		initGui("");
	}

	public ArenaGui(Arena arena, GraphicsConfiguration gc) {
		super(gc);
		m_arena = arena;
		initGui("");
	}

	public ArenaGui(Arena arena, String title) throws HeadlessException {
		super(title);
		m_arena = arena;
		initGui(title);
	}

	public ArenaGui(Arena arena, String title, GraphicsConfiguration gc) {
		super(title, gc);
		m_arena = arena;
		initGui(title);
	}

	void initGui(String title) {
        // scale the font of all status messages:
		Font lFont = new Font("Arial", Font.PLAIN, Types.GUI_DIALOGFONTSIZE);
		
		JPanel titlePanel = new JPanel();
		titlePanel.setBackground(Types.GUI_BGCOLOR);
		JLabel Blank = new JLabel(" "); // a little bit of space
		m_title = new JLabel("Arena  "+m_arena.getGameName(), SwingConstants.CENTER);
		m_title.setForeground(Color.black);
		Font tFont = new Font("Arial", 1, Types.GUI_TITLEFONTSIZE);
		m_title.setFont(tFont);
		titlePanel.add(Blank);
		titlePanel.add(m_title);

		JPanel infoPanel = new JPanel(new BorderLayout(0, 0));
		infoPanel.setBackground(Types.GUI_BGCOLOR);
		statusBar.setFont(lFont);
		statusBar.setBackground(Types.GUI_BGCOLOR);
		this.setLayout(new BorderLayout(10, 0));
		this.setBackground(Types.GUI_BGCOLOR); // Color.white
		JPanel jPanel = new JPanel();
		jPanel.setBackground(Types.GUI_BGCOLOR);
		infoPanel.add(jPanel, BorderLayout.NORTH); // a little gap
		infoPanel.add(statusBar, BorderLayout.CENTER);
		infoPanel.add(jPanel, BorderLayout.SOUTH); // just a little space at the bottom

		m_menu = new XArenaMenu(m_arena);
		this.add(titlePanel, BorderLayout.NORTH);
		this.add(m_arena.m_xab.m_XAB_gui, BorderLayout.CENTER);
		this.add(infoPanel, BorderLayout.SOUTH);

		// initialize GUI elements 
		this.addWindowListener(new WindowClosingAdapter(m_arena));
		this.setJMenuBar(m_menu);
		this.setSize(Types.GUI_ARENATRAIN_WIDTH, m_arena.getGuiArenaHeight());
		this.setBounds(0,0,Types.GUI_ARENATRAIN_WIDTH, m_arena.getGuiArenaHeight());
//		this.pack();
		this.setVisible(true);
		
	}
	
	public void setStatusMessage(String msg) {
		statusBar.setMessage(msg);
	}

	public void setTitle(String title) {
		m_title.setText(title);
	}
	
	/**
	 * helper class 
	 *
	 * @see Arena#initGame()
	 */
	protected static class WindowClosingAdapter
	extends WindowAdapter
	{
		Arena m_ar;
		public WindowClosingAdapter(Arena ar)  {  
			m_ar = ar;
		}

		public void windowClosing(WindowEvent event)
		{
			m_ar.destroy();
		}
	}
}
