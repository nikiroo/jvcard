package be.nikiroo.jvcard;

public class TypeInfo {
	private String name;
	private String value;
	private Data parent;
	private boolean dirty;

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

	/**
	 * Check if this {@link TypeInfo} has unsaved changes.
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
	}

	/**
	 * Set the parent of this {@link TypeInfo}.
	 * 
	 * @param parent
	 *            the new parent
	 */
	void setParent(Data parent) {
		this.parent = parent;
	}
}