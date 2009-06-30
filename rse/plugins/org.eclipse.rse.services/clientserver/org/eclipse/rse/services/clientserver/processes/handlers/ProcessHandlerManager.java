/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
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
 * David McKnight      (IBM) - [175293] [dstore] Processes do not work on Dstore-UNIX connection to Solaris
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.processes.handlers;



public class ProcessHandlerManager
{
	
	//	the singleton instance
	protected static ProcessHandlerManager _instance = new ProcessHandlerManager(); 

	/**
	 * @return The singleton instance of this class.
	 */
	public static ProcessHandlerManager getInstance()
	{
		return _instance;
	}
	
	/**
	 * Returns the ProcessHandler associated with the system type on which
	 * the server is running, or null if there is no associated ProcessHandler.
	 */
	public ProcessHandler getNewProcessHandler()
	{
		String osName = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

		if (osName.startsWith("linux")) return new UniversalLinuxProcessHandler(); //$NON-NLS-1$
		else if (osName.startsWith("aix")) return new UniversalAIXProcessHandler(); //$NON-NLS-1$
		else if (osName.startsWith("z/os")) return new UniversalZOSProcessHandler(); //$NON-NLS-1$
		else if (osName.startsWith("mac os x")) return new UniversalMacOSXProcessHandler(); //$NON-NLS-1$
		else if (osName.startsWith("sun")) return new UniversalSolarisProcessHandler(); //$NON-NLS-1$
		return null;
		
	}
}
