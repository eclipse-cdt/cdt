/*******************************************************************************
 * Copyright (c) 2006, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class implements process pool management for internal builder 
 *
 * NOTE: This class is subject to change and discuss, 
 * and is currently available in experimental mode only
 */
public class BuildProcessManager {
	protected OutputStream out;
	protected OutputStream err;
	protected boolean show;
	protected ProcessLauncher[] processes;
	protected int maxProcesses;  
	
//	 Number of CPUs is not dependent of object instance.
//   But user can change UI settings for processes number.
//   So we cannot set procNumber directly to maxProcesses. 	
	static int procNumber = 0;
	
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
		processes = new ProcessLauncher[maxProcesses];
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
		if (hasEmpty()) {
			int i = 0;
			for (; i < maxProcesses; i++) {
				if (processes[i] == null || processes[i].queryState() == ProcessLauncher.STATE_DONE) {
					break;
				}
			}
			
			if (i < maxProcesses) {
				processes[i] = new ProcessLauncher(cmd.getCommand(), cmd.getArgs(), mapToStringArray(cmd.getEnvironment()), cwd, out, err, monitor, show);
				processes[i].launch();
				return processes[i];
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
		ProcessLauncher result = null;
		
		for (int i = 0; i < maxProcesses; i++) {
			if (processes[i] != null) {
				int state = processes[i].queryState();
				if (state != ProcessLauncher.STATE_RUNNING) {
					if (state != ProcessLauncher.STATE_DONE && result == null)
						result = processes[i];
				}
			}
		}
		
		return result;
	}

	/**
	 * Checks states of all currently running processes. 
	 */
	public boolean hasEmpty() {
		for (int i = 0; i < maxProcesses; i++) {
			if (processes[i] == null) 
				return true;
			else {
				if (processes[i].queryState() != ProcessLauncher.STATE_RUNNING) 
					return true;
			}
		}
		return false;
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
	 */
	static public int checkCPUNumber() {
		if (procNumber > 0) return procNumber;
		
		procNumber = 1;
		int x = 0;
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os != null) {
			if (os.startsWith("Win")) { //$NON-NLS-1$
				IEnvironmentVariableProvider evp = ManagedBuildManager.getEnvironmentVariableProvider();
				if (evp != null) {
					IBuildEnvironmentVariable var = evp.getVariable("NUMBER_OF_PROCESSORS", null, false, false); //$NON-NLS-1$
					if (var != null) {
						try {
							x = new Integer(var.getValue()).intValue();
							if (x > 0) { procNumber = x; }
						} catch (NumberFormatException e) {} // fallthrough and return default
					}
				}
			} else { // linux
				String p = "/proc/cpuinfo"; //$NON-NLS-1$
				try {
					BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(p)));
					String s;
					while ((s = r.readLine() ) != null ) 
					   { if (s.startsWith("processor\t:")) x++; } //$NON-NLS-1$
					r.close();
					if (x > 0) { procNumber = x; }
				} 
				catch (IOException e) {} // fallthrough and return default
			}
		}
		if(DbgUtil.DEBUG)
			DbgUtil.trace("Number of processors detected: " + procNumber);	//$NON-NLS-1$
		return procNumber;
	}
}