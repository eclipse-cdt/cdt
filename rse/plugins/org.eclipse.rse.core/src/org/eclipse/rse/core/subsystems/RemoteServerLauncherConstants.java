/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/

package org.eclipse.rse.core.subsystems;

/**
 * This interface defines constants for the Remote Server Launcher.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface RemoteServerLauncherConstants
{

	/**
	 * Default daemon port, 4075.
	 */
	public static final int DEFAULT_DAEMON_PORT = 4075;

	/**
	 * Default REXEC port, 512.
	 */
	public static final int DEFAULT_REXEC_PORT = 512;

	/**
	 * Default REXEC path, "/opt/rseserver/".
	 */
	public static final String DEFAULT_REXEC_PATH = "/opt/rseserver/"; //$NON-NLS-1$

	/**
	 * Linux REXEC script command, "perl ./server.pl".
	 */
	public static final String LINUX_REXEC_SCRIPT = "sh -c \"PATH=/opt/j2sdk1.4.2/bin:$PATH; export PATH; perl ./server.pl\""; //$NON-NLS-1$

	/**
	 * Unix REXEC script command, "./server.sh".
	 */
	public static final String UNIX_REXEC_SCRIPT = "sh -c \"PATH=/opt/j2sdk1.4.2/bin:$PATH; export PATH; sh server.sh\""; //$NON-NLS-1$

	/**
	 * Default REXEC script command. TIt is equivalent to <code>LINUX_REXEC_SCRIPT</code>.
	 */
	public static final String DEFAULT_REXEC_SCRIPT = LINUX_REXEC_SCRIPT;
}