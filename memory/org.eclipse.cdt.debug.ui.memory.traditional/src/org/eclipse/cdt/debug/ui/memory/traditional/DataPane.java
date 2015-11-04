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

    private int getAddressWidth() {
    	// derive the number of characters per address e.g. 2 * NumOfOctets
    	int addressCharacterCount = fRendering.getAddressBytes() * 2;
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

            BigInteger endAddress = fRendering.getViewportEndAddress();
            markAddressesWithAdditionalInfo(pe.display, startAddress, endAddress);
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
        fRendering.getTraditionalRendering().registerValuesRequest(new DataRequestMonitor<RegisterDataContainer[]>(session.getExecutor(), null){
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
                        if (fRendering != null && fRendering.isVisible() && !isDisposed()) {
                            // Mark addresses with additional info with a rectangle
                            int addressWidth = getAddressWidth();
                            
                            if (addressWidth > 0) {
                                // Resolve rectangle dimensions
                                int cellHeight = getCellHeight();
                                int cellWidth = getCellWidth();
                                int paddingAdjustment = fRendering.getCellPadding();
                                int width = cellWidth < addressWidth ? cellWidth - paddingAdjustment : addressWidth - paddingAdjustment;
                                int height = cellHeight - paddingAdjustment;
                                
                                // Resolve Graphical context
                                GC gc = new GC(DataPane.this);
                                gc.setForeground(fRendering.getTraditionalRendering().getColorChanged());
                                
                                for (BigInteger regAddress : valueToRegisters.keySet()) {
                                    // Resolve starting point
                                    Point location = getCellLocation(regAddress);
                                    // Draw a colored rectangle around the address
                                    gc.drawRectangle(location.x, location.y, width, height);
                                }
                                gc.dispose();
                            }
                        }
                    }
                });
                   
                
            }

          //TODO: Remove this temporary tracing code block
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
    
    private Map<BigInteger, List<RegisterDataContainer>> mapValueToRegisters(RegisterDataContainer[] registers, BigInteger startAddress, BigInteger endAddress) {
        Map<BigInteger, List<RegisterDataContainer>> allValuesmap = new HashMap<>(registers.length);

        synchronized (fMapAddressToRegs) {
            // Refreshing the Address to register data map
            fMapAddressToRegs.clear();
            for (RegisterDataContainer reg : registers) {
                List<RegisterDataContainer> containers = allValuesmap.get(reg.getValue());
                if (containers == null) {
                    containers = new ArrayList<RegisterDataContainer>();
                    allValuesmap.put(reg.getValue(), containers);
                }
                containers.add(reg);
                
                // If the value is within start and end address we want it in the filtered result
                if (reg.getValue().compareTo(startAddress) > -1 && reg.getValue().compareTo(endAddress) < 1) {
                    fMapAddressToRegs.put(reg.getValue(), allValuesmap.get(reg.getValue()));
                }
            }            
        }

        return fMapAddressToRegs;
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

    protected MouseTrackAdapter createMouseHoverListener(){
    	return new AbstractPaneMouseHoverListener();
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

            // Resolve the address associated to hovering location
            BigInteger cellAddress = null;
            try {
                cellAddress = getCellAddressAt(e.x, e.y);
            } catch (DebugException e1) {
                fRendering
                .logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_DETERMINE_CELL_LOCATION"), e1); //$NON-NLS-1$
                return;
            }

            if (cellAddress == null) {
                // Invalid Address at location
                return;
            }
            
            // Display tooltip if there is a change in hover
            Point hoverPoint = control.toDisplay(new Point(e.x, e.y));
            if (!fToolTipShell.isVisible() || !cellAddress.equals(fTooltipAddress)) {
                diplayToolTip(hoverPoint, cellAddress);
            } else {
                // Still pointing to the same cell
                return;
            }

            // Keep Track of the latest visited address
            fTooltipAddress = cellAddress;
        }
        
        private int getAddressesPerColumn() {
            // Prevent division by zero
            assert (fRendering.getBytesPerColumn() > 0);
            int octetsPerColum = (fRendering.getBytesPerColumn() > 0) ? fRendering.getBytesPerColumn() : 1;
            return octetsPerColum / fRendering.getAddressableSize();
        }

        private void diplayToolTip(Point hoverPoint, BigInteger cellAddress) {
            StringBuilder sb = new StringBuilder("Address: 0x").append(cellAddress.toString(16)).append("\n");
            
            int addressesPerColumn = getAddressesPerColumn();
            BigInteger subAddress = cellAddress;
            // Multiple addresses per column, accumulate the information for all addresses involved
            // TODO: The hovering could be enhanced to detect single addresses even if there are multiple addresses per cell
            for (int i=0; i < addressesPerColumn; i++) {
                if (isRegisterValue(subAddress)) {
                    String registerNames = buildRegistersListString(subAddress);
                    if (registerNames.length() > 0) {
                        if (i != 0) {
                            sb.append("Address: 0x").append(subAddress.toString(16)).append("\n");
                        }

                        sb.append(buildRegistersListString(subAddress));
                    }
                }
                
                subAddress = subAddress.add(BigInteger.ONE);
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

        private String buildRegistersListString(BigInteger cellAddress) {
            List<RegisterDataContainer> registers = fMapAddressToRegs.get(cellAddress);
            StringBuilder sb = new StringBuilder();
            for (RegisterDataContainer regContainer : registers) {
                sb.append(regContainer.getName()).append("\n");
            }
            return sb.toString();
        }
        
        private Label createToolTip() {
            fToolTipShell = new Shell(getShell(), SWT.ON_TOP | SWT.RESIZE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 1;
            gridLayout.marginWidth = 2;
            gridLayout.marginHeight = 0;
            fToolTipShell.setLayout(gridLayout);
            fToolTipShell.setBackground(fToolTipShell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            createToolTipHeader(fToolTipShell);
            return createToolTipContent(fToolTipShell);
        }

        private void createToolTipHeader(Composite composite) {
            Label toolTipLabel = new Label(composite, SWT.NONE);
            toolTipLabel.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
            toolTipLabel.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            toolTipLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
            toolTipLabel.setText("Address Cross Reference: ");
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
