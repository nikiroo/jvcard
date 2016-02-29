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
	private ImageTextControl txt;

	public ContactDetails(Contact contact) {
		this.contact = contact;

		BorderLayout blayout = new BorderLayout();
		setLayoutManager(blayout);

		Panel top = new Panel();
		if (contact != null) {
			Data photo = contact.getPreferredData("PHOTO");
			if (photo != null) {
				TypeInfo encoding = null;
				TypeInfo type = null;
				for (TypeInfo info : photo.getTypes()) {
					if (info.getName() != null) {
						if (info.getName().equalsIgnoreCase("ENCODING"))
							encoding = info;
						if (info.getName().equalsIgnoreCase("TYPE"))
							type = info;
					}
				}

				if (encoding != null && encoding.getValue() != null
						&& encoding.getValue().equalsIgnoreCase("b")) {

					Image img = new ImageIcon(Base64.getDecoder().decode(
							photo.getValue())).getImage();

					TerminalSize size = new TerminalSize(40, 20);
					size = new TerminalSize(100, 50);

					txt = new ImageTextControl(img, size);
					top.addComponent(txt);
				}
			}
		}

		addComponent(top, BorderLayout.Location.TOP);
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
				Trans.StringId.DUMMY) {
			@Override
			public boolean onAction() {
				if (txt != null) {
					txt.invertColor();
				}

				return false;
			}
		});

		return actions;
	}
}
