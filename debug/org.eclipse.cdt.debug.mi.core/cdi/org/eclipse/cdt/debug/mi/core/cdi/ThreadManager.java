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
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoThreads;
import org.eclipse.cdt.debug.mi.core.output.CLIInfoThreadsInfo;


/**
 */
public class ThreadManager extends Manager { //implements ICDIThreadManager {

	static final Thread[] noThreads = new Thread[0];
	HashMap threadMap;

	class ThreadSet {
		ICDIThread[] currentThreads;
		int currentThreadId;
		ThreadSet(ICDIThread[] threads, int id) {
			currentThreads = threads;
			currentThreadId = id;
		}
	}

	public ThreadManager(Session session) {
		super(session, true);
		threadMap = new HashMap();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIThreadManager#getThreads()
	 */
	public ICDIThread[] getThreads() throws CDIException {
		return new ICDIThread[] {};
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIThreadManager#getThreads()
	 */
	public ICDIThread[] getThreads(Target process) throws CDIException {
		ThreadSet set =  (ThreadSet)threadMap.get(process);
		if (set == null) {
			set = getCThreads(process);
			threadMap.put(process, set);
		}
		return set.currentThreads;
	}

	public ThreadSet getCThreads(Target process) throws CDIException {
		Thread[] cthreads = noThreads;
		int currentThreadId = 0;
		MISession mi = process.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		try {
			// HACK/FIXME: gdb/mi thread-list-ids does not
			// show any newly create thread, we workaround by
			// issuing "info threads" instead.
			//MIThreadListIds tids = factory.createMIThreadListIds();
			//MIThreadListIdsInfo info = tids.getMIThreadListIdsInfo();

			CLIInfoThreads tids = factory.createCLIInfoThreads();
			mi.postCommand(tids);
			CLIInfoThreadsInfo info = tids.getMIInfoThreadsInfo();
			int [] ids;
			if (info == null) {
				ids = new int[0];
			} else {
				ids = info.getThreadIds();
			}
			if (ids != null && ids.length > 0) {
				cthreads = new Thread[ids.length];
				// Ok that means it is a multiThreaded.
				for (int i = 0; i < ids.length; i++) {
					cthreads[i] = new Thread(process, ids[i]);
				}
			} else {
				// Provide a dummy.
				cthreads = new Thread[]{new Thread(process, 0)};
			}
			currentThreadId = info.getCurrentThread();
			//FIX: When attaching there is no thread selected
			// We will choose the first one as a workaround.
			if (currentThreadId == 0 && cthreads.length > 0) {
				currentThreadId = cthreads[0].getId();
			}
		} catch (MIException e) {
			throw new CDIException(e.getMessage());
		}
		return new ThreadSet(cthreads, currentThreadId);
	}

	public void update(Target target) throws CDIException {
	}

}
