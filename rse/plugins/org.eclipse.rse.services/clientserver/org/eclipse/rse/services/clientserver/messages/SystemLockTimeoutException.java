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
 * Exception thrown when a remote operation requires an exclusive lock on some
 * resources, such as a shared Mutex for some channel, and a timeout occurs
 * acquiring that resource.
 *
 * @since 3.0
 */
public class SystemLockTimeoutException extends SystemMessageException {

	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * . * Default Constructor
	 * 
	 * @param pluginId ID of the plugin in which the problem occurred.
	 */
	public SystemLockTimeoutException(String pluginId) {
		super(getMyMessage(pluginId, null));
	}

	/**
	 * Constructor with a detail message.
	 * 
	 * @param pluginId ID of the plugin in which the problem occurred.
	 */
	public SystemLockTimeoutException(String pluginId, String detailMsg) {
		super(getMyMessage(pluginId, detailMsg));
	}

	private static SystemMessage getMyMessage(String pluginId, String detailMsg) {
		return new SimpleSystemMessage(pluginId, ICommonMessageIds.MSG_LOCK_TIMEOUT, IStatus.ERROR, CommonMessages.MSG_LOCK_TIMEOUT, detailMsg);
	}

}
