package be.nikiroo.jvcard;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

public class Data {
	private String name;
	private String value;
	private String group;
	private int b64; // -1 = no, 0 = still not ordered, the rest is order
	private List<TypeInfo> types;
	private boolean dirty;
	private Contact parent;

	public Data(List<TypeInfo> types, String name, String value, String group) {
		if (types == null) {
			types = new LinkedList<TypeInfo>();
		}

		this.types = types;
		this.name = name;
		this.value = value;
		this.group = group;

		b64 = -1;
		for (TypeInfo type : types) {
			if (type.getName().equals("ENCODING")
					&& type.getValue().equals("b")) {
				b64 = 0;
				break;
			}
		}
	}

	public List<TypeInfo> getTypes() {
		return types;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getGroup() {
		return group;
	}

	public int getB64Key() {
		return b64;
	}

	void resetB64Key(int i) {
		if (!isBinary())
			throw new InvalidParameterException(
					"Cannot add a BKey on a non-binary object");
		if (i < 0)
			throw new InvalidParameterException(
					"Cannot remove the BKey on a binary object");

		b64 = i;
	}

	public boolean isBinary() {
		return b64 >= 0;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setParent(Contact parent) {
		this.parent = parent;
	}
}
