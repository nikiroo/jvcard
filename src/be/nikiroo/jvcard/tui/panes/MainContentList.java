package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.i18n.Trans.StringId;
import be.nikiroo.jvcard.tui.Main;
import be.nikiroo.jvcard.tui.StringUtils;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.AbstractListBox.ListItemRenderer;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

abstract public class MainContentList extends MainContent implements Runnable {
	private ActionListBox lines;

	/**
	 * This class represent a part of a text line to draw in this
	 * {@link MainContentList}.
	 * 
	 * @author niki
	 * 
	 */
	public class TextPart {
		private String text;
		private Element element;

		public TextPart(String text, Element element) {
			this.text = text;
			this.element = element;
		}

		public String getText() {
			return text;
		}

		public Element getElement() {
			return element;
		}

		public TextColor getForegroundColor() {
			if (element != null)
				return element.getForegroundColor();
			return Element.DEFAULT.getForegroundColor();
		}

		public TextColor getBackgroundColor() {
			if (element != null)
				return element.getBackgroundColor();
			return Element.DEFAULT.getBackgroundColor();
		}
	}

	public MainContentList() {
		super(Direction.VERTICAL);

		lines = new ActionListBox();

		lines.setListItemRenderer(new ListItemRenderer<Runnable, ActionListBox>() {
			/**
			 * This is the main drawing method for a single list box item, it
			 * applies the current theme to setup the colors and then calls
			 * {@code getLabel(..)} and draws the result using the supplied
			 * {@code TextGUIGraphics}. The graphics object is created just for
			 * this item and is restricted so that it can only draw on the area
			 * this item is occupying. The top-left corner (0x0) should be the
			 * starting point when drawing the item.
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
			 *            Will be set to {@code true} if the item is currently
			 *            selected, otherwise {@code false}, but please notice
			 *            what context 'selected' refers to here (see
			 *            {@code setSelectedIndex})
			 * @param focused
			 *            Will be set to {@code true} if the list box currently
			 *            has input focus, otherwise {@code false}
			 */
			public void drawItem(TextGUIGraphics graphics,
					ActionListBox listBox, int index, Runnable item,
					boolean selected, boolean focused) {

				// width "-1" to reserve space for the optional vertical
				// scroll bar
				List<TextPart> parts = MainContentList.this.getLabel(index,
						lines.getSize().getColumns() - 1, selected, focused);

				int position = 0;
				for (TextPart part : parts) {
					graphics.setForegroundColor(part.getForegroundColor());
					graphics.setBackgroundColor(part.getBackgroundColor());

					String label = StringUtils.sanitize(part.getText(),
							UiColors.getInstance().isUnicode());

					graphics.putString(position, 0, label);
					position += label.length();
				}
			}
		});

		addComponent(lines,
				LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
	}

	/**
	 * Add an item to this {@link MainContentList}.
	 * 
	 * @param line
	 *            the item to add
	 */
	public void addItem(String line) {
		lines.addItem(line, this);
	}

	/**
	 * Clear all the items in this {@link MainContentList}
	 */
	public void clearItems() {
		lines.clearItems();
	}

	/**
	 * Get the index of the currently selected line.
	 * 
	 * @return the index
	 */
	public int getSelectedIndex() {
		return lines.getSelectedIndex();
	}

	/**
	 * Change the index of the currently selected line.
	 * 
	 * @param index
	 *            the new index
	 */
	public void setSelectedIndex(int index) {
		lines.setSelectedIndex(index);
	}

	/**
	 * Return the default content separator for text fields.
	 * 
	 * @return the separator
	 */
	public String getSeparator() {
		return Main.trans(StringId.DEAULT_FIELD_SEPARATOR);
	}

	@Override
	public void run() {
		// item selected.
		// ignore.
	}

	@Override
	public String move(int x, int y) {
		setSelectedIndex(getSelectedIndex() + x);
		// TODO: y?
		return null;
	}

	@Override
	public int getCount() {
		return lines.getItemCount();
	}

	/**
	 * Return the representation of the selected line, in {@link TextPart}s.
	 * 
	 * @param index
	 *            the line index
	 * @param width
	 *            the max width of the line
	 * @param selected
	 *            TRUE if the item is selected
	 * @param focused
	 *            TRUE if the item is focused
	 * 
	 * @return the text representation
	 */
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		List<TextPart> parts = new LinkedList<TextPart>();

		if (selected && focused) {
			parts.add(new TextPart("" + lines.getItems().get(index),
					Element.CONTACT_LINE_SELECTED));
		} else {
			parts.add(new TextPart("" + lines.getItems().get(index),
					Element.CONTACT_LINE));
		}

		return parts;
	}
}
