package be.nikiroo.jvcard.resources.enums;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Meta;
import be.nikiroo.jvcard.resources.Bundles.Bundle;

public enum RemotingOption {
	@Meta(what = "", where = "Server", format = "directory", info = "when starting as a jVCard remote server, where to look for data")
	SERVER_DATA_PATH, //

	@Meta(what = "", where = "Client", format = "directory", info = "when loading \"jvcard://\" links, where to save cache files")
	CLIENT_CACHE_DIR, //
	@Meta(what = "", where = "Client", format = "TRUE or FALSE", info = "Automatically synchronise remote cards")
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
