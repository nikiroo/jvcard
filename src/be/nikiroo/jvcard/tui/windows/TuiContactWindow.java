package be.nikiroo.jvcard.tui.windows;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import jexer.TLabel;
import jexer.TWindow;
import be.nikiroo.jvcard.Contact;

public class TuiContactWindow extends TuiBasicWindow {
	public TuiContactWindow(final TApplication app, final Contact contact) {
		super(app, "Contact view");

		addKeyBinding(TKeypress.kbQ, new TAction() {
			@Override
			public void DO() {
				app.closeWindow(TuiContactWindow.this);
			}
		});

		addKeyBinding(TKeypress.kbR, new TAction() {
			@Override
			public void DO() {
				@SuppressWarnings("unused")
				TWindow w = new TuiRawContactWindow(app, contact);
			}
		});

		@SuppressWarnings("unused")
		TLabel l = new TLabel(this, "'r' to see raw view", 0, 0);
	}
}
