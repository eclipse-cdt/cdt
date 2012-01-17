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

public class DataPane extends AbstractPane
{
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

    @Override
	protected Point getCellLocation(BigInteger cellAddress)
    {
        try
        {
            BigInteger address = fRendering.getViewportStartAddress();

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
        gc.setFont(fRendering.getFont());

        int cellHeight = getCellHeight();
        int cellWidth = getCellWidth();

        int columns = fRendering.getColumnCount();

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
                        * fRendering.getColumnCount() + col)
                        * fRendering.getAddressesPerColumn()));

                    TraditionalMemoryByte bytes[] = fRendering.getBytes(cellAddress,
                        fRendering.getBytesPerColumn());

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
                    }

                    gc.drawText(getCellText(bytes), cellWidth * col
                        + fRendering.getCellPadding(), cellHeight * i
                        + fRendering.getCellPadding());

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
   protected  void applyCustomColor(GC gc, TraditionalMemoryByte bytes[], int col)
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
