package be.nikiroo.jvcard.tui;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import be.nikiroo.jvcard.resources.bundles.ColorBundle;
import be.nikiroo.jvcard.resources.enums.ColorOption;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Label;

/**
 * All colour information must come from here.
 * 
 * @author niki
 * 
 */
public class UiColors extends ColorBundle {
	static private Object lock = new Object();
	static private UiColors instance = null;

	private Map<String, TextColor> colorMap = null;

	private UiColors() {
		super();
		colorMap = new HashMap<String, TextColor>();
	}

	/**
	 * Create a new {@link Label} with the colours of the given {@link ColorOption}.
	 * 
	 * @param el
	 *            the {@link ColorOption}
	 * @param text
	 *            the text of the {@link Label}
	 * 
	 * @return the new {@link Label}
	 */
	static public Label createLabel(ColorOption el, String text) {
		if (text == null)
			text = "";

		Label lbl = new Label(text);
		themeLabel(el, lbl);
		return lbl;
	}

	/**
	 * Theme a {@link Label} with the colours of the given {@link ColorOption}.
	 * 
	 * @param el
	 *            the {@link ColorOption}
	 * @param lbl
	 *            the {@link Label}
	 */
	static public void themeLabel(ColorOption el, Label lbl) {
		lbl.setForegroundColor(getForegroundColor(el));
		lbl.setBackgroundColor(getBackgroundColor(el));
	}

	/**
	 * Return the background colour of the given element.
	 * 
	 * @param el
	 *            the {@link ColorOption}
	 * 
	 * @return its background colour
	 */
	static public TextColor getBackgroundColor(ColorOption el) {
		if (!getInstance().colorMap.containsKey(el.name() + "_BG")) {
			String value = null;
			try {
				value = getInstance().map.getString(el.name() + "_BG");
			} catch (MissingResourceException mre) {
				value = null;
			}
			getInstance().colorMap.put(el.name() + "_BG",
					convertToColor(value, TextColor.ANSI.BLACK));
		}

		return getInstance().colorMap.get(el.name() + "_BG");
	}

	/**
	 * Return the foreground colour of the given element.
	 * 
	 * @param el
	 *            the {@link ColorOption}
	 * 
	 * @return its foreground colour
	 */
	static public TextColor getForegroundColor(ColorOption el) {
		if (!getInstance().colorMap.containsKey(el.name() + "_FG")) {
			String value = null;
			try {
				value = getInstance().map.getString(el.name() + "_FG");
			} catch (MissingResourceException mre) {
				value = null;
			}
			getInstance().colorMap.put(el.name() + "_FG",
					convertToColor(value, TextColor.ANSI.WHITE));
		}

		return getInstance().colorMap.get(el.name() + "_FG");
	}

	/**
	 * Get the (unique) instance of this class.
	 * 
	 * @return the (unique) instance
	 */
	static private UiColors getInstance() {
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
			} else if (value.replaceAll("[0-9]*", "").length() == 0) {
				return new TextColor.Indexed(Integer.parseInt(value));
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
