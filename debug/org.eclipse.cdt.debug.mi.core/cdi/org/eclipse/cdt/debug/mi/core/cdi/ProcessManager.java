/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.Vector;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIInferiorExitEvent;

/**
 */
public class ProcessManager extends Manager {

	static final Target[] EMPTY_TARGETS = new Target[0];
	Vector debugTargetList;
	Target currentTarget;

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

	public void addTargets(Target[] targets, Target current) {
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
		if (current != null && debugTargetList.contains(current)) {
			currentTarget = current;
		}
		debugTargetList.trimToSize();
	}

	public void removeTargets(Target[] targets) {
		EventManager eventManager = (EventManager)getSession().getEventManager();
		for (int i = 0; i < targets.length; ++i) {
			Target target = targets[i];
			MISession miSession = target.getMISession();
			if (miSession != null) {
				miSession.notifyObservers(new MIInferiorExitEvent(miSession, 0));
				miSession.deleteObserver(eventManager);
			}
			if (currentTarget != null && currentTarget.equals(target)) {
				currentTarget = null;
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

	/**
	 * @deprecated
	 * @return
	 */
	public Target getCurrentTarget() {
		return currentTarget;
	}

	/**
	 * @deprecated
	 * @param current
	 */
	public void setCurrentTarget(Target current) {
		currentTarget = current;
	}
	/**
	 * @deprecated
	 * @see org.eclipse.cdt.derug.core.cdi.ICDIManager#update()
	 */
	public void update() throws CDIException {
	}

}
