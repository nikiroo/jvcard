package be.nikiroo.jvcard.resources.bundles;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.Bundles.Bundle;
import be.nikiroo.jvcard.resources.Bundles.Target;
import be.nikiroo.jvcard.resources.enums.StringId;

/**
 * This class manages the translation of {@link TransBundle.StringId}s into
 * user-understandable text.
 * 
 * @author niki
 * 
 */
public class TransBundle extends Bundle<StringId> {
	private boolean utf = true;
	private Locale locale;

	/**
	 * Create a translation service with the default language.
	 */
	public TransBundle() {
		new Bundles().super(StringId.class, Target.resources);
		setLanguage(null);
	}

	/**
	 * Create a translation service for the given language. (Will fall back to
	 * the default one i not found.)
	 * 
	 * @param language
	 *            the language to use
	 */
	public TransBundle(String language) {
		new Bundles().super(StringId.class, Target.resources);

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
	public String getString(StringId stringId, Object... values) {
		StringId id = stringId;
		String result = "";

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
		map = getBundle(Target.resources, locale);
	}

	@Override
	public String getString(StringId id) {
		return getString(id, (Object[]) null);
	}

	@Override
	protected File getUpdateFile(String path) {
		String code = locale.toString();
		File file = null;
		if (code.length() > 0) {
			file = new File(path, name.name() + "_" + code + ".properties");
		} else {
			// Default properties file:
			file = new File(path, name.name() + ".properties");
		}

		return file;
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		String code = locale.toString();
		String name = locale.getDisplayCountry(locale);

		if (name.length() == 0)
			name = locale.getDisplayLanguage(locale);
		if (name.length() == 0)
			name = "default";

		if (code.length() > 0) {
			name = name + " (" + code + ")";
		}

		StringId.writeHeader(writer, name);
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

}
