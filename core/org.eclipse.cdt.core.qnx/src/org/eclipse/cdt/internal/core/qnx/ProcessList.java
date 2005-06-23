/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.qnx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
		Process pidin;
		BufferedReader pidinOutput;
		String[] args = {"pidin", "-fan" }; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			pidin = ProcessFactory.getFactory().exec(args);
			pidinOutput = new BufferedReader(new InputStreamReader(pidin.getInputStream()));
		} catch(Exception e) {
			return getProcessListPureJava();
		}
		
		//Read the output and parse it into an array list
		ArrayList procInfo = new ArrayList();

		String pidStr, nameStr, lastline;
		try {
			while((lastline = pidinOutput.readLine()) != null) {
				//The format of the output should be 
				//PID a/slash/delimited/name
		
				StringTokenizer tok = new StringTokenizer(lastline.trim());
				pidStr = tok.nextToken();
				if(pidStr == null || pidStr.charAt(0) < '0' || pidStr.charAt(0) > '9') {
					continue;
				}

				nameStr = tok.nextToken();

				int index = nameStr.lastIndexOf('/');			
				if(index != -1) {
					nameStr = nameStr.substring(index + 1);
				}

				procInfo.add(new ProcessInfo(pidStr, nameStr));
			}
		
			pidin.destroy();
		} catch(Exception e) {
			/* Ignore */
		} finally {
			pidin.destroy();
		}
		
		return (IProcessInfo [])procInfo.toArray(new IProcessInfo[procInfo.size()]);
	}

	/**
	 * This is our current backup strategy for getting the pid list 
	 * (reading /proc directly).  Currently the exename is not implemented
	 * so the names will all show up as unknown, but at least you get a
	 * pid list.
	 */
	private IProcessInfo [] getProcessListPureJava() {
		File proc = new File("/proc"); //$NON-NLS-1$
		File[] pidFiles = null;
		
		// We are only interrested in the pid so filter the rest out.
		try {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					boolean isPID = false;
					try {
						Integer.parseInt(name);
						isPID = true;
					} catch (NumberFormatException e) {
					}
					return isPID;
				}
			};
			pidFiles = proc.listFiles(filter);
		} catch (SecurityException e) {
		}
		
		ProcessInfo[] processInfo = empty;
		if (pidFiles != null) {
			processInfo = new ProcessInfo[pidFiles.length];
			for (int i = 0; i < pidFiles.length; i++) {
				File cmdLine = new File(pidFiles[i], "exename"); //$NON-NLS-1$
				StringBuffer line = new StringBuffer();
				try {
					FileReader reader = new FileReader(cmdLine);
					int c;
					while ((c = reader.read()) > 0) {
						line.append((char)c);
					}
				} catch (IOException e) {
				}
				String name = line.toString();
				if (name.length() == 0) {
					name = "Unknown"; //$NON-NLS-1$
				}
				processInfo[i] = new ProcessInfo(pidFiles[i].getName(), name);
			}
		} else {
			pidFiles = new File[0];
		}
		return processInfo;		
	}
}
