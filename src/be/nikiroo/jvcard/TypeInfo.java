package be.nikiroo.jvcard;

public class TypeInfo {
	private String name;
	private String value;

	public TypeInfo(String name, String value) {
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