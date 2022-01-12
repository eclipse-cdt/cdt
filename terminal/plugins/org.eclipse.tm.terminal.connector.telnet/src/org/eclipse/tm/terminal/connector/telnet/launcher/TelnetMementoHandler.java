/*******************************************************************************
 * Copyright (c) 2012, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.launcher;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.ui.IMemento;

/**
 * Telnet terminal connection memento handler implementation.
 */
public class TelnetMementoHandler implements IMementoHandler {

	@Override
	public void saveState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		// Do not write the terminal title to the memento -> needs to
		// be recreated at the time of restoration.
		memento.putString(ITerminalsConnectorConstants.PROP_IP_HOST,
				(String) properties.get(ITerminalsConnectorConstants.PROP_IP_HOST));
		Object value = properties.get(ITerminalsConnectorConstants.PROP_IP_PORT);
		memento.putInteger(ITerminalsConnectorConstants.PROP_IP_PORT,
				value instanceof Integer ? ((Integer) value).intValue() : -1);
		value = properties.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		memento.putInteger(ITerminalsConnectorConstants.PROP_TIMEOUT,
				value instanceof Integer ? ((Integer) value).intValue() : -1);
		memento.putString(ITerminalsConnectorConstants.PROP_ENCODING,
				(String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING));
	}

	@Override
	public void restoreState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		// Restore the terminal properties from the memento
		properties.put(ITerminalsConnectorConstants.PROP_IP_HOST,
				memento.getString(ITerminalsConnectorConstants.PROP_IP_HOST));
		properties.put(ITerminalsConnectorConstants.PROP_IP_PORT,
				memento.getInteger(ITerminalsConnectorConstants.PROP_IP_PORT));
		properties.put(ITerminalsConnectorConstants.PROP_TIMEOUT,
				memento.getInteger(ITerminalsConnectorConstants.PROP_TIMEOUT));
		properties.put(ITerminalsConnectorConstants.PROP_ENCODING,
				memento.getString(ITerminalsConnectorConstants.PROP_ENCODING));
	}
}
