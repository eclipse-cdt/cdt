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

package org.eclipse.rse.processes.ui;

import org.eclipse.osgi.util.NLS;

public class SystemProcessesResources extends NLS
{
	private static String BUNDLE_NAME = "org.eclipse.rse.processes.ui.SystemProcessesResources"; 
		
	// -------------------------
	// ACTIONS...
	// -------------------------
	public static String	ACTION_NEWPROCESSFILTER_LABEL;
	public static String	ACTION_NEWPROCESSFILTER_TOOLTIP;
	
	public static String	ACTION_UPDATEFILTER_LABEL;
	public static String	ACTION_UPDATEFILTER_TOOLTIP;
	
	public static String	ACTION_KILLPROCESS_LABEL;
	public static String	ACTION_KILLPROCESS_TOOLTIP;

	// -------------------------
	// WIZARDS...
	// -------------------------

	// New System process Filter wizard...
	public static String	RESID_NEWPROCESSFILTER_PAGE1_TITLE;
	public static String	RESID_NEWPROCESSFILTER_PAGE1_DESCRIPTION;

	// Change process filter
	public static String	RESID_CHGPROCESSFILTER_TITLE;
	
	// Process Filter String Re-Usable form (used in dialog and wizard)

	public static String	RESID_PROCESSFILTERSTRING_EXENAME_LABEL;
	public static String	RESID_PROCESSFILTERSTRING_USERNAME_LABEL;
	public static String	RESID_PROCESSFILTERSTRING_GID_LABEL; 
	public static String	RESID_PROCESSFILTERSTRING_MINVM_LABEL;
	public static String	RESID_PROCESSFILTERSTRING_MAXVM_LABEL; 
	public static String	RESID_PROCESSFILTERSTRING_UNLIMITED_LABEL; 
	public static String	RESID_PROCESSFILTERSTRING_STATUS_LABEL; 
	
	public static String	RESID_PROCESSFILTERSTRING_EXENAME_TOOLTIP;
	public static String	RESID_PROCESSFILTERSTRING_USERNAME_TOOLTIP;
	public static String	RESID_PROCESSFILTERSTRING_GID_TOOLTIP; 
	public static String	RESID_PROCESSFILTERSTRING_MINVM_TOOLTIP;
	public static String	RESID_PROCESSFILTERSTRING_MAXVM_TOOLTIP; 
	public static String	RESID_PROCESSFILTERSTRING_UNLIMITED_TOOLTIP; 
	public static String	RESID_PROCESSFILTERSTRING_STATUS_TOOLTIP;

	// Warnings
	public static String 	RESID_KILL_WARNING_LABEL;
	public static String 	RESID_KILL_WARNING_TOOLTIP;
	
	// KILL Process dialog
	public static String 	RESID_KILL_TITLE;
	public static String 	RESID_KILL_PROMPT;
	public static String 	RESID_KILL_PROMPT_SINGLE;
	public static String 	RESID_KILL_BUTTON;
	public static String 	RESID_KILL_TIP;
	public static String 	RESID_KILL_SIGNAL_TYPE_LABEL;
	public static String 	RESID_KILL_SIGNAL_TYPE_TOOLTIP;
	public static String    RESID_KILL_SIGNAL_TYPE_DEFAULT;
	public static String 	RESID_KILL_COLHDG_EXENAME;
	public static String 	RESID_KILL_COLHDG_PID;

	// Remote Processes dialog
	public static String	RESID_REMOTE_PROCESSES_EXECUTABLE_LABEL;
	public static String	RESID_REMOTE_PROCESSES_EXECUTABLE_TOOLTIP;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, SystemProcessesResources.class);
	}
}