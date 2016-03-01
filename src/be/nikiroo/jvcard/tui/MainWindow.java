package be.nikiroo.jvcard.tui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.Contact;
import be.nikiroo.jvcard.Data;
import be.nikiroo.jvcard.i18n.Trans;
import be.nikiroo.jvcard.i18n.Trans.StringId;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
import be.nikiroo.jvcard.tui.UiColors.Element;
import be.nikiroo.jvcard.tui.panes.ContactDetails;
import be.nikiroo.jvcard.tui.panes.ContactDetailsRaw;
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
	private KeyAction questionAction;
	private String titleCache;
	private Panel titlePanel;
	private Panel mainPanel;
	private Panel contentPanel;
	private Panel actionPanel;
	private Panel messagePanel;
	private TextBox text;

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
	 * @param action
	 *            the related action
	 * @param question
	 *            the question to ask
	 * @param initial
	 *            the initial answer if any (to be edited by the user)
	 */
	public void setQuestion(KeyAction action, String question, String initial) {
		setQuestion(action, question, initial, false);
	}

	/**
	 * Show a question to the user and switch to "ask for answer" mode see
	 * {@link MainWindow#handleQuestion}. The user will be asked to hit one key
	 * as an answer.
	 * 
	 * @param action
	 *            the related action
	 * @param question
	 *            the question to ask
	 */
	public void setQuestion(KeyAction action, String question) {
		setQuestion(action, question, null, true);
	}

	/**
	 * Show a question to the user and switch to "ask for answer" mode see
	 * {@link MainWindow#handleQuestion}.
	 * 
	 * @param action
	 *            the related action
	 * @param question
	 *            the question to ask
	 * @param initial
	 *            the initial answer if any (to be edited by the user)
	 * @param oneKey
	 *            TRUE for a one-key answer, FALSE for a text answer validated
	 *            by ENTER
	 */
	private void setQuestion(KeyAction action, String question, String initial,
			boolean oneKey) {
		questionAction = action;
		waitForOneKeyAnswer = oneKey;

		messagePanel.removeAllComponents();

		Panel hpanel = new Panel();
		LinearLayout llayout = new LinearLayout(Direction.HORIZONTAL);
		llayout.setSpacing(0);
		hpanel.setLayoutManager(llayout);

		Label lbl = UiColors.Element.LINE_MESSAGE_QUESTION.createLabel(" "
				+ question + " ");
		text = new TextBox(new TerminalSize(getSize().getColumns()
				- lbl.getSize().getColumns(), 1));
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
	 * Refresh the window and the empty-space handling. You should call this
	 * method when the window size changed.
	 * 
	 * @param size
	 *            the new size of the window
	 */
	public void refresh(TerminalSize size) {
		if (size == null)
			return;

		if (getSize() == null || !getSize().equals(size))
			setSize(size);

		setTitle();

		if (actions != null)
			setActions(new ArrayList<KeyAction>(actions), false);

		invalidate();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		for (MainContent content : contentStack) {
			content.invalidate();
		}
	}

	@Override
	public boolean handleInput(KeyStroke key) {
		boolean handled = false;

		if (questionAction != null) {
			String answer = handleQuestion(key);
			if (answer != null) {
				handled = true;

				handleAction(questionAction, answer);
				questionAction = null;
			}
		} else {
			handled = handleKey(key);
		}

		if (!handled)
			handled = super.handleInput(key);

		return handled;
	}

	/**
	 * Actually set the title <b>inside</b> the window. Will also call
	 * {@link BasicWindow#setTitle} with the computed parameters.
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
			title = StringUtils.sanitize(title, UiColors.getInstance()
					.isUnicode());
		}

		String countStr = "";
		if (count > -1) {
			countStr = "[" + count + "]";
		}

		int width = -1;
		if (getSize() != null) {
			width = getSize().getColumns();
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

			String keyTrans = Trans.getInstance().trans(action.getKey());

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
		int width = -1;
		if (getSize() != null) {
			width = getSize().getColumns();
		}

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

		// TODO: support ^H (backspace)
		// TODO: start at end of initial question, not start

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
	private boolean handleKey(KeyStroke key) {
		boolean handled = false;

		if (setMessage(null, false))
			return true;

		for (KeyAction action : actions) {
			if (!action.match(key))
				continue;

			handled = true;

			if (action.onAction()) {
				handleAction(action, null);
			}

			break;
		}

		return handled;
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
	private void handleAction(KeyAction action, String answer) {
		MainContent content = getContent();

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
			if (action.getCard() != null) {
				pushContent(new ContactList(action.getCard()));
			}
			break;
		case CONTACT_DETAILS:
			if (action.getContact() != null) {
				pushContent(new ContactDetails(action.getContact()));
			}
			break;
		case CONTACT_DETAILS_RAW:
			if (action.getContact() != null) {
				pushContent(new ContactDetailsRaw(action.getContact()));
			}
			break;
		// mode interpreted by MainWindow:
		case HELP:
			// TODO
			// setMessage("Help! I need somebody! Help!", false);
			if (answer == null) {
				setQuestion(action, "Test question?", "[initial]");
			} else {
				setMessage("You answered: " + answer, false);
			}

			break;
		case BACK:
			String warning = content.getExitWarning();
			if (warning != null) {
				if (answer == null) {
					setQuestion(action, warning);
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
		case ASK_USER:
			if (answer == null) {
				setQuestion(action, action.getQuestion(),
						action.getDefaultAnswer());
			} else {
				setMessage(action.callback(answer), true);
				content.refreshData();
				invalidate();
				setTitle();
			}
			break;
		case ASK_USER_KEY:
			if (answer == null) {
				setQuestion(action, action.getQuestion());
			} else {
				setMessage(action.callback(answer), true);
				content.refreshData();
				invalidate();
				setTitle();
			}
			break;
		default:
		case NONE:
			break;
		}
	}
}
