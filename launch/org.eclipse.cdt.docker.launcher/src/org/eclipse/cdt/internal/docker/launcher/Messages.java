/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.docker.launcher.messages"; //$NON-NLS-1$

	public static String LaunchShortcut_Binaries;
	public static String LaunchShortcut_Binary_not_found;
	public static String LaunchShortcut_Choose_a_launch_configuration;
	public static String LaunchShortcut_Choose_a_local_application;
	public static String LaunchShortcut_Launch_Configuration_Selection;
	public static String LaunchShortcut_Looking_for_executables;
	public static String LaunchShortcut_no_project_selected;
	public static String LaunchShortcut_Error_Launching;
	public static String LaunchShortcut_No_Connections;
	public static String LaunchShortcut_No_Images;
	public static String LaunchShortcut_Qualifier;
	public static String LaunchShortcut_Launcher;
	public static String Default_Image;
	public static String Keep_Container_After_Launch;
	public static String ContainerTab_Name;
	public static String ContainerTab_Group_Name;
	public static String ContainerTab_Option_Group_Name;
	public static String ContainerTab_Ports_Group_Name;
	public static String ContainerTab_Specify_Ports_Label;

	public static String ContainerTab_Add_Button;
	public static String ContainerTab_Edit_Button;
	public static String ContainerTab_New_Button;
	public static String ContainerTab_Remove_Button;
	public static String ContainerTab_Keep_Label;
	public static String ContainerTab_Publish_All_Ports_Label;
	public static String ContainerTab_Stdin_Support_Label;
	public static String ContainerTab_Privileged_Mode_Label;
	public static String ContainerTab_Error_Reading_Configuration;
	public static String ContainerTab_Connection_Selector_Label;
	public static String ContainerTab_Image_Selector_Label;
	public static String ContainerTab_Port_Column;
	public static String ContainerTab_Type_Column;
	public static String ContainerTab_HostAddress_Column;
	public static String ContainerTab_HostPort_Column;

	public static String ContainerTab_Error_No_Connections;
	public static String ContainerTab_Error_No_Images;
	public static String ContainerTab_Warning_Connection_Not_Found;
	public static String ContainerTab_Warning_Image_Not_Found;

	public static String HeaderPreferencePage_Connection_Label;
	public static String HeaderPreferencePage_Image_Label;
	public static String HeaderPreferencePage_Remove_Label;
	public static String HeaderPreferencePage_Remove_Tooltip;
	public static String HeaderPreferencePage_Confirm_Removal_Title;
	public static String HeaderPreferencePage_Confirm_Removal_Msg;

	public static String Remote_GDB_Debugger_Options;
	public static String Gdbserver_Settings_Tab_Name;
	public static String Gdbserver_name_textfield_label;
	public static String Port_number_textfield_label;
	public static String Gdbserver_start;
	public static String Gdbserver_up;

	public static String GDBDebuggerPage0;
	public static String GDBDebuggerPage1;
	public static String GDBDebuggerPage2;
	public static String GDBDebuggerPage3;
	public static String GDBDebuggerPage4;
	public static String GDBDebuggerPage5;
	public static String GDBDebuggerPage6;
	public static String GDBDebuggerPage7;
	public static String GDBDebuggerPage8;
	public static String GDBDebuggerPage9;
	public static String GDBDebuggerPage10;
	public static String GDBDebuggerPage11;
	public static String GDBDebuggerPage12;

	public static String GDBDebuggerPage_gdb_executable_not_specified;
	public static String GDBDebuggerPage_tab_name;
	public static String GDBDebuggerPage_main_tab_name;
	public static String GDBDebuggerPage_gdb_debugger;
	public static String GDBDebuggerPage_gdb_browse;
	public static String GDBDebuggerPage_gdb_browse_dlg_title;
	public static String GDBDebuggerPage_gdb_command_file;
	public static String GDBDebuggerPage_gdb_cmdfile_browse;
	public static String GDBDebuggerPage_gdb_cmdfile_dlg_title;
	public static String GDBDebuggerPage_cmdfile_warning;
	public static String GDBDebuggerPage_shared_libraries;
	public static String GDBDebuggerPage_nonstop_mode;
	public static String GDBDebuggerPage_reverse_Debugging;
	public static String GDBDebuggerPage_update_thread_list_on_suspend;
	public static String GDBDebuggerPage_Automatically_debug_forked_processes;
	public static String GDBDebuggerPage_tracepoint_mode_label;
	public static String GDBDebuggerPage_tracepoint_mode_fast;
	public static String GDBDebuggerPage_tracepoint_mode_normal;
	public static String GDBDebuggerPage_tracepoint_mode_auto;

	public static String StandardGDBDebuggerPage14;

	public static String ContainerPropertyTab_Title;
	public static String ContainerPropertyTab_Enable_Msg;
	public static String ContainerPropertyTab_Run_Autotools_In_Container_Msg;
	public static String ContainerPropertyTab_Run_Autotools_In_Container_Tooltip;

	public static String ContainerCommandLauncher_image_msg;
	public static String CommandLauncher_CommandCancelled;

	public static String ContainerTarget_name;

	public static String ContainerCommandLauncher_invalid_values;

	public static String Gdbserver_Settings_Remotetimeout_label;

	public static String Gdbserver_Settings_Remotetimeout_tooltip;

	public static String ContainerPortDialog_hostAddressLabel;
	public static String ContainerPortDialog_hostPortLabel;
	public static String ContainerPortDialog_shellTitle;
	public static String ContainerPortDialog_containerLabel;
	public static String ContainerPortDialog_explanationLabel;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
