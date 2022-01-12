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
package org.eclipse.tm.terminal.connector.remote.launcher;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.terminal.connector.remote.IRemoteSettings;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.ui.IMemento;

/**
 * Telnet terminal connection memento handler implementation.
 */
public class RemoteMementoHandler implements IMementoHandler {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler#saveState(org.eclipse.ui.IMemento, java.util.Map)
	 */
	@Override
	public void saveState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		// Do not write the terminal title to the memento -> needs to
		// be recreated at the time of restoration.
		memento.putString(IRemoteSettings.CONNECTION_NAME, (String) properties.get(IRemoteSettings.CONNECTION_NAME));
		memento.putString(IRemoteSettings.CONNECTION_TYPE_ID,
				(String) properties.get(IRemoteSettings.CONNECTION_TYPE_ID));
		memento.putString(ITerminalsConnectorConstants.PROP_ENCODING,
				(String) properties.get(ITerminalsConnectorConstants.PROP_ENCODING));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler#restoreState(org.eclipse.ui.IMemento, java.util.Map)
	 */
	@Override
	public void restoreState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		// Restore the terminal properties from the memento
		properties.put(IRemoteSettings.CONNECTION_NAME, memento.getString(IRemoteSettings.CONNECTION_NAME));
		properties.put(IRemoteSettings.CONNECTION_TYPE_ID, memento.getString(IRemoteSettings.CONNECTION_TYPE_ID));
		properties.put(ITerminalsConnectorConstants.PROP_ENCODING,
				memento.getString(ITerminalsConnectorConstants.PROP_ENCODING));
	}
}
