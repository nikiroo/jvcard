package be.nikiroo.jvcard.tui.windows;

import java.util.HashMap;
import java.util.Map;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import jexer.TLabel;
import jexer.TWindow;
import jexer.event.TKeypressEvent;
import be.nikiroo.jvcard.Contact;

public class TuiContactWindow extends TWindow {
	private Map<TKeypress, TAction> keyBindings;

	public TuiContactWindow(final TApplication app, final Contact contact) {
		super(app, "Contact view", 40, 20);

		keyBindings = new HashMap<TKeypress, TAction>();

		keyBindings.put(TKeypress.kbQ, new TAction() {
			@Override
			public void DO() {
				app.closeWindow(TuiContactWindow.this);
			}
		});

		keyBindings.put(TKeypress.kbR, new TAction() {
			@Override
			public void DO() {
				@SuppressWarnings("unused")
				TWindow w = new TuiRawContactWindow(app, contact);
			}
		});

		@SuppressWarnings("unused")
		TLabel l = new TLabel(this, "'r' to see raw view", 0, 0);

		// TODO: fullscreen selection?

		// TODO: auto-maximize on FS, auto-resize on maximize
		// setFullscreen(true);
		maximize();
		onResize(null);
	}

	@Override
	public void onKeypress(TKeypressEvent keypress) {
		if (keyBindings.containsKey(keypress.getKey())) {
			keyBindings.get(keypress.getKey()).DO();
		} else {
			super.onKeypress(keypress);
		}
	}
}
