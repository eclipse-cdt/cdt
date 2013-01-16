/*******************************************************************************
 * Copyright (c) 2006, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class implements process pool management for internal builder 
 */
public class BuildProcessManager {
	protected OutputStream out;
	protected OutputStream err;
	protected boolean show;
	protected Vector<ProcessLauncher> processes;
	protected int maxProcesses;
	
	/**
	 * Initializes process manager
	 * 
	 * @param _out Output stream
	 * @param _err Error output stream
	 * @param _show If true, print command line before launching
	 */
	public BuildProcessManager(OutputStream _out, OutputStream _err, boolean _show, int _procNumber) {
		out = _out;
		err = _err;
		show = _show;
		maxProcesses = _procNumber;
		processes = new Vector<ProcessLauncher>(Math.min(10, maxProcesses), 10);
	}
	
	/**
	 * Returns maximum number of processes
	 */
	public int getMaxProcesses() {
		return maxProcesses;
	}
	
	/**
	 * Performs an attempt to launch new process. Returns BuildProcessLauncher 
	 * if it was successfully launched, null if there is no room for it yet in 
	 * the process pool.
	 * 
	 * @param cmd Command to launch
	 * @param cwd Command working directory
	 * @param monitor Progress monitor for this task 
	 */
	public ProcessLauncher launchProcess(IBuildCommand cmd, IPath cwd, IProgressMonitor monitor) {
		for (int i = 0; i < maxProcesses; i++) {
			if (i >= processes.size()) {
				ProcessLauncher process = new ProcessLauncher(cmd.getCommand(), cmd.getArgs(), mapToStringArray(cmd.getEnvironment()), cwd, out, err, monitor, show);
				processes.add(process);
				process.launch();
				return process;
				
			}
			if (processes.get(i).queryState() == ProcessLauncher.STATE_DONE) {
				ProcessLauncher process = new ProcessLauncher(cmd.getCommand(), cmd.getArgs(), mapToStringArray(cmd.getEnvironment()), cwd, out, err, monitor, show);
				processes.set(i, process);
				process.launch();
				return process;
			}
		}
		return null;
	}
	
	/**
	 * Checks states of all currently running processes. If it finds 
	 * one with state other than STATE_DONE or STATE_RUNNING, it is
	 * returned as a result. Otherwise this method returns null.
	 */
	public ProcessLauncher queryStates() {
		for (ProcessLauncher process : processes) {
			int state = process.queryState();
			if (state != ProcessLauncher.STATE_RUNNING && state != ProcessLauncher.STATE_DONE)
				return process;
		}
		
		return null;
	}

	/**
	 * Checks states of all currently running processes. 
	 */
	public boolean hasEmpty() {
		if (processes.size() < maxProcesses)
			return true;
		
		for (ProcessLauncher process : processes) {
			if (process.queryState() != ProcessLauncher.STATE_RUNNING) 
				return true;
		}
		return false;
	}

	/**
	 * Returns maximum threads used up to that point
	 */
	public int getThreadsUsed() {
		return processes.size();
	}
	

	
	/**
	 * Converts map to strings array
	 */
	protected String[] mapToStringArray(Map<String, String> map){
		if(map == null)
			return null;
		
		List<String> list = new ArrayList<String>();
		
		Set<Entry<String, String>> entrySet = map.entrySet();
		for (Entry<String, String> entry : entrySet) {
			list.add(entry.getKey() + '=' + entry.getValue());
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * @return Number of processors detected
	 * @deprecated since CDT 9.0 - just use Runtime.getRuntime().availableProcessors()
	 */
	@Deprecated
	static public int checkCPUNumber() {
		return Runtime.getRuntime().availableProcessors();
	}
}