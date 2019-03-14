package agentIO;

import TournamentSystem.tools.TSDiskAgentDataTransfer;
import TournamentSystem.TSResultStorage;
import controllers.*;
import controllers.MC.MCAgent;
import controllers.MC.MCAgentN;
import controllers.MCTS.MCTSAgentT;
import controllers.MCTS0.MCTSAgentT0;
import controllers.TD.TDAgent;
import controllers.TD.ntuple2.SarsaAgt;
import controllers.TD.ntuple2.TDNTuple2Agt;
import controllers.TD.ntuple2.TDNTuple3Agt;
import games.Arena;
import games.XArenaButtons;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import tools.MessageBox;
import tools.Types;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LoadSaveGBG {
	private final JFileChooserApprove fc;
	private final FileFilter tdAgentExt = new ExtensionFilter("agt.zip", "TD-Agents");
	private final FileFilter tdTSRExt = new ExtensionFilter("tsr.zip", "Tournament-Result");
	private final FileFilter txtExt = new ExtensionFilter(".txt.zip", "Compressed Text-Files (.txt.zip)");
	private final Arena arenaGame;
	private final XArenaButtons arenaButtons;
	private final JFrame arenaFrame;
	private String tdstr="";
	private final String TAG = "[LoadSaveGBG] ";

	public LoadSaveGBG(Arena areGame, XArenaButtons areButtons, JFrame areFrame) {
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
	public void saveGBGAgent(PlayAgent pa) throws IOException {
		saveGBGHelper(pa, null, false);
	}

	/**
	 * save tournament results to disk with a filechooser to set name and folder
	 * @param tsr tournament results
	 * @throws IOException file not writable
	 */
	public void saveTSResult(TSResultStorage tsr) throws IOException {
		saveGBGHelper(null, tsr, false);
	}

	/**
	 * save tournament results to disk with a filechooser to set name and folder.
	 * if autoSave is enabled, theres no filechooser and a generic date based name will be used
	 * @param tsr tournament results
	 * @param autoSave use autoSave
	 * @throws IOException file not writeable
	 */
	public void saveTSResult(TSResultStorage tsr, boolean autoSave) throws IOException {
		saveGBGHelper(null, tsr, autoSave);
	}

	private void saveGBGHelper(PlayAgent pa, TSResultStorage tsr, final boolean autoSaveEnabled) throws IOException {
		if (pa != null && tsr != null) {
			System.out.println(TAG+"ERROR :: saveGBGHelper - pa and tsr handed over, just give one, aborting save");
			return;
		}
		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
		String subDir = arenaGame.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		if (tsr != null) // if TSR the save to subfolder
			strDir += "/TSR/";

		tools.Utils.checkAndCreateFolder(strDir);

		fc.removeChoosableFileFilter(txtExt);
		if (pa != null)
			fc.setFileFilter(tdAgentExt);
		if (tsr != null)
			fc.setFileFilter(tdTSRExt);
		fc.setCurrentDirectory(new File(strDir));
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal;
		if (autoSaveEnabled)
			returnVal = JFileChooser.APPROVE_OPTION;
		else
			returnVal = fc.showSaveDialog(arenaGame);
		String filePath;

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file;
			String path;

			if (autoSaveEnabled) {
				String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH.mm.ss"));
				String autoSaveFilename = "TSResultAutoSave";
				path = strDir + autoSaveFilename+"_"+currDateTime;
			}
			else
				path = fc.getSelectedFile().getPath();

			if (pa != null) {
				if (!path.toLowerCase().endsWith(".agt.zip")) {
					path += ".agt.zip";
				}
			}
			if (tsr != null) {
				if (!path.toLowerCase().endsWith(".tsr.zip")) {
					path += ".tsr.zip";
				}
			}

			file = new File(path);
			filePath = file.getPath();

			FileOutputStream fos;
			try {
				fos = new FileOutputStream(filePath);
			} catch (FileNotFoundException e2) {
				if (pa != null)
					MessageBox.show(arenaFrame,"ERROR: Could not save TDAgent to " + filePath,
						"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				if (tsr != null)
					MessageBox.show(arenaFrame,"ERROR: Could not save TSResultStorage to " + filePath,
							"save TSResultStorage", JOptionPane.ERROR_MESSAGE);
				arenaGame.setStatusMessage("[ERROR: Could not save to file " + filePath + " !]");
				return;
			}

			GZIPOutputStream gz;
			try {
				gz = new GZIPOutputStream(fos) {
					{
						def.setLevel(Deflater.BEST_COMPRESSION);
					}
				};

			} catch (IOException e1) {
				arenaGame.setStatusMessage("[ERROR: Could not create ZIP-OutputStream for" + filePath + " !]");
				throw e1;
			}

			// estimate agent size
			long bytes = 0;
			if (pa != null)
				pa.getSize();
			if (tsr != null)
				tsr.getSize();
			
			// new
			final agentIO.IOProgress p = new agentIO.IOProgress(bytes);
			final ProgressTrackingOutputStream ptos = new ProgressTrackingOutputStream(gz, p);

			ObjectOutputStream oos=null;
			try {
				oos = new ObjectOutputStream(ptos);
			} catch (IOException e) {
				arenaGame.setStatusMessage("[ERROR: Could not create Object-OutputStream for" + filePath + " !]");
				oos.close();
				return;
			}

			final JDialog dlg = createProgressDialog(ptos, "Saving...");

			try {
				/*
				if (pa instanceof MCTSAgentT) {
					MCTSAgentT mcts = (MCTSAgentT) pa;
					// only for debug:
//					System.out.println("saveIter1 "+mcts.getMCTSParams().getNumIter());
//					System.out.println("saveK_U1 "+mcts.getMCTSParams().getK_UCT());
//					System.out.println("saveK_U2 "+mcts.getK());
				}
				if (pa instanceof TDNTuple2Agt) {
					TDNTuple2Agt tdagt = (TDNTuple2Agt) pa;
					int dummy=1;
				}
				*/
				if (pa != null)
					oos.writeObject(pa);
				if (tsr != null)
					oos.writeObject(tsr);
			} catch (IOException e) {
				dlg.setVisible(false);
				if (e instanceof NotSerializableException) {
					if (pa != null)
						MessageBox.show(arenaFrame,"ERROR: Object pa of class "+pa.getClass().getName()
							+" is not serializable", "C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
					if (tsr != null)
						MessageBox.show(arenaFrame,"ERROR: Object tsr of class "+tsr.getClass().getName()
								+" is not serializable", "save TSResultStorage", JOptionPane.ERROR_MESSAGE);
				}
				arenaGame.setStatusMessage("[ERROR: Could not write to file " + filePath + " !]");
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

		} else {
			if (pa != null)
				arenaGame.setStatusMessage("[Save Agent: Aborted by User]");
			if (tsr != null)
				arenaGame.setStatusMessage("[Save TS-Result: Aborted by User]");
		}

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
			fileSize = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
			if (fileSize < 0)
				fileSize += (1L << 32);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileSize;
	}

	/**
	 * Load a GBG agent from disk and update it, if necessary (older agents on disk might 
	 * not yet have certain elements, which are then filled in from defaults)
	 * 
	 * @param filePath		if null, open a file choose dialog. If not null, open this fully 
	 * 						qualified file with suffix .agt.zip.
	 * @return				the agent loaded
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public PlayAgent loadGBGAgent(String filePath) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		File file = null;
		PlayAgent pa = null;

		if (filePath==null) {
			String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
			String subDir = arenaGame.getGameBoard().getSubDir();
			if (subDir != null){
				strDir += "/"+subDir;
			}
			tools.Utils.checkAndCreateFolder(strDir);

			fc.removeChoosableFileFilter(txtExt);
			fc.setFileFilter(tdAgentExt);
			fc.setCurrentDirectory(new File(strDir));
			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showOpenDialog(arenaGame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					file = fc.getSelectedFile();
					filePath = file.getPath();
					fis = new FileInputStream(filePath);
				} catch (IOException e) {
					arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
					//e.printStackTrace();
					throw e;
				}
			} else {
				arenaGame.setStatusMessage("[ERROR: File choose dialog not approved.]");
			}
		} else {	// i.e. if filePath is not null
			try {
				file = new File(filePath);
				fis = new FileInputStream(filePath);
			} catch (IOException e) {
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
				//e.printStackTrace();
				throw e;
			}
		}
		
		if (fis != null) {
			GZIPInputStream gs;
			try {
				gs = new GZIPInputStream(fis);
			} catch (IOException e1) {
				arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for" + filePath + " !]");
				throw e1;
			}

			long fileLength = (long) (estimateGZIPLength(file));
			final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
					gs, new agentIO.IOProgress(fileLength));
			try {
				ois = new ObjectInputStream(ptis);
			} catch (IOException e1) {
				ptis.close();
				arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for" + filePath + " !]");
				throw e1;
			}

			final JDialog dlg = createProgressDialog(ptis, "Loading...");

			try {
				// ois = new ObjectInputStream(gs);
				Object obj = ois.readObject();
				if (obj instanceof TDAgent) {
					pa = (TDAgent) obj;
				} else if (obj instanceof TDNTuple2Agt) {
					pa = (TDNTuple2Agt) obj;
					// set horizon cut for older agents (where horCut was not part of ParTD):
					if (((TDNTuple2Agt) pa).getParTD().getHorizonCut()==0.0) 
						((TDNTuple2Agt) pa).getParTD().setHorizonCut(0.1);
					// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
					// (they would stay otherwise at their default values, would not 
					// get the loaded values)
					((TDNTuple2Agt) pa).setTDParams(((TDNTuple2Agt) pa).getParTD(), pa.getMaxGameNum());
					((TDNTuple2Agt) pa).setNTParams(((TDNTuple2Agt) pa).getParNT());
					((TDNTuple2Agt) pa).weightAnalysis(null);
				} else if (obj instanceof TDNTuple3Agt) {
					pa = (TDNTuple3Agt) obj;
					// set horizon cut for older agents (where horCut was not part of ParTD):
					if (((TDNTuple3Agt) pa).getParTD().getHorizonCut()==0.0) 
						((TDNTuple3Agt) pa).getParTD().setHorizonCut(0.1);
					// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
					// (they would stay otherwise at their default values, would not 
					// get the loaded values)
					((TDNTuple3Agt) pa).setTDParams(((TDNTuple3Agt) pa).getParTD(), pa.getMaxGameNum());
					((TDNTuple3Agt) pa).setNTParams(((TDNTuple3Agt) pa).getParNT());
					((TDNTuple3Agt) pa).weightAnalysis(null);
				} else if (obj instanceof SarsaAgt) {
					pa = (SarsaAgt) obj;
					// set horizon cut for older agents (where horCut was not part of ParTD):
					if (((SarsaAgt) pa).getParTD().getHorizonCut()==0.0) 
						((SarsaAgt) pa).getParTD().setHorizonCut(0.1);
					// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
					// (they would stay otherwise at their default values, would not 
					// get the loaded values)
					((SarsaAgt) pa).setTDParams(((SarsaAgt) pa).getParTD(), pa.getMaxGameNum());
					((SarsaAgt) pa).setNTParams(((SarsaAgt) pa).getParNT());
					((SarsaAgt) pa).weightAnalysis(null);
				} else if (obj instanceof MCTSAgentT0) {
					pa = (MCTSAgentT0) obj;
				} else if (obj instanceof MCTSAgentT) {
					pa = (MCTSAgentT) obj;
				} else if (obj instanceof MCAgent) {
					pa = (MCAgent) obj;
				} else if (obj instanceof MCAgentN) {
					pa = (MCAgentN) obj;
				} else if (obj instanceof MinimaxAgent) {
					pa = (MinimaxAgent) obj;
				} else if (obj instanceof MaxNAgent) {
					pa = (MaxNAgent) obj;
				} else if (obj instanceof ExpectimaxNAgent) {
					pa = (ExpectimaxNAgent) obj;
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
				
				if (pa instanceof AgentBase) {
					// Some older agents on disk might not have ParOther m_oPar.
					// If this is the case, replace the null value with a default ParOther:
					if (((AgentBase) pa).getParOther() == null ) {
						((AgentBase) pa).setDefaultOtherPar();
					}						
				}

				dlg.setVisible(false);
				arenaGame.setProgress(null);
				arenaGame.setStatusMessage("Done.");
			} catch (IOException e) {
				dlg.setVisible(false);
				MessageBox.show(arenaFrame,"ERROR: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
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
		} 
		
		return pa;
	}

	/**
	 * load saved tournament results from disk to reopen visualization
	 * @param filePath		if null, open a file choose dialog. If not null, open this fully
	 * 						qualified file with suffix .tsr.zip.
	 * @return				the tournament result loaded
	 * @throws IOException	error while loading the file from disk
	 */
	public TSResultStorage loadGBGTSResult(String filePath) throws IOException {
		ObjectInputStream ois;
		FileInputStream fis = null;
		File file = null;
		TSResultStorage tsr = null;

		if (filePath==null) {
			String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
			String subDir = arenaGame.getGameBoard().getSubDir();
			if (subDir != null){
				strDir += "/"+subDir;
			}
			strDir += "/TSR/";
			tools.Utils.checkAndCreateFolder(strDir);

			fc.removeChoosableFileFilter(txtExt);
			fc.setFileFilter(tdTSRExt);
			fc.setCurrentDirectory(new File(strDir));
			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showOpenDialog(arenaGame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					file = fc.getSelectedFile();
					filePath = file.getPath();
					fis = new FileInputStream(filePath);
				} catch (IOException e) {
					arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
					//e.printStackTrace();
					throw e;
				}
			} else {
				arenaGame.setStatusMessage("[ERROR: File choose dialog not approved.]");
			}
		} else {	// i.e. if filePath is not null
			try {
				file = new File(filePath);
				fis = new FileInputStream(filePath);
			} catch (IOException e) {
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
				//e.printStackTrace();
				throw e;
			}
		}

		if (fis != null) {
			GZIPInputStream gs;
			try {
				gs = new GZIPInputStream(fis);
			} catch (IOException e1) {
				arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for" + filePath + " !]");
				throw e1;
			}

			long fileLength = (long) (estimateGZIPLength(file));
			final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
					gs, new agentIO.IOProgress(fileLength));
			try {
				ois = new ObjectInputStream(ptis);
			} catch (IOException e1) {
				ptis.close();
				arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for" + filePath + " !]");
				throw e1;
			}

			final JDialog dlg = createProgressDialog(ptis, "Loading...");

			try {
				// ois = new ObjectInputStream(gs);
				Object obj = ois.readObject();
				if (obj instanceof TSResultStorage) {
					tsr = (TSResultStorage) obj;
				} else {
					dlg.setVisible(false);
					MessageBox.show(arenaFrame,"ERROR: TSR class "+obj.getClass().getName()+" loaded from "
							+ filePath + " not processable", "Unknown TS Class", JOptionPane.ERROR_MESSAGE);
					arenaGame.setStatusMessage("[ERROR: Could not load TSR from "+ filePath + "!]");
					throw new ClassNotFoundException("ERROR: Unknown TSR class");
				}
				dlg.setVisible(false);
				arenaGame.setProgress(null);
				arenaGame.setStatusMessage("Done.");
			} catch (IOException e) {
				dlg.setVisible(false);
				MessageBox.show(arenaFrame,"ERROR: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
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
						//...
					}
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						//...
					}
			}
		}

		return tsr;
	}

	/**
	 * Load multiple GBG agents from disk and update them, if necessary (older agents on disk might 
	 * not yet have certain elements, which are then filled in from defaults)
	 * 
	 * @return object to transfer the loaded agents and their filenames
	 * @throws IOException
	 */
	public TSDiskAgentDataTransfer loadMultipleGBGAgent() throws IOException {
		TSDiskAgentDataTransfer output = null;
		String filePath = null;
		ObjectInputStream ois;
		FileInputStream fis;

		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
		String subDir = arenaGame.getGameBoard().getSubDir();
		if (subDir != null){
			strDir += "/"+subDir;
		}
		tools.Utils.checkAndCreateFolder(strDir);
		/*
		fc.removeChoosableFileFilter(txtExt);
		fc.setFileFilter(tdAgentExt);
		fc.setCurrentDirectory(new File(strDir));
		fc.setAcceptAllFileFilterUsed(false);
		*/
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.removeChoosableFileFilter(txtExt);
		fileChooser.setFileFilter(tdAgentExt);
		fileChooser.setCurrentDirectory(new File(strDir));
		fileChooser.setAcceptAllFileFilterUsed(false);

		int returnVal = fileChooser.showOpenDialog(arenaGame);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File[] files = fileChooser.getSelectedFiles();
			output = new TSDiskAgentDataTransfer(files.length);

			for(int i = 0; i<files.length; i++)
			{
				System.out.println("Selected file: " + files[i].getAbsolutePath());
				PlayAgent pa = null;
				File file = files[i];

				try {
					filePath = file.getPath();
					fis = new FileInputStream(filePath);
				} catch (IOException e) {
					arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
					throw e;
				}

				GZIPInputStream gs;
				try {
					gs = new GZIPInputStream(fis);
				} catch (IOException e1) {
					arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for" + filePath + " !]");
					throw e1;
				}

				long fileLength = (long) (estimateGZIPLength(file));
				final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
						gs, new agentIO.IOProgress(fileLength));
				try {
					ois = new ObjectInputStream(ptis);
				} catch (IOException e1) {
					ptis.close();
					arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for" + filePath + " !]");
					throw e1;
				}

				//final JDialog dlg = createProgressDialog(ptis, "Loading...");


				try {
					// ois = new ObjectInputStream(gs);
					Object obj = ois.readObject();
					if (obj instanceof TDAgent) {
						pa = (TDAgent) obj;
					} else if (obj instanceof TDNTuple2Agt) {
						pa = (TDNTuple2Agt) obj;
						// set horizon cut for older agents (where horCut was not part of ParTD):
						if (((TDNTuple2Agt) pa).getParTD().getHorizonCut()==0.0) 
							((TDNTuple2Agt) pa).getParTD().setHorizonCut(0.1);
						// set certain elements in td.m_Net (withSigmoid, useSymmetry) from tdPar and ntPar
						// (they would stay otherwise at their default values, would not 
						// get the loaded values)
						((TDNTuple2Agt) pa).setTDParams(((TDNTuple2Agt) pa).getParTD(), pa.getMaxGameNum());
						((TDNTuple2Agt) pa).setNTParams(((TDNTuple2Agt) pa).getParNT());
					} else if (obj instanceof MCTSAgentT0) {
						pa = (MCTSAgentT0) obj;
					} else if (obj instanceof MCTSAgentT) {
						pa = (MCTSAgentT) obj;
					} else if (obj instanceof MCAgent) {
						pa = (MCAgent) obj;
					} else if (obj instanceof MCAgentN) {
						pa = (MCAgentN) obj;
					} else if (obj instanceof MinimaxAgent) {
						pa = (MinimaxAgent) obj;
					} else if (obj instanceof MaxNAgent) {
						pa = (MaxNAgent) obj;
					} else if (obj instanceof ExpectimaxNAgent) {
						pa = (ExpectimaxNAgent) obj;
					} else if (obj instanceof RandomAgent) {
						pa = (RandomAgent) obj;
					} else {
						//dlg.setVisible(false);
						MessageBox.show(arenaFrame,"ERROR: Agent class "+obj.getClass().getName()+" loaded from "
								+ filePath + " not processable", "Unknown Agent Class", JOptionPane.ERROR_MESSAGE);
						arenaGame.setStatusMessage("[ERROR: Could not load agent from "
								+ filePath + "!]");
						throw new ClassNotFoundException("ERROR: Unknown agent class");
					}
					
					// bug fix WK 08/2018
					if (pa instanceof AgentBase) {
						// Some older agents on disk might not have ParOther m_oPar.
						// If this is the case, replace the null value with a default ParOther:
						if (((AgentBase) pa).getParOther() == null ) {
							((AgentBase) pa).setDefaultOtherPar();
						}						
					}
					
					//dlg.setVisible(false);
					arenaGame.setProgress(null);
					arenaGame.setStatusMessage("Done.");
				} catch (IOException e) {
					//dlg.setVisible(false);
					MessageBox.show(arenaFrame,"ERROR: " + e.getMessage(),
							e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
					arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
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
							e.printStackTrace();
						}
					if (fis != null)
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}

				//output[i][0] = pa;
				Path p = Paths.get(filePath);
				String fileNameSource = p.getFileName().toString();
				//System.out.println(TAG+" "+fileNameSource); // minimax.agt.zip
				//System.out.println(TAG+" "+fileNameSource.substring(0,fileNameSource.length()-8)); // minimax.agt.zip
				String filename = fileNameSource.substring(0,fileNameSource.length()-8);
				output.addAgent(filename,pa);
			} // for(int i = 0; i<files.length; i++)
		} // if (returnVal == JFileChooser.APPROVE_OPTION)
		else {
			arenaGame.setStatusMessage("[ERROR: File choose dialog not approved.]");
		}

		return output;
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

}
