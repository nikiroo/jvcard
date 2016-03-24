package be.nikiroo.jvcard.remote;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Vcard21Parser;
import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.StringUtils;

/**
 * This class implements a small server that can listen for requests to
 * synchronise, get and put {@link Card}s.
 * 
 * <p>
 * It is <b>NOT</b> secured in any way (it even is nice enough to give you a
 * help message when you connect in raw mode via nc on how to use it), so do
 * <b>NOT</b> enable such a server to be accessible from internet. This is not
 * safe. Use a ssh/openssl tunnel or similar.
 * </p>
 * 
 * @author niki
 *
 */
public class Server implements Runnable {
	private ServerSocket ss;
	private int port;
	private boolean stop;
	private File dataDir;

	private Object clientsLock = new Object();
	private List<SimpleSocket> clients = new LinkedList<SimpleSocket>();

	private Object updateLock = new Object();
	private Map<File, Integer> updates = new HashMap<File, Integer>();

	/**
	 * Create a new jVCard server on the given port.
	 * 
	 * @param port
	 *            the port to run on
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public Server(int port) throws IOException {
		this.port = port;
		ResourceBundle bundle = Bundles.getBundle("remote");
		try {
			String dir = bundle.getString("SERVER_DATA_PATH");
			dataDir = new File(dir);
			dataDir.mkdir();

			if (!dataDir.exists()) {
				throw new IOException("Cannot open or create data store at: "
						+ dataDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Cannot open or create data store at: "
					+ dataDir, e);
		}

		ss = new ServerSocket(port);
	}

	/**
	 * Stop the server. It may take some time before returning, but will only
	 * return when the server is actually stopped.
	 */
	public void stop() {
		stop = true;
		try {
			SimpleSocket c = new SimpleSocket(new Socket((String) null, port),
					"special STOP client");
			c.open(true);
			c.sendCommand(Command.STOP);
			c.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (clients.size() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			if (clients.size() > 0) {
				synchronized (clientsLock) {
					for (SimpleSocket s : clients) {
						System.err
								.println("Forcefully closing client connection");
						s.close();
					}

					clients.clear();
				}
			}
		}
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				final Socket s = ss.accept();
				// TODO: thread pool?
				new Thread(new Runnable() {
					@Override
					public void run() {
						SimpleSocket ss = new SimpleSocket(s, "[request]");

						addClient(ss);
						try {
							ss.open(false);

							while (processCmd(ss))
								;

						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							ss.close();
						}
						removeClient(ss);
					}
				}).start();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Add a client to the current count.
	 * 
	 * @return the client index number
	 */
	private void addClient(SimpleSocket s) {
		synchronized (clientsLock) {
			clients.add(s);
		}
	}

	/**
	 * Remove a client from the current count.
	 * 
	 * @param index
	 *            the client index number
	 */
	private void removeClient(SimpleSocket s) {
		synchronized (clientsLock) {
			clients.remove(s);
		}
	}

	/**
	 * Process a first-level command.
	 * 
	 * @param s
	 *            the {@link SimpleSocket} from which to get the command to
	 *            process
	 * 
	 * @return TRUE if the client is ready for another command, FALSE when the
	 *         client exited
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	private boolean processCmd(SimpleSocket s) throws IOException {
		CommandInstance cmd = s.receiveCommand();
		Command command = cmd.getCommand();

		if (command == null)
			return false;

		boolean clientContinue = true;

		System.out.println(s + " ->  " + command
				+ (cmd.getParam() == null ? "" : " " + cmd.getParam()));

		switch (command) {
		case STOP: {
			clientContinue = false;
			break;
		}
		case VERSION: {
			s.sendLine("" + SimpleSocket.CURRENT_VERSION);
			break;
		}
		case TIME: {
			s.sendLine(StringUtils.fromTime(new Date().getTime()));
			break;
		}
		case SELECT: {
			String name = cmd.getParam();
			File file = new File(dataDir.getAbsolutePath() + File.separator
					+ name);
			if (name == null || name.length() == 0 || !file.exists()) {
				System.err
						.println("SELECT: resource not found, closing connection: "
								+ name);
				clientContinue = false;
			} else {
				synchronized (updateLock) {
					for (File f : updates.keySet()) {
						if (f.getCanonicalPath()
								.equals(file.getCanonicalPath())) {
							file = f;
							break;
						}
					}

					if (!updates.containsKey(file))
						updates.put(file, 0);
					updates.put(file, updates.get(file) + 1);
				}

				synchronized (file) {
					try {
						s.sendLine(StringUtils.fromTime(file.lastModified()));

						while (processLockedCmd(s, name))
							;
					} catch (InvalidParameterException e) {
						System.err
								.println("Unsupported command received from a client connection, closing it: "
										+ command + " (" + e.getMessage() + ")");
						clientContinue = false;
					}
				}

				synchronized (updateLock) {
					int num = updates.get(file) - 1;
					if (num == 0) {
						updates.remove(file);
					} else {
						updates.put(file, num);
					}
				}
			}
			break;
		}
		case LIST_CARD: {
			for (File file : dataDir.listFiles()) {
				if (cmd.getParam() == null
						|| cmd.getParam().length() == 0
						|| file.getName().toLowerCase()
								.contains(cmd.getParam().toLowerCase())) {
					s.send(StringUtils.fromTime(file.lastModified()) + " "
							+ file.getName());
				}
			}
			s.sendBlock();
			break;
		}
		case HELP: {
			// TODO: i18n
			s.send("The following commands are available:");
			s.send("- TIME: get the server time");
			s.send("- HELP: this help screen");
			s.send("- LIST_CARD: list the available cards on this server");
			s.send("- VERSION/GET_*/PUT_*/POST_*/DELETE_*/STOP: TODO");
			s.sendBlock();
			break;
		}
		default: {
			System.err
					.println("Unsupported command received from a client connection, closing it: "
							+ command);
			clientContinue = false;
			break;
		}
		}

		return clientContinue;
	}

	/**
	 * Process a subcommand while protected for resource <tt>name</tt>.
	 * 
	 * @param s
	 *            the {@link SimpleSocket} to process
	 * 
	 * @param name
	 *            the resource that is protected (and to target)
	 * 
	 * @return TRUE if the client is ready for another command, FALSE when the
	 *         client is done
	 * 
	 * @throws IOException
	 *             in case of IO error
	 * 
	 * @throw InvalidParameterException in case of invalid subcommand
	 */
	private boolean processLockedCmd(SimpleSocket s, String name)
			throws IOException {
		CommandInstance cmd = s.receiveCommand();
		Command command = cmd.getCommand();

		if (command == null)
			return false;

		boolean clientContinue = true;

		System.out.println(s + " ->  " + command);

		switch (command) {
		case GET_CARD: {
			s.sendBlock(doGetCard(name));
			break;
		}
		case POST_CARD: {
			s.sendLine(doPostCard(name, s.receiveBlock()));
			break;
		}
		case PUT_CARD: {
			File vcf = getFile(name);
			if (vcf == null) {
				System.err
						.println("Fail to update a card, file not available: "
								+ name);
				clientContinue = false;
			} else {
				Card card = new Card(vcf, Format.VCard21);
				try {
					while (processContactCmd(s, card))
						;
					card.save();
					s.sendLine(StringUtils.fromTime(card.getLastModified()));
				} catch (InvalidParameterException e) {
					System.err
							.println("Unsupported command received from a client connection, closing it: "
									+ command + " (" + e.getMessage() + ")");
					clientContinue = false;
				}
			}
			break;
		}
		case DELETE_CARD: {
			// TODO
			System.err
					.println("Unsupported command received from a client connection, closing it: "
							+ command);
			clientContinue = false;
			break;
		}
		case SELECT: {
			clientContinue = false;
			break;
		}
		default: {
			throw new InvalidParameterException("command invalid here: "
					+ command);
		}
		}

		return clientContinue;
	}

	/**
	 * Process a *_CONTACT subcommand.
	 * 
	 * @param s
	 *            the {@link SimpleSocket} to process
	 * @param card
	 *            the target {@link Card}
	 * 
	 * @return TRUE if the client is ready for another command, FALSE when the
	 *         client is done
	 * 
	 * @throws IOException
	 *             in case of IO error
	 * 
	 * @throw InvalidParameterException in case of invalid subcommand
	 */
	private boolean processContactCmd(SimpleSocket s, Card card)
			throws IOException {
		CommandInstance cmd = s.receiveCommand();
		Command command = cmd.getCommand();

		if (command == null)
			return false;

		boolean clientContinue = true;

		System.out.println(s + " ->  " + command);

		switch (command) {
		case GET_CONTACT: {
			Contact contact = card.getById(cmd.getParam());
			if (contact != null)
				s.sendBlock(Vcard21Parser.toStrings(contact, -1));
			else
				s.sendBlock();
			break;
		}
		case POST_CONTACT: {
			List<Contact> list = Vcard21Parser.parseContact(s.receiveBlock());
			if (list.size() > 0) {
				Contact newContact = list.get(0);
				String uid = newContact.getPreferredDataValue("UID");
				Contact oldContact = card.getById(uid);
				if (oldContact != null)
					oldContact.delete();
				card.add(newContact);
			}

			break;
		}
		case PUT_CONTACT: {
			String uid = cmd.getParam();
			Contact contact = card.getById(uid);
			if (contact == null) {
				throw new InvalidParameterException(
						"Cannot find contact to modify for UID: " + uid);
			}
			while (processDataCmd(s, contact))
				;
			break;
		}
		case DELETE_CONTACT: {
			String uid = cmd.getParam();
			Contact contact = card.getById(uid);
			if (contact == null) {
				throw new InvalidParameterException(
						"Cannot find contact to delete for UID: " + uid);
			}

			contact.delete();
			break;
		}
		case HASH_CONTACT: {
			String uid = cmd.getParam();
			Contact contact = card.getById(uid);

			if (contact == null) {
				s.sendBlock();
			} else {
				s.sendLine(contact.getContentState(true));
			}
			break;
		}
		case LIST_CONTACT: {
			for (Contact contact : card) {
				if (cmd.getParam() == null
						|| cmd.getParam().length() == 0
						|| (contact.getPreferredDataValue("FN") + contact
								.getPreferredDataValue("N")).toLowerCase()
								.contains(cmd.getParam().toLowerCase())) {
					s.send(contact.getContentState(true) + " "
							+ contact.getId());
				}
			}
			s.sendBlock();
			break;
		}
		case PUT_CARD: {
			clientContinue = false;
			break;
		}
		default: {
			throw new InvalidParameterException("command invalid here: "
					+ command);
		}
		}

		return clientContinue;
	}

	/**
	 * Process a *_DATA subcommand.
	 * 
	 * @param s
	 *            the {@link SimpleSocket} to process
	 * @param card
	 *            the target {@link Contact}
	 * 
	 * @return TRUE if the client is ready for another command, FALSE when the
	 *         client is done
	 * 
	 * @throws IOException
	 *             in case of IO error
	 * 
	 * @throw InvalidParameterException in case of invalid subcommand
	 */
	private boolean processDataCmd(SimpleSocket s, Contact contact)
			throws IOException {
		CommandInstance cmd = s.receiveCommand();
		Command command = cmd.getCommand();

		if (command == null)
			return false;

		boolean clientContinue = true;

		System.out.println(s + " ->  " + command);

		switch (command) {
		case GET_DATA: {
			for (Data data : contact) {
				if (data.getName().equals(cmd.getParam())) {
					for (String line : Vcard21Parser.toStrings(data)) {
						s.send(line);
					}
				}
			}
			s.sendBlock();
			break;
		}
		case POST_DATA: {
			String cstate = cmd.getParam();
			Data data = null;
			for (Data d : contact) {
				if (cstate.equals(d.getContentState(true)))
					data = d;
			}

			if (data != null)
				data.delete();
			List<Data> list = Vcard21Parser.parseData(s.receiveBlock());
			if (list.size() > 0) {
				contact.add(list.get(0));
			}
			break;
		}
		case DELETE_DATA: {
			String cstate = cmd.getParam();
			Data data = null;
			for (Data d : contact) {
				if (cstate.equals(d.getContentState(true)))
					data = d;
			}

			if (data == null) {
				throw new InvalidParameterException(
						"Cannot find data to delete for content state: "
								+ cstate);
			}

			contact.delete();
			break;
		}
		case HASH_DATA: {
			for (Data data : contact) {
				if (data.getId().equals(cmd.getParam())) {
					s.send(data.getContentState(true));
				}
			}
			s.sendBlock();
			break;
		}
		case LIST_DATA: {
			for (Data data : contact) {
				if (cmd.getParam() == null
						|| cmd.getParam().length() == 0
						|| data.getName().toLowerCase()
								.contains(cmd.getParam().toLowerCase())) {
					s.send(data.getContentState(true) + " " + data.getName());
				}
			}
			s.sendBlock();
			break;
		}
		case PUT_CONTACT: {
			clientContinue = false;
			break;
		}
		default: {
			throw new InvalidParameterException("command invalid here: "
					+ command);
		}
		}

		return clientContinue;
	}

	/**
	 * Return the serialised {@link Card} (with timestamp).
	 * 
	 * @param name
	 *            the resource name to load
	 * 
	 * @return the serialised data
	 * 
	 * @throws IOException
	 *             in case of error
	 */
	private List<String> doGetCard(String name) throws IOException {
		List<String> lines = new LinkedList<String>();

		File vcf = getFile(name);

		if (vcf != null && vcf.exists()) {
			Card card = new Card(vcf, Format.VCard21);

			// timestamp + data
			lines.add(StringUtils.fromTime(card.getLastModified()));
			lines.addAll(Vcard21Parser.toStrings(card));
		}

		return lines;
	}

	/**
	 * Save the data to the new given resource.
	 * 
	 * @param name
	 *            the resource name to save
	 * @param data
	 *            the data to save
	 * 
	 * @return the date of last modification
	 * 
	 * @throws IOException
	 *             in case of error
	 */
	private String doPostCard(String name, List<String> data)
			throws IOException {

		File vcf = getFile(name);

		if (vcf != null) {
			Card card = new Card(Vcard21Parser.parseContact(data));
			card.saveAs(vcf, Format.VCard21);

			return StringUtils.fromTime(vcf.lastModified());
		}

		return "";
	}

	/**
	 * Return the {@link File} corresponding to the given resource name.
	 * 
	 * @param name
	 *            the resource name
	 * 
	 * @return the corresponding {@link File} or NULL if the name was NULL or
	 *         empty
	 */
	private File getFile(String name) {
		if (name != null && name.length() > 0) {
			return new File(dataDir.getAbsolutePath() + File.separator + name);
		}

		return null;
	}
}
