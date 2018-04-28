package be.nikiroo.jvcard.tui.windows;

import jexer.TAction;
import jexer.TKeypress;
import jexer.TLabel;
import jexer.TWindow;
import be.nikiroo.jvcard.Contact;

public class TuiContactWindow extends TuiBasicWindow {
	public TuiContactWindow(final TuiBasicWindow parent, final Contact contact) {
		super(parent, "Contact view");

		addKeyBinding(TKeypress.kbQ, "Quit", new TAction() {
			@Override
			public void DO() {
				parent.getApplication().closeWindow(TuiContactWindow.this);
			}
		});

		addKeyBinding(TKeypress.kbR, "Raw view", new TAction() {
			@Override
			public void DO() {
				@SuppressWarnings("unused")
				TWindow w = new TuiRawContactWindow(TuiContactWindow.this,
						contact);
			}
		});

		@SuppressWarnings("unused")
		TLabel l = new TLabel(this, "'r' to see raw view", 0, 0);
	}
}
