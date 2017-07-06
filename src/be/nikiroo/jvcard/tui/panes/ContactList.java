package be.nikiroo.jvcard.tui.panes;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.resources.ColorOption;
import be.nikiroo.jvcard.resources.DisplayBundle;
import be.nikiroo.jvcard.resources.DisplayOption;
import be.nikiroo.jvcard.resources.StringId;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;

import com.googlecode.lanterna.input.KeyType;

public class ContactList extends MainContentList {
	private Card card;
	private List<Contact> contacts;
	private String filter;

	private List<String> formats;
	private int selectedFormat;
	private String format;

	public ContactList(Card card) {
		DisplayBundle map = new DisplayBundle();
		formats = new LinkedList<String>();
		for (String format : map.getString(DisplayOption.CONTACT_LIST_FORMAT)
				.split(",")) {
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
			for (Contact c : card) {
				if (filter == null
						|| c.toString(format, "|").toLowerCase()
								.contains(filter.toLowerCase())) {
					addItem("x");
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

		// TODO ui
		actions.add(new KeyAction(Mode.ASK_USER, 'a',
				StringId.KEY_ACTION_ADD) {
			@Override
			public Object getObject() {
				return card;
			}

			@Override
			public String getQuestion() {
				return Main.trans(StringId.ASK_USER_CONTACT_NAME);
			}

			@Override
			public String callback(String answer) {
				if (answer.length() > 0) {
					List<Data> datas = new LinkedList<Data>();
					datas.add(new Data(null, "FN", answer, null));
					getCard().add(new Contact(datas));
					addItem("x");
				}

				return null;
			}
		});
		actions.add(new KeyAction(Mode.ASK_USER_KEY, 'd',
				StringId.KEY_ACTION_DELETE) {
			@Override
			public Object getObject() {
				return getSelectedContact();
			}

			@Override
			public String getQuestion() {
				Contact contact = getSelectedContact();
				String contactName = "null";
				if (contact != null)
					contactName = "" + contact.getPreferredDataValue("FN");

				return Main.trans(
						StringId.CONFIRM_USER_DELETE_CONTACT,
						contactName);
			}

			@Override
			public String callback(String answer) {
				if (answer.equalsIgnoreCase("y")) {
					Contact contact = getSelectedContact();
					if (contact != null && contact.delete()) {
						removeItem("x");
						return null;
					}

					String contactName = "null";
					if (contact != null)
						contactName = "" + contact.getPreferredDataValue("FN");

					return Main.trans(
							StringId.ERR_CANNOT_DELETE_CONTACT,
							contactName);
				}

				return null;
			}
		});
		actions.add(new KeyAction(Mode.ASK_USER_KEY, 's',
				StringId.KEY_ACTION_SAVE_CARD) {
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
				StringId.KEY_ACTION_VIEW_CONTACT) {
			@Override
			public Object getObject() {
				return getSelectedContact();
			}
		});
		actions.add(new KeyAction(Mode.NONE, KeyType.Tab,
				StringId.KEY_ACTION_SWITCH_FORMAT) {
			@Override
			public boolean onAction() {
				switchFormat();
				return false;
			}
		});
		actions.add(new KeyAction(Mode.ASK_USER, 'w',
				StringId.KEY_ACTION_SEARCH) {

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

		ColorOption el = (focused && selected) ? ColorOption.CONTACT_LINE_SELECTED
				: ColorOption.CONTACT_LINE;
		ColorOption elSep = (focused && selected) ? ColorOption.CONTACT_LINE_SEPARATOR_SELECTED
				: ColorOption.CONTACT_LINE_SEPARATOR;
		ColorOption elDirty = (focused && selected) ? ColorOption.CONTACT_LINE_DIRTY_SELECTED
				: ColorOption.CONTACT_LINE_DIRTY;

		width -= 2; // dirty mark space

		String[] array = contact.toStringArray(format, getSeparator(), " ",
				width, Main.isUnicode());
		
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
