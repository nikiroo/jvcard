package be.nikiroo.jvcard.remote;

/**
 * This enum list all the possible {@link Command}s you can send to a jVCard
 * remote servrer.
 * 
 * @author niki
 *
 */
public enum Command {
	/**
	 * Protocol version.
	 * 
	 * @return the version of the protocol used by this server
	 */
	VERSION,
	/**
	 * Server time.
	 * 
	 * @return the TIME of the remote server with the format yyyy-MM-dd HH:mm:ss
	 */
	TIME,
	/**
	 * STOP the client communication (the server will now close the
	 * communication)
	 */
	STOP,
	/**
	 * LIST the cards available on this server.
	 * 
	 * @param parameter
	 *            (optional) a search term that, if given, must be in the name
	 *            of the card for it to be returned (case insensitive)
	 * 
	 * @return a LIST of all the cards on the remote server that contain the
	 *         search term, or all cards if no search term given
	 * 
	 * @note The cards are listed each on their own line, preceded by their last
	 *       modified time and a space character (" ")
	 *       <p>
	 *       Example: <tt>
	 * <p>
	 * 2016-03-19 11:13:23 Family.vcf
	 * <p>
	 * 2016-03-19 11:13:23 CoWorkers.vcf
	 */
	LIST_CARD,
	/**
	 * Internationalised help message.
	 * 
	 * @return some HELP about the protocol for interactive access
	 */
	HELP,
	/**
	 * SELECT a resource (a card) to work on (you can issue *_CARD commands when
	 * in SELECT mode), or leave SELECT mode if already enabled.
	 * 
	 * @param parameter
	 *            the resource name (the card name) to work on to enter SELECT
	 *            mode, or nothing to leave it
	 * 
	 * @return the last modified date of the selected card
	 */
	SELECT,
	/**
	 * GET a remote card and return it as VCF data.
	 * 
	 * @return VCF data
	 */
	GET_CARD,
	/**
	 * Enter into PUT_CARD mode for the selected card (you can issue *_CONTACT
	 * commands when in PUT_CARD mode), or leave PUT_CARD mode if already
	 * enabled.
	 * 
	 * @requires SELECT mode must be enabled before
	 */
	PUT_CARD,
	/**
	 * POST a new card to the remote server as the selected resource.
	 * 
	 * @input will wait for the card content as VCF data
	 * 
	 * @requires SELECT mode must be enabled before
	 */
	POST_CARD,
	/**
	 * DELETE the selected contact from the remote server.
	 * 
	 * @requires SELECT mode must be enabled before
	 */
	DELETE_CARD,
	/**
	 * HASH the selected contact and return the hash, or return empty if not
	 * found.
	 * 
	 * @param parameter
	 *            the UID of the contact to hash
	 * 
	 * @return the hash, or an empty message if not found
	 * 
	 * @requires PUT_CARD mode must be enabled before
	 */
	HASH_CONTACT,
	/**
	 * LIST the contacts available in the selected card.
	 * 
	 * @param parameter
	 *            (optional) a search term that, if given, must be present in
	 *            the N or FN property of the contact for the contact to be
	 *            returned (case insensitive)
	 * 
	 * @return a LIST of all the contacts in the selected card that contain the
	 *         search term, or all contacts if no search term given
	 * 
	 * @requires PUT_CARD mode must be enabled before
	 * 
	 * @note The contacts are listed each on their own line, preceded by their
	 *       hashes and a space character (" ")
	 *       <p>
	 *       Example: <tt>
	 * <p>
	 * 5d1db4f26410eae670852b53e6ea80be 6pXXHy8T3b
	 * <p>
	 * 477eef8e57a12dffeeb4063d5a138c9a FoYJUyDOwM
	 */
	LIST_CONTACT,
	/**
	 * GET a remote contact if found.
	 * 
	 * @param parameter
	 *            the UID of the contact to return
	 * 
	 * @return the contact as VCF data or an empty message if the UID was not
	 *         found
	 * 
	 * @requires PUT_CARD mode must be enabled before
	 * 
	 */
	GET_CONTACT,
	/**
	 * Select the given contact by UID and enter into PUT_CONTACT mode (you can
	 * issue *_DATA commands when in PUT_CONTACT mode), or leave PUT_CONTACT
	 * mode if already enabled.
	 * 
	 * @param parameter
	 *            the UID of the contact to select to enter PUT_CONTACT mode, or
	 *            nothing to leave it
	 * 
	 * @requires PUT_CARD mode must be enabled before
	 */
	PUT_CONTACT,
	/**
	 * POST a new contact to the remote server in the selected card.
	 * 
	 * @input will wait for the contact VCF data
	 * 
	 * @requires PUT_CARD mode must be enabled before
	 */
	POST_CONTACT,
	/**
	 * DELETE an existing contact from the remote server.
	 * 
	 * @param parameter
	 *            the UID of the contact to delete
	 * 
	 * @requires PUT_CARD mode must be enabled before
	 */
	DELETE_CONTACT,
	/**
	 * HASH the data(s) with the given name.
	 * 
	 * @param parameter
	 *            the name of the data(s) you want
	 * 
	 * @return the hashes of all the datas that correspond to the given name
	 * 
	 * @requires PUT_CONTACT mode must be enabled before
	 */
	HASH_DATA,
	/**
	 * LIST the datas available in the selected contact.
	 * 
	 * @param parameter
	 *            (optional) a search term that, if given, must be present in
	 *            the name of the data for it to be returned (case insensitive)
	 * 
	 * @return a LIST of all the datas in the selected contact that contain the
	 *         search term, or all datas if no search term given
	 * 
	 * @requires PUT_CONTACT mode must be enabled before
	 * 
	 * @note The datas' names are listed each on their own line, preceded by
	 *       their hashes and a space character (" ")
	 *       <p>
	 *       Example: <tt>
	 * <p>
	 * 5d1db4f26410eae670852b53e6ea80be FN
	 * <p>
	 * 477eef8e57a12dffeeb4063d5a138c9a TEL
	 */
	LIST_DATA,
	/**
	 * GET one or more remote data(s) by name.
	 * 
	 * @param parameter
	 *            the name of the data(s) to return
	 * 
	 * @return the datas as VCF data or an empty message if no data were found
	 *         with that name
	 * 
	 * @requires PUT_CONTACT mode must be enabled before
	 * 
	 */
	GET_DATA,
	/**
	 * POST a new data to the remote server in the selected contact.
	 * 
	 * @input will wait for the data VCF data
	 * 
	 * @requires PUT_CONTACT mode must be enabled before
	 */
	POST_DATA,
	/**
	 * DELETE an existing data from the remote server.
	 * 
	 * @param parameter
	 *            the hash of the data to delete
	 * 
	 * @requires PUT_CONTACT mode must be enabled before
	 */
	DELETE_DATA,
}