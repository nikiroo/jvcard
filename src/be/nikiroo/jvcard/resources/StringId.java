package be.nikiroo.jvcard.resources;

import be.nikiroo.utils.resources.Meta;

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
	@Meta(info = "MUST BE 3 chars long", description = "Tab key")
	KEY_TAB, // keys
	@Meta(info = "MUST BE 3 chars long", description = "Enter key")
	KEY_ENTER, //
	@Meta(description = "Go back to previous screen")
	KEY_ACTION_BACK, //
	@Meta(description = "Get help text")
	KEY_ACTION_HELP, //
	@Meta(description = "View the selected card")
	KEY_ACTION_VIEW_CARD, //
	@Meta(description = "View the selected contact")
	KEY_ACTION_VIEW_CONTACT, //
	@Meta(description = "Edit the contact")
	KEY_ACTION_EDIT_CONTACT, //
	@Meta(description = "Edit the contact in RAW mode")
	KEY_ACTION_EDIT_CONTACT_RAW, //
	@Meta(description = "Edit the RAW field")
	KEY_ACTION_EDIT_FIELD, //
	@Meta(description = "Save the whole card")
	KEY_ACTION_SAVE_CARD, //
	@Meta(description = "Delete the selected element")
	KEY_ACTION_DELETE, //
	@Meta(description = "Filter the displayed contacts")
	KEY_ACTION_SEARCH, //
	@Meta(info = "we could use: ' ', ┃, │...", description = "Field separator")
	DEAULT_FIELD_SEPARATOR, // MainContentList
	@Meta(description = "Invert the photo's colours")
	KEY_ACTION_INVERT, //
	@Meta(description = "Show the photo in 'fullscreen'")
	KEY_ACTION_FULLSCREEN, //
	@Meta(description = "Switch between the available display formats")
	KEY_ACTION_SWITCH_FORMAT, // multi-usage
	@Meta(description = "Add a new contact/field")
	KEY_ACTION_ADD, //
	@Meta(description = "New contact")
	ASK_USER_CONTACT_NAME, //
	@Meta(info = "%s = contact name", description = "Delete contact")
	CONFIRM_USER_DELETE_CONTACT, //
	@Meta(info = "%s = contact name", description = "cannot delete a contact")
	ERR_CANNOT_DELETE_CONTACT, //
	@Meta(description = "The Help message header line")
	CLI_HELP, //
	@Meta(description = "The Help message line before explaining the different modes")
	CLI_HELP_MODES, //
	@Meta(description = "The Help message line for help usage")
	CLI_HELP_MODE_HELP, //
	@Meta(description = "The Help message line for contact manager usage")
	CLI_HELP_MODE_CONTACT_MANAGER, //
	@Meta(description = "The Help message line for contact manager usage")
	CLI_HELP_MODE_I18N, //
	@Meta(description = "The Help message line for jVCard server usage")
	CLI_HELP_MODE_SERVER, //
	@Meta(description = "The Help message line for --load-photo usage")
	CLI_HELP_MODE_LOAD_PHOTO, //
	@Meta(description = "The Help message line for --save-photo usage")
	CLI_HELP_MODE_SAVE_PHOTO, //
	@Meta(description = "The Help message line for --save-to usage")
	CLI_HELP_MODE_SAVE_TO, //
	@Meta(description = "The Help message line for config save usage")
	CLI_HELP_MODE_SAVE_CONFIG, //
	@Meta(description = "The Help message line before the list of options")
	CLI_HELP_OPTIONS, //
	@Meta(description = "The Help message line for: --")
	CLI_HELP_DD, //
	@Meta(description = "The Help message line for: --")
	CLI_HELP_LANG, //
	@Meta(description = "The Help message line for: --")
	CLI_HELP_GUI, //
	@Meta(description = "The Help message line for: --")
	CLI_HELP_TUI, //
	@Meta(description = "The Help message line for: --")
	CLI_HELP_NOUTF_OPTION, //
	@Meta(description = "The Help message line for: --")
	CLI_HELP_CONFIG, //
	@Meta(description = "The Help message footer about files and jvcard:// links")
	CLI_HELP_FOOTER, //
	@Meta(info = "%s = the error", description = "Syntax error: SOME TEXT")
	CLI_SERR, //
	@Meta(description = "More than one mode given")
	CLI_SERR_MODES, //
	@Meta(description = "--lang is required")
	CLI_SERR_NOLANG, //
	@Meta(description = "The dir is required")
	CLI_SERR_NODIR, //
	@Meta(description = "The port is required")
	CLI_SERR_NOPORT, //
	@Meta(description = "The format is required")
	CLI_SERR_NOFORMAT, //
	@Meta(info = "%s = bad port", description = "The port is not valid")
	CLI_SERR_BADPORT, //
	@Meta(info = "%s = mode", description = "Card files are not supported in mode %s")
	CLI_SERR_CANNOT_CARDS, //
	@Meta(info = "%s = the error", description = "Error: SOME TEXT")
	CLI_ERR, //
	@Meta(description = "No files given")
	CLI_ERR_NOFILES, //
	@Meta(info = "%s = dir", description = "Cannot create conf dir %s")
	CLI_ERR_CANNOT_CREATE_CONFDIR, //
	@Meta(description = "Remoting not available")
	CLI_ERR_NO_REMOTING, //
	@Meta(description = "TUI not available")
	CLI_ERR_NO_TUI, //
	@Meta(info = "%s = dir", description = "Cannot create/update language in dir %s")
	CLI_ERR_CANNOT_CREATE_LANG, //
	@Meta(info = "%s = card", description = "Cannot open card %s")
	CLI_ERR_CANNOT_OPEN, //
	@Meta(info = "%s = contact FN", description = "Cannot save photo of contact %s")
	CLI_ERR_CANNOT_SAVE_PHOTO, //
	@Meta(description = "Cannot start the program with the given cards")
	CLI_ERR_CANNOT_START, //
}
