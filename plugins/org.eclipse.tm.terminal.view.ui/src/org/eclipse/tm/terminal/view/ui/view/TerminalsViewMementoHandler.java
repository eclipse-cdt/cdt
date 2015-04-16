/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.actions.PinTerminalAction;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.tm.terminal.view.ui.tabs.TabFolderToolbarHandler;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

/**
 * Take care of the persisted state handling of the "Terminals" view.
 */
public class TerminalsViewMementoHandler {
	// The list of items to save. See the workbench listener implementation
	// in o.e.tm.terminal.view.ui.activator.UIPlugin.
	private final List<CTabItem> saveables = new ArrayList<CTabItem>();

	/**
	 * Sets the list of saveable items.
	 *
	 * @param saveables The list of saveable items. Must not be <code>null</code>.
	 */
	public void setSaveables(List<CTabItem> saveables) {
		Assert.isNotNull(saveables);
		this.saveables.clear();
		this.saveables.addAll(saveables);
	}

	/**
	 * Saves the view state in the given memento.
	 *
	 * @param view The terminals view. Must not be <code>null</code>.
	 * @param memento The memento. Must not be <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
    public void saveState(TerminalsView view, IMemento memento) {
		Assert.isNotNull(view);
		Assert.isNotNull(memento);

		// Create a child element within the memento holding the
		// connection info of the open, non-terminated tab items
		memento = memento.createChild("terminalConnections"); //$NON-NLS-1$
		Assert.isNotNull(memento);

		// Write the view id and secondary id
		memento.putString("id", view.getViewSite().getId()); //$NON-NLS-1$
		memento.putString("secondaryId", view.getViewSite().getSecondaryId()); //$NON-NLS-1$

		// Save the pinned state
		memento.putBoolean("pinned", view.isPinned()); //$NON-NLS-1$

		// Loop the saveable items and store the connection data of each
		// item to the memento
		for (CTabItem item : saveables) {
			// Ignore disposed items
			if (item.isDisposed()) continue;

			// Get the original terminal properties associated with the tab item
			Map<String, Object> properties = (Map<String, Object>)item.getData("properties"); //$NON-NLS-1$
			if (properties == null) continue;

			// Get the terminal launcher delegate
			String delegateId = (String)properties.get(ITerminalsConnectorConstants.PROP_DELEGATE_ID);
			ILauncherDelegate delegate = delegateId != null ? LauncherDelegateManager.getInstance().getLauncherDelegate(delegateId, false) : null;
			IMementoHandler mementoHandler = delegate != null ? (IMementoHandler)delegate.getAdapter(IMementoHandler.class) : null;
			if (mementoHandler != null) {
				// Create terminal connection child memento
				IMemento connectionMemento = memento.createChild("connection"); //$NON-NLS-1$
				Assert.isNotNull(connectionMemento);
				// Store the common attributes
				connectionMemento.putString(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegateId);

				String terminalConnectorId = (String)properties.get(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
				if (terminalConnectorId != null) {
					connectionMemento.putString(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, terminalConnectorId);
				}

				if (properties.get(ITerminalsConnectorConstants.PROP_FORCE_NEW) instanceof Boolean) {
					connectionMemento.putBoolean(ITerminalsConnectorConstants.PROP_FORCE_NEW, ((Boolean)properties.get(ITerminalsConnectorConstants.PROP_FORCE_NEW)).booleanValue());
				}

				// Store the current encoding
				ITerminalViewControl terminal = (ITerminalViewControl)item.getData();
				String encoding = terminal != null ? terminal.getEncoding() : null;
				if (encoding == null || "".equals(encoding)) encoding = (String)properties.get(ITerminalsConnectorConstants.PROP_ENCODING); //$NON-NLS-1$
				if (encoding != null && !"".equals(encoding)) { //$NON-NLS-1$
					connectionMemento.putString(ITerminalsConnectorConstants.PROP_ENCODING, encoding);
				}

				// Pass on to the memento handler
				mementoHandler.saveState(connectionMemento, properties);
			}
		}
	}

	/**
	 * Restore the view state from the given memento.
	 *
	 * @param view The terminals view. Must not be <code>null</code>.
	 * @param memento The memento. Must not be <code>null</code>.
	 */
	protected void restoreState(final TerminalsView view, IMemento memento) {
		Assert.isNotNull(view);
		Assert.isNotNull(memento);

		// Get the "terminalConnections" memento
		memento = memento.getChild("terminalConnections"); //$NON-NLS-1$
		if (memento != null) {
			// Read view id and secondary id
			String id = memento.getString("id"); //$NON-NLS-1$
			String secondaryId = memento.getString("secondaryId"); //$NON-NLS-1$
			if ("null".equals(secondaryId)) secondaryId = null; //$NON-NLS-1$

			final IMemento finMemento = memento;
			// Restore the pinned state of the after all connections completed
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					if (finMemento.getBoolean("pinned") != null) { //$NON-NLS-1$
						asyncExec(new Runnable() {
							@Override
							public void run() {
								view.setPinned(finMemento.getBoolean("pinned").booleanValue()); //$NON-NLS-1$

								TabFolderToolbarHandler toolbarHandler = (TabFolderToolbarHandler)view.getAdapter(TabFolderToolbarHandler.class);
								if (toolbarHandler != null) {
									PinTerminalAction action = (PinTerminalAction)toolbarHandler.getAdapter(PinTerminalAction.class);
									if (action != null) action.setChecked(view.isPinned());
								}
							}
						});
					}
				}
			};

			final AtomicBoolean allProcessed = new AtomicBoolean(false);
			final List<ITerminalService.Done> callbacks = new ArrayList<ITerminalService.Done>();

			// Get all the "connection" memento's.
			IMemento[] connections = memento.getChildren("connection"); //$NON-NLS-1$
			for (IMemento connection : connections) {
				// Create the properties container that holds the terminal properties
				Map<String, Object> properties = new HashMap<String, Object>();

				// Set the view id attributes
				properties.put(ITerminalsConnectorConstants.PROP_ID, id);
				properties.put(ITerminalsConnectorConstants.PROP_SECONDARY_ID, secondaryId);

				// Restore the common attributes
				properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, connection.getString(ITerminalsConnectorConstants.PROP_DELEGATE_ID));
				properties.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, connection.getString(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID));
				if (connection.getBoolean(ITerminalsConnectorConstants.PROP_FORCE_NEW) != null) {
					properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, connection.getBoolean(ITerminalsConnectorConstants.PROP_FORCE_NEW));
				}

				// Restore the encoding
				if (connection.getString(ITerminalsConnectorConstants.PROP_ENCODING) != null) {
					properties.put(ITerminalsConnectorConstants.PROP_ENCODING, connection.getString(ITerminalsConnectorConstants.PROP_ENCODING));
				}

                // Get the terminal launcher delegate
                String delegateId = (String)properties.get(ITerminalsConnectorConstants.PROP_DELEGATE_ID);
                ILauncherDelegate delegate = delegateId != null ? LauncherDelegateManager.getInstance().getLauncherDelegate(delegateId, false) : null;
                IMementoHandler mementoHandler = delegate != null ? (IMementoHandler)delegate.getAdapter(IMementoHandler.class) : null;
                if (mementoHandler != null) {
                	// Pass on to the memento handler
                	mementoHandler.restoreState(connection, properties);
                }

                // Restore the terminal connection
                if (delegate != null && !properties.isEmpty()) {
                	ITerminalService.Done done = new ITerminalService.Done() {
                		@Override
                		public void done(IStatus status) {
							callbacks.remove(this);
							if (allProcessed.get() && callbacks.isEmpty()) {
								asyncExec(runnable);
							}
						}
					};
					callbacks.add(done);
                	delegate.execute(properties, done);
                }
			}

			allProcessed.set(true);
			if (callbacks.isEmpty()) asyncExec(runnable);
		}
	}

	/**
	 * Executes the given runnable asynchronously in the display thread.
	 *
	 * @param runnable The runnable. Must not be <code>null</code>.
	 */
	/* default */ void asyncExec(Runnable runnable) {
		Assert.isNotNull(runnable);
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getDisplay() != null && !PlatformUI.getWorkbench().getDisplay().isDisposed()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
		}
	}
}
