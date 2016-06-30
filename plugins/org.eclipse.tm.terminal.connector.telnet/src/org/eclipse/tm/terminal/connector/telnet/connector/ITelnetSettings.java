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

public interface ITelnetSettings {
	static final String EOL_CRNUL = "CR+NUL"; //$NON-NLS-1$
	static final String EOL_CRLF = "CR+LF"; //$NON-NLS-1$

	String getHost();
	int getNetworkPort();
	int getTimeout();
	String getEndOfLine();
	String getSummary();
	void load(ISettingsStore store);
	void save(ISettingsStore store);
}
