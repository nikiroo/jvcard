package be.nikiroo.jvcard.launcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.remote.Command;
import be.nikiroo.jvcard.remote.SimpleSocket;
import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.StringUtils;
import be.nikiroo.jvcard.resources.Trans;
import be.nikiroo.jvcard.resources.Trans.StringId;

/**
 * This class contains the runnable Main method. It will parse the user supplied
 * parameters and take action based upon those. Most of the time, it will start
 * a MainWindow.
 * 
 * @author niki
 *
 */
public class Main {
	static public final String APPLICATION_TITLE = "jVcard";
	static public final String APPLICATION_VERSION = "1.0-beta2-dev";

	static private final int ERR_NO_FILE = 1;
	static private final int ERR_SYNTAX = 2;
	static private final int ERR_INTERNAL = 3;
	static private Trans transService;

	/**
	 * Translate the given {@link StringId}.
	 * 
	 * @param id
	 *            the ID to translate
	 * 
	 * @return the translation
	 */
	static public String trans(StringId id) {
		return transService.trans(id);
	}

	/**
	 * Check if unicode characters should be used.
	 * 
	 * @return TRUE to allow unicode
	 */
	static public boolean isUnicode() {
		return transService.isUnicode();
	}

	/**
	 * Start the application.
	 * 
	 * <p>
	 * The returned exit codes are:
	 * <ul>
	 * <li>1: no files to open</li>
	 * <li>2: invalid syntax</li>
	 * <li>3: internal error</li>
	 * </ul>
	 * </p>
	 * 
	 * @param args
	 *            the parameters (see <tt>--help</tt> to know which are
	 *            supported)
	 */
	public static void main(String[] args) {
		Boolean textMode = null;
		boolean noMoreParams = false;
		boolean filesTried = false;

		// get the "system default" language to help translate the --help
		// message if needed
		String language = null;
		transService = new Trans(language);

		boolean unicode = transService.isUnicode();
		String i18nDir = null;
		List<String> files = new LinkedList<String>();
		Integer port = null;
		for (int index = 0; index < args.length; index++) {
			String arg = args[index];
			if (!noMoreParams && arg.equals("--")) {
				noMoreParams = true;
			} else if (!noMoreParams && arg.equals("--help")) {
				System.out
						.println("TODO: implement some help text.\n"
								+ "Usable switches:\n"
								+ "\t--: stop looking for switches\n"
								+ "\t--help: this here thingy\n"
								+ "\t--lang LANGUAGE: choose the language, for instance en_GB\n"
								+ "\t--tui: force pure text mode even if swing treminal is available\n"
								+ "\t--gui: force swing terminal mode\n"
								+ "\t--noutf: force non-utf8 mode if you need it\n"
								+ "\t--config DIRECTORY: force the given directory as a CONFIG_DIR\n"
								+ "\t--server PORT: start a remoting server instead of a client\n"
								+ "\t--i18n DIR: generate the translation file for the given language (can be \"\") to/from the .properties given dir\n"
								+ "everyhing else is either a file to open or a directory to open\n"
								+ "(we will only open 1st level files in given directories)\n"
								+ "('jvcard://hostname:8888/file' links -- or without 'file' -- are also ok)\n");
				return;
			} else if (!noMoreParams && arg.equals("--tui")) {
				textMode = true;
			} else if (!noMoreParams && arg.equals("--gui")) {
				textMode = false;
			} else if (!noMoreParams && arg.equals("--noutf")) {
				unicode = false;
				transService.setUnicode(unicode);
			} else if (!noMoreParams && arg.equals("--lang")) {
				index++;
				if (index >= args.length) {
					System.err.println("Syntax error: no language given");
					System.exit(ERR_SYNTAX);
					return;
				}

				language = args[index];
				transService = new Trans(language);
				transService.setUnicode(unicode);
			} else if (!noMoreParams && arg.equals("--config")) {
				index++;
				if (index >= args.length) {
					System.err
							.println("Syntax error: no config directory given");
					System.exit(ERR_SYNTAX);
					return;
				}

				Bundles.setDirectory(args[index]);
				transService = new Trans(language);
				transService.setUnicode(unicode);
			} else if (!noMoreParams && arg.equals("--server")) {
				index++;
				if (index >= args.length) {
					System.err.println("Syntax error: no port given");
					System.exit(ERR_SYNTAX);
					return;
				}

				try {
					port = Integer.parseInt(args[index]);
				} catch (NumberFormatException e) {
					System.err.println("Invalid port number: " + args[index]);
					System.exit(ERR_SYNTAX);
					return;
				}
			} else if (!noMoreParams && arg.equals("--i18n")) {
				index++;
				if (index >= args.length) {
					System.err
							.println("Syntax error: no .properties directory given");
					System.exit(ERR_SYNTAX);
					return;
				}

				i18nDir = args[index];
			} else {
				filesTried = true;
				files.addAll(open(arg));
			}
		}

		if (unicode) {
			utf8();
		}

		// Error management:
		if (port != null && files.size() > 0) {
			System.err
					.println("Invalid syntax: you cannot both use --server and provide card files");
			System.exit(ERR_SYNTAX);
		} else if (i18nDir != null && files.size() > 0) {
			System.err
					.println("Invalid syntax: you cannot both use --i18n and provide card files");
			System.exit(ERR_SYNTAX);
		} else if (port != null && i18nDir != null) {
			System.err
					.println("Invalid syntax: you cannot both use --server and --i18n");
			System.exit(ERR_SYNTAX);
		} else if (i18nDir != null && language == null) {
			System.err
					.println("Invalid syntax: you cannot use --i18n without --lang");
			System.exit(ERR_SYNTAX);
		} else if (port == null && i18nDir == null && files.size() == 0) {
			if (files.size() == 0 && !filesTried) {
				files.addAll(open("."));
			}

			if (files.size() == 0) {
				System.err.println("No files to open");
				System.exit(ERR_NO_FILE);
				return;
			}
		}
		//

		if (port != null) {
			try {
				runServer(port);
			} catch (Exception e) {
				if (e instanceof IOException) {
					System.err
							.println("I/O Exception: Cannot start the server");
				} else {
					System.err.println("Remoting support not available");
					System.exit(ERR_INTERNAL);
				}
			}
		} else if (i18nDir != null) {
			try {
				Trans.generateTranslationFile(i18nDir, language);
			} catch (IOException e) {
				System.err
						.println("I/O Exception: Cannot create/update a language in directory: "
								+ i18nDir);
			}
		} else {
			try {
				startTui(textMode, files);
			} catch (Exception e) {
				if (e instanceof IOException) {
					System.err
							.println("I/O Exception: Cannot start the program with the given cards");
				} else {
					System.err.println("TUI support not available");
					System.exit(ERR_INTERNAL);
				}
			}
		}
	}

	/**
	 * Return the {@link Card} corresponding to the given resource name -- a
	 * file or a remote jvcard URL
	 * 
	 * @param input
	 *            a filename or a remote jvcard url with named resource (e.g.:
	 *            <tt>jvcard://localhost:4444/coworkers.vcf</tt>)
	 * 
	 * @return the {@link Card}
	 * 
	 * @throws IOException
	 *             in case of IO error or remoting not available
	 */
	static public Card getCard(String input) throws IOException {
		boolean remote = false;
		Format format = Format.Abook;
		String ext = input;
		if (ext.contains(".")) {
			String tab[] = ext.split("\\.");
			if (tab.length > 1 && tab[tab.length - 1].equalsIgnoreCase("vcf")) {
				format = Format.VCard21;
			}
		}

		if (input.contains("://")) {
			format = Format.VCard21;
			remote = true;
		}

		Card card = null;
		try {
			if (remote) {
				card = syncCard(input);
			} else {
				card = new Card(new File(input), format);
			}
		} catch (IOException ioe) {
			throw ioe;
		} catch (Exception e) {
			throw new IOException("Remoting support not available", e);
		}

		return card;
	}

	/**
	 * Create a new jVCard server on the given port, then run it.
	 * 
	 * @param port
	 *            the port to run on
	 *
	 * @throws SecurityException
	 *             in case of internal error
	 * @throws NoSuchMethodException
	 *             in case of internal error
	 * @throws ClassNotFoundException
	 *             in case of internal error
	 * @throws IllegalAccessException
	 *             in case of internal error
	 * @throws InstantiationException
	 *             in case of internal error
	 * @throws InvocationTargetException
	 *             in case of internal error
	 * @throws IllegalArgumentException
	 *             in case of internal error
	 * @throws IOException
	 *             in case of IO error
	 */
	@SuppressWarnings("unchecked")
	static private void runServer(int port) throws NoSuchMethodException,
			SecurityException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		@SuppressWarnings("rawtypes")
		Class serverClass = Class.forName("be.nikiroo.jvcard.remote.Server");
		Method run = serverClass.getDeclaredMethod("run", new Class[] {});
		run.invoke(serverClass.getConstructor(int.class).newInstance(port));
	}

	/**
	 * Start the TUI program.
	 * 
	 * @param textMode
	 *            TRUE to force text mode, FALSE to force the Swing terminal
	 *            emulator, null to automatically determine the best choice
	 * @param files
	 *            the files to show at startup
	 * 
	 * @throws SecurityException
	 *             in case of internal error
	 * @throws NoSuchMethodException
	 *             in case of internal error
	 * @throws ClassNotFoundException
	 *             in case of internal error
	 * @throws IllegalAccessException
	 *             in case of internal error
	 * @throws InstantiationException
	 *             in case of internal error
	 * @throws InvocationTargetException
	 *             in case of internal error
	 * @throws IllegalArgumentException
	 *             in case of internal error
	 * @throws IOException
	 *             in case of IO error
	 */
	@SuppressWarnings("unchecked")
	static private void startTui(Boolean textMode, List<String> files)
			throws NoSuchMethodException, SecurityException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		@SuppressWarnings("rawtypes")
		Class launcherClass = Class
				.forName("be.nikiroo.jvcard.tui.TuiLauncher");
		Method start = launcherClass.getDeclaredMethod("start", new Class[] {
				Boolean.class, List.class });
		start.invoke(launcherClass.newInstance(), textMode, files);
	}

	/**
	 * Return the {@link Card} corresponding to the given URL, synchronised if
	 * necessary.
	 * 
	 * @param input
	 *            the jvcard:// with resource name URL (e.g.:
	 *            <tt>jvcard://localhost:4444/coworkers</tt>)
	 * 
	 * @throws SecurityException
	 *             in case of internal error
	 * @throws NoSuchMethodException
	 *             in case of internal error
	 * @throws ClassNotFoundException
	 *             in case of internal error
	 * @throws IllegalAccessException
	 *             in case of internal error
	 * @throws InstantiationException
	 *             in case of internal error
	 * @throws InvocationTargetException
	 *             in case of internal error
	 * @throws IllegalArgumentException
	 *             in case of internal error
	 * @throws IOException
	 *             in case of IO error
	 */
	@SuppressWarnings("unchecked")
	static private Card syncCard(String input) throws ClassNotFoundException,
			NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException {
		@SuppressWarnings("rawtypes")
		Class syncClass = Class.forName("be.nikiroo.jvcard.remote.Sync");
		Method sync = syncClass.getDeclaredMethod("sync",
				new Class[] { boolean.class });

		Object o = syncClass.getConstructor(String.class).newInstance(input);
		Card card = (Card) sync.invoke(o, false);

		return card;
	}

	/**
	 * Open the given path and add all its files if it is a directory or just
	 * this one if not to the returned list.
	 * 
	 * @param path
	 *            the path to open
	 * 
	 * @return the list of opened files
	 */
	static private List<String> open(String path) {
		List<String> files = new LinkedList<String>();

		if (path != null && path.startsWith("jvcard://")) {
			if (path.endsWith("/")) {
				files.addAll(list(path));
			} else {
				files.add(path);
			}
		} else {
			File file = new File(path);
			if (file.exists()) {
				if (file.isDirectory()) {
					for (File subfile : file.listFiles()) {
						if (!subfile.isDirectory())
							files.add(subfile.getAbsolutePath());
					}
				} else {
					files.add(file.getAbsolutePath());
				}
			} else {
				System.err.println("File or directory not found: \"" + path
						+ "\"");
			}
		}

		return files;
	}

	/**
	 * List all the available {@link Card}s on the given network location (which
	 * is expected to be a jVCard remote server, obviously).
	 * 
	 * @param path
	 *            the jVCard remote server path (e.g.:
	 *            <tt>jvcard://localhost:4444/</tt>)
	 * 
	 * @return the list of {@link Card}s
	 */
	static private List<String> list(String path) {
		List<String> files = new LinkedList<String>();

		try {
			String host = path.split("\\:")[1].substring(2);
			int port = Integer.parseInt(path.split("\\:")[2].replaceAll("/$",
					""));
			SimpleSocket s = new SimpleSocket(new Socket(host, port),
					"sync client");
			s.open(true);

			s.sendCommand(Command.LIST_CARD);
			for (String p : s.receiveBlock()) {
				files.add(path
						+ p.substring(StringUtils.fromTime(0).length() + 1));
			}
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return files;
	}

	/**
	 * Really, really ask for UTF-8 encoding.
	 */
	static private void utf8() {
		try {
			System.setProperty("file.encoding", "UTF-8");
			Field charset = Charset.class.getDeclaredField("defaultCharset");
			charset.setAccessible(true);
			charset.set(null, null);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}
}
