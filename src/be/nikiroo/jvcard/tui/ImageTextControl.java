package be.nikiroo.jvcard.tui;

import java.awt.Image;

import be.nikiroo.jvcard.tui.ImageText.Mode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

public class ImageTextControl extends Panel {
	private ImageText img;
	private TextBox txt;
	private int mode;

	public ImageTextControl(Image image, TerminalSize size) {
		Mode mode = Mode.DOUBLE_DITHERING;
		if (!UiColors.getInstance().isUnicode()) {
			mode = Mode.ASCII;
		}

		Mode[] modes = Mode.values();
		for (int i = 0; i < modes.length; i++) {
			if (mode == modes[i])
				this.mode = i;
		}

		this.setLayoutManager(new BorderLayout());
		setImg(new ImageText(image, size, mode));
	}

	public boolean switchMode() {
		if (img == null || !UiColors.getInstance().isUnicode())
			return false;

		Mode[] modes = Mode.values();
		mode++;
		if (mode >= modes.length)
			mode = 0;

		img.setMode(modes[mode]);
		setImg(img);

		return true;
	}

	public void invertColor() {
		if (img != null) {
			img.setColorInvert(!img.isColorInvert());
			setImg(img);
		}
	}

	private void setImg(ImageText img) {
		this.img = img;
		removeAllComponents();
		txt = null;
		if (img != null) {
			txt = new TextBox(img.getText());
			this.addComponent(txt, BorderLayout.Location.CENTER);
		}
	}
}
