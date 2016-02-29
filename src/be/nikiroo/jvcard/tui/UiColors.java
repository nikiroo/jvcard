package be.nikiroo.jvcard.tui;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Label;

/**
 * All colour information must come from here.
 * 
 * @author niki
 * 
 */
public class UiColors {
	static private Object lock = new Object();
	static private UiColors instance = null;

	private Map<Element, TextColor> mapForegroundColor = null;
	private Map<Element, TextColor> mapBackgroundColor = null;
	private boolean utf = true;

	/**
	 * Get the (unique) instance of this class.
	 * 
	 * @return the (unique) instance
	 */
	static public UiColors getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new UiColors();
		}

		return instance;
	}

	public enum Element {
		DEFAULT, //
		TITLE_MAIN, TITLE_VARIABLE, TITLE_COUNT, //
		ACTION_KEY, ACTION_DESC, //
		LINE_MESSAGE, LINE_MESSAGE_ERR, LINE_MESSAGE_QUESTION, LINE_MESSAGE_ANS, //
		CONTACT_LINE, CONTACT_LINE_SEPARATOR, CONTACT_LINE_SELECTED, CONTACT_LINE_SEPARATOR_SELECTED, CONTACT_LINE_DIRTY, CONTACT_LINE_DIRTY_SELECTED;

		/**
		 * Get the foreground colour of this element.
		 * 
		 * @return the colour
		 */
		public TextColor getForegroundColor() {
			return UiColors.getInstance().getForegroundColor(this);
		}

		/**
		 * Get the background colour of this element.
		 * 
		 * @return the colour
		 */
		public TextColor getBackgroundColor() {
			return UiColors.getInstance().getBackgroundColor(this);
		}

		public Label createLabel(String text) {
			return UiColors.getInstance().createLabel(this, text);
		}

		public void themeLabel(Label lbl) {
			UiColors.getInstance().themeLabel(this, lbl);
		}
	}

	/**
	 * Check if unicode characters should be used.
	 * 
	 * @return TRUE to allow unicode
	 */
	public boolean isUnicode() {
		return utf;
	}

	/**
	 * Allow or disallow unicode characters in the program.
	 * 
	 * @param utf
	 *            TRUE to allow unuciode, FALSE to only allow ASCII characters
	 */
	public void setUnicode(boolean utf) {
		this.utf = utf;
	}

	private Label createLabel(Element el, String text) {
		Label lbl = new Label(text);
		themeLabel(el, lbl);
		return lbl;
	}

	private void themeLabel(Element el, Label lbl) {
		lbl.setForegroundColor(el.getForegroundColor());
		lbl.setBackgroundColor(el.getBackgroundColor());
	}

	private TextColor getForegroundColor(Element el) {
		if (mapForegroundColor.containsKey(el)) {
			return mapForegroundColor.get(el);
		}

		return TextColor.ANSI.BLACK;
	}

	private TextColor getBackgroundColor(Element el) {
		if (mapBackgroundColor.containsKey(el)) {
			return mapBackgroundColor.get(el);
		}

		return TextColor.ANSI.WHITE;
	}

	private UiColors() {
		mapForegroundColor = new HashMap<Element, TextColor>();
		mapBackgroundColor = new HashMap<Element, TextColor>();

		// TODO: get from a file instead?
		// TODO: use a theme that doesn't give headaches...
		addEl(Element.ACTION_KEY, TextColor.ANSI.WHITE, TextColor.ANSI.RED);
		addEl(Element.ACTION_DESC, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE);
		addEl(Element.CONTACT_LINE, TextColor.ANSI.WHITE, TextColor.ANSI.BLACK);
		addEl(Element.CONTACT_LINE_SELECTED, TextColor.ANSI.WHITE,
				TextColor.ANSI.BLUE);
		addEl(Element.CONTACT_LINE_SEPARATOR, TextColor.ANSI.RED,
				TextColor.ANSI.BLACK);
		addEl(Element.CONTACT_LINE_SEPARATOR_SELECTED, TextColor.ANSI.RED,
				TextColor.ANSI.BLUE);
		addEl(Element.LINE_MESSAGE, TextColor.ANSI.BLUE, TextColor.ANSI.WHITE);
		addEl(Element.LINE_MESSAGE_ERR, TextColor.ANSI.RED,
				TextColor.ANSI.WHITE);
		addEl(Element.LINE_MESSAGE_QUESTION, TextColor.ANSI.BLUE,
				TextColor.ANSI.WHITE);
		addEl(Element.LINE_MESSAGE_ANS, TextColor.ANSI.BLUE,
				TextColor.ANSI.BLACK);
		addEl(Element.TITLE_MAIN, TextColor.ANSI.WHITE, TextColor.ANSI.BLUE);
		addEl(Element.TITLE_VARIABLE, TextColor.ANSI.GREEN, TextColor.ANSI.BLUE);
		addEl(Element.TITLE_COUNT, TextColor.ANSI.RED, TextColor.ANSI.BLUE);
	}

	private void addEl(Element el, TextColor fore, TextColor back) {
		mapForegroundColor.put(el, fore);
		mapBackgroundColor.put(el, back);
	}
}
