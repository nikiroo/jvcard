package be.nikiroo.jvcard.tui.panes;

import java.util.List;

import be.nikiroo.jvcard.tui.KeyAction;

import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

/**
 * This class represents the main content that you can see in this application
 * (i.e., everything but the title and the actions keys is a {@link Panel}
 * extended from this class).
 * 
 * @author niki
 * 
 */
abstract public class MainContent extends Panel {

	public MainContent() {
		super();
	}

	public MainContent(Direction dir) {
		super();
		LinearLayout layout = new LinearLayout(dir);
		layout.setSpacing(0);
		setLayoutManager(layout);
	}

	/**
	 * The kind of data displayed by this {@link MainContent}.
	 * 
	 * @return the kind of data displayed
	 */
	abstract public KeyAction.DataType getDataType();

	/**
	 * Returns the list of actions and the keys that are bound to it.
	 * 
	 * @return the list of actions
	 */
	abstract public List<KeyAction> getKeyBindings();

	/**
	 * The title to display in addition to the application name, or NULL for the
	 * default application name.
	 * 
	 * @return the title or NULL
	 */
	public String getTitle() {
		return null;
	}

	/**
	 * Returns an error message ready to be displayed if we should ask something
	 * to the user before exiting.
	 * 
	 * @return an error message or NULL
	 */
	public String getExitWarning() {
		return null;
	}

	/**
	 * Move the active cursor (not the text cursor, but the currently active
	 * item).
	 * 
	 * @param x
	 *            the horizontal move (&lt; 0 for left, &gt; 0 for right)
	 * @param y
	 *            the vertical move (&lt; 0 for up, &gt; 0 for down)
	 * 
	 * @return the error message to display if any
	 */
	public String move(int x, int y) {
		return null;
	}

	/**
	 * Return the number of items in this {@link MainContent}, or -1 if this
	 * {@link MainContent} is not countable.
	 * 
	 * @return -1 or the number of present items
	 */
	public int getCount() {
		return -1;
	}

	/**
	 * Refresh the display according to the actual data (this method should be
	 * called when the data changed).
	 */
	public void refreshData() {
		invalidate();
	}
}
