/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;

public abstract class AbstractPane extends Canvas
{
    protected Rendering fRendering;

    // selection state
    protected boolean fSelectionStarted = false;
    protected boolean fSelectionInProgress = false;

    protected BigInteger fSelectionStartAddress = null;

    protected int fSelectionStartAddressSubPosition;

    // caret
    protected Caret fCaret = null;

    // character may not fall on byte boundary
    protected int fSubCellCaretPosition = 0;
    protected int fOldSubCellCaretPosition = 0; 

    protected boolean fCaretEnabled = false;

    protected BigInteger fCaretAddress = null;

    // storage
    protected int fRowCount = 0;
    
    protected boolean fPaneVisible = true;
    
    class AbstractPaneMouseListener implements MouseListener
    {
        public void mouseUp(MouseEvent me)
        { 
            positionCaret(me.x, me.y);

            fCaret.setVisible(true);

            if(fSelectionInProgress && me.button == 1)
            {
                endSelection(me.x, me.y);
            }
            
            fSelectionInProgress = fSelectionStarted = false;
        }

        public void mouseDown(MouseEvent me)
        {
            AbstractPane.this.forceFocus();

            positionCaret(me.x, me.y);

            fCaret.setVisible(false);

            if(me.button == 1)
            {
                // if shift is down and we have an existing start address,
                // append selection
                if((me.stateMask & SWT.SHIFT) != 0
                    && fRendering.getSelection().getStart() != null)
                {

                    // if the pane doesn't have a selection start (the
                    // selection was created in a different pane)
                    // then initialize the pane's selection start to the
                    // rendering's selection start
                    if(AbstractPane.this.fSelectionStartAddress == null)
                        AbstractPane.this.fSelectionStartAddress = fRendering
                            .getSelection().getStart();

                    AbstractPane.this.fSelectionStarted = true;

                    AbstractPane.this.appendSelection(me.x, me.y);

                }
                else
                {
                    // start a new selection

                    AbstractPane.this.startSelection(me.x, me.y);
                }
            }
        }

        public void mouseDoubleClick(MouseEvent me)
        {
        	handleMouseDoubleClick(me);
        }
    	
    }

    class AbstractPaneMouseMoveListener implements MouseMoveListener
    {
        public void mouseMove(MouseEvent me)
        {
            if(fSelectionStarted)
            {
            	fSelectionInProgress = true;
                appendSelection(me.x, me.y);
            }
        }
    }
    
    class AbstractPaneFocusListener implements FocusListener
    {
        public void focusLost(FocusEvent fe)
        {
        	IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
        	if(TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE_ON_ENTER_ONLY
        			.equals(store.getString(TraditionalRenderingPreferenceConstants.MEM_EDIT_BUFFER_SAVE)))
			{
        		fRendering.getViewportCache().clearEditBuffer(); 
			}
        	else
        	{
        		fRendering.getViewportCache().writeEditBuffer();
        	}
            
            // clear the pane local selection start
            AbstractPane.this.fSelectionStartAddress = null;
        }

        public void focusGained(FocusEvent fe)
        {
        }
    	
    }

    class AbstractPaneKeyListener implements KeyListener
    {
        public void keyPressed(KeyEvent ke)
        {
           	fOldSubCellCaretPosition = fSubCellCaretPosition;
            if((ke.stateMask & SWT.SHIFT) != 0)
            {
                switch(ke.keyCode)
                {
                    case SWT.ARROW_RIGHT:
                    case SWT.ARROW_LEFT:
                    case SWT.ARROW_UP:
                    case SWT.ARROW_DOWN:
                    case SWT.PAGE_DOWN:
                    case SWT.PAGE_UP:
                        if(fRendering.getSelection().getStart() == null)
                        {
                            fRendering.getSelection().setStart(fCaretAddress.add(BigInteger.valueOf(
                            	fRendering.getAddressesPerColumn())), fCaretAddress);
                        }
                        break;
                }
            }

            if(ke.keyCode == SWT.ARROW_RIGHT)
            {
            	handleRightArrowKey();
            }
            else if(ke.keyCode == SWT.ARROW_LEFT || ke.keyCode == SWT.BS)
            {
            	handleLeftArrowKey();
            }
            else if(ke.keyCode == SWT.ARROW_DOWN)
            {
            	handleDownArrowKey();
            }
            else if(ke.keyCode == SWT.ARROW_UP)
            {
            	handleUpArrowKey();
           }
            else if(ke.keyCode == SWT.PAGE_DOWN)
            {
            	handlePageDownKey();
            }
            else if(ke.keyCode == SWT.PAGE_UP)
            {
            	handlePageUpKey();
            }
            else if(ke.keyCode == SWT.ESC)
            {
                fRendering.getViewportCache().clearEditBuffer();
            }
            else if(ke.character == '\r')
            {
                fRendering.getViewportCache().writeEditBuffer();
            }
            else if(Rendering.isValidEditCharacter(ke.character))
            {
            	if(fRendering.getSelection().hasSelection())
            	{
            		setCaretAddress(fRendering.getSelection().getLow());
            		fSubCellCaretPosition = 0;
            	}
            	
                editCell(fCaretAddress, fSubCellCaretPosition, ke.character);
            }

            if((ke.stateMask & SWT.SHIFT) != 0)
            {
                switch(ke.keyCode)
                {
                    case SWT.ARROW_RIGHT:
                    case SWT.ARROW_LEFT:
                    case SWT.ARROW_UP:
                    case SWT.ARROW_DOWN:
                    case SWT.PAGE_DOWN:
                    case SWT.PAGE_UP:
                        fRendering.getSelection().setEnd(fCaretAddress.add(BigInteger.valueOf(
                        	fRendering.getAddressesPerColumn())), 
                            fCaretAddress);
                        break;
                }
            }
            else if(ke.keyCode != SWT.SHIFT && ke.keyCode != SWT.CTRL && ke.keyCode != SWT.COMMAND) 
            // if shift key, keep selection, we might add to it
            {
                fRendering.getSelection().clear();
            }
        }

        public void keyReleased(KeyEvent ke)
        {
            // do nothing
        }
    }
    
    class AbstractPanePaintListener implements PaintListener
    {
        public void paintControl(PaintEvent pe)
        {
            AbstractPane.this.paint(pe);
        }
    }
    
    public AbstractPane(Rendering rendering)
    {
        super(rendering, SWT.DOUBLE_BUFFERED);

        fRendering = rendering;

        try
        {
            fCaretAddress = rendering.getBigBaseAddress();
        }
        catch(Exception e)
        {
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
    
    protected MouseListener createMouseListener(){
    	return new AbstractPaneMouseListener();
    }
    
    protected MouseMoveListener createMouseMoveListener(){
    	return new AbstractPaneMouseMoveListener();
    }
    
    protected FocusListener createFocusListener() {
    	return new AbstractPaneFocusListener();
    }

    protected KeyListener createKeyListener(){
    	return new AbstractPaneKeyListener();
    }

    protected PaintListener createPaintListener(){
    	return new AbstractPanePaintListener();
    }

    protected void handleRightArrowKey()
    {
    	fSubCellCaretPosition++;
        if(fSubCellCaretPosition >= getCellCharacterCount())
        {
            fSubCellCaretPosition = 0;
            // Ensure that caret is within the addressable range
            BigInteger newCaretAddress = fCaretAddress.add(BigInteger
                .valueOf(getNumberOfBytesRepresentedByColumn() / fRendering.getAddressableSize()));
            if(newCaretAddress.compareTo(fRendering.getMemoryBlockEndAddress()) > 0)
            {
            	fSubCellCaretPosition = getCellCharacterCount();
            }
            else
            {
                setCaretAddress(newCaretAddress);
            }
        }
        updateCaret();
        ensureCaretWithinViewport();    	
    }
    
    protected void handleLeftArrowKey()
    {
        fSubCellCaretPosition--;
        if(fSubCellCaretPosition < 0)
        {
            fSubCellCaretPosition = getCellCharacterCount() - 1;
            // Ensure that caret is within the addressable range
            BigInteger newCaretAddress = fCaretAddress.subtract(BigInteger
                .valueOf(getNumberOfBytesRepresentedByColumn() / fRendering.getAddressableSize()));
            if(newCaretAddress.compareTo(fRendering.getMemoryBlockStartAddress()) < 0)
            {
            	fSubCellCaretPosition = 0;
            }
            else
            {
                setCaretAddress(newCaretAddress);
            }

        }
        updateCaret();
        ensureCaretWithinViewport();    	
    }

    protected void handleDownArrowKey()
    {
    	// Ensure that caret is within the addressable range
        BigInteger newCaretAddress = fCaretAddress.add(BigInteger
            .valueOf(fRendering.getAddressableCellsPerRow()));
        setCaretAddress(newCaretAddress);

        updateCaret();
        ensureCaretWithinViewport();    	
    }
    
    protected void handleUpArrowKey()
    {
        // Ensure that caret is within the addressable range
        BigInteger newCaretAddress = fCaretAddress.subtract(BigInteger
            .valueOf(fRendering.getAddressableCellsPerRow()));
        setCaretAddress(newCaretAddress);
            
        updateCaret();
        ensureCaretWithinViewport();
    }
    
    protected void handlePageDownKey()
    {
    	// Ensure that caret is within the addressable range
        BigInteger newCaretAddress = fCaretAddress.add(BigInteger
            .valueOf(fRendering.getAddressableCellsPerRow()
                * (fRendering.getRowCount() - 1)));

        setCaretAddress(newCaretAddress);

        updateCaret();
        ensureCaretWithinViewport();
    }
    
    protected void handlePageUpKey()
    {
    	// Ensure that caret is within the addressable range
        BigInteger newCaretAddress = fCaretAddress.subtract(BigInteger
            .valueOf(fRendering.getAddressableCellsPerRow()
                * (fRendering.getRowCount() - 1)));
        setCaretAddress(newCaretAddress);

        updateCaret();
        ensureCaretWithinViewport();    	
    }
    
    protected void handleMouseDoubleClick(MouseEvent me)
    {
    	try
    	{
    		BigInteger address = getViewportAddress(me.x / getCellWidth(), me.y
    			/ getCellHeight());
    		
    		fRendering.getSelection().clear();
    		fRendering.getSelection().setStart(address.add(BigInteger
                    .valueOf(fRendering.getAddressesPerColumn())), address);
    		fRendering.getSelection().setEnd(address.add(BigInteger
                    .valueOf(fRendering.getAddressesPerColumn())), address);
    	}
    	catch(DebugException de)
    	{
    		// do nothing
    	}
    }
    
    protected boolean isPaneVisible()
    {
    	return fPaneVisible;
    }
    
    protected void setPaneVisible(boolean visible)
    {
    	fPaneVisible = visible;
    	this.setVisible(visible);
    }

    protected int getNumberOfBytesRepresentedByColumn()
    {
        return fRendering.getBytesPerColumn();
    }

    protected void editCell(BigInteger address, int subCellPosition,
        char character)
    {
        // do nothing
    }

    // Set the caret address
    protected void setCaretAddress(BigInteger caretAddress)
    {
        // Ensure that caret is within the addressable range
        if((caretAddress.compareTo(fRendering.getMemoryBlockStartAddress()) >= 0) &&
            (caretAddress.compareTo(fRendering.getMemoryBlockEndAddress()) <= 0))
        {
        	fCaretAddress = caretAddress;
        }
        else if(caretAddress.compareTo(fRendering.getMemoryBlockStartAddress()) < 0)
        {
        	// calculate offset from the beginning of the row
            int cellOffset = fCaretAddress.subtract(fRendering.getViewportStartAddress()).intValue();
            int row = cellOffset / (fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter());   
            
            cellOffset -= row * fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter();
            
            fCaretAddress = fRendering.getMemoryBlockStartAddress().add(
            		BigInteger.valueOf(cellOffset / fRendering.getAddressableSize()));
         }
        else if(caretAddress.compareTo(fRendering.getMemoryBlockEndAddress()) > 0)
        {
        	// calculate offset from the end of the row
            int cellOffset = fCaretAddress.subtract(fRendering.getViewportEndAddress()).intValue() + 1;
            int row = cellOffset / (fRendering.getBytesPerRow() / fRendering.getBytesPerCharacter());    
            
            cellOffset -= row * fRendering.getBytesPerRow()/ fRendering.getBytesPerCharacter();
            
            fCaretAddress = fRendering.getMemoryBlockEndAddress().add(
            		BigInteger.valueOf(cellOffset / fRendering.getAddressableSize()));
         }  	
        
        fRendering.setCaretAddress(fCaretAddress);
    }
    
    protected boolean isOdd(int value)
    {
    	return (value / 2) * 2 == value;
    }
    
    @SuppressWarnings("all")
    protected void updateCaret()
    {
        try
        {
            if(fCaretAddress != null)
            {
                Point cellPosition = getCellLocation(fCaretAddress);
                if(cellPosition != null)
                    fCaret.setLocation(cellPosition.x + fSubCellCaretPosition * getCellCharacterWidth(), cellPosition.y);
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

    protected void ensureCaretWithinViewport() // TODO getAddressableSize() > 1 ?
    {
        BigInteger vpStart = fRendering.getViewportStartAddress();
        BigInteger vpEnd   = fRendering.getViewportEndAddress();
        
        Rectangle vpBounds = fRendering.getBounds();
        Rectangle apBounds = fRendering.fAddressPane.getBounds();
        Rectangle dpBounds = fRendering.fBinaryPane.getBounds();
        Rectangle tpBounds = fRendering.fTextPane.getBounds();
        
        ScrollBar hBar = fRendering.getHorizontalBar();
        
        Point adjustedCaret = null;
        
        int leftPaneEdge  = 0; 
        int rightPaneEdge = 0;
        int eolLocation   = 0;
        int bolSelection  = 0;
        int eolSelection  = 0;
        
        // Determine if we're in the address, data (binary) or text panes; return if none of 'em.
        
        if (this instanceof AddressPane)
        {
            adjustedCaret = new Point(fCaret.getLocation().x, fCaret.getLocation().y);
        }
        else if (this instanceof DataPane)
        {
            leftPaneEdge  = Math.max(vpBounds.x, dpBounds.x);
            rightPaneEdge = vpBounds.x + vpBounds.width;
            bolSelection  = hBar.getMinimum();
            eolSelection  = apBounds.width + dpBounds.width - (vpBounds.width/2);
            eolLocation   = apBounds.width + dpBounds.width - 10;
            adjustedCaret = new Point(fCaret.getLocation().x + dpBounds.x + 16, fCaret.getLocation().y);
        }
        else if (this instanceof TextPane)
        {
            leftPaneEdge  = apBounds.width + dpBounds.width;
            rightPaneEdge = leftPaneEdge + (vpBounds.width - tpBounds.x);
            bolSelection  = apBounds.width + dpBounds.width - 36;
            eolSelection  = hBar.getMaximum();
            eolLocation   = apBounds.width + dpBounds.width + tpBounds.width - 22;
            adjustedCaret = new Point(fCaret.getLocation().x + apBounds.width + dpBounds.width, fCaret.getLocation().y);
        }
        else
            return;

        if (fCaretAddress.compareTo(vpStart) < 0 || fCaretAddress.compareTo(vpEnd) >= 0)
        {
            // The caret was moved outside the viewport bounds:  Scroll the
            // viewport up or down by a row, depending on where the caret is
            
            boolean upArrow = fCaretAddress.compareTo(vpStart) <= 0;
            ScrollBar vBar = fRendering.getVerticalBar();
            vBar.setSelection(vBar.getSelection() + (upArrow ? -1 : 1));
            vBar.notifyListeners(SWT.Selection, new Event());
            
            // Check to see if we're at the beginning or end of a line, and
            // move the scrollbar, if necessary, to keep the caret in view.
            
            int currentCaretLocation = fCaret.getLocation().x + dpBounds.x + 16;
            int lowEolLimit  = eolLocation - 1;
            int highEolLimit = eolLocation + 1;
            
            if (fCaret.getLocation().x == 2)
            {
                hBar.setSelection(bolSelection);
                hBar.notifyListeners(SWT.Selection, new Event());
            }
            else if (upArrow && ((currentCaretLocation >= lowEolLimit && currentCaretLocation <= highEolLimit))) 
            {
                hBar.setSelection(eolSelection);
                hBar.notifyListeners(SWT.Selection, new Event());
            }
        }
        else if (!vpBounds.contains(adjustedCaret))
        {
            // Left or Right arrow:  The caret is now outside the viewport and beyond the pane.  Calculate
            // a new pane position at [up to] 33% left or right in the viewport, to center the caret; use a
            // positive or negative offset depending on which direction we're scrolling.
            
            int hBarOffset = (rightPaneEdge - leftPaneEdge) / 3;
            int newHBarSel = hBar.getSelection() + (adjustedCaret.x > rightPaneEdge ? hBarOffset : -hBarOffset);
            
            if (fCaret.getLocation().x == 2)
            {
                // Beginning of a line
              hBar.setSelection(bolSelection);
            }
            else if (adjustedCaret.x == eolLocation)
            {
                // End of a line
                hBar.setSelection(eolSelection);
            }
            else if (adjustedCaret.x > rightPaneEdge)
            {
                // Caret was moved by the user beyond the right edge of the pane
                hBar.setSelection(newHBarSel < hBar.getMaximum() ? newHBarSel : hBar.getMaximum());
            }
            else if (adjustedCaret.x < leftPaneEdge)
            {
                // Caret was moved by the user beyond the left edge of the pane
                hBar.setSelection(newHBarSel > hBar.getMinimum() ? newHBarSel : hBar.getMinimum());
            }
            else
                return;
            
            hBar.notifyListeners(SWT.Selection, new Event());
        }
        else
        {
            // Caret is inside the viewport
            return;
        }
        
        fRendering.ensureViewportAddressDisplayable();
        fRendering.setCaretAddress(fCaretAddress);
    }
    
    protected void advanceCursor()
    {
    	handleRightArrowKey();
    }

    protected void positionCaret(int x, int y)
    {
        // do nothing
    }

    protected int getRowCount()
    {
        return fRowCount;
    }

    protected void setRowCount()
    {
    	fRowCount = getBounds().height / getCellHeight();
    }
    
    protected void settingsChanged()
    {
        fSubCellCaretPosition = 0;
    }

    protected void startSelection(int x, int y)
    {
        try
        {
            BigInteger address = getViewportAddress(x / getCellWidth(), y
                / getCellHeight());

            if(address != null)
            {
                this.fSelectionStartAddress = address;
                Point cellPosition = getCellLocation(address);

                if(cellPosition != null)
                {
                    int offset = x - cellPosition.x;
                    fSelectionStartAddressSubPosition = offset
                        / getCellCharacterWidth();
                }
                fRendering.getSelection().clear();
                fRendering.getSelection().setStart(address.add(BigInteger.valueOf(
                    fRendering.getBytesPerColumn() / fRendering.getAddressableSize())), address);

                fSelectionStarted = true;
                
                new CopyDefaultAction(fRendering, DND.SELECTION_CLIPBOARD).run();
            }
        }
        catch(DebugException e)
        {
            fRendering
                .logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_START_SELECTION"), e); //$NON-NLS-1$
        }
    }

    protected void endSelection(int x, int y)
    {
        appendSelection(x, y);

        fSelectionInProgress = false;
    }

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
                // deal with sub cell selection
                Point cellPosition = getCellLocation(address);
                int offset = x - cellPosition.x;
                int subCellCharacterPosition = offset / getCellCharacterWidth();

                if(Math.abs(subCellCharacterPosition
                    - this.fSelectionStartAddressSubPosition) > this
                    .getCellCharacterCount() / 4)
                {
                    fRendering.getSelection().setEnd(address.add(BigInteger
                        .valueOf(fRendering.getAddressesPerColumn())), address);
                }
                else
                {
                    fRendering.getSelection().setEnd(null, null);
                }
            }
            else
            {
                fRendering.getSelection().setEnd(address.add(BigInteger
                    .valueOf(fRendering.getAddressesPerColumn())), address);
            }

            if(fRendering.getSelection().getEnd() != null)
            {
                this.fCaretAddress = fRendering.getSelection().getEnd();
                this.fSubCellCaretPosition = 0;
            }

            updateCaret();
            
            new CopyDefaultAction(fRendering, DND.SELECTION_CLIPBOARD).run();
        }
        catch(DebugException e)
        {
            fRendering
                .logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_APPEND_SELECTION"), e); //$NON-NLS-1$
        }
    }

    protected void paint(PaintEvent pe)
    {
    	fRowCount = getBounds().height / getCellHeight();
    	
    	if(fRendering.isDirty())
        {
    		fRendering.setDirty(false);
    		fRendering.refresh();
        }
    }

    abstract protected BigInteger getViewportAddress(int col, int row)
        throws DebugException;

    protected Point getCellLocation(BigInteger address)
    {
        return null;
    }

    protected String getCellText(MemoryByte bytes[])
    {
        return null;
    }

    abstract protected int getCellWidth();

    abstract protected int getCellCharacterCount();

    @Override
	public void setFont(Font font)
    {
    	super.setFont(font);
    	fCharacterWidth = -1;
    	fCellHeight = -1;
    	fTextHeight = -1;
    }
    
    private int fCellHeight = -1; // called often, cache

    protected int getCellHeight()
    {
        if(fCellHeight == -1)
        {
            fCellHeight = getCellTextHeight()
                + (fRendering.getCellPadding() * 2);
        }

        return fCellHeight;
    }

    private int fCharacterWidth = -1; // called often, cache

    protected int getCellCharacterWidth()
    {
        if(fCharacterWidth == -1)
        {
            GC gc = new GC(this);
            gc.setFont(fRendering.getFont()); 
            fCharacterWidth = gc.getAdvanceWidth('F');
            gc.dispose();
        }

        return fCharacterWidth;
    }

    private int fTextHeight = -1; // called often, cache

    protected int getCellTextHeight()
    {
        if(fTextHeight == -1)
        {
            GC gc = new GC(this);
            gc.setFont(fRendering.getFont());
            FontMetrics fontMetrics = gc.getFontMetrics();
            fTextHeight = fontMetrics.getHeight();
            gc.dispose();
        }
        return fTextHeight;
    }
}
