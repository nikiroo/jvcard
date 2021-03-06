package be.nikiroo.jvcard.tui.panes;

import java.awt.Image;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.resources.ColorOption;
import be.nikiroo.jvcard.resources.DisplayBundle;
import be.nikiroo.jvcard.resources.DisplayOption;
import be.nikiroo.jvcard.resources.StringId;
import be.nikiroo.jvcard.tui.ImageTextControl;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors;
import be.nikiroo.utils.StringUtils;
import be.nikiroo.utils.ui.ImageUtilsAwt;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextBox.Style;
import com.googlecode.lanterna.input.KeyType;

public class ContactDetails extends MainContent {
	private Contact contact;
	private Panel top;
	private ImageTextControl txtImage;
	private Image image;
	private boolean fullscreenImage;
	private Panel infoPanel;
	private TextBox note;

	// from .properties file:
	private int labelSize = -1;
	private String infoFormat = "";

	//

	public ContactDetails(Contact contact) {
		// Get the .properties info:
		DisplayBundle map = new DisplayBundle();
		labelSize = map.getInteger(DisplayOption.CONTACT_DETAILS_LABEL_WIDTH,
				-1);
		infoFormat = map.getString(DisplayOption.CONTACT_DETAILS_INFO);
		//

		BorderLayout blayout = new BorderLayout();
		setLayoutManager(blayout);

		top = new Panel();
		blayout = new BorderLayout();
		top.setLayoutManager(blayout);

		infoPanel = new Panel();
		infoPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
		top.addComponent(infoPanel, BorderLayout.Location.CENTER);

		Panel notePanel = new Panel();
		notePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

		notePanel.addComponent(UiColors.createLabel(
				ColorOption.VIEW_CONTACT_NOTES_TITLE, "Notes:"));
		// 10000x10000 is probably enough or "max"
		note = new TextBox(new TerminalSize(10000, 10000), Style.MULTI_LINE);
		note.setReadOnly(true);
		notePanel.addComponent(note);
		note.setVerticalFocusSwitching(false);
		note.setHorizontalFocusSwitching(false);

		setContact(contact);

		addComponent(top.withBorder(Borders.doubleLineBevel()),
				BorderLayout.Location.TOP);
		addComponent(notePanel.withBorder(Borders.singleLineBevel()),
				BorderLayout.Location.CENTER);
	}

	/**
	 * Change the enclosed {@link Contact} from this {@link ContactDetails}.
	 * Also re-set the image.
	 * 
	 * @param contact
	 *            the new {@link Contact}
	 */
	public void setContact(Contact contact) {
		this.contact = contact;
		image = null;

		if (contact != null) {
			infoPanel.removeAllComponents();

			String name = contact.getPreferredDataValue("FN");
			infoPanel.addComponent(UiColors.createLabel(
					ColorOption.VIEW_CONTACT_NAME, name));
			infoPanel.addComponent(UiColors.createLabel(
					ColorOption.VIEW_CONTACT_NORMAL, ""));

			// List of infos:
			String[] infos = infoFormat.split("\\|");
			for (String info : infos) {
				// # - "=FIELD" will take the preferred value for this field
				// # - "+FIELD" will take the preferred value for this field and
				// highlight it
				// # - "#FIELD" will take all the values with this field's name
				// # - "*FIELD" will take all the values with this field's name,
				// highlighting the preferred one
				// #

				boolean hl = false;
				boolean all = false;
				if (info.contains("+") || info.contains("#")) {
					hl = true;
				}
				if (info.contains("*") || info.contains("#")) {
					all = true;
				}

				if (all || hl || info.contains("=")) {
					ColorOption el = hl ? ColorOption.VIEW_CONTACT_HIGHLIGHT
							: ColorOption.VIEW_CONTACT_NORMAL;

					int index = info.indexOf('=');
					if (index < 0) {
						index = info.indexOf('+');
					}
					if (index < 0) {
						index = info.indexOf('#');
					}
					if (index < 0) {
						index = info.indexOf('*');
					}

					String label = info.substring(0, index);
					String field = info.substring(index + 1);

					if (all) {
						Data pref = contact.getPreferredData(field);
						for (Data data : contact.getData(field)) {
							if (data == pref) {
								infoPanel.addComponent(UiColors.createLabel(el,
										StringUtils.padString(label, labelSize)
												+ data.toString()));
							} else {
								infoPanel.addComponent(UiColors.createLabel(
										ColorOption.VIEW_CONTACT_NORMAL,
										StringUtils.padString(label, labelSize)
												+ data.toString()));
							}
						}
					} else {
						String val = contact.getPreferredDataValue(field);
						if (val == null) {
							val = "";
						}
						infoPanel.addComponent(UiColors.createLabel(el,
								StringUtils.padString(label, labelSize) + val));
					}
				} else {
					String label = info;
					infoPanel.addComponent(UiColors.createLabel(
							ColorOption.VIEW_CONTACT_NORMAL,
							StringUtils.padString(label, labelSize)));
				}
			}
			// end of list

			infoPanel.addComponent(UiColors.createLabel(
					ColorOption.VIEW_CONTACT_NORMAL, ""));

			String notes = contact.getPreferredDataValue("NOTE");
			if (notes == null) {
				notes = "";
			}
			note.setText(notes);

			Data photo = contact.getPreferredData("PHOTO");
			if (photo != null) {
				TypeInfo encoding = null;
				for (TypeInfo info : photo) {
					if (info.getName() != null) {
						if (info.getName().equalsIgnoreCase("ENCODING"))
							encoding = info;
						// We don't check for the "TYPE" anymore, we just defer
						// it to ImageIcon
					}
				}

				if (encoding != null && encoding.getValue() != null
						&& encoding.getValue().equalsIgnoreCase("b")) {
					try {
						be.nikiroo.utils.Image img = new be.nikiroo.utils.Image(
								photo.getValue());
						try {
							image = ImageUtilsAwt.fromImage(img);
						} finally {
							img.close();
						}
					} catch (Exception e) {
						System.err.println("Cannot parse image for contact: "
								+ contact.getPreferredDataValue("UID"));
					}
				}
			}
		}

		setImage(image);
	}

	@Override
	public DataType getDataType() {
		return DataType.DATA;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		List<KeyAction> actions = new LinkedList<KeyAction>();

		actions.add(new KeyAction(Mode.NONE, KeyType.Tab,
				StringId.KEY_ACTION_SWITCH_FORMAT) {
			@Override
			public boolean onAction() {
				if (txtImage != null) {
					txtImage.switchMode();
				}

				return false;
			}
		});
		actions.add(new KeyAction(Mode.NONE, 'i', StringId.KEY_ACTION_INVERT) {
			@Override
			public boolean onAction() {
				if (txtImage != null) {
					txtImage.invertColor();
				}

				return false;
			}
		});
		actions.add(new KeyAction(Mode.NONE, 'f',
				StringId.KEY_ACTION_FULLSCREEN) {
			@Override
			public boolean onAction() {
				fullscreenImage = !fullscreenImage;
				setImage(image);
				return false;
			}
		});
		// TODO: add "normal" edit
		actions.add(new KeyAction(Mode.CONTACT_DETAILS_RAW, 'r',
				StringId.KEY_ACTION_EDIT_CONTACT_RAW) {
			@Override
			public Object getObject() {
				return contact;
			}
		});

		return actions;
	}

	@Override
	public synchronized Panel setSize(TerminalSize size) {
		super.setSize(size);
		setImage(image);
		return this;
	}

	/**
	 * Set the {@link Image} to render and refresh it to the current size
	 * constraints.
	 * 
	 * @param image
	 *            the new {@link Image}
	 */
	private void setImage(Image image) {
		this.image = image;

		if (txtImage != null && top.containsComponent(txtImage)) {
			top.removeComponent(txtImage);
		}

		TerminalSize size = getTxtSize();
		if (size != null) {
			if (txtImage != null) {
				txtImage.setSize(size);
			} else {
				txtImage = new ImageTextControl(image, size);
			}
		}

		if (size != null) {
			top.addComponent(txtImage, BorderLayout.Location.LEFT);
		}

		invalidate();
	}

	/**
	 * Compute the size to use for the {@link Image} text rendering. Return NULL
	 * in case of error.
	 * 
	 * @return the {@link TerminalSize} to use or NULL if it is not possible
	 */
	private TerminalSize getTxtSize() {
		if (image != null && getSize() != null && getSize().getColumns() > 0
				&& getSize().getRows() > 0) {
			if (fullscreenImage) {
				return getSize();
			}

			// TODO: configure size?
			int w = getSize().getColumns() - 40;
			int h = getSize().getRows() - 9;
			if (w <= 0 || h <= 0) {
				return null;
			}

			return new TerminalSize(w, h);
		}

		return null;
	}
}
