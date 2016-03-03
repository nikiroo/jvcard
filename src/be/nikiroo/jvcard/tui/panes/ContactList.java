package be.nikiroo.jvcard.tui.panes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.resources.Bundles;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.input.KeyType;

public class ContactList extends MainContentList {
	private Card card;
	private List<Contact> contacts;
	private String filter;

	private List<String> formats;
	private int selectedFormat;
	private String format;

	public ContactList(Card card) {
		formats = new LinkedList<String>();
		for (String format : Bundles.getBundle("display")
				.getString("CONTACT_LIST_FORMAT").split(",")) {
			formats.add(format);
		}

		selectedFormat = -1;
		switchFormat();

		setCard(card);
	}

	/**
	 * Change the currently displayed contacts card, only allowing those that
	 * satisfy the current filter.
	 * 
	 * @param card
	 *            the new {@link Card}
	 * @param filter
	 *            the text filter or NULL for all contacts
	 */
	public void setCard(Card card) {
		clearItems();
		this.card = card;
		this.contacts = new LinkedList<Contact>();

		if (card != null) {
			for (int i = 0; i < card.size(); i++) {
				Contact c = card.get(i);
				if (filter == null
						|| c.toString(format).toLowerCase()
								.contains(filter.toLowerCase())) {
					addItem("[contact line]");
					contacts.add(c);
				}
			}
		}

		setSelectedIndex(0);
	}

	@Override
	public void refreshData() {
		int index = getSelectedIndex();
		setCard(card);
		if (index >= contacts.size())
			index = contacts.size() - 1;
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
		actions.add(new KeyAction(Mode.ASK_USER_KEY, 'd',
				Trans.StringId.KEY_ACTION_DELETE_CONTACT) {
			@Override
			public Object getObject() {
				return getSelectedContact();
			}

			@Override
			public String getQuestion() {
				// TODO i18n
				return "Delete contact? [Y/N]";
			}

			@Override
			public String callback(String answer) {
				if (answer.equalsIgnoreCase("y")) {
					Contact contact = getSelectedContact();
					if (contact != null && contact.delete()) {
						return null;
					}

					// TODO i18n
					return "Cannot delete contact";
				}

				return null;
			}
		});
		actions.add(new KeyAction(Mode.ASK_USER_KEY, 's',
				Trans.StringId.KEY_ACTION_SAVE_CARD) {
			@Override
			public Object getObject() {
				return card;
			}

			@Override
			public String getQuestion() {
				return "Save changes? [Y/N]";
			}

			@Override
			public String callback(String answer) {
				if (answer.equalsIgnoreCase("y")) {
					boolean ok = false;
					try {
						if (card != null && card.save())
							ok = true;
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}

					if (!ok) {
						return "Cannot save to file";
					}
				}

				return null;
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
		actions.add(new KeyAction(Mode.ASK_USER, 'w',
				Trans.StringId.KEY_ACTION_SEARCH) {

			@Override
			public String getQuestion() {
				return "Search:";
			}

			@Override
			public String getDefaultAnswer() {
				return filter;
			}

			@Override
			public String callback(String answer) {
				filter = answer;
				setCard(card);
				return null;
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
			if (filter != null)
				return card.getName() + " [" + filter + "]";
			return card.getName();
		}

		return null;
	}

	@Override
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		List<TextPart> parts = new LinkedList<TextPart>();

		Contact contact = null;
		if (index > -1 && index < contacts.size())
			contact = contacts.get(index);

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
		if (index > -1 && index < contacts.size())
			return contacts.get(index);
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
