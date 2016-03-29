package be.nikiroo.jvcard.parsers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;

public class Vcard21Parser {
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
	public static List<Contact> parseContact(Iterable<String> textData) {
		Iterator<String> lines = textData.iterator();
		List<Contact> contacts = new LinkedList<Contact>();
		List<String> datas = null;

		String nextRawLine = null;
		if (lines.hasNext()) {
			nextRawLine = lines.next();
			while (lines.hasNext() && isContinuation(nextRawLine)) {
				// BAD INPUT FILE. IGNORE.
				System.err
						.println("VCARD Parser warning: CONTINUATION line seen before any data line");
				nextRawLine = lines.next();
			}
		}

		while (nextRawLine != null) {
			StringBuilder rawLine = new StringBuilder(nextRawLine.trim());
			if (lines.hasNext())
				nextRawLine = lines.next();
			else
				nextRawLine = null;

			while (isContinuation(nextRawLine)) {
				rawLine.append(nextRawLine.trim());
				if (lines.hasNext())
					nextRawLine = lines.next();
				else
					nextRawLine = null;
			}

			String line = rawLine.toString();
			if (line.equals("BEGIN:VCARD")) {
				datas = new LinkedList<String>();
			} else if (line.equals("END:VCARD")) {
				if (datas == null) {
					// BAD INPUT FILE. IGNORE.
					System.err
							.println("VCARD Parser warning: END:VCARD seen before any VCARD:BEGIN");
				} else {
					contacts.add(new Contact(parseData(datas)));
				}
			} else {
				if (datas == null) {
					// BAD INPUT FILE. IGNORE.
					System.err
							.println("VCARD Parser warning: data seen before any VCARD:BEGIN");
				} else {
					datas.add(line);
				}
			}
		}

		return contacts;
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
	public static List<Data> parseData(Iterable<String> textData) {
		List<Data> datas = new LinkedList<Data>();

		for (String line : textData) {
			List<TypeInfo> types = new LinkedList<TypeInfo>();
			String name = "";
			String value = "";
			String group = "";

			if (line.contains(":")) {
				int colIndex = line.indexOf(':');
				String rest = line.substring(0, colIndex);
				value = line.substring(colIndex + 1);

				if (rest.contains(";")) {
					String tab[] = rest.split(";");
					name = tab[0];

					for (int i = 1; i < tab.length; i++) {
						if (tab[i].contains("=")) {
							int equIndex = tab[i].indexOf('=');
							String tname = tab[i].substring(0, equIndex);
							String tvalue = tab[i].substring(equIndex + 1);
							types.add(new TypeInfo(tname, tvalue));
						} else {
							types.add(new TypeInfo(tab[i], ""));
						}
					}
				} else {
					name = rest;
				}
			} else {
				name = line;
			}

			if (name.contains(".")) {
				int dotIndex = name.indexOf('.');
				group = name.substring(0, dotIndex);
				name = name.substring(dotIndex + 1);
			}

			datas.add(new Data(types, name, value, group));
		}

		return datas;
	}

	/**
	 * Write the given {@link Card} in the {@link Appendable}.
	 * 
	 * @param writer
	 *            the {@link Appendable}
	 * @param card
	 *            the {@link Card} to write
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public static void write(Appendable writer, Card card) throws IOException {
		for (Contact contact : card) {
			write(writer, contact, -1);
		}
	}

	/**
	 * Write the given {@link Contact} in the {@link Appendable}.
	 * 
	 * @param writer
	 *            the {@link Appendable}
	 * @param contact
	 *            the {@link Contact} to write
	 * @param startingBKey
	 *            the starting BKey number (all the other will follow) or -1 for
	 *            no BKey
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public static void write(Appendable writer, Contact contact,
			int startingBKey) throws IOException {

		writer.append("BEGIN:VCARD\r\n");
		writer.append("VERSION:2.1\r\n");
		for (Data data : contact) {
			write(writer, data);
		}
		writer.append("END:VCARD\r\n");
	}

	/**
	 * Write the given {@link Data} in the {@link Appendable}.
	 * 
	 * @param writer
	 *            the {@link Appendable}
	 * @param data
	 *            the {@link Data} to write
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	public static void write(Appendable writer, Data data) throws IOException {
		StringBuilder dataBuilder = new StringBuilder();
		if (data.getGroup() != null && !data.getGroup().trim().equals("")) {
			dataBuilder.append(data.getGroup().trim());
			dataBuilder.append('.');
		}
		dataBuilder.append(data.getName());
		for (TypeInfo type : data) {
			dataBuilder.append(';');
			dataBuilder.append(type.getName());
			if (type.getValue() != null && !type.getValue().trim().equals("")) {
				dataBuilder.append('=');
				dataBuilder.append(type.getRawValue());
			}
		}
		dataBuilder.append(':');

		// TODO: bkey!
		dataBuilder.append(data.getRawValue());

		// RFC says: Content lines SHOULD be folded to a maximum width of 75
		// octets -> since it is SHOULD, we will just cut it as 74/75 chars
		// depending if the last one fits in one char (note: chars != octet)
		int previous = 0;
		for (int index = 0; index < dataBuilder.length(); previous = index) {
			index += 74;
			if (previous > 0)
				index--; // the space takes 1
			if (dataBuilder.length() > index) {
				char car = dataBuilder.charAt(index - 1);
				// RFC forbids cutting a character in 2
				if (Character.isHighSurrogate(car)) {
					index++;
				}
			}

			index = Math.min(index, dataBuilder.length());
			if (previous > 0)
				writer.append(' ');
			writer.append(dataBuilder, previous, index);
			writer.append("\r\n");
		}
	}

	/**
	 * Clone the given {@link Card} by exporting then importing it again in VCF.
	 * 
	 * @param c
	 *            the {@link Card} to clone
	 * 
	 * @return the clone {@link Contact}
	 */
	public static Card clone(Card c) {
		try {
			File tmp = File.createTempFile("clone", ".vcf");
			c.saveAs(tmp, Format.VCard21);

			Card clone = new Card(tmp, Format.VCard21);
			clone.unlink();
			tmp.delete();

			return clone;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Clone the given {@link Contact} by exporting then importing it again in
	 * VCF.
	 * 
	 * @param c
	 *            the {@link Contact} to clone
	 * 
	 * @return the clone {@link Contact}
	 */
	public static Contact clone(Contact c) {
		try {
			File tmp = File.createTempFile("clone", ".vcf");
			FileWriter writer = new FileWriter(tmp);
			write(writer, c, -1);
			writer.close();

			Card clone = new Card(tmp, Format.VCard21);
			clone.unlink();
			tmp.delete();

			return clone.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Check if the given line is a continuation line or not.
	 * 
	 * @param line
	 *            the line to check
	 * 
	 * @return TRUE if the line is a continuation line
	 */
	private static boolean isContinuation(String line) {
		if (line != null && line.length() > 0)
			return (line.charAt(0) == ' ' || line.charAt(0) == '\t');
		return false;
	}
}
