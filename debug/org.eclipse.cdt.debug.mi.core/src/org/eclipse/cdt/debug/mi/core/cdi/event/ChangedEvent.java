/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.ExpressionManager;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.SignalManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
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
		VariableManager mgr = (VariableManager)session.getVariableManager();
		String varName = var.getVarName();
		Variable variable = mgr.getVariable(varName);
		if (variable != null) {
			source = variable;
		} else {
			ExpressionManager expMgr = (ExpressionManager)session.getExpressionManager();
			variable = expMgr.getExpression(varName);
			if (variable != null) {
				source = variable;
			} else {
				source = new CObject(session.getCurrentTarget());
			}
		}
	}

	public ChangedEvent(Session s, MIRegisterChangedEvent var) {
		session = s;
		RegisterManager mgr = (RegisterManager)session.getRegisterManager();
		int regno = var.getNumber();
		Register reg = null;
		try {
			reg = mgr.getRegister(regno);
		} catch (CDIException e) {
		}
		if (reg != null) {
			source = reg;
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public ChangedEvent(Session s, MIBreakpointChangedEvent bpoint) {
		session = s;
		BreakpointManager mgr = (BreakpointManager)session.getBreakpointManager();
		int number = bpoint.getNumber();
		ICDIBreakpoint breakpoint = mgr.getBreakpoint(number);
		if (breakpoint != null) {
			source = breakpoint;
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public ChangedEvent(Session s, MISharedLibChangedEvent slib) {
		session = s;
		SharedLibraryManager mgr = (SharedLibraryManager)session.getSharedLibraryManager();
		String name = slib.getName();
		ICDISharedLibrary lib = mgr.getSharedLibrary(name);
		if (lib != null) {
			source = lib;
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public ChangedEvent(Session s, MISignalChangedEvent sig) {
		session = s;
		SignalManager mgr = (SignalManager)session.getSignalManager();
		String name = sig.getName();
		ICDISignal signal = mgr.getSignal(name);
		if (signal != null) {
			source = signal;
		} else {
			source = new CObject(session.getCurrentTarget());
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
