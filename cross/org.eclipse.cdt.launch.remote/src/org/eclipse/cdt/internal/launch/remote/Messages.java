/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
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

	public static String Remote_GDB_Debugger_Options;

	public static String RemoteCMainTab_Prerun;

	public static String RemoteCMainTab_Program;

	public static String RemoteCMainTab_Remote_Path_Browse_Button;

	public static String RemoteCMainTab_Remote_Path_Browse_Button_Title;
	public static String RemoteCMainTab_SkipDownload;
	public static String RemoteCMainTab_ErrorNoProgram;
	public static String RemoteCMainTab_ErrorNoConnection;
	public static String RemoteCMainTab_Connection;
	public static String RemoteCMainTab_New;
	public static String RemoteCMainTab_Properties;
	public static String RemoteCMainTab_Properties_title;
	public static String RemoteCMainTab_Properties_Location;
	public static String RemoteCMainTab_Properties_Skip_default;

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
