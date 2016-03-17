package be.nikiroo.jvcard.remote;

public class CommandInstance {
	private Command cmd;
	private int version;
	private String param;

	/**
	 * Create a new, empty {@link CommandInstance} with the given
	 * {@link Command} and version.
	 * 
	 * @param command
	 *            the {@link Command}
	 * @param version
	 *            the version
	 */
	public CommandInstance(Command command, int version) {
		this(command, null, version);
	}

	/**
	 * Create a new, empty {@link CommandInstance} with the given
	 * {@link Command} and version.
	 * 
	 * @param cmd
	 *            the {@link Command}
	 * @param version
	 *            the version
	 */
	public CommandInstance(Command cmd, String param, int version) {
		this.cmd = cmd;
		this.version = version;
		this.param = param;
	}

	/**
	 * Read a command line (starting with a {@link Command}) and process its
	 * content here in a more readable format.
	 * 
	 * @param input
	 *            the command line
	 * @param version
	 *            the version (which can be overrided by a
	 *            {@link Command#VERSION} command)
	 */
	public CommandInstance(String input, int version) {
		this.version = version;

		if (input != null) {
			String v = input;
			int indexSp = input.indexOf(" ");
			if (indexSp >= 0) {
				v = input.substring(0, indexSp);
			}

			for (Command command : Command.values()) {
				if (v.equals(command.name())) {
					this.cmd = command;
				}
			}

			if (cmd != null) {
				String param = null;
				if (indexSp >= 0)
					param = input.substring(indexSp + 1);

				this.param = param;

				if (cmd == Command.VERSION) {
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
	 * Return the {@link Command}
	 * 
	 * @return the {@link Command}
	 */
	public Command getCommand() {
		return cmd;
	}

	/**
	 * Return the parameter of this {@link CommandInstance} if any.
	 * 
	 * @return the parameter or NULL
	 */
	public String getParam() {
		return param;
	}

	@Override
	public String toString() {
		if (cmd == null)
			return "[null command]";

		switch (cmd) {
		case VERSION:
			return cmd.name() + " " + version;
		default:
			return cmd.name() + (param == null ? "" : " " + param);
		}
	}
}
