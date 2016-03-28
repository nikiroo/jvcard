package be.nikiroo.jvcard.resources.enums;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Bundles.Bundle;
import be.nikiroo.jvcard.resources.Meta;

/**
 * The enum representing textual information to be translated to the user as a
 * key.
 * 
 * Note that each key that should be translated MUST be annotated with a
 * {@link Meta} annotation.
 * 
 * @author niki
 * 
 */
public enum StringId {
	DUMMY, // <-- TODO : remove
	NULL, // Special usage, no annotations so it is not visible in
			// .properties files
	@Meta(what = "a key to press", where = "action keys", format = "MUST BE 3 chars long", info = "Tab key")
	KEY_TAB, // keys
	@Meta(what = "a key to press", where = "action keys", format = "MUST BE 3 chars long", info = "Enter key")
	KEY_ENTER, //
	@Meta(what = "Action key", where = "All screens except the first (KEY_ACTION_QUIT)", format = "", info = "Go back to previous screen")
	KEY_ACTION_BACK, //
	@Meta(what = "Action key", where = "MainWindow", format = "", info = "Get help text")
	KEY_ACTION_HELP, //
	@Meta(what = "Action key", where = "FileList", format = "", info = "View the selected card")
	KEY_ACTION_VIEW_CARD, //
	@Meta(what = "Action key", where = "ContactList", format = "", info = "View the selected contact")
	KEY_ACTION_VIEW_CONTACT, //
	@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Edit the contact")
	KEY_ACTION_EDIT_CONTACT, //
	@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Edit the contact in RAW mode")
	KEY_ACTION_EDIT_CONTACT_RAW, //
	@Meta(what = "Action key", where = "ContactDetailsRaw", format = "", info = "Edit the RAW field")
	KEY_ACTION_EDIT_FIELD, //
	@Meta(what = "Action key", where = "ContactList", format = "", info = "Save the whole card")
	KEY_ACTION_SAVE_CARD, //
	@Meta(what = "", where = "ContactList/ContactDetailsRaw", format = "", info = "Delete the selected element")
	KEY_ACTION_DELETE, //
	@Meta(what = "Action key", where = "ContactList", format = "", info = "Filter the displayed contacts")
	KEY_ACTION_SEARCH, //
	@Meta(what = "", where = "", format = "we could use: ' ', ┃, │...", info = "Field separator")
	DEAULT_FIELD_SEPARATOR, // MainContentList
	@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Invert the photo's colours")
	KEY_ACTION_INVERT, //
	@Meta(what = "Action key", where = "ContactDetails", format = "", info = "Show the photo in 'fullscreen'")
	KEY_ACTION_FULLSCREEN, //
	@Meta(what = "Action key", where = "ContactList, ContactDetails, ContactDetailsRaw", format = "", info = "Switch between the available display formats")
	KEY_ACTION_SWITCH_FORMAT, // multi-usage
	@Meta(what = "Action key", where = "Contact list, Edit Contact", format = "", info = "Add a new contact/field")
	KEY_ACTION_ADD, //
	@Meta(what = "User question: TEXT", where = "Contact list", format = "", info = "New contact")
	ASK_USER_CONTACT_NAME, //
	@Meta(what = "User question: [Y|N]", where = "Contact list", format = "%s = contact name", info = "Delete contact")
	CONFIRM_USER_DELETE_CONTACT, //
	@Meta(what = "Error", where = "Contact list", format = "%s = contact name", info = "cannot delete a contact")
	ERR_CANNOT_DELETE_CONTACT, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message header line")
	CLI_HELP, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line before explaining the different modes")
	CLI_HELP_MODES, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for help usage")
	CLI_HELP_MODE_HELP, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for contact manager usage")
	CLI_HELP_MODE_CONTACT_MANAGER, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for contact manager usage")
	CLI_HELP_MODE_I18N, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for jVCard server usage")
	CLI_HELP_MODE_SERVER, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for --load-photo usage")
	CLI_HELP_MODE_LOAD_PHOTO, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for --save-photo usage")
	CLI_HELP_MODE_SAVE_PHOTO, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for config save usage")
	CLI_HELP_MODE_SAVE_CONFIG, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line before the list of options")
	CLI_HELP_OPTIONS, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for: --")
	CLI_HELP_DD, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for: --")
	CLI_HELP_LANG, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for: --")
	CLI_HELP_GUI, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for: --")
	CLI_HELP_TUI, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for: --")
	CLI_HELP_NOUTF, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message line for: --")
	CLI_HELP_CONFIG, //
	@Meta(what = "CLI --help", where = "", format = "", info = "The Help message footer about files and jvcard:// links")
	CLI_HELP_FOOTER, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = the error", info = "Syntax error: SOME TEXT")
	CLI_SERR, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "More than one mode given")
	CLI_SERR_MODES, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "--lang is required")
	CLI_SERR_NOLANG, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "The dir is required")
	CLI_SERR_NODIR, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "The port is required")
	CLI_SERR_NOPORT, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "The format is required")
	CLI_SERR_NOFORMAT, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = bad port", info = "The port is not valid")
	CLI_SERR_BADPORT, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = mode", info = "Card files are not supported in mode %s")
	CLI_SERR_CANNOT_CARDS, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = the error", info = "Error: SOME TEXT")
	CLI_ERR, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "No files given")
	CLI_ERR_NOFILES, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = dir", info = "Cannot create conf dir %s")
	CLI_ERR_CANNOT_CREATE_CONFDIR, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "Remoting not available")
	CLI_ERR_NO_REMOTING, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "TUI not available")
	CLI_ERR_NO_TUI, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = dir", info = "Cannot create/update language in dir %s")
	CLI_ERR_CANNOT_CREATE_LANG, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = card", info = "Cannot open card %s")
	CLI_ERR_CANNOT_OPEN, //
	@Meta(what = "CLI ERROR", where = "", format = "%s = contact FN", info = "Cannot save photo of contact %s")
	CLI_ERR_CANNOT_SAVE_PHOTO, //
	@Meta(what = "CLI ERROR", where = "", format = "", info = "Cannot start the program with the given cards")
	CLI_ERR_CANNOT_START, //

	;

	/**
	 * Write the header found in the configuration <tt>.properties</tt> file of
	 * this {@link Bundle}.
	 * 
	 * @param writer
	 *            the {@link Writer} to write the header in
	 * @param name
	 *            the file name
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	static public void writeHeader(Writer writer, String name)
			throws IOException {
		writer.write("# " + name + " translation file (UTF-8)\n");
		writer.write("# \n");
		writer.write("# Note that any key can be doubled with a _NOUTF suffix\n");
		writer.write("# to use when the flag --noutf is passed\n");
		writer.write("# \n");
		writer.write("# Also, the comments always refer to the key below them.\n");
		writer.write("# \n");
	}
};
