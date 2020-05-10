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

	private TerminalStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor, boolean bold,
			boolean blink, boolean underline, boolean reverse) {
		fForegroundTerminalColor = foregroundTerminalColor;
		fBackgroundTerminalColor = backgroundTerminalColor;
		fBold = bold;
		fBlink = blink;
		fUnderline = underline;
		fReverse = reverse;
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			boolean bold, boolean blink, boolean underline, boolean reverse) {
		TerminalStyle style = new TerminalStyle(foregroundTerminalColor, backgroundTerminalColor, bold, blink,
				underline, reverse);
		// If set had a computeIfAbsent we would use a set, instead just store 1-2-1 mapping
		return fgStyles.computeIfAbsent(style, (s) -> style);
	}

	public static TerminalStyle getDefaultStyle() {
		return getStyle(TerminalColor.FOREGROUND, TerminalColor.BACKGROUND);
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor) {
		return getStyle(foregroundTerminalColor, backgroundTerminalColor, false, false, false, false);
	}

	public TerminalStyle setForeground(TerminalColor foregroundTerminalColor) {
		return getStyle(foregroundTerminalColor, fBackgroundTerminalColor, fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBackground(TerminalColor backgroundTerminalColor) {
		return getStyle(fForegroundTerminalColor, backgroundTerminalColor, fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setForeground(TerminalStyle other) {
		return getStyle(other.fForegroundTerminalColor, fBackgroundTerminalColor, fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBackground(TerminalStyle other) {
		return getStyle(fForegroundTerminalColor, other.fBackgroundTerminalColor, fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBold(boolean bold) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, bold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBlink(boolean blink) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fBold, blink, fUnderline, fReverse);
	}

	public TerminalStyle setUnderline(boolean underline) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fBold, fBlink, underline, fReverse);
	}

	public TerminalStyle setReverse(boolean reverse) {
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fBold, fBlink, fUnderline, reverse);
	}

	public TerminalColor getForegroundTerminalColor() {
		return fForegroundTerminalColor;
	}

	public TerminalColor getBackgroundTerminalColor() {
		return fBackgroundTerminalColor;
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
		result = prime * result + (fBlink ? 1231 : 1237);
		result = prime * result + (fBold ? 1231 : 1237);
		result = prime * result + ((fForegroundTerminalColor == null) ? 0 : fForegroundTerminalColor.hashCode());
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
		if (fBlink != other.fBlink)
			return false;
		if (fBold != other.fBold)
			return false;
		if (fForegroundTerminalColor != other.fForegroundTerminalColor)
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
		result.append(fForegroundTerminalColor);
		result.append(", background="); //$NON-NLS-1$
		result.append(fBackgroundTerminalColor);
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
