/*******************************************************************************
 * Copyright (c) 2008, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation          
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.gdb.launching.IProcessExtendedInfo;

@Immutable
class ProcessInfo implements IProcessExtendedInfo, Comparable<ProcessInfo> {
	private final int pid;
	private final String name;
	private final String[] cores;
	private final String ownerId;
	
	public ProcessInfo(int pid, String name) {
		this(pid, name, null, null);
	}
	
	/** @since 2.2 */
	public ProcessInfo(int pid, String name, String[] cores, String owner) {
		this.pid = pid;
		this.name = name;
		this.cores = cores;
		this.ownerId = owner;
	}
	
	public String getName() {
		return name;
	}

	public int getPid() {
		return pid;
	}

	public String[] getCores() {
		return cores;
	}

	public String getOwner() {
		return ownerId;
	}
	
	/**
	 * Sort by name, then by pid.
	 * No need to sort any further since pids are unique.
	 */
	public int compareTo(ProcessInfo other) {
	    int nameCompare = getName().compareTo(other.getName());
	    if (nameCompare != 0) return nameCompare;
	    else return (getPid() < other.getPid()) ? -1 : 1;
	}

}