package be.nikiroo.jvcard.resources.enums;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Bundles.Bundle;
import be.nikiroo.jvcard.resources.Meta;

public enum DisplayOption {
	@Meta(what = "", where = "", format = "coma-separated list of CLF", info = "The format of each line in the contact list")
	CONTACT_LIST_FORMAT, //
	@Meta(what = "", where = "", format = "CDIF", info = "The list of details to show in View Contact mode")
	CONTACT_DETAILS_INFO, //
	@Meta(what = "", where = "", format = "Integer or nothing for auto", info = "The size of the details' labels")
	CONTACT_DETAILS_LABEL_WIDTH, //
	@Meta(what = "", where = "", format = "CLF", info = "The default value of FN if it is not present")
	CONTACT_DETAILS_DEFAULT_FN, //
	@Meta(what = "", where = "", format = "TRUE or FALSE", info = "TRUE to force all FNs to be recreated from CONTACT_DETAILS_DEFAULT_FN")
	CONTACT_DETAILS_SHOW_COMPUTED_FN, //

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
		writer.write("# Display options configuration\n");
		writer.write("#\n");
		writer.write("# The Contact List Format (CLF) is basically a list of VCF field names\n");
		writer.write("# separated by a pipe and optionally parametrised.\n");
		writer.write("# The parameters allows you to:\n");
		writer.write("# - @x: (the 'x' is the letter 'x') show only a present/not present info\n");
		writer.write("# - @n: limit the size to a fixed value 'n'\n");
		writer.write("# - @+: expand the size of this field as much as possible\n");
		writer.write("#\n");
		writer.write("# In case of lists or multiple-fields values, you can select a specific\n");
		writer.write("# list or field with:\n");
		writer.write("# - FIELD@(0): select the first value in a list\n");
		writer.write("# - FIELD@[1]: select the second field in a multiple-fields value\n");
		writer.write("#\n");
		writer.write("# You can also add a fixed text if it starts with a simple-quote (').\n");
		writer.write("#\n");
		writer.write("# Example: \"'Contact: |N@10|FN@20|NICK@+|PHOTO@x\"\n");
		writer.write("# \n");
		writer.write("# \n");
		writer.write("# The Contact Details Info Format (CDIF):\n");
		writer.write("# - Each detail (separated by a pipe \"|\" character) is visible on its own line\n");
		writer.write("# - It is made up of two parts: the label and the linked VCF field (optional),\n");
		writer.write("# 		separated by an equal \"=\", sharp \"#\", plus \"+\" or asterisk \"*\" sign\n");
		writer.write("# - \"=FIELD\" will take the preferred value for this field\n");
		writer.write("# - \"+FIELD\" will take the preferred value for this field and highlight it\n");
		writer.write("# - \"#FIELD\" will take all the values with this field's name\n");
		writer.write("# - \"*FIELD\" will take all the values with this field's name, highlighting the preferred one\n");
		writer.write("#\n");
		writer.write("# Example:\n");
		writer.write("# 	CONTACT_DETAILS_INFO = Phone:=TEL|eMail:=EMAIL\n");
		writer.write("#\n");
		writer.write("# This will print two lines:\n");
		writer.write("# 	Phone: +32 888 88 88 88\n");
		writer.write("# 	eMail: nobody@nowhere.com\n");
		writer.write("#\n");
	}
}
