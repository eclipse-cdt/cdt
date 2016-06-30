/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.terminal.connector.telnet.connector;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITelnetSettings {
	/**
	 * @since 4.2
	 */
	static final String EOL_CRNUL = "CR+NUL"; //$NON-NLS-1$
	/**
	 * @since 4.2
	 */
	static final String EOL_CRLF = "CR+LF"; //$NON-NLS-1$

	String getHost();
	int getNetworkPort();
	int getTimeout();
	/**
	 * @since 4.2
	 */
	String getEndOfLine();
	String getSummary();
	void load(ISettingsStore store);
	void save(ISettingsStore store);
}
