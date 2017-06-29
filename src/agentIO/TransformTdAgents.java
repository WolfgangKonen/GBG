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
import java.util.List;
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
import controllers.TD.ntuple.NTupleValueFunc;
import controllers.TD.ntuple.TDNTupleAgt;
import tools.ElapsedCpuTimer;
import tools.MessageBox;
import tools.Types;
import tools.ElapsedCpuTimer.TimerType;
import tools.Types.ACTIONS;
import games.Arena;
import games.StateObservation;
import games.XArenaButtons;
import games.XNTupleFuncs;
import params.MCTSParams;
import params.NTParams;
import params.ParMCTS;
import params.TDParams;

/**
 * This class is only needed for the one-time transformation of TD-NTuple agents 
 * (class {@link controllers.TD.ntuple.TDNTupleAgt}) saved to disk from the former 
 * version V12 (serialVersionUID=12L, with members TDParams and NTParams) to the new 
 * version V13 (serialVersionUID=13L, with members ParTD and ParNT). <p>
 * 
 * The XXParams objects had the disadvantage that they contained a lot of GUI-related stuff
 * which would lead to program crash if many agents were constructed. The new ParXX objects
 * are cleaner and tidier, they contain only the parameters themselves. <p>
 * 
 * To do the transformation, follow these steps
 * <ol>
 * <li> Specify in member lists {@code fileList} and {@code dirList} all the agent files 
 * 		you want to transform. This is done in the constructor.
 * <li> Call the constructor with {@code tdirect = V12ToTemp} when the software is still in 
 * 		V12-mode, that is the agents are with TDParams and NTParams. This will load all 
 * 		specified agent files and store them in TMP_DIR in a two-file version: one file 
 * 		.agtNT containing everything from the agent except the two XXParam objects. And 
 * 		one file .param containing the two param objects. It is assumed that the new ParXX 
 * 		classes are already present.
 * <li> Now modify the software to V13-mode. That is, delete XXParams, add ParXX {with 
 * 		XX=TD,NT}, and change the serialVersionUID of the two agent classes to 13L
 * <li> Call the constructor again, now with {@code tdirect = TempToV13}. This will load 
 * 		all files in TMP_DIR and store them according to {@code fileList} and {@code dirList} 
 * 		in the new V13-version of class {@link controllers.TD.ntuple.TDNTupleAgt}.
 * </ol>
 * 
 * @see games.TicTacToe.LaunchTrainTTT
 * 
 * @author Wolfgang Konen
 */
public class TransformTdAgents {
	public static enum TRANSFORMDIRECTION {V12ToTemp,TempToV13};
	private final String TMP_DIR = "agents/temp";
//	private final JFileChooserApprove fc;
	private final FileFilter tdAgentExt = new ExtensionFilter("agt.zip",
			"TD-Agents");
	private final FileFilter txtExt = new ExtensionFilter(".txt.zip",
			"Compressed Text-Files (.txt.zip)");
//	private final Arena arenaGame;
//	private final XArenaButtons arenaButtons;
	private final JFrame arenaFrame;

	private List<String> fileList = new ArrayList<String>();
	private List<String> dirList = new ArrayList<String>(); 
	private String tdFileExt="agt.zip";

	public TransformTdAgents(TRANSFORMDIRECTION tdirect, JFrame areFrame) {
//		this.arenaGame = areGame;
//		this.arenaButtons = areButtons;
		this.arenaFrame = areFrame;

//		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
//		fc = new JFileChooserApprove();
//		fc.setCurrentDirectory(new File(strDir));
		
		// Specify here the pairs (directory,file) you want to process.
		// Note that files are *without* extension ".agt.zip", this is added from tdFileExt (!)
		dirList.add("agents/TicTacToe"); fileList.add("tdntuple"); 

		String filePath = "";
		
		try {
		switch (tdirect) {
		case V12ToTemp:
			for (int i=0; i<fileList.size(); i++) {
				filePath = dirList.get(i)+"/"+fileList.get(i);
				TDNTupleAgt pa = (TDNTupleAgt) this.loadTDAgent(dirList.get(i), fileList.get(i));
				saveTempAgtFile(pa, dirList.get(i), fileList.get(i));
			}
			break;
		case TempToV13: 
			break;
		default:
			throw new RuntimeException("Case not handled in switch(tdirect): "+tdirect);
		}
		} catch(Exception e) {
			MessageBox.show(arenaFrame,"ERROR: Agent class loaded from "
					+ filePath + " not processable", "Unknown Agent Class", JOptionPane.ERROR_MESSAGE);
			System.out.println("[ERROR: Could not load agent from "
							+ filePath + "!]");
			
			
		}
	}

	/** 
	 * Loading an agent (.agt.zip) directly, without file chooser dialog. <br>
	 * Currently, only agents of class {@link controller.TD.ntuple.TDNTupleAgt} are 
	 * handled, others lead to a ClassNotFoundException.
	 * 
	 * @param strDir
	 * @param fileName
	 * @return the agent
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public PlayAgent loadTDAgent(String strDir, String fileName) throws IOException, ClassNotFoundException {
//		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
//		String subDir = arenaGame.getGameBoard().getSubDir();
//		if (subDir != null){
//			strDir += "/"+subDir;
//		}
		checkAndCreateFolder(strDir);

		
//		fc.removeChoosableFileFilter(txtExt);
//		fc.setFileFilter(tdAgentExt);
//		fc.setCurrentDirectory(new File(strDir));
//		fc.setAcceptAllFileFilterUsed(false);
//		int returnVal = fc.showOpenDialog(arenaGame);

		TDNTupleAgt pa = null;
		String filePath = strDir+"/"+fileName+"."+tdFileExt;

			ObjectInputStream ois = null;
			FileInputStream fis = null;
			try {

				fis = new FileInputStream(filePath);
			} catch (IOException e) {
				System.out.println("[ERROR: Could not open file " + filePath
						+ " !]");
				e.printStackTrace();
			}
			
			GZIPInputStream gs = null;
			try {
				gs = new GZIPInputStream(fis);
			} catch (IOException e1) {
				System.out.println("[ERROR: Could not create ZIP-InputStream for"
								+ filePath + " !]");
				throw e1;
			}

			long fileLength = (long) 1L; //(estimateGZIPLength(file));
			final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
					gs, new agentIO.IOProgress(fileLength));
			try {
				ois = new ObjectInputStream(ptis);
			} catch (IOException e1) {
				ptis.close();
				System.out.println("[ERROR: Could not create ObjectInputStream for"
								+ filePath + " !]");
				throw e1;
			}

			//final JDialog dlg = createProgressDialog(ptis, "Loading...");

			try {
				// ois = new ObjectInputStream(gs);
				Object obj = ois.readObject();
				if (obj instanceof TDNTupleAgt) {
					pa = (TDNTupleAgt) obj;
//				} else if (obj instanceof TDAgent) {
//					pa = (TDAgent) obj;
//				} else if (obj instanceof MCTSAgentT) {
//					pa = (MCTSAgentT) obj;
//				} else if (obj instanceof MinimaxAgent) {
//					pa = (MinimaxAgent) obj;
//				} else if (obj instanceof RandomAgent) {
//					pa = (RandomAgent) obj;
				} else {
					//dlg.setVisible(false);
					MessageBox.show(arenaFrame,"ERROR: Agent class "+obj.getClass().getName()+" loaded from "
							+ filePath + " not processable", "Unknown Agent Class", JOptionPane.ERROR_MESSAGE);
					System.out.println("[ERROR: Could not load agent from "
									+ filePath + "!]");
					throw new ClassNotFoundException("ERROR: Unknown agent class");
				}
				//dlg.setVisible(false);
				//arenaGame.setProgress(null);
				System.out.println("Done.");
			} catch (IOException e) {
				//dlg.setVisible(false);
				MessageBox.show(arenaFrame,"ERROR: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				System.out.println("[ERROR: Could not open file " + filePath
						+ " !]");
				//e.printStackTrace();
				//throw e;
			} catch (ClassNotFoundException e) {
				//dlg.setVisible(false);
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
		return pa;
	}

	public void saveTDAgent(PlayAgent pa, String strDir, String fileName) throws IOException {
//		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
//		String subDir = arenaGame.getGameBoard().getSubDir();
//		if (subDir != null){
//			strDir += "/"+subDir;
//		}
		checkAndCreateFolder(strDir);

//		fc.removeChoosableFileFilter(txtExt);
//		fc.setFileFilter(tdAgentExt);
//		fc.setCurrentDirectory(new File(strDir));
//		fc.setAcceptAllFileFilterUsed(false);
//		int returnVal = fc.showSaveDialog(arenaGame);

//		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = null;
			String path = strDir+"/"+fileName+"."+tdFileExt;

			file = new File(path);
			String filePath = file.getPath();

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filePath);
			} catch (FileNotFoundException e2) {
				MessageBox.show(arenaFrame,"ERROR: Could not save TDAgent to " + filePath,
						"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				System.out.println("[ERROR: Could not save to file "
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
				System.out.println("[ERROR: Could not create ZIP-OutputStream for"
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
				System.out.println("[ERROR: Could not create Object-OutputStream for"
								+ filePath + " !]");
			}

			//final JDialog dlg = createProgressDialog(ptos, "Saving...");

			try {
				oos.writeObject(pa);
			} catch (IOException e) {
				//dlg.setVisible(false);
				if (e instanceof NotSerializableException) {
					MessageBox.show(arenaFrame,"ERROR: Object pa of class "+pa.getClass().getName()
							+" is not serializable",
							"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				}
				System.out.println("[ERROR: Could not write to file "
						+ filePath + " !]");
				ptos.close();
				throw new IOException("ERROR: Could not write object to file! ["+e.getClass().getName()+"]");
			}

			try {
				oos.flush();
				oos.close();
				fos.close();
			} catch (IOException e) {
				System.out.println("[ERROR: Could not complete Save-Process]");
				throw new IOException("ERROR: Could not complete Save-Process ["+e.getClass().getName()+"]");
			}

			//dlg.setVisible(false);
			//arenaGame.setProgress(null);
			System.out.println("Done.");

//		} else
//			System.out.println("[Save Agent: Aborted by User]");

		// Rescan current directory, hope it helps
//		fc.rescanCurrentDirectory();
	}

	public void saveTempAgtFile(PlayAgent pa, String strDir, String fileName) throws IOException {

		if (pa instanceof TDNTupleAgt) {
			TDNTupleAgt_v12 tdv12 = new TDNTupleAgt_v12( (TDNTupleAgt)pa );
			
			checkAndCreateFolder(TMP_DIR+"/"+strDir);
			File file = null;
			String path = TMP_DIR+"/"+strDir+"/"+fileName+"."+tdFileExt;

			file = new File(path);
			String filePath = file.getPath();

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(filePath);
			} catch (FileNotFoundException e2) {
				MessageBox.show(arenaFrame,"ERROR: Could not save TDAgent to " + filePath,
						"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				System.out.println("[ERROR: Could not save to file "
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
				System.out.println("[ERROR: Could not create ZIP-OutputStream for"
								+ filePath + " !]");
				throw e1;
			}

			// estimate agent size
			long bytes = tdv12.getSize();
			
			// new
			final agentIO.IOProgress p = new agentIO.IOProgress(bytes);
			final ProgressTrackingOutputStream ptos = new ProgressTrackingOutputStream(
					gz, p);

			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(ptos);
			} catch (IOException e) {
				System.out.println("[ERROR: Could not create Object-OutputStream for"
								+ filePath + " !]");
			}

			//final JDialog dlg = createProgressDialog(ptos, "Saving...");

			try {
				oos.writeObject(tdv12);
			} catch (IOException e) {
				//dlg.setVisible(false);
				if (e instanceof NotSerializableException) {
					MessageBox.show(arenaFrame,"ERROR: Object pa of class "+tdv12.getClass().getName()
							+" is not serializable",
							"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				}
				System.out.println("[ERROR: Could not write to file "
						+ filePath + " !]");
				ptos.close();
				throw new IOException("ERROR: Could not write object to file! ["+e.getClass().getName()+"]");
			}

			try {
				oos.flush();
				oos.close();
				fos.close();
			} catch (IOException e) {
				System.out.println("[ERROR: Could not complete Save-Process]");
				throw new IOException("ERROR: Could not complete Save-Process ["+e.getClass().getName()+"]");
			}

			//dlg.setVisible(false);
			//arenaGame.setProgress(null);
			System.out.println("Done.");

		} else {
			throw new RuntimeException("Agent pa not of type TDNTupleAgt");
		}
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
//			final JDialog dlg = createProgressDialog(ptis, "Loading "
//					+ zipArchiveEntry.getName() + "...");

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
			//dlg.setVisible(false);
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
	 * @return true if a folder already existed
	 */
	private boolean checkAndCreateFolder(String filePath) {
		File file = new File(filePath);
		boolean exists = file.exists();
		if(!file.exists()) {
			file.mkdirs();
		}
		return exists;
	}

//	public JDialog createProgressDialog(final IGetProgress streamProgress,
//			final String msg) {
//		// ------------------------------------------------------------
//		// Setup Progressbar Dialog
//		final JDialog dlg = new JDialog(arenaFrame, msg, true);
//		final JProgressBar dpb = new JProgressBar(0, 100);
//		dlg.add(BorderLayout.CENTER, dpb);
//		dlg.add(BorderLayout.NORTH, new JLabel("Progress..."));
//		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//		dlg.setSize(300, 75);
//		dlg.setLocationRelativeTo(arenaFrame);
//
//		Thread t = new Thread(new Runnable() {
//			public void run() {
//				dlg.setVisible(true);
//			}
//		});
//		t.start();
//
//		arenaGame.setProgress(new tools.Progress() {
//			@Override
//			public String getStatusMessage() {
//				String str = new String(msg + getProgress() + "%");
//				return str;
//			}
//
//			@Override
//			public int getProgress() {
//				int i = (int) (streamProgress.getProgess().get() * 100);
//				if (i > 100)
//					i = 100;
//				dpb.setValue(i);
//				return i;
//			}
//		});
//		return dlg; // dialog has to be closed, if not needed anymore with
//					// dlg.setVisible(false)
//	}

}
