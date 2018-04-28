package be.nikiroo.jvcard.tui.windows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jexer.TAction;
import jexer.TApplication;
import jexer.TKeypress;
import jexer.TWindow;
import be.nikiroo.jvcard.Card;
import be.nikiroo.jvcard.launcher.CardResult;
import be.nikiroo.jvcard.launcher.CardResult.MergeCallback;
import be.nikiroo.jvcard.launcher.Main;
import be.nikiroo.jvcard.parsers.Format;

public class TuiFileListWindow extends TuiBrowserWindow {
	private List<String> files;
	private List<CardResult> cards;

	public TuiFileListWindow(TApplication app, int width, int height,
			List<String> files) {
		super(app, width, height, "Contacts", false);

		this.files = files;

		cards = new ArrayList<CardResult>();
		for (int i = 0; i < files.size(); i++) {
			cards.add(null);
		}

		List<String> headers = new ArrayList<String>();
		headers.add("File");
		List<List<String>> dataLines = new ArrayList<List<String>>();
		for (String file : files) {
			List<String> listOfOneFile = new ArrayList<String>(1);
			listOfOneFile.add(new File(file).getName());
			dataLines.add(listOfOneFile);
		}

		addKeyBinding(TKeypress.kbQ, "Quit", new TAction() {
			@Override
			public void DO() {
				close();
			}
		});

		setData(headers, dataLines);
	}

	@Override
	public void onAction(int selectedLine, int selectedColumn) {
		try {
			@SuppressWarnings("unused")
			TWindow w = new TuiContactListWindow(TuiFileListWindow.this,
					getCard(selectedLine));
		} catch (IOException e) {
			setMessage("Fail to get file: " + e.getMessage(), true);
		}
	}

	private Card getCard(int index) throws IOException {
		// TODO: check index?
		if (cards.get(index) == null) {
			String file = files.get(index);
			CardResult cardResult = retrieveCardResult(file);
			cards.set(index, cardResult);
		}

		return cards.get(index).getCard();
	}

	private CardResult retrieveCardResult(String file) throws IOException {
		CardResult cardResult = null;
		final Card arr[] = new Card[4];
		try {
			cardResult = Main.getCard(file, new MergeCallback() {
				@Override
				public Card merge(Card previous, Card local, Card server,
						Card autoMerged) {
					arr[0] = previous;
					arr[1] = local;
					arr[2] = server;
					arr[3] = autoMerged;

					return null;
				}
			});

			cardResult.getCard(); // throw IOE if sync issues
		} catch (IOException e) {
			// Check if merge issue or something else I/O related
			if (arr[0] == null)
				throw e; // other I/O problems

			// merge management: set all merge vars in
			// merger,
			// make sure it has cards but mergeTargetFile
			// does not exist
			// (create then delete if needed)
			// TODO: i18n
			setMessage("Merge error, please check/fix the merged contact", true);

			// TODO: i18n + filename with numbers in it to
			// fix
			File a = File.createTempFile("Merge result ", ".vcf");
			File p = File.createTempFile("Previous common version ", ".vcf");
			File l = File.createTempFile("Local ", ".vcf");
			File s = File.createTempFile("Remote ", ".vcf");
			arr[3].saveAs(a, Format.VCard21);
			arr[0].saveAs(p, Format.VCard21);
			arr[1].saveAs(l, Format.VCard21);
			arr[2].saveAs(s, Format.VCard21);
			List<String> mfiles = new LinkedList<String>();
			mfiles.add(a.getAbsolutePath());
			mfiles.add(p.getAbsolutePath());
			mfiles.add(l.getAbsolutePath());
			mfiles.add(s.getAbsolutePath());
			/*
			 * merger = new FileList(mfiles); merger.mergeRemoteState =
			 * arr[2].getContentState(false); merger.mergeSourceFile =
			 * files.get(index); merger.mergeTargetFile = a;
			 * 
			 * obj = merger;
			 */
		}

		// TODO:
		// invalidate();

		if (cardResult.isSynchronised()) {
			// TODO i18n
			if (cardResult.isChanged())
				setMessage("card synchronised: changes from server", false);
			else
				setMessage("card synchronised: no changes", false);
		}

		return cardResult;
	}

	// TODO
	private void setMessage(String message, boolean error) {
		if (error) {
			System.err.println(message);
		} else {
			System.out.println(message);
		}
	}
}
