/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
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

import org.eclipse.debug.core.DebugException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class AddressPane extends AbstractPane
{
    public AddressPane(Rendering parent)
    {
        super(parent);
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
	protected void appendSelection(int x, int y)
    {
        try
        {
            if(this.fSelectionStartAddress == null)
            	return;

            BigInteger address = getViewportAddress(x / getCellWidth(), y
                / getCellHeight());

            if(address.compareTo(this.fSelectionStartAddress) == 0)
            {
                fRendering.getSelection().setEnd(null, null);
            }
            else
            {
                fRendering.getSelection().setEnd(address.add(BigInteger
                    .valueOf(fRendering.getAddressesPerColumn() * fRendering.getColumnCount())), address);
            }
        }
        catch(DebugException e)
        {
            fRendering
                .logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_APPEND_SELECTION"), e); //$NON-NLS-1$
        }
    }

    @Override
	public Point computeSize(int wHint, int hHint)
    {
        return new Point(getCellWidth() + fRendering.getRenderSpacing(), 100);
    }

    @Override
	protected int getCellCharacterCount()
    {
        // two characters per byte of hex address
        
        return fRendering.getAddressBytes() * 2 
            + 2; // 0x
    }

    @Override
	protected int getCellWidth()
    {     
    	GC gc = new GC(this);
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < getCellCharacterCount(); i++)
        	buf.append("0");
        int width = gc.textExtent(buf.toString()).x;
        gc.dispose();
        return width;
    }

    private int getColumnCount()
    {
        return 0;
    }

    private BigInteger getCellAddressAt(int x, int y) throws DebugException
    {
        BigInteger address = fRendering.getViewportStartAddress();

        int col = x / getCellWidth();
        int row = y / getCellHeight();

        if(col > getColumnCount())
            return null;

        address = address.add(BigInteger.valueOf(row
            * fRendering.getColumnCount() * fRendering.getAddressesPerColumn()
            / fRendering.getBytesPerCharacter()));

        address = address.add(BigInteger.valueOf(col
            * fRendering.getAddressesPerColumn()));

        return address;
    }

    @Override
	protected Point getCellLocation(BigInteger cellAddress)
    {
        try
        {
            BigInteger address = fRendering.getViewportStartAddress();

            int cellOffset = cellAddress.subtract(address).intValue();
            
            cellOffset *= fRendering.getAddressableSize();

            if(fRendering.getColumnCount() == 0) // avoid divide by zero
            	return new Point(0,0);
            
            int row = cellOffset
                / (fRendering.getColumnCount() * fRendering.getBytesPerColumn() / fRendering
                    .getBytesPerCharacter());
     
            cellOffset -= row * fRendering.getColumnCount()
                * fRendering.getBytesPerColumn()
                / fRendering.getBytesPerCharacter();

            int col = cellOffset / fRendering.getBytesPerColumn()
                / fRendering.getBytesPerCharacter();

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
	protected int getNumberOfBytesRepresentedByColumn()
    {
        return fRendering.getBytesPerRow();
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
                int x2 = offset / getCellCharacterWidth();

                if(x2 >= this.getCellCharacterCount())
                {
                    cellAddress = cellAddress.add(BigInteger.valueOf(this
                        .getNumberOfBytesRepresentedByColumn()));
                    x2 = 0;
                    cellPosition = getCellLocation(cellAddress);
                }

                fCaret.setLocation(cellPosition.x + x2
                    * getCellCharacterWidth(), cellPosition.y);

                this.fCaretAddress = cellAddress;
                this.fSubCellCaretPosition = x2;
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
	protected void paint(PaintEvent pe)
    {
        super.paint(pe);

        GC gc = pe.gc;

        FontMetrics fontMetrics = gc.getFontMetrics();
        int textHeight = fontMetrics.getHeight();
        int cellHeight = textHeight + (fRendering.getCellPadding() * 2);

        try
        {
            BigInteger start = fRendering.getViewportStartAddress();

            for(int i = 0; i < this.getBounds().height / cellHeight; i++)
            {
                gc.setForeground(fRendering.getTraditionalRendering().getColorText());
                BigInteger lineAddress = start.add(BigInteger.valueOf(i
                    * fRendering.getColumnCount()
                    * fRendering.getAddressesPerColumn()));

                if(fRendering.getSelection().isSelected(lineAddress))
                {
                    gc.setBackground(fRendering.getTraditionalRendering().getColorSelection());
                    gc.fillRectangle(fRendering.getCellPadding() * 2,
                        cellHeight * i, getCellWidth(), cellHeight);

                    gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
                }
                else
                {
                    gc.setBackground(fRendering.getTraditionalRendering().getColorBackground());

                    gc.fillRectangle(fRendering.getCellPadding() * 2,
                        cellHeight * i, getCellWidth(), cellHeight);

                    // Allow subclass to override this method to do its own coloring
                    applyCustomColor(gc);
                }

                gc.drawText(fRendering.getAddressString(lineAddress),
                    fRendering.getCellPadding() * 2, cellHeight * i
                        + fRendering.getCellPadding());
            }
        }
        catch(Exception e)
        {
            fRendering.logError(TraditionalRenderingMessages
                .getString("TraditionalRendering.FAILURE_PAINT"), e); //$NON-NLS-1$
        }
    }

    // Allow subclass to override this method to do its own coloring
    protected void applyCustomColor(GC gc)
    {
        gc.setForeground(fRendering.getTraditionalRendering().getColorText());
    }
}
