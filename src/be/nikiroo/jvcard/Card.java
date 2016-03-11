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
	private long lastModified;
	private boolean remote;

	/**
	 * Create a new {@link Card} from the given {@link File} and {@link Format}.
	 * 
	 * @param file
	 *            the input {@link File} containing the {@link Card} data or
	 *            NULL for an empty card (usually a {@link File} name or a
	 *            network path)
	 * @param format
	 *            the {@link Format} to use to parse it
	 * 
	 * @throws IOException
	 *             in case of IO error
	 * @throws InvalidParameterException
	 *             if format is NULL
	 */
	public Card(File file, Format format) throws IOException {
		this(load(file, format));

		if (file != null) {
			if (file.exists()) {
				lastModified = file.lastModified();
			}
		}

		this.format = format;

		if (file != null) {
			this.file = file;
			switch (format) {
			case VCard21:
				this.name = file.getName().replaceAll(
						".[vV][cC][fF]$", "");
				break;
			case Abook:
			default:
				this.name = file.getName();
				break;
			}
		}
	}

	/**
	 * Create a new {@link Card} from the given {@link Contact}s.
	 * 
	 * @param contacts
	 *            the input contacts
	 * 
	 * @throws IOException
	 *             in case of IO error
	 * @throws InvalidParameterException
	 *             if format is NULL
	 */
	public Card(List<Contact> contacts) throws IOException {
		super(contacts);

		lastModified = -1;
	}

	/**
	 * Save the {@link Card} to the given {@link File} with the given
	 * {@link Format}.
	 * 
	 * @param output
	 *            the output to save to
	 * @param format
	 *            the {@link Format} to use
	 * 
	 * @return TRUE if it was saved
	 * 
	 * @throws IOException
	 *             in case of IO errors
	 */
	public boolean saveAs(File output, Format format) throws IOException {
		if (output == null)
			return false;

		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		writer.append(toString(format));
		writer.close();

		if (output.getCanonicalPath().equals(this.file.getCanonicalPath())) {
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
	 * Reload the data from the input.
	 * 
	 * @return TRUE if it was done
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public boolean reload() throws IOException {
		if (file == null)
			return false;

		this.replaceListContent(load(file, format));
		setPristine();
		return true;
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

	/**
	 * Return the original {@link Format} of the {@link Card}.
	 * 
	 * @return the {@link Format}
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Return the input which was used to open this {@link Card}.
	 * 
	 * @return the input
	 */
	public File getInput() {
		return file;
	}

	/**
	 * Return the date of the last modification for this {@link Card} (or -1 if
	 * unknown/new).
	 * 
	 * @return the last modified date
	 */
	public long getLastModified() {
		return lastModified;
	}

	/**
	 * Check if this {@link Card} is remote.
	 * 
	 * @return TRUE if this {@link Card} is remote
	 */
	public boolean isRemote() {
		return remote;
	}

	/**
	 * Set the remote option on this {@link Card}.
	 * 
	 * @param remote
	 *            TRUE if this {@link Card} is remote
	 */
	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	@Override
	public String toString() {
		return toString(Format.VCard21);
	}

	/**
	 * Load the data from the given {@link File} under the given {@link Format}.
	 * 
	 * @param file
	 *            the input to load from
	 * @param format
	 *            the {@link Format} to load as
	 * 
	 * @return the list of elements
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	private static List<Contact> load(File file, Format format)
			throws IOException {
		List<String> lines = null;

		if (file != null && file.exists()) {
			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), "UTF-8"));
			lines = new LinkedList<String>();
			for (String line = buffer.readLine(); line != null; line = buffer
					.readLine()) {
				lines.add(line);
			}
			buffer.close();
		}

		if (lines == null)
			return new LinkedList<Contact>();

		return Parser.parse(lines, format);
	}
}
