package be.nikiroo.jvcard.remote;

public class Command {
	public enum Verb {
		/** VERSION of the protocol */
		VERSION,
		/** TIME of the remote server in milliseconds since the Unix epoch */
		TIME,
		/** STOP the communication (client stops) */
		STOP,
		/**
		 * LIST all the contacts on the remote server that contain the search
		 * term, or all contacts if no search term given
		 */
		LIST,
		/** HELP about the protocol for interactive access */
		HELP,
		/** GET a remote contact */
		GET,
		/** PUT a new contact to the remote server or update an existing one */
		PUT,
		/** POST a new contact to the remote server */
		POST,
		/** DELETE an existing contact from the remote server */
		DELETE,
	}

	private Verb verb;
	private int version;
	private String param;

	/**
	 * Create a new, empty {@link Command} with the given {@link Verb} and
	 * version.
	 * 
	 * @param verb
	 *            the {@link Verb}
	 * @param version
	 *            the version
	 */
	public Command(Verb verb, int version) {
		this(verb, null, version);
	}

	/**
	 * Create a new, empty {@link Command} with the given {@link Verb} and
	 * version.
	 * 
	 * @param verb
	 *            the {@link Verb}
	 * @param version
	 *            the version
	 */
	public Command(Verb verb, String param, int version) {
		this.verb = verb;
		this.version = version;
		this.param = param;
	}

	/**
	 * Read a command line (starting with a {@link Verb}) and process its
	 * content here in a more readable format.
	 * 
	 * @param input
	 *            the command line
	 * @param version
	 *            the version (which can be overrided by a {@link Verb#VERSION}
	 *            command)
	 */
	public Command(String input, int version) {
		this.version = version;

		if (input != null) {
			String v = input;
			int indexSp = input.indexOf(" ");
			if (indexSp >= 0) {
				v = input.substring(0, indexSp);
			}

			for (Verb verb : Verb.values()) {
				if (v.equals(verb.name())) {
					this.verb = verb;
				}
			}

			if (verb != null) {
				String param = null;
				if (indexSp >= 0)
					param = input.substring(indexSp + 1);

				this.param = param;

				if (verb == Verb.VERSION) {
					try {
						version = Integer.parseInt(param);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Return the version
	 * 
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Return the {@link Verb}
	 * 
	 * @return the {@link Verb}
	 */
	public Verb getVerb() {
		return verb;
	}

	/**
	 * Return the parameter of this {@link Command} if any.
	 * 
	 * @return the parameter or NULL
	 */
	public String getParam() {
		return param;
	}

	@Override
	public String toString() {
		if (verb == null)
			return "[null command]";

		switch (verb) {
		case VERSION:
			return verb.name() + " " + version;
		default:
			return verb.name() + (param == null ? "" : " " + param);
		}
	}
}
