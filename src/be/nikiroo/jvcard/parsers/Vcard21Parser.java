package be.nikiroo.jvcard.parsers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;

public class Vcard21Parser {
	public static List<Contact> parse(Iterable<String> textData) {
		Iterator<String> lines = textData.iterator();
		List<Contact> contacts = new LinkedList<Contact>();
		List<Data> datas = null;

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
				datas = new LinkedList<Data>();
			} else if (line.equals("END:VCARD")) {
				if (datas == null) {
					// BAD INPUT FILE. IGNORE.
					System.err
							.println("VCARD Parser warning: END:VCARD seen before any VCARD:BEGIN");
				} else {
					contacts.add(new Contact(datas));
				}
			} else {
				if (datas == null) {
					// BAD INPUT FILE. IGNORE.
					System.err
							.println("VCARD Parser warning: data seen before any VCARD:BEGIN");
				} else {
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
									String tname = tab[i]
											.substring(0, equIndex);
									String tvalue = tab[i]
											.substring(equIndex + 1);
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
			}
		}

		return contacts;
	}

	// -1 = no bkeys
	public static String toString(Contact contact, int startingBKey) {
		StringBuilder builder = new StringBuilder();

		builder.append("BEGIN:VCARD");
		builder.append("\r\n");
		builder.append("VERSION:2.1");
		builder.append("\r\n");
		for (int indexData = 0; indexData < contact.size(); indexData++) {
			Data data = contact.get(indexData);
			if (data.getGroup() != null && !data.getGroup().trim().equals("")) {
				builder.append(data.getGroup().trim());
				builder.append('.');
			}
			builder.append(data.getName());
			for (int indexType = 0; indexType < data.size(); indexType++) {
				TypeInfo type = data.get(indexType);
				builder.append(';');
				builder.append(type.getName());
				if (type.getValue() != null
						&& !type.getValue().trim().equals("")) {
					builder.append('=');
					builder.append(type.getValue());
				}
			}
			builder.append(':');

			// TODO: bkey!
			builder.append(data.getValue());
			builder.append("\r\n");
		}
		builder.append("END:VCARD");
		builder.append("\r\n");

		return builder.toString();
	}

	public static String toString(Card card) {
		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < card.size(); index++) {
			builder.append(toString(card.get(index), -1));
		}

		builder.append("\r\n");

		return builder.toString();
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
