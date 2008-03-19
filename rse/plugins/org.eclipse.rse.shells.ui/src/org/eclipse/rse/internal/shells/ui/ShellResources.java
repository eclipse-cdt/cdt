/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)        - [216252] [nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Xuan Chen        (IBM)        - [223126] [api][breaking] Remove API related to User Actions in RSE Core/UI
 * David McKnight   (IBM)        - [223204] [cleanup] fix broken nls strings in files.ui and others
 *******************************************************************************/

package org.eclipse.rse.internal.shells.ui;

import org.eclipse.osgi.util.NLS;

public class ShellResources extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.internal.shells.ui.ShellResources"; //$NON-NLS-1$

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

	
	public static String ACTION_OPEN_WITH_LABEL;
	
	// error properties
	public static String	RESID_PROPERTY_ERROR_FILENAME_LABEL;
	public static String	RESID_PROPERTY_ERROR_FILENAME_TOOLTIP;

	public static String	RESID_PROPERTY_ERROR_LINENO_LABEL;
	public static String	RESID_PROPERTY_ERROR_LINENO_TOOLTIP;

	// shell status properties
	public static String	RESID_PROPERTY_SHELL_STATUS_LABEL;
	public static String	RESID_PROPERTY_SHELL_STATUS_TOOLTIP;
	public static String	RESID_PROPERTY_SHELL_CONTEXT_LABEL;
	public static String	RESID_PROPERTY_SHELL_CONTEXT_TOOLTIP;
	
	public static String	RESID_PROPERTY_SHELL_STATUS_ACTIVE_VALUE;
	public static String	RESID_PROPERTY_SHELL_STATUS_INACTIVE_VALUE;

	// universal commands
	public static String MSG_UCMD_INVOCATION_EMPTY;

	
	public static String ACTION_CONTENT_ASSIST;

	static 
	{
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ShellResources.class);
	}
	
}
