package be.nikiroo.jvcard.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class manages the translation of {@link Trans.StringId}s into
 * user-understandable text.
 * 
 * @author niki
 * 
 */
public class Trans {
	private ResourceBundle map;
	private boolean utf = true;
	private Locale locale;

	/**
	 * Create a translation service with the default language.
	 */
	public Trans() {
		setLanguage(null);
	}

	/**
	 * Create a translation service for the given language. (Will fall back to
	 * the default one i not found.)
	 * 
	 * @param language
	 *            the language to use
	 */
	public Trans(String language) {
		setLanguage(language);
	}

	/**
	 * Translate the given {@link StringId} into user text.
	 * 
	 * @param stringId
	 *            the ID to translate
	 * @param values
	 *            the values to insert instead of the place holders in the
	 *            translation
	 * 
	 * @return the translated text with the given value where required
	 */
	public String trans(StringId stringId, String... values) {
		StringId id = stringId;
		String result = null;

		if (!isUnicode()) {
			try {
				id = StringId.valueOf(stringId.name() + "_NOUTF");
			} catch (IllegalArgumentException iae) {
				// no special _NOUTF version found
			}
		}

		if (id == StringId.NULL) {
			result = "";
		} else if (id == StringId.DUMMY) {
			result = "[dummy]";
		} else if (map.containsKey(id.name())) {
			result = map.getString(id.name());
		} else {
			result = id.toString();
		}

		if (values != null && values.length > 0)
			return String.format(locale, result, (Object[]) values);
		else
			return result;
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
	 * Initialise the translation mappings for the given language.
	 * 
	 * @param language
	 *            the language to initialise, in the form "en-GB" or "fr" for
	 *            instance
	 */
	private void setLanguage(String language) {
		locale = getLocaleFor(language);
		map = Bundles.getBundle("resources", locale);
	}

	/**
	 * Create/update the translation .properties files. Will use the most likely
	 * candidate as base if the file does not already exists (for instance,
	 * "en_US" will use "en" as a base).
	 * 
	 * @param path
	 *            the path where the .properties files are
	 * 
	 * @param language
	 *            the language code to create/update (e.g.: <tt>fr-BE</tt>)
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	static public void generateTranslationFile(String path, String language)
			throws IOException {

		Locale locale = getLocaleFor(language);
		String code = locale.toString();
		Trans trans = new Trans(code);

		File file = null;
		if (code.length() > 0) {
			file = new File(path + "resources_" + code + ".properties");
		} else {
			// Default properties file:
			file = new File(path + "resources.properties");
		}

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), "UTF-8"));

		String name = locale.getDisplayCountry(locale);
		if (name.length() == 0)
			name = locale.getDisplayLanguage(locale);
		if (name.length() == 0)
			name = "default";

		if (code.length() > 0) {
			name = name + " (" + code + ")";
		}

		writer.append("# " + name + " translation file (UTF-8)\n");
		writer.append("# \n");
		writer.append("# Note that any key can be doubled with a _NOUTF suffix\n");
		writer.append("# to use when the flag --noutf is passed\n");
		writer.append("# \n");
		writer.append("# Also, the comments always refer to the key below them.\n");
		writer.append("# \n");
		writer.append("\n");

		for (Field field : StringId.class.getDeclaredFields()) {
			Meta meta = field.getAnnotation(Meta.class);
			if (meta != null) {
				StringId id = StringId.valueOf(field.getName());
				String info = getMetaInfo(meta);
				if (info != null) {
					writer.append(info);
					writer.append("\n");
				}

				writer.append(id.name());
				writer.append(" = ");
				if (!trans.trans(id).equals(id.name()))
					writer.append(trans.trans(id));
				writer.append("\n");
			}
		}

		writer.close();
	}

	/**
	 * Return the {@link Locale} representing the given language.
	 * 
	 * @param language
	 *            the language to initialise, in the form "en-GB" or "fr" for
	 *            instance
	 * 
	 * @return the corresponding {@link Locale} or the default {@link Locale} if
	 *         it is not known
	 */
	static private Locale getLocaleFor(String language) {
		Locale locale;

		if (language == null) {
			locale = Locale.getDefault();
		} else {
			language = language.replaceAll("_", "-");
			String lang = language;
			String country = null;
			if (language.contains("-")) {
				lang = language.split("-")[0];
				country = language.split("-")[1];
			}

			if (country != null)
				locale = new Locale(lang, country);
			else
				locale = new Locale(lang);
		}

		return locale;
	}

	/**
	 * Return formated, display-able information from the {@link Meta} field
	 * given. Each line will always starts with a "#" character.
	 * 
	 * @param meta
	 *            the {@link Meta} field
	 * 
	 * @return the information to display or NULL if none
	 */
	static private String getMetaInfo(Meta meta) {
		String what = meta.what();
		String where = meta.where();
		String format = meta.format();
		String info = meta.info();

		int opt = what.length() + where.length() + format.length();
		if (opt + info.length() == 0)
			return null;

		StringBuilder builder = new StringBuilder();
		builder.append("# ");

		if (opt > 0) {
			builder.append("(");
			if (what.length() > 0) {
				builder.append("WHAT: " + what);
				if (where.length() + format.length() > 0)
					builder.append(", ");
			}

			if (where.length() > 0) {
				builder.append("WHERE: " + where);
				if (format.length() > 0)
					builder.append(", ");
			}

			if (format.length() > 0) {
				builder.append("FORMAT: " + format);
			}

			builder.append(")");
			if (info.length() > 0) {
				builder.append("\n# ");
			}
		}

		builder.append(info);

		return builder.toString();
	}

	/**
	 * The enum representing textual information to be translated to the user as
	 * a key.
	 * 
	 * Note that each key that should be translated MUST be annotated with a
	 * {@link Meta} annotation.
	 * 
	 * @author niki
	 * 
	 */
	public enum StringId {
		DUMMY, // <-- TODO : remove
		NULL, // Special usage, no annotations so it is not visible in
				// .properties files
		@Meta(what = "a key to press", where = "action keys", format = "MUST BE 3 chars long", info = "Tab key")
		KEY_TAB, // keys
		@Meta(what = "a key to press", where = "action keys", format = "MUST BE 3 chars long", info = "Enter key")
		KEY_ENTER, //
		@Meta(what = "Action key", where = "All screens except the first (KEY_ACTION_QUIT)", format = "", info = "Go back to previous screen")
		KEY_ACTION_BACK, //
		@Meta(what = "Action key", where = "MainWindow", format = "", info = "Get help text")
		KEY_ACTION_HELP, //
		@Meta(what = "Action key", where = "FileList", format = "", info = "View the selected card")
		KEY_ACTION_VIEW_CARD, //
		@Meta(what = "Action key", where = "ContactList", format = "", info = "View the selected contact")
		KEY_ACTION_VIEW_CONTACT, //
		@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Edit the contact")
		KEY_ACTION_EDIT_CONTACT, //
		@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Edit the contact in RAW mode")
		KEY_ACTION_EDIT_CONTACT_RAW, //
		@Meta(what = "Action key", where = "ContactList", format = "", info = "Save the whole card")
		KEY_ACTION_SAVE_CARD, //
		@Meta(what = "", where = "ContactList", format = "", info = "Delete the selected contact")
		KEY_ACTION_DELETE_CONTACT, //
		@Meta(what = "Action key", where = "ContactList", format = "", info = "Filter the displayed contacts")
		KEY_ACTION_SEARCH, //
		@Meta(what = "", where = "", format = "we could use: ' ', ┃, │...", info = "Field separator")
		DEAULT_FIELD_SEPARATOR, // MainContentList
		@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Invert the photo's colours")
		KEY_ACTION_INVERT, //
		@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Show the photo in 'fullscreen'")
		KEY_ACTION_FULLSCREEN, //
		@Meta(what = "Action key", where = "ContactList, ContactDetails, ContactDetailsRaw", format = "", info = "Switch between the available display formats")
		KEY_ACTION_SWITCH_FORMAT, // multi-usage
		@Meta(what = "Action key", where = "Contact list, Edit Contact", format = "", info = "Add a new contact/field")
		KEY_ACTION_ADD, //
		@Meta(what = "User question: TEXT", where = "Contact list", format = "", info = "New contact")
		ASK_USER_CONTACT_NAME, //
		@Meta(what = "User question: [Y|N]", where = "Contact list", format = "%s = contact name", info = "Delete contact")
		CONFIRM_USER_DELETE_CONTACT, //
		@Meta(what = "Error", where = "Contact list", format = "%s = contact name", info = "cannot delete a contact")
		ERR_CANNOT_DELETE_CONTACT, //
	};
}
