
package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * @author Matthias Arzt
 */
public abstract class AbstractFileIoAction {

	public static final FileFilter TIFF_FILTER = new FileNameExtensionFilter(
		"TIF Image (*.tif, *.tiff)", "tif", "tiff");

	private final Extensible extensible;

	private final JFileChooser fileChooser;

	public AbstractFileIoAction(Extensible extensible, FileFilter fileFilter) {
		this.extensible = extensible;
		this.fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(fileFilter);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setFileFilter(fileFilter);
	}

	public void initSaveAction(String title, String command, Action action,
		String keyStroke)
	{
		initAction(title, command, action, keyStroke, JFileChooser.SAVE_DIALOG);
	}

	public void initOpenAction(String title, String command, Action action,
		String keyStroke)
	{
		initAction(title, command, action, keyStroke, JFileChooser.OPEN_DIALOG);
	}

	private void initAction(String title, String command, Action action,
		String keyStroke, int dialogType)
	{
		extensible.addAction(title, command, () -> openDialogAndThen(title,
			dialogType, action), keyStroke);
	}

	protected void openDialogAndThen(String title, int dialogType, Action action) {
		fileChooser.setDialogTitle(title);
		String filename = action.suggestedFile();
		if (filename != null) fileChooser.setSelectedFile(new File(filename));
		fileChooser.setDialogType(dialogType);
		final int returnVal = fileChooser.showDialog(extensible.dialogParent(),
			null);
		if (returnVal == JFileChooser.APPROVE_OPTION) runAction(action, fileChooser
			.getSelectedFile().getAbsolutePath());
	}

	private void runAction(Action action, String filename) {
		try {
			action.run(filename);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface Action {

		default String suggestedFile() {
			return null;
		}

		void run(String filename) throws Exception;
	}
}
