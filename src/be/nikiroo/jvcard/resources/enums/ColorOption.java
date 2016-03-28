package be.nikiroo.jvcard.resources.enums;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Meta;
import be.nikiroo.jvcard.resources.Bundles.Bundle;

/**
 * Represent an element that can be coloured (foreground/background colours).
 * 
 * @author niki
 *
 */
public enum ColorOption {
	@Meta(what = "", where = "", format = "colour", info = "")
	DEFAULT, //

	@Meta(what = "", where = "", format = "colour", info = "")
	TITLE_MAIN, //
	@Meta(what = "", where = "", format = "colour", info = "")
	TITLE_VARIABLE, //
	@Meta(what = "", where = "", format = "colour", info = "")
	TITLE_COUNT, //

	@Meta(what = "", where = "", format = "colour", info = "")
	ACTION_KEY, //
	@Meta(what = "", where = "", format = "colour", info = "")
	ACTION_DESC, //

	@Meta(what = "", where = "", format = "colour", info = "")
	LINE_MESSAGE, //
	@Meta(what = "", where = "", format = "colour", info = "")
	LINE_MESSAGE_ERR, //
	@Meta(what = "", where = "", format = "colour", info = "")
	LINE_MESSAGE_QUESTION, //
	@Meta(what = "", where = "", format = "colour", info = "")
	LINE_MESSAGE_ANS, //

	@Meta(what = "", where = "", format = "colour", info = "")
	CONTACT_LINE, //
	@Meta(what = "", where = "", format = "colour", info = "")
	CONTACT_LINE_SEPARATOR, //
	@Meta(what = "", where = "", format = "colour", info = "")
	CONTACT_LINE_SELECTED, //
	@Meta(what = "", where = "", format = "colour", info = "")
	CONTACT_LINE_SEPARATOR_SELECTED, //
	@Meta(what = "", where = "", format = "colour", info = "")
	CONTACT_LINE_DIRTY, //
	@Meta(what = "", where = "", format = "colour", info = "")
	CONTACT_LINE_DIRTY_SELECTED, //

	@Meta(what = "", where = "", format = "colour", info = "")
	VIEW_CONTACT_NAME, //
	@Meta(what = "", where = "", format = "colour", info = "")
	VIEW_CONTACT_NORMAL, //
	@Meta(what = "", where = "", format = "colour", info = "")
	VIEW_CONTACT_HIGHLIGHT, //
	@Meta(what = "", where = "", format = "colour", info = "")
	VIEW_CONTACT_NOTES_TITLE, //

	;

	/**
	 * Write the header found in the configuration <tt>.properties</tt> file of
	 * this {@link Bundle}.
	 * 
	 * @param writer
	 *            the {@link Writer} to write the header in
	 * 
	 * @throws IOException
	 *             in case of IO error
	 */
	static public void writeHeader(Writer writer) throws IOException {
		writer.write("# Application colours\n");
		writer.write("# \n");
		writer.write("# Note that you can define a colour in one of those 3 ways:\n");
		writer.write("# - WHITE: one of the ANSI colour names, in upper case\n");
		writer.write("# - @RRGGBB: a RGB code we will try to match using one of the 256 Terminal colours\n");
		writer.write("# - #RRGGBB: an exact RGB colour (please make sure your terminal supports this)\n");
		writer.write("# - 255: one of the 256 indexed colours of the terminal (the 16 first colours are theme-based) \n");
		writer.write("# \n");
		writer.write("# ...and thus either for xxx_FG (foreground colour) or xxx_BG (background colour)\n");
		writer.write("# \n");
	}
}