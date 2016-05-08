package be.nikiroo.jvcard.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Fixed ResourceBundle.Control class. It will use UTF-8 for the files to load.
 * 
 * Also support an option to first check into the given path before looking into
 * the resources.
 * 
 * @author niki
 *
 */
class FixedResourceBundleControl extends Control {
	private String outsideWorld = null;

	/**
	 * Create a new {@link FixedResourceBundleControl}.
	 * 
	 * @param outsideWorld
	 *            NULL if you are only interested into the resources, a path to
	 *            first check into it before looking at the actual resources
	 */
	public FixedResourceBundleControl(String outsideWorld) {
		this.outsideWorld = outsideWorld;
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		// The below is a copy of the default implementation.
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");

		ResourceBundle bundle = null;
		InputStream stream = null;
		if (reload) {
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					connection.setUseCaches(false);
					stream = connection.getInputStream();
				}
			}
		} else {
			// New code to support outside resources:
			if (outsideWorld != null) {
				String pkg = this.getClass().getPackage().getName();
				pkg = pkg.replaceAll("\\.", File.separator) + File.separator;

				if (resourceName.startsWith(pkg)) {
					try {
						String file = outsideWorld + File.separator
								+ resourceName.substring(pkg.length());
						stream = new FileInputStream(file);
					} catch (Exception e) {
						// file not in priority directory,
						// fallback to default resource
					}
				}
			}

			if (stream == null)
				stream = loader.getResourceAsStream(resourceName);
			//
		}
		if (stream != null) {
			try {
				// This line is changed to make it to read properties files
				// as UTF-8.
				// How can someone use an archaic encoding such as ISO 8859-1 by
				// *DEFAULT* is beyond me...
				bundle = new PropertyResourceBundle(new InputStreamReader(
						stream, "UTF-8"));
			} finally {
				stream.close();
			}
		}
		return bundle;
	}
}