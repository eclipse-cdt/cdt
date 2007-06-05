/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
 * {Name} (company) - description of contribution.
 *******************************************************************************/

package org.eclipse.rse.services.clientserver.processes.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;

public class UniversalLinuxProcessHandler implements ProcessHandler
{

	protected HashMap _usernamesByUid;
	protected HashMap _uidsByUserName;
	private HashMap stateMap;
	
	/**
	 * Creates a new ProcessHandler for Linux platforms. 
	 */	
	public UniversalLinuxProcessHandler()
	{
		stateMap = new HashMap();
		for (int i = ISystemProcessRemoteConstants.STATE_STARTING_INDEX; i < ISystemProcessRemoteConstants.STATE_ENDING_INDEX; i++)
		{
			stateMap.put(new Character(ISystemProcessRemoteConstants.ALL_STATES[i]), ISystemProcessRemoteConstants.ALL_STATES_STR[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.ProcessHandler#kill
	 */
	public IHostProcess kill(IHostProcess process, String type) throws Exception
	{
		if (type.equals(ISystemProcessRemoteConstants.PROCESS_SIGNAL_TYPE_DEFAULT)) type = ""; //$NON-NLS-1$
		else type = "-" + type; //$NON-NLS-1$
		// formulate command to send kill signal
		String cmdLine = "kill " + type + " " + process.getPid(); //$NON-NLS-1$ //$NON-NLS-2$
		Runtime.getRuntime().exec(cmdLine);
		
		// after the kill command is executed, the process might have changed
		// attributes, or might be gone, so requery
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid("" + process.getPid()); //$NON-NLS-1$
		SortedSet results = lookupProcesses(rpfs);
		if (results == null || results.size() == 0) return null;
		else return (IHostProcess) results.first();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.ProcessHandler#lookupProcesses
	 */
	public SortedSet lookupProcesses(IHostProcessFilter rpfs) throws Exception
	{
		File procDir = new File("/proc");  //$NON-NLS-1$
		
		if (!procDir.exists())
			throw new Exception(IServiceConstants.FAILED_WITH_DOES_NOT_EXIST);
		
		if (!procDir.canRead())
			throw new Exception(IServiceConstants.FAILED_WITH_SECURITY);
				
		// list all subdirectories of /proc
		File[] processes;
		if (rpfs.getPid().indexOf("*") == -1) //$NON-NLS-1$
		{
			processes = new File[1];
			processes[0] = new File(procDir, rpfs.getPid());
		}
		else processes = procDir.listFiles();

		SortedSet results = new TreeSet(new ProcessComparator());
		
		for (int i = 0; i < processes.length; i++)
		{
			// make sure the directory is a process directory (an integer)
			try 
				{ Integer.valueOf(processes[i].getName()); }
			catch (NumberFormatException e)
				{ continue; }
			
			String statusLine = ""; //$NON-NLS-1$
			try
			{
				// open the file containing the human-readable status info for the process
				File statusFile = new File(processes[i], "status"); //$NON-NLS-1$
				if (!statusFile.exists() || !statusFile.canRead())
					continue;
				
				// read the status info from the stat file
				FileReader fr = new FileReader(statusFile);
				BufferedReader reader = new BufferedReader(fr);
	
				HashMap statusFileContents = getStatusFileContents(reader);
				
				statusLine = processes[i].getName() + "|"; // add the pid to the status string //$NON-NLS-1$
	
				// add the name to the status string
				String name = (String) statusFileContents.get("name"); //$NON-NLS-1$
				if (name == null) name = " "; //$NON-NLS-1$
				//if (!pexematcher.matches(name)) continue;
				statusLine = statusLine + name + "|"; //$NON-NLS-1$
	
				// add the status letter to the status string
				String state = (String) statusFileContents.get("state"); //$NON-NLS-1$
				if (state == null) state = " "; //$NON-NLS-1$
				String stateCode = convertToStateCode(state);
				statusLine = statusLine + stateCode + "|"; //$NON-NLS-1$
				
				// add the Tgid
				String tgid = (String) statusFileContents.get("tgid"); //$NON-NLS-1$
				if (tgid == null) tgid = " "; //$NON-NLS-1$
				statusLine = statusLine + tgid + "|";				 //$NON-NLS-1$
				
				// add the Ppid
				String pPid = (String) statusFileContents.get("ppid"); //$NON-NLS-1$
				if (pPid == null) pPid = " "; //$NON-NLS-1$
				//if (!ppidmatcher.matches(pPid)) continue;
				statusLine = statusLine + pPid + "|"; //$NON-NLS-1$
	
				// add the TracerPid
				String tracerpid = (String) statusFileContents.get("tracerpid"); //$NON-NLS-1$
				if (tracerpid == null) tracerpid = " "; //$NON-NLS-1$
				statusLine = statusLine + tracerpid + "|"; //$NON-NLS-1$
				
				String uid = (String) statusFileContents.get("uid"); //$NON-NLS-1$
				if (uid == null) uid = " "; //$NON-NLS-1$
				statusLine = statusLine + uid + "|"; // add the uid to the status string //$NON-NLS-1$
	
				String username = getUsername(uid);
				if (username == null) username = " "; //$NON-NLS-1$
				statusLine = statusLine + username + "|"; // add the username to the status string //$NON-NLS-1$
				
				// add the gid to the status string
				String gid = (String) statusFileContents.get("gid"); //$NON-NLS-1$
				if (gid == null) gid = " "; //$NON-NLS-1$
				statusLine = statusLine + gid + "|"; //$NON-NLS-1$
				
				// add the VmSize to the status string
				String vmsize = (String) statusFileContents.get("vmsize"); //$NON-NLS-1$
				if (vmsize == null) vmsize = " "; //$NON-NLS-1$
				statusLine = statusLine + vmsize + "|"; //$NON-NLS-1$
				
				// add the VmRSS to the status string
				String vmrss = (String) statusFileContents.get("vmrss"); //$NON-NLS-1$
				if (vmrss == null) vmrss = " "; //$NON-NLS-1$
				statusLine = statusLine + vmrss;
	
				reader.close();
				fr.close();
			}
			catch (Exception e)
			{
				continue;
			}			
			if (rpfs.allows(statusLine))
			{
				UniversalServerProcessImpl usp = new UniversalServerProcessImpl(statusLine);
				results.add(usp);
			}	
		} // for loop
		return results;
	}

	/**
	 * Gets the uid associated with the given username on this system
	 */
	public String getUid(String username)
	{
		if (_uidsByUserName == null) populateUsernames();
		return (String) _uidsByUserName.get(username);	
	}
	
	/**
	 * Gets the username associated with the given uid on this system
	 */
	public String getUsername(String uid)
	{
		if (_usernamesByUid == null) populateUsernames();
		return (String) _usernamesByUid.get(uid);	
	}
	
	/**
	 * Given a handle to the status file for a process, parses the data
	 * in that status file and returns it as a key-value hashmap of
	 * attributes.
	 * @param reader A buffered reader of the status file
	 * @return a hashmap indexed by attribute name
	 */
	private HashMap getStatusFileContents(BufferedReader reader)
	{
		
		HashMap contents = new HashMap();
		String nextLine;
		try
		{
			while ((nextLine = reader.readLine()) != null)
			{
				String key = nextLine.substring(0, nextLine.indexOf(":")).trim().toLowerCase(); //$NON-NLS-1$
				String theRest = processStatusLine(nextLine, -1);
				StringTokenizer tz = new StringTokenizer(theRest);
				String value = null;
				if (tz.hasMoreTokens()) value = tz.nextToken();
				if (key != null && value != null) contents.put(key, value);
			}
		}
		catch (Exception e)
		{
		}
		return contents;
	}
	
	/**
	 * Returns all or part of <code>line</code> after the first ':' character, trimmed.
	 * @param line the line to process
	 * @param length the number of characters (after trimming) to return (-1 for all)
	 */
	private String processStatusLine(String line, int length)
	{
		if (length == -1)
		{
			return line.substring(line.indexOf(":") + 1).trim(); //$NON-NLS-1$
		}
		else
			return line.substring(line.indexOf(":") + 1).trim().substring(0, length); //$NON-NLS-1$
	}
	
	/**
	 * Populates the internal hashmap with uid/username info
	 */
	private void populateUsernames()
	{
		_usernamesByUid = new HashMap();
		_uidsByUserName = new HashMap();
				
		try
		{
			// read the uid info using the getent command
			Process ps = Runtime.getRuntime().exec("getent passwd"); //$NON-NLS-1$
			InputStreamReader isr = new InputStreamReader(ps.getInputStream());

			BufferedReader reader = new BufferedReader(isr);
			
			String nextLine;
			
			while ((nextLine = reader.readLine()) != null)
			{ 
				String[] fields = nextLine.split(":"); //$NON-NLS-1$
				int length = fields.length;
				if (length < 3) continue;
				String uid = fields[2];
				String username = fields[0];
				if (uid != null && username != null)
				{
					_usernamesByUid.put(uid, username);
					_uidsByUserName.put(username, uid);
				}
			}
			reader.close();
			isr.close();
		}
		catch (IOException e)
		{ return; }
		catch (Exception e)
		{ return; }
	}
	
	/**
	 * Return the unique state code assocated with the state given by
	 * the status file on the Linux machine.
	 */
	protected String convertToStateCode(String state)
	{
		String stateCode = " "; //$NON-NLS-1$
		if (state == null) return stateCode;
		if (state.trim().equals("")) return stateCode; //$NON-NLS-1$
		for (int i = 0; i < state.length(); i++)
		{
			String nextState = (String) stateMap.get(new Character(state.charAt(i)));
			if (nextState != null)
			{
				stateCode = stateCode + nextState;
				if (i < state.length() - 1) stateCode = stateCode + ","; //$NON-NLS-1$
			}
		}
		if (stateCode.trim().equals("")) return " "; //$NON-NLS-1$ //$NON-NLS-2$
		else return stateCode.trim();
	}
}
