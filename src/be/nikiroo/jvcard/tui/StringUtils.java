package be.nikiroo.jvcard.tui;

import com.googlecode.lanterna.gui2.LinearLayout.Alignment;

public class StringUtils {

	static public String padString(String text, int width) {
		return padString(text, width, true, Alignment.Beginning);
	}

	// TODO: doc it, width of -1 == no change to text
	static public String padString(String text, int width, boolean cut,
			Alignment align) {

		if (width >= 0) {
			if (text == null)
				text = "";

			int diff = width - text.length();

			if (diff < 0) {
				if (cut)
					text = text.substring(0, width);
			} else if (diff > 0) {
				if (diff < 2 && align != Alignment.End)
					align = Alignment.Beginning;

				switch (align) {
				case Beginning:
					text = text + new String(new char[diff]).replace('\0', ' ');
					break;
				case End:
					text = new String(new char[diff]).replace('\0', ' ') + text;
					break;
				case Center:
				case Fill:
				default:
					int pad1 = (diff) / 2;
					int pad2 = (diff + 1) / 2;
					text = new String(new char[pad1]).replace('\0', ' ') + text
							+ new String(new char[pad2]).replace('\0', ' ');
					break;
				}
			}
		}

		return text;
	}

}
