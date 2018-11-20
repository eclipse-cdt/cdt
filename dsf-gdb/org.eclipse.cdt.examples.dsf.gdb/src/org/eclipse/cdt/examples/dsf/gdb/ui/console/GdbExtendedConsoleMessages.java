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
package org.eclipse.cdt.examples.dsf.gdb.ui.console;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class GdbExtendedConsoleMessages extends NLS {
	public static String Request_Thread_Info;
	public static String Request_Thread_Info_Tip;
	public static String Set_Special_Background;
	public static String Set_Special_Background_Tip;

	static {
		// initialize resource bundle
		NLS.initializeMessages(GdbExtendedConsoleMessages.class.getName(), GdbExtendedConsoleMessages.class);
	}

	private GdbExtendedConsoleMessages() {
	}
}
