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

/**
 * Exception thrown when an operation is requested from a Service that is not
 * supported by that service.
 * 
 * @since 3.0
 */
public class SystemUnsupportedOperationException extends SystemMessageException {

	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param pluginId ID of the plugin not supporting requested operation.
	 * @param operation The operation being requested
	 */
	public SystemUnsupportedOperationException(String pluginId, String operation) {
		super(getMyMessage(pluginId, operation));
	}

	private static SystemMessage getMyMessage(String pluginId, String operation) {

		String msgTxt = NLS.bind(CommonMessages.MSG_OPERATION_UNSUPPORTED, operation);
		SystemMessage msg = new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_OPERATION_UNSUPPORTED, IStatus.ERROR, msgTxt);
		return msg;
	}

}
