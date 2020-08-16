package ludiiInterface;

import javax.swing.*;

public final class Util {
    public static String loadFileFromDialog(String dialogTitle) {
        final var dialog = new JFileChooser();
        dialog.setDialogTitle(dialogTitle);
        dialog.setDialogType(JFileChooser.OPEN_DIALOG);
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dialog.setVisible(true);

        final int result = dialog.showOpenDialog(null);

        return result == JFileChooser.APPROVE_OPTION
            ? dialog.getSelectedFile().getAbsolutePath()
            : null;
    }
}
