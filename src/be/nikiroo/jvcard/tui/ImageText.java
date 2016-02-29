package be.nikiroo.jvcard.tui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import com.googlecode.lanterna.TerminalSize;

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

	public ImageText(Image image, TerminalSize size, Mode mode) {
		setImage(image, size);
		setMode(mode);
	}

	public void setImage(Image image) {
		setImage(image, size);
	}

	public void setImage(TerminalSize size) {
		setImage(image, size);
	}

	public void setImage(Image image, TerminalSize size) {
		this.text = null;
		this.ready = false;
		this.size = size;
		if (image != null) {
			this.image = image;
		}
	}

	public void setMode(Mode mode) {
		this.mode = mode;
		this.text = null;
		this.ready = false;
	}

	public void setColorInvert(boolean invert) {
		this.invert = invert;
		this.text = null;
		this.ready = false;
	}

	public boolean getColorInvert() {
		return invert;
	}

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

	private float[] tmp = new float[4];

	private char getAsciiChar(int pixel) {
		float brigthness = getBrightness(pixel, tmp);
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

	private char getDitheringChar(int pixel) {
		float brigthness = getBrightness(pixel, tmp);
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

	private char getBlockChar(int upperleft, int upperright, int lowerleft,
			int lowerright, boolean dithering) {
		float trigger = dithering ? 0.20f : 0.50f;
		int choice = 0;
		if (getBrightness(upperleft, tmp) > trigger)
			choice += 1;
		if (getBrightness(upperright, tmp) > trigger)
			choice += 2;
		if (getBrightness(lowerleft, tmp) > trigger)
			choice += 4;
		if (getBrightness(lowerright, tmp) > trigger)
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
				avg += getBrightness(upperleft, tmp);
				avg += getBrightness(upperright, tmp);
				avg += getBrightness(lowerleft, tmp);
				avg += getBrightness(lowerright, tmp);
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

	float getBrightness(int argb, float[] array) {
		if (invert)
			return 1 - rgb2hsb(argb, tmp)[2];
		return rgb2hsb(argb, tmp)[2];
	}

	// return [h, s, l, a]; h/s/b/a: 0 to 1 (h is given in 1/360th)
	// like RGBtoHSB, array can be null or used
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
