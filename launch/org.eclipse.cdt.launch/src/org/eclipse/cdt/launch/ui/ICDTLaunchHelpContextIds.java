/*
 * Created on 22-Sep-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.ui;

import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;

public interface ICDTLaunchHelpContextIds {

	public static final String PREFIX = LaunchUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	// Launch configuration dialog pages
	public static final String LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB = PREFIX + "launch_configuration_dialog_main_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB = PREFIX + "launch_configuration_dialog_arguments_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB = PREFIX + "launch_configuration_dialog_environment_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB = PREFIX + "launch_configuration_dialog_debugger_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_SOURCELOOKUP_TAB = PREFIX + "launch_configuration_dialog_source_tab"; //$NON-NLS-1$

}
