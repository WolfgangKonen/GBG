package games.RubiksCube;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import games.Arena;
import tools.Types;

/**
 * Class GameBoardCube implements interface GameBoard for RubiksCube.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * <p>
 * The size of the GUI window is set in {@link #showGameBoard(Arena, boolean)}.
 *
 * @author Wolfgang Konen, TH Koeln, 2018-2020
 *
 * @see GameBoardCubeGui2x2
 * @see GameBoardCubeGui3x3
 */
abstract public class GameBoardCubeGui extends JFrame {

	protected final int labSize = (int)(20*Types.GUI_SCALING_FACTOR_X);
	protected JPanel BoardPanel;
	protected JPanel ButtonPanel;
	protected final JLabel leftInfo=new JLabel("");
	protected final JLabel rightInfo=new JLabel("");
	protected JLabel pMinLabel;
	protected JLabel pMinValue;
	protected JLabel pMaxLabel;
	protected JLabel pMaxValue;
	protected JLabel scrTwists_L;
	protected JComboBox<String> scrTwists_T;
	static String[] scrTwistsList = {"1","2","3","4","5","6","7","8","9","10","11","12","13","RANDOM"};
	/**
	 * The representation of the cube in the GUI. The [24|48] active panels in the [6*8 | 9*12] field
	 * represent the cubie faces of the flattened [Pocket | Rubik's] cube.
	 */
	protected JPanel[][] Board;
	protected JButton[][] Button;

	/**
	 * a reference to the 'parent' {@link GameBoardCube} object
	 */
	protected final GameBoardCube m_gb;
	
	/**
	 * A table for the stored values of each action
	 */
	protected double[][] VTable;
	
	// the colors of the TH Koeln logo and other colors used for cube coloring:
	protected final Color colTHK1 = new Color(183,29,13);	// the red cube color
	protected final Color colTHK2 = new Color(255,137,0);	// the orange cube color
	//protected final Color colTHK3 = new Color(162,0,162);
	protected final Color colOrang = colTHK2;
	protected final Color colGreen = new Color(0,184,0);
	protected final Color colYellow = new Color(255,250,40);
	protected final Color colBlue = new Color(0,0,184);
	protected final Color colRed = colTHK1;

	protected int boardX,boardY;
	protected int buttonX, buttonY;

	GameBoardCubeGui(GameBoardCube gb) {
		super("Rubiks Cube");
		m_gb = gb;
	}
	
	protected void initGameBoard()
	{
		Board       = new JPanel[boardY][boardX];
		BoardPanel	= InitBoard();
		Button		= new JButton[buttonY][buttonX];
		ButtonPanel = InitButton();
		VTable		= new double[buttonY][buttonX];
		pMinLabel 	= new JLabel("[Other pars] pMin:");
		pMinValue 	= new JLabel("");
		pMaxLabel 	= new JLabel("... pMax:");
		pMaxValue 	= new JLabel("");
		scrTwists_L = new JLabel("    Scrambling Twists: ");
		scrTwists_T	= new JComboBox<>(scrTwistsList);
		scrTwists_T.setSelectedItem("4");
		scrTwists_L.setToolTipText("During play: How many initial twists before start. If RANDOM: pMin,...,pMax.");

		Font font=new Font("Arial", Font.BOLD,Types.GUI_TITLEFONTSIZE);
		JLabel Blank=new JLabel("   ");		// a little bit of space
		Blank.setPreferredSize(new Dimension(2*labSize,labSize)); // controls the space between panels
		
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		northPanel.add(pMinLabel);
		northPanel.add(pMinValue);
		northPanel.add(pMaxLabel);
		northPanel.add(pMaxValue);
		northPanel.add(new JLabel("    "));
		northPanel.add(scrTwists_L);
		northPanel.add(scrTwists_T);

		JPanel boardPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		boardPanel.add(BoardPanel);
		boardPanel.add(Blank); 		// a little bit of space
		boardPanel.add(ButtonPanel);
		boardPanel.setBackground(Types.GUI_BGCOLOR);
		ButtonPanel.setVisible(false);
		
		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.setBackground(Types.GUI_BGCOLOR);
		leftInfo.setFont(font);	
		rightInfo.setFont(font);	
		infoPanel.add(leftInfo);
		infoPanel.add(rightInfo);
		infoPanel.setSize(100,10);
		
		setLayout(new BorderLayout(10,0));
		//add(titlePanel,BorderLayout.NORTH);
		add(northPanel,BorderLayout.NORTH);
		add(boardPanel,BorderLayout.CENTER);
		add(infoPanel,BorderLayout.SOUTH);
		pack();
		setVisible(false);
	}

	abstract protected JPanel InitBoard();

	protected JPanel InitButton()
	{
		Font bfont=new Font("Arial",Font.BOLD,Types.GUI_DIALOGFONTSIZE);
		Font lfont=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
		String[] twiStr = {"U","L","F","D","R","B"};	// Up, Left, Front, Down, Right, Back
		JPanel panel=new JPanel();
		panel.setLayout(new GridLayout(buttonY+2,buttonX+1,4,4));
		panel.setBackground(Types.GUI_BGCOLOR);
		int buSize = (int)(35*Types.GUI_SCALING_FACTOR_X);
		Dimension minimumSize = new Dimension(buSize,buSize); //controls the button sizes
		// set the column heads (twists "1","2","3"):
		for (int j=0; j<3; j++) {
			JLabel jlab = new JLabel();
			jlab.setFont(lfont);
			jlab.setText(""+(j+1));
			jlab.setPreferredSize(new Dimension(labSize,labSize)); // controls the label size
			jlab.setHorizontalAlignment(JLabel.CENTER);
			jlab.setVerticalAlignment(JLabel.BOTTOM);
			panel.add(jlab);
		}
		JLabel elab = new JLabel();		// an empty label
		elab.setFont(lfont);
		elab.setText("");
		elab.setPreferredSize(new Dimension(labSize,labSize)); // controls the label size
		panel.add(elab);
		for(int i=0;i<buttonY;i++){
			for(int j=0;j<buttonX;j++){
				Button[i][j] = new JButton();
				Button[i][j].setBackground(Color.GRAY);  //(colTHK3);
				Button[i][j].setForeground(Color.white);
				Button[i][j].setFont(bfont);
				Button[i][j].setPreferredSize(minimumSize);
				Button[i][j].setVisible(true);
				Button[i][j].setEnabled(true);
				Button[i][j].setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				//.createEtchedBorder(), .createLoweredSoftBevelBorder()
				Button[i][j].addActionListener(
						new ActionHandler(i,j)  // constructor copies (i,j) to members (x,y)
						{
							public void actionPerformed(ActionEvent e)
							{
								Arena.Task aTaskState = m_gb.m_Arena.taskState;
								if (aTaskState == Arena.Task.PLAY)
								{
									m_gb.HGameMove(x,y);		// i.e. make human move (i,j), if Button[i][j] is clicked
								}
								if (aTaskState == Arena.Task.INSPECTV)
								{
									m_gb.InspectMove(x,y);	// i.e. update inspection, if Button[i][j] is clicked
								}
							}
						}
				);
				panel.add(Button[i][j]);
			} // for (j)

			// set the row labels (faces "U","L","F",...)
			JLabel jlab = new JLabel();
			jlab.setFont(lfont);
			jlab.setText(twiStr[i]);
			jlab.setPreferredSize(new Dimension(labSize,labSize)); // controls the label size
			panel.add(jlab);
		} // for (i)

		// optional: set the column heads again at bottom (twists "1","2","3"):
		for (int j=0; j<buttonX; j++) {
			JLabel jlab = new JLabel();
			jlab.setFont(lfont);
			jlab.setText(""+(j+1));
			jlab.setPreferredSize(new Dimension(labSize,labSize)); // controls the label size
			jlab.setHorizontalAlignment(JLabel.CENTER);
			jlab.setVerticalAlignment(JLabel.TOP);
			panel.add(jlab);
		}
		return panel;
	}

	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			leftInfo.setText("  ");
		}
		if (vClear) {
			if (VTable==null) VTable		= new double[buttonY][buttonX];
			for(int i=0;i<buttonY;i++){
				for(int j=0;j<buttonX;j++){
					VTable[i][j] = Double.NaN;
				}
			}
		}
	}

	/**
	 * Update the play board and the associated VBoard.
	 * 
	 * @param soN	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverCube soN, 
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;
		
		// show ButtonPanel only if it is needed (for showing action values or for entering actions) 
		switch (m_gb.m_Arena.taskState) {
			case INSPECTV -> ButtonPanel.setVisible(true);
			case PLAY -> ButtonPanel.setVisible(m_gb.m_Arena.hasHumanAgent() || showValueOnGameboard);
			default -> ButtonPanel.setVisible(false);
		}
		
		// the other choice: have the button panel always visible
		ButtonPanel.setVisible(true); 
		
		if (soN!=null) {
			if (soN.isGameOver()) {
				leftInfo.setText("Solved in "+soN.getMoveCounter() +" twists! (p=" 
						+soN.getCubeState().minTwists+")"); 				
			} else {
				if (m_gb.m_Arena.m_xab!=null) {
					if (soN.getMoveCounter() > m_gb.m_Arena.m_xab.getEpisodeLength(0)) {
						leftInfo.setText("NOT solved in "+soN.getMoveCounter() +" twists! (p="
								+soN.getCubeState().minTwists+")");

					}
				}
			}
			
			if (showValueOnGameboard && soN.getStoredValues()!=null) {
				for(i=0;i<buttonY;i++)
					for(j=0;j<buttonX;j++)
						VTable[i][j]=Double.NaN;	
				
				for (int k=0; k<soN.getStoredValues().length; k++) {
					Types.ACTIONS action = soN.getStoredAction(k);
					int iAction = action.toInt();
					j=iAction%buttonX;
					i=(iAction-j)/buttonX;
					VTable[i][j] = soN.getStoredValues()[k];					
				}	
				rightInfo.setText("");					
			} 
		} // if(so!=null)
		
		guiUpdateBoard(true,showValueOnGameboard);
	}

	/**
	 * Update the play board and the associated action values (raw score*100) for the state 
	 * {@code m_gb.m_so}.
	 * <p>
	 * Color coding for the action buttons, if {@code showValueOnGameBoard==true}:<br>
	 * color green = good move, high value, color red = bad move, low value 
	 * 
	 * @param enable as a side effect, all buttons Button[i][j] 
	 * 				 will be set into enabled state <code>enable</code>. 
	 * @param showValueOnGameboard if true, show the values on the action buttons. If false, 
	 * 				 clear any previous values.
	 */
	abstract protected void guiUpdateBoard(boolean enable, boolean showValueOnGameboard);

	// called from guiUpdateBoard
	protected void guiUpdateButton(boolean enable, boolean showValueOnGameboard) {
		int i, j;
		double value;
		String valueTxt;
		for(i=0;i<buttonY;i++)
			for(j=0;j<buttonX;j++) {
				value = VTable[i][j]; 				
				if (Double.isNaN(value)) {
					valueTxt = "   ";
				} else {
					valueTxt = " "+(int)(value*100);
					if (value<0) valueTxt = ""+(int)(value*100);
				}
				if (showValueOnGameboard) {
					Button[i][j].setText(valueTxt);
					Color buttonCol = (Double.isNaN(value)) ? Color.GRAY : calculateTileColor(value);
					Button[i][j].setBackground(buttonCol);
				} else {
					Button[i][j].setText("   ");					
					Button[i][j].setBackground(Color.GRAY);
				}
				Button[i][j].setEnabled(enable);
			}

		//paint(this.getGraphics());
	}		

    /**
     * Calculate the color for a specific tile value.
     * Uses three color stops: Red for value 0, Yellow for value 0.5, Green for value +1.
     * Colors for values between 0 and 0.5 are interpolated between red and yellow.
     * Colors for values between 0.5 and 1 are interpolated between yellow and green.
     *
     * @param tileValue Value of the tile
     * @return Color the tile is supposed to be drawn in
     */
    protected Color calculateTileColor(double tileValue) {
        float percentage = (float) Math.abs(tileValue);
        float inverse_percentage = 1 - percentage;

        Color colorLow;
        Color colorHigh;
        Color colorNeutral = Color.YELLOW;
        int red, blue, green;

        if (tileValue < 0) {
            colorLow = Color.RED;
            colorHigh = colorNeutral;
        } else {
            colorLow = Color.RED; // colorNeutral;
            colorHigh = Color.GREEN;
        }

        red = Math.min(Math.max(Math.round(colorLow.getRed() * inverse_percentage
                + colorHigh.getRed() * percentage), 0), 255);
        blue = Math.min(Math.max(Math.round(colorLow.getBlue() * inverse_percentage
                + colorHigh.getBlue() * percentage), 0), 255);
        green = Math.min(Math.max(Math.round(colorLow.getGreen() * inverse_percentage
                + colorHigh.getGreen() * percentage), 0), 255);

        return new Color(red, green, blue, 255);
    }

	// --- currently no action required ---
	public void enableInteraction(boolean enable) {
//		for(int i=0;i<3;i++){
//			for(int j=0;j<3;j++){
//				Button[i][j].setEnabled(enable);
//			}
//		}
	}

	public String getScramblingTwists() {
		return (String)scrTwists_T.getSelectedItem();
	}

	public void setPMin(int pMin) {
		pMinValue.setText(pMin+"");
	}

	public void setPMax(int pMax) {
		pMaxValue.setText(pMax+"");
	}
	
	/**
	 * This class is needed for each ActionListener of {@code Board[i][j]} in 
	 * {@link #InitBoard()}
	 *
	 */
	class ActionHandler implements ActionListener
	{
		int x,y;
		
		ActionHandler(int num1,int num2)			
		{		
			x=num1;
			y=num2;
		}
		public void actionPerformed(ActionEvent e){}			
	}

	abstract public void showGameBoard(Arena ticGame, boolean alignToMain);

    @Override
	public void toFront() {
    	super.setState(Frame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}
   
    public void destroy() {
    	this.setVisible(false);
    	this.dispose();
    }

}
