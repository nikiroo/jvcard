package be.nikiroo.jvcard.tui.panes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.launcher.CardResult;
import be.nikiroo.jvcard.launcher.CardResult.MergeCallback;
import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.parsers.Format;
import be.nikiroo.jvcard.resources.ColorOption;
import be.nikiroo.jvcard.resources.StringId;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.utils.StringUtils;

import com.googlecode.lanterna.input.KeyType;

public class FileList extends MainContentList {
	private List<String> files;
	private List<CardResult> cards;

	private FileList merger;
	private String mergeRemoteState;
	private String mergeSourceFile;
	private File mergeTargetFile;

	public FileList(List<String> files) {
		setFiles(files);
	}

	/**
	 * Change the list of currently selected files.
	 * 
	 * @param files
	 *            the new files
	 */
	public void setFiles(List<String> files) {
		clearItems();
		this.files = files;
		cards = new ArrayList<CardResult>();

		for (String file : files) {
			addItem(file); // TODO
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

		ColorOption el = (focused && selected) ? ColorOption.CONTACT_LINE_SELECTED
				: ColorOption.CONTACT_LINE;
		ColorOption elSep = (focused && selected) ? ColorOption.CONTACT_LINE_SEPARATOR_SELECTED
				: ColorOption.CONTACT_LINE_SEPARATOR;

		List<TextPart> parts = new LinkedList<TextPart>();

		String count = "";
		if (cards.get(index) != null) {
			try {
				count += cards.get(index).getCard().size();
			} catch (IOException e) {
			}
		}

		String name = files.get(index).replaceAll("\\\\", "/");
		int indexSl = name.lastIndexOf('/');
		if (indexSl >= 0) {
			name = name.substring(indexSl + 1);
		}

		name = StringUtils.sanitize(name, Main.isUnicode());

		count = " " + StringUtils.padString(count, SIZE_COL_1) + " ";
		name = " "
				+ StringUtils.padString(name, width - SIZE_COL_1
						- getSeparator().length()) + " ";

		parts.add(new TextPart(count, el));
		parts.add(new TextPart(getSeparator(), elSep));
		parts.add(new TextPart(name, el));

		return parts;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		List<KeyAction> actions = new LinkedList<KeyAction>();

		// TODO del, save...
		actions.add(new KeyAction(Mode.CONTACT_LIST, KeyType.Enter,
				StringId.KEY_ACTION_VIEW_CARD) {
			private Object obj = null;

			@Override
			public Object getObject() {
				if (obj == null) {
					int index = getSelectedIndex();
					if (index < 0 || index >= cards.size())
						return null;

					try {
						if (cards.get(index) != null) {
							obj = cards.get(index).getCard();
						} else {
							String file = files.get(index);

							CardResult card = null;
							final Card arr[] = new Card[4];
							try {
								card = Main.getCard(file, new MergeCallback() {
									@Override
									public Card merge(Card previous,
											Card local, Card server,
											Card autoMerged) {
										arr[0] = previous;
										arr[1] = local;
										arr[2] = server;
										arr[3] = autoMerged;

										return null;
									}
								});

								obj = card.getCard(); // throw IOE if problem
							} catch (IOException e) {
								if (arr[0] == null)
									throw e;

								// merge management: set all merge vars in
								// merger,
								// make sure it has cards but mergeTargetFile
								// does not exist
								// (create then delete if needed)
								// TODO: i18n
								setMessage(
										"Merge error, please check/fix the merged contact",
										true);

								// TODO: i18n + filename with numbers in it to
								// fix
								File a = File.createTempFile("Merge result ",
										".vcf");
								File p = File.createTempFile(
										"Previous common version ", ".vcf");
								File l = File.createTempFile("Local ", ".vcf");
								File s = File.createTempFile("Remote ", ".vcf");
								arr[3].saveAs(a, Format.VCard21);
								arr[0].saveAs(p, Format.VCard21);
								arr[1].saveAs(l, Format.VCard21);
								arr[2].saveAs(s, Format.VCard21);
								List<String> mfiles = new LinkedList<String>();
								mfiles.add(a.getAbsolutePath());
								mfiles.add(p.getAbsolutePath());
								mfiles.add(l.getAbsolutePath());
								mfiles.add(s.getAbsolutePath());
								merger = new FileList(mfiles);
								merger.mergeRemoteState = arr[2]
										.getContentState(false);
								merger.mergeSourceFile = files.get(index);
								merger.mergeTargetFile = a;

								obj = merger;
								return obj;
							}

							cards.set(index, card);

							invalidate();

							if (card.isSynchronised()) {
								// TODO i18n
								if (card.isChanged())
									setMessage(
											"card synchronised: changes from server",
											false);
								else
									setMessage("card synchronised: no changes",
											false);
							}
						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
						// TODO
						setMessage("ERROR!", true);
					}
				}

				return obj;
			}

		});

		return actions;
	}

	@Override
	public String wakeup() throws IOException {
		String s = super.wakeup();
		if (s != null)
			return s;

		if (merger != null) {
			if (!merger.mergeTargetFile.exists()) {
				throw new IOException("Merge cancelled");
			}

			// merge back to server if needed and not changed:
			try {
				Main.getCard(merger.mergeSourceFile, new MergeCallback() {
					@Override
					public Card merge(Card previous, Card local, Card server,
							Card autoMerged) {
						try {
							if (server.getContentState(false).equals(
									merger.mergeRemoteState)) {
								return new Card(merger.mergeTargetFile,
										Format.VCard21);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

						return null;
					}
				}).getCard();
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Server changed since merge, cancel", e);
			}

			merger = null;

			// TODO i18n
			return "merged.";
		}

		return null;
	}
}
