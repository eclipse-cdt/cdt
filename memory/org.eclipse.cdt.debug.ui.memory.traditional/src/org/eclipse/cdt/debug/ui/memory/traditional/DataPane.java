/*******************************************************************************
 * Copyright (c) 2006-2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.ui.memory.traditional.TraditionalRendering.RegisterDataContainer;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DataPane extends AbstractPane
{
    private Shell fToolTipShell;
    private final Map<BigInteger, List<RegisterDataContainer>> fMapAddressToRegs = Collections.synchronizedMap(new HashMap<BigInteger, List<RegisterDataContainer>>());

    public DataPane(Rendering parent)
    {
        super(parent);
    }

    @Override
	protected String getCellText(MemoryByte bytes[])
    {
        return fRendering.getRadixText(bytes, fRendering.getRadix(), fRendering
            .isTargetLittleEndian());
    }

    @Override
	protected void editCell(BigInteger address, int subCellPosition,
        char character)
    {
        try
        {
            MemoryByte bytes[] = fRendering.getBytes(fCaretAddress, fRendering
                .getBytesPerColumn());
          
            String cellText = getCellText(bytes);
            if(cellText == null)
                return;
            
            StringBuffer cellTextBuffer = new StringBuffer(cellText);
            cellTextBuffer.setCharAt(subCellPosition, character);
            BigInteger value = new BigInteger(cellTextBuffer.toString().trim(),
                fRendering.getNumericRadix(fRendering.getRadix()));
            final boolean isSignedType = fRendering.getRadix() == Rendering.RADIX_DECIMAL_SIGNED;
            final boolean isSigned = isSignedType
                && value.compareTo(BigInteger.valueOf(0)) < 0;

            int bitCount = value.bitLength();
            if(isSignedType)
                bitCount++;
            if(bitCount > fRendering.getBytesPerColumn() * 8)
                return;

            int byteLen = fRendering.getBytesPerColumn();
            byte[] byteData = new byte[byteLen];
            for(int i = 0; i < byteLen; i++)
            {
                int bits = 255;
                if(isSignedType && i == byteLen - 1)
                    bits = 127;

                byteData[i] = (byte) (value.and(BigInteger.valueOf(bits))
                    .intValue() & bits);
                value = value.shiftRight(8);
            }

            if(isSigned)
                byteData[byteLen - 1] |= 128;

            if(!fRendering.isDisplayLittleEndian())
            {
                byte[] byteDataSwapped = new byte[byteData.length];
                for(int i = 0; i < byteData.length; i++)
                    byteDataSwapped[i] = byteData[byteData.length - 1 - i];
                byteData = byteDataSwapped;
            }

            if(byteData.length != bytes.length)
                return;

            TraditionalMemoryByte bytesToSet[] = new TraditionalMemoryByte[bytes.length];

            for(int i = 0; i < byteData.length; i++)
            {
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
        }
        catch(Exception e)
        {
            // do nothing
        }
    }

    @Override
	protected int getCellWidth()
    {
        return getCellCharacterCount() * getCellCharacterWidth()
            + (fRendering.getCellPadding() * 2);
    }

    @Override
	protected int getCellCharacterCount()
    {
        return fRendering.getRadixCharacterCount(fRendering.getRadix(),
            fRendering.getBytesPerColumn());
    }

    private int getAddressableWidth() {
        // derive the number of characters per addressable size e.g. 2 * NumOfOctets
        int addressCharacterCount = fRendering.getAddressableSize() * 2;
        // derive width by multiplying by the size of a character
        return addressCharacterCount * getCellCharacterWidth();
    }

    @Override
	public Point computeSize(int wHint, int hHint)
    {
        return new Point(fRendering.getColumnCount() * getCellWidth()
            + fRendering.getRenderSpacing(), 100);
    }

    private BigInteger getCellAddressAt(int x, int y) throws DebugException
    {
        BigInteger address = fRendering.getViewportStartAddress();

        int col = x / getCellWidth();
        int row = y / getCellHeight();

        if(col >= fRendering.getColumnCount())
            return null;

        address = address.add(BigInteger.valueOf(row
            * fRendering.getColumnCount() * fRendering.getAddressesPerColumn()));

        address = address.add(BigInteger.valueOf(col
            * fRendering.getAddressesPerColumn()));

        return address;
    }

    private BigInteger getAddressAt(int x, int y) {
        // Resolve the first address in the cell
        BigInteger cellBaseAddress;
        try {
            cellBaseAddress = getCellAddressAt(x, y);
        } catch (DebugException e) {
            fRendering.logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_ADDRESS_LOCATION"), e); //$NON-NLS-1$
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
        assert offset > 0;

        // Resolve the number of addresses between hover location and first address in the cell
        int addressableOffset = offset / getAddressableWidth();
        assert addressableOffset < getAddressableOctetsPerColumn();

        return cellBaseAddress.add(BigInteger.valueOf(addressableOffset));
    }

    private Point getAddressLocation(BigInteger regAddress) {
        // Resolve the location of the cell
        Point cellLocation = getCellLocation(regAddress);

        // Resolve the fist address in the cell
        BigInteger baseAddress;
        try {
            baseAddress = getCellAddressAt(cellLocation.x, cellLocation.y);
        } catch (DebugException e) {
            return null;
        }

        if (baseAddress == null) {
            return null;
        }

        int addressSpan = regAddress.subtract(baseAddress).intValue();
        assert addressSpan > 0;
        assert addressSpan < getAddressableOctetsPerColumn();

        // Resolve the horizontal distance from base address to given address
        int xOffset = addressSpan * getAddressableWidth();

        return new Point(cellLocation.x + xOffset, cellLocation.y);
    }

    @Override
	protected Point getCellLocation(BigInteger cellAddress)
    {
        try
        {
            BigInteger address = fRendering.getViewportStartAddress();

            // cell offset from base address in octets
            int cellOffset = cellAddress.subtract(address).intValue();
            cellOffset *= fRendering.getAddressableSize();

            int row = cellOffset
                / (fRendering.getColumnCount() * fRendering.getBytesPerColumn());
            cellOffset -= row * fRendering.getColumnCount()
                * fRendering.getBytesPerColumn();

            int col = cellOffset / fRendering.getBytesPerColumn();

            int x = col * getCellWidth() + fRendering.getCellPadding();
            int y = row * getCellHeight() + fRendering.getCellPadding();

            return new Point(x, y);
        }
        catch(Exception e)
        {
            fRendering
                .logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
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
            fRendering.logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
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
            fRendering.logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e); //$NON-NLS-1$
            return null;
        }
    }

    @Override
	protected void positionCaret(int x, int y)
    {
        try
        {
            BigInteger cellAddress = getCellAddressAt(x, y);
            if(cellAddress != null)
            {
                Point cellPosition = getCellLocation(cellAddress);
                int offset = x - cellPosition.x;
                int subCellCharacterPosition = offset / getCellCharacterWidth();

                if(subCellCharacterPosition == this.getCellCharacterCount())
                {
                    cellAddress = cellAddress.add(BigInteger.valueOf(fRendering
                        .getAddressesPerColumn()));
                    subCellCharacterPosition = 0;
                    cellPosition = getCellLocation(cellAddress);
                }

                fCaret.setLocation(cellPosition.x + subCellCharacterPosition
                    * getCellCharacterWidth(), cellPosition.y);

                this.fCaretAddress = cellAddress;
                this.fSubCellCaretPosition = subCellCharacterPosition;
                setCaretAddress(fCaretAddress);
            }
        }
        catch(Exception e)
        {
            fRendering
                .logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_POSITION_CURSOR"), e); //$NON-NLS-1$
        }
    }

    @Override
	protected BigInteger getViewportAddress(int col, int row)
        throws DebugException
    {
        BigInteger address = fRendering.getViewportStartAddress();
        address = address.add(BigInteger.valueOf((row
            * fRendering.getColumnCount() + col)
            * fRendering.getAddressesPerColumn()));

        return address;
    }

    @Override
	protected void paint(PaintEvent pe)
    {
        super.paint(pe);

        // Allow subclasses to override this method to do their own painting
        doPaintData(pe);

    }

    // Allow subclasses to override this method to do their own painting
    protected void doPaintData(PaintEvent pe)
    {
        GC gc = pe.gc;

        int cellHeight = getCellHeight();
        int cellWidth = getCellWidth();

        int columns = fRendering.getColumnCount();

        try
        {
            BigInteger startAddress = fRendering.getViewportStartAddress();

            for(int i = 0; i < this.getBounds().height / cellHeight; i++)
            {
                for(int col = 0; col < columns; col++)
                {
                    gc.setFont(fRendering.getFont());

                    if (isOdd(col))
                        gc.setForeground(fRendering.getTraditionalRendering().getColorText());
                    else
                        gc.setForeground(fRendering.getTraditionalRendering().getColorTextAlternate());

                    BigInteger cellAddress = startAddress.add(BigInteger.valueOf((i
                        * fRendering.getColumnCount() + col)
                        * fRendering.getAddressesPerColumn()));

                    TraditionalMemoryByte bytes[] = fRendering.getBytes(cellAddress,
                        fRendering.getBytesPerColumn());

                    boolean drawBox = false;

                    if(fRendering.getSelection().isSelected(cellAddress))
                    {
                        gc.setBackground(fRendering.getTraditionalRendering().getColorSelection());
                        gc.fillRectangle(cellWidth * col
                            + fRendering.getCellPadding(), cellHeight * i,
                            cellWidth, cellHeight);

                        gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
                    }
                    else
                    {
                        gc.setBackground(fRendering.getTraditionalRendering().getColorBackground());
                        gc.fillRectangle(cellWidth * col
                            + fRendering.getCellPadding(), cellHeight * i,
                            cellWidth, cellHeight);
                        
                        // Allow subclasses to override this method to do their own coloring
                        applyCustomColor(gc, bytes, col);
                        drawBox = shouldDrawBox(bytes, col);
                    }

                    gc.drawText(getCellText(bytes), cellWidth * col
                        + fRendering.getCellPadding(), cellHeight * i
                        + fRendering.getCellPadding());

                    if(drawBox)
                    {
                        gc.setForeground(fRendering.getTraditionalRendering().getColorTextAlternate());
                        gc.drawRectangle(cellWidth * col, cellHeight * i, cellWidth, cellHeight-1);
                    }

                    BigInteger cellEndAddress = cellAddress.add(BigInteger
                        .valueOf(fRendering.getAddressesPerColumn()));
                    cellEndAddress = cellEndAddress.subtract(BigInteger
                        .valueOf(1));

                    if(fCaretEnabled)
                    {
                        if(cellAddress.compareTo(fCaretAddress) <= 0
                            && cellEndAddress.compareTo(fCaretAddress) >= 0)
                        {
                            int x = cellWidth * col
                                + fRendering.getCellPadding()
                                + fSubCellCaretPosition
                                * this.getCellCharacterWidth();
                            int y = cellHeight * i
                                + fRendering.getCellPadding();
                            fCaret.setLocation(x, y);
                        }
                    }

                    if(fRendering.isDebug())
                        gc.drawRectangle(cellWidth * col
                            + fRendering.getCellPadding(), cellHeight * i
                            + fRendering.getCellPadding(), cellWidth,
                            cellHeight);
                }
            }

            BigInteger endAddress = fRendering.getViewportEndAddress();
            if (fRendering.isExtraInfo()) {
                markAddressesWithAdditionalInfo(pe.display, startAddress, endAddress);
            }
        }
        catch(Exception e)
        {
            fRendering.logError(TraditionalRenderingMessages
                .getString("TraditionalRendering.FAILURE_PAINT"), e); //$NON-NLS-1$
        }

    }

    private void markAddressesWithAdditionalInfo(final Display display, final BigInteger startAddress, final BigInteger endAddress) {
        DsfSession session = fRendering.getTraditionalRendering().fSession;
        if (session == null || session.getExecutor() == null) {
            return;
        }

        // Find out Registers with values that are within the given start and end addresses
        // Then mark these addresses to indicate that there is more information available
        fRendering.getTraditionalRendering().registerValuesRequest(new DataRequestMonitor<RegisterDataContainer[]>(session.getExecutor(), null) {
            @Override
            protected void handleSuccess() {
                RegisterDataContainer[] regData = getData();

                final Map<BigInteger, List<RegisterDataContainer>> valueToRegisters = mapValueToRegisters(regData, startAddress, endAddress);

                // TODO: Remove this temporary tracing call and method
                traceRegisterData(regData, valueToRegisters);

                // Check if there is additional information available
                if (valueToRegisters.size() < 1) {
                    return;
                }

                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (fRendering != null && !fRendering.isDisposed() && fRendering.isVisible() && !isDisposed()) {
                            // Mark addresses with additional info with a rectangle
                            int addressableWidth = getAddressableWidth();
                            assert addressableWidth > 0;

                            // Initialize the dimensions for the rectangle
                            int width = 1;
                            int height = getCellTextHeight() - fRendering.getCellPadding();

                            // Resolve Graphical context
                            GC gc = new GC(DataPane.this);
                            gc.setForeground(fRendering.getTraditionalRendering().getColorChanged());

                            for (BigInteger regAddress : valueToRegisters.keySet()) {
                                if (regAddress.equals(BigInteger.ZERO)) {
                                    // Not tracking registers with value zero
                                    continue;
                                }
                                // Resolve rectangle starting point and start / end row references
                                Point location = getAddressLocation(regAddress);
                                Point firstCellInRow = getRowFirstCellLocation(regAddress);
                                Point lastCellInRow = getRowLastCellLocation(regAddress);

                                // Range registers may indicate the number of addressable units it spans
                                // However multiple registers could be pointing to the same address,
                                // we determine the largest register range in number of addressable units to highlight / enclose
                                int addressUnits = resolveAddressRange(valueToRegisters.get(regAddress));
                                BigInteger endAddress = regAddress.add(BigInteger.valueOf(addressUnits - 1));

                                // End location to the start of next address may change to a different row
                                // So it's best to add the addressable width to the beginning of the last address
                                Point endLocation = getAddressLocation(endAddress);
                                endLocation.x = endLocation.x + addressableWidth;

                                // Resolve the rows index as the selection may span multiple rows
                                int rowsIndex = (endLocation.y - location.y) / getCellHeight();

                                for (int i = 0; i <= rowsIndex; i++) {
                                    Point rowLocation = new Point(firstCellInRow.x, firstCellInRow.y + i * getCellHeight());
                                    if (i == 0) {
                                        // Enclosing range in first row
                                        if (endLocation.y == location.y) {
                                            // End and beginning locations are in the same row
                                            width = endLocation.x - location.x;
                                            gc.drawRectangle(location.x, location.y, width, height);
                                        } else {
                                            // The end cell is in a different row,
                                            // mark from the location to the end of this row
                                            width = lastCellInRow.x + addressableWidth * fRendering.getAddressesPerColumn() - location.x;
                                            drawRectangleOpenEnd(location, width, height, gc);
                                        }
                                    } else if (i > 0 && i < rowsIndex) {
                                        // The marking started before this row and finishes after this row
                                        // we need to mark the whole row with opened ends i.e. two bordering lines top / bottom
                                        width = lastCellInRow.x + addressableWidth * fRendering.getAddressesPerColumn() - firstCellInRow.x;
                                        assert width > 0;
                                        drawParallelLines(rowLocation, width, height, gc);
                                    } else if (i == rowsIndex) {
                                        // The last row to highlight
                                        width = endLocation.x - firstCellInRow.x;
                                        // Draw a colored rectangle around the addressable units
                                        drawRectangleOpenStart(rowLocation, width, height, gc);
                                    }
                                }

                                if (fRendering.isExtraInfo()) {
                                    String registerNames = buildRegistersListString(regAddress, ",");
                                    // Display the associated register information
                                    if (registerNames.length() > 0) {
                                        gc.drawText(registerNames, location.x, location.y + getCellTextHeight());
                                    }
                                }
                            }
                            gc.dispose();
                        }
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
                        gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
                        gc.drawLine(erasep.x, erasep.y, erasep.x, erasep.y + height);
                        gc.setForeground(fRendering.getTraditionalRendering().getColorChanged());
                    }

                    private void drawParallelLines(Point rowLocation, int width, int height, GC gc) {
                        gc.drawLine(rowLocation.x, rowLocation.y, rowLocation.x + width, rowLocation.y);
                        gc.drawLine(rowLocation.x, rowLocation.y + height, rowLocation.x + width, rowLocation.y + height);
                    }
                });
            }

            /**
             * multiple registers with the same address value Determine the longest number of addresses (range) to be highlighted
             */
            private int resolveAddressRange(List<RegisterDataContainer> regData) {
                int maxNoAddressableUnits = 1;
                for (RegisterDataContainer reg : regData) {
                    if (reg.getAddressableRange() > maxNoAddressableUnits) {
                        maxNoAddressableUnits = reg.getAddressableRange();
                    }
                }

                return maxNoAddressableUnits;
            }

            // TODO: Remove this temporary tracing code block
            private void traceRegisterData(RegisterDataContainer[] regData, final Map<BigInteger, List<RegisterDataContainer>> valueToRegisters) {
                System.out.println("Got Register data, count: " + regData.length);
                System.out.println("Register values found in memory range: " + valueToRegisters.size());
                List<BigInteger> valList = new ArrayList<BigInteger>();
                valList.addAll(valueToRegisters.keySet());
                valList.sort(new Comparator<BigInteger>() {
                    @Override
                    public int compare(BigInteger o1, BigInteger o2) {
                        return o1.compareTo(o2);
                    }
                });

                for (BigInteger val : valList) {
                    List<RegisterDataContainer> regNames = valueToRegisters.get(val);
                    System.out.println("\nValue: " + val.toString() + "(0x" + val.toString(16) + ")" + " count: " + regNames.size());
                    System.out.print("\t");

                    for (RegisterDataContainer container : regNames) {
                        System.out.print(container.getName() + ", ");
                    }
                }
            }

        });
    }
    
    private Map<BigInteger, List<RegisterDataContainer>> mapValueToRegisters(RegisterDataContainer[] registers, BigInteger startAddress,
            BigInteger endAddress) {
        Map<BigInteger, List<RegisterDataContainer>> allValuesmap = new HashMap<>(registers.length);
        // build a snap shot of current filtered values needed as a return
        // preventing a concurrent access exception with fMapAddressToRegs
        Map<BigInteger, List<RegisterDataContainer>> filteredValuesmap = new HashMap<>(registers.length);

        synchronized (fMapAddressToRegs) {
            // Refreshing the Address to register data map
            fMapAddressToRegs.clear();
            for (RegisterDataContainer reg : registers) {
                // TODO: REmove me, this is debug code only
                // if (fMapAddressToRegs.size() > 0) {
                // break;
                // }

                List<RegisterDataContainer> containers = allValuesmap.get(reg.getValue());
                if (containers == null) {
                    containers = new ArrayList<RegisterDataContainer>();
                    allValuesmap.put(reg.getValue(), containers);
                }
                containers.add(reg);

                // If the value is within start and end address we want it in the filtered result
                if (reg.getValue().compareTo(startAddress) > -1 && reg.getValue().compareTo(endAddress) < 1) {
                    if (!reg.getValue().equals(BigInteger.ZERO)) {
                        fMapAddressToRegs.put(reg.getValue(), allValuesmap.get(reg.getValue()));
                        filteredValuesmap.put(reg.getValue(), allValuesmap.get(reg.getValue()));
                    }
                }
            }
        }

        return filteredValuesmap;
    }
    
    private boolean isRegisterValue(BigInteger cellAddress) {
        return fMapAddressToRegs.keySet().contains(cellAddress);
    }

	// Allow subclasses to override this method to do their own coloring
    protected void applyCustomColor(GC gc, TraditionalMemoryByte bytes[], int col)
    {
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

        if (anyByteEditing)
        {
            gc.setForeground(ren.getColorEdit());
            gc.setFont(ren.getFontEdit(gc.getFont()));
        }
        else
        {
            boolean isColored = false;
            for (int i = 0; i < fRendering.getHistoryDepth() && !isColored; i++)
            {
                // TODO consider adding finer granularity?
                for (int n = 0; n < bytes.length; n++)
                {
                    if (bytes[n].isChanged(i))
                    {
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

    public void dispose() {
        super.dispose();
        if (fToolTipShell != null) {
            fToolTipShell.dispose();
            fToolTipShell = null;
        }

        fMapAddressToRegs.clear();
    }

    protected MouseTrackAdapter createMouseHoverListener() {
        return new AbstractPaneMouseHoverListener();
    }

    private int getAddressableOctetsPerColumn() {
        // Prevent division by zero
        int addressableSize = (fRendering.getAddressableSize() > 0) ? fRendering.getAddressableSize() : 1;
        return fRendering.getBytesPerColumn() / addressableSize;
    }

    private String buildRegistersListString(BigInteger cellAddress, String separator) {
        List<RegisterDataContainer> registers = fMapAddressToRegs.get(cellAddress);
        StringBuilder sb = new StringBuilder();
        if (registers != null) {
            for (int i = 0; i < registers.size(); i++) {
                sb.append(registers.get(i).getName());
                if (registers.size() > 1 && i < registers.size() - 1) {
                    sb.append(separator);
                }
            }
        }

        return sb.toString();
    }

    class AbstractPaneMouseHoverListener extends MouseTrackAdapter {
        private BigInteger fTooltipAddress = null;
        private final Label fLabelContent;

        AbstractPaneMouseHoverListener() {
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
            if (e.widget == null || !(e.widget instanceof Control) || fToolTipShell == null || fToolTipShell.isDisposed()) {
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
            StringBuilder sb = new StringBuilder("0x").append(subAddress.toString(16)).append("\n");

            // Add additional register information for this address, if available
            if (fRendering.isExtraInfo() && isRegisterValue(subAddress) && !subAddress.equals(BigInteger.ZERO)) {
                String registerNames = buildRegistersListString(subAddress, "\n");
                if (registerNames.length() > 0) {
                    sb.append("Registers:\n").append(registerNames);
                }
            }

            fLabelContent.setText(sb.toString());

            // Setting location of the tool tip
            Rectangle shellBounds = fToolTipShell.getBounds();
            shellBounds.x = hoverPoint.x;
            shellBounds.y = hoverPoint.y + getCellWidth();

            fToolTipShell.setBounds(shellBounds);
            fToolTipShell.pack();

            fToolTipShell.setVisible(true);
        }

        private Label createToolTip() {
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
