/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Anton Leherbauer (Wind River) - [206329] Changing terminal size right after connect does not scroll properly
 * Anton Leherbauer (Wind River) - [433751] Add option to enable VT100 line wrapping mode
 * Anton Leherbauer (Wind River) - [458218] Add support for ANSI insert mode
 * Anton Leherbauer (Wind River) - [458402] Add support for scroll up/down and scroll region
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.emulator;

import org.eclipse.tm.terminal.model.ITerminalTextData;
import org.eclipse.tm.terminal.model.TerminalStyle;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noreference This class not intended to be referenced by clients.
 *      It used to be package protected, and it is public only for Unit Tests.
 *
 */
public class VT100EmulatorBackend implements IVT100EmulatorBackend {

	private static class ScrollRegion {
		static final ScrollRegion FULL_WINDOW = new ScrollRegion(0, Integer.MAX_VALUE - 1);
		private final int fTop;
		private final int fBottom;

		ScrollRegion(int top, int bottom) {
			fTop = top;
			fBottom = bottom;
		}

		boolean contains(int line) {
			return line >= fTop && line <= fBottom;
		}

		int getTopLine() {
			return fTop;
		}

		int getBottomLine() {
			return fBottom;
		}

		int getHeight() {
			return fBottom - fTop + 1;
		}
	}

	/**
	 * This field holds the number of the column in which the cursor is
	 * logically positioned. The leftmost column on the screen is column 0, and
	 * column numbers increase to the right. The maximum value of this field is
	 * {@link #widthInColumns} - 1. We track the cursor column using this field
	 * to avoid having to recompute it repeatly using StyledText method calls.
	 * <p>
	 *
	 * The StyledText widget that displays text has a vertical bar (called the
	 * "caret") that appears _between_ character cells, but ANSI terminals have
	 * the concept of a cursor that appears _in_ a character cell, so we need a
	 * convention for which character cell the cursor logically occupies when
	 * the caret is physically between two cells. The convention used in this
	 * class is that the cursor is logically in column N when the caret is
	 * physically positioned immediately to the _left_ of column N.
	 * <p>
	 *
	 * When fCursorColumn is N, the next character output to the terminal appears
	 * in column N. When a character is output to the rightmost column on a
	 * given line (column widthInColumns - 1), the cursor moves to column 0 on
	 * the next line after the character is drawn (this is the default line wrapping
	 * mode). If VT100 line wrapping mode is enabled, the cursor does not move
	 * to the next line until the next character is printed (this is known as
	 * the VT100 'eat_newline_glitch').
	 * If the cursor is in the bottommost line when line wrapping
	 * occurs, the topmost visible line is scrolled off the top edge of the
	 * screen.
	 * <p>
	 */
	private int fCursorColumn;
	private int fCursorLine;
	/* true if last output occurred on rightmost column
	 * and next output requires line wrap */
	private boolean fWrapPending;
	private boolean fInsertMode;
	private TerminalStyle fDefaultStyle;
	private TerminalStyle fStyle;
	int fLines;
	int fColumns;
	final private ITerminalTextData fTerminal;
	private boolean fVT100LineWrapping;
	private ScrollRegion fScrollRegion = ScrollRegion.FULL_WINDOW;

	public VT100EmulatorBackend(ITerminalTextData terminal) {
		fTerminal = terminal;
	}

	@Override
	public void clearAll() {
		synchronized (fTerminal) {
			// clear the history
			int n = fTerminal.getHeight();
			for (int line = 0; line < n; line++) {
				fTerminal.cleanLine(line);
			}
			fTerminal.setDimensions(fLines, fTerminal.getWidth());
			setStyle(getDefaultStyle());
			setCursor(0, 0);
		}
	}

	@Override
	public void setDimensions(int lines, int cols) {
		synchronized (fTerminal) {
			if (lines == fLines && cols == fColumns)
				return; // nothing to do
			// relative cursor line
			int cl = getCursorLine();
			int cc = getCursorColumn();
			int height = fTerminal.getHeight();
			// absolute cursor line
			int acl = cl + height - fLines;
			int newLines = Math.max(lines, height);
			if (lines < fLines) {
				if (height == fLines) {
					// if the terminal has no history, then resize by
					// setting the size to the new size
					// TODO We are assuming that cursor line points at end of text
					newLines = Math.max(lines, cl + 1);
				}
			}
			fLines = lines;
			fColumns = cols;
			// make the terminal at least as high as we need lines
			fTerminal.setDimensions(newLines, fColumns);
			// compute relative cursor line
			cl = acl - (newLines - fLines);
			setCursor(cl, cc);
		}
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 *      It used to be package protected, and it is public only for Unit Tests.
	 */
	public int toAbsoluteLine(int line) {
		synchronized (fTerminal) {
			return fTerminal.getHeight() - fLines + line;
		}
	}

	@Override
	public void insertCharacters(int charactersToInsert) {
		synchronized (fTerminal) {
			int line = toAbsoluteLine(fCursorLine);
			int n = charactersToInsert;
			for (int col = fColumns - 1; col >= fCursorColumn + n; col--) {
				char c = fTerminal.getChar(line, col - n);
				TerminalStyle style = fTerminal.getStyle(line, col - n);
				fTerminal.setChar(line, col, c, style);
			}
			int last = Math.min(fCursorColumn + n, fColumns);
			for (int col = fCursorColumn; col < last; col++) {
				fTerminal.setChar(line, col, '\000', null);
			}
		}
	}

	@Override
	public void eraseToEndOfScreen() {
		synchronized (fTerminal) {
			eraseLineToEnd();
			for (int line = toAbsoluteLine(fCursorLine + 1); line < toAbsoluteLine(fLines); line++) {
				fTerminal.cleanLine(line);
			}
		}

	}

	@Override
	public void eraseToCursor() {
		synchronized (fTerminal) {
			for (int line = toAbsoluteLine(0); line < toAbsoluteLine(fCursorLine); line++) {
				fTerminal.cleanLine(line);
			}
			eraseLineToCursor();
		}
	}

	@Override
	public void eraseAll() {
		synchronized (fTerminal) {
			for (int line = toAbsoluteLine(0); line < toAbsoluteLine(fLines); line++) {
				fTerminal.cleanLine(line);
			}
		}
	}

	@Override
	public void eraseLine() {
		synchronized (fTerminal) {
			fTerminal.cleanLine(toAbsoluteLine(fCursorLine));
		}
	}

	@Override
	public void eraseLineToEnd() {
		synchronized (fTerminal) {
			int line = toAbsoluteLine(fCursorLine);
			for (int col = fCursorColumn; col < fColumns; col++) {
				fTerminal.setChar(line, col, '\000', null);
			}
		}
	}

	@Override
	public void eraseLineToCursor() {
		synchronized (fTerminal) {
			int line = toAbsoluteLine(fCursorLine);
			for (int col = 0; col <= fCursorColumn; col++) {
				fTerminal.setChar(line, col, '\000', null);
			}
		}
	}

	@Override
	public void insertLines(int n) {
		synchronized (fTerminal) {
			if (!isCusorInScrollingRegion())
				return;
			assert n > 0;
			int line = toAbsoluteLine(fCursorLine);
			int nLines = Math.min(fTerminal.getHeight() - line, fScrollRegion.getBottomLine() - fCursorLine + 1);
			fTerminal.scroll(line, nLines, n);
		}
	}

	@Override
	public void deleteCharacters(int n) {
		synchronized (fTerminal) {
			int line = toAbsoluteLine(fCursorLine);
			for (int col = fCursorColumn + n; col < fColumns; col++) {
				char c = fTerminal.getChar(line, col);
				TerminalStyle style = fTerminal.getStyle(line, col);
				fTerminal.setChar(line, col - n, c, style);
			}
			int first = Math.max(fCursorColumn, fColumns - n);
			for (int col = first; col < fColumns; col++) {
				fTerminal.setChar(line, col, '\000', null);
			}
		}
	}

	@Override
	public void deleteLines(int n) {
		synchronized (fTerminal) {
			if (!isCusorInScrollingRegion())
				return;
			assert n > 0;
			int line = toAbsoluteLine(fCursorLine);
			int nLines = Math.min(fTerminal.getHeight() - line, fScrollRegion.getBottomLine() - fCursorLine + 1);
			fTerminal.scroll(line, nLines, -n);
		}
	}

	private boolean isCusorInScrollingRegion() {
		return fScrollRegion.contains(fCursorLine);
	}

	@Override
	public TerminalStyle getDefaultStyle() {
		synchronized (fTerminal) {
			return fDefaultStyle;
		}
	}

	@Override
	public void setDefaultStyle(TerminalStyle defaultStyle) {
		synchronized (fTerminal) {
			fDefaultStyle = defaultStyle;
		}
	}

	@Override
	public TerminalStyle getStyle() {
		synchronized (fTerminal) {
			if (fStyle == null)
				return fDefaultStyle;
			return fStyle;
		}
	}

	@Override
	public void setStyle(TerminalStyle style) {
		synchronized (fTerminal) {
			fStyle = style;
		}
	}

	@Override
	public void appendString(String buffer) {
		synchronized (fTerminal) {
			char[] chars = buffer.toCharArray();
			if (fInsertMode)
				insertCharacters(chars.length);
			int line = toAbsoluteLine(fCursorLine);
			int i = 0;
			while (i < chars.length) {
				if (fWrapPending) {
					line = doLineWrap();
				}
				int n = Math.min(fColumns - fCursorColumn, chars.length - i);
				fTerminal.setChars(line, fCursorColumn, chars, i, n, fStyle);
				int col = fCursorColumn + n;
				i += n;
				// wrap needed?
				if (col == fColumns) {
					if (fVT100LineWrapping) {
						// deferred line wrapping (eat_newline_glitch)
						setCursorColumn(col - 1);
						fWrapPending = true;
					} else {
						line = doLineWrap();
					}
				} else {
					setCursorColumn(col);
				}
			}
		}
	}

	private int doLineWrap() {
		int line;
		line = toAbsoluteLine(fCursorLine);
		fTerminal.setWrappedLine(line);
		doNewline();
		line = toAbsoluteLine(fCursorLine);
		setCursorColumn(0);
		return line;
	}

	/**
	 * MUST be called from a synchronized block!
	 */
	private void doNewline() {
		if (fCursorLine == fScrollRegion.getBottomLine())
			scrollUp(1);
		else if (fCursorLine + 1 >= fLines) {
			int h = fTerminal.getHeight();
			fTerminal.addLine();
			if (h != fTerminal.getHeight())
				setCursorLine(fCursorLine + 1);
		} else {
			setCursorLine(fCursorLine + 1);
		}
	}

	@Override
	public void processNewline() {
		synchronized (fTerminal) {
			doNewline();
		}
	}

	private void doReverseLineFeed() {
		if (fCursorLine == fScrollRegion.getTopLine())
			scrollDown(1);
		else
			setCursorLine(fCursorLine - 1);
	}

	@Override
	public void processReverseLineFeed() {
		synchronized (fTerminal) {
			doReverseLineFeed();
		}
	}

	@Override
	public int getCursorLine() {
		synchronized (fTerminal) {
			return fCursorLine;
		}
	}

	@Override
	public int getCursorColumn() {
		synchronized (fTerminal) {
			return fCursorColumn;
		}
	}

	@Override
	public void setCursor(int targetLine, int targetColumn) {
		synchronized (fTerminal) {
			setCursorLine(targetLine);
			setCursorColumn(targetColumn);
		}
	}

	@Override
	public void setCursorColumn(int targetColumn) {
		synchronized (fTerminal) {
			if (targetColumn < 0)
				targetColumn = 0;
			else if (targetColumn >= fColumns)
				targetColumn = fColumns - 1;
			fCursorColumn = targetColumn;
			fWrapPending = false;
			// We make the assumption that nobody is changing the
			// terminal cursor except this class!
			// This assumption gives a huge performance improvement
			fTerminal.setCursorColumn(targetColumn);
		}
	}

	@Override
	public void setCursorLine(int targetLine) {
		synchronized (fTerminal) {
			if (targetLine < 0)
				targetLine = 0;
			else if (targetLine >= fLines)
				targetLine = fLines - 1;
			fCursorLine = targetLine;
			// We make the assumption that nobody is changing the
			// terminal cursor except this class!
			// This assumption gives a huge performance improvement
			fTerminal.setCursorLine(toAbsoluteLine(targetLine));
		}
	}

	@Override
	public int getLines() {
		synchronized (fTerminal) {
			return fLines;
		}
	}

	@Override
	public int getColumns() {
		synchronized (fTerminal) {
			return fColumns;
		}
	}

	@Override
	public void setVT100LineWrapping(boolean enable) {
		fVT100LineWrapping = enable;
	}

	@Override
	public boolean isVT100LineWrapping() {
		return fVT100LineWrapping;
	}

	@Override
	public void setInsertMode(boolean enable) {
		fInsertMode = enable;
	}

	@Override
	public void setScrollRegion(int top, int bottom) {
		if (top < 0 || bottom < 0)
			fScrollRegion = ScrollRegion.FULL_WINDOW;
		else if (top < bottom)
			fScrollRegion = new ScrollRegion(top, bottom);
	}

	@Override
	public void scrollUp(int n) {
		assert n > 0;
		synchronized (fTerminal) {
			int line = toAbsoluteLine(fScrollRegion.getTopLine());
			int nLines = Math.min(fTerminal.getHeight() - line, fScrollRegion.getHeight());
			fTerminal.scroll(line, nLines, -n);
		}
	}

	@Override
	public void scrollDown(int n) {
		assert n > 0;
		synchronized (fTerminal) {
			int line = toAbsoluteLine(fScrollRegion.getTopLine());
			int nLines = Math.min(fTerminal.getHeight() - line, fScrollRegion.getHeight());
			fTerminal.scroll(line, nLines, n);
		}
	}
}
