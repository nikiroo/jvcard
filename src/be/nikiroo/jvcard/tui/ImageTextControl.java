package be.nikiroo.jvcard.tui;

import java.awt.Image;

import be.nikiroo.jvcard.tui.ImageText.Mode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;

/**
 * A {@link Panel} containing an {@link ImageText} rendering.
 * 
 * @author niki
 *
 */
public class ImageTextControl extends Panel {
	private ImageText image;
	private TextBox txt;
	private int mode;

	/**
	 * Create a new {@link ImageTextControl} for the given {@link Image} and
	 * {@link TerminalSize}.
	 * 
	 * @param image
	 *            the {@link Image} to render
	 * @param size
	 *            the target size of this control
	 */
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
		setSize(size);
		setImage(new ImageText(image, size, mode, false));
	}

	/**
	 * Cycle through the available rendering modes if possible.
	 * 
	 * @return TRUE if it was possible to switch modes
	 */
	public boolean switchMode() {
		if (image == null || !UiColors.getInstance().isUnicode())
			return false;

		Mode[] modes = Mode.values();
		mode++;
		if (mode >= modes.length)
			mode = 0;

		image.setMode(modes[mode]);
		setImage(image);

		return true;
	}

	/**
	 * Invert the colours.
	 */
	public void invertColor() {
		if (image != null) {
			image.setColorInvert(!image.isColorInvert());
			setImage(image);
		}
	}

	@Override
	public synchronized Panel setSize(TerminalSize size) {
		if (image != null)
			image.setSize(size);

		super.setSize(size);

		setImage(image);

		return this;
	};

	/**
	 * Set/reset the {@link ImageText} to render.
	 * 
	 * @param image
	 *            the new {@link ImageText}
	 */
	private void setImage(ImageText image) {
		this.image = image;
		removeAllComponents();
		txt = null;
		if (image != null) {
			txt = new TextBox(image.getText());
			this.addComponent(txt, BorderLayout.Location.CENTER);
		}
	}
}
