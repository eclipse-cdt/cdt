/*******************************************************************************
 * Copyright (c) 2012 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	static {
		// initialize resource bundle
		NLS.initializeMessages( Messages.class.getName(), Messages.class );
	}

	private Messages() {
	}
}
