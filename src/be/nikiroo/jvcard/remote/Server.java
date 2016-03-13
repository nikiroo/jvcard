package be.nikiroo.jvcard.remote;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Parser;
import be.nikiroo.jvcard.remote.Command.Verb;
import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.tui.StringUtils;

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

	private Object cardsLock = new Object();

	public static void main(String[] args) throws IOException {
		Server server = new Server(4444);
		server.run();
	}

	/**
	 * Create a new jVCard sercer on the given port.
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
			c.sendCommand(Verb.STOP);
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
						accept(new SimpleSocket(s, "[request]"));
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
	 * Accept a client and process it.
	 * 
	 * @param s
	 *            the client to process
	 */
	private void accept(SimpleSocket s) {
		addClient(s);

		try {
			s.open(false);

			boolean clientStop = false;
			while (!clientStop) {
				Command cmd = s.receiveCommand();
				Command.Verb verb = cmd.getVerb();

				if (verb == null)
					break;

				System.out.println(s + " ->  " + verb);

				switch (verb) {
				case STOP:
					clientStop = true;
					break;
				case VERSION:
					s.sendCommand(Verb.VERSION);
					break;
				case TIME:
					s.sendLine(StringUtils.fromTime(new Date().getTime()));
					break;
				case GET:
					synchronized (cardsLock) {
						s.sendBlock(doGetCard(cmd.getParam()));
					}
					break;
				case POST:
					synchronized (cardsLock) {
						s.sendLine(doPostCard(cmd.getParam(), s.receiveBlock()));
						break;
					}
				case LIST:
					for (File file : dataDir.listFiles()) {
						if (cmd.getParam() == null
								|| cmd.getParam().length() == 0
								|| file.getName().contains(cmd.getParam())) {
							s.send(StringUtils.fromTime(file.lastModified())
									+ " " + file.getName());
						}
					}
					s.sendBlock();
					break;
				case HELP:
					// TODO: i18n
					s.send("The following commands are available:");
					s.send("- TIME: get the server time");
					s.send("- HELP: this help screen");
					s.send("- LIST: list the available cards on this server");
					s.send("- VERSION/GET/PUT/POST/DELETE/STOP: TODO");
					s.sendBlock();
					break;
				default:
					System.err
							.println("Unsupported command received from a client connection, closing it: "
									+ verb);
					clientStop = true;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			s.close();
		}

		removeClient(s);
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

		if (name != null && name.length() > 0) {
			File vcf = new File(dataDir.getAbsolutePath() + File.separator
					+ name);

			if (vcf.exists()) {
				Card card = new Card(vcf, Format.VCard21);

				// timestamp:
				lines.add(StringUtils.fromTime(card.getLastModified()));
				lines.addAll(Parser.toStrings(card, Format.VCard21));
			}
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
		if (name != null && name.length() > 0) {
			File vcf = new File(dataDir.getAbsolutePath() + File.separator
					+ name);

			Card card = new Card(Parser.parse(data, Format.VCard21));
			card.saveAs(vcf, Format.VCard21);

			return StringUtils.fromTime(vcf.lastModified());
		}

		return "";
	}
}
