package be.nikiroo.jvcard.tui;

import java.io.File;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.resources.StringId;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

/**
 * This class represents a keybinding; it encapsulates data about the actual key
 * to press and the associated action to take.
 * 
 * You are expected to subclass it if you want to create a custom action.
 * 
 * @author niki
 * 
 */
public class KeyAction {
	/**
	 * The keybinding mode that will be triggered by this action.
	 * 
	 * @author niki
	 * 
	 */
	public enum Mode {
		NONE, MOVE, BACK, HELP, FILE_LIST, CONTACT_LIST, CONTACT_DETAILS_RAW, CONTACT_DETAILS, ASK_USER, ASK_USER_KEY,
	}

	public enum DataType {
		/**
		 * A list of Card {@link File}s.
		 */
		CARD_FILES,
		/**
		 * Contains a list of contacts.
		 */
		CARD,
		/**
		 * All the known informations about a specific contact person or
		 * company.
		 */
		CONTACT,
		/**
		 * An information about a contact.
		 */
		DATA,
		/**
		 * Empty.
		 */
		NONE
	}

	private StringId id;
	private KeyStroke key;
	private Mode mode;
	private String message;
	private boolean error;

	public KeyAction(Mode mode, KeyStroke key, StringId id) {
		this.id = id;
		this.key = key;
		this.mode = mode;
	}

	public KeyAction(Mode mode, KeyType keyType, StringId id) {
		this.id = id;
		this.key = new KeyStroke(keyType);
		this.mode = mode;
	}

	public KeyAction(Mode mode, char car, StringId id) {
		this.id = id;
		this.key = new KeyStroke(car, false, false);
		this.mode = mode;
	}

	/**
	 * Return the key used to trigger this {@link KeyAction}.
	 * 
	 * @return the shortcut {@link KeyStroke} to use to invoke this
	 *         {@link KeyAction}
	 */
	public KeyStroke getKey() {
		return key;
	}

	/**
	 * Return the associated message if any.
	 * 
	 * @return the associated message or NULL
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set a message to display to the user. This message will be get after
	 * {@link KeyAction#getObject()} has been called.
	 * 
	 * @param message
	 *            the message
	 * @param error
	 *            TRUE for an error message, FALSE for information
	 */
	public void setMessage(String message, boolean error) {
		this.message = message;
		this.error = error;
	}

	/**
	 * Check if the included message ({@link KeyAction#getMessage()}) is an
	 * error message or an information message.
	 * 
	 * @return TRUE for error, FALSE for information
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Check if the given {@link KeyStroke} should trigger this action.
	 * 
	 * @param mkey
	 *            the {@link KeyStroke} to check against
	 * 
	 * @return TRUE if it should
	 */
	public boolean match(KeyStroke mkey) {
		if (mkey == null || key == null)
			return false;

		if (mkey.getKeyType() == key.getKeyType()) {
			if (mkey.getKeyType() != KeyType.Character)
				return true;

			return mkey.getCharacter() == key.getCharacter();
		}

		return false;
	}

	/**
	 * The mode to change to when this action is completed.
	 * 
	 * @return the new mode
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Get the associated {@link StringId} or NULL if the action must not be
	 * displayed in the action bar.
	 * 
	 * @return the {@link StringId} or NULL
	 */
	public StringId getStringId() {
		return id;
	}

	/**
	 * Get the associated object as a {@link Card} if it is a {@link Card}.
	 * 
	 * @return the associated {@link Card} or NULL
	 */
	public Card getCard() {
		Object o = getObject();
		if (o instanceof Card)
			return (Card) o;
		return null;
	}

	/**
	 * Get the associated object as a {@link Contact} if it is a {@link Contact}
	 * .
	 * 
	 * @return the associated {@link Contact} or NULL
	 */
	public Contact getContact() {
		Object o = getObject();
		if (o instanceof Contact)
			return (Contact) o;
		return null;
	}

	/**
	 * Get the associated object as a {@link Data} if it is a {@link Data}.
	 * 
	 * @return the associated {@link Data} or NULL
	 */
	public Data getData() {
		Object o = getObject();
		if (o instanceof Data)
			return (Data) o;
		return null;
	}

	/**
	 * Return the associated target object. You should use
	 * {@link KeyAction#getCard()}, {@link KeyAction#getContact()} or
	 * {@link KeyAction#getData()} instead if you know the kind of object it is.
	 * 
	 * <p>
	 * 
	 * You are expected to override this method to return your object, the 3
	 * afore-mentioned methods will use this one as the source.
	 * 
	 * <p>
	 * 
	 * <b>DO NOT</b> process data here, this method will be called often; this
	 * should only be a <b>getter</b> method.
	 * 
	 * @return the associated object
	 */
	public Object getObject() {
		return null;
	}

	/**
	 * The method which is called when the action is performed. You can subclass
	 * it if you want to customise the action (by default, it just accepts the
	 * mode change (see {@link KeyAction#getMode}).
	 * 
	 * @return false to cancel mode change
	 */
	public boolean onAction() {
		return true;
	}

	/**
	 * Used to callback a function from the menu when the user has to introduce
	 * some text.
	 * 
	 * @param answer
	 *            the user answer
	 * 
	 * @return an error message if any
	 */
	public String callback(String answer) {
		return null;
	}

	/**
	 * When asking a question to the user, return the question.
	 * 
	 * @return the question
	 */
	public String getQuestion() {
		return null;
	}

	/**
	 * When asking a question to the user (not for one-key mode), return the
	 * default answer.
	 * 
	 * @return the default answer
	 */
	public String getDefaultAnswer() {
		return null;
	}

	/**
	 * Translate the given {@link KeyStroke} into a user text {@link String} of
	 * size 3.
	 * 
	 * @param key
	 *            the key to translate
	 * 
	 * @return the translated text
	 */
	static public String trans(KeyStroke key) {
		String keyTrans = "";

		switch (key.getKeyType()) {
		case Enter:
			if (Main.isUnicode())
				keyTrans = " ⤶ ";
			else
				keyTrans = Main.trans(StringId.KEY_ENTER);
			break;
		case Tab:
			if (Main.isUnicode())
				keyTrans = " ↹ ";
			else
				keyTrans = Main.trans(StringId.KEY_TAB);

			break;
		case Character:
			keyTrans = " " + key.getCharacter() + " ";
			break;
		default:
			keyTrans = "" + key.getKeyType();
			int width = 3;
			if (keyTrans.length() > width) {
				keyTrans = keyTrans.substring(0, width);
			} else if (keyTrans.length() < width) {
				keyTrans = keyTrans
						+ new String(new char[width - keyTrans.length()])
								.replace('\0', ' ');
			}
			break;
		}

		return keyTrans;
	}
}
