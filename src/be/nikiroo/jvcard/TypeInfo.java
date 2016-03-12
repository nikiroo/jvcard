package be.nikiroo.jvcard;

/**
 * This class describes a type, that is, a key-value pair.
 * 
 * @author niki
 *
 */
@SuppressWarnings("rawtypes")
public class TypeInfo extends BaseClass {
	private String name;
	private String value;

	@SuppressWarnings("unchecked")
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

	@Override
	public String getId() {
		return "" + name;
	}

	@Override
	public String getState() {
		return "" + name + value;
	}
}