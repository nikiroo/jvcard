package be.nikiroo.jvcard.parsers;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;

public class Vcard21Parser {
	public static List<Contact> parse(List<String> lines) {
		List<Contact> contacts = new LinkedList<Contact>();
		List<Data> datas = null;

		for (String l : lines) {
			String line = l.trim();
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
						String rest = line.split(":")[0];
						value = line.substring(rest.length() + 1);

						if (rest.contains(";")) {
							String tab[] = rest.split(";");
							name = tab[0];

							for (int i = 1; i < tab.length; i++) {
								if (tab[i].contains("=")) {
									String tname = tab[i].split("=")[0];
									String tvalue = tab[i].substring(tname
											.length() + 1);
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
						group = name.split("\\.")[0];
						name = name.substring(group.length() + 1);
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
		for (Data data : contact.getContent()) {
			if (data.getGroup() != null && !data.getGroup().trim().equals("")) {
				builder.append(data.getGroup().trim());
				builder.append('.');
			}
			builder.append(data.getName());
			for (TypeInfo type : data.getTypes()) {
				builder.append(';');
				builder.append(type.getName());
				if (type.getValue() != null
						&& !type.getValue().trim().equals("")) {
					builder.append('=');
					builder.append(type.getValue());
				}
			}
			builder.append(':');
			
			//TODO: bkey!
			builder.append(data.getValue());
			builder.append("\r\n");
		}
		builder.append("END:VCARD");
		builder.append("\r\n");

		return builder.toString();
	}

	public static String toString(Card card) {
		StringBuilder builder = new StringBuilder();

		for (Contact contact : card.getContacts()) {
			builder.append(toString(contact, -1));
		}

		return builder.toString();
	}
}
