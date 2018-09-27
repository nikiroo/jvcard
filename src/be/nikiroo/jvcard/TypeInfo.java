package be.nikiroo.jvcard;

/**
 * This class describes a type, that is, a key-value pair.
 * 
 * @author niki
 */
public class TypeInfo extends BaseClass<TypeInfo> {
	private String name;
	private String value;

	/**
	 * Create a new {@link TypeInfo}.
	 * 
	 * @param name
	 *            the name of this {@link TypeInfo} (<b>MUST NOT</b> be NULL)
	 * @param value
	 *            its value (<b>MUST NOT</b> be NULL)
	 */
	public TypeInfo(String name, String value) {
		super(null);

		this.name = name.toUpperCase();
		this.value = escape(value.toString()); // crash NOW if null
	}

	/**
	 * Return the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return unescape(value);
	}

	/**
	 * Return the RAW value
	 * 
	 * @return the RAW value
	 */
	public String getRawValue() {
		return value;
	}

	@Override
	public String getId() {
		return "" + name;
	}

	@Override
	public String getState() {
		return ("" + name + value).replace(' ', '_');
	}
}
