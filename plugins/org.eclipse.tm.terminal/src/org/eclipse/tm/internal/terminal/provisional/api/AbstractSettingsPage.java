/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract settings page providing a common implementation of the listener handling.
 */
public abstract class AbstractSettingsPage implements ISettingsPage, IMessageProvider {
	// A message associated with the control.
	private String message = null;

	// The message type of the associated message.
	private int messageType = IMessageProvider.NONE;

	// Reference to the listener
	private final ListenerList listeners = new ListenerList();

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#addListener(org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage.Listener)
	 */
	public void addListener(Listener listener) {
    	Assert.isNotNull(listener);
    	listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage#removeListener(org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage.Listener)
	 */
	public void removeListener(Listener listener) {
    	Assert.isNotNull(listener);
    	listeners.remove(listener);
	}

	/**
	 * Fire the listeners for the given control.
	 *
	 * @param control The control or <code>null</code>.
	 */
	public void fireListeners(Control control) {
		Object[] list = listeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			Object l = list[i];
			if (!(l instanceof Listener)) continue;
			((Listener)l).onSettingsPageChanged(control);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	public final String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public final int getMessageType() {
		return messageType;
	}

	/**
	 * Set the message and the message type.
	 *
	 * @param message The message or <code>null</code>.
	 * @param messageType The type of the message (NONE, INFORMATION, WARNING, ERROR).
	 */
	public final void setMessage(String message, int messageType) {
		this.message = message;
		this.messageType = messageType;
	}
}
