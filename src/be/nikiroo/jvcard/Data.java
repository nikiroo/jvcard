package be.nikiroo.jvcard;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

/**
 * A data is a piece of information present in a {@link Contact}. It is
 * basically a key/value pair with optional types and an optional group name.
 * 
 * @author niki
 *
 */
public class Data {
	public enum DataPart {
		FN_FAMILY, FN_GIVEN, FN_ADDITIONAL, // Name
		FN_PRE, FN_POST, // Pre/Post
		BDAY_YYYY, BDAY_MM, BDAY_DD, // BDay
		ADR_PBOX, ADR_EXTENDED, ADR_STREET, ADR_CITY, ADR_REGION, ADR_POSTAL_CODE, ADR_COUNTRY
		// Address
	}

	private String name;
	private String value;
	private String group;
	private int b64; // -1 = no, 0 = still not ordered, the rest is order
	private List<TypeInfo> types;
	private boolean dirty;
	private Contact parent;

	/**
	 * Create a new {@link Data} with the given values.
	 * 
	 * @param types
	 *            the types of this {@link Data}
	 * @param name
	 *            its name
	 * @param value
	 *            its value
	 * @param group
	 *            its group if any
	 */
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
			type.setParent(this);
			if (type.getName().equals("ENCODING")
					&& type.getValue().equals("b")) {
				b64 = 0;
				break;
			}
		}
	}

	/**
	 * Return the number of {@link TypeInfo} present in this {@link Data}.
	 * 
	 * @return the number of {@link TypeInfo}s
	 */
	public int size() {
		return types.size();
	}

	/**
	 * Return the {@link TypeInfo} at index <i>index</i>.
	 * 
	 * @param index
	 *            the index of the {@link TypeInfo} to find
	 * 
	 * @return the {@link TypeInfo}
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is < 0 or >= {@link Data#size()}
	 */
	public TypeInfo get(int index) {
		return types.get(index);
	}

	/**
	 * Add a new {@link TypeInfo} in this {@link Data}.
	 * 
	 * @param type
	 *            the new type
	 */
	public void add(TypeInfo type) {
		type.setParent(this);
		type.setDirty();
		types.add(type);
	}

	/**
	 * Remove the given {@link TypeInfo} from its this {@link Data} if it is in.
	 * 
	 * @return TRUE in case of success
	 */
	public boolean remove(TypeInfo type) {
		if (types.remove(type)) {
			setDirty();
		}

		return false;
	}

	/**
	 * Change the {@link TypeInfo}s of this {@link Data}.
	 * 
	 * @param types
	 *            the new types
	 */
	@Deprecated
	public void setTypes(List<TypeInfo> types) {
		// TODO: check if this method is required
		this.types.clear();
		for (TypeInfo type : types) {
			this.types.add(type);
			type.setParent(this);
		}

		setDirty();
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
		return value;
	}

	/**
	 * Change the value of this {@link Data}
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(String value) {
		if ((value == null && this.value != null)
				|| (value != null && !value.equals(this.value))) {
			this.value = value;
			setDirty();
		}
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
	 * 
	 * @return the bkey or -1
	 */
	public int getB64Key() {
		return b64;
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
	 * Check if this {@link Data} is binary
	 * 
	 * @return TRUE if it is
	 */
	public boolean isBinary() {
		return b64 >= 0;
	}

	/**
	 * Delete this {@link Contact} from its parent {@link Card} if any.
	 * 
	 * @return TRUE in case of success
	 */
	public boolean delete() {
		if (parent != null) {
			return parent.remove(this);
		}

		return false;
	}

	/**
	 * Check if this {@link Data} has unsaved changes.
	 * 
	 * @return TRUE if it has
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Notify that this element has unsaved changes, and notify its parent of
	 * the same if any.
	 */
	protected void setDirty() {
		this.dirty = true;
		if (this.parent != null)
			this.parent.setDirty();
	}

	/**
	 * Notify this element <i>and all its descendants</i> that it is in pristine
	 * state (as opposed to dirty).
	 */
	void setPristine() {
		dirty = false;
		for (TypeInfo type : types) {
			type.setPristine();
		}
	}

	/**
	 * Set the parent of this {@link Data}.
	 * 
	 * @param parent
	 *            the new parent
	 */
	void setParent(Contact parent) {
		this.parent = parent;
	}
}
