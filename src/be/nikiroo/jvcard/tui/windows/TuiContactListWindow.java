package be.nikiroo.jvcard.tui.windows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import jexer.TWindow;
import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.resources.DisplayBundle;
import be.nikiroo.jvcard.resources.DisplayOption;

public class TuiContactListWindow extends TuiBrowserWindow {
	private Card card;
	private String filter;
	private List<String> formats;
	private int selectedFormat;
	private String format;

	public TuiContactListWindow(TuiBasicWindow parent, Card card) {
		super(parent, "Contacts", false);

		this.card = card;
		this.selectedFormat = -1;

		DisplayBundle map = new DisplayBundle();
		formats = new LinkedList<String>();
		for (String format : map.getString(DisplayOption.CONTACT_LIST_FORMAT)
				.split(",")) {
			formats.add(format);
		}

		addKeyBinding(TKeypress.kbQ, "Quit", new TAction() {
			@Override
			public void DO() {
				close();
			}
		});
		
		addKeyBinding(TKeypress.kbTab, "Switch format", new TAction() {
			@Override
			public void DO() {
				switchFormat();
			}
		});

		switchFormat();
		setCard(card);
	}

	@Override
	public void onAction(int selectedLine, int selectedColumn) {
		try {
			@SuppressWarnings("unused")
			TWindow w = new TuiContactWindow(TuiContactListWindow.this, card.get(selectedLine));
		} catch (IndexOutOfBoundsException e) {
			setMessage("Fail to get contact", true);
		}
	}

	private void setCard(Card card) {
		List<String> headers = new ArrayList<String>();
		for (String field : format.split("\\|")) {
			headers.add(field);
		}

		List<List<String>> dataLines = new ArrayList<List<String>>();
		if (card != null) {
			for (Contact c : card) {
				if (filter == null
						|| c.toString(format, "|").toLowerCase()
								.contains(filter.toLowerCase())) {
					dataLines.add(Arrays.asList(c.toStringArray(format,
							getWidth(), true)));
				}
			}
		}

		setData(headers, dataLines);
	}

	private void switchFormat() {
		if (formats.size() == 0)
			return;

		selectedFormat++;
		if (selectedFormat >= formats.size()) {
			selectedFormat = 0;
		}

		format = formats.get(selectedFormat);

		setCard(card);
	}

	// TODO
	private void setMessage(String message, boolean error) {
		if (error) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
	}
}
