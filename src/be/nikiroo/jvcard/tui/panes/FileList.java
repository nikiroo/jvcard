package be.nikiroo.jvcard.tui.panes;

import java.io.File;
import java.util.List;

import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;

import com.googlecode.lanterna.gui2.Label;

public class FileList extends MainContentList {
	private List<File> files;

	public FileList(List<File> files) {
		super(UiColors.Element.CONTACT_LINE,
				UiColors.Element.CONTACT_LINE_SELECTED);

		setFiles(files);
	}

	/**
	 * Change the list of currently selected files.
	 * 
	 * @param files
	 *            the new files
	 */
	public void setFiles(List<File> files) {
		clearItems();
		this.files = files;

		// TODO
		for (File file : files) {
			addItem(file.getName());
		}

		setSelectedIndex(0);
	}

	@Override
	public DataType getDataType() {
		return DataType.CARD_FILES;
	}

	@Override
	public String getExitWarning() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mode getMode() {
		return Mode.FILE_LIST;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}
