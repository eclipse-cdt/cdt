/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361363] [TERMINALS] Implement "Pin&Clone" for the "Terminals" view
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.interfaces.IPreferenceKeys;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.interfaces.IUIConstants;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderManager;
import org.eclipse.tm.terminal.view.ui.view.TerminalsView;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

/**
 * Terminal console manager.
 */
public class ConsoleManager {

	// Constant to indicate any secondary id is acceptable
	private final static String ANY_SECONDARY_ID = new String("*"); //$NON-NLS-1$

	// Reference to the perspective listener instance
	private final IPerspectiveListener perspectiveListener;

	// Internal perspective listener implementation
	static class ConsoleManagerPerspectiveListener extends PerspectiveAdapter {
		private final List<IViewReference> references = new ArrayList<>();

		@Override
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			// If the old references list is empty, just return
			if (references.isEmpty())
				return;
			// Create a copy of the old view references list
			List<IViewReference> oldReferences = new ArrayList<>(references);

			// Get the current list of view references
			List<IViewReference> references = new ArrayList<>(Arrays.asList(page.getViewReferences()));
			for (IViewReference reference : oldReferences) {
				if (references.contains(reference))
					continue;
				// Previous visible terminals console view reference, make visible again
				try {
					page.showView(reference.getId(), reference.getSecondaryId(), IWorkbenchPage.VIEW_VISIBLE);
				} catch (PartInitException e) {
					/* Failure on part instantiation is ignored */ }
			}

		}

		@Override
		public void perspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			references.clear();
			for (IViewReference reference : page.getViewReferences()) {
				IViewPart part = reference.getView(false);
				if (part instanceof TerminalsView && !references.contains(reference)) {
					references.add(reference);
				}
			}
		}
	}

	// Reference to the part listener instance
	private final IPartListener2 partListener;

	// The ids of the last activated terminals view
	/* default */ String lastActiveViewId = null;
	/* default */ String lastActiveSecondaryViewId = null;

	// Internal part listener implementation
	class ConsoleManagerPartListener implements IPartListener2 {

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part instanceof ITerminalsView) {
				lastActiveViewId = ((ITerminalsView) part).getViewSite().getId();
				lastActiveSecondaryViewId = ((ITerminalsView) part).getViewSite().getSecondaryId();
				//System.out.println("Terminals view activated: id = " + lastActiveViewId + ", secondary id = " + lastActiveSecondaryViewId); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}
	}

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstanceHolder {
		public static ConsoleManager fInstance = new ConsoleManager();
	}

	/**
	 * Returns the singleton instance for the console manager.
	 */
	public static ConsoleManager getInstance() {
		return LazyInstanceHolder.fInstance;
	}

	/**
	 * Constructor.
	 */
	ConsoleManager() {
		super();

		perspectiveListener = new ConsoleManagerPerspectiveListener();
		partListener = new ConsoleManagerPartListener();

		if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(perspectiveListener);

			IPartService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService();
			service.addPartListener(partListener);
		}
	}

	/**
	 * Returns the active workbench window page if the workbench is still running.
	 *
	 * @return The active workbench window page or <code>null</code>
	 */
	private final IWorkbenchPage getActiveWorkbenchPage() {
		// To lookup the console view, the workbench must be still running
		if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		}
		return null;
	}

	/**
	 * Returns the console view if available within the active workbench window page.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 * @param secondaryId The terminals console view secondary id or <code>null</code>.
	 *
	 * @return The console view instance if available or <code>null</code> otherwise.
	 */
	public ITerminalsView findConsoleView(String id, String secondaryId) {
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		ITerminalsView view = null;

		// Get the active workbench page
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			// Look for the view
			IViewPart part = getTerminalsViewWithSecondaryId(id != null ? id : IUIConstants.ID, secondaryId, true);
			// Check the interface
			if (part instanceof ITerminalsView) {
				view = (ITerminalsView) part;
			}
		}

		return view;
	}

	/**
	 * Search and return a terminal view with a specific secondary id
	 *
	 * @param id The terminals console view id. Must not be <code>null</code>.
	 * @param secondaryId The terminals console view secondary id or <code>null</code>.
	 * @param restore <code>True</code> if to try to restore the view, <code>false</code> otherwise.
	 *
	 * @return The terminals console view instance or <code>null</code> if not found.
	 */
	private IViewPart getTerminalsViewWithSecondaryId(String id, String secondaryId, boolean restore) {
		Assert.isNotNull(id);

		for (IViewReference ref : getActiveWorkbenchPage().getViewReferences()) {
			if (ref.getId().equals(id)) {
				if (ANY_SECONDARY_ID.equals(secondaryId) || secondaryId == null && ref.getSecondaryId() == null
						|| secondaryId != null && secondaryId.equals(ref.getSecondaryId())) {
					return ref.getView(restore);
				}
			}
		}
		return null;
	}

	/**
	 * Search and return the active terminals view.
	 *
	 * @param id The terminals console view id. Must not be <code>null</code>.
	 * @param secondaryId The terminals console view secondary id or <code>null</code>.
	 * @return The terminals console view instance or <code>null</code> if not found.
	 */
	private IViewPart getActiveTerminalsView(String id, String secondaryId) {
		Assert.isNotNull(id);

		IViewPart part = null;

		if (id.equals(lastActiveViewId)) {
			if (secondaryId == null || ANY_SECONDARY_ID.equals(secondaryId)
					|| secondaryId.equals(lastActiveSecondaryViewId)) {
				part = getTerminalsViewWithSecondaryId(lastActiveViewId, lastActiveSecondaryViewId, false);
			}
		}

		if (part == null) {
			part = getTerminalsViewWithSecondaryId(id, secondaryId, true);
			if (part != null) {
				lastActiveViewId = part.getViewSite().getId();
				lastActiveSecondaryViewId = part.getViewSite().getSecondaryId();
			}
		}

		return part;
	}

	/**
	 * Return a new secondary id to use, based on the number of open terminal views.
	 *
	 * @param id The terminals console view id. Must not be <code>null</code>.
	 * @return The next secondary id, or <code>null</code> if it is the first one
	 * @since 4.1
	 */
	public String getNextTerminalSecondaryId(String id) {
		Assert.isNotNull(id);

		Map<String, IViewReference> terminalViews = new HashMap<>();

		int maxNumber = 0;
		for (IViewReference ref : getActiveWorkbenchPage().getViewReferences()) {
			if (ref.getId().equals(id)) {
				if (ref.getSecondaryId() != null) {
					terminalViews.put(ref.getSecondaryId(), ref);
					int scondaryIdInt = Integer.parseInt(ref.getSecondaryId());
					if (scondaryIdInt > maxNumber) {
						maxNumber = scondaryIdInt;
					}
				} else {
					// add the one with secondaryId == null with 0 by default
					terminalViews.put(Integer.toString(0), ref);
				}
			}
		}
		if (terminalViews.size() == 0) {
			return null;
		}

		int i = 0;
		for (; i < maxNumber; i++) {
			String secondaryIdStr = Integer.toString(i);
			if (!terminalViews.keySet().contains(secondaryIdStr)) {
				// found a free slot
				if (i == 0)
					return null;
				return Integer.toString(i);
			}
		}
		// add a new one
		return Integer.toString(i + 1);
	}

	/**
	 * Show the terminals console view specified by the given id.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 */
	public IViewPart showConsoleView(String id, String secondaryId) {
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Get the active workbench page
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			try {
				// show the view
				IViewPart part = getActiveTerminalsView(id != null ? id : IUIConstants.ID, secondaryId);
				if (part == null)
					part = page.showView(id != null ? id : IUIConstants.ID, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				// and force the view to the foreground
				page.bringToTop(part);
				return part;
			} catch (PartInitException e) {
				IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
				UIPlugin.getDefault().getLog().log(status);
			}
		}
		return null;
	}

	/**
	 * Bring the terminals console view, specified by the given id, to the top of the view stack.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 * @param secondaryId The terminals console view secondary id or <code>null</code>.
	 * @param activate If <code>true</code> activate the console view.
	 */
	private IViewPart bringToTop(String id, String secondaryId, boolean activate) {
		// Get the active workbench page
		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			// get (last) active terminal view
			IViewPart activePart = getActiveTerminalsView(id != null ? id : IUIConstants.ID, secondaryId);
			if (activePart == null) {
				// Create a new one
				IViewPart newPart = showConsoleView(id != null ? id : IUIConstants.ID, getSecondaryId(secondaryId, id));
				return newPart;
			}

			if (activate)
				page.activate(activePart);
			else
				page.bringToTop(activePart);

			return activePart;
		}
		return null;
	}

	/**
	 * Return the secondary id to use.
	 * @param secondaryId
	 * @param id
	 * @return the secondaryId argument is not null, or *, otherwise use the auto generated secondary id.
	 */
	private String getSecondaryId(String secondaryId, String id) {
		if (secondaryId == null || ANY_SECONDARY_ID.equals(secondaryId)) {
			return getNextTerminalSecondaryId(id != null ? id : IUIConstants.ID);
		}

		return secondaryId;
	}

	/**
	 * Opens the console with the given title and connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 * @param title The console title. Must not be <code>null</code>.
	 * @param encoding The terminal encoding or <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 * @param flags The flags controlling how the console is opened or <code>null</code> to use defaults.
	 */
	public CTabItem openConsole(String id, String title, String encoding, ITerminalConnector connector, Object data,
			Map<String, Boolean> flags) {
		return openConsole(id, ANY_SECONDARY_ID, title, encoding, connector, data, flags);
	}

	/**
	 * Opens the console with the given title and connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 * @param secondaryId The terminals console view secondary id or <code>null</code>.
	 * @param title The console title. Must not be <code>null</code>.
	 * @param encoding The terminal encoding or <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 * @param flags The flags controlling how the console is opened or <code>null</code> to use defaults.
	 */
	public CTabItem openConsole(String id, String secondaryId, String title, String encoding,
			ITerminalConnector connector, Object data, Map<String, Boolean> flags) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Get the flags handled by the openConsole method itself
		boolean activate = flags != null && flags.containsKey("activate") ? flags.get("activate").booleanValue() //$NON-NLS-1$//$NON-NLS-2$
				: false;
		boolean forceNew = flags != null && flags.containsKey(ITerminalsConnectorConstants.PROP_FORCE_NEW)
				? flags.get(ITerminalsConnectorConstants.PROP_FORCE_NEW).booleanValue()
				: false;

		// Make the consoles view visible
		IViewPart part = bringToTop(id, secondaryId, activate);
		if (!(part instanceof ITerminalsView))
			return null;
		// Cast to the correct type
		ITerminalsView view = (ITerminalsView) part;

		// Get the tab folder manager associated with the view
		TabFolderManager manager = view.getAdapter(TabFolderManager.class);
		if (manager == null)
			return null;

		// Lookup an existing console first
		String secId = ((IViewSite) part.getSite()).getSecondaryId();
		CTabItem item = findConsole(id, secId, title, connector, data);

		// Switch to the tab folder page _before_ calling TabFolderManager#createItem(...).
		// The createItem(...) method invokes the corresponding connect and this may take
		// a while if connecting to a remote host. To allow a "Connecting..." decoration,
		// the tab folder page needs to be visible.
		view.switchToTabFolderControl();

		// If no existing console exist or forced -> Create the tab item
		if (item == null || forceNew) {
			// If configured, check all existing tab items if they are associated
			// with terminated consoles
			if (UIPlugin.getScopedPreferences().getBoolean(IPreferenceKeys.PREF_REMOVE_TERMINATED_TERMINALS)) {
				// Remote all terminated tab items. This will invoke the
				// tab's dispose listener.
				manager.removeTerminatedItems();
				// Switch back to the tab folder control as removeTerminatedItems()
				// may have triggered the switch to the empty space control.
				view.switchToTabFolderControl();
			}

			// Create a new tab item
			item = manager.createTabItem(title, encoding, connector, data, flags);
		}
		// If still null, something went wrong
		if (item == null)
			return null;

		// Make the item the active console
		manager.bringToTop(item);

		// Make sure the terminals view has the focus after opening a new terminal
		view.setFocus();

		// Return the tab item of the opened console
		return item;
	}

	/**
	 * Lookup a console with the given title and the given terminal connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 * <b>Note:</b> The method will handle unified console titles itself.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 * @param secondaryId The terminals console view secondary id or <code>null</code>.
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 *
	 * @return The corresponding console tab item or <code>null</code>.
	 */
	public CTabItem findConsole(String id, String secondaryId, String title, ITerminalConnector connector,
			Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Get the console view
		ITerminalsView view = findConsoleView(id, secondaryId);
		if (view == null)
			return null;

		// Get the tab folder manager associated with the view
		TabFolderManager manager = view.getAdapter(TabFolderManager.class);
		if (manager == null)
			return null;

		return manager.findTabItem(title, connector, data);
	}

	/**
	 * Lookup a console which is assigned with the given terminal control.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 *
	 * @param control The terminal control. Must not be <code>null</code>.
	 * @return The corresponding console tab item or <code>null</code>.
	 */
	public CTabItem findConsole(ITerminalControl control) {
		Assert.isNotNull(control);

		CTabItem item = null;

		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			for (IViewReference ref : page.getViewReferences()) {
				IViewPart part = ref != null ? ref.getView(false) : null;
				if (part instanceof ITerminalsView) {
					CTabFolder tabFolder = part.getAdapter(CTabFolder.class);
					if (tabFolder == null)
						continue;
					CTabItem[] candidates = tabFolder.getItems();
					for (CTabItem candidate : candidates) {
						Object data = candidate.getData();
						if (data instanceof ITerminalControl && control.equals(data)) {
							item = candidate;
							break;
						}
					}
				}
				if (item != null)
					break;
			}
		}

		return item;
	}

	/**
	 * Search all console views for the one that contains a specific connector.
	 * <p>
	 * <b>Note:</b> The method will handle unified console titles itself.
	 *
	 * @param id The terminals console view id or <code>null</code> to show the default terminals console view.
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 *
	 * @return The corresponding console tab item or <code>null</code>.
	 */
	private CTabItem findConsoleForTerminalConnector(String id, String title, ITerminalConnector connector,
			Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);

		IWorkbenchPage page = getActiveWorkbenchPage();
		if (page != null) {
			IViewReference[] refs = page.getViewReferences();
			for (IViewReference ref : refs) {
				if (ref.getId().equals(id)) {
					IViewPart part = ref.getView(true);
					if (part instanceof ITerminalsView) {
						// Get the tab folder manager associated with the view
						TabFolderManager manager = part.getAdapter(TabFolderManager.class);
						if (manager == null) {
							continue;
						}
						CTabItem item = manager.findTabItem(title, connector, data);
						if (item != null) {
							return item;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Close the console with the given title and the given terminal connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 * <b>Note:</b> The method will handle unified console titles itself.
	 *
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 */
	public void closeConsole(String id, String title, ITerminalConnector connector, Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Lookup the console with this connector
		CTabItem console = findConsoleForTerminalConnector(id, title, connector, data);
		// If found, dispose the console
		if (console != null) {
			console.dispose();
		}
	}

	/**
	 * Terminate (disconnect) the console with the given title and the given terminal connector.
	 * <p>
	 * <b>Note:</b> The method must be called within the UI thread.
	 * <b>Note:</b> The method will handle unified console titles itself.
	 *
	 * @param title The console title. Must not be <code>null</code>.
	 * @param connector The terminal connector. Must not be <code>null</code>.
	 * @param data The custom terminal data node or <code>null</code>.
	 */
	public void terminateConsole(String id, String title, ITerminalConnector connector, Object data) {
		Assert.isNotNull(title);
		Assert.isNotNull(connector);
		Assert.isNotNull(Display.findDisplay(Thread.currentThread()));

		// Lookup the console
		CTabItem console = findConsoleForTerminalConnector(id, title, connector, data);
		// If found, disconnect the console
		if (console != null && !console.isDisposed()) {
			ITerminalViewControl terminal = (ITerminalViewControl) console.getData();
			if (terminal != null && !terminal.isDisposed()) {
				terminal.disconnectTerminal();
			}
		}
	}
}
