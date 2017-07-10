package be.nikiroo.jvcard.tui.windows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import jexer.TTable;
import jexer.TWindow;
import jexer.event.TKeypressEvent;
import jexer.event.TResizeEvent;
import be.nikiroo.jvcard.tui.panes.MainContent;

public abstract class TuiBrowserWindow extends TWindow {
	private TApplication app;
	private TTable table;
	private boolean showHeader;
	private Map<TKeypress, TAction> keyBindings;

	public TuiBrowserWindow(TApplication app, String title, boolean showHeaders) {
		super(app, title, 10, 10);

		this.app = app;
		this.showHeader = showHeaders;

		table = new TTable(this, 0, 0, getWidth(), getHeight(), new TAction() {
			@Override
			public void DO() {
				onAction(table.getSelectedLine(), table.getSelectedColumn());
			}
		}, null);

		keyBindings = new HashMap<TKeypress, TAction>();

		// TODO: fullscreen selection?

		// TODO: auto-maximize on FS, auto-resize on maximize
		// setFullscreen(true);
		maximize();
		onResize(null);
	}

	/**
	 * Change the currently displayed data.
	 * 
	 * @param headers
	 *            the table headers (mandatory)
	 * @param lines
	 *            the data to display
	 */
	public void setData(List<String> headers, List<List<String>> lines) {
		int prevLine = table.getSelectedLine();
		int prevColumn = table.getSelectedColumn();

		table.clear();
		table.setHeaders(headers, showHeader);
		for (List<String> line : lines) {
			table.addLine(line);
		}

		table.reflow();

		table.setSelectedLine(Math.min(prevLine, table.getNumberOfLines() - 1));
		table.setSelectedColumn(Math.min(prevColumn,
				table.getNumberOfColumns() - 1));
	}

	public void addKeyBinding(TKeypress key, TAction action) {
		keyBindings.put(key, action);
	}

	/**
	 * Return the number of items in this {@link MainContent}, or -1 if this
	 * {@link MainContent} is not countable.
	 * 
	 * @return -1 or the number of present items
	 */
	public int size() {
		return table.getNumberOfLines();
	}

	/**
	 * Close the window.
	 */
	public void close() {
		app.closeWindow(this);
	}

	/**
	 * An item has been selected.
	 * 
	 * @param selectedLine
	 *            the currently selected line
	 * @param selectedColumn
	 *            the currently selected column
	 */
	@SuppressWarnings("unused")
	public void onAction(int selectedLine, int selectedColumn) {
	}

	@Override
	public void onResize(TResizeEvent resize) {
		super.onResize(resize);
		table.setWidth(getWidth());
		table.setHeight(getHeight());
		table.reflow();
	}

	@Override
	public void onKeypress(TKeypressEvent keypress) {
		if (keyBindings.containsKey(keypress.getKey())) {
			keyBindings.get(keypress.getKey()).DO();
		} else {
			super.onKeypress(keypress);
		}
	}
}
