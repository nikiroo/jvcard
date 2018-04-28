package be.nikiroo.jvcard.tui;

import java.io.UnsupportedEncodingException;
import java.util.List;

import jexer.TAction;
import jexer.TApplication;
import be.nikiroo.jvcard.tui.windows.TuiBrowserWindow;
import be.nikiroo.jvcard.tui.windows.TuiFileListWindow;

/**
 * Starting the TUI.
 * 
 * @author niki
 */
public class TuiLauncherJexer extends TApplication {
	/**
	 * Application is in fullscreen mode, no windows.
	 * 
	 * TODO: make it an option
	 */
	static public final boolean FULLSCREEN = true;

	/**
	 * @param textMode
	 *            TRUE to force text mode, FALSE to force the Swing terminal
	 *            emulator, null to automatically determine the best choice
	 * @param files
	 *            the files to show at startup
	 * 
	 * @throws UnsupportedEncodingException
	 *             if an exception is thrown when creating the InputStreamReader
	 */
	public TuiLauncherJexer(final Boolean textMode, final List<String> files)
			throws UnsupportedEncodingException {
		super(backend(textMode));

		addFileMenu();
		addWindowMenu();

		int width = getBackend().getScreen().getWidth();
		int height = getBackend().getScreen().getHeight() - 2;

		if (backend(textMode) == BackendType.SWING) {
			// TODO: why does the size change after the FIRST window has been
			// created (SWING mode only?) ?
			// A problem with the graphical size not an exact number of
			// cols/lines?
			width--;
			height--;
		}

		width = Math.max(1, width);
		height = Math.max(1, height);

		TuiBrowserWindow main = new TuiFileListWindow(TuiLauncherJexer.this,
				width, height, files);

		main.addCloseListener(new TAction() {
			@Override
			public void DO() {
				TuiLauncherJexer.this.exit(false);
			}
		});
	}

	/**
	 * Start the TUI program.
	 */
	public void start() {
		(new Thread(this)).start();
	}

	/**
	 * Select the most appropriate backend.
	 * 
	 * @param textMode
	 *            NULL for auto-detection
	 * @return the backend type to use
	 */
	private static BackendType backend(Boolean textMode) {
		if (textMode == null) {
			boolean isMsWindows = System.getProperty("os.name", "")
					.toLowerCase().startsWith("windows");
			boolean forceSwing = System.getProperty("jexer.Swing", "false")
					.equals("true");
			boolean noConsole = System.console() == null;
			if (isMsWindows || forceSwing || noConsole) {
				return BackendType.SWING;
			}

			return BackendType.XTERM;
		}

		if (textMode) {
			return BackendType.XTERM;
		}

		return BackendType.SWING;
	}
}
