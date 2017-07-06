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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.launcher.CardResult;
import be.nikiroo.jvcard.launcher.CardResult.MergeCallback;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Vcard21Parser;
import be.nikiroo.jvcard.remote.SimpleSocket.BlockAppendable;
import be.nikiroo.jvcard.resources.RemoteBundle;
import be.nikiroo.jvcard.resources.RemotingOption;
import be.nikiroo.utils.StringUtils;

/**
 * This class will synchronise {@link Card}s between a local instance an a
 * remote jVCard server.
 * 
 * @author niki
 * 
 */
public class Sync {
	/** The time in ms after which we declare that 2 timestamps are different */
	static private final int GRACE_TIME = 2001;

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
	 * Check if the remote server already know about this resource.
	 * 
	 * @return TRUE if it is possible to contact the remote server and that this
	 *         server has the resource available
	 */
	public boolean isAvailable() {
		try {
			SimpleSocket s = new SimpleSocket(new Socket(host, port),
					"check avail client");
			s.open(true);
			s.sendCommand(Command.LIST_CARD);
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

	/**
	 * Synchronise the current resource if needed, then return the locally
	 * cached version of said resource.
	 * 
	 * <p>
	 * A synchronisation is deemed necessary if one of the following is true:
	 * <ul>
	 * <li><tt>force</tt> is TRUE</li>
	 * <li><tt>CLIENT_AUTO_SYNC</tt> is TRUE in the configuration file</li>
	 * <li>the {@link Card} exists locally but not on the remote server</li>
	 * <li>the {@link Card} exists on the remote server but not locally</li>
	 * </ul>
	 * </p>
	 * 
	 * @param force
	 *            force the synchronisation to occur
	 * @param callback
	 *            the {@link MergeCallback} to call in case of conflict
	 * 
	 * @return the synchronised (or not) {@link Card}
	 * 
	 * @throws UnknownHostException
	 *             in case of server name resolution failure
	 * @throws IOException
	 *             in case of IO error
	 */
	public CardResult sync(boolean force, MergeCallback callback)
			throws UnknownHostException, IOException {
		long tsOriginal = getLastModified();

		Card local = new Card(getCache(cacheDir), Format.VCard21);

		// do NOT update unless we are in autoSync or forced mode or we don't
		// have the file on cache
		if (!autoSync && !force && tsOriginal != -1) {
			return new CardResult(local, true, false, false);
		}

		SimpleSocket s = new SimpleSocket(new Socket(host, port), "sync client");

		// get the server time stamp
		long tsServer = -1;
		boolean serverChanges = false;
		try {
			s.open(true);
			s.sendCommand(Command.LIST_CARD);
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
			// - file not present neither in cache nor on server
			// - remote < previous
			if ((tsServer == -1 && tsOriginal == -1)
					|| (tsServer != -1 && tsOriginal != -1 && ((tsOriginal - tsServer) > GRACE_TIME))) {
				throw new IOException(
						"The timestamps between server and client are invalid");
			}

			// Check changes
			serverChanges = (tsServer - tsOriginal) > GRACE_TIME;
			boolean localChanges = false;
			Card original = null;
			if (tsOriginal != -1) {
				original = new Card(getCache(cacheDirOrig), Format.VCard21);
				localChanges = !local.isEquals(original, true);
			}

			Command action = null;

			// Sync to server if:
			if (localChanges) {
				action = Command.PUT_CARD;
			}

			// Sync from server if:
			if (serverChanges) {
				action = Command.HASH_CONTACT;
			}

			// Sync from/to server if
			if (serverChanges && localChanges) {
				action = Command.HELP;
			}

			// POST the whole file if:
			if (tsServer == -1) {
				action = Command.POST_CARD;
			}

			// GET the whole file if:
			if (tsOriginal == -1) {
				action = Command.GET_CARD;
			}

			System.err.println("remote: " + (tsServer / 1000) % 1000 + " ("
					+ tsServer + ")");
			System.err.println("previous: " + (tsOriginal / 1000) % 1000 + " ("
					+ tsOriginal + ")");
			System.err.println("local changes: " + localChanges);
			System.err.println("server changes: " + serverChanges);
			System.err.println("choosen action: " + action);

			if (action != null) {
				s.sendCommand(Command.SELECT, name);
				if (tsServer != StringUtils.toTime(s.receiveLine())) {
					System.err.println("DEBUG: it changed. retry.");
					s.sendCommand(Command.SELECT);
					s.close();
					return sync(force, callback);
				}

				switch (action) {
				case GET_CARD: {
					s.sendCommand(Command.GET_CARD);
					List<String> data = s.receiveBlock();
					setLastModified(data.remove(0));
					local.replaceListContent(Vcard21Parser.parseContact(data));

					if (local.isDirty())
						local.save();
					local.saveAs(getCache(cacheDirOrig), Format.VCard21);
					break;
				}
				case POST_CARD: {
					s.sendCommand(Command.POST_CARD);
					BlockAppendable app = s.createBlockAppendable();
					Vcard21Parser.write(app, local);
					app.close();
					local.saveAs(getCache(cacheDirOrig), Format.VCard21);
					setLastModified(s.receiveLine());
					break;
				}
				case PUT_CARD: {
					String serverLastModifTime = updateToServer(s, original,
							local);

					local.saveAs(getCache(cacheDirOrig), Format.VCard21);

					setLastModified(serverLastModifTime);
					break;
				}
				case HASH_CONTACT: {
					String serverLastModifTime = updateFromServer(s, local);

					local.save();
					local.saveAs(getCache(cacheDirOrig), Format.VCard21);

					setLastModified(serverLastModifTime);
					break;
				}
				case HELP: {
					// note: we are holding the server here, so it could throw
					// us away if we take too long

					// TODO: check if those files are deleted
					File mergeF = File.createTempFile("contact-merge", ".vcf");
					File serverF = File
							.createTempFile("contact-server", ".vcf");
					original.saveAs(serverF, Format.VCard21);

					Card server = new Card(serverF, Format.VCard21);
					updateFromServer(s, server);

					// Do an auto sync
					server.saveAs(mergeF, Format.VCard21);
					Card merge = new Card(mergeF, Format.VCard21);
					List<Contact> added = new LinkedList<Contact>();
					List<Contact> removed = new LinkedList<Contact>();
					original.compare(local, added, removed, removed, added);
					for (Contact c : removed)
						merge.getById(c.getId()).delete();
					for (Contact c : added)
						merge.add(Vcard21Parser.clone(c));

					merge.save();

					// defer to client:
					if (callback == null) {
						throw new IOException(
								"Conflicting changes detected and merge operation not allowed");
					}

					merge = callback.merge(original, local, server, merge);
					if (merge == null) {
						throw new IOException(
								"Conflicting changes detected and merge operation cancelled");
					}

					// TODO: something like:
					// String serverLastModifTime = updateToServer(s, original,
					// merge);
					// ...but without starting with original since it is not
					// true here
					s.sendCommand(Command.POST_CARD);
					BlockAppendable app = s.createBlockAppendable();
					Vcard21Parser.write(app, merge);
					app.close();
					String serverLastModifTime = s.receiveLine();
					//

					merge.saveAs(getCache(cacheDir), Format.VCard21);
					merge.saveAs(getCache(cacheDirOrig), Format.VCard21);

					setLastModified(serverLastModifTime);

					local = merge;

					break;
				}
				default:
					// will not happen
					break;
				}

				s.sendCommand(Command.SELECT);
			}
		} catch (IOException e) {
			return new CardResult(e);
		} catch (Exception e) {
			return new CardResult(new IOException(e));
		} finally {
			s.close();
		}

		return new CardResult(local, true, true, serverChanges);
	}

	/**
	 * Will update the currently selected {@link Card} on the remote server to
	 * be in the same state as <tt>local</tt>, assuming the server is currently
	 * in <tt>original</tt> state.
	 * 
	 * @param s
	 *            the {@link SimpleSocket} to work on, which <b>MUST</b> be in
	 *            SELECT mode
	 * @param original
	 *            the original {@link Card} as it was before the client made
	 *            changes to it
	 * @param local
	 *            the {@link Card} to which state we want the server in
	 * 
	 * @return the last modified time from the remote server (which is basically
	 *         "now")
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	private String updateToServer(SimpleSocket s, Card original, Card local)
			throws IOException {
		List<Contact> added = new LinkedList<Contact>();
		List<Contact> removed = new LinkedList<Contact>();
		List<Contact> from = new LinkedList<Contact>();
		List<Contact> to = new LinkedList<Contact>();
		original.compare(local, added, removed, from, to);

		s.sendCommand(Command.PUT_CARD);

		for (Contact c : removed) {
			s.sendCommand(Command.DELETE_CONTACT, c.getId());
		}
		for (Contact c : added) {
			s.sendCommand(Command.POST_CONTACT, c.getId());
			BlockAppendable app = s.createBlockAppendable();
			Vcard21Parser.write(app, c, -1);
			s.close();
		}
		if (from.size() > 0) {
			for (int index = 0; index < from.size(); index++) {
				Contact f = from.get(index);
				Contact t = to.get(index);

				List<Data> subadded = new LinkedList<Data>();
				List<Data> subremoved = new LinkedList<Data>();
				f.compare(t, subadded, subremoved, subremoved, subadded);
				s.sendCommand(Command.PUT_CONTACT, f.getId());
				for (Data d : subremoved) {
					s.sendCommand(Command.DELETE_DATA, d.getContentState(true));
				}
				for (Data d : subadded) {
					s.sendCommand(Command.POST_DATA, d.getContentState(true));
					BlockAppendable app = s.createBlockAppendable();
					Vcard21Parser.write(app, d);
					app.close();
				}
				s.sendCommand(Command.PUT_CONTACT);
			}
		}

		s.sendCommand(Command.PUT_CARD);

		return s.receiveLine();
	}

	/**
	 * Will update the given {@link Card} object (not {@link File}) to the
	 * currently selected {@link Card} on the remote server.
	 * 
	 * @param s
	 *            the {@link SimpleSocket} to work on, which <b>MUST</b> be in
	 *            SELECT mode
	 * @param local
	 *            the {@link Card} to update
	 * 
	 * @return the last modified time from the remote server
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	private String updateFromServer(SimpleSocket s, Card local)
			throws IOException {
		s.sendCommand(Command.PUT_CARD);

		s.sendCommand(Command.LIST_CONTACT);
		Map<String, String> remote = new HashMap<String, String>();
		for (String line : s.receiveBlock()) {
			int indexSp = line.indexOf(" ");
			String hash = line.substring(0, indexSp);
			String uid = line.substring(indexSp + 1);

			remote.put(uid, hash);
		}

		List<Contact> deleted = new LinkedList<Contact>();
		List<Contact> changed = new LinkedList<Contact>();
		List<String> added = new LinkedList<String>();

		for (Contact c : local) {
			String hash = remote.get(c.getId());
			if (hash == null) {
				deleted.add(c);
			} else if (!hash.equals(c.getContentState(true))) {
				changed.add(c);
			}
		}

		for (String uid : remote.keySet()) {
			if (local.getById(uid) == null)
				added.add(uid);
		}

		// process:

		for (Contact c : deleted) {
			c.delete();
		}

		for (String uid : added) {
			s.sendCommand(Command.GET_CONTACT, uid);
			for (Contact cc : Vcard21Parser.parseContact(s.receiveBlock())) {
				local.add(cc);
			}
		}

		for (Contact c : changed) {
			c.delete();
			s.sendCommand(Command.GET_CONTACT, c.getId());
			for (Contact cc : Vcard21Parser.parseContact(s.receiveBlock())) {
				local.add(cc);
			}
		}

		s.sendCommand(Command.PUT_CARD);

		return s.receiveLine();
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
		RemoteBundle bundle = new RemoteBundle();

		try {
			dir = bundle.getString(RemotingOption.CLIENT_CACHE_DIR);

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

			autoSync = bundle
					.getBoolean(RemotingOption.CLIENT_AUTO_SYNC, false);
		} catch (Exception e) {
			throw new InvalidParameterException(
					"Cannot open or create cache store at: " + dir);
		}
	}
}
