/*******************************************************************************
 * Copyright (c) 2021 Fabrizio Iannetti.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tm.terminal.model;

import org.eclipse.swt.graphics.Point;

/**
 * Represents a range of text in the terminal.
 * <p>
 * Used, for example, to store location of active hover
 *
 * @since 5.2
 */
public final class TextRange {
	public final int colStart;
	public final int colEnd;
	public final int rowStart;
	public final int rowEnd;
	public final String text;

	public static final TextRange EMPTY = new TextRange(0, 0, 0, 0, ""); //$NON-NLS-1$

	/**
	 * Constructor.
	 *
	 * @param rowStart start row
	 * @param rowEnd end row
	 * @param colStart start column (exclusive)
	 * @param colEnd end column (exclusive)
	 * @param text text in the range
	 */
	public TextRange(int rowStart, int rowEnd, int colStart, int colEnd, String text) {
		super();
		this.colStart = colStart;
		this.colEnd = colEnd;
		this.rowStart = rowStart;
		this.rowEnd = rowEnd;
		this.text = text;
	}

	public boolean contains(int col, int row) {
		int colStartInrow = row == rowStart ? colStart : 0;
		int colEndInRow = row == rowEnd - 1 ? colEnd : col + 1;
		return col >= colStartInrow && col < colEndInRow && row >= rowStart && row < rowEnd;
	}

	public boolean contains(int line) {
		return line >= rowStart && line < rowEnd;
	}

	/**
	 * Whether the range represents a non-empty (non-zero) amount of text
	 */
	public boolean isEmpty() {
		return !(colEnd > colStart || rowEnd > rowStart);
	}

	public Point getStart() {
		return new Point(colStart, rowStart);
	}

	public Point getEnd() {
		return new Point(colEnd, rowEnd);
	}

	public int getColStart() {
		return colStart;
	}

	public int getColEnd() {
		return colEnd;
	}

	public int getRowStart() {
		return rowStart;
	}

	public int getRowEnd() {
		return rowEnd;
	}

	@Override
	public String toString() {
		return String.format("TextRange (%s,%s)-(%s,%s)-'%s'", //$NON-NLS-1$
				colStart, rowStart, colEnd, rowEnd, text);
	}
}
