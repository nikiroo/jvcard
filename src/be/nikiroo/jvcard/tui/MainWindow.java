package be.nikiroo.jvcard.tui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.resources.StringUtils;
import be.nikiroo.jvcard.resources.enums.ColorOption;
import be.nikiroo.jvcard.resources.enums.StringId;
import be.nikiroo.jvcard.tui.KeyAction.Mode;
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
	private UserQuestion userQuestion;
	private String titleCache;
	private Panel titlePanel;
	private Panel mainPanel;
	private Panel contentPanel;
	private Panel actionPanel;
	private Panel messagePanel;
	private TextBox text;

	/**
	 * Information about a question to ask the user and its answer.
	 * 
	 * @author niki
	 *
	 */
	private class UserQuestion {
		private boolean oneKeyAnswer;
		private KeyAction action;
		private String answer;

		/**
		 * Create a new {@link UserQuestion}.
		 * 
		 * @param action
		 *            the action that triggered the question
		 * @param oneKeyAnswer
		 *            TRUE if we expect a one-key answer
		 */
		public UserQuestion(KeyAction action, boolean oneKeyAnswer) {
			this.action = action;
			this.oneKeyAnswer = oneKeyAnswer;
		}

		/**
		 * Return the {@link KeyAction} that triggered the question.
		 * 
		 * @return the {@link KeyAction}
		 */
		public KeyAction getAction() {
			return action;
		}

		/**
		 * Check if a one-key answer is expected.
		 * 
		 * @return TRUE if a one-key answer is expected
		 */
		public boolean isOneKeyAnswer() {
			return oneKeyAnswer;
		}

		/**
		 * Return the user answer.
		 * 
		 * @return the user answer
		 */
		public String getAnswer() {
			return answer;
		}

		/**
		 * Set the user answer.
		 * 
		 * @param answer
		 *            the new answer
		 */
		public void setAnswer(String answer) {
			this.answer = answer;
		}
	}

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

		if (prev != null) {
			try {
				String mess = prev.wakeup();
				if (mess != null)
					setMessage(mess, false);
			} catch (IOException e) {
				setMessage(e.getMessage(), true);
			}
		}

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
				ColorOption element = (error ? ColorOption.LINE_MESSAGE_ERR
						: ColorOption.LINE_MESSAGE);
				Label lbl = UiColors.createLabel(element, " " + mess + " ");
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
		userQuestion = new UserQuestion(action, oneKey);

		messagePanel.removeAllComponents();

		Panel hpanel = new Panel();
		LinearLayout llayout = new LinearLayout(Direction.HORIZONTAL);
		llayout.setSpacing(0);
		hpanel.setLayoutManager(llayout);

		Label lbl = UiColors.createLabel(ColorOption.LINE_MESSAGE_QUESTION, " "
				+ question + " ");
		text = new TextBox(new TerminalSize(getSize().getColumns()
				- lbl.getSize().getColumns(), 1));

		if (initial != null) {
			// add all chars one by one so the caret is at the end
			for (int index = 0; index < initial.length(); index++) {
				text.handleInput(new KeyStroke(initial.charAt(index), false,
						false));
			}
		}

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

		if (userQuestion != null) {
			handled = handleQuestion(userQuestion, key);
			if (handled) {
				if (userQuestion.getAnswer() != null) {
					handleAction(userQuestion.getAction(),
							userQuestion.getAnswer());

					userQuestion = null;
				}
			}
		} else {
			handled = handleKey(key);
		}

		if (!handled) {
			handled = super.handleInput(key);
		}

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
			title = StringUtils.sanitize(title, Main.isUnicode());
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
			UiColors.themeLabel(ColorOption.TITLE_MAIN, lblPrefix);

			Label lblTitle = null;
			if (title.length() > 0) {
				lblTitle = new Label(title);
				UiColors.themeLabel(ColorOption.TITLE_VARIABLE, lblTitle);
			}

			Label lblCount = null;
			if (countStr != null) {
				lblCount = new Label(countStr);
				UiColors.themeLabel(ColorOption.TITLE_COUNT, lblCount);
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
			String trans = " " + Main.trans(action.getStringId()) + " ";

			if ("  ".equals(trans))
				continue;

			String keyTrans = KeyAction.trans(action.getKey());

			Panel kPane = new Panel();
			LinearLayout layout = new LinearLayout(Direction.HORIZONTAL);
			layout.setSpacing(0);
			kPane.setLayoutManager(layout);

			kPane.addComponent(UiColors.createLabel(ColorOption.ACTION_KEY,
					keyTrans));
			kPane.addComponent(UiColors.createLabel(ColorOption.ACTION_DESC, trans));

			actionPanel.addComponent(kPane);
		}

		// fill with "desc" colour
		int width = -1;
		if (getSize() != null) {
			width = getSize().getColumns();
		}

		if (width > 0) {
			actionPanel.addComponent(UiColors.createLabel(ColorOption.ACTION_DESC,
					StringUtils.padString("", width)));
		}
	}

	/**
	 * Handle user input when in "ask for question" mode (see
	 * {@link MainWindow#userQuestion}).
	 * 
	 * @param userQuestion
	 *            the question data
	 * @param key
	 *            the key that has been pressed by the user
	 * 
	 * @return TRUE if the {@link KeyStroke} was handled
	 */
	private boolean handleQuestion(UserQuestion userQuestion, KeyStroke key) {
		userQuestion.setAnswer(null);

		if (userQuestion.isOneKeyAnswer()) {
			userQuestion.setAnswer("" + key.getCharacter());
		} else {
			// ^h == Backspace
			if (key.isCtrlDown() && key.getCharacter() == 'h') {
				key = new KeyStroke(KeyType.Backspace);
			}

			switch (key.getKeyType()) {
			case Enter:
				if (text != null)
					userQuestion.setAnswer(text.getText());
				else
					userQuestion.setAnswer("");
				break;
			case Backspace:
				int pos = text.getCaretPosition().getColumn();
				if (pos > 0) {
					String current = text.getText();
					// force caret one space before:
					text.setText(current.substring(0, pos - 1));
					// re-add full text:
					text.setText(current.substring(0, pos - 1)
							+ current.substring(pos));
				}
				return true;
			default:
				// Do nothing (continue entering text)
				break;
			}
		}

		if (userQuestion.getAnswer() != null) {
			Interactable focus = null;
			MainContent content = getContent();
			if (content != null)
				focus = content.nextFocus(null);

			focus.takeFocus();

			return true;
		}

		return false;
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

			action.getObject(); // see {@link KeyAction#getMessage()}
			String mess = action.getMessage();
			if (mess != null) {
				setMessage(mess, action.isError());
			}

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
			} else if (action.getObject() != null
					&& action.getObject() instanceof MainContent) {
				MainContent mergeContent = (MainContent) action.getObject();
				pushContent(mergeContent);
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
			setMessage("Help! I need somebody! Help!", false);

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
