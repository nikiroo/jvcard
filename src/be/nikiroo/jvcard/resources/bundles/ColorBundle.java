package be.nikiroo.jvcard.resources.bundles;

import java.io.IOException;
import java.io.Writer;

import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.resources.Bundles.Bundle;
import be.nikiroo.jvcard.resources.Bundles.Target;
import be.nikiroo.jvcard.resources.enums.ColorOption;

/**
 * All colour information must come from here.
 * 
 * @author niki
 * 
 */
public class ColorBundle extends Bundle<ColorOption> {
	public ColorBundle() {
		new Bundles().super(ColorOption.class, Target.colors);
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		ColorOption.writeHeader(writer);
	}

	@Override
	protected void writeValue(Writer writer, ColorOption id) throws IOException {
		writer.write(id.name() + "_FG");
		writer.write(" = ");
		if (map.containsKey(id.name() + "_FG"))
			writer.write(map.getString(id.name() + "_FG").trim());

		writer.write("\n");

		writer.write(id.name() + "_BG");
		writer.write(" = ");
		if (map.containsKey(id.name() + "_BG"))
			writer.write(map.getString(id.name() + "_BG").trim());

		writer.write("\n");
	}
}
