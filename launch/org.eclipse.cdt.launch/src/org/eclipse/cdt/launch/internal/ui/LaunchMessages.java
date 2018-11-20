/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc Khouzam (Ericsson) - New strings for CMainTab2
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.osgi.util.NLS;

public class LaunchMessages extends NLS {
	public static String AbstractCLaunchDelegate_Debugger_not_installed;
	public static String AbstractCLaunchDelegate_C_Project_not_specified;
	public static String AbstractCLaunchDelegate_Not_a_C_CPP_project;
	public static String AbstractCLaunchDelegate_Program_file_not_specified;
	public static String AbstractCLaunchDelegate_Program_file_does_not_exist;
	public static String AbstractCLaunchDelegate_PROGRAM_PATH_not_found;
	public static String AbstractCLaunchDelegate_Working_directory_does_not_exist;
	public static String AbstractCLaunchDelegate_WORKINGDIRECTORY_PATH_not_found;
	public static String AbstractCLaunchDelegate_Project_NAME_does_not_exist;
	public static String AbstractCLaunchDelegate_Project_NAME_is_closed;
	public static String AbstractCLaunchDelegate_Debugger_Process;
	public static String AbstractCLaunchDelegate_building_projects;
	public static String AbstractCLaunchDelegate_building;
	public static String AbstractCLaunchDelegate_searching_for_errors;
	public static String AbstractCLaunchDelegate_searching_for_errors_in;
	public static String AbstractCLaunchDelegate_20;
	public static String AbstractCLaunchDelegate_Program_is_not_a_recognized_executable;
	public static String AbstractCLaunchDelegate_BuildBeforeLaunch;
	public static String AbstractCLaunchDelegate_PerformingBuild;
	public static String AbstractCLaunchDelegate_PerformingIncrementalBuild;
	public static String AbstractCLaunchDelegate_Refresh;
	public static String LocalRunLaunchDelegate_Launching_Local_C_Application;
	public static String LocalRunLaunchDelegate_Failed_setting_runtime_option_though_debugger;
	public static String LocalRunLaunchDelegate_Error_starting_process;
	public static String LocalRunLaunchDelegate_Does_not_support_working_dir;
	public static String LocalAttachLaunchDelegate_Attaching_to_Local_C_Application;
	public static String LocalAttachLaunchDelegate_No_Process_ID_selected;
	public static String LocalAttachLaunchDelegate_Select_Process;
	public static String LocalAttachLaunchDelegate_Platform_cannot_list_processes;
	public static String LocalAttachLaunchDelegate_Select_Process_to_attach_debugger_to;
	public static String LocalAttachLaunchDelegate_CDT_Launch_Error;
	public static String CommonBuildTab_Default;
	public static String CommonBuildTab_NotFound;
	public static String CommonBuildTab_Toolchain;
	public static String CoreBuildTab_Build;
	public static String CoreBuildTab_NoOptions;
	public static String CoreFileLaunchDelegate_Launching_postmortem_debugger;
	public static String CoreFileLaunchDelegate_No_Corefile_selected;
	public static String CoreFileLaunchDelegate_No_Shell_available_in_Launch;
	public static String CoreFileLaunchDelegate_Select_Corefile;
	public static String CoreFileLaunchDelegate_Corefile_not_accessible;
	public static String CoreFileLaunchDelegate_Corefile_not_readable;
	public static String CoreFileLaunchDelegate_postmortem_debugging_failed;
	public static String AbstractCDebuggerTab_No_debugger_available;
	public static String AbstractCDebuggerTab_Debugger;
	public static String AbstractCDebuggerTab_ErrorLoadingDebuggerPage;
	public static String AbstractChange_compositeName0;
	public static String LaunchUIPlugin_Error;
	public static String CMainTab_Project_required;
	public static String CMainTab_Enter_project_before_searching_for_program;
	public static String CMainTab_Program_Selection;
	public static String CMainTab_Enter_project_before_browsing_for_program;
	public static String CMainTab_Program_selection;
	public static String CMainTab_Selection_must_be_file;
	public static String CMainTab_Selection_must_be_binary_file;
	public static String CMainTab_Project_Selection;
	public static String CMainTab_Choose_project_to_constrain_search_for_program;
	public static String CMainTab_Project_not_specified;
	public static String CMainTab_Program_not_specified;
	public static String CMainTab_Project_must_be_opened;
	public static String CMainTab_Program_does_not_exist;
	public static String CMainTab_Core_does_not_exist;
	public static String CMainTab_Main;
	public static String CMainTab_ProjectColon;
	public static String CMainTab_C_Application;
	public static String CMainTab_CoreFile_path;
	public static String CMainTab_Variables;
	public static String CMainTab_Search;
	public static String CMainTab_Choose_program_to_run;
	public static String CMainTab_Choose_program_to_run_from_NAME;
	public static String CMainTab_UseTerminal;
	public static String CMainTab_Program_invalid_proj_path;
	public static String CMainTab_Build_Config;
	public static String CMainTab_Use_Active;
	public static String CMainTab_Use_Automatic;
	public static String CMainTab_Build_Config_Active_tooltip;
	public static String CMainTab_Build_Config_Auto;
	public static String CMainTab_Build_Config_Auto_tooltip;
	public static String CMainTab_Configuration_name;
	public static String CMainTab_Build_options;
	public static String CMainTab_Disable_build_button_label;
	public static String CMainTab_Disable_build_button_tooltip;
	public static String CMainTab_Enable_build_button_label;
	public static String CMainTab_Enable_build_button_tooltip;
	public static String CMainTab_Workspace_settings_button_label;
	public static String CMainTab_Workspace_settings_button_tooltip;
	public static String CMainTab_Workspace_settings_link_label;
	public static String CDebuggerTab_Advanced_Options_Dialog_Title;
	public static String CDebuggerTab_Stop_at_main_on_startup;
	public static String CDebuggerTab_Automatically_track_values_of;
	public static String CDebuggerTab_Stop_on_startup_at_can_not_be_empty;
	public static String CDebuggerTab_Debugger_Options;
	public static String CDebuggerTab_Mode_not_supported;
	public static String CDebuggerTab_Advanced;
	public static String CDebuggerTab_Variables;
	public static String CDebuggerTab_Registers;
	public static String CDebuggerTab_No_debugger_available;
	public static String CDebuggerTab_CPU_is_not_supported;
	public static String CDebuggerTab_Platform_is_not_supported;
	public static String CoreFileDebuggerTab_No_debugger_available;
	public static String CoreFileDebuggerTab_platform_is_not_supported;
	public static String CEnvironmentTab_Edit_Variable;
	public static String CEnvironmentTab_New_Variable;
	public static String CEnvironmentTab_NameColon;
	public static String CEnvironmentTab_ValueColon;
	public static String CEnvironmentTab_Name;
	public static String CEnvironmentTab_Value;
	public static String CEnvironmentTab_New;
	public static String CEnvironmentTab_Import;
	public static String CEnvironmentTab_Edit;
	public static String CEnvironmentTab_Remove;
	public static String CEnvironmentTab_Environment;
	public static String CEnvironmentTab_Existing_Environment_Variable;
	public static String CEnvironmentTab_Environment_variable_NAME_exists;
	public static String CArgumentsTab_C_Program_Arguments;
	public static String CArgumentsTab_Arguments;
	public static String CArgumentsTab_Variables;
	public static String WorkingDirectoryBlock_4;
	public static String WorkingDirectoryBlock_7;
	public static String WorkingDirectoryBlock_0;
	public static String WorkingDirectoryBlock_Working_Directory_8;
	public static String WorkingDirectoryBlock_Working_directory;
	public static String WorkingDirectoryBlock_10;
	public static String WorkingDirectoryBlock_Use_default;
	public static String WorkingDirectoryBlock_17;
	public static String WorkingDirectoryBlock_1;
	public static String WorkingDirectoryBlock_Exception_occurred_reading_configuration_15;
	public static String Launch_common_Exception_occurred_reading_configuration_EXCEPTION;
	public static String Launch_common_DebuggerColon;
	public static String Launch_common_BinariesColon;
	public static String Launch_common_QualifierColon;
	public static String Launch_common_Browse_1;
	public static String Launch_common_Browse_2;
	public static String Launch_common_Browse_3;
	public static String Launch_common_Project_does_not_exist;
	public static String LocalCDILaunchDelegate_0;
	public static String LocalCDILaunchDelegate_1;
	public static String LocalCDILaunchDelegate_2;
	public static String LocalCDILaunchDelegate_3;
	public static String LocalCDILaunchDelegate_4;
	public static String LocalCDILaunchDelegate_5;
	public static String LocalCDILaunchDelegate_6;
	public static String LocalCDILaunchDelegate_7;
	public static String LocalCDILaunchDelegate_8;
	public static String LocalCDILaunchDelegate_9;
	public static String LocalCDILaunchDelegate_10;
	public static String MultiLaunchConfigurationDelegate_0;
	public static String MultiLaunchConfigurationDelegate_Cannot;
	public static String MultiLaunchConfigurationDelegate_Loop;
	public static String MultiLaunchConfigurationDelegate_Action_None;
	public static String MultiLaunchConfigurationDelegate_Action_WaitUntilTerminated;
	public static String MultiLaunchConfigurationDelegate_Action_Delay;
	public static String MultiLaunchConfigurationDelegate_Action_WaitingForTermination;
	public static String MultiLaunchConfigurationDelegate_Action_Delaying;
	public static String MultiLaunchConfigurationSelectionDialog_0;
	public static String MultiLaunchConfigurationSelectionDialog_4;
	public static String MultiLaunchConfigurationSelectionDialog_5;
	public static String MultiLaunchConfigurationSelectionDialog_7;
	public static String MultiLaunchConfigurationSelectionDialog_8;
	public static String MultiLaunchConfigurationSelectionDialog_9;
	public static String MultiLaunchConfigurationSelectionDialog_10;
	public static String MultiLaunchConfigurationSelectionDialog_11;
	public static String MultiLaunchConfigurationSelectionDialog_12;
	public static String MultiLaunchConfigurationSelectionDialog_13;
	public static String MultiLaunchConfigurationSelectionDialog_14;
	public static String MultiLaunchConfigurationSelectionDialog_15;
	public static String MultiLaunchConfigurationTabGroup_1;
	public static String MultiLaunchConfigurationTabGroup_2;
	public static String MultiLaunchConfigurationTabGroup_3;
	public static String MultiLaunchConfigurationTabGroup_4;
	public static String MultiLaunchConfigurationTabGroup_5;
	public static String MultiLaunchConfigurationTabGroup_6;
	public static String MultiLaunchConfigurationTabGroup_7;
	public static String MultiLaunchConfigurationTabGroup_10;
	public static String MultiLaunchConfigurationTabGroup_11;
	public static String MultiLaunchConfigurationTabGroup_12;
	public static String MultiLaunchConfigurationTabGroup_13;
	public static String MultiLaunchConfigurationTabGroup_14;
	public static String MultiLaunchConfigurationTabGroup_15;
	public static String MultiLaunchConfigurationTabGroup_16;
	public static String ProjectRenameChange_name;
	public static String ProjectRenameChange_saveFailed;
	public static String BuildErrPrompter_error_in_specific_config;
	public static String BuildErrPrompter_error_in_active_config;
	public static String BuildErrPrompter_error_in_referenced_project_specific;
	public static String BuildErrPrompter_error_in_referenced_project_active;
	public static String CMainTab2_File_does_not_exist;
	public static String CMainTab2_CoreFile_type;
	public static String CMainTab2_TraceFile_type;
	public static String CMainTab2_CoreFile_path;
	public static String CMainTab2_TraceFile_path;
	public static String CMainTab2_Application_Selection;
	public static String CMainTab2_Core_Selection;
	public static String CMainTab2_Trace_Selection;
	public static String CMainTab2_Post_mortem_file_type;

	private LaunchMessages() {
	}

	static {
		// Load message values from bundle file
		NLS.initializeMessages(LaunchMessages.class.getCanonicalName(), LaunchMessages.class);
	}
}
