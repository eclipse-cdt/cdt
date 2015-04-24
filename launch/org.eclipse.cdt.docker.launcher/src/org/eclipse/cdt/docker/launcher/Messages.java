/*******************************************************************************
 * Copyright (c) 2012, 2013, 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.docker.launcher;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.docker.launcher.messages"; //$NON-NLS-1$

	public static String LaunchShortcut_Binaries;
	public static String LaunchShortcut_Binary_not_found;
	public static String LaunchShortcut_Choose_a_launch_configuration;
	public static String LaunchShortcut_Choose_a_local_application;
	public static String LaunchShortcut_Launch_Configuration_Selection;
	public static String LaunchShortcut_Looking_for_executables;
	public static String LaunchShortcut_no_project_selected;
	public static String LaunchShortcut_Qualifier;
	public static String LaunchShortcut_Launcher;
	public static String Default_Image;
	public static String Keep_Container_After_Launch;
	public static String ContainerTab_Name;
	public static String ContainerTab_Group_Name;
	public static String ContainerTab_Option_Group_Name;
	public static String ContainerTab_New_Button;
	public static String ContainerTab_Remove_Button;
	public static String ContainerTab_Keep_Label;
	public static String ContainerTab_Stdin_Support_Label;
	public static String ContainerTab_Error_Reading_Configuration;
	public static String ContainerTab_Connection_Selector_Label;
	public static String ContainerTab_Image_Selector_Label;

	public static String Remote_GDB_Debugger_Options;
	public static String Gdbserver_Settings_Tab_Name;
	public static String Gdbserver_name_textfield_label;
	public static String Port_number_textfield_label;
	public static String Gdbserver_start;
	public static String Gdbserver_up;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
