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

package org.eclipse.rse.subsystems.shells.core;

import org.eclipse.osgi.util.NLS;

public class ShellStrings extends NLS 
{
	private static String BUNDLE_NAME = "org.eclipse.rse.subsystems.shells.core.ShellStrings";

	public static String RESID_SHELLS_COMMAND_SHELL_LABEL;
	public static String RSESubSystemOperation_Cancel_Shell_message;
	public static String RSESubSystemOperation_Remove_Shell_message;
	public static String RSESubSystemOperation_Run_command_message;
	public static String RSESubSystemOperation_Run_Shell_message;
	public static String RSESubSystemOperation_Send_command_to_Shell_message;
	public static String RSESubSystemOperation_Refresh_Output;
	
	static 
	{
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ShellStrings.class);
	}
	
}