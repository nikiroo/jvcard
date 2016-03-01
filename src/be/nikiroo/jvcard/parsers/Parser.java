package be.nikiroo.jvcard.parsers;

import java.security.InvalidParameterException;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;

public class Parser {

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
	public static String toString(Contact contact, Format format, int startingBKey) {
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
				int bkey = Integer.parseInt(data.getValue().replace("<HIDDEN_",
						"").replace(">", ""));
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
