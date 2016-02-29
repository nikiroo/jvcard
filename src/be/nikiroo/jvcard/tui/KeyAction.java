package be.nikiroo.jvcard.tui;

import java.io.File;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.i18n.Trans.StringId;

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
		NONE, MOVE, BACK, HELP, FILE_LIST, CONTACT_LIST, CONTACT_DETAILS, EDIT_DETAIL, DELETE_CONTACT, SAVE_CARD,
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
	 * Return the key used to trigger this {@link KeyAction} or '\0' if none.
	 * Also check the special key ({@link KeyAction#getKkey}) if any.
	 * 
	 * @return the shortcut character to use to invoke this {@link KeyAction} or
	 *         '\0'
	 */
	public KeyStroke getKey() {
		return key;
	}

	// check if the given key should trigger this action
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
	 * Return the kind of key this {@link KeyAction } is linked to. Will be
	 * {@link KeyType#NormalKey} if only normal keys can invoke this
	 * {@link KeyAction}. Also check the normal key ({@link KeyAction#getKey})
	 * if any.
	 * 
	 * @return the special shortcut key to use to invoke this {@link KeyAction}
	 *         or {@link KeyType#NormalKey}
	 */

	/**
	 * The mode to change to when this action is completed.
	 * 
	 * @return the new mode
	 */
	public Mode getMode() {
		return mode;
	}

	public StringId getStringId() {
		return id;
	}

	public Card getCard() {
		Object o = getObject();
		if (o instanceof Card)
			return (Card) o;
		return null;
	}

	public Contact getContact() {
		Object o = getObject();
		if (o instanceof Contact)
			return (Contact) o;
		return null;
	}

	public Data getData() {
		Object o = getObject();
		if (o instanceof Data)
			return (Data) o;
		return null;
	}

	// override this one if needed, DO NOT process here as it will be call a lot
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
}
