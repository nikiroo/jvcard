package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;
import be.nikiroo.jvcard.tui.panes.MainContentList.TextPart;

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
	public String getExitWarning() {
		if (card != null && card.isDirty()) {
			return "Some of your contact information is not saved";
		}
		return null;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO del, save...
		actions.add(new KeyAction(Mode.CONTACT_DETAILS, 'e',
				Trans.StringId.KEY_ACTION_EDIT_CONTACT) {
			@Override
			public Object getObject() {
				int index = getSelectedIndex();
				return card.getContacts().get(index);
			}
		});
		actions.add(new KeyAction(Mode.CONTACT_DETAILS, KeyType.Enter,
				Trans.StringId.KEY_ACTION_VIEW_CONTACT) {
			@Override
			public Object getObject() {
				int index = getSelectedIndex();
				return card.getContacts().get(index);
			}
		});
		actions.add(new KeyAction(Mode.SWICTH_FORMAT, KeyType.Tab,
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
	public Mode getMode() {
		return Mode.CONTACT_LIST;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		List<TextPart> parts = new LinkedList<TextPart>();

		Element el = (focused && selected) ? Element.CONTACT_LINE_SELECTED
				: Element.CONTACT_LINE;
		Element elSep = (focused && selected) ? Element.CONTACT_LINE_SEPARATOR_SELECTED
				: Element.CONTACT_LINE_SEPARATOR;

		// TODO: width/separator to check
		String separator = " ┃ ";
		width -= (format.split("\\|").length + 1) * separator.length();
		String[] array = card.getContacts().get(index).toStringArray(format,
				width);

		// we could use: " ", "┃", "│"...
		for (String str : array) {
			parts.add(new TextPart(str, el));
			parts.add(new TextPart(separator, elSep));
		}

		if (parts.size() > 0)
			parts.remove(parts.get(parts.size() - 1));

		return parts;
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
