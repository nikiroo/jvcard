package be.nikiroo.jvcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.parsers.Parser;
import be.nikiroo.utils.StringUtils;

/**
 * A contact is the information that represent a contact person or organisation.
 * <p>
 * Each {@link Data} inside can be binary encoded or not: if it is binary
 * encoded, it has an active BKey number (not -1) associated to it (of value 0
 * if still not sorted, or unique for the whole {@link Contact} if already
 * processed).
 * 
 * @author niki
 */
public class Contact extends BaseClass<Data> {
	private int nextBKey = 1;
	private Map<Integer, Data> binaries;

	/**
	 * Create a new Contact from the given information. Note that the BKeys data
	 * will be reset.
	 * 
	 * @param content
	 *            the information about the contact
	 */
	public Contact(List<Data> content) {
		super(load(content));
		updateBKeys(true);
	}

	/**
	 * Return the preferred Data field with the given name, the first one if
	 * none is preferred, or NULL if none at all.
	 * 
	 * @param name
	 *            the name to look for
	 * 
	 * @return the {@link Data} field, or NULL
	 */
	public Data getPreferredData(String name) {
		Data pref = null;
		int ipref = Integer.MAX_VALUE;
		for (Data data : getData(name)) {
			if (pref == null)
				pref = data;

			if (data.getPreferred() < ipref)
				pref = data;
		}

		return pref;
	}

	/**
	 * Return the value of the preferred data field with this name, or NULL if
	 * none (you cannot differentiate a NULL value and no value with this method
	 * -- for that, check {@link Contact#getPreferredData(String)}).
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
	 *            the name to look for
	 * @return a list of Data fields with this name
	 */
	public List<Data> getData(String name) {
		List<Data> found = new LinkedList<Data>();

		for (Data data : this) {
			if (data.getName().equals(name))
				found.add(data);
		}

		return found;
	}

	/**
	 * Return a {@link String} representation of this contact formated
	 * accordingly to the given format.
	 * 
	 * <p>
	 * The format is basically a list of field names separated by a pipe and
	 * optionally parametrised with the 'at' (@) symbol. The parameters allows
	 * you to:
	 * <ul>
	 * <li>@x: show only a present/not present info</li>
	 * <li>@n: limit the size to a fixed value 'n'</li>
	 * <li>@+: expand the size of this field as much as possible</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * In case of lists or multiple-fields values, you can select a specific
	 * list or field with:
	 * <ul>
	 * <li>FIELD@(0): select the first value in a list</li>
	 * <li>FIELD@[1]: select the second field in a multiple-fields value</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * You can also add a fixed text if it starts with a simple-quote (').
	 * </p>
	 * 
	 * <p>
	 * Example: "'Contact: |N@10|FN@20|NICK@+|PHOTO@x"
	 * </p>
	 * 
	 * @param format
	 *            the format to use
	 * @param separator
	 *            the separator {@link String} to use between fields
	 * 
	 * @return the {@link String} representation
	 */
	public String toString(String format, String separator) {
		return toString(format, separator, null, -1, true);
	}

	/**
	 * Return a {@link String} representation of this contact formated
	 * accordingly to the given format.
	 * 
	 * <p>
	 * The format is basically a list of field names separated by a pipe and
	 * optionally parametrised. The parameters allows you to:
	 * <ul>
	 * <li>@x: show only a present/not present info</li>
	 * <li>@n: limit the size to a fixed value 'n'</li>
	 * <li>@+: expand the size of this field as much as possible</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * In case of lists or multiple-fields values, you can select a specific
	 * list or field with:
	 * <ul>
	 * <li>FIELD@(0): select the first value in a list</li>
	 * <li>FIELD@[1]: select the second field in a multiple-fields value</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * You can also add a fixed text if it starts with a simple-quote (').
	 * </p>
	 * 
	 * <p>
	 * Example: "'Contact: |N@10|FN@20|NICK@+|PHOTO@x"
	 * </p>
	 * 
	 * @param format
	 *            the format to use
	 * @param separator
	 *            the separator {@link String} to use between fields
	 * @param padding
	 *            the {@link String} to use for left and right padding
	 * @param width
	 *            a fixed width or -1 for "as long as needed"
	 * @param unicode
	 *            allow Unicode or only ASCII characters
	 * 
	 * @return the {@link String} representation
	 */
	public String toString(String format, String separator, String padding,
			int width, boolean unicode) {
		StringBuilder builder = new StringBuilder();

		for (String str : toStringArray(format, separator, padding, width,
				unicode)) {
			builder.append(str);
		}

		return builder.toString();
	}

	/**
	 * Return a {@link String} representation of this contact formated
	 * accordingly to the given format, part by part.
	 * 
	 * <p>
	 * The format is basically a list of field names separated by a pipe and
	 * optionally parametrised. The parameters allows you to:
	 * <ul>
	 * <li>@x: show only a present/not present info</li>
	 * <li>@n: limit the size to a fixed value 'n'</li>
	 * <li>@+: expand the size of this field as much as possible</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * In case of lists or multiple-fields values, you can select a specific
	 * list or field with:
	 * <ul>
	 * <li>FIELD@(0): select the first value in a list</li>
	 * <li>FIELD@[1]: select the second field in a multiple-fields value</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * You can also add a fixed text if it starts with a simple-quote (').
	 * </p>
	 * 
	 * <p>
	 * Example: "'Contact: |N@10|FN@20|NICK@+|PHOTO@x"
	 * </p>
	 * 
	 * @param format
	 *            the format to use
	 * @param separator
	 *            the separator {@link String} to use between fields
	 * @param padding
	 *            the {@link String} to use for left and right padding
	 * @param width
	 *            a fixed width or -1 for "as long as needed"
	 * 
	 * @param unicode
	 *            allow Uniode or only ASCII characters
	 * 
	 * @return the {@link String} representation
	 */
	public String[] toStringArray(String format, String separator,
			String padding, int width, boolean unicode) {
		if (width > -1) {
			int numOfFields = format.split("\\|").length;
			if (separator != null)
				width -= (numOfFields - 1) * separator.length();
			if (padding != null)
				width -= (numOfFields) * (2 * padding.length());

			if (width < 0)
				width = 0;
		}

		List<String> str = new LinkedList<String>();

		boolean first = true;
		for (String s : toStringArray(format, width, unicode)) {
			if (!first) {
				str.add(separator);
			}

			if (padding != null)
				str.add(padding + s + padding);
			else
				str.add(s);

			first = false;
		}

		return str.toArray(new String[] {});
	}

	/**
	 * Return a {@link String} representation of this contact formated
	 * accordingly to the given format, part by part.
	 * 
	 * <p>
	 * The format is basically a list of field names separated by a pipe and
	 * optionally parametrised. The parameters allows you to:
	 * <ul>
	 * <li>@x: show only a present/not present info</li>
	 * <li>@n: limit the size to a fixed value 'n'</li>
	 * <li>@+: expand the size of this field as much as possible</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * In case of lists or multiple-fields values, you can select a specific
	 * list or field with:
	 * <ul>
	 * <li>FIELD@(0): select the first value in a list</li>
	 * <li>FIELD@[1]: select the second field in a multiple-fields value</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * You can also add a fixed text if it starts with a simple-quote (').
	 * </p>
	 * 
	 * <p>
	 * Example: "'Contact: |N@10|FN@20|NICK@+|PHOTO@x"
	 * </p>
	 * 
	 * @param format
	 *            the format to use
	 * @param width
	 *            a fixed width or -1 for "as long as needed"
	 * @param unicode
	 *            allow Uniode or only ASCII characters
	 * 
	 * @return the {@link String} representation
	 */
	public String[] toStringArray(String format, int width, boolean unicode) {
		List<String> str = new LinkedList<String>();

		String[] formatFields = format.split("\\|");
		String[] values = new String[formatFields.length];
		Boolean[] expandedFields = new Boolean[formatFields.length];
		Boolean[] fixedsizeFields = new Boolean[formatFields.length];
		int numOfFieldsToExpand = 0;
		int totalSize = 0;

		if (width == 0) {
			for (int i = 0; i < formatFields.length; i++) {
				str.add("");
			}

			return str.toArray(new String[] {});
		}

		for (int i = 0; i < formatFields.length; i++) {
			String field = formatFields[i];

			int size = -1;
			boolean binary = false;
			boolean expand = false;
			int fieldNum = -1;
			int valueNum = -1;

			if (field.length() > 0 && field.charAt(0) != '\''
					&& field.contains("@")) {
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
					} else if (opt.length() > 0 && opt.charAt(0) == '(') {
						try {
							opt = opt.substring(1, opt.length() - 1);
							valueNum = Integer.parseInt(opt);
						} catch (Exception e) {
						}
					} else if (opt.length() > 0 && opt.charAt(0) == '[') {
						try {
							opt = opt.substring(1, opt.length() - 1);
							fieldNum = Integer.parseInt(opt);
						} catch (Exception e) {
						}
					} else {
						try {
							size = Integer.parseInt(opt);
						} catch (NumberFormatException e) {
						}
					}
				}
			}

			String value = null;
			if (field.length() > 0 && field.charAt(0) == '\'') {
				value = field.substring(1);
			} else if (valueNum >= 0) {
				List<String> vv = getPreferredData(field).getValues();
				if (valueNum < vv.size()) {
					value = vv.get(valueNum);
				}
			} else if (fieldNum >= 0) {
				List<String> ff = getPreferredData(field).getFields();
				if (fieldNum < ff.size()) {
					value = ff.get(fieldNum);
				}
			} else {
				// we don't need the *data* in binary mode...
				if (binary)
					value = getData(field).size() > 0 ? "x" : null;
				else
					value = getPreferredDataValue(field);
			}

			if (value == null) {
				value = "";
			} else {
				value = StringUtils.sanitize(value, unicode);
			}

			if (size > -1) {
				value = StringUtils.padString(value, size);
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
									+ StringUtils.padString("", remainder);
							remainder = 0;
						}
						if (padPerItem > 0) {
							values[i] = values[i]
									+ StringUtils.padString("", padPerItem);
						}
					}
				}

				totalSize = width;
			}
		}

		int currentSize = 0;
		for (int i = 0; i < values.length; i++) {
			currentSize += addToList(str, values[i], currentSize, width);
		}

		return str.toArray(new String[] {});
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

		List<Data> newDatas = new LinkedList<Data>(vc);
		for (int i = 0; i < newDatas.size(); i++) {
			Data data = newDatas.get(i);
			int bkey = Parser.getBKey(data);
			if (bkey >= 0) {
				if (binaries.containsKey(bkey)) {
					newDatas.set(i, binaries.get(bkey));
				}
			}
		}

		replaceListContent(newDatas);
		this.nextBKey = vc.nextBKey;
	}

	@Override
	public String getId() {
		return "" + getPreferredDataValue("UID");
	}

	@Override
	public String getState() {
		return getId();
	}

	/**
	 * Return a simple {@link String} representation of this contact without
	 * BKeys.
	 * 
	 * @return the {@link String} representation
	 */
	@Override
	public String toString() {
		return "[Contact: " + getPreferredDataValue("FN") + "]";
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

		for (Data data : this) {
			if (data.isBinary() && (data.getB64Key() <= 0 || force)) {
				binaries.put(nextBKey, data);
				data.resetB64Key(nextBKey++);
			}
		}
	}

	/**
	 * Load the data from the given {@link File} under the given {@link Format}.
	 * 
	 * @param file
	 *            the {@link File} to load from
	 * @param format
	 *            the {@link Format} to load as
	 * 
	 * @return the list of elements
	 * @throws IOException
	 *             in case of IO error
	 */
	static private List<Data> load(List<Data> content) {
		List<Data> datas = new ArrayList<Data>();

		boolean fn = false;
		boolean n = false;
		boolean uid = false;
		if (content != null) {
			for (Data data : content) {
				if (data.getName().equals("N")) {
					n = true;
				} else if (data.getName().equals("FN")) {
					fn = true;
				} else if (data.getName().equals("UID")) {
					uid = true;
				}

				if (!data.getName().equals("VERSION")) {
					datas.add(data);
				}
			}
		}

		// required fields:
		if (!n) // required since vCard 3.0, supported in 2.1
			datas.add(new Data(null, "N", "", null));
		if (!fn) // not required anymore but still supported in 4.0
			datas.add(new Data(null, "FN", "", null));
		if (!uid) // supported by vCard, required by this program
			datas.add(new Data(null, "UID", UUID.randomUUID().toString(), null));

		return datas;
	}

	/**
	 * Add a {@link String} to the given {@link List}, but make sure it does not
	 * exceed the maximum size, and truncate it if needed to fit.
	 * <p>
	 * Will always add one and only one {@link String} (potentially empty) at
	 * the end of <tt>list</tt>.
	 * 
	 * @param list
	 *            the list to add to
	 * @param add
	 *            the {@link String} to (either fully or partially) add
	 * @param currentSize
	 *            the current total size (managed outside of this method)
	 * @param maxSize
	 *            the maximum size that cannot be exceeded (or -1 for
	 *            "no maximum") -- if the maximum size would be exceeded by
	 *            adding this {@link String}, only a part of it will be added;
	 *            if the maximum size is already reached or exceeded (should not
	 *            happen because of this method), an empty {@link String} will
	 *            be added
	 * 
	 * @return the number of characters added (the size of the last
	 *         {@link String} in <tt>list</tt>)
	 */
	static private int addToList(List<String> list, String add,
			int currentSize, int maxSize) {
		if (add == null || add.length() == 0) {
			if (add != null)
				list.add(add);
			return 0;
		}

		if (maxSize > -1) {
			if (currentSize < maxSize) {
				if (currentSize + add.length() >= maxSize) {
					add = add.substring(0, maxSize - currentSize);
				}
			} else {
				add = "";
			}
		}

		list.add(add);
		return add.length();
	}
}
