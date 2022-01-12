/*******************************************************************************
 * Copyright (c) 2012, 2013 Mentor Graphics and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String GdbDebugNewExecutableCommand_Arguments;

	public static String GdbDebugNewExecutableCommand_Binary;

	public static String GdbDebugNewExecutableCommand_Binary_file_does_not_exist;

	public static String GdbDebugNewExecutableCommand_Binary_must_be_specified;

	public static String GdbDebugNewExecutableCommand_Binary_on_host;

	public static String GdbDebugNewExecutableCommand_Binary_on_target;

	public static String GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified;

	public static String GdbDebugNewExecutableCommand_Browse;

	public static String GdbDebugNewExecutableCommand_Debug_New_Executable;

	public static String GdbDebugNewExecutableCommand_Host_binary_file_does_not_exist;

	public static String GdbDebugNewExecutableCommand_Host_binary_must_be_specified;

	public static String GdbDebugNewExecutableCommand_Invalid_binary;

	public static String GdbDebugNewExecutableCommand_Invalid_host_binary;

	public static String GdbDebugNewExecutableCommand_New_Executable_Prompt_Job;

	public static String GdbDebugNewExecutableCommand_Select_binaries_on_host_and_target;

	public static String GdbDebugNewExecutableCommand_Select_Binary;

	public static String GdbDebugNewExecutableCommand_Select_binary_and_specify_arguments;

	public static String GdbReverseDebugging_HardwareReverseDebugNotAvailable;

	public static String GdbReverseDebugging_ProcessorTraceReverseDebugNotAvailable;

	public static String GdbReverseDebugging_ReverseDebugNotAvailable;

	public static String GdbConnectCommand_Error;

	public static String GdbConnectCommand_FailureMessage;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
