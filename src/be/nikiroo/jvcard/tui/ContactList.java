package be.nikiroo.jvcard.tui;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.AbstractListBox.ListItemRenderer;
import com.googlecode.lanterna.input.KeyType;

public class ContactList extends MainContent implements Runnable {
	private Card card;
	private ActionListBox lines;

	private List<String> formats = new LinkedList<String>();
	private int selectedFormat = -1;
	private String format = "";

	public ContactList(Card card) {
		super(Direction.VERTICAL);

		// TODO: should get that in an INI file
		formats.add("NICKNAME@3|FN@+|EMAIL@30");
		formats.add("FN@+|EMAIL@40");
		switchFormat();

		lines = new ActionListBox();

		lines
				.setListItemRenderer(new ListItemRenderer<Runnable, ActionListBox>() {
					/**
					 * This is the main drawing method for a single list box
					 * item, it applies the current theme to setup the colors
					 * and then calls {@code getLabel(..)} and draws the result
					 * using the supplied {@code TextGUIGraphics}. The graphics
					 * object is created just for this item and is restricted so
					 * that it can only draw on the area this item is occupying.
					 * The top-left corner (0x0) should be the starting point
					 * when drawing the item.
					 * 
					 * @param graphics
					 *            Graphics object to draw with
					 * @param listBox
					 *            List box we are drawing an item from
					 * @param index
					 *            Index of the item we are drawing
					 * @param item
					 *            The item we are drawing
					 * @param selected
					 *            Will be set to {@code true} if the item is
					 *            currently selected, otherwise {@code false},
					 *            but please notice what context 'selected'
					 *            refers to here (see {@code setSelectedIndex})
					 * @param focused
					 *            Will be set to {@code true} if the list box
					 *            currently has input focus, otherwise {@code
					 *            false}
					 */
					public void drawItem(TextGUIGraphics graphics,
							ActionListBox listBox, int index, Runnable item,
							boolean selected, boolean focused) {

						if (selected && focused) {
							graphics
									.setForegroundColor(UiColors.Element.CONTACT_LINE_SELECTED
											.getForegroundColor());
							graphics
									.setBackgroundColor(UiColors.Element.CONTACT_LINE_SELECTED
											.getBackgroundColor());
						} else {
							graphics
									.setForegroundColor(UiColors.Element.CONTACT_LINE
											.getForegroundColor());
							graphics
									.setBackgroundColor(UiColors.Element.CONTACT_LINE
											.getBackgroundColor());
						}

						String label = getLabel(listBox, index, item);
						// label = TerminalTextUtils.fitString(label,
						// graphics.getSize().getColumns());

						Contact c = ContactList.this.card.getContacts().get(
								index);

						// we could use: " ", "┃", "│"...
						//TODO: why +5 ?? padding problem?
						label = c.toString(format, " ┃ ", lines.getSize().getColumns() + 5);

						graphics.putString(0, 0, label);
					}
				});

		addComponent(lines, LinearLayout
				.createLayoutData(LinearLayout.Alignment.Fill));

		setCard(card);
	}

	private void switchFormat() {
		if (formats.size() == 0)
			return;

		selectedFormat++;
		if (selectedFormat >= formats.size()) {
			selectedFormat = 0;
		}

		format = formats.get(selectedFormat);

		if (lines != null)
			lines.invalidate();
	}

	public void setCard(Card card) {
		lines.clearItems();
		this.card = card;

		if (card != null) {
			for (int i = 0; i < card.getContacts().size(); i++) {
				lines.addItem("[contact line]", this);
			}
		}

		lines.setSelectedIndex(0);
	}

	@Override
	public void run() {
		// TODO: item selected.
		// should we do something?
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
				int index = lines.getSelectedIndex();
				return card.getContacts().get(index);
			}
		});
		actions.add(new KeyAction(Mode.CONTACT_DETAILS, KeyType.Enter,
				Trans.StringId.KEY_ACTION_VIEW_CONTACT) {
			@Override
			public Object getObject() {
				int index = lines.getSelectedIndex();
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

	public DataType getDataType() {
		return DataType.CARD;
	}

	public Mode getMode() {
		return Mode.CONTACT_LIST;
	}

	@Override
	public String move(int x, int y) {
		lines.setSelectedIndex(lines.getSelectedIndex() + x);
		// TODO: y?
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}
}
