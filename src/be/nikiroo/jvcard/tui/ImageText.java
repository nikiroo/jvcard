package be.nikiroo.jvcard.tui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import com.googlecode.lanterna.TerminalSize;

/**
 * This class converts an {@link Image} into a textual representation that can
 * be displayed to the user in a TUI.
 * 
 * @author niki
 *
 */
public class ImageText {
	private Image image;
	private TerminalSize size;
	private String text;
	private boolean ready;
	private Mode mode;
	private boolean invert;

	public enum Mode {
		/**
		 * Use 5 different "colours" which are actually Unicode
		 * {@link Character}s representing
		 * <ul>
		 * <li>space (blank)</li>
		 * <li>low shade (░)</li>
		 * <li>medium shade (▒)</li>
		 * <li>high shade (▓)</li>
		 * <li>full block (█)</li>
		 * </ul>
		 */
		DITHERING,
		/**
		 * Use "block" Unicode {@link Character}s up to quarter blocks, thus in
		 * effect doubling the resolution both in vertical and horizontal space.
		 * Note that since 2 {@link Character}s next to each other are square,
		 * we will use 4 blocks per 2 blocks for w/h resolution.
		 */
		DOUBLE_RESOLUTION,
		/**
		 * Use {@link Character}s from both {@link Mode#DOUBLE_RESOLUTION} and
		 * {@link Mode#DITHERING}.
		 */
		DOUBLE_DITHERING,
		/**
		 * Only use ASCII {@link Character}s.
		 */
		ASCII,
	}

	/**
	 * Create a new {@link ImageText} with the given parameters.
	 * 
	 * @param image
	 *            the source {@link Image}
	 * @param size
	 *            the final text size to target
	 * @param mode
	 *            the mode of conversion
	 */
	public ImageText(Image image, TerminalSize size, Mode mode) {
		setImage(image, size);
		setMode(mode);
	}

	/**
	 * Change the source {@link Image}.
	 * 
	 * @param image
	 *            the new {@link Image}
	 */
	public void setImage(Image image) {
		setImage(image, size);
	}

	/**
	 * Change the source {@link Image}.
	 * 
	 * @param size
	 *            the size to use
	 */
	public void setImage(TerminalSize size) {
		setImage(image, size);
	}

	/**
	 * Change the source {@link Image}.
	 * 
	 * @param image
	 *            the new {@link Image}
	 * @param size
	 *            the size to use
	 */
	public void setImage(Image image, TerminalSize size) {
		this.text = null;
		this.ready = false;
		this.size = size;
		if (image != null) {
			this.image = image;
		}
	}

	/**
	 * Change the image-to-text mode.
	 * 
	 * @param mode
	 *            the new {@link Mode}
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
		this.text = null;
		this.ready = false;
	}

	/**
	 * Set the colour-invert mode.
	 * 
	 * @param invert
	 *            TRUE to inverse the colours
	 */
	public void setColorInvert(boolean invert) {
		this.invert = invert;
		this.text = null;
		this.ready = false;
	}

	/**
	 * Check if the colours are inverted.
	 * 
	 * @return TRUE if the colours are inverted
	 */
	public boolean isColorInvert() {
		return invert;
	}

	/**
	 * Return the textual representation of the included {@link Image}.
	 * 
	 * @return the {@link String} representation
	 */
	public String getText() {
		if (text == null) {
			if (image == null)
				return "";

			int mult = 1;
			if (mode == Mode.DOUBLE_RESOLUTION || mode == Mode.DOUBLE_DITHERING)
				mult = 2;

			int w = size.getColumns() * mult;
			int h = size.getRows() * mult;

			BufferedImage buff = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_ARGB);

			Graphics gfx = buff.getGraphics();

			TerminalSize srcSize = getSize(image);
			srcSize = new TerminalSize(srcSize.getColumns() * 2,
					srcSize.getRows());
			int x = 0;
			int y = 0;

			if (srcSize.getColumns() > srcSize.getRows()) {
				double ratio = (double) size.getColumns()
						/ (double) size.getRows();
				ratio *= (double) srcSize.getRows()
						/ (double) srcSize.getColumns();

				h = (int) Math.round(ratio * h);
				y = (buff.getHeight() - h) / 2;
			} else {
				double ratio = (double) size.getRows()
						/ (double) size.getColumns();
				ratio *= (double) srcSize.getColumns()
						/ (double) srcSize.getRows();

				w = (int) Math.round(ratio * w);
				x = (buff.getWidth() - w) / 2;
			}

			if (gfx.drawImage(image, x, y, w, h, new ImageObserver() {
				@Override
				public boolean imageUpdate(Image img, int infoflags, int x,
						int y, int width, int height) {
					ImageText.this.ready = true;
					return true;
				}
			})) {
				ready = true;
			}

			while (!ready) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}

			gfx.dispose();

			StringBuilder builder = new StringBuilder();

			for (int row = 0; row < buff.getHeight(); row += mult) {
				if (row > 0)
					builder.append('\n');

				for (int col = 0; col < buff.getWidth(); col += mult) {
					if (mult == 1) {
						if (mode == Mode.DITHERING)
							builder.append(getDitheringChar(buff.getRGB(col,
									row)));
						else
							// Mode.ASCII
							builder.append(getAsciiChar(buff.getRGB(col, row)));
					} else if (mult == 2) {
						builder.append(getBlockChar( //
								buff.getRGB(col, row),//
								buff.getRGB(col + 1, row),//
								buff.getRGB(col, row + 1),//
								buff.getRGB(col + 1, row + 1),//
								mode == Mode.DOUBLE_DITHERING//
						));
					}
				}
			}

			text = builder.toString();
		}

		return text;
	}

	@Override
	public String toString() {
		return getText();
	}

	/**
	 * Return the size of the given {@link Image}.
	 * 
	 * @param img
	 *            the image to measure
	 * 
	 * @return the size
	 */
	static private TerminalSize getSize(Image img) {
		TerminalSize size = null;
		while (size == null) {
			int w = img.getWidth(null);
			int h = img.getHeight(null);
			if (w > -1 && h > -1) {
				size = new TerminalSize(w, h);
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}

		return size;
	}

	/**
	 * Return the {@link Character} corresponding to this colour in
	 * {@link Mode#ASCII} mode.
	 * 
	 * @param pixel
	 *            the colour
	 * 
	 * @return the {@link Character} to use
	 */
	private char getAsciiChar(int pixel) {
		float brigthness = getBrightness(pixel);
		if (brigthness < 0.20) {
			return ' ';
		} else if (brigthness < 0.40) {
			return '.';
		} else if (brigthness < 0.60) {
			return '+';
		} else if (brigthness < 0.80) {
			return '*';
		} else {
			return '#';
		}
	}

	/**
	 * Return the {@link Character} corresponding to this colour in
	 * {@link Mode#DITHERING} mode.
	 * 
	 * @param pixel
	 *            the colour
	 * 
	 * @return the {@link Character} to use
	 */
	private char getDitheringChar(int pixel) {
		float brigthness = getBrightness(pixel);
		if (brigthness < 0.20) {
			return ' ';
		} else if (brigthness < 0.40) {
			return '░';
		} else if (brigthness < 0.60) {
			return '▒';
		} else if (brigthness < 0.80) {
			return '▓';
		} else {
			return '█';
		}
	}

	/**
	 * Return the {@link Character} corresponding to the 4 given colours in
	 * {@link Mode#DOUBLE_RESOLUTION} or {@link Mode#DOUBLE_DITHERING} mode.
	 * 
	 * @param upperleft
	 *            the upper left colour
	 * @param upperright
	 *            the upper right colour
	 * @param lowerleft
	 *            the lower left colour
	 * @param lowerright
	 *            the lower right colour
	 * @param dithering
	 *            TRUE to use {@link Mode#DOUBLE_DITHERING}, FALSE for
	 *            {@link Mode#DOUBLE_RESOLUTION}
	 * 
	 * @return the {@link Character} to use
	 */
	private char getBlockChar(int upperleft, int upperright, int lowerleft,
			int lowerright, boolean dithering) {
		int choice = 0;
		if (getBrightness(upperleft) > 0.5f)
			choice += 1;
		if (getBrightness(upperright) > 0.5f)
			choice += 2;
		if (getBrightness(lowerleft) > 0.5f)
			choice += 4;
		if (getBrightness(lowerright) > 0.5f)
			choice += 8;

		switch (choice) {
		case 0:
			return ' ';
		case 1:
			return '▘';
		case 2:
			return '▝';
		case 3:
			return '▀';
		case 4:
			return '▖';
		case 5:
			return '▌';
		case 6:
			return '▞';
		case 7:
			return '▛';
		case 8:
			return '▗';
		case 9:
			return '▚';
		case 10:
			return '▐';
		case 11:
			return '▜';
		case 12:
			return '▄';
		case 13:
			return '▙';
		case 14:
			return '▟';
		case 15:
			if (dithering) {
				float avg = 0;
				avg += getBrightness(upperleft);
				avg += getBrightness(upperright);
				avg += getBrightness(lowerleft);
				avg += getBrightness(lowerright);
				avg /= 4;

				if (avg < 0.20) {
					return ' ';
				} else if (avg < 0.40) {
					return '░';
				} else if (avg < 0.60) {
					return '▒';
				} else if (avg < 0.80) {
					return '▓';
				} else {
					return '█';
				}
			} else {
				return '█';
			}
		}

		return ' ';
	}

	/**
	 * Temporary array used so not to create a lot of new ones.
	 */
	private float[] tmp = new float[4];

	/**
	 * Return the brightness value to use from the given ARGB colour.
	 * 
	 * @param argb
	 *            the argb colour
	 * 
	 * @return the brightness to sue for computations
	 */
	private float getBrightness(int argb) {
		if (invert)
			return 1 - rgb2hsb(argb, tmp)[2];
		return rgb2hsb(argb, tmp)[2];
	}

	/**
	 * Convert the given ARGB colour in HSL/HSB, either into the supplied array
	 * or into a new one if array is NULL.
	 * 
	 * <p>
	 * ARGB pixels are given in 0xAARRGGBB format, while the returned array will
	 * contain Hue, Saturation, Lightness/Brightness, Alpha, in this order. H,
	 * S, L and A are all ranging from 0 to 1 (indeed, H is in 1/360th).
	 * </p>
	 * pixel
	 * 
	 * @param argb
	 *            the ARGB colour pixel to convert
	 * @param array
	 *            the array to convert into or NULL to create a new one
	 * 
	 * @return the array containing the HSL/HSB converted colour
	 */
	static float[] rgb2hsb(int argb, float[] array) {
		int a, r, g, b;
		a = ((argb & 0xff000000) >> 24);
		r = ((argb & 0x00ff0000) >> 16);
		g = ((argb & 0x0000ff00) >> 8);
		b = ((argb & 0x000000ff));

		if (array == null)
			array = new float[4];
		Color.RGBtoHSB(r, g, b, array);

		array[3] = a;

		return array;

		// // other implementation:
		//
		// float a, r, g, b;
		// a = ((argb & 0xff000000) >> 24) / 255.0f;
		// r = ((argb & 0x00ff0000) >> 16) / 255.0f;
		// g = ((argb & 0x0000ff00) >> 8) / 255.0f;
		// b = ((argb & 0x000000ff)) / 255.0f;
		//
		// float rgbMin, rgbMax;
		// rgbMin = Math.min(r, Math.min(g, b));
		// rgbMax = Math.max(r, Math.max(g, b));
		//
		// float l;
		// l = (rgbMin + rgbMax) / 2;
		//
		// float s;
		// if (rgbMin == rgbMax) {
		// s = 0;
		// } else {
		// if (l <= 0.5) {
		// s = (rgbMax - rgbMin) / (rgbMax + rgbMin);
		// } else {
		// s = (rgbMax - rgbMin) / (2.0f - rgbMax - rgbMin);
		// }
		// }
		//
		// float h;
		// if (r > g && r > b) {
		// h = (g - b) / (rgbMax - rgbMin);
		// } else if (g > b) {
		// h = 2.0f + (b - r) / (rgbMax - rgbMin);
		// } else {
		// h = 4.0f + (r - g) / (rgbMax - rgbMin);
		// }
		// h /= 6; // from 0 to 1
		//
		// return new float[] { h, s, l, a };
		//
		// // // natural mode:
		// //
		// // int aa = (int) Math.round(100 * a);
		// // int hh = (int) (360 * h);
		// // if (hh < 0)
		// // hh += 360;
		// // int ss = (int) Math.round(100 * s);
		// // int ll = (int) Math.round(100 * l);
		// //
		// // return new int[] { hh, ss, ll, aa };
	}
}
