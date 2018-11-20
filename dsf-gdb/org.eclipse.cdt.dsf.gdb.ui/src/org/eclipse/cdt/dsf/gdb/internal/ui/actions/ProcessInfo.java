/*******************************************************************************
 * Copyright (c) 2008, 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.gdb.launching.IProcessExtendedInfo;

@Immutable
public class ProcessInfo implements IProcessExtendedInfo, Comparable<ProcessInfo> {
	private final int pid;
	private final String name;
	private final String[] cores;
	private final String ownerId;
	private final String description;

	public ProcessInfo(int pid, String name) {
		this(pid, name, null, null);
	}

	/** @since 2.2 */
	public ProcessInfo(int pid, String name, String[] cores, String owner) {
		this(pid, name, cores, owner, null);
	}

	/**
	 * @since 2.6
	 */
	public ProcessInfo(int pid, String name, String[] cores, String owner, String description) {
		this.pid = pid;
		this.name = name;
		this.cores = cores;
		this.ownerId = owner;
		this.description = description;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPid() {
		return pid;
	}

	@Override
	public String[] getCores() {
		return cores;
	}

	@Override
	public String getOwner() {
		return ownerId;
	}

	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Sort by name, then by pid.
	 * No need to sort any further since pids are unique.
	 */
	@Override
	public int compareTo(ProcessInfo other) {
		int nameCompare = getName().compareTo(other.getName());
		if (nameCompare != 0)
			return nameCompare;
		else
			return (getPid() < other.getPid()) ? -1 : 1;
	}

}