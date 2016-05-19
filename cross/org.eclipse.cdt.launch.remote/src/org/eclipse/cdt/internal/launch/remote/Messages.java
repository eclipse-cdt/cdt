/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Martin Oberhuber      (Wind River) - initial API and implementation
 * Ewa Matejska          (PalmSource) - [158783] browse button for cdt remote path
 * Johann Draschwandtner (Wind River) - [231827][remotecdt]Auto-compute default for Remote path
 * Anna Dushistova       (MontaVista) - [244173][remotecdt][nls] Externalize Strings in RemoteRunLaunchDelegate
 * Anna Dushistova       (MontaVista) - [181517][usability] Specify commands to be run before remote application launch
 * Nikita Shulga      (EmbeddedAlley) - [265236][remotecdt] Wait for RSE to initialize before querying it for host list
 * Anna Dushistova       (MontaVista) - [368597][remote debug] if gdbserver fails to launch on target, launch doesn't get terminated
 *******************************************************************************/
package org.eclipse.cdt.internal.launch.remote;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.launch.remote.messages"; //$NON-NLS-1$

	public static String Gdbserver_name_textfield_label;

	public static String Gdbserver_Settings_Tab_Name;

	public static String Port_number_textfield_label;

	public static String Gdbserver_options_textfield_label;

	public static String Remote_GDB_Debugger_Options;

	public static String RemoteCMainTab_Prerun;

	public static String RemoteCMainTab_Program;

	public static String RemoteCMainTab_RemoteSetupGrp_Title;
	public static String RemoteCMainTab_RemoteSetupGrp_PathBrowse_Button;

	public static String RemoteCMainTab_Remote_Path_Browse_Button_Title;
	public static String RemoteCMainTab_LocalProgramGrp_Title;
	public static String RemoteCMainTab_LocalProgramGrp_UseLocal_Check;
	public static String RemoteCMainTab_LocalProgramGrp_Program_Label;
	public static String RemoteCMainTab_ErrorNoProgram;
	public static String RemoteCMainTab_Remote_Program_Error_Not_Absolute;
	public static String RemoteCMainTab_ConnectionInfo_Label;
	public static String RemoteCMainTab_ConnectionInfo_Link;
	/*
	 * Remote System Tab
	 */
	public static String RemoteSystemTab_Tab_Name;
	public static String RemoteSystemTab_Error_NoConnection;
	public static String RemoteSystemTab_Error_ConnectionNotFound;
	public static String RemoteSystemTab_ConnectionGrp_Group_Name;
	public static String RemoteSystemTab_ConnectionGrp_ConnectionName_Label;
	public static String RemoteSystemTab_ConnectionGrp_New_Button;
	public static String RemoteSystemTab_ConnectionGrp_Edit_Button;
	public static String RemoteSystemTab_ConnectionGrp_Properties_Button;
	public static String RemoteSystemTab_ConnectionGrp_Type_Property;
	public static String RemoteSystemTab_ConnectionGrp_Host_Property;
	public static String RemoteSystemTab_ConnectionGrp_User_Property;
	public static String RemoteSystemTab_ShowSystemInfo_Check;
	public static String RemoteSystemTab_ShowSystemInfo_NoInformation;
	public static String RemoteSystemTab_ShowSystemInfo_Error_UnableToGetInfo;
	public static String RemoteSystemTab_ShowSystemInfo_OS_Name_Prop;
	public static String RemoteSystemTab_ShowSystemInfo_OS_Version_Prop;
	public static String RemoteSystemTab_ShowSystemInfo_OS_Arch_Prop;
	public static String RemoteSystemTab_ShowSystemInfo_UpdateJob_Title;
	public static String RemoteSystemTab_ShowSystemInfo_UpdateJob_Error;
	public static String RemoteSystemTab_PropertiesDialog_Location_Label;
	public static String RemoteSystemTab_PropertiesDialog_SkipDownload_Check;
	public static String RemoteSystemTab_PropertiesDialog_Title;
	public static String RemoteSystemTab_NewDialog_Title;
	public static String RemoteSystemTab_NewDialog_ConnType_Combo;

	public static String RemoteGdbLaunchDelegate_gdbserverFailedToStartErrorMessage;

	public static String RemoteRunLaunchDelegate_0;

	public static String RemoteRunLaunchDelegate_RemoteShell;
	public static String RemoteRunLaunchDelegate_1;

	public static String RemoteRunLaunchDelegate_10;

	public static String RemoteRunLaunchDelegate_12;

	public static String RemoteRunLaunchDelegate_13;

	public static String RemoteRunLaunchDelegate_2;
	public static String RemoteRunLaunchDelegate_3;
	public static String RemoteRunLaunchDelegate_4;
	public static String RemoteRunLaunchDelegate_5;
	public static String RemoteRunLaunchDelegate_6;
	public static String RemoteRunLaunchDelegate_7;
	public static String RemoteRunLaunchDelegate_8;

	public static String RemoteRunLaunchDelegate_9;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
