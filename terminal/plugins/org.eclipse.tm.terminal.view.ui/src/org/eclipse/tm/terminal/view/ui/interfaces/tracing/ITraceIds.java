/*******************************************************************************
 * Copyright (c) 2011, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.interfaces.tracing;

/**
 * Core plug-in trace slot identifiers.
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
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
