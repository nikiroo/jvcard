package be.nikiroo.jvcard.parsers;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;

public class AbookParser {
	public static List<Contact> parse(List<String> lines) {
		List<Contact> contacts = new LinkedList<Contact>();

		for (String line : lines) {
			List<Data> content = new LinkedList<Data>();

			String tab[] = line.split("\t");

			if (tab.length >= 1)
				content.add(new Data(null, "NICKNAME", tab[0].trim(), null));
			if (tab.length >= 2)
				content.add(new Data(null, "FN", tab[1].trim(), null));
			if (tab.length >= 3)
				content.add(new Data(null, "EMAIL", tab[2].trim(), null));
			if (tab.length >= 4)
				content.add(new Data(null, "X-FCC", tab[3].trim(), null));
			if (tab.length >= 5)
				content.add(new Data(null, "NOTE", tab[4].trim(), null));

			contacts.add(new Contact(content));
		}

		return contacts;
	}

	// -1 = no bkeys
	public static String toString(Contact contact, int startingBKey) {
		// BKey is not used in pine mode

		StringBuilder builder = new StringBuilder();

		String nick = contact.getPreferredDataValue("NICKNAME");
		if (nick != null) {
			nick = nick.replaceAll(" ", "_");
			nick = nick.replaceAll(",", "-");
			nick = nick.replaceAll("@", "(a)");
			nick = nick.replaceAll("\"", "'");
			nick = nick.replaceAll(";", ".");
			nick = nick.replaceAll(":", "=");
			nick = nick.replaceAll("[()\\[\\]<>\\\\]", "/");

			builder.append(nick);
		}

		builder.append('\t');

		String fn = contact.getPreferredDataValue("FN");
		if (fn != null)
			builder.append(fn);

		builder.append('\t');

		String email = contact.getPreferredDataValue("EMAIL");
		if (email != null)
			builder.append(email);

		// optional fields follow:

		String xfcc = contact.getPreferredDataValue("X-FCC");
		if (xfcc != null) {
			builder.append('\t');
			builder.append(xfcc);
		}

		String notes = contact.getPreferredDataValue("NOTE");
		if (notes != null) {
			if (xfcc == null)
				builder.append('\t');

			builder.append('\t');
			builder.append(notes);
		}

		// note: save as pine means normal LN, nor CRLN
		builder.append('\n');

		return builder.toString();
	}

	public static String toString(Card card) {
		StringBuilder builder = new StringBuilder();

		for (int index = 0; index < card.size(); index++) {
			builder.append(toString(card.get(index), -1));
		}

		return builder.toString();
	}
}
