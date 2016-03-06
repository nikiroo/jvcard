package be.nikiroo.jvcard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Parser;

/**
 * A card is a contact information card. It contains data about one or more
 * contacts.
 * 
 * @author niki
 * 
 */
public class Card {
	private List<Contact> contacts;
	private File file;
	private boolean dirty;
	private String name;
	private Format format;

	/**
	 * Create a new {@link Card} from the given {@link File} and {@link Format}.
	 * 
	 * @param file
	 *            the file containing the {@link Card} data, must not be NULL
	 * @param format
	 *            the {@link Format} to use to parse it
	 * 
	 * @throws IOException
	 *             in case of IO error
	 * @throws NullPointerException
	 *             if file is NULL
	 * @throws InvalidParameterException
	 *             if format is NULL
	 */
	public Card(File file, Format format) throws IOException {
		this.file = file;
		this.format = format;
		this.name = file.getName();

		BufferedReader buffer = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		List<String> lines = new LinkedList<String>();
		for (String line = buffer.readLine(); line != null; line = buffer
				.readLine()) {
			lines.add(line);
		}
		buffer.close();

		load(lines, format);
		dirty = false; // initial load, so no change yet, so no need to call
						// setPristine()
	}

	/**
	 * Return the number of {@link Contact} present in this {@link Card}.
	 * 
	 * @return the number of {@link Contact}s
	 */
	public int size() {
		return contacts.size();
	}

	/**
	 * Return the {@link Contact} at index <i>index</i>.
	 * 
	 * @param index
	 *            the index of the {@link Contact} to find
	 * 
	 * @return the {@link Contact}
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is < 0 or >= {@link Card#size()}
	 */
	public Contact get(int index) {
		return contacts.get(index);
	}
	
	/**
	 * Add a new {@link Contact} in this {@link Card}.
	 * 
	 * @param contact
	 *            the new contact
	 */
	public void add(Contact contact) {
		contact.setParent(this);
		contact.setDirty();
		contacts.add(contact);
	}

	/**
	 * Remove the given {@link Contact} from its this {@link Card} if it is in.
	 * 
	 * @return TRUE in case of success
	 */
	public boolean remove(Contact contact) {
		if (contacts.remove(contact)) {
			setDirty();
		}

		return false;
	}

	/**
	 * Save the {@link Card} to the given {@link File} with the given
	 * {@link Format}.
	 * 
	 * @param file
	 *            the {@link File} to save to
	 * @param format
	 *            the {@link Format} to use
	 * 
	 * @return TRUE if it was saved
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	public boolean saveAs(File file, Format format) throws IOException {
		if (file == null)
			return false;

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.append(toString(format));
		writer.close();

		if (file.equals(this.file)) {
			setPristine();
		}

		return true;
	}

	/**
	 * Save the {@link Card} to the original {@link File} it was open from.
	 * 
	 * @return TRUE if it was saved
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	public boolean save() throws IOException {
		return saveAs(file, format);
	}

	/**
	 * Return a {@link String} representation of this {@link Card} in the given
	 * {@link Format}.
	 * 
	 * @param format
	 *            the {@link Format} to use
	 * 
	 * @return the {@link String}
	 */
	public String toString(Format format) {
		return Parser.toString(this, format);
	}

	/**
	 * Check if this {@link Card} has unsaved changes.
	 * 
	 * @return TRUE if it has
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Return the name of this card (the name of the {@link File} which it was
	 * opened from).
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return toString(Format.VCard21);
	}

	/**
	 * Load the given data from the given {@link Format} in this {@link Card}.
	 * 
	 * @param serializedContent
	 *            the data
	 * @param format
	 *            the {@link Format}
	 */
	protected void load(String serializedContent, Format format) {
		// note: fixed size array
		List<String> lines = Arrays.asList(serializedContent.split("\n"));
		load(lines, format);
	}

	/**
	 * Load the given data from the given {@link Format} in this {@link Card}.
	 * 
	 * @param lines
	 *            the data
	 * @param format
	 *            the {@link Format}
	 */
	protected void load(List<String> lines, Format format) {
		this.contacts = Parser.parse(lines, format);
		setDirty();

		for (Contact contact : contacts) {
			contact.setParent(this);
		}
	}

	/**
	 * Notify that this element has unsaved changes.
	 */
	void setDirty() {
		dirty = true;
	}

	/**
	 * Notify this element <i>and all its descendants</i> that it is in pristine
	 * state (as opposed to dirty).
	 */
	void setPristine() {
		dirty = false;
		for (Contact contact : contacts) {
			contact.setPristine();
		}
	}
}
