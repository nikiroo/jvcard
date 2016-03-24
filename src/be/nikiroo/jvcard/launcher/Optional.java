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
	static public void runServer(int port) throws NoSuchMethodException,
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
	static public void startTui(Boolean textMode, List<String> files)
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
	 * @param callback
	 *            the {@link MergeCallback} to call in case of conflict, or NULL
	 *            to disallow conflict management (the {@link Card} will not be
	 *            allowed to synchronise in case of conflicts)
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
	static public CardResult syncCard(String input, MergeCallback callback)
			throws ClassNotFoundException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException {
		@SuppressWarnings("rawtypes")
		Class syncClass = Class.forName("be.nikiroo.jvcard.remote.Sync");
		Method sync = syncClass.getDeclaredMethod("sync", new Class[] {
				boolean.class, MergeCallback.class });

		Object o = syncClass.getConstructor(String.class).newInstance(input);
		CardResult card = (CardResult) sync.invoke(o, false, callback);

		return card;
	}
}
