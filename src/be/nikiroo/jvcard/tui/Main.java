package be.nikiroo.jvcard.tui;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.tui.panes.ContactList;
import be.nikiroo.jvcard.tui.panes.FileList;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class Main {
	public static final String APPLICATION_TITLE = "jVcard";
	public static final String APPLICATION_VERSION = "0.9";

	public static void main(String[] args) throws IOException {
		Boolean textMode = null;
		if (args.length > 0 && args[0].equals("--tui"))
			textMode = true;
		if (args.length > 0 && args[0].equals("--gui"))
			textMode = false;

		Window win = null;

		// TODO: do not hardcode that:
		Card card = new Card(new File("/home/niki/.addressbook"), Format.Abook);
		win = new MainWindow(new ContactList(card));
		//
		List<File> files = new LinkedList<File>();
		files.add(new File("/home/niki/vcf/coworkers.vcf"));
		files.add(new File("/home/niki/vcf/oce.vcf"));
		win = new MainWindow(new FileList(files));
		//

		TuiLauncher.start(textMode, win);

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
