/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointDeletedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;

/**
 */
public class DestroyedEvent implements ICDIDestroyedEvent {

	Session session;
	ICDIObject source;
	
	public DestroyedEvent(Session s, MIThreadExitEvent ethread) {
		session = s;
		source = new Thread(session.getCurrentTarget(), ethread.getId());
	}

	public DestroyedEvent(Session s, MIVarChangedEvent var) {
		session = s;
		VariableManager mgr = (VariableManager)session.getVariableManager();
		String varName = var.getVarName();
		Variable variable = mgr.getVariable(varName);
		if (variable!= null) {
			source = variable;
			try {
				mgr.removeVariable(variable.getMIVar().getVarName());
			} catch (CDIException e) {
			}
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public DestroyedEvent(Session s, MIBreakpointDeletedEvent bpoint) {
		session = s;
		BreakpointManager mgr = (BreakpointManager)session.getBreakpointManager();
		int number = bpoint.getNumber();
		Breakpoint breakpoint = mgr.getBreakpoint(number);
		if (breakpoint != null) {
			source = breakpoint;
			mgr.deleteBreakpoint(number);
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public DestroyedEvent(Session s, MISharedLibUnloadedEvent slib) {
		session = s;
		SharedLibraryManager mgr = (SharedLibraryManager)session.getSharedLibraryManager();
		String name = slib.getName();
		ICDISharedLibrary lib = mgr.getSharedLibrary(name);
		if (lib != null) {
			mgr.deleteSharedLibrary(lib);
			source = lib;
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public DestroyedEvent(Session s, ICDIObject src) {
		session = s;
		source = src;
	}
	
	public DestroyedEvent(Session s) {
		session = s;
	}	
	
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
