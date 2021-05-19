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
package org.eclipse.tm.terminal.connector.local.launcher;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.IMementoHandler;
import org.eclipse.ui.IMemento;

/**
 * Local terminal connection memento handler implementation.
 */
public class LocalMementoHandler implements IMementoHandler {

	@Override
	public void saveState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		if ((String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_PATH) != null) {
			memento.putString(ITerminalsConnectorConstants.PROP_PROCESS_PATH,
					(String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_PATH));
		}
		if ((String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_ARGS) != null) {
			memento.putString(ITerminalsConnectorConstants.PROP_PROCESS_ARGS,
					(String) properties.get(ITerminalsConnectorConstants.PROP_PROCESS_ARGS));
		}
		if ((Boolean) properties.get(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE) != null) {
			memento.putBoolean(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE,
					(Boolean) properties.get(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE));
		}
	}

	@Override
	public void restoreState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		if (memento.getString(ITerminalsConnectorConstants.PROP_PROCESS_PATH) != null) {
			properties.put(ITerminalsConnectorConstants.PROP_PROCESS_PATH,
					memento.getString(ITerminalsConnectorConstants.PROP_PROCESS_PATH));
		}
		if (memento.getString(ITerminalsConnectorConstants.PROP_PROCESS_ARGS) != null) {
			properties.put(ITerminalsConnectorConstants.PROP_PROCESS_ARGS,
					memento.getString(ITerminalsConnectorConstants.PROP_PROCESS_ARGS));
		}
		if (memento.getBoolean(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE) != null) {
			properties.put(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE,
					memento.getBoolean(ITerminalsConnectorConstants.PROP_TRANSLATE_BACKSLASHES_ON_PASTE));
		}

	}
}
