package be.nikiroo.jvcard.i18n;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.lanterna.input.KeyStroke;

import be.nikiroo.jvcard.tui.UiColors;

/**
 * This class manages the translation of {@link Trans#StringId}s into
 * user-understandable text.
 * 
 * @author niki
 * 
 */
public class Trans {
	static private Object lock = new Object();
	static private Trans instance = null;

	private Map<StringId, String> map = null;

	/**
	 * An enum representing information to be translated to the user.
	 * 
	 * @author niki
	 * 
	 */
	public enum StringId {
		DUMMY, // <-- TODO : remove
		KEY_ACTION_BACK, KEY_ACTION_HELP, // MainWindow
		KEY_ACTION_VIEW_CARD, // FileList
		KEY_ACTION_VIEW_CONTACT, KEY_ACTION_EDIT_CONTACT, KEY_ACTION_SAVE_CARD, KEY_ACTION_DELETE_CONTACT, KEY_ACTION_SWITCH_FORMAT, // ContactList
		DEAULT_FIELD_SEPARATOR, DEAULT_FIELD_SEPARATOR_NOUTF, // MainContentList
		NULL; // Special usage

		public String trans() {
			return Trans.getInstance().trans(this);
		}
	};

	/**
	 * Get the (unique) instance of this class.
	 * 
	 * @return the (unique) instance
	 */
	static public Trans getInstance() {
		synchronized (lock) {
			if (instance == null)
				instance = new Trans();
		}

		return instance;
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

		if (map.containsKey(id)) {
			return map.get(id);
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
				keyTrans = "ENT";
			break;
		case Tab:
			if (UiColors.getInstance().isUnicode())
				keyTrans = " ↹ ";
			else
				keyTrans = "TAB";

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

	private Trans() {
		map = new HashMap<StringId, String>();

		// TODO: get from a file instead?
		map.put(StringId.NULL, "");
		map.put(StringId.DUMMY, "[dummy]");
		// we could use: " ", "┃", "│"...
		map.put(StringId.DEAULT_FIELD_SEPARATOR, "┃");
		map.put(StringId.DEAULT_FIELD_SEPARATOR_NOUTF, "|");
		map.put(StringId.KEY_ACTION_BACK, "Back");
		map.put(StringId.KEY_ACTION_HELP, "Help");
		map.put(StringId.KEY_ACTION_VIEW_CONTACT, "Open");
		map.put(StringId.KEY_ACTION_VIEW_CARD, "Open");
		map.put(StringId.KEY_ACTION_EDIT_CONTACT, "Edit");
		map.put(StringId.KEY_ACTION_DELETE_CONTACT, "Delete");
		map.put(StringId.KEY_ACTION_SWITCH_FORMAT, "Change view");
	}
}
