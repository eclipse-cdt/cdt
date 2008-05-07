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

import org.eclipse.rse.services.clientserver.IClientServerConstants;

/**
 * Exception thrown when an operation was requested on a given remote element,
 * but that element did not exist. Like trying to delete a file that does not
 * exist. The framework may treat such an exception differently than other kinds
 * of exceptions.
 * 
 * @since 3.0
 */
public class SystemElementNotFoundException extends SystemMessageException {
	/**
	 * A serialVersionUID is recommended for all serializable classes. This
	 * trait is inherited from Throwable. This should be updated if there is a
	 * schema change for this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param element an Absolute Path for the element that could not be found.
	 */
	public SystemElementNotFoundException(String element, String operation) {
		super(getMyMessage(element, operation));
	}

	private static SystemMessage getMyMessage(String element, String operation) {
		//TODO generate an internal backtrace and attach to the message
		String msgText = NLS.bind(CommonMessages.MSG_ELEMENT_NOT_FOUND, operation, element);
		SystemMessage msg = new SimpleSystemMessage(IClientServerConstants.PLUGIN_ID,
				ICommonMessageIds.MSG_ELEMENT_NOT_FOUND, IStatus.ERROR, msgText);
		return msg;
	}

}
