package be.nikiroo.jvcard.tui;

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

	public ImageText(Image image, TerminalSize size) {
		setImage(image, size);
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

	public String getText() {
		if (text == null) {
			if (image == null)
				return "";

			int w = size.getColumns() * 2;
			int h = size.getRows() * 2;
			BufferedImage buff = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_ARGB);
			Graphics gfx = buff.getGraphics();

			TerminalSize srcSize = getSize(image);
			int x = 0;
			int y = 0;
			if (srcSize.getColumns() > srcSize.getRows()) {
				double ratio = (double) srcSize.getRows()
						/ (double) srcSize.getColumns();
				h = (int) Math.round(ratio * h);
				y = (buff.getHeight() - h) / 2;
			} else {
				double ratio = (double) srcSize.getColumns()
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

			int[][] square = new int[2][2];
			StringBuilder builder = new StringBuilder();
			for (int row = 0; row < buff.getHeight(); row += 2) {
				if (row > 0)
					builder.append('\n');

				for (int col = 0; col < buff.getWidth(); col += 2) {
					square[0][0] = buff.getRGB(col, row);
					square[0][1] = buff.getRGB(col, row + 1);
					square[1][0] = buff.getRGB(col + 1, row);
					square[1][1] = buff.getRGB(col + 1, row + 1);
					builder.append(getChar(square));
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

	static private char getChar(int[][] square) {
		int choice = 0;
		if (rgb2hsl(square[0][0])[3] > 50)
			choice += 1;
		if (rgb2hsl(square[0][1])[3] > 50)
			choice += 2;
		if (rgb2hsl(square[1][0])[3] > 50)
			choice += 4;
		if (rgb2hsl(square[1][1])[3] > 50)
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
			return '█';
		}

		return ' ';
	}

	// return [a, h, s, l]; a/s/l: 0 to 100%, h = 0 to 359°
	static int[] rgb2hsl(int argb) {
		double a, r, g, b;
		a = ((argb & 0xff000000) >> 24) / 255.0;
		r = ((argb & 0x00ff0000) >> 16) / 255.0;
		g = ((argb & 0x0000ff00) >> 8) / 255.0;
		b = ((argb & 0x000000ff)) / 255.0;

		double rgbMin, rgbMax;
		rgbMin = Math.min(r, Math.min(g, b));
		rgbMax = Math.max(r, Math.max(g, b));

		double l;
		l = (rgbMin + rgbMax) / 2;

		double s;
		if (rgbMin == rgbMax) {
			s = 0;
		} else {
			if (l <= 0.5) {
				s = (rgbMax - rgbMin) / (rgbMax + rgbMin);
			} else {
				s = (rgbMax - rgbMin) / (2.0 - rgbMax - rgbMin);
			}
		}

		double h;
		if (r > g && r > b) {
			h = (g - b) / (rgbMax - rgbMin);
		} else if (g > b) {
			h = 2.0 + (b - r) / (rgbMax - rgbMin);
		} else {
			h = 4.0 + (r - g) / (rgbMax - rgbMin);
		}

		int aa = (int) Math.round(100 * a);
		int hh = (int) (60 * h);
		if (hh < 0)
			hh += 360;
		int ss = (int) Math.round(100 * s);
		int ll = (int) Math.round(100 * l);

		return new int[] { aa, hh, ss, ll };
	}
}
