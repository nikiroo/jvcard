package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.resources.StringUtils;
import be.nikiroo.jvcard.resources.Trans;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.input.KeyType;

public class ContactDetailsRaw extends MainContentList {
	private Contact contact;
	private boolean extMode;

	public ContactDetailsRaw(Contact contact) {
		this.contact = contact;
		this.extMode = false;

		for (int i = 0; i < contact.size(); i++) {
			addItem("x");
		}
	}

	@Override
	public DataType getDataType() {
		return DataType.DATA;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		// TODO Auto-generated method stub
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO: add, remove
		actions.add(new KeyAction(Mode.ASK_USER, KeyType.Enter,
				Trans.StringId.DUMMY) {
			@Override
			public Object getObject() {
				return getSelectedData();
			}

			@Override
			public String getQuestion() {
				Data data = getData();
				if (data != null) {
					return data.getName();
				}

				return null;
			}

			@Override
			public String getDefaultAnswer() {
				Data data = getData();
				if (data != null) {
					return data.getValue();
				}

				return null;
			}

			@Override
			public String callback(String answer) {
				Data data = getData();
				if (data != null) {
					data.setValue(answer);
					return null;
				}

				// TODO: i18n
				return "Cannot modify value";
			}
		});
		actions.add(new KeyAction(Mode.ASK_USER_KEY, 'd', Trans.StringId.DUMMY) {
			@Override
			public Object getObject() {
				return getSelectedData();
			}

			@Override
			public String getQuestion() {
				// TODO i18n
				return "Delete data? [Y/N]";
			}

			@Override
			public String callback(String answer) {
				if (answer.equalsIgnoreCase("y")) {
					Data data = getData();
					if (data != null && data.delete()) {
						removeItem("x");
						return null;
					}

					// TODO i18n
					return "Cannot delete data";
				}

				return null;
			}
		});
		// TODO: ui
		actions.add(new KeyAction(Mode.ASK_USER, 'a', Trans.StringId.DUMMY) {
			@Override
			public Object getObject() {
				return contact;
			}

			@Override
			public String getQuestion() {
				// TODO i18n
				return "new data (xx.group = yy): ";
			}

			@Override
			public String callback(String answer) {
				int indexEq = answer.indexOf('=');
				if (indexEq >= 0) {
					String name = answer.substring(0, indexEq).trim();
					String value = answer.substring(indexEq + 1).trim();
					String group = null;

					int indexDt = name.indexOf('.');
					if (indexDt >= 0) {
						group = name.substring(indexDt + 1).trim();
						name = name.substring(0, indexDt).trim();
					}

					Data data = new Data(null, name, value, group);
					getContact().add(data);
					addItem("x");
				}
				return null;
			}
		});
		// TODO: use a real UI for this, not a simple text box (a list or
		// something, maybe a whole new pane?)
		actions.add(new KeyAction(Mode.ASK_USER, 't', Trans.StringId.DUMMY) {
			private String previous;

			@Override
			public Object getObject() {
				return getSelectedData();
			}

			@Override
			public String getQuestion() {
				Data data = getData();
				if (data != null) {
					return data.getName();
				}

				return null;
			}

			@Override
			public String getDefaultAnswer() {
				Data data = getData();
				if (data != null) {
					previous = typesToString(data, null).toString();
					return previous;
				}

				return null;
			}

			@Override
			public String callback(String answer) {
				Data data = getData();
				if (data != null) {
					if (!answer.equals(previous)) {
						data.replaceListContent(stringToTypes(answer));
					}
					return null;
				}

				// TODO: i18n
				return "Cannot modify value";
			}
		});
		actions.add(new KeyAction(Mode.ASK_USER, 'g', Trans.StringId.DUMMY) {
			private String previous;

			@Override
			public Object getObject() {
				return getSelectedData();
			}

			@Override
			public String getQuestion() {
				Data data = getData();
				if (data != null) {
					return data.getName();
				}

				return null;
			}

			@Override
			public String getDefaultAnswer() {
				Data data = getData();
				if (data != null) {
					previous = data.getGroup();
					return previous;
				}

				return null;
			}

			@Override
			public String callback(String answer) {
				Data data = getData();
				if (data != null) {
					if (!answer.equals(previous)) {
						data.setGroup(answer);
					}
					return null;
				}

				// TODO: i18n
				return "Cannot modify group";
			}
		});
		actions.add(new KeyAction(Mode.NONE, KeyType.Tab,
				Trans.StringId.KEY_ACTION_SWITCH_FORMAT) {
			@Override
			public boolean onAction() {
				extMode = !extMode;
				return false;
			}
		});

		return actions;
	}

	@Override
	public String getTitle() {
		String title = null;

		if (contact != null) {
			title = contact.getPreferredDataValue("FN");
			if (title == null || title.length() == 0)
				title = contact.getPreferredDataValue("N");
		}

		return title;
	}

	@Override
	public String move(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		// TODO: from ini file?
		int SIZE_COL_1 = 15;
		int SIZE_COL_2_OPT = 10;

		if (!extMode)
			SIZE_COL_2_OPT = 0;

		List<TextPart> parts = new LinkedList<TextPart>();
		Data data = null;
		if (index > -1 && index < contact.size())
			data = contact.get(index);

		if (data == null)
			return parts;

		Element el = (focused && selected) ? Element.CONTACT_LINE_SELECTED
				: Element.CONTACT_LINE;
		Element elSep = (focused && selected) ? Element.CONTACT_LINE_SEPARATOR_SELECTED
				: Element.CONTACT_LINE_SEPARATOR;
		Element elDirty = (focused && selected) ? Element.CONTACT_LINE_DIRTY_SELECTED
				: Element.CONTACT_LINE_DIRTY;

		if (data.isDirty()) {
			parts.add(new TextPart(" ", el));
			parts.add(new TextPart("*", elDirty));
		} else {
			parts.add(new TextPart("  ", elSep));
		}
		String name = " " + data.getName() + " ";
		String value = null;
		String group = null;

		StringBuilder valueBuilder = new StringBuilder(" ");
		if (!extMode) {
			valueBuilder.append(data.getValue());
			if (data.getGroup() != null && data.getGroup().length() > 0) {
				valueBuilder.append("(");
				valueBuilder.append(data.getGroup());
				valueBuilder.append(")");
			}
		} else {
			group = data.getGroup();
			if (group == null)
				group = "";

			typesToString(data, valueBuilder);
		}
		valueBuilder.append(" ");

		value = valueBuilder.toString();

		name = StringUtils.sanitize(name, Main.isUnicode());
		value = StringUtils.sanitize(value, Main.isUnicode());

		name = StringUtils.padString(name, SIZE_COL_1);
		group = StringUtils.padString(group, SIZE_COL_2_OPT);
		value = StringUtils.padString(value, width - SIZE_COL_1
				- SIZE_COL_2_OPT - (extMode ? 2 : 1) * getSeparator().length()
				- 2);

		parts.add(new TextPart(name, el));
		parts.add(new TextPart(getSeparator(), elSep));
		parts.add(new TextPart(value, el));
		if (extMode) {
			parts.add(new TextPart(getSeparator(), elSep));
			parts.add(new TextPart(group, el));
		}

		return parts;
	}

	/**
	 * Return the currently selected {@link Data}.
	 * 
	 * @return the currently selected {@link Data}
	 */
	private Data getSelectedData() {
		int index = getSelectedIndex();
		if (index > -1 && index < this.contact.size())
			return contact.get(index);
		return null;
	}

	/**
	 * Serialise the {@link TypeInfo}s in the given {@link Data}.
	 * 
	 * @param data
	 *            the {@link Data} from which to take the {@link TypeInfo}s
	 * @param builder
	 *            an optional {@link StringBuilder} to append the serialized
	 *            version to
	 * 
	 * @return the given {@link StringBuilder} or a new one if the given one is
	 *         NULL
	 */
	static private StringBuilder typesToString(Data data, StringBuilder builder) {
		if (builder == null)
			builder = new StringBuilder();

		for (int indexType = 0; indexType < data.size(); indexType++) {
			TypeInfo type = data.get(indexType);
			if (builder.length() > 1)
				builder.append(", ");
			builder.append(type.getName().replaceAll(",", "\\,"));
			builder.append(": ");
			builder.append(type.getValue().replaceAll(":", "\\:"));
		}

		return builder;
	}

	/**
	 * Unserialise a list of {@link TypeInfo}s.
	 * 
	 * @param value
	 *            the serialised value
	 * 
	 * @return the {@link TypeInfo} in their object form
	 */
	static private List<TypeInfo> stringToTypes(String value) {
		List<TypeInfo> infos = new LinkedList<TypeInfo>();
		if (value == null || value.length() == 0)
			return infos;

		char previous = '\0';
		char car = '\0';
		int done = 0;
		for (int index = 0; index < value.length(); index++) {
			car = value.charAt(index);
			if (index == value.length() - 1) {
				index++;
				previous = '\0';
				car = ',';
			}

			if (previous != '\\' && car == ',') {
				String[] tab = value.substring(done, index).split("\\:");
				infos.add(new TypeInfo( //
						tab[0].replaceAll("\\,", ",").replaceAll("\\:", ":")
								.trim(), //
						tab[1].replaceAll("\\,", ",").replaceAll("\\:", ":")
								.trim()));
				done = index + 1;
			}

			previous = car;
		}

		return infos;
	}
}
