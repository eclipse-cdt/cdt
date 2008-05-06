/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * David McKnight   (IBM)        - [220547] [api][breaking] SimpleSystemMessage needs to specify a message id and some messages should be shared
 * Martin Oberhuber (Wind River) - [226374] Derived from RemoteFileSecurityException
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

/**
 * Exception thrown when attempting an operation and it fails for security
 * reasons. The original remote system's security message is always embedded and
 * retrievable via getRemoteException().
 *
 * @since 3.0
 */
public class SystemRemoteSecurityException extends SystemRemoteMessageException {
	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with plugin ID and operation being performed.
	 *
	 * @param pluginId ID of the plugin which detected the security violation
	 * @param operationPerformed element or operation which could not be
	 * 		accessed due to security restriction
	 * @param remoteException the initial cause of this exception
	 */
	public SystemRemoteSecurityException(String pluginId, String operationPerformed, Exception remoteException) {
		super(getMyMessage(pluginId, operationPerformed, remoteException), remoteException);
	}

	private static SystemMessage getMyMessage(String pluginId, String operationPerformed, Exception remoteException) {

		String msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FAILED, operationPerformed);

		String detailMessage = remoteException.getMessage();
		if (detailMessage == null) {
			detailMessage = remoteException.getClass().getName();
		}
		String secondLevel = null;
		Throwable cause = remoteException.getCause();
		if (cause != null) {
			secondLevel = cause.getMessage();
			if (secondLevel == null) {
				secondLevel = cause.getClass().getName();
				if (secondLevel.equals(detailMessage)) {
					secondLevel = null;
				}
			}
		}
		if (secondLevel != null) {
			// FIXME Use MessageFormat
			detailMessage = detailMessage + " : " + secondLevel; //$NON-NLS-1$
		}
		SystemMessage msg = new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_OPERATION_SECURITY_VIOLATION, IStatus.ERROR, msgTxt, secondLevel);
		return msg;
	}


}
