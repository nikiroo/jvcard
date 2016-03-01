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

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.input.KeyType;

public class ContactDetails extends MainContent {
	private Contact contact;
	private Panel top;
	private ImageTextControl txt;
	private Image image;
	private boolean fullscreenImage;

	public ContactDetails(Contact contact) {
		this.contact = contact;

		BorderLayout blayout = new BorderLayout();
		setLayoutManager(blayout);

		top = new Panel();
		setContact(contact);
		addComponent(top, BorderLayout.Location.TOP);
	}

	public void setContact(Contact contact) {
		Image img = null;
		this.contact = contact;

		if (contact != null) {
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

					img = new ImageIcon(Base64.getDecoder().decode(
							photo.getValue())).getImage();
				}
			}
		}

		setImage(img);
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
				if (txt != null) {
					txt.switchMode();
				}

				return false;
			}
		});
		actions.add(new KeyAction(Mode.NONE, 'i',
				Trans.StringId.KEY_ACTION_INVERT) {
			@Override
			public boolean onAction() {
				if (txt != null) {
					txt.invertColor();
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
	 * Set the {@link Image} to render.
	 * 
	 * @param image
	 *            the new {@link Image}
	 */
	private void setImage(Image image) {
		this.image = image;

		TerminalSize size = getTxtSize();
		if (size != null) {
			if (txt != null)
				txt.setSize(size);
			else
				txt = new ImageTextControl(image, size);
		}

		if (top.getChildCount() > 0)
			top.removeAllComponents();

		if (size != null)
			top.addComponent(txt);
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
				// TODO:
				return new TerminalSize(40, 20);
			}
		}

		return null;
	}
}
