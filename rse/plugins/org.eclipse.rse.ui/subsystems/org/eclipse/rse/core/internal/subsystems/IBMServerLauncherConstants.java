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

package org.eclipse.rse.core.internal.subsystems;

/**
 * This interface defines constants for IBM Server Launcher.
 */
public interface IBMServerLauncherConstants 
{
	
	/**
	 * Default daemon port, 4035. 
	 */
	public static final int DEFAULT_DAEMON_PORT = 4035;
	
	/** 
	 * Default REXEC port, 512.
	 */
	public static final int DEFAULT_REXEC_PORT = 512;
	
	/**
	 * Default REXEC path, "/opt/rseserver/".
	 */
	public static final String DEFAULT_REXEC_PATH = "/opt/rseserver/";
	
	/**
	 * Linux REXEC script command, "perl ./server.pl".
	 */
	public static final String LINUX_REXEC_SCRIPT = "perl ./server.pl";
	
	/**
	 * Unix REXEC script command, "./server.sh".
	 */
	public static final String UNIX_REXEC_SCRIPT = "./server.sh";
	
	/**
	 * Default REXEC script command. TIt is equivalent to <code>LINUX_REXEC_SCRIPT</code>.
	 */
	public static final String DEFAULT_REXEC_SCRIPT = LINUX_REXEC_SCRIPT;
}