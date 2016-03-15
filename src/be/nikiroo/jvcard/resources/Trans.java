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
	 * 
	 * @return the translated text
	 */
	public String trans(StringId stringId) {
		StringId id = stringId;
		if (!isUnicode()) {
			try {
				id = StringId.valueOf(stringId.name() + "_NOUTF");
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

		if (map.containsKey(id.name())) {
			return map.getString(id.name());
		}

		return id.toString();
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
		map = Bundles.getBundle("resources", getLocaleFor(language));
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

			builder.append(")\n# ");
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
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_BACK, // MainWindow
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_HELP, //
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_VIEW_CARD, // FileList
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_VIEW_CONTACT, // ContactList
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_EDIT_CONTACT, //
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_SAVE_CARD, //
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_DELETE_CONTACT, //
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_SEARCH, //
		@Meta(what = "", where = "", format = "", info = "we could use: ' ', ┃, │...")
		DEAULT_FIELD_SEPARATOR, // MainContentList
		@Meta(what = "", where = "", format = "", info = "")
		DEAULT_FIELD_SEPARATOR_NOUTF, //
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_INVERT, // ContactDetails
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_FULLSCREEN, //
		@Meta(what = "", where = "", format = "", info = "")
		KEY_ACTION_SWITCH_FORMAT, // multi-usage
	};
}
