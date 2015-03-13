/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.terminal.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class TerminalViewConnectionManager implements ITerminalViewConnectionManager {
	private static final String STORE_CONNECTION_PREFIX = "connection"; //$NON-NLS-1$
	private static final String STORE_SIZE = "size"; //$NON-NLS-1$
	private static final String STORE_ACTIVE_CONNECTION = "active"; //$NON-NLS-1$
	/**
	 * The list of {@link ITerminalViewConnection} in the order they were cerated.
	 * Ordered by creation time
	 */
	private final List<ITerminalViewConnection> fConnections = new ArrayList<>();
	/**
	 * The currently displayed connection
	 */
	private ITerminalViewConnection fActiveConnection;
	/**
	 * The list of {@link ITerminalViewConnection} in the order they
	 * were made the active connection. The most recently accessed
	 * connection is at the beginning of the list.
	 */
	private final List<ITerminalViewConnection> fConnectionHistory = new ArrayList<>();
	/**
	 * The {@link ITerminalViewConnectionListener}
	 */
	private final List<ITerminalViewConnectionListener> fListeners = new ArrayList<>();

	@Override
	public ITerminalViewConnection[] getConnections() {
		return fConnections.toArray(new ITerminalViewConnection[fConnections.size()]);
	}

	@Override
	public int size() { // TODO Auto-generated method stub
		return fConnections.size();
	}

	@Override
	public ITerminalViewConnection getActiveConnection() {
		return fActiveConnection;
	}

	@Override
	public void setActiveConnection(ITerminalViewConnection conn) {
		fActiveConnection = conn;
		// put the connection at the end of the history list
		fConnectionHistory.remove(conn);
		fConnectionHistory.add(0, conn);

		fireListeners();
	}

	@Override
	public void swapConnection() {
		ITerminalViewConnection conn = getPreviousConnection();
		if (conn != null) {
			setActiveConnection(conn);
		}
	}

	/**
	 * @return the connection that was most recently the active connection or null if there is
	 *         no previous connection
	 */
	private ITerminalViewConnection getPreviousConnection() {
		// find the first connection that is not the active connection in
		// the list
		for (Object element : fConnectionHistory) {
			ITerminalViewConnection conn = (ITerminalViewConnection) element;
			if (conn != fActiveConnection) {
				return conn;
			}
		}
		return null;
	}

	@Override
	public void addConnection(ITerminalViewConnection conn) {
		fConnections.add(conn);
		fireListeners();
	}

	public void removeConnection(ITerminalViewConnection conn) {
		fConnections.remove(conn);
		fConnectionHistory.remove(conn);
		fireListeners();
	}

	@Override
	public void addListener(ITerminalViewConnectionListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeListener(ITerminalViewConnectionListener listener) {
		fListeners.remove(listener);
	}

	protected void fireListeners() {
		ITerminalViewConnectionListener[] listeners = fListeners.toArray(new ITerminalViewConnectionListener[fListeners.size()]);
		for (ITerminalViewConnectionListener listener : listeners) {
			listener.connectionsChanged();
		}
	}

	@Override
	public void saveState(ISettingsStore store) {
		store.put(STORE_SIZE, "" + fConnections.size()); //$NON-NLS-1$
		// save all connections
		int n = 0;
		for (Object element : fConnections) {
			ITerminalViewConnection connection = (ITerminalViewConnection) element;
			// the name under which we store the connection
			String prefix = STORE_CONNECTION_PREFIX + n;
			n++;
			// remember the active connection by its prefix
			if (connection.equals(fActiveConnection)) {
				store.put(STORE_ACTIVE_CONNECTION, prefix);
			}
			connection.saveState(new SettingStorePrefixDecorator(store, prefix));
		}
	}

	@Override
	public void loadState(ISettingsStore store, ITerminalViewConnectionFactory factory) {
		int size = 0;
		try {
			size = Integer.parseInt(store.get(STORE_SIZE));
		} catch (Exception e) {
			// ignore
		}
		if (size > 0) {
			// a slot for the connections
			String active = store.get(STORE_ACTIVE_CONNECTION);
			int n = 0;
			for (int i = 0; i < size; i++) {
				// the name under which we stored the connection
				String prefix = STORE_CONNECTION_PREFIX + n;
				n++;
				try {
					ITerminalViewConnection connection = factory.create();
					fConnections.add(connection);
					fConnectionHistory.add(connection);
					if (prefix.equals(active)) {
						fActiveConnection = connection;
					}
					connection.loadState(new SettingStorePrefixDecorator(store, prefix));
				} catch (RuntimeException e) {
					// in case something goes wrong, make sure we can read the other
					// connections....
					TerminalViewPlugin
							.getDefault()
							.getLog()
							.log(new Status(IStatus.WARNING, TerminalViewPlugin.getDefault().getBundle().getSymbolicName(), 0, e
									.getLocalizedMessage(), e));
				}
			}
		}
	}

	@Override
	public void removeActive() {
		// don't remove the last connection -- we need at least one!
		if (fConnections.size() > 1) {
			fConnections.remove(fActiveConnection);
			fConnectionHistory.remove(fActiveConnection);

			// make sure connection is not null....
			fActiveConnection = getPreviousConnection();
			// if there is no previous connection then make
			// the first connection the list the active connection
			if (fActiveConnection == null) {
				fActiveConnection = fConnections.get(0);
			}

			fireListeners();
		}
	}
}
