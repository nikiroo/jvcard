package be.nikiroo.jvcard.tui.panes;

import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.resources.ColorOption;
import be.nikiroo.jvcard.resources.StringId;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.utils.StringUtils;

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
		private ColorOption element;

		public TextPart(String text, ColorOption element) {
			this.text = text;
			this.element = element;
		}

		public String getText() {
			return text;
		}

		public ColorOption getElement() {
			return element;
		}

		public TextColor getForegroundColor() {
			if (element != null)
				return UiColors.getForegroundColor(element);
			return UiColors.getForegroundColor(ColorOption.DEFAULT);
		}

		public TextColor getBackgroundColor() {
			if (element != null)
				return UiColors.getBackgroundColor(element);
			return UiColors.getBackgroundColor(ColorOption.DEFAULT);
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
			@Override
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
							Main.isUnicode());

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
	 * Delete the given item.
	 * 
	 * Remark: it will only delete the first found instance if multiple
	 * instances of this item are present.
	 * 
	 * @param line
	 *            the line to delete
	 * 
	 * @return TRUE if the item was deleted
	 */
	public boolean removeItem(String line) {
		boolean deleted = false;

		List<Runnable> copy = lines.getItems();
		for (int index = 0; index < copy.size(); index++) {
			if (copy.get(index).toString().equals(line)) {
				deleted = true;
				copy.remove(index);
				break;
			}
		}

		int index = getSelectedIndex();
		clearItems();
		for (Runnable run : copy) {
			addItem(run.toString());
		}
		setSelectedIndex(index);

		return deleted;
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
	protected List<TextPart> getLabel(int index,
			@SuppressWarnings("unused") int width, boolean selected,
			boolean focused) {
		List<TextPart> parts = new LinkedList<TextPart>();

		if (selected && focused) {
			parts.add(new TextPart("" + lines.getItems().get(index),
					ColorOption.CONTACT_LINE_SELECTED));
		} else {
			parts.add(new TextPart("" + lines.getItems().get(index),
					ColorOption.CONTACT_LINE));
		}

		return parts;
	}
}
