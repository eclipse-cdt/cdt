/*******************************************************************************
 * Copyright (c) 2006-2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Alvaro Sanchez-leon (Ericsson) - Add hovering support to the traditional memory render (Bug 489505)
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class DataPane extends AbstractPane
{
    private Shell fToolTipShell;
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

    /**
     * @return The width length in pixels needed to draw the characters of an addressable unit 
     */
    private int getAddressableWidth() {
        // derive the number of characters per addressable size e.g. 2 * NumOfOctets
        int charsPerOctet = 2;
        int addressCharacterCount = fRendering.getAddressableSize() * charsPerOctet;
        // derive width by multiplying by the size of a character
        return addressCharacterCount * getCellCharacterWidth();
    }

    @Override
	protected int getCellCharacterCount()
    {
        return fRendering.getRadixCharacterCount(fRendering.getRadix(),
            fRendering.getBytesPerColumn());
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
    
    /**
     * @return The address associated to the hovering location
     */
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
        if (offset < 0) {
        	return null;
        }

        // Resolve the number of addresses between hover location and first address in the cell
        int addressableOffset = offset / getAddressableWidth();
        assert addressableOffset <= getAddressableOctetsPerColumn();

        return cellBaseAddress.add(BigInteger.valueOf(addressableOffset));
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
        }
        catch(Exception e)
        {
            fRendering.logError(TraditionalRenderingMessages
                .getString("TraditionalRendering.FAILURE_PAINT"), e); //$NON-NLS-1$
        }

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
