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
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String ReverseDebugging_Error;
	public static String ReverseDebugging_UndefinedTraceMethod;
	public static String ReverseDebugging_ToggleHardwareTrace;
	public static String ReverseDebugging_ToggleSoftwareTrace;
	public static String ReverseDebugging_ToggleReverseDebugging;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
