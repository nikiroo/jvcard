package be.nikiroo.jvcard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	public Card(File file, Format format) throws IOException {
		this.file = file;

		BufferedReader buffer = new BufferedReader(new FileReader(file));
		List<String> lines = new LinkedList<String>();
		for (String line = buffer.readLine(); line != null; line = buffer
				.readLine()) {
			lines.add(line);
		}

		load(lines, format);
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public boolean saveAs(File file, Format format) throws IOException {
		if (file == null)
			return false;

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.append(toString(format));
		writer.close();

		if (file.equals(this.file)) {
			dirty = false;
		}

		return true;
	}

	public boolean save(Format format, boolean bKeys) throws IOException {
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
	 * Notify that this element has unsaved changes.
	 */
	void setDirty() {
		dirty = true;
	}
}
