/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Colors that can be used in the Terminal are represented by this class. The enum contains
 * the colors with well known names defined by the ANSI Escape Sequences, plus other colors needed
 * to render a display (such as Background color).
 *
 * Rather than name all the colors when using ANSI 8-bit indexed colors, the indexed colors
 * can be accessed via the {@link #getIndexedRGBColor(int)} or {@link #getIndexedRGBColor(int)}
 * (use {@link #isIndexedTerminalColor(int)} to determine which one is appropriate.
 *
 * The {@link TerminalStyle} supports any arbitrary color by using {@link RGB} defined colors.
 * This class provides the connection between the names exposed to the user in preferences
 * and their use in the terminal, along with how colors change when other attributes (such as
 * bright and invertColors) are applied to them.
 *
 * @since 5.0
 */
public enum TerminalColor {
	BLACK, //
	RED, //
	GREEN, //
	YELLOW, //
	BLUE, //
	MAGENTA, //
	CYAN, //
	WHITE, //

	BRIGHT_BLACK, //
	BRIGHT_RED, //
	BRIGHT_GREEN, //
	BRIGHT_YELLOW, //
	BRIGHT_BLUE, //
	BRIGHT_MAGENTA, //
	BRIGHT_CYAN, //
	BRIGHT_WHITE, //

	FOREGROUND, //
	BACKGROUND, //
	SELECTION_FOREGROUND, //
	SELECTION_BACKGROUND;

	/**
	 * The first 16-items in the 8-bit lookup table map to the user changeable colors
	 * above, so this array handles that mapping.
	 */
	private final static TerminalColor table8bitIndexedTerminalColors[] = new TerminalColor[16];

	/**
	 * The rest of the colors in the lookup table (240 colors) are pre-defined by
	 * the standard. The colors that fill this table were derived from
	 * https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit which was more
	 * digestible and accessible than the underlying ITU and ISO standards.
	 */
	private final static RGB table8bitIndexedRGB[] = new RGB[256 - 16];

	/**
	 * Color to use instead when inverted color is selected
	 */
	private TerminalColor invertColor;

	/**
	 * Color to use instead when bright color is selected
	 */
	private TerminalColor brightColor;

	/**
	 * Pre-calculate the lookup tables for 8-bit colors, inverses and equivalent brights.
	 */
	static {
		TerminalColor[] values = TerminalColor.values();

		// 8-bit color lookup tables
		{
			int index = 0;
			for (; index < 16; index++) {
				TerminalColor c = values[index];
				table8bitIndexedTerminalColors[index] = c;
			}

			int vals[] = { 0x00, 0x5f, 0x87, 0xaf, 0xd7, 0xff };
			Assert.isTrue(index == 16);
			for (int r = 0; r < 6; r++) {
				for (int g = 0; g < 6; g++) {
					for (int b = 0; b < 6; b++) {
						table8bitIndexedRGB[index++ - 16] = new RGB(vals[r], vals[g], vals[b]);
					}
				}
			}

			int greys[] = { 0x08, 0x12, 0x1c, 0x26, 0x30, 0x3a, 0x44, 0x4e, 0x58, 0x62, 0x6c, 0x76, 0x80, 0x8a, 0x94,
					0x9e, 0xa8, 0xb2, 0xbc, 0xc6, 0xd0, 0xda, 0xe4, 0xee };

			Assert.isTrue(index == 232);
			for (int g : greys) {
				table8bitIndexedRGB[index++ - 16] = new RGB(g, g, g);
			}
			Assert.isTrue(index == 256);
		}

		// bright equivalents
		{
			// The second set of 8 colors are the bright of the first 8.
			for (int i = 0; i < 8; i++) {
				values[i].brightColor = values[i + 8];
			}
			// The rest of the colors are not brightened
			for (int i = 8; i < values.length; i++) {
				values[i].brightColor = values[i];
			}
		}

		// inverses
		{
			// by default make all colors invert of themself
			for (int i = 0; i < values.length; i++) {
				values[i].invertColor = values[i];
			}
			// and then mark the colors that are actual inverts
			inverts(BLACK, WHITE);
			inverts(BRIGHT_BLACK, BRIGHT_WHITE);
			inverts(BACKGROUND, FOREGROUND);
		}

	}

	private static void inverts(TerminalColor a, TerminalColor b) {
		a.invertColor = b;
		b.invertColor = a;
	}

	/**
	 * Return a new color for the given color with inversions or brightness attributes applied.
	 *
	 * @param invert For invertible colors, return the inverse (typically white &lt;-&gt; black)
	 * @param bright returns the brighter version of the color if one is available
	 * @return {@link ColorDescriptor} that a {@link Color} can be made from
	 *     using {@link ColorDescriptor#createColor(org.eclipse.swt.graphics.Device)}
	 * @throws NullPointerException if there is no current {@link Display}
	 */
	public TerminalColor convertColor(boolean invert, boolean bright) {
		TerminalColor selected = this;
		// it doesn't matter which order you apply bright and invert, you get to
		// the same color when both are set
		if (invert) {
			selected = selected.invertColor;
		}
		if (bright) {
			selected = selected.brightColor;
		}
		return selected;
	}

	/**
	 * Query for whether the 8-bit color index will return a named color, in which case
	 * {@link #getIndexedTerminalColor(int)} must be called to get the named color. Use
	 * {@link #convertColor(boolean, boolean)} if this method returns false.
	 *
	 * @param index 8-bit index.
	 * @return true for named colors, false for RGB colors
	 */
	public static boolean isIndexedTerminalColor(int index) {
		Assert.isLegal(index >= 0 && index < 256, "Invalid 8-bit table index out of range 0-255"); //$NON-NLS-1$
		return index < table8bitIndexedTerminalColors.length && index >= 0;
	}

	/**
	 * Return the named color for the given 8-bit index.
	 *
	 * @param index 8-bit index in 0-15 range.
	 * @return named color
	 */
	public static TerminalColor getIndexedTerminalColor(int index) {
		Assert.isLegal(isIndexedTerminalColor(index), "Invalid table index used for ANSI Color"); //$NON-NLS-1$
		return table8bitIndexedTerminalColors[index];
	}

	/**
	 * Return the RGB color for the given 8-bit index.
	 *
	 * @param index 8-bit index in 16-255 range.
	 * @return RGB color
	 */
	public static RGB getIndexedRGBColor(int index) {
		Assert.isLegal(index >= 16 && index < 256, "Invalid table index used for RGB Color"); //$NON-NLS-1$
		return table8bitIndexedRGB[index - 16];
	}
}
