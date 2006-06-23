/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.ExpressionManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Breakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.SharedLibrary;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Thread;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointDeletedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadExitEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarDeletedEvent;

/**
 */
public class DestroyedEvent implements ICDIDestroyedEvent {

	Session session;
	ICDIObject source;
	
	public DestroyedEvent(Session s, MIThreadExitEvent cthread) {
		session = s;
		Target target = session.getTarget(cthread.getMISession());
		source = new Thread(target, cthread.getId());
	}

	public DestroyedEvent(Session s, MIVarDeletedEvent var) {
		session = s;
		VariableManager varMgr = session.getVariableManager();
		MISession miSession = var.getMISession();
		String varName = var.getVarName();
		Variable variable = varMgr.removeVariableFromList(miSession, varName);
		if (variable != null) {
			source = variable;
		} else {
			ExpressionManager expMgr = session.getExpressionManager();
			variable = expMgr.removeVariableFromList(miSession, varName);
			if (variable != null) {
				source = variable;
			} else {
				Target target = session.getTarget(miSession);
				source = new CObject(target);
			}
		}
	}

	public DestroyedEvent(Session s, MIBreakpointDeletedEvent bpoint) {
		session = s;
		BreakpointManager mgr = session.getBreakpointManager();
		MISession miSession = bpoint.getMISession();
		int number = bpoint.getNumber();
		Breakpoint breakpoint = mgr.getBreakpoint(miSession, number);
		if (breakpoint != null) {
			source = breakpoint;
			mgr.deleteBreakpoint(miSession, number);
		} else {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public DestroyedEvent(Session s, MISharedLibUnloadedEvent slib) {
		session = s;
		SharedLibraryManager mgr = session.getSharedLibraryManager();
		MISession miSession = slib.getMISession();
		String name = slib.getName();
		SharedLibrary lib = mgr.getSharedLibrary(miSession, name);
		if (lib != null) {
			mgr.deleteSharedLibrary(miSession, lib);
			source = lib;
		} else {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
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
