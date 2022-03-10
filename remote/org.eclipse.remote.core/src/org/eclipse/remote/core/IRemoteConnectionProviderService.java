/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

/**
 * A connection type service for connection types that have automatic means of adding
 * and removing services. For example, the Local connection type needs to be able
 * to ensure the Local connection is created, or adapters to other target management
 * systems may prefer to let those systems manage the connections.
 *
 * @since 2.0
 */
public interface IRemoteConnectionProviderService extends IRemoteConnectionType.Service {

	/**
	 * Initialize the service. Called after all existing connections are loaded.
	 * This method typically will add the initial connections or start up a job to do it.
	 * This method is called on startup, it's important that it be fast.
	 */
	void init();

}
