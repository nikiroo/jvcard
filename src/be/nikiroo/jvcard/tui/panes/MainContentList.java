package be.nikiroo.jvcard.tui.panes;

import be.nikiroo.jvcard.tui.UiColors;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.AbstractListBox.ListItemRenderer;

abstract public class MainContentList extends MainContent implements Runnable {
	private ActionListBox lines;

	public MainContentList(final UiColors.Element normalStyle,
			final UiColors.Element selectedStyle) {
		super(Direction.VERTICAL);

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
							graphics.setForegroundColor(selectedStyle
									.getForegroundColor());
							graphics.setBackgroundColor(selectedStyle
									.getBackgroundColor());
						} else {
							graphics.setForegroundColor(normalStyle
									.getForegroundColor());
							graphics.setBackgroundColor(normalStyle
									.getBackgroundColor());
						}

						// original impl:
						// String label = getLabel(listBox, index, item);
						// label = TerminalTextUtils.fitString(label,
						// graphics.getSize().getColumns());

						// TODO: why +5 ?? padding problem?
						String label = MainContentList.this.getLabel(index,
								lines.getSize().getColumns() + 5);
						graphics.putString(0, 0, label);
					}
				});

		addComponent(lines, LinearLayout
				.createLayoutData(LinearLayout.Alignment.Fill));
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

	/**
	 * Return the text representation of the selected line.
	 * 
	 * @param index
	 *            the line index
	 * @param width
	 *            the max width of the line
	 * 
	 * @return the text representation
	 */
	protected String getLabel(int index, int width) {
		return "" + lines.getItems().get(index);
	}
}
