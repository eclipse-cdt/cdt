/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.application.messages"; //$NON-NLS-1$
	public static String ExecutablesView_ImportExecutables;
	public static String Debugger_Title;
	public static String GetCompilerOptions;
	public static String GetBuildOptions;
	public static String ProblemSavingWorkbench;
	public static String ProblemsSavingWorkspace;
	public static String InternalError;
	public static String InitializingDebugger;
	public static String ImportExecutable;
	public static String SetLanguageProviders;
	public static String RestorePreviousLaunch;
	public static String RemoveOldExecutable;
	public static String LaunchingConfig;
	public static String LaunchInterruptedError;
	public static String LaunchMissingError;
	public static String DebuggerInitializingProblem;
	
	public static String GdbDebugNewExecutableCommand_Arguments;
	public static String GdbDebugNewExecutableCommand_Binary;
	public static String GdbDebugNewExecutableCommand_Binary_file_does_not_exist;
	public static String GdbDebugNewExecutableCommand_Binary_must_be_specified;
	public static String GdbDebugNewExecutableCommand_Binary_on_host;
	public static String GdbDebugNewExecutableCommand_Binary_on_target;
	public static String GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified;
	public static String GdbDebugNewExecutableCommand_Browse;
	public static String GdbDebugNewExecutableCommand_BuildLog;
	public static String GdbDebugNewExecutableCommand_BuildLog_file_does_not_exist;
	public static String GdbDebugNewExecutableCommand_Debug_New_Executable;
	public static String GdbDebugNewExecutableCommand_Host_binary_file_does_not_exist;
	public static String GdbDebugNewExecutableCommand_Host_binary_must_be_specified;
	public static String GdbDebugNewExecutableCommand_Invalid_binary;
	public static String GdbDebugNewExecutableCommand_Invalid_buildLog;
	public static String GdbDebugNewExecutableCommand_Invalid_host_binary;
	public static String GdbDebugNewExecutableCommand_Select_binaries_on_host_and_target;
	public static String GdbDebugNewExecutableCommand_Select_Binary;
	public static String GdbDebugNewExecutableCommand_Select_binary_and_specify_arguments;

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);


	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
