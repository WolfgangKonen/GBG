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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Locale;
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

import controllers.MinimaxAgent;
import controllers.PlayAgent;
import controllers.RandomAgent;
import controllers.MCTS.MCTSAgentT;
import tools.MessageBox;
import tools.Types;
import games.Arena;
import games.XArenaButtons;
import games.TicTacToe.TDPlayerTTT;
import params.TDParams;

public class LoadSaveTD {
	private static final String DEFAULT_DIR_AGENT = "agents";
	private final JFileChooserApprove fc;
	private final FileFilter tdAgentExt = new ExtensionFilter("agt.zip",
			"TD-Agents");
	private final FileFilter txtExt = new ExtensionFilter(".txt.zip",
			"Compressed Text-Files (.txt.zip)");
	private final Arena c4Game;
	private final XArenaButtons c4Buttons;
	private final JFrame c4Frame;

	public LoadSaveTD(Arena c4Game, XArenaButtons c4Buttons, JFrame c4Frame) {
		this.c4Game = c4Game;
		this.c4Buttons = c4Buttons;
		this.c4Frame = c4Frame;

		fc = new JFileChooserApprove();
		fc.setCurrentDirectory(new File(DEFAULT_DIR_AGENT));
	}

	public JDialog createProgressDialog(final IGetProgress streamProgress,
			final String msg) {
		// ------------------------------------------------------------
		// Setup Progressbar Dialog
		final JDialog dlg = new JDialog(c4Frame, msg, true);
		final JProgressBar dpb = new JProgressBar(0, 100);
		dlg.add(BorderLayout.CENTER, dpb);
		dlg.add(BorderLayout.NORTH, new JLabel("Progress..."));
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.setSize(300, 75);
		dlg.setLocationRelativeTo(c4Frame);

		Thread t = new Thread(new Runnable() {
			public void run() {
				dlg.setVisible(true);
			}
		});
		t.start();

		c4Game.setProgress(new tools.Progress() {
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
		fc.removeChoosableFileFilter(txtExt);
		fc.setFileFilter(tdAgentExt);
		fc.setCurrentDirectory(new File(Types.GUI_DEFAULT_DIR_AGENT));
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showSaveDialog(c4Game);
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
				MessageBox.show(c4Frame,"ERROR: Could not save TDPlayerTTT to " + filePath,
						"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				c4Game.setStatusMessage("[ERROR: Could not save to file "
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
				c4Game.setStatusMessage("[ERROR: Could not create ZIP-OutputStream for"
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
				c4Game.setStatusMessage("[ERROR: Could not create Object-OutputStream for"
								+ filePath + " !]");
			}

			final JDialog dlg = createProgressDialog(ptos, "Saving...");

			try {
				oos.writeObject(pa);
			} catch (IOException e) {
				dlg.setVisible(false);
				if (e instanceof NotSerializableException) {
					MessageBox.show(c4Frame,"ERROR: Object pa of class "+pa.getClass().getName()
							+" is not serializable",
							"C4Game.saveTDAgent", JOptionPane.ERROR_MESSAGE);
				}
				c4Game.setStatusMessage("[ERROR: Could not write to file "
						+ filePath + " !]");
				throw new IOException("ERROR: Could not write object to file! ["+e.getClass().getName()+"]");
			}

			try {
				oos.flush();
				oos.close();
				fos.close();
			} catch (IOException e) {
				c4Game.setStatusMessage("[ERROR: Could not complete Save-Process]");
				throw new IOException("ERROR: Could not complete Save-Process ["+e.getClass().getName()+"]");
			}

			dlg.setVisible(false);
			c4Game.setProgress(null);
			c4Game.setStatusMessage("Done.");

		} else
			c4Game.setStatusMessage("[Save Agent: Aborted by User]");

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
		PlayAgent pa = null;
		fc.removeChoosableFileFilter(txtExt);
		fc.setFileFilter(tdAgentExt);
		fc.setCurrentDirectory(new File(Types.GUI_DEFAULT_DIR_AGENT));
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showOpenDialog(c4Game);
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
				c4Game.setStatusMessage("[ERROR: Could not open file " + filePath
						+ " !]");
				e.printStackTrace();
			}
			
			GZIPInputStream gs = null;
			try {
				gs = new GZIPInputStream(fis);
			} catch (IOException e1) {
				c4Game.setStatusMessage("[ERROR: Could not create ZIP-InputStream for"
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
				c4Game.setStatusMessage("[ERROR: Could not create ObjectInputStream for"
								+ filePath + " !]");
				throw e1;
			}

			final JDialog dlg = createProgressDialog(ptis, "Loading...");

			try {
				// ois = new ObjectInputStream(gs);
				Object obj = ois.readObject();
				if (obj instanceof TDPlayerTTT) {
					pa = (TDPlayerTTT) obj;
				} else if (obj instanceof MCTSAgentT) {
					pa = (MCTSAgentT) obj;
				} else if (obj instanceof MinimaxAgent) {
					pa = (MinimaxAgent) obj;
				} else if (obj instanceof RandomAgent) {
					pa = (RandomAgent) obj;
				} else {
					dlg.setVisible(false);
					MessageBox.show(c4Frame,"ERROR: Agent class "+obj.getClass().getName()+" loaded from "
							+ filePath + " not processable", "Unknown Agent Class", JOptionPane.ERROR_MESSAGE);
					c4Game.setStatusMessage("[ERROR: Could not load agent from "
									+ filePath + "!]");
					throw new ClassNotFoundException("ERROR: Unknown agent class");
				}
				dlg.setVisible(false);
				c4Game.setProgress(null);
				c4Game.setStatusMessage("Done.");
			} catch (IOException e) {
				dlg.setVisible(false);
				MessageBox.show(c4Frame,"ERROR: Could not open file " + filePath,
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				c4Game.setStatusMessage("[ERROR: Could not open file " + filePath
						+ " !]");
				e.printStackTrace();
				throw e;
			} catch (ClassNotFoundException e) {
				dlg.setVisible(false);
				MessageBox.show(c4Frame,"ERROR: Class not found: " + e.getMessage(),
						e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();
				throw e;
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
			c4Game.setStatusMessage("[ERROR: Something went wrong while loading file "
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

}
