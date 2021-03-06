package be.nikiroo.jvcard;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link Data} is a piece of information present in a {@link Contact}. It is
 * basically a key/value pair with optional types and an optional group name.
 * <p>
 * A {@link Data} can also be binary encoded: in this case, it has an associated
 * BKey number to identify it.
 * 
 * @author niki
 */
public class Data extends BaseClass<TypeInfo> {
	public enum DataPart {
		FN_FAMILY, FN_GIVEN, FN_ADDITIONAL, // Name
		FN_PRE, FN_POST, // Pre/Post
		BDAY_YYYY, BDAY_MM, BDAY_DD, // BDay
		// Address:
		ADR_PBOX, ADR_EXTENDED, ADR_STREET, ADR_CITY, ADR_REGION, ADR_POSTAL_CODE, ADR_COUNTRY
	}

	private String name;
	private String value;
	private String group;
	private int b64; // -1 = no, 0 = still not ordered, the rest is order

	/**
	 * Create a new {@link Data} with the given values.
	 * 
	 * @param types
	 *            the types of this {@link Data}
	 * @param name
	 *            its name (<b>MUST NOT</b> be NULL)
	 * @param value
	 *            its value (<b>MUST NOT</b> be NULL)
	 * @param group
	 *            its group if any (or NULL if none)
	 */
	public Data(List<TypeInfo> types, String name, String value, String group) {
		super(types);

		this.name = name.toUpperCase();
		this.value = value.toString(); // crash NOW if null
		this.group = group;

		b64 = -1;
		for (TypeInfo type : this) {
			if (type.getName().equals("ENCODING")
					&& type.getValue().equals("b")) {
				b64 = 0;
				break;
			}
		}
	}

	/**
	 * Return the name of this {@link Data}
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value of this {@link Data}
	 * 
	 * @return the value
	 */
	public String getValue() {
		return unescape(value);
	}

	/**
	 * Change the value of this {@link Data}
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(String value) {
		setRawValue(escape(value));
	}

	/**
	 * Return the raw value of this {@link Data}
	 * 
	 * @return the raw value
	 */
	public String getRawValue() {
		return value;
	}

	/**
	 * Change the raw value of this {@link Data}
	 * 
	 * @param value
	 *            the new raw value
	 */
	public void setRawValue(String value) {
		if ((value == null && this.value != null)
				|| (value != null && !value.equals(this.value))) {
			this.value = value;
			setDirty();
		}
	}

	/**
	 * Return the {@link List} of comma-listed values from this {@link Data}.
	 * 
	 * @return the {@link List} of values
	 */
	public List<String> getValues() {
		return getList(',');
	}

	/**
	 * Set the {@link List} of comma-listed values from this {@link Data}.
	 * 
	 * @param values
	 *            the {@link List} of values
	 */
	public void setValues(List<String> values) {
		setList(values, ',');
	}

	/**
	 * Return the {@link List} of semi-column-listed fields from this
	 * {@link Data}.
	 * 
	 * @return the {@link List} of values
	 */
	public List<String> getFields() {
		return getList(';');
	}

	/**
	 * Set the {@link List} of comma-listed values from this {@link Data}.
	 * 
	 * @param values
	 *            the {@link List} of values
	 */
	public void setFields(List<String> values) {
		setList(values, ';');
	}

	/**
	 * Return the group of this {@link Data}
	 * 
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Change the group of this {@link Data}
	 * 
	 * @param group
	 *            the new group
	 */
	public void setGroup(String group) {
		if ((group == null && this.group != null)
				|| (group != null && !group.equals(this.group))) {
			this.group = group;
			setDirty();
		}
	}

	/**
	 * Return the bkey number of this {@link Data} or -1 if it is not binary.
	 * <p>
	 * For binary data, as long as the BKey is not processed, it will be 0.
	 * 
	 * @return the bkey, 0 or -1
	 */
	public int getB64Key() {
		return b64;
	}

	/**
	 * Check if this {@link Data} is binary (in this case, the BKey will be
	 * present).
	 * 
	 * @return TRUE if it is
	 */
	public boolean isBinary() {
		return b64 >= 0;
	}

	/**
	 * Return the preferred value of this {@link Data}, or
	 * {@link Integer#MAX_VALUE} if none.
	 * 
	 * @return the preferred value
	 */
	public int getPreferred() {
		for (TypeInfo type : this) {
			if (type.getName().equals("PRE")) {
				try {
					return Integer.parseInt(type.getValue());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}

		return Integer.MAX_VALUE;
	}

	/**
	 * Change the bkey of this {@link Data}
	 * 
	 * @param i
	 *            the new bkey
	 * 
	 * @throw InvalidParameterException if the {@link Data} is not binary or if
	 *        it is but you try to set a negative bkey
	 */
	void resetB64Key(int i) {
		if (!isBinary())
			throw new InvalidParameterException(
					"Cannot add a BKey on a non-binary object");
		if (i < 0)
			throw new InvalidParameterException(
					"Cannot remove the BKey on a binary object");

		b64 = i;
	}

	/**
	 * Return the {@link List} of sep-listed values from this {@link String}
	 * data.
	 * <p>
	 * Will take the backslash character into account (i.e., a backslash can
	 * escape the given separator).
	 * 
	 * @param value
	 *            the data
	 * 
	 * @param the
	 *            separator
	 * 
	 * @return the {@link List} of values
	 */
	private List<String> getList(char sep) {
		List<String> rep = new LinkedList<String>();

		if (value != null && value.length() > 0) {
			int last = 0;
			for (int i = 0; i < value.length(); i++) {
				if (value.charAt(i) == sep
						&& (i == 0 || value.charAt(i - 1) != '\\')) {
					rep.add(value.substring(last, i));
					last = i + 1;
				}
			}

			if (last < value.length())
				rep.add(value.substring(last));
		}

		return rep;
	}

	/**
	 * Create the {@link String}-encoded {@link List} of sep-listed values from
	 * the given values.
	 * 
	 * @param values
	 *            the {@link List} of values
	 * 
	 * @param sep
	 *            the separator
	 * 
	 * @return the {@link String}
	 */
	private void setList(List<String> values, char sep) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (String value : values) {
			if (!first)
				builder.append(sep);

			builder.append(escape(value));

			first = false;
		}

		value = builder.toString();
	}

	@Override
	public String getId() {
		return "" + name;
	}

	@Override
	public String getState() {
		return ("" + name + value + group).replace(' ', '_');
	}

	@Override
	public String toString() {
		String out = name + ": " + value;
		if (group != null && !group.isEmpty()) {
			out += " (" + group + ")";
		}
		if (b64 >= 0) {
			out += " [" + b64 + "]";
		}

		return out;
	}
}
