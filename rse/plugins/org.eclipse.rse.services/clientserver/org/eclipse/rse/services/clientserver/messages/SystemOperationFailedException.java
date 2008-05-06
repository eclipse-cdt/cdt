/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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

import org.eclipse.rse.internal.services.Activator;

/**
 * Generic exception thrown when anything fails and a child exception is
 * available to provide exception details.
 * <p>
 * The original remote system's exception message is always embedded and
 * retrievable via getRemoteException().
 *
 * @since 3.0
 */
public class SystemOperationFailedException extends SystemRemoteMessageException {
	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor.
	 * Clients are encouraged to use the more specific constructor with pluginId and operationPerformed instead of this one.   
	 *
	 * @param remoteException the initial cause of this exception
	 */
	public SystemOperationFailedException(Exception remoteException) {
		super(getMyMessage(Activator.PLUGIN_ID, null, remoteException), remoteException);
	}

	/**
	 * Constructor with plugin ID.
	 * Clients are encouraged to use the more specific constructor with pluginId and operationPerformed instead of this one.   
	 *
	 * @param remoteException the initial cause of this exception
	 */
	public SystemOperationFailedException(String pluginId, Exception remoteException) {
		super(getMyMessage(pluginId, null, remoteException), remoteException);
	}

	/**
	 * Constructor with plugin ID and operation being performed.
	 *
	 * @param remoteException the initial cause of this exception
	 */
	public SystemOperationFailedException(String pluginId, String operationPerformed, Exception remoteException) {
		super(getMyMessage(pluginId, operationPerformed, remoteException), remoteException);
	}

	private static SystemMessage getMyMessage(String pluginId, String operationPerformed, Exception remoteException) {

		String message = remoteException.getMessage();
		if (message == null) {
			message = remoteException.getClass().getName();
		}
		String msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FAILED, message);

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
		if (operationPerformed != null) {
			// FIXME Use Java MessageFormat for better formatting
			secondLevel = (secondLevel != null) ? operationPerformed + " : " + secondLevel : operationPerformed; //$NON-NLS-1$
		}
		SystemMessage msg = new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_OPERATION_FAILED,
				IStatus.ERROR, msgTxt, secondLevel);
		return msg;
	}

}
