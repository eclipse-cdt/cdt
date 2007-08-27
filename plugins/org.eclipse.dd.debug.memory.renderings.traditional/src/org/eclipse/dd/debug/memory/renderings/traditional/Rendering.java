/*******************************************************************************
 * Copyright (c) 2006-2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.memory.renderings.traditional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.internal.ui.views.memory.renderings.GoToAddressComposite;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Text;

public class Rendering extends Composite implements IDebugEventSetListener
{
    // the IMemoryRendering parent
    private TraditionalRendering fParent;

    // controls

    private AddressPane fAddressPane;

    private DataPane fBinaryPane;

    private TextPane fTextPane;

    private GoToAddressComposite fAddressBar;
    
    private Control fAddressBarControl; // FIXME why isn't there a getControl() ?

    private Selection fSelection = new Selection();

    // storage
 
    BigInteger fViewportAddress = null; // default visibility for performance

    BigInteger fMemoryBlockStartAddress = null;
    BigInteger fMemoryBlockEndAddress = null;
    
    BigInteger fBaseAddress = null; // remember the base address
    
    private int fColumnCount = 0; 	// auto calculate can be disabled by user,
    								// making this user settable
    
    private int fBytesPerRow = 0;	// current number of bytes per row are displayed
    
    private int fCurrentScrollSelection = 0;	// current scroll selection;
    
    private BigInteger fCaretAddress = BigInteger.valueOf(0); // -1 ?
    
    // user settings
    
    private int fTextMode = 1;  // ASCII default, TODO make preference?

    private int fBytesPerColumn = 4; // 4 byte cell width default

    private int fRadix = RADIX_HEX;
    
    private int fColumnsSetting = COLUMNS_AUTO_SIZE_TO_FIT;

    private boolean fLittleEndian = false;
    
    private boolean fCheckedLittleEndian = false;

    // constants used to identify radix
    protected final static int RADIX_HEX = 1;

    protected final static int RADIX_DECIMAL_SIGNED = 2;

    protected final static int RADIX_DECIMAL_UNSIGNED = 3;

    protected final static int RADIX_OCTAL = 4;

    protected final static int RADIX_BINARY = 5;

    // constants used to identify panes
    protected final static int PANE_ADDRESS = 1;

    protected final static int PANE_BINARY = 2;

    protected final static int PANE_TEXT = 3;
    
    // constants used to identify text, maybe java should be queried for all available sets
    protected final static int TEXT_ISO_8859_1 = 1;
    protected final static int TEXT_USASCII = 2;
    protected final static int TEXT_UTF8 = 3;
    protected final static int TEXT_UTF16 = 4;
    
    // internal constants
    protected final static int COLUMNS_AUTO_SIZE_TO_FIT = 0;
    
    // view internal settings
    private int fCellPadding = 2;

    private int fPaneSpacing = 16;
    
    // flag whether the memory cache is dirty
    private boolean fCacheDirty = false;

    public Rendering(Composite parent, TraditionalRendering renderingParent)
    {
        super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND | SWT.H_SCROLL
        		| SWT.V_SCROLL);
        
        this.setFont(JFaceResources
            .getFont(IInternalDebugUIConstants.FONT_NAME)); // TODO internal?

        this.fParent = renderingParent;

        // initialize the viewport start
        if(fParent.getMemoryBlock() != null)
        {
            fViewportAddress = fParent.getMemoryBlockStartAddress();
            
             // this will be null if memory may be retrieved at any address less than
             // this memory block's base.  if so use the base address.
             if (fViewportAddress == null)
               	 fViewportAddress = fParent.getBigBaseAddress();
             fBaseAddress = fViewportAddress;
        }
        
        // instantiate the panes, TODO default visibility from state or
        // plugin.xml?
        this.fAddressPane = new AddressPane(this);
        this.fBinaryPane = new DataPane(this);
        this.fTextPane = new TextPane(this);
        
        fAddressBar = new GoToAddressComposite();
		fAddressBarControl = fAddressBar.createControl(parent);
		Button button = fAddressBar.getButton(IDialogConstants.OK_ID);
		if (button != null)
		{
			button.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					doGoToAddress();
				}
			});
			
			button = fAddressBar.getButton(IDialogConstants.CANCEL_ID);
			if (button != null)
			{
				button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setVisibleAddressBar(false);
					}});
			}
		}
		
		fAddressBar.getExpressionWidget().addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				doGoToAddress();
			}});
		
		fAddressBar.getExpressionWidget().addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC)
					setVisibleAddressBar(false);
				super.keyPressed(e);
			}
		});
    	
        this.fAddressBarControl.setVisible(false);

        getHorizontalBar().addSelectionListener(new SelectionListener()
        {
        	public void widgetSelected(SelectionEvent se)
            {
        		Rendering.this.layout();
            }
        	
        	public void widgetDefaultSelected(SelectionEvent se)
            {
                // do nothing
            }
        });
        
        getVerticalBar().addSelectionListener(
        	new SelectionListener()
        {
            public void widgetSelected(SelectionEvent se)
            {
            	int addressableSize = getAddressableSize();
            	
                switch(se.detail)
                {
                    case SWT.ARROW_DOWN:
                        fViewportAddress = fViewportAddress.add(BigInteger
                            .valueOf(getAddressableCellsPerRow()));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.PAGE_DOWN:
                        fViewportAddress = fViewportAddress.add(BigInteger
                            .valueOf(getAddressableCellsPerRow()
                                * (Rendering.this.getRowCount() - 1)));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.ARROW_UP:
                        fViewportAddress = fViewportAddress.subtract(BigInteger
                            .valueOf(getAddressableCellsPerRow()));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.PAGE_UP:
                        fViewportAddress = fViewportAddress.subtract(BigInteger
                            .valueOf(getAddressableCellsPerRow()
                                * (Rendering.this.getRowCount() - 1)));
                        ensureViewportAddressDisplayable();
                        redrawPanes();
                        break;
                    case SWT.SCROLL_LINE:
                    	if(getVerticalBar().getSelection() == getVerticalBar().getMinimum())
                    	{
                    		// Set view port start address to the start address of the Memory Block
                    		fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
                    	}
                    	else if(getVerticalBar().getSelection() == getVerticalBar().getMaximum())
                    	{
                    		// The view port end address should be less or equal to the the end address of the Memory Block
                    		// Set view port address to be bigger than the end address of the Memory Block for now
                    		// and let ensureViewportAddressDisplayable() to figure out the correct view port start address
                    		fViewportAddress = Rendering.this.getMemoryBlockEndAddress();
                    	}
                    	else
                    	{
                            // Figure out the delta
                        	int delta = getVerticalBar().getSelection() - fCurrentScrollSelection;
                    		fViewportAddress = fViewportAddress.add(BigInteger.valueOf(
                    				getAddressableCellsPerRow() * delta));
                    	}
                        ensureViewportAddressDisplayable();
                        // Update tooltip
                        // FIXME conversion from slider to scrollbar
                        // getVerticalBar().setToolTipText(Rendering.this.getAddressString(fViewportAddress));
                        
                        // Update the addresses on the Address pane. 
                    	// Do not update the Binary and Text panes until dragging of the thumb nail stops
                        if(fAddressPane.isPaneVisible())
                        {
                            fAddressPane.redraw();
                        }                        
                    	break;
                    case SWT.NONE:
                        // Dragging of the thumb nail stops. Redraw the panes
                        redrawPanes();
                    	break;
                }

            }

            public void widgetDefaultSelected(SelectionEvent se)
            {
                // do nothing
            }
        });

        this.addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent pe)
            {
            	pe.gc.setBackground(Rendering.this.getTraditionalRendering().getColorBackground());
                pe.gc.fillRectangle(0, 0, Rendering.this.getBounds().width, 
                		Rendering.this.getBounds().height);
            }
        });

        this.setLayout(new Layout()
        {
            public void layout(Composite composite, boolean changed)
            {
            	int xOffset = 0;
            	if(Rendering.this.getHorizontalBar().isVisible())
                	xOffset = Rendering.this.getHorizontalBar().getSelection();
            	
                int x = xOffset * -1;
                int y = 0;
                
                if(fAddressBarControl.isVisible())
                {
                	fAddressBarControl.setBounds(0, 0,
                        Rendering.this.getBounds().width, fAddressBarControl
                            .computeSize(100, 30).y); // FIXME
                    //y = fAddressBarControl.getBounds().height;
                }

                if(fAddressPane.isPaneVisible())
                {
                    fAddressPane.setBounds(x, y,
                        fAddressPane.computeSize(0, 0).x, Rendering.this
                            .getBounds().height
                            - y);
                    x = fAddressPane.getBounds().x
                        + fAddressPane.getBounds().width;
                }

                if(fBinaryPane.isPaneVisible())
                {
                    fBinaryPane.setBounds(x, y,
                        fBinaryPane.computeSize(0, 0).x, Rendering.this
                            .getBounds().height
                            - y);
                    x = fBinaryPane.getBounds().x
                        + fBinaryPane.getBounds().width;
                }

                if(fTextPane.isPaneVisible())
                {
                    fTextPane.setBounds(x, y, 
                    	Math.max(fTextPane.computeSize(0, 0).x, Rendering.this.getClientArea().width 
                    		- x - xOffset), Rendering.this.getBounds().height - y);
                }

                if(getClientArea().width >= fTextPane.getBounds().x + fTextPane.getBounds().width + xOffset)
                {
                	Rendering.this.getHorizontalBar().setVisible(false);
                }
                else
                {
                	ScrollBar horizontal = Rendering.this.getHorizontalBar();
                	
                	horizontal.setVisible(true);
                	horizontal.setMinimum(0);
                	horizontal.setMaximum(fTextPane.getBounds().x 
                		+ fTextPane.getBounds().width + xOffset);
                	horizontal.setThumb(getClientArea().width);
                	horizontal.setPageIncrement(40); // TODO ?
                	horizontal.setIncrement(20); // TODO ?
                }
            }

            protected Point computeSize(Composite composite, int wHint,
                int hHint, boolean flushCache)
            {
                return new Point(100, 100); // dummy data
            }
        });

        this.addControlListener(new ControlListener()
        {
            public void controlMoved(ControlEvent ce)
            {
            }

            public void controlResized(ControlEvent ce)
            {
                packColumns();
            }
        });

        DebugPlugin.getDefault().addDebugEventListener(this);
    }

    protected TraditionalRendering getTraditionalRendering() // TODO rename
    {
    	return fParent;
    }
    
    protected void setCaretAddress(BigInteger address)
    {
    	fCaretAddress = address;
    }
    
    protected BigInteger getCaretAddress()
    {
    	return fCaretAddress;
    }
    
    private void doGoToAddress() {
		try {
			BigInteger address = fAddressBar.getGoToAddress(this.getMemoryBlockStartAddress(), this.getCaretAddress());
			getTraditionalRendering().gotoAddress(address);
			setVisibleAddressBar(false);
		} catch (NumberFormatException e1)
		{ 
			// FIXME log?
		}
	}
    
    // Ensure that all addresses displayed are within the addressable range
    protected void ensureViewportAddressDisplayable()
    {
        if(fViewportAddress.compareTo(Rendering.this.getMemoryBlockStartAddress()) < 0)
        {
        	fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
        }
        else if(getViewportEndAddress().compareTo(getMemoryBlockEndAddress().add(BigInteger.ONE)) > 0)
        {
            fViewportAddress = getMemoryBlockEndAddress().subtract(BigInteger.valueOf(getAddressableCellsPerRow() 
            		* getRowCount() - 1));
        }

        setCurrentScrollSelection();
    }
    
    protected Selection getSelection()
    {
        return fSelection;
    }
    
    protected void logError(String message, Exception e)
    {
        Status status = new Status(IStatus.ERROR, fParent.getRenderingId(),
            DebugException.INTERNAL_ERROR, message, e);

        DebugUIPlugin.getDefault().getLog().log(status);
    }

    public void handleFontPreferenceChange(Font font)
    {
        setFont(font);

        Control controls[] = this.getRenderingPanes();
        for(int i = 0; i < controls.length; i++)
            controls[i].setFont(font);

        packColumns();
        redrawPanes();
    }

    public void handleDebugEvents(DebugEvent[] events)
    {
    	for(int i = 0; i < events.length; i++)
        {
            if(events[0].getSource() instanceof IDebugElement)
            {
                final int kind = events[i].getKind();
                final int detail = events[i].getDetail();
                final IDebugElement source = (IDebugElement) events[i]
                    .getSource();
                
                // TODO allow extensible customization of event handling;
                // integration with user configurable update policies should happen here.
                if(source.getDebugTarget() == getMemoryBlock()
                        .getDebugTarget())
                {
                	if(kind == DebugEvent.SUSPEND && detail == 0)
                	{
	                    Display.getDefault().asyncExec(new Runnable()
	                    {
	                        public void run()
	                        {
	                            refresh();
	                        }
	                    });
                	}
                	else if(kind == DebugEvent.CHANGE)
                	{
	                    Display.getDefault().asyncExec(new Runnable()
	                    {
	                        public void run()
	                        {
	                            refresh();
	                        }
	                    });
                	}
                	else if(kind == DebugEvent.RESUME)
                	{
	                    Display.getDefault().asyncExec(new Runnable()
	                    {
	                        public void run()
	                        {
	                            archiveDeltas();
	                        }
	                    });
                	}
                }
            }
        }
    }

    // return true to enable development debug print statements
    protected boolean isDebug()
    {
        return false;
    }

    private IMemoryBlockExtension getMemoryBlock()
    {
        IMemoryBlock block = fParent.getMemoryBlock();
        if(block != null)
            return (IMemoryBlockExtension) block
                .getAdapter(IMemoryBlockExtension.class);

        return null;
    }
    
    protected BigInteger getBigBaseAddress()
    {
    	return fParent.getBigBaseAddress();
    }

    protected int getAddressableSize()
    {
    	return fParent.getAddressableSize();
    }
    
    protected ViewportCache getViewportCache()
    {
        return fViewportCache;
    }

    protected MemoryByte[] getBytes(BigInteger address, int bytes)
        throws DebugException
    {
        return fViewportCache.getBytes(address, bytes);
    }

    // default visibility for performance
    ViewportCache fViewportCache = new ViewportCache(); 

    private interface Request
    {
    }
    
    class ViewportCache extends Thread
    {
    	class ArchiveDeltas implements Request
    	{
    		
    	}
    	
        class AddressPair implements Request
        {
            BigInteger startAddress;

            BigInteger endAddress;
        }

        class MemoryUnit
        {
            BigInteger start;

            BigInteger end;

            MemoryByte[] bytes;

            public MemoryUnit clone()
            {
                MemoryUnit b = new MemoryUnit();

                b.start = this.start;
                b.end = this.end;
                b.bytes = new MemoryByte[this.bytes.length];
                for(int i = 0; i < this.bytes.length; i++)
                	b.bytes[i] = new MemoryByte(this.bytes[i].getValue());

                return b;
            }

            public boolean isValid()
            {
                return this.start != null && this.end != null
                    && this.bytes != null;
            }
        }

        private HashMap fEditBuffer = new HashMap();

        private boolean fDisposed = false;

        private Vector fQueue = new Vector();

        protected MemoryUnit fCache = null;

        protected MemoryUnit fHistoryCache = null;

        public ViewportCache()
        {
            start();
        }

        public void dispose()
        {
            fDisposed = true;
            synchronized(this)
            {
                this.notify();
            }
        }

        protected void refresh()
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if(fCache != null)
            {
                queueRequest(fViewportAddress, getViewportEndAddress());
            }
        }
        
        protected void archiveDeltas()
        {
        	assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if(fCache != null)
            {
                queueRequestArchiveDeltas();
            }
        }

        
        
        private void queueRequest(BigInteger startAddress, BigInteger endAddress)
        {
            AddressPair pair = new AddressPair();
            pair.startAddress = startAddress;
            pair.endAddress = endAddress;
            synchronized(fQueue)
            {
                fQueue.addElement(pair);
            }
            synchronized(this)
            {
                this.notify();
            }
        }
        
        private void queueRequestArchiveDeltas()
        {
        	ArchiveDeltas archive = new ArchiveDeltas();
        	synchronized(fQueue)
            {
                fQueue.addElement(archive);
            }
            synchronized(this)
            {
                this.notify();
            }
        }

        public void run()
        {
            while(!fDisposed)
            {
                AddressPair pair = null;
                boolean archiveDeltas = false;
                synchronized(fQueue)
                {
                    if(fQueue.size() > 0)
                    {
                    	Request request = (Request) fQueue.elementAt(0);
                    	Class type = null;
                    	if(request instanceof ArchiveDeltas)
                    	{
                    		archiveDeltas = true;
                    		type = ArchiveDeltas.class;
                    	}
                    	else if(request instanceof AddressPair)
                    	{
                    		pair = (AddressPair) request;
                    		type = AddressPair.class;
                    	}
                    	
                    	while(fQueue.size() > 0 && type.isInstance(fQueue.elementAt(0)))
                    		fQueue.removeElementAt(0);
                    }
                }
                if(archiveDeltas)
                {
                    fHistoryCache = fCache.clone();
                }
                else if(pair != null)
                {
                    populateCache(pair.startAddress, pair.endAddress);
                }
                else
                {
                    synchronized(this)
                    {
                        try
                        {
                            this.wait();
                        }
                        catch(Exception e)
                        {
                            // do nothing
                        }
                    }
                }
            }
        }

        // cache memory necessary to paint viewport
        // TODO: user setting to buffer +/- x lines
        // TODO: reuse existing cache? probably only a minor performance gain
        private void populateCache(final BigInteger startAddress,
            final BigInteger endAddress)
        {
            try
            {
                final IMemoryBlockExtension memoryBlock = getMemoryBlock();

                final BigInteger lengthInBytes = endAddress.subtract(startAddress);
                final BigInteger addressableSize = BigInteger.valueOf(getAddressableSize());
                
                final long units = lengthInBytes.divide(addressableSize).add(
                		lengthInBytes.mod(addressableSize).compareTo(BigInteger.ZERO) > 0
                			? BigInteger.ONE : BigInteger.ZERO).longValue();
                
                // CDT (and maybe other backends) will call setValue() on these MemoryBlock objects.
                // We don't want this to happen, because it interferes with this rendering's own
                // change history. Ideally, we should strictly use the back end change notification
                // and history, but it is only guaranteed to work for bytes within the address range
                // of the MemoryBlock. 
                final MemoryByte readBytes[] = memoryBlock
                	.getBytesFromAddress(startAddress, units);
                
                final MemoryByte cachedBytes[] = new MemoryByte[readBytes.length];
                for(int i = 0; i < readBytes.length; i++)
                	cachedBytes[i] = new MemoryByte(readBytes[i].getValue(), readBytes[i].getFlags());

                // we need to set the default endianess.  before it was set to BE
                // by default which wasn't very useful for LE targets.  now we will
                // query the first byte to get the endianess.  if not known then we'll
                // leave it as BE.  note that we only do this when reading the first
                // bit of memory for this rendering.  what happens when scrolling
                // through  memory and it changes endianess?  for now we just leave
                // it in the original endianess.
            	if (!fCheckedLittleEndian && cachedBytes.length > 0) {
                	if (cachedBytes[0].isEndianessKnown()) {
                		fLittleEndian = !cachedBytes[0].isBigEndian();
                    	fCheckedLittleEndian = true;
                    	fParent.bytesAreLittleEndian(fLittleEndian);
                	}
            	}
                
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        // generate deltas
                        if(fHistoryCache != null && fHistoryCache.isValid())
                        {
                            BigInteger maxStart = startAddress
                                .max(fHistoryCache.start);
                            BigInteger minEnd = endAddress
                                .min(fHistoryCache.end).subtract(
                                    BigInteger.valueOf(1));

                            BigInteger overlapLength = minEnd
                                .subtract(maxStart);
                            if(overlapLength.compareTo(BigInteger.valueOf(0)) > 0)
                            {
                                // there is overlap

                                int offsetIntoOld = maxStart.subtract(
                                    fHistoryCache.start).intValue();
                                int offsetIntoNew = maxStart.subtract(
                                    startAddress).intValue();

                                for(int i = overlapLength.intValue(); i >= 0; i--)
                                {
                                	cachedBytes[offsetIntoNew + i]
                                        .setChanged(cachedBytes[offsetIntoNew
                                            + i].getValue() != fHistoryCache.bytes[offsetIntoOld
                                            + i].getValue());
                                }
                            }
                        }

                        fCache = new MemoryUnit();
                        fCache.start = startAddress;
                        fCache.end = endAddress;
                        fCache.bytes = cachedBytes;

                        Rendering.this.redrawPanes();
                    }
                });

            }
            catch(Exception e)
            {
                logError(
                    TraditionalRenderingMessages
                        .getString("TraditionalRendering.FAILURE_READ_MEMORY"), e); //$NON-NLS-1$
            }
        }

        // bytes will be fetched from cache
        protected MemoryByte[] getBytes(BigInteger address, int bytesRequested)
            throws DebugException
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if(containsEditedCell(address)) // cell size cannot be switched during an edit
                return getEditedMemory(address);

            boolean contains = false;
            if(fCache != null && fCache.start != null)
            {
            	// see if all of the data requested is in the cache
            	BigInteger dataEnd = address.add(BigInteger.valueOf(bytesRequested));

                if(fCache.start.compareTo(address) <= 0
                	&& fCache.end.compareTo(dataEnd) >= 0
                	&& fCache.bytes.length > 0)
                    contains = true;
            }

            if(contains)
            {
                int offset = address.subtract(fCache.start).intValue();
                MemoryByte bytes[] = new MemoryByte[bytesRequested];
                for(int i = 0; i < bytes.length; i++)
                {
                    bytes[i] = fCache.bytes[offset + i];
                }

                return bytes;
            }
            
            MemoryByte bytes[] = new MemoryByte[bytesRequested];
            for(int i = 0; i < bytes.length; i++)
            {
                bytes[i] = new MemoryByte();
                bytes[i].setReadable(false);
            }

            fViewportCache.queueRequest(fViewportAddress,
                getViewportEndAddress());

            return bytes;    
        }

        private boolean containsEditedCell(BigInteger address)
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            return fEditBuffer.containsKey(address);
        }

        private MemoryByte[] getEditedMemory(BigInteger address)
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            return (MemoryByte[]) fEditBuffer.get(address);
        }

        protected void clearEditBuffer()
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            fEditBuffer.clear();
            Rendering.this.redrawPanes();
        }

        protected void writeEditBuffer()
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            Set keySet = fEditBuffer.keySet();
            Iterator iterator = keySet.iterator();

            while(iterator.hasNext())
                {
                    BigInteger address = (BigInteger) iterator.next();
                    MemoryByte[] bytes = (MemoryByte[]) fEditBuffer
                        .get(address);

                    byte byteValue[] = new byte[bytes.length];
                    for(int i = 0; i < bytes.length; i++)
                        byteValue[i] = bytes[i].getValue();

            	try
            	{
                    getMemoryBlock().setValue(address.subtract(fParent.getBigBaseAddress()), byteValue);
                }
                catch(Exception e)
                {
					MemoryViewUtil.openError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_WRITE_MEMORY"), "", e);

                    logError(
                        TraditionalRenderingMessages
                            .getString("TraditionalRendering.FAILURE_WRITE_MEMORY"), e); //$NON-NLS-1$
                }
            }
            
            clearEditBuffer();
        }

        protected void setEditedValue(BigInteger address, MemoryByte[] bytes)
        {
            assert Thread.currentThread().equals(
                Display.getDefault().getThread()) : TraditionalRenderingMessages
                .getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            fEditBuffer.put(address, bytes);
            Rendering.this.redrawPanes();
        }
    }

    public void setVisibleAddressBar(boolean visible)
    {
    	fAddressBarControl.setVisible(visible);
    	if(visible)
    	{
    		String selectedStr = "0x" + getCaretAddress().toString(16);
    		Text text = fAddressBar.getExpressionWidget();
    		text.setText(selectedStr);
    		text.setSelection(0, text.getCharCount());	
    		fAddressBar.getExpressionWidget().setFocus();
    	}
    	
        layout(true);
        layoutPanes();
    }
    
    public void setDirty(boolean needRefresh)
    {
    	fCacheDirty = needRefresh; 	
    }

    public boolean isDirty()
    {
    	return fCacheDirty; 	
    }

    public void dispose()
    {
        if(fViewportCache != null)
        {
            fViewportCache.dispose();
            fViewportCache = null;
        }
        super.dispose();
    }

    class Selection
    {
        private BigInteger fStartHigh;
        private BigInteger fStartLow;

        private BigInteger fEndHigh;
        private BigInteger fEndLow;

        protected void clear()
        {
            fEndHigh = fEndLow = fStartHigh = fStartLow = null;
            redrawPanes();
        }
        
        protected boolean hasSelection()
        {
        	return fStartHigh != null && fStartLow != null
        		&& fEndHigh != null && fEndLow != null;
        }
        
        protected boolean isSelected(BigInteger address)
        {
            // do we have valid start and end addresses
            if(getEnd() == null || getStart() == null)
                return false;
    
            // if end is greater than start
            if(getEnd().compareTo(getStart()) >= 0)
            {
                if(address.compareTo(getStart()) >= 0
                    && address.compareTo(getEnd()) < 0)
                    return true;
            }
            // if start is greater than end
            else if(getStart().compareTo(getEnd()) >= 0)
            {
                if(address.compareTo(getEnd()) >= 0
                    && address.compareTo(getStart()) < 0)
                    return true;
            }
            
            return false;
        }
    
        protected void setStart(BigInteger high, BigInteger low)
        {
            if(high == null && low == null)
            {
                if(fStartHigh != null && fStartLow != null)
                {
                    fStartHigh = null;
                    fStartLow = null;
                    redrawPanes();
                }
    
                return;
            }
    
            boolean changed = false;
            
            if(fStartHigh == null || !high.equals(fStartHigh))
            {
                fStartHigh = high;
                changed = true;
            }
            
            if(fStartLow == null || !low.equals(fStartLow))
            {
                fStartLow = low;
                changed = true;
            }
            
            if(changed)
                redrawPanes();
        }
    
        protected void setEnd(BigInteger high, BigInteger low)
        {
            if(high == null && low == null)
            {
                if(fEndHigh != null && fEndLow != null)
                {
                    fEndHigh = null;
                    fEndLow = null;
                    redrawPanes();
                }
    
                return;
            }
    
            boolean changed = false;
            
            if(fEndHigh == null || !high.equals(fEndHigh))
            {
                fEndHigh = high;
                changed = true;
            }
            
            if(fEndLow == null || !low.equals(fEndLow))
            {
                fEndLow = low;
                changed = true;
            }
            
            if(changed)
                redrawPanes();
        }
    
        protected BigInteger getHigh()
        {
        	if(!hasSelection())
        		return null;
        	
        	return getStart().max(getEnd());
        }
        
        protected BigInteger getLow()
        {
        	if(!hasSelection())
        		return null;
        	
        	return getStart().min(getEnd());
        }
        
        protected BigInteger getStart()
        {
            // if there is no start, return null
            if(fStartHigh == null)
                return null;
            
            // if there is no end, return the high address of the start
            if(fEndHigh == null)
                return fStartHigh;
            
            // if Start High/Low equal End High/Low, return a low start and high end
            if(fStartHigh.equals(fEndHigh) 
                && fStartLow.equals(fEndLow))
                return fStartLow;
            
            BigInteger differenceEndToStartHigh = fEndHigh.subtract(fStartHigh).abs();
            BigInteger differenceEndToStartLow = fEndHigh.subtract(fStartLow).abs();
            
            // return the start high or start low based on which creates a larger selection
            if(differenceEndToStartHigh.compareTo(differenceEndToStartLow) > 0)
                return fStartHigh;
            else 
                return fStartLow;
        }
    
        protected BigInteger getEnd()
        {
            // if there is no end, return null
            if(fEndHigh == null)
                return null;
            
            // if Start High/Low equal End High/Low, return a low start and high end
            if(fStartHigh.equals(fEndHigh) 
                && fStartLow.equals(fEndLow))
                return fStartHigh;
            
            BigInteger differenceStartToEndHigh = fStartHigh.subtract(fEndHigh).abs();
            BigInteger differenceStartToEndLow = fStartHigh.subtract(fEndLow).abs();
            
            // return the start high or start low based on which creates a larger selection
            if(differenceStartToEndHigh.compareTo(differenceStartToEndLow) >= 0)
                return fEndHigh;
            else 
                return fEndLow;
        }
    }
    
    protected void setPaneVisible(int pane, boolean visible)
    {
        switch(pane)
        {
            case PANE_ADDRESS:
                fAddressPane.setPaneVisible(visible);
                break;
            case PANE_BINARY:
                fBinaryPane.setPaneVisible(visible);
                break;
            case PANE_TEXT:
                fTextPane.setPaneVisible(visible);
                break;
        }

        fireSettingsChanged();
        layoutPanes();
    }

    protected boolean getPaneVisible(int pane)
    {
        switch(pane)
        {
            case PANE_ADDRESS:
                return fAddressPane.isPaneVisible();
            case PANE_BINARY:
                return fBinaryPane.isPaneVisible();
            case PANE_TEXT:
                return fTextPane.isPaneVisible();
            default:
                return false;
        }
    }

    protected void packColumns()
    {
        int availableWidth = Rendering.this.getSize().x;

        if(fAddressPane.isPaneVisible())
        {
            availableWidth -= fAddressPane.computeSize(0, 0).x;
            availableWidth -= Rendering.this.getRenderSpacing() * 2;
        }

        int combinedWidth = 0;

        if(fBinaryPane.isPaneVisible())
            combinedWidth += fBinaryPane.getCellWidth();

        if(fTextPane.isPaneVisible())
            combinedWidth += fTextPane.getCellWidth();

        if(getColumnsSetting() == Rendering.COLUMNS_AUTO_SIZE_TO_FIT)
        {
	        if(combinedWidth == 0)
	            fColumnCount = 0;
	        else
	        {
	            fColumnCount = availableWidth / combinedWidth;
	            if(fColumnCount == 0)
	            	fColumnCount = 1; // paint one column even if only part can show in view
	        }
        }
        else
        {
        	fColumnCount = getColumnsSetting();
        }
        
        try
        {
	        // Update the number of bytes per row;
	        // the max and min scroll range and the current thumb nail position.        
	        fBytesPerRow = getBytesPerColumn() * getColumnCount();
	        BigInteger difference = getMemoryBlockEndAddress().subtract(getMemoryBlockStartAddress()).add(BigInteger.ONE);
	        BigInteger maxScrollRange = difference.divide(BigInteger.valueOf(getAddressableCellsPerRow()));
	        if(maxScrollRange.multiply(BigInteger.valueOf(getAddressableCellsPerRow())).compareTo(difference) != 0)
	        	maxScrollRange = maxScrollRange.add(BigInteger.ONE);
	        
	        // support targets with an addressable size greater than 1
	        maxScrollRange = maxScrollRange.divide(BigInteger.valueOf(getAddressableSize()));
	                
	        getVerticalBar().setMinimum(1);
	        getVerticalBar().setMaximum(maxScrollRange.intValue());
	        getVerticalBar().setIncrement(1);
	        getVerticalBar().setPageIncrement(this.getRowCount() -1);
	    	//TW FIXME conversion of slider to scrollbar
	        // fScrollBar.setToolTipText(Rendering.this.getAddressString(fViewportAddress));
	        setCurrentScrollSelection();
        }
        catch(Exception e)
        {
        	// FIXME precautionary
        }
        
        Rendering.this.redraw();
     	Rendering.this.redrawPanes();
    }

    protected AbstractPane[] getRenderingPanes()
    {
        return new AbstractPane[] { fAddressPane, fBinaryPane,
            fTextPane };
    }

    protected int getCellPadding()
    {
        return fCellPadding;
    }

    protected int getRenderSpacing()
    {
        return fPaneSpacing;
    }

    protected void refresh()
    {
    	if(!this.isDisposed())
    	{
    		if(this.isVisible() && fViewportCache != null)
    		{
    			fViewportCache.refresh();
    		}
    		else
    		{
    			setDirty(true);
    			fParent.updateRenderingLabels();
    		}
    	}
    }
    
    protected void archiveDeltas()
    {
    	fViewportCache.archiveDeltas();
    }

    protected void gotoAddress(BigInteger address)
    {
    	// Ensure that the GoTo address is within the addressable range
    	if((address.compareTo(this.getMemoryBlockStartAddress())< 0) ||
    	   (address.compareTo(this.getMemoryBlockEndAddress()) > 0))
    	{
    		return;
    	}
    	
        fViewportAddress = address; // TODO update fCaretAddress
        redrawPanes();
    }

    protected void setViewportStartAddress(BigInteger newAddress)
    {
        fViewportAddress = newAddress;
    }
    
    protected BigInteger getViewportStartAddress()
    {
        return fViewportAddress;
    }

    protected BigInteger getViewportEndAddress()
    {
        return fViewportAddress.add(BigInteger.valueOf(this.getBytesPerRow() * getRowCount() / getAddressableSize()));
    }

    protected String getAddressString(BigInteger address)
    {
        StringBuffer addressString = new StringBuffer(address.toString(16)
            .toUpperCase());
        for(int chars = getAddressBytes() * 2 - addressString.length(); chars > 0; chars--)
        {
            addressString.insert(0, '0');
        }
        addressString.insert(0, "0x"); //$NON-NLS-1$

        return addressString.toString();
    }

    protected int getAddressBytes()
    {
        return fParent.getAddressSize();
    }

    protected int getColumnCount()
    {
        return fColumnCount;
    }

    public int getColumnsSetting() 
    {
		return fColumnsSetting;
	}

	public void setColumnsSetting(int columns) 
	{
		if(fColumnsSetting != columns)
		{
			fColumnsSetting = columns;
			fireSettingsChanged();
	        layoutPanes();
		}
	}

	protected void ensureVisible(BigInteger address)
    {
        BigInteger viewportStart = this.getViewportStartAddress();
        BigInteger viewportEnd = this.getViewportEndAddress();

        boolean isAddressBeforeViewportStart = address.compareTo(viewportStart) < 0;
        boolean isAddressAfterViewportEnd = address.compareTo(viewportEnd) > 0;

        if(isAddressBeforeViewportStart || isAddressAfterViewportEnd)
            gotoAddress(address);
    }

    protected int getRowCount()
    {
        int rowCount = 0;
        Control panes[] = getRenderingPanes();
        for(int i = 0; i < panes.length; i++)
        {
            if(panes[i] instanceof AbstractPane)
                rowCount = Math.max(rowCount,
                    ((AbstractPane) panes[i]).getRowCount());
        }

        return rowCount;
    }

    protected int getBytesPerColumn()
    {
        return fBytesPerColumn;
    }

    protected int getBytesPerRow()
    {
        return fBytesPerRow;
    }
    
    protected int getAddressableCellsPerRow()
    {
    	return getBytesPerRow() / getAddressableSize();
    }
    
    protected int getAddressesPerColumn()
    {
    	return this.getBytesPerColumn() / getAddressableSize();
    }

    /**
	 *  @return Set current scroll selection
	 */
	protected void setCurrentScrollSelection()
	{
		BigInteger selection = getViewportStartAddress().divide(
			BigInteger.valueOf(getAddressableCellsPerRow()).add(BigInteger.ONE));
		getVerticalBar().setSelection(selection.intValue());
		fCurrentScrollSelection = selection.intValue();
	}
    
    /**
	 * @return start address of the memory block
	 */
	protected BigInteger getMemoryBlockStartAddress()
	{
		if(fMemoryBlockStartAddress == null)
			fMemoryBlockStartAddress =  fParent.getMemoryBlockStartAddress();
		if(fMemoryBlockStartAddress == null)
			fMemoryBlockStartAddress = BigInteger.ZERO;
		
		return fMemoryBlockStartAddress;
	}
	
	/**
	 * @return end address of the memory block
	 */
	protected BigInteger getMemoryBlockEndAddress()
	{
		if(fMemoryBlockEndAddress == null)
			fMemoryBlockEndAddress = fParent.getMemoryBlockEndAddress();
		
		return fMemoryBlockEndAddress;
	}

    protected int getRadix()
    {
        return fRadix;
    }

    protected int getNumericRadix(int radix)
    {
        switch(radix)
        {
            case RADIX_BINARY:
                return 2;
            case RADIX_OCTAL:
                return 8;
            case RADIX_DECIMAL_SIGNED:
            case RADIX_DECIMAL_UNSIGNED:
                return 10;
            case RADIX_HEX:
                return 16;
        }

        return -1;
    }

    protected void setRadix(int mode)
    {
        if(fRadix == mode)
            return;

        fRadix = mode;
        fireSettingsChanged();
        layoutPanes();
    }

    protected void setTextMode(int mode)
    {
    	fTextMode = mode;
    	
        fireSettingsChanged();
        layoutPanes();
    }
    
    protected int getTextMode()
    {
    	return fTextMode;
    }
    
    protected String getCharacterSet(int mode)
    {
    	switch(mode)
    	{
    		case Rendering.TEXT_UTF8:
    			return "UTF8";
    		case Rendering.TEXT_UTF16:
    			return "UTF16";
    		case Rendering.TEXT_USASCII:
    			return "US-ASCII";
    		case Rendering.TEXT_ISO_8859_1:
    		default:
    			return "ISO-8859-1";
    	}
    }

    protected int getBytesPerCharacter()
    {
    	if(fTextMode == Rendering.TEXT_UTF16)
    		return 2;
    		
        return 1;
    }

    protected boolean isLittleEndian()
    {
        return fLittleEndian;
    }

    protected void setLittleEndian(boolean enable)
    {
        if(fLittleEndian == enable)
            return;

        fLittleEndian = enable;
        fireSettingsChanged();
        layoutPanes();
    }

    protected void setBytesPerColumn(int byteCount)
    {
        if(fBytesPerColumn != byteCount)
        {
            fBytesPerColumn = byteCount;
            fireSettingsChanged();
            layoutPanes();
        }
    }

    protected void redrawPanes()
    {
    	if(this.isVisible())
    	{
	        if(fAddressPane.isPaneVisible())
	        {
	            fAddressPane.redraw();
	            fAddressPane.setRowCount();
	            if(fAddressPane.isFocusControl())
	                fAddressPane.updateCaret();
	        }
	
	        if(fBinaryPane.isPaneVisible())
	        {
	            fBinaryPane.redraw();
	            fBinaryPane.setRowCount();
	            if(fBinaryPane.isFocusControl())
	                fBinaryPane.updateCaret();
	        }
	
	        if(fTextPane.isPaneVisible())
	        {
	            fTextPane.redraw();
	            fTextPane.setRowCount();
	            if(fTextPane.isFocusControl())
	                fTextPane.updateCaret();
	        }
    	}
    	
    	fParent.updateRenderingLabels();
    }

    private void layoutPanes()
    {
        packColumns();
        layout(true);

        redraw();
        redrawPanes();
    }

    private void fireSettingsChanged()
    {
        fAddressPane.settingsChanged();
        fBinaryPane.settingsChanged();
        fTextPane.settingsChanged();
    }
    
    protected void copyAddressToClipboard()
    {
    	 Clipboard clip = null;
         try
         {
             clip = new Clipboard(getDisplay());
             
             String addressString = "0x" + getCaretAddress().toString(16);

             TextTransfer plainTextTransfer = TextTransfer.getInstance();
             clip.setContents(new Object[] { addressString },
                 new Transfer[] { plainTextTransfer });
         }
         finally
         {
             if(clip != null)
             {
                 clip.dispose();
             }
         }
    }

    static final char[] hexdigits = { '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    
    protected String getRadixText(MemoryByte bytes[], int radix,
            boolean isLittleEndian)
    {
        boolean readable = true;
        for(int i = 0; i < bytes.length; i++)
            if(!bytes[i].isReadable())
                readable = false;

        if(readable)
        {
            // see if we need to swap the data or not.  if the bytes are BE
            // and we want to view in LE then we need to swap.  if the bytes
            // are LE and we want to view BE then we need to swap.
            boolean needsSwap = false;
            boolean bytesAreLittleEndian = !bytes[0].isBigEndian();
            if ((isLittleEndian && !bytesAreLittleEndian) || (!isLittleEndian && bytesAreLittleEndian))
            	needsSwap = true;

            switch(radix)
            {
                case Rendering.RADIX_HEX:
                case Rendering.RADIX_OCTAL:
                case Rendering.RADIX_BINARY:
                {
    				long value = 0;
                    if(needsSwap)
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value = value << 8;
                            value = value
                                | (bytes[bytes.length - 1 - i].getValue() & 0xFF);
                        }
                    }
                    else
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value = value << 8;
                            value = value | (bytes[i].getValue() & 0xFF);
                        }
                    }

                    char buf[] = new char[getRadixCharacterCount(radix,
                        bytes.length)];

                    switch(radix)
                    {
	                    case Rendering.RADIX_BINARY:
	                    {
	                        for(int i = buf.length - 1; i >= 0; i--)
	                        {
	                            buf[i] = hexdigits[(int) (value & 1)];
	                            value = value >>> 1;
	                        }
	                        break;
	                    }
	                    case Rendering.RADIX_OCTAL:
                        {
                            for(int i = buf.length - 1; i >= 0; i--)
                            {
                                buf[i] = hexdigits[(int) (value & 7)];
                                value = value >>> 3;
                            }
                            break;
                        }
                        case Rendering.RADIX_HEX:
                        {
                            for(int i = buf.length - 1; i >= 0; i--)
                            {
                                buf[i] = hexdigits[(int) (value & 15)];
                                value = value >>> 4;
                            }
                            break;
                        }
                    }

                    return new String(buf);
                }
                case Rendering.RADIX_DECIMAL_UNSIGNED:
                case Rendering.RADIX_DECIMAL_SIGNED:
                {
                    boolean isSignedType = radix == Rendering.RADIX_DECIMAL_SIGNED ? true
                        : false;

                    int textWidth = getRadixCharacterCount(radix, bytes.length);

                    char buf[] = new char[textWidth];

                    byte[] value = new byte[bytes.length + 1];

    				if(needsSwap)
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value[bytes.length - i] = bytes[i].getValue();
                        }
                    }
                    else
                    {
                        for(int i = 0; i < bytes.length; i++)
                        {
                            value[i + 1] = bytes[i].getValue();
                        }
                    }
                    
                    BigInteger bigValue;
                    boolean isNegative = false;
                    if(isSignedType && (value[1] & 0x80) != 0)
                    {
                        value[0] = -1;
                        isNegative = true;
                        bigValue = new BigInteger(value).abs();
                    }
                    else
                    {
                        value[0] = 0;
                        bigValue = new BigInteger(value);
                    }
                    
                    for(int i = 0; i < textWidth; i++)
                    {
                        BigInteger divideRemainder[] = bigValue.divideAndRemainder(
                        	BigInteger.valueOf(10));
                        int remainder = divideRemainder[1].intValue();
                        buf[textWidth - 1 - i] = hexdigits[remainder % 10];
                        bigValue = divideRemainder[0];
                    }
                    
                    if(isSignedType)
                    {
                        buf[0] = isNegative ? '-' : ' ';
                    }

                    return new String(buf);
                }
            }
        }

        StringBuffer errorText = new StringBuffer();
        for(int i = getRadixCharacterCount(radix, bytes.length); i > 0; i--)
            errorText.append('?');

        return errorText.toString();
    }

    protected int getRadixCharacterCount(int radix, int bytes)
    {
        switch(radix)
        {
            case Rendering.RADIX_HEX:
                return bytes * 2;
            case Rendering.RADIX_BINARY:
                return bytes * 8;
            case Rendering.RADIX_OCTAL:
            {
                switch(bytes)
                {
                    case 1:
                        return 3;
                    case 2:
                        return 6;
                    case 4:
                        return 11;
                    case 8:
                        return 22;
                }
            }
            case Rendering.RADIX_DECIMAL_UNSIGNED:
            {
                switch(bytes)
                {
                    case 1:
                        return 3;
                    case 2:
                        return 5;
                    case 4:
                        return 10;
                    case 8:
                        return 20;
                }
            }
            case Rendering.RADIX_DECIMAL_SIGNED:
            {
                switch(bytes)
                {
                    case 1:
                        return 4;
                    case 2:
                        return 6;
                    case 4:
                        return 11;
                    case 8:
                        return 21;
                }
            }
        }

        return 0;
    }

    protected static boolean isValidEditCharacter(char character)
    {
        return (character >= '0' && character <= '9')
            || (character >= 'a' && character <= 'z')
            || (character >= 'A' && character <= 'Z') || character == '-'
            || character == ' ';
    }
    
    protected String formatText(MemoryByte[] memoryBytes,
        boolean isLittleEndian, int textMode)
    {
    	// check memory byte for unreadable bytes
    	boolean readable = true;
        for(int i = 0; i < memoryBytes.length; i++)
            if(!memoryBytes[i].isReadable())
                readable = false;
        
        // if any bytes are not readable, return ?'s
        if(!readable)
        {
            StringBuffer errorText = new StringBuffer();
            for(int i = memoryBytes.length; i > 0; i--)
                errorText.append('?');
            return errorText.toString();
        }

        // TODO
        // does endian mean anything for text? ah, unicode?
        
        // create byte array from MemoryByte array
        byte bytes[] = new byte[memoryBytes.length];
        for(int i = 0; i < bytes.length; i++)
        {
            bytes[i] = memoryBytes[i].getValue();
        }
        
        // replace invalid characters with '.'
        // maybe there is a way to query the character set for
        // valid characters?
        
        // replace invalid US-ASCII with '.'
        if(textMode == Rendering.TEXT_USASCII)
        {
        	for(int i = 0; i < bytes.length; i++)
        	{
        		int byteValue = bytes[i];
        		if(byteValue < 0)
        			byteValue += 256;
        		
        		if(byteValue < 0x20 || byteValue > 0x7e)
        			bytes[i] = '.';
        	}
        }
        
        // replace invalid ISO-8859-1 with '.'
        if(textMode == Rendering.TEXT_ISO_8859_1)
        {
        	for(int i = 0; i < bytes.length; i++)
        	{
        		int byteValue = bytes[i];
        		if(byteValue < 0)
        			byteValue += 256;
        		
        		if(byteValue < 0x20 || 
        				(byteValue >= 0x7f && byteValue < 0x9f))
        			bytes[i] = '.';
        	}
        }
        
        try
    	{
        	// convert bytes to string using desired character set
    		StringBuffer buf = new StringBuffer(new String(bytes, this.getCharacterSet(textMode)));
    		
    		// pad string to (byte count - string length) with spaces
    		for(int i = 0; i < memoryBytes.length - buf.length(); i++)
    			buf.append(' ');
    		return buf.toString();
    	}
    	catch(Exception e)
    	{
    		// return ?s the length of byte count
    		StringBuffer buf = new StringBuffer();
    		for(int i = 0; i < memoryBytes.length - buf.length(); i++)
    			buf.append('?');
    		return buf.toString();
    	}   
    }

}
