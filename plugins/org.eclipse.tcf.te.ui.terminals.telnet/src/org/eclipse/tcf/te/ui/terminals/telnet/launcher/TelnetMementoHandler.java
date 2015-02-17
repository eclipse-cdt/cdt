/*******************************************************************************
 * Copyright (c) 2012, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.telnet.launcher;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.tcf.te.core.terminals.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tcf.te.ui.terminals.interfaces.IMementoHandler;
import org.eclipse.ui.IMemento;

/**
 * Telnet terminal connection memento handler implementation.
 */
public class TelnetMementoHandler implements IMementoHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IMementoHandler#saveState(org.eclipse.ui.IMemento, java.util.Map)
	 */
	@Override
	public void saveState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		// Do not write the terminal title to the memento -> needs to
		// be recreated at the time of restoration.
		memento.putString(ITerminalsConnectorConstants.PROP_IP_HOST, (String)properties.get(ITerminalsConnectorConstants.PROP_IP_HOST));
		Object value = properties.get(ITerminalsConnectorConstants.PROP_IP_PORT);
		memento.putInteger(ITerminalsConnectorConstants.PROP_IP_PORT, value instanceof Integer ? ((Integer)value).intValue() : -1);
		value = properties.get(ITerminalsConnectorConstants.PROP_TIMEOUT);
		memento.putInteger(ITerminalsConnectorConstants.PROP_TIMEOUT, value instanceof Integer ? ((Integer)value).intValue() : -1);
		memento.putString(ITerminalsConnectorConstants.PROP_ENCODING, (String)properties.get(ITerminalsConnectorConstants.PROP_ENCODING));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IMementoHandler#restoreState(org.eclipse.ui.IMemento, java.util.Map)
	 */
	@Override
	public void restoreState(IMemento memento, Map<String, Object> properties) {
		Assert.isNotNull(memento);
		Assert.isNotNull(properties);

		// Restore the terminal properties from the memento
		properties.put(ITerminalsConnectorConstants.PROP_IP_HOST, memento.getString(ITerminalsConnectorConstants.PROP_IP_HOST));
		properties.put(ITerminalsConnectorConstants.PROP_IP_PORT, memento.getInteger(ITerminalsConnectorConstants.PROP_IP_PORT));
		properties.put(ITerminalsConnectorConstants.PROP_TIMEOUT, memento.getInteger(ITerminalsConnectorConstants.PROP_TIMEOUT));
		properties.put(ITerminalsConnectorConstants.PROP_ENCODING, memento.getString(ITerminalsConnectorConstants.PROP_ENCODING));
	}
}
