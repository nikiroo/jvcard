package be.nikiroo.jvcard.resources;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class help you get UTF-8 bundles for this application.
 * 
 * @author niki
 *
 */
public class Bundles {
	/**
	 * Return the non-localised bundle of the given name.
	 * 
	 * @param name
	 *            the name of the bundle to load
	 * 
	 * @return the bundle
	 */
	static public ResourceBundle getBundle(String name) {
		return ResourceBundle.getBundle(Bundles.class.getPackage().getName()
				+ "." + name, new FixedResourceBundleControl());
	}

	/**
	 * Return the localised bundle of the given name and {@link Locale}.
	 * 
	 * @param name
	 *            the name of the bundle to load
	 * @param locale
	 *            the {@link Locale} to use
	 * 
	 * @return the localised bundle
	 */
	static public ResourceBundle getBundle(String name, Locale locale) {

		return ResourceBundle.getBundle(Bundles.class.getPackage().getName()
				+ "." + name, locale, new FixedResourceBundleControl());
	}
}
