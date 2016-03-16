package be.nikiroo.jvcard.remote;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Vcard21Parser;
import be.nikiroo.jvcard.remote.Command.Verb;
import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.StringUtils;

/**
 * This class will synchronise {@link Card}s between a local instance an a
 * remote jVCard server.
 * 
 * @author niki
 *
 */
public class Sync {
	/** The time in ms after which we declare that 2 timestamps are different */
	static private final int GRACE_TIME = 2000;

	/** Directory where to store local cache of remote {@link Card}s. */
	static private File cacheDir;

	/**
	 * Directory where to store cache of remote {@link Card}s without
	 * modifications since the last synchronisation.
	 */
	static private File cacheDirOrig;
	/** Directory where to store timestamps for files in cacheDirOrig */
	static private File cacheDirOrigTS;

	static private boolean autoSync;
	private String host;
	private int port;

	/** Resource name on the remote server. */
	private String name;

	/**
	 * Create a new {@link Sync} object, ready to operate for the given resource
	 * on the given server.
	 * 
	 * <p>
	 * Note that the format used is the standard "host:port_number/file", with
	 * an optional <tt>jvcard://</tt> prefix.
	 * </p>
	 * 
	 * <p>
	 * E.g.: <tt>jvcard://localhost:4444/family.vcf</tt>
	 * </p>
	 * 
	 * @param url
	 *            the server and port to contact, optionally prefixed with
	 *            <tt>jvcard://</tt>
	 * 
	 * @throws InvalidParameterException
	 *             if the remote configuration file <tt>remote.properties</tt>
	 *             cannot be accessed or if the cache directory cannot be used
	 */
	public Sync(String url) {
		if (cacheDir == null) {
			config();
		}

		try {
			url = url.replace("jvcard://", "");
			int indexSl = url.indexOf('/');
			this.name = url.substring(indexSl + 1);
			url = url.substring(0, indexSl);
			this.host = url.split("\\:")[0];
			this.port = Integer.parseInt(url.split("\\:")[1]);
		} catch (Exception e) {
			throw new InvalidParameterException(
					"the given parameter was not a valid HOST:PORT value: "
							+ url);
		}
	}

	/**
	 * Create a new {@link Sync} object, ready to operate on the given server.
	 * 
	 * 
	 * @param host
	 *            the server to contact
	 * @param port
	 *            the port to use
	 * @param name
	 *            the resource name to synchronise to
	 */
	public Sync(String host, int port, String name) {
		this.host = host;
		this.port = port;
		this.name = name;
	}

	/**
	 * Check if the synchronisation is available for this resource.
	 * 
	 * @return TRUE if it is possible to contact the remote server and that this
	 *         server has the resource available
	 */
	public boolean isAvailable() {
		try {
			SimpleSocket s = new SimpleSocket(new Socket(host, port),
					"check avail client");
			s.open(true);
			s.sendCommand(Verb.LIST);
			List<String> timestampedFiles = s.receiveBlock();
			s.close();

			for (String timestampedFile : timestampedFiles) {
				String file = timestampedFile.substring(StringUtils.fromTime(0)
						.length() + 1);
				if (file.equals(name)) {
					return true;
				}
			}
		} catch (Exception e) {
		}

		return false;
	}

	// return: synced or not
	// TODO jDoc
	public boolean sync(Card card, boolean force) throws UnknownHostException,
			IOException {

		long tsOriginal = getLastModified();

		// do NOT update unless we are in autoSync or forced mode or we don't
		// have the file on cache
		if (!autoSync && !force && tsOriginal != -1) {
			return false;
		}

		SimpleSocket s = new SimpleSocket(new Socket(host, port), "sync client");

		// get the server time stamp
		long tsServer = -1;
		try {
			s.open(true);
			s.sendCommand(Verb.LIST);
			List<String> timestampedFiles = s.receiveBlock();

			for (String timestampedFile : timestampedFiles) {
				String file = timestampedFile.substring(StringUtils.fromTime(0)
						.length() + 1);
				if (file.equals(name)) {
					tsServer = StringUtils.toTime(timestampedFile.substring(0,
							StringUtils.fromTime(0).length()));
					break;
				}
			}

			// Error cases:
			// - file not preset neither in cache nor on server
			// - remote < previous
			if ((tsServer == -1 && tsOriginal == -1)
					|| (tsServer != -1 && tsOriginal != -1 && ((tsOriginal - tsServer) > GRACE_TIME))) {
				throw new IOException(
						"The timestamps between server and client are invalid");
			}

			// Check changes
			boolean serverChanges = (tsServer - tsOriginal) > GRACE_TIME;
			boolean localChanges = false;
			Card local = null;
			Card original = null;
			if (tsOriginal != -1) {
				local = new Card(getCache(cacheDir), Format.VCard21);
				original = new Card(getCache(cacheDirOrig), Format.VCard21);
				localChanges = !local.isEquals(original, true);
			}

			Verb action = null;

			// Sync to server if:
			if (localChanges) {
				action = Verb.PUT_CARD;
			}

			// Sync from server if:
			if (serverChanges) {
				// TODO: only sends changed cstate if serverChanges
				action = Verb.GET_CARD;
			}

			// Sync from/to server if
			if (serverChanges && localChanges) {
				// TODO
				action = Verb.HELP;
			}

			// PUT the whole file if:
			if (tsServer == -1) {
				action = Verb.POST_CARD;
			}

			// GET the whole file if:
			if (tsOriginal == -1) {
				action = Verb.GET_CARD;
			}

			System.err.println("remote: " + (tsServer / 1000) % 1000 + " ("
					+ tsServer + ")");
			System.err.println("previous: " + (tsOriginal / 1000) % 1000 + " ("
					+ tsOriginal + ")");
			System.err.println("local changes: " + localChanges);
			System.err.println("server changes: " + serverChanges);
			System.err.println("choosen action: " + action);

			if (action != null) {

				s.sendCommand(Verb.SELECT, name);
				if (tsServer != StringUtils.toTime(s.receiveLine())) {
					System.err.println("DEBUG: it changed. retry.");
					s.sendCommand(Verb.SELECT);
					s.close();
					return sync(card, force);
				}

				switch (action) {
				case GET_CARD:
					s.sendCommand(Verb.GET_CARD);
					List<String> data = s.receiveBlock();
					setLastModified(data.remove(0));
					Card server = new Card(Vcard21Parser.parseContact(data));
					card.replaceListContent(server);

					if (card.isDirty())
						card.save();
					card.saveAs(getCache(cacheDirOrig), Format.VCard21);
					break;
				case POST_CARD:
					s.sendCommand(Verb.POST_CARD);
					s.sendBlock(Vcard21Parser.toStrings(card));
					card.saveAs(getCache(cacheDirOrig), Format.VCard21);
					setLastModified(s.receiveLine());
					break;
				case PUT_CARD:
					List<Contact> added = new LinkedList<Contact>();
					List<Contact> removed = new LinkedList<Contact>();
					List<Contact> from = new LinkedList<Contact>();
					List<Contact> to = new LinkedList<Contact>();
					original.compare(local, added, removed, from, to);

					s.sendCommand(Verb.PUT_CARD);

					for (Contact c : removed) {
						s.sendCommand(Verb.DELETE_CONTACT, c.getId());
					}
					for (Contact c : added) {
						s.sendCommand(Verb.POST_CONTACT, c.getId());
						s.sendBlock(Vcard21Parser.toStrings(c, -1));
					}
					if (from.size() > 0) {
						for (int index = 0; index < from.size(); index++) {
							Contact f = from.get(index);
							Contact t = to.get(index);

							List<Data> subadded = new LinkedList<Data>();
							List<Data> subremoved = new LinkedList<Data>();
							f.compare(t, subadded, subremoved, subremoved,
									subadded);
							s.sendCommand(Verb.PUT_CONTACT, name);
							for (Data d : subremoved) {
								s.sendCommand(Verb.DELETE_DATA,
										d.getContentState());
							}
							for (Data d : subadded) {
								s.sendCommand(Verb.POST_DATA,
										d.getContentState());
								s.sendBlock(Vcard21Parser.toStrings(d));
							}
						}
					}

					s.sendCommand(Verb.PUT_CARD);
					break;
				default:
					// TODO
					throw new IOException(action
							+ " operation not supported yet :(");
				}

				s.sendCommand(Verb.SELECT);
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			s.close();
		}

		return true;
	}

	/**
	 * Return the requested cache for the current resource.
	 * 
	 * @param dir
	 *            the cache to use
	 *
	 * @return the cached {@link File}
	 */
	private File getCache(File dir) {
		return new File(dir.getPath() + File.separator + name);
	}

	/**
	 * Return the cached {@link File} corresponding to the current resource.
	 * 
	 * @return the cached {@link File}
	 */
	public File getCache() {
		return new File(cacheDir.getPath() + File.separator + name);
	}

	/**
	 * Get the last modified date of the current resource's original cached
	 * file, that is, the time the server reported as the "last modified time"
	 * when this resource was transfered.
	 * 
	 * @return the last modified time from the server back when this resource
	 *         was transfered
	 */
	public long getLastModified() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(cacheDirOrigTS.getPath()
							+ File.separator + name)));
			String line = in.readLine();
			in.close();

			return StringUtils.toTime(line);
		} catch (FileNotFoundException e) {
			return -1;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Set the last modified date of the current resource's original cached
	 * file, that is, the time the server reported as the "last modified time"
	 * when this resource was transfered.
	 * 
	 * @param time
	 *            the last modified time from the server back when this resource
	 *            was transfered
	 */
	public void setLastModified(String time) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(cacheDirOrigTS.getPath()
							+ File.separator + name)));
			out.append(time);
			out.newLine();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the synchronisation mechanism (cache, auto update...).
	 * 
	 * @throws InvalidParameterException
	 *             if the remote configuration file <tt>remote.properties</tt>
	 *             cannot be accessed or if the cache directory cannot be used
	 */
	static private void config() {
		String dir = null;
		ResourceBundle bundle = Bundles.getBundle("remote");

		try {
			dir = bundle.getString("CLIENT_CACHE_DIR").trim();

			cacheDir = new File(dir + File.separator + "current");
			cacheDir.mkdir();
			cacheDirOrig = new File(dir + File.separator + "original");
			cacheDirOrig.mkdir();
			cacheDirOrigTS = new File(dir + File.separator + "timestamps");
			cacheDirOrigTS.mkdir();

			if (!cacheDir.exists() || !cacheDirOrig.exists()) {
				throw new IOException("Cannot open or create cache store at: "
						+ dir);
			}

			String autoStr = bundle.getString("CLIENT_AUTO_SYNC");
			if (autoStr != null && autoStr.trim().equalsIgnoreCase("true")) {
				autoSync = true;
			}

		} catch (MissingResourceException e) {
			throw new InvalidParameterException(
					"Cannot access remote.properties configuration file");
		} catch (Exception e) {
			throw new InvalidParameterException(
					"Cannot open or create cache store at: " + dir);
		}
	}
}
