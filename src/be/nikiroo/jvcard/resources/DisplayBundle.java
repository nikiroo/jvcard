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
public class DisplayBundle extends Bundle<DisplayOption> {
	public DisplayBundle() {
		super(DisplayOption.class, Target.display, null);
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		DisplayOption.writeHeader(writer);
	}
}
