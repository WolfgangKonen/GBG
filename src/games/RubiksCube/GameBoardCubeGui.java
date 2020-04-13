package games.RubiksCube;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.GameBoard;
import games.StateObservation;
import games.Arena;
import games.Arena.Task;
import games.RubiksCube.CSArrayList.CSAListType;
import games.RubiksCube.CSArrayList.TupleInt;
import games.RubiksCube.CubeState.Twist;
import games.ArenaTrain;
import tools.Types;
import tools.Types.ACTIONS;

/**
 * Class GameBoardCube implements interface GameBoard for RubiksCube.
 * It has the board game GUI. 
 * It shows board game states, optionally the values of possible next actions,
 * and allows user interactions with the board to enter legal moves during 
 * game play or to enter board positions for which the agent reaction is 
 * inspected. 
 * 
 * @author Wolfgang Konen, TH Koeln, 2018-2020
 */
public class GameBoardCubeGui extends JFrame {

	private int TICGAMEHEIGHT=280;
	private int labSize = (int)(20*Types.GUI_SCALING_FACTOR_X);
	private JPanel BoardPanel;
	private JPanel ButtonPanel;
	private JLabel leftInfo=new JLabel("");
	private JLabel rightInfo=new JLabel(""); 
	private JLabel pLabel;
	private JComboBox pChoice;
	static String[] pChoiceList = {"1","2","3","4","5","6","RANDOM"};
	/**
	 * The representation of the cube in the GUI. The 24 active panels in the 6*8 field
	 * represent the cubie faces of the flattened cube.
	 */
	protected JPanel[][] Board;
	protected JButton[][] Button;
	//
	// for guiUpdateBoard: which is the row index iarr and the column index jarr
	// for each of the cubie faces in CubeState.fcol[i], i=0,...,23
	private int[] iarr = {1,1,0,0, 2,2,3,3, 2,3,3,2, 5,4,4,5, 3,2,2,3, 3,3,2,2};
	private int[] jarr = {2,3,3,2, 1,0,0,1, 2,2,3,3, 3,3,2,2, 5,5,4,4, 6,7,7,6};

	/**
	 * a reference to the 'parent' {@link GameBoardCube} object
	 */
	private GameBoardCube m_gb=null;
	
	/**
	 * The representation of the state corresponding to the current 
	 * {@link #Board} position.
	 */
//	private StateObserverCube m_so;
	private double[][] VTable;
	/**
	 * the array of distance sets for training
	 */
	private CSArrayList[] D;		
	/**
	 * the array of distance sets for testing (= evaluation)
	 */
	private CSArrayList[] T;		
	private CSArrayList[] D2=null;
	private boolean arenaActReq=false;
	private int[][] realPMat;
	
	/**
	 * If true, select in {@link #chooseStartState(PlayAgent)} from distance set {@link #D}.
	 * If false, use {@link #selectByTwists2(int)}. 
	 */
	private boolean SELECT_FROM_D = true;  
	/**
	 * If true, increment the matrix realPMat, which measures the real p of each start state.  
	 * Make a debug printout of realPMat every 10000 training games.
	 * 
	 * @see #chooseStartState(PlayAgent) chooseStartState(PlayAgent) and its helpers selectByTwists1 or selectByTwists2
	 * @see #incrRealPMat(StateObserverCube, int)
	 * @see #printRealPMat()
	 */
	private boolean DBG_REALPMAT=false;

	
	// the colors of the TH Koeln logo (used for button coloring):
	private Color colTHK1 = new Color(183,29,13);
	private Color colTHK2 = new Color(255,137,0);
	private Color colTHK3 = new Color(162,0,162);
	
	public GameBoardCubeGui(GameBoardCube gb) {
		super("Rubiks Cube");
		m_gb = gb;
		initGameBoard("");
		
		// ensure that ButtonBoard is already visible in the beginning, 
		// if updateBoard() is configured in this way:
		this.updateBoard((StateObserverCube)m_gb.getDefaultStartState(), true, true);	
	}
	
	private void initGameBoard(String title) 
	{
		Board       = new JPanel[6][8];
		BoardPanel	= InitBoard();
		Button		= new JButton[3][3];
		ButtonPanel = InitButton();
		VTable		= new double[3][3];
		pLabel 		= new JLabel("Scrambling Twists: ");
		pChoice		= new JComboBox(pChoiceList);
		pChoice.setSelectedItem("4");

		Font font=new Font("Arial",1,Types.GUI_TITLEFONTSIZE);			
//		JPanel titlePanel = new JPanel();
//		titlePanel.setBackground(Types.GUI_BGCOLOR);
//		JLabel Title=new JLabel("   ",SwingConstants.CENTER);  // no title, it appears sometimes in the wrong place
//		Title.setForeground(Color.black);	
//		Title.setFont(font);	
//		titlePanel.add(Blank);
//		titlePanel.add(Title);
		JLabel Blank=new JLabel("   ");		// a little bit of space
		Blank.setPreferredSize(new Dimension(2*labSize,labSize)); // controls the space between panels
		
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		northPanel.add(pLabel);
		northPanel.add(pChoice);

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
		
		setLayout(new BorderLayout(10,00));
		//add(titlePanel,BorderLayout.NORTH);
		add(northPanel,BorderLayout.NORTH);
		add(boardPanel,BorderLayout.CENTER);
		add(infoPanel,BorderLayout.SOUTH);
		pack();
		setVisible(false);
	}

	private JPanel InitBoard()
	{
		JPanel panel=new JPanel();
		panel.setLayout(new GridLayout(6,8,1,1));
		panel.setBackground(Types.GUI_BGCOLOR);
		int buSize = (int)(25*Types.GUI_SCALING_FACTOR_X);
		Dimension minimumSize = new Dimension(buSize,buSize); //controls the cube face sizes
		for(int i=0;i<6;i++){
			for(int j=0;j<8;j++){
				Board[i][j] = new JPanel();
				Board[i][j].setBackground(colTHK2);
				Board[i][j].setForeground(Color.white);
				Font font=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
		        Board[i][j].setFont(font);
				Board[i][j].setPreferredSize(minimumSize); 
				boolean v = (i==2 || i==3 || j==2 || j==3);
				Board[i][j].setVisible(v);
				panel.add(Board[i][j]);
			}
		}
		return panel;
	}
	
	private JPanel InitButton()
	{
		//JPanel outerPanel=new JPanel(new BorderLayout(0,10));
		//JLabel Blank=new JLabel("  ");		// a little bit of space
		//Blank.setBackground(Types.GUI_BGCOLOR);  
		Font bfont=new Font("Arial",Font.BOLD,Types.GUI_DIALOGFONTSIZE);
		Font lfont=new Font("Arial",Font.BOLD,Types.GUI_HELPFONTSIZE);
		String[] twiStr = {"U","L","F"};
		JPanel panel=new JPanel();
		panel.setLayout(new GridLayout(5,4,4,4));
		panel.setBackground(Types.GUI_BGCOLOR);
		int buSize = (int)(35*Types.GUI_SCALING_FACTOR_X);
		Dimension minimumSize = new Dimension(buSize,buSize); //controls the button sizes
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
		for(int i=0;i<3;i++){
			for(int j=0;j<3;j++){
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
								int dummy=1;
							}
						}
				);
				panel.add(Button[i][j]);
			} // for (j)
			JLabel jlab = new JLabel();
			jlab.setFont(lfont);
			jlab.setText(twiStr[i]);
			jlab.setPreferredSize(new Dimension(labSize,labSize)); // controls the label size
			panel.add(jlab);
		} // for (i)
		for (int j=0; j<3; j++) {
			JLabel jlab = new JLabel();
			jlab.setFont(lfont);
			jlab.setText(""+(j+1));
			jlab.setPreferredSize(new Dimension(labSize,labSize)); // controls the label size
			jlab.setHorizontalAlignment(JLabel.CENTER);
			jlab.setVerticalAlignment(JLabel.TOP);
			panel.add(jlab);
		}
		//outerPanel.add(Blank,BorderLayout.NORTH);
		//outerPanel.add(Blank,BorderLayout.CENTER);
		//outerPanel.add(panel,BorderLayout.CENTER);
		return panel;
	}
	
	public void clearBoard(boolean boardClear, boolean vClear) {
		if (boardClear) {
			leftInfo.setText("  ");
		}
		if (vClear) {
			if (VTable==null) VTable		= new double[3][3];
			for(int i=0;i<3;i++){
				for(int j=0;j<3;j++){
					VTable[i][j] = Double.NaN;
				}
			}
		}
	}

	/**
	 * Update the play board and the associated VBoard.
	 * 
	 * @param so	the game state
	 * @param withReset  if true, reset the board prior to updating it to state so
	 * @param showValueOnGameboard	if true, show the game values for the available actions
	 * 				(only if they are stored in state {@code so}).
	 */
	public void updateBoard(StateObserverCube soN, 
							boolean withReset, boolean showValueOnGameboard) {
		int i,j;
		
		// show ButtonPanel only if it is needed (for showing action values or for entering actions) 
		switch(m_gb.m_Arena.taskState) {
		case INSPECTV: { ButtonPanel.setVisible(true); break; }
		case PLAY:     { ButtonPanel.setVisible(m_gb.m_Arena.hasHumanAgent()||showValueOnGameboard); break; }
		default: 	   { ButtonPanel.setVisible(false); break; }  
		}
		
		// the other choice: have the button panel always visible
		ButtonPanel.setVisible(true); 
		
		if (soN!=null) {
			if (soN.isGameOver()) {
				leftInfo.setText("Solved in "+soN.getMoveCounter() +" twists! (p=" 
						+soN.getCubeState().minTwists+")"); 				
			} else {
				if (soN.getMoveCounter() > m_gb.m_Arena.m_xab.getEpisodeLength(0)) {
					leftInfo.setText("NOT solved in "+soN.getMoveCounter() +" twists! (p=" 
							+soN.getCubeState().minTwists+")"); 				
					
				}				
			}
			
			if (showValueOnGameboard && soN.getStoredValues()!=null) {
				for(i=0;i<3;i++)
					for(j=0;j<3;j++) 
						VTable[i][j]=Double.NaN;	
				
				for (int k=0; k<soN.getStoredValues().length; k++) {
					Types.ACTIONS action = soN.getStoredAction(k);
					int iAction = action.toInt();
					j=iAction%3;
					i=(iAction-j)/3;
					VTable[i][j] = soN.getStoredValues()[k];					
				}	
				rightInfo.setText("");					
			} 
		} // if(so!=null)
		
		guiUpdateBoard(true,showValueOnGameboard);
	}

	/**
	 * Update the play board and the associated action values (raw score*100) for the state 
	 * {@code this.m_so}.
	 * <p>
	 * Color coding for the action buttons, if {@code showValueOnGameBoard==true}:<br>
	 * color green = good move, high value, color red = bad move, low value 
	 * 
	 * @param enable as a side effect, all buttons Button[i][j] 
	 * 				 will be set into enabled state <code>enable</code>. 
	 * @param showValueOnGameboard if true, show the values on the action buttons. If false, 
	 * 				 clear any previous values.
	 */ 
	private void guiUpdateBoard(boolean enable, boolean showValueOnGameboard)
	{		
		int i,j;
		double value;
		String valueTxt;
		int imax=0,jmax=0;
		int[] fcol = m_gb.m_so.getCubeState().fcol;
		Color[] colors = {Color.white, Color.blue, colTHK2, Color.yellow, Color.green, colTHK1};		//{w,b,o,y,g,r}
		for(i=0;i<fcol.length;i++){			
			Board[iarr[i]][jarr[i]].setEnabled(enable);
			Board[iarr[i]][jarr[i]].setBackground(colors[fcol[i]]);
			Board[iarr[i]][jarr[i]].setForeground(Color.white);
		}
		
		for(i=0;i<3;i++)
			for(j=0;j<3;j++) {
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
    private Color calculateTileColor(double tileValue) {
        float percentage = (float) Math.abs(tileValue);
        float inverse_percentage = 1 - percentage;

        Color colorLow;
        Color colorHigh;
        Color colorNeutral = Color.YELLOW;
        int red, blue, green;

//        double vLow=0.0, vMid=0.5, vHigh=1.0;
//        if (tileValue < vMid) {
//            colorLow = Color.RED;
//            colorHigh = colorNeutral;
//        } else {
//            colorLow = colorNeutral;
//            colorHigh = Color.GREEN;
//        }

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

	// --- currently not used ---
	public void enableInteraction(boolean enable) {
//		for(int i=0;i<3;i++){
//			for(int j=0;j<3;j++){
//				Button[i][j].setEnabled(enable);
//			}
//		}
	}

	public String getScramblingTwists() {
		return (String)pChoice.getSelectedItem();
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
	
	public void showGameBoard(Arena ticGame, boolean alignToMain) {
		this.setVisible(true);
		if (alignToMain) {
			// place window with game board below the main window
			int x = ticGame.m_xab.getX() + ticGame.m_xab.getWidth() + 8;
			int y = ticGame.m_xab.getLocation().y;
			if (ticGame.m_ArenaFrame!=null) {
				x = ticGame.m_ArenaFrame.getX();
				y = ticGame.m_ArenaFrame.getY() + ticGame.m_ArenaFrame.getHeight() +1;
				this.setSize(ticGame.m_ArenaFrame.getWidth(),
						 (int)(Types.GUI_SCALING_FACTOR_Y*TICGAMEHEIGHT));	
			}
			this.setLocation(x,y);	
		}		
	}

    @Override
	public void toFront() {
   	super.setState(Frame.NORMAL);	// if window is iconified, display it normally
		super.toFront();
	}
   
    public void destroy() {
	   this.setVisible(false);
	   this.dispose();
    }

    /* ---- METHODS BELOW ARE ONLY FOR DEBUG --- */

    private void checkIntersects() {
    	for (int p=1; p<=CubeConfig.pMax; p++) {
    	    Iterator itD = D[p].iterator();
    	    int intersectCount = 0;
    	    while (itD.hasNext()) {
    		    CubeState twin = (CubeState)itD.next();
    		    if (T[p].contains(twin)) intersectCount++;
            } 
    		System.out.println("checkIntersects: p="+p+", intersect(D[p],T[p])="+intersectCount+", D[p].size="+D[p].size());
    	}   	
    }
    
    /**
     * Find the real pR for state d_so which claims to be in T[p] and increment realPMat 
     * accordingly.
     * The real pR is only guaranteed to be found, if T[p] is complete.
     */
    void incrRealPMat(StateObserverCube d_so, int p) {
		boolean found=false;
		for (int pR=0; pR<=CubeConfig.pMax; pR++) {
			if (T[pR].contains(d_so.getCubeState())) {
				// the real p is pR
				realPMat[p][pR]++;
				found = true;
				break;
			}
		}
		
		if (!found) realPMat[p][CubeConfig.pMax+1]++;
		// A count in realPMat[X][pMax+1] means: the real p is not known for p=X.
		// This can happen if T[pR] is not the complete set: Then d_so might be truly in 
		// the distance set of pR, but it is not found in T[pR].
	}

	public void printRealPMat() {
		DecimalFormat df = new DecimalFormat("  00000");
		for (int i=0; i<realPMat.length; i++) {
			for (int j=0; j<realPMat[i].length; j++) {
				System.out.print(df.format(realPMat[i][j]));
			}
			System.out.println("");
		}
		
	}
	
   
}
