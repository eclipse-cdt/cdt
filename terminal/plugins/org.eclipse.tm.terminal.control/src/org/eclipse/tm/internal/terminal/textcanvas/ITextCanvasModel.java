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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;

import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;

public interface ITextCanvasModel {
	void addCellCanvasModelListener(ITextCanvasModelListener listener);

	void removeCellCanvasModelListener(ITextCanvasModelListener listener);

	ITerminalTextDataReadOnly getTerminalText();

	/**
	 * This is is
	 * @param startLine
	 * @param startCol
	 * @param height
	 * @param width
	 */
	void setVisibleRectangle(int startLine, int startCol, int height, int width);

	/**
	 * @return true when the cursor is shown (used for blinking cursors)
	 */
	boolean isCursorOn();

	/**
	 * Show/Hide the cursor.
	 * @param visible
	 */
	void setCursorEnabled(boolean visible);

	/**
	 * @return true if the cursor is shown.
	 */
	boolean isCursorEnabled();

	/**
	 * @return the line of the cursor
	 */
	int getCursorLine();

	/**
	 * @return the column of the cursor
	 */
	int getCursorColumn();

	/**
	 * @return the start of the selection or null if nothing is selected
	 * {@link Point#x} is the column and {@link Point#y} is the line.
	 */
	Point getSelectionStart();

	/**
	 * @return the end of the selection or null if nothing is selected
	 * {@link Point#x} is the column and {@link Point#y} is the line.
	 */
	Point getSelectionEnd();

	Point getSelectionAnchor();

	void setSelectionAnchor(Point anchor);

	/**
	 * Sets the selection. A negative startLine clears the selection.
	 * @param startLine
	 * @param endLine
	 * @param startColumn
	 * @param endColumn
	 */
	void setSelection(int startLine, int endLine, int startColumn, int endColumn);

	/**
	 * @param line
	 * @return true if line is part of the selection
	 */
	boolean hasLineSelection(int line);

	String getSelectedText();

	/**
	 * Expand the hover selection to the word at the given position.
	 *
	 * @param line line
	 * @param col column
	 */
	void expandHoverSelectionAt(int line, int col);

	/**
	 * @param line
	 * @return true if line is part of the hover selection
	 */
	boolean hasHoverSelection(int line);

	/**
	 * Get the text of the current hover selection.
	 *
	 * @return the hover selection text, never null.
	 */
	String getHoverSelectionText();

	/**
	 * Get the start of the hover selection.
	 *
	 * @return the start of the hover selection or null if nothing is selected
	 * {@link Point#x} is the column and {@link Point#y} is the line.
	 * Returns non-null if {@link #hasHoverSelection(int)} returns true
	 */
	Point getHoverSelectionStart();

	/**
	 * Get the end of the hover selection (inclusive).
	 *
	 * @return the end of the hover selection or null if nothing is selected
	 * {@link Point#x} is the column and {@link Point#y} is the line.
	 * Returns non-null if {@link #hasHoverSelection(int)} returns true
	 */
	Point getHoverSelectionEnd();

	/**
	 * Collect and return all text present in the model.
	 *
	 * <p>Individual lines of the returned text are separated by '\n'.
	 *
	 * <p>The method is primarily designed for test automation.
	 *
	 * @since 4.4
	 */
	String getAllText();

}