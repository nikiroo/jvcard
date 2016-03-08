package be.nikiroo.jvcard.tui;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
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

	static public void start(Boolean textMode, Window win) throws IOException {
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

		if (win instanceof MainWindow) {
			final MainWindow mwin = (MainWindow) win;
			mwin.refresh(terminal.getTerminalSize());
			terminal.addResizeListener(new ResizeListener() {
				@Override
				public void onResized(Terminal terminal, TerminalSize newSize) {
					mwin.refresh(newSize);
				}
			});
		}

		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();

		// Create gui and start gui
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen,
				new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
		gui.addWindowAndWait(win);

		screen.stopScreen();
	}
}
