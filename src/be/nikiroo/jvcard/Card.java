package be.nikiroo.jvcard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Parser;

/**
 * A card is a contact information card. It contains data about one or more
 * contacts.
 * 
 * @author niki
 */
public class Card extends BaseClass<Contact> {
	private File file;
	private String name;
	private Format format;
	private long lastModified;

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
		this(Parser.parseContact(file, format));

		if (file != null && file.exists()) {
			lastModified = file.lastModified();
		}

		this.format = format;

		if (file != null) {
			this.file = file;
			switch (format) {
			case VCard21:
				this.name = file.getName().replaceAll(".[vV][cC][fF]$", "");
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
	public Card(List<Contact> contacts) {
		super(contacts);

		lastModified = -1;
	}

	/**
	 * Save the {@link Card} to the given {@link File} with the given
	 * {@link Format}.
	 * 
	 * @param file
	 *            the output to save to
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
		try {
			Parser.write(writer, format, this);
		} finally {
			writer.close();
		}

		if (this.file != null
				&& file.getCanonicalPath().equals(this.file.getCanonicalPath())) {
			lastModified = file.lastModified();
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

		this.replaceListContent(Parser.parseContact(file, format));
		lastModified = file.lastModified();
		setPristine();

		return true;
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
	 * Return the {@link File} which was used to open this {@link Card}.
	 * 
	 * @return the input
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Break the link between this {@link Card} and he {@link File} which was
	 * used to open it if any.
	 */
	public void unlink() {
		file = null;
		lastModified = -1;
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

	@Override
	public String toString() {
		return "[Card: " + name + "]";
	}

	@Override
	public String getId() {
		return "" + name;
	}

	@Override
	public String getState() {
		return ("" + name + format).replace(' ', '_');
	}
}
