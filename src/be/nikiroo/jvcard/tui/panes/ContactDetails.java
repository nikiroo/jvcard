package be.nikiroo.jvcard.tui.panes;

import java.awt.Image;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.tui.ImageTextControl;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.KeyType;

public class ContactDetails extends MainContent {
	private Contact contact;
	private Panel top;
	private ImageTextControl txtImage;
	private Image image;
	private boolean fullscreenImage;
	private Panel infoPanel;
	private Label note;

	public ContactDetails(Contact contact) {
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

		notePanel.addComponent(UiColors.Element.VIEW_CONTACT_NOTES_TITLE
				.createLabel("Notes:"));
		note = UiColors.Element.VIEW_CONTACT_NORMAL.createLabel("");
		notePanel.addComponent(note);

		setContact(contact);

		addComponent(top, BorderLayout.Location.TOP);
		addComponent(notePanel, BorderLayout.Location.CENTER);
	}

	/**
	 * Change the enclosed {@link Contact} from this {@link ContactDetails}.
	 * 
	 * @param contact
	 *            the new {@link Contact}
	 */
	public void setContact(Contact contact) {
		if (this.contact == contact)
			return;

		this.contact = contact;

		if (contact == null) {
			image = null;
		} else {
			infoPanel.removeAllComponents();

			String name = contact.getPreferredDataValue("FN");
			if (name == null || name.length() == 0) {
				// TODO format it ourself
				name = contact.getPreferredDataValue("N");
			}

			// TODO: i18n + do it properly
			infoPanel.addComponent(UiColors.Element.VIEW_CONTACT_NAME
					.createLabel(name));

			infoPanel.addComponent(UiColors.Element.VIEW_CONTACT_NORMAL
					.createLabel(""));
			infoPanel.addComponent(UiColors.Element.VIEW_CONTACT_NORMAL
					.createLabel("Phone:    "
							+ contact.getPreferredDataValue("TEL")));
			infoPanel.addComponent(UiColors.Element.VIEW_CONTACT_NORMAL
					.createLabel("eMail:    "
							+ contact.getPreferredDataValue("EMAIL")));
			infoPanel.addComponent(UiColors.Element.VIEW_CONTACT_NORMAL
					.createLabel(""));

			String notes = contact.getPreferredDataValue("NOTE");
			if (notes == null)
				notes = "";
			note.setText(notes.replaceAll("\\\\n", "\n"));

			Data photo = contact.getPreferredData("PHOTO");
			if (photo != null) {
				TypeInfo encoding = null;
				TypeInfo type = null;
				for (int index = 0; index < photo.size(); index++) {
					TypeInfo info = photo.get(index);
					if (info.getName() != null) {
						if (info.getName().equalsIgnoreCase("ENCODING"))
							encoding = info;
						if (info.getName().equalsIgnoreCase("TYPE"))
							type = info;
					}
				}

				if (encoding != null && encoding.getValue() != null
						&& encoding.getValue().equalsIgnoreCase("b")) {

					image = new ImageIcon(Base64.getDecoder().decode(
							photo.getValue())).getImage();
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

		// TODO
		actions.add(new KeyAction(Mode.NONE, KeyType.Tab,
				Trans.StringId.KEY_ACTION_SWITCH_FORMAT) {
			@Override
			public boolean onAction() {
				if (txtImage != null) {
					txtImage.switchMode();
				}

				return false;
			}
		});
		actions.add(new KeyAction(Mode.NONE, 'i',
				Trans.StringId.KEY_ACTION_INVERT) {
			@Override
			public boolean onAction() {
				if (txtImage != null) {
					txtImage.invertColor();
				}

				return false;
			}
		});
		actions.add(new KeyAction(Mode.NONE, 'f',
				Trans.StringId.KEY_ACTION_FULLSCREEN) {
			@Override
			public boolean onAction() {
				fullscreenImage = !fullscreenImage;
				setImage(image);
				return false;
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

		if (txtImage != null && top.containsComponent(txtImage))
			top.removeComponent(txtImage);

		TerminalSize size = getTxtSize();
		if (size != null) {
			if (txtImage != null)
				txtImage.setSize(size);
			else
				txtImage = new ImageTextControl(image, size);
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
			} else {
				// TODO: configure size?
				int w = getSize().getColumns() - 40;
				int h = getSize().getRows() - 5;
				if (w <= 0 || h <= 0)
					return null;

				return new TerminalSize(w, h);
			}
		}

		return null;
	}
}
