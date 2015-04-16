/*******************************************************************************
 * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces.tracing;

/**
 * Core plug-in trace slot identifiers.
 */
public interface ITraceIds {

	/**
	 * If activated, tracing information about the terminals output stream monitor is printed out.
	 */
	public static final String TRACE_OUTPUT_STREAM_MONITOR = "trace/outputStreamMonitor"; //$NON-NLS-1$

	/**
	 * If activated, tracing information about the launch terminal command handler is printed out.
	 */
	public static final String TRACE_LAUNCH_TERMINAL_COMMAND_HANDLER = "trace/launchTerminalCommandHandler"; //$NON-NLS-1$
}
