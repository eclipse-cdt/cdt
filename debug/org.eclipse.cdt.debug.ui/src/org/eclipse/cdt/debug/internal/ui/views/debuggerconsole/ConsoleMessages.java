/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
