package be.nikiroo.jvcard.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.launcher.CardResult.MergeCallback;
import be.nikiroo.jvcard.launcher.Optional.NotSupportedException;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.remote.Command;
import be.nikiroo.jvcard.remote.SimpleSocket;
import be.nikiroo.jvcard.resources.DisplayBundle;
import be.nikiroo.jvcard.resources.DisplayOption;
import be.nikiroo.jvcard.resources.RemoteBundle;
import be.nikiroo.jvcard.resources.StringId;
import be.nikiroo.jvcard.resources.TransBundle;
import be.nikiroo.utils.ImageUtils;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.Version;
import be.nikiroo.utils.resources.Bundles;

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

	static private final int ERR_NO_FILE = 1;
	static private final int ERR_SYNTAX = 2;
	static private final int ERR_INTERNAL = 3;
	static private TransBundle transService;

	static private String defaultFn;
	static private boolean forceComputedFn;

	enum Mode {
		CONTACT_MANAGER, I18N, SERVER, LOAD_PHOTO, SAVE_PHOTO, SAVE_CONFIG, HELP
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
	static public String trans(StringId id, Object... values) {
		return transService.getString(id, values);
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
		transService = new TransBundle(language);

		boolean unicode = transService.isUnicode();
		String dir = null;
		List<String> files = new LinkedList<String>();
		int port = -1;
		Mode mode = Mode.CONTACT_MANAGER;
		String format = null;
		for (int index = 0; index < args.length; index++) {
			String arg = args[index];
			if (!noMoreParams && arg.equals("--")) {
				noMoreParams = true;
			} else if (!noMoreParams && arg.equals("--help")) {
				if (mode != Mode.CONTACT_MANAGER) {
					SERR(StringId.CLI_SERR_MODES);
					return;
				}
				mode = Mode.HELP;
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
					SERR(StringId.CLI_SERR_NOLANG);
					return;
				}

				language = args[index];
				transService = new TransBundle(language);
				transService.setUnicode(unicode);
			} else if (!noMoreParams && arg.equals("--config")) {
				index++;
				if (index >= args.length) {
					SERR(StringId.CLI_SERR_NODIR);
					return;
				}

				Bundles.setDirectory(args[index]);
				transService = new TransBundle(language);
				transService.setUnicode(unicode);
			} else if (!noMoreParams && arg.equals("--save-config")) {
				index++;
				if (index >= args.length) {
					SERR(StringId.CLI_SERR_NODIR);
					return;
				}
				dir = args[index];

				if (mode != Mode.CONTACT_MANAGER) {
					SERR(StringId.CLI_SERR_MODES);
					return;
				}
				mode = Mode.SAVE_CONFIG;
			} else if (!noMoreParams && arg.equals("--server")) {
				if (mode != Mode.CONTACT_MANAGER) {
					SERR(StringId.CLI_SERR_MODES);
					return;
				}
				mode = Mode.SERVER;

				index++;
				if (index >= args.length) {
					SERR(StringId.CLI_SERR_NOPORT);
					return;
				}

				try {
					port = Integer.parseInt(args[index]);
				} catch (NumberFormatException e) {
					SERR(StringId.CLI_SERR_BADPORT, "" + args[index]);
					return;
				}
			} else if (!noMoreParams && arg.equals("--i18n")) {
				if (mode != Mode.CONTACT_MANAGER) {
					SERR(StringId.CLI_SERR_MODES);
					return;
				}
				mode = Mode.I18N;

				index++;
				if (index >= args.length) {
					SERR(StringId.CLI_SERR_NODIR);
					return;
				}

				dir = args[index];
			} else if (!noMoreParams
					&& (arg.equals("--load-photo")
							|| arg.equals("--save-photo") || arg
								.equals("--only-photo"))) {
				if (mode != Mode.CONTACT_MANAGER) {
					SERR(StringId.CLI_SERR_MODES);
					return;
				}

				if (arg.equals("--load-photo")) {
					mode = Mode.LOAD_PHOTO;
				} else if (arg.equals("--save-photo")) {
					mode = Mode.SAVE_PHOTO;
				}

				index++;
				if (index >= args.length) {
					SERR(StringId.CLI_SERR_NODIR);
					return;
				}

				dir = args[index];

				index++;
				if (index >= args.length) {
					SERR(StringId.CLI_SERR_NOFORMAT);
					return;
				}

				format = args[index];
			} else {
				filesTried = true;
				files.addAll(open(arg));
			}
		}

		// Force headless mode if we run in forced-text mode
		if (mode != Mode.CONTACT_MANAGER || (textMode != null && textMode)) {
			// same as -Djava.awt.headless=true
			System.setProperty("java.awt.headless", "true");
		}

		if (unicode) {
			utf8();
		}

		// N/FN fix information:
		readNFN();

		// Error management:
		if (mode == Mode.SERVER && files.size() > 0) {
			SERR(StringId.CLI_SERR_NOLANG, "--server");
			return;
		} else if (mode == Mode.I18N && files.size() > 0) {
			SERR(StringId.CLI_SERR_NOLANG, "--i18n");
			return;
		} else if (mode == Mode.I18N && language == null) {
			SERR(StringId.CLI_SERR_NOLANG);
		} else if ((mode == Mode.CONTACT_MANAGER || mode == Mode.SAVE_PHOTO || mode == Mode.LOAD_PHOTO)
				&& files.size() == 0) {
			if (files.size() == 0 && !filesTried) {
				files.addAll(open("."));
			}

			if (files.size() == 0) {
				ERR(StringId.CLI_ERR, StringId.CLI_ERR_NOFILES, ERR_NO_FILE);
				return;
			}
		}
		//

		switch (mode) {
		case SAVE_CONFIG: {
			try {
				if (!new File(dir).isDirectory()) {
					if (!new File(dir).mkdir()) {
						System.err.println(trans(
								StringId.CLI_ERR_CANNOT_CREATE_CONFDIR, dir));
					}
				}

				new TransBundle().updateFile(dir); // default locale
				for (String lang : new TransBundle().getKnownLanguages()) {
					new TransBundle(lang).updateFile(dir);
				}

				// new UIColors().updateFile(dir);
				new DisplayBundle().updateFile(dir);
				new RemoteBundle().updateFile(dir);
			} catch (IOException e) {
				e.printStackTrace();
				System.err.flush();
				System.exit(ERR_INTERNAL);
			}
			break;
		}
		case SERVER: {
			try {
				Optional.runServer(port);
			} catch (IOException e) {
				ERR(StringId.CLI_ERR, StringId.CLI_ERR_CANNOT_START,
						ERR_INTERNAL);
				return;
			} catch (NotSupportedException e) {
				if (!e.isCompiledIn()) {
					ERR(StringId.CLI_ERR, StringId.CLI_ERR_NO_REMOTING,
							ERR_INTERNAL);
					return;
				} else {
					e.printStackTrace();
					ERR(StringId.CLI_ERR, StringId.CLI_ERR, ERR_INTERNAL);
					return;
				}
			}
			break;
		}
		case I18N: {
			try {
				transService.updateFile(dir);
			} catch (IOException e) {
				ERR(StringId.CLI_ERR, StringId.CLI_ERR_CANNOT_CREATE_LANG,
						ERR_INTERNAL);
				return;
			}
			break;
		}
		case LOAD_PHOTO: {
			for (String file : files) {
				try {
					Card card = getCard(file, null).getCard();
					for (Contact contact : card) {
						String filename = contact.toString(format, "");
						File f = new File(dir, filename);

						if (f.exists()) {
							System.out.println("Loading " + f);
							try {
								String type = "jpeg";
								int dotIndex = filename.indexOf('.');
								if (dotIndex >= 0
										&& (dotIndex + 1) < filename.length()) {
									type = filename.substring(dotIndex + 1)
											.toLowerCase();
								}

								String b64;
								InputStream in = null;
								try {
									in = new FileInputStream(f);
									b64 = ImageUtils.toBase64(in);
								} finally {
									if (in != null) {
										in.close();
									}
								}

								// remove previous photos:
								for (Data photo = contact
										.getPreferredData("PHOTO"); photo != null; photo = contact
										.getPreferredData("PHOTO")) {
									photo.delete();
								}
								//

								List<TypeInfo> types = new LinkedList<TypeInfo>();
								types.add(new TypeInfo("ENCODING", "b"));
								types.add(new TypeInfo("TYPE", type));
								Data photo = new Data(types, "PHOTO", b64, null);
								contact.add(photo);
							} catch (IOException e) {
								System.err.println("Cannot read photo: "
										+ filename);
							}
						}
					}
					card.save();
				} catch (IOException e) {
					System.err
							.println(trans(StringId.CLI_ERR_CANNOT_OPEN, file));
				}
			}
			break;
		}
		case SAVE_PHOTO: {
			for (String file : files) {
				try {
					Card card = getCard(file, null).getCard();
					for (Contact contact : card) {
						Data photo = contact.getPreferredData("PHOTO");
						if (photo != null) {
							String filename = contact.toString(format, "");
							File f = new File(dir, filename + ".png");
							System.out.println("Saving " + f);
							try {
								ImageIO.write(
										ImageUtils.fromBase64(photo.getValue()),
										"png", f);
							} catch (IOException e) {
								System.err.println(trans(
										StringId.CLI_ERR_CANNOT_SAVE_PHOTO,
										contact.getPreferredDataValue("FN")));
							}
						}
					}
				} catch (IOException e) {
					System.err
							.println(trans(StringId.CLI_ERR_CANNOT_OPEN, file));
				}
			}
			break;
		}
		case CONTACT_MANAGER: {
			try {
				Optional.startTui(textMode, files);
			} catch (IOException e) {
				ERR(StringId.CLI_ERR, StringId.CLI_ERR_CANNOT_START,
						ERR_NO_FILE);
				return;
			} catch (NotSupportedException e) {
				if (!e.isCompiledIn()) {
					ERR(StringId.CLI_ERR, StringId.CLI_ERR_NO_TUI, ERR_INTERNAL);
					return;
				} else {
					e.printStackTrace();
					ERR(StringId.CLI_ERR, StringId.CLI_ERR, ERR_INTERNAL);
					return;
				}
			}
			break;
		}
		case HELP: {
			System.out.println(APPLICATION_TITLE + " "
					+ Version.getCurrentVersion());
			System.out.println();

			System.out.println(trans(StringId.CLI_HELP));
			System.out.println();

			System.out.println(trans(StringId.CLI_HELP_MODES));
			System.out.println("\t--help : "
					+ trans(StringId.CLI_HELP_MODE_HELP));
			System.out.println("\t(--tui|--gui) (--noutf) ... : "
					+ trans(StringId.CLI_HELP_MODE_CONTACT_MANAGER));
			System.out.println("\t--server PORT ... : "
					+ trans(StringId.CLI_HELP_MODE_SERVER));
			System.out.println("\t--save-config DIR : "
					+ trans(StringId.CLI_HELP_MODE_SAVE_CONFIG));
			System.out.println("\t--i18n DIR ---lang LANG : "
					+ trans(StringId.CLI_HELP_MODE_I18N));
			System.out.println("\t--load-photo DIR FORMAT ... : "
					+ trans(StringId.CLI_HELP_MODE_LOAD_PHOTO));
			System.out.println("\t--save-photo DIR FORMAT ... : "
					+ trans(StringId.CLI_HELP_MODE_SAVE_PHOTO));
			System.out.println();

			System.out.println(trans(StringId.CLI_HELP_OPTIONS));
			System.out.println("\t-- : " + trans(StringId.CLI_HELP_DD));
			System.out.println("\t--lang LANG : "
					+ trans(StringId.CLI_HELP_LANG));
			System.out.println("\t--tui : " + trans(StringId.CLI_HELP_TUI));
			System.out.println("\t--gui : " + trans(StringId.CLI_HELP_GUI));
			System.out.println("\t--noutf : "
					+ trans(StringId.CLI_HELP_NOUTF_OPTION));
			System.out.println("\t--config : "
					+ trans(StringId.CLI_HELP_CONFIG));
			System.out.println();

			System.out.println(trans(StringId.CLI_HELP_FOOTER));
			System.out.println();

		}
		}
	}

	/**
	 * Return the {@link Card} corresponding to the given resource name -- a
	 * file or a remote jvcard URL.
	 * 
	 * <p>
	 * Will also fix the FN if required (see display.properties).
	 * </p>
	 * 
	 * @param input
	 *            a filename or a remote jvcard url with named resource (e.g.:
	 *            <tt>jvcard://localhost:4444/coworkers.vcf</tt>)
	 * @param callback
	 *            the {@link MergeCallback} to call in case of conflict, or NULL
	 *            to disallow conflict management (the {@link Card} will not be
	 *            allowed to synchronise in case of conflicts)
	 * 
	 * @return the {@link Card}
	 * 
	 * @throws IOException
	 *             in case of IO error or remoting not available
	 */
	static public CardResult getCard(String input, MergeCallback callback)
			throws IOException {
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

		CardResult card = null;
		try {
			if (remote) {
				card = Optional.syncCard(input, callback);
			} else {
				card = new CardResult(new Card(new File(input), format), false,
						false, false);
			}
		} catch (IOException ioe) {
			throw ioe;
		} catch (NotSupportedException e) {
			throw new IOException("Remoting support not available", e);
		}

		// Fix the FN value
		if (defaultFn != null) {
			try {
				for (Contact contact : card.getCard()) {
					Data name = contact.getPreferredData("FN");
					if (name == null || name.getValue().length() == 0
							|| forceComputedFn) {
						name.setValue(contact.toString(defaultFn, "").trim());
					}
				}
			} catch (Exception e) {
				// sync failed -> getCard() throws.
				// do not update.
			}
		}

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

	/**
	 * Read display.properties to know if we should fix the FN field when empty,
	 * or always, or never.
	 */
	static private void readNFN() {
		DisplayBundle map = new DisplayBundle();

		defaultFn = map.getString(DisplayOption.CONTACT_DETAILS_DEFAULT_FN);

		forceComputedFn = map.getBoolean(
				DisplayOption.CONTACT_DETAILS_SHOW_COMPUTED_FN, false);
	}

	/**
	 * Syntax error detected, closing the application with an error message.
	 * 
	 * @param err
	 *            the syntax error case
	 */
	static private void SERR(StringId err, Object... values) {
		ERR(StringId.CLI_SERR, err, ERR_SYNTAX, values);
	}

	/**
	 * Error detected, closing the application with an error message.
	 * 
	 * @param err
	 *            the error case
	 * @param suberr
	 *            the suberror or NULL if none
	 * @param CODE
	 *            the error code as declared above
	 */
	static private void ERR(StringId err, StringId suberr, int CODE,
			Object... subvalues) {
		if (suberr == null)
			System.err.println(trans(err));
		else
			System.err.println(trans(err, trans(suberr, subvalues)));

		System.err.flush();
		System.exit(CODE);
	}
}
