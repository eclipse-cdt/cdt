/********************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation. All rights reserved.
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

public class UniversalLinuxProcessHandler implements ProcessHandler, IServiceConstants, ISystemProcessRemoteConstants
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
		for (int i = STATE_STARTING_INDEX; i < STATE_ENDING_INDEX; i++)
		{
			stateMap.put(new Character(ALL_STATES[i]), ALL_STATES_STR[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.universal.processes.ProcessHandler#kill(com.ibm.etools.systems.universal.processes.IRemoteServerProcess, java.lang.String)
	 */
	public IHostProcess kill(IHostProcess process, String type) throws Exception
	{
		if (type.equals(PROCESS_SIGNAL_TYPE_DEFAULT)) type = "";
		else type = "-" + type;
		// formulate command to send kill signal
		String cmdLine = "kill " + type + " " + process.getPid();
		Runtime.getRuntime().exec(cmdLine);
		
		// after the kill command is executed, the process might have changed
		// attributes, or might be gone, so requery
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid("" + process.getPid());
		SortedSet results = lookupProcesses(rpfs);
		if (results == null || results.size() == 0) return null;
		else return (IHostProcess) results.first();
	}

	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.universal.processes.ProcessHandler#lookupProcesses(com.ibm.etools.systems.processes.clientserver.RemoteProcessFilterString, java.lang.String)
	 */
	public SortedSet lookupProcesses(IHostProcessFilter rpfs) throws Exception
	{
		File procDir = new File("/proc"); 
		
		if (!procDir.exists())
			throw new Exception(FAILED_WITH_DOES_NOT_EXIST);
		
		if (!procDir.canRead())
			throw new Exception(FAILED_WITH_SECURITY);
				
		// list all subdirectories of /proc
		File[] processes;
		if (rpfs.getPid().indexOf("*") == -1)
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
			
			String statusLine = "";
			try
			{
				// open the file containing the human-readable status info for the process
				File statusFile = new File(processes[i], "status");
				if (!statusFile.exists() || !statusFile.canRead())
					continue;
				
				// read the status info from the stat file
				FileReader fr = new FileReader(statusFile);
				if (fr == null) continue;
				BufferedReader reader = new BufferedReader(fr);
				if (reader == null) continue;
				HashMap statusFileContents = getStatusFileContents(reader);
				
				statusLine = processes[i].getName() + "|"; // add the pid to the status string
	
				// add the name to the status string
				String name = (String) statusFileContents.get("name");
				if (name == null) name = " ";
				//if (!pexematcher.matches(name)) continue;
				statusLine = statusLine + name + "|";
	
				// add the status letter to the status string
				String state = (String) statusFileContents.get("state");
				if (state == null) state = " ";
				String stateCode = convertToStateCode(state);
				statusLine = statusLine + stateCode + "|";
				
				// add the Tgid
				String tgid = (String) statusFileContents.get("tgid");
				if (tgid == null) tgid = " ";
				statusLine = statusLine + tgid + "|";				
				
				// add the Ppid
				String pPid = (String) statusFileContents.get("ppid");
				if (pPid == null) pPid = " ";
				//if (!ppidmatcher.matches(pPid)) continue;
				statusLine = statusLine + pPid + "|";
	
				// add the TracerPid
				String tracerpid = (String) statusFileContents.get("tracerpid");
				if (tracerpid == null) tracerpid = " ";
				statusLine = statusLine + tracerpid + "|";
				
				String uid = (String) statusFileContents.get("uid");
				if (uid == null) uid = " ";
				statusLine = statusLine + uid + "|"; // add the uid to the status string
	
				String username = getUsername(uid);
				if (username == null) username = " ";
				statusLine = statusLine + username + "|"; // add the username to the status string
				
				// add the gid to the status string
				String gid = (String) statusFileContents.get("gid");
				if (gid == null) gid = " ";
				statusLine = statusLine + gid + "|";
				
				// add the VmSize to the status string
				String vmsize = (String) statusFileContents.get("vmsize");
				if (vmsize == null) vmsize = " ";
				statusLine = statusLine + vmsize + "|";
				
				// add the VmRSS to the status string
				String vmrss = (String) statusFileContents.get("vmrss");
				if (vmrss == null) vmrss = " ";
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
				if (nextLine == null) break;
				String key = nextLine.substring(0, nextLine.indexOf(":")).trim().toLowerCase();
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
			return line.substring(line.indexOf(":") + 1).trim();
		}
		else
			return line.substring(line.indexOf(":") + 1).trim().substring(0, length);
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
			Process ps = Runtime.getRuntime().exec("getent passwd");
			InputStreamReader isr = new InputStreamReader(ps.getInputStream());
			if (isr == null) return;
			BufferedReader reader = new BufferedReader(isr);
			if (reader == null) return;
			
			String nextLine;
			
			while ((nextLine = reader.readLine()) != null)
			{ 
				String[] fields = nextLine.split(":");
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
		String stateCode = " ";
		if (state == null) return stateCode;
		if (state.trim().equals("")) return stateCode;
		for (int i = 0; i < state.length(); i++)
		{
			String nextState = (String) stateMap.get(new Character(state.charAt(i)));
			if (nextState != null)
			{
				stateCode = stateCode + nextState;
				if (i < state.length() - 1) stateCode = stateCode + ",";
			}
		}
		if (stateCode.trim().equals("")) return " ";
		else return stateCode.trim();
	}
}