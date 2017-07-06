package be.nikiroo.jvcard.resources;

import java.io.IOException;
import java.io.Writer;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import be.nikiroo.utils.resources.Bundle;

/**
 * All colour information must come from here.
 * <p>
 * TODO: delete this class, and think about a better way to get BG/FG colours...
 * 
 * @author niki
 */
public class ColorBundle extends Bundle<ColorOption> {
	public ColorBundle() {
		super(ColorOption.class, Target.colors, null);
	}

	@Override
	protected void writeHeader(Writer writer) throws IOException {
		ColorOption.writeHeader(writer);
	}

	@Override
	protected void writeValue(Writer writer, ColorOption id) throws IOException {
		String name = id.name() + "_FG";
		String value = "";
		if (containsKey(name))
			value = getString(name).trim();

		writeValue(writer, name, value);

		name = id.name() + "_BG";
		value = "";
		if (containsKey(name))
			value = getString(name).trim();

		writeValue(writer, name, value);
	}

	@Override
	protected void resetMap(ResourceBundle bundle) {
		// this.map.clear();

		if (bundle != null) {
			for (ColorOption field : type.getEnumConstants()) {
				try {
					// String value = bundle.getString(field.name());
					// this.map.put(field.name(), value == null ? null :
					// value.trim());
					setString(field.name() + "_FG",
							bundle.getString(field.name() + "_FG"));
					setString(field.name() + "_BG",
							bundle.getString(field.name() + "_BG"));
				} catch (MissingResourceException e) {
				}
			}
		}
	}

	@Override
	public String getStringX(ColorOption id, String suffix) {
		String key = id.name()
				+ (suffix == null ? "" : "_" + suffix.toUpperCase());

		return getString(key);
	}
}
