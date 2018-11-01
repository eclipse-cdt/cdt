/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [170910] Adopt RSE ITerminalService API for SSH
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh;

/**
 * Markup Interface for services using the SshConnectorService.
 *
 * By implementing this interface, services can be recognized
 * as operating against an SshConnectorService. The interface
 * is used as the key in a table for looking up the connector
 * service when needed.
 */
public interface ISshService {

	/**
	 * Get the Session Provider that cares for connect / disconnect of an SSH
	 * Session, and can be used to instantiate new Channels.
	 *
	 * @return an SSH Session Provider.
	 */
	public ISshSessionProvider getSessionProvider();

}
