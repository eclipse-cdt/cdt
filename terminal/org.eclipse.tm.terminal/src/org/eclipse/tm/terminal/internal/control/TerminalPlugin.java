/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/

package org.eclipse.tm.terminal.internal.control;

import org.eclipse.core.runtime.Platform;
import org.eclipse.tm.terminal.Logger;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TerminalPlugin extends AbstractUIPlugin {
	protected static TerminalPlugin fDefault;
	public static final String  PLUGIN_HOME  = "org.eclipse.tm.terminal"; //$NON-NLS-1$
	public static final String  HELP_VIEW  = PLUGIN_HOME + ".terminal_view"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public TerminalPlugin() {
		fDefault = this;
	}
	/**
	 * Returns the shared instance.
	 */
	public static TerminalPlugin getDefault() {
		return fDefault;
	}

	public static boolean isLogInfoEnabled() {
		return isOptionEnabled(Logger.TRACE_DEBUG_LOG_INFO);
	}
	public static boolean isLogErrorEnabled() {
		return isOptionEnabled(Logger.TRACE_DEBUG_LOG_ERROR);
	}
	public static boolean isLogEnabled() {
		return isOptionEnabled(Logger.TRACE_DEBUG_LOG);
	}

	public static boolean isOptionEnabled(String strOption) {
		String strEnabled = Platform.getDebugOption(strOption);
		if (strEnabled == null)
			return false;

		return new Boolean(strEnabled).booleanValue();
	}
}
