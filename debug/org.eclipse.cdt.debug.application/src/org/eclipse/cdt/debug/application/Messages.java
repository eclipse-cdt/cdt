/*******************************************************************************
 * Copyright (c) 2013, 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *    Marc Khouzam (Ericsson) - Update for remote debugging support (bug 450080)
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
	public static String GdbDebugExecutableCommand_Binary;
	public static String GdbDebugExecutableCommand_Binary_Optional;
	public static String GdbDebugNewExecutableCommand_Binary_file_does_not_exist;
	public static String GdbDebugNewExecutableCommand_Binary_must_be_specified;
	public static String GdbDebugNewExecutableCommand_Binary_on_host;
	public static String GdbDebugNewExecutableCommand_Binary_on_target;
	public static String GdbDebugNewExecutableCommand_Binary_on_target_must_be_specified;
	public static String GdbDebugExecutableCommand_Browse;
	public static String GdbDebugExecutableCommand_BuildLog;
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
	public static String GdbDebugRemoteExecutableCommand_Debug_Remote_Executable;
	public static String GdbDebugRemoteExecutableCommand_Select_Remote_Options;
	public static String GdbDebugRemoteExecutableCommand_Host_name_or_ip_address;
	public static String GdbDebugRemoteExecutableCommand_Port_number;
	public static String GdbDebugRemoteExecutableCommand_Attach;
	public static String GdbDebugRemoteExecutableCommand_address_must_be_specified;
	public static String GdbDebugRemoteExecutableCommand_port_must_be_specified;
	public static String GdbDebugRemoteExecutableCommand_port_must_be_a_number;
	public static String GdbDebugCoreFileCommand_CoreFile;
	public static String GdbDebugCoreFileCommand_Debug_Core_File;
	public static String GdbDebugCoreFileCommand_Select_binary_and_specify_corefile;
	public static String GdbDebugCoreFileCommand_Core_file_must_be_specified;
	public static String GdbDebugCoreFileCommand_Core_file_does_not_exist;
	public static String GdbDebugCoreFileCommand_Invalid_core_file;

	public static String FileMenuName;
	public static String EditMenuName;
	public static String WindowMenuName;
	public static String HelpMenuName;

	public static String ShowViewMenuName;

	public static String ResetPerspective_text;
	public static String ResetPerspective_toolTip;
	public static String Workbench_cut;
	public static String Workbench_cutToolTip;
	public static String Workbench_copy;
	public static String Workbench_copyToolTip;
	public static String Workbench_paste;
	public static String Workbench_pasteToolTip;
	public static String Workbench_selectAll;
	public static String Workbench_selectAllToolTip;
	public static String Workbench_findReplace;
	public static String Workbench_findReplaceToolTip;
	public static String Workbench_addBookmark;
	public static String Workbench_addBookmarkToolTip;
	public static String Workbench_addTask;
	public static String Workbench_addTaskToolTip;
	public static String Workbench_delete;
	public static String Workbench_deleteToolTip;

	public static String CoreFileMenuName;
	public static String CoreFile_toolTip;
	public static String NewExecutable_toolTip;
	public static String NewExecutableMenuName;
	public static String RemoteExecutable_toolTip;
	public static String RemoteExecutableMenuName;
	public static String AttachedExecutable_toolTip;
	public static String AttachedExecutableMenuName;
	public static String Application_WorkspaceRootNotExistError;
	public static String Application_WorkspaceCreationError;
	public static String Application_WorkspaceRootPermissionError;
	public static String Application_WorkspaceInUseError;

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

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
