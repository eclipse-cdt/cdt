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

package org.eclipse.rse.ui;

/**
 * This interface should be used to maintain all constants related to colors and fonts
 * that are settable by the user through preferences
 */
public interface ISystemThemeConstants {
	
	// color constants used for messages
	public static final String MESSAGE_ERROR_COLOR = "MESSAGE_ERROR_COLOR";
	public static final String MESSAGE_WARNING_COLOR = "MESSAGE_WARNING_COLOR" ;
	public static final String MESSAGE_INFORMATION_COLOR = "MESSAGE_INFORMATION_COLOR";

	// color constants used by Remote Commnds view
	public static final String REMOTE_COMMANDS_VIEW_BG_COLOR = "REMOTE_COMMANDS_VIEW_BG_COLOR";
	public static final String REMOTE_COMMANDS_VIEW_FG_COLOR = "REMOTE_COMMANDS_VIEW_FG_COLOR";
	public static final String REMOTE_COMMANDS_VIEW_PROMPT_COLOR = "REMOTE_COMMANDS_VIEW_PROMPT_COLOR";
		
	// font constant used by Remote Commands view
	public static final String REMOTE_COMMANDS_VIEW_FONT = "REMOTE_COMMANDS_VIEW_FONT";
}