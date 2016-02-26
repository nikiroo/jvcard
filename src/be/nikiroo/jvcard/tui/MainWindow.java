package be.nikiroo.jvcard.tui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.i18n.Trans.StringId;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;
import be.nikiroo.jvcard.tui.panes.ContactDetails;
import be.nikiroo.jvcard.tui.panes.ContactList;
import be.nikiroo.jvcard.tui.panes.MainContent;

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
 * This is the main "window" of the program. It can show up to one
 * {@link MainContent} at any one time, but keeps a stack of contents.
 * 
 * @author niki
 * 
 */
public class MainWindow extends BasicWindow {
	private List<KeyAction> defaultActions = new LinkedList<KeyAction>();
	private List<KeyAction> actions = new LinkedList<KeyAction>();
	private List<MainContent> contentStack = new LinkedList<MainContent>();
	private boolean waitForOneKeyAnswer;
	private KeyStroke questionKey; // key that "asked" a question, and to replay
	// later with an answer
	private String titleCache;
	private Panel titlePanel;
	private Panel mainPanel;
	private Panel contentPanel;
	private Panel actionPanel;
	private Panel messagePanel;
	private TextBox text;
	private int width;

	/**
	 * Create a new, empty window.
	 */
	public MainWindow() {
		this(null);
	}

	/**
	 * Create a new window hosting the given content.
	 * 
	 * @param content
	 *            the content to host
	 */
	public MainWindow(MainContent content) {
		super(content == null ? "" : content.getTitle());

		width = -1;

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

		BorderLayout blayout = new BorderLayout();
		titlePanel.setLayoutManager(blayout);

		llayout = new LinearLayout(Direction.VERTICAL);
		llayout.setSpacing(0);
		messagePanel.setLayoutManager(llayout);

		blayout = new BorderLayout();
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

	/**
	 * "push" some content to the window stack.
	 * 
	 * @param content
	 *            the new top-of-the-stack content
	 */
	public void pushContent(MainContent content) {
		List<KeyAction> actions = null;

		contentPanel.removeAllComponents();
		if (content != null) {
			actions = content.getKeyBindings();
			contentPanel.addComponent(content, BorderLayout.Location.CENTER);
			this.contentStack.add(content);

			Interactable focus = content.nextFocus(null);
			if (focus != null)
				focus.takeFocus();
		}

		setTitle();
		setActions(actions, true);
	}

	/**
	 * "pop" the latest, top-of-the-stack content from the window stack.
	 * 
	 * @return the removed content if any
	 */
	public MainContent popContent() {
		MainContent removed = null;
		MainContent prev = null;

		MainContent content = getContent();
		if (content != null)
			removed = contentStack.remove(contentStack.size() - 1);

		if (contentStack.size() > 0)
			prev = contentStack.remove(contentStack.size() - 1);

		pushContent(prev);

		return removed;
	}

	/**
	 * Show the given message on screen. It will disappear at the next action.
	 * 
	 * @param mess
	 *            the message to display
	 * @param error
	 *            TRUE for an error message, FALSE for an information message
	 * 
	 * @return TRUE if changes were performed
	 */
	public boolean setMessage(String mess, boolean error) {
		if (mess != null || messagePanel.getChildCount() > 0) {
			messagePanel.removeAllComponents();
			if (mess != null) {
				Element element = (error ? UiColors.Element.LINE_MESSAGE_ERR
						: UiColors.Element.LINE_MESSAGE);
				Label lbl = element.createLabel(" " + mess + " ");
				messagePanel.addComponent(lbl, LinearLayout
						.createLayoutData(LinearLayout.Alignment.Center));
			}
			return true;
		}

		return false;
	}

	/**
	 * Show a question to the user and switch to "ask for answer" mode see
	 * {@link MainWindow#handleQuestion}. The user will be asked to enter some
	 * answer and confirm with ENTER.
	 * 
	 * @param question
	 *            the question to ask
	 * @param initial
	 *            the initial answer if any (to be edited by the user)
	 */
	public void setQuestion(KeyStroke key, String question, String initial) {
		setQuestion(key, question, initial, false);
	}

	/**
	 * Show a question to the user and switch to "ask for answer" mode see
	 * {@link MainWindow#handleQuestion}. The user will be asked to hit one key
	 * as an answer.
	 * 
	 * @param question
	 *            the question to ask
	 */
	public void setQuestion(KeyStroke key, String question) {
		setQuestion(key, question, null, true);
	}

	@Override
	public void draw(TextGUIGraphics graphics) {
		int width = graphics.getSize().getColumns();

		if (width != this.width) {
			this.width = width;

			setTitle();

			if (actions != null)
				setActions(new ArrayList<KeyAction>(actions), false);
		}

		super.draw(graphics);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (MainContent content : contentStack) {
			content.invalidate();
		}
	}

	/**
	 * Show a question to the user and switch to "ask for answer" mode see
	 * {@link MainWindow#handleQuestion}.
	 * 
	 * @param question
	 *            the question to ask
	 * @param initial
	 *            the initial answer if any (to be edited by the user)
	 * @param oneKey
	 *            TRUE for a one-key answer, FALSE for a text answer validated
	 *            by ENTER
	 */
	private void setQuestion(KeyStroke key, String question, String initial,
			boolean oneKey) {
		questionKey = key;
		waitForOneKeyAnswer = oneKey;

		messagePanel.removeAllComponents();

		Panel hpanel = new Panel();
		LinearLayout llayout = new LinearLayout(Direction.HORIZONTAL);
		llayout.setSpacing(0);
		hpanel.setLayoutManager(llayout);

		Label lbl = UiColors.Element.LINE_MESSAGE_QUESTION.createLabel(" "
				+ question + " ");
		text = new TextBox(new TerminalSize(width - lbl.getSize().getColumns(),
				1));
		if (initial != null)
			text.setText(initial);

		hpanel.addComponent(lbl,
				LinearLayout.createLayoutData(LinearLayout.Alignment.Beginning));
		hpanel.addComponent(text,
				LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));

		messagePanel
				.addComponent(hpanel, LinearLayout
						.createLayoutData(LinearLayout.Alignment.Beginning));

		text.takeFocus();
	}

	/**
	 * Actually set the title <b>inside</b> the window. Will also call
	 * {@link BasicWindow#setTitle} with the compuited parameters.
	 */
	private void setTitle() {
		String prefix = " " + Main.APPLICATION_TITLE + " (version "
				+ Main.APPLICATION_VERSION + ")";

		String title = null;
		int count = -1;

		MainContent content = getContent();
		if (content != null) {
			title = content.getTitle();
			count = content.getCount();
		}

		if (title == null)
			title = "";

		if (title.length() > 0) {
			prefix = prefix + ": ";
		}

		String countStr = "";
		if (count > -1) {
			countStr = "[" + count + "]";
		}

		if (width > 0) {
			int padding = width - prefix.length() - title.length()
					- countStr.length();
			if (padding > 0) {
				if (title.length() > 0)
					title = StringUtils.padString(title, title.length()
							+ padding);
				else
					prefix = StringUtils.padString(prefix, prefix.length()
							+ padding);
			}
		}

		String titleCache = prefix + title + count;
		if (!titleCache.equals(this.titleCache)) {
			super.setTitle(prefix);

			Label lblPrefix = new Label(prefix);
			UiColors.Element.TITLE_MAIN.themeLabel(lblPrefix);

			Label lblTitle = null;
			if (title.length() > 0) {
				lblTitle = new Label(title);
				UiColors.Element.TITLE_VARIABLE.themeLabel(lblTitle);
			}

			Label lblCount = null;
			if (countStr != null) {
				lblCount = new Label(countStr);
				UiColors.Element.TITLE_COUNT.themeLabel(lblCount);
			}

			titlePanel.removeAllComponents();

			titlePanel.addComponent(lblPrefix, BorderLayout.Location.LEFT);
			if (lblTitle != null)
				titlePanel.addComponent(lblTitle, BorderLayout.Location.CENTER);
			if (lblCount != null)
				titlePanel.addComponent(lblCount, BorderLayout.Location.RIGHT);
		}
	}

	/**
	 * Return the current {@link MainContent} from the stack if any.
	 * 
	 * @return the current {@link MainContent}
	 */
	private MainContent getContent() {
		if (contentStack.size() > 0) {
			return contentStack.get(contentStack.size() - 1);
		}

		return null;
	}

	/**
	 * Update the list of actions and refresh the action panel.
	 * 
	 * @param actions
	 *            the list of actions to support
	 * @param enableDefaultactions
	 *            TRUE to enable the default actions
	 */
	private void setActions(List<KeyAction> actions,
			boolean enableDefaultactions) {
		this.actions.clear();

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

		// fill with "desc" colour
		if (width > 0) {
			actionPanel.addComponent(UiColors.Element.ACTION_DESC
					.createLabel(StringUtils.padString("", width)));

		}
	}

	/**
	 * Handle user input when in "ask for question" mode (see
	 * {@link MainWindow#questionKey}).
	 * 
	 * @param key
	 *            the key that has been pressed by the user
	 * 
	 * @return the user's answer if done
	 */
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
			MainContent content = getContent();
			if (content != null)
				focus = content.nextFocus(null);

			focus.takeFocus();
		}

		return answer;
	}

	/**
	 * Handle the input in case of "normal" (not "ask for answer") mode.
	 * 
	 * @param key
	 *            the key that was pressed
	 * @param answer
	 *            the answer given for this key
	 * 
	 * @return if the window handled the input
	 */
	private boolean handleInput(KeyStroke key, String answer) {
		boolean handled = false;

		// reset the message pane if no answers are pending
		if (answer == null) {
			if (setMessage(null, false))
				return true;
		}

		for (KeyAction action : actions) {
			if (!action.match(key))
				continue;

			MainContent content = getContent();
			handled = true;

			if (action.onAction()) {
				Card card = action.getCard();
				Contact contact = action.getContact();
				Data data = action.getData();

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

					if (content != null) {
						String err = content.move(x, y);
						if (err != null)
							setMessage(err, true);
					}

					break;
				// mode with windows:
				case CONTACT_LIST:
					if (card != null) {
						pushContent(new ContactList(card));
					}
					break;
				case CONTACT_DETAILS:
					if (contact != null) {
						pushContent(new ContactDetails(contact));
					}
					break;
				// mode interpreted by MainWindow:
				case HELP:
					// TODO
					// setMessage("Help! I need somebody! Help!", false);
					if (answer == null) {
						setQuestion(key, "Test question?", "[initial]");
					} else {
						setMessage("You answered: " + answer, false);
					}

					break;
				case BACK:
					String warning = content.getExitWarning();
					if (warning != null) {
						if (answer == null) {
							setQuestion(key, warning);
						} else {
							setMessage(null, false);
							if (answer.equalsIgnoreCase("y")) {
								popContent();
							}
						}
					} else {
						popContent();
					}

					if (contentStack.size() == 0) {
						close();
					}

					break;
				// action modes:
				case EDIT_DETAIL:
					if (answer == null) {
						if (data != null) {
							String name = data.getName();
							String value = data.getValue();
							setQuestion(key, name, value);
						}
					} else {
						setMessage(null, false);
						data.setValue(answer);
					}
					break;
				case DELETE_CONTACT:
					if (answer == null) {
						if (contact != null) {
							setQuestion(key, "Delete contact? [Y/N]");
						}
					} else {
						setMessage(null, false);
						if (answer.equalsIgnoreCase("y")) {
							if (contact.delete()) {
								content.refreshData();
								invalidate();
								setTitle();
							} else {
								setMessage("Cannot delete this contact", true);
							}
						}
					}
					break;
				case SAVE_CARD:
					if (answer == null) {
						if (card != null) {
							setQuestion(key, "Save changes? [Y/N]");
						}
					} else {
						setMessage(null, false);
						if (answer.equalsIgnoreCase("y")) {
							boolean ok = false;
							try {
								if (card.save()) {
									ok = true;
									invalidate();
								}
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}

							if (!ok) {
								setMessage("Cannot save to file", true);
							}
						}
					}
					break;
				default:
				case NONE:
					break;
				}
			}

			break;
		}

		return handled;
	}

	@Override
	public boolean handleInput(KeyStroke key) {
		boolean handled = false;

		if (questionKey != null) {
			String answer = handleQuestion(key);
			if (answer != null) {
				handled = true;

				key = questionKey;
				questionKey = null;

				handleInput(key, answer);
			}
		} else {
			handled = handleInput(key, null);
		}

		if (!handled)
			handled = super.handleInput(key);

		return handled;
	}
}
