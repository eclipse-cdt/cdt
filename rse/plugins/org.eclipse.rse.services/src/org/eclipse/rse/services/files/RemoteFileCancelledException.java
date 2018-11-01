/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [216351] Improve cancellation of SystemFetchOperation for files
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [226374] [api] Need default SystemMessageException specialisations
 *******************************************************************************/

package org.eclipse.rse.services.files;

import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;

/**
 * Exception thrown when attempting a file operation and the user cancelled it
 * before it could be completed. Used to extend RemoteFileException before 3.0
 * 
 * @deprecated use SystemOperationCancelledException
 * @since 3.0
 */
public class RemoteFileCancelledException extends SystemOperationCancelledException {

	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public RemoteFileCancelledException() {
		super();
	}
}
