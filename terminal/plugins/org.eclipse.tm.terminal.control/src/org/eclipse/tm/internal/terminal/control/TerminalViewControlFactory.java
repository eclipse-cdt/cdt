/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [378691][api] push Preferences into the Widget
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.delegates.TerminalViewControlTmDelegate;
import org.eclipse.tm.internal.delegates.TmTerminalConnectorDelegate;
import org.eclipse.tm.internal.delegates.TmTerminalListenerDelegate;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;

public class TerminalViewControlFactory {
	/**
	 * Instantiate a Terminal widget.
	 * @param target Callback for notifying the owner of Terminal state changes.
	 * @param wndParent The Window parent to embed the Terminal in.
	 * @param connectors Provided connectors.
	 */
	public static ITerminalViewControl makeControl(ITerminalListener target, Composite wndParent,
			ITerminalConnector[] connectors) {
		return makeControl(target, wndParent, connectors, false);
	}

	/**
	 * Instantiate a Terminal widget.
	 * @param target Callback for notifying the owner of Terminal state changes.
	 * @param wndParent The Window parent to embed the Terminal in.
	 * @param connectors Provided connectors.
	 * @param useCommonPrefs If <code>true</code>, the Terminal widget will pick up settings
	 *    from the <code>org.eclipse.tm.terminal.TerminalPreferencePage</code> Preference page.
	 *    Otherwise, clients need to maintain settings themselves.
	 * @since 3.2
	 */
	public static ITerminalViewControl makeControl(ITerminalListener target, Composite wndParent,
			ITerminalConnector[] connectors, boolean useCommonPrefs) {
		org.eclipse.terminal.connector.ITerminalConnector[] delegateConnectors;
		if (connectors == null) {
			delegateConnectors = null;
		} else {
			delegateConnectors = Arrays.stream(connectors).map(c -> new TmTerminalConnectorDelegate(c))
					.toArray(org.eclipse.terminal.connector.ITerminalConnector[]::new);
		}
		org.eclipse.terminal.control.ITerminalViewControl control = org.eclipse.terminal.control.TerminalViewControlFactory
				.makeControl(new TmTerminalListenerDelegate(target), wndParent, delegateConnectors);
		return new TerminalViewControlTmDelegate(control);
	}

}
