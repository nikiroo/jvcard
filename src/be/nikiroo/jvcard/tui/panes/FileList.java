package be.nikiroo.jvcard.tui.panes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.StringUtils;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.input.KeyType;

public class FileList extends MainContentList {
	private List<File> files;
	private List<Card> cards;

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
		cards = new ArrayList<Card>();

		for (File file : files) {
			addItem(file.getName());
			cards.add(null);
		}

		setSelectedIndex(0);
	}

	@Override
	public DataType getDataType() {
		return DataType.CARD_FILES;
	}

	@Override
	protected List<TextPart> getLabel(int index, int width, boolean selected,
			boolean focused) {
		// TODO: from ini file?
		int SIZE_COL_1 = 3;

		Element el = (focused && selected) ? Element.CONTACT_LINE_SELECTED
				: Element.CONTACT_LINE;
		Element elSep = (focused && selected) ? Element.CONTACT_LINE_SEPARATOR_SELECTED
				: Element.CONTACT_LINE_SEPARATOR;

		List<TextPart> parts = new LinkedList<TextPart>();

		String count = "";
		if (cards.get(index) != null)
			count += cards.get(index).size();

		String name = files.get(index).getName();

		count = " " + StringUtils.padString(count, SIZE_COL_1) + " ";
		name = " "
				+ StringUtils.padString(name, width - SIZE_COL_1
						- getSeparator().length()) + " ";

		parts.add(new TextPart(count, el));
		parts.add(new TextPart(getSeparator(), elSep));
		parts.add(new TextPart(name, el));

		return parts;
	};

	@Override
	public List<KeyAction> getKeyBindings() {
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO del, save...
		actions.add(new KeyAction(Mode.CONTACT_LIST, KeyType.Enter,
				Trans.StringId.KEY_ACTION_VIEW_CARD) {
			@Override
			public Object getObject() {
				int index = getSelectedIndex();

				if (index < 0 || index >= cards.size())
					return null;

				if (cards.get(index) != null)
					return cards.get(index);

				File file = files.get(index);
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
					Card card = new Card(file, format);
					cards.set(index, card);

					invalidate();

					return card;
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
}
