package be.nikiroo.jvcard.tui;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.tui.panes.FileList;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

/**
 * This class contains the runnable Main method. It will parse the user supplied
 * parameters and take action based upon those. Most of the time, it will start
 * a MainWindow.
 * 
 * @author niki
 *
 */
public class Main {
	public static final String APPLICATION_TITLE = "jVcard";
	public static final String APPLICATION_VERSION = "1.0-beta1-dev";

	public static void main(String[] args) {
		Boolean textMode = null;
		boolean noMoreParams = false;
		boolean filesTried = false;

		List<File> files = new LinkedList<File>();
		for (String arg : args) {
			if (!noMoreParams && arg.equals("--")) {
				noMoreParams = true;
			} else if (!noMoreParams && arg.equals("--help")) {
				System.out
						.println("TODO: implement some help text.\n"
								+ "Usable switches:\n"
								+ "\t--: stop looking for switches\n"
								+ "\t--help: this here thingy\n"
								+ "\t--tui: force pure text mode even if swing treminal is available\n"
								+ "\t--gui: force swing terminal mode\n"
								+ "everyhing else is either a file to open or a directory to open\n"
								+ "(we will only open 1st level files in given directories)");
				return;
			} else if (!noMoreParams && arg.equals("--tui")) {
				textMode = true;
			} else if (!noMoreParams && arg.equals("--gui")) {
				textMode = false;
			} else {
				filesTried = true;
				files.addAll(open(arg));
			}
		}

		if (files.size() == 0) {
			if (filesTried) {
				System.exit(1);
				return;
			}

			files.addAll(open("."));
		}

		try {
			TuiLauncher.start(textMode, new MainWindow(new FileList(files)));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(2);
		}

		/*
		 * String file = args.length > 0 ? args[0] : null; String file2 =
		 * args.length > 1 ? args[1] : null;
		 * 
		 * if (file == null) file =
		 * "/home/niki/workspace/rcard/utils/CVcard/test.vcf"; if (file2 ==
		 * null) file2 = "/home/niki/workspace/rcard/utils/CVcard/test.abook";
		 * 
		 * Card card = new Card(new File(file), Format.VCard21);
		 * System.out.println(card.toString());
		 * 
		 * System.out.println("\n -- PINE -- \n");
		 * 
		 * card = new Card(new File(file2), Format.Abook);
		 * System.out.println(card.toString(Format.Abook));
		 */
	}

	/**
	 * Open the given path and add all its files if it is a directory or just
	 * this one if not to the returned list.
	 * 
	 * @param path
	 *            the path to open
	 * 
	 * @return the list of opened files
	 */
	static private List<File> open(String path) {
		List<File> files = new LinkedList<File>();

		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File subfile : file.listFiles()) {
					if (!subfile.isDirectory())
						files.add(subfile);
				}
			} else {
				files.add(file);
			}
		} else {
			System.err.println("File or directory not found: \"" + path + "\"");
		}

		return files;
	}

	static private void fullTestTable() throws IOException {
		final Table<String> table = new Table<String>("Column 1", "Column 2",
				"Column 3");
		table.getTableModel().addRow("1", "2", "3");
		table.setSelectAction(new Runnable() {
			@Override
			public void run() {
				List<String> data = table.getTableModel().getRow(
						table.getSelectedRow());
				for (int i = 0; i < data.size(); i++) {
					System.out.println(data.get(i));
				}
			}
		});

		Window win = new BasicWindow();
		win.setComponent(table);

		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		Terminal terminal = factory.createTerminal();

		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();

		// Create gui and start gui
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen,
				new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
		gui.addWindowAndWait(win);

		screen.stopScreen();
	}
}
