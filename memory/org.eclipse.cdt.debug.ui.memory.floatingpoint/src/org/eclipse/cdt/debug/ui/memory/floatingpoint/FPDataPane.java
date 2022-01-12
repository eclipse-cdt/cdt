/*******************************************************************************
 * Copyright (c) 2006, 2010, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class FPDataPane extends FPAbstractPane {
	public FPDataPane(Rendering parent) {
		super(parent);
	}

	// Returns the representation of the given memory bytes as a scientific notation string

	protected String bytesToSciNotation(FPMemoryByte[] bytes) {
		return fRendering.sciNotationString(bytes, fRendering.getFPDataType(), fRendering.isDisplayLittleEndian());
	}

	// Cell editing:  Accumulate text entry characters, replacing or inserting them at the current cursor position

	@Override
	protected void editCell(BigInteger cellAddress, int subCellPosition, char character) {
		try {
			// Switch to cell edit mode if not we're not currently in the middle of an edit

			if (!fRendering.isEditingCell()) {
				// Calculate the memory address from the cell address

				BigInteger vpStart = fRendering.getViewportStartAddress();
				BigInteger colChars = BigInteger.valueOf(fRendering.getCharsPerColumn());
				BigInteger dtBytes = BigInteger.valueOf(fRendering.getFPDataType().getByteLength());
				BigInteger memoryAddress = vpStart
						.add(((cellAddress.subtract(vpStart)).divide(colChars)).multiply(dtBytes));

				if (!fRendering.insertMode()) {
					// Overstrike/overwrite mode: Enter cell-edit mode; start with the current cell contents.

					fRendering.startCellEditing(cellAddress, memoryAddress, bytesToSciNotation(
							fRendering.getBytes(cellAddress, fRendering.getFPDataType().getByteLength())));
				} else {
					// Insert/Replace mode:  Clear the current cell contents; start
					// with a blank string.  Move the caret to the start of the cell.

					fRendering.startCellEditing(cellAddress, memoryAddress,
							FPutilities.fillString(fRendering.getCharsPerColumn(), ' '));

					subCellPosition = 0;
					Point cellCoordinates = getCellLocation(cellAddress);
					positionCaret(cellCoordinates.x, cellCoordinates.y);
					fCaret.setVisible(true);
				}
			}

			// Validate the current string:  Only one decimal point and exponent character ('e' or 'E') is allowed.  Number
			// signs may be present up to two times - once for the number and once for the exponent, and may occur at the
			// very beginning of a number or immediately after the exponent character.  If an entry violates a rule, do not
			// echo the character (implicitly, through replacement and re-paint) and do not advance the cursor.

			String cellString = fRendering.getEditBuffer().toString().toLowerCase();

			// Check to see if a second decimal point or exponent was entered

			if ((character == '.' && FPutilities.countMatches(cellString, ".") > 0 //$NON-NLS-1$
					&& cellString.indexOf('.') != subCellPosition)
					|| (character == 'e' && FPutilities.countMatches(cellString, "e") > 0 //$NON-NLS-1$
							&& cellString.indexOf('e') != subCellPosition))
				return;

			// Check to see if more than two number signs have been entered

			if ((character == '+' && FPutilities.countMatches(cellString, "+") > 1) || //$NON-NLS-1$
					(character == '-' && FPutilities.countMatches(cellString, "-") > 1)) //$NON-NLS-1$
				return;

			// We've passed the previous checks:  Make the character substitution, if possible.
			// Advance the cursor as long as we're inside the column.  Re-paint the view.

			if (subCellPosition < fRendering.getEditBuffer().length())
				fRendering.getEditBuffer().setCharAt(subCellPosition, character);

			if (subCellPosition < fRendering.getCharsPerColumn())
				advanceCursor();

			redraw();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Returns cell width, in pixels

	@Override
	protected int getCellWidth() {
		return getCellCharacterCount() * getCellCharacterWidth() + (fRendering.getCellPadding() * 2);
	}

	@Override
	protected int getCellCharacterCount() {
		return fRendering.getCharsPerColumn();
	}

	@Override
	public Point computeSize(int wHint, int hHint) {
		return new Point(fRendering.getColumnCount() * getCellWidth() + fRendering.getRenderSpacing(), 100);
	}

	private BigInteger getCellAddressAt(int x, int y) throws DebugException {
		BigInteger address = fRendering.getViewportStartAddress();

		int col = x / getCellWidth();
		int row = y / getCellHeight();

		if (col >= fRendering.getColumnCount())
			return null;

		address = address.add(
				BigInteger.valueOf(row * fRendering.getColumnCount() * fRendering.getFPDataType().getByteLength()));
		address = address.add(BigInteger.valueOf(col * fRendering.getFPDataType().getByteLength()));

		return address;
	}

	// Return a Point representing the cell address

	@Override
	protected Point getCellLocation(BigInteger cellAddress) {
		try {
			BigInteger address = fRendering.getViewportStartAddress();

			int cellOffset = cellAddress.subtract(address).intValue();
			cellOffset *= fRendering.getAddressableSize();

			int row = cellOffset / (fRendering.getColumnCount() * fRendering.getFPDataType().getByteLength());
			cellOffset -= row * fRendering.getColumnCount() * fRendering.getFPDataType().getByteLength();
			int col = cellOffset / fRendering.getFPDataType().getByteLength();

			int x = col * getCellWidth() + fRendering.getCellPadding();
			int y = row * getCellHeight() + fRendering.getCellPadding();

			return new Point(x, y);
		} catch (Exception e) {
			fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	protected void positionCaret(int x, int y) {
		try {
			BigInteger cellAddress = getCellAddressAt(x, y);

			if (cellAddress != null) {
				Point cellPosition = getCellLocation(cellAddress);
				int offset = x - cellPosition.x;
				int subCellCharacterPosition = offset / getCellCharacterWidth();

				if (subCellCharacterPosition == this.getCellCharacterCount()) {
					cellAddress = cellAddress.add(BigInteger.valueOf(fRendering.getFPDataType().getByteLength()));
					subCellCharacterPosition = 0;
					cellPosition = getCellLocation(cellAddress);
				}

				fCaret.setLocation(cellPosition.x + subCellCharacterPosition * getCellCharacterWidth(), cellPosition.y);

				this.fCaretAddress = cellAddress;
				this.fSubCellCaretPosition = subCellCharacterPosition;
				setCaretAddress(fCaretAddress);
			}
		} catch (Exception e) {
			fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_POSITION_CURSOR"), e); //$NON-NLS-1$
		}
	}

	@Override
	protected BigInteger getViewportAddress(int col, int row) throws DebugException {
		BigInteger address = fRendering.getViewportStartAddress();
		address = address.add(BigInteger
				.valueOf((row * fRendering.getColumnCount() + col) * fRendering.getFPDataType().getByteLength()));

		return address;
	}

	@Override
	protected void paint(PaintEvent pe) {
		super.paint(pe);
		// Allow subclasses to override this method to do their own painting
		doPaintData(pe);
	}

	// Display text in the cell

	protected void doPaintData(PaintEvent pe) {
		GC gc = pe.gc;
		gc.setFont(fRendering.getFont());

		int cellHeight = getCellHeight();
		int cellWidth = getCellWidth();
		int boundsHeight = this.getBounds().height;
		int columns = fRendering.getColumnCount();

		int cellX = 0;
		int cellY = 0;

		String displayString = FPutilities.fillString(fRendering.getCharsPerColumn(), '.');

		try {
			BigInteger vpStart = fRendering.getViewportStartAddress();
			BigInteger cellStartAddr = vpStart;
			BigInteger memoryAddr = vpStart;
			BigInteger cellEndAddr;

			for (int row = 0; row < boundsHeight / cellHeight; row++) {
				for (int column = 0; column < columns; column++) {
					// Set alternating colors for every other column and display the text
					// FIXME: There is duplicate code in applyCustomColor() in this class

					if (isOdd(column))
						gc.setForeground(fRendering.getFPRendering().getColorText());
					else
						gc.setForeground(fRendering.getFPRendering().getColorTextAlternate());

					// Calculate the cell starting address and X/Y coordinates

					cellStartAddr = vpStart.add(BigInteger.valueOf(
							(row * fRendering.getColumnCount() + column) * fRendering.getFPDataType().getByteLength()));
					cellEndAddr = cellStartAddr.add(
							BigInteger.valueOf(fRendering.getFPDataType().getByteLength()).subtract(BigInteger.ONE));

					cellX = (cellWidth * column) + fRendering.getCellPadding();
					cellY = (cellHeight * row) + fRendering.getCellPadding();

					// Cell editing:  If we're in edit mode, change the cell color and then set the
					// edit buffer as the string to display.  Otherwise, just use the memory contents.

					if (fRendering.isEditingCell() && cellStartAddr.equals(fRendering.getCellEditAddress())) {
						gc.setForeground(fRendering.getFPRendering().getColorEdit());
						FPMemoryByte[] memoryBytes = fRendering.getBytes(cellStartAddr,
								fRendering.getFPDataType().getByteLength());
						for (FPMemoryByte memoryByte : memoryBytes)
							memoryByte.setEdited(true);
						applyCustomColor(gc, memoryBytes, column);
						displayString = fRendering.getEditBuffer().toString();
					} else
						displayString = bytesToSciNotation(
								fRendering.getBytes(memoryAddr, fRendering.getFPDataType().getByteLength()));

					// Cell selection

					if (fRendering.getSelection().isSelected(cellStartAddr)) {
						gc.setBackground(fRendering.getFPRendering().getColorSelection());
						gc.fillRectangle(cellX, row * cellHeight, cellWidth, cellHeight);
						gc.setForeground(fRendering.getFPRendering().getColorBackground());
					} else {
						gc.setBackground(fRendering.getFPRendering().getColorBackground());
						gc.fillRectangle(cellX, row * cellHeight, cellWidth, cellHeight);
						// Allow subclasses to override this method to do their own coloring
						applyCustomColor(gc,
								fRendering.getBytes(cellStartAddr, fRendering.getFPDataType().getByteLength()), column);
					}

					gc.drawText(displayString, cellX, cellY);

					// Move the caret if appropriate

					if (fCaretEnabled) {
						if (cellStartAddr.compareTo(fCaretAddress) <= 0 && cellEndAddr.compareTo(fCaretAddress) >= 0) {
							int x = cellWidth * column + fRendering.getCellPadding()
									+ fSubCellCaretPosition * this.getCellCharacterWidth();
							int y = cellHeight * row + fRendering.getCellPadding();
							fCaret.setLocation(x, y);
						}
					}

					// For debugging

					if (fRendering.isDebug())
						gc.drawRectangle(cellX, cellY, cellWidth, cellHeight);

					// Increment the memory address by the length of the data type

					memoryAddr = memoryAddr.add(BigInteger.valueOf(fRendering.getFPDataType().getByteLength()));
				}
			}
		} catch (Exception e) {
			fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_PAINT"), e); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	// Allow subclasses to override this method to do their own coloring

	protected void applyCustomColor(GC gc, FPMemoryByte bytes[], int col) {
		// Check to see if any byte has been changed and whether we're actually in edit mode

		boolean anyByteEditing = false;

		for (int n = 0; n < bytes.length && !anyByteEditing; n++)
			if (bytes[n].isEdited())
				anyByteEditing = true;

		anyByteEditing = anyByteEditing && fRendering.isEditingCell();

		// Even/odd column coloring

		if (isOdd(col))
			gc.setForeground(fRendering.getFPRendering().getColorText());
		else
			gc.setForeground(fRendering.getFPRendering().getColorTextAlternate());

		// Background

		gc.setBackground(fRendering.getFPRendering().getColorBackground());

		if (anyByteEditing) {
			gc.setForeground(fRendering.getFPRendering().getColorEdit());
		} else {
			boolean isColored = false;

			for (int index = 0; index < fRendering.getHistoryDepth() && !isColored; index++) {
				// TODO consider adding finer granularity?

				for (int n = 0; n < bytes.length; n++) {
					if (bytes[n].isChanged(index)) {
						if (index == 0)
							gc.setForeground(fRendering.getFPRendering().getColorsChanged()[index]);
						else
							gc.setBackground(fRendering.getFPRendering().getColorsChanged()[index]);

						isColored = true;
						break;
					}
				}
			}
		}
	}

	// Draw a box around the specified cell

	public void highCellBox(BigInteger memoryAddress) {
		//        if (fRendering.isDebug())
		//            gc.drawRectangle(cellX, cellY, cellWidth, cellHeight);
	}

	// Clear the box around the specified cell

	public void clearCellBox(BigInteger memoryAddress) {

	}
}
