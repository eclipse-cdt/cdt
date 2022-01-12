/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.util.EventListener;

/**
 * Listener used to register for notification of connection status changes.
 * Listeners can be registered on individual connections using
 * {@link IRemoteConnection#addConnectionChangeListener(IRemoteConnectionChangeListener)},
 * or globally for all connections using
 * {@link IRemoteServicesManager#addRemoteConnectionChangeListener(IRemoteConnectionChangeListener)}.
 */
public interface IRemoteConnectionChangeListener extends EventListener {

	/**
	 * Notifies this listener that the status of a connection has changed.
	 *
	 * @param event
	 *            the connection change event
	 * @since 2.0
	 */

	public void connectionChanged(RemoteConnectionChangeEvent event);

}
