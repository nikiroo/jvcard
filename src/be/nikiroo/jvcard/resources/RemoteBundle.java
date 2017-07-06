package be.nikiroo.jvcard.resources;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.utils.resources.Bundle;

/**
 * This class manages the display configuration of the application.
 * 
 * @author niki
 * 
 */
public class RemoteBundle extends Bundle<RemotingOption> {
	public RemoteBundle() {
		super(RemotingOption.class, Target.remote, null);
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		RemotingOption.writeHeader(writer);
	}
}
