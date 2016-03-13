package be.nikiroo.jvcard.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;

public class Parser {

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
	public static List<Contact> parse(File file, Format format)
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

		return parse(lines, format);
	}

	/**
	 * Load the given data from under the given {@link Format}.
	 * 
	 * @param lines
	 *            the input to load from
	 * @param format
	 *            the {@link Format} to load as
	 * 
	 * @return the list of elements
	 */
	public static List<Contact> parse(List<String> lines, Format format) {
		switch (format) {
		case VCard21:
			return Vcard21Parser.parse(lines);
		case Abook:
			return AbookParser.parse(lines);

		default:
			throw new InvalidParameterException("Unknown format: "
					+ format.toString());
		}
	}

	// -1 = no bkeys
	public static String toString(Card card, Format format) {
		switch (format) {
		case VCard21:
			return Vcard21Parser.toString(card);
		case Abook:
			return AbookParser.toString(card);

		default:
			throw new InvalidParameterException("Unknown format: "
					+ format.toString());
		}
	}

	// -1 = no bkeys
	public static String toString(Contact contact, Format format,
			int startingBKey) {
		switch (format) {
		case VCard21:
			return Vcard21Parser.toString(contact, startingBKey);
		case Abook:
			return AbookParser.toString(contact, startingBKey);

		default:
			throw new InvalidParameterException("Unknown format: "
					+ format.toString());
		}
	}

	// return -1 if no bkey
	public static int getBKey(Data data) {
		if (data.isBinary() && data.getValue().startsWith("<HIDDEN_")) {
			try {
				int bkey = Integer.parseInt(data.getValue()
						.replace("<HIDDEN_", "").replace(">", ""));
				if (bkey < 0)
					throw new InvalidParameterException(
							"All bkeys MUST be positive");
				return bkey;
			} catch (NumberFormatException nfe) {
			}
		}

		return -1;
	}

	static String generateBKeyString(int bkey) {
		if (bkey < 0)
			throw new InvalidParameterException("All bkeys MUST be positive");

		return "<HIDDEN_" + bkey + ">";
	}
}
