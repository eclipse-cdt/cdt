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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;


public class UniversalZOSProcessHandler extends UniversalAIXProcessHandler
{
	private static final String[] processAttributes = {"pid","ppid","comm","uid","user","gid","vsz","state"};
	private static final String firstColumnHeader = "PID";
	private HashMap stateMap;
	
	/**
	 * Creates a new ProcessHandler for z/OS platforms. 
	 */
	public UniversalZOSProcessHandler()
	{
		super();
		stateMap = new HashMap();
		for (int i = STATE_ZOS_STARTING_INDEX; i < STATE_ZOS_ENDING_INDEX; i++)
		{
			stateMap.put(new Character(ALL_STATES[i]), ALL_STATES_STR[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.etools.systems.processes.clientserver.handlers.ProcessHandler#lookupProcesses(com.ibm.etools.systems.processes.clientserver.RemoteProcessFilterString)
	 */
	public SortedSet lookupProcesses(HostProcessFilterImpl rpfs)
	throws Exception
	{
		SortedSet results = new TreeSet(new ProcessComparator());
		
		// create the remote command with the AIX specific attributes
		String cmdLine = "ps -A -o ";
		for (int i = 0; i < processAttributes.length; i++)
		{
			cmdLine = cmdLine + processAttributes[i];
			if ((processAttributes.length - i > 1)) cmdLine = cmdLine + ",";
		}
		// run the command and get output
		Process ps = Runtime.getRuntime().exec(cmdLine);
		InputStreamReader isr = new InputStreamReader(ps.getInputStream());
		if (isr == null) return null;
		BufferedReader reader = new BufferedReader(isr);
		if (reader == null) return null;
		String nextLine = reader.readLine();
		if (nextLine != null && nextLine.trim().startsWith(firstColumnHeader)) nextLine = reader.readLine();
		while (nextLine != null)
		{
			String statusLine = "";
			// put the details of each process into a hashmap
			HashMap psLineContents = getPSOutput(nextLine);
			if (psLineContents == null)
			{
				nextLine = reader.readLine();
				continue;
			}
		
			String pid = (String) psLineContents.get("pid");
			statusLine = pid + "|";
			
			// add the name to the status string
			String name = (String) psLineContents.get("comm");
			if (name == null) name = " ";
			statusLine = statusLine + name + "|";
		
			// add the status letter to the status string
			String state = (String) psLineContents.get("state");
			if (state == null) state = " ";
			String stateCode = convertToStateCode(state);
			statusLine = statusLine + stateCode + "|";
			
			// add the Tgid
			String tgid = (String) psLineContents.get("tgid");
			if (tgid == null) tgid = " ";
			statusLine = statusLine + tgid + "|";				
			
			// add the Ppid
			String pPid = (String) psLineContents.get("ppid");
			if (pPid == null) pPid = " ";
			statusLine = statusLine + pPid + "|";
		
			// add the TracerPid
			String tracerpid = (String) psLineContents.get("tracerpid");
			if (tracerpid == null) tracerpid = " ";
			statusLine = statusLine + tracerpid + "|";
			
			String uid = (String) psLineContents.get("uid");
			if (uid == null) uid = " ";
			statusLine = statusLine + uid + "|"; // add the uid to the status string
		
			String username = (String) psLineContents.get("user");
			if (username == null) username = " ";
			statusLine = statusLine + username + "|"; // add the username to the status string
			
			// add the gid to the status string
			String gid = (String) psLineContents.get("gid");
			if (gid == null) gid = " ";
			statusLine = statusLine + gid + "|";
			
			// add the VmSize to the status string
			String vmsize = (String) psLineContents.get("vsz");
			if (vmsize == null) vmsize = " ";
			statusLine = statusLine + vmsize +"|";
			
			// add a dummy vmrss to the status string
			// vmRss is not available on ZOS
			String vmrss = " ";
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

	/**
	 * Parses one line of output from the ps command - placing the contents into
	 * a hashmap, where the keys are the names of process attributes and the values
	 * are the attribute values
	 * @param nextLine a line of output from the ps command
	 * @return a map of names-values of process attributes
	 */
	protected HashMap getPSOutput(String nextLine)
	{	
		HashMap contents = new HashMap();
		String[] values = nextLine.trim().split("\\s+");
		if (values == null || values.length < processAttributes.length) return null;
		for (int i = 0; i < processAttributes.length; i++)
		{
			contents.put(processAttributes[i], values[i]);
		}
		return contents;
	}

	/**
	 * Return the unique state code assocated with the state given by
	 * the ps listing on the z/OS machine.
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