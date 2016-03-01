package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.StringUtils;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.input.KeyType;

public class ContactDetailsRaw extends MainContentList {
	private Contact contact;
	private int mode;

	public ContactDetailsRaw(Contact contact) {
		super(null, null);

		this.contact = contact;
		this.mode = 0;

		for (int i = 0; i < contact.size(); i++) {
			addItem("[detail line]");
		}
	}

	@Override
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		// TODO: from ini file?
		int SIZE_COL_1 = 15;

		Element el = (focused && selected) ? Element.CONTACT_LINE_SELECTED
				: Element.CONTACT_LINE;
		Element elSep = (focused && selected) ? Element.CONTACT_LINE_SEPARATOR_SELECTED
				: Element.CONTACT_LINE_SEPARATOR;
		Element elDirty = (focused && selected) ? Element.CONTACT_LINE_DIRTY_SELECTED
				: Element.CONTACT_LINE_DIRTY;

		Data data = contact.get(index);

		List<TextPart> parts = new LinkedList<TextPart>();
		if (data.isDirty()) {
			parts.add(new TextPart(" ", el));
			parts.add(new TextPart("*", elDirty));
		} else {
			parts.add(new TextPart("  ", elSep));
		}
		String name = " " + data.getName() + " ";
		String value = null;

		StringBuilder valueBuilder = new StringBuilder(" ");
		switch (mode) {
		case 0:
			valueBuilder.append(data.getValue());
			if (data.getGroup() != null && data.getGroup().length() > 0) {
				valueBuilder.append("(");
				valueBuilder.append(data.getGroup());
				valueBuilder.append(")");
			}
			break;
		case 1:
			for (int indexType = 0; indexType < data.size(); indexType++) {
				TypeInfo type = data.get(indexType);
				if (valueBuilder.length() > 1)
					valueBuilder.append(", ");
				valueBuilder.append(type.getName());
				valueBuilder.append(": ");
				valueBuilder.append(type.getValue());
			}
			break;
		}
		valueBuilder.append(" ");

		value = valueBuilder.toString();

		name = StringUtils.sanitize(name, UiColors.getInstance().isUnicode());
		value = StringUtils.sanitize(value, UiColors.getInstance().isUnicode());

		name = StringUtils.padString(name, SIZE_COL_1);
		value = StringUtils.padString(value, width - SIZE_COL_1
				- getSeparator().length() - 2);

		parts.add(new TextPart(name, el));
		parts.add(new TextPart(getSeparator(), elSep));
		parts.add(new TextPart(value, el));

		return parts;
	};

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
				return contact.get(getSelectedIndex());
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
		actions.add(new KeyAction(Mode.NONE, KeyType.Tab,
				Trans.StringId.KEY_ACTION_SWITCH_FORMAT) {
			@Override
			public boolean onAction() {
				mode++;
				if (mode > 1)
					mode = 0;

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
}
