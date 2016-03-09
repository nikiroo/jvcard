package be.nikiroo.jvcard;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
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
public class Card extends BaseClass<Contact> {
	private File file;
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
		super(load(file, format));

		this.file = file;
		this.format = format;
		this.name = file.getName();
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
	 * Load the data from the given {@link File} under the given {@link Format}.
	 * 
	 * @param file
	 *            the {@link File} to load from
	 * @param format
	 *            the {@link Format} to load as
	 * 
	 * @return the list of elements
	 * @throws IOException
	 *             in case of IO error
	 */
	static private List<Contact> load(File file, Format format)
			throws IOException {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "UTF-8"));
		List<String> lines = new LinkedList<String>();
		for (String line = buffer.readLine(); line != null; line = buffer
				.readLine()) {
			lines.add(line);
		}
		buffer.close();

		return Parser.parse(lines, format);
	}
}
