/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.util.EventListener;

/**
 * Listener used to register for notification of connection status changes. Clients should register a listener using the
 * {@link IRemoteConnection#addConnectionChangeListener(IRemoteConnectionChangeListener)} method.
 */
public interface IRemoteConnectionChangeListener extends EventListener {

	/**
	 * Notifies this listener that the status of a connection has changed.
	 * 
	 * @param event
	 *            the connection change event
	 */

	public void connectionChanged(IRemoteConnectionChangeEvent event);
}
