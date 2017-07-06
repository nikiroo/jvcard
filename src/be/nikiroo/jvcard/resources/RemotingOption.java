package be.nikiroo.jvcard.resources;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.utils.resources.Meta;
import be.nikiroo.utils.resources.Meta.Format;

public enum RemotingOption {
	@Meta(format = Format.DIRECTORY, description = "when starting as a jVCard remote server, where to look for data")
	SERVER_DATA_PATH, //

	@Meta(format = Format.DIRECTORY, description = "when loading \"jvcard://\" links, where to save cache files")
	CLIENT_CACHE_DIR, //
	@Meta(format = Format.BOOLEAN, description = "Automatically synchronise remote cards")
	CLIENT_AUTO_SYNC, //

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
		writer.write("# Remote configuration (client and server)\n");
		writer.write("#\n");
	}
}
