/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Michael Scharf (Wind River) - [205260] Terminal does not take the font from the preferences
 * Michael Scharf (Wind River) - [209746] There are cases where some colors not displayed correctly
 * Michael Scharf (Wind River) - [206328] Terminal does not draw correctly with proportional fonts
 * Martin Oberhuber (Wind River) - [247700] Terminal uses ugly fonts in JEE package
 * Martin Oberhuber (Wind River) - [335358] Fix Terminal color definition
 * Martin Oberhuber (Wind River) - [265352][api] Allow setting fonts programmatically
 * Martin Oberhuber (Wind River) - [475422] Fix display on MacOSX Retina
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;

import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.BLACK;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.BLUE;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.CYAN;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.GRAY;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.GREEN;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.MAGENTA;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.RED;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.WHITE;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.WHITE_FOREGROUND;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.YELLOW;
import static org.eclipse.tm.internal.terminal.textcanvas.AnsiColorNames.table;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.preferences.ITerminalConstants;
import org.eclipse.tm.terminal.model.Style;
import org.eclipse.tm.terminal.model.StyleColor;

public class StyleMap {

	private static final String PREFIX = "org.eclipse.tm.internal."; //$NON-NLS-1$
	String fFontName = ITerminalConstants.FONT_DEFINITION;
	Map<StyleColor, Color> fColorMapForeground = new HashMap<>();
	Map<StyleColor, Color> fColorMapBackground = new HashMap<>();
	Map<StyleColor, Color> fColorMapIntense = new HashMap<>();
	private Point fCharSize;
	private final Style fDefaultStyle;
	private boolean fInvertColors;
	private boolean fProportional;
	private final int[] fOffsets = new int[256];

	StyleMap() {
		initColors();
		fDefaultStyle = Style.getStyle(StyleColor.getStyleColor(BLACK), StyleColor.getStyleColor(WHITE));
		updateFont();
	}

	private void initColors() {
		initForegroundColors();
		initBackgroundColors();
		initIntenseColors();
	}

	private void initForegroundColors() {
		if (fInvertColors) {
			setColor(fColorMapForeground, WHITE, 0, 0, 0);
			setColor(fColorMapForeground, WHITE_FOREGROUND, 50, 50, 50);
			setColor(fColorMapForeground, 0, BLACK, 229, 229, 229);
		} else {
			setColor(fColorMapForeground, WHITE, 255, 255, 255);
			setColor(fColorMapForeground, WHITE_FOREGROUND, 229, 229, 229);
			setColor(fColorMapForeground, 0, BLACK, 50, 50, 50);
		}
		setColor(fColorMapForeground, 1, RED, 205, 0, 0);
		setColor(fColorMapForeground, 2, GREEN, 0, 205, 0);
		setColor(fColorMapForeground, 3, YELLOW, 205, 205, 0);
		setColor(fColorMapForeground, 4, BLUE, 0, 0, 238);
		setColor(fColorMapForeground, 5, MAGENTA, 205, 0, 205);
		setColor(fColorMapForeground, 6, CYAN, 0, 205, 205);
		setColor(fColorMapForeground, 7, GRAY, 229, 229, 229);

		setTableColors(fColorMapForeground);
	}

	private void initBackgroundColors() {
		if (fInvertColors) {
			setColor(fColorMapBackground, WHITE, 0, 0, 0);
			setColor(fColorMapBackground, WHITE_FOREGROUND, 50, 50, 50); // only used when colors are inverse
			setColor(fColorMapBackground, 0, BLACK, 255, 255, 255);
		} else {
			setColor(fColorMapBackground, WHITE, 255, 255, 255);
			setColor(fColorMapBackground, WHITE_FOREGROUND, 229, 229, 229);
			setColor(fColorMapBackground, 0, BLACK, 0, 0, 0);
		}
		setColor(fColorMapBackground, 1, RED, 205, 0, 0);
		setColor(fColorMapBackground, 2, GREEN, 0, 205, 0);
		setColor(fColorMapBackground, 3, YELLOW, 205, 205, 0);
		setColor(fColorMapBackground, 4, BLUE, 0, 0, 238);
		setColor(fColorMapBackground, 5, MAGENTA, 205, 0, 205);
		setColor(fColorMapBackground, 6, CYAN, 0, 205, 205);
		setColor(fColorMapBackground, 7, GRAY, 229, 229, 229);

		setTableColors(fColorMapBackground);
	}

	private void initIntenseColors() {
		if (fInvertColors) {
			setColor(fColorMapIntense, 8, WHITE, 127, 127, 127);
			setColor(fColorMapIntense, WHITE_FOREGROUND, 0, 0, 0); // only used when colors are inverse
			setColor(fColorMapIntense, BLACK, 255, 255, 255);
		} else {
			setColor(fColorMapIntense, 8, WHITE, 255, 255, 255);
			setColor(fColorMapIntense, WHITE_FOREGROUND, 255, 255, 255);
			setColor(fColorMapIntense, BLACK, 0, 0, 0);
		}
		setColor(fColorMapIntense, 9, RED, 255, 0, 0);
		setColor(fColorMapIntense, 10, GREEN, 0, 255, 0);
		setColor(fColorMapIntense, 11, YELLOW, 255, 255, 0);
		setColor(fColorMapIntense, 12, BLUE, 92, 92, 255);
		setColor(fColorMapIntense, 13, MAGENTA, 255, 0, 255);
		setColor(fColorMapIntense, 14, CYAN, 0, 255, 255);
		setColor(fColorMapIntense, 15, GRAY, 255, 255, 255);

		setTableColors(fColorMapIntense);
	}

	private void setTableColors(Map<StyleColor, Color> map) {
		int vals[] = { 0x00, 0x5f, 0x87, 0xaf, 0xd7, 0xff };
		int index = 16;
		for (int r = 0; r < 6; r++) {
			for (int g = 0; g < 6; g++) {
				for (int b = 0; b < 6; b++) {
					setColor(map, table(index), vals[r], vals[g], vals[b]);
					index++;
				}
			}
		}

		int greys[] = { 0x08, 0x12, 0x1c, 0x26, 0x30, 0x3a, 0x44, 0x4e, 0x58, 0x62, 0x6c, 0x76, 0x80, 0x8a, 0x94, 0x9e,
				0xa8, 0xb2, 0xbc, 0xc6, 0xd0, 0xda, 0xe4, 0xee };

		index = 232;
		for (int g : greys) {
			setColor(map, table(index), g, g, g);
			index++;
		}

	}

	private void setColor(Map<StyleColor, Color> colorMap, int tableIndex, String name, int r, int g, int b) {
		setColor(colorMap, table(tableIndex), r, g, b);
		setColor(colorMap, name, r, g, b);
	}

	private void setColor(Map<StyleColor, Color> colorMap, String name, int r, int g, int b) {
		String colorName = PREFIX + r + "-" + g + "-" + b; //$NON-NLS-1$//$NON-NLS-2$
		Color color = JFaceResources.getColorRegistry().get(colorName);
		if (color == null) {
			JFaceResources.getColorRegistry().put(colorName, new RGB(r, g, b));
			color = JFaceResources.getColorRegistry().get(colorName);
		}
		colorMap.put(StyleColor.getStyleColor(name), color);
		colorMap.put(StyleColor.getStyleColor(name.toUpperCase()), color);
	}

	public Optional<RGB> getForegroundRGB(Style style) {
		style = defaultIfNull(style);
		if (style.isReverse())
			return style.getBackground().getRGB();
		else
			return style.getForground().getRGB();
	}

	public Color getForegrondColor(Style style) {
		style = defaultIfNull(style);
		if (getBackgroundRGB(style).isPresent()) {
			style = fDefaultStyle;
		}
		Map<StyleColor, Color> map = style.isBold() ? fColorMapIntense : fColorMapForeground;
		//Map map = fColorMapForeground;
		if (style.isReverse())
			return getColor(map, style.getBackground());
		else
			return getColor(map, style.getForground());
	}

	public Optional<RGB> getBackgroundRGB(Style style) {
		style = defaultIfNull(style);
		if (style.isReverse())
			return style.getForground().getRGB();
		else
			return style.getBackground().getRGB();
	}

	public Color getBackgroundColor(Style style) {
		style = defaultIfNull(style);
		if (getBackgroundRGB(style).isPresent()) {
			style = fDefaultStyle;
		}
		if (style.isReverse())
			return getColor(fColorMapBackground, style.getForground());
		else
			return getColor(fColorMapBackground, style.getBackground());
	}

	Color getColor(Map<StyleColor, Color> map, StyleColor color) {
		Color c = map.get(color);
		if (c == null) {
			c = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		}
		return c;
	}

	private Style defaultIfNull(Style style) {
		if (style == null)
			style = fDefaultStyle;
		return style;
	}

	public void setInvertedColors(boolean invert) {
		if (invert == fInvertColors)
			return;
		fInvertColors = invert;
		initColors();
	}
	//	static Font getBoldFont(Font font) {
	//		FontData fontDatas[] = font.getFontData();
	//		FontData data = fontDatas[0];
	//		return new Font(Display.getCurrent(), data.getName(), data.getHeight(), data.getStyle()|SWT.BOLD);
	//	}

	public Font getFont(Style style) {
		style = defaultIfNull(style);
		if (style.isBold()) {
			return JFaceResources.getFontRegistry().getBold(fFontName);
		} else if (style.isUnderline()) {
			return JFaceResources.getFontRegistry().getItalic(fFontName);

		}
		return JFaceResources.getFontRegistry().get(fFontName);
	}

	public Font getFont() {
		return JFaceResources.getFontRegistry().get(fFontName);

	}

	public int getFontWidth() {
		return fCharSize.x;
	}

	public int getFontHeight() {
		return fCharSize.y;
	}

	/**
	 * @deprecated Use {@link #updateFont(String)}
	 */
	@Deprecated
	public void updateFont() {
		updateFont(ITerminalConstants.FONT_DEFINITION);
	}

	/**
	 * Update the StyleMap for a new font name.
	 * The font name must be a valid name in the Jface font registry.
	 * @param fontName Jface name of the new font to use.
	 * @since 3.2
	 */
	public void updateFont(String fontName) {
		Display display = Display.getCurrent();
		GC gc = new GC(display);
		if (JFaceResources.getFontRegistry().hasValueFor(fontName)) {
			fFontName = fontName;
		} else {
			//fall back to "basic jface text font"
			fFontName = "org.eclipse.jface.textfont"; //$NON-NLS-1$
		}
		gc.setFont(getFont());
		fCharSize = gc.textExtent("W"); //$NON-NLS-1$
		fProportional = false;

		for (char c = ' '; c <= '~'; c++) {
			// consider only the first 128 chars for deciding if a font
			// is proportional. Collect char width as a side-effect.
			if (measureChar(gc, c, true))
				fProportional = true;
		}
		if (fProportional) {
			// Widest char minus the padding on the left and right:
			// Looks much better for small fonts
			fCharSize.x -= 2;
			// Collect width of the upper characters (for offset calculation)
			for (char c = '~' + 1; c < fOffsets.length; c++) {
				measureChar(gc, c, false);
			}
			// Calculate offsets based on each character's width and the bounding box
			for (int i = ' '; i < fOffsets.length; i++) {
				fOffsets[i] = (fCharSize.x - fOffsets[i]) / 2;
			}
		} else {
			// Non-Proportional: Reset all offsets (eg after font change)
			for (int i = 0; i < fOffsets.length; i++) {
				fOffsets[i] = 0;
			}
			String t = "The quick brown Fox jumps over the Lazy Dog."; //$NON-NLS-1$
			Point ext = gc.textExtent(t);
			if (ext.x != fCharSize.x * t.length()) {
				//Bug 475422: On OSX with Retina display and due to scaling,
				//a text many be shorter than the sum of its bounding boxes.
				//Because even with fixed width font, bounding box size
				//may not be an integer but a fraction eg 6.75 pixels.
				//
				//Painting in proportional mode ensures that each character
				//is painted individually into its proper bounding box, rather
				//than using an optimization where Strings would be drawn as
				//a whole. This fixes the "fractional bounding box" problem.
				fProportional = true;
			}
			//measure font in boldface, too, and if wider then treat like proportional
			gc.setFont(getFont(fDefaultStyle.setBold(true)));
			Point charSizeBold = gc.textExtent("W"); //$NON-NLS-1$
			if (fCharSize.x != charSizeBold.x) {
				fProportional = true;
			}
		}
		gc.dispose();
	}

	/**
	 * @param gc
	 * @param c
	 * @param updateMax
	 * @return true if the the font is proportional
	 */
	private boolean measureChar(GC gc, char c, boolean updateMax) {
		boolean proportional = false;
		Point ext = gc.textExtent(String.valueOf(c));
		if (ext.x > 0 && ext.y > 0 && (fCharSize.x != ext.x || fCharSize.y != ext.y)) {
			proportional = true;
			if (updateMax) {
				fCharSize.x = Math.max(fCharSize.x, ext.x);
				fCharSize.y = Math.max(fCharSize.y, ext.y);
			}
		}
		fOffsets[c] = ext.x;
		return proportional;
	}

	public boolean isFontProportional() {
		return fProportional;
	}

	/**
	 * Return the offset in pixels required to center a given character
	 * @param c the character to measure
	 * @return the offset in x direction to center this character
	 */
	public int getCharOffset(char c) {
		if (c >= fOffsets.length)
			return 0;
		return fOffsets[c];
	}
}
