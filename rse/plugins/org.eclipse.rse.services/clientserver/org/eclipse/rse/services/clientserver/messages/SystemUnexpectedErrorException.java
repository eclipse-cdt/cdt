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

/**
 * Exception thrown when the cause for a problem can not be determined.
 * 
 * This class should be used rarely, because it's always better to tell users as
 * explicitly as possible what went wrong. It's usually used as a temporary
 * workaround while the time for finding a proper error message is not
 * available.
 * 
 * @since 3.0
 */
public class SystemUnexpectedErrorException extends SystemMessageException {

	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor.
	 *
	 * @param pluginId ID of the plugin in which the unexpected error was
	 * 		cought.
	 */
	public SystemUnexpectedErrorException(String pluginId) {
		super(getMyMessage(pluginId));
	}

	private static SystemMessage getMyMessage(String pluginId) {
		return new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_ERROR_UNEXPECTED, IStatus.ERROR, CommonMessages.MSG_ERROR_UNEXPECTED);
	}

}
