package be.nikiroo.jvcard.tui;

import java.util.List;

import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.tui.KeyAction.DataType;
import be.nikiroo.jvcard.tui.KeyAction.Mode;

import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;

public class ContactDetails extends MainContent {
	private Contact contact;

	public ContactDetails(Contact contact) {
		super(Direction.VERTICAL);

		this.contact = contact;

		for (Data data : contact.getContent()) {
			addComponent(new Label(data.getName() + ": " + data.getValue()));
		}
	}

	@Override
	public DataType getDataType() {
		return DataType.CONTACT;
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
		return Mode.CONTACT_DETAILS;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String move(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}
}
