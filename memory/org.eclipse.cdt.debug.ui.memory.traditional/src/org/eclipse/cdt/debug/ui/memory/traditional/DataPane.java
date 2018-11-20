/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
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
 *     Alvaro Sanchez-leon (Ericsson) - Add hovering support to the traditional memory render (Bug 489505)
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DataPane extends AbstractPane {
	private Shell fToolTipShell;
	private final static String UNICODE_NORTH_WEST_ARROW = "\u2196";

	public DataPane(Rendering parent) {
		super(parent);
	}

	@Override
	protected String getCellText(MemoryByte bytes[]) {
		return fRendering.getRadixText(bytes, fRendering.getRadix(), fRendering.isTargetLittleEndian());
	}

	@Override
	protected void editCell(BigInteger address, int subCellPosition, char character) {
		try {
			MemoryByte bytes[] = fRendering.getBytes(fCaretAddress, fRendering.getBytesPerColumn());

			String cellText = getCellText(bytes);
			if (cellText == null)
				return;

			StringBuilder cellTextBuffer = new StringBuilder(cellText);
			cellTextBuffer.setCharAt(subCellPosition, character);
			BigInteger value = new BigInteger(cellTextBuffer.toString().trim(),
					fRendering.getNumericRadix(fRendering.getRadix()));
			final boolean isSignedType = fRendering.getRadix() == Rendering.RADIX_DECIMAL_SIGNED;
			final boolean isSigned = isSignedType && value.compareTo(BigInteger.valueOf(0)) < 0;

			int bitCount = value.bitLength();
			if (isSignedType)
				bitCount++;
			if (bitCount > fRendering.getBytesPerColumn() * 8)
				return;

			int byteLen = fRendering.getBytesPerColumn();
			byte[] byteData = new byte[byteLen];
			for (int i = 0; i < byteLen; i++) {
				int bits = 255;
				if (isSignedType && i == byteLen - 1)
					bits = 127;

				byteData[i] = (byte) (value.and(BigInteger.valueOf(bits)).intValue() & bits);
				value = value.shiftRight(8);
			}

			if (isSigned)
				byteData[byteLen - 1] |= 128;

			if (!fRendering.isDisplayLittleEndian()) {
				byte[] byteDataSwapped = new byte[byteData.length];
				for (int i = 0; i < byteData.length; i++)
					byteDataSwapped[i] = byteData[byteData.length - 1 - i];
				byteData = byteDataSwapped;
			}

			if (byteData.length != bytes.length)
				return;

			TraditionalMemoryByte bytesToSet[] = new TraditionalMemoryByte[bytes.length];

			for (int i = 0; i < byteData.length; i++) {
				bytesToSet[i] = new TraditionalMemoryByte(byteData[i]);
				bytesToSet[i].setBigEndian(bytes[i].isBigEndian());

				// for embedded, the user wants feedback that the change will be sent to the target,
				// even if does not change the value. eventually, maybe we need another color to
				// indicate change.
				//if(bytes[i].getValue() != byteData[i])
				{
					bytesToSet[i].setEdited(true);
				}
				//else
				{
					//                	if(bytes[i] instanceof TraditionalMemoryByte)
					//                		bytesToSet[i].setEdited(((TraditionalMemoryByte) bytes[i]).isEdited());
					bytesToSet[i].setChanged(bytes[i].isChanged());
				}
			}

			fRendering.getViewportCache().setEditedValue(address, bytesToSet);

			advanceCursor();

			redraw();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Override
	protected int getCellWidth() {
		return getCellCharacterCount() * getCellCharacterWidth() + (fRendering.getCellPadding() * 2);
	}

	/**
	 * @return The width length in pixels needed to draw the characters of an addressable unit
	 */
	private int getAddressableWidth() {
		// derive the number of characters per addressable size e.g. 2 * NumOfOctets for hex representation
		int charsPerOctet = fRendering.getRadixCharacterCount(fRendering.getRadix(), 1);
		int addressCharacterCount = fRendering.getAddressableSize() * charsPerOctet;
		// derive width by multiplying by the size of a character
		return addressCharacterCount * getCellCharacterWidth();
	}

	@Override
	protected int getCellCharacterCount() {
		return fRendering.getRadixCharacterCount(fRendering.getRadix(), fRendering.getBytesPerColumn());
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

		address = address
				.add(BigInteger.valueOf(row * fRendering.getColumnCount() * fRendering.getAddressesPerColumn()));

		address = address.add(BigInteger.valueOf(col * fRendering.getAddressesPerColumn()));

		return address;
	}

	/**
	 * @return The address associated to the hovering location
	 */
	private BigInteger getAddressAt(int x, int y) {
		// Resolve the first address in the cell
		BigInteger cellBaseAddress;
		try {
			cellBaseAddress = getCellAddressAt(x, y);
		} catch (DebugException e) {
			fRendering.logError(
					TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_ADDRESS_LOCATION"), //$NON-NLS-1$
					e);
			return null;
		}

		if (cellBaseAddress == null) {
			return null;
		}

		// Get the start location of the cell
		Point cellPosition = getCellLocation(cellBaseAddress);
		if (cellPosition == null) {
			return null;
		}

		// Resolve the horizontal offset between hover location and
		// the start of the cell
		int offset = x - cellPosition.x;
		if (offset < 0) {
			return null;
		}

		// Resolve the number of addresses between hover location and first address in the cell
		int addressableOffset = offset / getAddressableWidth();
		assert addressableOffset <= getAddressableOctetsPerColumn();

		return cellBaseAddress.add(BigInteger.valueOf(addressableOffset));
	}

	private Point getAddressLocation(BigInteger address) {
		// Resolve the location of the cell
		Point cellLocation = getCellLocation(address);

		// Resolve the first address in the cell
		BigInteger baseAddress;
		try {
			baseAddress = getCellAddressAt(cellLocation.x, cellLocation.y);
		} catch (DebugException e) {
			return null;
		}

		if (baseAddress == null) {
			return null;
		}

		int addressSpan = address.subtract(baseAddress).intValue();
		// Resolve the horizontal distance from base address to given address in octets
		int charsWidth = fRendering.getRadixCharacterCount(fRendering.getRadix(), addressSpan)
				* getCellCharacterWidth();

		return new Point(cellLocation.x + charsWidth, cellLocation.y);
	}

	@Override
	protected Point getCellLocation(BigInteger cellAddress) {
		try {
			BigInteger address = fRendering.getViewportStartAddress();

			// cell offset from base address in octets
			int cellOffset = cellAddress.subtract(address).intValue();
			cellOffset *= fRendering.getAddressableSize();

			int row = cellOffset / (fRendering.getColumnCount() * fRendering.getBytesPerColumn());
			cellOffset -= row * fRendering.getColumnCount() * fRendering.getBytesPerColumn();

			int col = cellOffset / fRendering.getBytesPerColumn();

			int x = col * getCellWidth() + fRendering.getCellPadding();
			int y = row * getCellHeight() + fRendering.getCellPadding();

			return new Point(x, y);
		} catch (Exception e) {
			fRendering.logError(
					TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
			return null;
		}
	}

	private Point getRowFirstCellLocation(BigInteger cellAddress) {
		try {
			BigInteger address = fRendering.getViewportStartAddress();

			// cell offset from base address in octets
			int cellOffset = cellAddress.subtract(address).intValue();
			cellOffset *= fRendering.getAddressableSize();

			int row = cellOffset / (fRendering.getColumnCount() * fRendering.getBytesPerColumn());

			// column zero plus cell padding
			int x = fRendering.getCellPadding();
			int y = row * getCellHeight() + fRendering.getCellPadding();

			return new Point(x, y);
		} catch (Exception e) {
			fRendering.logError(
					TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
			return null;
		}
	}

	private Point getRowLastCellLocation(BigInteger cellAddress) {
		try {
			BigInteger address = fRendering.getViewportStartAddress();

			// cell offset from base address in octets
			int cellOffset = cellAddress.subtract(address).intValue();
			cellOffset *= fRendering.getAddressableSize();

			int row = cellOffset / (fRendering.getColumnCount() * fRendering.getBytesPerColumn());

			int col = fRendering.getColumnCount() - 1;

			int x = col * getCellWidth() + fRendering.getCellPadding();
			int y = row * getCellHeight() + fRendering.getCellPadding();

			return new Point(x, y);
		} catch (Exception e) {
			fRendering.logError(
					TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
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
					cellAddress = cellAddress.add(BigInteger.valueOf(fRendering.getAddressesPerColumn()));
					subCellCharacterPosition = 0;
					cellPosition = getCellLocation(cellAddress);
				}

				fCaret.setLocation(cellPosition.x + subCellCharacterPosition * getCellCharacterWidth(), cellPosition.y);

				this.fCaretAddress = cellAddress;
				this.fSubCellCaretPosition = subCellCharacterPosition;
				setCaretAddress(fCaretAddress);
			}
		} catch (Exception e) {
			fRendering.logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_POSITION_CURSOR"), //$NON-NLS-1$
					e);
		}
	}

	@Override
	protected BigInteger getViewportAddress(int col, int row) throws DebugException {
		BigInteger address = fRendering.getViewportStartAddress();
		address = address.add(
				BigInteger.valueOf((row * fRendering.getColumnCount() + col) * fRendering.getAddressesPerColumn()));

		return address;
	}

	@Override
	protected void paint(PaintEvent pe) {
		super.paint(pe);

		// Allow subclasses to override this method to do their own painting
		doPaintData(pe);

	}

	// Allow subclasses to override this method to do their own painting
	protected void doPaintData(PaintEvent pe) {
		GC gc = pe.gc;

		int cellHeight = getCellHeight();
		int cellWidth = getCellWidth();

		int columns = fRendering.getColumnCount();

		try {
			BigInteger startAddress = fRendering.getViewportStartAddress();

			for (int i = 0; i < fRendering.getRowCount(); i++) {
				for (int col = 0; col < columns; col++) {
					gc.setFont(fRendering.getFont());

					if (isOdd(col))
						gc.setForeground(fRendering.getTraditionalRendering().getColorText());
					else
						gc.setForeground(fRendering.getTraditionalRendering().getColorTextAlternate());

					BigInteger cellAddress = startAddress.add(BigInteger
							.valueOf((i * fRendering.getColumnCount() + col) * fRendering.getAddressesPerColumn()));

					TraditionalMemoryByte bytes[] = fRendering.getBytes(cellAddress, fRendering.getBytesPerColumn());

					boolean drawBox = false;

					if (fRendering.getSelection().isSelected(cellAddress)) {
						gc.setBackground(fRendering.getTraditionalRendering().getColorSelection());
						gc.fillRectangle(cellWidth * col + fRendering.getCellPadding(), cellHeight * i, cellWidth,
								cellHeight);

						gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
					} else {
						gc.setBackground(fRendering.getTraditionalRendering().getColorBackground());
						gc.fillRectangle(cellWidth * col + fRendering.getCellPadding(), cellHeight * i, cellWidth,
								cellHeight);

						// Allow subclasses to override this method to do their own coloring
						applyCustomColor(gc, bytes, col);
						drawBox = shouldDrawBox(bytes, col);
					}

					gc.drawText(getCellText(bytes), cellWidth * col + fRendering.getCellPadding(),
							cellHeight * i + fRendering.getCellPadding());

					if (drawBox) {
						gc.setForeground(fRendering.getTraditionalRendering().getColorTextAlternate());
						gc.drawRectangle(cellWidth * col, cellHeight * i, cellWidth, cellHeight - 1);
					}

					BigInteger cellEndAddress = cellAddress.add(BigInteger.valueOf(fRendering.getAddressesPerColumn()));
					cellEndAddress = cellEndAddress.subtract(BigInteger.valueOf(1));

					if (fCaretEnabled) {
						if (cellAddress.compareTo(fCaretAddress) <= 0 && cellEndAddress.compareTo(fCaretAddress) >= 0) {
							int x = cellWidth * col + fRendering.getCellPadding()
									+ fSubCellCaretPosition * this.getCellCharacterWidth();
							int y = cellHeight * i + fRendering.getCellPadding();
							fCaret.setLocation(x, y);
						}
					}

					if (fRendering.isDebug())
						gc.drawRectangle(cellWidth * col + fRendering.getCellPadding(),
								cellHeight * i + fRendering.getCellPadding(), cellWidth, cellHeight);
				}
			}

			markAddressesWithAdditionalInfo(gc);
		} catch (Exception e) {
			fRendering.logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_PAINT"), e); //$NON-NLS-1$
		}

	}

	private void markAddressesWithAdditionalInfo(GC gc) {
		if (fRendering.isDisposed() || !fRendering.isVisible() || isDisposed()) {
			return;
		}
		final Map<BigInteger, List<IMemoryBlockAddressInfoItem>> addressToInfoItems = fRendering
				.getVisibleValueToAddressInfoItems();

		// Check if there are information items available
		if (addressToInfoItems.size() < 1) {
			return;
		}
		// Prepare to enclose addresses with additional info in a rectangle
		int addressableWidth = getAddressableWidth();
		if (fRendering.getAddressableSize() == fRendering.getBytesPerColumn()) {
			// When the cell size is dimensioned to enclose an addressable size, the width can not be larger
			// than the containing cell, this adjustment is necessary when using radixes where the number of characters
			// does not increase in proportion to the number of bytes e.g. octal, decimal, etc.
			addressableWidth = getAddressableWidth() > getCellWidth() ? getCellWidth() : getAddressableWidth();
		}

		assert addressableWidth > 0;

		// Initialize the dimensions for the rectangle
		int width = 1;
		int leftMargin = 1;
		int rightMargin = leftMargin + 1;
		int lowerMargin = 1;
		int lineWidth = 2;
		int height = getCellTextHeight() - fRendering.getCellPadding() + lowerMargin;

		// Save current GC settings
		Color origColor = gc.getForeground();
		int origLineWidth = gc.getLineWidth();

		gc.setForeground(fRendering.getTraditionalRendering().getColorChanged());

		// Set the thickness of the lines being drawn, i.e. thicker than the default
		gc.setLineWidth(lineWidth);

		// Loop for each address from lowest to highest value
		BigInteger[] sortedAddresses = orderItemsAscending(addressToInfoItems.keySet());
		// Define rectangle margin space
		for (BigInteger startAddress : sortedAddresses) {
			// Resolve rectangle starting point and start / end row references
			Point location = getAddressLocation(startAddress);
			Point firstCellInRow = getRowFirstCellLocation(startAddress);
			Point lastCellInRow = getRowLastCellLocation(startAddress);

			// Mark each item even if they point to the same start address,
			// so the end address is visible on each of them
			List<IMemoryBlockAddressInfoItem> sameStartAddressitems = addressToInfoItems.get(startAddress);
			// Sort items starting in the same address to draw longest first, this will give more visibility to the embedded markings
			IMemoryBlockAddressInfoItem[] sameStartOrderedItems = orderItemsByLengthDescending(sameStartAddressitems);
			for (IMemoryBlockAddressInfoItem item : sameStartOrderedItems) {
				BigInteger addressUnits = item.getRangeInAddressableUnits();

				// Resolve the color for the rectangle
				Color rangeColor = resolveColor(item.getRegionRGBColor());
				if (rangeColor != null) {
					gc.setForeground(rangeColor);
				}

				// The start and end address are part of the length so we need to decrement / adjust by one
				BigInteger endAddress = startAddress.add(addressUnits.subtract(BigInteger.ONE));

				// End location to the start of next address may change to a different row
				// So it's best to add the addressable width to the beginning of the last address
				Point endLocation = getAddressLocation(endAddress);
				endLocation.x = endLocation.x + addressableWidth;

				// Resolve the rows index as the selection may span multiple rows
				int rowsIndex = (endLocation.y - location.y) / getCellHeight();

				for (int i = 0; i <= rowsIndex; i++) {
					Point rowLocation = new Point(firstCellInRow.x, firstCellInRow.y + i * getCellHeight());
					if (!isRowVisible(rowLocation.y)) {
						// No need to draw the portion of lines outside the visible area
						continue;
					}

					if (i == 0) {
						// Enclosing range in first row
						if (endLocation.y == location.y) {
							// End and beginning locations are in the same row
							width = endLocation.x - location.x + rightMargin;
							gc.drawRectangle(location.x - leftMargin, location.y, width, height);
						} else {
							// The end cell is in a different row,
							// mark from the location to the end of this row
							width = lastCellInRow.x + addressableWidth * fRendering.getAddressesPerColumn() - location.x
									+ rightMargin;
							// open ended first row
							location.x -= leftMargin;
							drawRectangleOpenEnd(location, width, height, gc);
						}
					} else if (i > 0 && i < rowsIndex) {
						// The marking started before this row and finishes after this row
						// we need to mark the whole row with opened ends i.e. two bordering lines top / bottom
						width = lastCellInRow.x + addressableWidth * fRendering.getAddressesPerColumn()
								- firstCellInRow.x + rightMargin;
						// parallel lines row
						assert width > 0;
						rowLocation.x -= leftMargin;
						drawParallelLines(rowLocation, width, height, gc);
					} else if (i == rowsIndex) {
						// The last row to highlight
						width = endLocation.x - firstCellInRow.x + rightMargin;
						// Draw a colored rectangle around the addressable units
						rowLocation.x -= leftMargin;
						drawRectangleOpenStart(rowLocation, width, height, gc);
					}
				}

				// Display the associated textual information
				String info = fRendering.buildAddressInfoString(startAddress, ",", false);
				if (info.length() > 0) {
					// Add one character e.g. up arrow, to indicate the start of the data i.e. upper or lower row
					gc.drawText(UNICODE_NORTH_WEST_ARROW + info, location.x, location.y + getCellTextHeight());
				}

				if (rangeColor != null) {
					rangeColor.dispose();
				}
			}
		}
		// Restore the original color
		gc.setForeground(origColor);
		gc.setLineWidth(origLineWidth);
	}

	private IMemoryBlockAddressInfoItem[] orderItemsByLengthDescending(
			List<IMemoryBlockAddressInfoItem> sameStartAddressitems) {

		if (sameStartAddressitems.isEmpty() || sameStartAddressitems.size() == 1) {
			// One item, nothing to sort
			return sameStartAddressitems.toArray(new IMemoryBlockAddressInfoItem[sameStartAddressitems.size()]);
		}

		// Perform a bubble sort
		boolean swapped = true;
		IMemoryBlockAddressInfoItem temp;

		for (int i = 0; i < sameStartAddressitems.size() - 1; i++) {
			swapped = false;
			for (int j = 0; j < sameStartAddressitems.size() - i - 1; j++) {
				// If current index item is smaller then swap to get reverse sorting
				if (sameStartAddressitems.get(j).getRangeInAddressableUnits()
						.compareTo(sameStartAddressitems.get(j + 1).getRangeInAddressableUnits()) < 0) {
					temp = sameStartAddressitems.get(j);
					sameStartAddressitems.set(j, sameStartAddressitems.get(j + 1));
					sameStartAddressitems.set(j + 1, temp);
					swapped = true;
				}
			}

			if (swapped == false) {
				// No swaps were needed, we are done!
				break;
			}
		}

		return sameStartAddressitems.toArray(new IMemoryBlockAddressInfoItem[sameStartAddressitems.size()]);
	}

	private BigInteger[] orderItemsAscending(Set<BigInteger> keySet) {
		List<BigInteger> collection = new ArrayList<>(keySet);
		Collections.sort(collection);
		return collection.toArray(new BigInteger[collection.size()]);
	}

	/**
	 * Convert from int to RGB octets to then create the corresponding Color
	 */
	private Color resolveColor(int intColor) {
		return new Color(getDisplay(), intColor >> 16, (intColor >> 8) & 0xff, intColor & 0xff);
	}

	private boolean isRowVisible(int y) {
		int firstVisibleRow = getAddressLocation(fRendering.getViewportStartAddress()).y;
		int lastVisibleRow = getAddressLocation(fRendering.getViewportEndAddress()).y;
		if (y >= firstVisibleRow && y <= lastVisibleRow) {
			return true;
		}

		return false;
	}

	private void drawRectangleOpenStart(Point location, int width, int height, GC gc) {
		gc.drawRectangle(location.x, location.y, width, height);
		// clear start border
		eraseVerticalLine(location, height, gc);
	}

	private void drawRectangleOpenEnd(Point location, int width, int height, GC gc) {
		gc.drawRectangle(location.x, location.y, width, height);
		// clear end border
		Point erasep = new Point(location.x + width, location.y);
		eraseVerticalLine(erasep, height, gc);
	}

	private void eraseVerticalLine(Point erasep, int height, GC gc) {
		Color currentColor = gc.getForeground();
		gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
		gc.drawLine(erasep.x, erasep.y, erasep.x, erasep.y + height);
		gc.setForeground(currentColor);
	}

	private void drawParallelLines(Point location, int width, int height, GC gc) {
		// NOTE: Writing parallel lines would be preferred, however this did not work in my environment
		//      gc.drawLine(location.x, location.y , location.x + width, location.y);
		//      gc.drawLine(location.x, location.y + height, location.x + width, location.y + height);

		// So we use the work around of writing a rectangle and erase start / end borders
		gc.drawRectangle(location.x, location.y, width, height);
		// clear start border
		eraseVerticalLine(location, height, gc);
		// clear end border
		Point erasep = new Point(location.x + width, location.y);
		eraseVerticalLine(erasep, height, gc);
	}

	// Allow subclasses to override this method to do their own coloring
	protected void applyCustomColor(GC gc, TraditionalMemoryByte bytes[], int col) {
		// TODO consider adding finer granularity?
		boolean anyByteEditing = false;
		for (int n = 0; n < bytes.length && !anyByteEditing; n++)
			if (bytes[n] instanceof TraditionalMemoryByte)
				if (bytes[n].isEdited())
					anyByteEditing = true;

		TraditionalRendering ren = fRendering.getTraditionalRendering();

		if (isOdd(col))
			gc.setForeground(ren.getColorText());
		else
			gc.setForeground(ren.getColorTextAlternate());
		gc.setBackground(ren.getColorBackground());

		if (anyByteEditing) {
			gc.setForeground(ren.getColorEdit());
			gc.setFont(ren.getFontEdit(gc.getFont()));
		} else {
			boolean isColored = false;
			for (int i = 0; i < fRendering.getHistoryDepth() && !isColored; i++) {
				// TODO consider adding finer granularity?
				for (int n = 0; n < bytes.length; n++) {
					if (bytes[n].isChanged(i)) {
						if (i == 0)
							gc.setForeground(ren.getColorsChanged()[i]);
						else
							gc.setBackground(ren.getColorsChanged()[i]);

						gc.setFont(ren.getFontChanged(gc.getFont()));

						isColored = true;
						break;
					}
				}
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (fToolTipShell != null) {
			fToolTipShell.dispose();
			fToolTipShell = null;
		}
	}

	@Override
	protected MouseTrackAdapter createMouseHoverListener() {
		return new DataPaneMouseHoverListener();
	}

	private int getAddressableOctetsPerColumn() {
		// Prevent division by zero
		int addressableSize = (fRendering.getAddressableSize() > 0) ? fRendering.getAddressableSize() : 1;
		return fRendering.getBytesPerColumn() / addressableSize;
	}

	class DataPaneMouseHoverListener extends MouseTrackAdapter {
		private BigInteger fTooltipAddress = null;
		private final Label fLabelContent;

		DataPaneMouseHoverListener() {
			fLabelContent = createToolTip();
		}

		@Override
		public void mouseExit(MouseEvent e) {
			if (fToolTipShell != null && !fToolTipShell.isDisposed()) {
				fToolTipShell.setVisible(false);
				fTooltipAddress = null;
			}
		}

		@Override
		public void mouseHover(MouseEvent e) {
			if (e.widget == null || !(e.widget instanceof Control) || fToolTipShell == null
					|| fToolTipShell.isDisposed()) {
				return;
			}

			Control control = (Control) e.widget;

			// Resolve the address associated to the hovering location
			BigInteger address = getAddressAt(e.x, e.y);

			if (address == null) {
				// Invalid Address at location
				return;
			}

			// Display tooltip if there is a change in hover
			Point hoverPoint = control.toDisplay(new Point(e.x, e.y));
			if (!fToolTipShell.isVisible() || !address.equals(fTooltipAddress)) {
				diplayToolTip(hoverPoint, address);
			} else {
				// Still pointing to the same cell
				return;
			}

			// Keep Track of the latest visited address
			fTooltipAddress = address;
		}

		private void diplayToolTip(Point hoverPoint, BigInteger subAddress) {
			// Show the current hovering address as the first line in the tooltip
			StringBuilder sb = new StringBuilder("0x").append(subAddress.toString(16));

			// Add additional address information, if available
			if (fRendering.hasAddressInfo(subAddress)) {
				String info = fRendering.buildAddressInfoString(subAddress, "\n", true);
				if (info.length() > 0) {
					sb.append("\n").append(info);
				}
			}

			fLabelContent.setText(sb.toString());

			// Setting location of the tool tip
			Rectangle shellBounds = fToolTipShell.getBounds();
			shellBounds.x = hoverPoint.x;
			shellBounds.y = hoverPoint.y + getCellHeight();

			fToolTipShell.setBounds(shellBounds);
			fToolTipShell.pack();

			fToolTipShell.setVisible(true);
		}

		private Label createToolTip() {
			if (fToolTipShell != null) {
				fToolTipShell.dispose();
			}

			fToolTipShell = new Shell(getShell(), SWT.ON_TOP | SWT.RESIZE);
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			gridLayout.marginWidth = 2;
			gridLayout.marginHeight = 0;
			fToolTipShell.setLayout(gridLayout);
			fToolTipShell.setBackground(fToolTipShell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			return createToolTipContent(fToolTipShell);
		}

		private Label createToolTipContent(Composite composite) {
			Label toolTipContent = new Label(composite, SWT.NONE);
			toolTipContent.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			toolTipContent.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			toolTipContent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
			return toolTipContent;
		}
	}
}
