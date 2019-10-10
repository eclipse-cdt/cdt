/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems Inc. and others.
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
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.eclipse.cdt.debug.core.model.provisional.IMemoryRenderingViewportProvider;
import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.Endian;
import org.eclipse.cdt.debug.ui.memory.floatingpoint.FPutilities.FPDataType;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.memory.IMemoryBlockConnection;
import org.eclipse.debug.internal.ui.memory.provisional.MemoryViewPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractMemoryRendering;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IRepositionableMemoryRendering;
import org.eclipse.debug.ui.memory.IResettableMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.UIJob;

/**
 * A memory rendering displaying memory in a floating point memory view look and
 * feel, optimized for minimal IO traffic.
 * <p>
 * requirements of the debug model implementation: - An IMemoryBlockExtension is
 * required.
 *
 * Since it is not possible to size the memory block to match the size of the
 * viewport, memory block change notification is not useful. Such events are
 * ignored by this rendering.
 */

@SuppressWarnings({ "restriction" })
public class FPRendering extends AbstractMemoryRendering implements IRepositionableMemoryRendering,
		IResettableMemoryRendering, IMemoryRenderingViewportProvider, IModelChangedListener {
	protected Rendering fRendering;
	protected Action actionDisplayBigEndian;
	protected Action actionDisplayLittleEndian;
	protected Action actionDisplayFloatingPoint;

	private IWorkbenchAdapter fWorkbenchAdapter;
	private IMemoryBlockConnection fConnection;

	private final static int MAX_MENU_COLUMN_COUNT = 8;

	Action actionFloatingPoint32 = null;
	Action actionFloatingPoint64 = null;

	Action actionDisplay4Digits = null;
	Action actionDisplay8Digits = null;
	Action actionDisplay16Digits = null;

	// Constructor

	public FPRendering(String id) {
		super(id);

		JFaceResources.getFontRegistry().addListener(event -> {
			if (event.getProperty().equals(IInternalDebugUIConstants.FONT_NAME)) {
				FPRendering.this.fRendering
						.handleFontPreferenceChange(JFaceResources.getFont(IInternalDebugUIConstants.FONT_NAME));
			}
		});

		this.addPropertyChangeListener(event -> {
			IMemoryRendering sourceRendering = (IMemoryRendering) event.getSource();
			if (!sourceRendering.getMemoryBlock().equals(getMemoryBlock()))
				return;

			Object address = event.getNewValue();

			if (event.getProperty().equals(AbstractTableRendering.PROPERTY_SELECTED_ADDRESS)
					&& address instanceof BigInteger) {
				FPRendering.this.fRendering.ensureVisible((BigInteger) address);
			}
		});

		FPRenderingPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
			disposeColors();
			allocateColors();
			applyPreferences();
		});

		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
			if (event.getProperty().equals(IDebugUIConstants.PREF_PADDED_STR)) {
				if (FPRendering.this.fRendering != null) {
					setRenderingPadding((String) event.getNewValue());
					FPRendering.this.fRendering.redrawPanes();
				}
			}
		});
	}

	static int paddingCounter = 0;

	void setRenderingPadding(String padding) {
		if (padding == null || padding.length() == 0)
			padding = "  "; //$NON-NLS-1$
		FPRendering.this.fRendering.setPaddingString(padding);
	}

	protected void logError(String message, Exception e) {
		Status status = new Status(IStatus.ERROR, getRenderingId(), DebugException.INTERNAL_ERROR, message, e);
		FPRenderingPlugin.getDefault().getLog().log(status);
	}

	BigInteger fBigBaseAddress; // Memory base address
	private BigInteger fStartAddress; // Starting address
	private BigInteger fEndAddress; // Ending address
	private int fAddressableSize; // Memory block size
	private int fAddressSize; // Size of address

	/*
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta, org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy)
	 */
	@Override
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
		/*
		 * The event model in the traditional renderer is written to expect a suspend first
		 * which will cause it to save its current  data set away in an archive.  Then when
		 * the state change comes through it will compare and refresh showing a difference.
		 */
		int flags = delta.getFlags();
		if ((flags & IModelDelta.STATE) != 0) {
			fRendering.handleSuspend(false);
		}

		fRendering.handleChange();
	}

	/*
	 * We use the model proxy which is supplied by the TCF implementation to provide the knowledge of memory
	 * change notifications. The older backends ( the reference model, Wind River Systems Inc. ) are written
	 * to generate the Debug Model events. TCF follows the "ModelDelta/IModelProxy" implementation  that the
	 * platform renderers use. So this implementation acts as a shim. If the older Debug Events come in then
	 * fine. If the newer model deltas come in fine also.
	 */
	IModelProxy fModel;

	@Override
	public void dispose() {
		/*
		 * We use the UI dispatch thread to protect the proxy information. Even though I believe the
		 * dispose routine is always called in the UI dispatch thread. I am going to make sure.
		 */
		Display.getDefault().asyncExec(() -> {
			if (fModel != null) {
				fModel.removeModelChangedListener(FPRendering.this);
				fModel.dispose();
			}
		});

		if (this.fRendering != null)
			this.fRendering.dispose();
		disposeColors();
		super.dispose();
	}

	@Override
	public void init(final IMemoryRenderingContainer container, final IMemoryBlock block) {
		super.init(container, block);

		/*
		 * Working with the model proxy must be done on the UI dispatch thread.
		 */
		final IModelProxyFactory factory = (IModelProxyFactory) DebugPlugin.getAdapter(block, IModelProxyFactory.class);
		if (factory != null) {
			Display.getDefault().asyncExec(() -> {

				/*
				 * The asynchronous model assumes we have an asynchronous viewer that has an IPresentationContext
				 * to represent it. The Platform memory subsystem provides a way to create one without a viewewr.
				 */
				IMemoryRenderingSite site = container.getMemoryRenderingSite();
				MemoryViewPresentationContext context = new MemoryViewPresentationContext(site, container,
						FPRendering.this);

				/*
				 * Get a new proxy and perform the initialization sequence so we are known the
				 * the model provider.
				 */
				fModel = factory.createModelProxy(block, context);
				if (fModel != null) {
					fModel.installed(null);
					fModel.addModelChangedListener(FPRendering.this);
				}

			});
		}

		try {
			fBigBaseAddress = ((IMemoryBlockExtension) block).getBigBaseAddress();
		} catch (DebugException de) {
			logError(FPRenderingMessages.getString("FPRendering.FAILURE_RETRIEVE_BASE_ADDRESS"), de); //$NON-NLS-1$
		}

		try {
			fAddressableSize = ((IMemoryBlockExtension) block).getAddressableSize();
		} catch (DebugException de) {
			fAddressableSize = 1;
		}

		try {
			fStartAddress = ((IMemoryBlockExtension) block).getMemoryBlockStartAddress();
		} catch (DebugException de) {
			fStartAddress = null;
			logError(FPRenderingMessages.getString("FPRendering.FAILURE_RETRIEVE_START_ADDRESS"), de); //$NON-NLS-1$
		}

		try {
			fAddressSize = ((IMemoryBlockExtension) block).getAddressSize();
		} catch (DebugException e) {
			fAddressSize = 0;
		}

		BigInteger endAddress;
		try {
			endAddress = ((IMemoryBlockExtension) block).getMemoryBlockEndAddress();
			if (endAddress != null)
				fEndAddress = endAddress;
		} catch (DebugException e) {
			fEndAddress = null;
		}

		if (fEndAddress == null) {
			int addressSize;
			try {
				addressSize = ((IMemoryBlockExtension) block).getAddressSize();
			} catch (DebugException e) {
				addressSize = 4;
			}

			endAddress = BigInteger.valueOf(2);
			endAddress = endAddress.pow(addressSize * 8);
			endAddress = endAddress.subtract(BigInteger.ONE);
			fEndAddress = endAddress;
		}

		// default to MAX_VALUE if we have trouble getting the end address
		if (fEndAddress == null)
			fEndAddress = BigInteger.valueOf(Integer.MAX_VALUE);
	}

	public BigInteger getBigBaseAddress() {
		return fBigBaseAddress;
	}

	public BigInteger getMemoryBlockStartAddress() {
		return fStartAddress;
	}

	public BigInteger getMemoryBlockEndAddress() {
		return fEndAddress;
	}

	public int getAddressableSize() {
		return fAddressableSize;
	}

	public int getAddressSize() {
		return fAddressSize;
	}

	@Override
	public Control createControl(Composite parent) {
		allocateColors();

		this.fRendering = new Rendering(parent, this);

		applyPreferences();

		createMenus();

		if (actionFloatingPoint32.isChecked())
			actionDisplay16Digits.setEnabled(false);
		else
			actionDisplay16Digits.setEnabled(true);

		return this.fRendering;
	}

	/*
	* We are duplicating the reference to the GoToAddress command because it is private in the platform.
	* This is not going to change at this point so just live with it.
	*/
	private static final String ID_GO_TO_ADDRESS_COMMAND = "org.eclipse.debug.ui.command.gotoaddress"; //$NON-NLS-1$
	private AbstractHandler fGoToAddressHandler;

	@Override
	public void activated() {
		super.activated();

		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandSupport = workbench.getAdapter(ICommandService.class);

		if (commandSupport != null) {
			Command gotoCommand = commandSupport.getCommand(ID_GO_TO_ADDRESS_COMMAND);

			if (fGoToAddressHandler == null) {
				fGoToAddressHandler = new AbstractHandler() {
					@Override
					public Object execute(ExecutionEvent event) throws ExecutionException {
						return null;
					}
				};
			}
			gotoCommand.setHandler(fGoToAddressHandler);
		}
	}

	@Override
	public void deactivated() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandSupport = workbench.getAdapter(ICommandService.class);

		if (commandSupport != null) {
			// remove handler
			Command command = commandSupport.getCommand(ID_GO_TO_ADDRESS_COMMAND);
			command.setHandler(null);
		}

		super.deactivated();
	}

	public void setSelection(BigInteger start, BigInteger end) {
		fRendering.getSelection().setStart(start, start);
		fRendering.getSelection().setEnd(end, end);
	}

	public void gotoAddress(final BigInteger address) {
		this.fRendering.gotoAddress(address);
	}

	public void updateRenderingLabels() {
		UIJob job = new UIJob("updateLabels") { //$NON-NLS-1$
			@SuppressWarnings("synthetic-access")
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				// Update tab labels
				String fLabel = getLabel();
				firePropertyChangedEvent(
						new PropertyChangeEvent(FPRendering.this, IBasicPropertyConstants.P_TEXT, null, fLabel));
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private Color colorBackground;
	private Color colorChanged;
	private Color colorsChanged[] = null;
	private Color colorEdit;
	private Color colorSelection;
	private Color colorText;
	private Color colorTextAlternate;

	public void allocateColors() {
		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

		colorBackground = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, FPRenderingPreferenceConstants.MEM_COLOR_BACKGROUND));
		colorChanged = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, FPRenderingPreferenceConstants.MEM_COLOR_CHANGED));
		colorEdit = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, FPRenderingPreferenceConstants.MEM_COLOR_EDIT));
		colorSelection = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, FPRenderingPreferenceConstants.MEM_COLOR_SELECTION));
		colorText = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, FPRenderingPreferenceConstants.MEM_COLOR_TEXT));

		// alternate cell color
		Color textColor = getColorText();
		int red = textColor.getRed();
		int green = textColor.getGreen();
		int blue = textColor.getBlue();

		float scale = store.getInt(FPRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS);

		red = (int) Math.min(red + ((255 - red) / 10) * scale, 255);
		green = (int) Math.min(green + ((255 - green) / 10) * scale, 255);
		blue = (int) Math.min(blue + ((255 - blue) / 10) * scale, 255);

		colorTextAlternate = new Color(Display.getDefault(), new RGB(red, green, blue));
	}

	public void disposeColors() {
		if (colorBackground != null)
			colorBackground.dispose();
		colorBackground = null;

		if (colorChanged != null)
			colorChanged.dispose();
		colorChanged = null;

		if (colorEdit != null)
			colorEdit.dispose();
		colorEdit = null;

		if (colorSelection != null)
			colorSelection.dispose();
		colorSelection = null;

		if (colorText != null)
			colorText.dispose();
		colorText = null;

		if (colorTextAlternate != null)
			colorTextAlternate.dispose();
		colorTextAlternate = null;

		disposeChangedColors();
	}

	public void applyPreferences() {
		if (fRendering != null && !fRendering.isDisposed()) {
			IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();
			fRendering.setHistoryDepth(store.getInt(FPRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT));
			fRendering.setBackground(getColorBackground());

			FPAbstractPane panes[] = fRendering.getRenderingPanes();
			for (int index = 0; index < panes.length; index++)
				panes[index].setBackground(getColorBackground());

			setRenderingPadding(
					FPRenderingPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_PADDED_STR));

			fRendering.redrawPanes();
		}
	}

	public Color getColorBackground() {
		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

		if (store.getBoolean(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND))
			return Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		return colorBackground;
	}

	public Color getColorChanged() {
		return colorChanged;
	}

	private void disposeChangedColors() {
		if (colorsChanged != null)
			for (int index = 0; index < colorsChanged.length; index++)
				colorsChanged[index].dispose();
		colorsChanged = null;
	}

	public Color[] getColorsChanged() {
		if (colorsChanged != null && colorsChanged.length != fRendering.getHistoryDepth()) {
			disposeChangedColors();
		}

		if (colorsChanged == null) {
			colorsChanged = new Color[fRendering.getHistoryDepth()];
			colorsChanged[0] = colorChanged;
			int shades = fRendering.getHistoryDepth() + 4;
			int red = (255 - colorChanged.getRed()) / shades;
			int green = (255 - colorChanged.getGreen()) / shades;
			int blue = (255 - colorChanged.getBlue()) / shades;
			for (int index = 1; index < fRendering.getHistoryDepth(); index++) {
				colorsChanged[index] = new Color(colorChanged.getDevice(),
						colorChanged.getRed() + ((shades - index) * red),
						colorChanged.getGreen() + ((shades - index) * green),
						colorChanged.getBlue() + ((shades - index) * blue));
			}
		}

		return colorsChanged;
	}

	public Color getColorEdit() {
		return colorEdit;
	}

	public Color getColorSelection() {
		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

		if (store.getBoolean(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION))
			return Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);

		return colorSelection;
	}

	public Color getColorText() {
		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

		if (store.getBoolean(FPRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT))
			return Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);

		return colorText;
	}

	public Color getColorTextAlternate() {
		return colorTextAlternate;
	}

	// Menu creation

	public void createMenus() {
		// Add the menu to each of the rendering panes

		Control[] renderingControls = this.fRendering.getRenderingPanes();

		for (int index = 0; index < renderingControls.length; index++)
			super.createPopupMenu(renderingControls[index]);

		super.createPopupMenu(this.fRendering);

		// Copy

		final Action copyAction = new CopyAction(this.fRendering);

		// Copy address

		final Action copyAddressAction = new Action(FPRenderingMessages.getString("FPRendering.COPY_ADDRESS")) //$NON-NLS-1$
		{
			@Override
			public void run() {
				Display.getDefault().asyncExec(() -> FPRendering.this.fRendering.copyAddressToClipboard());
			}
		};

		// Reset to base address

		final Action gotoBaseAddressAction = new Action(
				FPRenderingMessages.getString("FPRendering.RESET_TO_BASE_ADDRESS")) //$NON-NLS-1$
		{
			@Override
			public void run() {
				Display.getDefault().asyncExec(
						() -> FPRendering.this.fRendering.gotoAddress(FPRendering.this.fRendering.fBaseAddress));
			}
		};

		// Refresh

		final Action refreshAction = new Action(FPRenderingMessages.getString("FPRendering.REFRESH")) //$NON-NLS-1$
		{
			@Override
			public void run() {
				Display.getDefault().asyncExec(() -> {
					// For compatibility with DSF update modes (hopefully this will either be replaced
					// by an enhanced platform interface or the caching will move out of the data layer)

					try {
						Method m = fRendering.getMemoryBlock().getClass().getMethod("clearCache", new Class[0]); //$NON-NLS-1$
						if (m != null)
							m.invoke(fRendering.getMemoryBlock(), new Object[0]);
					} catch (Exception e) {
					}

					FPRendering.this.fRendering.refresh();
				});
			}
		};

		// Little Endian

		actionDisplayLittleEndian = new Action(FPRenderingMessages.getString("FPRendering.ENDIAN_LITTLE"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				fRendering.setDisplayLittleEndian(true);
				setRMCvalue(IFPRConstants.ENDIAN_KEY, Endian.LITTLE.getValue());
			}
		};

		// Big Endian

		actionDisplayBigEndian = new Action(FPRenderingMessages.getString("FPRendering.ENDIAN_BIG"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				fRendering.setDisplayLittleEndian(false);
				setRMCvalue(IFPRConstants.ENDIAN_KEY, Endian.BIG.getValue());
			}
		};

		// Endian settings

		int endian = getRMCvalue(IFPRConstants.ENDIAN_KEY);

		endian = endian != -1 ? endian
				: (fRendering.isDisplayLittleEndian() ? Endian.LITTLE.getValue() : Endian.BIG.getValue());
		boolean le = (endian == Endian.LITTLE.getValue());

		fRendering.setDisplayLittleEndian(le);
		actionDisplayLittleEndian.setChecked(le);
		actionDisplayBigEndian.setChecked(!le);

		// Float

		boolean dtFloat = getRMCvalue(IFPRConstants.DATATYPE_KEY) == FPDataType.FLOAT.getValue();
		this.fRendering.setFPDataType(dtFloat ? FPDataType.FLOAT : FPDataType.DOUBLE);

		actionFloatingPoint32 = new Action(FPRenderingMessages.getString("FPRendering.FLOATING_POINT_32"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				fRendering.setDisplayedPrecision(getRMCvalue(IFPRConstants.FLOAT_DISP_KEY));
				fRendering.setFPDataType(FPDataType.FLOAT);
				setRMCvalue(IFPRConstants.DATATYPE_KEY, FPDataType.FLOAT.getValue());
				setSelections();
			}
		};

		// Double

		actionFloatingPoint64 = new Action(FPRenderingMessages.getString("FPRendering.FLOATING_POINT_64"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				fRendering.setDisplayedPrecision(getRMCvalue(IFPRConstants.DOUBLE_DISP_KEY));
				fRendering.setFPDataType(FPDataType.DOUBLE);
				setRMCvalue(IFPRConstants.DATATYPE_KEY, FPDataType.DOUBLE.getValue());
				setSelections();
			}
		};

		// Displayed precision: 4 digits

		int savedPrecision = getDisplayedPrecision();

		actionDisplay4Digits = new Action(FPRenderingMessages.getString("FPRendering.DISPLAYED_PRECISION_4"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				FPRendering.this.fRendering.setDisplayedPrecision(4);
				setDisplayedPrecision(4);
			}
		};

		if (savedPrecision == 4) {
			FPRendering.this.fRendering.setDisplayedPrecision(4);
			actionDisplay4Digits.setChecked(savedPrecision == 4);
		}

		// Displayed precision: 8 digits

		actionDisplay8Digits = new Action(FPRenderingMessages.getString("FPRendering.DISPLAYED_PRECISION_8"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				FPRendering.this.fRendering.setDisplayedPrecision(8);
				setDisplayedPrecision(8);
			}
		};

		if (savedPrecision == 8) {
			FPRendering.this.fRendering.setDisplayedPrecision(8);
			actionDisplay8Digits.setChecked(savedPrecision == 8);
		}

		// Displayed precision: 16 digits (doubles only)

		actionDisplay16Digits = new Action(FPRenderingMessages.getString("FPRendering.DISPLAYED_PRECISION_16"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				FPRendering.this.fRendering.setDisplayedPrecision(16);
				setDisplayedPrecision(16);
			}
		};

		if (savedPrecision == 16) {
			FPRendering.this.fRendering.setDisplayedPrecision(16);
			actionDisplay16Digits.setChecked(savedPrecision == 16);
		}

		// Set RMC selections based on datatype and displayed precision settings in effect

		setSelections();

		// Columns

		int savedColumnCount = getRMCvalue(IFPRConstants.COLUMN_COUNT_KEY);

		final Action displayColumnCountAuto = new Action(FPRenderingMessages.getString("FPRendering.COLUMN_COUNT_AUTO"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				FPRendering.this.fRendering.setColumnsSetting(Rendering.COLUMNS_AUTO_SIZE_TO_FIT);
				setRMCvalue(IFPRConstants.COLUMN_COUNT_KEY, Rendering.COLUMNS_AUTO_SIZE_TO_FIT);
			}
		};

		boolean autoMode = savedColumnCount == Rendering.COLUMNS_AUTO_SIZE_TO_FIT;
		displayColumnCountAuto.setChecked(autoMode);

		final Action[] displayColumnCounts = new Action[MAX_MENU_COLUMN_COUNT];

		for (int index = 0, j = 1; index < MAX_MENU_COLUMN_COUNT; index++, j *= 2) {
			final int finali = j;
			displayColumnCounts[index] = new Action(FPRenderingMessages.getString("FPRendering.COLUMN_COUNT_" + finali), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					FPRendering.this.fRendering.setColumnsSetting(finali);
					setRMCvalue(IFPRConstants.COLUMN_COUNT_KEY, finali);
				}
			};
			displayColumnCounts[index].setChecked(fRendering.getColumnsSetting() == finali);
		}

		// Set/clear column count selections as appropriate

		int countValue = getRMCvalue(IFPRConstants.COLUMN_COUNT_KEY);

		for (int index = 0; index < MAX_MENU_COLUMN_COUNT; index++)
			displayColumnCounts[index]
					.setChecked(countValue != Rendering.COLUMNS_AUTO_SIZE_TO_FIT && (countValue == (1 << index)));

		fRendering.setColumnsSetting(getRMCvalue(IFPRConstants.COLUMN_COUNT_KEY));

		final Action displayColumnCountCustomValue = new Action("", IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
			}
		};

		final Action displayColumnCountCustom = new Action(
				FPRenderingMessages.getString("FPRendering.COLUMN_COUNT_CUSTOM"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
				InputDialog inputDialog = new InputDialog(fRendering.getShell(), "Set Column Count", //$NON-NLS-1$
						"Please enter column count", "", input -> {
							try {
								int index = Integer.parseInt(input);

								if (index <= 0)
									return "Please enter a positive integer"; //$NON-NLS-1$

								if (index > 200)
									return "Please enter a positive integer not greater than 200"; //$NON-NLS-1$
							} catch (NumberFormatException x) {
								return "Please enter a positive integer"; //$NON-NLS-1$
							}

							return null;
						});

				if (inputDialog.open() != Window.OK) {
					this.setChecked(false);
					int currentColumnSetting = FPRendering.this.fRendering.getColumnsSetting();

					if (currentColumnSetting == Rendering.COLUMNS_AUTO_SIZE_TO_FIT)
						displayColumnCountAuto.setChecked(true);
					else {
						boolean currentCountIsCustom = true;

						for (int index = 0, j = 1; index < MAX_MENU_COLUMN_COUNT
								&& currentCountIsCustom; index++, j *= 2) {
							currentCountIsCustom = (j != fRendering.getColumnsSetting());
							if (j == fRendering.getColumnsSetting())
								displayColumnCounts[index].setChecked(true);
						}

						if (currentCountIsCustom)
							displayColumnCountCustomValue.setChecked(true);
					}

					return;
				}

				int newColumnCount = -1;

				try {
					newColumnCount = Integer.parseInt(inputDialog.getValue());
				} catch (NumberFormatException x) {
					assert false;
				}

				boolean customIsOneOfStandardListChoices = false;

				for (int index = 0, j = 1; index < MAX_MENU_COLUMN_COUNT; index++, j *= 2) {
					if (newColumnCount == j) {
						customIsOneOfStandardListChoices = true;
						FPRendering.this.fRendering.setColumnsSetting(newColumnCount);
						setRMCvalue(IFPRConstants.COLUMN_COUNT_KEY, newColumnCount);
						this.setChecked(false);
						displayColumnCountCustomValue.setChecked(false);
						displayColumnCounts[index].setChecked(true);
						break;
					}
				}

				if (!customIsOneOfStandardListChoices) {
					FPRendering.this.fRendering.setColumnsSetting(newColumnCount);
					setRMCvalue(IFPRConstants.COLUMN_COUNT_KEY, newColumnCount);
					this.setChecked(false);

					displayColumnCountCustomValue.setChecked(true);
					displayColumnCountCustomValue.setText(Integer.valueOf(fRendering.getColumnsSetting()).toString());
				}
			}
		};

		// Check for a custom value:  If we're not in "Auto Fill" mode, check for standard column sizes.
		// If none of the standard sizes were entered, then it's a custom value; set it and display it.

		boolean customColumnCountSet = true;
		countValue = getRMCvalue(IFPRConstants.COLUMN_COUNT_KEY);

		if (countValue != Rendering.COLUMNS_AUTO_SIZE_TO_FIT) {
			for (int index = 0; index < MAX_MENU_COLUMN_COUNT; index++) {
				// If the column count is one of the standard values, set flag to false and exit the loop

				if (countValue == (1 << index)) {
					customColumnCountSet = false;
					break;
				}
			}

			if (customColumnCountSet) {
				FPRendering.this.fRendering.setColumnsSetting(countValue);
				displayColumnCountCustomValue.setChecked(true);
				displayColumnCountCustomValue.setText(Integer.valueOf(fRendering.getColumnsSetting()).toString());
			}
		}

		// Add the right-mouse-click (RMC) context menu items

		getPopupMenuManager().addMenuListener(manager -> {
			manager.add(new Separator());

			MenuManager sub = new MenuManager(FPRenderingMessages.getString("FPRendering.PANES")); //$NON-NLS-1$

			sub = new MenuManager(FPRenderingMessages.getString("FPRendering.ENDIAN")); //$NON-NLS-1$
			sub.add(actionDisplayBigEndian);
			sub.add(actionDisplayLittleEndian);
			manager.add(sub);

			sub = new MenuManager(FPRenderingMessages.getString("FPRendering.NUMBER_TYPE")); //$NON-NLS-1$
			sub.add(actionFloatingPoint32);
			sub.add(actionFloatingPoint64);
			manager.add(sub);

			sub = new MenuManager(FPRenderingMessages.getString("FPRendering.PRECISION")); //$NON-NLS-1$
			sub.add(actionDisplay4Digits);
			sub.add(actionDisplay8Digits);
			sub.add(actionDisplay16Digits);
			manager.add(sub);

			//              TODO: Add separator for FP group here: manager.add(new Separator());

			sub = new MenuManager(FPRenderingMessages.getString("FPRendering.COLUMN_COUNT")); //$NON-NLS-1$
			sub.add(displayColumnCountAuto);

			for (int index1 = 0; index1 < displayColumnCounts.length; index1++)
				sub.add(displayColumnCounts[index1]);

			boolean currentCountIsCustom = fRendering.getColumnsSetting() != 0;

			for (int index2 = 0, j = 1; index2 < MAX_MENU_COLUMN_COUNT && currentCountIsCustom; index2++, j *= 2)
				currentCountIsCustom = (j != fRendering.getColumnsSetting());

			if (currentCountIsCustom)
				sub.add(displayColumnCountCustomValue);

			sub.add(displayColumnCountCustom);
			manager.add(sub);

			// Update modes

			int updateMode = getRMCvalue(IFPRConstants.UPDATEMODE_KEY);

			final Action updateAlwaysAction = new Action(FPRenderingMessages.getString("FPRendering.UPDATE_ALWAYS"), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					fRendering.setUpdateMode(Rendering.UPDATE_ALWAYS);
					setRMCvalue(IFPRConstants.UPDATEMODE_KEY, Rendering.UPDATE_ALWAYS);
				}
			};
			updateAlwaysAction.setChecked(updateMode == Rendering.UPDATE_ALWAYS);

			final Action updateOnBreakpointAction = new Action(
					FPRenderingMessages.getString("FPRendering.UPDATE_ON_BREAKPOINT"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
			{
				@Override
				public void run() {
					fRendering.setUpdateMode(Rendering.UPDATE_ON_BREAKPOINT);
					setRMCvalue(IFPRConstants.UPDATEMODE_KEY, Rendering.UPDATE_ON_BREAKPOINT);
				}
			};
			updateOnBreakpointAction.setChecked(updateMode == Rendering.UPDATE_ON_BREAKPOINT);

			final Action updateManualAction = new Action(FPRenderingMessages.getString("FPRendering.UPDATE_MANUAL"), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					fRendering.setUpdateMode(Rendering.UPDATE_MANUAL);
					setRMCvalue(IFPRConstants.UPDATEMODE_KEY, Rendering.UPDATE_MANUAL);
				}
			};
			updateManualAction.setChecked(updateMode == Rendering.UPDATE_MANUAL);

			// Add menu

			sub = new MenuManager(FPRenderingMessages.getString("FPRendering.UPDATEMODE")); //$NON-NLS-1$
			sub.add(updateAlwaysAction);
			sub.add(updateOnBreakpointAction);
			sub.add(updateManualAction);
			manager.add(sub);
			manager.add(new Separator());

			BigInteger start = fRendering.getSelection().getStart();
			BigInteger end = fRendering.getSelection().getEnd();
			copyAction.setEnabled(start != null && end != null);

			manager.add(copyAction);
			manager.add(copyAddressAction);

			manager.add(gotoBaseAddressAction);
			manager.add(refreshAction);
			manager.add(new Separator());
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		});
	}

	// Set/clear selections as appropriate

	public void setSelections() {
		if (actionDisplay4Digits == null || actionDisplay8Digits == null || actionDisplay16Digits == null)
			return;

		// Enable/disable and set/clear menu RMC elements based on currently selected datatype

		boolean dtFloat = FPRendering.this.fRendering.getFPDataType() == FPDataType.FLOAT;

		actionDisplay16Digits.setEnabled(!dtFloat);
		actionFloatingPoint32.setChecked(dtFloat);
		actionFloatingPoint64.setChecked(!dtFloat);

		// Set/clear RMC elements based on displayed precision

		int displayedPrecision = getRMCvalue(dtFloat ? IFPRConstants.FLOAT_DISP_KEY : IFPRConstants.DOUBLE_DISP_KEY);

		actionDisplay4Digits.setChecked(displayedPrecision == 4);
		actionDisplay8Digits.setChecked(displayedPrecision == 8);
		actionDisplay16Digits.setChecked(displayedPrecision == 16);
	}

	@Override
	public Control getControl() {
		return this.fRendering;
	}

	// Selection is terminology for caret position

	@Override
	public BigInteger getSelectedAddress() {
		FPIMemorySelection selection = fRendering.getSelection();
		if (selection == null || selection.getStart() == null)
			return fRendering.getCaretAddress();

		return selection.getStartLow();
	}

	@Override
	public MemoryByte[] getSelectedAsBytes() {
		try {
			// default to the caret address and the cell count size
			BigInteger startAddr = fRendering.getCaretAddress();
			int byteCount = fRendering.getCharsPerColumn();

			// Now see if there's a selection
			FPIMemorySelection selection = fRendering.getSelection();
			if (selection != null && selection.getStart() != null) {
				// The implementation is such that just having a caret somewhere (without multiple cells
				// being selected) constitutes a selection, except for when the rendering is in its initial
				// state, i.e. just because we get here doesn't mean the user has selected more than one cell.

				startAddr = getSelectedAddress();

				if (selection.getHigh() != null)
					byteCount = selection.getHigh().subtract(selection.getLow()).intValue()
							* fRendering.getAddressableSize();
			}

			return fRendering.getViewportCache().getBytes(startAddr, byteCount);

		} catch (DebugException de) {
			return new MemoryByte[0];
		}
	}

	@Override
	public void goToAddress(final BigInteger address) throws DebugException {
		Display.getDefault().asyncExec(() -> fRendering.gotoAddress(address));
	}

	protected void setTargetMemoryLittleEndian(boolean littleEndian) {
		// Once we actually read memory we can determine the endianess and need to set these actions accordingly.
		actionDisplayBigEndian.setChecked(!littleEndian);
		actionDisplayLittleEndian.setChecked(littleEndian);

		// When target endian changes, force display endian to track.  User can then change display endian if desired.
		fRendering.setDisplayLittleEndian(littleEndian);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			if (this.fWorkbenchAdapter == null) {
				this.fWorkbenchAdapter = new IWorkbenchAdapter() {
					@Override
					public Object[] getChildren(Object o) {
						return new Object[0];
					}

					@Override
					public ImageDescriptor getImageDescriptor(Object object) {
						return null;
					}

					@Override
					public String getLabel(Object o) {
						return FPRenderingMessages.getString("FPRendering.RENDERING_NAME"); //$NON-NLS-1$
					}

					@Override
					public Object getParent(Object o) {
						return null;
					}
				};
			}
			return adapter.cast(this.fWorkbenchAdapter);
		}

		if (adapter == IMemoryBlockConnection.class) {
			if (fConnection == null) {
				fConnection = () -> {
					// update UI asynchronously
					Display display = FPRenderingPlugin.getDefault().getWorkbench().getDisplay();
					display.asyncExec(() -> {
						try {
							if (fBigBaseAddress != FPRendering.this.fRendering.getMemoryBlock().getBigBaseAddress()) {
								fBigBaseAddress = FPRendering.this.fRendering.getMemoryBlock().getBigBaseAddress();
								FPRendering.this.fRendering.gotoAddress(fBigBaseAddress);
							}
							FPRendering.this.fRendering.refresh();
						} catch (DebugException e) {
						}
					});
				};
			}

			return adapter.cast(fConnection);
		}

		return super.getAdapter(adapter);
	}

	@Override
	public void resetRendering() throws DebugException {
		fRendering.gotoAddress(fRendering.fBaseAddress);
	}

	@Override
	public BigInteger getViewportAddress() {
		return fRendering.getViewportStartAddress();
	}

	// Persistence methods

	void setDisplayedPrecision(int precision) {
		// Save the appropriate displayed precision value, based on data type, in the store

		if (FPRendering.this.fRendering.getFPDataType() == FPDataType.FLOAT)
			setRMCvalue(IFPRConstants.FLOAT_DISP_KEY, precision);
		else
			setRMCvalue(IFPRConstants.DOUBLE_DISP_KEY, precision);
	}

	private int getDisplayedPrecision() {
		// Retrieve the persisted data value from the store

		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();

		FPDataType dataType = FPRendering.this.fRendering.getFPDataType();

		if (store != null)
			return store.getInt(
					dataType == FPDataType.FLOAT ? IFPRConstants.FLOAT_DISP_KEY : IFPRConstants.DOUBLE_DISP_KEY);

		// If there's nothing persisted, return the default precision for data type

		return dataType == FPDataType.FLOAT ? FPDataType.FLOAT.getDisplayedPrecision()
				: FPDataType.DOUBLE.getDisplayedPrecision();
	}

	void setRMCvalue(String key, int value) {
		// Save the specified key and int value

		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();
		if (store != null)
			store.setValue(key, value);
	}

	int getRMCvalue(String key) {
		// Return the value for the specified key

		IPreferenceStore store = FPRenderingPlugin.getDefault().getPreferenceStore();
		return (store != null) ? store.getInt(key) : 0;
	}
}

// Copy class

class CopyAction extends Action {
	// TODO for the sake of large copies, this action should probably read in
	// blocks on a Job.

	private Rendering fRendering;
	private int fType = DND.CLIPBOARD;

	public CopyAction(Rendering rendering) {
		this(rendering, DND.CLIPBOARD);
	}

	@SuppressWarnings("restriction") // using platform's labels and images; acceptable build risk
	public CopyAction(Rendering rendering, int clipboardType) {
		super();
		fType = clipboardType;
		fRendering = rendering;
		setText(DebugUIMessages.CopyViewToClipboardAction_title);
		setToolTipText(DebugUIMessages.CopyViewToClipboardAction_tooltip);
		setImageDescriptor(
				DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_COPY_VIEW_TO_CLIPBOARD));
		setHoverImageDescriptor(
				DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_COPY_VIEW_TO_CLIPBOARD));
		setDisabledImageDescriptor(
				DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_COPY_VIEW_TO_CLIPBOARD));
	}

	@Override
	public void run() {
		Clipboard clip = null;
		final String PANE_SPACING = "  "; //$NON-NLS-1$

		try {
			clip = new Clipboard(fRendering.getDisplay());

			BigInteger start = fRendering.getSelection().getStart();
			BigInteger end = fRendering.getSelection().getEnd();

			if (end == null)
				return; // End will be null when there is nothing selected

			if (start.compareTo(end) > 0) {
				// Swap start and end

				BigInteger bigI = end;
				end = start;
				start = bigI;
			}

			final FPDataType numberType = fRendering.getFPDataType();
			final int bytesPerColumn = numberType.getByteLength();
			final boolean isLittleEndian = fRendering.isTargetLittleEndian();
			final int columns = fRendering.getColumnCount();
			BigInteger lengthToRead = end.subtract(start);

			int rows = lengthToRead.divide(BigInteger.valueOf(columns * (fRendering.getFPDataType().getByteLength())))
					.intValue();

			if (rows * columns * bytesPerColumn < lengthToRead.intValue())
				rows++;

			StringBuilder buffer = new StringBuilder();

			for (int row = 0; row < rows; row++) {
				BigInteger rowAddress = start.add(BigInteger.valueOf(row * columns * bytesPerColumn));

				if (fRendering.getPaneVisible(Rendering.PANE_ADDRESS)) {
					buffer.append(fRendering.getAddressString(rowAddress));
					buffer.append(PANE_SPACING);
				}

				if (fRendering.getPaneVisible(Rendering.PANE_DATA)) {
					for (int col = 0; col < columns; col++) {
						BigInteger cellAddress = rowAddress.add(BigInteger.valueOf(col * bytesPerColumn));

						if (cellAddress.compareTo(end) < 0) {
							try {
								FPMemoryByte bytes[] = fRendering.getBytes(cellAddress, bytesPerColumn);
								buffer.append(fRendering.sciNotationString(bytes, numberType, isLittleEndian));
							} catch (DebugException de) {
								fRendering.logError(FPRenderingMessages.getString("FPRendering.FAILURE_COPY_OPERATION"), //$NON-NLS-1$
										de);
								return;
							}
						} else {
							for (int i = fRendering.getCharsPerColumn(); i > 0; i--)
								buffer.append(' ');
						}

						if (col != columns - 1)
							buffer.append(' ');
					}
				}

				if (fRendering.getPaneVisible(Rendering.PANE_DATA))
					buffer.append(PANE_SPACING);

				buffer.append("\n"); //$NON-NLS-1$
			}

			if (buffer.length() > 0) {
				TextTransfer plainTextTransfer = TextTransfer.getInstance();
				clip.setContents(new Object[] { buffer.toString() }, new Transfer[] { plainTextTransfer }, fType);
			}
		} finally {
			if (clip != null) {
				clip.dispose();
			}
		}
	}
}
