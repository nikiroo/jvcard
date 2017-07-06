package be.nikiroo.jvcard.resources;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.utils.resources.Meta;
import be.nikiroo.utils.resources.Meta.Format;

/**
 * Represent an element that can be coloured (foreground/background colours).
 * 
 * @author niki
 * 
 */
public enum ColorOption {
	@Meta(format = Format.COLOR)
	DEFAULT, //

	@Meta(format = Format.COLOR)
	TITLE_MAIN, //
	@Meta(format = Format.COLOR)
	TITLE_VARIABLE, //
	@Meta(format = Format.COLOR)
	TITLE_COUNT, //

	@Meta(format = Format.COLOR)
	ACTION_KEY, //
	@Meta(format = Format.COLOR)
	ACTION_DESC, //

	@Meta(format = Format.COLOR)
	LINE_MESSAGE, //
	@Meta(format = Format.COLOR)
	LINE_MESSAGE_ERR, //
	@Meta(format = Format.COLOR)
	LINE_MESSAGE_QUESTION, //
	@Meta(format = Format.COLOR)
	LINE_MESSAGE_ANS, //

	@Meta(format = Format.COLOR)
	CONTACT_LINE, //
	@Meta(format = Format.COLOR)
	CONTACT_LINE_SEPARATOR, //
	@Meta(format = Format.COLOR)
	CONTACT_LINE_SELECTED, //
	@Meta(format = Format.COLOR)
	CONTACT_LINE_SEPARATOR_SELECTED, //
	@Meta(format = Format.COLOR)
	CONTACT_LINE_DIRTY, //
	@Meta(format = Format.COLOR)
	CONTACT_LINE_DIRTY_SELECTED, //

	@Meta(format = Format.COLOR)
	VIEW_CONTACT_NAME, //
	@Meta(format = Format.COLOR)
	VIEW_CONTACT_NORMAL, //
	@Meta(format = Format.COLOR)
	VIEW_CONTACT_HIGHLIGHT, //
	@Meta(format = Format.COLOR)
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