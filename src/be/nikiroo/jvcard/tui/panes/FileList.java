package be.nikiroo.jvcard.tui.panes;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;

import com.googlecode.lanterna.input.KeyType;

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
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO del, save...
		actions.add(new KeyAction(Mode.CONTACT_LIST, KeyType.Enter,
				Trans.StringId.KEY_ACTION_VIEW_CARD) {
			@Override
			public Object getObject() {
				File file = files.get(getSelectedIndex());
				Format format = Format.Abook;
				String ext = file.getName();
				if (ext.contains(".")) {
					String tab[] = ext.split("\\.");
					if (tab.length > 1
							&& tab[tab.length - 1].equalsIgnoreCase("vcf")) {
						format = Format.VCard21;
					}
				}
				try {
					return new Card(file, format);
				} catch (IOException ioe) {
					ioe.printStackTrace();
					return null;
				}
			}
		});

		return actions;
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
