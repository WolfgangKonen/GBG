package ludiiInterface;

import tools.Types;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class Util {
    public static String loadFileFromDialog(String dialogTitle) {
		String strDir = Types.GUI_DEFAULT_DIR_AGENT;
        final var dialog = new JFileChooser();
        dialog.setDialogTitle(dialogTitle);
        dialog.setDialogType(JFileChooser.OPEN_DIALOG);
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setCurrentDirectory(new File(strDir));
        dialog.setVisible(true);

        final int result = dialog.showOpenDialog(null);

        return result == JFileChooser.APPROVE_OPTION
            ? dialog.getSelectedFile().getAbsolutePath()
            : null;
    }

    public static void errorDialog(final Exception e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        errorDialog(sw.toString());
    }

    public static void errorDialog(final String message) {
        JOptionPane.showMessageDialog(
            new JFrame(),
            message,
            "An error occurred.",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
