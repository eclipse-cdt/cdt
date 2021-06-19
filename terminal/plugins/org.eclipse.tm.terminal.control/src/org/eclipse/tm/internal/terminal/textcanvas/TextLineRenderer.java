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
 * Michael Scharf (Wind River) - [206328] Terminal does not draw correctly with proportional fonts
 * Anton Leherbauer (Wind River) - [294468] Fix scroller and text line rendering
 * Martin Oberhuber (Wind River) - [265352][api] Allow setting fonts programmatically
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;

import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.impl.TerminalPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.TerminalColor;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 *
 */
public class TextLineRenderer implements ILinelRenderer {
	private static final boolean DEBUG_HOVER = TerminalPlugin.isOptionEnabled(Logger.TRACE_DEBUG_LOG_HOVER);
	private final ITextCanvasModel fModel;
	private final StyleMap fStyleMap;

	public TextLineRenderer(TextCanvas c, ITextCanvasModel model) {
		fModel = model;
		fStyleMap = new StyleMap();
	}

	@Override
	public int getCellWidth() {
		return fStyleMap.getFontWidth();
	}

	@Override
	public int getCellHeight() {
		return fStyleMap.getFontHeight();
	}

	@Override
	public void drawLine(ITextCanvasModel model, GC gc, int line, int x, int y, int colFirst, int colLast) {
		int width = getCellWidth() * (colLast - colFirst);
		int height = getCellHeight();
		if (width <= 0 || height <= 0) {
			return;
		}
		Image buffer = new Image(gc.getDevice(), width, height);
		GC doubleBufferGC = new GC(buffer);
		if (line < 0 || line >= getTerminalText().getHeight() || colFirst >= getTerminalText().getWidth()
				|| colFirst - colLast == 0) {
			fillBackground(doubleBufferGC, 0, 0, width, height);
		} else {
			colLast = Math.min(colLast, getTerminalText().getWidth());
			LineSegment[] segments = getTerminalText().getLineSegments(line, colFirst, colLast - colFirst);
			for (int i = 0; i < segments.length; i++) {
				LineSegment segment = segments[i];
				TerminalStyle style = segment.getStyle();
				setupGC(doubleBufferGC, style);
				String text = segment.getText();
				drawText(doubleBufferGC, 0, 0, colFirst, segment.getColumn(), text);
				drawCursor(model, doubleBufferGC, line, 0, 0, colFirst);
			}
			if (fModel.hasHoverSelection(line)) {
				if (DEBUG_HOVER) {
					System.out.format("hover: %s  contains hover selection\n", line); //$NON-NLS-1$
				}
				Point hsStart = fModel.getHoverSelectionStart();
				Point hsEnd = fModel.getHoverSelectionEnd();
				int colStart = line == hsStart.y ? hsStart.x : 0;
				int colEnd = line == hsEnd.y ? hsEnd.x : getTerminalText().getWidth();
				if (colStart < colEnd) {
					RGB defaultFg = fStyleMap.getForegrondRGB(null);
					doubleBufferGC.setForeground(new Color(doubleBufferGC.getDevice(), defaultFg));
					drawUnderline(doubleBufferGC, colStart, colEnd);
				}
			}
			if (fModel.hasLineSelection(line)) {
				TerminalStyle style = TerminalStyle.getStyle(TerminalColor.SELECTION_FOREGROUND,
						TerminalColor.SELECTION_BACKGROUND);
				setupGC(doubleBufferGC, style);
				Point start = model.getSelectionStart();
				Point end = model.getSelectionEnd();
				char[] chars = model.getTerminalText().getChars(line);
				if (chars != null) {
					int offset = 0;
					if (start.y == line)
						offset = start.x;
					offset = Math.max(offset, colFirst);
					int len;
					if (end.y == line)
						len = end.x - offset + 1;
					else
						len = chars.length - offset + 1;
					len = Math.min(len, chars.length - offset);
					if (len > 0) {
						String text = new String(chars, offset, len);
						drawText(doubleBufferGC, 0, 0, colFirst, offset, text);
					}
				}
			}
		}
		gc.drawImage(buffer, x, y);
		doubleBufferGC.dispose();
		buffer.dispose();
	}

	private void fillBackground(GC gc, int x, int y, int width, int height) {
		Color bg = gc.getBackground();
		gc.setBackground(getDefaultBackgroundColor(gc.getDevice()));
		gc.fillRectangle(x, y, width, height);
		gc.setBackground(bg);

	}

	@Override
	public Color getDefaultBackgroundColor() {
		return getDefaultBackgroundColor(Display.getDefault());
	}

	@Override
	public Color getDefaultBackgroundColor(Device device) {
		// null == default style
		RGB backgroundRGB = fStyleMap.getBackgroundRGB(null);
		return new Color(device, backgroundRGB);
	}

	private void drawCursor(ITextCanvasModel model, GC gc, int row, int x, int y, int colFirst) {
		if (!model.isCursorOn())
			return;
		int cursorLine = model.getCursorLine();

		if (row == cursorLine) {
			int cursorColumn = model.getCursorColumn();
			if (cursorColumn < getTerminalText().getWidth()) {
				TerminalStyle style = getTerminalText().getStyle(row, cursorColumn);
				if (style == null) {
					// TODO make the cursor color customizable
					style = TerminalStyle.getStyle(TerminalColor.FOREGROUND, TerminalColor.BACKGROUND);
				}
				style = style.setReverse(!style.isReverse());
				setupGC(gc, style);
				String text = String.valueOf(getTerminalText().getChar(row, cursorColumn));
				drawText(gc, x, y, colFirst, cursorColumn, text);
			}
		}
	}

	private void drawText(GC gc, int x, int y, int colFirst, int col, String text) {
		int offset = (col - colFirst) * getCellWidth();
		if (fStyleMap.isFontProportional()) {
			// draw the background
			// TODO why does this not work???????
			//			gc.fillRectangle(x,y,fStyleMap.getFontWidth()*text.length(),fStyleMap.getFontHeight());
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				int xx = x + offset + i * fStyleMap.getFontWidth();
				// TODO why do I have to draw the background character by character??????
				gc.fillRectangle(xx, y, fStyleMap.getFontWidth(), fStyleMap.getFontHeight());
				if (c != ' ' && c != '\000') {
					gc.drawString(String.valueOf(c), fStyleMap.getCharOffset(c) + xx, y, false);
				}
			}
		} else {
			text = text.replace('\000', ' ');
			gc.drawString(text, x + offset, y, false);
		}
	}

	/**
	 *
	 * @param gc
	 * @param colStart Starting text column to underline (inclusive)
	 * @param colEnd Ending text column to underline (inclusive)
	 */
	private void drawUnderline(GC gc, int colStart, int colEnd) {
		int y = getCellHeight() - 1;
		int x = getCellWidth() * colStart;

		// x2 is the right side of last column being underlined.
		int x2 = (colEnd + 1) * getCellWidth() - 1;
		gc.drawLine(x, y, x2, y);
	}

	private void setupGC(GC gc, TerminalStyle style) {
		RGB foregrondColor = fStyleMap.getForegrondRGB(style);
		gc.setForeground(new Color(gc.getDevice(), foregrondColor));
		RGB backgroundColor = fStyleMap.getBackgroundRGB(style);
		gc.setBackground(new Color(gc.getDevice(), backgroundColor));

		Font f = fStyleMap.getFont(style);
		if (f != gc.getFont()) {
			gc.setFont(f);
		}
	}

	ITerminalTextDataReadOnly getTerminalText() {
		return fModel.getTerminalText();
	}

	/**
	 * @deprecated Use {@link #updateFont(String)}
	 */
	@Deprecated
	@Override
	public void onFontChange() {
		fStyleMap.updateFont();
	}

	@Override
	public void updateFont(String fontName) {
		fStyleMap.updateFont(fontName);
	}

	@Override
	public void updateColors(Map<TerminalColor, RGB> map) {
		fStyleMap.updateColors(map);
	}

	@Override
	public void setInvertedColors(boolean invert) {
		fStyleMap.setInvertedColors(invert);

	}

	@Override
	public boolean isInvertedColors() {
		return fStyleMap.isInvertedColors();
	}
}
