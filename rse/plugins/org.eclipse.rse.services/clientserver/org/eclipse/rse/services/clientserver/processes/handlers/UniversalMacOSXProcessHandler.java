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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.rse.services.clientserver.processes.HostProcessFilterImpl;
import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.services.clientserver.processes.IHostProcessFilter;
import org.eclipse.rse.services.clientserver.processes.ISystemProcessRemoteConstants;

public class UniversalMacOSXProcessHandler implements ProcessHandler {
	private static final Map stateMap = new HashMap();
	
	static {
		String[] strings = ISystemProcessRemoteConstants.ALL_STATES_STR;
		stateMap.put("I", strings[ISystemProcessRemoteConstants.STATE_IDLE_INDEX]); //$NON-NLS-1$
		stateMap.put("R", strings[ISystemProcessRemoteConstants.STATE_RUNNING_INDEX]); //$NON-NLS-1$
		stateMap.put("S", strings[ISystemProcessRemoteConstants.STATE_SLEEPING_INDEX]); //$NON-NLS-1$
		stateMap.put("T", strings[ISystemProcessRemoteConstants.STATE_NONEXISTENT_INDEX]); //$NON-NLS-1$
		stateMap.put("U", strings[ISystemProcessRemoteConstants.STATE_WAITING_INDEX]); //$NON-NLS-1$
		stateMap.put("Z", strings[ISystemProcessRemoteConstants.STATE_ZOMBIE_INDEX]); //$NON-NLS-1$
	}

	/**
	 * Creates a new ProcessHandler for Mac OS X platforms. 
	 */
	public UniversalMacOSXProcessHandler() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.handlers.ProcessHandler#lookupProcesses
	 */
	public SortedSet lookupProcesses(IHostProcessFilter rpfs) throws Exception {
		SortedSet results = new TreeSet(new ProcessComparator());
		// Using -A is problematic - the command never returns! Using -a for now.
		String command = "/bin/ps -awwo pid,ucomm,state,ppid,uid,user,gid,vsz,rss";  //$NON-NLS-1$
		Process ps = Runtime.getRuntime().exec(command);
		InputStreamReader isr = new InputStreamReader(ps.getInputStream());
		BufferedReader reader = new BufferedReader(isr);
		String line = reader.readLine(); // Header line
		line = reader.readLine();
		while (line != null) {
			// Input line looks like "pid ucomm state ppid uid user gid vsz rss"
			String[] words = line.trim().split("\\s+"); //$NON-NLS-1$
			UniversalServerProcessImpl usp = new UniversalServerProcessImpl();
			usp.setPid(words[0]);
			usp.setName(words[1]);
			usp.setState(convertToStateCode(words[2]));
			usp.setPPid(words[3]);
			usp.setUid(words[4]);
			usp.setUsername(words[5]);
			usp.setGid(words[6]);
			usp.setVmSizeInKB(words[7]);
			usp.setVmRSSInKB(words[8]);
			usp.setTgid(""); //$NON-NLS-1$
			usp.setTracerPid(""); //$NON-NLS-1$
			if (rpfs.allows(usp.getAllProperties())) {
				results.add(usp);
			}
			line = reader.readLine();
		}
		reader.close();
		isr.close();
		return results;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.clientserver.processes.ProcessHandler#kill
	 */
	public IHostProcess kill(IHostProcess process, String type) throws Exception {
		if (type.equals(ISystemProcessRemoteConstants.PROCESS_SIGNAL_TYPE_DEFAULT)) {
			type = ""; //$NON-NLS-1$
		} else {
			type = "-" + type; //$NON-NLS-1$
		}

		// formulate command to send kill signal
		String cmdLine = "kill " + type + " " + process.getPid(); //$NON-NLS-1$ //$NON-NLS-2$
		Runtime.getRuntime().exec(cmdLine);

		// after the kill command is executed, the process might have changed
		// attributes, or might be gone, so requery
		HostProcessFilterImpl rpfs = new HostProcessFilterImpl();
		rpfs.setPid("" + process.getPid()); //$NON-NLS-1$
		SortedSet results = lookupProcesses(rpfs);
		if (results == null || results.size() == 0) {
			return null;
		} else {
			return (IHostProcess) results.first();
		}
	}

	/**
	 * Return the unique state code assocated with the state given by
	 * the ps listing on Mac OS X.
	 */
	protected String convertToStateCode(String state) {
		String key = state.substring(0, 1);
		String stateCode = (String) stateMap.get(key);
		if (stateCode == null) {
			stateCode = Character.toString(ISystemProcessRemoteConstants.STATE_RUNNING);
		}
		return stateCode;
	}
	
}
