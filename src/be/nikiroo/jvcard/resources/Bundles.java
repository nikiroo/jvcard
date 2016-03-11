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
	static private String confDir = getConfDir();

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
				+ "." + name, new FixedResourceBundleControl(confDir));
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
				+ "." + name, locale, new FixedResourceBundleControl(confDir));
	}

	/**
	 * Set the primary configuration directory to look for <tt>.properties</tt>
	 * files in.
	 * 
	 * All {@link ResourceBundle}s returned by this class after that point will
	 * respect this new directory.
	 * 
	 * @param confDir
	 *            the new directory
	 */
	static public void setDirectory(String confDir) {
		Bundles.confDir = confDir;
	}

	/**
	 * Return the configuration directory where to try to find the
	 * <tt>.properties</tt> files in priority.
	 * 
	 * @return the configuration directory
	 */
	static private String getConfDir() {
		// Do not override user-supplied config directory (see --help)
		if (Bundles.confDir != null)
			return Bundles.confDir;

		try {
			ResourceBundle bundle = ResourceBundle.getBundle(Bundles.class
					.getPackage().getName() + "." + "jvcard",
					Locale.getDefault(), new FixedResourceBundleControl(null));

			String configDir = bundle.getString("CONFIG_DIR");
			if (configDir != null && configDir.trim().length() > 0)
				return configDir;
		} catch (Exception e) {
		}

		return null;
	}

}
