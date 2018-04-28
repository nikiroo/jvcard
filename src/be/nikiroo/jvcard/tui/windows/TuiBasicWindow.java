package be.nikiroo.jvcard.tui.windows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import jexer.TStatusBar;
import jexer.TWindow;
import jexer.event.TKeypressEvent;
import be.nikiroo.jvcard.tui.TuiLauncherJexer;

/**
 * A basic window for using with jVCard.
 * 
 * @author niki
 */
public abstract class TuiBasicWindow extends TWindow {
	private TApplication app;
	private Map<TKeypress, TAction> keyBindings;
	private List<TAction> closeListeners;

	/**
	 * Create a new window with the given title.
	 * 
	 * @param parent
	 *            the parent {@link TuiBasicWindow}
	 * @param title
	 *            the window title
	 */
	public TuiBasicWindow(TuiBasicWindow parent, String title) {
		this(parent.app, title, parent.getWidth(), parent.getHeight());
	}

	/**
	 * Create a new window with the given title.
	 * 
	 * @param app
	 *            the application that will manage this window
	 * @param title
	 *            the window title
	 * @param width
	 *            the window width
	 * @param height
	 *            the window height
	 */
	public TuiBasicWindow(TApplication app, String title, int width, int height) {
		super(app, title, width, height);

		this.app = app;

		keyBindings = new HashMap<TKeypress, TAction>();
		closeListeners = new ArrayList<TAction>();

		if (TuiLauncherJexer.FULLSCREEN) {
			setFullscreen(true);
		}
	}

	/**
	 * Add a key binding, that is, describe a key to press and its action on he
	 * window.
	 * 
	 * @param key
	 *            the key to press
	 * @param text
	 *            the text to display for this command
	 * @param action
	 *            the action
	 */
	public void addKeyBinding(TKeypress key, String text, TAction action) {
		keyBindings.put(key, action);

		TStatusBar statusbar = getStatusBar();
		if (statusbar == null) {
			statusbar = newStatusBar("");
		}

		statusbar.addShortcutKeypress(key, null, text);
	}

	/**
	 * Add a close listener on this window that will be called when the window
	 * closes.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addCloseListener(TAction listener) {
		closeListeners.add(listener);
	}

	@Override
	public void onClose() {
		super.onClose();
		for (TAction listener : closeListeners) {
			listener.DO();
		}
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
