/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.MemoryManager;
import org.eclipse.cdt.debug.mi.core.cdi.RegisterManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.VariableManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.CObject;
import org.eclipse.cdt.debug.mi.core.cdi.model.Register;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.cdi.model.Variable;
import org.eclipse.cdt.debug.mi.core.event.MIBreakpointCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIMemoryCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIRegisterCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIThreadCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MIVarCreatedEvent;

/**
 */
public class CreatedEvent implements ICDICreatedEvent {

	Session session;
	ICDIObject source;

	public CreatedEvent(Session s, MIBreakpointCreatedEvent bpoint) {
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

	public CreatedEvent(Session s, MIVarCreatedEvent var) {
		session = s;
		VariableManager mgr = (VariableManager)session.getVariableManager();
		String varName = var.getVarName();
		Variable variable = mgr.getVariable(varName);
		if (variable != null) {
			source = variable;
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public CreatedEvent(Session s, MIRegisterCreatedEvent var) {
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

	public CreatedEvent(Session s, MIThreadCreatedEvent ethread) {
		session = s;
		Target target = (Target)session.getCurrentTarget();
		ICDIThread thread = target.getThread(ethread.getId());
		if (thread != null) {
			source = thread;
		} else {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public CreatedEvent(Session s, MIMemoryCreatedEvent mblock) {
		session = s;
		MemoryManager mgr = (MemoryManager)session.getMemoryManager();
		ICDIMemoryBlock[] blocks = mgr.listMemoryBlocks();
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i].getStartAddress() == mblock.getAddress() &&
			    blocks[i].getLength() == mblock.getLength()) {
				source = blocks[i];
				break;
			}
		}
		if (source == null) {
			source = new CObject(session.getCurrentTarget());
		}
	}

	public CreatedEvent(Session s, MISharedLibCreatedEvent slib) {
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

	public CreatedEvent(Session s, ICDIObject src) {
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
