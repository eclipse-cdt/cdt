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
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.traditional;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.provisional.IMemoryRenderingViewportProvider;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlock;
import org.eclipse.cdt.debug.core.model.provisional.IMemorySpaceAwareMemoryBlockRetrieval;
import org.eclipse.cdt.debug.internal.core.CRequest;
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
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.memory.IMemoryBlockConnection;
import org.eclipse.debug.internal.ui.memory.provisional.MemoryViewPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.views.memory.renderings.GoToAddressAction;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.UIJob;

/**
 * A memory rendering displaying memory in a traditional
 * memory view look and feel, optimized for minimal IO traffic.
 * <p>
 * requirements of the debug model implementation:
 *   - An IMemoryBlockExtension is required.
 *
 *  Since it is not possible to size the memory block to match
 *  the size of the viewport, memory block change notification
 *  is not useful. Such events are ignored by this rendering.
 */

@SuppressWarnings("restriction")
public class TraditionalRendering extends AbstractMemoryRendering implements IRepositionableMemoryRendering,
		IResettableMemoryRendering, IMemoryRenderingViewportProvider, IModelChangedListener {
	protected Rendering fRendering;
	protected Action displayEndianBigAction;
	protected Action displayEndianLittleAction;

	private IWorkbenchAdapter fWorkbenchAdapter;
	private IMemoryBlockConnection fConnection;

	private String fMemorySpaceId;

	private final static int MAX_MENU_COLUMN_COUNT = 8;

	private IMemorySpacePreferencesHelper fMemSpacePreferenceHelper;

	private class GetMemorySpacesRequest extends CRequest
			implements IMemorySpaceAwareMemoryBlockRetrieval.GetMemorySpacesRequest {
		String[] fMemorySpaces;

		@Override
		public String[] getMemorySpaces() {
			return fMemorySpaces;
		}

		@Override
		public void setMemorySpaces(String[] memorySpaceIds) {
			fMemorySpaces = memorySpaceIds;
		}
	}

	public TraditionalRendering(String id) {
		super(id);

		JFaceResources.getFontRegistry().addListener(event -> {
			if (event.getProperty().equals(IInternalDebugUIConstants.FONT_NAME)) {
				TraditionalRendering.this.fRendering
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
				TraditionalRendering.this.fRendering.ensureVisible((BigInteger) address);
			}
		});

		TraditionalRenderingPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
			disposeColors();
			allocateColors();
			applyPreferences();
		});

		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
			if (event.getProperty().equals(IDebugUIConstants.PREF_PADDED_STR)) {
				if (TraditionalRendering.this.fRendering != null) {
					setRenderingPadding((String) event.getNewValue());
					TraditionalRendering.this.fRendering.redrawPanes();
				}
			}
		});

	}

	private void setRenderingPadding(String padding) {
		if (padding == null || padding.length() == 0)
			padding = "?"; //$NON-NLS-1$
		TraditionalRendering.this.fRendering.setPaddingString(padding);
	}

	protected void logError(String message, Exception e) {
		Status status = new Status(IStatus.ERROR, getRenderingId(), DebugException.INTERNAL_ERROR, message, e);

		TraditionalRenderingPlugin.getDefault().getLog().log(status);
	}

	private BigInteger fBigBaseAddress;
	private BigInteger fStartAddress;
	private BigInteger fEndAddress;
	private int fAddressableSize;
	private int fAddressSize;

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
	private IModelProxy fModel;

	@Override
	public void dispose() {
		/*
		 * We use the UI dispatch thread to protect the proxy information. Even though I believe the
		 * dispose routine is always called in the UI dispatch thread. I am going to make sure.
		 */
		Display.getDefault().asyncExec(() -> {
			if (fModel != null) {
				fModel.removeModelChangedListener(TraditionalRendering.this);
				fModel.dispose();
			}
		});

		if (this.fRendering != null)
			this.fRendering.dispose();
		disposeColors();
		disposeFonts();
		super.dispose();
	}

	@Override
	public void init(final IMemoryRenderingContainer container, final IMemoryBlock block) {
		super.init(container, block);

		fMemSpacePreferenceHelper = TraditionalMemoryRenderingFactory.getMemorySpacesPreferencesHelper();

		// resolve memory space, if any
		if (block instanceof IMemorySpaceAwareMemoryBlock) {
			IMemorySpaceAwareMemoryBlock memBlock = (IMemorySpaceAwareMemoryBlock) block;
			String id = memBlock.getMemorySpaceID();
			fMemorySpaceId = id;
		}

		// extract memory space info, if applicable
		if (block instanceof IMemorySpaceAwareMemoryBlock) {
			IMemoryBlockRetrieval retrieval = ((IMemorySpaceAwareMemoryBlock) block).getMemoryBlockRetrieval();
			((IMemorySpaceAwareMemoryBlockRetrieval) retrieval).getMemorySpaces(block, new GetMemorySpacesRequest() {
				@Override
				public void done() {
					final String[] spaces = isSuccess() ? getMemorySpaces() : new String[0];
					// remember memory spaces
					Display.getDefault().asyncExec(() -> fMemSpacePreferenceHelper.updateMemorySpaces(spaces));
				}
			});
		}

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
						TraditionalRendering.this);

				/*
				 * Get a new proxy and perform the initialization sequence so we are known the
				 * the model provider.
				 */
				fModel = factory.createModelProxy(block, context);
				if (fModel != null) {
					fModel.installed(null);
					fModel.addModelChangedListener(TraditionalRendering.this);
				}

			});
		}

		try {
			fBigBaseAddress = ((IMemoryBlockExtension) block).getBigBaseAddress();
		} catch (DebugException de) {
			logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_RETRIEVE_BASE_ADDRESS"), de); //$NON-NLS-1$ // FIXME
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
			logError(TraditionalRenderingMessages.getString("TraditionalRendering.FAILURE_RETRIEVE_START_ADDRESS"), de); //$NON-NLS-1$
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
			endAddress = endAddress.subtract(BigInteger.valueOf(1));
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

		this.fRendering = new RenderingAddressInfo(parent, this);

		applyPreferences();

		createMenus();

		return this.fRendering;
	}

	// FIXME
	private static final String ID_GO_TO_ADDRESS_COMMAND = "org.eclipse.debug.ui.command.gotoaddress"; //$NON-NLS-1$
	private AbstractHandler fGoToAddressHandler;
	private IAction fSavedActionHandler; // When we set the global copy action handler we save off the existing so we can restore it.

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
						// TODO
						return null;
					}
				};
			}
			gotoCommand.setHandler(fGoToAddressHandler);
		}

		// Set the action handler for the global copy action
		final Action defaultAction = new CopyDefaultAction(this.fRendering, DND.CLIPBOARD);

		IWorkbenchPartSite site = getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
		if (site instanceof IViewSite) {
			IActionBars actionBars = ((IViewSite) site).getActionBars();
			if (actionBars != null) {
				fSavedActionHandler = actionBars.getGlobalActionHandler(ActionFactory.COPY.getId());
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), defaultAction);
			}
		}

	}

	@Override
	public void deactivated() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandSupport = workbench.getAdapter(ICommandService.class);

		if (commandSupport != null) {
			// 	remove handler
			Command command = commandSupport.getCommand(ID_GO_TO_ADDRESS_COMMAND);
			command.setHandler(null);
		}

		// Restore the saved action handler for the memory view.
		IWorkbenchPartSite site = getMemoryRenderingContainer().getMemoryRenderingSite().getSite();
		if (site instanceof IViewSite) {
			IActionBars actionBars = ((IViewSite) site).getActionBars();
			if (actionBars != null) {
				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), fSavedActionHandler);
			}
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
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				// update tab labels
				String fLabel = getLabel();
				firePropertyChangedEvent(new PropertyChangeEvent(TraditionalRendering.this,
						IBasicPropertyConstants.P_TEXT, null, fLabel));

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

	private Map<Integer, Font> fonts = new HashMap<>(3);

	public void allocateColors() {

		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
		colorBackground = null;
		// has a memory-space-specific background color been set for the associated memory space?
		if (fMemorySpaceId != null) {
			String key = fMemSpacePreferenceHelper.getMemorySpaceKey(fMemorySpaceId);
			if (store.getString(key) != "") {
				colorBackground = new Color(Display.getDefault(), PreferenceConverter.getColor(store, key));
			}
		}
		// no - then use default
		if (colorBackground == null) {
			colorBackground = new Color(Display.getDefault(),
					PreferenceConverter.getColor(store, TraditionalRenderingPreferenceConstants.MEM_COLOR_BACKGROUND));
		}
		colorChanged = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED));

		colorEdit = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT));

		colorSelection = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, TraditionalRenderingPreferenceConstants.MEM_COLOR_SELECTION));

		colorText = new Color(Display.getDefault(),
				PreferenceConverter.getColor(store, TraditionalRenderingPreferenceConstants.MEM_COLOR_TEXT));

		// alternate cell color
		Color textColor = getColorText();
		int red = textColor.getRed();
		int green = textColor.getGreen();
		int blue = textColor.getBlue();

		float scale = store.getInt(TraditionalRenderingPreferenceConstants.MEM_LIGHTEN_DARKEN_ALTERNATE_CELLS);

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

	public void disposeFonts() {
		for (Font font : fonts.values())
			font.dispose();
	}

	public void applyPreferences() {
		if (fRendering != null && !fRendering.isDisposed()) {
			IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();

			fRendering.setHistoryDepth(store.getInt(TraditionalRenderingPreferenceConstants.MEM_HISTORY_TRAILS_COUNT));

			fRendering.setBackground(getColorBackground());

			AbstractPane panes[] = fRendering.getRenderingPanes();
			for (int i = 0; i < panes.length; i++)
				panes[i].setBackground(getColorBackground());

			setRenderingPadding(TraditionalRenderingPlugin.getDefault().getPreferenceStore()
					.getString(IDebugUIConstants.PREF_PADDED_STR));
			if (store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_CROSS_REFERENCE_INFO)) {
				fRendering.resolveAddressInfoForCurrentSelection();
			}

			fRendering.redrawPanes();
		}
	}

	private Font makeFont(Font font, String boldKey, String italicKey) {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
		int style = SWT.NONE;
		if (store.getBoolean(boldKey))
			style |= SWT.BOLD;
		if (store.getBoolean(italicKey))
			style |= SWT.ITALIC;

		if (style == SWT.NONE)
			return font;

		Font modified = fonts.get(style);
		if (modified == null) {
			FontData fontData = font.getFontData()[0];
			modified = new Font(font.getDevice(), fontData.getName(), fontData.getHeight(),
					fontData.getStyle() | style);
			fonts.put(style, modified);
		}
		return modified;
	}

	public Font getFontChanged(Font font) {
		return makeFont(font, TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_BOLD,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_ITALIC);
	}

	public Font getFontEdit(Font font) {
		return makeFont(font, TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_BOLD,
				TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_ITALIC);
	}

	public boolean getBoxChanged() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_COLOR_CHANGED_BOX);
	}

	public boolean getBoxEdit() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_COLOR_EDIT_BOX);
	}

	public Color getColorBackground() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();

		if (store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_BACKGROUND))
			return Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		else
			return colorBackground;
	}

	public Color getColorChanged() {
		return colorChanged;
	}

	private void disposeChangedColors() {
		if (colorsChanged != null)
			for (int i = 0; i < colorsChanged.length; i++)
				colorsChanged[i].dispose();
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
			for (int i = 1; i < fRendering.getHistoryDepth(); i++) {
				colorsChanged[i] = new Color(colorChanged.getDevice(), colorChanged.getRed() + ((shades - i) * red),
						colorChanged.getGreen() + ((shades - i) * green),
						colorChanged.getBlue() + ((shades - i) * blue));
			}
		}

		return colorsChanged;
	}

	public Color getColorEdit() {
		return colorEdit;
	}

	public Color getColorSelection() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();

		if (store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_SELECTION))
			return Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION);
		else
			return colorSelection;
	}

	public Color getColorText() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();

		if (store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_USE_GLOBAL_TEXT))
			return Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		else
			return colorText;
	}

	public Color getColorTextAlternate() {
		return colorTextAlternate;
	}

	/**
	 * @since 1.4
	 * @deprecated - use: {@link Rendering#isShowCrossReferenceInfo()}
	 */
	@Deprecated
	public boolean isShowCrossRefInfoGlobalPref() {
		IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(TraditionalRenderingPreferenceConstants.MEM_CROSS_REFERENCE_INFO);
	}

	public void createMenus() {
		// add the menu to each of the rendering panes
		Control[] renderingControls = this.fRendering.getRenderingPanes();
		for (int i = 0; i < renderingControls.length; i++)
			super.createPopupMenu(renderingControls[i]);
		super.createPopupMenu(this.fRendering);

		// Create the copy actions

		final CopyAction copyBinaryAction = new CopyBinaryAction(this.fRendering);
		final CopyAction copyTextAction = new CopyTextAction(this.fRendering);
		final CopyAction copyAddressAction = new CopyAddressAction(this.fRendering);
		final CopyAction copyAllAction = new CopyAllAction(this.fRendering);

		// go-to address action
		final IAction goToAddressAction = new GoToAddressAction(getMemoryRenderingContainer(), this);

		// reset to base address

		final Action gotoBaseAddressAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.RESET_TO_BASE_ADDRESS")) //$NON-NLS-1$
		{
			@Override
			public void run() {
				Display.getDefault().asyncExec(() -> TraditionalRendering.this.fRendering
						.gotoAddress(TraditionalRendering.this.fRendering.fBaseAddress));
			}
		};

		// refresh

		final Action refreshAction = new Action(TraditionalRenderingMessages.getString("TraditionalRendering.REFRESH")) //$NON-NLS-1$
		{
			@Override
			public void run() {
				Display.getDefault().asyncExec(() -> {
					// For compatibility with DSF update modes (hopefully this will either be replaced by an enhanced
					// platform interface or the caching will move out of the data layer
					try {
						Method m = fRendering.getMemoryBlock().getClass().getMethod("clearCache", new Class[0]); //$NON-NLS-1$
						if (m != null)
							m.invoke(fRendering.getMemoryBlock(), new Object[0]);
					} catch (Exception e) {
					}

					TraditionalRendering.this.fRendering.refresh();
				});
			}
		};

		// display address

		final Action displayAddressPaneAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.ADDRESS"), //$NON-NLS-1$
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setPaneVisible(Rendering.PANE_ADDRESS, isChecked());
			}
		};
		displayAddressPaneAction.setChecked(this.fRendering.getPaneVisible(Rendering.PANE_ADDRESS));

		// display hex

		final Action displayBinaryPaneAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.BINARY"), //$NON-NLS-1$
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setPaneVisible(Rendering.PANE_BINARY, isChecked());
			}
		};
		displayBinaryPaneAction.setChecked(this.fRendering.getPaneVisible(Rendering.PANE_BINARY));

		// display text

		final Action displayTextPaneAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.TEXT"), //$NON-NLS-1$
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setPaneVisible(Rendering.PANE_TEXT, isChecked());
			}
		};
		displayTextPaneAction.setChecked(this.fRendering.getPaneVisible(Rendering.PANE_TEXT));

		// display size

		final Action displaySize1BytesAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.1_BYTE"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setBytesPerColumn(1);
			}
		};
		displaySize1BytesAction.setChecked(this.fRendering.getBytesPerColumn() == 1);

		final Action displaySize2BytesAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.2_BYTES"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setBytesPerColumn(2);
			}
		};
		displaySize2BytesAction.setChecked(this.fRendering.getBytesPerColumn() == 2);

		final Action displaySize4BytesAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.4_BYTES"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setBytesPerColumn(4);
			}
		};
		displaySize4BytesAction.setChecked(this.fRendering.getBytesPerColumn() == 4);

		final Action displaySize8BytesAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.8_BYTES"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setBytesPerColumn(8);
			}
		};
		displaySize8BytesAction.setChecked(this.fRendering.getBytesPerColumn() == 8);

		// text / unicode ?

		final Action displayCharactersISO8859Action = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.ISO-8859-1"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setTextMode(Rendering.TEXT_ISO_8859_1);
			}
		};
		displayCharactersISO8859Action.setChecked(this.fRendering.getTextMode() == Rendering.TEXT_ISO_8859_1);

		final Action displayCharactersUSASCIIAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.USASCII"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setTextMode(Rendering.TEXT_USASCII);
			}
		};
		displayCharactersUSASCIIAction.setChecked(this.fRendering.getTextMode() == Rendering.TEXT_USASCII);

		final Action displayCharactersUTF8Action = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.UTF8"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setTextMode(Rendering.TEXT_UTF8);
			}
		};
		displayCharactersUTF8Action.setChecked(this.fRendering.getTextMode() == Rendering.TEXT_UTF8);

		//        final Action displayCharactersUTF16Action = new Action(
		//                TraditionalRenderingMessages
		//                    .getString("TraditionalRendering.UTF16"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		//        {
		//            @Override
		//            public void run()
		//            {
		//                TraditionalRendering.this.fRendering
		//                .setTextMode(Rendering.TEXT_UTF16);
		//            }
		//        };
		//        displayCharactersUTF16Action.setChecked(this.fRendering
		//            .getTextMode() == Rendering.TEXT_UTF16);

		// endian

		displayEndianBigAction = new Action(TraditionalRenderingMessages.getString("TraditionalRendering.BIG"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setDisplayLittleEndian(false);
			}
		};
		displayEndianBigAction.setChecked(!this.fRendering.isTargetLittleEndian());

		displayEndianLittleAction = new Action(TraditionalRenderingMessages.getString("TraditionalRendering.LITTLE"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setDisplayLittleEndian(true);
			}
		};
		displayEndianLittleAction.setChecked(this.fRendering.isTargetLittleEndian());

		// radix

		final Action displayRadixHexAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.HEX"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setRadix(Rendering.RADIX_HEX);
			}
		};
		displayRadixHexAction.setChecked(this.fRendering.getRadix() == Rendering.RADIX_HEX);

		final Action displayRadixDecSignedAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.DECIMAL_SIGNED"), IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setRadix(Rendering.RADIX_DECIMAL_SIGNED);
			}
		};
		displayRadixDecSignedAction.setChecked(this.fRendering.getRadix() == Rendering.RADIX_DECIMAL_SIGNED);

		final Action displayRadixDecUnsignedAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.DECIMAL_UNSIGNED"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setRadix(Rendering.RADIX_DECIMAL_UNSIGNED);
			}
		};
		displayRadixDecUnsignedAction.setChecked(this.fRendering.getRadix() == Rendering.RADIX_DECIMAL_UNSIGNED);

		final Action displayRadixOctAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.OCTAL"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setRadix(Rendering.RADIX_OCTAL);
			}
		};
		displayRadixOctAction.setChecked(this.fRendering.getRadix() == Rendering.RADIX_OCTAL);

		final Action displayRadixBinAction = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.BINARY"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setRadix(Rendering.RADIX_BINARY);
			}
		};
		displayRadixBinAction.setChecked(this.fRendering.getRadix() == Rendering.RADIX_BINARY);

		final Action displayColumnCountAuto = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.COLUMN_COUNT_AUTO"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				TraditionalRendering.this.fRendering.setColumnsSetting(Rendering.COLUMNS_AUTO_SIZE_TO_FIT);
			}
		};
		displayColumnCountAuto.setChecked(fRendering.getColumnsSetting() == Rendering.COLUMNS_AUTO_SIZE_TO_FIT);

		final Action[] displayColumnCounts = new Action[MAX_MENU_COLUMN_COUNT];
		for (int i = 0, j = 1; i < MAX_MENU_COLUMN_COUNT; i++, j *= 2) {
			final int finali = j;
			displayColumnCounts[i] = new Action(
					TraditionalRenderingMessages.getString("TraditionalRendering.COLUMN_COUNT_" + finali), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					TraditionalRendering.this.fRendering.setColumnsSetting(finali);
				}
			};
			displayColumnCounts[i].setChecked(fRendering.getColumnsSetting() == finali);
		}

		final Action displayColumnCountCustomValue = new Action("", IAction.AS_RADIO_BUTTON) //$NON-NLS-1$
		{
			@Override
			public void run() {
			}
		};

		final Action displayColumnCountCustom = new Action(
				TraditionalRenderingMessages.getString("TraditionalRendering.COLUMN_COUNT_CUSTOM"), //$NON-NLS-1$
				IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				InputDialog inputDialog = new InputDialog(fRendering.getShell(), "Set Column Count", //$NON-NLS-1$
						"Please enter column count", //$NON-NLS-1$
						"", //$NON-NLS-1$
						input -> {
							try {
								int i = Integer.parseInt(input);
								if (i <= 0)
									return "Please enter a positive integer"; //$NON-NLS-1$
								if (i > 200)
									return "Please enter a positive integer not greater than 200"; //$NON-NLS-1$

							} catch (NumberFormatException x) {
								return "Please enter a positive integer"; //$NON-NLS-1$
							}
							return null;
						});

				if (inputDialog.open() != Window.OK) {
					this.setChecked(false);
					int currentColumnSetting = TraditionalRendering.this.fRendering.getColumnsSetting();
					if (currentColumnSetting == Rendering.COLUMNS_AUTO_SIZE_TO_FIT)
						displayColumnCountAuto.setChecked(true);
					else {
						boolean currentCountIsCustom = true;
						for (int i = 0, j = 1; i < MAX_MENU_COLUMN_COUNT && currentCountIsCustom; i++, j *= 2) {
							currentCountIsCustom = (j != fRendering.getColumnsSetting());
							if (j == fRendering.getColumnsSetting())
								displayColumnCounts[i].setChecked(true);
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

				for (int i = 0, j = 1; i < MAX_MENU_COLUMN_COUNT; i++, j *= 2) {
					if (newColumnCount == j) {
						customIsOneOfStandardListChoices = true;
						TraditionalRendering.this.fRendering.setColumnsSetting(newColumnCount);
						this.setChecked(false);
						displayColumnCountCustomValue.setChecked(false);
						displayColumnCounts[i].setChecked(true);
						break;
					}
				}

				if (!customIsOneOfStandardListChoices) {
					TraditionalRendering.this.fRendering.setColumnsSetting(newColumnCount);
					this.setChecked(false);

					displayColumnCountCustomValue.setChecked(true);
					displayColumnCountCustomValue.setText(Integer.valueOf(fRendering.getColumnsSetting()).toString());
				}
			}
		};

		getPopupMenuManager().addMenuListener(manager -> {
			manager.add(new Separator());

			MenuManager sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.PANES")); //$NON-NLS-1$
			sub.add(displayAddressPaneAction);
			sub.add(displayBinaryPaneAction);
			sub.add(displayTextPaneAction);
			manager.add(sub);

			// if there is cross reference info types available
			// add Actions to allow the user to toggle the visibility for each of them
			if (fRendering.isShowCrossReferenceInfo()) {
				Action[] dynamicActions = fRendering.getDynamicActions();
				if (dynamicActions != null) {
					sub = new MenuManager(TraditionalRenderingMessages
							.getString("TraditionalRenderingPreferencePage_ShowCrossRefInfo_Label")); //$NON-NLS-1$
					for (Action action : dynamicActions) {
						sub.add(action);
					}

					manager.add(sub);
				}
			}

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.ENDIAN")); //$NON-NLS-1$
			sub.add(displayEndianBigAction);
			sub.add(displayEndianLittleAction);
			manager.add(sub);

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.TEXT")); //$NON-NLS-1$
			sub.add(displayCharactersISO8859Action);
			sub.add(displayCharactersUSASCIIAction);
			sub.add(displayCharactersUTF8Action);
			//sub.add(displayCharactersUTF16Action);
			manager.add(sub);

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.CELL_SIZE")); //$NON-NLS-1$
			sub.add(displaySize1BytesAction);
			sub.add(displaySize2BytesAction);
			sub.add(displaySize4BytesAction);
			sub.add(displaySize8BytesAction);
			manager.add(sub);

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.RADIX")); //$NON-NLS-1$
			sub.add(displayRadixHexAction);
			sub.add(displayRadixDecSignedAction);
			sub.add(displayRadixDecUnsignedAction);
			sub.add(displayRadixOctAction);
			sub.add(displayRadixBinAction);
			manager.add(sub);

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.COLUMN_COUNT")); //$NON-NLS-1$
			sub.add(displayColumnCountAuto);
			for (int i = 0; i < displayColumnCounts.length; i++)
				sub.add(displayColumnCounts[i]);

			boolean currentCountIsCustom = fRendering.getColumnsSetting() != 0;
			for (int i = 0, j = 1; i < MAX_MENU_COLUMN_COUNT && currentCountIsCustom; i++, j *= 2)
				currentCountIsCustom = (j != fRendering.getColumnsSetting());
			if (currentCountIsCustom)
				sub.add(displayColumnCountCustomValue);

			sub.add(displayColumnCountCustom);
			manager.add(sub);

			final Action updateAlwaysAction = new Action(
					TraditionalRenderingMessages.getString("TraditionalRendering.UPDATE_ALWAYS"), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					fRendering.setUpdateMode(Rendering.UPDATE_ALWAYS);
				}
			};
			updateAlwaysAction.setChecked(fRendering.getUpdateMode() == Rendering.UPDATE_ALWAYS);

			final Action updateOnBreakpointAction = new Action(
					TraditionalRenderingMessages.getString("TraditionalRendering.UPDATE_ON_BREAKPOINT"), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					fRendering.setUpdateMode(Rendering.UPDATE_ON_BREAKPOINT);
				}
			};
			updateOnBreakpointAction.setChecked(fRendering.getUpdateMode() == Rendering.UPDATE_ON_BREAKPOINT);

			final Action updateManualAction = new Action(
					TraditionalRenderingMessages.getString("TraditionalRendering.UPDATE_MANUAL"), //$NON-NLS-1$
					IAction.AS_RADIO_BUTTON) {
				@Override
				public void run() {
					fRendering.setUpdateMode(Rendering.UPDATE_MANUAL);
				}
			};
			updateManualAction.setChecked(fRendering.getUpdateMode() == Rendering.UPDATE_MANUAL);

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.UPDATEMODE")); //$NON-NLS-1$
			sub.add(updateAlwaysAction);
			sub.add(updateOnBreakpointAction);
			sub.add(updateManualAction);
			manager.add(sub);
			manager.add(new Separator());

			BigInteger start = fRendering.getSelection().getStart();
			BigInteger end = fRendering.getSelection().getEnd();
			copyBinaryAction.setEnabled(start != null && end != null);
			copyTextAction.setEnabled(start != null && end != null);

			sub = new MenuManager(TraditionalRenderingMessages.getString("TraditionalRendering.COPY")); //$NON-NLS-1$
			sub.add(copyBinaryAction);
			sub.add(copyTextAction);
			sub.add(copyAddressAction);
			sub.add(copyAllAction);
			manager.add(sub);

			copyBinaryAction.checkStatus();
			copyTextAction.checkStatus();
			copyAddressAction.checkStatus();
			copyAllAction.checkStatus();

			manager.add(gotoBaseAddressAction);
			manager.add(goToAddressAction);
			manager.add(refreshAction);
			manager.add(new Separator());
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		});

	}

	@Override
	public Control getControl() {
		return this.fRendering;
	}

	// selection is terminology for caret position
	@Override
	public BigInteger getSelectedAddress() {
		IMemorySelection selection = fRendering.getSelection();
		if (selection == null || selection.getStart() == null)
			return fRendering.getCaretAddress();

		return selection.getStartLow();
	}

	@Override
	public MemoryByte[] getSelectedAsBytes() {
		try {
			// default to the caret address and the cell count size
			BigInteger startAddr = fRendering.getCaretAddress();
			int byteCount = fRendering.getBytesPerColumn();

			// Now see if there's a selection
			IMemorySelection selection = fRendering.getSelection();
			if (selection != null && selection.getStart() != null) {
				// The implementation is such that just having a caret somewhere
				// (without multiple cells being selected) constitutes a selection,
				// except for when the rendering is in its initial state. I.e.,
				// just because we get here doesn't mean the user has selected more
				// than one cell.

				startAddr = getSelectedAddress();

				if (selection.getHigh() != null) {
					byteCount = selection.getHigh().subtract(selection.getLow()).intValue()
							* fRendering.getAddressableSize();
				}
			}
			return fRendering.getViewportCache().getBytes(startAddr, byteCount);

		} catch (DebugException de) {
			// FIXME log?
			return null;
		}
	}

	@Override
	public void goToAddress(final BigInteger address) throws DebugException {
		Display.getDefault().asyncExec(() -> fRendering.gotoAddress(address));
	}

	protected void setTargetMemoryLittleEndian(boolean littleEndian) {
		// once we actually read memory we can determine the
		// endianess and need to set these actions accordingly.
		displayEndianBigAction.setChecked(!littleEndian);
		displayEndianLittleAction.setChecked(littleEndian);

		// when target endian changes, force display endian to track.
		// user can then change display endian if desired.
		fRendering.setDisplayLittleEndian(littleEndian);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings({ "unchecked" })
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
						return TraditionalRenderingMessages.getString("TraditionalRendering.RENDERING_NAME"); //$NON-NLS-1$
					}

					@Override
					public Object getParent(Object o) {
						return null;
					}
				};
			}
			return (T) this.fWorkbenchAdapter;
		}

		if (adapter == IMemoryBlockConnection.class) {
			if (fConnection == null) {
				fConnection = () -> {
					// update UI asynchronously
					Display display = TraditionalRenderingPlugin.getDefault().getWorkbench().getDisplay();
					display.asyncExec(() -> {
						try {
							if (fBigBaseAddress != TraditionalRendering.this.fRendering.getMemoryBlock()
									.getBigBaseAddress()) {
								fBigBaseAddress = TraditionalRendering.this.fRendering.getMemoryBlock()
										.getBigBaseAddress();
								TraditionalRendering.this.fRendering.gotoAddress(fBigBaseAddress);
							}
							TraditionalRendering.this.fRendering.refresh();
						} catch (DebugException e) {
						}
					});
				};
			}
			return (T) fConnection;
		}

		return super.getAdapter(adapter);
	}

	@Override
	public void resetRendering() throws DebugException {
		fRendering.gotoAddress(fRendering.fBaseAddress);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.model.provisional.IMemoryRenderingViewportProvider#getViewportAddress()
	 */
	@Override
	public BigInteger getViewportAddress() {
		return fRendering.getViewportStartAddress();
	}
}

class CopyBinaryAction extends CopyAction {

	public CopyBinaryAction(Rendering rendering) {
		super(rendering, CopyType.BINARY, DND.CLIPBOARD);
		setText(TraditionalRenderingMessages.getString("TraditionalRendering.BINARY")); //$NON-NLS-1$
		setToolTipText(TraditionalRenderingMessages.getString("TraditionalRendering.COPY_SELECTED_DATA")); //$NON-NLS-1$
	}
}

class CopyTextAction extends CopyAction {

	public CopyTextAction(Rendering rendering) {
		super(rendering, CopyType.TEXT, DND.CLIPBOARD);
		setText(TraditionalRenderingMessages.getString("TraditionalRendering.TEXT")); //$NON-NLS-1$
		setToolTipText(TraditionalRenderingMessages.getString("TraditionalRendering.COPY_SELECTED_DATA_TEXT")); //$NON-NLS-1$
	}
}

class CopyAddressAction extends CopyAction {

	public CopyAddressAction(Rendering rendering) {
		super(rendering, CopyType.ADDRESS, DND.CLIPBOARD);
		setText(TraditionalRenderingMessages.getString("TraditionalRendering.ADDRESS")); //$NON-NLS-1$
		setToolTipText(TraditionalRenderingMessages.getString("TraditionalRendering.COPY_SELECTED_ADDDESS")); //$NON-NLS-1$
	}
}

class CopyAllAction extends CopyAction {

	public CopyAllAction(Rendering rendering) {
		super(rendering, CopyType.ALL, DND.CLIPBOARD);
		setText(TraditionalRenderingMessages.getString("TraditionalRendering.ALL")); //$NON-NLS-1$
		setToolTipText(TraditionalRenderingMessages.getString("TraditionalRendering.COPY_ALL")); //$NON-NLS-1$
	}
}

class CopyDefaultAction extends CopyAction {

	public CopyDefaultAction(Rendering rendering, int clipType) {
		super(rendering, CopyType.DEFAULT, clipType);
		setText(TraditionalRenderingMessages.getString("TraditionalRendering.DEFAULT")); //$NON-NLS-1$
	}
}

abstract class CopyAction extends Action {
	// TODO for the sake of large copies, this action should probably read in
	// blocks on a Job.
	public enum CopyType {
		UNDEFINED, BINARY, TEXT, ADDRESS, ALL, DEFAULT
	}

	private Rendering fRendering;
	private int fClipboardType = DND.CLIPBOARD;
	private CopyType fCopyType = CopyType.ALL;
	static private CopyType fDefaultCopyType = CopyType.UNDEFINED;

	public CopyAction(Rendering rendering, CopyType copyType, int clipboardType) {
		super();
		fCopyType = copyType;
		fClipboardType = clipboardType;
		fRendering = rendering;

		if (fDefaultCopyType == CopyType.UNDEFINED) {
			IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
			String defaultString = store.getString(TraditionalRenderingPreferenceConstants.MEM_DEFAULT_COPY_ACTION);
			if (defaultString.length() > 0) {
				try {
					fDefaultCopyType = Enum.valueOf(CopyType.class, defaultString);
				} catch (Exception e) {
					fDefaultCopyType = CopyType.ALL;
				}
			} else
				fDefaultCopyType = CopyType.ALL;
		}

	}

	public void checkStatus() {
		setChecked(fDefaultCopyType == fCopyType);
	}

	@Override
	public void run() {
		final String PANE_SPACING = "  "; //$NON-NLS-1$

		Clipboard clip = null;
		try {
			if (fCopyType == CopyType.DEFAULT)
				fCopyType = fDefaultCopyType;

			if (fCopyType != fDefaultCopyType) {
				IPreferenceStore store = TraditionalRenderingPlugin.getDefault().getPreferenceStore();
				store.putValue(TraditionalRenderingPreferenceConstants.MEM_DEFAULT_COPY_ACTION, fCopyType.name());
			}
			fDefaultCopyType = fCopyType;

			boolean copyAddress = fRendering.getPaneVisible(Rendering.PANE_ADDRESS)
					&& (fCopyType == CopyType.ADDRESS || fCopyType == CopyType.ALL);
			boolean copyBinary = fRendering.getPaneVisible(Rendering.PANE_BINARY)
					&& (fCopyType == CopyType.BINARY || fCopyType == CopyType.ALL);
			boolean copyText = fRendering.getPaneVisible(Rendering.PANE_TEXT)
					&& (fCopyType == CopyType.TEXT || fCopyType == CopyType.ALL);

			clip = new Clipboard(fRendering.getDisplay());

			BigInteger start = fRendering.getSelection().getStart();
			BigInteger end = fRendering.getSelection().getEnd();
			// end will be null when there is nothing selected
			if (end == null) {
				if (fCopyType == CopyType.ADDRESS)
					fRendering.copyAddressToClipboard();
				return;
			}

			if (start.compareTo(end) > 0) {
				// swap start and end

				BigInteger bigI = end;
				end = start;
				start = bigI;
			}

			final int radix = fRendering.getRadix();
			final int bytesPerColumn = fRendering.getBytesPerColumn();
			final boolean isLittleEndian = fRendering.isTargetLittleEndian();

			//            final int binaryCellWidth = fRendering.getRadixCharacterCount(
			//                radix, bytesPerColumn) + 1;
			//
			//            final int asciiCellWidth = fRendering.getBytesPerColumn()
			//                / fRendering.getBytesPerCharacter();

			//            final int combindCellWidths = (fRendering
			//                .getPaneVisible(Rendering.PANE_BINARY) ? binaryCellWidth : 0)
			//                + (fRendering.getPaneVisible(Rendering.PANE_TEXT) ? asciiCellWidth
			//                    : 0);

			final int columns = fRendering.getColumnCount();
			int addressableSize = fRendering.getAddressableSize();
			assert (addressableSize != 0);

			int addressesPerColumn = bytesPerColumn / addressableSize;

			BigInteger lengthToRead = end.subtract(start).multiply(BigInteger.valueOf(addressableSize));

			int rows = lengthToRead.divide(BigInteger.valueOf(columns * bytesPerColumn)).intValue();

			if (rows * columns * bytesPerColumn < lengthToRead.intValue())
				rows++;

			StringBuilder buffer = new StringBuilder();

			for (int row = 0; row < rows; row++) {
				BigInteger rowAddress = start.add(BigInteger.valueOf(row * columns * addressesPerColumn));

				if (copyAddress) {
					buffer.append(fRendering.getAddressString(rowAddress));
					buffer.append(PANE_SPACING);
				}

				if (copyBinary) {
					for (int col = 0; col < columns; col++) {
						BigInteger cellAddress = rowAddress.add(BigInteger.valueOf(col * addressesPerColumn));

						if (cellAddress.compareTo(end) < 0) {
							try {
								MemoryByte bytes[] = fRendering.getBytes(cellAddress, bytesPerColumn);
								buffer.append(fRendering.getRadixText(bytes, radix, isLittleEndian));
							} catch (DebugException de) {
								fRendering.logError(TraditionalRenderingMessages
										.getString("TraditionalRendering.FAILURE_COPY_OPERATION"), de); //$NON-NLS-1$
								return;
							}
						} else {
							for (int i = fRendering.getRadixCharacterCount(radix, bytesPerColumn); i > 0; i--)
								buffer.append(' ');
						}

						if (col != columns - 1)
							buffer.append(' ');
					}
				}

				if (copyBinary && copyText) {
					buffer.append(PANE_SPACING);
				}

				if (copyText) {
					for (int col = 0; col < columns; col++) {
						BigInteger cellAddress = rowAddress.add(BigInteger.valueOf(col * addressesPerColumn));

						if (cellAddress.compareTo(end) < 0) {
							try {
								MemoryByte bytes[] = fRendering.getBytes(cellAddress, fRendering.getBytesPerColumn());
								buffer.append(fRendering.formatText(bytes, isLittleEndian, fRendering.getTextMode()));
							} catch (DebugException de) {
								fRendering.logError(TraditionalRenderingMessages
										.getString("TraditionalRendering.FAILURE_COPY_OPERATION"), de); //$NON-NLS-1$
								return;
							}
						} else {

						}
					}
				}

				buffer.append("\n"); //$NON-NLS-1$
			}

			if (buffer.length() > 0) {
				TextTransfer plainTextTransfer = TextTransfer.getInstance();
				clip.setContents(new Object[] { buffer.toString() }, new Transfer[] { plainTextTransfer },
						fClipboardType);
			}
		} finally {
			checkStatus();
			if (clip != null) {
				clip.dispose();
			}
		}
	}
}
