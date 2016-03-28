package be.nikiroo.jvcard.resources.bundles;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.Bundles.Bundle;
import be.nikiroo.jvcard.resources.Bundles.Target;
import be.nikiroo.jvcard.resources.enums.DisplayOption;

/**
 * This class manages the display configuration of the application.
 * 
 * @author niki
 * 
 */
public class DisplayBundle extends Bundle<DisplayOption> {
	public DisplayBundle() {
		new Bundles().super(DisplayOption.class, Target.display);
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		DisplayOption.writeHeader(writer);
	}
}
