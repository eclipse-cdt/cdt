/*******************************************************************************
 * Copyright (c) 2007, 2020 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;

/**
 * @author scharf
 * Flyweight
 * Threadsafe.
 * @since 5.0
 *
 */
// TODO add an Object for user data, use weak map to keep track of styles with associated
// user data
public class TerminalStyle {
	private final TerminalColor fForegroundTerminalColor;
	private final TerminalColor fBackgroundTerminalColor;
	private final RGB fForegroundRGB;
	private final RGB fBackgroundRGB;
	private final boolean fBold;
	private final boolean fBlink;
	private final boolean fUnderline;
	private final boolean fReverse;
	private final static Map<TerminalStyle, TerminalStyle> fgStyles = Collections
			.synchronizedMap(new LinkedHashMap<TerminalStyle, TerminalStyle>() {
				@Override
				protected boolean removeEldestEntry(Map.Entry<TerminalStyle, TerminalStyle> eldest) {
					int size = size();
					boolean removeEldest = size >= 1000;
					if (TerminalPlugin.isOptionEnabled(Logger.TRACE_DEBUG_LOG_VT100BACKEND)) {
						if (removeEldest) {
							Logger.log("Removing eldest Style from style cache, size = " + size); //$NON-NLS-1$
						} else {
							Logger.log("Leaving eldest Style in style cache, size = " + size); //$NON-NLS-1$
						}
					}
					return removeEldest;
				}
			});

	private TerminalStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink, boolean underline, boolean reverse) {
		Assert.isLegal(foregroundTerminalColor == null || foregroundRGB == null,
				"Only one of ANSI or RGB colors can be specified as a foreground color"); //$NON-NLS-1$
		Assert.isLegal(backgroundTerminalColor == null || backgroundRGB == null,
				"Only one of ANSI or RGB colors can be specified as a background color"); //$NON-NLS-1$
		fForegroundTerminalColor = foregroundTerminalColor;
		fBackgroundTerminalColor = backgroundTerminalColor;
		fForegroundRGB = foregroundRGB;
		fBackgroundRGB = backgroundRGB;
		fBold = bold;
		fBlink = blink;
		fUnderline = underline;
		fReverse = reverse;
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink, boolean underline, boolean reverse) {
		TerminalStyle style = new TerminalStyle(foregroundTerminalColor, backgroundTerminalColor, foregroundRGB,
				backgroundRGB, bold, blink, underline, reverse);
		// If set had a computeIfAbsent we would use a set, instead just store 1-2-1 mapping
		return fgStyles.computeIfAbsent(style, (s) -> style);
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			boolean bold, boolean blink, boolean underline, boolean reverse) {
		return getStyle(foregroundTerminalColor, backgroundTerminalColor, null, null, bold, blink, underline, reverse);
	}

	public static TerminalStyle getStyle(RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink,
			boolean underline, boolean reverse) {
		return getStyle(null, null, foregroundRGB, backgroundRGB, bold, blink, underline, reverse);
	}

	public static TerminalStyle getDefaultStyle() {
		return getStyle(TerminalColor.FOREGROUND, TerminalColor.BACKGROUND);
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor) {
		return getStyle(foregroundTerminalColor, backgroundTerminalColor, null, null, false, false, false, false);
	}

	public TerminalStyle setForeground(TerminalColor foregroundTerminalColor) {
		return getStyle(foregroundTerminalColor, fBackgroundTerminalColor, null, fBackgroundRGB, fBold, fBlink,
				fUnderline, fReverse);
	}

	public TerminalStyle setBackground(TerminalColor backgroundTerminalColor) {
		return getStyle(fForegroundTerminalColor, backgroundTerminalColor, fForegroundRGB, null, fBold, fBlink,
				fUnderline, fReverse);
	}

	public TerminalStyle setForeground(RGB foregroundRGB) {
		return getStyle(null, fBackgroundTerminalColor, foregroundRGB, fBackgroundRGB, fBold, fBlink, fUnderline,
				fReverse);
	}

	public TerminalStyle setBackground(RGB backgroundRGB) {
		return getStyle(fForegroundTerminalColor, null, fForegroundRGB, backgroundRGB, fBold, fBlink, fUnderline,
				fReverse);
	}

	public TerminalStyle setForeground(TerminalStyle other) {
		return getStyle(other.fForegroundTerminalColor, fBackgroundTerminalColor, other.fForegroundRGB, fBackgroundRGB,
				fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBackground(TerminalStyle other) {
		return getStyle(fForegroundTerminalColor, other.fBackgroundTerminalColor, fForegroundRGB, other.fBackgroundRGB,
				fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setForeground(int eightBitindexedColor) {
		boolean isIndexTerminalColor = TerminalColor.isIndexedTerminalColor(eightBitindexedColor);
		if (isIndexTerminalColor) {
			TerminalColor foregroundTerminalColor = TerminalColor.getIndexedTerminalColor(eightBitindexedColor);
			return getStyle(foregroundTerminalColor, fBackgroundTerminalColor, null, fBackgroundRGB, fBold, fBlink,
					fUnderline, fReverse);
		} else {
			RGB foregroundRGB = TerminalColor.getIndexedRGBColor(eightBitindexedColor);
			return getStyle(null, fBackgroundTerminalColor, foregroundRGB, fBackgroundRGB, fBold, fBlink, fUnderline,
					fReverse);
		}
	}

	public TerminalStyle setBackground(int eightBitindexedColor) {
		boolean isIndexTerminalColor = TerminalColor.isIndexedTerminalColor(eightBitindexedColor);
		if (isIndexTerminalColor) {
			TerminalColor backgroundTerminalColor = TerminalColor.getIndexedTerminalColor(eightBitindexedColor);
			return getStyle(fForegroundTerminalColor, backgroundTerminalColor, fForegroundRGB, null, fBold, fBlink,
					fUnderline, fReverse);
		} else {
			RGB backgroundRGB = TerminalColor.getIndexedRGBColor(eightBitindexedColor);
			return getStyle(fForegroundTerminalColor, null, fForegroundRGB, backgroundRGB, fBold, fBlink, fUnderline,
					fReverse);
		}
	}

	public TerminalStyle setBold(boolean bold) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, bold,
				fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBlink(boolean blink) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				blink, fUnderline, fReverse);
	}

	public TerminalStyle setUnderline(boolean underline) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				fBlink, underline, fReverse);
	}

	public TerminalStyle setReverse(boolean reverse) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				fBlink, fUnderline, reverse);
	}

	public TerminalColor getForegroundTerminalColor() {
		return fForegroundTerminalColor;
	}

	public TerminalColor getBackgroundTerminalColor() {
		return fBackgroundTerminalColor;
	}

	public RGB getForegroundRGB() {
		return fForegroundRGB;
	}

	public RGB getBackgroundRGB() {
		return fBackgroundRGB;
	}

	public boolean isBlink() {
		return fBlink;
	}

	public boolean isBold() {
		return fBold;
	}

	public boolean isReverse() {
		return fReverse;
	}

	public boolean isUnderline() {
		return fUnderline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fBackgroundTerminalColor == null) ? 0 : fBackgroundTerminalColor.hashCode());
		result = prime * result + ((fBackgroundRGB == null) ? 0 : fBackgroundRGB.hashCode());
		result = prime * result + (fBlink ? 1231 : 1237);
		result = prime * result + (fBold ? 1231 : 1237);
		result = prime * result + ((fForegroundTerminalColor == null) ? 0 : fForegroundTerminalColor.hashCode());
		result = prime * result + ((fForegroundRGB == null) ? 0 : fForegroundRGB.hashCode());
		result = prime * result + (fReverse ? 1231 : 1237);
		result = prime * result + (fUnderline ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TerminalStyle other = (TerminalStyle) obj;
		if (fBackgroundTerminalColor != other.fBackgroundTerminalColor)
			return false;
		if (fBackgroundRGB == null) {
			if (other.fBackgroundRGB != null)
				return false;
		} else if (!fBackgroundRGB.equals(other.fBackgroundRGB))
			return false;
		if (fBlink != other.fBlink)
			return false;
		if (fBold != other.fBold)
			return false;
		if (fForegroundTerminalColor != other.fForegroundTerminalColor)
			return false;
		if (fForegroundRGB == null) {
			if (other.fForegroundRGB != null)
				return false;
		} else if (!fForegroundRGB.equals(other.fForegroundRGB))
			return false;
		if (fReverse != other.fReverse)
			return false;
		if (fUnderline != other.fUnderline)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Style(foreground="); //$NON-NLS-1$
		if (fForegroundTerminalColor != null) {
			result.append(fForegroundTerminalColor);
		} else {
			result.append(fForegroundRGB);
		}
		result.append(", background="); //$NON-NLS-1$
		if (fForegroundTerminalColor != null) {
			result.append(fBackgroundTerminalColor);
		} else {
			result.append(fBackgroundRGB);
		}
		if (fBlink)
			result.append(", blink"); //$NON-NLS-1$
		if (fBold)
			result.append(", bold"); //$NON-NLS-1$
		if (fBlink)
			result.append(", blink"); //$NON-NLS-1$
		if (fReverse)
			result.append(", reverse"); //$NON-NLS-1$
		if (fUnderline)
			result.append(", underline"); //$NON-NLS-1$
		result.append(")"); //$NON-NLS-1$
		return result.toString();
	}

}
