package be.nikiroo.jvcard.tui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import jexer.TApplication;
import jexer.TWindow;
import be.nikiroo.jvcard.tui.windows.TuiFileListWindow;

/**
 * Starting the TUI.
 * 
 * @author niki
 */
public class TuiLauncherJexer extends TApplication {

	/**
	 * @param textMode
	 *            TRUE to force text mode, FALSE to force the Swing terminal
	 *            emulator, null to automatically determine the best choice
	 * @param files
	 *            the files to show at startup
	 * @throws UnsupportedEncodingException
	 */
	public TuiLauncherJexer(final Boolean textMode, final List<String> files)
			throws UnsupportedEncodingException {
		super(backend(textMode));

		addFileMenu();
		addWindowMenu();

		@SuppressWarnings("unused")
		TWindow w = new TuiFileListWindow(this, files);
	}

	/**
	 * Start the TUI program.
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void start() throws IOException {
		(new Thread(this)).start();
	}

	/**
	 * Select the most appropriate backend.
	 * 
	 * @param textMode
	 *            NULL for auto-dection
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
