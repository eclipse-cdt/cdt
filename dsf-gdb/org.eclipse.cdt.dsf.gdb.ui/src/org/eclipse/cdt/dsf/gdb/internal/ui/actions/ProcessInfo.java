/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation          
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.dsf.concurrent.Immutable;

@Immutable
class ProcessInfo implements IProcessInfo, Comparable<ProcessInfo> {
	private final int pid;
	private final String name;
	
	public ProcessInfo(String pidString, String name) {
		int tmpPid = 0;
		try {
			tmpPid = Integer.parseInt(pidString);
		} catch (NumberFormatException e) {
		}
		this.pid = tmpPid;
		this.name = name;
	}
	
	public ProcessInfo(int pid, String name) {
		this.pid = pid;
		this.name = name;
	}
	
	/**
	 * @see org.eclipse.cdt.core.IProcessInfo#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.eclipse.cdt.core.IProcessInfo#getPid()
	 */
	public int getPid() {
		return pid;
	}

	public int compareTo(ProcessInfo other) {
	    int nameCompare = getName().compareTo(other.getName());
	    if (nameCompare != 0) return nameCompare;
	    else return (getPid() < other.getPid()) ? -1 : 1;
	}
}