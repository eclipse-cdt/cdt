/*******************************************************************************
 * Copyright (c) 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;



/**
 * A container to deal with configuration panels.
 */
public interface IConfigurationPanelContainer {

	/**
	 * Validates the container status.
	 * <p>
	 * If necessary, set the corresponding messages and message types to signal when some sub
	 * elements of the container needs user attention.
	 */
	public void validate();

	/**
	 * Set the message and the message type to display.
	 *
	 * @param message The message or <code>null</code>.
	 * @param messageType The message type or <code>IMessageProvider.NONE</code>.
	 */
	public void setMessage(String message, int messageType);

}
