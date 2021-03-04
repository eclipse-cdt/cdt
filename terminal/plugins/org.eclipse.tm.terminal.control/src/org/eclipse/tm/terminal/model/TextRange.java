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
 * @since 5.1
 */
public final class TextRange {
	public final int colStart;
	public final int colEnd;
	public final int rowStart;
	public final int rowEnd;
	public final String text;

	public static final TextRange EMPTY = new TextRange(0, 0, 0, 0, ""); //$NON-NLS-1$

	public TextRange(int rowStart, int rowEnd, int colStart, int colEnd, String text) {
		super();
		this.colStart = colStart;
		this.colEnd = colEnd;
		this.rowStart = rowStart;
		this.rowEnd = rowEnd;
		this.text = text;
	}

	public boolean contains(int col, int row) {
		return col >= colStart && col < colEnd && row >= rowStart && row < rowEnd;
	}

	public boolean isEmpty() {
		return !(colEnd > colStart && rowEnd > rowStart);
	}

	public Point getStart() {
		return new Point(colStart, rowStart);
	}

	public Point getEnd() {
		return new Point(colEnd, rowEnd);
	}
}
