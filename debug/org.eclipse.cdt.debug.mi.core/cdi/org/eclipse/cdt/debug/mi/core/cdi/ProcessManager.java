/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class ProcessManager extends Manager {

	static final Target[] noProcess = new Target[0];

	HashMap processMap;

	class ProcessSet {
		Target[] currentProcs;
		int currentProcessId;
		ProcessSet(Target[] procs, int id) {
			currentProcs = procs;
			currentProcessId = id;
		}
	}

	public ProcessManager(Session session) {
		super(session, true);
		processMap = new HashMap();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIProcessManager#getProcesses()
	 */
	public Target[] getProcesses(Session session) throws CDIException {
		ProcessSet set =  (ProcessSet)processMap.get(session);
		if (set == null) {
			set = getProcessSet(session);
			processMap.put(session, set);
		}
		return set.currentProcs;
	}

	protected ProcessSet getProcessSet(Session session) throws CDIException {
		//Target[] cprocs = new Target[] {new Target((Session)session)};
		//return new ProcessSet(cprocs, 0);
		return null;
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.derug.core.cdi.ICDIManager#update()
	 */
	public void update() throws CDIException {
	}

}
