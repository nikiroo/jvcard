package be.nikiroo.jvcard.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.tui.ContactList;
import be.nikiroo.jvcard.tui.MainWindow;
import be.nikiroo.jvcard.tui.TuiLauncher;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
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

public class TestCli {
	public static void main(String[] args) throws IOException {
		Boolean textMode = null;
		if (args.length > 0 && args[0].equals("--tui"))
			textMode = true;
		if (args.length > 0 && args[0].equals("--gui"))
			textMode = false;

		//TODO: do not hardcode that:
		Card card = new Card(new File("/home/niki/.addressbook"), Format.Abook);
		Window win = new MainWindow(new ContactList(card));
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

	static private Table test2() throws IOException {
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

		return table;
	}

	static private void test() throws IOException {
		// Setup terminal and screen layers
		Terminal terminal = new DefaultTerminalFactory().createTerminal();
		Screen screen = new TerminalScreen(terminal);
		screen.startScreen();

		// Create panel to hold components
		Panel panel = new Panel();
		panel.setLayoutManager(new GridLayout(2));

		panel.addComponent(new Label("Forename"));
		panel.addComponent(new TextBox());

		panel.addComponent(new Label("Surname"));
		panel.addComponent(new TextBox());

		panel.addComponent(new EmptySpace(new TerminalSize(0, 0))); // Empty
		// space
		// underneath
		// labels
		panel.addComponent(new Button("Submit"));

		// Create window to hold the panel
		BasicWindow window = new BasicWindow();
		window.setComponent(panel);

		// Create gui and start gui
		MultiWindowTextGUI gui = new MultiWindowTextGUI(screen,
				new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
		gui.addWindowAndWait(window);
	}
}
