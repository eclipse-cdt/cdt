/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import org.eclipse.rse.services.clientserver.IClientServerConstants;

/**
 * Exception thrown in case of errors due to network I/O Problems.
 *
 * @since 3.0
 */
public class SystemNetworkIOException extends SystemRemoteMessageException {
	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor.
	 * Clients are encouraged to use the more specific constructor with pluginId instead of this one.
	 *
	 *
	 * @param remoteException exception from communication layer.
	 */
	public SystemNetworkIOException(Exception remoteException) {
		super(getMyMessage(IClientServerConstants.PLUGIN_ID, remoteException), remoteException);
	}

	/**
	 * Constructor with a plugin ID and exception cause.
	 *
	 * @param pluginId Plugin ID that caused the error.
	 * @param remoteException exception from communication layer.
	 */
	public SystemNetworkIOException(String pluginId, Exception remoteException) {
		super(getMyMessage(pluginId, remoteException), remoteException);
	}

	private static SystemMessage getMyMessage(String pluginId, Exception remoteException) {

		String message = remoteException.getMessage();
		if (message == null) {
			message = remoteException.getClass().getName();
		} else {
			// TODO Most remote messages like IOException don't have a message
			// that's understandable without the class type. For full translated
			// messages, code like in the Eclipse Team/CVS provider would need
			// to translate into readable messages. See also
			// o.e.tm.terminal.ssh/SshMessages#getMessageFor(Throwable).
			message = remoteException.getClass().getName() + ": " + message; //$NON-NLS-1$
		}
		String msgTxt = NLS.bind(CommonMessages.MSG_COMM_NETWORK_ERROR, message);

		String secondLevel = null;
		Throwable cause = remoteException.getCause();
		if (cause != null) {
			secondLevel = cause.getMessage();
			if (secondLevel == null) {
				secondLevel = cause.getClass().getName();
				if (secondLevel.equals(message)) {
					secondLevel = null;
				}
			}
		}
		SystemMessage msg = new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_COMM_NETWORK_ERROR, IStatus.ERROR, msgTxt, secondLevel);
		return msg;
	}

}
