package be.nikiroo.jvcard.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.remote.Command.Verb;

/**
 * A client or server connection, that will allow you to connect to, send and
 * receive data to/from a jVCard remote server.
 * 
 * 
 * 
 * @author niki
 */
public class SimpleSocket {
	/**
	 * The current version of the network protocol.
	 */
	static private final int CURRENT_VERSION = 1;

	/**
	 * The end of block marker.
	 * 
	 * An end of block marker needs to be on a line on itself to be valid, and
	 * will denote the end of a block of data.
	 */
	static private String EOB = ".";

	private Socket s;
	private PrintWriter out;
	private BufferedReader in;
	private int version; // version of the OTHER end, not this one (this one is
							// CURRENT_VERSION obviously)

	private String label; // can be used for debugging

	/**
	 * Create a new {@link SimpleSocket} with the given {@link Socket}.
	 * 
	 * @param s
	 *            the {@link Socket}
	 */
	public SimpleSocket(Socket s, String label) {
		this.s = s;
		this.label = label;
	}

	/**
	 * Return the label of this {@link SimpleSocket}. This is mainly used for
	 * debugging purposes or user display if any. It is optional.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		if (label == null)
			return "[no name]";
		return label;
	}

	/**
	 * Open the {@link SimpleSocket} for reading/writing and negotiates the
	 * version.
	 * 
	 * Note that you <b>MUST</b> call {@link SimpleSocket#close()} when you are
	 * done to release the acquired resources.
	 * 
	 * @param client
	 *            TRUE for clients, FALSE for servers (server speaks first)
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void open(boolean client) throws IOException {
		out = new PrintWriter(s.getOutputStream(), false);
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));

		if (client) {
			version = new Command(receiveLine(), -1).getVersion();
			sendLine(new Command(Command.Verb.VERSION, CURRENT_VERSION)
					.toString());
		} else {
			send(new Command(Command.Verb.VERSION, CURRENT_VERSION).toString());
			// TODO: i18n
			send("[Some help info here]");
			send("you need to reply with your VERSION + end of block");
			send("please send HELP in a full block or help");
			sendBlock();
			version = new Command(receiveLine(), -1).getVersion();
		}
	}

	/**
	 * Close the connection and release acquired resources.
	 * 
	 * @return TRUE if everything was closed properly, FALSE if the connection
	 *         was broken (in all cases, resources are released)
	 */
	public boolean close() {
		boolean broken = false;

		try {
			sendBlock();
			broken = out.checkError();
		} catch (IOException e) {
			broken = true;
		}

		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
			broken = true;
			try {
				in.close();
			} catch (IOException ee) {
			}
			out.close();
		}

		s = null;
		in = null;
		out = null;

		return !broken;
	}

	/**
	 * Sends commands to the remote server. Do <b>NOT</b> sends the end-of-block
	 * marker.
	 * 
	 * @param data
	 *            the data to send
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	protected void send(String data) throws IOException {
		if (data != null) {
			out.write(data);
		}

		out.write("\n");

		if (out.checkError())
			throw new IOException();
	}

	/**
	 * Sends an end-of-block marker to the remote server.
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void sendBlock() throws IOException {
		sendBlock((List<String>) null);
	}

	/**
	 * Sends commands to the remote server, then sends an end-of-block marker.
	 * 
	 * @param data
	 *            the data to send
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void sendLine(String data) throws IOException {
		sendBlock(Arrays.asList(new String[] { data }));
	}

	/**
	 * Sends commands to the remote server, then sends an end-of-block marker.
	 * 
	 * @param data
	 *            the data to send
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void sendBlock(List<String> data) throws IOException {
		if (data != null) {
			for (String dataLine : data) {
				send(dataLine);
			}
		}

		send(EOB);
	}

	/**
	 * Sends commands to the remote server, then sends an end-of-block marker.
	 * 
	 * @param verb
	 *            the {@link Verb} to send
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void sendCommand(Command.Verb verb) throws IOException {
		sendCommand(verb, null);
	}

	/**
	 * Sends commands to the remote server, then sends an end-of-block marker.
	 * 
	 * @param verb
	 *            the data to send
	 * 
	 * @param param
	 *            the parameter for this command if any
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public void sendCommand(Command.Verb verb, String param) throws IOException {
		sendLine(new Command(verb, param, CURRENT_VERSION).toString());
	}

	/**
	 * Read a line from the remote server.
	 * 
	 * Do <b>NOT</b> read until the end-of-block marker, and can return said
	 * block without conversion.
	 * 
	 * @return the read line
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	protected String receive() throws IOException {
		String line = in.readLine();
		return line;
	}

	/**
	 * Read lines from the remote server until the end-of-block ("\0\n") marker
	 * is detected.
	 * 
	 * @return the read lines without the end marker, or NULL if nothing more to
	 *         read
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public List<String> receiveBlock() throws IOException {
		List<String> result = new LinkedList<String>();

		String line = receive();
		for (; line != null && !line.equals(EOB); line = receive()) {
			result.add(line);
		}

		if (line == null)
			return null;

		return result;
	}

	/**
	 * Read a line from the remote server then read until the next end-of-block
	 * marker.
	 * 
	 * @return the parsed line, or NULL if nothing more to read
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public String receiveLine() throws IOException {
		List<String> lines = receiveBlock();

		if (lines.size() > 0)
			return lines.get(0);

		return null;
	}

	/**
	 * Read a line from the remote server and convert it to a {@link Command},
	 * then read until the next end-of-block marker.
	 * 
	 * @return the parsed {@link Command}
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public Command receiveCommand() throws IOException {
		String line = receive();
		Command cmd = new Command(line, version);
		receiveBlock();
		return cmd;
	}

	@Override
	public String toString() {
		String source = "[not connected]";
		InetAddress iadr = s.getInetAddress();
		if (iadr != null)
			source = iadr.getHostName();

		return getLabel() + " (" + source + ")";
	}
}
