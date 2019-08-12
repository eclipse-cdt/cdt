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
 *     Alvaro Sanchez-Leon (Ericsson AB) - [Memory] Support 16 bit addressable size (Bug 426730)
 *     Ling Wang (Silicon Laboratories) - Honor start address (Bug 414519)
 *     Teodor Madan (Freescale) - Fix scrolling for memory spaces with 64-bit address
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.debug.core.model.IMemoryBlockAddressInfoRetrieval.IMemoryBlockAddressInfoItem;
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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
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

@SuppressWarnings("restriction")
public class Rendering extends Composite implements IDebugEventSetListener {
	// the IMemoryRendering parent
	private TraditionalRendering fParent;

	// controls

	protected AddressPane fAddressPane;

	protected DataPane fBinaryPane;

	protected TextPane fTextPane;

	private GoToAddressComposite fAddressBar;

	protected Control fAddressBarControl;

	private Selection fSelection = new Selection();

	// storage

	BigInteger fViewportAddress = null; // default visibility for performance

	BigInteger fMemoryBlockStartAddress = null;
	BigInteger fMemoryBlockEndAddress = null;

	protected BigInteger fBaseAddress = null; // remember the base address

	protected int fColumnCount = 0; // auto calculate can be disabled by user,
	// making this user settable

	protected int fBytesPerRow = 0; // current number of bytes per row are displayed

	private int fCurrentScrollSelection = 0; // current scroll selection;

	private BigInteger fCaretAddress;

	// user settings

	private int fTextMode = 1; // ASCII default, TODO make preference?

	private int fBytesPerColumn = 4; // 4 byte cell width default

	private int fRadix = RADIX_HEX;

	private int fColumnsSetting = COLUMNS_AUTO_SIZE_TO_FIT;

	private boolean fIsTargetLittleEndian = false;

	private boolean fIsDisplayLittleEndian = false;

	// constants used to identify radix
	public final static int RADIX_HEX = 1;

	public final static int RADIX_DECIMAL_SIGNED = 2;

	public final static int RADIX_DECIMAL_UNSIGNED = 3;

	public final static int RADIX_OCTAL = 4;

	public final static int RADIX_BINARY = 5;

	// constants used to identify panes
	public final static int PANE_ADDRESS = 1;

	public final static int PANE_BINARY = 2;

	public final static int PANE_TEXT = 3;

	// constants used to identify text, maybe java should be queried for all available sets
	public final static int TEXT_ISO_8859_1 = 1;
	public final static int TEXT_USASCII = 2;
	public final static int TEXT_UTF8 = 3;
	protected final static int TEXT_UTF16 = 4;

	// internal constants
	public final static int COLUMNS_AUTO_SIZE_TO_FIT = 0;

	// view internal settings
	private int fCellPadding = 2;

	private int fPaneSpacing = 16;

	private String fPaddingString = "?"; //$NON-NLS-1$

	// flag whether the memory cache is dirty
	private boolean fCacheDirty = false;

	// update modes
	public final static int UPDATE_ALWAYS = 1;
	public final static int UPDATE_ON_BREAKPOINT = 2;
	public final static int UPDATE_MANUAL = 3;
	public int fUpdateMode = UPDATE_ALWAYS;

	/**
	 * Maintains the subset of items visible in the current view address range.
	 * This information is refreshed when the associated Panes are about to be redrawn
	 * Note: Only the start address of each entry is included i.e. one entry per information item
	 * @since 1.4
	 */
	protected final Map<BigInteger, List<IMemoryBlockAddressInfoItem>> fMapStartAddrToInfoItems = Collections
			.synchronizedMap(new HashMap<BigInteger, List<IMemoryBlockAddressInfoItem>>());

	/**
	 * Maps any address within a memory information range to a corresponding list of information items
	 * sharing that address.
	 * This is useful to e.g. produce a tooltip while hovering over any memory location of a memory information range
	 *
	 * @since 1.5
	 */
	protected final Map<BigInteger, List<IMemoryBlockAddressInfoItem>> fMapAddrToInfoItems = Collections
			.synchronizedMap(new HashMap<BigInteger, List<IMemoryBlockAddressInfoItem>>());

	public Rendering(Composite parent, TraditionalRendering renderingParent) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);

		this.setFont(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME)); // TODO internal?

		this.fParent = renderingParent;

		// initialize the viewport start
		if (fParent.getMemoryBlock() != null) {
			// This is base address from user.
			// Honor it if the block has no limits or the base is within the block limits.
			// Fix Bug 414519.
			BigInteger base = fParent.getBigBaseAddress();

			fViewportAddress = fParent.getMemoryBlockStartAddress();

			// this will be null if memory may be retrieved at any address less than
			// this memory block's base.  if so use the base address.
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

		// instantiate the panes, TODO default visibility from state or
		// plugin.xml?
		this.fAddressPane = createAddressPane();
		this.fBinaryPane = createDataPane();
		this.fTextPane = createTextPane();

		fAddressBar = new GoToAddressComposite();
		fAddressBarControl = fAddressBar.createControl(parent);
		Button button = fAddressBar.getButton(IDialogConstants.OK_ID);
		if (button != null) {
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					doGoToAddress();
				}
			});

			button = fAddressBar.getButton(IDialogConstants.CANCEL_ID);
			if (button != null) {
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						setVisibleAddressBar(false);
					}
				});
			}
		}

		fAddressBar.getExpressionWidget().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				doGoToAddress();
			}
		});

		fAddressBar.getExpressionWidget().addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC)
					setVisibleAddressBar(false);
				super.keyPressed(e);
			}
		});

		this.fAddressBarControl.setVisible(false);

		getHorizontalBar().addSelectionListener(createHorizontalBarSelectionListener());

		getVerticalBar().addSelectionListener(createVerticalBarSelectinListener());

		this.addPaintListener(pe -> {
			pe.gc.setBackground(Rendering.this.getTraditionalRendering().getColorBackground());
			pe.gc.fillRectangle(0, 0, Rendering.this.getBounds().width, Rendering.this.getBounds().height);
		});

		setLayout();

		this.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent ce) {
			}

			@Override
			public void controlResized(ControlEvent ce) {
				packColumns();
			}
		});

		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	protected void setLayout() {
		this.setLayout(new Layout() {
			@Override
			public void layout(Composite composite, boolean changed) {
				int xOffset = 0;
				if (Rendering.this.getHorizontalBar().isVisible())
					xOffset = Rendering.this.getHorizontalBar().getSelection();

				int x = xOffset * -1;
				int y = 0;

				if (fAddressBarControl.isVisible()) {
					fAddressBarControl.setBounds(0, 0, Rendering.this.getBounds().width,
							fAddressBarControl.computeSize(100, 30).y); // FIXME
					//y = fAddressBarControl.getBounds().height;
				}

				if (fAddressPane.isPaneVisible()) {
					fAddressPane.setBounds(x, y, fAddressPane.computeSize(0, 0).x,
							Rendering.this.getBounds().height - y);
					x = fAddressPane.getBounds().x + fAddressPane.getBounds().width;
				}

				if (fBinaryPane.isPaneVisible()) {
					fBinaryPane.setBounds(x, y, fBinaryPane.computeSize(0, 0).x, Rendering.this.getBounds().height - y);
					x = fBinaryPane.getBounds().x + fBinaryPane.getBounds().width;
				}

				if (fTextPane.isPaneVisible()) {
					fTextPane.setBounds(x, y,
							Math.max(fTextPane.computeSize(0, 0).x, Rendering.this.getClientArea().width - x - xOffset),
							Rendering.this.getBounds().height - y);
				}

				if (getClientArea().width >= fTextPane.getBounds().x + fTextPane.getBounds().width + xOffset) {
					Rendering.this.getHorizontalBar().setVisible(false);
				} else {
					ScrollBar horizontal = Rendering.this.getHorizontalBar();

					horizontal.setVisible(true);
					horizontal.setMinimum(0);
					horizontal.setMaximum(fTextPane.getBounds().x + fTextPane.getBounds().width + xOffset);
					horizontal.setThumb(getClientArea().width);
					horizontal.setPageIncrement(40); // TODO ?
					horizontal.setIncrement(20); // TODO ?
				}
			}

			@Override
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(100, 100); // dummy data
			}
		});
	}

	protected void handleDownArrow() {
		fViewportAddress = fViewportAddress.add(BigInteger.valueOf(getAddressableCellsPerRow()));
		ensureViewportAddressDisplayable();
		redrawPanes();
	}

	protected void handleUpArrow() {
		fViewportAddress = fViewportAddress.subtract(BigInteger.valueOf(getAddressableCellsPerRow()));
		ensureViewportAddressDisplayable();
		redrawPanes();
	}

	protected void handlePageDown() {
		fViewportAddress = fViewportAddress
				.add(BigInteger.valueOf(getAddressableCellsPerRow() * (Rendering.this.getRowCount() - 1)));
		ensureViewportAddressDisplayable();
		redrawPanes();
	}

	protected void handlePageUp() {
		fViewportAddress = fViewportAddress
				.subtract(BigInteger.valueOf(getAddressableCellsPerRow() * (Rendering.this.getRowCount() - 1)));
		ensureViewportAddressDisplayable();
		redrawPanes();
	}

	protected SelectionListener createHorizontalBarSelectionListener() {
		return new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				Rendering.this.layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
				// do nothing
			}
		};
	}

	protected SelectionListener createVerticalBarSelectinListener() {
		return new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				switch (se.detail) {
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
					// Figure out the delta, ignore events with no delta
					int deltaScroll = getVerticalBar().getSelection() - fCurrentScrollSelection;
					if (deltaScroll == 0)
						break;

					fViewportAddress = fViewportAddress.add(
							BigInteger.valueOf(getAddressableCellsPerRow()).multiply(BigInteger.valueOf(deltaScroll)));

					ensureViewportAddressDisplayable();
					// Update tooltip
					// FIXME conversion from slider to scrollbar
					// getVerticalBar().setToolTipText(Rendering.this.getAddressString(fViewportAddress));

					// Update the addresses on the Address pane.
					if (fAddressPane.isPaneVisible()) {
						fAddressPane.redraw();
					}
					redrawPanes();
					break;
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent se) {
				// do nothing
			}
		};
	}

	protected AddressPane createAddressPane() {
		return new AddressPane(this);
	}

	protected DataPane createDataPane() {
		return new DataPane(this);
	}

	protected TextPane createTextPane() {
		return new TextPane(this);
	}

	public TraditionalRendering getTraditionalRendering() // TODO rename
	{
		return fParent;
	}

	protected void setCaretAddress(BigInteger address) {
		fCaretAddress = address;
	}

	protected BigInteger getCaretAddress() {
		// Return the caret address if it has been set, otherwise return the
		// viewport address. When the rendering is first created, the caret is
		// unset until the user clicks somewhere in the rendering. It also reset
		// (unset) when the user gives us a new viewport address
		return (fCaretAddress != null) ? fCaretAddress : fViewportAddress;
	}

	private void doGoToAddress() {
		try {
			BigInteger address = fAddressBar.getGoToAddress(this.getMemoryBlockStartAddress(), this.getCaretAddress());
			getTraditionalRendering().gotoAddress(address);
			setVisibleAddressBar(false);
		} catch (NumberFormatException e1) {
			// FIXME log?
		}
	}

	// Ensure that all addresses displayed are within the addressable range
	protected void ensureViewportAddressDisplayable() {
		if (fViewportAddress.compareTo(Rendering.this.getMemoryBlockStartAddress()) < 0) {
			fViewportAddress = Rendering.this.getMemoryBlockStartAddress();
		} else if (getViewportEndAddress().compareTo(getMemoryBlockEndAddress().add(BigInteger.ONE)) > 0) {
			fViewportAddress = getMemoryBlockEndAddress()
					.subtract(BigInteger.valueOf(getAddressableCellsPerRow() * getRowCount() - 1));
		}

		setCurrentScrollSelection();
	}

	public IMemorySelection getSelection() {
		return fSelection;
	}

	protected int getHistoryDepth() {
		return fViewportCache.getHistoryDepth();
	}

	protected void setHistoryDepth(int depth) {
		fViewportCache.setHistoryDepth(depth);
	}

	public void logError(String message, Exception e) {
		Status status = new Status(IStatus.ERROR, fParent.getRenderingId(), DebugException.INTERNAL_ERROR, message, e);

		TraditionalRenderingPlugin.getDefault().getLog().log(status);
	}

	public void handleFontPreferenceChange(Font font) {
		setFont(font);

		Control controls[] = this.getRenderingPanes();
		for (int i = 0; i < controls.length; i++)
			controls[i].setFont(font);

		packColumns();
		layout(true);
	}

	public void setPaddingString(String padding) {
		fPaddingString = padding;

		refresh();
	}

	public char getPaddingCharacter() {
		return fPaddingString.charAt(0); // use only the first character
	}

	static int suspendCount = 0;

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		if (this.isDisposed())
			return;

		boolean isChangeOnly = false;
		boolean isSuspend = false;
		boolean isBreakpointHit = false;

		for (int i = 0; i < events.length; i++) {
			if (events[0].getSource() instanceof IDebugElement) {
				final int kind = events[i].getKind();
				final int detail = events[i].getDetail();
				final IDebugElement source = (IDebugElement) events[i].getSource();

				if (source.getDebugTarget() == getMemoryBlock().getDebugTarget()) {
					/* For CDT/DSF or CDT/TCF the IDebugTarget interface is not longer used,
					 * and returns null.  In such a case, we should check that we are dealing
					 * with the correct memory block instead.
					 */
					if (source.getDebugTarget() != null || source.equals(getMemoryBlock())) {
						if ((detail & DebugEvent.BREAKPOINT) != 0)
							isBreakpointHit = true;
						if (kind == DebugEvent.SUSPEND) {
							handleSuspendEvent(detail);
							isSuspend = true;
						} else if (kind == DebugEvent.CHANGE) {
							handleChangeEvent();
							isChangeOnly = true;
						}
					}
				}
			}
		}

		if (isSuspend)
			handleSuspend(isBreakpointHit);
		else if (isChangeOnly)
			handleChange();
	}

	protected void handleSuspend(boolean isBreakpointHit) {
		if (getUpdateMode() == UPDATE_ALWAYS || (getUpdateMode() == UPDATE_ON_BREAKPOINT && isBreakpointHit)) {
			Display.getDefault().asyncExec(() -> {
				archiveDeltas();
				refresh();
			});
		}
	}

	protected void handleChange() {
		if (getUpdateMode() == UPDATE_ALWAYS) {
			Display.getDefault().asyncExec(() -> {
				archiveDeltas();
				refresh();
			});
		}
	}

	protected void handleSuspendEvent(int detail) {
	}

	protected void handleChangeEvent() {
	}

	// return true to enable development debug print statements
	public boolean isDebug() {
		return false;
	}

	protected IMemoryBlockExtension getMemoryBlock() {
		IMemoryBlock block = fParent.getMemoryBlock();
		if (block != null)
			return block.getAdapter(IMemoryBlockExtension.class);

		return null;
	}

	public BigInteger getBigBaseAddress() {
		return fParent.getBigBaseAddress();
	}

	public int getAddressableSize() {
		return fParent.getAddressableSize();
	}

	protected IViewportCache getViewportCache() {
		return fViewportCache;
	}

	public TraditionalMemoryByte[] getBytes(BigInteger address, int bytes) throws DebugException {
		return getViewportCache().getBytes(address, bytes);
	}

	// default visibility for performance
	ViewportCache fViewportCache = new ViewportCache();

	private BigInteger fScrollStartAddress = BigInteger.ZERO;

	private interface Request {
	}

	class ViewportCache extends Thread implements IViewportCache {
		class ArchiveDeltas implements Request {

		}

		class AddressPair implements Request {
			BigInteger startAddress;

			BigInteger endAddress;

			public AddressPair(BigInteger start, BigInteger end) {
				startAddress = start;
				endAddress = end;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == null)
					return false;
				if (obj instanceof AddressPair) {
					return ((AddressPair) obj).startAddress.equals(startAddress)
							&& ((AddressPair) obj).endAddress.equals(endAddress);
				}

				return false;
			}

		}

		class MemoryUnit {
			BigInteger start;

			BigInteger end;

			TraditionalMemoryByte[] bytes;

			@Override
			public MemoryUnit clone() {
				MemoryUnit b = new MemoryUnit();

				b.start = this.start;
				b.end = this.end;
				b.bytes = new TraditionalMemoryByte[this.bytes.length];
				for (int i = 0; i < this.bytes.length; i++)
					b.bytes[i] = new TraditionalMemoryByte(this.bytes[i].getValue());

				return b;
			}

			public boolean isValid() {
				return this.start != null && this.end != null && this.bytes != null;
			}
		}

		private HashMap<BigInteger, TraditionalMemoryByte[]> fEditBuffer = new HashMap<>();

		private boolean fDisposed = false;

		private Object fLastQueued = null;

		private Vector<Object> fQueue = new Vector<>();

		protected MemoryUnit fCache = null;

		protected MemoryUnit fHistoryCache[] = new MemoryUnit[0];

		protected int fHistoryDepth = 0;

		public ViewportCache() {
			start();
		}

		@Override
		public void dispose() {
			fDisposed = true;
			synchronized (fQueue) {
				fQueue.notify();
			}
		}

		public int getHistoryDepth() {
			return fHistoryDepth;
		}

		public void setHistoryDepth(int depth) {
			fHistoryDepth = depth;
			fHistoryCache = new MemoryUnit[fHistoryDepth];
		}

		@Override
		public void refresh() {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			if (fCache != null) {
				queueRequest(fViewportAddress, getViewportEndAddress());
			}
		}

		@Override
		public void archiveDeltas() {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			if (fCache != null) {
				queueRequestArchiveDeltas();
			}
		}

		private void queueRequest(BigInteger startAddress, BigInteger endAddress) {
			AddressPair pair = new AddressPair(startAddress, endAddress);
			queue(pair);
		}

		private void queueRequestArchiveDeltas() {
			ArchiveDeltas archive = new ArchiveDeltas();
			queue(archive);
		}

		private void queue(Object element) {
			synchronized (fQueue) {
				if (!(fQueue.size() > 0 && element.equals(fLastQueued))) {
					fQueue.addElement(element);
					fLastQueued = element;
				}
				fQueue.notify();
			}
		}

		@Override
		public void run() {
			while (!fDisposed) {
				AddressPair pair = null;
				boolean archiveDeltas = false;
				synchronized (fQueue) {
					if (fQueue.size() > 0) {
						Request request = (Request) fQueue.elementAt(0);
						Class<?> type = request.getClass();

						while (fQueue.size() > 0 && type.isInstance(fQueue.elementAt(0))) {
							request = (Request) fQueue.elementAt(0);
							fQueue.removeElementAt(0);
						}

						if (request instanceof ArchiveDeltas)
							archiveDeltas = true;
						else if (request instanceof AddressPair)
							pair = (AddressPair) request;
					}
				}
				if (archiveDeltas) {
					for (int i = fViewportCache.getHistoryDepth() - 1; i > 0; i--)
						fHistoryCache[i] = fHistoryCache[i - 1];

					fHistoryCache[0] = fCache.clone();
				} else if (pair != null) {
					populateCache(pair.startAddress, pair.endAddress);
				} else {
					synchronized (fQueue) {
						try {
							if (fQueue.isEmpty()) {
								fQueue.wait();
							}
						} catch (Exception e) {
							// do nothing
						}
					}
				}
			}
		}

		// cache memory necessary to paint viewport
		// TODO: user setting to buffer +/- x lines
		// TODO: reuse existing cache? probably only a minor performance gain
		private void populateCache(final BigInteger startAddress, final BigInteger endAddress) {
			try {
				IMemoryBlockExtension memoryBlock = getMemoryBlock();

				final BigInteger addressableSize = BigInteger.valueOf(getAddressableSize());
				BigInteger lengthInBytes = endAddress.subtract(startAddress).multiply(addressableSize);

				long units = lengthInBytes.divide(addressableSize)
						.add(lengthInBytes.mod(addressableSize).compareTo(BigInteger.ZERO) > 0 ? BigInteger.ONE
								: BigInteger.ZERO)
						.longValue();

				// CDT (and maybe other backends) will call setValue() on these MemoryBlock objects.
				// We don't want this to happen, because it interferes with this rendering's own
				// change history. Ideally, we should strictly use the back end change notification
				// and history, but it is only guaranteed to work for bytes within the address range
				// of the MemoryBlock.
				MemoryByte readBytes[] = memoryBlock.getBytesFromAddress(startAddress, units);

				TraditionalMemoryByte cachedBytes[] = new TraditionalMemoryByte[readBytes.length];
				for (int i = 0; i < readBytes.length; i++)
					cachedBytes[i] = new TraditionalMemoryByte(readBytes[i].getValue(), readBytes[i].getFlags());

				// derive the target endian from the read MemoryBytes.
				if (cachedBytes.length > 0) {
					if (cachedBytes[0].isEndianessKnown()) {
						setTargetLittleEndian(!cachedBytes[0].isBigEndian());
					}
				}

				// reorder bytes within unit to be a sequential byte stream if the endian is already little
				if (isTargetLittleEndian()) {
					// there isn't an order when the unit size is one, so skip for performance
					if (addressableSize.compareTo(BigInteger.ONE) != 0) {
						int unitSize = addressableSize.intValue();
						TraditionalMemoryByte cachedBytesAsByteSequence[] = new TraditionalMemoryByte[cachedBytes.length];
						for (int unit = 0; unit < units; unit++) {
							for (int unitbyte = 0; unitbyte < unitSize; unitbyte++) {
								cachedBytesAsByteSequence[unit * unitSize + unitbyte] = cachedBytes[unit * unitSize
										+ unitSize - unitbyte];
							}
						}
						cachedBytes = cachedBytesAsByteSequence;
					}
				}

				final TraditionalMemoryByte[] cachedBytesFinal = cachedBytes;

				fCache = new MemoryUnit();
				fCache.start = startAddress;
				fCache.end = endAddress;
				fCache.bytes = cachedBytesFinal;

				Display.getDefault().asyncExec(() -> {
					// generate deltas
					for (int historyIndex = 0; historyIndex < getHistoryDepth(); historyIndex++) {
						if (fHistoryCache[historyIndex] != null && fHistoryCache[historyIndex].isValid()) {
							BigInteger maxStart = startAddress.max(fHistoryCache[historyIndex].start);
							BigInteger minEnd = endAddress.min(fHistoryCache[historyIndex].end)
									.subtract(BigInteger.valueOf(1));

							BigInteger overlapLength = minEnd.subtract(maxStart).multiply(addressableSize);
							if (overlapLength.compareTo(BigInteger.valueOf(0)) > 0) {
								// there is overlap

								int offsetIntoOld = (maxStart.subtract(fHistoryCache[historyIndex].start)
										.multiply(addressableSize)).intValue();
								int offsetIntoNew = maxStart.subtract(startAddress).multiply(addressableSize)
										.intValue();

								for (int i = overlapLength.intValue(); i >= 0; i--) {
									cachedBytesFinal[offsetIntoNew + i].setChanged(historyIndex,
											cachedBytesFinal[offsetIntoNew + i]
													.getValue() != fHistoryCache[historyIndex].bytes[offsetIntoOld + i]
															.getValue());
								}
							}
						}
					}

					// If the history does not exist, populate the history with the just populated cache. This solves the
					// use case of 1) connect to target; 2) edit memory before the first suspend debug event; 3) paint
					// differences in changed color.
					if (fHistoryCache[0] == null)
						fHistoryCache[0] = fCache.clone();

					Rendering.this.redrawPanes();
				});

			} catch (Exception e) {
				// User can scroll to any memory, whether it's valid on the
				// target or not. Doesn't make much sense to fill up the Eclipse
				// error log with such "failures".
				//                logError(
				//                    TraditionalRenderingMessages
				//                        .getString("TraditionalRendering.FAILURE_READ_MEMORY"), e); //$NON-NLS-1$
			}
		}

		// bytes will be fetched from cache
		@Override
		public TraditionalMemoryByte[] getBytes(BigInteger address, int bytesRequested) throws DebugException {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			//calculate the number of units needed for the number of requested bytes
			int rem = (bytesRequested % getAddressableSize()) > 0 ? 1 : 0;
			int units = bytesRequested / getAddressableSize() + rem;

			if (containsEditedCell(address)) // cell size cannot be switched during an edit
				return getEditedMemory(address);

			boolean contains = false;
			if (fCache != null && fCache.start != null) {
				// see if all of the data requested is in the cache
				BigInteger dataEnd = address.add(BigInteger.valueOf(units));

				if (fCache.start.compareTo(address) <= 0 && fCache.end.compareTo(dataEnd) >= 0
						&& fCache.bytes.length > 0)
					contains = true;
			}

			if (contains) {
				int offset = address.subtract(fCache.start).multiply(BigInteger.valueOf(getAddressableSize()))
						.intValue();
				TraditionalMemoryByte bytes[] = new TraditionalMemoryByte[bytesRequested];
				for (int i = 0; i < bytes.length; i++) {
					bytes[i] = fCache.bytes[offset + i];
				}

				return bytes;
			}

			TraditionalMemoryByte bytes[] = new TraditionalMemoryByte[bytesRequested];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = new TraditionalMemoryByte();
				bytes[i].setReadable(false);
			}

			fViewportCache.queueRequest(fViewportAddress, getViewportEndAddress());

			return bytes;
		}

		@Override
		public boolean containsEditedCell(BigInteger address) {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			return fEditBuffer.containsKey(address);
		}

		private TraditionalMemoryByte[] getEditedMemory(BigInteger address) {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			return fEditBuffer.get(address);
		}

		@Override
		public void clearEditBuffer() {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			fEditBuffer.clear();
			Rendering.this.redrawPanes();
		}

		@Override
		public void writeEditBuffer() {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			Set<BigInteger> keySet = fEditBuffer.keySet();
			Iterator<BigInteger> iterator = keySet.iterator();

			while (iterator.hasNext()) {
				BigInteger address = iterator.next();
				TraditionalMemoryByte[] bytes = fEditBuffer.get(address);

				byte byteValue[] = new byte[bytes.length];
				for (int i = 0; i < bytes.length; i++)
					byteValue[i] = bytes[i].getValue();

				try {
					IMemoryBlockExtension block = getMemoryBlock();
					BigInteger offset = address.subtract(block.getBigBaseAddress());
					block.setValue(offset, byteValue);
				} catch (Exception e) {
					MemoryViewUtil.openError(
							TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_WRITE_MEMORY"), "", e);

					logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_WRITE_MEMORY"), e); //$NON-NLS-1$
				}
			}

			clearEditBuffer();
		}

		@Override
		public void setEditedValue(BigInteger address, TraditionalMemoryByte[] bytes) {
			assert Thread.currentThread().equals(Display.getDefault().getThread()) : TraditionalRenderingMessages
					.getString("TraditionalRendering.CALLED_ON_NON_DISPATCH_THREAD"); //$NON-NLS-1$

			fEditBuffer.put(address, bytes);
			Rendering.this.redrawPanes();
		}
	}

	public void setVisibleAddressBar(boolean visible) {
		fAddressBarControl.setVisible(visible);
		if (visible) {
			String selectedStr = "0x" + getCaretAddress().toString(16);
			Text text = fAddressBar.getExpressionWidget();
			text.setText(selectedStr);
			text.setSelection(0, text.getCharCount());
			fAddressBar.getExpressionWidget().setFocus();
		}

		layout(true);
		layoutPanes();
	}

	public void setDirty(boolean needRefresh) {
		fCacheDirty = needRefresh;
	}

	public boolean isDirty() {
		return fCacheDirty;
	}

	@Override
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		if (fViewportCache != null) {
			fViewportCache.dispose();
			fViewportCache = null;
		}

		fMapStartAddrToInfoItems.clear();
		fMapAddrToInfoItems.clear();
		super.dispose();
	}

	class Selection implements IMemorySelection {
		private BigInteger fStartHigh;
		private BigInteger fStartLow;

		private BigInteger fEndHigh;
		private BigInteger fEndLow;

		@Override
		public void clear() {
			fEndHigh = fEndLow = fStartHigh = fStartLow = null;
			redrawPanes();
		}

		@Override
		public boolean hasSelection() {
			return fStartHigh != null && fStartLow != null && fEndHigh != null && fEndLow != null;
		}

		@Override
		public boolean isSelected(BigInteger address) {
			// do we have valid start and end addresses
			if (getEnd() == null || getStart() == null)
				return false;

			// if end is greater than start
			if (getEnd().compareTo(getStart()) >= 0) {
				if (address.compareTo(getStart()) >= 0 && address.compareTo(getEnd()) < 0)
					return true;
			}
			// if start is greater than end
			else if (getStart().compareTo(getEnd()) >= 0) {
				if (address.compareTo(getEnd()) >= 0 && address.compareTo(getStart()) < 0)
					return true;
			}

			return false;
		}

		@Override
		public void setStart(BigInteger high, BigInteger low) {
			if (high == null && low == null) {
				if (fStartHigh != null && fStartLow != null) {
					fStartHigh = null;
					fStartLow = null;
					redrawPanes();
				}

				return;
			}

			boolean changed = false;

			if (fStartHigh == null || !fStartHigh.equals(high)) {
				fStartHigh = high;
				changed = true;
			}

			if (fStartLow == null || !low.equals(fStartLow)) {
				fStartLow = low;
				changed = true;
			}

			if (changed)
				redrawPanes();
		}

		@Override
		public void setEnd(BigInteger high, BigInteger low) {
			if (high == null && low == null) {
				if (fEndHigh != null && fEndLow != null) {
					fEndHigh = null;
					fEndLow = null;
					redrawPanes();
				}

				return;
			}

			boolean changed = false;

			if (fEndHigh == null || !fEndHigh.equals(high)) {
				fEndHigh = high;
				changed = true;
			}

			if (fEndLow == null || !low.equals(fEndLow)) {
				fEndLow = low;
				changed = true;
			}

			if (changed)
				redrawPanes();
		}

		@Override
		public BigInteger getHigh() {
			if (!hasSelection())
				return null;

			return getStart().max(getEnd());
		}

		@Override
		public BigInteger getLow() {
			if (!hasSelection())
				return null;

			return getStart().min(getEnd());
		}

		@Override
		public BigInteger getStart() {
			// if there is no start, return null
			if (fStartHigh == null)
				return null;

			// if there is no end, return the high address of the start
			if (fEndHigh == null)
				return fStartHigh;

			// if Start High/Low equal End High/Low, return a low start and high end
			if (fStartHigh.equals(fEndHigh) && fStartLow.equals(fEndLow))
				return fStartLow;

			BigInteger differenceEndToStartHigh = fEndHigh.subtract(fStartHigh).abs();
			BigInteger differenceEndToStartLow = fEndHigh.subtract(fStartLow).abs();

			// return the start high or start low based on which creates a larger selection
			if (differenceEndToStartHigh.compareTo(differenceEndToStartLow) > 0)
				return fStartHigh;
			else
				return fStartLow;
		}

		@Override
		public BigInteger getStartLow() {
			return fStartLow;
		}

		@Override
		public BigInteger getEnd() {
			// if there is no end, return null
			if (fEndHigh == null)
				return null;

			// if Start High/Low equal End High/Low, return a low start and high end
			if (fStartHigh.equals(fEndHigh) && fStartLow.equals(fEndLow))
				return fStartHigh;

			BigInteger differenceStartToEndHigh = fStartHigh.subtract(fEndHigh).abs();
			BigInteger differenceStartToEndLow = fStartHigh.subtract(fEndLow).abs();

			// return the start high or start low based on which creates a larger selection
			if (differenceStartToEndHigh.compareTo(differenceStartToEndLow) >= 0)
				return fEndHigh;
			else
				return fEndLow;
		}
	}

	public void setPaneVisible(int pane, boolean visible) {
		switch (pane) {
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

	public boolean getPaneVisible(int pane) {
		switch (pane) {
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

	protected void packColumns() {
		int availableWidth = Rendering.this.getSize().x;

		if (fAddressPane.isPaneVisible()) {
			availableWidth -= fAddressPane.computeSize(0, 0).x;
			availableWidth -= Rendering.this.getRenderSpacing() * 2;
		}

		int combinedWidth = 0;

		if (fBinaryPane.isPaneVisible())
			combinedWidth += fBinaryPane.getCellWidth();

		if (fTextPane.isPaneVisible())
			combinedWidth += fTextPane.getCellWidth();

		if (getColumnsSetting() == Rendering.COLUMNS_AUTO_SIZE_TO_FIT) {
			if (combinedWidth == 0)
				fColumnCount = 0;
			else {
				fColumnCount = availableWidth / combinedWidth;
				if (fColumnCount == 0)
					fColumnCount = 1; // paint one column even if only part can show in view
			}
		} else {
			fColumnCount = getColumnsSetting();
		}

		try {
			// Update the number of bytes per row;
			// the max and min scroll range and the current thumb nail position.
			fBytesPerRow = getBytesPerColumn() * getColumnCount();

			getVerticalBar().setMinimum(1);
			// scrollbar maximum range is Integer.MAX_VALUE.
			getVerticalBar().setMaximum(getMaxScrollLines());
			getVerticalBar().setIncrement(1);
			getVerticalBar().setPageIncrement(this.getRowCount() - 1);
			//TW FIXME conversion of slider to scrollbar
			// fScrollBar.setToolTipText(Rendering.this.getAddressString(fViewportAddress));
			setCurrentScrollSelection();
		} catch (Exception e) {
			// FIXME precautionary
		}

		Rendering.this.redraw();
		Rendering.this.redrawPanes();
	}

	public AbstractPane[] getRenderingPanes() {
		return new AbstractPane[] { fAddressPane, fBinaryPane, fTextPane };
	}

	public int getCellPadding() {
		return fCellPadding;
	}

	protected int getRenderSpacing() {
		return fPaneSpacing;
	}

	public void refresh() {
		if (!this.isDisposed()) {
			if (this.isVisible() && getViewportCache() != null) {
				getViewportCache().refresh();
			} else {
				setDirty(true);
				fParent.updateRenderingLabels();
			}
		}
	}

	protected void archiveDeltas() {
		this.getViewportCache().archiveDeltas();
	}

	public void gotoAddress(BigInteger address) {
		// Ensure that the GoTo address is within the addressable range
		if ((address.compareTo(this.getMemoryBlockStartAddress()) < 0)
				|| (address.compareTo(this.getMemoryBlockEndAddress()) > 0)) {
			return;
		}

		fViewportAddress = address;

		// reset the caret and selection state (no caret and no selection)
		fCaretAddress = null;
		fSelection = new Selection();

		ensureViewportAddressDisplayable();
		redrawPanes();
	}

	public void setViewportStartAddress(BigInteger newAddress) {
		fViewportAddress = newAddress;
	}

	public BigInteger getViewportStartAddress() {
		return fViewportAddress;
	}

	public BigInteger getViewportEndAddress() {
		return fViewportAddress.add(BigInteger.valueOf(this.getBytesPerRow() * getRowCount() / getAddressableSize()));
	}

	private BigInteger getScrollStartAddress() {
		return fScrollStartAddress;
	}

	private void setScrollStartAddress(BigInteger newAddress) {
		fScrollStartAddress = newAddress;
	}

	private BigInteger getScrollEndAddress() {
		int scrollLines = getMaxScrollLines();
		// substract scroll thumb as it is not possible to scroll beyond it
		scrollLines -= getVerticalBar().getThumb();

		BigInteger newAddress = getScrollStartAddress()
				.add(BigInteger.valueOf(getAddressableCellsPerRow()).multiply(BigInteger.valueOf(scrollLines)));

		return newAddress.min(getMemoryBlockEndAddress());
	}

	public String getAddressString(BigInteger address) {
		StringBuilder addressString = new StringBuilder(address.toString(16).toUpperCase());
		for (int chars = getAddressBytes() * 2 - addressString.length(); chars > 0; chars--) {
			addressString.insert(0, '0');
		}
		addressString.insert(0, "0x"); //$NON-NLS-1$

		return addressString.toString();
	}

	protected int getAddressBytes() {
		return fParent.getAddressSize();
	}

	public Control getAddressBarControl() {
		return fAddressBarControl;
	}

	public int getColumnCount() {
		return fColumnCount;
	}

	public int getColumnsSetting() {
		return fColumnsSetting;
	}

	protected void setBytesPerRow(int count) {
		fBytesPerRow = count;
	}

	protected void setColumnCount(int count) {
		fColumnCount = count;
	}

	public void setColumnsSetting(int columns) {
		if (fColumnsSetting != columns) {
			fColumnsSetting = columns;
			fireSettingsChanged();
			layoutPanes();
		}
	}

	protected void ensureVisible(BigInteger address) {
		BigInteger viewportStart = this.getViewportStartAddress();
		BigInteger viewportEnd = this.getViewportEndAddress();

		boolean isAddressBeforeViewportStart = address.compareTo(viewportStart) < 0;
		boolean isAddressAfterViewportEnd = address.compareTo(viewportEnd) > 0;

		if (isAddressBeforeViewportStart || isAddressAfterViewportEnd)
			gotoAddress(address);
	}

	protected int getRowCount() {
		int rowCount = 0;
		Control panes[] = getRenderingPanes();
		for (int i = 0; i < panes.length; i++) {
			if (panes[i] instanceof AbstractPane)
				rowCount = Math.max(rowCount, ((AbstractPane) panes[i]).getRowCount());
		}

		// Add an extra row of information as we can present part of the information on
		// the remaining space of the canvas
		int extra = 1;
		return rowCount + extra;
	}

	public int getBytesPerColumn() {
		return fBytesPerColumn;
	}

	protected int getBytesPerRow() {
		return fBytesPerRow;
	}

	protected int getAddressableCellsPerRow() {
		return getBytesPerRow() / getAddressableSize();
	}

	public int getAddressesPerColumn() {
		return this.getBytesPerColumn() / getAddressableSize();
	}

	/**
	 *  Update current scroll selection to match current viewport range
	 */
	protected void setCurrentScrollSelection() {
		BigInteger viewportStartAddress = getViewportStartAddress();
		if (TraceOptions.DEBUG) {
			TraceOptions.trace(MessageFormat.format("Update scroll for viewrange[0x{0} : 0x{1}]:\n",
					viewportStartAddress.toString(16), getViewportEndAddress().toString(16)));
			TraceOptions.trace(MessageFormat.format("  current ScrollRange=[0x{0} : 0x{1}]; selection = {2}\n",
					getScrollStartAddress().toString(16), getScrollEndAddress().toString(16), fCurrentScrollSelection));
		}
		BigInteger addressableRow = BigInteger.valueOf(getAddressableCellsPerRow());
		if ((viewportStartAddress.compareTo(getScrollStartAddress()) <= 0)) {
			// must reposition scroll area, center position into the center of the new scroll area
			// unless it is already at the start of first line.

			int scrollLines = getMaxScrollLines() / 2;
			BigInteger newScrollStart = BigInteger.ZERO
					.max(viewportStartAddress.subtract(BigInteger.valueOf(scrollLines).multiply(addressableRow)));
			setScrollStartAddress(newScrollStart);

			if (TraceOptions.DEBUG) {
				TraceOptions.trace(MessageFormat.format("  new ScrollRange=[0x{0} : 0x{1}]\n",
						getScrollStartAddress().toString(16), getScrollEndAddress().toString(16)));
			}
		} else if (getViewportEndAddress().compareTo(getScrollEndAddress()) >= 0) {
			// must reposition scroll area, center position into the center of the new scroll area
			//  unless when user gets closer to end of addressable size of memory, when leave
			//  scroll start address at end of block memory scroll start

			BigInteger eomScrollStart = getMemoryBlockEndAddress().add(BigInteger.ONE).subtract(
					BigInteger.valueOf(getMaxScrollLines() - getVerticalBar().getThumb()).multiply(addressableRow));

			int scrollLines = getMaxScrollLines() / 2;
			BigInteger newScrollStart = eomScrollStart
					.min(viewportStartAddress.subtract(BigInteger.valueOf(scrollLines).multiply(addressableRow)));
			setScrollStartAddress(newScrollStart);

			if (TraceOptions.DEBUG) {
				TraceOptions.trace(MessageFormat.format("  new ScrollRange=[0x{0} : 0x{1}]\n",
						getScrollStartAddress().toString(16), getScrollEndAddress().toString(16)));
			}
		}

		// calculate selection line from the start of scroll area
		fCurrentScrollSelection = viewportStartAddress.subtract(getScrollStartAddress()).divide(addressableRow)
				.intValue() + 1;
		if (TraceOptions.DEBUG) {
			TraceOptions.trace(MessageFormat.format("  new selection={0}\n", fCurrentScrollSelection));
		}
		getVerticalBar().setSelection(fCurrentScrollSelection);
	}

	/**
	 * compute the maximum scrolling range.
	 * @return number of lines that rendering can display
	 */
	private int getMaxScrollLines() {
		BigInteger difference = getMemoryBlockEndAddress().subtract(getMemoryBlockStartAddress()).add(BigInteger.ONE);
		BigInteger maxScrollRange = difference.divide(BigInteger.valueOf(getAddressableCellsPerRow()));
		if (maxScrollRange.multiply(BigInteger.valueOf(getAddressableCellsPerRow())).compareTo(difference) != 0)
			maxScrollRange = maxScrollRange.add(BigInteger.ONE); // add a line to hold bytes that do not fill an entire raw

		// support targets with an addressable size greater than 1
		maxScrollRange = maxScrollRange.divide(BigInteger.valueOf(getAddressableSize()));

		// cap to maximum lines a SWT ScrollBar can hold.
		return maxScrollRange.min(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
	}

	/**
	 * @return start address of the memory block
	 */
	protected BigInteger getMemoryBlockStartAddress() {
		if (fMemoryBlockStartAddress == null)
			fMemoryBlockStartAddress = fParent.getMemoryBlockStartAddress();
		if (fMemoryBlockStartAddress == null)
			fMemoryBlockStartAddress = BigInteger.ZERO;

		return fMemoryBlockStartAddress;
	}

	/**
	 * @return end address of the memory block
	 */
	protected BigInteger getMemoryBlockEndAddress() {
		if (fMemoryBlockEndAddress == null)
			fMemoryBlockEndAddress = fParent.getMemoryBlockEndAddress();

		return fMemoryBlockEndAddress;
	}

	public int getRadix() {
		return fRadix;
	}

	protected int getNumericRadix(int radix) {
		switch (radix) {
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

	public void setRadix(int mode) {
		if (fRadix == mode)
			return;

		fRadix = mode;
		fireSettingsChanged();
		layoutPanes();
	}

	public void setTextMode(int mode) {
		fTextMode = mode;

		fireSettingsChanged();
		layoutPanes();
	}

	public int getTextMode() {
		return fTextMode;
	}

	public int getUpdateMode() {
		return fUpdateMode;
	}

	public void setUpdateMode(int fUpdateMode) {
		this.fUpdateMode = fUpdateMode;
	}

	protected String getCharacterSet(int mode) {
		switch (mode) {
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

	public int getBytesPerCharacter() {
		if (fTextMode == Rendering.TEXT_UTF16)
			return 2;

		return 1;
	}

	public boolean isTargetLittleEndian() {
		return fIsTargetLittleEndian;
	}

	public void setTargetLittleEndian(boolean littleEndian) {
		if (fIsTargetLittleEndian == littleEndian)
			return;

		fParent.setTargetMemoryLittleEndian(littleEndian);
		fIsTargetLittleEndian = littleEndian;
		Display.getDefault().asyncExec(() -> {
			fireSettingsChanged();
			layoutPanes();
		});
	}

	public boolean isDisplayLittleEndian() {
		return fIsDisplayLittleEndian;
	}

	public void setDisplayLittleEndian(boolean littleEndian) {
		if (fIsDisplayLittleEndian == littleEndian)
			return;

		fIsDisplayLittleEndian = littleEndian;

		fireSettingsChanged();
		Display.getDefault().asyncExec(() -> {
			if (isShowCrossReferenceInfo()) {
				resolveAddressInfoForCurrentSelection();
			}
			layoutPanes();
		});
	}

	public void setBytesPerColumn(int byteCount) {
		if (fBytesPerColumn != byteCount) {
			fBytesPerColumn = byteCount;
			fireSettingsChanged();
			layoutPanes();
		}
	}

	protected void redrawPane(int paneId) {
		if (!isDisposed() && this.isVisible()) {
			AbstractPane pane = null;
			if (paneId == Rendering.PANE_ADDRESS) {
				pane = fAddressPane;
			} else if (paneId == Rendering.PANE_BINARY) {
				pane = fBinaryPane;
			}
			if (paneId == Rendering.PANE_TEXT) {
				pane = fTextPane;
			}
			if (pane != null && pane.isPaneVisible()) {
				pane.redraw();
				pane.setRowCount();
				if (pane.isFocusControl())
					pane.updateCaret();
			}

		}

		fParent.updateRenderingLabels();
	}

	protected void redrawPanes() {
		if (!isDisposed() && this.isVisible()) {
			if (fAddressPane.isPaneVisible()) {
				fAddressPane.redraw();
				fAddressPane.setRowCount();
				if (fAddressPane.isFocusControl())
					fAddressPane.updateCaret();
			}

			if (fBinaryPane.isPaneVisible()) {
				fBinaryPane.redraw();
				fBinaryPane.setRowCount();
				if (fBinaryPane.isFocusControl())
					fBinaryPane.updateCaret();
			}

			if (fTextPane.isPaneVisible()) {
				fTextPane.redraw();
				fTextPane.setRowCount();
				if (fTextPane.isFocusControl())
					fTextPane.updateCaret();
			}
		}

		fParent.updateRenderingLabels();
	}

	private void layoutPanes() {
		packColumns();
		layout(true);

		redraw();
		redrawPanes();
	}

	private void fireSettingsChanged() {
		fAddressPane.settingsChanged();
		fBinaryPane.settingsChanged();
		fTextPane.settingsChanged();
	}

	protected void copyAddressToClipboard() {
		Clipboard clip = null;
		try {
			clip = new Clipboard(getDisplay());

			String addressString = "0x" + getCaretAddress().toString(16);

			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			clip.setContents(new Object[] { addressString }, new Transfer[] { plainTextTransfer });
		} finally {
			if (clip != null) {
				clip.dispose();
			}
		}
	}

	static final char[] hexdigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public String getRadixText(MemoryByte bytes[], int radix, boolean isLittleEndian) {
		boolean readableByte = false;
		boolean allBytesReadable = true;
		for (int i = 0; i < bytes.length; i++) {
			if (!bytes[i].isReadable()) {
				allBytesReadable = false;
			} else {
				readableByte = true;
			}
		}

		// convert byte to character if all bytes are readable or
		// it is a mixed of readable&non-readable bytes and format is Hex or Binary. Bugzilla 342239
		if (allBytesReadable || readableByte && (radix == Rendering.RADIX_HEX || radix == Rendering.RADIX_BINARY)) {
			// bytes from the cache are stored as a sequential byte sequence regardless of target endian.
			// the endian attribute tells us the recommended endian for display. the user may change this
			// from the default. if the endian is little, we must swap the byte order.
			boolean needsSwap = false;
			if (isDisplayLittleEndian())
				needsSwap = true;

			switch (radix) {
			case Rendering.RADIX_HEX:
			case Rendering.RADIX_OCTAL:
			case Rendering.RADIX_BINARY: {
				long value = 0;
				if (needsSwap) {
					for (int i = 0; i < bytes.length; i++) {
						value = value << 8;
						value = value | (bytes[bytes.length - 1 - i].getValue() & 0xFF);
					}
				} else {
					for (int i = 0; i < bytes.length; i++) {
						value = value << 8;
						value = value | (bytes[i].getValue() & 0xFF);
					}
				}

				char buf[] = new char[getRadixCharacterCount(radix, bytes.length)];

				switch (radix) {
				case Rendering.RADIX_BINARY: {
					for (int i = buf.length - 1; i >= 0; i--) {
						int byteIndex = needsSwap ? bytes.length - 1 - i / 8 : i / 8;
						if (bytes[byteIndex].isReadable()) {
							buf[i] = hexdigits[(int) (value & 1)];
						} else {
							buf[i] = getPaddingCharacter();
						}
						value = value >>> 1;
					}
					break;
				}
				case Rendering.RADIX_OCTAL: {
					for (int i = buf.length - 1; i >= 0; i--) {
						buf[i] = hexdigits[(int) (value & 7)];
						value = value >>> 3;
					}
					break;
				}
				case Rendering.RADIX_HEX: {
					for (int i = buf.length - 1; i >= 0; i--) {
						int byteIndex = needsSwap ? bytes.length - 1 - i / 2 : i / 2;
						if (bytes[byteIndex].isReadable()) {
							buf[i] = hexdigits[(int) (value & 15)];
						} else {
							buf[i] = getPaddingCharacter();
						}
						value = value >>> 4;
					}
					break;
				}
				}

				return new String(buf);
			}
			case Rendering.RADIX_DECIMAL_UNSIGNED:
			case Rendering.RADIX_DECIMAL_SIGNED: {
				boolean isSignedType = radix == Rendering.RADIX_DECIMAL_SIGNED ? true : false;

				int textWidth = getRadixCharacterCount(radix, bytes.length);

				char buf[] = new char[textWidth];

				byte[] value = new byte[bytes.length + 1];

				if (needsSwap) {
					for (int i = 0; i < bytes.length; i++) {
						value[bytes.length - i] = bytes[i].getValue();
					}
				} else {
					for (int i = 0; i < bytes.length; i++) {
						value[i + 1] = bytes[i].getValue();
					}
				}

				BigInteger bigValue;
				boolean isNegative = false;
				if (isSignedType && (value[1] & 0x80) != 0) {
					value[0] = -1;
					isNegative = true;
					bigValue = new BigInteger(value).abs();
				} else {
					value[0] = 0;
					bigValue = new BigInteger(value);
				}

				for (int i = 0; i < textWidth; i++) {
					BigInteger divideRemainder[] = bigValue.divideAndRemainder(BigInteger.valueOf(10));
					int remainder = divideRemainder[1].intValue();
					buf[textWidth - 1 - i] = hexdigits[remainder % 10];
					bigValue = divideRemainder[0];
				}

				if (isSignedType) {
					buf[0] = isNegative ? '-' : ' ';
				}

				return new String(buf);
			}
			}
		}

		StringBuilder errorText = new StringBuilder();
		for (int i = getRadixCharacterCount(radix, bytes.length); i > 0; i--)
			errorText.append(getPaddingCharacter());

		return errorText.toString();
	}

	public int getRadixCharacterCount(int radix, int bytes) {
		switch (radix) {
		case Rendering.RADIX_HEX:
			return bytes * 2;
		case Rendering.RADIX_BINARY:
			return bytes * 8;
		case Rendering.RADIX_OCTAL: {
			switch (bytes) {
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
		case Rendering.RADIX_DECIMAL_UNSIGNED: {
			switch (bytes) {
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
		case Rendering.RADIX_DECIMAL_SIGNED: {
			switch (bytes) {
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

	protected static boolean isValidEditCharacter(char character) {
		return (character >= '0' && character <= '9') || (character >= 'a' && character <= 'z')
				|| (character >= 'A' && character <= 'Z') || character == '-' || character == ' ';
	}

	public String formatText(MemoryByte[] memoryBytes, boolean isLittleEndian, int textMode) {
		// check memory byte for unreadable bytes
		boolean readable = true;
		for (int i = 0; i < memoryBytes.length; i++)
			if (!memoryBytes[i].isReadable())
				readable = false;

		// if any bytes are not readable, return ?'s
		if (!readable) {
			StringBuilder errorText = new StringBuilder();
			for (int i = memoryBytes.length; i > 0; i--)
				errorText.append(getPaddingCharacter());
			return errorText.toString();
		}

		// TODO
		// does endian mean anything for text? ah, unicode?

		// create byte array from MemoryByte array
		byte bytes[] = new byte[memoryBytes.length];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = memoryBytes[i].getValue();
		}

		// replace invalid characters with '.'
		// maybe there is a way to query the character set for
		// valid characters?

		// replace invalid US-ASCII with '.'
		if (textMode == Rendering.TEXT_USASCII) {
			for (int i = 0; i < bytes.length; i++) {
				int byteValue = bytes[i];
				if (byteValue < 0)
					byteValue += 256;

				if (byteValue < 0x20 || byteValue > 0x7e)
					bytes[i] = '.';
			}
		}

		// replace invalid ISO-8859-1 with '.'
		if (textMode == Rendering.TEXT_ISO_8859_1) {
			for (int i = 0; i < bytes.length; i++) {
				int byteValue = bytes[i];
				if (byteValue < 0)
					byteValue += 256;

				if (byteValue < 0x20 || (byteValue >= 0x7f && byteValue < 0x9f))
					bytes[i] = '.';
			}
		}

		try {
			// convert bytes to string using desired character set
			StringBuilder buf = new StringBuilder(new String(bytes, this.getCharacterSet(textMode)));

			// pad string to (byte count - string length) with spaces
			for (int i = 0; i < memoryBytes.length - buf.length(); i++)
				buf.append(' ');
			return buf.toString();
		} catch (Exception e) {
			// return ?s the length of byte count
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < memoryBytes.length - buf.length(); i++)
				buf.append(getPaddingCharacter());
			return buf.toString();
		}
	}

	public AbstractPane incrPane(AbstractPane currentPane, int offset) {
		final List<AbstractPane> panes = Arrays.asList(getRenderingPanes());
		return panes.get((panes.indexOf(currentPane) + offset) % panes.size());
	}

	/**
	 * Indicates if additional address information is available to display in the current visible range
	 */
	boolean hasVisibleRangeInfo() {
		return false;
	}

	/**
	 * @return True if the given address has additional information to display e.g. variables, registers, etc.
	 */
	boolean hasAddressInfo(BigInteger address) {
		return false;
	}

	/**
	 * @return The items that would be visible in the current viewable area if the rows were to use a single
	 *         height
	 */
	Map<BigInteger, List<IMemoryBlockAddressInfoItem>> getVisibleValueToAddressInfoItems() {
		return fMapStartAddrToInfoItems;
	}

	/**
	 * @since 1.5
	 */
	protected boolean isShowCrossReferenceInfo() {
		// Check settings in preference store
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
		boolean prefShowInfo = store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_CROSS_REFERENCE_INFO);

		// Cross Reference information can not be properly highlighted for a Little Endian display
		// see Bug 509577 - [Traditional Rendering] Local variable enclosing markings may be wrong in Little Endian presentation
		return (!isDisplayLittleEndian() && prefShowInfo);
	}

	/**
	 * Provides a string with the information relevant to a given address, the separator helps to format it
	 * e.g. Separated items by comma, new line, etc.
	 *
	 * @param addTypeHeaders
	 *            Indicates if the string shall include a data type name before each list of items of the same
	 *            type e.g. Variables, Registers, etc.
	 */
	String buildAddressInfoString(BigInteger address, String separator, boolean addTypeHeaders) {
		return "";
	}

	/**
	 * Returns Dynamic context menu actions to this render, No actions by default
	 * @since 1.4
	 */
	Action[] getDynamicActions() {
		return new Action[0];
	}

	/**
	 * Trigger the resolution of additional address information - No action by default
	 */
	void resolveAddressInfoForCurrentSelection() {
	}

}
