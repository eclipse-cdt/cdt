/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

public class LineSegment {
	private final String fText;
	private final int fCol;
	private final TerminalStyle fStyle;

	/**
	 * @since 5.0
	 */
	public LineSegment(int col, String text, TerminalStyle style) {
		fCol = col;
		fText = text;
		fStyle = style;
	}

	/**
	 * @since 5.0
	 */
	public TerminalStyle getStyle() {
		return fStyle;
	}

	public String getText() {
		return fText;
	}

	public int getColumn() {
		return fCol;
	}

	@Override
	public String toString() {
		return "LineSegment(" + fCol + ", \"" + fText + "\"," + fStyle + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}