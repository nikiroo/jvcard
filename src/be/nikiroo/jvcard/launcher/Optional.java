package be.nikiroo.jvcard.launcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.launcher.CardResult.MergeCallback;

/**
 * This class let you call "optional" methods, that is, methods and classes that
 * may or may not be present.
 * 
 * <p>
 * It currently offers services for:
 * <ul>
 * <li>remoting support</li>
 * <li>TUI support</li>
 * </ul>
 * </p>
 * 
 * @author niki
 *
 */
class Optional {
	/***
	 * An {@link Exception} that is raised when you try to access functionality
	 * that has not been compiled into the code.
	 * 
	 * @author niki
	 *
	 */
	public class NotSupportedException extends Exception {
		private static final long serialVersionUID = 1L;

		private boolean notCompiled;

		/**
		 * Create a new {@link NotSupportedException}.
		 * 
		 * @param notSupportedOption
		 *            the option that is not supported
		 * @param notCompiled
		 *            FALSE when the operation is compiled in but not compatible
		 *            for internal reasons
		 */
		public NotSupportedException(Exception e, String notSupportedOption,
				boolean notCompiled) {
			super((notCompiled ? "Option not supported: "
					: "Internal error when trying to use: ")
					+ notSupportedOption, e);

			this.notCompiled = notCompiled;
		}

		/**
		 * Check if the support is supposed to be compiled in the sources.
		 * 
		 * @return TRUE if it should have worked (hence, if an internal error
		 *         occurred)
		 */
		public boolean isCompiledIn() {
			return !notCompiled;
		}
	}

	/**
	 * Create a new jVCard server on the given port, then run it.
	 * 
	 * @param port
	 *            the port to run on
	 *
	 * @throws NotSupportedException
	 *             in case the option is not supported
	 * @throws IOException
	 *             in case of IO error
	 */
	@SuppressWarnings("unchecked")
	static public void runServer(int port) throws IOException,
			NotSupportedException {
		try {
			@SuppressWarnings("rawtypes")
			Class serverClass = Class
					.forName("be.nikiroo.jvcard.remote.Server");
			Method run = serverClass
					.getDeclaredMethod("run", new Class<?>[] {});
			run.invoke(serverClass.getConstructor(int.class).newInstance(port));
		} catch (NoSuchMethodException e) {
			throw new Optional().new NotSupportedException(e, "remoting", true);
		} catch (ClassNotFoundException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (SecurityException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (InstantiationException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (IllegalAccessException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (IllegalArgumentException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (InvocationTargetException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		}
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
	 * @throws NotSupportedException
	 *             in case the option is not supported
	 * @throws IOException
	 *             in case of IO error
	 */
	@SuppressWarnings("unchecked")
	static public void startTui(Boolean textMode, List<String> files)
			throws IOException, NotSupportedException {
		try {
			@SuppressWarnings("rawtypes")
			Class launcherClass = Class
					.forName("be.nikiroo.jvcard.tui.TuiLauncher");
			Method start = launcherClass.getDeclaredMethod("start",
					new Class<?>[] { Boolean.class, List.class });
			start.invoke(launcherClass.newInstance(), textMode, files);
		} catch (NoSuchMethodException e) {
			throw new Optional().new NotSupportedException(e, "TUI", true);
		} catch (ClassNotFoundException e) {
			throw new Optional().new NotSupportedException(e, "TUI", false);
		} catch (SecurityException e) {
			throw new Optional().new NotSupportedException(e, "TUI", false);
		} catch (InstantiationException e) {
			throw new Optional().new NotSupportedException(e, "TUI", false);
		} catch (IllegalAccessException e) {
			throw new Optional().new NotSupportedException(e, "TUI", false);
		} catch (IllegalArgumentException e) {
			throw new Optional().new NotSupportedException(e, "TUI", false);
		} catch (InvocationTargetException e) {
			throw new Optional().new NotSupportedException(e, "TUI", false);
		}
	}

	/**
	 * Return the {@link Card} corresponding to the given URL, synchronised if
	 * necessary.
	 * 
	 * @param input
	 *            the jvcard:// with resource name URL (e.g.:
	 *            <tt>jvcard://localhost:4444/coworkers</tt>)
	 * @param callback
	 *            the {@link MergeCallback} to call in case of conflict, or NULL
	 *            to disallow conflict management (the {@link Card} will not be
	 *            allowed to synchronise in case of conflicts)
	 * 
	 * @throws NotSupportedException
	 *             in case the option is not supported
	 * @throws IOException
	 *             in case of IO error
	 */
	@SuppressWarnings("unchecked")
	static public CardResult syncCard(String input, MergeCallback callback)
			throws IOException, NotSupportedException {
		try {
			@SuppressWarnings("rawtypes")
			Class syncClass = Class.forName("be.nikiroo.jvcard.remote.Sync");
			Method sync = syncClass.getDeclaredMethod("sync", new Class<?>[] {
					boolean.class, MergeCallback.class });

			Object o = syncClass.getConstructor(String.class)
					.newInstance(input);
			CardResult card = (CardResult) sync.invoke(o, false, callback);

			return card;
		} catch (NoSuchMethodException e) {
			throw new Optional().new NotSupportedException(e, "remoting", true);
		} catch (ClassNotFoundException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (SecurityException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (InstantiationException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (IllegalAccessException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (IllegalArgumentException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		} catch (InvocationTargetException e) {
			throw new Optional().new NotSupportedException(e, "remoting", false);
		}
	}
}
