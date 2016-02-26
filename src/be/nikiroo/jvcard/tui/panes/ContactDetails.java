package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import com.googlecode.lanterna.input.KeyType;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.StringUtils;
import be.nikiroo.jvcard.tui.UiColors.Element;

public class ContactDetails extends MainContentList {
	private Contact contact;
	private int mode;

	public ContactDetails(Contact contact) {
		super(null, null);

		this.contact = contact;
		this.mode = 0;

		for (int i = 0; i < contact.getContent().size(); i++) {
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

		Data data = contact.getContent().get(index);

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
			for (TypeInfo type : data.getTypes()) {
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
	public String getExitWarning() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		// TODO Auto-generated method stub
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO: add, remove
		actions.add(new KeyAction(Mode.EDIT_DETAIL, 'd', Trans.StringId.DUMMY) {
			@Override
			public Object getObject() {
				return contact.getContent().get(getSelectedIndex());
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
	public Mode getMode() {
		return Mode.CONTACT_DETAILS;
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
