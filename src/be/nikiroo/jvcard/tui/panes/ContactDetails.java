package be.nikiroo.jvcard.tui.panes;

import java.awt.Image;
import java.util.Base64;
import java.util.List;

import javax.swing.ImageIcon;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.TypeInfo;
import be.nikiroo.jvcard.tui.ImageText;
import be.nikiroo.jvcard.tui.KeyAction;
import be.nikiroo.jvcard.tui.KeyAction.DataType;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

public class ContactDetails extends MainContent {
	private Contact contact;

	@Override
	public DataType getDataType() {
		return DataType.DATA;
	}

	@Override
	public List<KeyAction> getKeyBindings() {
		// TODO Auto-generated method stub
		return null;
	}

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
					size = new TerminalSize(120, 50);

					String str = new ImageText(img, size).getText();
					top.addComponent(new TextBox(size, str));
				}
			}
		}

		addComponent(top, BorderLayout.Location.TOP);
	}
}
