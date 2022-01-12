/*******************************************************************************
 * Copyright (c) 2015, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces;

/**
 * A container to deal with configuration panels.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
