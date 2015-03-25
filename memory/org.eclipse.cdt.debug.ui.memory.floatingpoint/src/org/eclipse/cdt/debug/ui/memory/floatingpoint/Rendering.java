/*******************************************************************************
 * Copyright (c) 2006, 2014 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.FPDataType;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.internal.ui.views.memory.renderings.GoToAddressComposite;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;


@SuppressWarnings("restriction")
public class Rendering extends Composite implements IDebugEventSetListener
{
    // The IMemoryRendering parent

    private FPRendering fParent;

    // Controls

    protected FPAddressPane fAddressPane;
    protected FPDataPane fDataPane;
    private GoToAddressComposite fAddressBar;
    protected Control fAddressBarControl;
    private Selection fSelection = new Selection();

    // Internal management

    BigInteger fViewportAddress = null;             // Default visibility for performance
    BigInteger fMemoryBlockStartAddress = null;     // Starting address
    BigInteger fMemoryBlockEndAddress = null;       // Ending address
    protected BigInteger fBaseAddress = null;       // Base address
    protected int fColumnCount = 0;                 // Auto calculate can be disabled by user, making this user settable
    protected int fBytesPerRow = 0;                 // Number of bytes per row are displayed
    int fScrollSelection = 0;               // Scroll selection
    private BigInteger fCaretAddress = null;        // Caret/cursor position
    private boolean fCellEditState = false;         // Cell editing mode:  'true' = currently editing a cell; 'false' = not editing a cell
    private BigInteger cellEditAddress = null;      // The address of the cell currently being edited
    private BigInteger memoryAddress = null;        // The memory address associated with the cell that currently being edited
    private StringBuffer fEditBuffer = null;        // Character buffer used during editing
    static boolean initialDisplayModeSet = false;   // Initial display mode been set to the same endianness as the target

    // Constants used to identify the panes

    public final static int PANE_ADDRESS = 1;
    public final static int PANE_DATA = 2;

    // Decimal precision used when converting between scroll units and number of memory
    // rows. Calculations do not need to be exact; two decimal places is good enough.

    static private final MathContext SCROLL_CONVERSION_PRECISION = new MathContext(2);

    // Constants used to identify text, maybe java should be queried for all available sets

    public final static int TEXT_ISO_8859_1 = 1;
    public final static int TEXT_USASCII = 2;
    public final static int TEXT_UTF8 = 3;
    protected final static int TEXT_UTF16 = 4;

    // Internal constants

    public final static int COLUMNS_AUTO_SIZE_TO_FIT = 0;

    // View internal settings

    private int fCellPadding =  2;
    private int fPaneSpacing = 16;
    private String fPaddingString = " "; //$NON-NLS-1$

    // Flag whether the memory cache is dirty

    private boolean fCacheDirty = false;

    // Update modes

    public final static int UPDATE_ALWAYS = 1;
    public final static int UPDATE_ON_BREAKPOINT = 2;
    public final static int UPDATE_MANUAL = 3;
    public int fUpdateMode = UPDATE_ALWAYS;

    // Constants for cell-width calculations

    private static final int DECIMAL_POINT_SIZE = 1;
    private static final int SIGN_SIZE = 1;
    private static final int EXPONENT_CHARACTER_SIZE = 1;
    private static final int EXPONENT_VALUE_SIZE = 3;

    // User settings

    private FPDataType fFPDataType = FPDataType.FLOAT;          // Default to float data type
    private int fDisplayedPrecision = 8;                        // The default number of digits of displayed precision
    private int fCharsPerColumn = charsPerColumn();             // Figure out the initial cell-width size
    private int fColumnsSetting = COLUMNS_AUTO_SIZE_TO_FIT;     // Default column setting
    private boolean fIsTargetLittleEndian  = true;              // Default target endian setting
    private boolean fIsDisplayLittleEndian = true;              // Default display endian setting
    private boolean fEditInserMode = false;                     // Insert mode:  true = replace existing number, false = overstrike

    // Constructors

    public Rendering(Composite parent, FPRendering renderingParent)
    {
        super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);
        this.setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME)); // TODO: internal?
        this.fParent = renderingParent;

        // Initialize the viewport start

        if (fParent.getMemoryBlock() != null)
        {
        	// This is base address from user.
        	// Honor it if the block has no limits or the base is within the block limits.
        	// Fix Bug 414519.  
        	BigInteger base = fParent.getBigBaseAddress();

            fViewportAddress = fParent.getMemoryBlockStartAddress();

            // The viewport address will be null if memory may be retrieved at any
            // address less than this memory block's base.  If so use the base address.
            if (fViewportAddress == null)
                fViewportAddress = base;
            else {
            	BigInteger blockEndAddr = fParent.getMemoryBlockEndAddress();
            	if (base.compareTo(fViewportAddress) > 0) {
            		if (blockEndAddr == null || base.compareTo(blockEndAddr) < 0)
            			fViewportAddress = base;
            	}
            }

            fBaseAddress = fViewportAddress;
        }

        // Instantiate the panes, TODO default visibility from state or plugin.xml?

        this.fAddressPane = createAddressPane();
        this.fDataPane = createDataPane();

        fAddressBar = new GoToAddressComposite();
        fAddressBarControl = fAddressBar.createControl(parent);
        Button button = fAddressBar.getButton(IDialogConstants.OK_ID);

        if (button != null)
        {
            button.addSelectionListener(new SelectionAdapter()
            {

                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    doGoToAddress();
                }
            });

            button = fAddressBar.getButton(IDialogConstants.CANCEL_ID);
            if (button != null)
            {
                button.addSelectionListener(new SelectionAdapter()
                {
                    @Override
                    public void widgetSelected(SelectionEvent e)
                    {
                        setVisibleAddressBar(false);
                    }
                });
            }
        }

        fAddressBar.getExpressionWidget().addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
                doGoToAddress();
            }
        });

        fAddressBar.getExpressionWidget().addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.keyCode == SWT.ESC)
                    setVisibleAddressBar(false);
                super.keyPressed(e);
            }
        });

        this.fAddressBarControl.setVisible(false);

        getHorizontalBar().addSelectionListener(createHorizontalBarSelectionListener());
        getVerticalBar().addSelectionListener(createVerticalBarSelectinListener());

        this.addPaintListener(new PaintListener()
        {
            @Override
            public void paintControl(PaintEvent pe)
            {
                pe.gc.setBackground(Rendering.this.getFPRendering().getColorBackground());
                pe.gc.fillRectangle(0, 0, Rendering.this.getBounds().width, Rendering.this.getBounds().height);
            }
        });

        setLayout();

        this.addControlListener(new ControlListener()
        {
            @Override
            public void controlMoved(ControlEvent ce)
            {
            }

            @Override
            public void controlResized(ControlEvent ce)
            {
                packColumns();
            }
        });

        DebugPlugin.getDefault().addDebugEventListener(this);
    }

    // Determine how many characters are allowed in the column

    private int charsPerColumn()
    {
        return fDisplayedPrecision + DECIMAL_POINT_SIZE + SIGN_SIZE + EXPONENT_CHARACTER_SIZE + EXPONENT_VALUE_SIZE + fCellPadding;
    }

    // Establish the visible layout of the view

    protected void setLayout()
    {
        this.setLayout(new Layout()
        {
            @Override
            public void layout(Composite composite, boolean changed)
            {
                int xOffset = 0;

                if (Rendering.this.getHorizontalBar().isVisible())
                    xOffset = Rendering.this.getHorizontalBar().getSelection();

                int x = xOffset * -1;
                int y = 0;

                if (fAddressBarControl.isVisible())
                {
                    fAddressBarControl.setBounds(0, 0, Rendering.this.getBounds().width, fAddressBarControl.computeSize(100, 30).y); // FIXME
                    // y = fAddressBarControl.getBounds().height;
                }

                if (fAddressPane.isPaneVisible())
                {
                    fAddressPane.setBounds(x, y, fAddressPane.computeSize(0, 0).x, Rendering.this.getBounds().height - y);
                    x = fAddressPane.getBounds().x + fAddressPane.getBounds().width;
                }

                if (fDataPane.isPaneVisible())
                {
                    fDataPane.setBounds(x, y, fDataPane.computeSize(0, 0).x, Rendering.this.getBounds().height - y);
                    x = fDataPane.getBounds().x + fDataPane.getBounds().width;
                }

                ScrollBar horizontal = Rendering.this.getHorizontalBar();

                horizontal.setVisible(true);
                horizontal.setMinimum(0);
                horizontal.setMaximum(fDataPane.getBounds().x + fDataPane.getBounds().width + xOffset);
                @SuppressWarnings("unused")
                int temp = horizontal.getMaximum();
                horizontal.setThumb(getClientArea().width);
                horizontal.setPageIncrement(40);    // TODO ?
                horizontal.setIncrement(20);        // TODO ?
            }

            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache)
            {
                return new Point(100, 100); // dummy data
            }
        });
    }

    // Handles for the caret/cursor movement keys

    protected void handleDownArrow()
    {
        fViewportAddress = fViewportAddress.add(BigInteger.valueOf(getAddressableCellsPerRow()));
        ensureViewportAddressDisplayable();
        redrawPanes();
    }

    protected void handleUpArrow()
    {
        fViewportAddress = fViewportAddress.subtract(BigInteger.valueOf(getAddressableCellsPerRow()));
        ensureViewportAddressDisplayable();
        redrawPanes();
    }

    protected void handlePageDown()
    {
        fViewportAddress = fViewportAddress.add(BigInteger.valueOf(getAddressableCellsPerRow() * (Rendering.this.getRowCount() - 1)));
        ensureViewportAddressDisplayable();
        redrawPanes();
    }

    protected void handlePageUp()
    {
        fViewportAddress = fViewportAddress.subtract(BigInteger.valueOf(getAddressableCellsPerRow() * (Rendering.this.getRowCount() - 1)));
        ensureViewportAddressDisplayable();
        redrawPanes();
    }

    protected SelectionListener createHorizontalBarSelectionListener()
    {
        return new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                Rendering.this.layout();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent se)
            {
                // Do nothing
            }
        };
    }

    protected SelectionListener createVerticalBarSelectinListener()
    {
        return new SelectionListener()
        {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                switch (se.detail)
                {
                    case SWT.ARROW_DOWN:
                        handleDownArrow();
                        break;

                    case SWT.PAGE_DOWN:
                        handlePageDown();
                        break;

                    case SWT.ARROW_UP:
                        handleUpArrow();
                        break;

                    case SWT.PAGE_UP:
                        handlePageUp();
                        break;

                    case SWT.SCROLL_LINE:
                        // See: BUG 203068 selection event details broken on GTK < 2.6

                    default:
                    {
                        if (getVerticalBar().getSelection() == getVerticalBar().getMinimum())
                        {
                            // Set view port start address to the start address of the Memory Block
                            fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
                        }
                        else if (getVerticalBar().getSelection() == getVerticalBar().getMaximum())
                        {
                            // The view port end address should be less or equal to the the end address of the Memory Block
                            // Set view port address to be bigger than the end address of the Memory Block for now
                            // and let ensureViewportAddressDisplayable() to figure out the correct view port start address
                            fViewportAddress = Rendering.this.getMemoryBlockEndAddress();
                        }
                        else
                        {
                            // Figure out the delta, ignore events with no delta
                            int deltaScroll = getVerticalBar().getSelection() - fScrollSelection;
                            if (deltaScroll == 0) break;
                            BigInteger deltaRows = scrollbar2rows(deltaScroll);
                            BigInteger newAddress = fViewportAddress.add(BigInteger.valueOf(getAddressableCellsPerRow()).multiply(deltaRows));
                            fViewportAddress = newAddress;
                        }

                        ensureViewportAddressDisplayable();

                        // Update tooltip; FIXME conversion from slider to scrollbar
                        // getVerticalBar().setToolTipText(Rendering.this.getAddressString(fViewportAddress));

                        // Update the addresses in the Address pane.

                        if (fAddressPane.isPaneVisible())
                            fAddressPane.redraw();

                        redrawPanes();

                        break;
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent se)
            {
                // do nothing
            }
        };
    }

    protected FPAddressPane createAddressPane()
    {
        return new FPAddressPane(this);
    }

    protected FPDataPane createDataPane()
    {
        return new FPDataPane(this);
    }

    public FPRendering getFPRendering() // TODO rename
    {
        return fParent;
    }

    protected void setCaretAddress(BigInteger address)
    {
        fCaretAddress = address;
    }

    protected BigInteger getCaretAddress()
    {
        // Return the caret address if it has been set.  Otherwise return the viewport address.
        // When the rendering is first created, the caret is unset until the user clicks somewhere
        // in the rendering. It also reset (unset) when the user gives us a new viewport address

        return (fCaretAddress != null) ? fCaretAddress : fViewportAddress;
    }

    void doGoToAddress()
    {
        try
        {
            BigInteger address = fAddressBar.getGoToAddress(this.getMemoryBlockStartAddress(), this.getCaretAddress());
            getFPRendering().gotoAddress(address);
            setVisibleAddressBar(false);
        }
        catch (NumberFormatException e1)
        {
            // FIXME log?
        }
    }

    // Ensure that all addresses displayed are within the addressable range
    protected void ensureViewportAddressDisplayable()
    {
        if (fViewportAddress.compareTo(Rendering.this.getMemoryBlockStartAddress()) < 0)
        {
            fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
        }
        else if (getViewportEndAddress().compareTo(getMemoryBlockEndAddress().add(BigInteger.ONE)) > 0)
        {
            fViewportAddress = getMemoryBlockEndAddress().subtract(BigInteger.valueOf(getAddressableCellsPerRow() * getRowCount() - 1));
        }

        setScrollSelection();
    }

    public FPIMemorySelection getSelection()
    {
        return fSelection;
    }

    protected int getHistoryDepth()
    {
        return fViewportCache.getHistoryDepth();
    }

    protected void setHistoryDepth(int depth)
    {
        fViewportCache.setHistoryDepth(depth);
    }

    public void logError(String message, Exception e)
    {
        Status status = new Status(IStatus.ERROR, fParent.getRenderingId(), DebugException.INTERNAL_ERROR, message, e);
        FPRenderingPlugin.getDefault().getLog().log(status);
    }

    public void handleFontPreferenceChange(Font font)
    {
        setFont(font);

        Control controls[] = this.getRenderingPanes();
        for (int index = 0; index < controls.length; index++)
            controls[index].setFont(font);

        packColumns();
        layout(true);
    }

    public void setPaddingString(String padding)
    {
        fPaddingString = padding;
        refresh();
    }

    public char getPaddingCharacter()
    {
        return fPaddingString.charAt(0); // use only the first character
    }

    static int suspendCount = 0;

    @Override
    public void handleDebugEvents(DebugEvent[] events)
    {
        if (this.isDisposed()) return;

        boolean isChangeOnly = false;
        boolean isSuspend = false;
        boolean isBreakpointHit = false;

        for (int index = 0; index < events.length; index++)
        {
            if (events[0].getSource() instanceof IDebugElement)
            {
                final int kind = events[index].getKind();
                final int detail = events[index].getDetail();
                final IDebugElement source = (IDebugElement) events[index].getSource();

                /*
                 * We have to make sure we are comparing memory blocks here. It pretty much is now the
                 * case that the IDebugTarget is always null.  Almost no one in the Embedded Space  is
                 * using anything but CDT/DSF or CDT/TCF at this point. The older CDI stuff will still
                 * be using the old Debug Model API. But this will generate the same memory block  and
                 * a legitimate IDebugTarget which will match properly.
                 */
                if( source.equals( getMemoryBlock() ) && source.getDebugTarget() == getMemoryBlock().getDebugTarget() )
                {
                    if ((detail & DebugEvent.BREAKPOINT) != 0) isBreakpointHit = true;

                    if (kind == DebugEvent.SUSPEND)
                    {
                        handleSuspendEvent(detail);
                        isSuspend = true;
                    }
                    else if (kind == DebugEvent.CHANGE)
                    {
                        handleChangeEvent();
                        isChangeOnly = true;
                    }
                }
            }
        }

        if (isSuspend)
            handleSuspend(isBreakpointHit);
        else if (isChangeOnly)
            handleChange();
    }

    protected void handleSuspend(boolean isBreakpointHit)
    {
        if (getUpdateMode() == UPDATE_ALWAYS || (getUpdateMode() == UPDATE_ON_BREAKPOINT && isBreakpointHit))
        {
            Display.getDefault().asyncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    archiveDeltas();
                    refresh();
                }
            });
        }
    }

    protected void handleChange()
    {
        if (getUpdateMode() == UPDATE_ALWAYS)
        {
            Display.getDefault().asyncExec(new Runnable()
            {
                @Override
                public void run()
                {
                    refresh();
                }
            });
        }
    }

    protected void handleSuspendEvent(int detail)
    {
    }

    protected void handleChangeEvent()
    {
    }

    // Return true to enable development debug print statements

    public boolean isDebug()
    {
        return false;
    }

    protected IMemoryBlockExtension getMemoryBlock()
    {
        IMemoryBlock block = fParent.getMemoryBlock();
        if (block != null)
            return block.getAdapter(IMemoryBlockExtension.class);

        return null;
    }

    public BigInteger getBigBaseAddress()
    {
        return fParent.getBigBaseAddress();
    }

    public int getAddressableSize()
    {
        return fParent.getAddressableSize();
    }

    protected FPIViewportCache getViewportCache()
    {
        return fViewportCache;
    }

    public FPMemoryByte[] getBytes(BigInteger address, int bytes) throws DebugException
    {
        return getViewportCache().getBytes(address, bytes);
    }

    // Default visibility for performance

    ViewportCache fViewportCache = new ViewportCache();

    private interface Request
    {
    }

    class ViewportCache extends Thread implements FPIViewportCache
    {
        class ArchiveDeltas implements Request
        {
        }

        class AddressPair implements Request
        {
            BigInteger startAddress;
            BigInteger endAddress;

            public AddressPair(BigInteger start, BigInteger end)
            {
                startAddress = start;
                endAddress = end;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (obj == null)
                    return false;
                if (obj instanceof AddressPair)
                {
                    return ((AddressPair) obj).startAddress.equals(startAddress) && ((AddressPair) obj).endAddress.equals(endAddress);
                }

                return false;
            }

			@Override
			public int hashCode() {
				return super.hashCode() + startAddress.hashCode() + endAddress.hashCode();
			}

        }

        class MemoryUnit implements Cloneable
        {
            BigInteger start;

            BigInteger end;

            FPMemoryByte[] bytes;

            @Override
            public MemoryUnit clone()
            {
                MemoryUnit b = new MemoryUnit();

                b.start = this.start;
                b.end = this.end;
                b.bytes = new FPMemoryByte[this.bytes.length];
                for (int index = 0; index < this.bytes.length; index++)
                    b.bytes[index] = new FPMemoryByte(this.bytes[index].getValue());

                return b;
            }

            public boolean isValid()
            {
                return this.start != null && this.end != null && this.bytes != null;
            }
        }

        @SuppressWarnings("hiding")
        private HashMap<BigInteger, FPMemoryByte[]> fEditBuffer = new HashMap<>();
        private boolean fDisposed = false;
        private Object fLastQueued = null;
        private Vector<Object> fQueue = new Vector<>();
        protected MemoryUnit fCache = null;
        protected MemoryUnit fHistoryCache[] = new MemoryUnit[0];
        protected int fHistoryDepth = 0;

        public ViewportCache()
        {
            start();
        }

        @Override
        public void dispose()
        {
            fDisposed = true;
            synchronized (fQueue)
            {
                fQueue.notify();
            }
        }

        public int getHistoryDepth()
        {
            return fHistoryDepth;
        }

        public void setHistoryDepth(int depth)
        {
            fHistoryDepth = depth;
            fHistoryCache = new MemoryUnit[fHistoryDepth];
        }

        @Override
        public void refresh()
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if (fCache != null)
            {
                queueRequest(fViewportAddress, getViewportEndAddress());
            }
        }

        @Override
        public void archiveDeltas()
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if (fCache != null)
            {
                queueRequestArchiveDeltas();
            }
        }

        private void queueRequest(BigInteger startAddress, BigInteger endAddress)
        {
            AddressPair pair = new AddressPair(startAddress, endAddress);
            queue(pair);
        }

        private void queueRequestArchiveDeltas()
        {
            ArchiveDeltas archive = new ArchiveDeltas();
            queue(archive);
        }

        private void queue(Object element)
        {
            synchronized (fQueue)
            {
                if (!(fQueue.size() > 0 && element.equals(fLastQueued)))
                {
                    fQueue.addElement(element);
                    fLastQueued = element;
                }
                fQueue.notify();
            }
        }

        @Override
        public void run()
        {
            while (!fDisposed)
            {
                AddressPair pair = null;
                boolean archiveDeltas = false;
                synchronized (fQueue)
                {
                    if (fQueue.size() > 0)
                    {
                        Request request = (Request) fQueue.elementAt(0);
                        Class<?> type = request.getClass();

                        while (fQueue.size() > 0 && type.isInstance(fQueue.elementAt(0)))
                        {
                            request = (Request) fQueue.elementAt(0);
                            fQueue.removeElementAt(0);
                        }

                        if (request instanceof ArchiveDeltas)
                            archiveDeltas = true;
                        else if (request instanceof AddressPair)
                            pair = (AddressPair) request;
                    }
                }

                if (archiveDeltas)
                {
                    for (int i = fViewportCache.getHistoryDepth() - 1; i > 0; i--)
                        fHistoryCache[i] = fHistoryCache[i - 1];

                    fHistoryCache[0] = fCache.clone();
                }
                else if (pair != null)
                {
                    populateCache(pair.startAddress, pair.endAddress);
                }
                else
                {
                    synchronized (fQueue)
                    {
                        try
                        {
                            if (fQueue.isEmpty())
                            {
                                fQueue.wait();
                            }
                        } catch (Exception e)
                        {
                            // do nothing
                        }
                    }
                }
            }
        }

        // Cache memory necessary to paint viewport
        // TODO: user setting to buffer +/- x lines
        // TODO: reuse existing cache? probably only a minor performance gain

        private void populateCache(final BigInteger startAddress, final BigInteger endAddress)
        {
            try
            {
                IMemoryBlockExtension memoryBlock = getMemoryBlock();

                BigInteger lengthInBytes = endAddress.subtract(startAddress);
                BigInteger addressableSize = BigInteger.valueOf(getAddressableSize());

                long units = lengthInBytes.divide(addressableSize)
                        .add(lengthInBytes.mod(addressableSize).compareTo(BigInteger.ZERO) > 0 ? BigInteger.ONE : BigInteger.ZERO).longValue();

                // CDT (and maybe other backends) will call setValue() on these MemoryBlock objects.  We
                // don't want this to happen, because it interferes with this rendering's own change history.
                // Ideally, we should strictly use the back end change notification and history, but it is
                // only guaranteed to work for bytes within the address range of the MemoryBlock.

                MemoryByte readBytes[] = memoryBlock.getBytesFromAddress(startAddress, units);
                FPMemoryByte cachedBytes[] = new FPMemoryByte[readBytes.length];

                for (int index = 0; index < readBytes.length; index++)
                    cachedBytes[index] = new FPMemoryByte(readBytes[index].getValue(), readBytes[index].getFlags());

                // Derive the target endian from the read MemoryBytes.

                if (cachedBytes.length > 0)
                    if (cachedBytes[0].isEndianessKnown())
                        setTargetLittleEndian(!cachedBytes[0].isBigEndian());

                // The first time we execute this method, set the display endianness to the target endianness.

                if (!initialDisplayModeSet)
                {
                    setDisplayLittleEndian(isTargetLittleEndian());
                    initialDisplayModeSet = true;
                }

                // Re-order bytes within unit to be a sequential byte stream if the endian is already little

                if (isTargetLittleEndian())
                {
                    // There isn't an order when the unit size is one, so skip for performance

                    if (addressableSize.compareTo(BigInteger.ONE) != 0)
                    {
                        int unitSize = addressableSize.intValue();
                        FPMemoryByte cachedBytesAsByteSequence[] = new FPMemoryByte[cachedBytes.length];
                        for (int unit = 0; unit < units; unit++)
                        {
                            for (int unitbyte = 0; unitbyte < unitSize; unitbyte++)
                            {
                                cachedBytesAsByteSequence[unit * unitSize + unitbyte] = cachedBytes[unit * unitSize + unitSize - unitbyte];
                            }
                        }
                        cachedBytes = cachedBytesAsByteSequence;
                    }
                }

                final FPMemoryByte[] cachedBytesFinal = cachedBytes;

                fCache = new MemoryUnit();
                fCache.start = startAddress;
                fCache.end = endAddress;
                fCache.bytes = cachedBytesFinal;

                Display.getDefault().asyncExec(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // Generate deltas

                        for (int historyIndex = 0; historyIndex < getHistoryDepth(); historyIndex++)
                        {
                            if (fHistoryCache[historyIndex] != null && fHistoryCache[historyIndex].isValid())
                            {
                                BigInteger maxStart = startAddress.max(fHistoryCache[historyIndex].start);
                                BigInteger minEnd = endAddress.min(fHistoryCache[historyIndex].end).subtract(BigInteger.ONE);

                                BigInteger overlapLength = minEnd.subtract(maxStart);
                                if (overlapLength.compareTo(BigInteger.valueOf(0)) > 0)
                                {
                                    // there is overlap

                                    int offsetIntoOld = maxStart.subtract(fHistoryCache[historyIndex].start).intValue();
                                    int offsetIntoNew = maxStart.subtract(startAddress).intValue();

                                    for (int i = overlapLength.intValue(); i >= 0; i--)
                                    {
                                        cachedBytesFinal[offsetIntoNew + i].setChanged(historyIndex,
                                                cachedBytesFinal[offsetIntoNew + i].getValue() != fHistoryCache[historyIndex].bytes[offsetIntoOld + i]
                                                        .getValue());
                                    }

                                    // There are several scenarios where the history cache must be updated from the data cache, so that when a
                                    // cell is edited the font color changes appropriately. The following code deals with the different cases.

                                    if (historyIndex != 0) continue;

                                    int dataStart     = fCache.start.intValue();
                                    int dataEnd       = fCache.end.intValue();
                                    int dataLength    = fCache.bytes.length;

                                    int historyStart  = fHistoryCache[0].start.intValue();
                                    int historyEnd    = fHistoryCache[0].end.intValue();
                                    int historyLength = fHistoryCache[0].bytes.length;

                                    // Case 1: The data cache is smaller than the history cache; the data cache's
                                    //         address range is fully covered by the history cache.  Do nothing.

                                    if ((dataStart >= historyStart) && (dataEnd <= historyEnd))
                                        continue;

                                    // Case 2: The data and history cache's do not overlap at all

                                    if (((dataStart < historyStart) && (dataEnd < historyStart)) || (dataStart > historyEnd))
                                    {
                                        // Create a new history cache: Copy the data cache bytes to the history cache

                                        MemoryUnit newHistoryCache = new MemoryUnit();

                                        newHistoryCache.start   = fCache.start;
                                        newHistoryCache.end     = fCache.end;
                                        int newHistoryCacheSize = fCache.bytes.length;
                                        newHistoryCache.bytes   = new FPMemoryByte[newHistoryCacheSize];

                                        for (int index = 0; index < newHistoryCacheSize; index++)
                                            newHistoryCache.bytes[index] = new FPMemoryByte(fCache.bytes[index].getValue());

                                        fHistoryCache[0] = newHistoryCache;

                                        continue;
                                    }

                                    // Case 3: The data cache starts at a lower address than the history cache, but overlaps the history cache

                                    if ((dataStart < historyStart) && ((dataEnd >= historyStart) && (dataEnd <= historyEnd)))
                                    {
                                        // Create a new history cache with the missing data from the main cache and append the old history to it.

                                        int missingDataByteCount = historyStart - dataStart;
                                        int historyCacheSize     = historyLength;
                                        int newHistoryCacheSize  = missingDataByteCount + historyLength;

                                        if (missingDataByteCount <= 0 && historyCacheSize <= 0) break;

                                        MemoryUnit newHistoryCache = new MemoryUnit();

                                        newHistoryCache.start = fCache.start;
                                        newHistoryCache.end   = fHistoryCache[0].end;
                                        newHistoryCache.bytes = new FPMemoryByte[newHistoryCacheSize];

                                        // Copy the missing bytes from the beginning of the main cache to the history cache.

                                        for (int index = 0; index < missingDataByteCount; index++)
                                            newHistoryCache.bytes[index] = new FPMemoryByte(fCache.bytes[index].getValue());

                                        // Copy the remaining bytes from the old history cache to the new history cache

                                        for (int index = 0; index < historyCacheSize; index++)
                                            newHistoryCache.bytes[index + missingDataByteCount] =
                                                new FPMemoryByte(fHistoryCache[0].bytes[index].getValue());

                                        fHistoryCache[0] = newHistoryCache;

                                        continue;
                                    }

                                    // Case 4: The data cache starts at a higher address than the history cache

                                    if (((dataStart >= historyStart) && (dataStart <= historyEnd)) && (dataEnd > historyEnd))
                                    {
                                        // Append the missing main cache bytes to the history cache.

                                        int missingDataByteCount = dataEnd - historyEnd;
                                        int historyCacheSize     = historyEnd - historyStart;
                                        int newHistoryCacheSize  = missingDataByteCount + historyLength;

                                        if (missingDataByteCount > 0 && historyCacheSize > 0)
                                        {
                                            MemoryUnit newHistoryCache = new MemoryUnit();

                                            newHistoryCache.start = fHistoryCache[0].start;
                                            newHistoryCache.end   = fCache.end;
                                            newHistoryCache.bytes = new FPMemoryByte[newHistoryCacheSize];

                                            // Copy the old history bytes to the new history cache

                                            System.arraycopy(fHistoryCache[0].bytes, 0, newHistoryCache.bytes, 0, historyLength);

                                            // Copy the bytes from the main cache that are not in the history cache to the end of the new history cache.

                                            for (int index = 0; index < missingDataByteCount; index++)
                                            {
                                                int srcIndex = dataLength - missingDataByteCount + index;
                                                int dstIndex = historyLength + index;
                                                newHistoryCache.bytes[dstIndex] = new FPMemoryByte(fCache.bytes[srcIndex].getValue());
                                            }

                                            fHistoryCache[0] = newHistoryCache;

                                            continue;
                                        }
                                    }

                                    // Case 5 - The data cache is greater than the history cache and fully covers it

                                    if (dataStart < historyStart && dataEnd > historyEnd)
                                    {
                                        int start = 0;
                                        int end   = 0;

                                        // Create a new history cache to reflect the entire data cache

                                        MemoryUnit newHistoryCache = new MemoryUnit();

                                        newHistoryCache.start   = fCache.start;
                                        newHistoryCache.end     = fCache.end;
                                        int newHistoryCacheSize = fCache.bytes.length;
                                        newHistoryCache.bytes   = new FPMemoryByte[newHistoryCacheSize];

                                        int topByteCount    = historyStart - dataStart;
                                        int bottomByteCount = dataEnd - historyEnd;

                                        // Copy the bytes from the beginning of the data cache to the new history cache

                                        for (int index = 0; index < topByteCount; index++)
                                            newHistoryCache.bytes[index] = new FPMemoryByte(fCache.bytes[index].getValue());

                                        // Copy the old history cache bytes to the new history cache

                                        start = topByteCount;
                                        end   = topByteCount + historyLength;

                                        for (int index = start; index < end; index++)
                                            newHistoryCache.bytes[index] = new FPMemoryByte(fCache.bytes[index].getValue());

                                        // Copy the bytes from the end of the data cache to the new history cache

                                        start = topByteCount + historyLength;
                                        end   = topByteCount + historyLength + bottomByteCount;

                                        for (int index = start; index < end; index++)
                                            newHistoryCache.bytes[index] = new FPMemoryByte(fCache.bytes[index].getValue());

                                        fHistoryCache[0] = newHistoryCache;

                                        continue;
                                    }
                                }
                            }
                        }

                        // If the history does not exist, populate the history with the just populated
                        // cache.  This solves the use case of (1) connect to target; (2) edit memory
                        // before the first suspend debug event; (3) paint differences in changed color.

                        if (fHistoryCache[0] == null)
                            fHistoryCache[0] = fCache.clone();

                        Rendering.this.redrawPanes();
                    }
                });

            }
            catch (Exception e)
            {
                // User can scroll to any memory, whether it's valid on the target or not.  It doesn't make
                // much sense to fill up the Eclipse error log with such "failures."  So, comment out for now.
                // logError(FPRenderingMessages.getString("FAILURE_READ_MEMORY"), e); //$NON-NLS-1$
            }
        }

        // Bytes will be fetched from cache

        @Override
        public FPMemoryByte[] getBytes(BigInteger address, int bytesRequested) throws DebugException
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            if (containsEditedCell(address))        // Cell size cannot be switched during an edit
                return getEditedMemory(address);

            boolean contains = false;
            if (fCache != null && fCache.start != null)
            {
                // See if all of the data requested is in the cache

                BigInteger dataEnd = address.add(BigInteger.valueOf(bytesRequested));

                if (fCache.start.compareTo(address) <= 0 && fCache.end.compareTo(dataEnd) >= 0 && fCache.bytes.length > 0)
                    contains = true;
            }

            if (contains)
            {
                int offset = address.subtract(fCache.start).intValue();
                FPMemoryByte bytes[] = new FPMemoryByte[bytesRequested];

                for (int index = 0; index < bytes.length; index++)
                    bytes[index] = fCache.bytes[offset + index];

                return bytes;
            }

            FPMemoryByte bytes[] = new FPMemoryByte[bytesRequested];

            for (int index = 0; index < bytes.length; index++)
            {
                bytes[index] = new FPMemoryByte();
                bytes[index].setReadable(false);
            }

            fViewportCache.queueRequest(fViewportAddress, getViewportEndAddress());

            return bytes;
        }

        @Override
        public boolean containsEditedCell(BigInteger address)
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$
            return fEditBuffer.containsKey(address);
        }

        public FPMemoryByte[] getEditedMemory(BigInteger address)
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$
            return fEditBuffer.get(address);
        }

        @Override
        public void clearEditBuffer()
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$
            fEditBuffer.clear();
            Rendering.this.redrawPanes();
        }

        @Override
        public void writeEditBuffer()
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

            Set<BigInteger> keySet = fEditBuffer.keySet();
            Iterator<BigInteger> iterator = keySet.iterator();

            while (iterator.hasNext())
            {
                BigInteger address = iterator.next();
                FPMemoryByte[] bytes = fEditBuffer.get(address);

                byte byteValue[] = new byte[bytes.length];

                for (int index = 0; index < bytes.length; index++)
                    byteValue[index] = bytes[index].getValue();

                try
                {
                    IMemoryBlockExtension block = getMemoryBlock();
                    BigInteger offset = address.subtract(block.getBigBaseAddress());
                    block.setValue(offset, byteValue);
                }
                catch (Exception e)
                {
                    MemoryViewUtil.openError(FPRenderingMessages.getString("FAILURE_WRITE_MEMORY"), "", e); //$NON-NLS-1$ //$NON-NLS-2$
                    logError(FPRenderingMessages.getString("FAILURE_WRITE_MEMORY"), e); //$NON-NLS-1$
                }
            }

            clearEditBuffer();
        }

        @Override
        public void setEditedValue(BigInteger address, FPMemoryByte[] bytes)
        {
            assert Thread.currentThread().equals(Display.getDefault().getThread()) : FPRenderingMessages.getString("CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$
            fEditBuffer.put(address, bytes);
            Rendering.this.redrawPanes();
        }
    }

    public void setVisibleAddressBar(boolean visible)
    {
        fAddressBarControl.setVisible(visible);
        if (visible)
        {
            String selectedStr = "0x" + getCaretAddress().toString(16); //$NON-NLS-1$
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

    @Override
    public void dispose()
    {
        DebugPlugin.getDefault().removeDebugEventListener(this);
        if (fViewportCache != null)
        {
            fViewportCache.dispose();
            fViewportCache = null;
        }
        super.dispose();
    }

    class Selection implements FPIMemorySelection
    {
        private BigInteger fStartHigh = null;
        private BigInteger fStartLow = null;

        private BigInteger fEndHigh = null;
        private BigInteger fEndLow = null;

        @Override
        public void clear()
        {
            fEndHigh = fEndLow = fStartHigh = fStartLow = null;
            redrawPanes();
        }

        @Override
        public boolean hasSelection()
        {
            return fStartHigh != null && fStartLow != null && fEndHigh != null && fEndLow != null;
        }

        @Override
        public boolean isSelected(BigInteger address)
        {
            // Do we have valid start and end addresses?

            if (getEnd() == null || getStart() == null) return false;

            // If end is greater than start

            if (getEnd().compareTo(getStart()) >= 0)
            {
                // If address is greater-than-or-equal-to start and less then end, return true
                if (address.compareTo(getStart()) >= 0 && address.compareTo(getEnd()) < 0) return true;
            }

            // If start is greater than end

            else if (getStart().compareTo(getEnd()) >= 0)
            {
                // If address is greater-than-or-equal-to zero and less than start, return true
                if (address.compareTo(getEnd()) >= 0 && address.compareTo(getStart()) < 0) return true;
            }

            return false;
        }

        // Set selection start

        @Override
        public void setStart(BigInteger high, BigInteger low)
        {
            if (high == null && low == null)
            {
                if (fStartHigh != null && fStartLow != null)
                {
                    fStartHigh = null;
                    fStartLow = null;
                    redrawPanes();
                }

                return;
            }

            boolean changed = false;

            if (fStartHigh == null || !high.equals(fStartHigh))
            {
                fStartHigh = high;
                changed = true;
            }

            if (fStartLow == null || !low.equals(fStartLow))
            {
                fStartLow = low;
                changed = true;
            }

            if (changed) redrawPanes();
        }

        // Set selection end

        @Override
        public void setEnd(BigInteger high, BigInteger low)
        {
            if (high == null && low == null)
            {
                if (fEndHigh != null && fEndLow != null)
                {
                    fEndHigh = null;
                    fEndLow = null;
                    redrawPanes();
                }

                return;
            }

            boolean changed = false;

            if (fEndHigh == null || !high.equals(fEndHigh))
            {
                fEndHigh = high;
                changed = true;
            }

            if (fEndLow == null || !low.equals(fEndLow))
            {
                fEndLow = low;
                changed = true;
            }

            if (changed) redrawPanes();
        }

        @Override
        public BigInteger getHigh()
        {
            if (!hasSelection()) return null;
            return getStart().max(getEnd());
        }

        @Override
        public BigInteger getLow()
        {
            if (!hasSelection()) return null;
            return getStart().min(getEnd());
        }

        @Override
        public BigInteger getStart()
        {
            // If there is no start, return null
            if (fStartHigh == null) return null;

            // If there is no end, return the high address of the start
            if (fEndHigh == null) return fStartHigh;

            // If Start High/Low equal End High/Low, return a low start and high end
            if (fStartHigh.equals(fEndHigh) && fStartLow.equals(fEndLow)) return fStartLow;

            BigInteger differenceEndToStartHigh = fEndHigh.subtract(fStartHigh).abs();
            BigInteger differenceEndToStartLow  = fEndHigh.subtract(fStartLow).abs();

            // Return the start high or start low based on which creates a larger selection
            if (differenceEndToStartHigh.compareTo(differenceEndToStartLow) > 0)
                return fStartHigh;

            return fStartLow;
        }

        @Override
        public BigInteger getStartLow()
        {
            return fStartLow;
        }

        @Override
        public BigInteger getEnd()
        {
            // If there is no end, return null
            if (fEndHigh == null) return null;

            // *** Temporary for debugging ***

            if (fStartHigh == null || fStartLow == null)
            {
                return null;
            }

            // If Start High/Low equal End High/Low, return a low start and high end
            if (fStartHigh.equals(fEndHigh) && fStartLow.equals(fEndLow)) return fStartHigh;

            BigInteger differenceStartToEndHigh = fStartHigh.subtract(fEndHigh).abs();
            BigInteger differenceStartToEndLow = fStartHigh.subtract(fEndLow).abs();

            // Return the start high or start low based on which creates a larger selection
            if (differenceStartToEndHigh.compareTo(differenceStartToEndLow) >= 0)
                return fEndHigh;

            return fEndLow;
        }
    }

    public void setPaneVisible(int pane, boolean visible)
    {
        switch (pane)
        {
            case PANE_ADDRESS:
                fAddressPane.setPaneVisible(visible);
                break;
            case PANE_DATA:
                fDataPane.setPaneVisible(visible);
                break;
        }

        fireSettingsChanged();
        layoutPanes();
    }

    public boolean getPaneVisible(int pane)
    {
        switch (pane)
        {
            case PANE_ADDRESS:
                return fAddressPane.isPaneVisible();
            case PANE_DATA:
                return fDataPane.isPaneVisible();
            default:
                return false;
        }
    }

    protected void packColumns()
    {
        int availableWidth = Rendering.this.getSize().x;

        if (fAddressPane.isPaneVisible())
        {
            availableWidth -= fAddressPane.computeSize(0, 0).x;
            availableWidth -= Rendering.this.getRenderSpacing() * 2;
        }

        int combinedWidth = 0;

        if (fDataPane.isPaneVisible()) combinedWidth += fDataPane.getCellWidth();

        if (getColumnsSetting() == Rendering.COLUMNS_AUTO_SIZE_TO_FIT)
        {
            if (combinedWidth == 0)
                fColumnCount = 0;
            else
            {
                fColumnCount = availableWidth / combinedWidth;
                if (fColumnCount == 0) fColumnCount = 1;    // Paint one column even if only part can show in view
            }
        }
        else
            fColumnCount = getColumnsSetting();

        try
        {
            // Update the number of bytes per row; the max/min scroll range and the current thumbnail position.

            fBytesPerRow = getCharsPerColumn() * getColumnCount();
            getVerticalBar().setMinimum(1);

            // scrollbar maximum range is Integer.MAX_VALUE.

            getVerticalBar().setMaximum(getMaxScrollRange().min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue());
            getVerticalBar().setIncrement(1);
            getVerticalBar().setPageIncrement(this.getRowCount() - 1);

            // FIXME: conversion of slider to scrollbar
            // fScrollBar.setToolTipText(Rendering.this.getAddressString(fViewportAddress));

            setScrollSelection();
        }
        catch (Exception e)
        {
            // FIXME precautionary
        }

        Rendering.this.redraw();
        Rendering.this.redrawPanes();
    }

    public FPAbstractPane[] getRenderingPanes()
    {
        return new FPAbstractPane[] { fAddressPane, fDataPane };
    }

    public int getCellPadding()
    {
        return fCellPadding;
    }

    protected int getRenderSpacing()
    {
        return fPaneSpacing;
    }

    public void refresh()
    {
        if (!this.isDisposed())
        {
            if (this.isVisible() && getViewportCache() != null)
            {
                getViewportCache().refresh();
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
        this.getViewportCache().archiveDeltas();
    }

    public void gotoAddress(BigInteger address)
    {
        // Ensure that the GoTo address is within the addressable range

        if ((address.compareTo(this.getMemoryBlockStartAddress()) < 0) || (address.compareTo(this.getMemoryBlockEndAddress()) > 0))
            return;

        fViewportAddress = address;

        // Reset the caret and selection state (no caret and no selection)

        fCaretAddress = null;
        fSelection = new Selection();

        redrawPanes();
    }

    public void setViewportStartAddress(BigInteger newAddress)
    {
        fViewportAddress = newAddress;
    }

    public BigInteger getViewportStartAddress()
    {
        return fViewportAddress;
    }

    public BigInteger getViewportEndAddress()
    {
        return fViewportAddress.add(BigInteger.valueOf(this.getBytesPerRow() * getRowCount() / getAddressableSize()));
    }

    public String getAddressString(BigInteger address)
    {
        StringBuffer addressString = new StringBuffer(address.toString(16).toUpperCase());

        for (int chars = getAddressBytes() * 2 - addressString.length(); chars > 0; chars--)
            addressString.insert(0, '0');

        addressString.insert(0, "0x"); //$NON-NLS-1$

        return addressString.toString();
    }

    protected int getAddressBytes()
    {
        return fParent.getAddressSize();
    }

    public Control getAddressBarControl()
    {
        return fAddressBarControl;
    }

    public int getColumnCount()
    {
        return fColumnCount;
    }

    public int getColumnsSetting()
    {
        return fColumnsSetting;
    }

    protected void setBytesPerRow(int count)
    {
        fBytesPerRow = count;
    }

    protected void setColumnCount(int count)
    {
        fColumnCount = count;
    }

    public void setColumnsSetting(int columns)
    {
        if (fColumnsSetting != columns)
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

        if (isAddressBeforeViewportStart || isAddressAfterViewportEnd) gotoAddress(address);
    }

    protected int getRowCount()
    {
        int rowCount = 0;
        Control panes[] = getRenderingPanes();
        for (int i = 0; i < panes.length; i++)
            if (panes[i] instanceof FPAbstractPane)
                rowCount = Math.max(rowCount, ((FPAbstractPane) panes[i]).getRowCount());

        return rowCount;
    }

    protected int getBytesPerRow()
    {
        return fBytesPerRow;
    }

    protected int getAddressableCellsPerRow()
    {
        return getBytesPerRow() / getAddressableSize();
    }

    public int getAddressesPerColumn()
    {
        return this.getCharsPerColumn() / getAddressableSize();
    }

    /**
     * @return Set current scroll selection
     */
    protected void setScrollSelection()
    {
        BigInteger selection = getViewportStartAddress().divide(BigInteger.valueOf(getAddressableCellsPerRow()));

        fScrollSelection = rows2scrollbar(selection);
        getVerticalBar().setSelection(fScrollSelection);
    }

    /**
     * compute the maximum scrolling range.
     *
     * @return number of lines that rendering can display
     */
    private BigInteger getMaxScrollRange()
    {
        BigInteger difference = getMemoryBlockEndAddress().subtract(getMemoryBlockStartAddress()).add(BigInteger.ONE);
        BigInteger maxScrollRange = difference.divide(BigInteger.valueOf(getAddressableCellsPerRow()));
        if (maxScrollRange.multiply(BigInteger.valueOf(getAddressableCellsPerRow())).compareTo(difference) != 0)
            maxScrollRange = maxScrollRange.add(BigInteger.ONE);

        // Support targets with an addressable size greater than 1

        maxScrollRange = maxScrollRange.divide(BigInteger.valueOf(getAddressableSize()));
        return maxScrollRange;
    }

    /**
     * The scroll range is limited by SWT. Because it can be less than the number
     * of rows (of memory) that we need to display, we need an arithmetic mapping.
     *
     * @return ratio this function returns how many rows a scroll bar unit
     *         represents. The number will be some fractional value, up to but
     *         not exceeding the value 1. I.e., when the scroll range exceeds
     *         the row range, we use a 1:1 mapping.
     */
    private final BigDecimal getScrollRatio()
    {
        BigInteger maxRange = getMaxScrollRange();
        if (maxRange.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
        {
            return new BigDecimal(maxRange).divide(BigDecimal.valueOf(Integer.MAX_VALUE), SCROLL_CONVERSION_PRECISION);
        }

        return BigDecimal.ONE;
    }

    /**
     * Convert memory row units to scroll bar units. The scroll range is limited
     * by SWT. Because it can be less than the number of rows (of memory) that
     * we need to display, we need an arithmetic mapping.
     *
     * @param rows
     *            units of memory
     * @return scrollbar units
     */
    private int rows2scrollbar(BigInteger rows)
    {
        return new BigDecimal(rows).divide(getScrollRatio(), SCROLL_CONVERSION_PRECISION).intValue();
    }

    /**
     * Convert scroll bar units to memory row units. The scroll range is limited
     * by SWT. Because it can be less than the number of rows (of memory) that
     * we need to display, we need an arithmetic mapping.
     *
     * @param scrollbarUnits
     *            scrollbar units
     * @return number of rows of memory
     */
    BigInteger scrollbar2rows(int scrollbarUnits)
    {
        return getScrollRatio().multiply(BigDecimal.valueOf(scrollbarUnits), SCROLL_CONVERSION_PRECISION).toBigInteger();
    }

    /**
     * @return start address of the memory block
     */
    protected BigInteger getMemoryBlockStartAddress()
    {
        if (fMemoryBlockStartAddress == null)
            fMemoryBlockStartAddress = fParent.getMemoryBlockStartAddress();
        if (fMemoryBlockStartAddress == null)
            fMemoryBlockStartAddress = BigInteger.ZERO;

        return fMemoryBlockStartAddress;
    }

    /**
     * @return end address of the memory block
     */
    protected BigInteger getMemoryBlockEndAddress()
    {
        if (fMemoryBlockEndAddress == null)
            fMemoryBlockEndAddress = fParent.getMemoryBlockEndAddress();

        return fMemoryBlockEndAddress;
    }

    public FPDataType getFPDataType()
    {
        return fFPDataType;
    }

    public void setFPDataType(FPDataType numberType)
    {
        if (fFPDataType == numberType) return;

        fFPDataType = numberType;
        fireSettingsChanged();
        layoutPanes();
    }

    public int getUpdateMode()
    {
        return fUpdateMode;
    }

    public void setUpdateMode(int fUpdateMode)
    {
        this.fUpdateMode = fUpdateMode;
    }

    protected String getCharacterSet(int mode)
    {
        switch (mode)
        {
            case Rendering.TEXT_UTF8:
                return "UTF8"; //$NON-NLS-1$

            case Rendering.TEXT_UTF16:
                return "UTF16"; //$NON-NLS-1$

            case Rendering.TEXT_USASCII:
                return "US-ASCII"; //$NON-NLS-1$

            case Rendering.TEXT_ISO_8859_1:
            default:
                return "ISO-8859-1"; //$NON-NLS-1$
        }
    }

    public int getBytesPerCharacter()
    {
        return 1;
    }

    public boolean isTargetLittleEndian()
    {
        return fIsTargetLittleEndian;
    }

    public void setTargetLittleEndian(boolean littleEndian)
    {
        if (fIsTargetLittleEndian == littleEndian) return;

        fParent.setTargetMemoryLittleEndian(littleEndian);
        fIsTargetLittleEndian = littleEndian;

        Display.getDefault().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                fireSettingsChanged();
                layoutPanes();
            }
        });
    }

    public boolean isDisplayLittleEndian()
    {
        return fIsDisplayLittleEndian;
    }

    public void setDisplayLittleEndian(boolean isLittleEndian)
    {
        if (fIsDisplayLittleEndian == isLittleEndian) return;
        fIsDisplayLittleEndian = isLittleEndian;
        fireSettingsChanged();

        Display.getDefault().asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                layoutPanes();
            }
        });
    }

    public int getCharsPerColumn()
    {
        return fCharsPerColumn;
    }

    public int getDisplayedPrecision()
    {
        return fDisplayedPrecision;
    }

    // Set the number of precision digits that are displayed in the view

    public void setDisplayedPrecision(int displayedPrecision)
    {
        if (fDisplayedPrecision != displayedPrecision)
        {
            fDisplayedPrecision = displayedPrecision;
            fCharsPerColumn = charsPerColumn();
            fireSettingsChanged();
            layoutPanes();
        }
    }

    protected void redrawPane(int paneId)
    {
        if (!isDisposed() && this.isVisible())
        {
            FPAbstractPane pane = null;

            if (paneId == Rendering.PANE_ADDRESS)
            {
                pane = fAddressPane;
            }
            else if (paneId == Rendering.PANE_DATA)
            {
                pane = fDataPane;
            }

            if (pane != null && pane.isPaneVisible())
            {
                pane.redraw();
                pane.setRowCount();
                if (pane.isFocusControl()) pane.updateTheCaret();
            }
        }

        fParent.updateRenderingLabels();
    }

    protected void redrawPanes()
    {
        if (!isDisposed() && this.isVisible())
        {
            if (fAddressPane.isPaneVisible())
            {
                fAddressPane.redraw();
                fAddressPane.setRowCount();
                if (fAddressPane.isFocusControl()) fAddressPane.updateTheCaret();
            }

            if (fDataPane.isPaneVisible())
            {
                fDataPane.redraw();
                fDataPane.setRowCount();
                if (fDataPane.isFocusControl()) fDataPane.updateTheCaret();
            }
        }

        fParent.updateRenderingLabels();
    }

    void layoutPanes()
    {
        packColumns();
        layout(true);

        redraw();
        redrawPanes();
    }

    void fireSettingsChanged()
    {
        fAddressPane.settingsChanged();
        fDataPane.settingsChanged();
    }

    protected void copyAddressToClipboard()
    {
        Clipboard clip = null;

        try
        {
            clip = new Clipboard(getDisplay());

            String addressString = "0x" + getCaretAddress().toString(16); //$NON-NLS-1$

            TextTransfer plainTextTransfer = TextTransfer.getInstance();
            clip.setContents(new Object[] { addressString }, new Transfer[] { plainTextTransfer });
        }
        finally
        {
            if (clip != null)
            {
                clip.dispose();
            }
        }
    }

    // Given an array of bytes, the data type and endianness, return a scientific notation string representation

    public String sciNotationString(FPMemoryByte byteArray[], FPDataType fpDataType, boolean isLittleEndian)
    {
        StringBuffer textString = null;

        // Check the byte array for readability

        for (int index = 0; index < byteArray.length; index++)
            if (!byteArray[index].isReadable())
                return FPutilities.fillString(fCharsPerColumn, '?');

        // Convert the byte array to a floating point value and check to see if it's within the range
        // of the data type.  If the valid range is exceeded, set string to "-Infinity" or "Infinity".

        ByteOrder byteOrder = isLittleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

        if (fpDataType == FPDataType.FLOAT)
        {
            float floatValue = ByteBuffer.wrap(FPutilities.memoryBytesToByteArray(byteArray)).order(byteOrder).getFloat();
            floatValue = FPutilities.floatLimitCheck(floatValue);

            if (floatValue == Float.NEGATIVE_INFINITY)
                textString = new StringBuffer("-Infinity"); //$NON-NLS-1$

            if (floatValue == Float.POSITIVE_INFINITY)
                textString = new StringBuffer(" Infinity"); //$NON-NLS-1$
        }

        if (fpDataType == FPDataType.DOUBLE)
        {
            double doubleValue = ByteBuffer.wrap(FPutilities.memoryBytesToByteArray(byteArray)).order(byteOrder).getDouble();
            doubleValue = FPutilities.doubleLimitCheck(doubleValue);

            if (doubleValue == Double.NEGATIVE_INFINITY)
                textString = new StringBuffer("-Infinity"); //$NON-NLS-1$

            if (doubleValue == Double.POSITIVE_INFINITY)
                textString = new StringBuffer(" Infinity"); //$NON-NLS-1$
        }

        // If we do not already have a StringBuffer value, Convert the value to
        // a string.  In any case, pad the string with spaces to the cell width.

        if (textString == null)
            textString = new StringBuffer(FPutilities.byteArrayToSciNotation(fpDataType, isLittleEndian, byteArray, fDisplayedPrecision));

        return (textString.append(FPutilities.fillString(fCharsPerColumn - textString.length(), getPaddingCharacter()))).toString();
    }

    // Convert the floating point edit buffer string to a byte array and update memory

    public void convertAndUpdateCell(BigInteger memoryAddress, String editBuffer)
    {
        if (editBuffer != null && editBuffer.length() > 0)
        {
            // Convert the edit buffer string to byte arrays

            byte[] targetByteArray = new byte[getFPDataType().getByteLength()];

            targetByteArray = FPutilities.floatingStringToByteArray(getFPDataType(), editBuffer, getFPDataType().getBitsize());

            // If we're in little endian mode, reverse the bytes, which is required
            // due to lower-level internal conversion when written to memory.

            if (fIsDisplayLittleEndian)
                targetByteArray = FPutilities.reverseByteOrder(targetByteArray);

            FPMemoryByte[] newMemoryBytes = new FPMemoryByte[targetByteArray.length];

            for (int index = 0; index < targetByteArray.length; index++)
            {
                newMemoryBytes[index] = new FPMemoryByte(targetByteArray[index]);
                newMemoryBytes[index].setBigEndian(!isTargetLittleEndian());
                newMemoryBytes[index].setEdited(true);
                newMemoryBytes[index].setChanged(true);
            }

            //  Apply the change and make it visible in the view

            getViewportCache().setEditedValue(memoryAddress, newMemoryBytes);
            getViewportCache().writeEditBuffer();
            redraw();
        }
        else
        {
            // The edit buffer string is null or has a zero length.

            final String errorText = NLS.bind(FPRenderingMessages.getString("FPRendering.ERROR_FPENTRY_POPUP_TEXT"), "<blanks>"); //$NON-NLS-1$ //$NON-NLS-2$

            try
            {
                // Restore the previous value
                setEditBuffer(new StringBuffer(fDataPane.bytesToSciNotation(getBytes(fCaretAddress, getFPDataType().getByteLength()))));
            }
            catch (DebugException e)
            {
                e.printStackTrace();
            }

            // Put together the pop-up window components and show the user the error

            String statusString = FPRenderingMessages.getString("FPRendering.ERROR_FPENTRY_STATUS"); //$NON-NLS-1$
            Status status = new Status(IStatus.ERROR, FPRenderingPlugin.getUniqueIdentifier(), statusString);
            FPutilities.popupMessage(FPRenderingMessages.getString("FPRendering.ERROR_FPENTRY_POPUP_TITLE"), errorText, status); //$NON-NLS-1$
        }
    }

    // Getter/setter for Insert Mode state

    public void setInsertMode(boolean insertMode)
    {
        this.fEditInserMode = insertMode;
    }

    public boolean insertMode()
    {
        return fEditInserMode;
    }

    // Getter/setter for cell edit state:  true = we're in cell edit mode; false = we're not in cell edit mode

    public boolean isEditingCell()
    {
        return fCellEditState;
    }

    public void setEditingCell(boolean state)
    {
        this.fCellEditState = state;
    }

    // Getter/setter for the address of the cell currently being edited

    public BigInteger getCellEditAddress()
    {
        return cellEditAddress;
    }

    public void setCellEditAddress(BigInteger cellEditAddress)
    {
        this.cellEditAddress = cellEditAddress;
    }

    // Getter/Setter for the memory address of the cell currently being edited

    public BigInteger getMemoryAddress()
    {
        return memoryAddress;
    }

    public void setMemoryAddress(BigInteger memoryAddress)
    {
        this.memoryAddress = memoryAddress;
    }

    // Getter/setter for storing the raw/uninterpreted/not-converted-to-scientific-notation text entry string

    public StringBuffer getEditBuffer()
    {
        return fEditBuffer;
    }

    public void setEditBuffer(StringBuffer cellChars)
    {
        this.fEditBuffer = cellChars;
    }

    // Enter cell-edit mode

    public void startCellEditing(BigInteger cellAddress, BigInteger memoryAddress, String editString)
    {
        setEditBuffer(new StringBuffer(editString));
        setCellEditAddress(cellAddress);
        setMemoryAddress(memoryAddress);
        setEditingCell(true);
    }

    // Exit cell-edit mode

    public void endCellEditing()
    {
        setEditingCell(false);
        setCellEditAddress(null);
        setMemoryAddress(null);
        setEditBuffer(null);

    }

    // Floating point number edit mode status-line display: 'true' = display edit mode, 'false' clear edit mode

    public void displayEditModeIndicator(final boolean indicatorON)
    {
        UIJob job = new UIJob("FP Renderer Edit Indicator") //$NON-NLS-1$
        {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor)
            {
                String statusLineMessage;
                IViewPart viewInstance = null;

                if (indicatorON)
                {
                    // Construct the edit mode message
                    statusLineMessage = NLS.bind(FPRenderingMessages.getString("FPRendering.EDIT_MODE"), //$NON-NLS-1$
                                 (insertMode() ? FPRenderingMessages.getString("FPRendering.EDIT_MODE_INSERT") : //$NON-NLS-1$
                                                 FPRenderingMessages.getString("FPRendering.EDIT_MODE_OVERWRITE"))); //$NON-NLS-1$
                }
                else
                {
                    // 'null' = clear the message
                    statusLineMessage = null;
                }

                // Get the window and page references

                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) return Status.OK_STATUS;

                IWorkbenchPage page = window.getActivePage();
                if (page == null) return Status.OK_STATUS;

                // Update (or clear) the Workbench status line when the Memory and Memory Browser views are in focus

                viewInstance = page.findView("org.eclipse.debug.ui.MemoryView");    // TODO:  programmatically retrieve ID //$NON-NLS-1$

                if (viewInstance != null)
                    viewInstance.getViewSite().getActionBars().getStatusLineManager().setMessage(statusLineMessage);

                viewInstance = page.findView("org.eclipse.cdt.debug.ui.memory.memorybrowser.MemoryBrowser");    // TODO:  programmatically retrieve ID //$NON-NLS-1$

                if (viewInstance != null)
                    viewInstance.getViewSite().getActionBars().getStatusLineManager().setMessage(statusLineMessage);

                return Status.OK_STATUS;
            }
        };

        job.setSystem(true);
        job.schedule();
    }

    // Calculate memory address from the cell address

    public BigInteger cellToMemoryAddress(BigInteger cellAddress)
    {
        BigInteger vpStart  = getViewportStartAddress();
        BigInteger colChars = BigInteger.valueOf(getCharsPerColumn());
        BigInteger dtBytes  = BigInteger.valueOf(getFPDataType().getByteLength());

        return vpStart.add(((cellAddress.subtract(vpStart)).divide(colChars)).multiply(dtBytes));
    }
}
