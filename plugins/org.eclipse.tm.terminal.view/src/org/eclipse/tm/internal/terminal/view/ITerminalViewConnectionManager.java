/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.view;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;


/**
 * Supports multiple connections
 *
 */
public interface ITerminalViewConnectionManager {
	/**
	 * Notifies any change in the state of the connections:
	 * <ul>
	 * <li> a connection is added or removed
	 * <li> the active connection has changed
	 * </ul>
	 *
	 */
	interface ITerminalViewConnectionListener {
		void connectionsChanged();
	}
	/**
	 * Used to create instances of the ITerminalViewConnection 
	 * when the state is read from the {@link ISettingsStore}
	 *
	 */
	interface ITerminalViewConnectionFactory {
		ITerminalViewConnection create();
	}
	/**
	 * @return a list of all connections this view can display
	 */
	ITerminalViewConnection[] getConnections();
	/**
	 * @return the number of connections
	 */
	int size();
	/**
	 * @return th connection the view is showing at the moment
	 */
	ITerminalViewConnection getActiveConnection();

	/**
	 * @param conn make this connection the active connection
	 */
	void setActiveConnection(ITerminalViewConnection conn);
	/**
	 * If more than two connections are available, remove the active connection
	 */
	void removeActive();

	/**
	 * @param conn adds a new connection
	 */
	void addConnection(ITerminalViewConnection conn);

	/**
	 * If there are more than two connections toggle between this and the
	 * previously shown connection
	 */
	void swapConnection();

	void addListener(ITerminalViewConnectionListener listener);
	void removeListener(ITerminalViewConnectionListener listener);
	
	void saveState(ISettingsStore store);
	/**
	 * @param store
	 * @param factory used to create new {@link ITerminalViewConnection}
	 */
	void loadState(ISettingsStore store,ITerminalViewConnectionFactory factory);
	
}