/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointDeletedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;

/**
 */
public class DestroyedEvent implements ICDIDestroyedEvent {

	CSession session;
	ICDIObject source;
	
	public DestroyedEvent(CSession s, MIThreadExitEvent ethread) {
		session = s;
		source = new CThread(session.getCTarget(), ethread.getId());
	}

	public DestroyedEvent(CSession s, MIVarChangedEvent var) {
		session = s;
		VariableManager mgr = session.getVariableManager();
		String varName = var.getVarName();
		VariableManager.Element element = mgr.removeOutOfScope(varName);
		if (element != null && element.variable != null) {
			source = element.variable;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public DestroyedEvent(CSession s, MIBreakpointDeletedEvent bpoint) {
		session = s;
		BreakpointManager mgr = (BreakpointManager)session.getBreakpointManager();
		int number = bpoint.getNumber();
		Breakpoint breakpoint = mgr.deleteBreakpoint(number);
		if (breakpoint != null) {
			source = breakpoint;
		} else {
			source = new CObject(session.getCTarget());
		}
	}

	public DestroyedEvent(CSession s, ICDIObject src) {
		session = s;
		source = src;
	}
	
	public DestroyedEvent(CSession s) {
		session = s;
	}	
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
