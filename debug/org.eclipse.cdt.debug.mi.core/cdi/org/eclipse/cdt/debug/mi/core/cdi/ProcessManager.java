/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;

/**
 */
public class ProcessManager extends Manager { //implements ICDIProcessManager {

	static final ICDITarget[] noProcess = new Target[0];

	HashMap processMap;

	class ProcessSet {
		ICDITarget[] currentProcs;
		int currentProcessId;
		ProcessSet(ICDITarget[] procs, int id) {
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
	public ICDITarget[] getProcesses(ICDISession session) throws CDIException {
		ProcessSet set =  (ProcessSet)processMap.get(session);
		if (set == null) {
			set = getCProcesses(session);
			processMap.put(session, set);
		}
		return set.currentProcs;
	}

	public ProcessSet getCProcesses(ICDISession session) throws CDIException {
		ICDITarget[] cprocs = new Target[] {new Target((Session)session)};
		return new ProcessSet(cprocs, 0);
	}

	/**
	 * @see org.eclipse.cdt.derug.core.cdi.ICDIManager#update()
	 */
	public void update() throws CDIException {
	}

}
