package be.nikiroo.jvcard.parsers;

import java.util.Arrays;
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

	/**
	 * Return a {@link String} representation of the given {@link Card}, line by
	 * line.
	 * 
	 * <p>
	 * Note that the BKey is actually not used in Pine mode.
	 * </p>
	 * 
	 * @param card
	 *            the card to convert
	 * 
	 * @param startingBKey
	 *            the starting BKey number (all the other will follow) or -1 for
	 *            no BKey
	 * 
	 * @return the {@link String} representation
	 */
	public static List<String> toStrings(Contact contact, int startingBKey) {
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

		return Arrays.asList(new String[] { builder.toString() });
	}

	/**
	 * Return a {@link String} representation of the given {@link Card}, line by
	 * line.
	 * 
	 * @param card
	 *            the card to convert
	 * 
	 * @return the {@link String} representation
	 */
	public static List<String> toStrings(Card card) {
		List<String> lines = new LinkedList<String>();

		for (int index = 0; index < card.size(); index++) {
			lines.addAll(toStrings(card.get(index), -1));
		}

		return lines;
	}
}
