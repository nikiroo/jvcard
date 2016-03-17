package be.nikiroo.jvcard.remote;

public enum Command {
	/** VERSION of the protocol */
	VERSION,
	/** TIME of the remote server in milliseconds since the Unix epoch */
	TIME,
	/** STOP the communication (client stops) */
	STOP,
	/**
	 * LIST all the cards on the remote server that contain the search term,
	 * or all contacts if no search term given; also add their timestamps
	 */
	LIST_CARD,
	/** HELP about the protocol for interactive access */
	HELP,
	/** SELECT a resource (a card) to work on */
	SELECT,
	/** GET a remote card */
	GET_CARD,
	/**
	 * PUT mode activation toggle for a card on the remote server (you can issue
	 * *_CONTACT commands when in PUT mode)
	 */
	PUT_CARD,
	/** POST a new card to the remote server */
	POST_CARD,
	/** DELETE an existing contact from the remote server */
	DELETE_CARD,
	/** HASH the given contact and return the hash, or empty if not found */
	HASH_CONTACT,
	/** LIST all the contacts of the current card; also add their hashes */
	LIST_CONTACT,
	/** GET a remote contact */
	GET_CONTACT,
	/**
	 * PUT mode activation toggle for a contact on the remote server (you can
	 * issue *_DATA commands when in PUT mode), param = uid
	 */
	PUT_CONTACT,
	/** POST a new contact to the remote server */
	POST_CONTACT,
	/** DELETE an existing contact from the remote server */
	DELETE_CONTACT,
	/** HASH the data(s) with the given name */
	HASH_DATA,
	/** LIST all the datas of the current contact; also add their hashes */
	LIST_DATA,
	/** GET a (or more) remote data(s) by name */
	GET_DATA,
	/** POST a new data to the remote server */
	POST_DATA,
	/** DELETE an existing data from the remote server */
	DELETE_DATA,
}