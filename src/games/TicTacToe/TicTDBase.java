package games.TicTacToe;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import controllers.PlayAgent;
import controllers.TD.TDAgent;
import controllers.TD.ntuple.TDNTupleAgt;
import tools.Types;
import tools.Types.ACTIONS;
import controllers.AgentBase;
import controllers.MinimaxAgent;


/**
 * Class {@link TicTDBase} contains basic things which are similar for all 
 * {@link PlayAgent}s  playing TicTacToe. Among these things are: <ul>
 * <li> functions like {@link #tie(int[][])} or {@link #win(int[][])} to decide whether the game is finished,
 * <li> setter and getter for featmode (0,1,2,3,4,5,9 for 'Levkovich', thin, thick, rich, .., raw feature vector),
 * <li> {@link #prepareInputVector(int, int[][])} to generate the feature vector for a board position 
 * (based on <code>featmode</code>, the first parameter),
 * <li> functions like {@link #inputToString(double[])}, {@link #tableToString(int, int[][])} or
 * {@link #stringToTable(String, int[][])} to transform states and feature vectors into strings and vice versa,
 * <li> functions for diagnostic checks like {@link #analyze_hmC(int, int)}, {@link #analyze_hmX(int)}, 
 * {@link #check_state(String, String)}, {@link #diversityCheck(String)}, 
 * {@link #diversityCheck(String, int[])},
 * <li> the HashMaps {@link #hmC} and {@link #hmX} to perform these diagnostics.
 * </ul> 
 * <p>
 * Class {@link TicTDBase} can be trained with different feature sets:
 * <ul>
 * <li>0: Levkovich's features
 * <li>1,2: thin, thick feature set (6 or 10 features)
 * <li>3: thick feature set + board position (19 features)
 * <li>4: extended = thick + extra features (13 features)
 * <li>9: raw = only board position (9 features)
 * </ul> <p>
 * Class {@link TicTDBase} is an abstract class because it does not implement 
 * {@link AgentBase#getScore(games.StateObservation)}. This is left for the derived 
 * classes. <p>
 * Known classes having {@link TicTDBase} as super class: {@link FeatureTTT}
 * 
 * @author Wolfgang Konen, TH Köln, Dec'08 - Nov'16
 */
abstract public class TicTDBase  extends AgentBase implements Serializable {
	private int[][] crosspoint_w;
	private int[][] crosspoint_b;
	private int featmode=2;		// 0,1,2,3,4,5,9 for 'Levkovich',thin,thick,rich,...,raw feature vector

	protected Random rand;			// needed by derived class TDSNPlayer
	//private boolean MakeMove;		// true, if an agent move is requested 
	protected transient MinimaxAgent referee;
	/**
	 * @deprecated (use {@link TDNTupleAgt} instead)
	 */
	private int nTuple[][] = {{0,1,2},{3,4,5},{0,4,8}
							 ,{0,3,6,7,8},{0,4,8,2,6} 
	 						 };
	/**
	 * change the version ID for serialization only if a newer version is no longer 
	 * compatible with an older one (older .agt.zip will become unreadable or you have
	 * to provide a special version transformation)
	 */
	private static final long  serialVersionUID = 12L;

	/**
	 * Counter class, usually mapped to a state (board position): <ul>
	 * <li> <code>counter0</code> counts how often this state was visited during training
	 * <li> <code>counter1</code> counts how often this state was updated during training
	 * </ul>
	 * <b>Only for diagnostics.</b> 
	 */
	protected class CounterC {
		public int counter0;	 
		public int counter1;	
		public CounterC(int c0, int c1) {
			counter0=c0;
			counter1=c1;
		}
	}
	/**
	 * Score class, usually mapped to a state (board position): <ul>
	 * <li> <code>score</code> is the value for this state
	 * <li> <code>counter</code> is incremented each time the corresp. state is visited in trainNet
	 * </ul><p>
	 * ValItPlayer uses this class to map the values to states.
	 */
	protected class ScoreC {
		public int counter; 		 
		public double score; 
		ScoreC(int c, double s) {
			counter=c;
			score=s;
		}
	}
	
	/**
	 * HashMap for all states (board positions). Use hmC.size() to find out how many different 
	 * states were visited during training. <p>
	 * hmC has as key: a board position (state), as value: the counters object (see {@link CounterC}).
	 * hmC is filled in member function <code>trainNet</code> of the derived class.
	 * <p><b>Only for diagnostics.</b> 
	 */
	protected transient HashMap<String,CounterC> hmC;
	/**
	 * HashMap for all feature vectors. Use hmX.size() to find out how many different feature  
	 * vectors were visited during training. <p>
	 * hmX has as key: input feature vector, as value: a HashMap containing all board positions  
	 * (states) which are mapped to this feature vector; this inner HashMap has as key: a state and    
	 * as value: a ScoreC object. ScoreC contains the ideal (Minimax) score and a counter how often 
	 * this state was visited.
	 * hmX is filled in member function <code>trainNet</code> of the derived class.
	 * <p><b>Only for diagnostics.</b> 
	 */
	protected transient HashMap<String, HashMap<String,ScoreC>> hmX;
	protected static transient HashMap<String, Integer> hm2;

	protected int POSVALUES = 3; // Possible Values for each Field of the Board
	protected boolean USESYMMETRY = true; // Use Rotation in NTuple-System
	protected boolean RANDINITWEIGHTS = false; // Init Weights of Value-Function
												// randomly
	/**
	 * Default constructor for TicTDBase, needed for loading a serialized version
	 */
	protected TicTDBase() {
		this("TDS",2);
	}
	protected TicTDBase(String name, int featmode) {
		super(name);
        this.rand = new Random(System.currentTimeMillis());
		this.featmode = featmode;
		crosspoint_w = new int[3][3];
		crosspoint_b = new int[3][3];
		
		// only for diagnostics
        hmC = new HashMap<String, CounterC>();
        hmX = new HashMap<String, HashMap<String,ScoreC>>();
    	String sMinimax = Types.GUI_AGENT_LIST[1];
		referee = new MinimaxAgent(sMinimax);
		
	}
	
	/**
	 * Is it a legal state?  
	 * (equal X and O count if Player==+1, countX == countO+1 if Player==-1)
	 * @param Table
	 * @param Player player to move next
	 * @return
	 */
	public static boolean legalState(int[][] Table, int Player)
	{
		int countX=0, countO=0;
		int delta=-(Player-1)/2;			// the desired countX-countO
		for (int i=0; i<3; i++)
			for (int j=0; j<3; j++) {
				if (Table[i][j]==+1) countX++;
				if (Table[i][j]==-1) countO++;
			}
		if (countX-countO==delta) return true;
		return false;
	}
	
//	/**
//	 * @deprecated (use {@link TD_NTPlayer} instead)
//	 */
//	protected int getInputSize8() {
//		//assert -1 > 0 : "should not be called";
//		if (ntupleSet==null) throw new RuntimeException("ntupleSet not initialized!");
//		return ntupleSet.getInputSize();
//	}
//	/**
//	 * @deprecated (use {@link TD_NTPlayer} instead)
//	 */
//	public NTuple[] getNTuples() {
//		//assert -1 > 0 : "should not be called";
//		return ntupleSet.getNTuples();
//	}
	

	/**
	 * 
	 * @param state
	 * @return int[2] counters: 
	 * 	   [0]: how often was this state visited during training,
	 *     [1]: how often was this state updated during training
	 */
	public int[] getCounters(String state)		
	{		
		int[] counters = new int[3];
		CounterC cc = (CounterC) hmC.get(state);
		if (cc!=null) {
			counters[0] = cc.counter0;
			counters[1] = cc.counter1;
		}
		return counters;	
	}		
	/**
	 * @param player	+1 or -1, player who made the last move (currently not used)
	 * @param table
	 * @return int[2] counters: 
	 * 	   [0]: how often was this state visited during training,
	 *     [1]: how often was this state updated during training
	 */
	public int[] getCounters(int player, int[][] table)		
	{
		return getCounters(TicTDBase.tableToString(player,table));
	}
	
	/**
	 * Helper class for each Nimm3 PlayAgent's getBestTable()
	 * See {@link Scorer#update(double, int[][], int[][], int, boolean, boolean, Random)}
	 * for functional description
	 */
	public class Scorer {
		double MaxScore;
		int count;				// counts the moves with same MaxScore

		public Scorer() {
			MaxScore = -Double.MAX_VALUE;
			count = 1;
		}
		/**
		 * update() is called with different possible moves in NewTable and associated 
		 * scores CurrentScore. If CurrentScore is greater than MaxScore, this move is taken 
		 * and copied to BestTable. If several moves have the same MaxScore, one of them 
		 * is selected at random.
		 *   
		 * @param CurrentScore is initially the score given by the PlayAgent. If NewTable is
		 * 	a win position, CurrentScore is replaced by the final reward. If randomSelect is 
		 *  true, CurrentScore is replaced by a uniform random value from [0,1].
		 * @param NewTable
		 * @param BestTable
		 * @param Player
		 * @param randomSelect
		 * @param silent
		 * @param rand
		 * @return MaxScore
		 */
		public double update(double CurrentScore, int[][] NewTable, int[][] BestTable, 
						int Player, boolean randomSelect, boolean silent, Random rand) 
		{
			if (win(NewTable))
			{
				CurrentScore = Player*(Player+1.0)/2.0;		// 0 / 1 version for O / X - win
				//CurrentScore = +1;						// -1/ 1 version
			}
			if (!silent) print_V(Player,NewTable,CurrentScore*Player);
			if (randomSelect)
			{
				CurrentScore = rand.nextDouble();
			}
			
			if (MaxScore<CurrentScore)		// this is always true on first pass
			{							
				MaxScore = CurrentScore;
				copyTable(NewTable,BestTable);
				count=1;
			}
			else if (MaxScore==CurrentScore)
			{
				// if there are 'count' possibilities with the same score MaxScore, each one
				// has the probability 1/count of being selected:
				count++;
				if (rand.nextDouble()<1.0/count)
				{
					copyTable(NewTable,BestTable);
				}
			}
			return(MaxScore);
		}
	}
	
	/**
	 * Print on System.out the board position table (one line), the net score (scaled to
	 * -1..1) and the input vector, e.g. <p>
	 * <code>      
	 * 			o---XX--o  +0,68229  +2+1+0+0+2+3+0+0+0+1 
	 * </code> 
	 * @param player	the player who left this board position table
	 * @param table
	 * @param score		the net score, usually scaled to -1..1
	 */
	public static void print_V(int player, int[][] table, double score)
	{
		DecimalFormat frmS = new DecimalFormat("+0.00000;-0.00000");
		String str[] = new String[3]; 
		str[0] = "o"; str[1]="-"; str[2]="X";
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<3;j++)
			{
				System.out.print(str[table[i][j]+1]);
			}
		}
		System.out.print("  "+frmS.format(score)+"  ");
		System.out.println();
	}
	
	public static boolean isGameOver(int[][] table) {
		if (win(table)) return true;
		if (tie(table)) return true; 
		return false;
	}
	/**
	 * 
	 * @param Table
	 * @return true, if the current position in Table is a win (for either player)
	 */
	protected static boolean win(int[][] Table)
	{
		if (Win(Table,+1)) return true;
		return Win(Table,-1);
	}
	
	/**
	 * check whether Table contains a win position for player kind
	 * @param Table
	 * @param kind 	1: "X" / white, -1: "O" / black 
	 * @return		true, if it is a win for player kind
	 */
	public static boolean Win(int[][] Table,int kind)
	{
		int[] S=new int[3];
		int i;
		int j;
//		int Kind=kind;			// never used
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++)
			{
				S[j]=Table[i][j];
			}
			if ((S[0]==S[1]) && (S[1]==S[2]) && (S[0]!=0) && S[0]==kind)
			{
				return true;
			}
		}
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++)
			{
				S[j]=Table[j][i];
			}
			if ((S[0]==S[1]) && (S[1]==S[2]) && (S[0]!=0) && S[0]==kind)
			{
				return true;
			}
		}
		
		for (i=0;i<3;i++)
		{
			S[i]=Table[i][i];		// diagonal						
		}
		if ((S[0]==S[1]) && (S[1]==S[2]) && (S[0]!=0) && S[0]==kind)
		{
			return true;
		}
		for (i=0;i<3;i++)
		{
			S[i]=Table[i][2-i];		// anti-diagonal				
		}
		if ((S[0]==S[1]) && (S[1]==S[2]) && (S[0]!=0) && S[0]==kind)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * How many legal states (total / final / in-game) has the game TicTacToe? Prints out on console.
	 * (total = in-game + final) <p>
	 * 
	 * The calculation is verified by the formula in the nice blog post
	 * <a href="http://imagine.kicbak.com/blog/?p=249">http://imagine.kicbak.com/blog/?p=249</a>.
	 *  
	 * @param gameTreeStates <br>
	 *        {@code =false}: count the possible legal boards (without symmetry considerations);<br>   
	 * 		  {@code =true}: count the number of nodes in the game tree
	 * 
	 * @see games.TicTacToe.LaunchTrainTTT
	 */
	public static void countStates2(boolean gameTreeStates) {
		hm2 = new HashMap<String, Integer>();
		StateObserverTTT so = new StateObserverTTT();  // empty board
		int[] nVec = new int[3];   // [nStates, nFinal, nInGame]   
		nVec[0]=1;
		nVec[2]=1;
		countStates2_intern(0,gameTreeStates,so,nVec);
		System.out.print("[TicTDBase.countStates2] nStates="+nVec[0]+
				", nFinal="+nVec[1]+
				", nInGame="+nVec[2]);	
		if (gameTreeStates) {
			System.out.println(" (game tree complexity)");
		} else {
			System.out.println(" (different boards)");
		}
	}
	private static void countStates2_intern(int depth, boolean gts,
			StateObserverTTT so, int[] nVec) 
	{
		StateObserverTTT NewSO;
		String stringRep;
		ArrayList<ACTIONS> act = so.getAvailableActions();
		assert (depth<9) : "Oops, too many recursions";
		for (int i=0; i<act.size(); i++) {
			NewSO = so.copy();
			NewSO.advance(act.get(i));
			stringRep = NewSO.stringDescr();
			Integer in = hm2.get(stringRep); // was this state there before?
			if (in==null) {		// no, then count it
				if (gts==false) hm2.put(stringRep, 9);
				nVec[0] = nVec[0] + 1;
				if (NewSO.isGameOver()) {
					nVec[1] = nVec[1] + 1;
				} else {
					nVec[2] = nVec[2] + 1;
					countStates2_intern(depth+1,gts,NewSO,nVec);
				}			
			}
		}
		return;
	}
	/**
	 * 
	 * @param Table
	 * @return true, if the current position in Table is a tie (draw)
	 */
	public static boolean tie(int[][] Table)
	{
		int i,j;
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++)
			{
				if (Table[i][j]==0) return false;
			}
		}
		return true;
	}
	
	protected static void copyTable(int Table[][], int NewTable[][])
	{		
		for (int i=0;i<3;i++)
		{
			for (int j=0;j<3;j++)
			{
				NewTable[i][j] = Table[i][j];
			}
		}
	}
	
//	public static int[][] cloneTable(int Table[][]) {
//		int[][] NewTable = new int[3][3];
//		copyTable(Table,NewTable);
//		return NewTable;
//	}
	
	
	/**
	 * 
	 * @param {l1,l2,l3}	three fields in a line (horizontal, vertical, diagonal)
	 * @return 	<ul><li> -3/+3, if a line is completely occupied by player -1/+1 (will only happen in a finish state!)
	 * 				<li> -2/+2, if a line has a doublet of player -1/+1 and one empty field
	 * 				<li> -1/+1, if a line has a singlet of player -1/+1 and two empty fields
	 *				<li> 0, if a line has any other content
	 *			</ul> 
	 */
	private int getLineStatus(int l1,int l2, int l3)
	{
		if ((l1==l2) && (l2==l3))
		{
			return l1*3;
		}
		if ((l1==l2) && (l3==0))
		{
			return l1*2;
		}
		if ((l1==l3) && (l2==0))
		{
			return l1*2;
		}
		if ((l2==l3) && (l1==0))
		{
			return l2*2;
		}
		if ((l1!=0) && (l2==0) && (l3==0))
		{
			return l1;
		}
		if ((l1==0) && (l2!=0) && (l3==0))
		{
			return l2;
		}
		if ((l1==0) && (l2==0) && (l3!=0))
		{
			return l3;
		}
		return 0;
	}
	
	// helper function for prepareInputVector0
	private void addFeature0(double[] Input, int[] S, int featSign) {
		int lineStatus;
		lineStatus=getLineStatus(S[0],S[1],S[2]);			
		if (sign(lineStatus)==featSign)
		{
			Input[Math.abs(lineStatus)-1]++;
		}
		else if (sign(lineStatus)==-featSign)
		{
			Input[3+Math.abs(lineStatus)-1]++;   // WK
		}
	}
	// helper function for prepareInputVector1
	private void addFeature1(double[] Input, int[] S, int Player) {
		int lineStatus;
		lineStatus=getLineStatus(S[0],S[1],S[2]);
		if (Player==-1) {
			if (sign(lineStatus)==1)
			{
				Input[Math.abs(lineStatus)-1]++;
			}
		} else {
			if (sign(lineStatus)==-1)
			{
				Input[3+Math.abs(lineStatus)-1]++;  
			}
		}
	}
	// helper function for prepareInputVector2
	private int addFeature2(double[] Input, int[][] diversity, int[] S, int Player, int kind) {
		int lineStatus;
		lineStatus=getLineStatus(S[0],S[1],S[2]);
		if (Math.abs(lineStatus)==3) lineStatus=0;	// erase triplets
		if (sign(lineStatus)==1)
		{
			if (Player==-1) 
			{
				Input[Math.abs(lineStatus)-1]++;
			}
		} else if (sign(lineStatus)==-1) 
		{
			if (Player==+1) 
			{
				Input[2+Math.abs(lineStatus)-1]++;  
			}
		}
		if (sign(lineStatus)==-1)
		{
			diversity[0][kind] = 1; 
		} else if (sign(lineStatus)==+1)
		{
			diversity[1][kind] = 1;
		}
		return lineStatus;
	}
	// helper function for prepareInputVector2
	private void addRandomInputs(double[] Input) {
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				Input[10+3*i+j] = (double) (rand.nextInt(3)-1);
			}
		}
		return;
	}

	/**
	 * Given the current game position {Player,Table} and given the feature
	 * mode coded in member variable {@link #featmode}, construct the feature 
	 * vector (input vector for neural network in {@link controllers.TD.TDAgent} and similar play agents) 
	 * @param Player	+1 or -1, player who **made** the last move
	 * @param Table
	 * @return	double[], the feature vector
	 * <p>
	 * Known classes using prepareInputvector: {@link controllers.TD.TDAgent}
	 */
	protected double[] prepareInputVector(int Player, int Table[][])
	{
		if (featmode==0) {
			return prepareInputVector0(Player,Table);
		} else if (featmode==1) {
			return prepareInputVector1(Player,Table);
		} else if (featmode==2 || featmode==3 || featmode==5) {
			return  prepareInputVector2(Player,Table,featmode); 
		} else if (featmode==4) {
			return prepareInputVector4(Player,Table);
		} else if (featmode==9) {
			return prepareInputVector9(Player,Table);
		} else {  
			throw new RuntimeException("[TicTDBase] featmode = " + featmode + " not allowed!");
		}
	}
	/**
	 * The 'Levkovich' feature vector for featmode=0. <ul>
	 * <li> n0,n1,n2 = number of singlets/doublets/triplets for actual player
	 * <li> n3,n4,n5 = number of singlets/doublets/triplets for opponent
	 * </ul>
	 * @param Player	+1 or -1, player who made the last move
	 * @param Table
	 * @return double[], the feature vector
	 */
	protected double[] prepareInputVector0(int Player, int Table[][])
	{		
		double []Input=new double[6];		
		int i;
		int j;		
		int[] S=new int[3];
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++) 
				S[j]=Table[i][j];
			addFeature0(Input,S,sign(Player));
		}
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++) 
				S[j]=Table[j][i];
			addFeature0(Input,S,sign(Player));
		}
		
		for (i=0;i<3;i++)
			S[i]=Table[i][i];						
		addFeature0(Input,S,sign(Player));
		for (i=0;i<3;i++)
			S[i]=Table[i][2-i];						
		addFeature0(Input,S,sign(Player));
		return Input;		
	}
	/**
	 * The 'thin' feature vector for featmode=1  (modified Levkovich).<ul>
	 * <li> n0,n1,n2 = number of white singlets/doublets/triplets if Player==-1 (black); 0 else
	 * <li> n3,n4,n5 = number of black singlets/doublets/triplets if Player==+1 (white); 0 else
	 * </ul>
	 * @param Player	+1 or -1, player who made the last move
	 * @param Table
	 * @return double[], the feature vector
	 */
	protected double[] prepareInputVector1(int Player, int Table[][])
	{		
		double []Input=new double[6];		
		int i;
		int j;		
		int[] S=new int[3];
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++) 
				S[j]=Table[i][j];
			addFeature1(Input,S,Player);
		}
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++) 
				S[j]=Table[j][i];
			addFeature1(Input,S,Player);
		}
		
		for (i=0;i<3;i++)
			S[i]=Table[i][i];						
		addFeature1(Input,S,Player);
		for (i=0;i<3;i++)
			S[i]=Table[i][2-i];						
		addFeature1(Input,S,Player);
		return Input;		
	}
	/**
	 * The 'rich'/'thick' feature vector for featmode=2,3 or 5. <ul>
	 * <li> n0,n1 = number of white singlets/doublets if Player==-1 (black); 0 else
	 * <li> n2,n3 = number of black singlets/doublets if Player==+1 (white); 0 else
	 * <li> n4,n5 = the diversity of black/white singlets, if Player==-1 (black); 0 else 
	 * <li> n6,n7 = the diversity of black/white singlets, if Player==+1 (white); 0 else
	 * <li> n8,n9 = 1, if an empty crosspoint exists for white/black 
	 * </ul><p>
	 * If featmode==3, then add to this 10 inputs the board position as 9 more inputs. <p>
	 * If featmode==5, then add to this 10 inputs additional 9 random inputs \in {-1,0,1}. <p>
	 * @param Player	+1 or -1, player who made the last move
	 * @param Table		the board position
	 * @param featmode	2, 3 or 5
	 * @return double[], the feature vector
	 */
	protected double[] prepareInputVector2(int Player, int Table[][], int featmode)
	{		
		double[] Input;
		if (featmode==2) {
			Input=new double[10];
		} else { // featmode==3
			Input=new double[19];
		}
		int[][] diversity = new int[2][4];
		int i;
		int j;
		int lstat;
		int[] S=new int[3];
		double dsum0=0,dsum1=0;
		for (i=0;i<3;i++) for(j=0;j<3;j++) {
			crosspoint_w[i][j]=0;
			crosspoint_b[i][j]=0;
		}
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++) 
				S[j]=Table[i][j];
			lstat=addFeature2(Input,diversity,S,Player,0);
			if (lstat>0) for (j=0;j<3;j++) {
				if (S[j]==0) crosspoint_w[i][j] += 1;
			}
			if (lstat<0) for (j=0;j<3;j++) {
				if (S[j]==0) crosspoint_b[i][j] += 1;
			}
		}
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++) 
				S[j]=Table[j][i];
			lstat=addFeature2(Input,diversity,S,Player,1);
			if (lstat>0) for (j=0;j<3;j++) {
				if (S[j]==0) crosspoint_w[j][i] += 1;
			}
			if (lstat<0) for (j=0;j<3;j++) {
				if (S[j]==0) crosspoint_b[j][i] += 1;
			}
		}
		
		for (i=0;i<3;i++)
			S[i]=Table[i][i];						
		lstat=addFeature2(Input,diversity,S,Player,2);
		if (lstat>0) for (j=0;j<3;j++) {
			if (S[j]==0) crosspoint_w[j][j] += 1;
		}
		if (lstat<0) for (j=0;j<3;j++) {
			if (S[j]==0) crosspoint_b[j][j] += 1;
		}
		for (i=0;i<3;i++)
			S[i]=Table[i][2-i];						
		lstat=addFeature2(Input,diversity,S,Player,3);
		if (lstat>0) for (j=0;j<3;j++) {
			if (S[j]==0) crosspoint_w[j][2-j] += 1;
		}
		if (lstat<0) for (j=0;j<3;j++) {
			if (S[j]==0) crosspoint_b[j][2-j] += 1;
		}
		for (i=0;i<4;i++) {
			dsum0 += diversity[0][i];	// black diversity
			dsum1 += diversity[1][i];	// white diversity
		}
		if (Player==-1) {
			Input[4]=dsum0;
			Input[5]=dsum1;
		}
		if (Player==+1) {
			Input[6]=dsum0;
			Input[7]=dsum1;
		}
		for (i=0;i<3;i++) {
			for (j=0;j<3;j++) {
				if (crosspoint_w[i][j]>=2) Input[8]=1;
				if (crosspoint_b[i][j]>=2) Input[9]=1;				
			}
		}
		
		// just present more state differentiating material, nothing special
		if (featmode==3) {
			for (i=0;i<3;i++) {
				for (j=0;j<3;j++) {
					Input[10+3*i+j] = Player*Table[i][j];
				}
			}
		}
		
		// just add 9 more completely irrelevant inputs - distractive?
		if (featmode==5) addRandomInputs(Input);
		
		return Input;		
	}

	/**
	 * The 'extended' feature vector for featmode==4. <ul>
	 * <li> n0..n9  = see {@link #prepareInputVector2(int, int[][], int)}
	 * <li> n10		= occupation of midpoint
	 * <li> n11,12	= number of occupied corners for Player -1, +1	
	 * </ul>
	 * @param Player	+1 or -1, player who made the last move
	 * @param Table		the board position
	 * @return double[], the feature vector
	 */
	protected double[] prepareInputVector4(int Player, int Table[][])
	{		
		int i;
		int j;
		double[] Input = new double[13];;
		double[] Input2 = prepareInputVector2(Player, Table, 2);
		for (i=0; i<Input2.length; i++) Input[i] = Input2[i];
		 
		Input[10] = Table[1][1];		// occupation of midpoint
		for (j=0; j<2; j++) {
			if (Table[0][0]==2*j-1) (Input[11+j])++;
			if (Table[0][2]==2*j-1) (Input[11+j])++;
			if (Table[2][0]==2*j-1) (Input[11+j])++;
			if (Table[2][2]==2*j-1) (Input[11+j])++;
		}
		return Input;
	}
	
	/**
	 * The 'raw' feature vector for featmode==9. <ul>
	 * <li> n0..n8  = the raw board position (after state)
	 * </ul>
	 * @param Player	+1 or -1, player who made the last move
	 * @param Table		the board position
	 * @return double[], the feature vector
	 */
	protected double[] prepareInputVector9(int Player, int Table[][])
	{		
		int i,j,k;
		double[] Input = new double[9];;
		for (i=0,k=0;i<3;i++)
		{
			for (j=0;j<3;j++,k++)
			{
				Input[k] = Table[i][j];
			}
		}
		return Input;
	}

//	/**
//	 * @deprecated (use {@link TDNTupleAgt} instead)
//	 * <p>
//	 * The Ntuple feature vector for featmode==8. <ul>
//	 * <li> length = sum of LUT lengths for all Ntuples
//	 * </ul>
//	 * @param Player	+1 or -1, player who made the last move
//	 * @param Table		the board position
//	 * @return double[], the feature vector
//	 */
//	protected double[] prepareInputNtuple(int Player, int Table[][])
//	{		
//		//assert -1 > 0 : "should not be called";
//		return ntupleSet.prepareInput(Player,Table);
//	}

	/**
	 * @param input	the feature vector
	 * @return the representation of this feature vector as string, e.g. <p>
	 * <code>
	 * 			"+0+0+1-1+0+0+0+2+3+2"
	 * </code>
	 */
	protected String inputToString(double[] input)
	{
		String str="";
		DecimalFormat frmI = new DecimalFormat("+0;-0");
		for (int i=0;i<input.length;i++)
		{
				str = str + frmI.format(input[i]);
		}
		return  str; 
	}
	
	/**
	 * @param Player	+1 or -1, player who made the last move (currently not used)
	 * @param Table
	 * @return	the representation of this board position as string, e.g. <p>
	 * <code>	
	 * 			"XoX--XX--"
	 * </code>
	 */
	protected static String tableToString(int Player, int Table[][])
	{
		String str="";
		String[] ch = {"o","-","X"};
		int i,j;
		for (i=0;i<3;i++)
		{
			for (j=0;j<3;j++)
			{
				str = str + ch[Table[i][j]+1];
			}
		}
		return  str; 
	}
	
	/**
	 * Given a state as string, return its corresponding board position table and player
	 * @param state [input] board position as string	
	 * @param table [output] the corresponding board
	 * @return	the player who makes the next move, -1 for O ('o'), +1 for X ('X')
	 */
	protected int stringToTable(String state, int table[][]) {
		int Xcount=0, Ocount=0;
		for (int i=0;i<3;++i)
			for (int j=0;j<3;++j) {
				if (state.charAt(3*i+j)=='X') {
					table[i][j]=+1;
					Xcount++;
				}
				if (state.charAt(3*i+j)=='o') {
					table[i][j]=-1;
					Ocount++;
				}
			}
		int player = (Ocount-Xcount)*2+1;	// the player who makes the next move
		if (player!=-1 & player!=1)
			throw new RuntimeException("prepareTable: invalid state!!");
		return player;
	}

	/** 
	 * Helper function for diagnostics in {@link controllers.TD.TDAgent#trainAgent(StateObservation)}, ValItPlayer.
	 * @param hsc the {@link HashMap} for which we update or add S_old
	 * @param S_old string rep of old state 
	 * @return the modified {@link HashMap}
	 */
	protected HashMap<String,ScoreC> add_hm_state(HashMap<String,ScoreC> hsc, String S_old) {
		ScoreC sc = hsc.get(S_old);
		if (sc==null) {
			int table[][] = new int[3][3];
			int player = stringToTable(S_old, table);		// reconstruct player & table
			StateObserverTTT so = new StateObserverTTT(table,player);
			double score = referee.getScore(so);
			sc = new ScoreC(0,score);
		}
		sc.counter++;		// each pass through add_hm_state increments the counter for S_old
		hsc.put(S_old, sc);
		return hsc;
	}
	
	/**
	 * Does the state coded in string <code>state</code> lead to the same feature vector 
	 * as represented by <code>istate</code>? 
	 * <p><b>Only for diagnostics.</b>
     * @param state String representation of a board position
     * @param istate String representation of an input feature vector
	 */
	public boolean check_state(String state, String istate) {
		boolean check = false;
		int table[][] = new int[3][3];
		int player = -stringToTable(state, table);
		
		double[] Input = prepareInputVector(player,table);
		if (istate.equals(inputToString(Input))) check=true;
		
		return check;
	}
	
    /**
     * Analyze the information contained in HashMap {@link #hmC}. <ul>
     * <li> How many states were visited?
     * <li> What is the total sum of visit-counts?
     * <li> What is the total sum of updated-counts? 
     * </ul>
	 * <p><b>Only for diagnostics.</b>
     * @param gameNum
     * @see #hmC
     */
	public void analyze_hmC(int gameNum, int verbose) {
		int count=0;
		int csum0=0, csum1=0;
		if (verbose==0) return;
		
    	Iterator<Map.Entry<String,CounterC>> itC = hmC.entrySet().iterator(); 
    	while (itC.hasNext()) {
    			Map.Entry<String,CounterC> entry = itC.next();
        		CounterC cc = entry.getValue();	        		
    			count++;
    			csum0 = csum0+cc.counter0;
        	    csum1 = csum1+cc.counter1;

    	} // while 
    	assert count==hmC.size();
    	if (verbose>=1)
    		System.out.println(gameNum + ": " + count + " number of visited states " + 
     			",    C-sum=" + csum0 + ", " + csum1 + " [TicTDBase.analyze_hmC]");
    } 

    /**
     * Analyze the information contained in HashMap {@link #hmX}. <ul>
     * <li> How many feature vectors were visited?
     * <li> What is average number of states mapped to one feature vector?
     * <li> How many percent of the *states* have feature vectors passing the diversity check 
     *      as described in {@link #diversityCheck(String, int[])}?
     * </ul> 
     * Additional information is printed if the variables <code>showFeat</code> or 
     * <code>showStates</code> in the source code are greater than zero: <ul>
     * <li> a line for each of the first <code>showFeat</code> feature vectors showing its 
     * number of states and its diversity check result
     * <li> multiple lines showing state info for each of the first <code>showStates</code> feature vectors
     * </ul> 
	 * <p><b>Only for diagnostics.</b>
     * @see #hmX
     * @see #diversityCheck(String, int[])
     */
    public void analyze_hmX(int verbose) {
    	if (verbose==0) return;
		int count=0;
		int showFeat=200;		// print out a line for each of the first showFeat feature vectors
		int showStates=0;		// print out the state info for the first showStates feature vectors
		int stateCount=0;		// counts the total number of states (board positions)
		int divCount=0;			// counts the number of *states* for which the divergence check is OK
		String sI,sK;
		DecimalFormat frmC = new DecimalFormat("##0: Feature vector ");
		DecimalFormat frmD = new DecimalFormat("+0.0;-0.0");
		DecimalFormat frmP = new DecimalFormat("00.0%");
		Iterator<Map.Entry<String,HashMap<String,ScoreC>>> itX = hmX.entrySet().iterator();
		HashMap<String,ScoreC> hm;
		while (itX.hasNext()) {
			count++;
			Map.Entry<String,HashMap<String,ScoreC>> entry = itX.next();
			hm = entry.getValue();
			
			sI = entry.getKey();			// the feature vector as string
			stateCount += hm.size();
		    	Iterator<Map.Entry<String,ScoreC>> itS = hm.entrySet().iterator(); 
	    	int[] counters = new int[3]; 	// [0]: O-win, [1]: tie, [2]: X-win 
	    	// the following code is the same as in diversityCheck(String state, int[] counters)
	    	// but with two lines for extra printout 'verbose>=1':
	    	while (itS.hasNext()) {
	    		Map.Entry<String,ScoreC> entryS = itS.next();
	    		ScoreC s = entryS.getValue();
	    		counters[(int)(s.score+1)]++;
	    		sK = entryS.getKey();		// the state as string
	    		if (featmode!=5 && !check_state(sK,sI)) 
	    			throw new RuntimeException("State "+sK+" does not fit to feature vector "+sI);
				if (count<=showStates && verbose>=1) 
					System.out.println("  "+sK+" "+frmD.format(s.score)+" C:"+s.counter);
	    	}
	    	int nZeros=0;
	    	for (int i=0; i<3; i++) if (counters[i]==0) nZeros++;
	    	String divCheck = (nZeros==2 ? "OK" : " W");
	    	if (divCheck.equals("OK")) divCount += hm.size();
	    	
	    	if (count<=showFeat && verbose>=2)
	    		System.out.println(frmC.format(count)+sI + " contains states: " + hm.size() 
	    			+ " Diversity: ("+counters[0]+"/"+counters[1]+"/"+counters[2]+") "+divCheck);
		} // while
		if (hmX.size()>0 && verbose>=1)
		System.out.println("In total: "+ hmX.size() + " feature vectors with "
				+ stateCount + " states. Average: " 
				+ frmD.format((double)stateCount/hmX.size()) + " states/feature vector. DivCheck OK for " 
				+ frmP.format((double)divCount/stateCount) + " [TicTDBase.analyze_hmX]");
	} 

	/**
	 * Given a board position <code>state</code> (e.g. "XoX---Xo-") as input, calculate its 
	 * corresponding feature vector FV and then the diversity of this feature vector FV, i.e. how 
	 * many states belonging to FV are mapped by MinimaxAgent to -1 (O-win), 0 (tie), +1 (X-win).
	 * 
	 * <p><b>Only for diagnostics.</b>
	 * @param state	(e.g. "XoX---Xo-")
	 * @param counters int[3] which contains on output: [0]: O-win, [1]: tie, [2]: X-win counts (Minimax)
	 * @return a boolean flag <code>isDivergent</code>: false if all states are mapped to one score 
	 * or true if they are mapped to different scores.
	 */
	public boolean diversityCheck(String state, int[] counters) {
		String sK;
		int[][] table = new int[3][3];
		int player = -stringToTable(state,table);
		double[] Input = prepareInputVector(player,table);
		String sI = inputToString(Input);
		HashMap<String,ScoreC> hm = hmX.get(sI);	// note that hmX contains the *Minimax* score in its ScoreC objects
		if (hm==null) {
			System.out.println("[diversityCheck] Nothing known in hmX about state "+state+" with feature vector "+sI);
			return false;
		}
		
		for (int i=0; i<3; i++) counters[i]=0;
			
	    	Iterator<Map.Entry<String,ScoreC>> itS = hm.entrySet().iterator(); 
	    	while (itS.hasNext()) {
	    		Map.Entry<String,ScoreC> entryS = itS.next();
	    		ScoreC s = entryS.getValue();
	    		counters[(int)(s.score+1)]++;
	    		sK = entryS.getKey();		// the state as string
	    		if (featmode!=5 && !check_state(sK,sI)) 
	    			throw new RuntimeException("State "+sK+" does not fit to feature vector "+sI);
	    	}
	    	int nZeros=0;
	    	for (int i=0; i<3; i++) if (counters[i]==0) nZeros++;
	    	boolean isDivergent = (nZeros==2 ? false : true);
	    	
	    return isDivergent;

	}
	/**
	 * Given a board position <code>state</code> (e.g. "XoX---Xo-") as input, calculate its 
	 * corresponding feature vector FV and then the diversity of this feature vector FV, i.e. how 
	 * many states belonging to FV are mapped by MinimaxAgent to -1 (O-win), 0 (tie), +1 (X-win).
	 * The resulting divCheck tag is "OK" if all states are mapped to one score or "W" (for 
	 * 'wrong') if they are mapped to different scores. 
	 * <p>
	 * Known callers: {@code Evaluator#innerEval} 
	 * <p><b>Only for diagnostics.</b>
	 * @param state	(e.g. "XoX---Xo-")
	 * @return a string, e.g. "Div: (0/0/8) OK"
	 */
	public String diversityCheck(String state) {
    	int[] counters = new int[3]; 	// [0]: O-win, [1]: tie, [2]: X-win 
		boolean isDivergent;
		
    	isDivergent = diversityCheck(state,counters);
    	String divCheck = (isDivergent ? "W" : "OK");
    	String s = " Div: ("+counters[0]+"/"+counters[1]+"/"+counters[2]+") "+divCheck;
    	return s;
	}
	
	private int sign(int x)
	{
		if (x<0) return -1;
		if (x>0) return 1; 
		return 0;
	}
	
	public int getFeatmode() {
		return featmode;
	}

	public void setFeatmode(int featmode) {
		this.featmode = featmode;
	}

}
