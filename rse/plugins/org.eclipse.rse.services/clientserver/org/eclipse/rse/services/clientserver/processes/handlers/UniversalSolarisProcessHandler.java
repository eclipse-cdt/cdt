/********************************************************************************
 * Copyright (c) 2009 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David McKnight      (IBM) - [175293] [dstore] Processes do not work on Dstore-UNIX connection to Solaris
 ********************************************************************************/
package org.eclipse.rse.services.clientserver.processes.handlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;

/**
 * ProcessHandler implementation for Solaris. This is part of internal
 * implementation, and should not be used by clients directly. Use
 * {@link ProcessHandlerManager#getNewProcessHandler()} on a Solaris system
 * instead.
 *
 * @since 3.1 (actually 3.1.1)
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class UniversalSolarisProcessHandler extends UniversalAIXProcessHandler {

	private static final String[] processAttributes = {"pid","ppid","comm","uid","user","gid","vsz","s"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	private static final String firstColumnHeader = "PID"; //$NON-NLS-1$


	public IHostProcess kill(IHostProcess process, String type)
			throws Exception {
		return super.kill(process, type);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandler#lookupProcesses
	 */
	public SortedSet lookupProcesses(IHostProcessFilter rpfs)
	throws Exception
	{
		SortedSet results = new TreeSet(new ProcessComparator());

		// create the remote command with the UNIX specific attributes
		String cmdLine = "/usr/bin/ps -A -o "; //$NON-NLS-1$
		for (int i = 0; i < processAttributes.length; i++)
		{
			cmdLine = cmdLine + processAttributes[i];
			if ((processAttributes.length - i > 1)) cmdLine = cmdLine + ","; //$NON-NLS-1$
		}
		// run the command and get output
		Process ps = Runtime.getRuntime().exec(cmdLine);
		InputStreamReader isr = new InputStreamReader(ps.getInputStream());

		BufferedReader reader = new BufferedReader(isr);

		String nextLine = reader.readLine();
		if (nextLine != null && nextLine.trim().startsWith(firstColumnHeader)) nextLine = reader.readLine();
		while (nextLine != null)
		{
			String statusLine = ""; //$NON-NLS-1$
			// put the details of each process into a hashmap
			HashMap psLineContents = getPSOutput(nextLine);
			if (psLineContents == null)
			{
				nextLine = reader.readLine();
				continue;
			}

			String pid = (String) psLineContents.get("pid"); //$NON-NLS-1$
			statusLine = pid + "|"; //$NON-NLS-1$

			// add the name to the status string
			String name = (String) psLineContents.get("comm"); //$NON-NLS-1$
			if (name == null) name = " "; //$NON-NLS-1$
			statusLine = statusLine + name + "|"; //$NON-NLS-1$

			// add the status letter to the status string
			String state = (String) psLineContents.get("s"); //$NON-NLS-1$
			if (state == null) state = " "; //$NON-NLS-1$
			String stateCode = convertToStateCode(state);
			statusLine = statusLine + stateCode + "|"; //$NON-NLS-1$

			// add the Tgid
			String tgid = (String) psLineContents.get("tgid"); //$NON-NLS-1$
			if (tgid == null) tgid = " "; //$NON-NLS-1$
			statusLine = statusLine + tgid + "|";				 //$NON-NLS-1$

			// add the Ppid
			String pPid = (String) psLineContents.get("ppid"); //$NON-NLS-1$
			if (pPid == null) pPid = " "; //$NON-NLS-1$
			statusLine = statusLine + pPid + "|"; //$NON-NLS-1$

			// add the TracerPid
			String tracerpid = (String) psLineContents.get("tracerpid"); //$NON-NLS-1$
			if (tracerpid == null) tracerpid = " "; //$NON-NLS-1$
			statusLine = statusLine + tracerpid + "|"; //$NON-NLS-1$

			String uid = (String) psLineContents.get("uid"); //$NON-NLS-1$
			if (uid == null) uid = " "; //$NON-NLS-1$
			statusLine = statusLine + uid + "|"; // add the uid to the status string //$NON-NLS-1$

			String username = (String) psLineContents.get("user"); //$NON-NLS-1$
			if (username == null) username = " "; //$NON-NLS-1$
			statusLine = statusLine + username + "|"; // add the username to the status string //$NON-NLS-1$

			// add the gid to the status string
			String gid = (String) psLineContents.get("gid"); //$NON-NLS-1$
			if (gid == null) gid = " "; //$NON-NLS-1$
			statusLine = statusLine + gid + "|"; //$NON-NLS-1$

			// add the VmSize to the status string
			String vmsize = (String) psLineContents.get("vsz"); //$NON-NLS-1$
			if (vmsize == null) vmsize = " "; //$NON-NLS-1$
			statusLine = statusLine + vmsize +"|"; //$NON-NLS-1$

			// add a dummy vmrss to the status string
			// vmRss is not available on ZOS
			String vmrss = " "; //$NON-NLS-1$
			statusLine = statusLine + vmrss;

			if (rpfs.allows(statusLine))
			{
				UniversalServerProcessImpl usp = new UniversalServerProcessImpl(statusLine);
				results.add(usp);
			}
			nextLine = reader.readLine();
		}
		reader.close();
		isr.close();
		if (results.size() == 0) return null;
		return results;
	}

}
