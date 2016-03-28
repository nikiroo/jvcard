package be.nikiroo.jvcard.resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
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
	 * The type of configuration information the associated {@link Bundle} will
	 * convey.
	 * 
	 * @author niki
	 *
	 */
	public enum Target {
		colors, display, jvcard, remote, resources
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
	 * This class encapsulate a {@link ResourceBundle} in UTF-8. It only allows
	 * to retrieve values associated to an enumeration, and allows some
	 * additional methods.
	 * 
	 * @author niki
	 *
	 * @param <E>
	 *            the enum to use to get values out of this class
	 */
	public class Bundle<E extends Enum<E>> {
		private Class<E> type;
		protected Target name;
		protected ResourceBundle map;

		/**
		 * Create a new {@link Bundles} of the given name.
		 * 
		 * @param type
		 *            a runtime instance of the class of E
		 * 
		 * @param name
		 *            the name of the {@link Bundles}
		 */
		protected Bundle(Class<E> type, Target name) {
			this.type = type;
			this.name = name;
			this.map = getBundle(name);
		}

		/**
		 * Return the value associated to the given id as a {@link String}.
		 * 
		 * @param mame
		 *            the id of the value to get
		 * 
		 * @return the associated value
		 */
		public String getString(E id) {
			if (map.containsKey(id.name())) {
				return map.getString(id.name()).trim();
			}

			return "";
		}

		/**
		 * Return the value associated to the given id as a {@link Boolean}.
		 * 
		 * @param mame
		 *            the id of the value to get
		 * 
		 * @return the associated value
		 */
		public Boolean getBoolean(E id) {
			String str = getString(id);
			if (str != null && str.length() > 0) {
				if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on")
						|| str.equalsIgnoreCase("yes"))
					return true;
				if (str.equalsIgnoreCase("false")
						|| str.equalsIgnoreCase("off")
						|| str.equalsIgnoreCase("no"))
					return false;

			}

			return null;
		}

		/**
		 * Return the value associated to the given id as a {@link boolean}.
		 * 
		 * @param mame
		 *            the id of the value to get
		 * @param def
		 *            the default value when it is not present in the config
		 *            file or if it is not a boolean value
		 * 
		 * @return the associated value
		 */
		public boolean getBoolean(E id, boolean def) {
			Boolean b = getBoolean(id);
			if (b != null)
				return b;

			return def;
		}

		/**
		 * Return the value associated to the given id as an {@link Integer}.
		 * 
		 * @param mame
		 *            the id of the value to get
		 * 
		 * @return the associated value
		 */
		public Integer getInteger(E id) {
			try {
				return Integer.parseInt(getString(id));
			} catch (Exception e) {
			}

			return null;
		}

		/**
		 * Return the value associated to the given id as a {@link int}.
		 * 
		 * @param mame
		 *            the id of the value to get
		 * @param def
		 *            the default value when it is not present in the config
		 *            file or if it is not a int value
		 * 
		 * @return the associated value
		 */
		public int getInteger(E id, int def) {
			Integer i = getInteger(id);
			if (i != null)
				return i;

			return def;
		}

		/**
		 * Create/update the .properties file. Will use the most likely
		 * candidate as base if the file does not already exists and this
		 * resource is translatable (for instance, "en_US" will use "en" as a
		 * base if the resource is a translation file).
		 * 
		 * @param path
		 *            the path where the .properties files are
		 * 
		 * @throws IOException
		 *             in case of IO errors
		 */
		public void updateFile(String path) throws IOException {
			File file = getUpdateFile(path);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));

			writeHeader(writer);
			writer.write("\n");
			writer.write("\n");

			for (Field field : type.getDeclaredFields()) {
				Meta meta = field.getAnnotation(Meta.class);
				if (meta != null) {
					E id = E.valueOf(type, field.getName());
					String info = getMetaInfo(meta);

					if (info != null) {
						writer.write(info);
						writer.write("\n");
					}

					writeValue(writer, id);
				}
			}

			writer.close();
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
		protected String getMetaInfo(Meta meta) {
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
		 * Write the header found in the configuration <tt>.properties</tt> file
		 * of this {@link Bundles}.
		 * 
		 * @param writer
		 *            the {@link Writer} to write the header in
		 * 
		 * @throws IOException
		 *             in case of IO error
		 */
		protected void writeHeader(Writer writer) throws IOException {
			writer.write("# " + name + "\n");
			writer.write("#\n");
		}

		/**
		 * Write the given id to the config file, i.e.,
		 * "MY_ID = my_curent_value" followed by a new line
		 * 
		 * @param writer
		 *            the {@link Writer} to write into
		 * @param id
		 *            the id to write
		 * 
		 * @throws IOException
		 *             in case of IO error
		 */
		protected void writeValue(Writer writer, E id) throws IOException {
			writer.write(id.name());
			writer.write(" = ");

			String[] lines = getString(id).replaceAll("\\\t", "\\\\\\t").split(
					"\n");
			for (int i = 0; i < lines.length; i++) {
				writer.write(lines[i]);
				if (i < lines.length - 1) {
					writer.write("\\n\\");
				}
				writer.write("\n");
			}
		}

		/**
		 * Return the non-localised bundle of the given name.
		 * 
		 * @param name
		 *            the name of the bundle to load
		 * 
		 * @return the bundle
		 */
		protected ResourceBundle getBundle(Target name) {
			return ResourceBundle.getBundle(Bundles.class.getPackage()
					.getName() + "." + name.name(),
					new FixedResourceBundleControl(confDir));
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
		protected ResourceBundle getBundle(Target name, Locale locale) {
			return ResourceBundle.getBundle(Bundles.class.getPackage()
					.getName() + "." + name.name(), locale,
					new FixedResourceBundleControl(confDir));
		}

		/**
		 * Return the source file for this {@link Bundles} from the given path.
		 * 
		 * @param path
		 *            the path where the .properties files are
		 * 
		 * @return the source {@link File}
		 * 
		 * @throws IOException
		 *             in case of IO errors
		 */
		protected File getUpdateFile(String path) {
			return new File(path, name.name() + ".properties");
		}
	}
}
