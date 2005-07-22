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

import java.util.Vector;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorCreatedEvent;

/**
 */
public class ProcessManager extends Manager {

	static final Target[] EMPTY_TARGETS = new Target[0];
	Vector debugTargetList;

	public ProcessManager(Session session) {
		super(session, true);
		debugTargetList = new Vector(1);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIProcessManager#getProcesses()
	 */
	public Target[] getTargets() {
		return (Target[]) debugTargetList.toArray(new Target[debugTargetList.size()]);
	}

	public ICDITarget[] getCDITargets() {
		return (ICDITarget[]) debugTargetList.toArray(new ICDITarget[debugTargetList.size()]);
	}

	public void addTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			MISession miSession = target.getMISession();
			if (miSession != null) {
				miSession.addObserver(eventManager);
				miSession.fireEvent(new MIInferiorCreatedEvent(miSession, 0));
				if (!debugTargetList.contains(target)) {
					debugTargetList.add(target);
				}
			}
		}
		debugTargetList.trimToSize();
	}

	public void removeTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			MISession miSession = target.getMISession();
			if (miSession != null) {
				miSession.deleteObserver(eventManager);
			}
			debugTargetList.remove(target);
		}
		debugTargetList.trimToSize();
	}

	public Target getTarget(MISession miSession) {
		synchronized(debugTargetList) {
			for (int i = 0; i < debugTargetList.size(); ++i) {
				Target target = (Target)debugTargetList.get(i);
				MISession mi = target.getMISession();
				if (mi.equals(miSession)) {
					return target;
				}
			}
		}
		// ASSERT: it should not happen.
		return null;
	}

	public void update(Target target) throws CDIException {
	}

}
