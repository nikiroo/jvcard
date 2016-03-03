package be.nikiroo.jvcard.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import be.nikiroo.jvcard.tui.UiColors;

import com.googlecode.lanterna.input.KeyStroke;

/**
 * This class manages the translation of {@link Trans#StringId}s into
 * user-understandable text.
 * 
 * @author niki
 * 
 */
public class Trans {
	ResourceBundle map;

	/**
	 * An enum representing information to be translated to the user.
	 * 
	 * @author niki
	 * 
	 */
	public enum StringId {
		DUMMY, // <-- TODO : remove
		KEY_TAB, KEY_ENTER, // keys
		KEY_ACTION_BACK, KEY_ACTION_HELP, // MainWindow
		KEY_ACTION_VIEW_CARD, // FileList
		KEY_ACTION_VIEW_CONTACT, KEY_ACTION_EDIT_CONTACT, KEY_ACTION_SAVE_CARD, KEY_ACTION_DELETE_CONTACT, KEY_ACTION_SEARCH, // ContactList
		DEAULT_FIELD_SEPARATOR, DEAULT_FIELD_SEPARATOR_NOUTF, // MainContentList
		KEY_ACTION_INVERT, KEY_ACTION_FULLSCREEN, // ContactDetails
		KEY_ACTION_SWITCH_FORMAT, // multi-usage
		NULL; // Special usage
	};

	/**
	 * Create a translation service with the default language.
	 */
	public Trans() {
		init(null);
	}

	/**
	 * Create a translation service for the given language. (Will fall back to
	 * the default one i not found.)
	 * 
	 * @param language
	 *            the language to use
	 */
	public Trans(String language) {
		init(language);
	}

	/**
	 * Translate the given {@link StringId} into user text.
	 * 
	 * @param stringId
	 *            the ID to translate
	 * 
	 * @return the translated text
	 */
	public String trans(StringId stringId) {
		StringId id = stringId;
		if (!UiColors.getInstance().isUnicode()) {
			try {
				id = StringId.valueOf(stringId.toString() + "_NOUTF");
			} catch (IllegalArgumentException iae) {
				// no special _NOUTF version found
			}
		}

		if (id == StringId.NULL) {
			return "";
		}

		if (id == StringId.DUMMY) {
			return "[dummy]";
		}

		if (map.containsKey(id.toString())) {
			return map.getString(id.toString());
		}

		return id.toString();
	}

	/**
	 * Translate the given {@link KeyStroke} into a user text {@link String} of
	 * size 3.
	 * 
	 * @param key
	 *            the key to translate
	 * 
	 * @return the translated text
	 */
	public String trans(KeyStroke key) {
		String keyTrans = "";

		switch (key.getKeyType()) {
		case Enter:
			if (UiColors.getInstance().isUnicode())
				keyTrans = " ⤶ ";
			else
				keyTrans = trans(StringId.KEY_ENTER);
			break;
		case Tab:
			if (UiColors.getInstance().isUnicode())
				keyTrans = " ↹ ";
			else
				keyTrans = trans(StringId.KEY_TAB);

			break;
		case Character:
			keyTrans = " " + key.getCharacter() + " ";
			break;
		default:
			keyTrans = "" + key.getKeyType();
			int width = 3;
			if (keyTrans.length() > width) {
				keyTrans = keyTrans.substring(0, width);
			} else if (keyTrans.length() < width) {
				keyTrans = keyTrans
						+ new String(new char[width - keyTrans.length()])
								.replace('\0', ' ');
			}
			break;
		}

		return keyTrans;
	}

	/**
	 * Initialise the translation mappings for the given language.
	 * 
	 * @param lang
	 *            the language to initialise
	 */
	private void init(String lang) {
		Locale locale = null;

		if (lang == null) {
			locale = Locale.getDefault();
		} else {
			locale = Locale.forLanguageTag(lang);
		}

		map = ResourceBundle.getBundle(Trans.class.getPackage().getName()
				+ ".resources", locale, new FixedResourceBundleControl());
	}
}
