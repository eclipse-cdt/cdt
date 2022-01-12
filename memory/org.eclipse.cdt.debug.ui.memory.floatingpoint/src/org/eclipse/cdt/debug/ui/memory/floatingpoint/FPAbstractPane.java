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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;

public abstract class FPAbstractPane extends Canvas {
	protected Rendering fRendering;

	// Selection state

	protected boolean fSelectionStarted = false;
	protected boolean fSelectionInProgress = false;
	protected BigInteger fSelectionStartAddress = null;
	protected int fSelectionStartAddressSubPosition;

	// Caret

	protected Caret fCaret = null;

	// Character may not fall on byte boundary

	protected int fSubCellCaretPosition = 0;
	protected int fOldSubCellCaretPosition = 0;
	protected boolean fCaretEnabled = false;
	protected BigInteger fCaretAddress = null;

	// Storage

	protected int fRowCount = 0;
	protected boolean fPaneVisible = true;

	// Mouse listener class

	class AbstractPaneMouseListener implements MouseListener {
		@Override
		public void mouseUp(MouseEvent me) {
			// Move the caret

			positionCaret(me.x, me.y);

			fCaret.setVisible(true);

			if (fSelectionInProgress && me.button == 1)
				endSelection(me.x, me.y);

			fSelectionInProgress = fSelectionStarted = false;
		}

		// Mouse down click

		@Override
		public void mouseDown(MouseEvent me) {
			// Any click, whether inside this cell or elsewhere, terminates the edit and acts the same as a carriage return would.

			handleCarriageReturn();

			// Switch focus and check for selection

			FPAbstractPane.this.forceFocus();
			positionCaret(me.x, me.y);
			fCaret.setVisible(false);

			if (me.button == 1) {
				// If shift is down and we have an existing start address, append selection

				if ((me.stateMask & SWT.SHIFT) != 0 && fRendering.getSelection().getStart() != null) {
					// If the pane doesn't have a selection start (the selection was created in a different
					// pane) then initialize the pane's selection start to the rendering's selection start.

					if (FPAbstractPane.this.fSelectionStartAddress == null)
						FPAbstractPane.this.fSelectionStartAddress = fRendering.getSelection().getStart();

					FPAbstractPane.this.fSelectionStarted = true;
					FPAbstractPane.this.appendSelection(me.x, me.y);

				} else {
					// Start a new selection

					FPAbstractPane.this.startSelection(me.x, me.y);
				}
			}

		}

		// Double click

		@Override
		public void mouseDoubleClick(MouseEvent me) {
			handleMouseDoubleClick(me);
		}
	}

	// Mouse move listener class

	class AbstractPaneMouseMoveListener implements MouseMoveListener {
		@Override
		public void mouseMove(MouseEvent me) {
			if (fSelectionStarted) {
				fSelectionInProgress = true;
				appendSelection(me.x, me.y);
			}
		}
	}

	// Focus listener class

	class AbstractPaneFocusListener implements FocusListener {
		@Override
		public void focusLost(FocusEvent fe) {
			IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

			if (FPRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY
					.equals(store.getString(FPRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE)))
				fRendering.getViewportCache().clearEditBuffer();
			else
				fRendering.getViewportCache().writeEditBuffer();

			// clear the pane local selection start
			FPAbstractPane.this.fSelectionStartAddress = null;
		}

		@Override
		public void focusGained(FocusEvent fe) {
			// Set the floating point edit mode indicator if the user clicked in the Data Pane; otherwise clear it

			if (FPAbstractPane.this instanceof FPDataPane)
				fRendering.displayEditModeIndicator(true);
			else
				fRendering.displayEditModeIndicator(false);
		}
	}

	// Key listener class

	class AbstractPaneKeyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent ke) {
			fOldSubCellCaretPosition = fSubCellCaretPosition;

			// Shift

			if ((ke.stateMask & SWT.SHIFT) != 0) {
				switch (ke.keyCode) {
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
				case SWT.PAGE_DOWN:
				case SWT.PAGE_UP: {
					if (fRendering.getSelection().getStart() == null)
						fRendering.getSelection().setStart(
								fCaretAddress.add(BigInteger.valueOf(fRendering.getAddressesPerColumn())),
								fCaretAddress);
					break;
				}
				}
			}

			// Arrow, Page, Insert, Escape and standard characters

			if (ke.keyCode == SWT.ARROW_RIGHT) {
				handleRightArrowKey();
			} else if (ke.keyCode == SWT.ARROW_LEFT || ke.keyCode == SWT.BS) {
				handleLeftArrowKey();
			} else if (ke.keyCode == SWT.ARROW_DOWN) {
				handleDownArrowKey();
			} else if (ke.keyCode == SWT.ARROW_UP) {
				handleUpArrowKey();
			} else if (ke.keyCode == SWT.PAGE_DOWN) {
				handlePageDownKey();
			} else if (ke.keyCode == SWT.PAGE_UP) {
				handlePageUpKey();
			} else if (ke.keyCode == SWT.INSERT) {
				handleInsertKey();
			} else if (ke.keyCode == SWT.ESC) {
				fRendering.getViewportCache().clearEditBuffer();
				handleCTRLZ();
			} else if (ke.character == '\r') {
				fRendering.getViewportCache().writeEditBuffer();
				handleCarriageReturn();
			} else if (FPutilities.validEditCharacter(ke.character)) {
				// Check for selection

				if (fRendering.getSelection().hasSelection()) {
					setCaretAddress(fRendering.getSelection().getLow());
					fSubCellCaretPosition = 0;
				}

				// Add the chatacter to the cell

				editCell(fCaretAddress, fSubCellCaretPosition, ke.character);
			}

			// Control

			if ((ke.stateMask & SWT.CTRL) != 0) {
				// CTRL/Z

				if (ke.keyCode == 'z' || ke.keyCode == 'Z')
					handleCTRLZ();
			}

			// Alt

			if ((ke.stateMask & SWT.ALT) != 0) {
				// Future use
			}

			// Shift

			if ((ke.stateMask & SWT.SHIFT) != 0) {
				switch (ke.keyCode) {
				case SWT.ARROW_RIGHT:
				case SWT.ARROW_LEFT:
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
				case SWT.PAGE_DOWN:
				case SWT.PAGE_UP:
					fRendering.getSelection().setEnd(
							fCaretAddress.add(BigInteger.valueOf(fRendering.getAddressesPerColumn())), fCaretAddress);
					break;
				}
			} else if (ke.keyCode != SWT.SHIFT) {
				// If it's a SHIFT key, keep the selection since we may add to it
				fRendering.getSelection().clear();
			}
		}

		@Override
		public void keyReleased(KeyEvent ke) {
			// do nothing
		}
	}

	class AbstractPanePaintListener implements PaintListener {
		@Override
		public void paintControl(PaintEvent pe) {
			FPAbstractPane.this.paint(pe);
		}
	}

	public FPAbstractPane(Rendering rendering) {
		super(rendering, SWT.DOUBLE_BUFFERED);

		fRendering = rendering;

		try {
			fCaretAddress = rendering.getBigBaseAddress();
		} catch (Exception e) {
			// do nothing
		}

		// pref

		this.setFont(fRendering.getFont());

		GC gc = new GC(this);
		gc.setFont(this.getFont());
		fCaret = new Caret(this, SWT.NONE);
		fCaret.setSize(1, gc.stringExtent("|").y); //$NON-NLS-1$
		gc.dispose();

		this.addPaintListener(createPaintListener());
		this.addMouseListener(createMouseListener());
		this.addMouseMoveListener(createMouseMoveListener());
		this.addKeyListener(createKeyListener());
		this.addFocusListener(createFocusListener());
	}

	// Listener methods

	protected MouseListener createMouseListener() {
		return new AbstractPaneMouseListener();
	}

	protected MouseMoveListener createMouseMoveListener() {
		return new AbstractPaneMouseMoveListener();
	}

	protected FocusListener createFocusListener() {
		return new AbstractPaneFocusListener();
	}

	protected KeyListener createKeyListener() {
		return new AbstractPaneKeyListener();
	}

	protected PaintListener createPaintListener() {
		return new AbstractPanePaintListener();
	}

	// Right arrow

	protected void handleRightArrowKey() {
		fSubCellCaretPosition++;

		if (fSubCellCaretPosition >= getCellCharacterCount()) {
			// We've moved beyond the end of the cell: End the edit to the previous cell.

			handleCarriageReturn();

			// Move to the next cell; ensure that caret is within the addressable range

			fSubCellCaretPosition = 0;
			BigInteger newCaretAddress = fCaretAddress
					.add(BigInteger.valueOf(fRendering.getFPDataType().getByteLength()));

			if (newCaretAddress.compareTo(fRendering.getMemoryBlockEndAddress()) > 0)
				fSubCellCaretPosition = getCellCharacterCount();
			else
				setCaretAddress(newCaretAddress);
		}

		updateTheCaret();
		ensureCaretWithinViewport();
	}

	// Left arrow

	protected void handleLeftArrowKey() {
		fSubCellCaretPosition--;

		if (fSubCellCaretPosition < 0) {
			// We've moved beyond the beginning of the cell:  This action ends the edit to the previous cell.

			handleCarriageReturn();

			// Move to the previous cell; ensure that caret is within the addressable range

			fSubCellCaretPosition = getCellCharacterCount() - 1;
			BigInteger newCaretAddress = fCaretAddress
					.subtract(BigInteger.valueOf(fRendering.getFPDataType().getByteLength()));

			if (newCaretAddress.compareTo(fRendering.getMemoryBlockStartAddress()) < 0)
				fSubCellCaretPosition = 0;
			else
				setCaretAddress(newCaretAddress);
		}

		updateTheCaret();
		ensureCaretWithinViewport();
	}

	// Down arrow

	protected void handleDownArrowKey() {
		// We've moved beyond the beginning of the cell:  This action ends the edit to the previous cell.

		handleCarriageReturn();

		// Ensure that caret is within the addressable range

		BigInteger newCaretAddress = fCaretAddress
				.add(BigInteger.valueOf(fRendering.getFPDataType().getByteLength() * fRendering.getColumnCount()));
		setCaretAddress(newCaretAddress);
		updateTheCaret();
		ensureCaretWithinViewport();
	}

	// Up arrow

	protected void handleUpArrowKey() {
		// We've moved beyond the beginning of the cell:  This action ends the edit to the previous cell.

		handleCarriageReturn();

		// Ensure that caret is within the addressable range

		BigInteger newCaretAddress = fCaretAddress
				.subtract(BigInteger.valueOf(fRendering.getFPDataType().getByteLength() * fRendering.getColumnCount()));
		setCaretAddress(newCaretAddress);
		updateTheCaret();
		ensureCaretWithinViewport();
	}

	// Page down

	protected void handlePageDownKey() {
		// We've moved beyond the beginning of the cell:  This action ends the edit to the previous cell.

		handleCarriageReturn();

		// Ensure that caret is within the addressable range

		BigInteger newCaretAddress = fCaretAddress
				.add(BigInteger.valueOf(fRendering.getAddressableCellsPerRow() * (fRendering.getRowCount() - 1)));
		setCaretAddress(newCaretAddress);
		updateTheCaret();
		ensureCaretWithinViewport();
	}

	// Page up

	protected void handlePageUpKey() {
		// We've moved beyond the beginning of the cell:  This action ends the edit to the previous cell.

		handleCarriageReturn();

		// Ensure that caret is within the addressable range

		BigInteger newCaretAddress = fCaretAddress
				.subtract(BigInteger.valueOf(fRendering.getAddressableCellsPerRow() * (fRendering.getRowCount() - 1)));
		setCaretAddress(newCaretAddress);
		updateTheCaret();
		ensureCaretWithinViewport();
	}

	// Insert key

	protected void handleInsertKey() {
		// If focus is in the Data Pane, toggle Insert/Overwrite mode and make sure the cell edit
		// status line indicator is displayed.  Otherwise, make clear the status line indicator.

		if (FPAbstractPane.this instanceof FPDataPane) {
			if (!fRendering.isEditingCell())
				fRendering.setInsertMode(!fRendering.insertMode());
			fRendering.displayEditModeIndicator(true);
		} else
			fRendering.displayEditModeIndicator(false);
	}

	// Double-click

	protected void handleMouseDoubleClick(MouseEvent me) {
		try {
			BigInteger address = getViewportAddress(me.x / getCellWidth(), me.y / getCellHeight());

			fRendering.getSelection().clear();
			fRendering.getSelection().setStart(address.add(BigInteger.valueOf(fRendering.getAddressesPerColumn())),
					address);
			fRendering.getSelection().setEnd(address.add(BigInteger.valueOf(fRendering.getAddressesPerColumn())),
					address);
		} catch (DebugException de) {
			// do nothing
		}
	}

	// Carriage return

	protected void handleCarriageReturn() {
		// If we're not editing a cell or there is no string buffer to use, nothing to do:  Exit edit mode and return.

		if (!fRendering.isEditingCell() || fRendering.getEditBuffer() == null) {
			fRendering.endCellEditing();
			return;
		}

		// Remove all whitespace from the string buffer.

		fRendering.setEditBuffer(new StringBuffer(fRendering.getEditBuffer().toString().trim().replaceAll(" ", ""))); //$NON-NLS-1$ //$NON-NLS-2$

		// Check the string to make sure it's in valid, acceptable form.

		if (FPutilities.isValidFormat(fRendering.getEditBuffer().toString())) {
			// Valid string:  Convert it to a byte array and write the buffer back to memory;
			// a subsequent re-draw/paint converts it to normalized scientific notation.

			fRendering.convertAndUpdateCell(fRendering.getCellEditAddress(), fRendering.getEditBuffer().toString());
		} else {
			// Invalid string: Create the error text and restore the previous value

			String errorText = NLS.bind(FPRenderingMessages.getString("FPRendering.ERROR_FPENTRY_POPUP_TEXT"), //$NON-NLS-1$
					fRendering.getEditBuffer().toString());

			try {
				fRendering.setEditBuffer(new StringBuffer(fRendering.fDataPane.bytesToSciNotation(
						fRendering.getBytes(fCaretAddress, fRendering.getFPDataType().getByteLength()))));
			} catch (DebugException e) {
				e.printStackTrace();
			}

			// Put together the pop-up window components and show the user the error

			String statusString = FPRenderingMessages.getString("FPRendering.ERROR_FPENTRY_STATUS"); //$NON-NLS-1$
			Status status = new Status(IStatus.ERROR, FPRenderingPlugin.getUniqueIdentifier(), statusString);
			FPutilities.popupMessage(FPRenderingMessages.getString("FPRendering.ERROR_FPENTRY_POPUP_TITLE"), errorText, //$NON-NLS-1$
					status);
		}

		// Exit cell-edit mode

		fRendering.endCellEditing();
	}

	// CTRL/Z handling

	protected void handleCTRLZ() {
		// CTRL/Z:  Replace the cell contents with the original value and exit "number edit mode"

		try {
			fRendering.setEditBuffer(new StringBuffer(fRendering.fDataPane.bytesToSciNotation(
					fRendering.getBytes(fCaretAddress, fRendering.getFPDataType().getByteLength()))));
		} catch (DebugException e) {
			e.printStackTrace();
		}

		fRendering.endCellEditing();
	}

	// Other getter/setters

	protected boolean isPaneVisible() {
		return fPaneVisible;
	}

	protected void setPaneVisible(boolean visible) {
		fPaneVisible = visible;
		this.setVisible(visible);
	}

	protected int getNumberOfBytesRepresentedByColumn() {
		return fRendering.getCharsPerColumn();
	}

	protected void editCell(BigInteger cellAddress, int subCellPosition, char character) {
		// Do nothing; overridden in subclass FPDataPane
	}

	// Set the caret address

	protected void setCaretAddress(BigInteger caretAddress) {
		// Ensure that caret is within the addressable range

		if ((caretAddress.compareTo(fRendering.getMemoryBlockStartAddress()) >= 0)
				&& (caretAddress.compareTo(fRendering.getMemoryBlockEndAddress()) <= 0)) {
			fCaretAddress = caretAddress;
		} else if (caretAddress.compareTo(fRendering.getMemoryBlockStartAddress()) < 0) {
			// Calculate offset from the beginning of the row

			int cellOffset = fCaretAddress.subtract(fRendering.getViewportStartAddress()).intValue();
			int row = cellOffset / (fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter());

			cellOffset -= row * fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter();

			fCaretAddress = fRendering.getMemoryBlockStartAddress()
					.add(BigInteger.valueOf(cellOffset / fRendering.getAddressableSize()));
		} else if (caretAddress.compareTo(fRendering.getMemoryBlockEndAddress()) > 0) {
			// Calculate offset from the end of the row

			int cellOffset = fCaretAddress.subtract(fRendering.getViewportEndAddress()).intValue() + 1;
			int row = cellOffset / (fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter());

			cellOffset -= row * fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter();

			fCaretAddress = fRendering.getMemoryBlockEndAddress()
					.add(BigInteger.valueOf(cellOffset / fRendering.getAddressableSize()));
		}

		fRendering.setCaretAddress(fCaretAddress);
	}

	protected boolean isOdd(int value) {
		return (value / 2) * 2 == value;
	}

	protected void updateTheCaret() {
		try {
			if (fCaretAddress != null) {
				Point cellPosition = getCellLocation(fCaretAddress);

				if (cellPosition != null)
					fCaret.setLocation(cellPosition.x + fSubCellCaretPosition * getCellCharacterWidth(),
							cellPosition.y);
			}
		} catch (Exception e) {
			fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_POSITION_CURSOR"), e); //$NON-NLS-1$
		}
	}

	// This method scrolls the viewport to insure that the caret is within the viewable area

	protected void ensureCaretWithinViewport() // TODO getAddressableSize() > 1 ?
	{
		// If the caret is before the viewport start if so, scroll viewport up by several rows

		BigInteger rowCount = BigInteger.valueOf(getRowCount());
		BigInteger rowMemBytes = BigInteger
				.valueOf(fRendering.getFPDataType().getByteLength() * fRendering.getColumnCount());
		BigInteger viewableBytes = rowCount.multiply(rowMemBytes);
		BigInteger viewableEnd = fRendering.getViewportStartAddress().add(viewableBytes);

		if (fCaretAddress.compareTo(fRendering.getViewportStartAddress()) < 0) {
			fRendering.setViewportStartAddress(fRendering.getViewportStartAddress().subtract(rowMemBytes));
			fRendering.ensureViewportAddressDisplayable();
			fRendering.gotoAddress(fRendering.getViewportStartAddress());
		}

		// If the caret is after the viewport end if so, scroll viewport down by appropriate rows

		else if (fCaretAddress.compareTo(viewableEnd) >= 0) {
			fRendering.setViewportStartAddress(fRendering.getViewportStartAddress().add(rowMemBytes));
			fRendering.ensureViewportAddressDisplayable();
			fRendering.gotoAddress(fRendering.getViewportStartAddress());
		}

		fRendering.setCaretAddress(fCaretAddress);
	}

	protected void advanceCursor() {
		handleRightArrowKey();
	}

	protected void positionCaret(int x, int y) {
		// do nothing
	}

	protected int getRowCount() {
		return fRowCount;
	}

	protected void setRowCount() {
		fRowCount = getBounds().height / getCellHeight();
	}

	protected void settingsChanged() {
		fSubCellCaretPosition = 0;
	}

	// Start selection

	protected void startSelection(int x, int y) {
		try {
			BigInteger address = getViewportAddress(x / getCellWidth(), y / getCellHeight());

			if (address != null) {
				this.fSelectionStartAddress = address;
				Point cellPosition = getCellLocation(address);

				if (cellPosition != null) {
					int offset = x - cellPosition.x;
					fSelectionStartAddressSubPosition = offset / getCellCharacterWidth();
				}

				fRendering.getSelection().clear();
				fRendering.getSelection()
						.setStart(address.add(BigInteger.valueOf(fRendering.getFPDataType().getByteLength())), address);
				fSelectionStarted = true;

				new CopyAction(fRendering, DND.SELECTION_CLIPBOARD).run();
			}
		} catch (DebugException e) {
			fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_START_SELECTION"), e); //$NON-NLS-1$
		}
	}

	// End selection

	protected void endSelection(int x, int y) {
		appendSelection(x, y);
		fSelectionInProgress = false;
	}

	protected void appendSelection(int x, int y) {
		try {
			if (this.fSelectionStartAddress == null)
				return;

			BigInteger address = getViewportAddress(x / getCellWidth(), y / getCellHeight());

			if (address.compareTo(this.fSelectionStartAddress) == 0) {
				// Sub-cell selection

				Point cellPosition = getCellLocation(address);
				int offset = x - cellPosition.x;
				int subCellCharacterPosition = offset / getCellCharacterWidth();

				if (Math.abs(subCellCharacterPosition - this.fSelectionStartAddressSubPosition) > this
						.getCellCharacterCount() / 4) {
					fRendering.getSelection().setEnd(
							address.add(BigInteger.valueOf((fRendering.getFPDataType().getByteLength()))), address);
				} else {
					fRendering.getSelection().setEnd(null, null);
				}
			} else {
				fRendering.getSelection()
						.setEnd(address.add(BigInteger.valueOf(fRendering.getFPDataType().getByteLength())), address);
			}

			if (fRendering.getSelection().getEnd() != null) {
				this.fCaretAddress = fRendering.getSelection().getEnd();
				this.fSubCellCaretPosition = 0;
			}

			updateTheCaret();

			new CopyAction(fRendering, DND.SELECTION_CLIPBOARD).run();
		} catch (Exception e) {
			fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_APPEND_SELECTION"), e); //$NON-NLS-1$
		}
	}

	protected void paint(PaintEvent pe) {
		fRowCount = getBounds().height / getCellHeight();

		if (fRendering.isDirty()) {
			fRendering.setDirty(false);
			fRendering.refresh();
		}
	}

	abstract protected BigInteger getViewportAddress(int col, int row) throws DebugException;

	protected Point getCellLocation(BigInteger address) {
		return null;
	}

	protected String getCellText(MemoryByte bytes[]) {
		return null;
	}

	abstract protected int getCellWidth();

	abstract protected int getCellCharacterCount();

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		fCharacterWidth = -1;
		fCellHeight = -1;
		fTextHeight = -1;
	}

	private int fCellHeight = -1; // called often, cache

	protected int getCellHeight() {
		if (fCellHeight == -1) {
			fCellHeight = getCellTextHeight() + (fRendering.getCellPadding() * 2);
		}

		return fCellHeight;
	}

	private int fCharacterWidth = -1; // called often, cache

	protected int getCellCharacterWidth() {
		if (fCharacterWidth == -1) {
			GC gc = new GC(this);
			gc.setFont(fRendering.getFont());
			fCharacterWidth = gc.getAdvanceWidth('F');
			gc.dispose();
		}

		return fCharacterWidth;
	}

	private int fTextHeight = -1; // called often, cache

	protected int getCellTextHeight() {
		if (fTextHeight == -1) {
			GC gc = new GC(this);
			gc.setFont(fRendering.getFont());
			FontMetrics fontMetrics = gc.getFontMetrics();
			fTextHeight = fontMetrics.getHeight();
			gc.dispose();
		}
		return fTextHeight;
	}
}
