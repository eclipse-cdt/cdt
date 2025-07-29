/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Uwe Stieber (Wind River) - [282996] [terminal][api] Add "hidden" attribute to terminal connector extension point
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.provisional.api;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.tm.internal.delegates.TerminalConnectorTmDelegate;

/**
 * A factory to get {@link ITerminalConnector} instances.
 *
 * @author Michael Scharf
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the <a href="http://www.eclipse.org/tm/">Target Management</a>
 * team.
 * </p>
 */
public class TerminalConnectorExtension {

	/**
	 * Return a specific terminal connector for a given connector id. The
	 * terminal connector is not yet instantiated to any real connection.
	 *
	 * @param id the id of the terminal connector in the
	 *            <code>org.eclipse.tm.terminal.control.connectors</code>
	 *            extension point
	 * @return a new ITerminalConnector with id or <code>null</code> if there
	 *         is no extension with that id.
	 * @since org.eclipse.tm.terminal 2.0
	 */
	public static ITerminalConnector makeTerminalConnector(String id) {

		org.eclipse.terminal.connector.ITerminalConnector terminalConnector;
		try {
			terminalConnector = org.eclipse.terminal.connector.TerminalConnectorExtension.makeTerminalConnector(id);
		} catch (CoreException e) {
			return null;
		}
		if (terminalConnector == null) {
			return null;
		}

		return new TerminalConnectorTmDelegate(terminalConnector);
	}

	/**
	 * Return a list of available terminal connectors (connection types).
	 *
	 * The terminal connectors returned are not yet instantiated to any real
	 * connection. Each terminal connector can connect to one remote system at a
	 * time.
	 *
	 * @return a new list of {@link ITerminalConnector} instances defined in the
	 *         <code>org.eclipse.tm.terminal.control.connectors</code>
	 *         extension point
	 * @since org.eclipse.tm.terminal 2.0 return value is ITerminalConnector[]
	 */
	public static ITerminalConnector[] makeTerminalConnectors() {

		org.eclipse.terminal.connector.ITerminalConnector[] connectors = org.eclipse.terminal.connector.TerminalConnectorExtension
				.makeTerminalConnectors();
		return Arrays.stream(connectors).map(TerminalConnectorTmDelegate::new).toArray(ITerminalConnector[]::new);
	}

}
