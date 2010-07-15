/*******************************************************************************
 * Copyright (c) 2006 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * STMicroelectronics - Process console enhancements
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.ui.console.actions;

import org.eclipse.osgi.util.NLS;

public class MiConsoleMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.mi.ui.console.actions.MiConsoleMessages"; //$NON-NLS-1$

	public static String saveActionTooltip;
	public static String verboseActionTooltip;
	
	public static String confirmOverWrite;
	public static String infoIOError;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, MiConsoleMessages.class);
	}

}
