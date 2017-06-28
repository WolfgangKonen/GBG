package agentIO;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileFilter;

// download the following imports from 
// https://commons.apache.org/proper/commons-compress/download_compress.cgi
// if the JAR file commons-compress-1.9.jar is not on your system, then
// link the JAR file via Build Path - Java Build Path - Add JARs...
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import controllers.AgentBase;
import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTS.SingleMCTSPlayer;
import controllers.MCTS.SingleTreeNode;
import controllers.PlayAgent.AgentState;
import controllers.TD.TDAgent;
import controllers.TD.ntuple.TDNTupleAgt;
import tools.ElapsedCpuTimer;
import tools.MessageBox;
import tools.Types;
import tools.ElapsedCpuTimer.TimerType;
import tools.Types.ACTIONS;
import games.Arena;
import games.StateObservation;
import games.XArenaButtons;
import params.MCTSParams;
import params.NTParams;
import params.ParMCTS;
import params.TDParams;

public class LoadSaveTD {
	private final JFileChooserApprove fc;
	private final FileFilter tdAgentExt = new ExtensionFilter("agt.zip",
			"TD-Agents");
	private final FileFilter txtExt = new ExtensionFilter(".txt.zip",
			"Compressed Text-Files (.txt.zip)");
	private final Arena arenaGame;
	private final XArenaButtons arenaButtons;
	private final JFrame arenaFrame;

	public LoadSaveTD(Arena areGame, XArenaButtons areButtons, JFrame areFrame) {
		this.arenaGame = areGame;
		this.arenaButtons = areButtons;
		this.arenaFrame = areFrame;

		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
		fc = new JFileChooserApprove();
		fc.setCurrentDirectory(new File(strDir));
	}

	public JDialog createProgressDialog(final IGetProgress streamProgress,
			final String msg) {
		// ------------------------------------------------------------
		// Setup Progressbar Dialog
		final JDialog dlg = new JDialog(arenaFrame, msg, true);
		final JProgressBar dpb = new JProgressBar(0, 100);
		dlg.add(BorderLayout.CENTER, dpb);
		dlg.add(BorderLayout.NORTH, new JLabel("Progress..."));
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.setSize(300, 75);
		dlg.setLocationRelativeTo(arenaFrame);

		Thread t = new Thread(new Runnable() {
			public void run() {
				dlg.setVisible(true);
			}
		});
		t.start();

		arenaGame.setProgress(new tools.Progress() {
			@Override
			public String getStatusMessage() {
				String str = new String(msg + getProgress() + "%");
				return str;
			}

			@Override
			public int getProgress() {
				int i = (int) (streamProgress.getProgess().get() * 100);
				if (i > 100)
					i = 100;
				dpb.setValue(i);
				return i;
			}
		});
		return dlg; // dialog has to be closed, if not needed anymore with
					// dlg.setVisible(false)
	}

	// ==============================================================
	// Menu: Save Agent
	// ==============================================================
	public void saveTDAgent(PlayAgent pa) throws IOException {
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
		String subDir = arenaGame.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		checkAndCreateFolder(strDir);

		fc.removeChoosableFileFilter(txtExt);
		fc.setFileFilter(tdAgentExt);
		fc.setCurrentDirectory(new File(strDir));
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showSaveDialog(arenaGame);
		String filePath = "";

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = null;
			String path = fc.getSelectedFile().getPath();

			if (!path.toLowerCase().endsWith(".agt.zip"))
				path = path + ".agt.zip";

			file = new File(path);
			filePath = file.getPath();

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filePath);
			} catch (FileNotFoundException e2) {
				MessageBox.show(arenaFrame,"ERROR: Could not save TDAgent to " + filePath,
						"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				arenaGame.setStatusMessage("[ERROR: Could not save to file "
						+ filePath + " !]");
			}

			GZIPOutputStream gz = null;
			try {
				gz = new GZIPOutputStream(fos) {
					{
						def.setLevel(Deflater.BEST_COMPRESSION);
					}
				};

			} catch (IOException e1) {
				arenaGame.setStatusMessage("[ERROR: Could not create ZIP-OutputStream for"
								+ filePath + " !]");
				throw e1;
			}

			// estimate agent size
			long bytes = pa.getSize();
			
			// new
			final agentIO.IOProgress p = new agentIO.IOProgress(bytes);
			final ProgressTrackingOutputStream ptos = new ProgressTrackingOutputStream(
					gz, p);

			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(ptos);
			} catch (IOException e) {
				arenaGame.setStatusMessage("[ERROR: Could not create Object-OutputStream for"
								+ filePath + " !]");
			}

			final JDialog dlg = createProgressDialog(ptos, "Saving...");

			try {
				if (pa instanceof MCTSAgentT) {
					MCTSAgentT mcts = (MCTSAgentT) pa;
					// only for debug:
//					System.out.println("saveIter1 "+mcts.getMCTSParams().getNumIter());
//					System.out.println("saveK_U1 "+mcts.getMCTSParams().getK_UCT());
//					System.out.println("saveK_U2 "+mcts.getK());
				}
				if (pa instanceof TDNTupleAgt) {
					TDNTupleAgt tdnt = (TDNTupleAgt) pa;
					NTParams ntpar = tdnt.getNTParams();
					// only for debug:
//					System.out.println("randWalk:  "+tdnt.getNTParams().getRandWalk());
//					System.out.println("randNumTup: "+ntpar.getNtupleNumber());
				}
				oos.writeObject(pa);
			} catch (IOException e) {
				dlg.setVisible(false);
				if (e instanceof NotSerializableException) {
					MessageBox.show(arenaFrame,"ERROR: Object pa of class "+pa.getClass().getName()
							+" is not serializable",
							"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				}
				arenaGame.setStatusMessage("[ERROR: Could not write to file "
						+ filePath + " !]");
				throw new IOException("ERROR: Could not write object to file! ["+e.getClass().getName()+"]");
			}

			try {
				oos.flush();
				oos.close();
				fos.close();
			} catch (IOException e) {
				arenaGame.setStatusMessage("[ERROR: Could not complete Save-Process]");
				throw new IOException("ERROR: Could not complete Save-Process ["+e.getClass().getName()+"]");
			}

			dlg.setVisible(false);
			arenaGame.setProgress(null);
			arenaGame.setStatusMessage("Done.");

		} else
			arenaGame.setStatusMessage("[Save Agent: Aborted by User]");

		// Rescan current directory, hope it helps
		fc.rescanCurrentDirectory();
	}

	public int estimateGZIPLength(File f) {
		RandomAccessFile raf;
		int fileSize = 0;
		try {
			raf = new RandomAccessFile(f, "r");
			raf.seek(raf.length() - 4);
			byte[] bytes = new byte[4];
			raf.read(bytes);
			fileSize = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
					.getInt();
			if (fileSize < 0)
				fileSize += (1L << 32);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileSize;
	}

	public PlayAgent loadTDAgent() throws IOException, ClassNotFoundException {
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
		String subDir = arenaGame.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		checkAndCreateFolder(strDir);

		PlayAgent pa = null;
		fc.removeChoosableFileFilter(txtExt);
		fc.setFileFilter(tdAgentExt);
		fc.setCurrentDirectory(new File(strDir));
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showOpenDialog(arenaGame);
		String filePath = "";

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			ObjectInputStream ois = null;
			FileInputStream fis = null;
			File file = null;
			try {

				file = fc.getSelectedFile();
				filePath = file.getPath();
				fis = new FileInputStream(filePath);
			} catch (IOException e) {
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath
						+ " !]");
				e.printStackTrace();
			}
			
			GZIPInputStream gs = null;
			try {
				gs = new GZIPInputStream(fis);
			} catch (IOException e1) {
				arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for"
								+ filePath + " !]");
				throw e1;
			}

			long fileLength = (long) (estimateGZIPLength(file));
			final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
					gs, new agentIO.IOProgress(fileLength));
			try {
				ois = new ObjectInputStream(ptis);
			} catch (IOException e1) {
				ptis.close();
				arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for"
								+ filePath + " !]");
				throw e1;
			}

			final JDialog dlg = createProgressDialog(ptis, "Loading...");

			try {
				// ois = new ObjectInputStream(gs);
				Object obj = ois.readObject();
				if (obj instanceof TDAgent) {
					pa = (TDAgent) obj;
				} else if (obj instanceof TDNTupleAgt) {
					pa = (TDNTupleAgt) obj;
				} else if (obj instanceof MCTSAgentT) {
					pa = (MCTSAgentT) obj;
				} else if (obj instanceof MinimaxAgent) {
					pa = (MinimaxAgent) obj;
				} else if (obj instanceof RandomAgent) {
					pa = (RandomAgent) obj;
				} else {
					dlg.setVisible(false);
					MessageBox.show(arenaFrame,"ERROR: Agent class "+obj.getClass().getName()+" loaded from "
							+ filePath + " not processable", "Unknown Agent Class", JOptionPane.ERROR_MESSAGE);
					arenaGame.setStatusMessage("[ERROR: Could not load agent from "
									+ filePath + "!]");
					throw new ClassNotFoundException("ERROR: Unknown agent class");
				}
				dlg.setVisible(false);
				arenaGame.setProgress(null);
				arenaGame.setStatusMessage("Done.");
			} catch (IOException e) {
				dlg.setVisible(false);
				MessageBox.show(arenaFrame,"ERROR: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath
						+ " !]");
				//e.printStackTrace();
				//throw e;
			} catch (ClassNotFoundException e) {
				dlg.setVisible(false);
				MessageBox.show(arenaFrame,"ERROR: Class not found: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				//throw e;
			} finally {
				if (ois != null)
					try {
						ois.close();
					} catch (IOException e) {
					}
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
					}
			}
		} else
			arenaGame.setStatusMessage("[ERROR: Something went wrong while loading file "
							+ filePath + " !]");
		return pa;
	}

	public String getZipContentFiles(ZipFile zipFile,
			ZipArchiveEntry zipArchiveEntry) {
		String txtFileContent = null;
		InputStream is = null;
		try {
			is = zipFile.getInputStream(zipArchiveEntry);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ZipArchiveInputStream zis = new ZipArchiveInputStream(is);
		if (zis.canReadEntryData(zipArchiveEntry)) {
			long fileLength = (long) zipArchiveEntry.getSize();
			ProgressTrackingInputStream ptis = null;
			try {
				ptis = new ProgressTrackingInputStream(is,
						new agentIO.IOProgress(fileLength));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			final JDialog dlg = createProgressDialog(ptis, "Loading "
					+ zipArchiveEntry.getName() + "...");

			InputStreamReader isr = new InputStreamReader(ptis);
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(isr);
			String read = null;
			try {
				read = br.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (read != null) {
				sb.append(read);
				sb.append(System.lineSeparator());
				try {
					read = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			dlg.setVisible(false);
			txtFileContent = sb.toString();
			try {
				br.close();
				isr.close();
				ptis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		try {
			zis.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return txtFileContent;
	}

	/**
	 * checks if a folder exists and creates a new one if it doesn't
	 *
	 * @param filePath the folder Path
	 * @return true if a folder allready existed
	 */
	private boolean checkAndCreateFolder(String filePath) {
		File file = new File(filePath);
		boolean exists = file.exists();
		if(!file.exists()) {
			file.mkdirs();
		}
		return exists;
	}

	class MCTSAgentT_v12 extends AgentBase implements PlayAgent, Serializable 
	{ 
	    private transient ElapsedCpuTimer m_Timer;
	    
	    @Deprecated 
	    // should use this.getMCTSParams() instead 
		private MCTSParams params;
	    //private ParMCTS pmcts; 

	    /**
	     * The MCTS-UCT implementation
	     */
	    private SingleMCTSPlayer_v12 mctsPlayer;

		/**
		 * change the version ID for serialization only if a newer version is no longer 
		 * compatible with an older one (older .agt.zip will become unreadable or you have
		 * to provide a special version transformation)
		 */
		private static final long  serialVersionUID = 12L;

		/**
		 * Default constructor, needed for loading a serialized version
		 */
		public MCTSAgentT_v12() {
	    	super("MCTS");
		}

	    private void initMCTSAgent(StateObservation so, ParMCTS parMCTS) {    	
	        //Create the player.
	        mctsPlayer = new SingleMCTSPlayer_v12(new Random(),parMCTS);		
	        //mctsPlayer = new SingleMCTSPlayer(new Random(1),mcPar);	// /WK/ reproducible debugging: seed 1

	        //Set the available actions for stateObs.
	        if (so!=null) mctsPlayer.initActions(so);
	        
	        m_Timer = new ElapsedCpuTimer(TimerType.CPU_TIME);
	        m_Timer.setMaxTimeMillis(40);
	        setAgentState(AgentState.TRAINED);
	    }
	    
		@Override
		public ACTIONS getNextAction(StateObservation sob, boolean random, double[] vtable, boolean silent) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean wasRandomAction() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String stringDescr() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getScore(StateObservation sob) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	
	public class SingleMCTSPlayer_v12 implements Serializable
	{
	    /**
	     * Root of the tree.
	     */
	    public transient SingleTreeNode m_root;

	    /**
	     * Random generator.
	     */
	    public Random m_rnd;

	    public transient Types.ACTIONS[] actions; 			

	    private int NUM_ACTIONS;
	// --- this is now in ParMCTS: ---
//	    private int ROLLOUT_DEPTH = DEFAULT_ROLLOUT_DEPTH;
//	    private int TREE_DEPTH = DEFAULT_TREE_DEPTH;
//	    private int NUM_ITERS = DEFAULT_NUM_ITERS;
//	    private double K = DEFAULT_K;
//	    private int verbose = DEFAULT_VERBOSITY; 
	    int nRolloutFinished = 0;		// counts the number of rollouts ending with isGameOver==true
		
		/**
		 * Member {@link #m_mcPar} is only needed for saving and loading the agent
		 * (to restore the agent with all its parameter settings)
		 */
		//private MCTSParams m_mcPar;
		private ParMCTS m_parMCTS;

		/**
		 * change the version ID for serialization only if a newer version is no longer 
		 * compatible with an older one (older .agt.zip will become unreadable or you have
		 * to provide a special version transformation)
		 */
		private static final long  serialVersionUID = 13L;


		/**
		 * Default constructor for SingleMCTSPlayer_v12, needed for loading a serialized version
		 */
		public SingleMCTSPlayer_v12() {
			m_parMCTS = new ParMCTS();
			//m_mcPar = new MCTSParams();
	        m_rnd = new Random();
	        //m_root = new SingleTreeNode(m_rnd,this);
		}

		/**
	     * Creates the MCTS player. 
	     * @param a_rnd 	random number generator object.
	     * @param parMC		parameters for MCTS
	     */
	    public SingleMCTSPlayer_v12(Random a_rnd, ParMCTS parMC)
	    {
			m_parMCTS = parMC;
	        m_rnd = a_rnd;
	        //m_root = new SingleTreeNode(a_rnd,this);
	    }
	   	
		/**
	     * Creates the MCTS player. 
	     * @param a_rnd 	random number generator object.
	     * @param mcPar		parameters for MCTS
	     */
	    public SingleMCTSPlayer_v12(Random a_rnd, MCTSParams mcPar)
	    {
	    	// --- OLD, can be deleted, when the new version is tested and works:  ---
	    	//
	    	// Why do we have m_mcpar and the several single parameters?
	    	// We need both, m_mcPar for saving to disk and re-loading (use setFrom() to set
	    	// the values in the params tab). And the single parameters for computational
	    	// efficient access from the nodes of the tree.
	    	//
	    	// The setters are responsible for updating the parameters in both locations (!)    	
//			m_mcPar = new MCTSParams();
//			m_mcPar.setFrom(mcPar);
//	    	m_mcPar = new MCTSParams();
//	    	if (mcPar!=null) {
//	            this.setK(mcPar.getK_UCT());
//	            this.setNUM_ITERS(mcPar.getNumIter());
//	            this.setROLLOUT_DEPTH(mcPar.getRolloutDepth());
//	            this.setTREE_DEPTH(mcPar.getTreeDepth());
//	            this.setVerbosity(mcPar.getVerbosity());
//	    	}
	    	
			m_parMCTS = new ParMCTS(mcPar);

	        m_rnd = a_rnd;
	        //m_root = new SingleTreeNode(a_rnd,this);
	    }
	    
	    /**
	     * Set the available actions for state {@code so}.
	     * Called from {@link MCTSAgentT#act(StateObservation, ElapsedCpuTimer, double[])}.
	     * @param so
	     */
	    public void initActions(StateObservation so) {
	        //Get the actions into an array.
	        ArrayList<Types.ACTIONS> acts = so.getAvailableActions();
	        this.actions = new Types.ACTIONS[acts.size()];
	        for(int i = 0; i < actions.length; ++i)
	        {
	            actions[i] = acts.get(i);
	        }
	        NUM_ACTIONS = actions.length;    	
	    }


	}
	


}
