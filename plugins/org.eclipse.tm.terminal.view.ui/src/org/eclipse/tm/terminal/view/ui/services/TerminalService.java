/*******************************************************************************
 * Copyright (c) 2011, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.terminal.view.core.activator.CoreBundleActivator;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalTabListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.interfaces.IUIConstants;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.eclipse.tm.terminal.view.ui.manager.ConsoleManager;
import org.eclipse.tm.terminal.view.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Terminal service implementation.
 */
@SuppressWarnings("restriction")
public class TerminalService implements ITerminalService {
	/**
	 * The registered terminal tab dispose listeners.
	 */
	private final ListenerList terminalTabListeners = new ListenerList();

	// Flag to remember if the terminal view has been restored or not.
	private boolean fRestoringView;

	// Terminal tab events

	/**
	 * A terminal tab got disposed.
	 */
	public static final int TAB_DISPOSED = 1;

	/**
	 * Common terminal service runnable implementation.
	 */
	protected static abstract class TerminalServiceRunnable {

		/**
		 * Invoked to execute the terminal service runnable.
		 *
		 * @param id The terminals view id or <code>null</code>.
		 * @param secondaryId The terminals view secondary id or <code>null</code>.
		 * @param title The terminal tab title. Must not be <code>null</code>.
		 * @param connector The terminal connector. Must not be <code>null</code>.
		 * @param data The custom terminal data node or <code>null</code>.
		 * @param done The callback to invoke if the operation finished or <code>null</code>.
		 */
		public abstract void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, Done done);

		/**
		 * Returns if or if not to execute the runnable asynchronously.
		 * <p>
		 * The method returns per default <code>true</code>. Overwrite to
		 * modify the behavior.
		 *
		 * @return <code>True</code> to execute the runnable asynchronously, <code>false</code> otherwise.
		 */
		public boolean isExecuteAsync() { return true; }
	}

	/**
     * Constructor
     */
    public TerminalService() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.terminal.view.core.interfaces.ITerminalService#addTerminalTabListener(org.eclipse.tm.terminal.view.core.interfaces.ITerminalTabListener)
     */
    @Override
    public final void addTerminalTabListener(ITerminalTabListener listener) {
		Assert.isNotNull(listener);
		terminalTabListeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.terminal.view.core.interfaces.ITerminalService#removeTerminalTabListener(org.eclipse.tm.terminal.view.core.interfaces.ITerminalTabListener)
     */
    @Override
    public final void removeTerminalTabListener(ITerminalTabListener listener) {
		Assert.isNotNull(listener);
		terminalTabListeners.remove(listener);
    }

	/**
	 * Convenience method for notifying the registered terminal tab listeners.
	 *
	 * @param event The terminal tab event.
	 * @param source The disposed tab item. Must not be <code>null</code>.
	 * @param data The custom data object associated with the disposed tab item or <code>null</code>.
	 */
	public final void fireTerminalTabEvent(final int event, final Object source, final Object data) {
		Assert.isNotNull(source);

		// If no listener is registered, we are done here
		if (terminalTabListeners.isEmpty()) return;

		// Get the list or currently registered listeners
		Object[] l = terminalTabListeners.getListeners();
		// Loop the registered terminal tab listeners and invoke the proper method
		for (int i = 0; i < l.length; i++) {
			final ITerminalTabListener listener = (ITerminalTabListener) l[i];
			ISafeRunnable job = new ISafeRunnable() {
				@Override
				public void handleException(Throwable exception) {
					// already logged in Platform#run()
				}

				@Override
				public void run() throws Exception {
					switch (event) {
					case TAB_DISPOSED:
						listener.terminalTabDisposed(source, data);
						break;
					default:
					}
				}
			};
			SafeRunner.run(job);
		}
	}

	/**
	 * Executes the given runnable operation and invokes the given callback, if any,
	 * after the operation finished.
	 *
	 * @param properties The terminal properties. Must not be <code>null</code>.
	 * @param runnable The terminal service runnable. Must not be <code>null</code>.
	 * @param done The callback to invoke if the operation has been finished or <code>null</code>.
	 */
	protected final void executeServiceOperation(final Map<String, Object> properties, final TerminalServiceRunnable runnable, final Done done) {
		Assert.isNotNull(properties);
		Assert.isNotNull(runnable);

		// Extract the properties
		String id = (String)properties.get(ITerminalsConnectorConstants.PROP_ID);
		String secondaryId = (String)properties.get(ITerminalsConnectorConstants.PROP_SECONDARY_ID);
		String title = (String)properties.get(ITerminalsConnectorConstants.PROP_TITLE);
		Object data = properties.get(ITerminalsConnectorConstants.PROP_DATA);

		// Normalize the terminals console view id
		id = normalizeId(id, data);
		// Normalize the terminal console tab title
		title = normalizeTitle(title, data);

		// Create the terminal connector instance
		final ITerminalConnector connector = createTerminalConnector(properties);
		if (connector == null) {
			// Properties contain invalid connector arguments
			if (done != null) {
				Exception e = new IllegalArgumentException(Messages.TerminalService_error_cannotCreateConnector);
				done.done(new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(), e.getLocalizedMessage(), e));
			}
			return;
		}

		// Finalize the used variables
		final String finId = id;
		final String finSecondaryId = secondaryId;
		final String finTitle = title;
		final Object finData = data;

		// Execute the operation
		if (!runnable.isExecuteAsync()) {
			runnable.run(finId, finSecondaryId, finTitle, connector, finData, done);
		}
		else {
	        try {
	            Display display = PlatformUI.getWorkbench().getDisplay();
	            display.asyncExec(new Runnable() {
					@Override
	                public void run() {
						runnable.run(finId, finSecondaryId, finTitle, connector, finData, done);
					}
				});
	        }
	        catch (Exception e) {
	            // if display is disposed, silently ignore.
	        }
		}
	}

	/**
	 * Normalize the terminals view id.
	 *
	 * @param id The terminals view id or <code>null</code>.
	 * @param data The custom data object or <code>null</code>.
	 *
	 * @return The normalized terminals console view id.
	 */
	protected String normalizeId(String id, Object data) {
		return id != null ? id : IUIConstants.ID;
	}

	/**
	 * Normalize the terminal tab title.
	 *
	 * @param title The terminal tab title or <code>null</code>.
	 * @param data The custom data object or <code>null</code>.
	 *
	 * @return The normalized terminal tab title.
	 */
	protected String normalizeTitle(String title, Object data) {
		// If the title is explicitly specified, return as is
		if (title != null) return title;

		// Return the default console title in all other cases
		return Messages.TerminalService_defaultTitle;
	}

	/**
	 * Creates the terminal connector configured within the given properties.
	 *
	 * @param properties The terminal console properties. Must not be <code>null</code>.
	 * @return The terminal connector or <code>null</code>.
	 */
	protected ITerminalConnector createTerminalConnector(Map<String, Object> properties) {
		Assert.isNotNull(properties);

		// The terminal connector result object
		ITerminalConnector connector = null;

		// Get the launcher delegate id from the properties
		String delegateId = (String)properties.get(ITerminalsConnectorConstants.PROP_DELEGATE_ID);
		if (delegateId != null) {
			// Get the launcher delegate
			ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate(delegateId, false);
			if (delegate != null) {
				// Create the terminal connector
				connector = delegate.createTerminalConnector(properties);
			}
		}

		return connector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.core.interfaces.ITerminalService#openConsole(java.util.Map, org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done)
	 */
	@Override
    public void openConsole(final Map<String, Object> properties, final Done done) {
		Assert.isNotNull(properties);
		final boolean restoringView = fRestoringView;

		executeServiceOperation(properties, new TerminalServiceRunnable() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void run(final String id, final String secondaryId, final String title,
							final ITerminalConnector connector, final Object data, final Done done) {
				if (restoringView) {
					doRun(id, secondaryId, title, connector, data, done);
				} else {
					// First, restore the view. This opens consoles from the memento
					fRestoringView = true;
					ConsoleManager.getInstance().showConsoleView(id, secondaryId);
					fRestoringView = false;

					// After that schedule opening the requested console
			        try {
			            Display display = PlatformUI.getWorkbench().getDisplay();
			            display.asyncExec(new Runnable() {
							@Override
							public void run() {
								doRun(id, secondaryId, title, connector, data, done);
							}
						});
			        }
			        catch (Exception e) {
			            // if display is disposed, silently ignore.
			        }
				}
			}

			public void doRun(String id, String secondaryId, String title, ITerminalConnector connector, Object data, Done done) {
				// Determine the terminal encoding
				String encoding = (String)properties.get(ITerminalsConnectorConstants.PROP_ENCODING);
				// Create the flags to pass on to openConsole
				Map<String, Boolean> flags = new HashMap<String, Boolean>();
				flags.put("activate", Boolean.TRUE); //$NON-NLS-1$
				if (properties.get(ITerminalsConnectorConstants.PROP_FORCE_NEW) instanceof Boolean) {
					flags.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, (Boolean)properties.get(ITerminalsConnectorConstants.PROP_FORCE_NEW));
				}
				if (properties.get(ITerminalsConnectorConstants.PROP_DATA_NO_RECONNECT) instanceof Boolean) {
					flags.put(ITerminalsConnectorConstants.PROP_DATA_NO_RECONNECT, (Boolean)properties.get(ITerminalsConnectorConstants.PROP_DATA_NO_RECONNECT));
				}
				// Open the new console
				CTabItem item;
				if (secondaryId != null)
					item = ConsoleManager.getInstance().openConsole(id, secondaryId, title, encoding, connector, data, flags);
				else
					item = ConsoleManager.getInstance().openConsole(id, title, encoding, connector, data, flags);
				// Associate the original terminal properties with the tab item.
				// This makes it easier to persist the connection data within the memento handler
				if (item != null && !item.isDisposed()) item.setData("properties", properties); //$NON-NLS-1$

				// Invoke the callback
				if (done != null) done.done(Status.OK_STATUS);
			}
		}, done);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.core.interfaces.ITerminalService#closeConsole(java.util.Map, org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done)
	 */
	@Override
    public void closeConsole(final Map<String, Object> properties, final Done done) {
		Assert.isNotNull(properties);

		executeServiceOperation(properties, new TerminalServiceRunnable() {
			@Override
			public void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, Done done) {
				// Close the console
				ConsoleManager.getInstance().closeConsole(id, title, connector, data);
				// Invoke the callback
				if (done != null) done.done(Status.OK_STATUS);
			}
		}, done);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.terminal.view.core.interfaces.ITerminalService#terminateConsole(java.util.Map, org.eclipse.tm.terminal.view.core.interfaces.ITerminalService.Done)
	 */
	@Override
	public void terminateConsole(Map<String, Object> properties, Done done) {
		Assert.isNotNull(properties);

		executeServiceOperation(properties, new TerminalServiceRunnable() {
			@Override
			public void run(String id, String secondaryId, String title, ITerminalConnector connector, Object data, Done done) {
				// Close the console
				ConsoleManager.getInstance().terminateConsole(id, title, connector, data);
				// Invoke the callback
				if (done != null) done.done(Status.OK_STATUS);
			}
		}, done);
	}
}
