/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [227135] Cryptic exception when sftp-server is missing
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.messages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import org.eclipse.rse.services.clientserver.IClientServerConstants;

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
		super(getMyMessage(IClientServerConstants.PLUGIN_ID, null, remoteException), remoteException);
	}

	/**
	 * Constructor with plugin ID and plain text failure information. Clients
	 * are encouraged to use the more specific constructor with pluginId and
	 * remoteException instead of this one.
	 *
	 * @param msg message about failed operation
	 */
	public SystemOperationFailedException(String pluginId, String msg) {
		super(getMyMessage(pluginId, msg, null), null);
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
	 * Constructor with plugin ID and user message.
	 *
	 * @param pluginId ID of the plugin issuing the message.
	 * @param userMessage user-readable message detailing cause of the
	 *            exception, or <code>null</code>. If not specified, a generic
	 *            message will be used ("Operation failed with exception").
	 * @param remoteException the initial cause of this exception. Will be added
	 *            to the user message.
	 */
	public SystemOperationFailedException(String pluginId, String userMessage, Exception remoteException) {
		super(getMyMessage(pluginId, userMessage, remoteException), remoteException);
	}

	private static SystemMessage getMyMessage(String pluginId, String userMessage, Exception remoteException) {

		String exceptionMessage = null;
		String secondLevel = null;
		if (remoteException != null) {
			exceptionMessage = remoteException.getMessage();
			if (exceptionMessage == null) {
				exceptionMessage = remoteException.getClass().getName();
			}
			Throwable cause = remoteException.getCause();
			if (cause != null) {
				secondLevel = cause.getMessage();
				if (secondLevel == null) {
					secondLevel = cause.getClass().getName();
					if (secondLevel.equals(exceptionMessage)) {
						secondLevel = null;
					}
				}
			}
		}
		String msgTxt = userMessage;
		if (msgTxt == null) {
			// no user text -- use standard message with (non-localized)
			// exception message + second level
			msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_FAILED, exceptionMessage);
		} else if (secondLevel == null) {
			// user text but no second level -- move exception text to 2nd level
			secondLevel = exceptionMessage;
		} else {
			// user text, exception and second level -- concatenate user text
			// and exception
			msgTxt = NLS.bind(CommonMessages.MSG_FAILURE_WITH_CAUSE, userMessage, exceptionMessage);
		}

		SystemMessage msg = new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_OPERATION_FAILED,
				IStatus.ERROR, msgTxt, secondLevel);
		return msg;
	}

}
