/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.debuggerconsole;

import org.eclipse.osgi.util.NLS;

public class ConsoleMessages extends NLS {
	public static String ConsoleMessages_no_console;
	public static String ConsoleDropDownAction_name;
	public static String ConsoleDropDownAction_description;

	static {
		// initialize resource bundle
		NLS.initializeMessages(ConsoleMessages.class.getName(), ConsoleMessages.class);
	}

	private ConsoleMessages() {
	}
}
