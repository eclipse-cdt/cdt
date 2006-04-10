/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.shells.ui;

import org.eclipse.osgi.util.NLS;

public class ShellResources extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.shells.ui.ShellResources";

	public static String RESID_SHELL_PROPERTYPAGE_TITLE;
	public static String RESID_SHELL_PROPERTYPAGE_DESCRIPTION;
	public static String RESID_SHELL_PROPERTYPAGE_ENCODING;
	public static String RESID_SHELL_PROPERTYPAGE_DEFAULT_ENCODING;
	public static String RESID_SHELL_PROPERTYPAGE_HOST_ENCODING;
	public static String RESID_SHELL_PROPERTYPAGE_OTHER_ENCODING;
	public static String RESID_UNSUPPORTED_ENCODING;


	public static String RESID_SHELLS_RUN_IN_NEW_SHELL_LABEL; 
	public static String RESID_SHELLS_RUN_IN_NEW_SHELL_TOOLTIP;
	public static String RESID_SHELLS_RUN_COMMAND_LABEL;
	public static String RESID_SHELLS_COMMAND_LABEL;
	public static String RESID_SHELLS_COMMAND_TOOLTIP;
	public static String RESID_SHELLS_COMMAND_SHELL_LABEL;

	// COMMANDS VIEW
	public static String RESID_COMMANDSVIEW_RUN_LABEL;
	public static String RESID_COMMANDSVIEW_RUN_TOOLTIP;
	public static String RESID_COMMANDSVIEW_CMDCOMBO_TOOLTIP;
	public static String RESID_COMMANDSVIEW_SUBSYSCOMBO_TOOLTIP;
	public static String RESID_PREF_COMMANDSVIEW_BRINGTOFRONT_LABEL;
	public static String RESID_PREF_COMMANDSVIEW_BRINGTOFRONT_TOOLTIP;

	// UNIVERSAL COMMMANDS VIEW
	public static String RESID_COMMANDSVIEW_COMMAND_LABEL;
	public static String RESID_COMMANDSVIEW_COMMAND_TOOLTIP;
	public static String RESID_COMMANDSVIEW_PREVIOUS_TOOLTIP;

	// ACTIONS
	public static String ACTION_RUN_COMMAND_LABEL;
	public static String ACTION_RUN_COMMAND_TOOLTIP;

	public static String ACTION_RUN_SHELL_LABEL;
	public static String ACTION_RUN_SHELL_TOOLTIP;

	public static String ACTION_LAUNCH_LABEL;
	public static String ACTION_LAUNCH_TOOLTIP;

	public static String ACTION_SHOW_SHELL_LABEL;
	public static String ACTION_SHOW_SHELL_TOOLTIP;

	public static String ACTION_CANCEL_SHELL_LABEL;
	public static String ACTION_CANCEL_SHELL_TOOLTIP;

	public static String ACTION_CANCEL_REMOVE_SHELL_LABEL;
	public static String ACTION_CANCEL_REMOVE_SHELL_TOOLTIP;

	public static String ACTION_EXPORT_SHELL_OUTPUT_LABEL;
	public static String ACTION_EXPORT_SHELL_OUTPUT_TOOLTIP;

	public static String ACTION_EXPORT_SHELL_HISTORY_LABEL;
	public static String ACTION_EXPORT_SHELL_HISTORY_TOOLTIP;

	public static String ACTION_OPEN_OUTPUT_LABEL;
	public static String ACTION_OPEN_OUTPUT_TOOLTIP;

	static 
	{
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ShellResources.class);
	}
	
}