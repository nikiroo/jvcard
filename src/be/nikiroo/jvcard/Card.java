package be.nikiroo.jvcard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

	public Card(File file, Format format) throws IOException {
		this.file = file;
		this.format = format;

		if (file != null) {
			name = file.getName();
		}

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
	 * Return the full list of {@link Contact}s. Please use responsibly (this is
	 * the original list, do not modify the list itself).
	 * 
	 * @return the list of {@link Contact}s
	 */
	public List<Contact> getContactsList() {
		return contacts;
	}

	/**
	 * Return the list of {@link Contact}s. Note that this list is a copy.
	 * 
	 * @return the list of {@link Contact}s
	 */
	public List<Contact> getContacts() {
		ArrayList<Contact> list = new ArrayList<Contact>(size());
		list.addAll(contacts);
		return list;
	}

	public int size() {
		return contacts.size();
	}

	public Contact get(int index) {
		return contacts.get(index);
	}

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

	public boolean save() throws IOException {
		return saveAs(file, format);
	}

	public String toString(Format format) {
		return Parser.toString(this, format);
	}

	public String toString() {
		return toString(Format.VCard21);
	}

	protected void load(String serializedContent, Format format) {
		// note: fixed size array
		List<String> lines = Arrays.asList(serializedContent.split("\n"));
		load(lines, format);
	}

	protected void load(List<String> lines, Format format) {
		this.contacts = Parser.parse(lines, format);
		setDirty();

		for (Contact contact : contacts) {
			contact.setParent(this);
		}
	}

	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Return the name of this card.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
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
