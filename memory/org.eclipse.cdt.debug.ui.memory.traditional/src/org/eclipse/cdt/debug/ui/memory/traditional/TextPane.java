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
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class TextPane extends AbstractPane
{
    public TextPane(Rendering parent)
    {
        super(parent);
    }

    @Override
	protected int getCellCharacterCount()
    {
        return fRendering.getBytesPerColumn()
            / fRendering.getBytesPerCharacter();
    }

    @Override
	protected String getCellText(MemoryByte bytes[])
    {
        return fRendering.formatText(bytes, fRendering
            .isTargetLittleEndian(), fRendering.getTextMode());
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

            byte byteData[] = cellTextBuffer.toString().getBytes(fRendering.getCharacterSet(fRendering.getTextMode()));
            if(byteData.length != bytes.length)
                return;

            TraditionalMemoryByte bytesToSet[] = new TraditionalMemoryByte[bytes.length];

            for(int i = 0; i < byteData.length; i++)
            {
                bytesToSet[i] = new TraditionalMemoryByte(byteData[i]);

                if(bytes[i].getValue() != byteData[i])
                {
                    bytesToSet[i].setEdited(true);
                }
                else
                {
                    bytesToSet[i].setChanged(bytes[i].isChanged());
                }
            }

            fRendering.getViewportCache().setEditedValue(address, bytesToSet);

            advanceCursor();

            redraw();
        }
        catch(Exception e)
        {
            // this is ok
        }
    }

    @Override
	protected int getCellWidth()
    {
        GC gc = new GC(this);
        gc.setFont(fRendering.getFont());
        int width = gc.getAdvanceWidth('F');
        gc.dispose();

        return fRendering.getBytesPerColumn()
            / fRendering.getBytesPerCharacter() * width;
    }

    @Override
	public Point computeSize(int wHint, int hHint)
    {
        return new Point(fRendering.getColumnCount() * getCellWidth()
            + fRendering.getRenderSpacing(), 100);

    }

    @Override
	protected Point getCellLocation(BigInteger cellAddress)
    {
        try
        {
            BigInteger address = fRendering.getViewportStartAddress();

            int cellOffset = cellAddress.subtract(address).intValue();
            cellOffset *= fRendering.getAddressableSize();

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

    private BigInteger getCellAddressAt(int x, int y) throws DebugException
    {
        BigInteger address = fRendering.getViewportStartAddress();

        int col = x / getCellWidth();
        int row = y / getCellHeight();

        if(col >= fRendering.getColumnCount())
            return null;

        address = address.add(BigInteger.valueOf(row
            * fRendering.getColumnCount() * fRendering.getAddressesPerColumn()
            / fRendering.getBytesPerCharacter()));

        address = address.add(BigInteger.valueOf(col
            * fRendering.getAddressesPerColumn()));

        return address;
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

                if(x2 == this.getCellCharacterCount())
                {
                    cellAddress = cellAddress.add(BigInteger.valueOf(fRendering
                        .getAddressesPerColumn()));
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
	protected BigInteger getViewportAddress(int col, int row)
        throws DebugException
    {
        BigInteger address = fRendering.getViewportStartAddress();
        address = address.add(BigInteger.valueOf((row
            * fRendering.getColumnCount() + col)
            * fRendering.getAddressesPerColumn()
            / fRendering.getBytesPerCharacter()));

        return address;
    }

    @Override
	protected void paint(PaintEvent pe)
    {
        super.paint(pe);

        GC gc = pe.gc;
        gc.setFont(fRendering.getFont());

        int cellHeight = getCellHeight();
        int cellWidth = getCellWidth();

        final int columns = fRendering.getColumnCount();

        final boolean isLittleEndian = fRendering.isTargetLittleEndian();
        
        gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
        gc.fillRectangle(columns * cellWidth, 0, this.getBounds().width, this
            .getBounds().height);

        try
        {
            BigInteger start = fRendering.getViewportStartAddress();

            for(int i = 0; i < this.getBounds().height / cellHeight; i++)
            {
                for(int col = 0; col < columns; col++)
                {
                	if(isOdd(col))
                		gc.setForeground(fRendering.getTraditionalRendering().getColorText());
                	else
                		gc.setForeground(fRendering.getTraditionalRendering().getColorTextAlternate());

                    BigInteger cellAddress = start.add(BigInteger.valueOf((i
                        * columns + col)
                        * fRendering.getAddressesPerColumn()));

                    TraditionalMemoryByte bytes[] = fRendering.getBytes(cellAddress,
                        fRendering.getBytesPerColumn());

                    if(fRendering.getSelection().isSelected(cellAddress))
                    {
                        gc.setBackground(fRendering.getTraditionalRendering().getColorSelection());
                        gc.fillRectangle(cellWidth * col, cellHeight * i,
                            cellWidth, cellHeight);

                        gc.setForeground(fRendering.getTraditionalRendering().getColorBackground());
                    }
                    else
                    {
                        gc.setBackground(fRendering.getTraditionalRendering().getColorBackground());
                        gc.fillRectangle(cellWidth * col, cellHeight * i,
                            cellWidth, cellHeight);

                        applyCustomColor(gc, bytes, col);
                    }

                    gc.drawText(fRendering.formatText(bytes,
                        isLittleEndian, fRendering.getTextMode()), cellWidth * col, cellHeight * i
                        + fRendering.getCellPadding());

                    if(fRendering.isDebug())
                        gc.drawRectangle(cellWidth * col, cellHeight * i
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
        for(int n = 0; n < bytes.length && !anyByteEditing; n++)
        	if(bytes[n] instanceof TraditionalMemoryByte)
        		if(bytes[n].isEdited())
        			anyByteEditing = true;
         
         if(isOdd(col))
     		gc.setForeground(fRendering.getTraditionalRendering().getColorText());
     	else
     		gc.setForeground(fRendering.getTraditionalRendering().getColorTextAlternate());
         gc.setBackground(fRendering.getTraditionalRendering().getColorBackground());
         
         if(anyByteEditing)
         {
         	gc.setForeground(fRendering.getTraditionalRendering().getColorEdit());
         }
         else
         {
         	boolean isColored = false;
         	for(int i = 0; i < fRendering.getHistoryDepth() && !isColored; i++)
         	{
 	        	// TODO consider adding finer granularity?
 	            for(int n = 0; n < bytes.length; n++)
 	            {
 	                if(bytes[n].isChanged(i))
 	                {
 	                	if(i == 0)
 	                		gc.setForeground(fRendering.getTraditionalRendering().getColorsChanged()[i]);
 	                	else
 	                		gc.setBackground(fRendering.getTraditionalRendering().getColorsChanged()[i]);
 	                	isColored = true;
 	                	break;
 	                }
 	            }
         	}
            
         }
     }
}
