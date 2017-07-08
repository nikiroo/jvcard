package be.nikiroo.jvcard.launcher;

import java.io.IOException;

import be.nikiroo.jvcard.Card;

/**
 * This class is a placeholder for a {@link Card} result and some information
 * about it.
 * 
 * @author niki
 * 
 */
public class CardResult {
	/**
	 * This interface represents the merge callback when the {@link Card}
	 * synchronisation is not able to process fully automatically.
	 * 
	 * @author niki
	 * 
	 */
	public interface MergeCallback {
		/**
		 * This method will be called when the local cache and the server both
		 * have changes. You need to review the proposed changes, or do your own
		 * merge, and return the final result. You can also cancel the merge
		 * operation by returning NULL.
		 * 
		 * @param previous
		 *            the previous version of the {@link Card}
		 * @param local
		 *            the local cache version of the {@link Card}
		 * @param server
		 *            the remote server version of the {@link Card}
		 * @param autoMerged
		 *            the automatic merge result you should manually check
		 * 
		 * @return the final merged result, or NULL for cancel
		 */
		public Card merge(Card previous, Card local, Card server,
				Card autoMerged);
	}

	private Card card;
	private boolean remote;
	private boolean synced;
	private boolean changed;
	private IOException exception;

	/**
	 * Create a new {@link CardResult}.
	 * 
	 * @param card
	 *            the target {@link Card}
	 * @param remtote
	 *            TRUE if it is linked to a remote server
	 * @param synced
	 *            TRUE if it was synchronised
	 */
	public CardResult(Card card, boolean remote, boolean synced, boolean changed) {
		this.card = card;
		this.remote = remote;
		this.synced = synced;
		this.changed = changed;
	}

	/**
	 * Create a new {@link CardResult}.
	 * 
	 * @param exception
	 *            the synchronisation exception that occurred
	 */
	public CardResult(IOException exception) {
		this(null, true, false, false);
		this.exception = exception;
	}

	/**
	 * Check if this {@link Card} is linked to a remote jVCard server.
	 * 
	 * @return TRUE if it is
	 */
	public boolean isRemote() {
		return remote;
	}

	/**
	 * Check if the {@link Card} was synchronised.
	 * 
	 * @return TRUE if it was
	 */
	public boolean isSynchronised() {
		return synced;
	}

	/**
	 * Check if this {@link Card} changed after the synchronisation.
	 * 
	 * @return TRUE if it has
	 */
	public boolean isChanged() {
		return remote && changed;
	}

	/**
	 * Return the {@link Card}
	 * 
	 * @return the {@link Card}
	 * 
	 * @throws IOException
	 *             in case of synchronisation issues
	 */
	public Card getCard() throws IOException {
		if (exception != null)
			throw exception;

		return card;
	}
}
