package be.nikiroo.jvcard.tui;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import be.nikiroo.jvcard.resources.Bundles;

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

	private ResourceBundle bundle = null;
	private Map<String, TextColor> colorMap = null;
	private boolean utf = true;

	private UiColors() {
		colorMap = new HashMap<String, TextColor>();
		bundle = Bundles.getBundle("colors");
	}

	/**
	 * Represent an element that can be coloured (foreground/background
	 * colours).
	 * 
	 * @author niki
	 *
	 */
	public enum Element {
		DEFAULT, //
		TITLE_MAIN, TITLE_VARIABLE, TITLE_COUNT, //
		ACTION_KEY, ACTION_DESC, //
		LINE_MESSAGE, LINE_MESSAGE_ERR, LINE_MESSAGE_QUESTION, LINE_MESSAGE_ANS, //
		CONTACT_LINE, CONTACT_LINE_SEPARATOR, CONTACT_LINE_SELECTED, CONTACT_LINE_SEPARATOR_SELECTED, CONTACT_LINE_DIRTY, CONTACT_LINE_DIRTY_SELECTED, //
		VIEW_CONTACT_NAME, VIEW_CONTACT_NORMAL, VIEW_CONTACT_NOTES_TITLE, //
		;

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

		/**
		 * Create a new {@link Label} with the colours of this {@link Element}.
		 * 
		 * @param text
		 *            the text of the {@link Label}
		 * 
		 * @return the new {@link Label}
		 */
		public Label createLabel(String text) {
			return UiColors.getInstance().createLabel(this, text);
		}

		/**
		 * Theme a {@link Label} with the colours of this {@link Element}.
		 * 
		 * @param lbl
		 *            the {@link Label}
		 */
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

	/**
	 * Create a new {@link Label} with the colours of the given {@link Element}.
	 * 
	 * @param el
	 *            the {@link Element}
	 * @param text
	 *            the text of the {@link Label}
	 * 
	 * @return the new {@link Label}
	 */
	private Label createLabel(Element el, String text) {
		if (text == null)
			text = "";

		Label lbl = new Label(text);
		themeLabel(el, lbl);
		return lbl;
	}

	/**
	 * Theme a {@link Label} with the colours of the given {@link Element}.
	 * 
	 * @param el
	 *            the {@link Element}
	 * @param lbl
	 *            the {@link Label}
	 */
	private void themeLabel(Element el, Label lbl) {
		lbl.setForegroundColor(el.getForegroundColor());
		lbl.setBackgroundColor(el.getBackgroundColor());
	}

	/**
	 * Return the background colour of the given element.
	 * 
	 * @param el
	 *            the {@link Element}
	 * 
	 * @return its background colour
	 */
	private TextColor getBackgroundColor(Element el) {
		if (!colorMap.containsKey(el.name() + "_BG")) {
			String value = null;
			try {
				value = bundle.getString(el.name() + "_BG");
			} catch (MissingResourceException mre) {
				value = null;
			}
			colorMap.put(el.name() + "_BG",
					convertToColor(value, TextColor.ANSI.BLACK));
		}

		return colorMap.get(el.name() + "_BG");
	}

	/**
	 * Return the foreground colour of the given element.
	 * 
	 * @param el
	 *            the {@link Element}
	 * 
	 * @return its foreground colour
	 */
	private TextColor getForegroundColor(Element el) {
		if (!colorMap.containsKey(el.name() + "_FG")) {
			String value = null;
			try {
				value = bundle.getString(el.name() + "_FG");
			} catch (MissingResourceException mre) {
				value = null;
			}
			colorMap.put(el.name() + "_FG",
					convertToColor(value, TextColor.ANSI.WHITE));
		}

		return colorMap.get(el.name() + "_FG");
	}

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

	/**
	 * Convert the given {@link String} value to a {@link TextColor}.
	 * 
	 * @param value
	 *            the {@link String} to convert
	 * @param defaultColor
	 *            the default {@link TextColor} to return if the conversion
	 *            failed
	 * 
	 * @return the converted colour
	 */
	static private TextColor convertToColor(String value, TextColor defaultColor) {
		try {
			if (value.startsWith("@")) {
				int r = Integer.parseInt(value.substring(1, 3), 16);
				int g = Integer.parseInt(value.substring(3, 5), 16);
				int b = Integer.parseInt(value.substring(5, 7), 16);
				return TextColor.Indexed.fromRGB(r, g, b);
			} else if (value.startsWith("#")) {
				int r = Integer.parseInt(value.substring(1, 3), 16);
				int g = Integer.parseInt(value.substring(3, 5), 16);
				int b = Integer.parseInt(value.substring(5, 7), 16);
				return new TextColor.RGB(r, g, b);
			} else {
				return TextColor.ANSI.valueOf(value);
			}
		} catch (Exception e) {
			new Exception("Cannot convert value to colour: " + value, e)
					.printStackTrace();
		}

		return defaultColor;
	}

}
