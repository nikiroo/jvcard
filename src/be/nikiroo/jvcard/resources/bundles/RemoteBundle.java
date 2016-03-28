package be.nikiroo.jvcard.resources.bundles;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.Bundles.Bundle;
import be.nikiroo.jvcard.resources.Bundles.Target;
import be.nikiroo.jvcard.resources.enums.RemotingOption;

/**
 * This class manages the display configuration of the application.
 * 
 * @author niki
 * 
 */
public class RemoteBundle extends Bundle<RemotingOption> {
	public RemoteBundle() {
		new Bundles().super(RemotingOption.class, Target.remote);
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		RemotingOption.writeHeader(writer);
	}
}
