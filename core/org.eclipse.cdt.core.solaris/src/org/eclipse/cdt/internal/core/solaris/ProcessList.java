/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.solaris;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

/**
 * Insert the type's description here.
 * @see IProcessList
 */
public class ProcessList implements IProcessList {
	
	ProcessInfo[] empty = new ProcessInfo[0];
	
	public ProcessList() {
	}
	
	/**
	 * Insert the method's description here.
	 * @see IProcessList#getProcessList
	 */
	public IProcessInfo [] getProcessList()  {
		Process ps;
		BufferedReader psOutput;
		String[] args = {"/usr/bin/ps", "-e", "-o", "pid,args"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		try {
			ps = ProcessFactory.getFactory().exec(args);
			psOutput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		} catch(Exception e) {
			return new IProcessInfo[0];
		}
		
		//Read the output and parse it into an array list
		ArrayList procInfo = new ArrayList();

		try {
			String lastline;
			while ((lastline = psOutput.readLine()) != null) {
				//The format of the output should be 
				//PID space name

				lastline = lastline.trim();
				int index = lastline.indexOf(' ');
				if (index != -1) {
					String pidString = lastline.substring(0, index).trim();
					try {
						int pid = Integer.parseInt(pidString);
						String arg = lastline.substring(index + 1);
						procInfo.add(new ProcessInfo(pid, arg));
					} catch (NumberFormatException e) {
					}
				}
			}
		
		} catch(Exception e) {
			/* Ignore */
		}
		
		ps.destroy();
		return (IProcessInfo [])procInfo.toArray(new IProcessInfo[procInfo.size()]);
	}
	
}
