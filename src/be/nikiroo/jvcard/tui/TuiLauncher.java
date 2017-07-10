package be.nikiroo.jvcard.tui;

import java.io.IOException;
import java.util.List;

import be.nikiroo.jvcard.tui.panes.FileList;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.ResizeListener;
import com.googlecode.lanterna.terminal.Terminal;

/**
 * Starting the TUI.
 * 
 * @author niki
 * 
 */
public class TuiLauncher {
	static private Screen screen;

	private Boolean textMode;
	private List<String> files;

	/**
	 * 
	 * @param textMode
	 *            TRUE to force text mode, FALSE to force the Swing terminal
	 *            emulator, null to automatically determine the best choice
	 * @param files
	 *            the files to show at startup
	 * 
	 */
	public TuiLauncher(Boolean textMode, List<String> files) {
		this.textMode = textMode;
		this.files = files;
	}

	/**
	 * Start the TUI program.
	 * 
	 * 
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void start() throws IOException {
		Terminal terminal = null;

		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		if (textMode == null) {
			terminal = factory.createTerminal();
		} else if (textMode) {
			factory.setForceTextTerminal(true);
			terminal = factory.createTerminal();
		} else {
			terminal = factory.createTerminalEmulator();
		}

		final MainWindow win = new MainWindow(new FileList(files));
		win.refresh(terminal.getTerminalSize());
		terminal.addResizeListener(new ResizeListener() {
			@Override
			public void onResized(Terminal terminal, TerminalSize newSize) {
				win.refresh(newSize);
			}
		});

		screen = new TerminalScreen(terminal);
		screen.startScreen();

		// Create gui and start gui
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen,
				TextColor.ANSI.BLUE);

		gui.setTheme(UiColors.getCustomTheme());

		gui.addWindowAndWait(win);
		screen.stopScreen();
	}

	/**
	 * Return the used {@link Screen}.
	 * 
	 * @return the {@link Screen}
	 */
	static public Screen getScreen() {
		return screen;
	}
}
