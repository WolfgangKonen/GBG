package agentIO;

import TournamentSystem.tools.TSDiskAgentDataTransfer;
import TournamentSystem.TSResultStorage;
import controllers.*;
import games.Arena;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import tools.Types;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
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
	private JFileChooserApprove fc = null;
	private final FileFilter tdAgentExt = new ExtensionFilter("agt.zip", "TD-Agents");
	private final FileFilter tdTSRExt = new ExtensionFilter("tsr.zip", "Tournament-Result");
	private final FileFilter txtExt = new ExtensionFilter(".txt.zip", "Compressed Text-Files (.txt.zip)");
	private final Arena arenaGame;
	private final JFrame arenaFrame;
	private final String TAG = "[LoadSaveGBG] ";

	private String subDirDef;
	private String strDirDef;

	public LoadSaveGBG(Arena areGame, JFrame areFrame) {
		this.arenaGame = areGame;
		this.arenaFrame = areFrame;

		String strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
		if (arenaGame.hasGUI()) {
			fc = new JFileChooserApprove();
			fc.setCurrentDirectory(new File(strDir));			
		}
	}

	public LoadSaveGBG(String directory, String subDirectory) {
		subDirDef = subDirectory;
		strDirDef = directory;
		arenaGame = null;
		arenaFrame = null;

	}

	// It seems that the progress bar dialog and its later disposal cause from time to
	// time the 'GUI hangs' problem --> therefore we disable it in the following and comment it usages out
	//
//	public JDialog createProgressDialog(final IGetProgress streamProgress,
//			final String msg) {
//		return null;
//		// ------------------------------------------------------------
//		// setup progress-bar dialog
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

//	public void disposeProgressDialog(JDialog dlg) {
//		if (dlg!=null) {
//			dlg.setVisible(false);
//			dlg.dispose();
//			dlg=null;
//		}
//	}
	
	// ==============================================================
	// Menu: Save Agent
	// ==============================================================
	/**
	 * Save agent to disk without file chooser dialog
	 * 
	 * @param pa	the agent
	 * @param filePath the location on disk
	 * @throws IOException
	 */
	public void saveGBGAgent(PlayAgent pa, String filePath) throws IOException {
		FileOutputStream fos;
		
		if (pa==null) {
			throw new IOException("ERROR: pa=null, there is no agent to save to disk");
		}
		
		try {
			fos = new FileOutputStream(filePath);
		} catch (FileNotFoundException e2) {
			if(arenaGame!=null) {
				arenaGame.showMessage("ERROR: Could not save TDAgent to " + filePath,
						"LoadSaveGBG", JOptionPane.ERROR_MESSAGE);
				arenaGame.setStatusMessage("[ERROR: Could not save to file " + filePath + " !]");
			}
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
			if(arenaGame!=null)
				arenaGame.setStatusMessage("[ERROR: Could not create ZIP-OutputStream for" + filePath + " !]");
			throw e1;
		}

//		// estimate agent size
		long bytes = 0;
//		if (pa != null)
//			pa.getSize();
		
		// new
		final agentIO.IOProgress p = new agentIO.IOProgress(bytes);
		final ProgressTrackingOutputStream ptos = new ProgressTrackingOutputStream(gz, p);

		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(ptos);
		} catch (IOException e) {
			if(arenaGame!=null)
				arenaGame.setStatusMessage("[ERROR: Could not create Object-OutputStream for" + filePath + " !]");
//			oos.close();
			return;
		}

//		final JDialog dlg = createProgressDialog(ptos, "Saving...");

		try {
			oos.writeObject(pa);
		} catch (IOException e) {
//			disposeProgressDialog(dlg);
			if (e instanceof NotSerializableException) {
				if(arenaGame!=null)
					arenaGame.showMessage("ERROR: Object pa of class "+pa.getClass().getName()
						+" is not serializable", "LoadSaveGBG", JOptionPane.ERROR_MESSAGE);
			}
			if(arenaGame!=null)
				arenaGame.setStatusMessage("[ERROR: Could not write to file " + filePath + " !]");
			oos.close();
			throw new IOException("ERROR: Could not write object to file! ["+e.getClass().getName()+"]");
		}

		try {
			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException e) {
			if(arenaGame!=null)
				arenaGame.setStatusMessage("[ERROR: Could not complete save process]");
//			disposeProgressDialog(dlg);
			throw new IOException("ERROR: Could not complete save process ["+e.getClass().getName()+"]");
		}

//		disposeProgressDialog(dlg);
//		arenaGame.setProgress(null);
		if(arenaGame!=null)
			arenaGame.setStatusMessage("Done.");
	}

	// 
	// several methods to save to disk with file chooser dialog
	//
	/**
	 * save agent to disk with a file chooser
	 * @param pa
	 * @throws IOException
	 */
	public void saveGBGAgent(PlayAgent pa) throws IOException {
		saveGBGHelper(pa, null, false);
	}	
	/**
	 * save tournament results to disk with a file chooser to set name and folder
	 * @param tsr tournament results
	 * @throws IOException file not writable
	 */
	public void saveTSResult(TSResultStorage tsr) throws IOException {
		saveGBGHelper(null, tsr, false);
	}
	/**
	 * save tournament results to disk with a file chooser to set name and folder.
	 * if autoSave is enabled, there is no file chooser and a generic date based name will be used
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
		String strDir;
		String subDir;
		if(arenaGame!=null) {
			strDir = Types.GUI_DEFAULT_DIR_AGENT + "/" + this.arenaGame.getGameName();
			subDir = arenaGame.getGameBoard().getSubDir();
		}else{
			strDir = this.strDirDef;
			subDir  = this.subDirDef;
		}
		if (subDir != null){
			strDir += "/"+subDir;
		}
		if (tsr != null) // if TSR then save to subdir
			strDir += "/TSR/";

		tools.Utils.checkAndCreateFolder(strDir);

		if (fc==null) throw new IOException("No JFileChooser present!");

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
			returnVal = fc.showSaveDialog(arenaFrame);
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
					if(arenaGame!=null)
						arenaGame.showMessage("ERROR: Could not save TDAgent to " + filePath,
							"LoadSaveGBG", JOptionPane.ERROR_MESSAGE);
				if (tsr != null)
					if(arenaGame!=null)
						arenaGame.showMessage("ERROR: Could not save TSResultStorage to " + filePath,
								"LoadSaveGBG", JOptionPane.ERROR_MESSAGE);
				if(arenaGame!=null)
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
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not create ZIP-OutputStream for" + filePath + " !]");
//				gz.close();
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

			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(ptos);
			} catch (IOException e) {
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not create Object-OutputStream for" + filePath + " !]");
//				oos.close();
				return;
			}

//			final JDialog dlg = createProgressDialog(ptos, "Saving...");

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
//				disposeProgressDialog(dlg);
				if (e instanceof NotSerializableException) {
					if (pa != null)
						if(arenaGame!=null)
							arenaGame.showMessage("ERROR: Object pa of class "+pa.getClass().getName()
								+" is not serializable", "LoadSaveGBG", JOptionPane.ERROR_MESSAGE);
					if (tsr != null)
						if(arenaGame!=null)
							arenaGame.showMessage("ERROR: Object tsr of class "+tsr.getClass().getName()
									+" is not serializable", "LoadSaveGBG", JOptionPane.ERROR_MESSAGE);
				}
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not write to file " + filePath + " !]");
				throw new IOException("ERROR: Could not write object to file! ["+e.getClass().getName()+"]");
			}

			try {
				oos.flush();
				oos.close();
				fos.close();
			} catch (IOException e) {
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not complete Save-Process]");
//				disposeProgressDialog(dlg);
				throw new IOException("ERROR: Could not complete Save-Process ["+e.getClass().getName()+"]");
			}

//			disposeProgressDialog(dlg);
//			arenaGame.setProgress(null);
			if(arenaGame!=null)
				arenaGame.setStatusMessage("Done.");

		} else {
			if (pa != null)
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[Save Agent: Aborted by User]");
			if (tsr != null)
				if(arenaGame!=null)
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
	 */
	public PlayAgent loadGBGAgent(String filePath) throws IOException {
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		File file = null;
		PlayAgent pa = null;

		if (filePath==null) {
			String strDir;
			String subDir;
			if(arenaGame!=null) {
				strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
				subDir = arenaGame.getGameBoard().getSubDir();
			}else{
				strDir = this.strDirDef;
				subDir  = this.subDirDef;
			}

			if (subDir != null){
				strDir += "/"+subDir;
			}
			tools.Utils.checkAndCreateFolder(strDir);
			
			if (fc==null) throw new IOException("No JFileChooser present!");
			
			fc.removeChoosableFileFilter(txtExt);
			fc.setFileFilter(tdAgentExt);
			fc.setCurrentDirectory(new File(strDir));
			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showOpenDialog(arenaFrame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					file = fc.getSelectedFile();
					filePath = file.getPath();
					fis = new FileInputStream(filePath);
				} catch (IOException e) {
					if(arenaGame!=null)
						arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
					//e.printStackTrace();
					throw e;
				}
			} else {
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: File choose dialog not approved.]");
			}
		} else {	// i.e. if filePath is not null
			try {
				file = new File(filePath);
				fis = new FileInputStream(filePath);
			} catch (IOException e) {
				if(arenaGame!=null)
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
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for" + filePath + " !]");
				throw e1;
			}

			long fileLength = estimateGZIPLength(file);
			final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
					gs, new agentIO.IOProgress(fileLength));
			try {
				ois = new ObjectInputStream(ptis);
			} catch (IOException e1) {
				ptis.close();
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for" + filePath + " !]");
				throw e1;
			}

//			final JDialog dlg = createProgressDialog(ptis, "Loading...");

			pa = transformObjectToPlayAgent(ois, fis, filePath);
			
//			disposeProgressDialog(dlg);

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
			String strDir;
			String subDir;
			if(arenaGame!=null) {
				strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
				subDir = arenaGame.getGameBoard().getSubDir();
			}else{
				strDir = this.strDirDef;
				subDir  = this.subDirDef;
			}
			if (subDir != null){
				strDir += "/"+subDir;
			}
			strDir += "/TSR/";
			tools.Utils.checkAndCreateFolder(strDir);

			if (fc==null) throw new IOException("No JFileChooser present!");

			fc.removeChoosableFileFilter(txtExt);
			fc.setFileFilter(tdTSRExt);
			fc.setCurrentDirectory(new File(strDir));
			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showOpenDialog(arenaFrame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					file = fc.getSelectedFile();
					filePath = file.getPath();
					fis = new FileInputStream(filePath);
				} catch (IOException e) {
					if(arenaGame!=null)
						arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
					//e.printStackTrace();
					throw e;
				}
			} else {
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: File choose dialog not approved.]");
			}
		} else {	// i.e. if filePath is not null
			try {
				file = new File(filePath);
				fis = new FileInputStream(filePath);
			} catch (IOException e) {
				if(arenaGame!=null)
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
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for" + filePath + " !]");
				throw e1;
			}

			long fileLength = estimateGZIPLength(file);
			final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
					gs, new agentIO.IOProgress(fileLength));
			try {
				ois = new ObjectInputStream(ptis);
			} catch (IOException e1) {
				ptis.close();
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for" + filePath + " !]");
				throw e1;
			}

//			final JDialog dlg = createProgressDialog(ptis, "Loading...");

			try {
				// ois = new ObjectInputStream(gs);
				Object obj = ois.readObject();
				if (obj instanceof TSResultStorage) {
					tsr = (TSResultStorage) obj;
				} else {
//					disposeProgressDialog(dlg);
					if (arenaGame != null){
						arenaGame.showMessage("ERROR: TSR class " + obj.getClass().getName() + " loaded from "
								+ filePath + " not processable", "Unknown TS Class", JOptionPane.ERROR_MESSAGE);
						arenaGame.setStatusMessage("[ERROR: Could not load TSR from " + filePath + "!]");
					}
					throw new ClassNotFoundException("ERROR: Unknown TSR class");
				}
//				disposeProgressDialog(dlg);
//				arenaGame.setProgress(null);

				if(arenaGame!=null)
					arenaGame.setStatusMessage("Done.");
			} catch (IOException e) {
//				disposeProgressDialog(dlg);
				if(arenaGame!=null) {
					arenaGame.showMessage("ERROR: " + e.getMessage(),
							e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
					arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
				}
				//e.printStackTrace();
				//throw e;
			} catch (ClassNotFoundException e) {
//				disposeProgressDialog(dlg);
				if(arenaGame!=null)
					arenaGame.showMessage("ERROR: Class not found: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				//throw e;
			} finally {
//				if (ois != null)		// is always true
					try {
						ois.close();
					} catch (IOException e) {
						//...
					}
//				if (fis != null)		// is always true
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
		String strDir;
		String subDir;
		if(arenaGame!=null) {
			strDir = Types.GUI_DEFAULT_DIR_AGENT+"/"+this.arenaGame.getGameName();
			subDir = arenaGame.getGameBoard().getSubDir();
		}else{
			strDir = this.strDirDef;
			subDir  = this.subDirDef;
		}

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

		int returnVal = fileChooser.showOpenDialog(arenaFrame);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File[] files = fileChooser.getSelectedFiles();
			output = new TSDiskAgentDataTransfer(files.length);

			for(int i = 0; i<files.length; i++)
			{
				System.out.println("Selected file: " + files[i].getAbsolutePath());
				PlayAgent pa;
				File file = files[i];

				try {
					filePath = file.getPath();
					fis = new FileInputStream(filePath);
				} catch (IOException e) {
					if(arenaGame!=null)
						arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
					throw e;
				}

				GZIPInputStream gs;
				try {
					gs = new GZIPInputStream(fis);
				} catch (IOException e1) {
					if(arenaGame!=null)
						arenaGame.setStatusMessage("[ERROR: Could not create ZIP-InputStream for" + filePath + " !]");
					throw e1;
				}

				long fileLength = estimateGZIPLength(file);
				final ProgressTrackingObjectInputStream ptis = new ProgressTrackingObjectInputStream(
						gs, new agentIO.IOProgress(fileLength));
				try {
					ois = new ObjectInputStream(ptis);
				} catch (IOException e1) {
					ptis.close();
					if(arenaGame!=null)
						arenaGame.setStatusMessage("[ERROR: Could not create ObjectInputStream for" + filePath + " !]");
					throw e1;
				}

				//final JDialog dlg = createProgressDialog(ptis, "Loading...");

				pa = transformObjectToPlayAgent(ois, fis, filePath);
				
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
			if(arenaGame!=null)
				arenaGame.setStatusMessage("[ERROR: File choose dialog not approved.]");
		}

		return output;
	}

	/**
	 *
	 * @param ois	from where to read
	 * @param fis	just needed to close everything in case of exception
	 * @param filePath	for diagnostic messages
	 * @return	the agent read and instantiated
	 *
	 * This method is public only to make its Javadoc accessible from other classes
	 */
	public PlayAgent transformObjectToPlayAgent(ObjectInputStream ois, FileInputStream fis, String filePath/*,JDialog dlg*/)
	{
		PlayAgent pa;
		try {
			Object obj = ois.readObject();
			if (obj instanceof PlayAgent) {
				pa = (PlayAgent) obj;
				pa.instantiateAfterLoading();	// special treatment of agents after loading (if necessary)
				// [instantiateAfterLoading replaces completely the long and complicated switch statement we had here before (!)]
			} else {
//				disposeProgressDialog(dlg);
				if(arenaGame!=null)
					arenaGame.showMessage("ERROR: Agent class "+obj.getClass().getName()+" loaded from "
						+ filePath + " not processable", "Unknown Agent Class", JOptionPane.ERROR_MESSAGE);
				if(arenaGame!=null)
					arenaGame.setStatusMessage("[ERROR: Could not load agent from "
								+ filePath + "!]");
				throw new ClassNotFoundException("ERROR: Unknown agent class");
			}
				
			// Some older agents on disk might not have ParOther m_oPar.
			// If this is the case, replace the null value with a default ParOther.
			if (pa.getParOther() == null) {
				((AgentBase) pa).setDefaultParOther();
			}
			if (pa.getParReplay() == null) {
				((AgentBase) pa).setDefaultParReplay();
			}

//			disposeProgressDialog(dlg);
//			arenaGame.setProgress(null);
			if(arenaGame!=null)
				arenaGame.setStatusMessage("Done.");
		} catch (IOException e) {
//			disposeProgressDialog(dlg);
			if(arenaGame!=null)
				arenaGame.showMessage("ERROR: " + e.getMessage(),
					e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			if(arenaGame!=null)
				arenaGame.setStatusMessage("[ERROR: Could not open file " + filePath + " !]");
			//e.printStackTrace();
			pa=null;
		} catch (ClassNotFoundException e) {
//			disposeProgressDialog(dlg);
			if(arenaGame!=null)
				arenaGame.showMessage("ERROR: Class not found: " + e.getMessage(),
					e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
			pa=null;
		} catch (AssertionError e) {
//			disposeProgressDialog(dlg);
			if(arenaGame!=null)
				arenaGame.showMessage("Instantiation failed: " + e.getMessage(),
					e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
			pa=null;
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
			long fileLength = zipArchiveEntry.getSize();
			ProgressTrackingInputStream ptis = null;
			try {
				ptis = new ProgressTrackingInputStream(is,
						new agentIO.IOProgress(fileLength));
			} catch (IOException e2) {
				e2.printStackTrace();
			}
//			final JDialog dlg = createProgressDialog(ptis, "Loading " + zipArchiveEntry.getName() + "...");

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
//			disposeProgressDialog(dlg);
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
