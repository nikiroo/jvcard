package be.nikiroo.jvcard.tui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.i18n.Trans.StringId;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

/**
 * This is the main "window" of the program. It will host one
 * {@link MainContent} at any one time.
 * 
 * @author niki
 * 
 */
public class MainWindow extends BasicWindow {
	private List<KeyAction> defaultActions = new LinkedList<KeyAction>();
	private List<KeyAction> actions = new LinkedList<KeyAction>();
	private List<MainContent> content = new LinkedList<MainContent>();
	private boolean actionsPadded;
	private Boolean waitForOneKeyAnswer; // true, false, (null = do not wait for
	// an answer)
	private String title;
	private Panel titlePanel;
	private Panel mainPanel;
	private Panel contentPanel;
	private Panel actionPanel;
	private Panel messagePanel;
	private TextBox text;

	public MainWindow() {
		this(null);
	}

	public MainWindow(MainContent content) {
		super(content == null ? "" : content.getTitle());

		setHints(Arrays.asList(Window.Hint.FULL_SCREEN,
				Window.Hint.NO_DECORATIONS, Window.Hint.FIT_TERMINAL_WINDOW));

		defaultActions.add(new KeyAction(Mode.BACK, 'q',
				StringId.KEY_ACTION_BACK));
		defaultActions.add(new KeyAction(Mode.BACK, KeyType.Escape,
				StringId.NULL));
		defaultActions.add(new KeyAction(Mode.HELP, 'h',
				StringId.KEY_ACTION_HELP));
		defaultActions.add(new KeyAction(Mode.HELP, KeyType.F1, StringId.NULL));

		actionPanel = new Panel();
		contentPanel = new Panel();
		mainPanel = new Panel();
		messagePanel = new Panel();
		titlePanel = new Panel();

		Panel actionMessagePanel = new Panel();

		LinearLayout llayout = new LinearLayout(Direction.HORIZONTAL);
		llayout.setSpacing(0);
		actionPanel.setLayoutManager(llayout);

		llayout = new LinearLayout(Direction.VERTICAL);
		llayout.setSpacing(0);
		titlePanel.setLayoutManager(llayout);

		llayout = new LinearLayout(Direction.VERTICAL);
		llayout.setSpacing(0);
		messagePanel.setLayoutManager(llayout);

		BorderLayout blayout = new BorderLayout();
		mainPanel.setLayoutManager(blayout);

		blayout = new BorderLayout();
		contentPanel.setLayoutManager(blayout);

		blayout = new BorderLayout();
		actionMessagePanel.setLayoutManager(blayout);

		actionMessagePanel
				.addComponent(messagePanel, BorderLayout.Location.TOP);
		actionMessagePanel.addComponent(actionPanel,
				BorderLayout.Location.CENTER);

		mainPanel.addComponent(titlePanel, BorderLayout.Location.TOP);
		mainPanel.addComponent(contentPanel, BorderLayout.Location.CENTER);
		mainPanel
				.addComponent(actionMessagePanel, BorderLayout.Location.BOTTOM);

		pushContent(content);

		setComponent(mainPanel);
	}

	public void pushContent(MainContent content) {
		List<KeyAction> actions = null;
		String title = null;

		contentPanel.removeAllComponents();
		if (content != null) {
			title = content.getTitle();
			actions = content.getKeyBindings();
			contentPanel.addComponent(content, BorderLayout.Location.CENTER);
			this.content.add(content);
		}

		setTitle(title);
		setActions(actions, true, true);
		invalidate();
	}

	/**
	 * Set the application title.
	 * 
	 * @param title
	 *            the new title or NULL for the default title
	 */
	public void setTitle(String title) {
		if (title == null) {
			title = Trans.StringId.TITLE.trans();
		}

		if (!title.equals(this.title)) {
			super.setTitle(title);
			this.title = title;
		}

		Label lbl = new Label(title);
		titlePanel.removeAllComponents();

		titlePanel.addComponent(lbl, LinearLayout
				.createLayoutData(LinearLayout.Alignment.Center));
	}

	@Override
	public void draw(TextGUIGraphics graphics) {
		setTitle(title);
		if (!actionsPadded) {
			// fill with "desc" colour
			actionPanel.addComponent(UiColors.Element.ACTION_DESC
					.createLabel(StringUtils.padString("", graphics.getSize()
							.getColumns())));
			actionsPadded = true;
		}
		super.draw(graphics);
	}

	public MainContent popContent() {
		MainContent removed = null;
		MainContent prev = null;
		if (content.size() > 0)
			removed = content.remove(content.size() - 1);
		if (content.size() > 0)
			prev = content.remove(content.size() - 1);
		pushContent(prev);

		return removed;
	}

	private void setActions(List<KeyAction> actions, boolean allowKeys,
			boolean enableDefaultactions) {

		this.actions.clear();
		actionsPadded = false;
		
		if (enableDefaultactions)
			this.actions.addAll(defaultActions);

		if (actions != null)
			this.actions.addAll(actions);

		actionPanel.removeAllComponents();
		for (KeyAction action : this.actions) {
			String trans = " " + action.getStringId().trans() + " ";

			if ("  ".equals(trans))
				continue;

			String keyTrans = "";
			switch (action.getKey().getKeyType()) {
			case Enter:
				keyTrans = " ⤶ ";
				break;
			case Tab:
				keyTrans = " ↹ ";
				break;
			case Character:
				keyTrans = " " + action.getKey().getCharacter() + " ";
				break;
			default:
				keyTrans = "" + action.getKey().getKeyType();
				int width = 3;
				if (keyTrans.length() > width) {
					keyTrans = keyTrans.substring(0, width);
				} else if (keyTrans.length() < width) {
					keyTrans = keyTrans
							+ new String(new char[width - keyTrans.length()])
									.replace('\0', ' ');
				}
				break;
			}

			Panel kPane = new Panel();
			LinearLayout layout = new LinearLayout(Direction.HORIZONTAL);
			layout.setSpacing(0);
			kPane.setLayoutManager(layout);

			kPane.addComponent(UiColors.Element.ACTION_KEY
					.createLabel(keyTrans));
			kPane.addComponent(UiColors.Element.ACTION_DESC.createLabel(trans));

			actionPanel.addComponent(kPane);
		}
	}

	/**
	 * Show the given message on screen. It will disappear at the next action.
	 * 
	 * @param mess
	 *            the message to display
	 * @param error
	 *            TRUE for an error message, FALSE for an information message
	 */
	public void setMessage(String mess, boolean error) {
		messagePanel.removeAllComponents();
		if (mess != null) {
			Element element = (error ? UiColors.Element.LINE_MESSAGE_ERR
					: UiColors.Element.LINE_MESSAGE);
			Label lbl = element.createLabel(" " + mess + " ");
			messagePanel.addComponent(lbl, LinearLayout
					.createLayoutData(LinearLayout.Alignment.Center));
		}
	}

	public void setQuestion(String mess, boolean oneKey) {
		messagePanel.removeAllComponents();
		if (mess != null) {
			waitForOneKeyAnswer = oneKey;

			Panel hpanel = new Panel();
			LinearLayout llayout = new LinearLayout(Direction.HORIZONTAL);
			llayout.setSpacing(0);
			hpanel.setLayoutManager(llayout);

			Label lbl = UiColors.Element.LINE_MESSAGE_QUESTION.createLabel(" "
					+ mess + " ");
			text = new TextBox(new TerminalSize(getSize().getColumns()
					- lbl.getSize().getColumns(), 1));

			hpanel.addComponent(lbl, LinearLayout
					.createLayoutData(LinearLayout.Alignment.Beginning));
			hpanel.addComponent(text, LinearLayout
					.createLayoutData(LinearLayout.Alignment.Fill));

			messagePanel.addComponent(hpanel, LinearLayout
					.createLayoutData(LinearLayout.Alignment.Beginning));

			this.setFocusedInteractable(text);
		}
	}

	private String handleQuestion(KeyStroke key) {
		String answer = null;

		if (waitForOneKeyAnswer) {
			answer = "" + key.getCharacter();
		} else {
			if (key.getKeyType() == KeyType.Enter) {
				if (text != null)
					answer = text.getText();
				else
					answer = "";
			}
		}

		if (answer != null) {
			Interactable focus = null;
			if (this.content.size() > 0)
				// focus = content.get(0).getDefaultFocusElement();
				focus = content.get(0).nextFocus(null);

			this.setFocusedInteractable(focus);
		}

		return answer;
	}

	@Override
	public boolean handleInput(KeyStroke key) {
		boolean handled = false;

		if (waitForOneKeyAnswer != null) {
			String answer = handleQuestion(key);
			if (answer != null) {
				waitForOneKeyAnswer = null;
				setMessage("ANS: " + answer, false);

				handled = true;
			}
		} else {
			setMessage(null, false);

			for (KeyAction action : actions) {
				if (!action.match(key))
					continue;

				handled = true;

				if (action.onAction()) {
					switch (action.getMode()) {
					case MOVE:
						int x = 0;
						int y = 0;

						if (action.getKey().getKeyType() == KeyType.ArrowUp)
							x = -1;
						if (action.getKey().getKeyType() == KeyType.ArrowDown)
							x = 1;
						if (action.getKey().getKeyType() == KeyType.ArrowLeft)
							y = -1;
						if (action.getKey().getKeyType() == KeyType.ArrowRight)
							y = 1;

						if (content.size() > 0) {
							String err = content.get(content.size() - 1).move(
									x, y);
							if (err != null)
								setMessage(err, true);
						}

						break;
					// mode with windows:
					case CONTACT_LIST:
						Card card = action.getCard();
						if (card != null) {
							pushContent(new ContactList(card));
						}
						break;
					case CONTACT_DETAILS:
						Contact contact = action.getContact();
						if (contact != null) {
							pushContent(new ContactDetails(contact));
						}
						break;
					// mode interpreted by MainWindow:
					case HELP:
						// TODO
						// setMessage("Help! I need somebody! Help!", false);
						setQuestion("Test question?", false);
						handled = true;
						break;
					case BACK:
						popContent();
						if (content.size() == 0)
							close();
						break;
					default:
					case NONE:
						break;
					}
				}

				break;
			}
		}

		if (!handled)
			handled = super.handleInput(key);

		return handled;
	}
}
