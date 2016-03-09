package be.nikiroo.jvcard;

/**
 * This class describes a type, that is, a key-value pair.
 * 
 * @author niki
 *
 */
@SuppressWarnings("rawtypes") // expected
public class TypeInfo extends BaseClass {
	private String name;
	private String value;

	@SuppressWarnings("unchecked") // expected
	public TypeInfo(String name, String value) {
		super(null);

		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}