package be.nikiroo.jvcard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Parser;

/**
 * A contact is the information that represent a contact person or organisation.
 * 
 * @author niki
 * 
 */
public class Contact {
	private List<Data> datas;
	private int nextBKey = 1;
	private Map<Integer, Data> binaries;
	private boolean dirty;
	private Card parent;

	/**
	 * Create a new Contact from the given information. Note that the BKeys data
	 * will be reset.
	 * 
	 * @param content
	 *            the information about the contact
	 */
	public Contact(List<Data> content) {
		this.datas = new LinkedList<Data>();

		boolean fn = false;
		boolean n = false;
		for (Data data : content) {
			if (data.getName().equals("N")) {
				n = true;
			} else if (data.getName().equals("FN")) {
				fn = true;
			}

			if (!data.getName().equals("VERSION")) {
				datas.add(data);
			}
		}

		// required fields:
		if (!n) {
			datas.add(new Data(null, "N", "", null));
		}
		if (!fn) {
			datas.add(new Data(null, "FN", "", null));
		}

		updateBKeys(true);
	}

	/**
	 * Return the informations (note: this is the actual list, be careful).
	 * 
	 * @return the list of data anout this contact
	 */
	public List<Data> getContent() {
		return datas;
	}

	/**
	 * Return the preferred Data field with the given name, or NULL if none.
	 * 
	 * @param name
	 *            the name to look for
	 * @return the Data field, or NULL
	 */
	public Data getPreferredData(String name) {
		Data first = null;
		for (Data data : getData(name)) {
			if (first == null)
				first = data;
			for (TypeInfo type : data.getTypes()) {
				if (type.getName().equals("TYPE")
						&& type.getValue().equals("pref")) {
					return data;
				}
			}
		}

		return first;
	}

	/**
	 * Return the value of the preferred data field with this name, or NULL if
	 * none (you cannot differentiate a NULL value and no value).
	 * 
	 * @param name
	 *            the name to look for
	 * @return the value (which can be NULL), or NULL
	 */
	public String getPreferredDataValue(String name) {
		Data data = getPreferredData(name);
		if (data != null && data.getValue() != null)
			return data.getValue().trim();
		return null;
	}

	/**
	 * Get the Data fields that share the given name.
	 * 
	 * @param name
	 *            the name to ook for
	 * @return a list of Data fields with this name
	 */
	public List<Data> getData(String name) {
		List<Data> found = new LinkedList<Data>();

		for (Data data : datas) {
			if (data.getName().equals(name))
				found.add(data);
		}

		return found;
	}

	/**
	 * Return a {@link String} representation of this contact.
	 * 
	 * @param format
	 *            the {@link Format} to use
	 * @param startingBKey
	 *            the starting BKey or -1 for no BKeys
	 * @return the {@link String} representation
	 */
	public String toString(Format format, int startingBKey) {
		updateBKeys(false);
		return Parser.toString(this, format, startingBKey);
	}

	/**
	 * Return a {@link String} representation of this contact formated
	 * accordingly to the given format.
	 * 
	 * The format is basically a list of field names separated by a pipe and
	 * optionally parametrised. The parameters allows you to:
	 * <ul>
	 * <li>@x: show only a present/not present info</li>
	 * <li>@n: limit the size to a fixed value 'n'</li>
	 * <li>@+: expand the size of this field as much as possible</li>
	 * </ul>
	 * 
	 * Example: "N@10|FN@20|NICK@+|PHOTO@x"
	 * 
	 * @param format
	 *            the format to use
	 * @param separator
	 *            the separator {@link String} to use between fields
	 * @param width
	 *            a fixed width or -1 for "as long as needed"
	 * 
	 * @return the {@link String} representation
	 */
	public String toString(String format, String separator, int width) {
		String str = null;

		String[] formatFields = format.split("\\|");
		String[] values = new String[formatFields.length];
		Boolean[] expandedFields = new Boolean[formatFields.length];
		Boolean[] fixedsizeFields = new Boolean[formatFields.length];
		int numOfFieldsToExpand = 0;
		int totalSize = 0;

		if (width == 0) {
			return "";
		}

		if (width > -1 && separator != null && separator.length() > 0
				&& formatFields.length > 1) {
			int swidth = (formatFields.length - 1) * separator.length();
			if (swidth >= width) {
				str = separator;
				while (str.length() < width) {
					str += separator;
				}

				return str.substring(0, width);
			}

			width -= swidth;
		}

		for (int i = 0; i < formatFields.length; i++) {
			String field = formatFields[i];

			int size = -1;
			boolean binary = false;
			boolean expand = false;

			if (field.contains("@")) {
				String[] opts = field.split("@");
				if (opts.length > 0)
					field = opts[0];
				for (int io = 1; io < opts.length; io++) {
					String opt = opts[io];
					if (opt.equals("x")) {
						binary = true;
					} else if (opt.equals("+")) {
						expand = true;
						numOfFieldsToExpand++;
					} else {
						try {
							size = Integer.parseInt(opt);
						} catch (Exception e) {
						}
					}
				}
			}

			String value = getPreferredDataValue(field);
			if (value == null)
				value = "";

			if (size > -1) {
				value = fixedString(value, size);
			}

			expandedFields[i] = expand;
			fixedsizeFields[i] = (size > -1);

			if (binary) {
				if (value != null && !value.equals(""))
					values[i] = "x";
				else
					values[i] = " ";
				totalSize++;
			} else {
				values[i] = value;
				totalSize += value.length();
			}
		}
		
		if (width > -1 && totalSize > width) {
			int toDo = totalSize - width;
			for (int i = fixedsizeFields.length - 1; toDo > 0 && i >= 0; i--) {
				if (!fixedsizeFields[i]) {
					int valueLength = values[i].length();
					if (valueLength > 0) {
						if (valueLength >= toDo) {
							values[i] = values[i].substring(0, valueLength
									- toDo);
							toDo = 0;
						} else {
							values[i] = "";
							toDo -= valueLength;
						}
					}
				}
			}

			totalSize = width + toDo;
		}
		
		if (width > -1 && numOfFieldsToExpand > 0) {
			int availablePadding = width - totalSize;

			if (availablePadding > 0) {
				int padPerItem = availablePadding / numOfFieldsToExpand;
				int remainder = availablePadding % numOfFieldsToExpand;

				for (int i = 0; i < values.length; i++) {
					if (expandedFields[i]) {
						if (remainder > 0) {
							values[i] = values[i]
									+ new String(new char[remainder]).replace(
											'\0', ' ');
							remainder = 0;
						}
						if (padPerItem > 0) {
							values[i] = values[i]
									+ new String(new char[padPerItem]).replace(
											'\0', ' ');
						}
					}
				}

				totalSize = width;
			}
		}
		
		for (String field : values) {
			if (str == null) {
				str = field;
			} else {
				str += separator + field;
			}
		}

		if (str == null)
			str = "";

		if (width > -1) {
			str = fixedString(str, width);
		}

		return str;
	}

	/**
	 * Fix the size of the given {@link String} either with space-padding or by
	 * shortening it.
	 * 
	 * @param string
	 *            the {@link String} to fix
	 * @param size
	 *            the size of the resulting {@link String}
	 * 
	 * @return the fixed {@link String} of size <i>size</i>
	 */
	static private String fixedString(String string, int size) {
		int length = string.length();

		if (length > size)
			string = string.substring(0, size);
		else if (length < size)
			string = string
					+ new String(new char[size - length]).replace('\0', ' ');

		return string;
	}

	/**
	 * Return a {@link String} representation of this contact, in vCard 2.1,
	 * without BKeys.
	 * 
	 * @return the {@link String} representation
	 */
	public String toString() {
		return toString(Format.VCard21, -1);
	}

	/**
	 * Update the information from this contact with the information in the
	 * given contact. Non present fields will be removed, new fields will be
	 * added, BKey'ed fields will be completed with the binary information known
	 * by this contact.
	 * 
	 * @param vc
	 *            the contact with the newer information and optional BKeys
	 */
	public void updateFrom(Contact vc) {
		updateBKeys(false);

		List<Data> newDatas = new LinkedList<Data>(vc.datas);
		for (int i = 0; i < newDatas.size(); i++) {
			Data data = newDatas.get(i);
			int bkey = Parser.getBKey(data);
			if (bkey >= 0) {
				if (binaries.containsKey(bkey)) {
					newDatas.set(i, binaries.get(bkey));
				}
			}
		}

		this.datas = newDatas;
		this.nextBKey = vc.nextBKey;

		setParent(parent);
		setDirty();
	}

	/**
	 * Mark all the binary fields with a BKey number.
	 * 
	 * @param force
	 *            force the marking, and reset all the numbers.
	 */
	protected void updateBKeys(boolean force) {
		if (force) {
			binaries = new HashMap<Integer, Data>();
			nextBKey = 1;
		}

		if (binaries == null) {
			binaries = new HashMap<Integer, Data>();
		}

		for (Data data : datas) {
			if (data.isBinary() && (data.getB64Key() <= 0 || force)) {
				binaries.put(nextBKey, data);
				data.resetB64Key(nextBKey++);
			}
		}
	}

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

	public void setParent(Card parent) {
		this.parent = parent;
		for (Data data : datas) {
			data.setParent(this);
		}
	}
}
