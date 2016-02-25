package be.nikiroo.jvcard.i18n;

import java.util.HashMap;
import java.util.Map;

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
		KEY_ACTION_BACK, KEY_ACTION_HELP, KEY_ACTION_VIEW_CONTACT, KEY_ACTION_VIEW_CARD, KEY_ACTION_EDIT_CONTACT, KEY_ACTION_SWITCH_FORMAT, NULL;

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

	public String trans(StringId stringId) {
		if (map.containsKey(stringId)) {
			return map.get(stringId);
		}

		return stringId.toString();
	}

	private Trans() {
		map = new HashMap<StringId, String>();

		// TODO: get from a file instead?
		map.put(StringId.NULL, "");
		map.put(StringId.DUMMY, "[dummy]");
		map.put(StringId.KEY_ACTION_BACK, "Back");
		map.put(StringId.KEY_ACTION_VIEW_CONTACT, "view");
		map.put(StringId.KEY_ACTION_EDIT_CONTACT, "edit");
		map.put(StringId.KEY_ACTION_SWITCH_FORMAT, "Change view");
	}
}
