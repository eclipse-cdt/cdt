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

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.preferences.ITerminalConstants;
import org.eclipse.tm.internal.terminal.preferences.TerminalColorPresets;
import org.eclipse.tm.terminal.model.TerminalColor;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * The split between responsibilities of StyleMap and TerminalStyle are not always clear. Generally
 * the style parts that are global for a terminal are here, where as in TerminalStyle is about
 * a specific range.
 */
public class StyleMap {

	String fFontName = ITerminalConstants.FONT_DEFINITION;
	private Point fCharSize;
	private final TerminalStyle fDefaultStyle;
	private boolean fInvertColors;
	private boolean fProportional;
	private final int[] fOffsets = new int[256];
	private final Map<TerminalColor, RGB> fColorMap = new EnumMap<>(TerminalColor.class);

	public StyleMap() {
		fDefaultStyle = TerminalStyle.getDefaultStyle();
		initFont();
		initColors();
	}

	private void initColors() {
		Map<TerminalColor, RGB> map = new EnumMap<>(TerminalColor.class);
		TerminalColor[] values = TerminalColor.values();
		for (TerminalColor terminalColor : values) {
			RGB rgb = TerminalColorPresets.INSTANCE.getDefaultPreset().getRGB(terminalColor);
			map.put(terminalColor, rgb);
		}
		updateColors(map);
	}

	private void initFont() {
		updateFont(ITerminalConstants.FONT_DEFINITION);
	}

	private RGB getRGB(TerminalColor color) {
		return fColorMap.get(color);
	}

	public RGB getForegrondRGB(TerminalStyle style) {
		style = defaultIfNull(style);
		RGB foregroundRGB;
		if (style.isReverse()) {
			foregroundRGB = style.getBackgroundRGB();
		} else {
			foregroundRGB = style.getForegroundRGB();
		}
		if (foregroundRGB != null) {
			return foregroundRGB;
		}

		TerminalColor color;
		if (style.isReverse()) {
			color = style.getBackgroundTerminalColor();
		} else {
			color = style.getForegroundTerminalColor();
		}

		if (color == null) {
			color = TerminalColor.FOREGROUND;
		}

		color = color.convertColor(fInvertColors, style.isBold());
		return getRGB(color);
	}

	public RGB getBackgroundRGB(TerminalStyle style) {
		style = defaultIfNull(style);
		RGB backgroundRGB;
		if (style.isReverse()) {
			backgroundRGB = style.getForegroundRGB();
		} else {
			backgroundRGB = style.getBackgroundRGB();
		}
		if (backgroundRGB != null) {
			return backgroundRGB;
		}

		TerminalColor color;
		if (style.isReverse()) {
			color = style.getForegroundTerminalColor();
		} else {
			color = style.getBackgroundTerminalColor();
		}

		if (color == null) {
			color = TerminalColor.BACKGROUND;
		}

		color = color.convertColor(fInvertColors, style.isBold());
		return getRGB(color);
	}

	private TerminalStyle defaultIfNull(TerminalStyle style) {
		if (style == null)
			style = fDefaultStyle;
		return style;
	}

	public void setInvertedColors(boolean invert) {
		fInvertColors = invert;
	}

	public boolean isInvertedColors() {
		return fInvertColors;
	}

	public Font getFont(TerminalStyle style) {
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

	public void updateColors(Map<TerminalColor, RGB> colorMap) {
		fColorMap.putAll(colorMap);
	}
}
