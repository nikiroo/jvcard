package be.nikiroo.jvcard.tui.windows;

import java.util.ArrayList;
import java.util.List;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;

public class TuiRawContactWindow extends TuiBrowserWindow {

	public TuiRawContactWindow(TuiBasicWindow parent, Contact contact) {
		super(parent, "Contact RAW mode", false);

		List<String> headers = new ArrayList<String>();
		headers.add("Name");
		headers.add("Value");
		List<List<String>> dataLines = new ArrayList<List<String>>();
		for (Data data : contact) {
			List<String> dataLine = new ArrayList<String>(1);
			dataLine.add(data.getName());
			if (data.isBinary()) {
				dataLine.add("[BINARY]");
			} else {
				dataLine.add(data.getValue());
			}
			dataLines.add(dataLine);
		}

		addKeyBinding(TKeypress.kbQ, "Quit", new TAction() {
			@Override
			public void DO() {
				close();
			}
		});

		setData(headers, dataLines);
	}
}
