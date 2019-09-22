package games.CFour;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.swing.JDialog;

import agentIO.LoadSaveGBG;
import controllers.PlayAgent;
import controllers.PlayAgent.AgentState;
import games.StateObservation;
import games.XArenaMenu;
//import nTupleTD.TDSAgent;
import games.CFour.openingBook.BookSum;
import params.ParOther;
import tools.Types;
import tools.Types.ACTIONS_VT;
import tools.Types.ScoreTuple;

/**
 * 
 * This class is an implementation of an Alpha-Beta-Agent, specialized to the game CFour. 
 * This agent uses transposition tables, move-ordering, opening books, symmetries and 
 * some other techniques to get fast results.
 * 
 * <p>
 * Changes: <br>
 * 2011-12-12: /MT/ alphaBetaStartP1 and alphaBetaStartP2 look for the most distant
 * loss. This makes the search a little bit slower, but allows a better
 * evaluation of the TD-agent <br>
 * 2019-07-13: /WK/ because {@link C4Base} now extends {@link AgentBase}, this class has 
 * now all methods and members that {@link AgentBase} has.
 * 
 * @author Markus Thill
 * 
 */
public class AlphaBetaAgent extends C4Base implements Serializable, PlayAgent {

	private static final long serialVersionUID = 5000820077350196867L;

	private Random rand = new Random();
//	private AgentState m_agentState;			// now in AgentBase
//	private String m_name = "AlphaBeta";		// now in AgentBase
//	protected ParOther m_oPar = new ParOther();	// now in AgentBase

	// Binary Semaphore, to prevent multiple access (e.g. by parallel threads)
	private Semaphore mutex; // = new Semaphore(1);

	// Transposition Table Constants
	private static final int[] TRANSPOSSIZE = { 262144, 524288, 1048576,
			2097152, 4194304, 8388608, 16777216, 33554432 };
	public static final int[] TRANSPOSBYTES = { 262144 * 99 / 8,
			524288 * 99 / 8, 1048576 * 99 / 8, 2097152 * 99 / 8,
			4194304 * 99 / 8, 8388608 * 99 / 8, 16777216 * 99 / 8,
			33554432 / 8 * 99 };
	private int transPosSize = 4194304 / 2;
	private int lTransPosSize = transPosSize / 8;
	private static final byte TRANSPOSEXACT = 1;
	private static final byte TRANSPOSUPPER = 2;
	private static final byte TRANSPOSLOWER = 3;

	// Random-Numbers for Zobrist Keys
	private static final long rnd20[] = { 0L, 2704506115994628L,
			886077597871704230L, 49389502572435258L, 82333996817139652L,
			263967204879563328L, 81975673952415600L };
	private static final long rnd21[] = { 0L, 73398886193708064L,
			1595863713887220963L, 46261610206381440L, 58705059883690202L,
			231696507446129885L, 22606427398883328L };
	private static final long rnd22[] = { 0L, 12595346104058426L,
			4097820997223100L, 101324622437045280L, 31779374605300125L,
			35633797708573350L, 22416112427922675L };
	private static final long rnd23[] = { 0L, 36363358841386824L,
			4173667863902779612L, 9131869703157656L, 14138249969764235L,
			214348955873908032L, 58547228054472360L };
	private static final long rnd24[] = { 0L, 55740094356093127L,
			1777939723684020L, 362858203316162568L, 28975890403315160L,
			1242349240448115806L, 59601464106441712L };
	private static final long rnd25[] = { 0L, 9110872168202946L,
			10631234269963860L, 16888881020037981L, 1159792823631456L,
			36205525950397736L, 47068546447093808L };
	private static final long rnd26[] = { 0L, 375817237236357603L,
			32189775283681470L, 1493718293429439L, 20793138156733824L,
			101478045365676084L, 110552240521049760L };
	private static final long rnd10[] = { 0L, 19298943901485610L,
			6548220796761019L, 1777628907278452803L, 11891769178478592L,
			3564258696970080L, 236708853179436288L };
	private static final long rnd11[] = { 0L, 349182760342233125L,
			429289086240375L, 121921717543355343L, 31495429917193824L,
			5694462647075520L, 30758051047680284L };
	private static final long rnd12[] = { 0L, 1365712501364505581L,
			17363511325679223L, 119226791868480L, 6220173073409360L,
			11647770880598424L, 24507207907919492L };
	private static final long rnd13[] = { 0L, 551903736927872L,
			2097977134396858L, 3108717381973636075L, 25389306976498643L,
			254362479754036508L, 119080142037405540L };
	private static final long rnd14[] = { 0L, 65628472867223040L,
			116416206906490816L, 130703539652185785L, 1541174198942728362L,
			37852277734190556L, 22426187114508354L };
	private static final long rnd15[] = { 0L, 290694253237906321L,
			3460150747465650L, 12108862858045894L, 124792798959719156L,
			6572334569141376L, 2726416766762766L };
	private static final long rnd16[] = { 0L, 378828340783306008L,
			72087612995472200L, 113983283880328672L, 376285078915283L,
			62397498210717124L, 1193066389676430L };

	// Field-Masks for all Columns
	private static final long fieldMask0[] = fieldMask[0];
	private static final long fieldMask1[] = fieldMask[1];
	private static final long fieldMask2[] = fieldMask[2];
	private static final long fieldMask3[] = fieldMask[3];
	private static final long fieldMask4[] = fieldMask[4];
	private static final long fieldMask5[] = fieldMask[5];
	private static final long fieldMask6[] = fieldMask[6];

	// For Alpha-Beta-Search
	private int movesTillFull = 0;
	private int searchDepth = 100;

	// Opening Book
	private boolean useBook = false;
	private boolean useDeepBook = false;
	private boolean useDeepBookDist = true;

	// Transposition Table for higher Search-Depths
	private long key[] = new long[transPosSize];
	private short value[] = new short[transPosSize];
	private byte flag[] = new byte[transPosSize];

	// Transposition Table for lower Search-Depths
	private long lKey[] = new long[lTransPosSize];
	private short lValue[] = new short[lTransPosSize];
	private byte lFlag[] = new byte[lTransPosSize];

	// If is already searching for a far loose: Don't Change!!!
	private boolean seekFarLoose = true; //false;
	private int looseIntervall = 20;

	// All opening Books
	private transient BookSum books = null;		// transient: to make AlphaBetaAgent serializable

	// Random Choice for a Move if more than one equal Value
	private boolean randomizeEqualMoves = true;

	// Random Choice for a Move, when more than one equal value
	// For a loss make a complete random Move;
	private boolean randomizeLosses = false;

	/**
	 * Generate an empty Board
	 */
	public AlphaBetaAgent(BookSum books) {
		super();
		this.books = books; 	// see comment on 'books' in instantiateAfterLoading()
		mutex = new Semaphore(1);
		setAgentState(AgentState.TRAINED);
	}

	/**
	 * (currently never used in GBG)
	 * 
	 * @param field  a 7x6 Connect-Four board with '1' for player 1 (who starts) 
	 *               and '2' for player 2
	 */
	public AlphaBetaAgent(int field[][], BookSum books) {
		super(field);
		this.books = books; 	// see comment on 'books' in instantiateAfterLoading()
		mutex = new Semaphore(1);
		setAgentState(AgentState.TRAINED);
	}

	/**
	 * (currently never used in GBG)
	 * 
	 * @param fieldP1  BitBoard of Player1
	 * @param fieldP2  BitBoard of Player2
	 */
	public AlphaBetaAgent(long fieldP1, long fieldP2, BookSum books) {
		super(fieldP1, fieldP2);
		this.books = books; 	// see comment on 'books' in instantiateAfterLoading()
		mutex = new Semaphore(1);
		setAgentState(AgentState.TRAINED);
	}

	/**
	 * If agents need a special treatment after being loaded from disk (e. g. instantiation
	 * of transient members), put the relevant code in here.
	 * 
	 * @see LoadSaveGBG#transformObjectToPlayAgent
	 */
	public boolean instantiateAfterLoading() {
		this.books = new BookSum();
		// the book members of 'books' may be initially null. They are read in 
		// on first use by one of the methods BookSum.getOpeningBook[Deep][Dist] which are 
		// called by alphaBetaStartP1, getNextVTable or getScore(int[][],boolean).
		this.resetBoard();
		this.setTransPosSize(4);		// index into table
		this.setBooks(true,false,true);	// use normal book and deep book dist
		this.setDifficulty(42);			// search depth
		this.randomizeEqualMoves(true);
		return true; 
	}

	/**
	 * Init the Transposition-Table. All Values are set to ZERO
	 */
	public void initTranspositionTable() {
		for (int i = 0; i < transPosSize; i++) {
			key[i] = 0L;
			value[i] = 0;
			flag[i] = 0;
		}

		// Transposition Table for lower Search-Depths
		for (int i = 0; i < lTransPosSize; i++) {
			lKey[i] = 0L;
			lValue[i] = 0;
			lFlag[i] = 0;
		}
	}
	

	/**
	 * The difficulty of {@link AlphaBetaAgent} is distinguished by the {@code searchDepth}
	 * 
	 * @param searchDepth
	 */
	public void setDifficulty(int searchDepth) {
		this.searchDepth = (searchDepth + (searchDepth % 2 == 0 ? 1 : 0));
	}

	/**
	 * This method (the source) was generated with a C-program, because its
	 * source-code is very long. <br>
	 * Find a column, in which player can create a threat.
	 * 
	 * @param player
	 * @param startWithCol
	 *            Column to start with search
	 * @return column for creating a threat, or else -1
	 */
	protected int findThreat(int player, int startWithCol) {
		// Prüfen, ob eine Drohung erstellt werden kann
		long temp = (player == PLAYER1 ? fieldP1 : fieldP2);
		switch (startWithCol) {
		case 0:
			switch (colHeight[0]) {
			case 0:
				if ((temp & 0x808100000L) == 0x8100000L && colHeight[1] < 1) {
					return 0;
				}
				if ((temp & 0x410100000L) == 0x400100000L && colHeight[2] < 2) {
					return 0;
				}
				if ((temp & 0x408200000L) == 0x408000000L && colHeight[3] < 3) {
					return 0;
				}
				break;
			case 1:
				if ((temp & 0x404080000L) == 0x4080000L && colHeight[1] < 2) {
					return 0;
				}
				if ((temp & 0x208080000L) == 0x200080000L && colHeight[2] < 3) {
					return 0;
				}
				if ((temp & 0x204100000L) == 0x204000000L && colHeight[3] < 4) {
					return 0;
				}
				if ((temp & 0x810400000L) == 0x10400000L && colHeight[1] < 1) {
					return 0;
				}
				if ((temp & 0x420400000L) == 0x400400000L && colHeight[2] < 1) {
					return 0;
				}
				if ((temp & 0x410800000L) == 0x410000000L && colHeight[3] < 1) {
					return 0;
				}
				break;
			case 2:
				if ((temp & 0x202040000L) == 0x2040000L && colHeight[1] < 3) {
					return 0;
				}
				if ((temp & 0x104040000L) == 0x100040000L && colHeight[2] < 4) {
					return 0;
				}
				if ((temp & 0x102080000L) == 0x102000000L && colHeight[3] < 5) {
					return 0;
				}
				if ((temp & 0x408200000L) == 0x8200000L && colHeight[1] < 2) {
					return 0;
				}
				if ((temp & 0x210200000L) == 0x200200000L && colHeight[2] < 2) {
					return 0;
				}
				if ((temp & 0x208400000L) == 0x208000000L && colHeight[3] < 2) {
					return 0;
				}
				break;
			case 3:
				if ((temp & 0x204100000L) == 0x4100000L && colHeight[1] < 3) {
					return 0;
				}
				if ((temp & 0x108100000L) == 0x100100000L && colHeight[2] < 3) {
					return 0;
				}
				if ((temp & 0x104200000L) == 0x104000000L && colHeight[3] < 3) {
					return 0;
				}
				if ((temp & 0x410800000L) == 0x10800000L && colHeight[1] < 2) {
					return 0;
				}
				if ((temp & 0x220800000L) == 0x200800000L && colHeight[2] < 1) {
					return 0;
				}
				break;
			case 4:
				if ((temp & 0x102080000L) == 0x2080000L && colHeight[1] < 4) {
					return 0;
				}
				if ((temp & 0x84080000L) == 0x80080000L && colHeight[2] < 4) {
					return 0;
				}
				if ((temp & 0x82100000L) == 0x82000000L && colHeight[3] < 4) {
					return 0;
				}
				if ((temp & 0x208400000L) == 0x8400000L && colHeight[1] < 3) {
					return 0;
				}
				if ((temp & 0x110400000L) == 0x100400000L && colHeight[2] < 2) {
					return 0;
				}
				if ((temp & 0x108800000L) == 0x108000000L && colHeight[3] < 1) {
					return 0;
				}
				break;
			case 5:
				if ((temp & 0x81040000L) == 0x1040000L && colHeight[1] < 5) {
					return 0;
				}
				if ((temp & 0x42040000L) == 0x40040000L && colHeight[2] < 5) {
					return 0;
				}
				if ((temp & 0x41080000L) == 0x41000000L && colHeight[3] < 5) {
					return 0;
				}
				if ((temp & 0x104200000L) == 0x4200000L && colHeight[1] < 4) {
					return 0;
				}
				if ((temp & 0x88200000L) == 0x80200000L && colHeight[2] < 3) {
					return 0;
				}
				if ((temp & 0x84400000L) == 0x84000000L && colHeight[3] < 2) {
					return 0;
				}
				break;
			default:
				break;
			}
		case 1:
			switch (colHeight[1]) {
			case 0:
				if ((temp & 0x204000L) == 0x204000L && colHeight[2] < 1) {
					return 1;
				}
				if ((temp & 0x10004000L) == 0x10004000L && colHeight[3] < 2) {
					return 1;
				}
				if ((temp & 0x10200000L) == 0x10200000L && colHeight[4] < 3) {
					return 1;
				}
				break;
			case 1:
				if ((temp & 0x102000L) == 0x102000L && colHeight[2] < 2) {
					return 1;
				}
				if ((temp & 0x8002000L) == 0x8002000L && colHeight[3] < 3) {
					return 1;
				}
				if ((temp & 0x8100000L) == 0x8100000L && colHeight[4] < 4) {
					return 1;
				}
				if ((temp & 0x20000100000L) == 0x20000100000L
						&& colHeight[2] < 2) {
					return 1;
				}
				if ((temp & 0x20008000000L) == 0x20008000000L
						&& colHeight[3] < 3) {
					return 1;
				}
				if ((temp & 0x10400000L) == 0x10400000L
						&& (colHeight[0] < 1 || colHeight[4] < 1)) {
					return 1;
				}
				if ((temp & 0x10000400000L) == 0x10000400000L
						&& colHeight[2] < 1) {
					return 1;
				}
				if ((temp & 0x10010000000L) == 0x10010000000L
						&& colHeight[3] < 1) {
					return 1;
				}
				if ((temp & 0x410000L) == 0x410000L && colHeight[2] < 1) {
					return 1;
				}
				if ((temp & 0x10010000L) == 0x10010000L && colHeight[3] < 1) {
					return 1;
				}
				break;
			case 2:
				if ((temp & 0x81000L) == 0x81000L && colHeight[2] < 3) {
					return 1;
				}
				if ((temp & 0x4001000L) == 0x4001000L && colHeight[3] < 4) {
					return 1;
				}
				if ((temp & 0x4080000L) == 0x4080000L
						&& (colHeight[0] < 1 || colHeight[4] < 5)) {
					return 1;
				}
				if ((temp & 0x10000080000L) == 0x10000080000L
						&& colHeight[2] < 3) {
					return 1;
				}
				if ((temp & 0x10004000000L) == 0x10004000000L
						&& colHeight[3] < 4) {
					return 1;
				}
				if ((temp & 0x8200000L) == 0x8200000L
						&& (colHeight[0] < 2 || colHeight[4] < 2)) {
					return 1;
				}
				if ((temp & 0x8000200000L) == 0x8000200000L && colHeight[2] < 2) {
					return 1;
				}
				if ((temp & 0x8008000000L) == 0x8008000000L && colHeight[3] < 2) {
					return 1;
				}
				if ((temp & 0x208000L) == 0x208000L && colHeight[2] < 2) {
					return 1;
				}
				if ((temp & 0x8008000L) == 0x8008000L && colHeight[3] < 2) {
					return 1;
				}
				if ((temp & 0x10800000L) == 0x10800000L && colHeight[0] < 3) {
					return 1;
				}
				if ((temp & 0x4000800000L) == 0x4000800000L && colHeight[2] < 1) {
					return 1;
				}
				break;
			case 3:
				if ((temp & 0x2040000L) == 0x2040000L && colHeight[0] < 2) {
					return 1;
				}
				if ((temp & 0x8000040000L) == 0x8000040000L && colHeight[2] < 4) {
					return 1;
				}
				if ((temp & 0x8002000000L) == 0x8002000000L && colHeight[3] < 5) {
					return 1;
				}
				if ((temp & 0x4100000L) == 0x4100000L
						&& (colHeight[0] < 3 || colHeight[4] < 3)) {
					return 1;
				}
				if ((temp & 0x4000100000L) == 0x4000100000L && colHeight[2] < 3) {
					return 1;
				}
				if ((temp & 0x4004000000L) == 0x4004000000L && colHeight[3] < 3) {
					return 1;
				}
				if ((temp & 0x104000L) == 0x104000L && colHeight[2] < 3) {
					return 1;
				}
				if ((temp & 0x4004000L) == 0x4004000L && colHeight[3] < 3) {
					return 1;
				}
				if ((temp & 0x8400000L) == 0x8400000L && colHeight[0] < 4) {
					return 1;
				}
				if ((temp & 0x2000400000L) == 0x2000400000L && colHeight[2] < 2) {
					return 1;
				}
				if ((temp & 0x2008000000L) == 0x2008000000L && colHeight[3] < 1) {
					return 1;
				}
				if ((temp & 0x420000L) == 0x420000L && colHeight[2] < 2) {
					return 1;
				}
				if ((temp & 0x8020000L) == 0x8020000L && colHeight[3] < 1) {
					return 1;
				}
				break;
			case 4:
				if ((temp & 0x2080000L) == 0x2080000L
						&& (colHeight[0] < 4 || colHeight[4] < 4)) {
					return 1;
				}
				if ((temp & 0x2000080000L) == 0x2000080000L && colHeight[2] < 4) {
					return 1;
				}
				if ((temp & 0x2002000000L) == 0x2002000000L && colHeight[3] < 4) {
					return 1;
				}
				if ((temp & 0x82000L) == 0x82000L && colHeight[2] < 4) {
					return 1;
				}
				if ((temp & 0x2002000L) == 0x2002000L && colHeight[3] < 4) {
					return 1;
				}
				if ((temp & 0x4200000L) == 0x4200000L
						&& (colHeight[0] < 5 || colHeight[4] < 1)) {
					return 1;
				}
				if ((temp & 0x1000200000L) == 0x1000200000L && colHeight[2] < 3) {
					return 1;
				}
				if ((temp & 0x1004000000L) == 0x1004000000L && colHeight[3] < 2) {
					return 1;
				}
				if ((temp & 0x210000L) == 0x210000L && colHeight[2] < 3) {
					return 1;
				}
				if ((temp & 0x4010000L) == 0x4010000L && colHeight[3] < 2) {
					return 1;
				}
				break;
			case 5:
				if ((temp & 0x1040000L) == 0x1040000L
						&& (colHeight[0] < 5 || colHeight[4] < 5)) {
					return 1;
				}
				if ((temp & 0x1000040000L) == 0x1000040000L && colHeight[2] < 5) {
					return 1;
				}
				if ((temp & 0x1001000000L) == 0x1001000000L && colHeight[3] < 5) {
					return 1;
				}
				if ((temp & 0x41000L) == 0x41000L && colHeight[2] < 5) {
					return 1;
				}
				if ((temp & 0x1001000L) == 0x1001000L && colHeight[3] < 5) {
					return 1;
				}
				if ((temp & 0x108000L) == 0x108000L && colHeight[2] < 4) {
					return 1;
				}
				if ((temp & 0x2008000L) == 0x2008000L && colHeight[3] < 3) {
					return 1;
				}
				if ((temp & 0x2100000L) == 0x2100000L && colHeight[4] < 2) {
					return 1;
				}
				break;
			default:
				break;
			}
		case 2:
			switch (colHeight[2]) {
			case 0:
				if ((temp & 0x8100L) == 0x8100L && colHeight[3] < 1) {
					return 2;
				}
				if ((temp & 0x400100L) == 0x400100L && colHeight[4] < 2) {
					return 2;
				}
				if ((temp & 0x408000L) == 0x408000L && colHeight[5] < 3) {
					return 2;
				}
				break;
			case 1:
				if ((temp & 0x4080L) == 0x4080L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x200080L) == 0x200080L && colHeight[4] < 3) {
					return 2;
				}
				if ((temp & 0x204000L) == 0x204000L && colHeight[5] < 4) {
					return 2;
				}
				if ((temp & 0x200800000L) == 0x200800000L && colHeight[0] < 3) {
					return 2;
				}
				if ((temp & 0x4000800000L) == 0x4000800000L && colHeight[1] < 2) {
					return 2;
				}
				if ((temp & 0x800004000L) == 0x800004000L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x800200000L) == 0x800200000L && colHeight[4] < 3) {
					return 2;
				}
				if ((temp & 0x400400000L) == 0x400400000L
						&& (colHeight[0] < 1 || colHeight[4] < 1)) {
					return 2;
				}
				if ((temp & 0x10000400000L) == 0x10000400000L
						&& colHeight[1] < 1) {
					return 2;
				}
				if ((temp & 0x10400000000L) == 0x10400000000L
						&& colHeight[3] < 1) {
					return 2;
				}
				if ((temp & 0x410000L) == 0x410000L
						&& (colHeight[1] < 1 || colHeight[5] < 1)) {
					return 2;
				}
				if ((temp & 0x400010000L) == 0x400010000L && colHeight[3] < 1) {
					return 2;
				}
				if ((temp & 0x10400L) == 0x10400L && colHeight[3] < 1) {
					return 2;
				}
				if ((temp & 0x400400L) == 0x400400L && colHeight[4] < 1) {
					return 2;
				}
				break;
			case 2:
				if ((temp & 0x2040L) == 0x2040L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x100040L) == 0x100040L && colHeight[4] < 4) {
					return 2;
				}
				if ((temp & 0x102000L) == 0x102000L
						&& (colHeight[1] < 1 || colHeight[5] < 5)) {
					return 2;
				}
				if ((temp & 0x100400000L) == 0x100400000L && colHeight[0] < 4) {
					return 2;
				}
				if ((temp & 0x2000400000L) == 0x2000400000L && colHeight[1] < 3) {
					return 2;
				}
				if ((temp & 0x2100000000L) == 0x2100000000L && colHeight[3] < 1) {
					return 2;
				}
				if ((temp & 0x400002000L) == 0x400002000L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x400100000L) == 0x400100000L && colHeight[4] < 4) {
					return 2;
				}
				if ((temp & 0x200200000L) == 0x200200000L
						&& (colHeight[0] < 2 || colHeight[4] < 2)) {
					return 2;
				}
				if ((temp & 0x8000200000L) == 0x8000200000L && colHeight[1] < 2) {
					return 2;
				}
				if ((temp & 0x8200000000L) == 0x8200000000L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x208000L) == 0x208000L
						&& (colHeight[1] < 2 || colHeight[5] < 2)) {
					return 2;
				}
				if ((temp & 0x200008000L) == 0x200008000L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x8200L) == 0x8200L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x200200L) == 0x200200L && colHeight[4] < 2) {
					return 2;
				}
				if ((temp & 0x420000L) == 0x420000L && colHeight[1] < 3) {
					return 2;
				}
				if ((temp & 0x100020000L) == 0x100020000L && colHeight[3] < 1) {
					return 2;
				}
				if ((temp & 0x20000100000L) == 0x20000100000L
						&& colHeight[1] < 1) {
					return 2;
				}
				if ((temp & 0x20400000000L) == 0x20400000000L
						&& colHeight[3] < 3) {
					return 2;
				}
				break;
			case 3:
				if ((temp & 0x80200000L) == 0x80200000L
						&& (colHeight[0] < 5 || colHeight[4] < 1)) {
					return 2;
				}
				if ((temp & 0x1000200000L) == 0x1000200000L && colHeight[1] < 4) {
					return 2;
				}
				if ((temp & 0x1080000000L) == 0x1080000000L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x81000L) == 0x81000L && colHeight[1] < 2) {
					return 2;
				}
				if ((temp & 0x200001000L) == 0x200001000L && colHeight[3] < 4) {
					return 2;
				}
				if ((temp & 0x200080000L) == 0x200080000L
						&& (colHeight[0] < 1 || colHeight[4] < 5)) {
					return 2;
				}
				if ((temp & 0x100100000L) == 0x100100000L
						&& (colHeight[0] < 3 || colHeight[4] < 3)) {
					return 2;
				}
				if ((temp & 0x4000100000L) == 0x4000100000L && colHeight[1] < 3) {
					return 2;
				}
				if ((temp & 0x4100000000L) == 0x4100000000L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x104000L) == 0x104000L
						&& (colHeight[1] < 3 || colHeight[5] < 3)) {
					return 2;
				}
				if ((temp & 0x100004000L) == 0x100004000L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x4100L) == 0x4100L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x100100L) == 0x100100L && colHeight[4] < 3) {
					return 2;
				}
				if ((temp & 0x210000L) == 0x210000L && colHeight[1] < 4) {
					return 2;
				}
				if ((temp & 0x80010000L) == 0x80010000L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x10000080000L) == 0x10000080000L
						&& colHeight[1] < 2) {
					return 2;
				}
				if ((temp & 0x10200000000L) == 0x10200000000L
						&& colHeight[3] < 4) {
					return 2;
				}
				if ((temp & 0x10800L) == 0x10800L && colHeight[3] < 2) {
					return 2;
				}
				if ((temp & 0x200800L) == 0x200800L && colHeight[4] < 1) {
					return 2;
				}
				break;
			case 4:
				if ((temp & 0x80080000L) == 0x80080000L
						&& (colHeight[0] < 4 || colHeight[4] < 4)) {
					return 2;
				}
				if ((temp & 0x2000080000L) == 0x2000080000L && colHeight[1] < 4) {
					return 2;
				}
				if ((temp & 0x2080000000L) == 0x2080000000L && colHeight[3] < 4) {
					return 2;
				}
				if ((temp & 0x82000L) == 0x82000L
						&& (colHeight[1] < 4 || colHeight[5] < 4)) {
					return 2;
				}
				if ((temp & 0x80002000L) == 0x80002000L && colHeight[3] < 4) {
					return 2;
				}
				if ((temp & 0x2080L) == 0x2080L && colHeight[3] < 4) {
					return 2;
				}
				if ((temp & 0x80080L) == 0x80080L && colHeight[4] < 4) {
					return 2;
				}
				if ((temp & 0x108000L) == 0x108000L
						&& (colHeight[1] < 5 || colHeight[5] < 1)) {
					return 2;
				}
				if ((temp & 0x40008000L) == 0x40008000L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x40100000L) == 0x40100000L && colHeight[4] < 2) {
					return 2;
				}
				if ((temp & 0x100040000L) == 0x100040000L && colHeight[0] < 2) {
					return 2;
				}
				if ((temp & 0x8000040000L) == 0x8000040000L && colHeight[1] < 3) {
					return 2;
				}
				if ((temp & 0x8100000000L) == 0x8100000000L && colHeight[3] < 5) {
					return 2;
				}
				if ((temp & 0x8400L) == 0x8400L && colHeight[3] < 3) {
					return 2;
				}
				if ((temp & 0x100400L) == 0x100400L && colHeight[4] < 2) {
					return 2;
				}
				break;
			case 5:
				if ((temp & 0x40040000L) == 0x40040000L
						&& (colHeight[0] < 5 || colHeight[4] < 5)) {
					return 2;
				}
				if ((temp & 0x1000040000L) == 0x1000040000L && colHeight[1] < 5) {
					return 2;
				}
				if ((temp & 0x1040000000L) == 0x1040000000L && colHeight[3] < 5) {
					return 2;
				}
				if ((temp & 0x41000L) == 0x41000L
						&& (colHeight[1] < 5 || colHeight[5] < 5)) {
					return 2;
				}
				if ((temp & 0x40001000L) == 0x40001000L && colHeight[3] < 5) {
					return 2;
				}
				if ((temp & 0x1040L) == 0x1040L && colHeight[3] < 5) {
					return 2;
				}
				if ((temp & 0x40040L) == 0x40040L && colHeight[4] < 5) {
					return 2;
				}
				if ((temp & 0x4200L) == 0x4200L && colHeight[3] < 4) {
					return 2;
				}
				if ((temp & 0x80200L) == 0x80200L && colHeight[4] < 3) {
					return 2;
				}
				if ((temp & 0x84000L) == 0x84000L && colHeight[5] < 2) {
					return 2;
				}
				break;
			default:
				break;
			}
		case 3:
			switch (colHeight[3]) {
			case 0:
				if ((temp & 0x210000000L) == 0x210000000L && colHeight[0] < 3) {
					return 3;
				}
				if ((temp & 0x4010000000L) == 0x4010000000L && colHeight[1] < 2) {
					return 3;
				}
				if ((temp & 0x4200000000L) == 0x4200000000L && colHeight[2] < 1) {
					return 3;
				}
				if ((temp & 0x204L) == 0x204L && colHeight[4] < 1) {
					return 3;
				}
				if ((temp & 0x10004L) == 0x10004L && colHeight[5] < 2) {
					return 3;
				}
				if ((temp & 0x10200L) == 0x10200L && colHeight[6] < 3) {
					return 3;
				}
				break;
			case 1:
				if ((temp & 0x108000000L) == 0x108000000L && colHeight[0] < 4) {
					return 3;
				}
				if ((temp & 0x2008000000L) == 0x2008000000L && colHeight[1] < 3) {
					return 3;
				}
				if ((temp & 0x2100000000L) == 0x2100000000L && colHeight[2] < 2) {
					return 3;
				}
				if ((temp & 0x102L) == 0x102L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x8002L) == 0x8002L && colHeight[5] < 3) {
					return 3;
				}
				if ((temp & 0x8100L) == 0x8100L && colHeight[6] < 4) {
					return 3;
				}
				if ((temp & 0x8020000L) == 0x8020000L && colHeight[1] < 3) {
					return 3;
				}
				if ((temp & 0x100020000L) == 0x100020000L && colHeight[2] < 2) {
					return 3;
				}
				if ((temp & 0x20000100L) == 0x20000100L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x20008000L) == 0x20008000L && colHeight[5] < 3) {
					return 3;
				}
				if ((temp & 0x410000000L) == 0x410000000L
						&& (colHeight[0] < 1 || colHeight[4] < 1)) {
					return 3;
				}
				if ((temp & 0x10010000000L) == 0x10010000000L
						&& colHeight[1] < 1) {
					return 3;
				}
				if ((temp & 0x10400000000L) == 0x10400000000L
						&& colHeight[2] < 1) {
					return 3;
				}
				if ((temp & 0x10010000L) == 0x10010000L
						&& (colHeight[1] < 1 || colHeight[5] < 1)) {
					return 3;
				}
				if ((temp & 0x400010000L) == 0x400010000L && colHeight[2] < 1) {
					return 3;
				}
				if ((temp & 0x10400L) == 0x10400L
						&& (colHeight[2] < 1 || colHeight[6] < 1)) {
					return 3;
				}
				if ((temp & 0x10000400L) == 0x10000400L && colHeight[4] < 1) {
					return 3;
				}
				if ((temp & 0x410L) == 0x410L && colHeight[4] < 1) {
					return 3;
				}
				if ((temp & 0x10010L) == 0x10010L && colHeight[5] < 1) {
					return 3;
				}
				break;
			case 2:
				if ((temp & 0x84000000L) == 0x84000000L
						&& (colHeight[0] < 5 || colHeight[4] < 1)) {
					return 3;
				}
				if ((temp & 0x1004000000L) == 0x1004000000L && colHeight[1] < 4) {
					return 3;
				}
				if ((temp & 0x1080000000L) == 0x1080000000L && colHeight[2] < 3) {
					return 3;
				}
				if ((temp & 0x81L) == 0x81L && colHeight[4] < 3) {
					return 3;
				}
				if ((temp & 0x4001L) == 0x4001L && colHeight[5] < 4) {
					return 3;
				}
				if ((temp & 0x4080L) == 0x4080L
						&& (colHeight[2] < 1 || colHeight[6] < 5)) {
					return 3;
				}
				if ((temp & 0x4010000L) == 0x4010000L && colHeight[1] < 4) {
					return 3;
				}
				if ((temp & 0x80010000L) == 0x80010000L && colHeight[2] < 3) {
					return 3;
				}
				if ((temp & 0x10000080L) == 0x10000080L && colHeight[4] < 3) {
					return 3;
				}
				if ((temp & 0x10004000L) == 0x10004000L && colHeight[5] < 4) {
					return 3;
				}
				if ((temp & 0x208000000L) == 0x208000000L
						&& (colHeight[0] < 2 || colHeight[4] < 2)) {
					return 3;
				}
				if ((temp & 0x8008000000L) == 0x8008000000L && colHeight[1] < 2) {
					return 3;
				}
				if ((temp & 0x8200000000L) == 0x8200000000L && colHeight[2] < 2) {
					return 3;
				}
				if ((temp & 0x8008000L) == 0x8008000L
						&& (colHeight[1] < 2 || colHeight[5] < 2)) {
					return 3;
				}
				if ((temp & 0x200008000L) == 0x200008000L && colHeight[2] < 2) {
					return 3;
				}
				if ((temp & 0x8200L) == 0x8200L
						&& (colHeight[2] < 2 || colHeight[6] < 2)) {
					return 3;
				}
				if ((temp & 0x8000200L) == 0x8000200L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x208L) == 0x208L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x8008L) == 0x8008L && colHeight[5] < 2) {
					return 3;
				}
				if ((temp & 0x10800L) == 0x10800L && colHeight[2] < 3) {
					return 3;
				}
				if ((temp & 0x4000800L) == 0x4000800L && colHeight[4] < 1) {
					return 3;
				}
				if ((temp & 0x800004000L) == 0x800004000L && colHeight[2] < 1) {
					return 3;
				}
				if ((temp & 0x810000000L) == 0x810000000L && colHeight[4] < 3) {
					return 3;
				}
				break;
			case 3:
				if ((temp & 0x2008000L) == 0x2008000L
						&& (colHeight[1] < 5 || colHeight[5] < 1)) {
					return 3;
				}
				if ((temp & 0x40008000L) == 0x40008000L && colHeight[2] < 4) {
					return 3;
				}
				if ((temp & 0x42000000L) == 0x42000000L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x2040L) == 0x2040L && colHeight[2] < 2) {
					return 3;
				}
				if ((temp & 0x8000040L) == 0x8000040L && colHeight[4] < 4) {
					return 3;
				}
				if ((temp & 0x8002000L) == 0x8002000L
						&& (colHeight[1] < 1 || colHeight[5] < 5)) {
					return 3;
				}
				if ((temp & 0x104000000L) == 0x104000000L
						&& (colHeight[0] < 3 || colHeight[4] < 3)) {
					return 3;
				}
				if ((temp & 0x4004000000L) == 0x4004000000L && colHeight[1] < 3) {
					return 3;
				}
				if ((temp & 0x4100000000L) == 0x4100000000L && colHeight[2] < 3) {
					return 3;
				}
				if ((temp & 0x4004000L) == 0x4004000L
						&& (colHeight[1] < 3 || colHeight[5] < 3)) {
					return 3;
				}
				if ((temp & 0x100004000L) == 0x100004000L && colHeight[2] < 3) {
					return 3;
				}
				if ((temp & 0x4100L) == 0x4100L
						&& (colHeight[2] < 3 || colHeight[6] < 3)) {
					return 3;
				}
				if ((temp & 0x4000100L) == 0x4000100L && colHeight[4] < 3) {
					return 3;
				}
				if ((temp & 0x104L) == 0x104L && colHeight[4] < 3) {
					return 3;
				}
				if ((temp & 0x4004L) == 0x4004L && colHeight[5] < 3) {
					return 3;
				}
				if ((temp & 0x8400L) == 0x8400L && colHeight[2] < 4) {
					return 3;
				}
				if ((temp & 0x2000400L) == 0x2000400L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x400002000L) == 0x400002000L && colHeight[2] < 2) {
					return 3;
				}
				if ((temp & 0x408000000L) == 0x408000000L && colHeight[4] < 4) {
					return 3;
				}
				if ((temp & 0x420L) == 0x420L && colHeight[4] < 2) {
					return 3;
				}
				if ((temp & 0x8020L) == 0x8020L && colHeight[5] < 1) {
					return 3;
				}
				if ((temp & 0x20008000000L) == 0x20008000000L
						&& colHeight[1] < 1) {
					return 3;
				}
				if ((temp & 0x20400000000L) == 0x20400000000L
						&& colHeight[2] < 2) {
					return 3;
				}
				break;
			case 4:
				if ((temp & 0x82000000L) == 0x82000000L
						&& (colHeight[0] < 4 || colHeight[4] < 4)) {
					return 3;
				}
				if ((temp & 0x2002000000L) == 0x2002000000L && colHeight[1] < 4) {
					return 3;
				}
				if ((temp & 0x2080000000L) == 0x2080000000L && colHeight[2] < 4) {
					return 3;
				}
				if ((temp & 0x2002000L) == 0x2002000L
						&& (colHeight[1] < 4 || colHeight[5] < 4)) {
					return 3;
				}
				if ((temp & 0x80002000L) == 0x80002000L && colHeight[2] < 4) {
					return 3;
				}
				if ((temp & 0x2080L) == 0x2080L
						&& (colHeight[2] < 4 || colHeight[6] < 4)) {
					return 3;
				}
				if ((temp & 0x2000080L) == 0x2000080L && colHeight[4] < 4) {
					return 3;
				}
				if ((temp & 0x82L) == 0x82L && colHeight[4] < 4) {
					return 3;
				}
				if ((temp & 0x2002L) == 0x2002L && colHeight[5] < 4) {
					return 3;
				}
				if ((temp & 0x4200L) == 0x4200L
						&& (colHeight[2] < 5 || colHeight[6] < 1)) {
					return 3;
				}
				if ((temp & 0x1000200L) == 0x1000200L && colHeight[4] < 3) {
					return 3;
				}
				if ((temp & 0x1004000L) == 0x1004000L && colHeight[5] < 2) {
					return 3;
				}
				if ((temp & 0x4001000L) == 0x4001000L && colHeight[1] < 2) {
					return 3;
				}
				if ((temp & 0x200001000L) == 0x200001000L && colHeight[2] < 3) {
					return 3;
				}
				if ((temp & 0x204000000L) == 0x204000000L
						&& (colHeight[0] < 1 || colHeight[4] < 5)) {
					return 3;
				}
				if ((temp & 0x210L) == 0x210L && colHeight[4] < 3) {
					return 3;
				}
				if ((temp & 0x4010L) == 0x4010L && colHeight[5] < 2) {
					return 3;
				}
				if ((temp & 0x10004000000L) == 0x10004000000L
						&& colHeight[1] < 2) {
					return 3;
				}
				if ((temp & 0x10200000000L) == 0x10200000000L
						&& colHeight[2] < 3) {
					return 3;
				}
				break;
			case 5:
				if ((temp & 0x41000000L) == 0x41000000L
						&& (colHeight[0] < 5 || colHeight[4] < 5)) {
					return 3;
				}
				if ((temp & 0x1001000000L) == 0x1001000000L && colHeight[1] < 5) {
					return 3;
				}
				if ((temp & 0x1040000000L) == 0x1040000000L && colHeight[2] < 5) {
					return 3;
				}
				if ((temp & 0x1001000L) == 0x1001000L
						&& (colHeight[1] < 5 || colHeight[5] < 5)) {
					return 3;
				}
				if ((temp & 0x40001000L) == 0x40001000L && colHeight[2] < 5) {
					return 3;
				}
				if ((temp & 0x1040L) == 0x1040L
						&& (colHeight[2] < 5 || colHeight[6] < 5)) {
					return 3;
				}
				if ((temp & 0x1000040L) == 0x1000040L && colHeight[4] < 5) {
					return 3;
				}
				if ((temp & 0x41L) == 0x41L && colHeight[4] < 5) {
					return 3;
				}
				if ((temp & 0x1001L) == 0x1001L && colHeight[5] < 5) {
					return 3;
				}
				if ((temp & 0x108L) == 0x108L && colHeight[4] < 4) {
					return 3;
				}
				if ((temp & 0x2008L) == 0x2008L && colHeight[5] < 3) {
					return 3;
				}
				if ((temp & 0x2100L) == 0x2100L && colHeight[6] < 2) {
					return 3;
				}
				if ((temp & 0x102000000L) == 0x102000000L && colHeight[0] < 2) {
					return 3;
				}
				if ((temp & 0x8002000000L) == 0x8002000000L && colHeight[1] < 3) {
					return 3;
				}
				if ((temp & 0x8100000000L) == 0x8100000000L && colHeight[2] < 4) {
					return 3;
				}
				break;
			default:
				break;
			}
		case 4:
			switch (colHeight[4]) {
			case 0:
				if ((temp & 0x8400000L) == 0x8400000L && colHeight[1] < 3) {
					return 4;
				}
				if ((temp & 0x100400000L) == 0x100400000L && colHeight[2] < 2) {
					return 4;
				}
				if ((temp & 0x108000000L) == 0x108000000L && colHeight[3] < 1) {
					return 4;
				}
				break;
			case 1:
				if ((temp & 0x4200000L) == 0x4200000L && colHeight[1] < 4) {
					return 4;
				}
				if ((temp & 0x80200000L) == 0x80200000L && colHeight[2] < 3) {
					return 4;
				}
				if ((temp & 0x84000000L) == 0x84000000L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x200800L) == 0x200800L && colHeight[2] < 3) {
					return 4;
				}
				if ((temp & 0x4000800L) == 0x4000800L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x800004L) == 0x800004L && colHeight[5] < 2) {
					return 4;
				}
				if ((temp & 0x800200L) == 0x800200L && colHeight[6] < 3) {
					return 4;
				}
				if ((temp & 0x10400000L) == 0x10400000L
						&& (colHeight[1] < 1 || colHeight[5] < 1)) {
					return 4;
				}
				if ((temp & 0x400400000L) == 0x400400000L && colHeight[2] < 1) {
					return 4;
				}
				if ((temp & 0x410000000L) == 0x410000000L && colHeight[3] < 1) {
					return 4;
				}
				if ((temp & 0x400400L) == 0x400400L
						&& (colHeight[2] < 1 || colHeight[6] < 1)) {
					return 4;
				}
				if ((temp & 0x10000400L) == 0x10000400L && colHeight[3] < 1) {
					return 4;
				}
				if ((temp & 0x410L) == 0x410L && colHeight[3] < 1) {
					return 4;
				}
				if ((temp & 0x400010L) == 0x400010L && colHeight[5] < 1) {
					return 4;
				}
				break;
			case 2:
				if ((temp & 0x2100000L) == 0x2100000L
						&& (colHeight[1] < 5 || colHeight[5] < 1)) {
					return 4;
				}
				if ((temp & 0x40100000L) == 0x40100000L && colHeight[2] < 4) {
					return 4;
				}
				if ((temp & 0x42000000L) == 0x42000000L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x100400L) == 0x100400L && colHeight[2] < 4) {
					return 4;
				}
				if ((temp & 0x2000400L) == 0x2000400L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x102L) == 0x102L && colHeight[3] < 1) {
					return 4;
				}
				if ((temp & 0x400002L) == 0x400002L && colHeight[5] < 3) {
					return 4;
				}
				if ((temp & 0x400100L) == 0x400100L && colHeight[6] < 4) {
					return 4;
				}
				if ((temp & 0x8200000L) == 0x8200000L
						&& (colHeight[1] < 2 || colHeight[5] < 2)) {
					return 4;
				}
				if ((temp & 0x200200000L) == 0x200200000L && colHeight[2] < 2) {
					return 4;
				}
				if ((temp & 0x208000000L) == 0x208000000L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x200200L) == 0x200200L
						&& (colHeight[2] < 2 || colHeight[6] < 2)) {
					return 4;
				}
				if ((temp & 0x8000200L) == 0x8000200L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x208L) == 0x208L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x200008L) == 0x200008L && colHeight[5] < 2) {
					return 4;
				}
				if ((temp & 0x420L) == 0x420L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x100020L) == 0x100020L && colHeight[5] < 1) {
					return 4;
				}
				if ((temp & 0x20000100L) == 0x20000100L && colHeight[3] < 1) {
					return 4;
				}
				if ((temp & 0x20400000L) == 0x20400000L && colHeight[5] < 3) {
					return 4;
				}
				break;
			case 3:
				if ((temp & 0x80200L) == 0x80200L
						&& (colHeight[2] < 5 || colHeight[6] < 1)) {
					return 4;
				}
				if ((temp & 0x1000200L) == 0x1000200L && colHeight[3] < 4) {
					return 4;
				}
				if ((temp & 0x1080000L) == 0x1080000L && colHeight[5] < 2) {
					return 4;
				}
				if ((temp & 0x81L) == 0x81L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x200001L) == 0x200001L && colHeight[5] < 4) {
					return 4;
				}
				if ((temp & 0x200080L) == 0x200080L
						&& (colHeight[2] < 1 || colHeight[6] < 5)) {
					return 4;
				}
				if ((temp & 0x4100000L) == 0x4100000L
						&& (colHeight[1] < 3 || colHeight[5] < 3)) {
					return 4;
				}
				if ((temp & 0x100100000L) == 0x100100000L && colHeight[2] < 3) {
					return 4;
				}
				if ((temp & 0x104000000L) == 0x104000000L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x100100L) == 0x100100L
						&& (colHeight[2] < 3 || colHeight[6] < 3)) {
					return 4;
				}
				if ((temp & 0x4000100L) == 0x4000100L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x104L) == 0x104L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x100004L) == 0x100004L && colHeight[5] < 3) {
					return 4;
				}
				if ((temp & 0x210L) == 0x210L && colHeight[3] < 4) {
					return 4;
				}
				if ((temp & 0x80010L) == 0x80010L && colHeight[5] < 2) {
					return 4;
				}
				if ((temp & 0x10000080L) == 0x10000080L && colHeight[3] < 2) {
					return 4;
				}
				if ((temp & 0x10200000L) == 0x10200000L && colHeight[5] < 4) {
					return 4;
				}
				if ((temp & 0x800200000L) == 0x800200000L && colHeight[2] < 1) {
					return 4;
				}
				if ((temp & 0x810000000L) == 0x810000000L && colHeight[3] < 2) {
					return 4;
				}
				break;
			case 4:
				if ((temp & 0x2080000L) == 0x2080000L
						&& (colHeight[1] < 4 || colHeight[5] < 4)) {
					return 4;
				}
				if ((temp & 0x80080000L) == 0x80080000L && colHeight[2] < 4) {
					return 4;
				}
				if ((temp & 0x82000000L) == 0x82000000L && colHeight[3] < 4) {
					return 4;
				}
				if ((temp & 0x80080L) == 0x80080L
						&& (colHeight[2] < 4 || colHeight[6] < 4)) {
					return 4;
				}
				if ((temp & 0x2000080L) == 0x2000080L && colHeight[3] < 4) {
					return 4;
				}
				if ((temp & 0x82L) == 0x82L && colHeight[3] < 4) {
					return 4;
				}
				if ((temp & 0x80002L) == 0x80002L && colHeight[5] < 4) {
					return 4;
				}
				if ((temp & 0x108L) == 0x108L && colHeight[3] < 5) {
					return 4;
				}
				if ((temp & 0x40008L) == 0x40008L && colHeight[5] < 3) {
					return 4;
				}
				if ((temp & 0x40100L) == 0x40100L && colHeight[6] < 2) {
					return 4;
				}
				if ((temp & 0x100040L) == 0x100040L && colHeight[2] < 2) {
					return 4;
				}
				if ((temp & 0x8000040L) == 0x8000040L && colHeight[3] < 3) {
					return 4;
				}
				if ((temp & 0x8100000L) == 0x8100000L
						&& (colHeight[1] < 1 || colHeight[5] < 5)) {
					return 4;
				}
				if ((temp & 0x400100000L) == 0x400100000L && colHeight[2] < 2) {
					return 4;
				}
				if ((temp & 0x408000000L) == 0x408000000L && colHeight[3] < 3) {
					return 4;
				}
				break;
			case 5:
				if ((temp & 0x1040000L) == 0x1040000L
						&& (colHeight[1] < 5 || colHeight[5] < 5)) {
					return 4;
				}
				if ((temp & 0x40040000L) == 0x40040000L && colHeight[2] < 5) {
					return 4;
				}
				if ((temp & 0x41000000L) == 0x41000000L && colHeight[3] < 5) {
					return 4;
				}
				if ((temp & 0x40040L) == 0x40040L
						&& (colHeight[2] < 5 || colHeight[6] < 5)) {
					return 4;
				}
				if ((temp & 0x1000040L) == 0x1000040L && colHeight[3] < 5) {
					return 4;
				}
				if ((temp & 0x41L) == 0x41L && colHeight[3] < 5) {
					return 4;
				}
				if ((temp & 0x40001L) == 0x40001L && colHeight[5] < 5) {
					return 4;
				}
				if ((temp & 0x4080000L) == 0x4080000L && colHeight[1] < 2) {
					return 4;
				}
				if ((temp & 0x200080000L) == 0x200080000L && colHeight[2] < 3) {
					return 4;
				}
				if ((temp & 0x204000000L) == 0x204000000L && colHeight[3] < 4) {
					return 4;
				}
				break;
			default:
				break;
			}
		case 5:
			switch (colHeight[5]) {
			case 0:
				if ((temp & 0x210000L) == 0x210000L && colHeight[2] < 3) {
					return 5;
				}
				if ((temp & 0x4010000L) == 0x4010000L && colHeight[3] < 2) {
					return 5;
				}
				if ((temp & 0x4200000L) == 0x4200000L && colHeight[4] < 1) {
					return 5;
				}
				break;
			case 1:
				if ((temp & 0x108000L) == 0x108000L && colHeight[2] < 4) {
					return 5;
				}
				if ((temp & 0x2008000L) == 0x2008000L && colHeight[3] < 3) {
					return 5;
				}
				if ((temp & 0x2100000L) == 0x2100000L && colHeight[4] < 2) {
					return 5;
				}
				if ((temp & 0x8020L) == 0x8020L && colHeight[3] < 3) {
					return 5;
				}
				if ((temp & 0x100020L) == 0x100020L && colHeight[4] < 2) {
					return 5;
				}
				if ((temp & 0x410000L) == 0x410000L
						&& (colHeight[2] < 1 || colHeight[6] < 1)) {
					return 5;
				}
				if ((temp & 0x10010000L) == 0x10010000L && colHeight[3] < 1) {
					return 5;
				}
				if ((temp & 0x10400000L) == 0x10400000L && colHeight[4] < 1) {
					return 5;
				}
				if ((temp & 0x10010L) == 0x10010L && colHeight[3] < 1) {
					return 5;
				}
				if ((temp & 0x400010L) == 0x400010L && colHeight[4] < 1) {
					return 5;
				}
				break;
			case 2:
				if ((temp & 0x84000L) == 0x84000L
						&& (colHeight[2] < 5 || colHeight[6] < 1)) {
					return 5;
				}
				if ((temp & 0x1004000L) == 0x1004000L && colHeight[3] < 4) {
					return 5;
				}
				if ((temp & 0x1080000L) == 0x1080000L && colHeight[4] < 3) {
					return 5;
				}
				if ((temp & 0x4010L) == 0x4010L && colHeight[3] < 4) {
					return 5;
				}
				if ((temp & 0x80010L) == 0x80010L && colHeight[4] < 3) {
					return 5;
				}
				if ((temp & 0x208000L) == 0x208000L
						&& (colHeight[2] < 2 || colHeight[6] < 2)) {
					return 5;
				}
				if ((temp & 0x8008000L) == 0x8008000L && colHeight[3] < 2) {
					return 5;
				}
				if ((temp & 0x8200000L) == 0x8200000L && colHeight[4] < 2) {
					return 5;
				}
				if ((temp & 0x8008L) == 0x8008L && colHeight[3] < 2) {
					return 5;
				}
				if ((temp & 0x200008L) == 0x200008L && colHeight[4] < 2) {
					return 5;
				}
				if ((temp & 0x800004L) == 0x800004L && colHeight[4] < 1) {
					return 5;
				}
				if ((temp & 0x810000L) == 0x810000L && colHeight[6] < 3) {
					return 5;
				}
				break;
			case 3:
				if ((temp & 0x2008L) == 0x2008L && colHeight[3] < 5) {
					return 5;
				}
				if ((temp & 0x40008L) == 0x40008L && colHeight[4] < 4) {
					return 5;
				}
				if ((temp & 0x42000L) == 0x42000L && colHeight[6] < 2) {
					return 5;
				}
				if ((temp & 0x104000L) == 0x104000L
						&& (colHeight[2] < 3 || colHeight[6] < 3)) {
					return 5;
				}
				if ((temp & 0x4004000L) == 0x4004000L && colHeight[3] < 3) {
					return 5;
				}
				if ((temp & 0x4100000L) == 0x4100000L && colHeight[4] < 3) {
					return 5;
				}
				if ((temp & 0x4004L) == 0x4004L && colHeight[3] < 3) {
					return 5;
				}
				if ((temp & 0x100004L) == 0x100004L && colHeight[4] < 3) {
					return 5;
				}
				if ((temp & 0x8002L) == 0x8002L && colHeight[3] < 1) {
					return 5;
				}
				if ((temp & 0x400002L) == 0x400002L && colHeight[4] < 2) {
					return 5;
				}
				if ((temp & 0x408000L) == 0x408000L && colHeight[6] < 4) {
					return 5;
				}
				if ((temp & 0x20008000L) == 0x20008000L && colHeight[3] < 1) {
					return 5;
				}
				if ((temp & 0x20400000L) == 0x20400000L && colHeight[4] < 2) {
					return 5;
				}
				break;
			case 4:
				if ((temp & 0x82000L) == 0x82000L
						&& (colHeight[2] < 4 || colHeight[6] < 4)) {
					return 5;
				}
				if ((temp & 0x2002000L) == 0x2002000L && colHeight[3] < 4) {
					return 5;
				}
				if ((temp & 0x2080000L) == 0x2080000L && colHeight[4] < 4) {
					return 5;
				}
				if ((temp & 0x2002L) == 0x2002L && colHeight[3] < 4) {
					return 5;
				}
				if ((temp & 0x80002L) == 0x80002L && colHeight[4] < 4) {
					return 5;
				}
				if ((temp & 0x4001L) == 0x4001L && colHeight[3] < 2) {
					return 5;
				}
				if ((temp & 0x200001L) == 0x200001L && colHeight[4] < 3) {
					return 5;
				}
				if ((temp & 0x204000L) == 0x204000L
						&& (colHeight[2] < 1 || colHeight[6] < 5)) {
					return 5;
				}
				if ((temp & 0x10004000L) == 0x10004000L && colHeight[3] < 2) {
					return 5;
				}
				if ((temp & 0x10200000L) == 0x10200000L && colHeight[4] < 3) {
					return 5;
				}
				break;
			case 5:
				if ((temp & 0x41000L) == 0x41000L
						&& (colHeight[2] < 5 || colHeight[6] < 5)) {
					return 5;
				}
				if ((temp & 0x1001000L) == 0x1001000L && colHeight[3] < 5) {
					return 5;
				}
				if ((temp & 0x1040000L) == 0x1040000L && colHeight[4] < 5) {
					return 5;
				}
				if ((temp & 0x1001L) == 0x1001L && colHeight[3] < 5) {
					return 5;
				}
				if ((temp & 0x40001L) == 0x40001L && colHeight[4] < 5) {
					return 5;
				}
				if ((temp & 0x102000L) == 0x102000L && colHeight[2] < 2) {
					return 5;
				}
				if ((temp & 0x8002000L) == 0x8002000L && colHeight[3] < 3) {
					return 5;
				}
				if ((temp & 0x8100000L) == 0x8100000L && colHeight[4] < 4) {
					return 5;
				}
				break;
			default:
				break;
			}
		case 6:
			switch (colHeight[6]) {
			case 0:
				if ((temp & 0x8400L) == 0x8400L && colHeight[3] < 3) {
					return 6;
				}
				if ((temp & 0x100400L) == 0x100400L && colHeight[4] < 2) {
					return 6;
				}
				if ((temp & 0x108000L) == 0x108000L && colHeight[5] < 1) {
					return 6;
				}
				break;
			case 1:
				if ((temp & 0x4200L) == 0x4200L && colHeight[3] < 4) {
					return 6;
				}
				if ((temp & 0x80200L) == 0x80200L && colHeight[4] < 3) {
					return 6;
				}
				if ((temp & 0x84000L) == 0x84000L && colHeight[5] < 2) {
					return 6;
				}
				if ((temp & 0x10400L) == 0x10400L && colHeight[3] < 1) {
					return 6;
				}
				if ((temp & 0x400400L) == 0x400400L && colHeight[4] < 1) {
					return 6;
				}
				if ((temp & 0x410000L) == 0x410000L && colHeight[5] < 1) {
					return 6;
				}
				break;
			case 2:
				if ((temp & 0x2100L) == 0x2100L && colHeight[3] < 5) {
					return 6;
				}
				if ((temp & 0x40100L) == 0x40100L && colHeight[4] < 4) {
					return 6;
				}
				if ((temp & 0x42000L) == 0x42000L && colHeight[5] < 3) {
					return 6;
				}
				if ((temp & 0x8200L) == 0x8200L && colHeight[3] < 2) {
					return 6;
				}
				if ((temp & 0x200200L) == 0x200200L && colHeight[4] < 2) {
					return 6;
				}
				if ((temp & 0x208000L) == 0x208000L && colHeight[5] < 2) {
					return 6;
				}
				break;
			case 3:
				if ((temp & 0x4100L) == 0x4100L && colHeight[3] < 3) {
					return 6;
				}
				if ((temp & 0x100100L) == 0x100100L && colHeight[4] < 3) {
					return 6;
				}
				if ((temp & 0x104000L) == 0x104000L && colHeight[5] < 3) {
					return 6;
				}
				if ((temp & 0x800200L) == 0x800200L && colHeight[4] < 1) {
					return 6;
				}
				if ((temp & 0x810000L) == 0x810000L && colHeight[5] < 2) {
					return 6;
				}
				break;
			case 4:
				if ((temp & 0x2080L) == 0x2080L && colHeight[3] < 4) {
					return 6;
				}
				if ((temp & 0x80080L) == 0x80080L && colHeight[4] < 4) {
					return 6;
				}
				if ((temp & 0x82000L) == 0x82000L && colHeight[5] < 4) {
					return 6;
				}
				if ((temp & 0x8100L) == 0x8100L && colHeight[3] < 1) {
					return 6;
				}
				if ((temp & 0x400100L) == 0x400100L && colHeight[4] < 2) {
					return 6;
				}
				if ((temp & 0x408000L) == 0x408000L && colHeight[5] < 3) {
					return 6;
				}
				break;
			case 5:
				if ((temp & 0x1040L) == 0x1040L && colHeight[3] < 5) {
					return 6;
				}
				if ((temp & 0x40040L) == 0x40040L && colHeight[4] < 5) {
					return 6;
				}
				if ((temp & 0x41000L) == 0x41000L && colHeight[5] < 5) {
					return 6;
				}
				if ((temp & 0x4080L) == 0x4080L && colHeight[3] < 2) {
					return 6;
				}
				if ((temp & 0x200080L) == 0x200080L && colHeight[4] < 3) {
					return 6;
				}
				if ((temp & 0x204000L) == 0x204000L && colHeight[5] < 4) {
					return 6;
				}
				break;
			default:
				break;
			}
		default:
			break;
		}
		return (-1);
	}

	// This Method was generated with another Programm

	/**
	 * This Method (the source) was generated with a C-program, because its
	 * source-code is very long. Can only be used for Player 1.<br>
	 * Find a field, in which player 1 can create an odd threat.
	 * 
	 * @param startWith
	 *            Column to start with search
	 * @return column for creating a odd-threat, or else -1
	 */
	protected int findOddThreatP1(int startWith) {
		// Diese Methode sucht nach ungeraden Drohung, JEDOCH nur für den
		// Anziehenden
		switch (startWith) {
		case 0:
			switch (colHeight[0]) {
			case 0:
				if ((fieldP1 & 0x410100000L) == 0x400100000L
						&& colHeight[2] < 2) {
					return 0;
				}
				break;
			case 1:
				if ((fieldP1 & 0x404080000L) == 0x4080000L && colHeight[1] < 2) {
					return 0;
				}
				if ((fieldP1 & 0x204100000L) == 0x204000000L
						&& colHeight[3] < 4) {
					return 0;
				}
				break;
			case 2:
				if ((fieldP1 & 0x104040000L) == 0x100040000L
						&& colHeight[2] < 4) {
					return 0;
				}
				if ((fieldP1 & 0x408200000L) == 0x8200000L && colHeight[1] < 2) {
					return 0;
				}
				if ((fieldP1 & 0x210200000L) == 0x200200000L
						&& colHeight[2] < 2) {
					return 0;
				}
				if ((fieldP1 & 0x208400000L) == 0x208000000L
						&& colHeight[3] < 2) {
					return 0;
				}
				break;
			case 3:
				if ((fieldP1 & 0x410800000L) == 0x10800000L && colHeight[1] < 2) {
					return 0;
				}
				break;
			case 4:
				if ((fieldP1 & 0x102080000L) == 0x2080000L && colHeight[1] < 4) {
					return 0;
				}
				if ((fieldP1 & 0x84080000L) == 0x80080000L && colHeight[2] < 4) {
					return 0;
				}
				if ((fieldP1 & 0x82100000L) == 0x82000000L && colHeight[3] < 4) {
					return 0;
				}
				if ((fieldP1 & 0x110400000L) == 0x100400000L
						&& colHeight[2] < 2) {
					return 0;
				}
				break;
			case 5:
				if ((fieldP1 & 0x104200000L) == 0x4200000L && colHeight[1] < 4) {
					return 0;
				}
				if ((fieldP1 & 0x84400000L) == 0x84000000L && colHeight[3] < 2) {
					return 0;
				}
				break;
			default:
				break;
			}
		case 1:
			switch (colHeight[1]) {
			case 0:
				if ((fieldP1 & 0x10004000L) == 0x10004000L && colHeight[3] < 2) {
					return 1;
				}
				break;
			case 1:
				if ((fieldP1 & 0x102000L) == 0x102000L && colHeight[2] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x8100000L) == 0x8100000L && colHeight[4] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x20000100000L) == 0x20000100000L
						&& colHeight[2] < 2) {
					return 1;
				}
				break;
			case 2:
				if ((fieldP1 & 0x4001000L) == 0x4001000L && colHeight[3] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x10004000000L) == 0x10004000000L
						&& colHeight[3] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x8200000L) == 0x8200000L
						&& (colHeight[0] < 2 || colHeight[4] < 2)) {
					return 1;
				}
				if ((fieldP1 & 0x8000200000L) == 0x8000200000L
						&& colHeight[2] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x8008000000L) == 0x8008000000L
						&& colHeight[3] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x208000L) == 0x208000L && colHeight[2] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x8008000L) == 0x8008000L && colHeight[3] < 2) {
					return 1;
				}
				break;
			case 3:
				if ((fieldP1 & 0x2040000L) == 0x2040000L && colHeight[0] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x8000040000L) == 0x8000040000L
						&& colHeight[2] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x8400000L) == 0x8400000L && colHeight[0] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x2000400000L) == 0x2000400000L
						&& colHeight[2] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x420000L) == 0x420000L && colHeight[2] < 2) {
					return 1;
				}
				break;
			case 4:
				if ((fieldP1 & 0x2080000L) == 0x2080000L
						&& (colHeight[0] < 4 || colHeight[4] < 4)) {
					return 1;
				}
				if ((fieldP1 & 0x2000080000L) == 0x2000080000L
						&& colHeight[2] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x2002000000L) == 0x2002000000L
						&& colHeight[3] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x82000L) == 0x82000L && colHeight[2] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x2002000L) == 0x2002000L && colHeight[3] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x1004000000L) == 0x1004000000L
						&& colHeight[3] < 2) {
					return 1;
				}
				if ((fieldP1 & 0x4010000L) == 0x4010000L && colHeight[3] < 2) {
					return 1;
				}
				break;
			case 5:
				if ((fieldP1 & 0x108000L) == 0x108000L && colHeight[2] < 4) {
					return 1;
				}
				if ((fieldP1 & 0x2100000L) == 0x2100000L && colHeight[4] < 2) {
					return 1;
				}
				break;
			default:
				break;
			}
		case 2:
			switch (colHeight[2]) {
			case 0:
				if ((fieldP1 & 0x400100L) == 0x400100L && colHeight[4] < 2) {
					return 2;
				}
				break;
			case 1:
				if ((fieldP1 & 0x4080L) == 0x4080L && colHeight[3] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x204000L) == 0x204000L && colHeight[5] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x4000800000L) == 0x4000800000L
						&& colHeight[1] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x800004000L) == 0x800004000L
						&& colHeight[3] < 2) {
					return 2;
				}
				break;
			case 2:
				if ((fieldP1 & 0x100040L) == 0x100040L && colHeight[4] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x100400000L) == 0x100400000L
						&& colHeight[0] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x400100000L) == 0x400100000L
						&& colHeight[4] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x200200000L) == 0x200200000L
						&& (colHeight[0] < 2 || colHeight[4] < 2)) {
					return 2;
				}
				if ((fieldP1 & 0x8000200000L) == 0x8000200000L
						&& colHeight[1] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x8200000000L) == 0x8200000000L
						&& colHeight[3] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x208000L) == 0x208000L
						&& (colHeight[1] < 2 || colHeight[5] < 2)) {
					return 2;
				}
				if ((fieldP1 & 0x200008000L) == 0x200008000L
						&& colHeight[3] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x8200L) == 0x8200L && colHeight[3] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x200200L) == 0x200200L && colHeight[4] < 2) {
					return 2;
				}
				break;
			case 3:
				if ((fieldP1 & 0x1000200000L) == 0x1000200000L
						&& colHeight[1] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x1080000000L) == 0x1080000000L
						&& colHeight[3] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x81000L) == 0x81000L && colHeight[1] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x200001000L) == 0x200001000L
						&& colHeight[3] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x210000L) == 0x210000L && colHeight[1] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x80010000L) == 0x80010000L && colHeight[3] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x10000080000L) == 0x10000080000L
						&& colHeight[1] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x10200000000L) == 0x10200000000L
						&& colHeight[3] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x10800L) == 0x10800L && colHeight[3] < 2) {
					return 2;
				}
				break;
			case 4:
				if ((fieldP1 & 0x80080000L) == 0x80080000L
						&& (colHeight[0] < 4 || colHeight[4] < 4)) {
					return 2;
				}
				if ((fieldP1 & 0x2000080000L) == 0x2000080000L
						&& colHeight[1] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x2080000000L) == 0x2080000000L
						&& colHeight[3] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x82000L) == 0x82000L
						&& (colHeight[1] < 4 || colHeight[5] < 4)) {
					return 2;
				}
				if ((fieldP1 & 0x80002000L) == 0x80002000L && colHeight[3] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x2080L) == 0x2080L && colHeight[3] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x80080L) == 0x80080L && colHeight[4] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x40100000L) == 0x40100000L && colHeight[4] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x100040000L) == 0x100040000L
						&& colHeight[0] < 2) {
					return 2;
				}
				if ((fieldP1 & 0x100400L) == 0x100400L && colHeight[4] < 2) {
					return 2;
				}
				break;
			case 5:
				if ((fieldP1 & 0x4200L) == 0x4200L && colHeight[3] < 4) {
					return 2;
				}
				if ((fieldP1 & 0x84000L) == 0x84000L && colHeight[5] < 2) {
					return 2;
				}
				break;
			default:
				break;
			}
		case 3:
			switch (colHeight[3]) {
			case 0:
				if ((fieldP1 & 0x4010000000L) == 0x4010000000L
						&& colHeight[1] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x10004L) == 0x10004L && colHeight[5] < 2) {
					return 3;
				}
				break;
			case 1:
				if ((fieldP1 & 0x108000000L) == 0x108000000L
						&& colHeight[0] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2100000000L) == 0x2100000000L
						&& colHeight[2] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x102L) == 0x102L && colHeight[4] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8100L) == 0x8100L && colHeight[6] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x100020000L) == 0x100020000L
						&& colHeight[2] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x20000100L) == 0x20000100L && colHeight[4] < 2) {
					return 3;
				}
				break;
			case 2:
				if ((fieldP1 & 0x1004000000L) == 0x1004000000L
						&& colHeight[1] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x4001L) == 0x4001L && colHeight[5] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x4010000L) == 0x4010000L && colHeight[1] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x10004000L) == 0x10004000L && colHeight[5] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x208000000L) == 0x208000000L
						&& (colHeight[0] < 2 || colHeight[4] < 2)) {
					return 3;
				}
				if ((fieldP1 & 0x8008000000L) == 0x8008000000L
						&& colHeight[1] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8200000000L) == 0x8200000000L
						&& colHeight[2] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8008000L) == 0x8008000L
						&& (colHeight[1] < 2 || colHeight[5] < 2)) {
					return 3;
				}
				if ((fieldP1 & 0x200008000L) == 0x200008000L
						&& colHeight[2] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8200L) == 0x8200L
						&& (colHeight[2] < 2 || colHeight[6] < 2)) {
					return 3;
				}
				if ((fieldP1 & 0x8000200L) == 0x8000200L && colHeight[4] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x208L) == 0x208L && colHeight[4] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8008L) == 0x8008L && colHeight[5] < 2) {
					return 3;
				}
				break;
			case 3:
				if ((fieldP1 & 0x40008000L) == 0x40008000L && colHeight[2] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x42000000L) == 0x42000000L && colHeight[4] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x2040L) == 0x2040L && colHeight[2] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8000040L) == 0x8000040L && colHeight[4] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x8400L) == 0x8400L && colHeight[2] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2000400L) == 0x2000400L && colHeight[4] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x400002000L) == 0x400002000L
						&& colHeight[2] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x408000000L) == 0x408000000L
						&& colHeight[4] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x420L) == 0x420L && colHeight[4] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x20400000000L) == 0x20400000000L
						&& colHeight[2] < 2) {
					return 3;
				}
				break;
			case 4:
				if ((fieldP1 & 0x82000000L) == 0x82000000L
						&& (colHeight[0] < 4 || colHeight[4] < 4)) {
					return 3;
				}
				if ((fieldP1 & 0x2002000000L) == 0x2002000000L
						&& colHeight[1] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2080000000L) == 0x2080000000L
						&& colHeight[2] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2002000L) == 0x2002000L
						&& (colHeight[1] < 4 || colHeight[5] < 4)) {
					return 3;
				}
				if ((fieldP1 & 0x80002000L) == 0x80002000L && colHeight[2] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2080L) == 0x2080L
						&& (colHeight[2] < 4 || colHeight[6] < 4)) {
					return 3;
				}
				if ((fieldP1 & 0x2000080L) == 0x2000080L && colHeight[4] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x82L) == 0x82L && colHeight[4] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2002L) == 0x2002L && colHeight[5] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x1004000L) == 0x1004000L && colHeight[5] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x4001000L) == 0x4001000L && colHeight[1] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x4010L) == 0x4010L && colHeight[5] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x10004000000L) == 0x10004000000L
						&& colHeight[1] < 2) {
					return 3;
				}
				break;
			case 5:
				if ((fieldP1 & 0x108L) == 0x108L && colHeight[4] < 4) {
					return 3;
				}
				if ((fieldP1 & 0x2100L) == 0x2100L && colHeight[6] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x102000000L) == 0x102000000L
						&& colHeight[0] < 2) {
					return 3;
				}
				if ((fieldP1 & 0x8100000000L) == 0x8100000000L
						&& colHeight[2] < 4) {
					return 3;
				}
				break;
			default:
				break;
			}
		case 4:
			switch (colHeight[4]) {
			case 0:
				if ((fieldP1 & 0x100400000L) == 0x100400000L
						&& colHeight[2] < 2) {
					return 4;
				}
				break;
			case 1:
				if ((fieldP1 & 0x4200000L) == 0x4200000L && colHeight[1] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x84000000L) == 0x84000000L && colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x4000800L) == 0x4000800L && colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x800004L) == 0x800004L && colHeight[5] < 2) {
					return 4;
				}
				break;
			case 2:
				if ((fieldP1 & 0x40100000L) == 0x40100000L && colHeight[2] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x100400L) == 0x100400L && colHeight[2] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x400100L) == 0x400100L && colHeight[6] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x8200000L) == 0x8200000L
						&& (colHeight[1] < 2 || colHeight[5] < 2)) {
					return 4;
				}
				if ((fieldP1 & 0x200200000L) == 0x200200000L
						&& colHeight[2] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x208000000L) == 0x208000000L
						&& colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x200200L) == 0x200200L
						&& (colHeight[2] < 2 || colHeight[6] < 2)) {
					return 4;
				}
				if ((fieldP1 & 0x8000200L) == 0x8000200L && colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x208L) == 0x208L && colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x200008L) == 0x200008L && colHeight[5] < 2) {
					return 4;
				}
				break;
			case 3:
				if ((fieldP1 & 0x1000200L) == 0x1000200L && colHeight[3] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x1080000L) == 0x1080000L && colHeight[5] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x81L) == 0x81L && colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x200001L) == 0x200001L && colHeight[5] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x210L) == 0x210L && colHeight[3] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x80010L) == 0x80010L && colHeight[5] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x10000080L) == 0x10000080L && colHeight[3] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x10200000L) == 0x10200000L && colHeight[5] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x810000000L) == 0x810000000L
						&& colHeight[3] < 2) {
					return 4;
				}
				break;
			case 4:
				if ((fieldP1 & 0x2080000L) == 0x2080000L
						&& (colHeight[1] < 4 || colHeight[5] < 4)) {
					return 4;
				}
				if ((fieldP1 & 0x80080000L) == 0x80080000L && colHeight[2] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x82000000L) == 0x82000000L && colHeight[3] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x80080L) == 0x80080L
						&& (colHeight[2] < 4 || colHeight[6] < 4)) {
					return 4;
				}
				if ((fieldP1 & 0x2000080L) == 0x2000080L && colHeight[3] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x82L) == 0x82L && colHeight[3] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x80002L) == 0x80002L && colHeight[5] < 4) {
					return 4;
				}
				if ((fieldP1 & 0x40100L) == 0x40100L && colHeight[6] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x100040L) == 0x100040L && colHeight[2] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x400100000L) == 0x400100000L
						&& colHeight[2] < 2) {
					return 4;
				}
				break;
			case 5:
				if ((fieldP1 & 0x4080000L) == 0x4080000L && colHeight[1] < 2) {
					return 4;
				}
				if ((fieldP1 & 0x204000000L) == 0x204000000L
						&& colHeight[3] < 4) {
					return 4;
				}
				break;
			default:
				break;
			}
		case 5:
			switch (colHeight[5]) {
			case 0:
				if ((fieldP1 & 0x4010000L) == 0x4010000L && colHeight[3] < 2) {
					return 5;
				}
				break;
			case 1:
				if ((fieldP1 & 0x108000L) == 0x108000L && colHeight[2] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x2100000L) == 0x2100000L && colHeight[4] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x100020L) == 0x100020L && colHeight[4] < 2) {
					return 5;
				}
				break;
			case 2:
				if ((fieldP1 & 0x1004000L) == 0x1004000L && colHeight[3] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x4010L) == 0x4010L && colHeight[3] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x208000L) == 0x208000L
						&& (colHeight[2] < 2 || colHeight[6] < 2)) {
					return 5;
				}
				if ((fieldP1 & 0x8008000L) == 0x8008000L && colHeight[3] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x8200000L) == 0x8200000L && colHeight[4] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x8008L) == 0x8008L && colHeight[3] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x200008L) == 0x200008L && colHeight[4] < 2) {
					return 5;
				}
				break;
			case 3:
				if ((fieldP1 & 0x40008L) == 0x40008L && colHeight[4] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x42000L) == 0x42000L && colHeight[6] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x400002L) == 0x400002L && colHeight[4] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x408000L) == 0x408000L && colHeight[6] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x20400000L) == 0x20400000L && colHeight[4] < 2) {
					return 5;
				}
				break;
			case 4:
				if ((fieldP1 & 0x82000L) == 0x82000L
						&& (colHeight[2] < 4 || colHeight[6] < 4)) {
					return 5;
				}
				if ((fieldP1 & 0x2002000L) == 0x2002000L && colHeight[3] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x2080000L) == 0x2080000L && colHeight[4] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x2002L) == 0x2002L && colHeight[3] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x80002L) == 0x80002L && colHeight[4] < 4) {
					return 5;
				}
				if ((fieldP1 & 0x4001L) == 0x4001L && colHeight[3] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x10004000L) == 0x10004000L && colHeight[3] < 2) {
					return 5;
				}
				break;
			case 5:
				if ((fieldP1 & 0x102000L) == 0x102000L && colHeight[2] < 2) {
					return 5;
				}
				if ((fieldP1 & 0x8100000L) == 0x8100000L && colHeight[4] < 4) {
					return 5;
				}
				break;
			default:
				break;
			}
		case 6:
			switch (colHeight[6]) {
			case 0:
				if ((fieldP1 & 0x100400L) == 0x100400L && colHeight[4] < 2) {
					return 6;
				}
				break;
			case 1:
				if ((fieldP1 & 0x4200L) == 0x4200L && colHeight[3] < 4) {
					return 6;
				}
				if ((fieldP1 & 0x84000L) == 0x84000L && colHeight[5] < 2) {
					return 6;
				}
				break;
			case 2:
				if ((fieldP1 & 0x40100L) == 0x40100L && colHeight[4] < 4) {
					return 6;
				}
				if ((fieldP1 & 0x8200L) == 0x8200L && colHeight[3] < 2) {
					return 6;
				}
				if ((fieldP1 & 0x200200L) == 0x200200L && colHeight[4] < 2) {
					return 6;
				}
				if ((fieldP1 & 0x208000L) == 0x208000L && colHeight[5] < 2) {
					return 6;
				}
				break;
			case 3:
				if ((fieldP1 & 0x810000L) == 0x810000L && colHeight[5] < 2) {
					return 6;
				}
				break;
			case 4:
				if ((fieldP1 & 0x2080L) == 0x2080L && colHeight[3] < 4) {
					return 6;
				}
				if ((fieldP1 & 0x80080L) == 0x80080L && colHeight[4] < 4) {
					return 6;
				}
				if ((fieldP1 & 0x82000L) == 0x82000L && colHeight[5] < 4) {
					return 6;
				}
				if ((fieldP1 & 0x400100L) == 0x400100L && colHeight[4] < 2) {
					return 6;
				}
				break;
			case 5:
				if ((fieldP1 & 0x4080L) == 0x4080L && colHeight[3] < 2) {
					return 6;
				}
				if ((fieldP1 & 0x204000L) == 0x204000L && colHeight[5] < 4) {
					return 6;
				}
				break;
			default:
				break;
			}
		default:
			break;
		}
		return (-1);
	}

	/**
	 * Generate the zobrist-Key for a board
	 * 
	 * @param f1
	 *            BitBoard of Player1
	 * @param f2
	 *            BitBoard of Player2
	 * @return
	 */
	public static long toZobrist(long f1, long f2) {
		int i, j, stelle;
		long temp;
		long ZobristKey = 0L;
		for (i = 0; i < 7; i++) {
			for (j = 0; j < 6; j++) {
				stelle = 41 - (i * 6 + j);
				temp = (1L << stelle);
				if ((f1 & temp) == temp)
					ZobristKey ^= rnd[0][i * 6 + j];
				else if ((f2 & temp) == temp)
					ZobristKey ^= rnd[1][i * 6 + j];
				else
					break;
			}
		}
		return ZobristKey;
	}

	/**
	 * Check, if the current board is symmetric
	 * 
	 * @return
	 */
	private boolean isSymmetric() {
		long fieldP1mirrored = getMirroredField(PLAYER1);
		long fieldP2mirrored = getMirroredField(PLAYER2);
		return (fieldP1mirrored == fieldP1 && fieldP2mirrored == fieldP2);
	}

	/**
	 * Check, if it is possible to create a symmetric board from the current one
	 * 
	 * @return
	 */
	private boolean symPossible() {
		long fieldP1mirrored = getMirroredField(PLAYER1);
		long fieldP2mirrored = getMirroredField(PLAYER2);
		return ((fieldP1mirrored & fieldP2) == 0L && (fieldP2mirrored & fieldP1) == 0L);
	}

	/**
	 * Root-node of the alpha-Beta-algorithm
	 * 
	 * @param retValue
	 *            true, if game-theoretic-value shall be returned
	 * @return best move for the current player, or game-theoretic-value for the
	 *         current board
	 */
	protected int rootNode(boolean retValue) {

		int moves[], x, y = 0, bestMove = -1, player;
		int value = 0, alpha = -9999, beta = 9999;

		// check, which players turn
		player = (countPieces() % 2 == 0 ? PLAYER1 : PLAYER2);

		// Number of moves until a draw is reached
		movesTillFull = (42 - countPieces());
		moves = generateMoves(player, true);

		// look for a direct Win for the current player
		if (hasWin(player)) {
			if (!retValue)
				for (x = 0; moves[x] != -1; x++) {
					if (canWin(player, moves[x], colHeight[moves[x]])) {
						return moves[x];
					}
				}
			else {
				return (player == 1 ? 1000 : -1000);
			}
		}

		// If the board is symmetric, only one half has to be analyzed
		if (isSymmetric()) {
			int tmp[] = new int[8];
			for (x = 0; moves[x] != -1; x++) {
				if (moves[x] < 4)
					tmp[y++] = moves[x];
			}
			for (x = 0; x < y; x++)
				moves[x] = tmp[x];
			moves[x] = -1;
		}

		// Try all moves for the current player
		for (x = 0; moves[x] > -1; x++) {
			putPiece(player, moves[x]);
			if (player == PLAYER1)
				value = alphaBetaStartP2(1, alpha, beta,
						toZobrist(fieldP1, fieldP2), symPossible());
			else
				value = alphaBetaStartP1(1, alpha, beta,
						toZobrist(fieldP1, fieldP2), symPossible());
			removePiece(player, moves[x]);
			if (player == PLAYER1) {
				if (value > alpha) {
					alpha = value;
					bestMove = moves[x];
				}
				if (value == 1000) {
					if (!retValue)
						return bestMove;
					return alpha;
				}
			} else if (player == PLAYER2) {
				if (value < beta) {
					beta = value;
					bestMove = moves[x];
				}
				if (value == -1000) {
					if (!retValue)
						return bestMove;
					return beta;
				}
			}
		}

		// if the board is a loose for the player, look for the most distant one
		if (!seekFarLoose
				&& !retValue
				&& ((player == PLAYER1 && alpha < -500) || (player == PLAYER2 && beta > 500))
				&& (countPieces() > 12 || useDeepBookDist)) {

			initTranspositionTable();

			boolean oldUsebook = useBook;
			boolean oldUseDeepBook = useDeepBook;
			boolean oldUseDeepBookDist = useDeepBookDist;
			useBook = false;
			useDeepBook = false;
			useDeepBookDist = true;
			seekFarLoose = true;
			looseIntervall = 42 / countPieces() * 2;
			int newBestMove = rootNode(false);
			seekFarLoose = false;
			looseIntervall = 20;

			useBook = oldUsebook;
			useDeepBook = oldUseDeepBook;
			useDeepBookDist = oldUseDeepBookDist;
			System.gc();
			return newBestMove;
		}
		if (!retValue)
			return bestMove;
		return (player == 1 ? alpha : beta);
	}

	/**
	 * @param depth
	 *            current search depth
	 * @param alpha
	 * @param beta
	 * @param zobr
	 *            current Zobrist-Key
	 * @param symPos
	 *            is a symmetry possible for the current board
	 * @return
	 */
	private int alphaBetaStartP1(int depth, int alpha, int beta, long zobr,
			boolean symPos) {
		// distance to win / loss
		int distance = depth;// (seekFarLoose ? countPieces() / looseIntervall :
								// 0);

		// First, check for own Win (Player 1)
		if (hasWin(PLAYER1))
			return 1020 - distance;

		// Check for draw
		if (depth == movesTillFull) {
			return 0;
		}

		// if search-horizon is reached
		if (depth >= searchDepth) {
			int eval = evaluate(PLAYER1, zobr); //Params actually not needed
			if (eval != -1)
				return eval;
			// if evaluate doesn't get a proper value, then
			// search a little deeper
		}

		// Normal Book
		if (useBook && depth < 10 && countPieces() == 8 && books != null) {
			// The params must be swapped, because the database uses a other
			// representation
			int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
			int codedPosMirrored = fieldToHuffman(getMirroredField(PLAYER2),
					getMirroredField(PLAYER1), true);
			int value = books.getOpeningBook().getValue(codedPos,
					codedPosMirrored);
			if (value == 0) {
				return 0;
			} else if (value == 1) {
				return -992;
			} else {
				return 992;
			}
		}

		// Deep Book with Distance
		if (useDeepBookDist && depth < 14 && countPieces() == 12
				&& books != null) {
			// The params must be swapped, because the database uses a other
			// representation
			int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
			int codedPosMirrored = fieldToHuffman(getMirroredField(PLAYER2),
					getMirroredField(PLAYER1), true);
			return books.getOpeningBookDeepDist().getValue(codedPos,
					codedPosMirrored);
		}

		// Deep Book
		if (useDeepBook && depth < 14 && countPieces() == 12 && books != null) {
			// The params must be swapped, because the database uses a other
			// representation
			int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
			int codedPosMirrored = fieldToHuffman(getMirroredField(PLAYER2),
					getMirroredField(PLAYER1), true);
			int value = books.getOpeningBookDeep().getValue(codedPos,
					codedPosMirrored);
			if (value == 0) {
				return 0;
			} else if (value == 1) {
				return -988;
			} else {
				return 988;
			}
		}

		// Get index in Transposition-Table
		// 32-Bit-Operation
		int index = ((int) zobr & (lTransPosSize - 1));

		// int transPosition = (unsigned long)zobr % lTransPosSize;
		// should be equal to above operation (time)

		// Check for Entry in Transposition-Table
		if (lKey[index] == zobr) {
			short v = lValue[index];
			switch (lFlag[index]) {
			case TRANSPOSEXACT:
				return v;
			case TRANSPOSLOWER:
				if (v >= beta)
					return v;
				if (v > alpha)
					alpha = v;
				break;
			case TRANSPOSUPPER:
				if (v <= alpha)
					return v;
				if (v < beta)
					beta = v;
				break;
			}
		}
		boolean isExactValue = false;

		int x, y = 0, later = -1, z = -1, q = -1;
		long t;
		short value = 0;

		int[] drlt = new int[1];
		int anz;
		int moves[];

		// Mirror Board
		long f1 = getMirroredField(PLAYER1);
		long f2 = getMirroredField(PLAYER2);

//		 if (tds != null && countPieces() <= 10)
//			 moves = tds.getBestMoveList(fieldP1, fieldP2);
//		 else
		// dynamic move-ordering
		moves = generateMoves(PLAYER1, true);

		// Check, if symmetry is still possible. If current board is symmetric,
		// only one half of the board must be evaluated
		if (symPos)
			if ((symPos = ((f1 & fieldP1) == 0L && (f2 & fieldP2) == 0L)))
				if ((f1 == fieldP2 && f2 == fieldP1)) {
					int tmp[] = new int[8];
					for (x = 0; moves[x] != -1; x++) {
						if (moves[x] < 4)
							tmp[y++] = moves[x];
					}
					for (x = 0; x < y; x++)
						moves[x] = tmp[x];
					moves[x] = -1;
				}

		// Try to find mirrored board in Transposition-Table
		if (depth < 16 /* && symPos */) { // scheint ohne das
											// Auskommentiere schneller zu sein
			long nZobr = toZobrist(f1, f2);
			int transPositionN = ((int) nZobr & (lTransPosSize - 1));
			if (lKey[transPositionN] == nZobr) {
				short v = lValue[transPositionN];
				switch (lFlag[transPositionN]) {
				case TRANSPOSEXACT:
					return v;
				case TRANSPOSLOWER:
					if (v >= beta)
						return v;
					if (v > alpha)
						alpha = v;
					break;
				case TRANSPOSUPPER:
					if (v <= alpha)
						return v;
					if (v < beta)
						beta = v;
					break;
				}
			}
		}

		// Some special tests, to get the GTV directly. Doesn't work all the
		// time
		if (depth < 14 && !hasWin(PLAYER2)) {
			for (x = 0; moves[x] > -1; x++) {
				t = fieldP1;
				putPiece(PLAYER1, moves[x]);

				if (canWin(PLAYER2, moves[x], colHeight[moves[x]])) {
					fieldP1 = t;
					colHeight[moves[x]]--;
					continue;
				}
				anz = hasWin(PLAYER1, drlt);
				// Check if current player has two direct threats -> immediate
				// Win
				if (anz > 1) {
					fieldP1 = t;
					colHeight[moves[x]]--;

					lKey[index] = zobr;
					lFlag[index] = TRANSPOSEXACT;
					lValue[index] = (short) (1020 - distance);
					return 1020 - distance;
				} else if (anz != 0) {
					// Check if current player has two threats on top of
					// each-other -> direct Win
					if (colHeight[drlt[0]] < 5
							&& canWin(PLAYER1, drlt[0], colHeight[drlt[0]] + 1)) {
						fieldP1 = t;
						colHeight[moves[x]]--;

						lKey[index] = zobr;
						lFlag[index] = TRANSPOSEXACT;
						lValue[index] = (short) (1020 - distance);
						return 1020 - distance;
					}
					colHeight[drlt[0]]++;
					// Check, if a threat can be forced
					if (q == -1 && findOddThreatP1(0) != -1)
						q = moves[x];
					colHeight[drlt[0]]--;
				}
				fieldP1 = t;
				colHeight[moves[x]]--;
			}
		} else {
			// Search for threats, which are on top of each other
			for (x = 0; moves[x] != -1; x++) {
				if (colHeight[moves[x]] < 4) {
					colHeight[moves[x]]++;
					if (canWin(PLAYER1, moves[x], colHeight[moves[x]]))
						if (canWin(PLAYER1, moves[x], colHeight[moves[x]] + 1)
								&& !hasWin(PLAYER2)) { // direct win ->
							colHeight[moves[x]]--;
							return 1020 - distance;
						}
					colHeight[moves[x]]--;
				}
			}
		}

		// Check, if a Odd Threat can be created (only odd threats are from
		// interest for the beginning player)
		y = findOddThreatP1(0);
		if (y > -1) {
			// Look for another odd threat
			z = findOddThreatP1(y);
			if (z != -1 && colHeight[z] < colHeight[y]) {
				x = z;
				z = y;
				y = x;
			}
			t = fieldP1;
			fieldP1 |= fieldMask[y][colHeight[y]++];
			value = (short) alphaBetaStartP2(depth + 1, alpha, beta, zobr
					^ rnd[0][y * 6 - 1 + colHeight[y]], symPos);

			fieldP1 = t;
			colHeight[y]--;
			if (value >= beta) {
				lKey[index] = zobr;
				lFlag[index] = TRANSPOSLOWER;
				lValue[index] = value;
				return value;
			} else if (value > alpha) {
				alpha = value;
				isExactValue = true;
			}
			if (value >= 1000) {
				lKey[index] = zobr;
				lFlag[index] = (isExactValue ? TRANSPOSEXACT : TRANSPOSUPPER);
				lValue[index] = value;
				return value;
			}

			if (z > -1) {
				t = fieldP1;
				fieldP1 |= fieldMask[z][colHeight[z]++];
				value = (short) alphaBetaStartP2(depth + 1, alpha, beta, zobr
						^ rnd[0][z * 6 - 1 + colHeight[z]], symPos);

				fieldP1 = t;
				colHeight[z]--;
				if (value >= beta) {
					lKey[index] = zobr;
					lFlag[index] = TRANSPOSLOWER;
					lValue[index] = value;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isExactValue = true;
				}
				if (value >= 1000) {
					lKey[index] = zobr;
					lFlag[index] = (isExactValue ? TRANSPOSEXACT
							: TRANSPOSUPPER);
					lValue[index] = value;
					return value;
				}
			}
		}

		// Try to force a odd threat
		if (q != -1) {
			if (colHeight[q] < 4 && colHeight[q] % 2 != 0 && later == -1
					&& canWin(PLAYER1, q, colHeight[q] + 1))
				later = q;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask[q][colHeight[q]++];
				value = (short) alphaBetaStartP2(depth + 1, alpha, beta, zobr
						^ rnd[0][q * 6 - 1 + colHeight[q]], symPos);

				fieldP1 = t;
				colHeight[q]--;
				if (value >= beta) {
					lKey[index] = zobr;
					lFlag[index] = TRANSPOSLOWER;
					lValue[index] = value;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isExactValue = true;
				}
				if (value >= 1000) {
					lKey[index] = zobr;
					lFlag[index] = (isExactValue ? TRANSPOSEXACT
							: TRANSPOSUPPER);
					lValue[index] = value;
					return value;
				}
			}
		}

		// Try all legal Moves
		for (x = 0; moves[x] > -1; x++) {
			if (y == moves[x] || z == moves[x] || q == moves[x])
				continue;

			// Don't put a piece underneath a own threat -> threat would become
			// worthless
			if (colHeight[moves[x]] < 4 && colHeight[moves[x]] % 2 != 0
					&& later == -1
					&& canWin(PLAYER1, moves[x], colHeight[moves[x]] + 1))
				later = moves[x];
			else {
				t = fieldP1;
				fieldP1 |= fieldMask[moves[x]][colHeight[moves[x]]++];
				value = (short) alphaBetaStartP2(depth + 1, alpha, beta, zobr
						^ rnd[0][moves[x] * 6 - 1 + colHeight[moves[x]]],
						symPos);

				fieldP1 = t;
				colHeight[moves[x]]--;
				if (value >= beta) {
					lKey[index] = zobr;
					lFlag[index] = TRANSPOSLOWER;
					lValue[index] = value;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isExactValue = true;
				}
				if (value >= 1000) {
					lKey[index] = zobr;
					lFlag[index] = (isExactValue ? TRANSPOSEXACT
							: TRANSPOSUPPER);
					lValue[index] = value;
					return value;
				}
			}
		}

		// Put a piece underneath a own threat
		if (later > -1) {
			t = fieldP1;
			fieldP1 |= fieldMask[later][colHeight[later]++];
			value = (short) alphaBetaStartP2(depth + 1, alpha, beta, zobr
					^ rnd[0][later * 6 - 1 + colHeight[later]], symPos);

			fieldP1 = t;
			colHeight[later]--;
			if (value >= beta) {
				lKey[index] = zobr;
				lFlag[index] = TRANSPOSLOWER;
				lValue[index] = value;
				return value;
			} else if (value > alpha) {
				alpha = value;
				isExactValue = true;
			}
		}
		if (isExactValue)
			lFlag[index] = TRANSPOSEXACT;
		else
			// No move could be found, that was better than alpha
			lFlag[index] = TRANSPOSUPPER;
		lKey[index] = zobr;
		lValue[index] = (short) alpha;
		return alpha;
	}

	/**
	 * For comments, look at alphaBetaStartP1
	 * 
	 * @param depth
	 *            current searchDepth
	 * @param alpha
	 * @param beta
	 * @param zobr
	 *            current Zobrist-Key
	 * @param symPos
	 *            is a symmetry possible on the current board
	 * @return Value
	 */
	private int alphaBetaStartP2(int depth, int alpha, int beta, long zobr,
			boolean symPos) {
		int distance = depth;// (seekFarLoose ? countPieces() / looseIntervall :
								// 0);
		if (hasWin(PLAYER2))
			return -1020 + distance;
		int moves[];

		int x;
		long t;
		int transPosition;

		//if (tds != null && countPieces() <= 10)
		//	moves = tds.getBestMoveList(fieldP1, fieldP2);
		//else
			// dynamische Zugsortierung
			moves = generateMoves(PLAYER2, true);

		// Enhanced Transposition Cutoff
		for (x = 0; moves[x] != (-1); x++) {
			t = zobr ^ rnd[1][moves[x] * 6 + colHeight[moves[x]]];
			long k;
			short v;
			byte f;

			// Hash-Table ist in zwei Stufen unterteilt, daher die
			// Unterscheidung
			if (depth > 13) {
				transPosition = ((int) t & (transPosSize - 1));
				k = key[transPosition];
				v = value[transPosition];
				f = flag[transPosition];
			} else {
				transPosition = ((int) t & (lTransPosSize - 1));
				k = lKey[transPosition];
				v = lValue[transPosition];
				f = lFlag[transPosition];
			}
			if (k == t && f != TRANSPOSLOWER && v <= alpha)
				return v;
		}

		int y = 0, later = -1, z = -1, q = -1, anz;
		int[] drlt = new int[1];
		int value = 0;

		if (symPos) // War beim letzten Zug eine symmetrische Stellung möglich
			if ((symPos = symPossible())) // ist immer noch eine möglich
				if (isSymmetric()) { // liegt eine echte Symmetrie vor
					// Nur eine Seite des Spielfeldes muss ausprobiert werden
					int tmp[] = new int[8];
					for (x = 0; moves[x] != -1; x++) {
						if (moves[x] < 4)
							tmp[y++] = moves[x];
					}
					for (x = 0; x < y; x++)
						moves[x] = tmp[x];
					moves[x] = -1;
				}

		// Wenn Spieler 1 eine akute Drohung hat, dann lohnt es sich nicht,
		// diese Stellung genauuer zu untersuchen
		if (depth < 12 && !hasWin(PLAYER1)) {
			for (x = 0; moves[x] > -1; x++) {
				t = fieldP2;
				fieldP2 |= fieldMask[moves[x]][colHeight[moves[x]]++];
				// Wenn Spieler2 einen Zug macht und Spieler1 daraufhin gewinnt,
				// dann abbrechen
				if (canWin(PLAYER1, moves[x], colHeight[moves[x]])) {
					fieldP2 = t;
					colHeight[moves[x]]--;
					continue;
				}
				anz = hasWin(PLAYER2, drlt);
				if (anz > 1) {
					fieldP2 = t;
					colHeight[moves[x]]--;
					return -1020 + distance;
				} else if (anz != 0) {
					// Wenn Spieler2 aber doch noch eine Drohung hatte
					if (colHeight[drlt[0]] < 5
							&& canWin(PLAYER2, drlt[0], colHeight[drlt[0]] + 1)) {
						fieldP2 = t;
						colHeight[moves[x]]--;
						return -1020 + distance;
					}
					colHeight[drlt[0]]++;
					// Kann eine Drohung erzwungen werden
					if (q == -1 && findThreat(PLAYER2, 0) != -1)
						q = moves[x];
					colHeight[drlt[0]]--;
				}
				fieldP2 = t;
				colHeight[moves[x]]--;
			}
		} else {
			// Nach Doppeldrohungen suchen, die direkt übereinander sind
			for (x = 0; moves[x] != -1; x++) {
				if (colHeight[moves[x]] < 4) {
					colHeight[moves[x]]++;
					if (canWin(PLAYER2, moves[x], colHeight[moves[x]]))
						if (canWin(PLAYER2, moves[x], colHeight[moves[x]] + 1)
								&& !hasWin(PLAYER1)) {
							colHeight[moves[x]]--;
							return -1020 + distance;
						}
					colHeight[moves[x]]--;
				}
			}
		}

		y = findThreat(PLAYER2, 0);
		if (y > -1) {
			t = fieldP2;
			fieldP2 |= fieldMask[y][colHeight[y]++];
			if (depth > 13)
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd[1][y * 6 - 1 + colHeight[y]]);
			else
				value = alphaBetaStartP1(depth + 1, alpha, beta, zobr
						^ rnd[1][y * 6 - 1 + colHeight[y]], symPos);

			fieldP2 = t;
			colHeight[y]--;
			if (value <= alpha)
				return value;
			else if (value <= -1000) // Bei Sieg (eindeutig) abbrechen
				return value;
			else if (value < beta)
				beta = value;
			z = findThreat(PLAYER2, y);
			if (z > -1) {
				t = fieldP2;
				fieldP2 |= fieldMask[z][colHeight[z]++];
				if (depth > 13)
					value = alphaBetaP1(depth + 1, alpha, beta, zobr
							^ rnd[1][z * 6 - 1 + colHeight[z]]);
				else
					value = alphaBetaStartP1(depth + 1, alpha, beta, zobr
							^ rnd[1][z * 6 - 1 + colHeight[z]], symPos);

				fieldP2 = t;
				colHeight[z]--;
				if (value <= alpha)
					return value;
				else if (value <= -1000)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (q != -1) {
			if (colHeight[q] < 5 && colHeight[q] % 2 == 0 && later == -1
					&& canWin(PLAYER2, q, colHeight[q] + 1))
				later = q;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask[q][colHeight[q]++];
				value = alphaBetaStartP1(depth + 1, alpha, beta, zobr
						^ rnd[1][q * 6 - 1 + colHeight[q]], symPos);
				fieldP2 = t;
				colHeight[q]--;
				if (value <= alpha)
					return value;
				else if (value <= -1000)
					return value;
				else if (value < beta)
					beta = value;
			}
		}

		for (x = 0; moves[x] > -1; x++) {
			if (y == moves[x] || z == moves[x] || q == moves[x])
				continue;
			if (colHeight[moves[x]] < 5 && colHeight[moves[x]] % 2 == 0
					&& later == -1
					&& canWin(PLAYER2, moves[x], colHeight[moves[x]] + 1))
				later = moves[x];
			else {
				t = fieldP2;
				fieldP2 |= fieldMask[moves[x]][colHeight[moves[x]]++];
				if (depth > 13)
					value = alphaBetaP1(depth + 1, alpha, beta, zobr
							^ rnd[1][moves[x] * 6 - 1 + colHeight[moves[x]]]);
				else
					value = alphaBetaStartP1(depth + 1, alpha, beta, zobr
							^ rnd[1][moves[x] * 6 - 1 + colHeight[moves[x]]],
							symPos);

				fieldP2 = t;
				colHeight[moves[x]]--;
				if (value <= alpha)
					return value;
				else if (value <= -1000)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (later > -1) {
			t = fieldP2;
			fieldP2 |= fieldMask[later][colHeight[later]++];
			if (depth > 13)
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd[1][later * 6 - 1 + colHeight[later]]);
			else
				value = alphaBetaStartP1(depth + 1, alpha, beta, zobr
						^ rnd[1][later * 6 - 1 + colHeight[later]], symPos);

			fieldP2 = t;
			colHeight[later]--;
			if (value <= alpha)
				return value;
			else if (value <= -1000)
				return value;
			else if (value < beta)
				beta = value;
		}
		return beta;
	}

	/**
	 * Faster Alpha-Beta-Search for more deep depths.
	 * 
	 * @param depth
	 *            current search-depth
	 * @param alpha
	 * @param beta
	 * @param zobr
	 *            current Zobrist-Key
	 * @return Value
	 */
	private int alphaBetaP1(int depth, int alpha, int beta, long zobr) {

		// Check for direct Win
		if (hasWin(PLAYER1)) {
			int distance = (seekFarLoose ? countPieces() / looseIntervall : 0);
			return 1000 - distance;
		}

		// If only 2 free fields are left on the board, a exact value can be
		// determined
		if (depth == movesTillFull - 2) {
			long f = (~(fieldP2 | fieldP1)) & TOPROW;
			// Wenn beide leeren Felder in oberster Reihe sind
			if ((f & (f - 1L)) != 0L) {
				int v[] = new int[2], i = 0;
				if (colHeight[0] != 6)
					v[i++] = 0;
				if (colHeight[6] != 6)
					v[i++] = 6;
				if (colHeight[1] != 6)
					v[i++] = 1;
				if (colHeight[5] != 6)
					v[i++] = 5;
				if (colHeight[2] != 6)
					v[i++] = 2;
				if (colHeight[4] != 6)
					v[i++] = 4;
				if (colHeight[3] != 6)
					v[i] = 3;
				if (canWin(PLAYER2, v[0], colHeight[v[0]])
						&& canWin(PLAYER2, v[1], colHeight[v[1]]))
					return -1000;
				return 0;

			} else {
				short v = -1;
				if (colHeight[0] != 6)
					v = 0;
				else if (colHeight[6] != 6)
					v = 6;
				else if (colHeight[1] != 6)
					v = 1;
				else if (colHeight[5] != 6)
					v = 5;
				else if (colHeight[2] != 6)
					v = 2;
				else if (colHeight[4] != 6)
					v = 4;
				else if (colHeight[3] != 6)
					v = 3;
				if (canWin(PLAYER2, v, colHeight[v] + 1))
					return -1000;
				return 0;
			}
		}

		// Check for a draw
		if (depth == movesTillFull) {
			return 0;
		}

		// Evaluate, if search-horizon is reached
		if (depth >= searchDepth) {
			// TODO: Maybe this part should be put beneath the
			// transposition-retrieval, so that the value of the evaluation
			// can be stored in the transposition-table
			int eval = evaluate(PLAYER1, zobr);
			if (eval != -1)
				return eval;
			// if evaluate doesn't get a proper value, then
			// search a little deeper
		}

		// Index of Transposition-Table
		int index = ((int) zobr & (transPosSize - 1));

		// Check, if current board is in Transposition-Table
		if (key[index] == zobr) {
			short v = value[index];
			switch (flag[index]) {
			case TRANSPOSEXACT:
				return v;
			case TRANSPOSLOWER:
				if (v >= beta)
					return v;
				if (v > alpha)
					alpha = v;
				break;
			case TRANSPOSUPPER:
				if (v <= alpha)
					return v;
				if (v < beta)
					beta = v;
				break;
			}
		}

		Boolean isValueExact = false;
		int later = -1, x = -1, value;
		long t;

		// Look for odd Threats
		if (depth < 24) {
			x = findOddThreatP1(0);
			if (x > -1) {
				t = fieldP1;
				fieldP1 |= fieldMask[x][colHeight[x]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd[0][x * 6 - 1 + colHeight[x]]);

				fieldP1 = t;
				colHeight[x]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}

		// Try Moves in all the columns

		if (x != 3 && colHeight[3] < 6) {
			if (colHeight[3] < 4 && colHeight[3] % 2 != 0
					&& canWin(PLAYER1, 3, colHeight[3] + 1))
				later = 3;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask3[colHeight[3]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd13[colHeight[3]]);

				fieldP1 = t;
				colHeight[3]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}
		if (x != 4 && colHeight[4] < 6) {
			if (colHeight[4] < 4 && later == -1 && colHeight[4] % 2 != 0
					&& canWin(PLAYER1, 4, colHeight[4] + 1))
				later = 4;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask4[colHeight[4]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd14[colHeight[4]]);

				fieldP1 = t;
				colHeight[4]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}
		if (x != 2 && colHeight[2] < 6) {
			if (later == -1 && colHeight[2] < 4 && colHeight[2] % 2 != 0
					&& canWin(PLAYER1, 2, colHeight[2] + 1))
				later = 2;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask2[colHeight[2]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd12[colHeight[2]]);

				fieldP1 = t;
				colHeight[2]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}
		if (x != 5 && colHeight[5] < 6) {
			if (later == -1 && colHeight[5] < 4 && colHeight[5] % 2 != 0
					&& canWin(PLAYER1, 5, colHeight[5] + 1))
				later = 5;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask5[colHeight[5]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd15[colHeight[5]]);

				fieldP1 = t;
				colHeight[5]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}
		if (x != 1 && colHeight[1] < 6) {
			if (later == -1 && colHeight[1] < 4 && colHeight[1] % 2 != 0
					&& canWin(PLAYER1, 1, colHeight[1] + 1))
				later = 1;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask1[colHeight[1]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd11[colHeight[1]]);

				fieldP1 = t;
				colHeight[1]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}
		if (x != 6 && colHeight[6] < 6) {
			if (later == -1 && colHeight[6] < 4 && colHeight[6] % 2 != 0
					&& canWin(PLAYER1, 6, colHeight[6] + 1))
				later = 6;
			else {
				t = fieldP1;
				fieldP1 |= fieldMask6[colHeight[6]++];
				value = alphaBetaP2(depth + 1, alpha, beta, zobr
						^ rnd16[colHeight[6]]);

				fieldP1 = t;
				colHeight[6]--;
				if (value >= beta) {
					key[index] = zobr;
					this.value[index] = (short) value;
					flag[index] = TRANSPOSLOWER;
					return value;
				} else if (value > alpha) {
					alpha = value;
					isValueExact = true;
				}
			}
		}
		if (x != 0 && colHeight[0] < 6) {
			t = fieldP1;
			fieldP1 |= fieldMask0[colHeight[0]++];
			value = alphaBetaP2(depth + 1, alpha, beta, zobr
					^ rnd10[colHeight[0]]);

			fieldP1 = t;
			colHeight[0]--;
			if (value >= beta) {
				key[index] = zobr;
				this.value[index] = (short) value;
				flag[index] = TRANSPOSLOWER;
				return value;
			} else if (value > alpha) {
				alpha = value;
				isValueExact = true;
			}
		}

		// Put piece underneath own threat
		if (later != (-1)) {
			t = fieldP1;
			fieldP1 |= fieldMask[later][colHeight[later]++];
			value = alphaBetaP2(depth + 1, alpha, beta, zobr
					^ rnd[0][later * 6 - 1 + colHeight[later]]);

			fieldP1 = t;
			colHeight[later]--;
			if (value >= beta) {
				key[index] = zobr;
				this.value[index] = (short) value;
				flag[index] = TRANSPOSLOWER;
				return value;
			} else if (value > alpha) {
				alpha = value;
				isValueExact = true;
			}
		}

		key[index] = zobr;
		this.value[index] = (short) alpha;
		flag[index] = (isValueExact ? TRANSPOSEXACT : TRANSPOSUPPER);
		return alpha;
	}

	/**
	 * For Comments, check the almost equal method above (alphaBetaP1)
	 * 
	 * @param depth
	 *            current search-depth
	 * @param alpha
	 * @param beta
	 * @param zobr
	 *            current Zobrist-Key
	 * @return Value
	 */
	private int alphaBetaP2(int depth, int alpha, int beta, long zobr) {
		if (hasWin(PLAYER2)) {
			int pieceCount = (seekFarLoose ? countPieces() / looseIntervall : 0);
			return -1000 + pieceCount;
		}

		int later = -1, x, value;
		long t;

		if (depth < 20)
			for (x = 0; x < 7; x++) {
				if (colHeight[x] != 6) {
					t = zobr ^ rnd[1][x * 6 + colHeight[x]];
					int transPosition = ((int) t & (transPosSize - 1));
					long k = key[transPosition];
					short v = this.value[transPosition];
					byte f = flag[transPosition];

					if (k == t && f != TRANSPOSLOWER && v <= alpha)
						return v;
				}
			}

		x = -1;
		if (depth < 20) {
			x = findThreat(PLAYER2, 0);
			if (x > -1) {
				t = fieldP2;
				fieldP2 |= fieldMask[x][colHeight[x]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd[1][x * 6 - 1 + colHeight[x]]);

				fieldP2 = t;
				colHeight[x]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}

		if (x != 3 && colHeight[3] < 6) {
			if (colHeight[3] < 5 && canWin(PLAYER2, 3, colHeight[3] + 1))
				later = 3;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask3[colHeight[3]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd23[colHeight[3]]);

				fieldP2 = t;
				colHeight[3]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (x != 4 && colHeight[4] < 6) {
			if (later == -1 && colHeight[4] < 5
					&& canWin(PLAYER2, 4, colHeight[4] + 1))
				later = 4;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask4[colHeight[4]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd24[colHeight[4]]);

				fieldP2 = t;
				colHeight[4]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (x != 2 && colHeight[2] < 6) {
			if (later == -1 && colHeight[2] < 5
					&& canWin(PLAYER2, 2, colHeight[2] + 1))
				later = 2;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask2[colHeight[2]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd22[colHeight[2]]);

				fieldP2 = t;
				colHeight[2]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (x != 5 && colHeight[5] < 6) {
			if (later == -1 && colHeight[5] < 5
					&& canWin(PLAYER2, 5, colHeight[5] + 1))
				later = 5;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask5[colHeight[5]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd25[colHeight[5]]);

				fieldP2 = t;
				colHeight[5]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (x != 1 && colHeight[1] < 6) {
			if (later == -1 && colHeight[1] < 5
					&& canWin(PLAYER2, 1, colHeight[1] + 1))
				later = 1;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask1[colHeight[1]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd21[colHeight[1]]);

				fieldP2 = t;
				colHeight[1]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (x != 6 && colHeight[6] < 6) {
			if (later == -1 && colHeight[6] < 5
					&& canWin(PLAYER2, 6, colHeight[6] + 1))
				later = 6;
			else {
				t = fieldP2;
				fieldP2 |= fieldMask6[colHeight[6]++];
				value = alphaBetaP1(depth + 1, alpha, beta, zobr
						^ rnd26[colHeight[6]]);

				fieldP2 = t;
				colHeight[6]--;
				if (value <= alpha)
					return value;
				else if (value < beta)
					beta = value;
			}
		}
		if (x != 0 && colHeight[0] < 6) {
			t = fieldP2;
			fieldP2 |= fieldMask0[colHeight[0]++];
			value = alphaBetaP1(depth + 1, alpha, beta, zobr
					^ rnd20[colHeight[0]]);

			fieldP2 = t;
			colHeight[0]--;
			if (value <= alpha)
				return value;
			else if (value < beta)
				beta = value;
		}
		if (later != (-1)) {
			t = fieldP2;
			fieldP2 |= fieldMask[later][colHeight[later]++];
			value = alphaBetaP1(depth + 1, alpha, beta, zobr
					^ rnd[1][later * 6 - 1 + colHeight[later]]);

			fieldP2 = t;
			colHeight[later]--;
			if (value <= alpha)
				return value;
			else if (value < beta)
				beta = value;
		}
		return beta;
	}

	/**
	 * This method currently only works for boards with not more than 2 threats
	 * (&gt; 70% of all boards)
	 * 
	 * @return estimated Value for the current board
	 */
	public int evaluate(int curPlayer, long zobr) {
		int numThreats = 0, threatsCol = 0, y;
		long threatsP1 = 0L, threatsP2 = 0L;

		// Ermittlung aller Drohungen beider Spieler!
		// Außerdem Spalten markieren, in denen Drohungen
		// vorhanden sind!
		for (byte i = 0; i < COLCOUNT; i++) {
			y = colHeight[i];
			while (y < 6) {
				if (canWin(PLAYER1, i, y)) {
					threatsP1 |= fieldMask[i][y];
					numThreats++;
					// Markiert Spalte in der Drohung ist
					threatsCol |= (1 << (6 - i));
					break;
				}
				if (canWin(PLAYER2, i, y)) {
					threatsP2 |= fieldMask[i][y];
					numThreats++;
					// Markiert Spalte in der Drohung ist
					threatsCol |= (1 << (6 - i));
				}
				++y;

				// Mehr Drohungen werden sowieso nicht untersucht
				if (numThreats > 2)
					return -1;
			}
		}

		// Prüfen, wie viele Drohungen vorhanden sind
		// und bei 0,1 oder 2 Drohungen Bewertung der
		// Stellung ermitteln
		switch (numThreats) {
		case 0: // Wenn keiner eine Drohung hat
			return 0;
		case 1: // Wenn einer der beiden Spieler eine Drohung hat
			if (threatsP2 != 0) {// Wenn Spieler2 die Drohung hat
				// Wenn Spieler2 eine gerade Drohung hat
				if ((threatsP2 & EVENROWS) != 0L)
					return -500;
				return 0;
			}
			// Wenn Spieler1 eine ungerade Drohung hat
			if ((threatsP1 & ODDROWS) != 0L)
				return 500;
			return 0;
		case 2: // Wenn insgesamt 2 Drohungen vorhanden sind
			if (threatsP2 == 0L) { // Wenn Spieler2 keine Drohung hat
				if ((threatsP1 & ODDROWS) != 0L)
					return 500;
				return 0;
			}
			if (threatsP1 == 0L) { // Wenn Spieler1 keine Drohung hat
				// Wenn Spieler2 min. eine gerade Drohung hat
				if ((threatsP2 & EVENROWS) != 0L)
					return -500;
				// Befinden sich die Drohungen von Spieler2 in unterschiedlichen
				// spalten
				if ((threatsCol & (threatsCol - 1)) != 0L)
					// Wenn Spieler2 2 ungerade Drohungen hat
					if ((threatsP2 & ODDROWS) == threatsP2)
						return -500;
				break;
			}
			// Wenn beide eine Drohung haben
			// Wenn beide Drohungen in unterschiedlichen Spalten sind
			if ((threatsCol & (threatsCol - 1)) != 0L) {
				if ((threatsP1 & ODDROWS) != 0L)
					if ((threatsP2 & ODDROWS) != 0L)
						return 0; // Beide haben eine ungerade Drohung
					else
						// Spieler1 hat eine ungerade und Spieler2 eine gerade
						// Drohung
						return 500;
				if ((threatsP1 & EVENROWS) != 0L)
					return -500; // Spieler1 hat nur eine gerade Drohung
			}
			// Wenn beide Drohungen im gleichen Feld sind...
			if ((threatsP1 & threatsP2) != 0L)
				if ((threatsP1 & EVENROWS) != 0L)
					// Spieler1 und Spieler2 haben eine gerade Drohung
					return -500;
				else
					// Spieler1 und Spieler2 haben eine ungerade Drohung
					return 500;
			break;
		}
		return -1;
	}

	/**
	 * Find the best move for the current Player. If there are equivalent moves,
	 * select one randomly. If this board is a loss, make a completely random move.
	 * This method always resets the variable randomizeLosses.
	 * 
	 * @param table
	 * @return best move (random choice)
	 */
	private int getBestMoveRandLoss(int[][] table) {
		double[] vals = getNextVTable(table, false);
		int sign = (countPieces() % 2 == 0 ? 1 : -1);
		double bestVal = Double.NEGATIVE_INFINITY;
		int bestMove = -1;
		int countEqual = 1;
		for (int i = 0; i < COLCOUNT; i++) {
			if (vals[i] != Double.NaN) {
				double curVal = vals[i] * sign;
				if (curVal < 0 && curVal > -1000)
					curVal = -10;
				if (curVal > bestVal) {
					bestMove = i;
					bestVal = curVal;
					countEqual = 1;
				} else if (curVal == bestVal) {
					countEqual++;
					if (rand.nextDouble() < 1.0 / countEqual)
						bestMove = i;
				}
			}
		}

		if (bestVal <= -1000 /* && countPieces() <= 10 */) {
			boolean oldVal = randomizeEqualMoves;
			randomizeLosses = false;
			randomizeEqualMoves = false;
			int x = getBestMove(table);
			randomizeEqualMoves = oldVal;
			randomizeLosses = true;
			return x;

		} else if (bestVal < 0 && countPieces() > 10) {
			// Search most distant loss after opening-phase
			boolean oldVal = randomizeEqualMoves;
			randomizeLosses = false;
			randomizeEqualMoves = false;
			int x = getBestMove(table);
			randomizeEqualMoves = oldVal;
			randomizeLosses = true;
			return x;
		}
		return bestMove;
	}
	
	/**
	 * Find the best move for the current Player. If there are equivalent moves,
	 * select one randomly
	 * 
	 * @param table
	 * @return best move (random choice)
	 */
	private int getBestMoveRand(int[][] table) {
		double[] vals = getNextVTable(table, false);
		int sign = (countPieces() % 2 == 0 ? 1 : -1);
		double bestVal = Double.NEGATIVE_INFINITY;
		int bestMove = -1;
		int countEqual = 1;
		for (int i = 0; i < COLCOUNT; i++) {
			if (vals[i] != Double.NaN) {
				double curVal = vals[i] * sign;
				if (curVal > bestVal) {
					bestMove = i;
					bestVal = curVal;
					countEqual = 1;
				} else if (curVal == bestVal) {
					countEqual++;
					if (rand.nextDouble() < 1.0 / countEqual)
						bestMove = i;
				}
			}
		}
		if (bestVal < 0) {
			randomizeEqualMoves = false;
			int x = getBestMove(table);
			randomizeEqualMoves = true;
			return x;

		}
		return bestMove;
	}

	/**
	 * Set Size of the Transposition Table
	 * 
	 * @param index
	 *            index in the Table of possible Values
	 */
	public void setTransPosSize(int index) {
		// index in the array of possible values
		transPosSize = TRANSPOSSIZE[index];
		lTransPosSize = transPosSize / 8;

		// Transposition Table for higher Search-Depths
		key = new long[transPosSize];
		value = new short[transPosSize];
		flag = new byte[transPosSize];

		// Transposition Table for lower Search-Depths
		lKey = new long[lTransPosSize];
		lValue = new short[lTransPosSize];
		lFlag = new byte[lTransPosSize];

		System.gc();
	}

	/**
	 * @param useNormalBook
	 *            true, if normal book shall be used
	 * @param useDeepBook
	 *            true, if deep book shall be used
	 * @param useDeepBookDist
	 *            true, if normal deep book with exact distances shall be used
	 */
	public void setBooks(boolean useNormalBook, boolean useDeepBook,
			boolean useDeepBookDist) {
		useBook = useNormalBook;
		this.useDeepBook = useDeepBook;
		this.useDeepBookDist = useDeepBookDist;
	}

	/**
	 * Choose, if random move is made when more than one move has the same value
	 * 
	 * @param randomize
	 */
	public void randomizeEqualMoves(boolean randomize) {
		randomizeEqualMoves = randomize;
	}

	/**
	 * Find the best move for the current Player in method getBestMove(). If
	 * there are equal moves, select randomly. If a board is a loss, make a
	 * almost completly random move.
	 */
	public void randomizeLosses(boolean value) {
		randomizeLosses = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see c4.PlayAgent#getBestMove(int[][])
	 */
	public int getBestMove(int[][] table) {
		if (randomizeLosses)
			return getBestMoveRandLoss(table);
		if (randomizeEqualMoves)
			return getBestMoveRand(table);
		semOpDown();
		setBoard(table);

		int val = rootNode(false);
		semOpUp();
		return val;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see c4.PlayAgent#getScore(int[][], boolean)
	 */
	public double getScore(int[][] table, boolean useSigmoid) {
		semOpDown();
		setBoard(table);
		double score = 0.1;

		if (useBook && countPieces() == 8) {
			int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
			int codedPosMirrored = fieldToHuffman(getMirroredField(PLAYER2),
					getMirroredField(PLAYER1), true);
			int value = books.getOpeningBook().getValue(codedPos,
					codedPosMirrored);
			if (value == 0) {
				score = 0;
			} else if (value == 1) {
				score = -992;
			} else {
				score = 992;
			}
		}

		// Deep Book with Distance
		else if (useDeepBookDist && countPieces() == 12 && books != null) {
			// The params must be swapped, because the database uses a other
			// representation
			int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
			int codedPosMirrored = fieldToHuffman(getMirroredField(PLAYER2),
					getMirroredField(PLAYER1), true);
			score = books.getOpeningBookDeepDist().getValue(codedPos,
					codedPosMirrored);
		}

		// Deep Book
		else if (useDeepBook && countPieces() == 12 && books != null) {
			// The params must be swapped, because the database uses a other
			// representation
			int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
			int codedPosMirrored = fieldToHuffman(getMirroredField(PLAYER2),
					getMirroredField(PLAYER1), true);
			int value = books.getOpeningBookDeep().getValue(codedPos,
					codedPosMirrored);
			if (value == 0) {
				score = 0;
			} else if (value == 1) {
				score = -988;
			} else {
				score = 988;
			}
		} else
			score = rootNode(true);

		semOpUp();

		if (!useSigmoid)
			return score;
		return Math.tanh(score);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see c4.PlayAgent#getNextVTable(int[][], boolean)
	 */
//	@Override
	public double[] getNextVTable(int[][] table, boolean useSigmoid) {
		semOpDown();
		setBoard(table);
		double[] values = new double[7];
		int player = (countPieces() % 2 == 0 ? PLAYER1 : PLAYER2);
		int otherPlayer = (player == PLAYER1 ? PLAYER2 : PLAYER1);
		int x;
		for (x = 0; x < COLCOUNT; x++) {
			if (colHeight[x] < ROWCOUNT) {
				if (canWin(player, x, colHeight[x])) {
					values[x] = (player == PLAYER1 ? 1001 : -1001);
					if (useSigmoid)
						values[x] = Math.tanh(values[x]);
					continue;
				}
				putPiece(player, x);
				double score = 0.1;
				if (hasWin(otherPlayer))
					score = (otherPlayer == PLAYER1 ? 1001 : -1001);
				else if (useBook && countPieces() == 8) {
					int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
					int codedPosMirrored = fieldToHuffman(
							getMirroredField(PLAYER2),
							getMirroredField(PLAYER1), true);
					int value = books.getOpeningBook().getValue(codedPos,
							codedPosMirrored);
					if (value == 0) {
						score = 0;
					} else if (value == 1) {
						score = -992;
					} else {
						score = 992;
					}
				} else if ((useDeepBook) && countPieces() == 12) {
					// Die Parameter muessen vertauscht werden, da Spieler2 der
					// Anziehende in der Datenbank ist
					int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
					int codedPosMirrored = fieldToHuffman(
							getMirroredField(PLAYER2),
							getMirroredField(PLAYER1), true);
					int value = books.getOpeningBookDeep().getValue(codedPos,
							codedPosMirrored);
					if (value == 0) {
						score = 0;
					} else if (value == 1) {
						score = -988;
					} else {
						score = 988;
					}
				} else if (useDeepBookDist && countPieces() == 12) {
					// Die Parameter muessen vertauscht werden, da Spieler2 der
					// Anziehende in der Datenbank ist
					int codedPos = fieldToHuffman(fieldP2, fieldP1, false);
					int codedPosMirrored = fieldToHuffman(
							getMirroredField(PLAYER2),
							getMirroredField(PLAYER1), true);
					score = books.getOpeningBookDeepDist().getValue(codedPos,
							codedPosMirrored);
				} else
					score = rootNode(true);
				values[x] = score;
				if (useSigmoid)
					values[x] = Math.tanh(values[x]);
				removePiece(player, x);
			} else
				values[x] = Double.NaN;
		}
		semOpUp();
		return values;
	}

	/**
	 * @see #semOpUp()
	 */
	public void semOpDown() {
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see #semOpDown()
	 */
	public void semOpUp() {
		mutex.release();
	}

	@Override
	public ACTIONS_VT getNextAction2(StateObservation sob, boolean random, boolean silent) {
		int i,j,sign;
        int iBest;
		int count = 1; // counts the moves with same iMaxScore
		boolean useSigmoid=false;
		double CurrentScore;
		double iMaxScore = -Double.MAX_VALUE;
		assert (sob instanceof StateObserverC4);
		StateObserverC4 sc = (StateObserverC4) sob; 
		StateObserverC4 newsc;
		sign = (sc.countPieces() % 2 == 0 ? 1 : -1);
        Types.ACTIONS actBest = null;
        Types.ACTIONS_VT actBestVT = null;
        List<Types.ACTIONS> actions = sob.getAvailableActions();
		double[] vtable;
        vtable = new double[actions.size()+1];  
        double[] vtable7 = this.getNextVTable(sc.getBoard(), true);
        for(i = 0; i < actions.size(); ++i)
        {
//        	newsc = sc.copy();
//        	newsc.advance(actions.get(i));
//        	CurrentScore = this.getScore(newsc)*sign;
        	CurrentScore = vtable7[actions.get(i).toInt()]*sign;
        	vtable[i] = CurrentScore;
        	if (iMaxScore < CurrentScore) {
        		iMaxScore = CurrentScore;
        		actBest = actions.get(i);
        		iBest  = i; 
        		count = 1;
        	} else  {
        		if (iMaxScore == CurrentScore) count++;	        
        	}
        }
        
        if (count>1) {  // more than one action with iMaxScore: 
        	// break ties by selecting one of them randomly
        	int selectJ = (int)(rand.nextDouble()*count);
        	for (i=0, j=0; i < actions.size(); ++i) 
        	{
        		if (vtable[i]==iMaxScore) {
        			if (j==selectJ) actBest = actions.get(i);
        			j++;
        		}
        	}
        }
        
        assert actBest != null : "Oops, no best action actBest";
        actBest.setRandomSelect(false);
        // optional: print the best action
        if (!silent) {
        	newsc = sc.copy();
        	newsc.advance(actBest);
        	System.out.println("---Best Move: "+newsc.stringDescr()+"   "+iMaxScore);
        }			

        vtable[actions.size()] = iMaxScore;
      
        return new Types.ACTIONS_VT(actBest.toInt(), actBest.isRandomAction(), vtable);
	}

	@Override
	public double getScore(StateObservation so) {
		assert (so instanceof StateObserverC4);
		StateObserverC4 sc = (StateObserverC4) so; 
		this.setBoard(sc.getBoard());
		return getScore(sc.getBoard(),false);
	}

	@Override
	public ScoreTuple getScoreTuple(StateObservation so) {
        int player = so.getPlayer();
        int opponent = (player==0) ? 1 : 0;
		ScoreTuple sTuple = new ScoreTuple(2);
		sTuple.scTup[player] = getScore(so);
		sTuple.scTup[opponent] = -sTuple.scTup[player];
		return sTuple;
	}

	@Override
	public ScoreTuple estimateGameValueTuple(StateObservation so) {
		return getScoreTuple(so);
	}

	@Override
	public String stringDescr() {
		String cs = getClass().getSimpleName();
		cs = cs + ", depth:"+this.searchDepth;
		return cs;
	}

}