/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.ExpressionManager;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.SignalManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;

/**
 */
public class ChangedEvent implements ICDIChangedEvent {

	Session session;
	ICDIObject source;

	public ChangedEvent(Session s, MIVarChangedEvent var) {
		session = s;

		// Try the Variable manager.
		VariableManager mgr = (VariableManager)session.getVariableManager();
		String varName = var.getVarName();
		MISession miSession = var.getMISession();
		source = mgr.getVariable(miSession, varName);

		// Try the Expression manager
		if (source == null) {
			ExpressionManager expMgr = (ExpressionManager)session.getExpressionManager();
			source = expMgr.getExpression(miSession, varName);
		}

		// Try the Register manager
		if (source == null) {
			RegisterManager regMgr = (RegisterManager)session.getRegisterManager();
			source = regMgr.getRegister(miSession, varName);
		}

		// Fall back
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public ChangedEvent(Session s, MIRegisterChangedEvent reg) {
		session = s;
		RegisterManager mgr = (RegisterManager)session.getRegisterManager();
		MISession miSession = reg.getMISession();
		int regno = reg.getNumber();
		source = mgr.getRegister(miSession, regno);
		if (source == null) {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public ChangedEvent(Session s, MIBreakpointChangedEvent bpoint) {
		session = s;
		BreakpointManager mgr = session.getBreakpointManager();
		MISession miSession = bpoint.getMISession();
		int number = bpoint.getNumber();
		ICDIBreakpoint breakpoint = mgr.getBreakpoint(miSession, number);
		if (breakpoint != null) {
			source = breakpoint;
		} else {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public ChangedEvent(Session s, MISharedLibChangedEvent slib) {
		session = s;
		SharedLibraryManager mgr = (SharedLibraryManager)session.getSharedLibraryManager();
		MISession miSession = slib.getMISession();
		String name = slib.getName();
		ICDISharedLibrary lib = mgr.getSharedLibrary(miSession, name);
		if (lib != null) {
			source = lib;
		} else {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public ChangedEvent(Session s, MISignalChangedEvent sig) {
		session = s;
		SignalManager mgr = (SignalManager)session.getSignalManager();
		MISession miSession = sig.getMISession();
		String name = sig.getName();
		ICDISignal signal = mgr.getSignal(miSession, name);
		if (signal != null) {
			source = signal;
		} else {
			Target target = session.getTarget(miSession);
			source = new CObject(target);
		}
	}

	public ChangedEvent(Session s, ICDIObject src) {
		session = s;
		source = src;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}
