package be.nikiroo.jvcard.tui.windows;

import java.util.List;

import jexer.TAction;
import jexer.TApplication;
import jexer.TTable;
import jexer.event.TResizeEvent;
import be.nikiroo.jvcard.tui.panes.MainContent;

public abstract class TuiBrowserWindow extends TuiBasicWindow {
	private TTable table;
	private boolean showHeader;

	public TuiBrowserWindow(TuiBasicWindow parent, String title,
			boolean showHeaders) {
		super(parent, title);
		init(showHeaders);
	}

	public TuiBrowserWindow(TApplication app, int width, int height,
			String title, boolean showHeaders) {
		super(app, title, width, height);
		init(showHeaders);
	}

	private void init(boolean showHeaders) {
		this.showHeader = showHeaders;

		table = new TTable(this, 0, 0, getWidth() - 2, getHeight() - 2,
				new TAction() {
					@Override
					public void DO() {
						onAction(table.getSelectedRow(),
								table.getSelectedColumn());
					}
				}, null);
	}

	/**
	 * Change the currently displayed data.
	 * 
	 * @param headers
	 *            the table headers (mandatory)
	 * @param rows
	 *            the data to display
	 */
	public void setData(List<String> headers, List<List<String>> rows) {
		int prevRow = table.getSelectedRow();
		int prevColumn = table.getSelectedColumn();

		table.clear();
		table.setHeaders(headers, showHeader);
		for (List<String> row : rows) {
			table.addRow(row);
		}

		table.reflow();

		table.setSelectedRow(Math.min(prevRow, table.getNumberOfRows() - 1));
		table.setSelectedColumn(Math.min(prevColumn,
				table.getNumberOfColumns() - 1));
	}

	/**
	 * Return the number of items in this {@link MainContent}, or -1 if this
	 * {@link MainContent} is not countable.
	 * 
	 * @return -1 or the number of present items
	 */
	public int size() {
		return table.getNumberOfRows();
	}

	/**
	 * An item has been selected.
	 * 
	 * @param selectedRow
	 *            the currently selected row
	 * @param selectedColumn
	 *            the currently selected column
	 */
	@SuppressWarnings("unused")
	public void onAction(int selectedRow, int selectedColumn) {
	}

	@Override
	public void onResize(TResizeEvent resize) {
		super.onResize(resize);
		// Will be NULL at creation time in super()
		if (table != null) {
			table.setWidth(getWidth() - 2);
			table.setHeight(getHeight() - 2);
			table.reflow();
		}
	}
}
