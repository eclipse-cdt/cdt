/*******************************************************************************
 * Copyright (c) 2008, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;

public interface ICDTLaunchHelpContextIds {

	public static final String PREFIX = GdbUIPlugin.PLUGIN_ID + "."; //$NON-NLS-1$

	// Launch configuration dialog pages
	public static final String LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB = PREFIX + "launch_configuration_dialog_main_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB = PREFIX + "launch_configuration_dialog_arguments_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB = PREFIX + "launch_configuration_dialog_environment_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_DEBBUGER_TAB = PREFIX + "launch_configuration_dialog_debugger_tab"; //$NON-NLS-1$	
	public static final String LAUNCH_CONFIGURATION_DIALOG_SOURCELOOKUP_TAB = PREFIX + "launch_configuration_dialog_source_tab"; //$NON-NLS-1$

}
