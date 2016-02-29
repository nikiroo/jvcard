package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.input.KeyType;

public class ContactList extends MainContentList {
	private Card card;

	private List<String> formats = new LinkedList<String>();
	private int selectedFormat = -1;
	private String format = "";

	public ContactList(Card card) {
		super(UiColors.Element.CONTACT_LINE,
				UiColors.Element.CONTACT_LINE_SELECTED);

		// TODO: should get that in an INI file
		formats.add("NICKNAME@3|FN@+|EMAIL@30");
		formats.add("FN@+|EMAIL@40");
		switchFormat();

		setCard(card);
	}

	/**
	 * Change the currently displayed contacts card.
	 * 
	 * @param card
	 *            the new {@link Card}
	 */
	public void setCard(Card card) {
		clearItems();
		this.card = card;

		if (card != null) {
			for (int i = 0; i < card.getContacts().size(); i++) {
				addItem("[contact line]");
			}
		}

		setSelectedIndex(0);
	}

	@Override
	public void refreshData() {
		int index = getSelectedIndex();
		setCard(card);
		setSelectedIndex(index);
		super.refreshData();
	}

	@Override
	public String getExitWarning() {
		if (card != null && card.isDirty()) {
			return "Ignore unsaved changes? [Y/N]";
		}

		return null;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO add
		actions.add(new KeyAction(Mode.CONTACT_DETAILS_RAW, 'e',
				Trans.StringId.KEY_ACTION_EDIT_CONTACT) {
			@Override
			public Object getObject() {
				return getSelectedContact();
			}
		});
		actions.add(new KeyAction(Mode.DELETE_CONTACT, 'd',
				Trans.StringId.KEY_ACTION_DELETE_CONTACT) {
			@Override
			public Object getObject() {
				return getSelectedContact();
			}
		});
		actions.add(new KeyAction(Mode.SAVE_CARD, 's',
				Trans.StringId.KEY_ACTION_SAVE_CARD) {
			@Override
			public Object getObject() {
				return card;
			}
		});
		actions.add(new KeyAction(Mode.CONTACT_DETAILS, KeyType.Enter,
				Trans.StringId.KEY_ACTION_VIEW_CONTACT) {
			@Override
			public Object getObject() {
				return getSelectedContact();
			}
		});
		actions.add(new KeyAction(Mode.NONE, KeyType.Tab,
				Trans.StringId.KEY_ACTION_SWITCH_FORMAT) {
			@Override
			public boolean onAction() {
				switchFormat();
				return false;
			}
		});

		return actions;
	}

	@Override
	public DataType getDataType() {
		return DataType.CARD;
	}

	@Override
	public String getTitle() {
		if (card != null) {
			return card.getName();
		}

		return null;
	}

	@Override
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		List<TextPart> parts = new LinkedList<TextPart>();

		Contact contact = null;
		if (index > -1 && index < card.size())
			contact = card.get(index);

		if (contact == null)
			return parts;

		Element el = (focused && selected) ? Element.CONTACT_LINE_SELECTED
				: Element.CONTACT_LINE;
		Element elSep = (focused && selected) ? Element.CONTACT_LINE_SEPARATOR_SELECTED
				: Element.CONTACT_LINE_SEPARATOR;
		Element elDirty = (focused && selected) ? Element.CONTACT_LINE_DIRTY_SELECTED
				: Element.CONTACT_LINE_DIRTY;

		width -= 2; // dirty mark space

		String[] array = contact.toStringArray(format, getSeparator(), " ",
				width, UiColors.getInstance().isUnicode());

		if (contact.isDirty()) {
			parts.add(new TextPart(" ", el));
			parts.add(new TextPart("*", elDirty));
		} else {
			parts.add(new TextPart("  ", elSep));
		}

		boolean separator = false;
		for (String str : array) {
			parts.add(new TextPart(str, (separator ? elSep : el)));
			separator = !separator;
		}

		return parts;
	}

	/**
	 * Return the currently selected {@link Contact}.
	 * 
	 * @return the currently selected {@link Contact}
	 */
	private Contact getSelectedContact() {
		int index = getSelectedIndex();
		if (index > -1 && index < card.size())
			return card.get(index);
		return null;
	}

	private void switchFormat() {
		if (formats.size() == 0)
			return;

		selectedFormat++;
		if (selectedFormat >= formats.size()) {
			selectedFormat = 0;
		}

		format = formats.get(selectedFormat);

		invalidate();
	}
}
